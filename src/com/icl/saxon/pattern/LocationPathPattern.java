package com.icl.saxon.pattern;
import com.icl.saxon.expr.*;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.om.Axis;
import com.icl.saxon.expr.XPathException;

/**
* A LocationPathPattern represents a path, e.g. of the form A/B/C... The components are represented
* as a linked list, each component pointing to its predecessor
*/

public final class LocationPathPattern extends Pattern {

    // the following public variables are exposed to the ExpressionParser

    public Pattern parentPattern = null;
    public Pattern ancestorPattern = null;
    public NodeTest nodeTest = AnyNodeTest.getInstance();
    protected Expression[] filters = null;
    protected int numberOfFilters = 0;
    protected Expression equivalentExpr = null;
    protected boolean firstElementPattern = false;
    protected boolean lastElementPattern = false;
    protected boolean specialFilter = false;

    /**
    * Add a filter to the pattern (while under construction)
    * @param filter The predicate (a boolean expression or numeric expression) to be added
    */

    public void addFilter(Expression filter) {
    	if (filters==null) {
    		filters = new Expression[3];
    	} else if (numberOfFilters == filters.length) {
            Expression[] f2 = new Expression[numberOfFilters * 2];
            System.arraycopy(filters, 0, f2, 0, numberOfFilters);
            filters = f2;
        }
        filters[numberOfFilters++] = filter;
    }

    /**
    * Simplify the pattern: perform any context-independent optimisations
    */

    public Pattern simplify() throws XPathException {

        // detect the simple cases: no parent or ancestor pattern, no predicates

        if (    parentPattern == null &&
                ancestorPattern == null &&
                filters == null) {
            nodeTest.setStaticContext(getStaticContext());
            return nodeTest;
        }

        // simplify each component of the pattern

        if (parentPattern != null) parentPattern = parentPattern.simplify();
        if (ancestorPattern != null) ancestorPattern = ancestorPattern.simplify();
        if (filters != null) {
	        for (int i=numberOfFilters-1; i>=0; i--) {
	            Expression filter = filters[i].simplify();
	            filters[i] = filter;
	            // if the last filter is constant true, remove it
	            if ((filter instanceof BooleanValue) && (((Value)filter).asBoolean())) {
	                if (i==numberOfFilters-1) {
	                    numberOfFilters--;
	                } // otherwise don't bother doing anything with it.
	            }
	        }
	    }

        // see if it's an element pattern with a single positional predicate of [1]

        if (nodeTest.getNodeType() == NodeInfo.ELEMENT &&
                numberOfFilters==1 &&
                (filters[0] instanceof NumericValue) &&
                (int)((NumericValue)filters[0]).asNumber()==1 ) {
            firstElementPattern = true;
            specialFilter = true;
            numberOfFilters = 0;
            filters = null;
        }

        // see if it's an element pattern with a single positional predicate
        // of [position()=last()]

        if (nodeTest.getNodeType() == NodeInfo.ELEMENT &&
                numberOfFilters==1 &&
                filters[0] instanceof IsLastExpression &&
                ((IsLastExpression)filters[0]).getCondition()) {
            lastElementPattern = true;
            specialFilter = true;
            numberOfFilters = 0;
            filters = null;
        }

        if (isRelative()) {
            makeEquivalentExpression();
            specialFilter = true;
        }

        return this;
    }


    /**
    * For a positional pattern, make an equivalent nodeset expression to evaluate the filters
    */

    private void makeEquivalentExpression() throws XPathException {
        byte axis = (nodeTest.getNodeType()==NodeInfo.ATTRIBUTE ?
                        Axis.ATTRIBUTE :
                        Axis.CHILD );
        Step step = new Step(axis, nodeTest);
        step.setFilters(filters, numberOfFilters);
        equivalentExpr = new PathExpression(new ParentNodeExpression(), step);
    }

    /**
    * Determine whether the pattern matches a given node.
    * @param node the node to be tested
    * @return true if the pattern matches, else false
    */

    // diagnostic version of method
    public boolean matchesX(NodeInfo node, Context context) throws XPathException {
        System.err.println("Matching node " + node + " against LP pattern " + this);
        System.err.println("Node types " + node.getNodeType() + " / " + this.getNodeType());
        boolean b = matches(node, context);
        System.err.println((b ? "matches" : "no match"));
        return b;
    }

    public boolean matches(NodeInfo node, Context context) throws XPathException {

        if (!nodeTest.matches(node)) return false;

        if (parentPattern!=null) {
            NodeInfo par = node.getParent();
            if (par==null) return false;
            if (!(parentPattern.matches(par, context))) return false;
        }

        if (ancestorPattern!=null) {
            NodeInfo anc = node.getParent();
            while (true) {
                if (ancestorPattern.matches(anc, context)) break;
                anc = anc.getParent();
                if (anc==null) return false;
            }
        }

        if (specialFilter) {
            if (firstElementPattern) {
                NodeEnumeration enm = node.getEnumeration(Axis.PRECEDING_SIBLING, nodeTest);
                return !enm.hasMoreElements();
            }

            if (lastElementPattern) {
                NodeEnumeration enm = node.getEnumeration(Axis.FOLLOWING_SIBLING, nodeTest);
                return !enm.hasMoreElements();
            }

            if (equivalentExpr!=null) {

                // for a positional pattern, we do it the hard way: test whether the
                // node is a member of the nodeset obtained by evaluating the
                // equivalent expression

                Context c = context.newContext();
                c.setContextNode(node);
                c.setPosition(1);
                c.setLast(1);
                NodeEnumeration nsv = equivalentExpr.enumerate(c, false);
                while (nsv.hasMoreElements()) {
                	NodeInfo n = nsv.nextElement();
                	if (n.isSameNodeInfo(node)) {
                		return true;
                	}
                }
                return false;
            }
        }

        if (filters!=null) {
            Context c = context.newContext();
            c.setContextNode(node);
            c.setPosition(1);       // the filters aren't positional
            c.setLast(1);

            for (int i=0; i<numberOfFilters; i++) {
                if (!filters[i].evaluateAsBoolean(c)) return false;
            }
        }

        return true;
    }

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return Node.NODE
    * @return the type of node matched by this pattern. e.g. Node.ELEMENT or Node.TEXT
    */

    public short getNodeType() {
        return nodeTest.getNodeType();
    }

    /**
    * Determine the fingerprint of nodes to which this pattern applies.
    * Used for optimisation.
    * @return the fingerprint of nodes matched by this pattern.
    */

    public int getFingerprint() {
        return nodeTest.getFingerprint();
    }

    /**
    * Determine if the pattern uses positional filters
    * @return true if there is a numeric filter in the pattern, or one that uses the position()
    * or last() functions
    */

    public boolean isRelative() throws XPathException {
    	if (filters==null) return false;
        for (int i=0; i<numberOfFilters; i++) {
            int type = filters[i].getDataType();
            if (type==Value.NUMBER || type==Value.ANY) return true;
            if ((filters[i].getDependencies() &
            		 (Context.POSITION | Context.LAST)) != 0) return true;
        }
        return false;
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is
// Michael Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

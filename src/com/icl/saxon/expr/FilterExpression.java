package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.functions.Last;
import com.icl.saxon.functions.Position;

/**
* A FilterExpression contains a base expression and a filter predicate, which may be an
* integer expression (positional filter), or a boolean expression (qualifier)
*/

class FilterExpression extends NodeSetExpression {

    private Expression start;
    private Expression filter;
    private int dependencies = -1;

    /**
    * Constructor
    * @param start A node-set expression denoting the absolute or relative set of nodes from which the
    * navigation path should start.
    * @param filter An expression defining the filter predicate
    */

    public FilterExpression(Expression start, Expression filter) {
        this.start = start;
        this.filter = filter;
    }

    /**
    * Simplify an expression
    */

    public Expression simplify() throws XPathException {

        start = start.simplify();
        filter = filter.simplify();

        // ignore the filter if the base expression is an empty node-set
        if (start instanceof EmptyNodeSet) {
            return start;
        }

        // check whether the filter is a constant true() or false()
        if (filter instanceof Value && !(filter instanceof NumericValue)) {
            boolean f = ((Value)filter).asBoolean();
            if (f) {
                return start;
            } else {
                return new EmptyNodeSet();
            }
        }

        // check whether the filter is [last()] (note, position()=last() will
        // have already been simplified)

        if (filter instanceof Last) {
            filter = new IsLastExpression(true);
        }

        // following code is designed to catch the case where we recurse over a node-set
        // setting $ns := $ns[position()>1]. The effect is to combine the accumulating
        // filters, for example on the third iteration the filter will be effectively
        // x[position()>3] rather than x[position()>1][position()>1][position()>1].

        if (start instanceof NodeSetIntent &&
                filter instanceof PositionRange) {
            PositionRange pred = (PositionRange)filter;
            if (pred.getMinPosition()==2 && pred.getMaxPosition()==Integer.MAX_VALUE) {
                //System.err.println("Found candidate ");
                NodeSetIntent b = (NodeSetIntent)start;
                //System.err.println("Found candidate start is " + b.getNodeSetExpression().getClass());
                if (b.getNodeSetExpression() instanceof FilterExpression) {
                    FilterExpression t = (FilterExpression)b.getNodeSetExpression();
                    if (t.filter instanceof PositionRange) {
                        PositionRange pred2 = (PositionRange)t.filter;
                        if (pred2.getMaxPosition()==Integer.MAX_VALUE) {
                            //System.err.println("Opt!! start =" + pred2.getMinPosition() );
                            return new FilterExpression(t.start,
                                            new PositionRange(pred2.getMinPosition()+1, Integer.MAX_VALUE));
                        }
                    }
                }
            }
        }

        return this;
    }

    /**
    * Evaluate the filter expression in a given context to return a Node Enumeration
    * @param context the evaluation context
    * @param sort true if the result must be in document order
    */

    public NodeEnumeration enumerate(Context context, boolean sort) throws XPathException {

    	// if the expression references variables, or depends on other aspects
    	// of the XSLT context, then fix up these dependencies now. If the expression
    	// will only return nodes from the context document, then any dependency on
    	// the context document within the predicate can also be fixed up now.

    	int actualdep = getDependencies();
    	int removedep = 0;

    	if ((actualdep & Context.XSLT_CONTEXT) != 0) {
    	    removedep |= Context.XSLT_CONTEXT;
    	}

    	if (start.isContextDocumentNodeSet() && ((actualdep & Context.CONTEXT_DOCUMENT) != 0)) {
    	    removedep |= Context.CONTEXT_DOCUMENT;
    	}

    	if (removedep != 0) {
    		return reduce(removedep, context).enumerate(context, sort);
    	}

    	if (!sort) {
    		// the user didn't ask for document order, but we may need to do it anyway
	        if ( filter.getDataType()==Value.NUMBER ||
	             filter.getDataType()==Value.ANY ||
	             (filter.getDependencies() & (Context.POSITION|Context.LAST)) != 0 ) {
	            sort = true;
	        }
	    }

	    if (start instanceof SingletonNodeSet) {
	        if (!((SingletonNodeSet)start).isGeneralUseAllowed()) {
	            throw new XPathException("To use a result tree fragment in a filter expression, either use exsl:node-set() or specify version='1.1'");
            }
        }

        NodeEnumeration base = start.enumerate(context, sort);
        if (!base.hasMoreElements()) {
            return base;        // quick exit for an empty node set
        }

        return new FilterEnumerator(base, filter, context, false);
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        // not all dependencies in the filter expression matter, because the context node,
        // position, and size are not dependent on the outer context.
        if (dependencies==-1) {
        	dependencies = start.getDependencies() |
                (filter.getDependencies() & Context.XSLT_CONTEXT);
        }
        // System.err.println("Filter expression getDependencies() = " + dependencies);
        return dependencies;
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dep The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dep, Context context) throws XPathException {
        if ((dep & getDependencies()) != 0) {
            Expression newstart = start.reduce(dep, context);
            Expression newfilter = filter.reduce(dep & Context.XSLT_CONTEXT, context);
            Expression e = new FilterExpression(newstart, newfilter);
            e.setStaticContext(getStaticContext());
            return e.simplify();
        } else {
            return this;
        }
    }

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return start.isContextDocumentNodeSet();
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "filter");
        start.display(level+1);
        filter.display(level+1);
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

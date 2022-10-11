package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.sort.LocalOrderComparer;

/**
* An AxisExpression is always obtained by simplifying a PathExpression.
* It represents a PathExpression that starts at the context node, and uses
* a simple node-test with no filters. For example "*", "title", "./item",
* "@*", or "ancestor::chapter*".
*/

final class AxisExpression extends NodeSetExpression {

    private byte axis;
    private NodeTest test;
    private NodeInfo contextNode = null;

    /**
    * Constructor
    * @param start A node-set expression denoting the absolute or relative set of nodes from which the
    * navigation path should start.
    * @param step The step to be followed from each node in the start expression to yield a new
    * node-set
    */

    public AxisExpression(byte axis, NodeTest nodeTest) {
        this.axis = axis;
        this.test = nodeTest;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() {
        return this;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        if (contextNode==null) {
	        return Context.CONTEXT_NODE;
	    } else {
	        return 0;
	    }
    }

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return true;
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
        if (contextNode == null && (dep & Context.CONTEXT_NODE) != 0) {
            AxisExpression exp2 = new AxisExpression(axis, test);
            exp2.contextNode = context.getContextNodeInfo();
            return exp2;
        } else {
            return this;
        }
    }

    /**
    * Evaluate the path-expression in a given context to return a NodeSet
    * @param context the evaluation context
    * @param sort true if the returned nodes must be in document order
    */

    public NodeEnumeration enumerate(Context context, boolean sort) throws XPathException {
        NodeInfo start;
        if (contextNode==null) {
            start = context.getContextNodeInfo();
        } else {
            start = contextNode;
        }
        AxisEnumeration enm = start.getEnumeration(axis, test);
        if (sort && !enm.isSorted()) {
            NodeSetExtent ns = new NodeSetExtent(enm, LocalOrderComparer.getInstance());
            ns.sort();
            return ns.enumerate();
        }
        return enm;
    }

    /**
    * Evaluate the expression
    * (typically used if the result is to be stored in a variable)
    */

    public Value evaluate(Context context) throws XPathException {
        NodeSetIntent nsi = new NodeSetIntent(
                    (NodeSetExpression)reduce(Context.CONTEXT_NODE, context),
                    context.getController());
        nsi.setSorted(Axis.isForwards[axis]);
        return nsi;
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + Axis.axisName[axis] + "::" + test.toString());
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

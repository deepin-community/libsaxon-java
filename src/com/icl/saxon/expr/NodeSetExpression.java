package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.output.Outputter;

import javax.xml.transform.TransformerException;

/**
* A NodeSetExpression is any expression denoting a set of nodes. <BR>
* This is an abstract class, the methods are defaults which may be overridden in subclasses
*/


public abstract class NodeSetExpression extends Expression {

    /**
    * Return a node enumeration. All NodeSetExpressions must implement this method:
    * the evaluate() function is defined in terms of it. (But note that some expressions
    * that return node-sets are not NodeSetExpressions: for example functions such as
    * key(), id(), and document() are not, and neither are variable references).
    * @param context The evaluation context
    * @param sorted True if the nodes must be returned in document order
    */

    public abstract NodeEnumeration enumerate(Context context, boolean sorted) throws XPathException;

    /**
    * Evaluate this node-set. This doesn't actually retrieve all the nodes: it returns a wrapper
    * around a node-set expression in which all context dependencies have been eliminated.
    */

    public Value evaluate(Context context) throws XPathException {

        // lazy evaluation:
        // we eliminate all context dependencies, and save the resulting expression.

        Expression exp = reduce(Context.ALL_DEPENDENCIES, context);
        if (exp instanceof NodeSetValue) {
            return (Value)exp;
        } else if (exp instanceof NodeSetExpression) {
            return new NodeSetIntent((NodeSetExpression)exp, context.getController());
        } else {
            Value value = exp.evaluate(context);
            if (value instanceof NodeSetValue) {
                return value;
            } else {
                throw new XPathException("Value must be a node-set: it is a " + exp.getClass());
            }
        }
    }

    /**
    * Return the first node selected by this Expression when evaluated
    * in the current context
    * @param context The context for the evaluation
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context) throws XPathException {
        NodeEnumeration enm = enumerate(context, false);
        if (enm.isSorted()) {
            if (enm.hasMoreElements()) {
                return enm.nextElement();
            } else {
                return null;
            }
        } else {
            // avoid doing a sort:
            // just scan for the node that's first in document order
            Controller controller = context.getController();
            NodeInfo first = null;
            while (enm.hasMoreElements()) {
                NodeInfo next = enm.nextElement();
                if (first==null || controller.compare(next, first)<0) {
                    first = next;
                }
            }
            return first;
        }
    }

    /**
    * Evaluate as a string. Returns the string value of the first node
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return the value of the NodeSetExpression, evaluated in the current context
    */

    public String evaluateAsString(Context context) throws XPathException {
        NodeInfo e = selectFirst(context);
        if (e==null) return "";
        return e.getStringValue();
    }

    /**
    * Evaluate an expression as a String and write the result to the
    * specified outputter.<br>
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public void outputStringValue(Outputter out, Context context) throws TransformerException {
    	NodeInfo first = selectFirst(context);
        if (first!=null) {
        	first.copyStringValue(out);
        }
    }

    /**
    * Evaluate as a boolean. Returns true if there are any nodes
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public boolean evaluateAsBoolean(Context context) throws XPathException {
        return enumerate(context, false).hasMoreElements();
    }

    /**
    * Evaluate an expression as a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws XPathException {
        return (NodeSetValue)this.evaluate(context);
    }

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
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

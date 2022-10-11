package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;



/**
* A node set expression that will always return zero or one nodes
*/

public abstract class SingletonExpression extends NodeSetExpression {

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return true;
    }

    /**
    * Get the single node to which this expression refers
    */

    public abstract NodeInfo getNode(Context context) throws XPathException;

    /**
    * Return the first node selected by this Expression when evaluated in the current context
    * @param context The context for the evaluation
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context) throws XPathException {
        return getNode(context);
    }

    /**
    * Evaluate the expression in a given context to return a Node enumeration
    * @param context the evaluation context
    * @param sort Indicates result must be in document order
    */

    public NodeEnumeration enumerate(Context context, boolean sort) throws XPathException {
        return new SingletonEnumeration(getNode(context));
    }

    /**
    * Evaluate an expression as a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws XPathException {
        return new SingletonNodeSet(getNode(context));
    }

    /**
    * Evaluate as a string. Returns the string value of the node if it exists
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public String evaluateAsString(Context context) throws XPathException {
        NodeInfo node = getNode(context);
        if (node==null) return "";
        return node.getStringValue();
    }

    /**
    * Evaluate as a boolean. Returns true if there are any nodes
    * selected by the NodeSetExpression
    * @param context The context in which the expression is to be evaluated
    * @return true if there are any nodes selected by the NodeSetExpression
    */

    public boolean evaluateAsBoolean(Context context) throws XPathException {
        return getNode(context) != null;
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

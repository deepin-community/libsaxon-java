package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeEnumeration;

/**
* A NodeListExpression is an expression denoting a set of nodes sorted in document order. <BR>
*
* It is not possible to write a NodeListExpression directly using XPath; however a node set
* expression is treated as a NodeListExpression when it appears in certain contexts, specifically
* the select attribute of xsl:apply-templates or xsl:for-each.
*/


public class NodeListExpression extends NodeSetExpression {

    private Expression baseExpression;

    /**
    * Constructor
    * @param exp The expression that delivers the unsorted node-set
    */

    public NodeListExpression(Expression exp) {
        baseExpression = exp;
    }

    /**
    * Simplify the expression
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {
        baseExpression = baseExpression.simplify();
        if (baseExpression instanceof EmptyNodeSet) {
            return baseExpression;
        } else if (baseExpression instanceof SingletonNodeSet) {
            return baseExpression;
        } else {
            return this;
        }
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return baseExpression.getDependencies();
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws XPathException {
        if ((getDependencies() & dependencies) != 0) {
            return new NodeListExpression(baseExpression.reduce(dependencies, context));
        } else {
            return this;
        }
    }


    /**
    * Return an enumeration that returns the nodes in document order
    * @param c the evaluation context
    * @param sort: ignored, this class is used because the enumeration is always in
    * document order
    */

    public NodeEnumeration enumerate(Context c, boolean sort) throws XPathException {
        return baseExpression.enumerate(c, true);
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "NodeListExpression");
        baseExpression.display(level+1);
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

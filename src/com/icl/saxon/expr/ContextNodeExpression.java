package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;


/**
* This class represents the expression ".", which always returns the context node.
*/

public final class ContextNodeExpression extends SingletonExpression {

    /**
    * Return the node selected by this expression.
    * @param context The context for the evaluation
    * @return one NodeInfo only,
    * namely the current node defined by the context
    */

    public NodeInfo getNode(Context context) throws XPathException {
        return context.getContextNodeInfo();
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return Context.CONTEXT_NODE;
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
        if ((dependencies & Context.CONTEXT_NODE) != 0 ) {
            return new SingletonNodeSet(context.getContextNodeInfo());
        } else {
            return this;
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + ".");
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

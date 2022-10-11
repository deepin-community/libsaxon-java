package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeEnumeration;


/**
* An expression representing a nodeset that is a union of two other NodeSets
*/

class UnionExpression extends NodeSetExpression {

    // we could have implemented this as a subclass of BinaryExpression but we get more reuse
    // this way. A rare situation where multiple inheritance would have been nice

    protected Expression p1, p2;

    /**
    * Constructor
    * @param p1 the left-hand operand
    * @param p2 the right-hand operand
    */

    public UnionExpression(Expression p1, Expression p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {
        p1 = p1.simplify();
        p2 = p2.simplify();
        if (p1 instanceof EmptyNodeSet) return p2;
        if (p2 instanceof EmptyNodeSet) return p1;
        return this;
    }

    /**
    * Evaluate the union expression. The result will always be sorted in document order,
    * with duplicates eliminated
    * @param c The context for evaluation
    * @param sort Request the nodes in document order (they will be, regardless)
    * @return a NodeSetValue representing the union of the two operands
    */

    public NodeEnumeration enumerate(Context c, boolean sort) throws XPathException {
        return new UnionEnumeration(p1.enumerate(c, true),
                                    p2.enumerate(c, true),
                                    c.getController());
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return p1.getDependencies() | p2.getDependencies();
    }

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return p1.isContextDocumentNodeSet() && p2.isContextDocumentNodeSet();
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
        if ((getDependencies() & dependencies) != 0 ) {
            Expression e = new UnionExpression(
                            p1.reduce(dependencies, context),
                            p2.reduce(dependencies, context));
            e.setStaticContext(getStaticContext());
            return e;
        } else {
            return this;
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "union");
        p1.display(level+1);
        p2.display(level+1);
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

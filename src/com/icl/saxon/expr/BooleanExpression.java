package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.BooleanFn;


/**
* Boolean expression: two booleans combined using AND or OR
*/

class BooleanExpression extends BinaryExpression {

    public BooleanExpression(){};

    public BooleanExpression(Expression p1, int operator, Expression p2) {
        super(p1, operator, p2);
    }

    public Expression simplify() throws XPathException {
        p1 = p1.simplify();
        p2 = p2.simplify();
        if ((p1 instanceof Value) && (p2 instanceof Value)) {
            return evaluate(null);
        }

        // there's no useful saving to be made if only one argument is known,
        // evaluating it at runtime is just as fast.

        // compbine two PositionRanges

        if (p1 instanceof PositionRange && p2 instanceof PositionRange) {
            PositionRange pr1 = (PositionRange)p1;
            PositionRange pr2 = (PositionRange)p2;
            if (pr1.getMaxPosition()==Integer.MAX_VALUE && pr2.getMinPosition()==1) {
                return new PositionRange(pr1.getMinPosition(), pr2.getMaxPosition());
            }
            if (pr2.getMaxPosition()==Integer.MAX_VALUE && pr1.getMinPosition()==1) {
                return new PositionRange(pr2.getMinPosition(), pr1.getMaxPosition());
            }
        }
        return this;
    }

    public Value evaluate(Context c) throws XPathException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    public boolean evaluateAsBoolean(Context c) throws XPathException {
        switch(operator) {
            case Tokenizer.AND:
                return p1.evaluateAsBoolean(c) && p2.evaluateAsBoolean(c);
            case Tokenizer.OR:
                return p1.evaluateAsBoolean(c) || p2.evaluateAsBoolean(c);
            default:
                throw new XPathException("Unknown operator in boolean expression");
        }
    }

    /**
    * Determine the data type of the expression
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
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
            Expression e = new BooleanExpression(
                            p1.reduce(dependencies, context),
                            operator,
                            p2.reduce(dependencies, context));
            e.setStaticContext(getStaticContext());
            return e.simplify();
        } else {
            return this;
        }
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

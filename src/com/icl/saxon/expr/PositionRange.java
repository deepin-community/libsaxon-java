package com.icl.saxon.expr;
import com.icl.saxon.Context;


/**
* PositionRange: a boolean expression that tests whether the position() is
* within a certain range. This expression can occur in any context but it is
* optimized when it appears as a predicate (see FilterEnumerator)
*/

class PositionRange extends Expression {

    private int minPosition;
    private int maxPosition;

    /**
    * Create a position range
    */

    public PositionRange(int min, int max) {
        minPosition = min;
        maxPosition = max;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {
        return this;
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a BooleanValue representing the result of the numeric comparison of the two operands
    */

    public Value evaluate(Context c) throws XPathException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a boolean representing the result of the numeric comparison of the two operands
    */

    public boolean evaluateAsBoolean(Context c) throws XPathException {
        int p = c.getContextPosition();
        return p >= minPosition && p <= maxPosition;
    }

    /**
    * Determine the data type of the expression
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Get the dependencies
    */

    public int getDependencies() {
        return Context.POSITION;
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

        if ((Context.POSITION & dependencies) != 0 ) {
            return evaluate(context);
        }
        return this;
    }

    /**
    * Get the minimum position
    */

    protected int getMinPosition() {
        return minPosition;
    }

    /**
    * Get the maximum position
    */

    protected int getMaxPosition() {
        return maxPosition;
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "positionRange(" + minPosition + "," + maxPosition + ")");
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

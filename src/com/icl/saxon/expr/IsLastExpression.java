package com.icl.saxon.expr;
import com.icl.saxon.Context;


/**
* position()=last() expression
*/

public final class IsLastExpression extends Expression {

    private boolean condition;

    /**
    * Construct a condition that tests position()=last (if condition
    * is true) or position()!=last() (if condition is false).
    */

    public IsLastExpression(boolean condition){
        this.condition = condition;
    };

    public boolean getCondition() {
        return condition;
    }

    public Expression simplify() {
        return this;
    }

    public Value evaluate(Context c) throws XPathException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    public boolean evaluateAsBoolean(Context c) throws XPathException {
        return condition==c.isAtLast();
    }

    /**
    * Determine the data type of the expression
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Get the dependencies of this expression on the context
    */

    public int getDependencies() {
        return Context.POSITION | Context.LAST;
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
        if (((Context.LAST | Context.POSITION) & dependencies) != 0 ) {
            return new BooleanValue(context.isAtLast());
        } else {
            return this;
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "isLast()");
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

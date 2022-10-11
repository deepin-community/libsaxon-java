package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;

import java.util.*;
import java.lang.Math;
import java.text.*;



public class Round extends Function {


    /**
    * Function name (for diagnostics)
    */

    public String getName() {
        return "round";
    };

    /**
    * Determine the data type of the expression
    * @return Value.NUMBER
    */

    public int getDataType() {
        return Value.NUMBER;
    }

    /**
    * Simplify and validate.
    * This is a pure function so it can be simplified in advance if the arguments are known
    */

    public Expression simplify() throws XPathException {
        checkArgumentCount(1, 1);
        argument[0] = argument[0].simplify();
        if (argument[0] instanceof Value) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function in a numeric context
    */

    public double evaluateAsNumber(Context c) throws XPathException {
        return round(argument[0].evaluateAsNumber(c));
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new NumericValue(evaluateAsNumber(c));
    }

    /**
    * Determine the dependencies
    */

    public int getDependencies() {
        return argument[0].getDependencies();
    }

    /**
    * Reduce the dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        Round f = new Round();
        f.addArgument(argument[0].reduce(dep, c));
        f.setStaticContext(getStaticContext());
        return f.simplify();
    }

    /**
    * Here is the actual rounding algorithm
    */

    public static double round(double arg0) {
        if (Double.isNaN(arg0)) return arg0;
        if (Double.isInfinite(arg0)) return arg0;
        if (arg0==0.0) return arg0;    // handles the negative zero case
        if (arg0 > -0.5 && arg0 < 0.0) return -0.0;
        if (arg0 > Long.MIN_VALUE && arg0 < Long.MAX_VALUE) {
            return Math.round(arg0);
        }
        double fraction = arg0 % 1.0;
        if (fraction < 0.5) return arg0 - fraction;
        return arg0 - fraction + 1.0;

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

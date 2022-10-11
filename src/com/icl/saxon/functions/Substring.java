package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;

import java.util.*;
import java.lang.Math;

public class Substring extends Function {

    public String getName() {
        return "substring";
    };

    /**
    * Determine the data type of the expression
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Simplfy and validate
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(2,3);
        argument[0] = argument[0].simplify();
        argument[1] = argument[1].simplify();
        boolean fixed = (argument[0] instanceof Value) && (argument[1] instanceof Value);
        if (numArgs==3) {
            argument[2] = argument[2].simplify();
            fixed = fixed && (argument[2] instanceof Value);
        }
        if (fixed) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function in a string context
    */

    public String evaluateAsString(Context context) throws XPathException {

        String s = argument[0].evaluateAsString(context);
        double a = argument[1].evaluateAsNumber(context);

        if (getNumberOfArguments()==2) {
            return substring(s, a);
        } else {
            double b = argument[2].evaluateAsNumber(context);
            return substring(s, a, b);
        }
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new StringValue(evaluateAsString(c));
    }

    /**
    * Implement substring function. This follows the algorithm in the spec precisely.
    */

    private static String substring(String s, double start) {
        int slength = s.length();
        int estlength = slength - (int)start+1;
        if (estlength < 0) estlength = 1;
        if (estlength > slength) estlength = slength;
        StringBuffer sb = new StringBuffer(estlength);
        int pos=1;
        int cpos=0;
        double rstart = Round.round(start);

        while (cpos<slength) {
            if (pos >= rstart) {
                sb.append(s.charAt(cpos));
            }

            int ch = (int)s.charAt(cpos++);
            if (ch<55296 || ch>56319) pos++;    // don't count high surrogates, i.e. D800 to DBFF
        }
        return sb.toString();
    }

    /**
    * Implement substring function. This follows the algorithm in the spec precisely, except that
    * we exit the loop once we've exceeded the required length.
    */

    private static String substring(String s, double start, double len) {
        int slength = s.length();
        int estlength = (int)len;
        if (estlength < 0) estlength = 1;
        if (estlength > slength) estlength = slength;

        StringBuffer sb = new StringBuffer(estlength);
        int pos=1;
        int cpos=0;
        double rstart = Round.round(start);
        double rlen = Round.round(len);

        while (cpos<slength) {
            if (pos >= rstart) {
                if (pos < rstart + rlen) {
                    sb.append(s.charAt(cpos));
                } else {
                    break;
                }
            }

            int ch = (int)s.charAt(cpos++);
            if (ch<55296 || ch>56319) pos++;    // don't count high surrogates, i.e. D800 to DBFF
        }

        return sb.toString();
    }

    /**
    * Get dependencies
    */

    public int getDependencies() {
        int dep = argument[0].getDependencies() | argument[1].getDependencies();
        if (getNumberOfArguments()==3) {
            dep |= argument[2].getDependencies();
        }
        return dep;
    }

    /**
    * Remove dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        Substring f = new Substring();
        f.addArgument(argument[0].reduce(dep, c));
        f.addArgument(argument[1].reduce(dep, c));
        if (getNumberOfArguments()==3) {
            f.addArgument(argument[2].reduce(dep, c));
        }
        f.setStaticContext(getStaticContext());
        return f.simplify();
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

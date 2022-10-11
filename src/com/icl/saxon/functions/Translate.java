package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;

//import java.util.*;
//import java.lang.Math;
//import java.text.*;



public class Translate extends Function {

    public String getName() {
        return "translate";
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
        int numArgs = checkArgumentCount(3,3);
        argument[0] = argument[0].simplify();
        argument[1] = argument[1].simplify();
        argument[2] = argument[2].simplify();

        boolean fixed = (argument[0] instanceof Value) &&
                         (argument[1] instanceof Value) &&
                         (argument[2] instanceof Value);

        if (fixed) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function in a string context
    */

    public String evaluateAsString(Context context) throws XPathException {

        String s1 = argument[0].evaluateAsString(context);
        String s2 = argument[1].evaluateAsString(context);
        String s3 = argument[2].evaluateAsString(context);

        return translate(s1, s2, s3);
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new StringValue(evaluateAsString(c));
    }

    /**
    * Get dependencies
    */

    public int getDependencies() {
        return argument[0].getDependencies() |
                     argument[1].getDependencies() |
                     argument[2].getDependencies();
    }

    /**
    * Remove dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        Translate f = new Translate();
        f.addArgument(argument[0].reduce(dep, c));
        f.addArgument(argument[1].reduce(dep, c));
        f.addArgument(argument[2].reduce(dep, c));
        f.setStaticContext(getStaticContext());
        return f.simplify();
    }

    /**
    * Perform the translate function
    */

    private static String translate(String s0, String s1, String s2) {

        // check for surrogate pairs
        int len0 = StringValue.getLength(s0);
        int len1 = StringValue.getLength(s1);
        int len2 = StringValue.getLength(s2);
        if (s0.length()!=len0 ||
                s1.length()!=len1 ||
                s2.length()!=len2 ) {
            return slowTranslate(s0, s1, s2);
        }

        StringBuffer sb = new StringBuffer();
        int s2len = s2.length();
        for (int i=0; i<s0.length(); i++) {
            char c = s0.charAt(i);
            int j = s1.indexOf(c);
            if (j<s2len) {
                sb.append(( j<0 ? c : s2.charAt(j) ));
            }
        }
        return sb.toString();
    }

    /**
    * Perform the translate function when surrogate pairs are in use
    */

    private static String slowTranslate(String s0, String s1, String s2) {
        int[] a0 = StringValue.expand(s0);
        int[] a1 = StringValue.expand(s1);
        int[] a2 = StringValue.expand(s2);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<a0.length; i++) {
            int c = a0[i];
            int j = -1;
            for (int test=0; test<a1.length; test++) {
                if (a1[test]==c) {
                    j = test;
                    break;
                }
            }
            int newchar = -1;
            if (j<0) {
                newchar = a0[i];
            } else if (j<a2.length) {
                newchar = a2[j];
            } else {
                // no new character
            }

            if (newchar>=0) {
                if (newchar<65536) {
                    sb.append((char)newchar);
                }
                else {  // output a surrogate pair
                    //To compute the numeric value of the character corresponding to a surrogate
                    //pair, use this formula (all numbers are hex):
            	    //(FirstChar - D800) * 400 + (SecondChar - DC00) + 10000
                    newchar -= 65536;
                    sb.append((char)((newchar / 1024) + 55296));
                    sb.append((char)((newchar % 1024) + 56320));
                }
            }
        }
        return sb.toString();
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

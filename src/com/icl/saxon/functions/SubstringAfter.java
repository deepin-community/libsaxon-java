package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;

import java.util.*;



public class SubstringAfter extends Function {

    public String getName() {
        return "substring-after";
    };

    /**
    * Determine the data type of the expression
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Simplify and validate
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(2,2);
        argument[0] = argument[0].simplify();
        argument[1] = argument[1].simplify();
        boolean fixed = (argument[0] instanceof Value) && (argument[1] instanceof Value);

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
        String a = argument[1].evaluateAsString(context);
        return after(s, a);
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

    public int getDependencies()  {
        int dep = argument[0].getDependencies() | argument[1].getDependencies();
        return dep;
    }

    /**
    * Remove dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        SubstringAfter f = new SubstringAfter();
        f.addArgument(argument[0].reduce(dep, c));
        f.addArgument(argument[1].reduce(dep, c));
        f.setStaticContext(getStaticContext());
        return f.simplify();
    }

    /**
    * Return those characters in the input string s1 that come after the first appearance of
    * another string s2. If s2 is not present in s1, return the empty string.
    */

    private static String after(String s1, String s2) {
        int i = s1.indexOf(s2);
        if (i<0) return "";
        return s1.substring(i+s2.length());
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

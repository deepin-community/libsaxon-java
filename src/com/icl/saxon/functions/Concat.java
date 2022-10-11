package com.icl.saxon.functions;
import com.icl.saxon.Context;
import com.icl.saxon.expr.*;
//import java.lang.Math;
//import java.text.*;

public class Concat extends Function {

    /**
    * Function name (for diagnostics)
    */

    public String getName() {
        return "concat";
    };

    /**
    * Determine the data type of the expression
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Simplify and validate.
    * This is a pure function so it can be simplified in advance if the arguments are known
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(2, Integer.MAX_VALUE);
        boolean allKnown = true;
        for (int i=0; i<numArgs; i++) {
            argument[i] = argument[i].simplify();
            if (!(argument[i] instanceof Value)) {
                allKnown = false;
            }
        }
        if (allKnown) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function in a string context
    */

    public String evaluateAsString(Context c) throws XPathException {
        int numArgs = getNumberOfArguments();

        StringBuffer sb = new StringBuffer();
        for (int i=0; i<numArgs; i++) {
            sb.append(argument[i].evaluateAsString(c));
        }

        return sb.toString();
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new StringValue(evaluateAsString(c));
    }

    /**
    * Determine the dependencies
    */

    public int getDependencies() {
        int numArgs = getNumberOfArguments();
        int dep = 0;
        for (int i=0; i<numArgs; i++) {
            dep |= argument[i].getDependencies();
        }
        return dep;
    }

    /**
    * Reduce the dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        Concat f = new Concat();
        int numArgs = getNumberOfArguments();
        for (int i=0; i<numArgs; i++) {
            f.addArgument(argument[i].reduce(dep, c));
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

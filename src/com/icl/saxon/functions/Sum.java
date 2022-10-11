package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.expr.*;

import java.util.*;


public class Sum extends Function {

    /**
    * Function name (for diagnostics)
    */

    public String getName() {
        return "sum";
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
    */

    public Expression simplify() throws XPathException {
        checkArgumentCount(1, 1);
        argument[0] = argument[0].simplify();

        if (argument[0] instanceof Value) { // can't happen at present?
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function in a numeric context
    */

    public double evaluateAsNumber(Context c) throws XPathException {
        NodeEnumeration nsv = argument[0].enumerate(c, false);
        return total(nsv);
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
        Sum f = new Sum();
        f.addArgument(argument[0].reduce(dep, c));
        f.setStaticContext(getStaticContext());
        return f.simplify();
    }

    /**
    * Here is the algorithm
    */

    private static double total(NodeEnumeration enm) throws XPathException {

        double sum = 0.0;
        while (enm.hasMoreElements()) {
            String val = enm.nextElement().getStringValue();
            sum += Value.stringToNumber(val);
        }
        return sum;
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

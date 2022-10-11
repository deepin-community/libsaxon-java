package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;
import java.util.*;
import java.text.*;



public class StringFn extends Function {

    /**
    * Function name (for diagnostics)
    */

    public String getName() {
        return "string";
    };

    /**
    * Determine the data type of the expression
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Simplify and validate.
    * This is a pure function so it can be simplified in advance if the arguments are known
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(0, 1);
        if (numArgs==1) {
            argument[0] = argument[0].simplify();
            if (argument[0].getDataType()==Value.STRING) {
                return argument[0];
            }
            if (argument[0] instanceof Value) {
                return new StringValue(((Value)argument[0]).asString());
            }
        }
        return this;
    }

    /**
    * Evaluate the function in a string context
    */

    public String evaluateAsString(Context c) throws XPathException {
        if (getNumberOfArguments()==1) {
            return argument[0].evaluateAsString(c);
        } else {
            return (c.getContextNodeInfo()).getStringValue();
        }
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
        if (getNumberOfArguments()==1) {
            return argument[0].getDependencies();
        } else {
            return Context.CONTEXT_NODE;
        }
    }

    /**
    * Reduce the dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        if (getNumberOfArguments()==1) {
            StringFn f = new StringFn();
            f.addArgument(argument[0].reduce(dep, c));
            f.setStaticContext(getStaticContext());
            return f.simplify();
        } else {
            if ((dep & Context.CONTEXT_NODE) != 0) {
                return evaluate(c);
            } else {
                return this;
            }
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

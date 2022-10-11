package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;

//import java.util.*;
//import java.lang.Math;
import java.text.*;



public class FormatNumber extends Function {

    private DecimalFormat decimalFormat = new DecimalFormat();
    private String previousFormat = "[null]";
    private DecimalFormatSymbols previousDFS = null;
    private Controller boundController = null;

    public String getName() {
        return "format-number";
    };

    /**
    * Determine the data type of the exprEssion
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Simplify and validate
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(2, 3);

        // Note, even if all arguments are known we can't pre-evaluate the
        // function because we don't have access to the DecimalFormatManager

        argument[0] = argument[0].simplify();
        argument[1] = argument[1].simplify();
        if (numArgs==3) {
            argument[2] = argument[2].simplify();
        }
        return this;

    }

    /**
    * Evaluate in a context where a string is wanted
    */

    public String evaluateAsString(Context context) throws XPathException {
        int numArgs = getNumberOfArguments();

        Controller ctrl = boundController;
        if (ctrl==null) {
            ctrl = context.getController();
        }
        DecimalFormatManager dfm = ctrl.getDecimalFormatManager();
        DecimalFormatSymbols dfs;

        double number = argument[0].evaluateAsNumber(context);
        String format = argument[1].evaluateAsString(context);

        if (numArgs==2) {
            dfs = dfm.getDefaultDecimalFormat();
        } else {
            String df = argument[2].evaluateAsString(context);
            int dfnum = getStaticContext().getFingerprint(df, false);
            dfs = dfm.getNamedDecimalFormat(dfnum);
            if (dfs==null) {
                throw new XPathException(
                    "format-number function: decimal-format " + df + " not registered");
            }
        }
        return formatNumber(number, format, dfs);
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new StringValue(evaluateAsString(c));
    }

    /**
    * Here is the method that does the work. It needs to be synchronized because
    * it remembers information from one invocation to the next; it doesn't matter
    * if these are in different threads but it can't be interrupted. The reason for
    * remembering information is that getting a new DecimalFormatSymbols each time
    * is incredibly expensive, especially with the Microsoft Java VM. Actually
    * the synchronization is unnecessary if there is a bound Controller.
    */

    public synchronized String
            formatNumber(double n, String format, DecimalFormatSymbols dfs)
            throws XPathException {
        try {
            DecimalFormat df = decimalFormat;
            if (!(dfs==previousDFS && format.equals(previousFormat))) {
                df.setDecimalFormatSymbols(dfs);
                df.applyLocalizedPattern(format);
                previousDFS = dfs;
                previousFormat = format;
            }
            return df.format(n);
        } catch (Exception err) {
            throw new XPathException("Unable to interpret format pattern " + format + " (" + err + ")");
        }
    }

    /**
    * Determine the dependencies
    */

    public int getDependencies() {
        int dep = 0;
        if (boundController==null) {
            dep = Context.CONTROLLER;
        }
        dep |= argument[0].getDependencies();
        dep |= argument[1].getDependencies();
        if (getNumberOfArguments()==3) {
            dep |= argument[2].getDependencies();
        }
        return dep;
    }

    /**
    * Reduce the dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        FormatNumber f = new FormatNumber();
        f.addArgument(argument[0].reduce(dep, c));
        f.addArgument(argument[1].reduce(dep, c));
        if (getNumberOfArguments()==3) {
            f.addArgument(argument[2].reduce(dep, c));
        }
        if ((dep & Context.CONTROLLER) != 0) {
            f.boundController = c.getController();
        }
        f.setStaticContext(getStaticContext());
        return f;
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

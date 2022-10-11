package com.icl.saxon.expr;
import com.icl.saxon.*;


import java.util.*;


/**
* Abstract superclass for system-defined and user-defined functions
*/

public abstract class Function extends Expression {

    protected Expression[] argument = new Expression[6];
    private int numberOfArguments = 0;

    /**
    * Method to add an argument during function definition.
    */

    public void addArgument(Expression expr) {
        if (numberOfArguments>=argument.length) {
            Expression[] arg2 = new Expression[argument.length*2];
            System.arraycopy(argument, 0, arg2, 0, numberOfArguments);
            argument = arg2;
        }
        argument[numberOfArguments++] = expr;
    }

    /**
    * Determine the number of actual arguments supplied in the function call
    */

    public int getNumberOfArguments() {
        return numberOfArguments;
    }

    /**
    * Get the name of the function.
    * This method must be implemented in all subclasses.
    * @return the name of the function, as used in XSL expressions, but excluding
    * its namespace prefix
    */

    public abstract String getName();

    /**
    * Check number of arguments. <BR>
    * A convenience routine for use in subclasses.
    * @param min the minimum number of arguments allowed
    * @param max the maximum number of arguments allowed
    * @return the actual number of arguments
    * @throws XPathException if the number of arguments is out of range
    */

    protected int checkArgumentCount(int min, int max) throws XPathException {
        int numArgs = numberOfArguments;
        if (min==max && numArgs != min) {
            throw new XPathException("Function " + getName() + " must have " + min + pluralArguments(min));
        }
        if (numArgs < min) {
            throw new XPathException("Function " + getName() + " must have at least " + min + pluralArguments(min));
        }
        if (numArgs > max) {
            throw new XPathException("Function " + getName() + " must have no more than " + max + pluralArguments(max));
        }
        return numArgs;
    }

    /**
    * Utility routine used in constructing error messages
    */

    private String pluralArguments(int num) {
        if (num==1) return " argument";
        return " arguments";
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "function " + getName());
        for (int a=0; a<numberOfArguments; a++) {
            argument[a].display(level+1);
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

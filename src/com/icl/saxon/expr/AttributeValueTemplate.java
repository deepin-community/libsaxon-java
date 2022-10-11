package com.icl.saxon.expr;
import com.icl.saxon.Context;


/**
* This class represents an attribute value template. Although it is not technically
* an expression in the sense of XSL syntax, we model it as an expression for inheritance
* purposes.
*/

public final class AttributeValueTemplate extends Expression  {

    private Expression[] components = new Expression[10];
    private int numberOfComponents;

    /**
    * Constructor to make an AVT from a list of components. The components are the elements
    * of the AVT, e.g. abc{def}geh has three components, String "abc", expression "def", and
    * String "geh"
    */

    private AttributeValueTemplate(Expression[] components, int numberOfComponents) {
        this.components = new Expression[numberOfComponents];
        this.numberOfComponents = numberOfComponents;
        System.arraycopy(components, 0, this.components, 0, numberOfComponents);
    }

    /**
    * Static factory method to create an AVT from an XSL string representation
    */

    public static Expression make(String avt, StaticContext env) throws XPathException {

        if ( avt.indexOf("{") < 0 && avt.indexOf("}") < 0) {        // fast path: no embedded expressions
            return new StringValue(avt);

        } else {
            int nr = 0;
            Expression[] components = new Expression[avt.length()];     // for once, we'll set a limit!
                                             // process embedded expressions within the avt
            int i0, i1, i2, i8, i9, len, last;
            char inquote = ' ';
            last = 0;
            len = avt.length();
            while (last<len) {
                i0 = avt.indexOf("{", last);
                i1 = avt.indexOf("{{", last);
                i8 = avt.indexOf("}", last);
                i9 = avt.indexOf("}}", last);
                //System.err.println("AVT:" + avt + ": " + i0 + "," + i1 + "," + i8 + "," + i9);

                if (i8>=0 && (i0<0 || i8<i0)) {             // found a "}"
                    if (i8 != i9) {                        // a "}" that isn't a "}}"
                        throw new XPathException("Closing curly brace in attribute value template \"" + avt + "\" must be doubled");
                    }
                    components[nr++] = new StringValue(avt.substring(last, i8+1));
                    last = i8+2;
                } else if (i1>=0 && i1==i0) {              // found a doubled "{{"
                    components[nr++] = new StringValue(avt.substring(last, i1+1));
                    last = i1+2;
                } else if (i0>=0) {                        // found a single "{"
                    if (i0>last) {
                        components[nr++] = new StringValue(avt.substring(last, i0));
                    }
                    for (i2=i0+1; i2<len; i2++) {
                        if (avt.charAt(i2)=='\"') inquote = '\"';
                        if (avt.charAt(i2)=='\'') inquote = '\'';
                        if (inquote!=' ') {
                            i2++;
                            while (i2<len && avt.charAt(i2)!=inquote) i2++;
                            inquote = ' ';
                        } else {
                            if (avt.charAt(i2)=='}') {
                               // we're in an expression so we don't need to check for a doubled "}}"
                               break;
                            }
                        }
                    }
                    if (i2>=len) {
                        throw new XPathException("No closing \"}\" in attribute value template " + avt);
                    }

                    String expr = avt.substring(i0+1, i2);
                    Expression ex = Expression.make(expr, env);
                    components[nr++] = ex;
                    last=i2+1;
                } else {                // didn't find anything
                    components[nr++] = new StringValue(avt.substring(last));
                    last=len;
                }
            }
            return (new AttributeValueTemplate(components, nr)).simplify();
        }
    }

    /**
    * Simplify an expression.
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {

        // is it empty?
        if (numberOfComponents==0) {
            return new StringValue("");
        }

        // if there's only one component, return that
        if (numberOfComponents==1) {
            return components[0];
        }

        // otherwise, simplify each of the components
        for (int i=0; i<numberOfComponents; i++) {
            components[i] = components[i].simplify();
        }

        // TODO: see if adjacent components can be merged.
        return this;
    }

    /**
    * Evaluate an AVT.
    * @param context The context in which the AVT is to be evaluated
    * @return the value of the AVT, evaluated in the current context
    */

    public Value evaluate(Context context) throws XPathException {
        return new StringValue(evaluateAsString(context));
    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Evaluate an expression as a String.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public String evaluateAsString(Context context) throws XPathException {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<numberOfComponents; i++) {
            sb.append(components[i].evaluateAsString(context));
        }
        return sb.toString();
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = 0;
        for (int i=0; i<numberOfComponents; i++) {
            dep |= components[i].getDependencies();
        }
        return dep;
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
        throw new XPathException("Cannot reduce expressions in an attribute value template");
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "{<AVT>}");
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

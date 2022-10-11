package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.style.SAXONFunction;
import com.icl.saxon.om.NodeInfo;
import javax.xml.transform.TransformerException;


import java.util.*;


/**
* This class represents a call to a function defined in the stylesheet
*/

public class StyleSheetFunctionCall extends Function {

    private SAXONFunction function;
    private Controller boundController = null;
    private NodeInfo boundContextNode = null;
    private int boundContextPosition = -1;
    private int boundContextSize = -1;

    /**
    * Create the reference to the saxon:function element
    */

    public void setFunction(SAXONFunction f) {
        function = f;
    }

    /**
    * Get the name of the function.
    * @return the name of the function, as used in XSL expressions, but excluding
    * its namespace prefix
    */

    public String getName() {
        return function.getAttribute("name");   // not quite as specified
    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.ANY;
    }


    /**
    * Simplify the function call
    */

    public Expression simplify() throws XPathException {
        for (int i=0; i<getNumberOfArguments(); i++) {
            argument[i] = argument[i].simplify();
        }
        return this;
    }


    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {

        // we could do better than this by examining the XSLT code

        int dep = Context.NO_DEPENDENCIES;
        if (boundController==null) dep |= Context.CONTROLLER;
        if (boundContextNode==null) dep |= Context.CONTEXT_NODE;
        if (boundContextPosition==-1) dep |= Context.POSITION;
        if (boundContextSize==-1) dep |= Context.LAST;

        for (int i=0; i<getNumberOfArguments(); i++) {
            dep |= argument[i].getDependencies();
        }
        return dep;

    }

    /**
    * Remove dependencies.
    */

    public Expression reduce(int dependencies, Context context)
            throws XPathException {

        StyleSheetFunctionCall nf = new StyleSheetFunctionCall();
        nf.setFunction(function);
        nf.setStaticContext(getStaticContext());
        nf.boundController = boundController;
        nf.boundContextNode = boundContextNode;
        nf.boundContextPosition = boundContextPosition;
        nf.boundContextSize = boundContextSize;

        for (int a=0; a<getNumberOfArguments(); a++) {
            nf.addArgument(argument[a].reduce(dependencies, context));
        }

        if (boundController==null && (dependencies & Context.CONTROLLER) != 0) {
            nf.boundController = context.getController();
        }
        if (boundContextNode==null && (dependencies & Context.CONTEXT_NODE) != 0) {
            nf.boundContextNode = context.getContextNodeInfo();
        }
        if (boundContextPosition==-1 && (dependencies & Context.POSITION) != 0) {
            nf.boundContextPosition = context.getContextPosition();
        }
        if (boundContextSize==-1 && (dependencies & Context.LAST) != 0) {
            nf.boundContextSize = context.getLast();
        }

        return nf.simplify();
    }

    /**
    * Evaluate the function
    * @param context The context in which the function is to be evaluated
    * @return a Value representing the result of the function. This must be of the data type
    * corresponding to the result of getType().
    * @throws XPathException if the function cannot be evaluated.
    */

    public Value evaluate(Context c) throws XPathException {
        Context context = c.newContext();
        if (boundController!=null) {
            context.setController(boundController);
        }
        if (boundContextNode!=null) {
            context.setCurrentNode(boundContextNode);
            context.setContextNode(boundContextNode);
        }
        if (boundContextPosition!=-1) {
            context.setPosition(boundContextPosition);
        }
        if (boundContextSize!=-1) {
            context.setLast(boundContextSize);
        }

        ParameterSet ps = new ParameterSet();
        for (int i=0; i<getNumberOfArguments(); i++) {
            int param = function.getNthParameter(i);
            if (param==-1) {
                throw new XPathException("Too many arguments");
            }
            ps.put(param, argument[i].evaluate(c));
        }
        try {
            return function.call(ps, context);
        } catch (TransformerException err) {
            throw new XPathException(err);
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

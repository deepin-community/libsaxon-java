package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.output.Outputter;

import javax.xml.transform.TransformerException;


/**
* This class serves two purposes: it is an abstract superclass for different kinds of XPath expression,
* and it contains a static method to invoke the expression parser
*/

public abstract class Expression  {

    protected StaticContext staticContext;

    /**
    * Parse an expression
    * @param expression The expression (as a character string)
    * @param env An object giving information about the compile-time context of the expression
    * @return an object of type Expression
    */

    public static Expression make(String expression, StaticContext env) throws XPathException {
        try {
            Expression exp = (new ExpressionParser()).parse(expression, env).simplify();
            exp.staticContext = env;
            return exp;
        } catch (XPathException err) {
            if (env.forwardsCompatibleModeIsEnabled()) {
                return new ErrorExpression(err);
            } else {
                throw err;
            }
        }
    }

    /**
    * Simplify an expression. Default implementation does nothing.
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {
        return this;
    };

    /**
    * Set the static context used when the expression was parsed
    */

    public final void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Determine the static context used when the expression was parsed
    */

    public final StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Determine whether the expression contains any references to variables
    * @return true if so
    */

    public boolean containsReferences() throws XPathException {
        return (getDependencies() & Context.VARIABLES) != 0;
    }

    /**
    * Evaluate an expression.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public abstract Value evaluate(Context context) throws XPathException;

    /**
    * Evaluate an expression as a Boolean.<br>
    * The result of x.evaluateAsBoolean(c) must be equivalent to x.evaluate(c).asBoolean();
    * but optimisations are possible when it is known that a boolean result is required,
    * especially in the case of a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public boolean evaluateAsBoolean(Context context) throws XPathException {
        return evaluate(context).asBoolean();
    }

    /**
    * Evaluate an expression as a Number.<br>
    * The result of x.evaluateAsNumber(c) must be equivalent to x.evaluate(c).asNumber();
    * but optimisations are possible when it is known that a numeric result is required,
    * especially in the case of a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public double evaluateAsNumber(Context context) throws XPathException {
        return evaluate(context).asNumber();
    }

    /**
    * Evaluate an expression as a String.<br>
    * The result of x.evaluateAsString(c) must be equivalent to x.evaluate(c).asString();
    * but optimisations are possible when it is known that a string result is required,
    * especially in the case of a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public String evaluateAsString(Context context) throws XPathException {
        return evaluate(context).asString();
    }

    /**
    * Evaluate an expression as a String and write the result to the
    * specified outputter.<br>
    * @param out The required outputter
    * @param context The context in which the expression is to be evaluated
    */

    public void outputStringValue(Outputter out, Context context) throws TransformerException {
        out.writeContent(evaluateAsString(context));
    }

    /**
    * Evaluate an expression as a NodeSet.<br>
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context. Note that
    * the result is not necessarily in document order; to get it into document order,
    * call sort() on the result.
    * @throws XPathException when the expression does not return a nodeset.
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws XPathException {
        // Default implementation: see also NodeSetExpression
        Value val = evaluate(context);
        if (val instanceof NodeSetValue)
            return ((NodeSetValue)val);
        throw new XPathException("The value is not a node-set");
    }

    /**
    * Return an enumeration of nodes in a nodeset.
    * @param context The context in which the expression is to be evaluated
    * @param sorted Indicates whether the nodes are required in document order. If
    * this is false, they may come in any order, but there will be no duplicates.
    * @throws XPathException when the expression does not return a nodeset.
    */

    public NodeEnumeration enumerate(Context context, boolean sorted) throws XPathException {
        // default implementation: see also NodeSetExpression
        Value val = evaluate(context);
        if (val instanceof NodeSetValue) {
            if (sorted) {
                ((NodeSetValue)val).sort();
            }
            NodeEnumeration z = ((NodeSetValue)val).enumerate();
            return z;
        }
        throw new XPathException("The value is not a node-set");
    }

    /**
    * Determine the data type of the expression, if possible
    * @return one of the values Value.STRING, Value.BOOLEAN, Value.NUMBER, Value.NODESET,
    * Value.FRAGMENT, or Value.ANY (meaning not known in advance)
    */

	public abstract int getDataType();

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return false;
    }

    /**
    * Determine whether the expression uses the current() function. This is an error if the
    * expression is within a pattern
    */

    public boolean usesCurrent() {
        return (getDependencies() & Context.CURRENT_NODE) != 0;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public abstract int getDependencies();

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed, e.g. Context.VARIABLES
    * @param context The context to be used for the partial evaluation
    * @return a new expression (or Value) that does not have any of the specified dependencies
    */

    public abstract Expression reduce(int dependencies, Context context) throws XPathException;

    /**
    * Diagnostic print of expression structure
    */

    public abstract void display(int level);

    /**
    * Construct indent string, for diagnostic output
    */

    protected static String indent(int level) {
        String s = "";
        for (int i=0; i<level; i++) {
            s += "  ";
        }
        return s;
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
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved.
//
// Contributor(s): Michael Kay, Peter Bryant
//

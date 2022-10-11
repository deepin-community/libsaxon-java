package com.icl.saxon.expr;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.Context;


/**
* NodeSetComparison: A Relational Expression that compares a node-set with a string
* or numeric value for equals, not-equals, greater-than or less-than.
*/

public class NodeSetComparison extends Expression {

    NodeSetExpression nodeset;
    int operator;
    Value value;

    public NodeSetComparison(NodeSetExpression p1, int op, Value p2) {
        nodeset = p1;
        operator = op;
        value = p2;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() {
        return this;
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a BooleanValue representing the result of the comparison of the two operands
    */

    public Value evaluate(Context c) throws XPathException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a boolean representing the result of the numeric comparison of the two operands
    */

    public boolean evaluateAsBoolean(Context c) throws XPathException {
        NodeEnumeration enm = nodeset.enumerate(c, false);
        switch (operator) {
            case Tokenizer.EQUALS:
                if (value.getDataType() == value.NUMBER) {
                    double n1 = value.asNumber();
                    while (enm.hasMoreElements()) {
                        NodeInfo node = enm.nextElement();
                        if (Value.stringToNumber(node.getStringValue()) == n1) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    String s1 = value.asString();
                    while (enm.hasMoreElements()) {
                        NodeInfo node = enm.nextElement();
                        if (node.getStringValue().equals(s1)) {
                            return true;
                        }
                    }
                    return false;
                }

            case Tokenizer.NE:
                if (value.getDataType() == value.NUMBER) {
                    double n2 = value.asNumber();
                    while (enm.hasMoreElements()) {
                        NodeInfo node = enm.nextElement();
                        if (Value.stringToNumber(node.getStringValue()) != n2) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    String s2 = value.asString();
                    while (enm.hasMoreElements()) {
                        NodeInfo node = enm.nextElement();
                        if (!node.getStringValue().equals(s2)) {
                            return true;
                        }
                    }
                    return false;
                }

            case Tokenizer.LE:
                double n1 = value.asNumber();
                while (enm.hasMoreElements()) {
                    NodeInfo node = enm.nextElement();
                    if (Value.stringToNumber(node.getStringValue()) <= n1) {
                        return true;
                    }
                }
                return false;

            case Tokenizer.LT:
                double n2 = value.asNumber();
                while (enm.hasMoreElements()) {
                    NodeInfo node = enm.nextElement();
                    if (Value.stringToNumber(node.getStringValue()) < n2) {
                        return true;
                    }
                }
                return false;

            case Tokenizer.GE:
                double n3 = value.asNumber();
                while (enm.hasMoreElements()) {
                    NodeInfo node = enm.nextElement();
                    if (Value.stringToNumber(node.getStringValue()) >= n3) {
                        return true;
                    }
                }
                return false;

            case Tokenizer.GT:
                double n4 = value.asNumber();
                while (enm.hasMoreElements()) {
                    NodeInfo node = enm.nextElement();
                    if (Value.stringToNumber(node.getStringValue()) > n4) {
                        return true;
                    }
                }
                return false;

            default:
                return false;
        }


    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return nodeset.getDependencies();
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
        if ((nodeset.getDependencies() & dependencies) != 0 ) {
            Expression e = nodeset.reduce(dependencies, context);
            if (e instanceof SingletonExpression) {
                e = new SingletonComparison(
                                (SingletonExpression)e,
                                operator,
                                value);
                e.setStaticContext(getStaticContext());
                return e.simplify();
            } else if (e instanceof NodeSetExpression) {
                e = new NodeSetComparison(
                                (NodeSetExpression)e,
                                operator,
                                value);
                e.setStaticContext(getStaticContext());
                return e.simplify();
            } else if (e instanceof NodeSetValue) {
                return new BooleanValue(((NodeSetValue)e).compare(operator, value));
            } else {
                throw new XPathException("Failed to reduce NodeSetComparison: returned " + e.getClass());
            }
        } else {
            return this;
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + Tokenizer.tokens[operator]);
        nodeset.display(level+1);
        value.display(level+1);
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

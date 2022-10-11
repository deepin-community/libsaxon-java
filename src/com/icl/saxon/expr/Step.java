package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.functions.*;
import com.icl.saxon.pattern.NodeTest;

import java.util.*;

/**
* A step in a path expression
*/

public final class Step {

    private byte axis;
    private NodeTest test;

    private Expression[] filters = new Expression[3];      // list of filter expressions to apply
    private int numberOfFilters = 0;

    public Step(byte axis, NodeTest nodeTest) {
        this.axis = axis;
        this.test = nodeTest;
    }

    public Step addFilter(Expression exp) {
        if (numberOfFilters == filters.length) {
            Expression[] f2 = new Expression[numberOfFilters * 2];
            System.arraycopy(filters, 0, f2, 0, numberOfFilters);
            filters = f2;
        }
        filters[numberOfFilters++] = exp;
        return this;
    }

    public void setFilters(Expression[] filters, int count) {
        this.filters = filters;
        this.numberOfFilters = count;
    }

    public byte getAxis() {
        return axis;
    }

    public NodeTest getNodeTest() {
        return test;
    }

    public Expression[] getFilters() {
        return filters;
    }

    public int getNumberOfFilters() {
        return numberOfFilters;
    }

    /**
    * Simplify the step. Return either the same step after simplification, or null,
    * indicating that the step will always give an empty result.
    */

    public Step simplify() throws XPathException {

        for (int i=numberOfFilters-1; i>=0; i--) {
            Expression exp = filters[i].simplify();
            filters[i] = exp;

            // look for a filter that is constant true or false (which can arise after
            // an expression is reduced).

            if (exp instanceof Value && !(exp instanceof NumericValue)) {
                if (((Value)exp).asBoolean()) {         // filter is constant true
                    // only bother removing it if it's the last
                    if (i==numberOfFilters-1) {
                        numberOfFilters--;
                    }
                } else {                                // filter is constant false,
                                                        // so the wbole path-expression is empty
                    return null;
                }
            }

            // look for the filter [last()]

            if (exp instanceof Last) {
                filters[i] = new IsLastExpression(true);
            }

        }
        return this;
    }

    /**
    * Enumerate this step.
    * @param node: The node from which we want to make the step
    * @param context: The context for evaluation. Affects the result of positional
    * filters
    * @return: an enumeration of nodes that result from applying this step
    */

    public NodeEnumeration enumerate(NodeInfo node, Context context)
        throws XPathException {

        NodeEnumeration enm = node.getEnumeration(axis, test);
        if (enm.hasMoreElements()) {       // if there are no nodes, there's nothing to filter
            for (int i=0; i<numberOfFilters; i++) {
                enm = new FilterEnumerator(enm, filters[i],
                                             context, false);
            }
        }
        return enm;

    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(Expression.indent(level) + "Step " + Axis.axisName[axis] + "::" + test.toString() +
            (numberOfFilters > 0 ? " [" : ""));
        for (int f=0; f<numberOfFilters; f++) {
            filters[f].display(level+1);
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

package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.sort.*;
import com.icl.saxon.om.NodeEnumeration;
//import java.util.*;

/**
* A NodeSetExpression that retrieves nodes in order according to a specified sort key. <br>
* Note there is no direct XSL expression syntax that generates this. <br>
* The expression sorts a base NodeSet according to the value of a specified
* sort key. The sort key may be composite. The base NodeSet will always be in document
* order.
*/

public class SortedSelection extends NodeSetExpression {

    private Expression selection;
    private SortKeyDefinition[] sortkeys;
                                           // in major-to-minor order
    private int numberOfSortKeys;

    /**
    * Constructor
    * @param s An expression whose value is the base nodeset to be sorted
    * @param k the number of sort keys
    */

    public SortedSelection(Expression s, int k) {
        selection = s;
        sortkeys = new SortKeyDefinition[k];
        numberOfSortKeys = k;
    }

    /**
    * Add a sort key and other sorting parameters
    * @param sk A SortKeyDefinition
    * @param k The index of this SortKeyDefinition. The first sort key in major-to-minor
    * order is numbered 0 (zero), the others are 1, 2, ... in sequence.
    * @throws ArrayIndexOutOfBoundsException if the sort key index is out of range,
    * according to the number of sort keys defined when the SortedSelection was
    * initialized.
    */

    public void setSortKey(SortKeyDefinition sk, int k) {
        sortkeys[k] = sk;
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {

        // simplify the base expression and the sort keys
        selection = selection.simplify();
        for (int i=0; i<numberOfSortKeys; i++) {
            SortKeyDefinition sk = sortkeys[i];
            sk.setSortKey(sk.getSortKey().simplify());
            sk.setOrder(sk.getOrder().simplify());
            sk.setDataType(sk.getDataType().simplify());
            sk.setCaseOrder(sk.getCaseOrder().simplify());
            sk.setLanguage(sk.getLanguage().simplify());
        }
        return this;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = selection.getDependencies();
        for (int i=0; i<sortkeys.length; i++) {
            SortKeyDefinition sk = sortkeys[i];
            // Not all dependencies in the sort key matter, because the context node, etc,
            // are not dependent on the outer context
            dep |= (sk.getSortKey().getDependencies() &
                        (Context.VARIABLES | Context.CONTROLLER));
            dep |= sk.getOrder().getDependencies();
            dep |= sk.getDataType().getDependencies();
            dep |= sk.getCaseOrder().getDependencies();
            dep |= sk.getLanguage().getDependencies();
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
        if ((dependencies & getDependencies()) != 0) {
            Expression newselection = selection.reduce(dependencies, context);
            SortedSelection newss = new SortedSelection(newselection, numberOfSortKeys);
            newss.setStaticContext(getStaticContext());
            for (int i=0; i<numberOfSortKeys; i++) {
                SortKeyDefinition sk = sortkeys[i];
                SortKeyDefinition sknew = new SortKeyDefinition();
                sknew.setStaticContext(getStaticContext());
                sknew.setSortKey(
                    sk.getSortKey().reduce(
                        dependencies & (Context.VARIABLES | Context.CONTROLLER),
                        context));
                sknew.setOrder(sk.getOrder().reduce(dependencies, context));
                sknew.setDataType(sk.getDataType().reduce(dependencies, context));
                sknew.setCaseOrder(sk.getCaseOrder().reduce(dependencies, context));
                sknew.setLanguage(sk.getLanguage().reduce(dependencies, context));
                newss.setSortKey(sknew, i);
            }
            return newss.simplify();
        } else {
            return this;
        }
    }

    /**
    * Evaluate the expression by sorting the base nodeset using the supplied key.
    * @param context The context for the evaluation
    * @param sort: must be false (because document order would be meaningless)
    * @return the sorted nodeset
    */

    public NodeEnumeration enumerate(Context context, boolean sort) throws XPathException
    {
        if (sort==true) {
            throw new XPathException("SortedSelection doesn't provide nodes in document order");
        }
        NodeEnumeration base = selection.enumerate(context, true);
        return new SortKeyEnumeration(context, base, sortkeys);
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "sorted");
        selection.display(level+1);
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

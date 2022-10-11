package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;

/**
* A node-set value implemented intensionally. It is a wrapper round an Expression which
* can be evaluated independently of context, that is it has been reduced so there are
* no remaining context-dependencies.
*/

public class NodeSetIntent extends NodeSetValue {
    private NodeSetExpression expression;
    private NodeSetExtent extent = null;
    private Controller controller;
    private boolean sorted = false;
    private int useCount = 0;

    /**
    * Construct a node-set containing all the nodes in a NodeEnumeration
    */

    public NodeSetIntent(NodeSetExpression exp, Controller controller) throws XPathException {
        if (exp.getDependencies()!=0) {
            exp.display(10);
            throw new UnsupportedOperationException("Cannot create intensional node-set with context dependencies: " + exp.getClass() + ":" + exp.getDependencies());
        }
        expression = exp;
        this.controller = controller;
    }

    /**
    * Make a Context to use when enumerating the underlying expression
    */

    private Context makeContext() {
        Context c = new Context(controller);
        c.setStaticContext(expression.getStaticContext());
        return c;
    }

    /**
    * Get the encapsulated NodeSetExpression
    */

    public NodeSetExpression getNodeSetExpression() {
        return expression;
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes will be delivered
    * in document order and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {
        sorted = isSorted;
    }

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() throws XPathException {
        return (sorted || expression.enumerate(makeContext(), false).isSorted());
    }

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return expression.isContextDocumentNodeSet();
    }

    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() throws XPathException {
        NodeInfo first = getFirst();
        return (first==null ? "" : first.getStringValue());
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public boolean asBoolean() throws XPathException {
        return enumerate().hasMoreElements();
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() throws XPathException {
        if (extent == null) {
            NodeEnumeration enumeration = expression.enumerate(makeContext(), false);
            if (enumeration instanceof LastPositionFinder && enumeration.isSorted()) {
                return ((LastPositionFinder)enumeration).getLastPosition();
            }
            extent = new NodeSetExtent(enumeration, controller);
        }
        return extent.getCount();
    }

    private void fix() throws XPathException {
        if (extent == null) {
            NodeEnumeration enumeration = expression.enumerate(makeContext(), false);
            extent = new NodeSetExtent(enumeration, controller);
        }
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting.
    */

    public NodeSetValue sort() throws XPathException {
        if (sorted) return this;
        fix();
        return extent.sort();
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public NodeInfo getFirst() throws XPathException {
        if (extent!=null) return extent.getFirst();

        NodeEnumeration enumeration = expression.enumerate(makeContext(), false);
        if (sorted || enumeration.isSorted()) {
            sorted = true;
            if (enumeration.hasMoreElements()) {
                return enumeration.nextElement();
            } else {
                return null;
            }
        } else {
            NodeInfo first = null;
            while (enumeration.hasMoreElements()) {
                NodeInfo node = enumeration.nextElement();
                if (first==null || controller.compare(node, first) < 0) {
                    first = node;
                }
            }
            return first;
        }
    }

    /**
    * Return the first node in the nodeset (in document order)
    * @param context The context for the evaluation: not used
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context)  throws XPathException {
        return getFirst();
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws XPathException {
        if (extent!=null) {
            return extent.enumerate();
        } else {
            // arbitrarily, we decide that the third time the expression is used,
            // we will allocate it some memory for faster access on future occasions.
            useCount++;
            if (useCount < 3) {
                return expression.enumerate(makeContext(), false);
            } else {
                fix();
                return extent.enumerate();
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


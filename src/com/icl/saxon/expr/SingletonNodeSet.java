package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
* A node-set value containing zero or one nodes
*/

public class SingletonNodeSet extends NodeSetValue implements NodeList {

    protected NodeInfo node = null;
    protected boolean generalUseAllowed = true;

    /**
    * Allow general use as a node-set. This is required to lift the 1.0
    * restrictions on use of result tree fragments
    */

    public void allowGeneralUse() {
        generalUseAllowed = true;
    }

    /**
    * Determine if general use as a node-set is allowed
    */

    public boolean isGeneralUseAllowed() {
        return generalUseAllowed;
    }

    /**
    * Create an empty node-set
    */

    public SingletonNodeSet() {
        node = null;
    }

    /**
    * Create a node-set containing one node
    */

    public SingletonNodeSet(NodeInfo node) {
        this.node = node;
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() {
        if (node==null) {
            return new EmptyNodeSet();
        } else {
            return this;
        }
    }

    /**
    * Evaluate the Node Set. This guarantees to return the result in sorted order.
    * @param context The context for evaluation (not used)
    */

    public Value evaluate(Context context) {
        return this;
    }

    /**
    * Evaluate an expression as a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) {
        return this;
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {}

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() {
        return true;
    }

    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() {
        if (node==null) {
            return "";
        } else {
            return node.getStringValue();
        }
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public boolean asBoolean() {
        return node!=null;
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() {
        return (node==null ? 0 : 1);
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (Historic)
    */

    public NodeSetValue sort() {
        return this;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public NodeInfo getFirst() {
        return node;
    }


    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws XPathException {

        if (node==null) {
            if (other instanceof BooleanValue) {
                return !other.asBoolean();
            } else {
                return false;
            }
        }

        if (other instanceof StringValue ||
        		other instanceof FragmentValue ||
        		other instanceof TextFragmentValue ||
        		other instanceof ObjectValue) {
            return node.getStringValue().equals(other.asString());

        } else if (other instanceof NodeSetValue) {

            // see if there is a node in A with the same string value as a node in B

            try {
                String value = node.getStringValue();
                NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
                while (e2.hasMoreElements()) {
                    if (e2.nextElement().getStringValue().equals(value)) return true;
                }
                return false;
            } catch (XPathException err) {
                throw new InternalSaxonError(err.getMessage());
            }

        } else if (other instanceof NumericValue) {
                 return Value.stringToNumber(node.getStringValue())==other.asNumber();

        } else if (other instanceof BooleanValue) {
                 return other.asBoolean();

        } else {
                throw new InternalSaxonError("Unknown data type in a relational expression");
        }
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws XPathException {

        if (node==null) {
            if (other instanceof BooleanValue) {
                return other.asBoolean();
            } else {
                return false;
            }
        }

        if (other instanceof StringValue ||
        		other instanceof FragmentValue ||
        		other instanceof TextFragmentValue ||
        		other instanceof ObjectValue) {
            return !node.getStringValue().equals(other.asString());

        } else if (other instanceof NodeSetValue) {

            try {
                String value = node.getStringValue();

                NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
                while (e2.hasMoreElements()) {
                    if (!e2.nextElement().getStringValue().equals(value)) return true;
                }
                return false;
            } catch (XPathException err) {
                throw new InternalSaxonError(err.getMessage());
            }

        } else if (other instanceof NumericValue) {
             return Value.stringToNumber(node.getStringValue())!=other.asNumber();

        } else if (other instanceof BooleanValue) {
             return !other.asBoolean();

        } else {
             throw new InternalSaxonError("Unknown data type in a relational expression");

        }
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws XPathException {
        return new SingletonEnumeration(node);
    }

    // implement DOM NodeList

    /**
    * return the number of nodes in the list (DOM method)
    */

    public int getLength() {
        return getCount();
    }

    /**
    * Return the n'th item in the list (DOM method)
    */

    public Node item(int index) {
        if (index==0 && (node instanceof Node)) {
            return (Node)node;
        } else {
            return null;
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


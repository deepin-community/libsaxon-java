package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.sort.NodeOrderComparer;
import com.icl.saxon.sort.QuickSort;
import com.icl.saxon.sort.Sortable;
import org.w3c.dom.Node;

import java.util.Vector;

/**
* A node-set value implemented extensionally. This class also implements the
* DOM NodeList interface - though this will only work if the nodes themselves
* implement the DOM Node interface (which is true of the two Saxon tree models,
* but not necessarily of all possible implementations).
*/

public final class NodeSetExtent extends NodeSetValue
                           implements Sortable, org.w3c.dom.NodeList {
    private NodeInfo[] value;
    private int length;
    private boolean sorted;         // true only if values are known to be in document order
    private boolean reverseSorted;  // true if known to be in reverse document order
    private NodeOrderComparer comparer;      // Comparer used for sorting nodes.

    /**
    * Construct an empty node set
    */

    public NodeSetExtent(NodeOrderComparer comparer) {
        this.comparer = comparer;
        this.value = new NodeInfo[0];
        length = 0;
        sorted = true;
        reverseSorted = true;
    }

    /**
    * Construct a node-set given the set of nodes as an array
    * @param nodes An array whose elements must be NodeInfo objects
    * @param comparer Comparer used for sorting into document order
    */

    public NodeSetExtent(NodeInfo[] nodes, NodeOrderComparer comparer) {
        this.value = nodes;
        this.length = nodes.length;
        sorted = length<2;
        reverseSorted = length<2;
        this.comparer = comparer;
    }


    /**
    * Construct a node-set given the set of nodes as a Vector
    * @param nodes a Vector whose elements must be NodeInfo objects
    * @param comparer Comparer used for sorting into document order
    */

    public NodeSetExtent(Vector nodes, NodeOrderComparer comparer) {
        value = new NodeInfo[nodes.size()];
        for (int i=0; i<nodes.size(); i++) {
            value[i] = (NodeInfo)nodes.elementAt(i);
        }
        length = nodes.size();
        sorted = length<2;
        reverseSorted = length<2;
        this.comparer = comparer;
    }

    /**
    * Construct a node-set containing all the nodes in a NodeEnumeration.
    * @param enm The supplied node enumeration. This must be positioned at the start,
    * so that hasMoreElements() returns true if there are any nodes in the node-set,
    * and nextElement() returns the first node.
    * @param comparer Comparer used for sorting into document order
    */

    public NodeSetExtent(NodeEnumeration enm, NodeOrderComparer comparer) throws XPathException {
        this.comparer = comparer;
        int size = 20;
        //if (enm instanceof LastPositionFinder) {
        //    size = ((LastPositionFinder)enm).getLastPosition();
        //} else {
        //    size = 20;
        //}
                // above commented out. Although the enumeration may be able to
                // say how many nodes it contains, it may be an expensive operation,
                // so we behave as if we don't know.
        value = new NodeInfo[size];
        int i = 0;
        while (enm.hasMoreElements()) {
            if (i>=size) {
                size *= 2;
                NodeInfo newarray[] = new NodeInfo[size];
                System.arraycopy(value, 0, newarray, 0, i);
                value = newarray;
            }
            value[i++] = enm.nextElement();
        }
        sorted = enm.isSorted() || i<2;
        reverseSorted = enm.isReverseSorted() || i<2;
        length = i;
    }

    /**
    * Append a node to the node-set. This is used only when building indexes.
    * The node-set must be sorted; the new node must follow the others in document
    * order. The new node is not added if it is the same as the last node currently
    * in the node-set.
    */

    public void append(NodeInfo node) {
        reverseSorted = false;
        if (value.length < length + 1) {
            NodeInfo[] newval = new NodeInfo[(length==0 ? 10 : length * 2)];
            System.arraycopy(value, 0, newval, 0, length);
            value = newval;
        }
        if (length>0 && value[length-1].isSameNodeInfo(node)) {
            return;
        } else {
            value[length++] = node;
        }
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() {
        if (length==0) {
            return new EmptyNodeSet();
        } else if (length==1) {
            return new SingletonNodeSet(value[0]);
        } else {
            return this;
        }
    }


    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {
        sorted = isSorted;
    }

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() {
        return sorted;
    }

    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public String asString() {
        return (length>0 ? getFirst().getStringValue() : "");
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public boolean asBoolean() throws XPathException {
        return (length>0);
    }

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public int getCount() {
        sort();
        return length;
    }

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (The reason for returning this is that
    * it makes life easier for the XSL compiler).
    */

    public NodeSetValue sort() {
        if (length<2) sorted=true;
        if (sorted) return this;

        if (reverseSorted) {

            NodeInfo[] array = new NodeInfo[length];
            for (int n=0; n<length; n++) {
                array[n] = value[length-n-1];
            }
            value = array;
            sorted = true;
            reverseSorted = false;

        } else {
            // sort the array

            QuickSort.sort(this, 0, length-1);

            // need to eliminate duplicate nodes. Note that we cannot compare the node
            // objects directly, because with attributes and namespaces there might be
            // two objects representing the same node.

            int j=1;
            for(int i=1; i<length; i++) {
                if (!value[i].isSameNodeInfo(value[i-1])) {
                    value[j++] = value[i];
                }
            }
            length = j;

            sorted = true;
            reverseSorted = false;
        }
        return this;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node, or null if the nodeset is empty
    */

    public NodeInfo getFirst() {
        if (length==0) return null;
        if (sorted) return value[0];

        // scan to find the first in document order
        NodeInfo first = value[0];
        for(int i=1; i<length; i++) {
            if (comparer.compare(value[i], first) < 0) {
                first = value[i];
            }
        }
        return first;
    }

    /**
    * Return the first node in the nodeset (in document order)
    * @param context The context for the evaluation: not used
    * @return the NodeInfo of the first node in document order, or null if the node-set
    * is empty.
    */

    public NodeInfo selectFirst(Context context) {
        return getFirst();
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() {
        return new NodeSetValueEnumeration();
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
        sort();
        if (length>index && (value[index] instanceof Node)) {
            return (Node)(value[index]);
        } else {
            return null;
        }
    }

    /**
    * Compare two nodes in document sequence
    * (needed to implement the Sortable interface)
    */

    public int compare(int a, int b) {
        return comparer.compare(value[a], value[b]);
    }

    /**
    * Swap two nodes (needed to implement the Sortable interface)
    */

    public void swap(int a, int b) {
        NodeInfo temp = value[a];
        value[a] = value[b];
        value[b] = temp;
    }

    /**
    * Inner class NodeSetValueEnumeration
    */

    private class NodeSetValueEnumeration implements AxisEnumeration, LastPositionFinder {

        int index=0;

        public NodeSetValueEnumeration() {
            index = 0;
            //System.err.println("NSV enumeration: " + length);
        }

        public boolean hasMoreElements() {
            //System.err.println("NSV hasMoreElements?: " + index + " of " + length);
            return index<length;
        }

        public NodeInfo nextElement() {
            //System.err.println("NSV enumeration: " + index + " of " + length);
            return value[index++];
        }

        public boolean isSorted() {
            return sorted;
        }

        public boolean isReverseSorted() {
            return reverseSorted;
        }

        public boolean isPeer() {
            return false;
        }

        public int getLastPosition() {
            return length;
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


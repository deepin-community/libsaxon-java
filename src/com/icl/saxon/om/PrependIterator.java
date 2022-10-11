package com.icl.saxon.om;



/**
 * An iterator over nodes, that prepends a given node to the nodes
 * returned by another iterator. Used to modify an iterator over axis A
 * to one that iterates over A-OR-SELF.
 */

public class PrependIterator implements AxisEnumeration {

    NodeInfo start;
    AxisEnumeration base;
    int position = 0;

    public PrependIterator(NodeInfo start, AxisEnumeration base) {
        this.start = start;
        this.base = base;
    }

    /**
     * Determine whether there are more nodes to come. <BR>
     * (Note the term "Element" is used here in the sense of the standard Java Enumeration class,
     * it has nothing to do with XML elements).
     *
     * @return true if there are more nodes
     */

    public boolean hasMoreElements() {
        if (position == 0) {
            return true;
        } else {
            return base.hasMoreElements();
        }
    }

    /**
     * Get the next item in the sequence. <BR>
     *
     * @return the next Item. If there are no more nodes, return null.
     */

    public NodeInfo nextElement() {
        if (position == 0) {
            position = 1;
            return start;
        }
        NodeInfo n = base.nextElement();
        if (n == null) {
            position = -1;
        } else {
            position++;
        }
        return n;
    }

    /**
     * Determine whether the nodes returned by this enumeration are known to be peers, that is,
     * no node is a descendant or ancestor of another node. This significance of this property is
     * that if a peer enumeration is applied to each node in a set derived from another peer
     * enumeration, and if both enumerations are sorted, then the result is also sorted.
     */

    public boolean isPeer() {
        return false;
    }

    /**
     * Determine whether the nodes returned by this enumeration are known to be in
     * reverse document order.
     *
     * @return true if the nodes are guaranteed to be in document order.
     */

    public boolean isReverseSorted() {
        return base.isReverseSorted();
    }

    /**
     * Determine whether the nodes returned by this enumeration are known to be in document order
     *
     * @return true if the nodes are guaranteed to be in document order.
     */

    public boolean isSorted() {
        return base.isSorted();
    }

    /**
     * Get the last position
     */

    public int getLastPosition() {
        return base.getLastPosition() + 1;
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
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

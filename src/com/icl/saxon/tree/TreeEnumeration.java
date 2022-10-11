package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.pattern.NodeTest;


abstract class TreeEnumeration implements AxisEnumeration {

    protected NodeImpl start;
	protected NodeImpl next;
	protected NodeTest nodeTest;
	protected int last=-1;

	/**
	* Create an axis enumeration for a given type and name of node, from a given
	* origin node
	*/

	public TreeEnumeration(NodeImpl origin, NodeTest nodeTest) {
	    next = origin;
	    start = origin;
	    this.nodeTest = nodeTest;
	}

	/**
	* Test whether a node conforms to the node type and name constraints.
	* Note that this returns true if the supplied node is null, this is a way of
	* terminating a loop.
	*/

	protected boolean conforms(NodeImpl node) {
	    if (node==null) return true;
		return nodeTest.matches(node);
	}

	/**
	* Advance along the axis until a node is found that matches the required criteria
	*/

	protected final void advance() {
	    do {
	        step();
	    } while (!conforms(next));
	}

	/**
	* Advance one step along the axis: the resulting node might not meet the required
	* criteria for inclusion
	*/

	protected abstract void step();

	/**
	* Determine if there are more nodes to be returned
	*/

	public final boolean hasMoreElements() {
	    return next!=null;
	}

	/**
	* Return the next node in the enumeration
	*/

	public final NodeInfo nextElement() {
	    NodeInfo n = next;
	    advance();
	    return n;
	}

	/**
	* Determine if the nodes are guaranteed to be sorted in document order
	*/

	public boolean isSorted() {
	    return false;           // unless otherwise specified
	}

	/**
	* Determine if the nodes are guaranteed to be sorted in reverse document order
	*/

    public boolean isReverseSorted() {
        return !isSorted();
    }

	/**
	* Determine if the nodes are guaranteed to be peers (i.e. no node is a descendant of
	* another node)
	*/

	public boolean isPeer() {
	    return false;           // unless otherwise specified
	}

    /**
    * Count the number of nodes in the enumeration. This is used to support
    * finding the last() position. Note that it must be used on a "clean"
    * enumeration: the enumeration must be positioned at the start, and is left
    * positioned at the end.
    */

    protected int count() {
        int i=0;
        while (hasMoreElements()) {
            nextElement();
            i++;
        }
        return i;
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

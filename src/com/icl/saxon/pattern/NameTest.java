package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.expr.XPathException;

/**
  * NodeTest is an interface that enables a test of whether a node has a particular
  * name and type. A NameTest matches the node type and the namespace URI and the local
  * name.
  *
  * @author Michael H. Kay
  */

public class NameTest extends NodeTest {

	private short nodeType;
	private int fingerprint;

	public NameTest(short nodeType, int nameCode) {
		this.nodeType = nodeType;
		this.fingerprint = nameCode & 0xfffff;
		String s = " ";
		// This last line is absurd, but it circumvents a bug in the Microsoft JVM
	}

	/**
	* Create a NameTest for nodes of the same type and name as a given node
	*/

	public NameTest(NodeInfo node) {
		this.nodeType = node.getNodeType();
		this.fingerprint = node.getFingerprint();
	}

    /**
    * Test whether this node test is satisfied by a given node
    */

    public final boolean matches(NodeInfo node) {
    	return fingerprint == node.getFingerprint() &&
    	       nodeType == node.getNodeType();
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public boolean matches(short nodeType, int nameCode) {
        // System.err.println("Matching node " + fingerprint + " against " + this.fingerprint);
        // System.err.println("  " + (fingerprint == this.fingerprint && nodeType == this.nodeType));
        return ((nameCode&0xfffff) == this.fingerprint && nodeType == this.nodeType);
        // deliberately in this order for speed (first test usually fails)
    }

    /**
    * Determine the default priority of this node test when used on its own as a Pattern
    */

    public final double getDefaultPriority() {
    	return 0.0;
    }

	/**
	* Get the fingerprint required
	*/

	public int getFingerprint() {
		return fingerprint;
	}

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return NodeInfo.NODE
    * @return the type of node matched by this pattern. e.g. NodeInfo.ELEMENT or NodeInfo.TEXT
    */

    public short getNodeType() {
        return nodeType;
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

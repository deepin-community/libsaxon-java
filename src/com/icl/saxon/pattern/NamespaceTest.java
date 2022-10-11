package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.expr.XPathException;

/**
  * NodeTest is an interface that enables a test of whether a node has a particular
  * name and type. A NamespaceTest matches the node type and the namespace URI.
  *
  * @author Michael H. Kay
  */

public final class NamespaceTest extends NodeTest {

	private NamePool namePool;
	private short type;
	private short uriCode;

	public NamespaceTest(NamePool pool, short nodeType, short uriCode) {
	    namePool = pool;
		type = nodeType;
		this.uriCode = uriCode;
	}

    /**
    * Test whether this node test is satisfied by a given node
    */

    public final boolean matches(NodeInfo node) {
        int fingerprint = node.getFingerprint();
        if (fingerprint == -1) return false;
    	return type == node.getNodeType() &&
    	       uriCode == namePool.getURICode(fingerprint);
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public boolean matches(short nodeType, int fingerprint) {
        if (fingerprint == -1) return false;
        if (nodeType != type) return false;
        return uriCode == namePool.getURICode(fingerprint);
    }

    /**
    * Determine the default priority of this node test when used on its own as a Pattern
    */

    public final double getDefaultPriority() {
    	return -0.25;
    }

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return NodeInfo.NODE
    * @return the type of node matched by this pattern. e.g. NodeInfo.ELEMENT or NodeInfo.TEXT
    */

    public short getNodeType() {
        return type;
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

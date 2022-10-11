package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.XPathException;

/**
  * NodeTest is an interface that enables a test of whether a node has a particular
  * name and type. A NoNodeTest matches no nodes.
  *
  * @author Michael H. Kay
  */

public final class NoNodeTest extends NodeTest {

    private static NoNodeTest instance = new NoNodeTest();

    /**
    * Get a NoNodeTest instance
    */

    public static NoNodeTest getInstance() {
        return instance;
    }

	public final short getNodeType() {
		return NodeInfo.NONE;
	}

    /**
    * Test whether this node test is satisfied by a given node
    */

    public final boolean matches(NodeInfo node) {
    	return false;
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public boolean matches(short nodeType, int fingerprint) {
        return false;
    }

    /**
    * Determine the default priority of this node test when used on its own as a Pattern
    */

    public final double getDefaultPriority() {
    	return -0.5;
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

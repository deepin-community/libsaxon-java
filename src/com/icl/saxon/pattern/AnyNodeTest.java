package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;

/**
  * NodeTest is an interface that enables a test of whether a node has a particular
  * name and type. An AnyNodeTest matches any node.
  *
  * @author Michael H. Kay
  */

public final class AnyNodeTest extends NodeTest {

    static AnyNodeTest instance = new AnyNodeTest();

    public AnyNodeTest() {
        originalText = "node()";
    }

    /**
    * Get an instance of AnyNodeTest
    */

    public static AnyNodeTest getInstance() {
        return instance;
    }

    /**
    * Test whether this node test is satisfied by a given node
    */

    public final boolean matches(NodeInfo node) {
    	return true;
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public final boolean matches(short nodeType, int fingerprint) {
        return true;
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

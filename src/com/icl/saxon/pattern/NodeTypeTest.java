package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.XPathException;

/**
  * NodeTest is an interface that enables a test of whether a node has a particular
  * name and type. A NodeTypeTest matches the node type only.
  *
  * @author Michael H. Kay
  */

public class NodeTypeTest extends NodeTest {

	private short type;

	public NodeTypeTest(short nodeType) {
		type = nodeType;
		switch (nodeType) {
		    case NodeInfo.ROOT:
		        originalText = "/";
		        break;
		    case NodeInfo.ELEMENT:
		    case NodeInfo.ATTRIBUTE:
		        originalText = "*";
		        break;
		    case NodeInfo.COMMENT:
		        originalText = "comment()";
		        break;
		    case NodeInfo.TEXT:
		        originalText = "text()";
		        break;
		    case NodeInfo.PI:
		        originalText = "processing-instruction()";
		        break;
		    case NodeInfo.NAMESPACE:
		        originalText = "namespace()";
		        break;
		}
	}

    /**
    * Test whether this node test is satisfied by a given node
    */

    public final boolean matches(NodeInfo node) {
    	return type==node.getNodeType();
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public boolean matches(short nodeType, int fingerprint) {
        return (type == nodeType);
    }

    /**
    * Determine the default priority of this node test when used on its own as a Pattern
    */

    public final double getDefaultPriority() {
    	return -0.5;
    }

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
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

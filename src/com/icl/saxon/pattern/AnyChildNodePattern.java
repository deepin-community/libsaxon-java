package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.XPathException;

/**
* An AnyChildNodePattern is the pattern node(), which matches any node except a root node,
* an attribute node, or a namespace node: in other words, any node that is the child of another
* node.
*/

public final class AnyChildNodePattern extends NodeTest {

    /**
    * Determine whether the pattern matches a given node.
    * @param node the node to be tested
    * @return true if the pattern matches, else false
    */

    public boolean matches(NodeInfo node) {
        short type = node.getNodeType();
        return (type == NodeInfo.ELEMENT ||
                type == NodeInfo.TEXT ||
                type == NodeInfo.COMMENT ||
                type == NodeInfo.PI);
    }

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched
    */

    public boolean matches(short nodeType, int fingerprint) {
        return (nodeType == NodeInfo.ELEMENT ||
                nodeType == NodeInfo.TEXT ||
                nodeType == NodeInfo.COMMENT ||
                nodeType == NodeInfo.PI);
    }

    /**
    * Determine the type of nodes to which this pattern applies.
    * @return the node type
    */

    public short getNodeType() {
        return NodeInfo.NODE;
    }

    /**
    * Determine the default priority to use if this pattern appears as a match pattern
    * for a template with no explicit priority attribute.
    */

    public double getDefaultPriority() {
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

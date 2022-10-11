package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.NodeTest;

final class FollowingEnumeration extends TreeEnumeration {

    private NodeImpl root;

    public FollowingEnumeration(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);
        root = (DocumentImpl)node.getDocumentRoot();
        // skip the descendant nodes if any
        short type = node.getNodeType();
        if (type==NodeInfo.ATTRIBUTE || type==NodeInfo.NAMESPACE) {
            next = ((NodeImpl)node.getParentNode()).getNextInDocument(root);
        } else {
            do {
                next = (NodeImpl)node.getNextSibling();
                if (next==null) node = (NodeImpl)node.getParentNode();
            } while (next==null && node!=null);
        }
        while (!conforms(next)) {
            step();
        }
    }

    protected void step() {
        next = next.getNextInDocument(root);
    }

    public boolean isSorted() {
        return true;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last>=0) return last;
        FollowingEnumeration enm =
            new FollowingEnumeration(start, nodeTest);
        return enm.count();
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

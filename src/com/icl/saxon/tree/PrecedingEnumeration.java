package com.icl.saxon.tree;
import com.icl.saxon.pattern.NodeTest;

final class PrecedingEnumeration extends TreeEnumeration {

    NodeImpl nextAncestor;

    public PrecedingEnumeration(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);

        // we need to avoid returning ancestors of the starting node
        nextAncestor = (NodeImpl)node.getParent();
        advance();
    }


    /**
    * Special code to skip the ancestors of the start node
    */

    protected boolean conforms(NodeImpl node) {
        // ASSERT: we'll never test the root node, because it's always
        // an ancestor, so nextAncestor will never be null.
        if (node!=null) {
            if (node.isSameNode(nextAncestor)) {
                nextAncestor = (NodeImpl)nextAncestor.getParent();
                return false;
            }
        }
        return super.conforms(node);
    }

    protected void step() {
        next = next.getPreviousInDocument();
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last>=0) return last;
        PrecedingEnumeration enm =
            new PrecedingEnumeration(start, nodeTest);
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

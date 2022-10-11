package com.icl.saxon.tinytree;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.NodeInfo;

/**
* This class enumerates the ancestor:: or ancestor-or-self:: axes,
* starting at a given node. The start node will never be the root.
*/

final class AncestorEnumeration implements AxisEnumeration {

    private int nextNodeNr;
    private TinyDocumentImpl document;
    private TinyNodeImpl node;
    private NodeTest test;
    private TinyNodeImpl first = null;
    private boolean includeSelf;
    private int last = -1;

    public AncestorEnumeration(TinyDocumentImpl doc, TinyNodeImpl node,
                                NodeTest nodeTest, boolean includeSelf) {
        document = doc;
        test = nodeTest;
        this.node = node;
        this.includeSelf = includeSelf;
        if (includeSelf && nodeTest.matches(node)) {
            first = node;
        }

        // this code is designed to catch the case where the first node
        // is an attribute or namespace node

        TinyNodeImpl next = (TinyNodeImpl)node.getParent();
        nextNodeNr = next.nodeNr;
        if (!nodeTest.matches(next)) {
            advance();
        }
    }

    public boolean hasMoreElements() {
        return first != null || nextNodeNr >= 0;
    }

    public NodeInfo nextElement() {
        if (first!=null) {
            NodeInfo n = first;
            first = null;
            return n;
        } else {
            TinyNodeImpl node = document.getNode(nextNodeNr);
            advance();
            return node;
        }
    }

    private void advance() {
        int parentDepth = document.depth[nextNodeNr] - 1;
        do {
            do {
                nextNodeNr--;
                if (nextNodeNr<0) return;
            } while (document.depth[nextNodeNr] > parentDepth);
            if (test.matches(document.nodeType[nextNodeNr],
                              document.nameCode[nextNodeNr])) {
                return;
            }
            parentDepth--;
        } while ( nextNodeNr >= 0);
    }

    public boolean isSorted() {
        return false;
    }

    public boolean isReverseSorted() {
        return true;
    }

    public boolean isPeer() {
        return false;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last>=0) return last;
        AncestorEnumeration enm =
            new AncestorEnumeration(document, node, test, includeSelf);
        last = 0;
        while (enm.hasMoreElements()) {
            enm.nextElement();
            last++;
        }
        return last;
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

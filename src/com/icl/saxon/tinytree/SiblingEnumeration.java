package com.icl.saxon.tinytree;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.NodeTest;

/**
* This class supports both the child:: and following-sibling:: axes, which are
* identical except for the route to the first candidate node.
* It enumerates either the children or the following siblings of the specified node.
* In the case of children, the specified node must always
* be a node that has children: to ensure this, construct the enumeration
* using NodeInfo#getEnumeration()
*/

final class SiblingEnumeration implements AxisEnumeration {

    TinyDocumentImpl document;
    int nextNodeNr;
    NodeTest test;
    TinyNodeImpl startNode;
    TinyNodeImpl parentNode;
    boolean getChildren;
    int last = -1;

    protected SiblingEnumeration(TinyDocumentImpl doc, TinyNodeImpl node,
                              NodeTest nodeTest, boolean getChildren) {
        document = doc;
        test = nodeTest;
        startNode = node;
        this.getChildren = getChildren;
        if (getChildren) {          // child:: axis
            parentNode = node;

            // move to first child
            nextNodeNr = node.nodeNr + 1;

        } else {                    // following-sibling:: axis
            parentNode = (TinyNodeImpl)node.getParent();

            // move to next sibling
            nextNodeNr = doc.next[node.nodeNr];
        }

        // check if this matches the conditions
        if (nextNodeNr >= 0) {
            if (!nodeTest.matches(document.nodeType[nextNodeNr],
                                  document.nameCode[nextNodeNr])) {
                advance();
            }
        }
    }

    public boolean hasMoreElements() {
        return nextNodeNr >= 0;
    }

    public NodeInfo nextElement() {
        TinyNodeImpl node = document.getNode(nextNodeNr);
        node.setParentNode(parentNode);
        advance();
        return node;
    }

    private void advance() {
        do {
            nextNodeNr = document.next[nextNodeNr];
        } while ( nextNodeNr >= 0 &&
                !test.matches(document.nodeType[nextNodeNr],
                              document.nameCode[nextNodeNr]));
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted() {
        return false;
    }

    public boolean isPeer() {
        return true;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last >= 0) return last;
        SiblingEnumeration enm =
            new SiblingEnumeration(document, startNode, test, getChildren);
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

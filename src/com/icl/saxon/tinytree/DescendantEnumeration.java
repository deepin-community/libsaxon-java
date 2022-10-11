package com.icl.saxon.tinytree;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.NodeTest;

/**
* This class supports both the descendant:: and descendant-or-self:: axes, which are
* identical except for the route to the first candidate node.
* It enumerates descendants of the specified node.
* The calling code must ensure that the start node is not an attribute or namespace node.
*/

final class DescendantEnumeration implements AxisEnumeration {

    TinyDocumentImpl document;
    TinyNodeImpl startNode;
    boolean includeSelf;
    int nextNodeNr;
    int startDepth;
    NodeTest test;
    int last = -1;
    TinyNodeImpl parentNode;

    protected DescendantEnumeration(TinyDocumentImpl doc, TinyNodeImpl node,
                                    NodeTest nodeTest, boolean includeSelf) {
        document = doc;
        startNode = node;
        this.includeSelf = includeSelf;
        test = nodeTest;
        nextNodeNr = node.nodeNr;
        startDepth = doc.depth[nextNodeNr];
        if (includeSelf) {          // descendant-or-self:: axis
            // no action
        } else {                    // descendant:: axis
            nextNodeNr++;
            if (doc.depth[nextNodeNr] <= startDepth) {
                nextNodeNr = -1;
            }
        }

        // check if this matches the conditions
        if (nextNodeNr >= 0 &&
                nextNodeNr < doc.numberOfNodes &&
                !nodeTest.matches(document.nodeType[nextNodeNr],
                              document.nameCode[nextNodeNr])) {
            advance();
        }
    }

    public boolean hasMoreElements() {
        return nextNodeNr >= 0;
    }

    public NodeInfo nextElement() {
        TinyNodeImpl node = document.getNode(nextNodeNr);
        advance();
        return node;
    }

    private void advance() {
        do {
            nextNodeNr++;
            if (nextNodeNr >= document.numberOfNodes ||
                document.depth[nextNodeNr] <= startDepth) {
                nextNodeNr = -1;
                return;
            }
        } while (!test.matches(document.nodeType[nextNodeNr],
                                document.nameCode[nextNodeNr]));
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted() {
        return false;
    }

    public boolean isPeer() {
        return false;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last>=0) return last;
        DescendantEnumeration enm =
            new DescendantEnumeration(document, startNode, test, includeSelf);
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

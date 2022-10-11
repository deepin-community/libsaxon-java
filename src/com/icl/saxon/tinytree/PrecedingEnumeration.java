package com.icl.saxon.tinytree;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.AxisEnumeration;

/**
* Enumerate all the nodes on the preceding axis from a given start node.
* The calling code ensures that the start node is not a root, attribute,
* or namespace node. As well as the standard XPath preceding axis, this
* class also implements a Saxon-specific "preceding-or-ancestor" axis
* which returns ancestor nodes as well as preceding nodes. This is used
* when performing xsl:number level="any".
*/

final class PrecedingEnumeration implements AxisEnumeration {

    TinyDocumentImpl document;
    TinyNodeImpl startNode;
    NodeTest test;
    int nextNodeNr;
    int nextAncestorDepth;
    boolean includeAncestors;
    int last = -1;

    public PrecedingEnumeration(TinyDocumentImpl doc, TinyNodeImpl node,
                                NodeTest nodeTest, boolean includeAncestors) {

        this.includeAncestors = includeAncestors;
        test = nodeTest;
        document = doc;
        startNode = node;
        nextNodeNr = node.nodeNr;
        nextAncestorDepth = doc.depth[nextNodeNr] - 1;
        advance();
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
            nextNodeNr--;
            if (!includeAncestors) {
                // skip over ancestor elements
                while (nextNodeNr >= 0 && document.depth[nextNodeNr] == nextAncestorDepth) {
                    nextAncestorDepth--;
                    nextNodeNr--;
                }
            }
        } while ( nextNodeNr >= 0 &&
                !test.matches(document.nodeType[nextNodeNr],
                              document.nameCode[nextNodeNr]));
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
        if (last >= 0) return last;
        PrecedingEnumeration enm =
            new PrecedingEnumeration(document, startNode, test, includeAncestors);
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

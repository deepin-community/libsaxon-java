package com.icl.saxon.tinytree;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.NodeTest;

/**
* This class supports the preceding-sibling axis.
* The starting node must be an element, text node, comment, or processing instruction:
* to ensure this, construct the enumeration using NodeInfo#getEnumeration()
*/

final class PrecedingSiblingEnumeration implements AxisEnumeration {

    TinyDocumentImpl document;
    TinyNodeImpl startNode;
    int nextNodeNr;
    int depth;
    NodeTest test;
    TinyNodeImpl parentNode;
    int last = -1;

    protected PrecedingSiblingEnumeration(TinyDocumentImpl doc, TinyNodeImpl node,
                              NodeTest nodeTest) {
        document = doc;
        document.ensurePriorIndex();
        test = nodeTest;
        startNode = node;
        nextNodeNr = node.nodeNr;
        depth = doc.depth[nextNodeNr];
        parentNode = node.parent;   // doesn't matter if this is null (unknown)
        advance();
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
            nextNodeNr = document.prior[nextNodeNr];
        } while ( nextNodeNr >= 0 &&
                !test.matches(document.nodeType[nextNodeNr],
                              document.nameCode[nextNodeNr]));
/*
        for (int i=nextNodeNr-1; i>=0; i--) {
            int ndepth = document.depth[i];
            if (ndepth>=depth) {
                if (ndepth==depth &&
                    test.matches(document.nodeType[i],
                                  document.nameCode[i] & 0xfffff)) {
                    nextNodeNr = i;
                    return;
                }
            } else {
                nextNodeNr = -1;
                return;
            }
        }
*/
    }

    public boolean isSorted() {
        return false;
    }

    public boolean isReverseSorted() {
        return true;
    }

    public boolean isPeer() {
        return true;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last >= 0) return last;
        PrecedingSiblingEnumeration enm =
            new PrecedingSiblingEnumeration(document, startNode, test);
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

package com.icl.saxon.tinytree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.pattern.NodeTypeTest;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.AxisEnumeration;
import java.util.Vector;

/**
* Enumeration of the namespace nodes of an element
*/

final class NamespaceEnumeration implements AxisEnumeration {

    private TinyDocumentImpl document;
    private TinyElementImpl element;
    private NamePool pool;
    private int owner;
    private int currentElement;
    private int index;
    private Vector list = new Vector();
    private NodeTest nodeTest;
    private int last = -1;
    private int xmlNamespace;

    /**
    * Constructor. Note: this constructor will only be called if the owning
    * node is an element. Otherwise, an EmptyEnumeration will be returned
    */

    protected NamespaceEnumeration(TinyElementImpl node, NodeTest nodeTest) {
        // System.err.println("new NS enm");
        element = node;
        owner = node.nodeNr;
        document = (TinyDocumentImpl)node.getDocumentRoot();
        pool = document.getNamePool();
        currentElement = owner;
        index = document.length[currentElement]; // by convention
        this.nodeTest = nodeTest;
        xmlNamespace = pool.allocate("", "", "xml");
        advance();
    }

    private void advance() {
        // System.err.println("NSEnum advance index=" + index + " max= " + document.numberOfNamespaces);
        if (index == 0) {
            index = -1;
            return;
        } else if (index > 0) {
            while (index < document.numberOfNamespaces &&
                            document.namespaceParent[index] == currentElement) {

                int nsCode = document.namespaceCode[index];

                // don't return a namespace undeclaration (xmlns=""), but add it to the list
                // of prefixes encountered, to suppress outer xmlns="xyz" declarations

                if (nsCode == Namespace.NULL_CODE) {
                    list.addElement(new Short((short)0));
                } else {
                    if (matches(nsCode)) {
                        short prefixCode = (short)(nsCode>>16);

                        int max = list.size();
                        boolean duplicate = false;

                        // Don't add a node if the prefix has been previously encountered
                        for (int j=0; j<max; ) {
                            short nsj = ((Short)(list.elementAt(j++))).shortValue();
                            if (nsj==prefixCode) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            list.addElement(new Short(prefixCode));
                            return;
                        }
                    }
                }

                index++;
            }
        }

        NodeInfo parent = document.getNode(currentElement).getParent();
        if (parent.getNodeType()==NodeInfo.ROOT) {
            if (nodeTest.matches(NodeInfo.NAMESPACE, xmlNamespace)) {
                index = 0;
            } else {
                index = -1;
            }
        } else {
            currentElement = ((TinyElementImpl)parent).nodeNr;
            index = document.length[currentElement]; // by convention
            advance();
        }

    }

    private boolean matches(int nsCode) {
        if (nodeTest instanceof NodeTypeTest && nodeTest.getNodeType()==NodeInfo.NAMESPACE) {
            // fast path when selecting namespace::*
            return true;
        } else {
            int nameCode = pool.allocate("", "", pool.getPrefixFromNamespaceCode(nsCode));
            return nodeTest.matches(NodeInfo.NAMESPACE, nameCode);
        }
    }

    public boolean hasMoreElements() {
        return index>=0;
    }

    public NodeInfo nextElement() {
        // System.err.println("next NS, index = " + index);
        TinyNamespaceImpl nsi = document.getNamespaceNode(index);
        nsi.setParentNode(owner);
        advance();
        return nsi;
    }

    public boolean isSorted() {
        return false;
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
        NamespaceEnumeration enm =
            new NamespaceEnumeration(element, nodeTest);
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

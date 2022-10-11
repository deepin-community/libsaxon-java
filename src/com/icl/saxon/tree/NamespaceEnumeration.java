package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.pattern.NodeTest;
import java.util.Vector;

final class NamespaceEnumeration extends TreeEnumeration {

    private ElementImpl element;
    private Vector nslist;
    private int index;
    private int length;

    public NamespaceEnumeration(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);

        if (node instanceof ElementImpl) {
            element = (ElementImpl)node;
            nslist = new Vector(10);
            element.addNamespaceNodes(element, nslist, true);
            index = -1;
            length = nslist.size();
            advance();
        } else {      // if it's not an element then there are no namespace nodes
            next = null;
        }

    }

    public void step() {
        index++;
        if (index<length) {
            next = (NamespaceImpl)nslist.elementAt(index);
        } else {
            next = null;
        }
    }

	/**
	* Test whether a node conforms. Reject a node with prefix="", uri="" since
	* this represents a namespace undeclaration and not a true namespace node.
	*/

	protected boolean conforms(NodeInfo node) {
	    if (node==null) return true;
        NamespaceImpl ns = (NamespaceImpl)node;
        if (ns.getLocalName().equals("") && ns.getStringValue().equals("")) {
            return false;
        }
        return nodeTest.matches(node);
	}

    public boolean isSorted() {
        return false;
    }

    public boolean isPeer() {
        return true;
    }

    /**
    * Get the last position, that is the number of nodes in the enumeration
    */

    public int getLastPosition() {
        if (last>=0) return last;
        NamespaceEnumeration enm =
            new NamespaceEnumeration(start, nodeTest);
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

package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.expr.LastPositionFinder;


/**
  * ArrayEnumeration is used to enumerate nodes held in an array.
  * It is used only to enumerate the children of a node: this is assumed in the
  * values returned by isSorted(), etc.
  * @author Michael H. Kay
  */


final class ArrayEnumeration implements AxisEnumeration {

    NodeInfo[] nodes;
    int index = 0;

    public ArrayEnumeration(NodeInfo[] nodes) {
        this.nodes = nodes;
        index = 0;
        for (int i=0; i<nodes.length; i++) {
            if (nodes[i]==null) {
                System.err.println("  node " + i + " is null");
            }
        }
    }

    public boolean hasMoreElements() {
        return index < nodes.length;
    }

    public NodeInfo nextElement() {
        return nodes[index++];
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

    public int getLastPosition() {
        return nodes.length;
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

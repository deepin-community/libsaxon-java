package com.icl.saxon.tinytree;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.pattern.NameTest;

/**
* AttributeEnumeration is an enumeration of all the attribute nodes of an Element.
*/

final class AttributeEnumeration implements AxisEnumeration {

    private TinyDocumentImpl doc;
    private int element;
    private NodeTest nodeTest;
    private int index;
    private int last = -1;

    /**
    * Constructor. Note: this constructor will only be called if the relevant node
    * is an element and if it has one or more attributes. Otherwise an EmptyEnumeration
    * will be constructed instead.
    * @param node: the element whose attributes are required. This may be any type of node,
    * but if it is not an element the enumeration will be empty
    * @param nodeType: the type of node required. This may be any type of node,
    * but if it is not an attribute the enumeration will be empty
    * @param nameTest: condition to be applied to the names of the attributes selected
    */

    protected AttributeEnumeration(TinyDocumentImpl doc, int element, NodeTest nodeTest) {

        this.nodeTest = nodeTest;
        this.doc = doc;
        this.element = element;

        index = doc.offset[element];
        advance();
    }

    /**
    * Test if there are mode nodes still to come.
    * ("elements" is used here in the sense of the Java enumeration class, not in the XML sense)
    */

    public boolean hasMoreElements() {
        return index >=0;
    }

    /**
    * Get the next node in the enumeration.
    * ("elements" is used here in the sense of the Java enumeration class, not in the XML sense)
    */

    public NodeInfo nextElement() {
        int node = index++;
        if (nodeTest instanceof NameTest) {
            // there can only be one match, so abandon the search now
            index = -1;
        } else {
            advance();
        }
        return doc.getAttributeNode(node);
    }

    /**
    * Move to the next node in the enumeration.
    */

    private void advance() {
        do {
            if (index >= doc.numberOfAttributes || doc.attParent[index] != element) {
                index = -1;
                return;
            }
            if (nodeTest.matches(NodeInfo.ATTRIBUTE, doc.attCode[index])) {
                return;
            }
            index++;
        } while (true);
    }

    public boolean isSorted() {
        return true;            // in the sense that there is no need to sort them again
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
        if (last>=0) return last;
        AttributeEnumeration enm =
            new AttributeEnumeration(doc, element, nodeTest);
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

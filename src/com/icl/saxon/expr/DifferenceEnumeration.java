package com.icl.saxon.expr;
import com.icl.saxon.Controller;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;


/**
* An enumeration representing a nodeset that is an intersection of two other NodeSets.
* There is currently no operator in XPath to create such an expression, but it is used
* by the extension function difference(). The code is derived from the analagous UnionEnumeration,
* an inner class of UnionExpression.
*/


public class DifferenceEnumeration implements NodeEnumeration {


    private NodeEnumeration p1;
    private NodeEnumeration p2;
    private NodeEnumeration e1;
    private NodeEnumeration e2;
    private NodeInfo nextNode1 = null;
    private NodeInfo nextNode2 = null;
    private Controller controller;

    NodeInfo nextNode = null;

    /**
    * Form an enumeration of the difference of two nodesets, that is, the nodes
    * that are in p1 and that are not in p2.
    * @param p1 the first operand
    * @param p2 the second operand
    * @param comparer the comparer
    */

    public DifferenceEnumeration(NodeEnumeration p1, NodeEnumeration p2,
                                 Controller controller) throws XPathException {
        this.p1 = p1;
        this.p2 = p2;
        this.controller = controller;
        e1 = p1;
        e2 = p2;
        if (!e1.isSorted()) {
            e1 = (new NodeSetExtent(e1, controller)).sort().enumerate();
        }
        if (!e2.isSorted()) {
            e2 = (new NodeSetExtent(e2, controller)).sort().enumerate();
        }

        // move to the first node in each input nodeset

        if (e1.hasMoreElements()) {
            nextNode1 = e1.nextElement();
        }
        if (e2.hasMoreElements()) {
            nextNode2 = e2.nextElement();
        }

        // move to the first node in p1 that isn't in p2

        advance();

    }

    public boolean hasMoreElements() {
        return nextNode!=null;
    }

    public NodeInfo nextElement() throws XPathException {
        NodeInfo current = nextNode;
        advance();
        return current;
    }

    private void advance() throws XPathException {

        // main merge loop: if the node in p1 has a lower key value that that in p2, return it;
        // if they are equal, advance both nodesets; if p1 is higher, advance p2.

        while (nextNode1 != null && nextNode2 != null) {
            int c = controller.compare(nextNode1, nextNode2);
            if (c<0) {                              // p1 is lower
                NodeInfo next = nextNode1;
                if (e1.hasMoreElements()) {
                    nextNode1 = e1.nextElement();
                } else {
                    nextNode1 = null;
                    nextNode = null;
                }
                nextNode = next;
                return;

            } else if (c>0) {                       // p1 is higher
                NodeInfo next = nextNode2;
                if (e2.hasMoreElements()) {
                    nextNode2 = e2.nextElement();
                } else {
                    nextNode2 = null;
                    nextNode = null;
                }

            } else {                                // keys are equal

                NodeInfo next = nextNode2;
                if (e2.hasMoreElements()) {
                    nextNode2 = e2.nextElement();
                } else {
                    nextNode2 = null;
                }
                if (e1.hasMoreElements()) {
                    nextNode1 = e1.nextElement();
                } else {
                    nextNode1 = null;
                }
            }
        }

        // collect the remaining nodes from the residue of p1

        if (nextNode1!=null) {
            nextNode = nextNode1;
            if (e1.hasMoreElements()) {
                nextNode1 = e1.nextElement();
            } else {
                nextNode1 = null;
            }
            return;
        }

        nextNode = null;

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

package com.icl.saxon.exslt;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
* This class implements extension functions in the
* http://exslt.org/sets namespace. <p>
*/

public abstract class Sets  {

    /**
    * Return the intersection of two node-sets
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return A node-set containing all nodes that are in both p1 and p2
    */

    public static NodeEnumeration intersection(Context c, NodeEnumeration p1, NodeEnumeration p2) throws XPathException {
        return new IntersectionEnumeration(p1, p2, c.getController());
    }

    /**
    * Return the difference of two node-sets
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return A node-set containing all nodes that are in p1 and not in p2
    */

    public static NodeEnumeration difference(Context c, NodeEnumeration p1, NodeEnumeration p2) throws XPathException {
        return new DifferenceEnumeration(p1, p2, c.getController());
    }

    /**
    * Determine whether two node-sets contain at least one node in common
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return true if p1 and p2 contain at least one node in common (i.e. if the intersection
    * is not empty)
    */

    public static boolean hasSameNode(Context c, NodeEnumeration p1, NodeEnumeration p2) throws XPathException {
        NodeEnumeration intersection =
            new IntersectionEnumeration(p1, p2, c.getController());
        return intersection.hasMoreElements();
    }

    /**
    * Given a node-set, return a subset that includes only nodes with distinct string-values
    */

    public static NodeEnumeration distinct(Context c, NodeEnumeration in) throws XPathException {
        return new DistinctEnumeration(in, c.getController());
    }

    /**
    * Find all the nodes in ns1 that are before the first node in ns2.
    * Return empty set if ns2 is empty,
    */

    public static NodeSetValue leading (
                     Context c,
                     NodeEnumeration ns1, NodeEnumeration ns2) throws XPathException {

        Controller controller = c.getController();
        if (!ns2.hasMoreElements()) {
            return new NodeSetExtent(ns1, controller);
        }
        NodeInfo test = ns2.nextElement();

        Vector v = new Vector();
        while (ns1.hasMoreElements()) {
            NodeInfo node = ns1.nextElement();
            if (controller.compare(node, test) < 0) {
                v.addElement(node);
            } else {
                break;
            }
        }
        return new NodeSetExtent(v, controller);

    }

    /**
    * Find all the nodes in ns1 that are after the first node in ns2.
    * Return empty set if ns2 is empty,
    */

    public static NodeSetValue trailing (
                     Context c,
                     NodeEnumeration ns1, NodeEnumeration ns2) throws XPathException {

        if (!ns2.hasMoreElements()) {
            return new EmptyNodeSet();
        }
        NodeInfo test = ns2.nextElement();
        Controller controller = c.getController();

        Vector v = new Vector();
        boolean pastLimit = false;
        while (ns1.hasMoreElements()) {
            NodeInfo node = ns1.nextElement();
            if (pastLimit) {
                v.addElement(node);
            } else if (controller.compare(node, test) > 0) {
                pastLimit = true;
                v.addElement(node);
            }
        }
        return new NodeSetExtent(v, controller);
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

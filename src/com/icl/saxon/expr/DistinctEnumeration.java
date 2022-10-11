package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.*;


import java.util.Hashtable;

/**
* An enumeration returning the distinct nodes from a supplied nodeset
*/


public class DistinctEnumeration implements NodeEnumeration {

    private NodeEnumeration p1;
    private NodeEnumeration e1;
    private Hashtable lookup = new Hashtable();
    private Context context;
    private Expression expression;
    private Controller controller;

    NodeInfo nextNode = null;

    /**
    * Form an enumeration of the distinct nodes in a node-set, distinguishing nodes
    * by their string-value
    */

    public DistinctEnumeration(NodeEnumeration p1, Controller controller) throws XPathException {
        this.p1 = p1;
        this.context = null;
        this.expression = null;
        this.controller = controller;
        e1 = p1;
        if (!e1.isSorted()) {
            //System.err.println("distinct - base enumeration not sorted");
            e1 = (new NodeSetExtent(e1, controller)).sort().enumerate();
        }

        // move to the first node in the input nodeset

        if (e1.hasMoreElements()) {
            nextNode = e1.nextElement();
            advance();
        }
    }

    public DistinctEnumeration(Context c, NodeEnumeration p1, Expression exp)
    throws XPathException {
        this.p1 = p1;
        this.context = c.newContext();
        this.expression = exp;
        this.controller = c.getController();
        e1 = p1;
        if (!e1.isSorted()) {
            e1 = (new NodeSetExtent(e1, controller)).sort().enumerate();
        }

        // move to the first node in the input nodeset

        if (e1.hasMoreElements()) {
            nextNode = e1.nextElement();
            advance();
        }
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

        while (nextNode != null) {
            String val;
            if (expression==null) {
                val = nextNode.getStringValue();
            } else {
                context.setContextNode(nextNode);
                context.setPosition(1);
                context.setLast(1);
                val = expression.evaluateAsString(context);
            }
            if (lookup.get(val)==null) {
                lookup.put(val, nextNode);
                return;
            } else {
                if (e1.hasMoreElements()) {
                    nextNode = e1.nextElement();
                } else {
                    nextNode = null;
                }
            }
        }

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

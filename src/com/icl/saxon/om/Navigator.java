package com.icl.saxon.om;

import com.icl.saxon.Context;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.pattern.*;

import java.util.Vector;


/**
  * Navigator provides helper classes for navigating a tree, irrespective
  * of its implementation
  * @author Michael H. Kay
  */



public class Navigator {

    /**
    * Determine if a string is all-whitespace
    */

    public static boolean isWhite(String content) {
        for (int i=0; i<content.length(); i++) {
            char c = content.charAt(i);
            // all valid XML whitespace characters, and only whitespace characters, are <= 0x20
            if (((int)c) > 32) {
                return false;
            }
        }
        return true;
    }

    /**
    * Determine whether this node is an ancestor of another node
    * @param node the putative ancestor node
    * @param other the other node (the putative descendant of this node)
    * @return true of this node is an ancestor of the other node
    */

    public static boolean isAncestor(NodeInfo node, NodeInfo other) {
        NodeInfo parent = other.getParent();
        if (parent==null) return false;
        if (parent.isSameNodeInfo(node)) return true;
        return isAncestor(node, parent);
    }

    /**
    * Get an absolute XPath expression that identifies a given node within its document
    */

    public static String getPath(NodeInfo node) {
        String pre;
        switch (node.getNodeType()) {
            case NodeInfo.ROOT:
                return "/";
            case NodeInfo.ELEMENT:
                pre = getPath(node.getParent());
                return (pre.equals("/") ? "" : pre) +
                        "/" + node.getDisplayName() + "[" + getNumberSimple(node) + "]";
            case NodeInfo.ATTRIBUTE:
                return getPath(node.getParent()) + "/@" + node.getDisplayName();
            case NodeInfo.TEXT:
                pre = getPath(node.getParent());
                return (pre.equals("/") ? "" : pre) +
                        "/text()[" + getNumberSimple(node) + "]";
            case NodeInfo.COMMENT:
                pre = getPath(node.getParent());
                return (pre.equals("/") ? "" : pre) +
                    "/comment()[" + getNumberSimple(node) + "]";
            case NodeInfo.PI:
                pre = getPath(node.getParent());
                return (pre.equals("/") ? "" : pre) +
                    "/processing-instruction()[" + getNumberSimple(node) + "]";
            case NodeInfo.NAMESPACE:
                return getPath(node.getParent())+ "/namespace::" + node.getLocalName();
            default:
                return "";
        }
    }

    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL.
    * @param context Used for remembering previous result, for performance
    */

    public static int getNumberSimple(NodeInfo node, Context context) throws XPathException {

        //checkNumberable(node);

        int fingerprint = node.getFingerprint();
        NodeTest same;

        if (fingerprint==-1) {
            same = new NodeTypeTest(node.getNodeType());
        } else {
            same = new NameTest(node);
        }

        NodeEnumeration preceding = node.getEnumeration(Axis.PRECEDING_SIBLING, same);

        int i=1;
        while (preceding.hasMoreElements()) {
            NodeInfo prev = preceding.nextElement();

            int memo = context.getRememberedNumber(prev);
            if (memo>0) {
                memo += i;
                context.setRememberedNumber(node, memo);
                return memo;
            }

            i++;
        }

        context.setRememberedNumber(node, i);
        return i;
    }

    /**
    * Get simple node number. This is defined as one plus the number of previous siblings of the
    * same node type and name. It is not accessible directly in XSL. This version doesn't require
    * the context, and therefore doesn't remember previous results
    */

    public static int getNumberSimple(NodeInfo node) {

        try {
            int fingerprint = node.getFingerprint();
            NodeTest same;

            if (fingerprint==-1) {
                same = new NodeTypeTest(node.getNodeType());
            } else {
                same = new NameTest(node);
            }

            NodeEnumeration preceding = node.getEnumeration(Axis.PRECEDING_SIBLING, same);

            int i=1;
            while (preceding.hasMoreElements()) {
                NodeInfo prev = preceding.nextElement();
                i++;
            }

            return i;
        } catch (XPathException err) {
            // TODO: improve this.
            return 1;
        }
    }

    /**
    * Get node number (level="single"). If the current node matches the supplied pattern, the returned
    * number is one plus the number of previous siblings that match the pattern. Otherwise,
    * return the element number of the nearest ancestor that matches the supplied pattern.
    * @param count Pattern that identifies which nodes should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * (This parameter does not seem useful but is included for the sake of XSLT conformance.)
    * @return the node number established as follows: go to the nearest ancestor-or-self that
    * matches the 'count' pattern and that is a descendant of the nearest ancestor that matches the
    * 'from' pattern. Return one plus the nunber of preceding siblings of that ancestor that match
    * the 'count' pattern. If there is no such ancestor, return 0.
    */

    public static int getNumberSingle(NodeInfo node, Pattern count,
                    Pattern from, Context context) throws XPathException {

        //checkNumberable(node);  // Post-6.5.4 fix (see limitations.html). Test case numb38

        if (count==null && from==null) {
            return getNumberSimple(node, context);
        }

        boolean knownToMatch = false;
        if (count==null) {
            if (node.getFingerprint()==-1) {	// unnamed node
                count = new NodeTypeTest(node.getNodeType());
            } else {
                count = new NameTest(node);
            }
            knownToMatch = true;
        }

        NodeInfo target = node;
        while (!(knownToMatch || count.matches(target, context))) {
            target = target.getParent();
            if (target==null) {
                return 0;
            }
            if (from!=null && from.matches(target, context)) {
                return 0;
            }
        }

        // we've found the ancestor to count from

        NodeEnumeration preceding =
            target.getEnumeration(Axis.PRECEDING_SIBLING, AnyNodeTest.getInstance());
        int i = 1;
        while (preceding.hasMoreElements()) {
            NodeInfo p = preceding.nextElement();
            if (count.matches(p, context)) {
                i++;
            }
        }
        return i;
    }

    /**
    * Get node number (level="any").
    * Return one plus the number of previous nodes in the
    * document that match the supplied pattern
    * @param count Pattern that identifies which nodes should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * Only nodes after the first (most recent) node that matches the 'from' pattern are counted.
    * @return one plus the number of nodes that precede the current node, that match the count pattern,
    * and that follow the first node that matches the from pattern if specified.
    */

    public static int getNumberAny(NodeInfo node, Pattern count,
                    Pattern from, Context context) throws XPathException {

        //checkNumberable(node);

        int num = 0;
        if (count==null) {
            if (node.getFingerprint()==-1) {	// unnamed node
                count = new NodeTypeTest(node.getNodeType());
            } else {
                count = new NameTest(node);
            }
            num = 1;
        } else if (count.matches(node, context)) {
            num = 1;
        }

        // We use a special axis invented for the purpose: the union of the preceding and
        // ancestor axes, but in reverse document order

        NodeEnumeration preceding =
            node.getEnumeration(Axis.PRECEDING_OR_ANCESTOR, AnyNodeTest.getInstance());

        while (preceding.hasMoreElements()) {
            NodeInfo prev = preceding.nextElement();
            if (from!=null && from.matches(prev, context)) {
                return num;
            }
            if (count.matches(prev, context)) {
                num++;
            }
        }
        return num;
    }

    /**
    * Get node number (level="multiple").
    * Return a vector giving the hierarchic position of this node. See the XSLT spec for details.
    * @param count Pattern that identifies which nodes (ancestors and their previous siblings)
    * should be counted. Default (null) is the element
    * name if the current node is an element, or "node()" otherwise.
    * @param from Pattern that specifies where counting starts from. Default (null) is the root node.
    * Only nodes below the first (most recent) node that matches the 'from' pattern are counted.
    * @return a vector containing for each ancestor-or-self that matches the count pattern and that
    * is below the nearest node that matches the from pattern, an Integer which is one greater than
    * the number of previous siblings that match the count pattern.
    */

    public static Vector getNumberMulti(NodeInfo node, Pattern count,
                    Pattern from, Context context) throws XPathException {

        //checkNumberable(node);

        Vector v = new Vector();

        if (count==null) {
            if (node.getFingerprint()==-1) {    // unnamed node
                count = new NodeTypeTest(node.getNodeType());
            } else {
                count = new NameTest(node);
            }
        }

        NodeInfo curr = node;

        while(true) {
            if (count.matches(curr, context)) {
                int num = getNumberSingle(curr, count, null, context);
                v.insertElementAt(new Integer(num), 0);
            }
            curr = curr.getParent();
            if (curr==null) break;
            if (from!=null && from.matches(curr, context)) break;
        }

        return v;
    }

//    private static void checkNumberable(NodeInfo node) throws XPathException {
//        short type = node.getNodeType();
//        if (type == NodeInfo.ATTRIBUTE) {
//            throw new XPathException("Attribute nodes cannot be numbered");
//        }
//        if (type == NodeInfo.NAMESPACE) {
//            throw new XPathException("Namespace nodes cannot be numbered");
//        }
//    }

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

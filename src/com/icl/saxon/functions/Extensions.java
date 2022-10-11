package com.icl.saxon.functions;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.*;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.tinytree.TinyBuilder;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.TransformerException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
* This class implements functions that are supplied as standard with SAXON,
* but which are not defined in the XSLT or XPath specifications. <p>
*
* To invoke these functions, use a function call of the form prefix:name() where
* name is the method name, and prefix maps to a URI such as
* http://icl.com/saxon/com.icl.saxon.functions.Extensions (only the part
* of the URI after the last slash is important).
*/



public class Extensions  {


    public static void pauseTracing(Context c) {
        c.getController().pauseTracing(true);
    }
    public static void resumeTracing(Context c) {
        c.getController().pauseTracing(false);
    }

    /**
    * Convert a result tree fragment to a node-set. This simply marks a result
    * tree fragment as being available for general use.
    */

    public static NodeSetValue nodeSet(Context c, Value frag) throws XPathException {
        if (frag instanceof SingletonNodeSet) {
            ((SingletonNodeSet)frag).allowGeneralUse();
        }
        if (frag instanceof NodeSetValue) {
            return (NodeSetValue)frag;
        } else {
            TextFragmentValue v =
                new TextFragmentValue(frag.asString(), "", c.getController());
            v.allowGeneralUse();
            return v;
        }
    }

    /**
    * Alternative spelling (allows "nodeset()")
    */

    public static NodeSetValue nodeset(Context c, Value frag) throws XPathException {
        return nodeSet(c, frag);
    }

    /**
    * Return the system identifier of the context node
    */

    public static String systemId(Context c) throws XPathException {
        return c.getContextNodeInfo().getSystemId();
    }

    /**
    * Return the line number of the context node.
    * This must be returned as a double to meet the calling requirements for extension functions.
    */

    public static double lineNumber(Context c) throws XPathException {
        return c.getContextNodeInfo().getLineNumber();
    }

    /**
    * Return the base URI of the context node
    */

    public static String baseUri(Context c) throws XPathException {
        return c.getContextNodeInfo().getBaseURI();
    }

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
    * Determine whether two node-sets contain the same nodes
    * @param p1 The first node-set
    * @param p2 The second node-set
    * @return true if p1 and p2 contain the same set of nodes
    */

    public static boolean hasSameNodes(Context context, NodeEnumeration p1, NodeEnumeration p2) throws XPathException {
        NodeEnumeration e1 = p1;
        NodeEnumeration e2 = p2;
        Controller controller = context.getController();
        if (!e1.isSorted()) {
            e1 = (new NodeSetExtent(e1, controller)).sort().enumerate();
        }
        if (!e2.isSorted()) {
            e2 = (new NodeSetExtent(e2, controller)).sort().enumerate();
        }
        while (e1.hasMoreElements()) {
            if (!e2.hasMoreElements()) {
                return false;
            }
            if (!e1.nextElement().isSameNodeInfo(e2.nextElement())) {
                return false;
            }
        }
        if (e2.hasMoreElements()) {
            return false;
        }
        return true;
    }


    /**
    * Return the value of the second argument if the first is true, or the third argument
    * otherwise. Note that all three arguments are evaluated.
    * @param test A value treated as a boolean
    * @param thenValue Any value
    * @param elseValue Any value
    * @return (test ? thenValue : elseValue)
    */

    public static Value IF (Value test, Value thenValue, Value elseValue ) throws XPathException {
        return ( test.asBoolean() ? thenValue : elseValue );
    }

    /**
    * Evaluate the expression supplied in the first argument as a string
    */

    public static Value evaluate (Context c, String expr) throws XPathException {
    	StaticContext env = c.getStaticContext().makeRuntimeContext(
    									c.getController().getNamePool());
        Expression e = Expression.make(expr, env);
        return e.evaluate(c);
    }

    /**
    * Evaluate the stored expression supplied in the first argument
    */

    public static Value eval (Context c, Expression expr) throws XPathException {
        return expr.evaluate(c);
    }

    /**
    * Return an object representing a stored expression,
    * from the string supplied in the first argument.
    */

    public static Value expression (Context c, String expr) throws XPathException {
    	StaticContext env = c.getStaticContext().makeRuntimeContext(
    									c.getController().getNamePool());
        Expression e1 = Expression.make(expr, env);
        // substitute values of variables
        Expression e2 = e1.reduce(Context.VARIABLES, c).simplify();
        return new ObjectValue(e2);
    }

    /**
    * Total a stored expression over a set of nodes
    */

    public static double sum (Context context,
                              NodeEnumeration nsv,
                              Expression expression) throws XPathException {
        double total = 0.0;
        Context c = context.newContext();
        NodeEnumeration v;
        if (nsv instanceof LastPositionFinder) {
            v = nsv;
        } else {
            v = new LookaheadEnumerator(nsv);
        }
        c.setLastPositionFinder((LastPositionFinder)v);
        int pos = 1;
        while (v.hasMoreElements()) {
            c.setContextNode(v.nextElement());
            c.setPosition(pos++);
            double x = expression.evaluateAsNumber(c);
            total += x;
        }
        return total;
    }

    /**
    * Get the maximum numeric value of the string-value of each of a set of nodes
    */

    public static double max (NodeEnumeration nsv) throws XPathException {
        return com.icl.saxon.exslt.Math.max(nsv);
    }


    /**
    * Get the maximum numeric value of a stored expression over a set of nodes
    */

    public static double max (Context context,
                              NodeEnumeration nsv,
                              Expression expression) throws XPathException {
        double max = Double.NEGATIVE_INFINITY;
        Context c = context.newContext();
        NodeEnumeration v;
        if (nsv instanceof LastPositionFinder) {
            v = nsv;
        } else {
            v = new LookaheadEnumerator(nsv);
        }
        c.setLastPositionFinder((LastPositionFinder)v);
        int pos = 1;
        while (v.hasMoreElements()) {
            c.setContextNode(v.nextElement());
            c.setPosition(pos++);
            double x = expression.evaluateAsNumber(c);
            if (x>max) max = x;
        }
        return max;
    }

    /**
    * Get the minimum numeric value of the string-value of each of a set of nodes
    */

    public static double min (NodeEnumeration nsv) throws XPathException {
        return com.icl.saxon.exslt.Math.min(nsv);
    }

    /**
    * Get the minimum numeric value of a stored expression over a set of nodes
    */

    public static double min (Context context,
                              NodeEnumeration nsv,
                              Expression expression) throws XPathException {
        double min = Double.POSITIVE_INFINITY;
        Context c = context.newContext();
        NodeEnumeration v;
        if (nsv instanceof LastPositionFinder) {
            v = nsv;
        } else {
            v = new LookaheadEnumerator(nsv);
        }
        c.setLastPositionFinder((LastPositionFinder)v);
        int pos = 1;
        while (v.hasMoreElements()) {
            c.setContextNode(v.nextElement());
            c.setPosition(pos++);
            double x = expression.evaluateAsNumber(c);
            if (x<min) min = x;
        }
        return min;
    }

    /**
    * Get the node with maximum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue highest (Context c, NodeEnumeration nsv) throws XPathException {
        return com.icl.saxon.exslt.Math.highest(c, nsv);
    }


    /**
    * Get the maximum numeric value of a stored expression over a set of nodes
    */

    public static NodeEnumeration highest (Context context,
                                        NodeEnumeration nsv,
                                        Expression expression) throws XPathException {
        double max = Double.NEGATIVE_INFINITY;
        Context c = context.newContext();
        NodeInfo highest = null;
        NodeEnumeration v;
        if (nsv instanceof LastPositionFinder) {
            v = nsv;
        } else {
            v = new LookaheadEnumerator(nsv);
        }
        c.setLastPositionFinder((LastPositionFinder)v);
        int pos = 1;
        while (v.hasMoreElements()) {
            c.setContextNode(v.nextElement());
            c.setPosition(pos++);
            double x = expression.evaluateAsNumber(c);
            if (x>max) {
                max = x;
                highest = c.getContextNodeInfo();
            }
        }
        return new SingletonEnumeration(highest);
    }

    /**
    * Get the node with minimum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue lowest (Context c, NodeEnumeration nsv) throws XPathException {
        return com.icl.saxon.exslt.Math.lowest(c, nsv);
    }

    /**
    * Get the node with minimum numeric value of a stored expression over a set of nodes
    */

    public static NodeEnumeration lowest (Context context,
                                       NodeEnumeration nsv,
                                       Expression expression) throws XPathException {
        double min = Double.POSITIVE_INFINITY;
        Context c = context.newContext();
        NodeInfo lowest = null;
        NodeEnumeration v;
        if (nsv instanceof LastPositionFinder) {
            v = nsv;
        } else {
            v = new LookaheadEnumerator(nsv);
        }
        c.setLastPositionFinder((LastPositionFinder)v);
        int pos = 1;
        while (v.hasMoreElements()) {
            c.setContextNode(v.nextElement());
            c.setPosition(pos++);
            double x = expression.evaluateAsNumber(c);
            if (x<min) {
                min = x;
                lowest = c.getContextNodeInfo();
            }
        }
        return new SingletonEnumeration(lowest);
    }

    /**
    * Given a node-set, return a subset that includes only nodes with distinct string-values
    */

    public static NodeEnumeration distinct(Context context, NodeEnumeration in) throws XPathException {
        return new DistinctEnumeration(in, context.getController());
    }

    /**
    * Given a node-set, return a subset that includes only nodes with distinct string-values
    * for the supplied expression
    */

    public static NodeEnumeration distinct(Context context,
                                        NodeEnumeration in,
                                        Expression exp) throws XPathException {
        return new DistinctEnumeration(context, in, exp);
    }

    /**
    * Evaluate the transitive closure of a node-set expression
    */

    public static NodeEnumeration closure (Context c,
            NodeEnumeration enm, Expression expr) throws XPathException {

        NodeEnumeration result = EmptyEnumeration.getInstance();
        Controller controller = c.getController();
        while(enm.hasMoreElements()) {
            NodeInfo node = enm.nextElement();
            Context c2 = c.newContext();
            c2.setContextNode(node);
            c2.setCurrentNode(node);
            c2.setPosition(1);
            c2.setLast(1);
            NodeEnumeration next =
                new UnionEnumeration(
                    new SingletonEnumeration(node),
                    closure(c2, expr.enumerate(c2, false), expr),
                    controller);
            result = new UnionEnumeration(result, next, controller);
        }
        return result;
    }

    /**
    * Get the nodes that satisfy the given expression, up to and excluding the first one
    * (in document order) that doesn't
    */

    public static NodeEnumeration leading (Context context,
                         NodeEnumeration in, Expression exp) throws XPathException {
        return new FilterEnumerator(in, exp, context.newContext(), true);
    }

    /**
    * Find all the nodes in ns1 that are before the last node in ns2.
    * Return empty set if ns2 is empty,
    */

    public static NodeSetValue before (
                                 Context context,
                                 NodeSetValue ns1,
                                 NodeSetValue ns2) throws XPathException {

        NodeInfo test = null;
        NodeEnumeration enum2 = ns2.enumerate();
        while (enum2.hasMoreElements()) {
            test = enum2.nextElement();
        }
        if (test==null) {
            return new EmptyNodeSet();
        }
        Controller controller = context.getController();

        Vector v = new Vector();
        NodeEnumeration enm = ns1.enumerate();
        while (enm.hasMoreElements()) {
            NodeInfo node = enm.nextElement();
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

    public static NodeSetValue after (
                     Context context,
                     NodeSetValue ns1, NodeSetValue ns2) throws XPathException {

        NodeInfo test = ns2.getFirst();
        if (test==null) {
            return new EmptyNodeSet();
        }
        Controller controller = context.getController();

        Vector v = new Vector();
        NodeEnumeration enm = ns1.enumerate();
        boolean pastLimit = false;
        while (enm.hasMoreElements()) {
            NodeInfo node = enm.nextElement();
            if (pastLimit) {
                v.addElement(node);
            } else if (controller.compare(node, test) > 0) {
                pastLimit = true;
                v.addElement(node);
            }
        }
        return new NodeSetExtent(v, controller);

    }

    /**
    * Test whether node-set contains a node that satisfies a given condition
    */

    public static boolean exists (Context context,
                              NodeEnumeration nsv,
                              Expression expression) throws XPathException {
        return new FilterEnumerator(nsv, expression, context.newContext(), false)
                    .hasMoreElements();
    }

    /**
    * Test whether all nodes in a node-set satisfy a given condition
    */

    public static boolean forAll (Context context,
                              NodeEnumeration nsv,
                              Expression expression) throws XPathException {
        Not notexp = new Not();
        notexp.addArgument(expression);
        return !(new FilterEnumerator(nsv, notexp, context.newContext(), false)
                    .hasMoreElements());
    }


    /**
    * Return a node-set whose nodes have string-values "1", "2", ... "n"
    */

    public static NodeEnumeration range(Context context, double start, double finish) throws XPathException {
        int a = (int)Round.round(start);
        int b = (int)Round.round(finish);

        try {
            TinyBuilder builder = new TinyBuilder();
            NamePool pool = context.getController().getNamePool();
            int[] namespaces = new int[1];
            namespaces[0] = pool.getNamespaceCode("saxon", Namespace.SAXON);
            int saxonRange = pool.allocate("saxon", Namespace.SAXON, "range");
            builder.setNamePool(pool);
            builder.startDocument();
            AttributeCollection emptyAtts = new AttributeCollection(pool);

            for (int i=a; i<=b; i++) {
                builder.startElement(saxonRange, emptyAtts, namespaces, 1);
                String n = i+"";
                builder.characters(n.toCharArray(), 0, n.length());
                builder.endElement(saxonRange);
            }

            builder.endDocument();
            DocumentInfo doc = builder.getCurrentDocument();
            return doc.getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());

        } catch (TransformerException err) {
            throw new XPathException(err);
        }

    }

    /**
    * Return a node-set by tokenizing a supplied string. Tokens are delimited by any sequence of
    * whitespace characters.
    */

    public static NodeEnumeration tokenize(Context context, String s) throws XPathException {

        try {
            Builder builder = context.getController().makeBuilder();
            NamePool pool = context.getController().getNamePool();
            builder.startDocument();
            int[] namespaces = new int[1];
            namespaces[0] = pool.getNamespaceCode("saxon", Namespace.SAXON);
            int saxonToken = pool.allocate("saxon", Namespace.SAXON, "token");
            AttributeCollection emptyAtts = new AttributeCollection(pool);

            StringTokenizer st = new StringTokenizer(s);
            while (st.hasMoreTokens()) {
                builder.startElement(saxonToken, emptyAtts, namespaces, 1);
                String n = st.nextToken();
                builder.characters(n.toCharArray(), 0, n.length());
                builder.endElement(saxonToken);
            }

            builder.endDocument();
            DocumentInfo doc = builder.getCurrentDocument();
            return doc.getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        } catch (TransformerException err) {
            throw new XPathException(err);
        }
    }

    /**
    * Return a node-set by tokenizing a supplied string. The argument delim is a String, any character
    * in this string is considered to be a delimiter character, and any sequence of delimiter characters
    * acts as a separator between tokens.
    */

    public static NodeEnumeration tokenize(Context context, String s, String delim) throws XPathException {
        try {
            Builder builder = context.getController().makeBuilder();
            NamePool pool = context.getController().getNamePool();
            builder.setNamePool(pool);
            builder.startDocument();
            int[] namespaces = new int[1];
            namespaces[0] = pool.getNamespaceCode("saxon", Namespace.SAXON);
            int saxonToken = pool.allocate("saxon", Namespace.SAXON, "token");
            AttributeCollection emptyAtts = new AttributeCollection(pool);

            StringTokenizer st = new StringTokenizer(s, delim);
            while (st.hasMoreTokens()) {
                builder.startElement(saxonToken, emptyAtts, namespaces, 1);
                String n = st.nextToken();
                builder.characters(n.toCharArray(), 0, n.length());
                builder.endElement(saxonToken);
            }

            builder.endDocument();
            DocumentInfo doc = builder.getCurrentDocument();
            return doc.getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        } catch (TransformerException err) {
            throw new XPathException(err);
        }
    }


    /**
    * Return an XPath expression that identifies the current node
    */

    public static String path(Context c) throws XPathException {
        return Navigator.getPath(c.getContextNodeInfo());
    }

	/**
	* Array of names of node types. You can index into this with the numeric node type.
	*/

	private static final String[] NODE_TYPE_NAMES =
		{"Node", "Element", "Attribute", "Text", "?", "?", "?",
			"Processing Instruction", "Comment", "Root", "?", "?", "?", "Namespace"};

	/**
	* A diagnostic function to print the contents of a node-set
	*/

	public static String showNodeset(Context c, NodeSetValue in) throws XPathException {
		System.err.println("Node-set contents at line " + c.getStaticContext().getLineNumber() + " [");
		NodeEnumeration enm = in.enumerate(c, true);
		int count = 0;
		while (enm.hasMoreElements()) {
			count++;
			NodeInfo next = enm.nextElement();
			String typeName = NODE_TYPE_NAMES[next.getNodeType()];
			System.err.println("  " + typeName + " " +
								 next.getDisplayName() + " " +
								 Navigator.getPath(next) + " " +
								 next.generateId());
		}
		System.err.println("] (Total number of nodes: " + count + ")");
		return "";
	}


    /**
    * Test whether an encapsulated Java object is null
    */

    public static boolean isNull(Object x) throws XPathException {
        return x==null;
    }


    /**
    * Save a value associated with the context node
    */

    public static void setUserData(Context c, String name, Value value) throws XPathException {
            // System.err.println("Set user data " + name + " on " + c.getContextNode().getPath() + " = " + value);
        c.getController().setUserData(
            (c.getContextNodeInfo()), name, value);
    }

    /**
    * Retrieve a value associated with the context node
    */

    public static Value getUserData(Context c, String name) throws XPathException {
        Object o = c.getController().getUserData(
                        (c.getContextNodeInfo()), name);
            // System.err.println("Get user data " + name + " on " + c.getContextNode().getPath() + " = " + o);
        if (o==null) return new StringValue("");
        if (o instanceof Value) return (Value)o;
        return new ObjectValue(o);
    }

	/**
	* Return the Context object
	*/

	public static Context getContext(Context c) {
		return c;
	}

	/**
	* Get a pseudo-attribute of a processing instruction. Return an empty string
	* if the context node is not a processing instruction, or if the pseudo-attribute
	* is not present. Character references and built-in entity references are expanded
	*/

	public static String getPseudoAttribute(Context c, String name) {
	    NodeInfo pi = c.getContextNodeInfo();
	    if (pi.getNodeType() != NodeInfo.PI) return "";
	    String val = ProcInstParser.getPseudoAttribute(pi.getStringValue(), name);
	    if (val==null) return "";
	    return val;
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

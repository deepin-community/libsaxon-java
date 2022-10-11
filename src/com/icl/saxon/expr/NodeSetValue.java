package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.sort.LocalOrderComparer;

import java.util.*;
import org.w3c.dom.*;

/**
* A node-set value. We use this both for node-sets and node-lists. The node set will only
* be sorted into document order when requested (using sort() or evaluate()). This is an abstract
* class with a number of concrete implementations including NodeSetExtent (for extensional node-sets)
* and NodeSetIntent (for intensional node-sets).
*/

public abstract class NodeSetValue extends Value {

    private Hashtable stringValues = null;     // used for testing equality

    /**
    * Determine the data type of the expression
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }

    /**
    * Evaluate the Node Set. This guarantees to return the result in sorted order.
    * @param context The context for evaluation (not used)
    */

    public Value evaluate(Context context) throws XPathException {
        sort();
        return this;
    }

    /**
    * Evaluate an expression as a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws XPathException {
        sort();
        return this;
    }

    /**
    * Return an enumeration of this nodeset value. Unless sort() has been called
    * the nodes can be in any order.
    */

    public abstract NodeEnumeration enumerate() throws XPathException;

    /**
    * Return an enumeration of this nodeset value. This is to satisfy the interface for
    * Expression.
    * @param context The context is ignored.
    * @param sorted Indicates that the result must be in document order
    */

    public NodeEnumeration enumerate(Context c, boolean sorted) throws XPathException {
        if (sorted) sort();
        return enumerate();
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public abstract void setSorted(boolean isSorted);

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public abstract boolean isSorted() throws XPathException;

    /**
    * Convert to string value
    * @return the value of the first node in the node-set if there
    * is one, otherwise an empty string
    */

    public abstract String asString() throws XPathException;

    /**
    * Evaluate as a number.
    * @return the number obtained by evaluating as a String and converting the string to a number
    */

    public double asNumber() throws XPathException {
        return (new StringValue(asString())).asNumber();
    }

    /**
    * Evaluate as a boolean.
    * @return true if the node set is not empty
    */

    public abstract boolean asBoolean() throws XPathException;

    /**
    * Count the nodes in the node-set. Note this will sort the node set if necessary, to
    * make sure there are no duplicates.
    */

    public abstract int getCount() throws XPathException;

    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @param controller The controller used to sort nodes into document order
    * @return the same NodeSetValue, after sorting. (The reason for returning this is that
    * it makes life easier for the XSL compiler).
    */

    public abstract NodeSetValue sort() throws XPathException;

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public abstract NodeInfo getFirst() throws XPathException;

    /**
    * Get a hashtable containing all the string values of nodes in this node-set
    */

    private Hashtable getStringValues() throws XPathException {
        if (stringValues==null) {
            stringValues = new Hashtable();
            NodeEnumeration e1 = this.enumerate();
            while (e1.hasMoreElements()) {
                stringValues.put(e1.nextElement().getStringValue(), "x");
            }
        }
        return stringValues;
    }

    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws XPathException {

        if (other instanceof ObjectValue) {
            return false;

	    } else if (other instanceof SingletonNodeSet) {
	        if (other.asBoolean()) {
	            return equals(new StringValue(other.asString()));
	        } else {
	            return false;
	        }

        } else if (other instanceof NodeSetValue) {

            // see if there is a node in A with the same string value as a node in B

            Hashtable table = getStringValues();

            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                if (table.get(e2.nextElement().getStringValue())!=null) return true;
            }
            return false;

        } else if (other instanceof NumericValue) {
            NodeEnumeration e1 = this.enumerate();
            while (e1.hasMoreElements()) {
                NodeInfo node = e1.nextElement();
                if (Value.stringToNumber(node.getStringValue())==other.asNumber()) return true;
            }
            return false;

        } else if (other instanceof StringValue) {
            if (stringValues==null) {
                NodeEnumeration e1 = this.enumerate();
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (node.getStringValue().equals(other.asString())) return true;
                }
                return false;
            } else {
                return stringValues.get(other.asString()) != null;
            }

        } else if (other instanceof BooleanValue) {
                            // fix bug 4.5/010
            return (asBoolean()==other.asBoolean());

        } else {
            throw new InternalSaxonError("Unknown data type in a relational expression");
            // TODO: handle External Object data type
        }

    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws XPathException {

        if (other instanceof ObjectValue) {
            return false;

	    } else if (other instanceof SingletonNodeSet) {
	        if (other.asBoolean()) {
	            return notEquals(new StringValue(other.asString()));
	        } else {
	            return false;
	        }

        } else if (other instanceof NodeSetValue) {

            // see if there is a node in A with a different string value as a node in B
            // use a nested loop: it will usually finish very quickly!

            NodeEnumeration e1 = this.enumerate();
            while (e1.hasMoreElements()) {
                String s1 = e1.nextElement().getStringValue();
                NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
                while (e2.hasMoreElements()) {
                    String s2 = e2.nextElement().getStringValue();
                    if (!s1.equals(s2)) return true;
                }
            }
            return false;

        } else if (other instanceof NumericValue) {
            NodeEnumeration e1 = this.enumerate();
            while (e1.hasMoreElements()) {
                NodeInfo node = e1.nextElement();
                if (Value.stringToNumber(node.getStringValue())!=other.asNumber()) return true;
            }
            return false;

        } else if (other instanceof StringValue) {
            NodeEnumeration e1 = this.enumerate();
            while (e1.hasMoreElements()) {
                NodeInfo node = e1.nextElement();
                if (!(node.getStringValue().equals(other.asString()))) return true;
            }
            return false;

        } else if (other instanceof BooleanValue) {
            // bug 4.5/010
            return (asBoolean()!=other.asBoolean());

        } else {
            throw new InternalSaxonError("Unknown data type in a relational expression");
        }

    }

    /**
    * Test how a nodeset compares to another Value under a relational comparison
    * @param operator The comparison operator, one of Tokenizer.LE, Tokenizer.LT,
    * Tokenizer.GE, Tokenizer.GT,
    */

    public boolean compare(int operator, Value other) throws XPathException {
        if (other instanceof ObjectValue) {
            return false;

	    }
	    if (other instanceof SingletonNodeSet) {
	        if (other.asBoolean()) {
	            other = new StringValue(other.asString());
	        } else {
	            return false;
	        }
	    }

        if (operator==Tokenizer.EQUALS) return equals(other);
        if (operator==Tokenizer.NE) return notEquals(other);

        if (other instanceof NodeSetValue) {

            // find the min and max values in this nodeset

            double thismax = Double.NEGATIVE_INFINITY;
            double thismin = Double.POSITIVE_INFINITY;
            boolean thisIsEmpty = true;

            NodeEnumeration e1 = enumerate();
            while (e1.hasMoreElements()) {
                double val = Value.stringToNumber(e1.nextElement().getStringValue());
                if (val<thismin) thismin = val;
                if (val>thismax) thismax = val;
                thisIsEmpty = false;
            }

            if (thisIsEmpty) return false;

            // find the minimum and maximum values in the other nodeset

            double othermax = Double.NEGATIVE_INFINITY;
            double othermin = Double.POSITIVE_INFINITY;
            boolean otherIsEmpty = true;

            NodeEnumeration e2 = ((NodeSetValue)other).enumerate();
            while (e2.hasMoreElements()) {
                double val = Value.stringToNumber(e2.nextElement().getStringValue());
                if (val<othermin) othermin = val;
                if (val>othermax) othermax = val;
                otherIsEmpty = false;
            }

            if (otherIsEmpty) return false;

            switch(operator) {
                case Tokenizer.LT:
                    return thismin < othermax;
                case Tokenizer.LE:
                    return thismin <= othermax;
                case Tokenizer.GT:
                    return thismax > othermin;
                case Tokenizer.GE:
                    return thismax >= othermin;
                default:
                    return false;
            }

        } else {
            if (other instanceof NumericValue || other instanceof StringValue) {
                NodeEnumeration e1 = enumerate();
                while (e1.hasMoreElements()) {
                    NodeInfo node = e1.nextElement();
                    if (numericCompare(operator,
                                 Value.stringToNumber(node.getStringValue()),
                                 other.asNumber()))
                        return true;
                }
                return false;
            } else if (other instanceof BooleanValue) {
                return numericCompare(operator,
                                    new BooleanValue(this.asBoolean()).asNumber(),
                                    new BooleanValue(other.asBoolean()).asNumber());
            } else {
                throw new InternalSaxonError("Unknown data type in a relational expression");
            }
        }

    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "** node set value (class " + getClass() + ") **");
    }

    /**
    * Get conversion preference for this value to a Java class. A low result
    * indicates higher preference.
    */

    public int conversionPreference(Class required) {

        if (required.isAssignableFrom(NodeSetValue.class)) return 0;

        if (required==NodeList.class) return 0;
        if (required==boolean.class) return 8;
        if (required==Boolean.class) return 9;
        if (required==byte.class) return 6;
        if (required==Byte.class) return 7;
        if (required==char.class) return 4;
        if (required==Character.class) return 5;
        if (required==double.class) return 6;
        if (required==Double.class) return 7;
        if (required==float.class) return 6;
        if (required==Float.class) return 7;
        if (required==int.class) return 6;
        if (required==Integer.class) return 7;
        if (required==long.class) return 6;
        if (required==Long.class) return 7;
        if (required==short.class) return 6;
        if (required==Short.class) return 7;
        if (required==String.class) return 2;
        if (required==Object.class) return 3;
        if (required==Node.class) return 1;
        if (required==Element.class) return 1;
        if (required==Document.class) return 1;
        if (required==DocumentFragment.class) return 1;
        if (required==Attr.class) return 1;
        if (required==Comment.class) return 1;
        if (required==Text.class) return 1;
        if (required==CharacterData.class) return 1;
        if (required==ProcessingInstruction.class) return 1;
        return Integer.MAX_VALUE;
    }

    /**
    * Convert to Java object (for passing to external functions)
    */

    public Object convertToJava(Class target) throws XPathException {

        if (target.isAssignableFrom(getClass())) {
            return this;
        } else if (target==NodeEnumeration.class) {
            return this.enumerate();

        } else if (target==boolean.class) {
            return new Boolean(asBoolean());
        } else if (target==Boolean.class) {
            return new Boolean(asBoolean());

        } else if (target==Object.class || target==NodeList.class) {
            if (this instanceof NodeList) {
                return this;
            } else {
                // TODO: really need a full DocumentOrderComparer here...
                return new NodeSetExtent(this.enumerate(), new LocalOrderComparer());
            }

        } else if (target==Node.class) {
            NodeInfo node = getFirst();
            return node;

        } else if (target==Attr.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.ATTRIBUTE) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==CharacterData.class || target==Text.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.TEXT) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==Comment.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.COMMENT) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==Document.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.ROOT) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==Element.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.ELEMENT) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==ProcessingInstruction.class) {
            NodeInfo node = getFirst();
            if (node==null) return null;
            if (node.getNodeType() == NodeInfo.PI) return node;
            throw new XPathException("Node is of wrong type");

        } else if (target==String.class) {
            return asString();
        } else if (target==double.class) {
            return new Double(asNumber());
        } else if (target==Double.class) {
            return new Double(asNumber());
        } else if (target==float.class) {
            return new Float(asNumber());
        } else if (target==Float.class) {
            return new Float(asNumber());
        } else if (target==long.class) {
            return new Long((long)asNumber());
        } else if (target==Long.class) {
            return new Long((long)asNumber());
        } else if (target==int.class) {
            return new Integer((int)asNumber());
        } else if (target==Integer.class) {
            return new Integer((int)asNumber());
        } else if (target==short.class) {
            return new Short((short)asNumber());
        } else if (target==Short.class) {
            return new Short((short)asNumber());
        } else if (target==byte.class) {
            return new Byte((byte)asNumber());
        } else if (target==Byte.class) {
            return new Byte((byte)asNumber());
        } else if (target==char.class || target==Character.class) {
            String s = asString();
            if (s.length()==1) {
                return new Character(s.charAt(0));
            } else {
                throw new XPathException("Cannot convert string to Java char unless length is 1");
            }
        } else {
            throw new XPathException("Conversion of node-set to " + target.getName() +
                        " is not supported");
        }
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


package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tinytree.TinyBuilder;
import com.icl.saxon.output.*;
import javax.xml.transform.TransformerException;


/**
* This class represents a Value of type result tree fragment, specifically,
* an RTF whose root owns a single text node. <BR>
*/

public final class TextFragmentValue extends SingletonNodeSet  {

    private String text;
    private String baseURI;
    private Controller controller;

    /**
    * Constructor: create a result tree fragment containing a single text node
    * @param value: a String containing the value
    */

    public TextFragmentValue(String value, String systemId, Controller controller) {
        this.text = value;
        this.node = null;
        this.baseURI = systemId;
        this.controller = controller;
        generalUseAllowed = false;
    }

    /**
    * Convert the result tree fragment to a string.
    */

    public String asString() {
        return text;
    }

    /**
    * Evaluate an expression as a String and write the result to the
    * specified outputter.<br>
    * @param out The required outputter
    * @param context The context in which the expression is to be evaluated
    */

    public void outputStringValue(Outputter out, Context context) throws TransformerException {
        out.writeContent(text);
    }

    /**
    * Convert the result tree fragment to a number
    */

    public double asNumber() {
        return Value.stringToNumber(text);
    }

    /**
    * Convert the result tree fragment to a boolean
    */

    public boolean asBoolean() {
        return true;
    }

    /**
    * Count the nodes in the node-set.
    */

    public int getCount() {
        return 1;
    }

    /**
    * Simplify the expression
    */

    public Expression simplify() {
        // overrides method on superclass
        return this;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return the first node
    */

    public NodeInfo getFirst() {
        return getRootNode();
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() throws XPathException {
        if (!generalUseAllowed) {
            throw new XPathException("Cannot process a result tree fragment as a node-set under XSLT 1.0");
        }
        return new SingletonEnumeration(getRootNode());
    }

    /**
    * Test whether a nodeset "equals" another Value
    */

    public boolean equals(Value other) throws XPathException {
    	if (other instanceof StringValue) {					// short cut for common case
    		return text.equals(other.asString());
    	}
		return new StringValue(text).equals(other);
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws XPathException {
		return new StringValue(text).notEquals(other);
    }

    /**
    * Test how a FragmentValue compares to another Value under a relational comparison.
    */

    public boolean compare(int operator, Value other) throws XPathException {
		return new StringValue(text).compare(operator, other);
    }

    /**
    * Return the type of the value
    * @return  Value.NODESET (always)
    */

    public int getType() {
        return Value.NODESET;
    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }

    /**
    * Get the root (document) node
    */

    public DocumentInfo getRootNode() {
        if (node!=null) {        // only do it once
            return (DocumentInfo)node;
        }
        try {
            int len = text.length();
            char[] chars = new char[len];
            text.getChars(0, len, chars, 0);
            Builder builder = new TinyBuilder();
            builder.setSystemId(baseURI);
            builder.setNamePool(controller.getNamePool());  // not used
            builder.startDocument();
            builder.characters(chars, 0, len);
            builder.endDocument();
            node = builder.getCurrentDocument();
            controller.getDocumentPool().add((DocumentInfo)node, null);
            return (DocumentInfo)node;
        } catch (TransformerException err) {
            throw new InternalSaxonError("Error building temporary tree: " + err.getMessage());
        }
    }

    /**
    * Copy the result tree fragment value to a given Outputter
    */

    public void copy(Outputter out) throws TransformerException {
        out.writeContent(text);
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "** result tree fragment ** (" + text + ")");
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


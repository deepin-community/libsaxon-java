package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.InternalSaxonError;
import com.icl.saxon.om.*;
import com.icl.saxon.output.Emitter;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.TreeBuilder;
import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;
import java.util.Enumeration;
import java.util.Vector;


/**
* This class represents a Value of type result tree fragment. <BR>
* A Result Tree Fragment can be created by defining a variable in XSL whose value is defined by
* the contents of the xsl:variable element, possibly including start and end element tags. <BR>
*/

public final class FragmentValue extends SingletonNodeSet  {

    private char[] buffer = new char[4096];
    private int used = 0;
    private Vector events = new Vector(20);
    private String baseURI = null;
    private FragmentEmitter emitter = new FragmentEmitter();
    private Controller controller;

    private static AttributeCollection emptyAttributeCollection = new AttributeCollection((NamePool)null);

    private static Integer START_ELEMENT = new Integer(1);
    private static Integer END_ELEMENT = new Integer(2);
    private static Integer CHARACTERS = new Integer(5);
    private static Integer PROCESSING_INSTRUCTION = new Integer(6);
    private static Integer COMMENT = new Integer(7);
    private static Integer ESCAPING_ON = new Integer(8);
    private static Integer ESCAPING_OFF = new Integer(9);

    public FragmentValue(Controller c) {
        controller = c;
        generalUseAllowed = false;
    }

    /**
    * Set the Base URI for the nodes in the result tree fragment. This is defined to be
    * the Base URI of the relevant xsl:variable element in the stylesheet.
    */

    public void setBaseURI(String uri) {
        baseURI = uri;
    }

	/**
	* Provide a namepool
	*/

	//public void setNamePool(NamePool pool) {
	//	namePool = pool;
	//}

    /**
    * Get an Emitter that can be used to feed data to this result tree fragment
    */

    public Emitter getEmitter() {
        return emitter;
    }

    /**
    * Convert the result tree fragment to a string.
    */

    public String asString() {
        return new String(buffer, 0, used);
    }

    /**
    * Evaluate an expression as a String and write the result to the
    * specified outputter.<br>
    * @param out The required outputter
    * @param context The context in which the expression is to be evaluated
    */

    public void outputStringValue(Outputter out, Context context) throws TransformerException {
        out.writeContent(buffer, 0, used);
    }

    /**
    * Convert the result tree fragment to a number
    */

    public double asNumber() {
        return Value.stringToNumber(asString());
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
    		return asString().equals(other.asString());
    	}
		return new StringValue(asString()).equals(other);
    }

    /**
    * Test whether a nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) throws XPathException {
		return new StringValue(asString()).notEquals(other);
    }

    /**
    * Test how a FragmentValue compares to another Value under a relational comparison.
    */

    public boolean compare(int operator, Value other) throws XPathException {
		return new StringValue(asString()).compare(operator, other);
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
            Builder builder = new TreeBuilder();
            builder.setSystemId(baseURI);
            builder.setNamePool(controller.getNamePool());
            builder.startDocument();
            replay(builder);
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
        Emitter emitter = out.getEmitter();
        replay(emitter);
    }

    /**
    * Replay the saved emitter events to a new emitter
    */

    public void replay(Emitter emitter) throws TransformerException {
        Enumeration enm = events.elements();

        while (enm.hasMoreElements()) {
            Object e = enm.nextElement();
            Object e1;
            Object e2;
            Object e3;
            if (e==START_ELEMENT) {
                e1 = enm.nextElement();
                e2 = enm.nextElement();
                e3 = enm.nextElement();
                int[] namespaces = (int[])e3;
                emitter.startElement(((Integer)e1).intValue(),
                					 (AttributeCollection)e2,
                					 namespaces, namespaces.length);

            } else if (e==END_ELEMENT) {
                e1 = enm.nextElement();
                emitter.endElement(((Integer)e1).intValue());

            } else if (e==CHARACTERS) {
                e1 = enm.nextElement();
                emitter.characters(buffer, ((int[])e1)[0], ((int[])e1)[1]);

            } else if (e==PROCESSING_INSTRUCTION) {
                e1 = enm.nextElement();
                e2 = enm.nextElement();
                emitter.processingInstruction((String)e1, (String)e2);

            } else if (e==COMMENT) {
                e1 = enm.nextElement();
                emitter.comment(((String)e1).toCharArray(), 0, ((String)e1).length());

            } else if (e==ESCAPING_ON) {
                emitter.setEscaping(true);

            } else if (e==ESCAPING_OFF) {
                emitter.setEscaping(false);

            } else {
                throw new InternalSaxonError("Corrupt data in temporary tree: " + e);
            }
        }

    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "** result tree fragment **");
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Implement the Emitter interface
    //////////////////////////////////////////////////////////////////////////////////

    private class FragmentEmitter extends Emitter {

        boolean previousCharacters = false;

        /**
        * Notify document start
        */

        public void startDocument() {
            previousCharacters = false;
        }

        /**
        * Notify document end
        */

        public void endDocument() {
            previousCharacters = false;
        }

        /**
        * Output an element start tag.
        * @param name The Name object naming the element. Use the getDisplayName() method
        * to obtain the tag to display in XML output.
        * @param attributes The attributes (excluding namespace declarations) associated with
        * this element. Note that the emitter is permitted to modify this list, e.g. to add
        * namespace declarations.
        */

        public void startElement(int name, Attributes attributes,
        						 int[] namespaces, int nscount) {
            events.addElement(START_ELEMENT);
            events.addElement(new Integer(name));



            // copy the attribute collection
            AttributeCollection atts;
            int numAtts = attributes.getLength();
            if (numAtts==0) {
                atts = emptyAttributeCollection;
            } else {
                atts = new AttributeCollection((AttributeCollection)attributes);
            }

            events.addElement(atts);

            // copy the namespaces
            int[] ns = new int[nscount];
            System.arraycopy(namespaces, 0, ns, 0, nscount);
            events.addElement(ns);

            previousCharacters = false;
        }

        /**
        * Output an element end tag
        * @param name The Name object naming the element. Use the getDisplayName() method
        * to obtain the tag to display in XML output.
        */

        public void endElement(int name) {
            events.addElement(END_ELEMENT);
            events.addElement(new Integer(name));
            previousCharacters = false;
        }

        /**
        * Output character data
        */

        public void characters(char[] chars, int start, int len) {
            while (used + len >= buffer.length) {
                char[] newbuffer = new char[buffer.length * 2];
                System.arraycopy(buffer, 0, newbuffer, 0, used);
                buffer = newbuffer;
            }
            System.arraycopy(chars, start, buffer, used, len);
            if (previousCharacters) {
                // concatenate with the previous text node
                int[] v = (int[])events.elementAt(events.size()-1);
                v[1] += len;
            } else {
                events.addElement(CHARACTERS);
                int[] val = {used, len};        // objects are expensive so we only create one
                events.addElement(val);
            }
            used += len;
            previousCharacters = true;
        }

        /**
        * Output a processing instruction
        */

        public void processingInstruction(String name, String data) {
            events.addElement(PROCESSING_INSTRUCTION);
            events.addElement(name);
            events.addElement(data);
            previousCharacters = false;
        }

        /**
        * Output a comment. <br>
        */

        public void comment (char[] chars, int start, int length) {
            events.addElement(COMMENT);
            events.addElement(new String(chars, start, length));
            previousCharacters = false;
        }

        /**
        * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
        * is used to switch escaping on or off.
        */

        public void setEscaping(boolean escaping) throws TransformerException {
            events.addElement((escaping ? ESCAPING_ON : ESCAPING_OFF));
            previousCharacters = false;
        }

    }   // end of inner class FragmentEmitter


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


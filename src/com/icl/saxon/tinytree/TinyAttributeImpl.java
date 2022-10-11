package com.icl.saxon.tinytree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

import javax.xml.transform.TransformerException;


/**
  * A node in the XML parse tree representing an attribute. Note that this is
  * generated only "on demand", when the attribute is selected by a select pattern.<P>
  * @author Michael H. Kay
  */

final class TinyAttributeImpl extends TinyNodeImpl implements Attr {

    public TinyAttributeImpl(TinyDocumentImpl doc, int nodeNr) {
        this.document = doc;
        this.nodeNr = nodeNr;
    }

    /**
    * Get the parent node
    */

    public NodeInfo getParent() {
        return document.getNode(document.attParent[nodeNr]);
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    */

    protected long getSequenceNumber() {
        // need the variable as workaround for a Java HotSpot problem, reported 11 Oct 2000
        long z =
            ((TinyNodeImpl)getParent()).getSequenceNumber()
            + 0x8000 +
            (nodeNr - document.offset[document.attParent[nodeNr]]);
        return z;
        // note the 0x8000 is to leave room for namespace nodes
    }

    /**
    * Return the type of node.
    * @return Node.ATTRIBUTE
    */

    public final short getNodeType() {
        return ATTRIBUTE;
    }

    /**
    * Return the character value of the node.
    * @return the attribute value
    */

    public String getStringValue() {
        return document.attValue[nodeNr];
    }

	/**
	* Get the fingerprint of the node, used for matching names
	*/

	public int getFingerprint() {
		return document.attCode[nodeNr] & 0xfffff;
	}

	/**
	* Get the name code of the node, used for finding names in the name pool
	*/

	public int getNameCode() {
		return document.attCode[nodeNr];
	}

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return null.
    */

    public String getPrefix() {
    	int code = document.attCode[nodeNr];
    	if ((code>>20 & 0xff) == 0) return "";
    	return document.getNamePool().getPrefix(code);
    }

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        return document.getNamePool().getDisplayName(document.attCode[nodeNr]);
    }


    /**
    * Get the local name of this node.
    * @return The local name of this node.
    * For a node with no name, return an empty string.
    */

    public String getLocalName() {
        return document.getNamePool().getLocalName(document.attCode[nodeNr]);
    }

    /**
    * Get the URI part of the name of this node.
    * @return The URI of the namespace of this node. For the default namespace, return an
    * empty string
    */

    public final String getURI() {
        return document.getNamePool().getURI(document.attCode[nodeNr]);
    }

    /**
    * Generate id. Returns key of owning element with the attribute name as a suffix
    */

    public String generateId() {
        return (getParent()).generateId() + "_a" + nodeNr;
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException {
		int nameCode = document.attCode[nodeNr];
    	if ((nameCode>>20 & 0xff) != 0) {	// non-null prefix
    		// check there is no conflict of namespaces
			nameCode = out.checkAttributePrefix(nameCode);
		}
        out.writeAttribute(nameCode, getStringValue());
    }

    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return getParent().getLineNumber();
    }

    /**
     * The type information associated with this attribute. While the type
     * information contained in this attribute is guarantee to be correct
     * after loading the document or invoking
     * <code>Document.normalizeDocument()</code>, <code>schemaTypeInfo</code>
     * may not be reliable if the node was moved.
     *
     * @since DOM Level 3
     */
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    /**
     * Returns whether this attribute is known to be of type ID (i.e. to
     * contain an identifier for its owner element) or not. When it is and
     * its value is unique, the <code>ownerElement</code> of this attribute
     * can be retrieved using the method <code>Document.getElementById</code>
     * . The implementation could use several ways to determine if an
     * attribute node is known to contain an identifier:
     * <ul>
     * <li> If validation
     * occurred using an XML Schema [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>]
     * while loading the document or while invoking
     * <code>Document.normalizeDocument()</code>, the post-schema-validation
     * infoset contributions (PSVI contributions) values are used to
     * determine if this attribute is a schema-determined ID attribute using
     * the <a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/#term-sdi'>
     * schema-determined ID</a> definition in [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
     * .
     * </li>
     * <li> If validation occurred using a DTD while loading the document or
     * while invoking <code>Document.normalizeDocument()</code>, the infoset <b>[type definition]</b> value is used to determine if this attribute is a DTD-determined ID
     * attribute using the <a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/#term-ddi'>
     * DTD-determined ID</a> definition in [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
     * .
     * </li>
     * <li> from the use of the methods <code>Element.setIdAttribute()</code>,
     * <code>Element.setIdAttributeNS()</code>, or
     * <code>Element.setIdAttributeNode()</code>, i.e. it is an
     * user-determined ID attribute;
     * <p ><b>Note:</b>  XPointer framework (see section 3.2 in [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
     * ) consider the DOM user-determined ID attribute as being part of the
     * XPointer externally-determined ID definition.
     * </li>
     * <li> using mechanisms that
     * are outside the scope of this specification, it is then an
     * externally-determined ID attribute. This includes using schema
     * languages different from XML schema and DTD.
     * </li>
     * </ul>
     * <br> If validation occurred while invoking
     * <code>Document.normalizeDocument()</code>, all user-determined ID
     * attributes are reset and all attribute nodes ID information are then
     * reevaluated in accordance to the schema used. As a consequence, if
     * the <code>Attr.schemaTypeInfo</code> attribute contains an ID type,
     * <code>isId</code> will always return true.
     *
     * @since DOM Level 3
     */
    public boolean isId() {
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

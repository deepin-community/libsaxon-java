package com.icl.saxon.om;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.NodeTest;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top class in the interface hierarchy for nodes; see NodeImpl for the implementation
  * hierarchy.
  * @author Michael H. Kay
  */

public interface NodeInfo extends Source {

    // Node types. "NODE" means any type.
    // These node numbers should be kept aligned with those defined in the DOM.

    public static final short NODE = 0;       // matches any kind of node
    public static final short ELEMENT = 1;
    public static final short ATTRIBUTE = 2;
    public static final short TEXT = 3;
    public static final short PI = 7;
    public static final short COMMENT = 8;
    public static final short ROOT = 9;
    public static final short NAMESPACE = 13;
    public static final short NUMBER_OF_TYPES = 13;
    public static final short NONE = 9999;    // a test for this node type will never be satisfied

    /**
    * Return the type of node.
    * @return one of the values Node.ELEMENT, Node.TEXT, Node.ATTRIBUTE, etc.
    */

    public short getNodeType();

    /**
    * Determine whether this is the same node as another node. <br />
    * Note: a.isSameNodeInfo(b) if and only if generateId(a)==generateId(b)
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNodeInfo(NodeInfo other);

    /**
    * Get the System ID for the node.
    * @return the System Identifier of the entity in the source document containing the node,
    * or null if not known. Note this is not the same as the base URI: the base URI can be
    * modified by xml:base, but the system ID cannot.
    */

    public String getSystemId();

    /**
    * Get the Base URI for the node, that is, the URI used for resolving a relative URI contained
    * in the node. This will be the same as the System ID unless xml:base has been used.
    */

    public String getBaseURI();

    /**
    * Get line number
    * @return the line number of the node in its original source document; or -1 if not available
    */

    public int getLineNumber();

    /**
    * Determine the relative position of this node and another node, in document order.
    * The other node will always be in the same document.
    * @param other The other node, whose position is to be compared with this node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public int compareOrder(NodeInfo other);

    /**
    * Return the string value of the node. The interpretation of this depends on the type
    * of node. For an element it is the accumulated character content of the element,
    * including descendant elements.
    * @return the string value of the node
    */

    public String getStringValue();

	/**
	* Get name code. The name code is a coded form of the node name: two nodes
	* with the same name code have the same namespace URI, the same local name,
	* and the same prefix. By masking the name code with &0xfffff, you get a
	* fingerprint: two nodes with the same fingerprint have the same local name
	* and namespace URI.
    * @see com.icl.saxon.om.NamePool#allocate allocate
    * @see com.icl.saxon.om.NamePool#getFingerprint getFingerprint
	*/

	public int getNameCode();

	/**
	* Get fingerprint. The fingerprint is a coded form of the expanded name
	* of the node: two nodes
	* with the same name code have the same namespace URI and the same local name.
	* A fingerprint of -1 should be returned for a node with no name.
	*/

	public int getFingerprint();

    /**
    * Get the local part of the name of this node. This is the name after the ":" if any.
    * @return the local part of the name. For an unnamed node, return an empty string.
    */

    public String getLocalName();

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return an empty string.
    */

    public String getPrefix();

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, return null.
    * For a node with an empty prefix, return an empty string.
    */

    public String getURI();

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName();

    /**
    * Get the NodeInfo object representing the parent of this node
    */

    public NodeInfo getParent();

    /**
    * Return an enumeration over the nodes reached by the given axis from this node
    * @param axisNumber the axis to be followed (a constant in class {@link Axis})
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return a NodeEnumeration that scans the nodes reached by the axis in turn.
    */

    public AxisEnumeration getEnumeration(byte axisNumber, NodeTest nodeTest);

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param uri the namespace uri of an attribute ("" if no namespace)
     * @param localName the local name of the attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue(String uri, String localName);

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint);

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot();

    /**
    * Determine whether the node has any children. <br />
    * Note: the result is equivalent to <br />
    * getEnumeration(Axis.CHILD, AnyNodeTest.getInstance()).hasMoreElements()
    */

    public boolean hasChildNodes();

    /**
    * Get a character string that uniquely identifies this node.<br />
    * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b)
    * @return a string that uniquely identifies this node, within this
    * document. The calling code prepends information to make the result
    * unique across all documents.
    */

    public String generateId();

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException;

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws TransformerException;

    /**
    * Output all namespace nodes associated with this element. Does nothing if
    * the node is not an element.
    * @param out The relevant outputter
    * @param includeAncestors True if namespaces declared on ancestor elements must
    * be output; false if it is known that these are already on the result tree
    */

    public void outputNamespaceNodes(Outputter out, boolean includeAncestors)
        throws TransformerException;

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

package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Axis;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.SingletonEnumeration;
import com.icl.saxon.om.EmptyEnumeration;
import com.icl.saxon.om.AbstractNode;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.output.Outputter;

import org.w3c.dom.*;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;


/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top-level class in the implementation class hierarchy; it essentially contains
  * all those methods that can be defined using other primitive methods, without direct access
  * to data.
  * @author Michael H. Kay
  */

abstract public class NodeImpl extends AbstractNode {

    protected static NodeInfo[] emptyArray = new NodeInfo[0];

    protected ParentNodeImpl parent;
    protected int index;

    /**
    * Set the system ID of this node. This method is provided so that a NodeInfo
    * implements the javax.xml.transform.Source interface, allowing a node to be
    * used directly as the Source of a transformation
    */

    public void setSystemId(String uri) {
        // overridden in DocumentImpl and ElementImpl
        getParent().setSystemId(uri);
    }

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNodeInfo(NodeInfo other) {
        // default implementation: differs for attribute and namespace nodes
        return this==other;
    }

	/**
	* Get the nameCode of the node. This is used to locate the name in the NamePool
	*/

	public int getNameCode() {
		// default implementation: return -1 for an unnamed node
		return -1;
	}

	/**
	* Get the fingerprint of the node. This is used to compare whether two nodes
	* have equivalent names. Return -1 for a node with no name.
	*/

	public int getFingerprint() {
	    int nameCode = getNameCode();
	    if (nameCode==-1) return -1;
		return nameCode & 0xfffff;
	}

    /**
    * Get a character string that uniquely identifies this node within this document
    * (The calling code will prepend a document identifier)
    * @return a string.
    */

    public String generateId() {
        return "" + NODE_LETTER[getNodeType()] +
            getSequenceNumber();
    }

	/**
	* Get the node corresponding to this javax.xml.transform.dom.DOMLocator
	*/

    public Node getOriginatingNode() {
        return this;
    }

    /**
    * Get the system ID for the node. Default implementation for child nodes.
    */

    public String getSystemId() {
        return parent.getSystemId();
    }

    /**
    * Get the base URI for the node. Default implementation for child nodes.
    */

    public String getBaseURI() {
        return parent.getBaseURI();
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    * This is the default implementation for child nodes.
    */

    protected long getSequenceNumber() {
        NodeImpl prev = this;
        for (int i=0;; i++) {
            if (prev instanceof ParentNodeImpl) {
                return prev.getSequenceNumber() + 0x10000 + i;
                // note the 0x10000 is to leave room for namespace and attribute nodes.
            }
            prev = prev.getPreviousInDocument();
        }

    }

    /**
    * Determine the relative position of this node and another node, in document order.
    * The other node will always be in the same document.
    * @param other The other node, whose position is to be compared with this node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public final int compareOrder(NodeInfo other) {
        long a = getSequenceNumber();
        long b = ((NodeImpl)other).getSequenceNumber();
        if (a<b) return -1;
        if (a>b) return +1;
        return 0;
    }

	/**
	* Get the NamePool
	*/

	public NamePool getNamePool() {
		return getDocumentRoot().getNamePool();
	}

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return an empty string.
    */

    public String getPrefix() {
    	int nameCode = getNameCode();
    	if (nameCode == -1) return "";
    	if ((nameCode>>20 & 0xff) == 0) return "";
        return getNamePool().getPrefix(nameCode);
    }

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For the default namespace, return an
    * empty string. For an unnamed node, return null.
    */

    public String getURI() {
    	int nameCode = getNameCode();
    	if (nameCode == -1) return null;
        return getNamePool().getURI(nameCode);
    }

    /**
    * Get the name of this node, following the DOM rules
    * @return The name of the node. For an element this is the element name, for an attribute
    * it is the attribute name, as a QName. Other node types return conventional names such
    * as "#text" or "#comment"
    */

    //public String getNodeName() {
        // default implementation
    //    return getDisplayName();
    //}


    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
    	int nameCode = getNameCode();
    	if (nameCode == -1) return "";
        return getNamePool().getDisplayName(nameCode);
    }

    /**
    * Get the local name of this node.
    * @return The local name of this node.
    * For a node with no name, return an empty string.
    */

    public String getLocalName() {
    	int nameCode = getNameCode();
    	if (nameCode == -1) return "";
        return getNamePool().getLocalName(nameCode);
    }

    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return parent.getLineNumber();
    }

    /**
    * Get the column number of the node. This is not maintained, so
    * return -1
    */

    //public int getColumnNumber() {
    //    return -1;
    //}


    /**
    * Get the public identifier of the document entity containing this node. This
    * is not currently maintained: return null
    */

    //public String getPublicId() {
    //    return null;
    //}

    /**
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    public final NodeInfo getParent()  {
        return parent;
    }

    /**
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    //public final Node getParentNode()  {
    //    return parent;
    //}

    /**
    * Get the previous sibling of the node
    * @return The previous sibling node. Returns null if the current node is the first
    * child of its parent.
    */

    public Node getPreviousSibling()  {
        return parent.getNthChild(index-1);
    }


   /**
    * Get next sibling node
    * @return The next sibling node of the required type. Returns null if the current node is the last
    * child of its parent.
    */

    public Node getNextSibling()  {
        return parent.getNthChild(index+1);
    }

    /**
    * Get first child - default implementation used for leaf nodes
    * @return null
    */

    public Node getFirstChild()  {
        return null;
    }

    /**
    * Get last child - default implementation used for leaf nodes
    * @return null
    */

    public Node getLastChild()  {
        return null;
    }

    /**
    * Return an enumeration over the nodes reached by the given axis from this node
    * @param node NodeInfo representing the node from which the enumeration starts
    * @param nodeType the type(s) of node to be included, e.g. NodeInfo.ELEMENT, NodeInfo.TEXT.
    * The value NodeInfo.NODE means include any type of node.
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return an AxisEnumeration that scans the nodes reached by the axis in turn.
    */

    public AxisEnumeration getEnumeration(
                                        byte axisNumber,
                                        NodeTest nodeTest) {

        switch (axisNumber) {
            case Axis.ANCESTOR:
                 return new AncestorEnumeration(this, nodeTest, false);

            case Axis.ANCESTOR_OR_SELF:
                 return new AncestorEnumeration(this, nodeTest, true);

            case Axis.ATTRIBUTE:
                 if (this.getNodeType()!=ELEMENT) return EmptyEnumeration.getInstance();
                 return new AttributeEnumeration(this, nodeTest);

            case Axis.CHILD:
                 if (this instanceof ParentNodeImpl) {
                    return ((ParentNodeImpl)this).enumerateChildren(nodeTest);
                 } else {
                    return EmptyEnumeration.getInstance();
                 }

            case Axis.DESCENDANT:
                if (getNodeType()==ROOT &&
                        nodeTest instanceof NameTest &&
                        nodeTest.getNodeType()==ELEMENT) {
                    return ((DocumentImpl)this).getAllElements(
                                ((NameTest)nodeTest).getFingerprint());
                } else if (hasChildNodes()) {
                    return new DescendantEnumeration(this, nodeTest, false);
                } else {
                    return EmptyEnumeration.getInstance();
                }

            case Axis.DESCENDANT_OR_SELF:
                 return new DescendantEnumeration(this, nodeTest, true);

            case Axis.FOLLOWING:
                 return new FollowingEnumeration(this, nodeTest);

            case Axis.FOLLOWING_SIBLING:
                 return new FollowingSiblingEnumeration(this, nodeTest);

            case Axis.NAMESPACE:
                 if (this.getNodeType()!=ELEMENT) return EmptyEnumeration.getInstance();
                 return new NamespaceEnumeration(this, nodeTest);

            case Axis.PARENT:
                 NodeInfo parent = (NodeInfo)getParentNode();
                 if (parent==null) return EmptyEnumeration.getInstance();
                 if (nodeTest.matches(parent)) return new SingletonEnumeration(parent);
                 return EmptyEnumeration.getInstance();

            case Axis.PRECEDING:
                 return new PrecedingEnumeration(this, nodeTest);

            case Axis.PRECEDING_SIBLING:
                 return new PrecedingSiblingEnumeration(this, nodeTest);

            case Axis.SELF:
                 if (nodeTest.matches(this)) return new SingletonEnumeration(this);
                 return EmptyEnumeration.getInstance();

            case Axis.PRECEDING_OR_ANCESTOR:
                 return new PrecedingOrAncestorEnumeration(this, nodeTest);

            default:
                 throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public boolean hasAttributes() {
        return false;
    }

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param uri the namespace uri of an attribute
     * @param localName the local name of an attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue( String uri, String localName ) {
        return null;
    }

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param name the name of an attribute. This must be an unqualified attribute name,
     * i.e. one with no namespace prefix.
     * @return the value of the attribute, if it exists, otherwise null
     */

    //public String getAttributeValue( String name ) {
    //    return null;
    //}

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint) {
    	return null;
    }

    /**
     * Get the outermost element.
     * @return the Element node for the outermost element of the document. If the document is
     * not well-formed, this returns the last element child of the root if there is one, otherwise
     * null.
     */

    public Element getDocumentElement() {
        return ((DocumentImpl)getDocumentRoot()).getDocumentElement();

    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        return getParent().getDocumentRoot();
    }

    /**
    * Get the next node in document order
    * @param anchor: the scan stops when it reaches a node that is not a descendant of the specified
    * anchor node
    * @return the next node in the document, or null if there is no such node
    */

    public NodeImpl getNextInDocument(NodeImpl anchor) {
        // find the first child node if there is one; otherwise the next sibling node
        // if there is one; otherwise the next sibling of the parent, grandparent, etc, up to the anchor element.
        // If this yields no result, return null.

        NodeImpl next = (NodeImpl)getFirstChild();
        if (next!=null) return next;
        if (this==anchor) return null;
        next = (NodeImpl)getNextSibling();
        if (next!=null) return next;
        NodeImpl parent = this;
        while (true) {
            parent = (NodeImpl)parent.getParent();
            if (parent==null) return null;
            if (parent==anchor) return null;
            next = (NodeImpl)parent.getNextSibling();
            if (next!=null) return next;
        }
    }


    /**
    * Get the previous node in document order
    * @return the previous node in the document, or null if there is no such node
    */

    public NodeImpl getPreviousInDocument() {

        // finds the last child of the previous sibling if there is one;
        // otherwise the previous sibling element if there is one;
        // otherwise the parent, up to the anchor element.
        // If this reaches the document root, return null.

        NodeImpl prev = (NodeImpl)getPreviousSibling();
        if (prev!=null) {
            return ((NodeImpl)prev).getLastDescendantOrSelf();
        }
        return (NodeImpl)getParentNode();
    }

    private NodeImpl getLastDescendantOrSelf() {
        NodeImpl last = (NodeImpl)getLastChild();
        if (last==null) return this;
        return last.getLastDescendantOrSelf();
    }

    /**
    * Output all namespace nodes associated with this element. Does nothing if
    * the node is not an element.
    * @param out The relevant outputter
    * @param includeAncestors True if namespaces declared on ancestor elements must
    * be output; false if it is known that these are already on the result tree
    */

    public void outputNamespaceNodes(Outputter out, boolean includeAncestors)
        throws TransformerException
    {}

    /**
    * Remove this node from the tree. For system use only.
    * When one or more nodes have been removed, renumberChildren()
    * must be called to adjust the numbering of remaining nodes.
    * PRECONDITION: The node must have a parent node.
    */

    public void removeNode() {
        parent.removeChild(index);
    }


    // implement DOM Node methods

    /**
    * Get the node value as defined in the DOM. This is not the same as the XPath string-value.
    */

    //public String getNodeValue() {
        // default implementation
    //    return getStringValue();
    //}

    /**
     * Return a <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     */

    public NodeList getChildNodes() {
        try {
            return new NodeSetExtent(EmptyEnumeration.getInstance(), null);
        } catch (XPathException err) {
            return null;
        }
    }

    /**
     * Return a <code>NamedNodeMap</code> containing the attributes of this node (if
     * it is an <code>Element</code> ) or <code>null</code> otherwise. (DOM method)
     */

    public NamedNodeMap getAttributes() {
        // default implementation
        return null;
    }



    /**
     * Determine whether the node has any children.
     * @return  <code>true</code> if the node has any children,
     *   <code>false</code> if the node has no children.
     */

    public boolean hasChildNodes() {
        return getFirstChild() != null;
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

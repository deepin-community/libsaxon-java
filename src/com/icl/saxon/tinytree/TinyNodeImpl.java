package com.icl.saxon.tinytree;
import com.icl.saxon.om.*;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NodeTest;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;


/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top-level class in the implementation class hierarchy; it essentially contains
  * all those methods that can be defined using other primitive methods, without direct access
  * to data.
  * @author Michael H. Kay
  */

abstract class TinyNodeImpl extends AbstractNode {

    protected TinyDocumentImpl document;
    protected int nodeNr;
    protected TinyNodeImpl parent = null;

    /**
    * Set the system id of this node. <br />
    * This method is present to ensure that
    * the class implements the javax.xml.transform.Source interface, so a node can
    * be used as the source of a transformation.
    */

    public void setSystemId(String uri) {
        short type = document.nodeType[nodeNr];
        if (type==ATTRIBUTE || type==NAMESPACE) {
            ((TinyNodeImpl)getParent()).setSystemId(uri);
        } else {
            document.setSystemId(nodeNr, uri);
        }
    }

    /**
    * Set the parent of this node. Providing this information is useful,
    * if it is known, because otherwise getParent() has to search backwards
    * through the document.
    */

    protected void setParentNode(TinyNodeImpl parent) {
        this.parent = parent;
    }

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNodeInfo(NodeInfo other) {
        if (this==other) return true;
        if (!(other instanceof TinyNodeImpl)) return false;
        if (this.getNodeType() != other.getNodeType()) return false;    // "Limitation" 7
        return this.document==((TinyNodeImpl)other).document &&
             this.nodeNr==((TinyNodeImpl)other).nodeNr;
    }

    /**
    * Get the system ID for the entity containing the node.
    */

    public String getSystemId() {
        return document.getSystemId(nodeNr);
    }

    /**
    * Get the base URI for the node. Default implementation for child nodes gets
    * the base URI of the parent node.
    */

    public String getBaseURI() {
        return (getParent()).getBaseURI();
    }

	/**
	* Get the node corresponding to this javax.xml.transform.dom.DOMLocator
	*/

    public Node getOriginatingNode() {
        return this;
    }


    /**
    * Set the line number of the node within its source document entity
    */

    public void setLineNumber(int line) {
        document.setLineNumber(nodeNr, line);
    }


    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return document.getLineNumber(nodeNr);
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. The sequence number must be unique within the document (not, as in
    * previous releases, within the whole document collection)
    */

    protected long getSequenceNumber() {
        return (long)nodeNr << 32;
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
        long b = ((TinyNodeImpl)other).getSequenceNumber();
        if (a<b) return -1;
        if (a>b) return +1;
        return 0;
    }

	/**
	* Get the fingerprint of the node, used for matching names
	*/

	public int getFingerprint() {
	    int nc = getNameCode();
	    if (nc==-1) return -1;
		return nc & 0xfffff;
	}

	/**
	* Get the name code of the node, used for matching names
	*/

	public int getNameCode() {
	    // overridden for attributes and namespace nodes.
		return document.nameCode[nodeNr];
	}

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return "".
    */

    public String getPrefix() {
        int code = document.nameCode[nodeNr];
        if (code<0) return "";
        if ((code>>20 & 0xff) == 0) return "";
        return document.getNamePool().getPrefix(code);
    }

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, or for
    * an element or attribute in the default namespace, return an empty string.
    */

    public String getURI() {
        int code = document.nameCode[nodeNr];
        if (code<0) return "";
        return document.getNamePool().getURI(code);
    }

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        int code = document.nameCode[nodeNr];
        if (code<0) return "";
        return document.getNamePool().getDisplayName(code);
    }

    /**
    * Get the local name of this node.
    * @return The local name of this node.
    * For a node with no name, return an empty string.
    */

    public String getLocalName() {
        int code = document.nameCode[nodeNr];
        if (code<0) return "";
        return document.getNamePool().getLocalName(code);
    }

    /**
    * Return an enumeration over the nodes reached by the given axis from this node
    * @param axisNumber Identifies the required axis, eg. Axis.CHILD or Axis.PARENT
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return a AxisEnumeration that scans the nodes reached by the axis in turn.
    */

    public AxisEnumeration getEnumeration(
                                        byte axisNumber,
                                        NodeTest nodeTest) {

        // System.err.println("Get enumeration of axis " + axisNumber + " from " + generateId());

        short type = getNodeType();
        switch (axisNumber) {
            case Axis.ANCESTOR:
                if (type==ROOT) {
                    return EmptyEnumeration.getInstance();
                } else {
                    return new AncestorEnumeration(document, this, nodeTest, false);
                }

            case Axis.ANCESTOR_OR_SELF:
                if (type==ROOT) {
                    if (nodeTest.matches(this)) {
                        return new SingletonEnumeration(this);
                    } else {
                        return EmptyEnumeration.getInstance();
                    }
                } else {
                    return new AncestorEnumeration(document, this, nodeTest, true);
                }

            case Axis.ATTRIBUTE:
                 if (type!=ELEMENT) return EmptyEnumeration.getInstance();
                 if (document.offset[nodeNr]<0) return EmptyEnumeration.getInstance();
                 return new AttributeEnumeration(document, nodeNr, nodeTest);

            case Axis.CHILD:
                 if (hasChildNodes()) {
                    return new SiblingEnumeration(document, this, nodeTest, true);
                 } else {
                    return EmptyEnumeration.getInstance();
                 }

            case Axis.DESCENDANT:
                if (type==ROOT &&
                        nodeTest instanceof NameTest &&
                        nodeTest.getNodeType()==ELEMENT) {
                    return ((TinyDocumentImpl)this).getAllElements(
                                ((NameTest)nodeTest).getFingerprint());
                } else if (hasChildNodes()) {
                    return new DescendantEnumeration(document, this, nodeTest, false);
                } else {
                    return EmptyEnumeration.getInstance();
                }

            case Axis.DESCENDANT_OR_SELF:
                 if (hasChildNodes()) {
                    return new DescendantEnumeration(document, this, nodeTest, true);
                 } else {
                    if (nodeTest.matches(this)) {
                        return new SingletonEnumeration(this);
                    } else {
                        return EmptyEnumeration.getInstance();
                    }
                 }
            case Axis.FOLLOWING:
                if (type==ROOT) {
                    return EmptyEnumeration.getInstance();
                } else if (type==ATTRIBUTE || type==NAMESPACE) {
                    return new FollowingEnumeration(
                                document, (TinyNodeImpl)getParent(), nodeTest, true);
                } else {
                    return new FollowingEnumeration(
                                document, this, nodeTest, false);
                }

            case Axis.FOLLOWING_SIBLING:
                if (type==ROOT || type==ATTRIBUTE || type==NAMESPACE) {
                    return EmptyEnumeration.getInstance();
                } else {
                    return new SiblingEnumeration(
                                document, this, nodeTest, false);
                }

            case Axis.NAMESPACE:
                if (type!=ELEMENT) return EmptyEnumeration.getInstance();
                return new NamespaceEnumeration((TinyElementImpl)this, nodeTest);

            case Axis.PARENT:
                 NodeInfo parent = (NodeInfo)getParent();
                 if (parent==null) return EmptyEnumeration.getInstance();
                 if (nodeTest.matches(parent)) return new SingletonEnumeration(parent);
                 return EmptyEnumeration.getInstance();

            case Axis.PRECEDING:
                if (type==ROOT) {
                    return EmptyEnumeration.getInstance();
                } else if (type==ATTRIBUTE || type==NAMESPACE) {
                    return new PrecedingEnumeration(
                                document, (TinyNodeImpl)getParent(), nodeTest, false);
                } else {
                    return new PrecedingEnumeration(
                                document, this, nodeTest, false);
                }

            case Axis.PRECEDING_SIBLING:
                if (type==ROOT || type==ATTRIBUTE || type==NAMESPACE) {
                    return EmptyEnumeration.getInstance();
                } else {
                    return new PrecedingSiblingEnumeration(
                                document, this, nodeTest);
                }

            case Axis.SELF:
                if (nodeTest.matches(this)) return new SingletonEnumeration(this);
                return EmptyEnumeration.getInstance();

            case Axis.PRECEDING_OR_ANCESTOR:
                if (type==ROOT) {
                    return EmptyEnumeration.getInstance();
                } else if (type==ATTRIBUTE || type==NAMESPACE) {
                    // See test numb32.
                    TinyNodeImpl el = (TinyNodeImpl)getParent();
                    return new PrependIterator(el, new PrecedingEnumeration(document, el, nodeTest, true));
//                    return new PrecedingEnumeration(
//                                document, (TinyNodeImpl)getParent(), nodeTest, true);
                } else {
                    return new PrecedingEnumeration(
                                document, this, nodeTest, true);
                }

            default:
                 throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }
    }

    /**
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    public NodeInfo getParent()  {
        if (parent!=null) return parent;

        // if parent is unknown, search backwards for it
        for (int i=nodeNr-1; i>=0; i--) {
            if (document.depth[i]<document.depth[nodeNr]) {
                parent = document.getNode(i);
                return parent;
            }
        }
        parent = document;
        return parent;
    }

    /**
    * Determine whether the node has any children.
    * @return <code>true</code> if this node has any attributes,
    *   <code>false</code> otherwise.
    */

    public boolean hasChildNodes() {
        // overridden in TinyParentNodeImpl
        return false;
    }

    /**
     * Returns whether this node has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public boolean hasAttributes() {
        // overridden in TinyElementImpl
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
        // overridden in TinyElementImpl
    //    return null;
    //}


    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint) {
        // overridden in TElementImpl
    	return null;
    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        return document;
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
    * Get a character string that uniquely identifies this node
    * @return a string.
    */

    public String generateId() {
        return document.generateId() +
                NODE_LETTER[getNodeType()] +
                nodeNr;
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

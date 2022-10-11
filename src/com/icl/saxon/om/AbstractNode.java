package com.icl.saxon.om;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NodeTest;
import com.icl.saxon.pattern.NodeTypeTest;
import com.icl.saxon.sort.LocalOrderComparer;
import com.icl.saxon.tree.DOMExceptionImpl;
import org.w3c.dom.*;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMLocator;


/**
  * This class is an abstract implementation of the Saxon NodeInfo interface;
  * it also contains concrete implementations of most of the DOM methods in terms
  * of the NodeInfo methods. These include all the methods defined on the DOM Node
  * class itself, and most of those defined on subclasses such as Document, Text,
  * and Comment: because
  * of the absence of multiple inheritance, this is the only way of making these
  * methods reusable by multiple implementations.
  * The class contains no data, and can be used as a common
  * superclass for different implementations of Node and NodeInfo.
  * @author Michael H. Kay
  */

public abstract class AbstractNode implements Node, NodeInfo, SourceLocator, DOMLocator {

    /**
    * Chararacteristic letters to identify each type of node, indexed using the node type
    * values. These are used as the initial letter of the result of generate-id()
    */

    public static final char[] NODE_LETTER =
        {'x', 'e', 'a', 't', 'x', 'x', 'x', 'p', 'c', 'r', 'x', 'x', 'x', 'n'};

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public abstract boolean isSameNodeInfo(NodeInfo other);

    /**
    * Get a character string that uniquely identifies this node
    * @return a string.
    */

    public abstract String generateId();

    /**
    * Get the system ID for the entity containing the node.
    */

    public abstract String getSystemId();

    /**
    * Get the base URI for the node. Default implementation for child nodes gets
    * the base URI of the parent node.
    */

    public abstract String getBaseURI();

	/**
	* Get the node corresponding to this javax.xml.transform.dom.DOMLocator
	*/

    public Node getOriginatingNode() {
        return this;
    }

    /**
    * Determine the relative position of this node and another node, in document order.
    * The other node will always be in the same document.
    * @param other The other node, whose position is to be compared with this node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public abstract int compareOrder(NodeInfo other);

	/**
	* Get the name code of the node, used for displaying names
	*/

	public abstract int getNameCode();

	/**
	* Get the fingerprint of the node, used for matching names
	*/

	public abstract int getFingerprint();

    /**
    * Get the name of this node, following the DOM rules
    * @return The name of the node. For an element this is the element name, for an attribute
    * it is the attribute name, as a QName. Other node types return conventional names such
    * as "#text" or "#comment"
    */

    public String getNodeName() {
        switch (getNodeType()) {
            case NodeInfo.ROOT:
                return "#document";
            case NodeInfo.ELEMENT:
                return getDisplayName();
            case NodeInfo.ATTRIBUTE:
                return getDisplayName();
            case NodeInfo.TEXT:
                return "#text";
            case NodeInfo.COMMENT:
                return "#comment";
            case NodeInfo.PI:
                return getLocalName();
            case NodeInfo.NAMESPACE:
                return getLocalName();
            default:
                return "#unknown";
       }
    }

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return "".
    */

    public abstract String getPrefix();

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, or for
    * an element or attribute in the default namespace, return an empty string.
    */

    public abstract String getURI();

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        String localName = getLocalName();
        if ("".equals(localName)) {
            return "";
        }
        String prefix = getPrefix();
        if ("".equals(prefix)) {
            return localName;
        }
        return prefix + ":" + localName;
    }

    /**
    * Get the local name of this node.
    * @return The local name of this node.
    * For a node with no name, return an empty string.
    */

    public abstract String getLocalName();

    /**
    * Determine whether the node has any children.
    * @return <code>true</code> if this node has any attributes,
    *   <code>false</code> otherwise.
    */

    public abstract boolean hasChildNodes();

    /**
     * Returns whether this node has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public abstract boolean hasAttributes();

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param uri the namespace uri of an attribute
     * @param localName the local name of an attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public abstract String getAttributeValue( String uri, String localName );

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public abstract String getAttributeValue(int fingerprint);

    /**
    * Get the line number of the node within its source document entity.
    * The default implementation returns -1, meaning unknown
    */

    public int getLineNumber() {
        return -1;
    }

    /**
    * Get the column number of the node.
    * The default implementation returns -1, meaning unknown
    */

    public int getColumnNumber() {
        return -1;
    }

    /**
    * Get the public identifier of the document entity containing this node.
    * The default implementation returns null, meaning unknown
    */

    public String getPublicId() {
        return null;
    }

    /**
    * Return an enumeration over the nodes reached by the given axis from this node
    * @param axisNumber The axis to be used (a constant in class {@link Axis})
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return a AxisEnumeration that scans the nodes reached by the axis in turn.
    */

    public abstract AxisEnumeration getEnumeration(
                                        byte axisNumber,
                                        NodeTest nodeTest);

    /**
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    public abstract NodeInfo getParent();

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        NodeInfo parent = this;
        while (parent.getNodeType() != NodeInfo.ROOT) {
            parent = parent.getParent();
        }
        return (DocumentInfo)parent;
    }

    /**
     * Find the parent node of this node (DOM method).
     * @return The Node object describing the containing element or root node.
     */

    public Node getParentNode()  {
        return (Node)getParent();
    }

    /**
    * Get the previous sibling of the node (DOM method)
    * @return The previous sibling node. Returns null if the current node is the first
    * child of its parent.
    */

    public Node getPreviousSibling()  {
        AxisEnumeration prev =
            getEnumeration(Axis.PRECEDING_SIBLING, AnyNodeTest.getInstance());
        if (prev.hasMoreElements()) {
            return (Node)prev.nextElement();
        } else {
            return null;
        }
    }

   /**
    * Get next sibling node (DOM method)
    * @return The next sibling node. Returns null if the current node is the last
    * child of its parent.
    */

    public Node getNextSibling()  {
        AxisEnumeration foll =
            getEnumeration(Axis.FOLLOWING_SIBLING, AnyNodeTest.getInstance());
        if (foll.hasMoreElements()) {
            return (Node)foll.nextElement();
        } else {
            return null;
        }
    }

    /**
    * Get first child (DOM method)
    * @return the first child node of this node, or null if it has no children
    */

    public Node getFirstChild()  {
        AxisEnumeration children =
            getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        if (children.hasMoreElements()) {
            return (Node)children.nextElement();
        } else {
            return null;
        }
    }

    /**
    * Get last child (DOM method)
    * @return last child of this node, or null if it has no children
    */

    public Node getLastChild()  {
        AxisEnumeration children =
            getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        NodeInfo last = null;
        while (children.hasMoreElements()) {
            last = children.nextElement();
        }
        return (Node)last;
    }


    /**
     * Get the outermost element. (DOM method)
     * @return the Element for the outermost element of the document. If the document is
     * not well-formed, this returns the last element child of the root if there is one, otherwise
     * null.
     */

    public Element getDocumentElement() {
        NodeInfo root = getDocumentRoot();
        AxisEnumeration children =
            root.getEnumeration(Axis.CHILD, new NodeTypeTest(NodeInfo.ELEMENT));
        if (children.hasMoreElements()) {
            return (Element)children.nextElement();
        } else {
            return null;
        }

    }

    /**
    * Copy the string-value of this node to a given outputter.
    * Default implementation does "out.writeContent(getStringValue());" but it
    * is useful to provide an optimized implementation.
    */

    public void copyStringValue(Outputter out) throws TransformerException {
        out.writeContent(getStringValue());   // default implementation
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
    * Get the node value as defined in the DOM.
    * This is not necessarily the same as the XPath string-value.
    */

    public String getNodeValue() {
        switch (getNodeType()) {
            case NodeInfo.ROOT:
            case NodeInfo.ELEMENT:
                return null;
            case NodeInfo.ATTRIBUTE:
            case NodeInfo.TEXT:
            case NodeInfo.COMMENT:
            case NodeInfo.PI:
            case NodeInfo.NAMESPACE:
                return getStringValue();
            default:
                return null;
        }
    }

    /**
    * Set the node value. DOM method: always fails
    */

    public void setNodeValue(String nodeValue) throws DOMException {
        disallowUpdate();
    }

    /**
     * Return a <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes. DOM Method.
     */

    public NodeList getChildNodes() {
        try {
            return new NodeSetExtent(
                    getEnumeration(Axis.CHILD, AnyNodeTest.getInstance()),
                    LocalOrderComparer.getInstance());
        } catch (XPathException err) {
            return null;
            // can't happen
        }
    }

    /**
     * Return a <code>NamedNodeMap</code> containing the attributes of this node (if
     * it is an <code>Element</code> ) or <code>null</code> otherwise. (DOM method)
     */

    public NamedNodeMap getAttributes() {
        if (getNodeType()==NodeInfo.ELEMENT) {
            return new AttributeMap();
        } else {
            return null;
        }
    }

    /**
     * Return the <code>Document</code> object associated with this node. (DOM method)
     */

    public Document getOwnerDocument() {
        return (Document)getDocumentRoot();
    }

    /**
     * Insert the node <code>newChild</code> before the existing child node
     * <code>refChild</code>. DOM method: always fails.
     * @param newChild  The node to insert.
     * @param refChild  The reference node, i.e., the node before which the
     *   new node must be inserted.
     * @return  The node being inserted.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */

    public Node insertBefore(Node newChild,
                             Node refChild)
                             throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Replace the child node <code>oldChild</code> with
     * <code>newChild</code> in the list of children, and returns the
     * <code>oldChild</code> node. Always fails.
     * @param newChild  The new node to put in the child list.
     * @param oldChild  The node being replaced in the list.
     * @return  The node replaced.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */

    public Node replaceChild(Node newChild,
                             Node oldChild)
                             throws DOMException{
        disallowUpdate();
        return null;
    }

    /**
     * Remove the child node indicated by <code>oldChild</code> from the
     * list of children, and returns it. DOM method: always fails.
     * @param oldChild  The node being removed.
     * @return  The node removed.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */

    public Node removeChild(Node oldChild) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     *  Adds the node <code>newChild</code> to the end of the list of children
     * of this node. DOM method: always fails.
     * @param newChild  The node to add.
     * @return  The node added.
     * @exception DOMException
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Always raised.
     */

    public Node appendChild(Node newChild) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy
     * constructor for nodes. The duplicate node has no parent. Not
     * implemented: always returns null. (Because trees are read-only, there
     * would be no way of using the resulting node.)
     * @param deep  If <code>true</code> , recursively clone the subtree under
     *   the specified node; if <code>false</code> , clone only the node
     *   itself (and its attributes, if it is an <code>Element</code> ).
     * @return  The duplicate node.
     */

    public Node cloneNode(boolean deep) {
        // Not implemented
        return null;
    }

    /**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree
     * underneath this <code>Node</code>, including attribute nodes, into a
     * "normal" form where only structure (e.g., elements, comments,
     * processing instructions, CDATA sections, and entity references)
     * separates <code>Text</code> nodes, i.e., there are neither adjacent
     * <code>Text</code> nodes nor empty <code>Text</code> nodes.
     * @since DOM Level 2
     */

    public void normalize() {
        // null operation; nodes are always normalized
    }

    /**
     *  Tests whether the DOM implementation implements a specific feature and
     * that feature is supported by this node.
     * @param feature  The name of the feature to test. This is the same name
     *   which can be passed to the method <code>hasFeature</code> on
     *   <code>DOMImplementation</code> .
     * @param version  This is the version number of the feature to test. In
     *   Level 2, version 1, this is the string "2.0". If the version is not
     *   specified, supporting any version of the feature will cause the
     *   method to return <code>true</code> .
     * @return  Returns <code>true</code> if the specified feature is supported
     *    on this node, <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public boolean isSupported(String feature,
                               String version) {
        return feature.equalsIgnoreCase("xml");
    }

    /**
    * Alternative to isSupported(), defined in a draft DOM spec
    */

    public boolean supports(String feature,
                               String version) {
        return isSupported(feature, version);
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified. DOM method.
     * <br> This is not a computed value that is the result of a namespace
     * lookup based on an examination of the namespace declarations in scope.
     * It is merely the namespace URI given at creation time.
     * <br> For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code> .
     * Per the  Namespaces in XML Specification  an attribute does not
     * inherit its namespace from the element it is attached to. If an
     * attribute is not explicitly given a namespace, it simply has no
     * namespace.
     * @since DOM Level 2
     */

    public String getNamespaceURI() {
        String uri = getURI();
        return (uri.equals("") ? null : uri);
    }

    /**
    * Set the namespace prefix of this node. Always fails.
    */

    public void setPrefix(String prefix)
                            throws DOMException {
        disallowUpdate();
    }

    /**
    * Internal method used to indicate that update operations are not allowed
    */

    protected void disallowUpdate() throws DOMException {
        throw new UnsupportedOperationException("The Saxon DOM cannot be updated");
    }

    ////////////////////////////////////////////////////////////////////////////
    // DOM methods defined on the Document class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get the Document Type Declaration (see <code>DocumentType</code> )
     * associated with this document. For HTML documents as well as XML
     * documents without a document type declaration this returns
     * <code>null</code>. DOM method.
     * @return null: The Saxon tree model does not include the document type
     * information.
     */

    public DocumentType getDoctype() {
        return null;
    }

    /**
     * Get a <code>DOMImplementation</code> object that handles this document.
     * A DOM application may use objects from multiple implementations.
     * DOM method.
     */

    public DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }

    /**
     * Creates an element of the type specified. DOM method: always fails,
     * because the Saxon tree is not updateable.
     */

    public Element createElement(String tagName) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Creates an empty <code>DocumentFragment</code> object.
     * @return  A new <code>DocumentFragment</code> .
     * DOM method: returns null, because the Saxon tree is not updateable.
     */

    public DocumentFragment createDocumentFragment() {
        return null;
    }

    /**
     * Create a <code>Text</code> node given the specified string.
     * DOM method: returns null, because the Saxon tree is not updateable.
     * @param data  The data for the node.
     * @return  The new <code>Text</code> object.
     */

    public Text createTextNode(String data) {
        return null;
    }

    /**
     * Create a <code>Comment</code> node given the specified string.
     * DOM method: returns null, because the Saxon tree is not updateable.
     * @param data  The data for the node.
     * @return  The new <code>Comment</code> object.
     */
    public Comment createComment(String data) {
        return null;
    }

    /**
     * Create a <code>CDATASection</code> node whose value  is the specified
     * string.
     * DOM method: always fails, because the Saxon tree is not updateable.
     * @param data  The data for the <code>CDATASection</code> contents.
     * @return  The new <code>CDATASection</code> object.
     * @exception DOMException
     *    NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */

    public CDATASection createCDATASection(String data) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Create a <code>ProcessingInstruction</code> node given the specified
     * name and data strings.
     * DOM method: returns null, because the Saxon tree is not updateable.
     * @param target  The target part of the processing instruction.
     * @param data  The data for the node.
     * @return  The new <code>ProcessingInstruction</code> object.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified target contains an
     *   illegal character.
     *   <br> NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
                                                             throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Create an <code>Attr</code> of the given name.
     * DOM method: always fails, because the Saxon tree is not updateable.
     * @param name  The name of the attribute.
     * @return  A new <code>Attr</code> object with the <code>nodeName</code>
     *   attribute set to <code>name</code> , and <code>localName</code> ,
     *   <code>prefix</code> , and <code>namespaceURI</code> set to
     *   <code>null</code> .
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     */

    public Attr createAttribute(String name) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Create an <code>EntityReference</code> object.
     * DOM method: returns null, because the Saxon tree is not updateable.
     * @param name  The name of the entity to reference.
     * @return  The new <code>EntityReference</code> object.
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     *   <br> NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */

    public EntityReference createEntityReference(String name) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Return a <code>NodeList</code> of all the <code>Elements</code> with
     * a given tag name in the order in which they are encountered in a
     * preorder traversal of the <code>Document</code> tree.
     * @param tagname  The name of the tag to match on. The special value "*"
     *   matches all tags.
     * @return  A new <code>NodeList</code> object containing all the matched
     *   <code>Elements</code> .
     */

    public NodeList getElementsByTagName(String tagname) {
        // The DOM method is defined only on the Document and Element nodes,
        // but we'll support it on any node.

        AxisEnumeration allElements =
            getEnumeration(Axis.DESCENDANT, AnyNodeTest.getInstance());
        NodeSetExtent nodes = new NodeSetExtent(LocalOrderComparer.getInstance());
        while(allElements.hasMoreElements()) {
            NodeInfo next = allElements.nextElement();
            if (next.getNodeType()==ELEMENT) {
                if (tagname.equals("*") || tagname.equals(next.getDisplayName())) {
                    nodes.append(next);
                }
            }
        }
        return nodes;
    }


    /**
     * Import a node from another document to this document.
     * DOM method: always fails, because the Saxon tree is not updateable.
     * @exception DOMException
     * @since DOM Level 2
     */

    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Create an element of the given qualified name and namespace URI.
     * HTML-only DOM implementations do not need to implement this method.
     * DOM method: always fails, because the Saxon tree is not updateable.
     * @param namespaceURI  The  namespace URI of the element to create.
     * @param qualifiedName  The  qualified name of the element type to
     *   instantiate.
     * @return  A new <code>Element</code> object
     * @exception DOMException
     */

    public Element createElementNS(String namespaceURI,
                                   String qualifiedName)
                                   throws DOMException
    {
        disallowUpdate();
        return null;
    }

    /**
     * Create an attribute of the given qualified name and namespace URI.
     * HTML-only DOM implementations do not need to implement this method.
     * DOM method: returns null, because the Saxon tree is not updateable.
     * @param namespaceURI  The  namespace URI of the attribute to create.
     * @param qualifiedName  The  qualified name of the attribute to
     *   instantiate.
     * @return  A new <code>Attr</code> object.
     * @exception DOMException
     */

    public Attr createAttributeNS(String namespaceURI,
                                  String qualifiedName)
                                  throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Return a <code>NodeList</code> of all the <code>Elements</code> with
     * a given  local name and namespace URI in the order in which they are
     * encountered in a preorder traversal of the <code>Document</code> tree.
     * DOM method.
     * @param namespaceURI  The  namespace URI of the elements to match on.
     *   The special value "*" matches all namespaces.
     * @param localName  The  local name of the elements to match on. The
     *   special value "*" matches all local names.
     * @return  A new <code>NodeList</code> object containing all the matched
     *   <code>Elements</code> .
     * @since DOM Level 2
     */

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        // The DOM method is defined only on the Document and Element nodes,
        // but we'll support it on any node.

        AxisEnumeration allElements =
            getEnumeration(Axis.DESCENDANT, AnyNodeTest.getInstance());
        NodeSetExtent nodes = new NodeSetExtent(LocalOrderComparer.getInstance());
        while(allElements.hasMoreElements()) {
            NodeInfo next = allElements.nextElement();
            if (next.getNodeType()==ELEMENT) {
                if ((namespaceURI.equals("*") || namespaceURI.equals(next.getURI())) &&
                    (localName.equals("*") || localName.equals(next.getLocalName()))) {
                    nodes.append(next);
                }
            }
        }
        return nodes;
    }

    /**
     * Return the <code>Element</code> whose <code>ID</code> is given by
     * <code>elementId</code> . If no such element exists, returns
     * <code>null</code> . Behavior is not defined if more than one element
     * has this <code>ID</code> .  The DOM implementation must have
     * information that says which attributes are of type ID. Attributes with
     * the name "ID" are not of type ID unless so defined. Implementations
     * that do not know whether attributes are of type ID or not are expected
     * to return <code>null</code> .
     * @param elementId  The unique <code>id</code> value for an element.
     * @return  The matching element, or null if there is none.
     * @since DOM Level 2
     */

    public Element getElementById(String elementId) {
        // Defined on Document node; but we support it on any node.
        return (Element)getDocumentRoot().selectID(elementId);
    }

    //////////////////////////////////////////////////////////////////
    // Methods defined on the DOM Element class
    //////////////////////////////////////////////////////////////////

    /**
     *  The name of the element (DOM interface).
     */

    public String getTagName() {
        return getDisplayName();
    }

    /**
     * Retrieves an attribute value by name. Namespace declarations will not
     * be retrieved. DOM interface.
     * @param name  The QName of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     */

    public String getAttribute(String name) {
        AxisEnumeration atts = getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (att.getDisplayName().equals(name)) {
                String val = att.getStringValue();
                if (val==null) return "";
                return val;
            }
        }
        return "";
    }

    /**
     * Retrieves an attribute node by name.
     * Namespace declarations will not be retrieved.
     * <br> To retrieve an attribute node by qualified name and namespace URI,
     * use the <code>getAttributeNodeNS</code> method.
     * @param name  The name (<code>nodeName</code> ) of the attribute to
     *   retrieve.
     * @return  The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code> ) or <code>null</code> if there is no such
     *   attribute.
     */

    public Attr getAttributeNode(String name) {
        AxisEnumeration atts = getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (att.getDisplayName().equals(name)) {
                return (Attr)att;
            }
        }
        return null;
    }

    /**
     * Adds a new attribute node. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * Removes the specified attribute. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public void removeAttribute(String oldAttr) throws DOMException {
        disallowUpdate();
    }

    /**
     * Removes the specified attribute node. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        disallowUpdate();
        return null;
    }


    /**
     * Retrieves an attribute value by local name and namespace URI.
     * HTML-only DOM implementations do not need to implement this method.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> value as a string, or the empty string if
     *    that attribute does not have a specified or default value.
     * @since DOM Level 2
     */

    public String getAttributeNS(String namespaceURI, String localName) {
    	String val = getAttributeValue(namespaceURI, localName);
    	if (val==null) return "";
    	return val;
    }

    /**
     * Adds a new attribute. Always fails.
     * @param namespaceURI  The  namespace URI of the attribute to create or
     *   alter.
     * @param qualifiedName  The  qualified name of the attribute to create or
     *   alter.
     * @param value  The value to set in string form.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public void setAttributeNS(String namespaceURI,
                               String qualifiedName,
                               String value)
                               throws DOMException {
        disallowUpdate();
    }

    /**
     * Removes an attribute by local name and namespace URI. Always fails
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     */

    public void removeAttributeNS(String namespaceURI,
                                  String localName)
                                  throws DOMException{
        disallowUpdate();
    }

    /**
     * Retrieves an <code>Attr</code> node by local name and namespace URI.
     * DOM method, so namespace declarations count as attributes.
     * @param namespaceURI  The  namespace URI of the attribute to retrieve.
     * @param localName  The  local name of the attribute to retrieve.
     * @return  The <code>Attr</code> node with the specified attribute local
     *   name and namespace URI or <code>null</code> if there is no such
     *   attribute.
     * @since DOM Level 2
     */

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        int fingerprint = getDocumentRoot().getNamePool().getFingerprint(namespaceURI, localName);
        if (fingerprint==-1) return null;
        NameTest test = new NameTest(ATTRIBUTE, fingerprint);
        AxisEnumeration atts = getEnumeration(Axis.ATTRIBUTE, test);
        if (atts.hasMoreElements()) return (Attr)atts.nextElement();
        return null;
    }

    /**
     * Add a new attribute. Always fails.
     * @param newAttr  The <code>Attr</code> node to add to the attribute list.
     * @return  If the <code>newAttr</code> attribute replaces an existing
     *   attribute with the same  local name and  namespace URI , the
     *   replaced <code>Attr</code> node is returned, otherwise
     *   <code>null</code> is returned.
     * @exception DOMException
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     */

    public Attr setAttributeNodeNS(Attr newAttr)
                                   throws DOMException{
        disallowUpdate();
        return null;
    }

    /**
     * Returns <code>true</code> when an attribute with a given name is
     * specified on this element or has a default value, <code>false</code>
     * otherwise.
     * Namespace declarations will not be retrieved.
     * @param name  The name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given name is
     *   specified on this element or has a default value, <code>false</code>
     *   otherwise.
     * @since DOM Level 2
     */

    public boolean hasAttribute(String name) {
        AxisEnumeration atts = getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (att.getDisplayName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> when an attribute with a given local name
     * and namespace URI is specified on this element or has a default value,
     * <code>false</code> otherwise.
     * Namespace declarations will not be retrieved.
     * @param namespaceURI  The  namespace URI of the attribute to look for.
     * @param localName  The  local name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given local name and
     *   namespace URI is specified or has a default value on this element,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public boolean hasAttributeNS(String namespaceURI, String localName) {
		return (getAttributeValue(namespaceURI, localName) != null);
    }




    ///////////////////////////////////////////////////////////////////
    // Methods defined on the DOM Text and Comment classes
    ///////////////////////////////////////////////////////////////////


    /**
    * Get the character data of a Text or Comment node.
    * DOM method.
    */

    public String getData() {
        return getStringValue();
    }

    /**
    * Set the character data of a Text or Comment node.
    * DOM method: always fails, Saxon tree is immutable.
    */

    public void setData(String data) throws DOMException {
        disallowUpdate();
    }

    /**
    * Get the length of a Text or Comment node.
    * DOM method.
    */

    public int getLength() {
        return getStringValue().length();
    }

    /**
     * Extract a range of data from a Text or Comment node. DOM method.
     * @param offset  Start offset of substring to extract.
     * @param count  The number of 16-bit units to extract.
     * @return  The specified substring. If the sum of <code>offset</code> and
     *   <code>count</code> exceeds the <code>length</code> , then all 16-bit
     *   units to the end of the data are returned.
     * @exception DOMException
     *    INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *   negative or greater than the number of 16-bit units in
     *   <code>data</code> , or if the specified <code>count</code> is
     *   negative.
     */

    public String substringData(int offset, int count) throws DOMException {
        try {
            return getStringValue().substring(offset, offset+count);
        } catch (IndexOutOfBoundsException err2) {
            throw new DOMExceptionImpl(DOMException.INDEX_SIZE_ERR,
                             "substringData: index out of bounds");
        }
    }

    /**
     * Append the string to the end of the character data of the node.
     * DOM method: always fails.
     * @param arg  The <code>DOMString</code> to append.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public void appendData(String arg) throws DOMException {
        disallowUpdate();
    }

    /**
     * Insert a string at the specified character offset.
     * DOM method: always fails.
     * @param offset  The character offset at which to insert.
     * @param arg  The <code>DOMString</code> to insert.
     * @exception DOMException
     */

    public void insertData(int offset, String arg) throws DOMException {
        disallowUpdate();
    }

    /**
     * Remove a range of 16-bit units from the node.
     * DOM method: always fails.
     * @param offset  The offset from which to start removing.
     * @param count  The number of 16-bit units to delete.
     * @exception DOMException
     */

    public void deleteData(int offset, int count) throws DOMException {
        disallowUpdate();
    }

    /**
     * Replace the characters starting at the specified 16-bit unit offset
     * with the specified string. DOM method: always fails.
     * @param offset  The offset from which to start replacing.
     * @param count  The number of 16-bit units to replace.
     * @param arg  The <code>DOMString</code> with which the range must be
     *   replaced.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */

    public void replaceData(int offset,
                            int count,
                            String arg) throws DOMException {
        disallowUpdate();
    }


    /**
     * Break this node into two nodes at the specified offset,
     * keeping both in the tree as siblings. DOM method, always fails.
     * @param offset  The 16-bit unit offset at which to split, starting from 0.
     * @return  The new node, of the same type as this node.
     * @exception DOMException
     */

    public Text splitText(int offset)
                          throws DOMException {
        disallowUpdate();
        return null;
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods to implement the DOM Attr interface
    /////////////////////////////////////////////////////////////////////////

    /**
    * Get the name of an attribute node (the QName) (DOM method)
    */

    public String getName() {
        return getDisplayName();
    }

    /**
    * Return the character value of an attribute node (DOM method)
    * @return the attribute value
    */

    public String getValue() {
        return getStringValue();
    }

    /**
     * If this attribute was explicitly given a value in the original
     * document, this is <code>true</code> ; otherwise, it is
     * <code>false</code>. (DOM method)
     * @return Always true in this implementation.
     */

    public boolean getSpecified() {
        return true;
    }

    /**
    * Set the value of an attribute node. (DOM method).
    * Always fails (because tree is readonly)
    */

    public void setValue(String value) throws DOMException {
        disallowUpdate();
    }

    /**
     * The <code>Element</code> node this attribute is attached to or
     * <code>null</code> if this attribute is not in use.
     * @since DOM Level 2
     */

    public Element getOwnerElement() {
        if (getNodeType()!=ATTRIBUTE) {
            throw new UnsupportedOperationException(
                        "This method is defined only on attribute nodes");
        }
        return (Element)getParent();
    }

    /**
     * The type information associated with this attribute. While the type
     * information contained in this attribute is guarantee to be correct
     * after loading the document or invoking
     * <code>Document.normalizeDocument()</code>, <code>schemaTypeInfo</code>
     * may not be reliable if the node was moved.
     * <p>
     * This implementation always returns null;
     *
     * @since DOM Level 3
     */
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    /**
     * Returns whether this attribute is known to be of type ID (i.e. to
     * contain an identifier for its owner element) or not.
     * <p>
     * This implementation always returns false.
     * @since DOM Level 3
     */
    public boolean isId() {
        return false;
    }

    //////////////////////////////////////////////////////////////////////
    // Dummy implementations of DOM Level 3 methods added in Saxon 6.5.4
    //////////////////////////////////////////////////////////////////////

    /**
     * Compares the reference node, i.e. the node on which this method is
     * being called, with a node, i.e. the one passed as a parameter, with
     * regard to their position in the document and according to the
     * document order.
     *
     * @param other The node to compare against the reference node.
     * @return Returns how the node is positioned relatively to the reference
     *         node.
     * @throws org.w3c.dom.DOMException NOT_SUPPORTED_ERR: when the compared nodes are from different DOM
     *                                  implementations that do not coordinate to return consistent
     *                                  implementation-specific results.
     * @since DOM Level 3
     */
    public short compareDocumentPosition(Node other) throws DOMException {
        if (other instanceof NodeInfo) {
            int c = compareOrder((NodeInfo)other);
            if (c == 0) {
                return 0;
            } else if (c < 0) {
                NodeInfo p = ((NodeInfo)other).getParent();
                while (p != null) {
                    if (p.isSameNodeInfo(this)) {
                        return 16; //DOCUMENT_POSITION_CONTAINED_BY;
                    }
                    p = p.getParent();
                }
                return 4; //DOCUMENT_POSITION_FOLLOWING;
            } else {
               NodeInfo p = getParent();
                while (p != null) {
                    if (p.isSameNodeInfo(this)) {
                        return 8; //DOCUMENT_POSITION_CONTAINS;
                    }
                    p = p.getParent();
                }
                return 2; //DOCUMENT_POSITION_PRECEDING;
            }
        } else {
            return 32; //DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC;
        }
    }

    /**
     * This method returns a specialized object which implements the
     * specialized APIs of the specified feature and version, as specified
     * in . The specialized object may also be obtained by using
     * binding-specific casting methods but is not necessarily expected to,
     * as discussed in . This method also allow the implementation to
     * provide specialized objects which do not support the <code>Node</code>
     * interface.
     *
     * @param feature The name of the feature requested. Note that any plus
     *                sign "+" prepended to the name of the feature will be ignored since
     *                it is not significant in the context of this method.
     * @param version This is the version number of the feature to test.
     * @return Returns an object which implements the specialized APIs of
     *         the specified feature and version, if any, or <code>null</code> if
     *         there is no object which implements interfaces associated with that
     *         feature. If the <code>DOMObject</code> returned by this method
     *         implements the <code>Node</code> interface, it must delegate to the
     *         primary core <code>Node</code> and not return results inconsistent
     *         with the primary core <code>Node</code> such as attributes,
     *         childNodes, etc.
     * <p>
     * The Saxon implementation of this method always returns null
     * @since DOM Level 3
     */
    public Object getFeature(String feature, String version) {
        return null;
    }

      /**
     * This attribute returns the text content of this node and its
     * descendants. When it is defined to be <code>null</code>, setting it
     * has no effect. On setting, any possible children this node may have
     * are removed and, if it the new string is not empty or
     * <code>null</code>, replaced by a single <code>Text</code> node
     * containing the string this attribute is set to.
     * <br> On getting, no serialization is performed, the returned string
     * does not contain any markup. No whitespace normalization is performed
     * and the returned string does not contain the white spaces in element
     * content (see the attribute
     * <code>Text.isElementContentWhitespace</code>). Similarly, on setting,
     * no parsing is performed either, the input string is taken as pure
     * textual content.
     * <br>The string returned is made of the text content of this node
     * depending on its type, as defined below:
     * <table border='1' cellpadding='3'>
     * <tr>
     * <th>Node type</th>
     * <th>Content</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     * ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE,
     * DOCUMENT_FRAGMENT_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>concatenation of the <code>textContent</code>
     * attribute value of every child node, excluding COMMENT_NODE and
     * PROCESSING_INSTRUCTION_NODE nodes. This is the empty string if the
     * node has no children.</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
     * PROCESSING_INSTRUCTION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'><code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>DOCUMENT_NODE,
     * DOCUMENT_TYPE_NODE, NOTATION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'><em>null</em></td>
     * </tr>
     * </table>
       *
       * <p>The Saxon implementation returns the same result as getStringValue()
     *
     * @throws org.w3c.dom.DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *                                  fit in a <code>DOMString</code> variable on the implementation
     *                                  platform.
     * @since DOM Level 3
     */
    public String getTextContent() throws DOMException {
        return getStringValue();
    }

    /**
     * Retrieves the object associated to a key on a this node. The object
     * must first have been set to this node by calling
     * <code>setUserData</code> with the same key.
     *
     * <p>The Saxon implementation always returns null.
     *
     * @param key The key the object is associated to.
     * @return Returns the <code>DOMUserData</code> associated to the given
     *         key on this node, or <code>null</code> if there was none.
     * @since DOM Level 3
     */
    public Object getUserData(String key) {
        return null;
    }

    /**
     * This method checks if the specified <code>namespaceURI</code> is the
     * default namespace or not.
     *
     * @param namespaceURI The namespace URI to look for.
     * @return Returns <code>true</code> if the specified
     *         <code>namespaceURI</code> is the default namespace,
     *         <code>false</code> otherwise.
     * @since DOM Level 3
     */
    public boolean isDefaultNamespace(String namespaceURI) {
        NodeInfo start = this;
        while (start != null && start.getNodeType() != NodeInfo.ELEMENT) {
            start = start.getParent();
        }
        if (start == null) {
            return false;
        }
        AxisEnumeration enm = getEnumeration(Axis.NAMESPACE, AnyNodeTest.getInstance());
        while (enm.hasMoreElements()) {
            NodeInfo ns = enm.nextElement();
            if (ns.getStringValue().equals(namespaceURI)) {
                return ns.getLocalName().equals("");
            }
        }
        return false;
    }

    /**
     * Tests whether two nodes are equal.
     * <br>This method tests for equality of nodes, not sameness (i.e.,
     * whether the two nodes are references to the same object) which can be
     * tested with <code>Node.isSameNode()</code>. All nodes that are the
     * same will also be equal, though the reverse may not be true.
     * <br>Two nodes are equal if and only if the following conditions are
     * satisfied:
     * <ul>
     * <li>The two nodes are of the same type.
     * </li>
     * <li>The following string
     * attributes are equal: <code>nodeName</code>, <code>localName</code>,
     * <code>namespaceURI</code>, <code>prefix</code>, <code>nodeValue</code>
     * . This is: they are both <code>null</code>, or they have the same
     * length and are character for character identical.
     * </li>
     * <li>The
     * <code>attributes</code> <code>NamedNodeMaps</code> are equal. This
     * is: they are both <code>null</code>, or they have the same length and
     * for each node that exists in one map there is a node that exists in
     * the other map and is equal, although not necessarily at the same
     * index.
     * </li>
     * <li>The <code>childNodes</code> <code>NodeLists</code> are equal.
     * This is: they are both <code>null</code>, or they have the same
     * length and contain equal nodes at the same index. Note that
     * normalization can affect equality; to avoid this, nodes should be
     * normalized before being compared.
     * </li>
     * </ul>
     * <br>For two <code>DocumentType</code> nodes to be equal, the following
     * conditions must also be satisfied:
     * <ul>
     * <li>The following string attributes
     * are equal: <code>publicId</code>, <code>systemId</code>,
     * <code>internalSubset</code>.
     * </li>
     * <li>The <code>entities</code>
     * <code>NamedNodeMaps</code> are equal.
     * </li>
     * <li>The <code>notations</code>
     * <code>NamedNodeMaps</code> are equal.
     * </li>
     * </ul>
     * <br>On the other hand, the following do not affect equality: the
     * <code>ownerDocument</code>, <code>baseURI</code>, and
     * <code>parentNode</code> attributes, the <code>specified</code>
     * attribute for <code>Attr</code> nodes, the <code>schemaTypeInfo</code>
     * attribute for <code>Attr</code> and <code>Element</code> nodes, the
     * <code>Text.isElementContentWhitespace</code> attribute for
     * <code>Text</code> nodes, as well as any user data or event listeners
     * registered on the nodes.
     * <p ><b>Note:</b>  As a general rule, anything not mentioned in the
     * description above is not significant in consideration of equality
     * checking. Note that future versions of this specification may take
     * into account more attributes and implementations conform to this
     * specification are expected to be updated accordingly.
     *
     * @param arg The node to compare equality with.
     * @return Returns <code>true</code> if the nodes are equal,
     *         <code>false</code> otherwise.
     * @since DOM Level 3
     */
    public boolean isEqualNode(Node arg) {
        throw new UnsupportedOperationException("isEqualNode() is not supported");
    }

    /**
     * Returns whether this node is the same node as the given one.
     * <br>This method provides a way to determine whether two
     * <code>Node</code> references returned by the implementation reference
     * the same object. When two <code>Node</code> references are references
     * to the same object, even if through a proxy, the references may be
     * used completely interchangeably, such that all attributes have the
     * same values and calling the same DOM method on either reference
     * always has exactly the same effect.
     *
     * @param other The node to test against.
     * @return Returns <code>true</code> if the nodes are the same,
     *         <code>false</code> otherwise.
     * @since DOM Level 3
     */
    public boolean isSameNode(Node other) {
        if (other instanceof NodeInfo) {
            return isSameNodeInfo((NodeInfo)other);
        } else {
            return false;
        }
    }

    /**
     * Look up the namespace URI associated to the given prefix, starting from
     * this node.
     * <br>See  for details on the algorithm used by this method.
     *
     * @param prefix The prefix to look for. If this parameter is
     *               <code>null</code>, the method will return the default namespace URI
     *               if any.
     * @return Returns the associated namespace URI or <code>null</code> if
     *         none is found.
     * @since DOM Level 3
     */
    public String lookupNamespaceURI(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        NodeInfo start = this;
        while (start != null && start.getNodeType() != NodeInfo.ELEMENT) {
            start = start.getParent();
        }
        if (start == null) {
            return null;
        }
        AxisEnumeration enm = getEnumeration(Axis.NAMESPACE, AnyNodeTest.getInstance());
        while (enm.hasMoreElements()) {
            NodeInfo ns = enm.nextElement();
            if (ns.getLocalName().equals(prefix)) {
                return ns.getStringValue();
            }
        }
        return null;
    }

    /**
     * Look up the prefix associated to the given namespace URI, starting from
     * this node. The default namespace declarations are ignored by this
     * method.
     * <br>See  for details on the algorithm used by this method.
     *
     * @param namespaceURI The namespace URI to look for.
     * @return Returns an associated namespace prefix if found or
     *         <code>null</code> if none is found. If more than one prefix are
     *         associated to the namespace prefix, the returned namespace prefix
     *         is implementation dependent.
     * @since DOM Level 3
     */
    public String lookupPrefix(String namespaceURI) {
        NodeInfo start = this;
        while (start != null && start.getNodeType() != NodeInfo.ELEMENT) {
            start = start.getParent();
        }
        if (start == null) {
            return null;
        }
        AxisEnumeration enm = getEnumeration(Axis.NAMESPACE, AnyNodeTest.getInstance());
        while (enm.hasMoreElements()) {
            NodeInfo ns = enm.nextElement();
            if (ns.getStringValue().equals(namespaceURI)) {
                return ns.getLocalName();
            }
        }
        return null;
    }

    /**
     * This attribute returns the text content of this node and its
     * descendants. When it is defined to be <code>null</code>, setting it
     * has no effect. On setting, any possible children this node may have
     * are removed and, if it the new string is not empty or
     * <code>null</code>, replaced by a single <code>Text</code> node
     * containing the string this attribute is set to.
     * <br> On getting, no serialization is performed, the returned string
     * does not contain any markup. No whitespace normalization is performed
     * and the returned string does not contain the white spaces in element
     * content (see the attribute
     * <code>Text.isElementContentWhitespace</code>). Similarly, on setting,
     * no parsing is performed either, the input string is taken as pure
     * textual content.
     * <br>The string returned is made of the text content of this node
     * depending on its type, as defined below:
     * <table border='1' cellpadding='3'>
     * <tr>
     * <th>Node type</th>
     * <th>Content</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     * ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE,
     * DOCUMENT_FRAGMENT_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>concatenation of the <code>textContent</code>
     * attribute value of every child node, excluding COMMENT_NODE and
     * PROCESSING_INSTRUCTION_NODE nodes. This is the empty string if the
     * node has no children.</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
     * PROCESSING_INSTRUCTION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'><code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>DOCUMENT_NODE,
     * DOCUMENT_TYPE_NODE, NOTATION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'><em>null</em></td>
     * </tr>
     * </table>
     *
     * @throws org.w3c.dom.DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @since DOM Level 3
     */
    public void setTextContent(String textContent) throws DOMException {
        disallowUpdate();
    }

    /**
     * Associate an object to a key on this node. The object can later be
     * retrieved from this node by calling <code>getUserData</code> with the
     * same key.
     *
     * @param key     The key to associate the object to.
     * @param data    The object to associate to the given key, or
     *                <code>null</code> to remove any existing association to that key.
     * @param handler The handler to associate to that key, or
     *                <code>null</code>.
     * @return Returns the <code>DOMUserData</code> previously associated to
     *         the given key on this node, or <code>null</code> if there was none.
     * @since DOM Level 3
     */
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        disallowUpdate();
        return null;
    }

    /////////////////////////////////////////////////////////////////////////
    // Additional DOM Level 3 methods for text nodes
    /////////////////////////////////////////////////////////////////////////

    /**
     * Returns all text of <code>Text</code> nodes logically-adjacent text
     * nodes to this node, concatenated in document order.
     * <br>For instance, in the example below <code>wholeText</code> on the
     * <code>Text</code> node that contains "bar" returns "barfoo", while on
     * the <code>Text</code> node that contains "foo" it returns "barfoo".
     *
     * @since DOM Level 3
     */
    public String getWholeText() {
        return getStringValue();
    }

    /**
     * Returns whether this text node contains <a href='http://www.w3.org/TR/2004/REC-xml-infoset-20040204#infoitem.character'>
     * element content whitespace</a>, often abusively called "ignorable whitespace". The text node is
     * determined to contain whitespace in element content during the load
     * of the document or if validation occurs while using
     * <code>Document.normalizeDocument()</code>.
     * <p>
     * The Saxon implementation always returns false.
     *
     * @since DOM Level 3
     */
    public boolean isElementContentWhitespace() {
        return false;
    }

    /**
     * Replaces the text of the current node and all logically-adjacent text
     * nodes with the specified text. All logically-adjacent text nodes are
     * removed including the current node unless it was the recipient of the
     * replacement text.
     * <br>This method returns the node which received the replacement text.
     * The returned node is:
     * <ul>
     * <li><code>null</code>, when the replacement text is
     * the empty string;
     * </li>
     * <li>the current node, except when the current node is
     * read-only;
     * </li>
     * <li> a new <code>Text</code> node of the same type (
     * <code>Text</code> or <code>CDATASection</code>) as the current node
     * inserted at the location of the replacement.
     * </li>
     * </ul>
     * <br>For instance, in the above example calling
     * <code>replaceWholeText</code> on the <code>Text</code> node that
     * contains "bar" with "yo" in argument results in the following:
     * <br>Where the nodes to be removed are read-only descendants of an
     * <code>EntityReference</code>, the <code>EntityReference</code> must
     * be removed instead of the read-only nodes. If any
     * <code>EntityReference</code> to be removed has descendants that are
     * not <code>EntityReference</code>, <code>Text</code>, or
     * <code>CDATASection</code> nodes, the <code>replaceWholeText</code>
     * method must fail before performing any modification of the document,
     * raising a <code>DOMException</code> with the code
     * <code>NO_MODIFICATION_ALLOWED_ERR</code>.
     * <br>For instance, in the example below calling
     * <code>replaceWholeText</code> on the <code>Text</code> node that
     * contains "bar" fails, because the <code>EntityReference</code> node
     * "ent" contains an <code>Element</code> node which cannot be removed.
     *
     * @param content The content of the replacing <code>Text</code> node.
     * @return The <code>Text</code> node created with the specified content.
     * @throws org.w3c.dom.DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if one of the <code>Text</code>
     *                                  nodes being replaced is readonly.
     * @since DOM Level 3
     */
    public Text replaceWholeText(String content) throws DOMException {
        disallowUpdate();
        return null;
    }


    /**
     * If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     * <br> To specify an attribute by local name and namespace URI, use the
     * <code>setIdAttributeNS</code> method.
     *
     * @param name The name of the attribute.
     * @param isId Whether the attribute is a of type ID.
     * @throws org.w3c.dom.DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                                  <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *                                  of this element.
     * @since DOM Level 3
     */
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        disallowUpdate();
    }

    /**
     * If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     *
     * @param idAttr The attribute node.
     * @param isId   Whether the attribute is a of type ID.
     * @throws org.w3c.dom.DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                                  <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *                                  of this element.
     * @since DOM Level 3
     */
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        disallowUpdate();
    }

    /**
     * If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     *
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName    The local name of the attribute.
     * @param isId         Whether the attribute is a of type ID.
     * @throws org.w3c.dom.DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *                                  <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *                                  of this element.
     * @since DOM Level 3
     */
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        disallowUpdate();
    }


    /////////////////////////////////////////////////////////////////////////
    // Methods to implement the DOMImplementation interface
    /////////////////////////////////////////////////////////////////////////

    /**
    * Inner class implementing the DOMImplementation interface
    */

    private class DOMImplementationImpl implements DOMImplementation {

     /**
     *  Test if the DOM implementation implements a specific feature.
     * @param feature  The name of the feature to test (case-insensitive).
     * @param version  This is the version number of the feature to test.
     * @return <code>true</code> if the feature is implemented in the
     *   specified version, <code>false</code> otherwise.
     */

    public boolean hasFeature(String feature, String version) {
        return false;
    }


    /**
     *  Creates an empty <code>DocumentType</code> node.
     * @param qualifiedName  The  qualified name of the document type to be
     *   created.
     * @param publicId  The external subset public identifier.
     * @param systemId  The external subset system identifier.
     * @return  A new <code>DocumentType</code> node with
     *   <code>Node.ownerDocument</code> set to <code>null</code> .
     * @exception DOMException
     *    INVALID_CHARACTER_ERR: Raised if the specified qualified name
     *   contains an illegal character.
     *   <br> NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is
     *   malformed.
     * @since DOM Level 2
     */

    public DocumentType createDocumentType(String qualifiedName,
                                           String publicId,
                                           String systemId)
                                           throws DOMException
    {
        disallowUpdate();
        return null;
    }

    /**
     *  Creates an XML <code>Document</code> object of the specified type with
     * its document element.
     * @param namespaceURI  The  namespace URI of the document element to
     *   create.
     * @param qualifiedName  The  qualified name of the document element to be
     *   created.
     * @param doctype  The type of document to be created or <code>null</code>.
     * @return  A new <code>Document</code> object.
     * @exception DOMException
     * @since DOM Level 2
     */
    public Document createDocument(String namespaceURI,
                                   String qualifiedName,
                                   DocumentType doctype)
                                   throws DOMException
    {
        disallowUpdate();
        return null;
    }

        /**
         * This method returns a specialized object which implements the
         * specialized APIs of the specified feature and version, as specified
         * in . The specialized object may also be obtained by using
         * binding-specific casting methods but is not necessarily expected to,
         * as discussed in . This method also allow the implementation to
         * provide specialized objects which do not support the
         * <code>DOMImplementation</code> interface.
         *
         * @param feature The name of the feature requested. Note that any plus
         *                sign "+" prepended to the name of the feature will be ignored since
         *                it is not significant in the context of this method.
         * @param version This is the version number of the feature to test.
         * @return Returns an object which implements the specialized APIs of
         *         the specified feature and version, if any, or <code>null</code> if
         *         there is no object which implements interfaces associated with that
         *         feature. If the <code>DOMObject</code> returned by this method
         *         implements the <code>DOMImplementation</code> interface, it must
         *         delegate to the primary core <code>DOMImplementation</code> and not
         *         return results inconsistent with the primary core
         *         <code>DOMImplementation</code> such as <code>hasFeature</code>,
         *         <code>getFeature</code>, etc.
         * <p>
         * The Saxon implementation of this method always returns null.
         * @since DOM Level 3
         */
        public Object getFeature(String feature, String version) {
            return null;
        }

    } // end of inner class DOMImplementationImpl

    //////////////////////////////////////////////////////////////////////
    // Inner class to implement DOM NamedNodeMap (the set of attributes)
    //////////////////////////////////////////////////////////////////////

    private class AttributeMap implements NamedNodeMap {

    /**
    * Get named attribute (DOM NamedNodeMap method)
    */

    public Node getNamedItem(String name) {
        AxisEnumeration atts =
            getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (name.equals(att.getDisplayName())) {
                return (Node)att;
            }
        }
        return null;
    }

    /**
    * Get n'th attribute (DOM NamedNodeMap method).
    * Namespace declarations are not retrieved.
    */

    public Node item(int index) {
        if (index<0) {
            return null;
        }
        int length = 0;
        AxisEnumeration atts =
            getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (length==index) {
                return (Node)att;
            }
            length++;
        }
        return null;
    }

    /**
    * Get number of attributes (DOM NamedNodeMap method).
    */

    public int getLength() {
        int length = 0;
        AxisEnumeration atts =
            getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            atts.nextElement();
            length++;
        }
        return length;
    }

    /**
    * Get named attribute (DOM NamedNodeMap method)
    */

    public Node getNamedItemNS(String uri, String localName) {
        if (uri==null) uri="";
        AxisEnumeration atts =
            getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (atts.hasMoreElements()) {
            NodeInfo att = atts.nextElement();
            if (uri.equals(att.getURI()) && localName.equals(att.getLocalName())) {
                return (Node)att;
            }
        }
        return null;
    }

    /**
    * Set named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node setNamedItem(Node arg) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
    * Remove named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node removeNamedItem(String name) throws DOMException {
        disallowUpdate();
        return null;
    }
    /**
    * Set named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node setNamedItemNS(Node arg) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
    * Remove named attribute (DOM NamedNodeMap method: always fails)
    */

    public Node removeNamedItemNS(String uri, String localName) throws DOMException {
        disallowUpdate();
        return null;
    }

    } // end of inner class NamedAttributeMap

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

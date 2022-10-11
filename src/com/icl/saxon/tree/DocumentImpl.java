package com.icl.saxon.tree;
import com.icl.saxon.KeyManager;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.sort.LocalOrderComparer;
import org.w3c.dom.*;
import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;
import java.util.Hashtable;

/**
  * A node in the XML parse tree representing the Document itself (or equivalently, the root
  * node of the Document).<P>
  * @author Michael H. Kay
  */

public final class DocumentImpl extends ParentNodeImpl
    implements DocumentInfo, Document {

    //private static int nextDocumentNumber = 0;

    private ElementImpl documentElement;

    private Hashtable idTable = null;
    //private int documentNumber;
    private Hashtable entityTable = null;
    private Hashtable elementList = null;
    private StringBuffer characterBuffer;
    private NamePool namePool;
    private NodeFactory nodeFactory;
    private LineNumberMap lineNumberMap;
    private SystemIdMap systemIdMap = new SystemIdMap();

    // list of indexes for keys. Each entry is a triple: KeyManager, fingerprint of Key name, Hashtable.
    // This reflects the fact that the same document may contain indexes for more than one stylesheet.

    private Object[] index = new Object[30];
    private int indexEntriesUsed = 0;

    public DocumentImpl() {
        parent = null;
    }

    /**
    * Set the character buffer
    */

    protected void setCharacterBuffer(StringBuffer buffer) {
        characterBuffer = buffer;
    }

    /**
    * Get the character buffer
    */

    public final StringBuffer getCharacterBuffer() {
        return characterBuffer;
    }

	/**
	* Set the name pool used for all names in this document
	*/

	public void setNamePool(NamePool pool) {
		namePool = pool;
	}

	/**
	* Get the name pool used for the names in this document
	*/

	public NamePool getNamePool() {
		return namePool;
	}

	/**
	* Set the node factory that was used to build this document
	*/

	public void setNodeFactory(NodeFactory factory) {
		nodeFactory = factory;
	}

	/**
	* Get the node factory that was used to build this document
	*/

	public NodeFactory getNodeFactory() {
		return nodeFactory;
	}

    /**
    * Set the top-level element of the document (variously called the root element or the
    * document element). Note that a DocumentImpl may represent the root of a result tree
    * fragment, in which case there is no document element.
    * @param e the top-level element
    */

    protected void setDocumentElement(ElementImpl e) {
        documentElement = e;
    }

    /**
    * Set the system id of this node
    */

    public void setSystemId(String uri) {
        //if (uri==null) {
        //    throw new IllegalArgumentException("System ID must not be null");
        //}
        if (uri==null) {
            uri = "";
        }
        systemIdMap.setSystemId(sequence, uri);
    }

    /**
    * Get the system id of this root node
    */

    public String getSystemId() {
        return systemIdMap.getSystemId(sequence);
    }

    /**
    * Get the base URI of this root node. For a root node the base URI is the same as the
    * System ID.
    */

    public String getBaseURI() {
        return getSystemId();
    }

    /**
    * Set the system id of an element in the document
    */

    protected void setSystemId(int seq, String uri) {
        if (uri==null) {
            uri = "";
        //    uri = "*unknown.uri*";
        //    throw new NullPointerException("URI may not be null");
        }
        systemIdMap.setSystemId(seq, uri);
    }


    /**
    * Get the system id of an element in the document
    */

    protected String getSystemId(int seq) {
        return systemIdMap.getSystemId(seq);
    }


    /**
    * Set line numbering on
    */

    public void setLineNumbering() {
        lineNumberMap = new LineNumberMap();
        lineNumberMap.setLineNumber(sequence, 0);
    }

    /**
    * Set the line number for an element. Ignored if line numbering is off.
    */

    protected void setLineNumber(int sequence, int line) {
        if (lineNumberMap != null) {
            lineNumberMap.setLineNumber(sequence, line);
        }
    }

    /**
    * Get the line number for an element. Return -1 if line numbering is off.
    */

    protected int getLineNumber(int sequence) {
        if (lineNumberMap != null) {
            return lineNumberMap.getLineNumber(sequence);
        }
        return -1;
    }

    /**
    * Get the line number of this root node.
    * @return 0 always
    */

    public int getLineNumber() {
        return 0;
    }

    /**
    * Return the type of node.
    * @return NodeInfo.ROOT (always)
    */

    public final short getNodeType() {
        return ROOT;
    }

    /**
    * Get next sibling - always null
    * @return null
    */

    public final Node getNextSibling() {
        return null;
    }

    /**
    * Get previous sibling - always null
    * @return null
    */

    public final Node getPreviousSibling()  {
        return null;
    }

    /**
     * Get the root (outermost) element.
     * @return the Element node for the outermost element of the document.
     */

    public Element getDocumentElement() {
        return (ElementImpl)documentElement;
    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing this document
    */

    public DocumentInfo getDocumentRoot() {
        return this;
    }

    /**
    * Get a character string that uniquely identifies this node within the document
    * @return the empty string
    */

    public String generateId() {
        return "";
    }

    /**
    * Get a list of all elements with a given name fingerprint
    */

    protected AxisEnumeration getAllElements(int fingerprint) {
        Integer elkey = new Integer(fingerprint);
        if (elementList==null) {
            elementList = new Hashtable();
        }
        NodeSetExtent list = (NodeSetExtent)elementList.get(elkey);
        if (list==null) {
            list = new NodeSetExtent(LocalOrderComparer.getInstance());
            list.setSorted(true);
            NodeImpl next = getNextInDocument(this);
            while (next!=null) {
                if (next.getNodeType()==ELEMENT &&
                        next.getFingerprint() == fingerprint) {
                    list.append(next);
                }
                next = next.getNextInDocument(this);
            }
            elementList.put(elkey, list);
        }
        return (AxisEnumeration)list.enumerate();
    }

    /**
    * Index all the ID attributes. This is done the first time the id() function
    * is used on this document
    */

    private void indexIDs() {
        if (idTable!=null) return;      // ID's are already indexed
        idTable = new Hashtable();

        NodeImpl curr = this;
        NodeImpl root = curr;
        while(curr!=null) {
            if (curr.getNodeType()==ELEMENT) {
                ElementImpl e = (ElementImpl)curr;
                Attributes atts = e.getAttributeList();
                for (int i=0; i<atts.getLength(); i++) {
                    if ("ID".equals(atts.getType(i))) {
                        registerID(e, atts.getValue(i));
                    }
                }
            }
            curr = curr.getNextInDocument(root);
        }
    }

    /**
    * Register a unique element ID. Fails if there is already an element with that ID.
    * @param e The Element having a particular unique ID value
    * @param id The unique ID value
    */

    private void registerID(NodeInfo e, String id) {
        // the XPath spec (5.2.1) says ignore the second ID if it's not unique
        Object old = idTable.get(id);
        if (old==null) {
            idTable.put(id, e);
        }

    }

    /**
    * Get the element with a given ID.
    * @param id The unique ID of the required element, previously registered using registerID()
    * @return The NodeInfo for the given ID if one has been registered, otherwise null.
    */

    public NodeInfo selectID(String id) {
        if (idTable==null) indexIDs();
        return (NodeInfo)idTable.get(id);
    }

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value. If no index
    * has been built, returns null.
    */

    public synchronized Hashtable getKeyIndex(KeyManager keymanager, int fingerprint) {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager &&
            		 ((Integer)index[k+1]).intValue()==fingerprint) {
                Object ix = index[k+2];
                return (Hashtable)index[k+2];

                            // circular references are now a compile-time error
            }
        }
        return null;
    }

    /**
    * Set the index for a given key. The method is synchronized because the same document
    * can be used by several stylesheets at the same time.
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @param keyindex the index, in the form of a Hashtable that
    * maps the key value to a Vector of nodes having that key value. Or the String
    * "under construction", indicating that the index is being built.
    */

    public synchronized void setKeyIndex(KeyManager keymanager, int fingerprint, Hashtable keyindex) /*throws SAXException*/ {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager &&
            		 ((Integer)index[k+1]).intValue() == fingerprint) {
                index[k+2] = keyindex;
                return;
            }
        }

        if (indexEntriesUsed+3 >= index.length) {
            Object[] index2 = new Object[indexEntriesUsed*2];
            System.arraycopy(index, 0, index2, 0, indexEntriesUsed);
            index = index2;
        }
        index[indexEntriesUsed++] = keymanager;
        index[indexEntriesUsed++] = new Integer(fingerprint);
        index[indexEntriesUsed++] = keyindex;
    }

    /**
    * Set an unparsed entity URI associated with this document. For system use only, while
    * building the document.
    */

    protected void setUnparsedEntity(String name, String uri) {
        if (entityTable==null) {
            entityTable = new Hashtable();
        }
        entityTable.put(name, uri);
    }

    /**
    * Get the unparsed entity with a given name
    * @param name the name of the entity
    * @return the URI of the entity if there is one, or empty string if not
    */

    public String getUnparsedEntity(String name) {
        if (entityTable==null) {
            return "";
        }
        String uri = (String)entityTable.get(name);
        return (uri==null ? "" : uri);
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException {
        NodeImpl next = (NodeImpl)getFirstChild();
        while (next!=null) {
            next.copy(out);
            next = (NodeImpl)next.getNextSibling();
        }
    }

    /**
     * Attempts to adopt a node from another document to this document.
     * @param source The node to move into this document.
     * @return The adopted node, or <code>null</code> if this operation
     *         fails, such as when the source node comes from a different
     *         implementation.
     * @throws org.w3c.dom.DOMException NOT_SUPPORTED_ERR: Raised if the source node is of type
     *                                  <code>DOCUMENT</code>, <code>DOCUMENT_TYPE</code>.
     *                                  <br>NO_MODIFICATION_ALLOWED_ERR: Raised when the source node is
     *                                  readonly.
     * @since DOM Level 3
     */
    public Node adoptNode(Node source) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * The location of the document or <code>null</code> if undefined or if
     * the <code>Document</code> was created using
     * <code>DOMImplementation.createDocument</code>. No lexical checking is
     * performed when setting this attribute; this could result in a
     * <code>null</code> value returned when using <code>Node.baseURI</code>
     * .
     * <br> Beware that when the <code>Document</code> supports the feature
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over
     * this attribute when computing <code>Node.baseURI</code>.
     *
     * @since DOM Level 3
     */
    public String getDocumentURI() {
        return getSystemId();
    }

    /**
     * The configuration used when <code>Document.normalizeDocument()</code>
     * is invoked.
     *
     * @since DOM Level 3
     */
    public DOMConfiguration getDomConfig() {
        return null;
    }

    /**
     * An attribute specifying the encoding used for this document at the time
     * of the parsing. This is <code>null</code> when it is not known, such
     * as when the <code>Document</code> was created in memory.
     *
     * @since DOM Level 3
     */
    public String getInputEncoding() {
        return null;
    }

    /**
     * An attribute specifying whether error checking is enforced or not. When
     * set to <code>false</code>, the implementation is free to not test
     * every possible error case normally defined on DOM operations, and not
     * raise any <code>DOMException</code> on DOM operations or report
     * errors while using <code>Document.normalizeDocument()</code>. In case
     * of error, the behavior is undefined. This attribute is
     * <code>true</code> by default.
     *
     * @since DOM Level 3
     */
    public boolean getStrictErrorChecking() {
        return true;
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the encoding of this document. This is <code>null</code> when
     * unspecified or when it is not known, such as when the
     * <code>Document</code> was created in memory.
     *
     * @since DOM Level 3
     */
    public String getXmlEncoding() {
        return null;
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, whether this document is standalone. This is <code>false</code> when
     * unspecified.
     * <p ><b>Note:</b>  No verification is done on the value when setting
     * this attribute. Applications should use
     * <code>Document.normalizeDocument()</code> with the "validate"
     * parameter to verify if the value matches the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#sec-rmd'>validity
     * constraint for standalone document declaration</a> as defined in [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>].
     *
     * @since DOM Level 3
     */
    public boolean getXmlStandalone() {
        return false;
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the version number of this document. If there is no declaration and if
     * this document supports the "XML" feature, the value is
     * <code>"1.0"</code>.
     *
     * @since DOM Level 3
     */
    public String getXmlVersion() {
        return "1.0";
    }

    /**
     * This method acts as if the document was going through a save and load
     * cycle, putting the document in a "normal" form.
     * @since DOM Level 3
     */
    public void normalizeDocument() {
        disallowUpdate();
    }

    /**
     * Rename an existing node of type <code>ELEMENT_NODE</code> or
     * <code>ATTRIBUTE_NODE</code>.

     *
     * @param n             The node to rename.
     * @param namespaceURI  The new namespace URI.
     * @param qualifiedName The new qualified name.
     * @return The renamed node. This is either the specified node or the new
     *         node that was created to replace the specified node.
     * @throws org.w3c.dom.DOMException NOT_SUPPORTED_ERR: Raised when the type of the specified node is
     *                                  neither <code>ELEMENT_NODE</code> nor <code>ATTRIBUTE_NODE</code>,
     *                                  or if the implementation does not support the renaming of the
     *                                  document element.
     *                                  <br>INVALID_CHARACTER_ERR: Raised if the new qualified name is not an
     *                                  XML name according to the XML version in use specified in the
     *                                  <code>Document.xmlVersion</code> attribute.
     *                                  <br>WRONG_DOCUMENT_ERR: Raised when the specified node was created
     *                                  from a different document than this document.
     *                                  <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a
     *                                  malformed qualified name, if the <code>qualifiedName</code> has a
     *                                  prefix and the <code>namespaceURI</code> is <code>null</code>, or
     *                                  if the <code>qualifiedName</code> has a prefix that is "xml" and
     *                                  the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *                                  http://www.w3.org/XML/1998/namespace</a>" [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     *                                  . Also raised, when the node being renamed is an attribute, if the
     *                                  <code>qualifiedName</code>, or its prefix, is "xmlns" and the
     *                                  <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>".
     * @since DOM Level 3
     */
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        disallowUpdate();
        return null;
    }

    /**
     * The location of the document or <code>null</code> if undefined or if
     * the <code>Document</code> was created using
     * <code>DOMImplementation.createDocument</code>. No lexical checking is
     * performed when setting this attribute; this could result in a
     * <code>null</code> value returned when using <code>Node.baseURI</code>
     * .
     * <br> Beware that when the <code>Document</code> supports the feature
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over
     * this attribute when computing <code>Node.baseURI</code>.
     *
     * @since DOM Level 3
     */
    public void setDocumentURI(String documentURI) {
        setSystemId(documentURI);
    }

    /**
     * An attribute specifying whether error checking is enforced or not. When
     * set to <code>false</code>, the implementation is free to not test
     * every possible error case normally defined on DOM operations, and not
     * raise any <code>DOMException</code> on DOM operations or report
     * errors while using <code>Document.normalizeDocument()</code>. In case
     * of error, the behavior is undefined. This attribute is
     * <code>true</code> by default.
     *
     * @since DOM Level 3
     */
    public void setStrictErrorChecking(boolean strictErrorChecking) {
        throw new UnsupportedOperationException("setStrictErrorChecking() is not supported");
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, whether this document is standalone. This is <code>false</code> when
     * unspecified.
     * <p ><b>Note:</b>  No verification is done on the value when setting
     * this attribute. Applications should use
     * <code>Document.normalizeDocument()</code> with the "validate"
     * parameter to verify if the value matches the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#sec-rmd'>validity
     * constraint for standalone document declaration</a> as defined in [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>].
     *
     * @throws org.w3c.dom.DOMException NOT_SUPPORTED_ERR: Raised if this document does not support the
     *                                  "XML" feature.
     * @since DOM Level 3
     */
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        disallowUpdate();
    }

    /**
     * An attribute specifying, as part of the <a href='http://www.w3.org/TR/2004/REC-xml-20040204#NT-XMLDecl'>XML declaration</a>, the version number of this document. If there is no declaration and if
     * this document supports the "XML" feature, the value is
     * <code>"1.0"</code>. If this document does not support the "XML"
     * feature, the value is always <code>null</code>. Changing this
     * attribute will affect methods that check for invalid characters in
     * XML names. Application should invoke
     * <code>Document.normalizeDocument()</code> in order to check for
     * invalid characters in the <code>Node</code>s that are already part of
     * this <code>Document</code>.
     * <br> DOM applications may use the
     * <code>DOMImplementation.hasFeature(feature, version)</code> method
     * with parameter values "XMLVersion" and "1.0" (respectively) to
     * determine if an implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml-20040204'>XML 1.0</a>]. DOM
     * applications may use the same method with parameter values
     * "XMLVersion" and "1.1" (respectively) to determine if an
     * implementation supports [<a href='http://www.w3.org/TR/2004/REC-xml11-20040204/'>XML 1.1</a>]. In both
     * cases, in order to support XML, an implementation must also support
     * the "XML" feature defined in this specification. <code>Document</code>
     * objects supporting a version of the "XMLVersion" feature must not
     * raise a <code>NOT_SUPPORTED_ERR</code> exception for the same version
     * number when using <code>Document.xmlVersion</code>.
     *
     * @throws org.w3c.dom.DOMException NOT_SUPPORTED_ERR: Raised if the version is set to a value that is
     *                                  not supported by this <code>Document</code> or if this document
     *                                  does not support the "XML" feature.
     * @since DOM Level 3
     */
    public void setXmlVersion(String xmlVersion) throws DOMException {
        disallowUpdate();
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
// The Original Code is: all this file except PB-SYNC section.
//
// The Initial Developer of the Original Code is
// Michael Kay
//
// Portions marked PB-SYNC are Copyright (C) Peter Bryant (pbryant@bigfoot.com). All Rights Reserved.
//
// Contributor(s): Michael Kay, Peter Bryant.
//

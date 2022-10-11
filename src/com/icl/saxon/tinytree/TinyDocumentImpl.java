package com.icl.saxon.tinytree;
import com.icl.saxon.KeyManager;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.om.*;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.sort.LocalOrderComparer;
import com.icl.saxon.tree.LineNumberMap;
import com.icl.saxon.tree.SystemIdMap;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import java.util.Hashtable;


/**
  * A node in the XML parse tree representing the Document itself (or equivalently, the root
  * node of the Document).<P>
  * @author Michael H. Kay
  * @version 26 April 1999
  */

public final class TinyDocumentImpl extends TinyParentNodeImpl
    implements DocumentInfo, Document {

    private Hashtable idTable = null;
    private NamePool namePool;
    private Hashtable elementList = null;
    private boolean usesNamespaces = false;
    private Hashtable entityTable = null;

    // the contents of the document

    protected char[] charBuffer = new char[4000];
    protected int charBufferLength = 0;
    protected StringBuffer commentBuffer = new StringBuffer(500);

    protected int numberOfNodes = 0;    // excluding attributes and namespaces
    protected int lastLevelOneNode = -1;

    protected byte[] nodeType = new byte[4000];
    protected short[] depth = new short[4000];
    /*NEXT*/ protected int[] next = new int[4000];
    protected int[] offset = new int[4000];
    protected int[] length = new int[4000];
    protected int[] nameCode = new int[4000];
    // the prior array indexes preceding-siblings; it is constructed only when required
    protected int[] prior = null;

    protected int numberOfAttributes = 0;
    protected int[] attParent = new int[100];
    protected int[] attCode = new int[100];
    protected String[] attValue = new String[100];

    protected int numberOfNamespaces = 0;
    protected int[] namespaceParent = new int[20];
    protected int[] namespaceCode = new int[20];

    private LineNumberMap lineNumberMap;
    private SystemIdMap systemIdMap = new SystemIdMap();

    // list of indexes for keys. Each entry is a triple: KeyManager, Fingerprint of Name of Key, Hashtable.
    // This reflects the fact that the same document may contain indexes for more than one stylesheet.

    private Object[] index = new Object[30];
    private int indexEntriesUsed = 0;


    public TinyDocumentImpl() {
        nodeNr = 0;
        document = this;
    }

	/**
	* Set the name pool used for all names in this document
	*/

	public void setNamePool(NamePool pool) {
		namePool = pool;
		addNamespace(0, pool.getNamespaceCode("xml", Namespace.XML));
	}

	/**
	* Get the name pool used for the names in this document
	*/

	public NamePool getNamePool() {
		return namePool;
	}

    protected void ensureNodeCapacity() {
        if (nodeType.length < numberOfNodes+1) {
            int k = numberOfNodes*2;

            byte[] nodeType2 = new byte[k];
            /*NEXT*/ int[] next2 = new int[k];
            short[] depth2 = new short[k];
            int[] offset2 = new int[k];
            int[] length2 = new int[k];
            int[] nameCode2 = new int[k];

            System.arraycopy(nodeType, 0, nodeType2, 0, numberOfNodes);
            /*NEXT*/ System.arraycopy(next, 0, next2, 0, numberOfNodes);
            System.arraycopy(depth, 0, depth2, 0, numberOfNodes);
            System.arraycopy(offset, 0, offset2, 0, numberOfNodes);
            System.arraycopy(length, 0, length2, 0, numberOfNodes);
            System.arraycopy(nameCode, 0, nameCode2, 0, numberOfNodes);

            nodeType = nodeType2;
            /*NEXT*/ next = next2;
            depth = depth2;
            offset = offset2;
            length = length2;
            nameCode = nameCode2;
        }
    }

    protected void ensureAttributeCapacity() {
        if (attParent.length < numberOfAttributes+1) {
            int k = numberOfAttributes*2;

            int[] attParent2 = new int[k];
            int[] attCode2 = new int[k];
            //byte[] attType2 = new byte[k];
            String[] attValue2 = new String[k];


            System.arraycopy(attParent, 0, attParent2, 0, numberOfAttributes);
            System.arraycopy(attCode, 0, attCode2, 0, numberOfAttributes);
            //System.arraycopy(attType, 0, attType2, 0, numberOfAttributes);
            System.arraycopy(attValue, 0, attValue2, 0, numberOfAttributes);

            attParent = attParent2;
            attCode = attCode2;
            //attType = attType2;
            attValue = attValue2;
        }
    }

    protected void ensureNamespaceCapacity() {
        if (namespaceParent.length < numberOfNamespaces+1) {
            int k = numberOfNamespaces*2;

            int[] namespaceParent2 = new int[k];
            int[] namespaceCode2 = new int[k];

            System.arraycopy(namespaceParent, 0, namespaceParent2, 0, numberOfNamespaces);
            System.arraycopy(namespaceCode, 0, namespaceCode2, 0, numberOfNamespaces);

            namespaceParent = namespaceParent2;
            namespaceCode = namespaceCode2;
        }
    }

    protected void addNode(short type0, int depth0, int offset0, int length0, int nameCode0) {
        ensureNodeCapacity();
        nodeType[numberOfNodes] = (byte)type0;
        depth[numberOfNodes] = (short)depth0;
        offset[numberOfNodes] = offset0;
        length[numberOfNodes] = length0;
        nameCode[numberOfNodes] = nameCode0;
        /*NEXT*/ next[numberOfNodes] = -1;      // safety precaution, esp for preview mode

        if (depth0 == 1) lastLevelOneNode = numberOfNodes;

        numberOfNodes++;
    }

    protected void appendChars(char[] chars, int start, int length) {
        while (charBuffer.length < charBufferLength + length) {
            char[] ch2 = new char[charBuffer.length * 2];
            System.arraycopy(charBuffer, 0, ch2, 0, charBufferLength);
            charBuffer = ch2;
        }
        System.arraycopy(chars, start, charBuffer, charBufferLength, length);
        charBufferLength += length;
    }

    /**
    * Truncate the tree: used in preview mode to delete an element after it has
    * been processed
    */

    protected void truncate(int nodes) {

        if (nodes==numberOfNodes) return;

        // shrink the text buffer
        for (int i=nodes; i<numberOfNodes; i++) {
            if (nodeType[i]==NodeInfo.TEXT) {
                charBufferLength = offset[i];
                break;
            }
        }

        // shrink the attributes array
        for (int i=nodes; i<numberOfNodes; i++) {
            if (nodeType[i]==NodeInfo.ELEMENT && offset[i]>=0) {
                numberOfAttributes = offset[i];
                break;
            }
        }

        // shrink the namespace array
        for (int i=nodes; i<numberOfNodes; i++) {
            if (nodeType[i]==NodeInfo.ELEMENT && length[i]>=0) {
                numberOfNamespaces = length[i];
                break;
            }
        }

        // TODO: shrink the comment buffer

        // shrink the main node array
        numberOfNodes = nodes;

        // kill the prior index, to be rebuilt when needed
        prior = null;

        // add a dummy node at the end, because some axes such as "following"
        // can otherwise walk off the end

        nodeType[nodes] = (byte)NodeInfo.ROOT;
        depth[nodes] = 0;
        // System.err.println("After truncate:"); diagnosticDump();
    }

    /**
    * On demand, make an index for quick access to preceding-sibling nodes
    */

    protected void ensurePriorIndex() {
        if (prior==null) {
            makePriorIndex();
        }
    }

    private synchronized void makePriorIndex() {
        prior = new int[numberOfNodes];
        for (int i=0; i<numberOfNodes; i++) {
            prior[i] = -1;
        }
        for (int i=0; i<numberOfNodes; i++) {
            int nextNode = next[i];
            if (nextNode!=-1) {
                prior[nextNode] = i;
            }
        }
    }


    protected void addAttribute(int parent0, int code0, String type0, String value0) {
        ensureAttributeCapacity();
        attParent[numberOfAttributes] = parent0;
        attCode[numberOfAttributes] = code0;
        attValue[numberOfAttributes] = value0;
        numberOfAttributes++;

        if (type0.equals("ID")) {
        	if (idTable==null) {
        		idTable = new Hashtable();
        	}
			NodeInfo e = getNode(parent0);
            registerID(e, value0);
        }
    }

    protected void addNamespace(int parent0, int nscode0 ) {
        usesNamespaces = true;
        ensureNamespaceCapacity();
        namespaceParent[numberOfNamespaces] = parent0;
        namespaceCode[numberOfNamespaces] = nscode0;
        numberOfNamespaces++;
    }

    public TinyNodeImpl getNode(int nr) {
        switch ((short)nodeType[nr]) {
            case NodeInfo.ROOT:
                return this;
            case NodeInfo.ELEMENT:
                return new TinyElementImpl(this, nr);
            case NodeInfo.TEXT:
                return new TinyTextImpl(this, nr);
            case NodeInfo.COMMENT:
                return new TinyCommentImpl(this, nr);
            case NodeInfo.PI:
                return new TinyProcInstImpl(this, nr);
        }
        return null;
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive.
    */

    public long getSequenceNumber() {
        return 0;
    }

    /**
    * Make a (transient) attribute node from the array of attributes
    */

    protected TinyAttributeImpl getAttributeNode(int nr) {
        return new TinyAttributeImpl(this, nr);
    }

    /**
    * determine whether this document uses namespaces
    */

    protected boolean isUsingNamespaces() {
        return usesNamespaces;
    }

    /**
    * Make a (transient) namespace node from the array of namespace declarations
    */

    protected TinyNamespaceImpl getNamespaceNode(int nr) {
        return new TinyNamespaceImpl(this, nr);
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
        systemIdMap.setSystemId(nodeNr, uri);
    }

    /**
    * Get the system id of this root node
    */

    public String getSystemId() {
        return systemIdMap.getSystemId(nodeNr);
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
        //if (uri==null) {
        //    throw new IllegalArgumentException("System ID must not be null");
        //}
        if (uri==null) {
            uri = "";
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
        lineNumberMap.setLineNumber(0, 0);
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
     * Find the parent node of this node.
     * @return The Node object describing the containing element or root node.
     */

    public NodeInfo getParent()  {
        return null;
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
    * Get a unique number identifying this document
    */

    //public int getDocumentNumber() {
    //    return documentNumber;
    //}

    /**
    * Get a list of all elements with a given name. This is implemented
    * as a memo function: the first time it is called for a particular
    * element type, it remembers the result for next time.
    */

    protected AxisEnumeration getAllElements(int fingerprint) {
    	Integer key = new Integer(fingerprint);
    	if (elementList==null) {
    	    elementList = new Hashtable();
    	}
        NodeSetExtent list = (NodeSetExtent)elementList.get(key);
        if (list==null) {
            list = new NodeSetExtent(LocalOrderComparer.getInstance());
            list.setSorted(true);
            for (int i=1; i<numberOfNodes; i++) {
                if (nodeType[i]==NodeInfo.ELEMENT &&
                        (nameCode[i] & 0xfffff ) == fingerprint) {
                    list.append(getNode(i));
                }
            }
            elementList.put(key, list);
        }
        return (AxisEnumeration)list.enumerate();
    }

    /**
    * Register a unique element ID. Fails if there is already an element with that ID.
    * @param e The NodeInfo (always an element) having a particular unique ID value
    * @param id The unique ID value
    */

    private void registerID(NodeInfo e, String id) {
        // the XPath spec (5.2.1) says ignore the second ID if it's not unique
        NodeInfo old = (NodeInfo)idTable.get(id);
        if (old==null) {
            idTable.put(id, e);
        }

    }

    /**
    * Get the element with a given ID.
    * @param id The unique ID of the required element, previously registered using registerID()
    * @return The NodeInfo (always an Element) for the given ID if one has been registered,
    * otherwise null.
    */

    public NodeInfo selectID(String id) {
        if (idTable==null) return null;			// no ID values found
        return (NodeInfo)idTable.get(id);
    }

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a set of nodes having that key value. If no index
    * has been built, returns null.
    */

    public synchronized Hashtable getKeyIndex(KeyManager keymanager, int fingerprint) {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager &&
            		 ((Integer)index[k+1]).intValue() == fingerprint) {
                Object ix = index[k+2];
                return (Hashtable)index[k+2];
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
    * maps the key value to a set of nodes having that key value. Or the String
    * "under construction", indicating that the index is being built.
    */

    public synchronized void setKeyIndex(KeyManager keymanager, int fingerprint, Hashtable keyindex) {
        for (int k=0; k<indexEntriesUsed; k+=3) {
            if (((KeyManager)index[k])==keymanager &&
            		 ((Integer)index[k+1]).intValue()==fingerprint) {
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

        // TODO: this could be optimized by walking all the descendants in order,
        // instead of doing a recursive tree walk. It would be necessary to maintain
        // a stack, so that end tags could be written when the depth decreases.

        // output the children

        AxisEnumeration children =
            getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());

        while (children.hasMoreElements()) {
            children.nextElement().copy(out);
        }
    }

	/**
	* Produce diagnostic print of main tree arrays
	*/

	public void diagnosticDump() {
		System.err.println("Node\ttype\tdepth\toffset\tlength");
		for (int i=0; i<numberOfNodes; i++) {
			System.err.println(i + "\t" + nodeType[i] + "\t" + depth[i] + "\t" +
									 offset[i] + "\t" + length[i] + "\t" + Navigator.getPath(getNode(i)));
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

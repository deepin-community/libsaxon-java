package com.icl.saxon.jdom;
import com.icl.saxon.om.*;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.pattern.NodeTest;
import org.jdom.*;
import org.jdom.Namespace;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
  * A node in the XML parse tree representing an XML element, character content, or attribute.<P>
  * This is the top class in the interface hierarchy for nodes; see NodeImpl for the implementation
  * hierarchy.
  * @author Michael H. Kay
  */

public class NodeWrapper implements NodeInfo {

    protected Object node;
    protected short nodeType;
    protected NodeWrapper parent;
    protected DocumentWrapper docWrapper;
    protected int index;
        // the index is the only way of distinguishing
        // two text nodes with the same parent!

    public NodeWrapper(Object node, NodeWrapper parent, int index) {
        this.node = node;
        this.parent = parent;
        this.index = index;
    }

    public NodeWrapper makeWrapper(Object node, NodeWrapper parent, int index) {
        NodeWrapper wrapper;
        if (node instanceof Document) {
            return docWrapper;
        } else if (node instanceof Element) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.ELEMENT;
        } else if (node instanceof Attribute) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.ATTRIBUTE;
        } else if (node instanceof String || node instanceof Text) { // changed in JDOM beta 8
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.TEXT;
        } else if (node instanceof CDATA) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.TEXT;
        } else if (node instanceof Comment) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.COMMENT;
        } else if (node instanceof ProcessingInstruction) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.PI;
        } else if (node instanceof Namespace) {
            wrapper = new NodeWrapper(node, parent, index);
            wrapper.nodeType = NodeInfo.NAMESPACE;
        } else {
            throw new IllegalArgumentException("Bad node type in JDOM! " + node.getClass() + " instance " + node.toString());
        }
        wrapper.docWrapper = parent.docWrapper;
        return wrapper;
    }

    /**
    * Get the underlying JDOM node
    */

    public Object getNode() {
        return node;
    }

    /**
    * Return the type of node.
    * @return one of the values Node.ELEMENT, Node.TEXT, Node.ATTRIBUTE, etc.
    */

    public short getNodeType() {
        return nodeType;
    }

    /**
    * Determine whether this is the same node as another node. <br />
    * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b)
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public boolean isSameNodeInfo(NodeInfo other) {
        if (!(other instanceof NodeWrapper)) {
            return false;
        }
        NodeWrapper ow = (NodeWrapper)other;
        if (nodeType != ow.getNodeType()) {
            return false;
        }
        if (index != ow.index) {
            return false;
        }
        if (node instanceof String) {
            return parent.isSameNodeInfo(ow.parent);
        } else {
            return node.equals(ow.node);    // this test doesn't work for text nodes in beta 0.7
                                            // but it's OK in beta 0.8
        }
    }

    /**
    * Get the System ID for the node.
    * @return the System Identifier of the entity in the source document containing the node,
    * or null if not known. Note this is not the same as the base URI: the base URI can be
    * modified by xml:base, but the system ID cannot.
    */

    public String getSystemId() {
        return docWrapper.baseURI;
    }

    public void setSystemId(String uri) {
        docWrapper.baseURI = uri;
    }

    /**
    * Get the Base URI for the node, that is, the URI used for resolving a relative URI contained
    * in the node. This will be the same as the System ID unless xml:base has been used.
    */

    public String getBaseURI() {
        return docWrapper.baseURI;
    }

    /**
    * Get line number
    * @return the line number of the node in its original source document; or -1 if not available
    */

    public int getLineNumber() {
        return -1;
    }

    /**
    * Determine the relative position of this node and another node, in document order.
    * The other node will always be in the same document.
    * @param other The other node, whose position is to be compared with this node
    * @return -1 if this node precedes the other node, +1 if it follows the other
    * node, or 0 if they are the same node. (In this case, isSameNode() will always
    * return true, and the two nodes will produce the same result for generateId())
    */

    public int compareOrder(NodeInfo other) {

        NodeWrapper ow = (NodeWrapper)other;

        // are they the same node?
        if (this.isSameNodeInfo(other)) {
            return 0;
        }

        // are they siblings (common case)
        if (this.getParent().isSameNodeInfo(other.getParent())) {
            return this.index - ow.index;
        }

        // find the depths of both nodes in the tree

        int depth1 = 0;
        int depth2 = 0;
        NodeInfo p1 = this;
        NodeInfo p2 = other;
        while (p1 != null) {
            depth1++;
            p1 = p1.getParent();
        }
        while (p2 != null) {
            depth2++;
            p2 = p2.getParent();
        }

        // move up one branch of the tree so we have two nodes on the same level

        p1 = this;
        while (depth1>depth2) {
            p1 = p1.getParent();
            if (p1.isSameNodeInfo(ow)) {
                return +1;
            }
            depth1--;
        }

        p2 = ow;
        while (depth2>depth1) {
            p2 = p2.getParent();
            if (p2.isSameNodeInfo(this)) {
                return -1;
            }
            depth2--;
        }

        // now move up both branches in sync until we find a common parent
        while (true) {
            NodeInfo par1 = p1.getParent();
            NodeInfo par2 = p2.getParent();
            if (par1==null || par2==null) {
                throw new NullPointerException("JDOM tree compare - internal error");
            }
            if (par1.isSameNodeInfo(par2)) {
                return ((NodeWrapper)p1).index - ((NodeWrapper)p2).index;
            }
            p1 = par1;
            p2 = par2;
        }
    }

    /**
    * Return the string value of the node. The interpretation of this depends on the type
    * of node. For an element it is the accumulated character content of the element,
    * including descendant elements.
    * @return the string value of the node
    */

    public String getStringValue() {
        switch (nodeType) {
            case NodeInfo.ROOT:
                List children1 = ((Document)node).getContent();
                StringBuffer sb1 = new StringBuffer();
                expandStringValue(children1, sb1);
                return sb1.toString();

            case NodeInfo.ELEMENT:
                List children2 = ((Element)node).getContent();
                StringBuffer sb2 = new StringBuffer();
                expandStringValue(children2, sb2);
                return sb2.toString();

            case NodeInfo.ATTRIBUTE:
                return ((Attribute)node).getValue();

            case NodeInfo.TEXT:
                if (node instanceof String) return (String)node;
                if (node instanceof Text) return ((Text)node).getText();
                if (node instanceof CDATA) return ((CDATA)node).getText();
                return "";

            case NodeInfo.COMMENT:
                return ((Comment)node).getText();

            case NodeInfo.PI:
                return ((ProcessingInstruction)node).getData();

            case NodeInfo.NAMESPACE:
                return ((Namespace)node).getURI();

            default:
                return "";
        }
    }

    private static void expandStringValue(List list, StringBuffer sb) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof Element) {
                expandStringValue(((Element)obj).getContent(), sb);
            } else if (obj instanceof String) {     // beta 0.7
                sb.append((String)obj);
            } else if (obj instanceof Text) {       // beta 0.8
                sb.append(((Text)obj).getText());
            } else if (obj instanceof CDATA) {
                sb.append(((CDATA)obj).getText());
            } else if (obj instanceof EntityRef) {
                throw new IllegalStateException("Unexpanded entity in JDOM tree");
            }
        }
    }

	/**
	* Get name code. The name code is a coded form of the node name: two nodes
	* with the same name code have the same namespace URI, the same local name,
	* and the same prefix. By masking the name code with &0xfffff, you get a
	* fingerprint: two nodes with the same fingerprint have the same local name
	* and namespace URI.
    * @see com.icl.saxon.om.NamePool#allocate allocate
	*/

	public int getNameCode() {
	    switch (nodeType) {
            case NAMESPACE:
                if (((Namespace)node).getPrefix().equals("")) return -1;
                // else fall through
	        case ELEMENT:
	        case ATTRIBUTE:
	        case PI:
	            return docWrapper.namePool.allocate(getPrefix(),
	                                                getURI(),
	                                                getLocalName());
	        default:
	            return -1;
	    }
	}

	/**
	* Get fingerprint. The fingerprint is a coded form of the expanded name
	* of the node: two nodes
	* with the same name code have the same namespace URI and the same local name.
	* A fingerprint of -1 should be returned for a node with no name.
	*/

	public int getFingerprint() {
	    return getNameCode()&0xfffff;
	}

    /**
    * Get the local part of the name of this node. This is the name after the ":" if any.
    * @return the local part of the name. For an unnamed node, return an empty string.
    */

    public String getLocalName() {
        switch (nodeType) {
            case NodeInfo.ELEMENT:
                return ((Element)node).getName();
            case NodeInfo.ATTRIBUTE:
                return ((Attribute)node).getName();
            case NodeInfo.TEXT:
            case NodeInfo.COMMENT:
            case NodeInfo.ROOT:
                return "";
            case NodeInfo.PI:
                return ((ProcessingInstruction)node).getTarget();
            case NodeInfo.NAMESPACE:
                return ((Namespace)node).getPrefix();
            default:
                return "";
        }
    }

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. For an unnamed node, return an empty string.
    */

    public String getPrefix() {
        switch (nodeType) {
            case NodeInfo.ELEMENT:
                return ((Element)node).getNamespacePrefix();
            case NodeInfo.ATTRIBUTE:
                return ((Attribute)node).getNamespacePrefix();
            default:
                return "";
        }
    }

    /**
    * Get the URI part of the name of this node. This is the URI corresponding to the
    * prefix, or the URI of the default namespace if appropriate.
    * @return The URI of the namespace of this node. For an unnamed node, return null.
    * For a node with an empty prefix, return an empty string.
    */

    public String getURI() {
        switch (nodeType) {
            case NodeInfo.ELEMENT:
                return ((Element)node).getNamespaceURI();
            case NodeInfo.ATTRIBUTE:
                return ((Attribute)node).getNamespaceURI();
            default:
                return "";
        }
    }

    /**
    * Get the display name of this node. For elements and attributes this is [prefix:]localname.
    * For unnamed nodes, it is an empty string.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        switch (nodeType) {
            case NodeInfo.ELEMENT:
                return ((Element)node).getQualifiedName();
            case NodeInfo.ATTRIBUTE:
                return ((Attribute)node).getQualifiedName();
            case NodeInfo.PI:
            case NodeInfo.NAMESPACE:
                return getLocalName();
            default:
                return "";

        }
    }

    /**
    * Get the NodeInfo object representing the parent of this node
    */

    public NodeInfo getParent() {
        return parent;
    }

    /**
    * Return an enumeration over the nodes reached by the given axis from this node
    * @param nodeTest the type(s) of node to be included, e.g. NodeInfo.ELEMENT, NodeInfo.TEXT.
    * The value NodeInfo.NODE means include any type of node.
    * @param nodeTest A pattern to be matched by the returned nodes
    * @return a NodeEnumeration that scans the nodes reached by the axis in turn.
    */

    public AxisEnumeration getEnumeration(byte axisNumber, NodeTest nodeTest) {
        switch (axisNumber) {
            case Axis.ANCESTOR:
                if (nodeType==ROOT) return EmptyEnumeration.getInstance();
                return new FilterEnumeration(
                            new AncestorEnumeration(this, false),
                            nodeTest);

            case Axis.ANCESTOR_OR_SELF:
                if (nodeType==ROOT) return EmptyEnumeration.getInstance();
                return new FilterEnumeration(
                            new AncestorEnumeration(this, true),
                            nodeTest);

            case Axis.ATTRIBUTE:
                if (nodeType!=ELEMENT) return EmptyEnumeration.getInstance();
                return new FilterEnumeration(
                            new AttributeEnumeration(this),
                            nodeTest);

            case Axis.CHILD:
                if (hasChildNodes()) {
                    return new FilterEnumeration(
                            new ChildEnumeration(this, true, true),
                            nodeTest);
                } else {
                    return EmptyEnumeration.getInstance();
                }

            case Axis.DESCENDANT:
                if (hasChildNodes()) {
                    return new FilterEnumeration(
                            new DescendantEnumeration(this, false, true),
                            nodeTest);
                } else {
                    return EmptyEnumeration.getInstance();
                }

            case Axis.DESCENDANT_OR_SELF:
                 return new FilterEnumeration(
                            new DescendantEnumeration(this, true, true),
                            nodeTest);

            case Axis.FOLLOWING:
                 return new FilterEnumeration(
                            new FollowingEnumeration(this),
                            nodeTest);

            case Axis.FOLLOWING_SIBLING:
                 switch (nodeType) {
                    case ROOT:
                    case ATTRIBUTE:
                    case NAMESPACE:
                        return EmptyEnumeration.getInstance();
                    default:
                        return new FilterEnumeration(
                            new ChildEnumeration(this, false, true),
                            nodeTest);
                 }

            case Axis.NAMESPACE:
                 if (nodeType!=ELEMENT) return EmptyEnumeration.getInstance();
                 return new FilterEnumeration(
                                new NamespaceEnumeration(this),
                                nodeTest);
                 //throw new IllegalArgumentException(
                 //           "namespace axis not implemented for JDOM");

            case Axis.PARENT:
                 if (parent==null) return EmptyEnumeration.getInstance();
                 if (nodeTest.matches(parent)) return new SingletonEnumeration(parent);
                 return EmptyEnumeration.getInstance();

            case Axis.PRECEDING:
                 return new FilterEnumeration(
                            new PrecedingEnumeration(this, false),
                            nodeTest);

            case Axis.PRECEDING_SIBLING:
                 switch (nodeType) {
                    case ROOT:
                    case ATTRIBUTE:
                    case NAMESPACE:
                        return EmptyEnumeration.getInstance();
                    default:
                        return new FilterEnumeration(
                            new ChildEnumeration(this, false, false),
                            nodeTest);
                 }

            case Axis.SELF:
                 if (nodeTest.matches(this)) return new SingletonEnumeration(this);
                 return EmptyEnumeration.getInstance();

            case Axis.PRECEDING_OR_ANCESTOR:
                 return new FilterEnumeration(
                            new PrecedingEnumeration(this, true),
                            nodeTest);

            default:
                 throw new IllegalArgumentException("Unknown axis number " + axisNumber);
        }
    }

    /**
     * Find the value of a given attribute of this node. <BR>
     * This method is defined on all nodes to meet XSL requirements, but for nodes
     * other than elements it will always return null.
     * @param uri the namespace uri of an attribute ("" if no namespace)
     * @param localName the local name of the attribute
     * @return the value of the attribute, if it exists, otherwise null
     */

    public String getAttributeValue(String uri, String localName) {
        if (nodeType==NodeInfo.ELEMENT) {
            Namespace ns = (uri.equals("http://www.w3.org/XML/1998/namespace") ?
                            Namespace.XML_NAMESPACE :
                            Namespace.getNamespace(uri));
            return ((Element)node).getAttributeValue(localName, ns);
        } else {
            return "";
        }
    }

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint) {
        if (nodeType==NodeInfo.ELEMENT) {
            Iterator list = ((Element)node).getAttributes().iterator();
            NamePool pool = docWrapper.getNamePool();
            while (list.hasNext()) {
                Attribute att = (Attribute)list.next();
                int nameCode = pool.allocate(att.getNamespacePrefix(),
                                             att.getNamespaceURI(),
                                             att.getName());
                if (fingerprint == (nameCode & 0xfffff)) {
                    return att.getValue();
                }
            }
        }
        return null;
    }

    /**
    * Get the root (document) node
    * @return the DocumentInfo representing the containing document
    */

    public DocumentInfo getDocumentRoot() {
        return docWrapper;
    }

    /**
    * Determine whether the node has any children. <br />
    * Note: the result is equivalent to <br />
    * getEnumeration(Axis.CHILD, AnyNodeTest.getInstance()).hasMoreElements()
    */

    public boolean hasChildNodes() {
        switch (nodeType) {
            case NodeInfo.ROOT:
                return true;
            case NodeInfo.ELEMENT:
                return !((Element)node).getContent().isEmpty();
            default:
                return false;
        }
    }
    /**
    * Get a character string that uniquely identifies this node.<br />
    * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b)
    * @return a string that uniquely identifies this node, within this
    * document. The calling code prepends information to make the result
    * unique across all documents.
    */

    public String generateId() {
        if (node instanceof String || node instanceof Text) {
            return parent.generateId() + "text" + index;
        } else {
            return "j" + node.hashCode();
        }
    }

    /**
    * Copy this node to a given outputter (deep copy)
    */

    public void copy(Outputter out) throws TransformerException {

        // TODO: this is a completely generic implementation: it ought to invoke a
        // helper class.

        switch (nodeType) {
            case NodeInfo.ROOT:
                AxisEnumeration children0 = getEnumeration(Axis.CHILD, new AnyNodeTest());
                while (children0.hasMoreElements()) {
                    children0.nextElement().copy(out);
                }
                return;

            case NodeInfo.ELEMENT:
        		int nc = getNameCode();
                out.writeStartTag(nc);

                // output the namespaces

                outputNamespaceNodes(out, true);

                // output the attributes

                AxisEnumeration attributes = getEnumeration(Axis.ATTRIBUTE, new AnyNodeTest());
                while (attributes.hasMoreElements()) {
                    attributes.nextElement().copy(out);
                }

                // output the children

                AxisEnumeration children = getEnumeration(Axis.CHILD, new AnyNodeTest());
                while (children.hasMoreElements()) {
                    children.nextElement().copy(out);
                }

                // finally the end tag

                out.writeEndTag(nc);
                return;

            case NodeInfo.ATTRIBUTE:
                out.writeAttribute(getNameCode(), getStringValue());
                return;

            case NodeInfo.TEXT:
                out.writeContent(getStringValue());
                return;

            case NodeInfo.COMMENT:
                out.writeComment(getStringValue());
                return;

            case NodeInfo.PI:
                out.writePI(getLocalName(), getStringValue());
                return;

            case NodeInfo.NAMESPACE:
                out.copyNamespaceNode(docWrapper.getNamePool().allocateNamespaceCode(
                        ((Namespace)node).getPrefix(),
                        ((Namespace)node).getURI()));

            default:

        }
    }

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws TransformerException {
        out.writeContent(getStringValue());
    }

    /**
    * Output all namespace nodes associated with this element. Does nothing if
    * the node is not an element.
    * @param out The relevant outputter
    * @param includeAncestors True if namespaces declared on ancestor elements must
    * be output; false if it is known that these are already on the result tree
    */

    public void outputNamespaceNodes(Outputter out, boolean includeAncestors)
        throws TransformerException {
            // TODO: use the includeAncestors flag for optimization
        if (nodeType==ELEMENT) {
            NamePool pool = docWrapper.getNamePool();
            AxisEnumeration enm = getEnumeration(Axis.NAMESPACE,
                                                  AnyNodeTest.getInstance());
            while (enm.hasMoreElements()) {
                Namespace ns = (Namespace)((NodeWrapper)enm.nextElement()).node;
                int nscode = pool.allocateNamespaceCode(
                                ns.getPrefix(),
                                ns.getURI());
                out.writeNamespaceDeclaration(nscode);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Axis enumeration classes
    ///////////////////////////////////////////////////////////////////////////////

    private class FilterEnumeration implements AxisEnumeration {
        private BaseEnumeration base;
        private NodeTest nodeTest;
        private NodeInfo next;
        private int last = -1;

        public FilterEnumeration(BaseEnumeration base, NodeTest test) {
            this.base = base;
            this.nodeTest = test;
            advance();
        }

        public void advance() {
            while (base.hasMoreElements()) {
    	        next = base.nextElement();
    	        if (nodeTest.matches(next)) return;
    	    }
    	    next = null;
    	}

    	public NodeInfo nextElement() {
    	    NodeInfo n = next;
    	    advance();
    	    return n;
        }

    	public boolean hasMoreElements() {
    	    return next!=null;
    	}

    	public int getLastPosition() {

    	    // To find out how many nodes there are in the axis, we
    	    // make a copy of the original node enumeration, and run through
    	    // the whole thing again, counting how many nodes match the filter.

    	    if (last>=0) {
    	        return last;
    	    }
    	    last = 0;
    	    BaseEnumeration b = base.copy();
    	    while (b.hasMoreElements()) {
    	        NodeInfo n = b.nextElement();
    	        if (nodeTest.matches(n)) {
    	            last++;
    	        }
    	    }
    	    return last;
    	}

    	public boolean isSorted() {
    	    return base.isSorted();
    	}

        public boolean isReverseSorted() {
            return base.isReverseSorted();
        }

    	public boolean isPeer() {
    	    return base.isPeer();
    	}
	}

	private abstract class BaseEnumeration implements AxisEnumeration {

        protected NodeWrapper next;

        public final boolean hasMoreElements() {
            return next!=null;
        }

        public final NodeInfo nextElement() {
            NodeInfo n = next;
            advance();
            return n;
        }

        public abstract void advance();

	    public abstract BaseEnumeration copy();

	    public final int getLastPosition() {// not used
	        return 1;
	    }

	    public boolean isSorted() {
	        return true;
	    }

	    public final boolean isReverseSorted() {
	        return !isSorted();
	    }

	    public boolean isPeer() {
	        return false;
	    }
	}


    final class AncestorEnumeration extends BaseEnumeration {

        private boolean includeSelf;
        private NodeWrapper start;

        public AncestorEnumeration(NodeWrapper start, boolean includeSelf) {
            this.start = start;
            this.includeSelf = includeSelf;
            this.next = start;
            if (!includeSelf) {
                advance();
            }
        }

        public void advance() {
            next=(NodeWrapper)next.getParent();
        }

	    public boolean isSorted() {
	        return false;
	    }

	    public boolean isPeer() {
	        return false;
	    }

        public BaseEnumeration copy() {
            return new AncestorEnumeration(start, includeSelf);
        }

    } // end of class AncestorEnumeration

    private final class AttributeEnumeration extends BaseEnumeration {

        private Iterator atts;
        private int ix = 0;
        private NodeWrapper start;

        public AttributeEnumeration(NodeWrapper start) {
            this.start = start;
            atts = ((Element)start.node).getAttributes().iterator();
            advance();
        }

        public void advance() {
            if (atts.hasNext()) {
                next = makeWrapper(atts.next(), start, ix++);
            } else {
                next = null;
            }
        }

	    public boolean isPeer() {
	        return true;
	    }

        public BaseEnumeration copy() {
            return new AttributeEnumeration(start);
        }

    }  // end of class AttributeEnumeration

    private final class NamespaceEnumeration extends BaseEnumeration {

        private HashMap nslist = new HashMap();
        private Iterator prefixes;
        private int ix = 0;
        private NodeWrapper start;

        public NamespaceEnumeration(NodeWrapper start) {
            this.start = start;
            NodeWrapper curr = start;

            // build the complete list of namespaces

            do {
                Element elem = (Element)curr.node;
                Namespace ns = elem.getNamespace();
                if (!nslist.containsKey(ns.getPrefix())) {
                    nslist.put(ns.getPrefix(), ns);
                }
                List addl = elem.getAdditionalNamespaces();
                if (addl.size() > 0) {
                    Iterator itr = addl.iterator();
                    while (itr.hasNext()) {
                        ns = (Namespace) itr.next();
                        if (!nslist.containsKey(ns.getPrefix())) {
                            nslist.put(ns.getPrefix(), ns);
                        }
                    }
                }
                curr = (NodeWrapper)curr.getParent();
            } while (curr.getNodeType()==ELEMENT);

            nslist.put("xml", Namespace.XML_NAMESPACE);
            prefixes = nslist.keySet().iterator();
            advance();
        }

        public void advance() {
            if (prefixes.hasNext()) {
                String prefix = (String)prefixes.next();
                Namespace ns = (Namespace)nslist.get(prefix);
                next = makeWrapper(ns, start, ix++);
            } else {
                next = null;
            }
        }

	    public boolean isPeer() {
	        return true;
	    }

        public BaseEnumeration copy() {
            return new NamespaceEnumeration(start);
        }

    }  // end of class NamespaceEnumeration


    /**
    * The class ChildEnumeration handles not only the child axis, but also the
    * following-sibling and preceding-sibling axes. It can also enumerate the children
    * of the start node in reverse order, something that is needed to support the
    * preceding and preceding-or-ancestor axes (the latter being used by xsl:number)
    */

    private class ChildEnumeration extends BaseEnumeration {

        private NodeWrapper start;
        private NodeWrapper commonParent;
        private ListIterator children;
        private int ix = 0;
        private boolean downwards;  // enumerate children of start node (not siblings)
        private boolean forwards;   // enumerate in document order (not reverse order)

        public ChildEnumeration(NodeWrapper start,
                                boolean downwards, boolean forwards) {
            this.start = start;
            this.downwards = downwards;
            this.forwards = forwards;

            if (downwards) {
                commonParent = start;
            } else {
                commonParent = (NodeWrapper)start.getParent();
            }

            if (commonParent.getNodeType()==ROOT) {
                children = ((Document)commonParent.node).getContent().listIterator();
            } else {
                children = ((Element)commonParent.node).getContent().listIterator();
            }

            if (downwards) {
                if (!forwards) {
                    // backwards enumeration: go to the end
                    while (children.hasNext()) {
                        children.next();
                        ix++;
                    }
                }
            } else {
                ix = start.index;
                // find the start node among the list of siblings
                if (forwards) {
                    for (int i=0; i<=ix; i++) {
                        children.next();
                    }
                    ix++;
                } else {
                    for (int i=0; i<ix; i++) {
                        children.next();
                    }
                    ix--;
                }
            }
            advance();
        }



        public void advance() {
            if (forwards) {
                if (children.hasNext()) {
                    while (true) {
                        Object nextChild = children.next();
                        if (nextChild instanceof EntityRef) {
                            throw new IllegalStateException("Unexpanded entity in JDOM tree");
                        } else if (nextChild instanceof DocType) {
                            continue;
                        } else {
                            next = makeWrapper(nextChild, commonParent, ix++);
                            break;
                        }
                    }
                } else {
                    next = null;
                }
            } else {    // backwards
                if (children.hasPrevious()) {
                    Object nextChild = children.previous();
                    if (nextChild instanceof EntityRef) {
                        throw new IllegalStateException("Unexpanded entity in JDOM tree");
                    } else {
                        next = makeWrapper(nextChild, commonParent, ix--);
                    }
                } else {
                    next = null;
                }
            }
        }

        public boolean isSorted() {
            return forwards;
        }

        public boolean isPeer() {
            return true;
        }

        public BaseEnumeration copy() {
            return new ChildEnumeration(start, downwards, forwards);
        }

    } // end of class ChildEnumeration

    /**
    * The DescendantEnumeration class supports the XPath descendant axis.
    * But it also has the option to return the descendants in reverse document order;
    * this is used when evaluating the preceding axis. Note that the includeSelf option
    * should not be used when scanning in reverse order, as the self node will always be
    * returned first.
    */

    private final class DescendantEnumeration extends BaseEnumeration {

        private AxisEnumeration children = null;
        private AxisEnumeration descendants = null;
        private NodeWrapper start;
        private boolean includeSelf;
        private boolean forwards;
        private boolean atEnd = false;

        public DescendantEnumeration(NodeWrapper start,
                                 boolean includeSelf, boolean forwards) {
            this.start = start;
            this.includeSelf = includeSelf;
            this.forwards = forwards;
            advance();
        }

        public void advance() {
            if (descendants!=null) {
                if (descendants.hasMoreElements()) {
                    next = (NodeWrapper)descendants.nextElement();
                    return;
                } else {
                    descendants = null;
                }
            }
            if (children!=null) {
                if (children.hasMoreElements()) {
                    NodeWrapper n = (NodeWrapper)children.nextElement();
                    if (n.hasChildNodes()) {
                        if (forwards) {
                            descendants = new DescendantEnumeration(n,
                                             false, forwards);
                            next = n;
                        } else {
                            descendants = new DescendantEnumeration(n, true, forwards);
                            advance();
                        }
                    } else {
                        next = n;
                    }
                } else {
                    if (forwards || !includeSelf) {
                        next = null;
                    } else {
                        atEnd = true;
                        children = null;
                        next = start;
                    }
                }
            } else if (atEnd) {
                // we're just finishing a backwards scan
                next = null;
            } else {
                // we're just starting...
                if (start.hasChildNodes()) {
                    children = new ChildEnumeration(start, true, forwards);
                } else {
                    children = EmptyEnumeration.getInstance();
                }
                if (forwards && includeSelf) {
                    next = start;
                } else {
                    advance();
                }
            }
        }

        public boolean isSorted() {
            return forwards;
        }

        public boolean isPeer() {
            return false;
        }

        public BaseEnumeration copy() {
            return new DescendantEnumeration(start, includeSelf, forwards);
        }

    } // end of class DescendantEnumeration

    private class FollowingEnumeration extends BaseEnumeration {

        private NodeWrapper start;
        private AxisEnumeration ancestorEnum = null;
        private AxisEnumeration siblingEnum = null;
        private AxisEnumeration descendEnum = null;

        public FollowingEnumeration(NodeWrapper start) {
            this.start = start;
            ancestorEnum = new AncestorEnumeration(start, false);
            switch (start.getNodeType()) {
                case ELEMENT:
                case TEXT:
                case COMMENT:
                case PI:
                    siblingEnum = new ChildEnumeration(start, false, true);
                    break;
                case ATTRIBUTE:
                case NAMESPACE:
                    siblingEnum = new ChildEnumeration((NodeWrapper)start.getParent(), true, true);
                        // gets children of the attribute's parent node
                    break;
                default:
                    siblingEnum = EmptyEnumeration.getInstance();
            }
            advance();
        }

        public void advance() {
            if (descendEnum!=null) {
                if (descendEnum.hasMoreElements()) {
                    next = (NodeWrapper)descendEnum.nextElement();
                    return;
                } else {
                    descendEnum = null;
                }
            }
            if (siblingEnum!=null) {
                if (siblingEnum.hasMoreElements()) {
                    next = (NodeWrapper)siblingEnum.nextElement();
                    if (next.hasChildNodes()) {
                        descendEnum = new DescendantEnumeration(next, false, true);
                    } else {
                        descendEnum = null;
                    }
                    return;
                } else {
                    descendEnum = null;
                    siblingEnum = null;
                }
            }
            if (ancestorEnum.hasMoreElements()) {
                next = (NodeWrapper)ancestorEnum.nextElement();
                if (next.getNodeType() == ROOT) {
                    siblingEnum = EmptyEnumeration.getInstance();
                } else {
                    siblingEnum = new ChildEnumeration(next, false, true);
                }
                advance();
            } else {
                next = null;
            }
        }

        public boolean isSorted() {
            return true;
        }

        public boolean isPeer() {
            return false;
        }

        public BaseEnumeration copy() {
            return new FollowingEnumeration(start);
        }

    } // end of class FollowingEnumeration

    private class PrecedingEnumeration extends BaseEnumeration {

        private NodeWrapper start;
        private AxisEnumeration ancestorEnum = null;
        private AxisEnumeration siblingEnum = null;
        private AxisEnumeration descendEnum = null;
        private boolean includeAncestors;

        public PrecedingEnumeration(NodeWrapper start, boolean includeAncestors) {
            this.start = start;
            this.includeAncestors = includeAncestors;
            ancestorEnum = new AncestorEnumeration(start, false);
            switch (start.getNodeType()) {
                case ELEMENT:
                case TEXT:
                case COMMENT:
                case PI:
                    // get preceding-sibling enumeration
                    siblingEnum = new ChildEnumeration(start, false, false);
                    break;
                default:
                    siblingEnum = EmptyEnumeration.getInstance();
            }
            advance();
        }

        public void advance() {
            if (descendEnum!=null) {
                if (descendEnum.hasMoreElements()) {
                    next = (NodeWrapper)descendEnum.nextElement();
                    return;
                } else {
                    descendEnum = null;
                }
            }
            if (siblingEnum!=null) {
                if (siblingEnum.hasMoreElements()) {
                    NodeWrapper sib = (NodeWrapper)siblingEnum.nextElement();
                    if (sib.hasChildNodes()) {
                        descendEnum = new DescendantEnumeration(sib, true, false);
                        advance();
                    } else {
                        descendEnum = null;
                        next = sib;
                    }
                    return;
                } else {
                    descendEnum = null;
                    siblingEnum = null;
                }
            }
            if (ancestorEnum.hasMoreElements()) {
                next = (NodeWrapper)ancestorEnum.nextElement();
                if (next.getNodeType() == ROOT) {
                    siblingEnum = EmptyEnumeration.getInstance();
                } else {
                    siblingEnum = new ChildEnumeration(next, false, false);
                }
                if (!includeAncestors) {
                    advance();
                }
            } else {
                next = null;
            }
        }

        public boolean isSorted() {
            return false;
        }

        public boolean isPeer() {
            return false;
        }

        public BaseEnumeration copy() {
            return new PrecedingEnumeration(start, includeAncestors);
        }

    } // end of class PrecedingEnumeration

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
// Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

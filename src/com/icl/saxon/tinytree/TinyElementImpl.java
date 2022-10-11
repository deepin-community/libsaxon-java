package com.icl.saxon.tinytree;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.AxisEnumeration;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.tree.DOMExceptionImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;

/**
  * A node in the XML parse tree representing an XML element.<P>
  * This class is an implementation of NodeInfo and also implements the
  * DOM Element interface
  * @author Michael H. Kay
  */

final class TinyElementImpl extends TinyParentNodeImpl
    implements Element {

    /**
    * Constructor
    */

    public TinyElementImpl(TinyDocumentImpl doc, int nodeNr) {
        this.document = doc;
        this.nodeNr = nodeNr;
    }

    /**
    * Return the type of node.
    * @return NodeInfo.ELEMENT
    */

    public final short getNodeType() {
        return ELEMENT;
    }

    /**
    * Get the base URI of this element node. This will be the same as the System ID unless
    * xml:base has been used.
    */

    public String getBaseURI() {
        String xmlBase = getAttributeValue(Namespace.XML, "base");
        if (xmlBase!=null) {
            return xmlBase;
        }
        String startSystemId = getSystemId();
        NodeInfo parent = getParent();
        String parentSystemId = parent.getSystemId();
        if (startSystemId.equals(parentSystemId)) {
            return parent.getBaseURI();
        } else {
            return startSystemId;
        }
    }

    /**
    * Output all namespace nodes associated with this element.
    * @param out The relevant outputter
    * @param includeAncestors True if namespaces associated with ancestor
    * elements must also be output; false if these are already known to be
    * on the result tree.
    */

    public void outputNamespaceNodes(Outputter out, boolean includeAncestors)
                throws TransformerException {

        int ns = document.length[nodeNr]; // by convention
        if (ns>0 ) {
            while (ns < document.numberOfNamespaces &&
                    document.namespaceParent[ns] == nodeNr ) {
                int nscode = document.namespaceCode[ns];
                out.writeNamespaceDeclaration(nscode);
                ns++;
            }
        }

        // now add the namespaces defined on the ancestor nodes. We rely on the outputter
        // to eliminate multiple declarations of the same prefix

        if (includeAncestors && document.isUsingNamespaces()) {
            getParent().outputNamespaceNodes(out, true);
            // terminates when the parent is a root node
        }
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */

    public boolean hasAttributes() {
        return document.offset[nodeNr] >= 0;
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
        int f = document.getNamePool().getFingerprint(uri, localName);
		return getAttributeValue(f);
    }

    /**
    * Get the value of a given attribute of this node
    * @param fingerprint The fingerprint of the attribute name
    * @return the attribute value if it exists or null if not
    */

    public String getAttributeValue(int fingerprint) {
        int a = document.offset[nodeNr];
        if (a<0) return null;
        while (a < document.numberOfAttributes && document.attParent[a] == nodeNr) {
            if ((document.attCode[a] & 0xfffff) == fingerprint ) {
                return document.attValue[a];
            }
            a++;
        }
        return null;
    }

    /**
    * Make an attribute node for a given attribute of this element
    * @param index The relative position of the attribute, counting from zero. This
    * is trusted to be in range.
    */

    public TinyAttributeImpl makeAttributeNode(int index) {
        int a = document.offset[nodeNr];
        if (a<0) return null;
        return document.getAttributeNode(a+index);
    }

    /**
    * Set the value of an attribute on the current element. This affects subsequent calls
    * of getAttribute() for that element.
    * @param name The name of the attribute to be set. Any prefix is interpreted relative
    * to the namespaces defined for this element.
    * @param value The new value of the attribute. Set this to null to remove the attribute.
    */

    public void setAttribute(String name, String value ) throws DOMException {
        throw new DOMExceptionImpl((short)9999, "Saxon DOM is not updateable");
    }

    /**
    * Copy this node to a given outputter (supporting xsl:copy-of)
    */

    public void copy(Outputter out) throws TransformerException {
        copy(out, true);
    }

    /**
    * Copy this node to a given outputter
    * @param allNamespaces true if all namespace nodes must be copied; false
    * if namespace nodes for the parent element are already on the result tree
    */

    public void copy(Outputter out, boolean allNamespaces) throws TransformerException {

        // TODO: this could be optimized by walking all the descendants in order,
        // instead of doing a recursive tree walk. It would be necessary to maintain
        // a stack, so that end tags could be written when the depth decreases.

        int nc = getNameCode();
        out.writeStartTag(nc);

        // output the namespaces

        outputNamespaceNodes(out, allNamespaces);

        // output the attributes

        int a = document.offset[nodeNr];
        if (a >= 0) {
            while (a < document.numberOfAttributes && document.attParent[a] == nodeNr) {
            	document.getAttributeNode(a).copy(out);
                a++;
            }
        }

        // output the children

        AxisEnumeration children =
            getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());

        while (children.hasMoreElements()) {
            NodeInfo next = children.nextElement();
            if (next instanceof TinyElementImpl) {
                ((TinyElementImpl)next).copy(out, false);
                          // no need to do all the namespaces again
            } else {
                next.copy(out);
            }
        }
        out.writeEndTag(nc);
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

package com.icl.saxon.tinytree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;

import javax.xml.transform.TransformerException;
/**
  * A node in the XML parse tree representing a Namespace. Note that this is
  * generated only "on demand", when the namespace axis is expanded.<P>
  * @author Michael H. Kay
  * @version 28 September 2000
  */

final class TinyNamespaceImpl extends TinyNodeImpl {

    private int parentNode;     // an entry in the namespace array corresponds
                                // to a namespace declaration. This can result in one
                                // namespace node for each ancestor element. Therefore
                                // the namespace node needs to contain a reference to the
                                // actual parent element.
	private int nameCode;		// the name code of the name of the namespace node.
								// The name of the namespace node is the prefix: the
								// namecode is NOT the same as the namespace code, which
								// identifies the prefix/uri pair

    public TinyNamespaceImpl(TinyDocumentImpl doc, int nodeNr) {
        document = doc;
        this.nodeNr = nodeNr;
        nameCode = document.getNamePool().allocate("", "", getLocalName());
    }

    /**
    * Get the namespace code (a numeric representation of the prefix and URI)
    */

    public int getNamespaceCode() {
        return document.namespaceCode[nodeNr];
    }

    /**
    * Get the fingerprint
    */

    public int getFingerprint() {
        return nameCode & 0xfffff;
    }

    /**
    * Set the parent element for this namespace node
    */

    void setParentNode(int nodeNr) {
        parentNode = nodeNr;
    }

	/**
	* Get the nameCode, for name matching
	*/

	public int getNameCode() {
		return nameCode;
	}

    /**
    * Get the prefix part of the name of this node. This is the name before the ":" if any.
    * @return the prefix part of the name. Always null.
    */

    public String getPrefix() {
        return null;
    }

    /**
    * Get the display name of this node. For namespaces this is the namespace prefix.
    * @return The display name of this node.
    * For a node with no name, return an empty string.
    */

    public String getDisplayName() {
        return getLocalName();
    }

    /**
    * Get the local name of this node. For namespaces this is the namespace prefix.
    * @return The local name of this node.
    */

    public String getLocalName() {
        return document.getNamePool().getPrefixFromNamespaceCode(
        				document.namespaceCode[nodeNr]);
    }

    /**
    * Get the URI part of the name of this node.
    * @return The URI of the namespace of this node. Always null.
    */

    public String getURI() {
        return null;
    }

    /**
    * Get the parent element of this namespace node
    */

    public NodeInfo getParent() {
        return document.getNode(parentNode);
    }

    /**
    * Determine whether this is the same node as another node
    * @return true if this Node object and the supplied Node object represent the
    * same node in the tree.
    */

    public final boolean isSameNodeInfo(NodeInfo other) {
        if (!(other instanceof TinyNamespaceImpl)) return false;
        if (this==other) return true;
        TinyNamespaceImpl otherN = (TinyNamespaceImpl)other;
        return (this.parentNode==((TinyNamespaceImpl)other).parentNode &&
             this.document==otherN.document &&
             this.nodeNr==((TinyNamespaceImpl)other).nodeNr);
    }

    /**
    * Return the type of node.
    * @return NodeInfo.NAMESPACE
    */

    public final short getNodeType() {
        return NAMESPACE;
    }

    /**
    * Return the string value of the node.
    * @return the namespace uri
    */

    public final String getStringValue() {
        return document.getNamePool().getURIFromNamespaceCode(
        				document.namespaceCode[nodeNr]);
    }

    /**
    * Get unique identifier. Returns key of owning element with the name fingerprint as a suffix
    */

    public String generateId() {
        return (getParent()).generateId() + "_xmlns_" + getFingerprint();
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException {
		out.copyNamespaceNode(getNamespaceCode());
    }

    /**
    * Get the node sequence number (in document order). Sequence numbers are monotonic but not
    * consecutive. In the current implementation, parent nodes (elements and roots) have a zero
    * least-significant word, while namespaces, attributes, text nodes, comments, and PIs have
    * the top word the same as their owner and the bottom half reflecting their relative position.
    */

    protected long getSequenceNumber() {
        return ((TinyNodeImpl)getParent()).getSequenceNumber() + nodeNr + 1;
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

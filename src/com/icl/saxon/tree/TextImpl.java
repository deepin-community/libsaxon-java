package com.icl.saxon.tree;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.output.Outputter;

import org.w3c.dom.Text;
import org.w3c.dom.DOMException;
import javax.xml.transform.TransformerException;

/**
  * A node in the XML parse tree representing character content<P>
  * @author Michael H. Kay
  */

final class TextImpl extends NodeImpl implements Text {

	private NodeInfo parent;
    private String content;

    public TextImpl(ParentNodeImpl parent, String content) {
    	this.parent = parent;
    	this.content = content;
    }

	/**
	* Get the root of the document.
	*/

    public DocumentInfo getDocumentRoot() {
        return parent.getDocumentRoot();
    }

    /**
    * Return the character value of the node.
    * @return the string value of the node
    */

    public String getStringValue() {
		return content;
    }

    /**
    * Return the type of node.
    * @return Node.TEXT
    */

    public final short getNodeType() {
        return TEXT;
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException {
        out.writeContent(content);
    }

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws TransformerException {
        out.writeContent(content);
    }

    /**
    * Delete string content of this and all subsequent nodes. For use when deleting
    * an element in preview mode
    */

    public void truncateToStart() {
       //getCharacterBuffer().setLength(start);
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
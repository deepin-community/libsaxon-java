package com.icl.saxon.tree;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.om.NodeInfo;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;

/**
  * CommentImpl is an implementation of a Comment node
  * @author Michael H. Kay
  */


final class CommentImpl extends NodeImpl implements Comment {

    String comment;

    public CommentImpl(String content) {
        this.comment = content;
    }

    /**
    * Get the name of this node, following the DOM rules
    * @return "#comment"
    */

    public final String getNodeName() {
        return "#comment";
    }

    public final String getStringValue() {
        return comment;
    }

    public final short getNodeType() {
        return NodeInfo.COMMENT;
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws TransformerException {
        out.writeComment(comment);
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

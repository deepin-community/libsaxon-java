package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.expr.XPathException;

import java.util.StringTokenizer;

/**
* An IDPattern is a pattern of the form id(literal)
*/

public final class IDPattern extends Pattern {

    private String id;                      // the id value supplied
    private boolean containsSpaces;

    public IDPattern(String idvalue) {
        id = idvalue;
        containsSpaces =
                (id.indexOf(' ') >= 0 ||
                id.indexOf(0x09) >= 0 ||
                id.indexOf(0x0a) >= 0 ||
                id.indexOf(0x0c) >= 0);
    }

    /**
    * Determine whether this Pattern matches the given Node
    * @param e The NodeInfo representing the Element or other node to be tested against the Pattern
    * @return true if the node matches the Pattern, false otherwise
    */

    public boolean matches(NodeInfo e, Context c) throws XPathException {
        if (e.getNodeType() != NodeInfo.ELEMENT) return false;
        DocumentInfo doc = e.getDocumentRoot();
        if (!containsSpaces) {
            NodeInfo element = doc.selectID(id);
            if (element==null) return false;
            return (element.isSameNodeInfo(e));
        } else {
            StringTokenizer tokenizer = new StringTokenizer(id);
            while (tokenizer.hasMoreElements()) {
                String idv = (String)tokenizer.nextElement();
                NodeInfo element = doc.selectID(idv);
                if (element != null && e.isSameNodeInfo(element)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
    * Determine the type of nodes to which this pattern applies.
    * @return NodeInfo.ELEMENT
    */

    public short getNodeType() {
        return NodeInfo.ELEMENT;
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

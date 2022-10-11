package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;

import javax.xml.transform.*;
import java.util.*;

/**
* Handler for xsl:copy elements in stylesheet.<BR>
*/

public class XSLCopy extends StyleElement {

    private String use;                     // value of use-attribute-sets attribute

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.USE_ATTRIBUTE_SETS) {
        		use = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        if (use!=null) {
            findAttributeSets(use);         // find any referenced attribute sets
        }
    }

    public void process(Context context) throws TransformerException
    {
        NodeInfo source = context.getCurrentNodeInfo();
        Outputter out = context.getOutputter();

        // Processing depends on the node type.

        switch(source.getNodeType()) {

        case NodeInfo.ELEMENT:
            out.writeStartTag(source.getNameCode());

            source.outputNamespaceNodes(out, true);

            processAttributeSets(context);
            processChildren(context);
            out.writeEndTag(source.getNameCode());
            break;

        case NodeInfo.ATTRIBUTE:
        	int nameCode = source.getNameCode();
        	if (((nameCode>>20)&0xff) != 0) {	// prefix!=""
        		nameCode = out.checkAttributePrefix(nameCode);
        	}
            out.writeAttribute(nameCode, source.getStringValue());
            break;

        case NodeInfo.TEXT:
            out.writeContent(source.getStringValue());
            break;

        case NodeInfo.PI:
            out.writePI(source.getDisplayName(), source.getStringValue());
            break;

        case NodeInfo.COMMENT:
            out.writeComment(source.getStringValue());
            break;

        case NodeInfo.NAMESPACE:
            source.copy(out);
            break;

        case NodeInfo.ROOT:
            processChildren(context);
            break;

        default:
            throw new IllegalArgumentException("Unknown node type " + source.getNodeType());

        }
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

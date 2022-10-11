package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import javax.xml.transform.*;

import org.w3c.dom.Node;

/**
* Handler for xsl:text elements in stylesheet. <BR>
*/

public class XSLText extends StyleElement {

    private boolean disable = false;
    private String value = null;


    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

        String disableAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.DISABLE_OUTPUT_ESCAPING) {
        		disableAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (disableAtt != null) {
            if (disableAtt.equals("yes")) {
                disable = true;
            } else if (disableAtt.equals("no")) {
                disable = false;
            } else {
                compileError("disable-output-escaping attribute must be either yes or no");
            }
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        NodeImpl node = (NodeImpl)getFirstChild();
        if (node==null) {
            value = "";
        } else {
            value = node.getStringValue();
            while (node!=null) {
                if (node.getNodeType()==NodeInfo.ELEMENT) {
                    compileError("xsl:text must not have any child elements");
                }
                node = (NodeImpl)node.getNextSibling();
            }
        }
    }

    public void process(Context context) throws TransformerException {
        if (!value.equals("")) {
            Outputter out = context.getOutputter();
            if (disable) {
                out.setEscaping(false);
                out.writeContent(value);
                out.setEscaping(true);
            } else {
                out.writeContent(value);
            }
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

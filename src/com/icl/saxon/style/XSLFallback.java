package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
* xsl:fallback element in stylesheet.<BR>
*/

public class XSLFallback extends StyleElement {

    boolean active;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this element does any processing after instantiating any children.
    * This implementation says it doesn't, thus enabling tail recursion.
    */

    public boolean doesPostProcessing() {
        return false;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {
		AttributeCollection atts = getAttributeList();
		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
        	checkUnknownAttribute(nc);
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process(Context context) throws TransformerException {
        if (((StyleElement)getParentNode()).validationError!=null) {
        	processChildren(context);
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

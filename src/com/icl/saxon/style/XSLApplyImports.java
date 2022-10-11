package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.ParameterSet;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
* An xsl:apply-imports element in the stylesheet
*/

public class XSLApplyImports extends StyleElement {


    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		AttributeCollection atts = getAttributeList();

		String selectAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
        	checkUnknownAttribute(nc);
        }

    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process(Context context) throws TransformerException {

        // handle parameters if any

        ParameterSet params = null;

        if (hasChildNodes()) {
            NodeImpl child = (NodeImpl)getFirstChild();
            params = new ParameterSet();
            while (child != null) {
                if (child instanceof XSLWithParam) {    // currently always true
                    XSLWithParam param = (XSLWithParam)child;
                    params.put(param.getVariableFingerprint(), param.getParamValue(context));
                }
                child = (NodeImpl)child.getNextSibling();
            }
        }

        XSLTemplate currentTemplate = context.getCurrentTemplate();
        if (currentTemplate==null) {
            throw new TransformerException("There is no current template");
        }

        int min = currentTemplate.getMinImportPrecedence();
        int max = currentTemplate.getPrecedence()-1;
        context.getController().applyImports(   context,
                                                context.getMode(),
                                                min,
                                                max,
                                                params);
        context.setCurrentTemplate(currentTemplate);
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

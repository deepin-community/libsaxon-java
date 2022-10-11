package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;

import javax.xml.transform.*;


/**
* Handler for saxon:return or exslt:result elements in stylesheet.<BR>
* The element has optional attribute select
*/

public class SAXONReturn extends XSLGeneralVariable {

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

	public int getVariableFingerprint() {
		return -1;
	}

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		String selectAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        }

    }


    /**
    * Validate
    */

    public void validate() throws TransformerConfigurationException {

        // check it's within a function body

        NodeInfo anc = (NodeInfo)getParentNode();
        while (anc!=null) {
            if (anc instanceof SAXONFunction) break;
            if (anc instanceof XSLGeneralVariable ) {
                compileError(getDisplayName() + " must not be used within a variable definition");
            };
            anc = (NodeInfo)anc.getParent();
        }

        if (anc==null) {
            compileError(getDisplayName() + " must only be used within a function definition");
        }


        // check there is no following instruction

        NodeImpl next = (NodeImpl)getNextSibling();
        if (next!=null && !(next instanceof XSLFallback)) {
            compileError(getDisplayName() + " must be the last instruction in its template body");
        }

        if (select==null) {
            if (!hasChildNodes()) {
                select = new StringValue("");
            }
        }
    }

    /**
    * Process the return/result instruction
    */

    public void process(Context context) throws TransformerException
    {
        Value value = getSelectValue(context);
        context.setReturnValue(value);
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

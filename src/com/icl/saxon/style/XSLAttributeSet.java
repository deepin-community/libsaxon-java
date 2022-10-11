package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamespaceException;

import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import org.w3c.dom.Node;
import javax.xml.transform.*;


/**
* An xsl:attribute-set element in the stylesheet.<BR>
*/

public class XSLAttributeSet extends StyleElement {

    int fingerprint;  // the name of this attribute set, as a Name object
    String use;     // the value of the use-attribute-sets attribute, as supplied
    Procedure procedure = new Procedure();   // needed if there are variables

    public int getAttributeSetFingerprint() {
        return fingerprint;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		String name = null;
		use = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		name = atts.getValue(a);
        	} else if (f==sn.USE_ATTRIBUTE_SETS) {
        		use = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (name==null) {
            reportAbsence("name");
            return;
        }

        if (!Name.isQName(name)) {
            compileError("Attribute set name must be a valid QName");
        }

        try {
            fingerprint = makeNameCode(name, false) & 0xfffff;
        } catch (NamespaceException err) {
            compileError(err.getMessage());
        }

    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();

        Node child = getFirstChild();
        while (child!=null) {
            if (!(child instanceof XSLAttribute)) {
                compileError("Only xsl:attribute is allowed within xsl:attribute-set");
            }
            child = child.getNextSibling();
        }

        if (use!=null) {
            findAttributeSets(use);    // identify any attribute sets that this one refers to
        }
    }

    /**
    * Get associated Procedure (for details of stack frame)
    */

    public Procedure getProcedure() {
        return procedure;
    }

    public void preprocess() throws TransformerConfigurationException {
        getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
    }

    public void process(Context context) throws TransformerException {

        // do nothing until the attribute set is expanded
    }

    public void expand(Context context) throws TransformerException {
        processAttributeSets(context);
        if (procedure.getNumberOfVariables()==0) {
            processChildren(context);
        } else {
            Bindery bindery = context.getController().getBindery();
            bindery.openStackFrame(null);
            processChildren(context);
            bindery.closeStackFrame();
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

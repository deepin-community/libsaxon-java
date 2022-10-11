package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import javax.xml.transform.*;

import java.io.*;
import java.util.*;

/**
* Handler for saxon:preview elements in stylesheet. <BR>
* Attributes: <br>
* mode identifies the mode in which preview templates will be called. <br>
* elements is a space-separated list of element names which are eligible for preview processing.
*/

public class SAXONPreview extends StyleElement {

    int previewModeNameCode = -1;
    String elements = null;


    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.MODE) {
        		String previewMode = atts.getValue(a);
        		try {
        		    previewModeNameCode = makeNameCode(previewMode, false);
        		} catch (NamespaceException err) {
        		    compileError(err.getMessage());
        		}
        	} else if (f==sn.ELEMENTS) {
        		elements = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (previewModeNameCode==-1) {
            reportAbsence("mode");
        }
        if (elements==null) {
            reportAbsence("elements");
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException
    {
        XSLStyleSheet sheet = getPrincipalStyleSheet();
        PreviewManager pm = sheet.getPreviewManager();
        if (pm==null) {
            pm = new PreviewManager();
            sheet.setPreviewManager(pm);
        }
        pm.setPreviewMode(previewModeNameCode);

        StringTokenizer st = new StringTokenizer(elements);
        while (st.hasMoreTokens()) {
            String elementName = st.nextToken();
            try {
                pm.setPreviewElement(makeNameCode(elementName, true) & 0xfffff);
            } catch (NamespaceException err) {
                compileError(err.getMessage());
            }
        }
    }

    public void process(Context context) throws TransformerException {}

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

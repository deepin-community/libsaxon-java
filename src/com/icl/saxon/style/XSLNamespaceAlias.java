package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import javax.xml.transform.*;

import com.icl.saxon.om.NamespaceException;
import java.util.*;

/**
* An xsl:namespace-alias element in the stylesheet.<BR>
*/

public class XSLNamespaceAlias extends StyleElement {

    private short stylesheetURICode;
    private short resultURICode;

    public void prepareAttributes() throws TransformerConfigurationException {

	    String stylesheetPrefix=null;
	    String resultPrefix=null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.STYLESHEET_PREFIX) {
        		stylesheetPrefix = atts.getValue(a);
        	} else if (f==sn.RESULT_PREFIX) {
        		resultPrefix = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        if (stylesheetPrefix==null) {
            reportAbsence("stylesheet-prefix");
            return;
        }
        if (stylesheetPrefix.equals("#default")) {
            stylesheetPrefix="";
        }
        if (resultPrefix==null) {
            reportAbsence("result-prefix");
            return;
        }
        if (resultPrefix.equals("#default")) {
            resultPrefix="";
        }
        try {
            stylesheetURICode = getURICodeForPrefix(stylesheetPrefix);
            resultURICode = getURICodeForPrefix(resultPrefix);
        } catch (NamespaceException err) {
            compileError(err.getMessage());
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException {}

    public void process(Context c) {}

    public short getStylesheetURICode() {
        return stylesheetURICode;
    }

    public short getResultURICode() {
        return resultURICode;
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

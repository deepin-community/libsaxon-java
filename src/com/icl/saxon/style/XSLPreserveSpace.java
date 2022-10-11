package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.*;
import com.icl.saxon.om.*;
import javax.xml.transform.*;
import java.util.*;

/**
* An xsl:preserve-space or xsl:strip-space elements in stylesheet.<BR>
*/

public class XSLPreserveSpace extends StyleElement {

    private String elements;

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.ELEMENTS) {
        		elements = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        if (elements==null) {
            reportAbsence("elements");
            elements="*";   // for error recovery
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException
    {
        Boolean preserve = new Boolean(getFingerprint()==getStandardNames().XSL_PRESERVE_SPACE);
        Mode stripperRules = getPrincipalStyleSheet().getStripperRules();

        // elements is a space-separated list of element names

        StringTokenizer st = new StringTokenizer(elements);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            try {
                if (s.equals("*")) {
                    stripperRules.addRule(
                                AnyNodeTest.getInstance(),
                                preserve,
                                getPrecedence(),
                                -0.5);

                } else if (s.endsWith(":*")) {
                    String prefix = s.substring(0, s.length()-2);
                    stripperRules.addRule(
                    			new NamespaceTest(
                    			        getNamePool(),
    	                				NodeInfo.ELEMENT,
    	                				getURICodeForPrefix(prefix)),
                    			preserve,
                    			getPrecedence(),
                    			-0.25);
                } else {
                    if (!Name.isQName(s)) {
                        compileError("Element name " + s + " is not a valid QName");
                    }
                	stripperRules.addRule(
                				new NameTest(
                						NodeInfo.ELEMENT,
                						makeNameCode(s, false)),
                				preserve,
                				getPrecedence(),
                				0);
                }
            } catch (NamespaceException err) {
                compileError(err.getMessage());
            }
        }
    }

    public void process(Context c) {}

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

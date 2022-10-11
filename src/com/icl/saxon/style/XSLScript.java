package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.Namespace;
import javax.xml.transform.*;

import com.icl.saxon.om.NamespaceException;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
* An xsl:script element in the stylesheet.<BR>
*/

public class XSLScript extends StyleElement {

    private Class javaClass = null;
    private String implementsURI = null;
    private String language = null;

    public void prepareAttributes() throws TransformerConfigurationException {

	    String languageAtt = null;
	    String implementsAtt = null;
	    String srcAtt = null;
	    String archiveAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.LANGUAGE) {
        		languageAtt = atts.getValue(a);
        	} else if (f==sn.IMPLEMENTS_PREFIX) {
        		implementsAtt = atts.getValue(a);
        	} else if (f==sn.SRC) {
        		srcAtt = atts.getValue(a);
        	} else if (f==sn.ARCHIVE) {
        		archiveAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        if (implementsAtt==null) {
            reportAbsence("implements-prefix");
            return;
        } else {
            try {
                short uriCode = getURICodeForPrefix(implementsAtt);
                implementsURI = getNamePool().getURIFromURICode(uriCode);
            } catch (NamespaceException err) {
                compileError(err.getMessage());
            }
        }

        if (languageAtt==null) {
            reportAbsence("language");
            return;
        } else {
            language = languageAtt;
        }

        // TODO: validate that language is valid

        if (language.equals("java")) {
            if (srcAtt==null) {
                compileError("For java, the src attribute is mandatory");
                return;
            }
            if (!srcAtt.startsWith("java:")) {
                compileError("The src attribute must be a URI of the form java:full.class.Name");
                return;
            }
            String className = srcAtt.substring(5);

            if (archiveAtt==null) {
                try {
                    javaClass = Loader.getClass(className);
                } catch (TransformerException err) {
                    compileError(err);
                    return;
                }
            } else {
                URL base;
                try {
                    base = new URL(getBaseURI());
                } catch (MalformedURLException err) {
                    compileError("Invalid base URI " + getBaseURI());
                    return;
                }
                StringTokenizer st = new StringTokenizer(archiveAtt);
                int count = 0;
                while (st.hasMoreTokens()) {
                    count++;
                    st.nextToken();
                }
                URL[] urls = new URL[count];
                count = 0;
                st = new StringTokenizer(archiveAtt);
                while (st.hasMoreTokens()) {
                    String s = (String)st.nextToken();
                    try {
                        urls[count++] = new URL(base, s);
                    } catch (MalformedURLException err) {
                        compileError("Invalid URL " + s);
                        return;
                    }
                }
                try {
                    javaClass = new URLClassLoader(urls).loadClass(className);
                } catch (java.lang.ClassNotFoundException err) {
                    compileError("Cannot find class " + className + " in the specified archive"
                                    + (count>1 ? "s" : ""));
                } catch (java.lang.NoClassDefFoundError err2) {
                    compileError("Cannot use the archive attribute with this Java VM");
                }
            }
        }
    }

    public void validate() throws TransformerConfigurationException {
        if (getURI().equals(Namespace.XSLT)) {
            // saxon:script is OK, but xsl:script requires version="1.1"
            if (!forwardsCompatibleModeIsEnabled()) {
                compileError("To use xsl:script, set xsl:stylesheet version='1.1'");
            }
        }
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException {}

    public void process(Context c) {}

    /**
    * Get the Java class, if this XSLScript element matches the specified URI.
    * Otherwise return null
    */

    public Class getJavaClass(String uri) throws TransformerException {
        if (language==null) {
            // allow for forwards references
            prepareAttributes();
        }
        if (language.equals("java") && implementsURI.equals(uri)) {
            return javaClass;
        } else {
            return null;
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

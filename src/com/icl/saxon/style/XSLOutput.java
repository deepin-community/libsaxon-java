package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.output.SaxonOutputKeys;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Properties;

/**
* An xsl:output element in the stylesheet.
*/

public class XSLOutput extends XSLGeneralOutput {

    public void prepareAttributes() throws TransformerConfigurationException {
        super.prepareAttributes();
        if (href!=null) {
            compileError("The href attribute is not allowed on this element");
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
        checkEmpty();

        // AVTs are not allowed with version="1.0"

        if (!forwardsCompatibleModeIsEnabled()) {
		    AttributeCollection atts = getAttributeList();
            for (int a=0; a<atts.getLength(); a++) {
                if (atts.getValue(a).indexOf('{') >= 0) {
                    compileError("To use attribute value templates in xsl:output, set xsl:stylesheet version='1.1'");
                    break;
                }
            }
        }
    }

    public void process(Context context) throws TransformerException {}

    /**
    * Gather the unvalidated and unexpanded values of the properties.
    */

    protected Properties gatherOutputProperties(Properties details) {
		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();
        if (method != null) {
            details.put(OutputKeys.METHOD,
                                atts.getValueByFingerprint(sn.METHOD));
        }

        if (version != null) {
            details.put(OutputKeys.VERSION,
                                atts.getValueByFingerprint(sn.VERSION));
        }

        if (indent != null) {
            details.put(OutputKeys.INDENT,
                                atts.getValueByFingerprint(sn.INDENT));
        }

        if (indentSpaces != null) {
            details.put(SaxonOutputKeys.INDENT_SPACES,
                                atts.getValueByFingerprint(sn.SAXON_INDENT_SPACES));
        }

        if (encoding != null) {
            details.put(OutputKeys.ENCODING,
                                atts.getValueByFingerprint(sn.ENCODING));
        }

        if (mediaType != null) {
            details.put(OutputKeys.MEDIA_TYPE,
                                atts.getValueByFingerprint(sn.MEDIA_TYPE));
        }

        if (doctypeSystem != null) {
            details.put(OutputKeys.DOCTYPE_SYSTEM,
                                atts.getValueByFingerprint(sn.DOCTYPE_SYSTEM));
        }

        if (doctypePublic != null) {
            details.put(OutputKeys.DOCTYPE_PUBLIC,
                                atts.getValueByFingerprint(sn.DOCTYPE_PUBLIC));
        }

        if (omitDeclaration != null) {
            details.put(OutputKeys.OMIT_XML_DECLARATION,
                                atts.getValueByFingerprint(sn.OMIT_XML_DECLARATION));
        }

        if (standalone != null) {
            details.put(OutputKeys.STANDALONE,
                                atts.getValueByFingerprint(sn.STANDALONE));
        }

        if (cdataElements != null) {
            String existing = details.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS);
            String s = existing + " " + atts.getValueByFingerprint(sn.CDATA_SECTION_ELEMENTS);
            details.put(OutputKeys.CDATA_SECTION_ELEMENTS, s);
        }

        if (nextInChain != null) { //TODO

        }

        if (requireWellFormed != null) {
            details.put(SaxonOutputKeys.REQUIRE_WELL_FORMED,
                                atts.getValueByFingerprint(sn.SAXON_REQUIRE_WELL_FORMED));
        }

        return details;
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

package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.StandardURIResolver;
import com.icl.saxon.StylesheetStripper;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tree.ElementImpl;
import com.icl.saxon.tree.TreeBuilder;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;


/**
* Abstract class to represent xsl:include or xsl:import element in the stylesheet.<BR>
* The xsl:include and xsl:import elements have mandatory attribute href
*/

public abstract class XSLGeneralIncorporate extends StyleElement {

    String href;
    DocumentImpl includedDoc;

    /**
    * isImport() returns true if this is an xsl:import statement rather than an xsl:include
    */

    public abstract boolean isImport();

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.HREF) {
        		href = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (href==null) {
            reportAbsence("href");
        }
    }

    public void validate() throws TransformerConfigurationException {
        // The node will never be validated, because it replaces itself
        // by the contents of the included file.
        checkEmpty();
        checkTopLevel();
    }

    public XSLStyleSheet getIncludedStyleSheet(XSLStyleSheet importer, int precedence)
                 throws TransformerConfigurationException {

        if (href==null) {
            // error already reported
            return null;
        }

        checkEmpty();
        checkTopLevel();

        try {
            XSLStyleSheet thisSheet = (XSLStyleSheet)getParentNode();
            DocumentInfo thisDoc = getDocumentRoot();
            TransformerFactoryImpl factory =
                getPreparedStyleSheet().getTransformerFactory();
            Source source = factory.getURIResolver().resolve(href, getBaseURI());

            // if a user URI resolver returns null, try the standard one
            // (Note, the standard URI resolver never returns null)
            if (source==null) {
                source = (new StandardURIResolver (factory)).resolve(href, getBaseURI());
            }

            if (source instanceof NodeInfo) {
                if (source instanceof Node) {
                    source = new DOMSource((Node)source);
                } else {
                    throw new TransformerException("URIResolver must not return a " + source.getClass());
                }
            }
            SAXSource saxSource = factory.getSAXSource(source, true);

            // check for recursion

            XSLStyleSheet anc = thisSheet;
            while(anc!=null) {
                if (saxSource.getSystemId().equals(anc.getSystemId())) {
                    compileError("A stylesheet cannot " + getLocalName() + " itself");
                    return null;
                }
                anc = anc.getImporter();
            }

            // load the included stylesheet

            NamePool pool = getDocumentRoot().getNamePool();
            StylesheetStripper styleStripper = new StylesheetStripper();
            styleStripper.setStylesheetRules(pool);

            TreeBuilder builder = new TreeBuilder();
			builder.setNamePool(pool);
            builder.setStripper(styleStripper);
            builder.setNodeFactory(new StyleNodeFactory(pool));
            builder.setDiscardCommentsAndPIs(true);
            builder.setLineNumbering(true);

            includedDoc = (DocumentImpl)builder.build(saxSource);

            // allow the included document to use "Literal Result Element as Stylesheet" syntax

            ElementImpl outermost = (ElementImpl)includedDoc.getDocumentElement();
            if (outermost instanceof LiteralResultElement) {
                includedDoc = ((LiteralResultElement)outermost).makeStyleSheet(getPreparedStyleSheet());
                outermost = (ElementImpl)includedDoc.getDocumentElement();
            }

            if (!(outermost instanceof XSLStyleSheet)) {
                compileError("Included document " + href + " is not a stylesheet");
                return null;
            }
            XSLStyleSheet incSheet = (XSLStyleSheet)outermost;

            if (incSheet.validationError != null ) {
                int circumstances = incSheet.reportingCircumstances;
                if (circumstances == REPORT_ALWAYS) {
                    compileError(incSheet.validationError);
                } else if (circumstances == REPORT_UNLESS_FORWARDS_COMPATIBLE
                              && !incSheet.forwardsCompatibleModeIsEnabled()) {
                    compileError(incSheet.validationError);
                }
            }

            incSheet.setPrecedence(precedence);
            incSheet.setImporter(importer);
            incSheet.spliceIncludes();          // resolve any nested includes;

            return incSheet;

        } catch (TransformerException err) {
            compileError(err);
            return null;
        }
    }

    public void process(Context context)
    {
        // no action. The node will never be processed, because it replaces itself
        // by the contents of the included file.
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

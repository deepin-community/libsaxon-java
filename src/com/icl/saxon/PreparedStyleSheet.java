package com.icl.saxon;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.style.LiteralResultElement;
import com.icl.saxon.style.StyleElement;
import com.icl.saxon.style.StyleNodeFactory;
import com.icl.saxon.style.XSLStyleSheet;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tree.TreeBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import java.util.Properties;

/**
  * This <B>PreparedStyleSheet</B> class represents a StyleSheet that has been
  * prepared for execution (or "compiled").
  */

public class PreparedStyleSheet implements Templates {

    private DocumentImpl styleDoc;
    private TransformerFactoryImpl factory;
    private NamePool namePool;
    private StyleNodeFactory nodeFactory;
    private int errorCount = 0;

    /**
    * Constructor: deliberately protected
    */

    protected PreparedStyleSheet(TransformerFactoryImpl factory) {
        this.factory = factory;
    }

    /**
    * Make a Transformer from this Templates object.
    */

    public Transformer newTransformer() {
        Controller c = new Controller(factory);
        c.setPreparedStyleSheet(this);
        return c;
    }

    /**
    * Get the TransformerFactory used to create this PreparedStyleSheet
    */

    public TransformerFactoryImpl getTransformerFactory() {
        return factory;
    }

	/**
	* Set the name pool to be used
	*/

	public void setNamePool(NamePool pool) {
		namePool = pool;
	}

	/**
	* Get the name pool in use
	*/

	public NamePool getNamePool() {
		return namePool;
	}

	/**
	* Get the StyleNodeFactory in use
	*/

	public StyleNodeFactory getStyleNodeFactory() {
		return nodeFactory;
	}

    /**
    * Prepare a stylesheet from an InputSource
    */

    protected void prepare(SAXSource styleSource) throws TransformerConfigurationException {

        if (namePool==null || namePool.isSealed()) {
        	namePool = NamePool.getDefaultNamePool();
        }

        nodeFactory = new StyleNodeFactory(namePool);
        //namePool.setStylesheetSignature(this);

        StylesheetStripper styleStripper = new StylesheetStripper();
        styleStripper.setStylesheetRules(namePool);

        TreeBuilder styleBuilder = new TreeBuilder();
        styleBuilder.setNamePool(namePool);
        styleBuilder.setErrorListener(factory.getErrorListener());
        styleBuilder.setStripper(styleStripper);
        styleBuilder.setSystemId(styleSource.getSystemId());
        styleBuilder.setNodeFactory(nodeFactory);
        styleBuilder.setDiscardCommentsAndPIs(true);
        styleBuilder.setLineNumbering(true);

        // build the stylesheet document

        DocumentImpl doc;
        try {
            doc = (DocumentImpl)styleBuilder.build(styleSource);
        } catch (TransformerException err) {
            Throwable cause = err.getException();
            if (cause != null) {
                if (cause instanceof SAXParseException) {
                    // details already reported, don't repeat them
                    throw new TransformerConfigurationException("Failed to parse stylesheet");
                } else if (cause instanceof TransformerConfigurationException) {
                    throw (TransformerConfigurationException)cause;
                } else {
                    throw new TransformerConfigurationException(cause);
                }
            }
            throw new TransformerConfigurationException(err);
        }

        if (doc.getDocumentElement()==null) {
            throw new TransformerConfigurationException("Stylesheet is empty or absent");
        }

        setStyleSheetDocument(doc);

        if (errorCount > 0) {
            throw new TransformerConfigurationException(
                            "Failed to compile stylesheet. " +
                            errorCount +
                            (errorCount==1 ? " error " : " errors ") +
                            "detected.");
        }

    }


    /**
    * Create a PreparedStyleSheet from a supplied DocumentInfo
    * Note: the document must have been built using the StyleNodeFactory
    */

    protected void setStyleSheetDocument(DocumentImpl doc)
    throws TransformerConfigurationException {

        styleDoc = doc;
        namePool = doc.getNamePool();
		//namePool.setStylesheetSignature(this);
		nodeFactory = new StyleNodeFactory(namePool);

        // If top-level node is a literal result element, stitch it into a skeleton stylesheet

        StyleElement topnode = (StyleElement)styleDoc.getDocumentElement();
        if (topnode instanceof LiteralResultElement) {
            styleDoc = ((LiteralResultElement)topnode).makeStyleSheet(this);
        }

        if (!(styleDoc.getDocumentElement() instanceof XSLStyleSheet)) {
            throw new TransformerConfigurationException(
                        "Top-level element of stylesheet is not xsl:stylesheet or xsl:transform or literal result element");
        }

        XSLStyleSheet top = (XSLStyleSheet)styleDoc.getDocumentElement();

        // Preprocess the stylesheet, performing validation and preparing template definitions

        top.setPreparedStyleSheet(this);
        top.preprocess();
    }

    /**
    * Get the root node of the principal stylesheet document
    */

    public DocumentImpl getStyleSheetDocument() {
        return styleDoc;
    }

    /**
    * Get the properties for xsl:output.  TRAX method. The object returned will
    * be a clone of the internal values, and thus it can be mutated
    * without mutating the Templates object, and then handed in to
    * the process method.
    * @return A OutputProperties object that may be mutated. Note that
    * if any attributes of xsl:output are written as attribute value templates,
    * the values returned will be unexpanded.
    *
    */

    public Properties getOutputProperties() {

        Properties defaults = new Properties();
        //defaults.put(OutputKeys.METHOD, "xml");
        // "xml" is wrong because the default is determined at run-time.
        defaults.put(OutputKeys.ENCODING, "utf-8");
        //defaults.put(OutputKeys.VERSION, "1.0"); - default depends on method
        defaults.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        //defaults.put(OutputKeys.STANDALONE, ""); - default is "absent"
        //defaults.put(OutputKeys.DOCTYPE_PUBLIC, ""); - default is "absent"
        //defaults.put(OutputKeys.DOCTYPE_SYSTEM, ""); - default is "absent"
        defaults.put(OutputKeys.CDATA_SECTION_ELEMENTS, "");
        //defaults.put(OutputKeys.INDENT, "no"); - default depends on method
        //defaults.put(OutputKeys.MEDIA_TYPE, "text/xml"); - default depends on method

        Properties details = new Properties(defaults);
        ((XSLStyleSheet)styleDoc.getDocumentElement()).gatherOutputProperties(details);
        return details;
    }

    /**
    * Report a compile time error. This calls the errorListener to output details
    * of the error, and increments an error count.
    */

    public void reportError(TransformerException err) throws TransformerException {
        errorCount++;
        factory.getErrorListener().error(err);
    }

    /**
    * Use the xsl:strip-space directives in this stylesheet to strip spaces from a
    * source document. The rest of the stylesheet is ignored.
    */

    public DocumentInfo stripWhitespace(Document doc) throws TransformerException {
        XSLStyleSheet top = (XSLStyleSheet)styleDoc.getDocumentElement();
        if (top.stripsWhitespace() || (!(doc instanceof DocumentInfo))) {
            Builder b = ((Controller)newTransformer()).makeBuilder();
            b.setNamePool(namePool);
            return b.build(factory.getSAXSource(new DOMSource(doc), false));
        } else {
            return (DocumentInfo)doc;
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

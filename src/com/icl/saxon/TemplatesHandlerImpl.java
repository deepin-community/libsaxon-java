package com.icl.saxon;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.tree.TreeBuilder;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.style.StyleNodeFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.TemplatesHandler;

import org.xml.sax.*;


/**
  * <b>TemplatesHandlerImpl</b> implements the javax.xml.transform.sax.TemplatesHandler
  * interface. It acts as a ContentHandler which receives a stream of
  * SAX events representing a stylesheet, and returns a Templates object that
  * represents the compiled form of this stylesheet.
  * @author Michael H. Kay
  */

public class TemplatesHandlerImpl extends ContentEmitter implements TemplatesHandler {

    TransformerFactoryImpl factory;
    TreeBuilder builder;
    Templates templates;
    String systemId;

    /**
    * Create a TemplatesHandlerImpl and initialise variables. The constructor is protected, because
    * the Filter should be created using newTemplatesHandler() in the SAXTransformerFactory
    * class
    */

    protected TemplatesHandlerImpl(TransformerFactoryImpl factory) {
        NamePool pool = NamePool.getDefaultNamePool();
        setNamePool(pool);
        this.factory = factory;
        builder = new TreeBuilder();
        builder.setNamePool(pool);
        StyleNodeFactory nodeFactory = new StyleNodeFactory(pool);
        //pool.setStylesheetSignature(this);

        StylesheetStripper styleStripper = new StylesheetStripper();
        styleStripper.setStylesheetRules(pool);

        builder = new TreeBuilder();
        builder.setNamePool(pool);
        builder.setStripper(styleStripper);
        builder.setNodeFactory(nodeFactory);
        builder.setDiscardCommentsAndPIs(true);
        builder.setLineNumbering(true);

        this.setEmitter(styleStripper);
        styleStripper.setUnderlyingEmitter(builder);

    }

    /**
    * Get the Templates object to used for a transformation
    */

    public Templates getTemplates() {
        if (templates==null) {
            DocumentImpl doc = (DocumentImpl)builder.getCurrentDocument();
            if (doc==null) {
                return null;
            }
            PreparedStyleSheet sheet = new PreparedStyleSheet(factory);
            try {
                sheet.setStyleSheetDocument(doc);
                templates = sheet;
            } catch (TransformerConfigurationException tce) {
                // TODO: don't know why we aren't allowed to just throw it!
                System.err.println(tce.getMessage());
                return null;
            }
        }

        return templates;
    }

    /**
    * Set the SystemId of the document
    */

    public void setSystemId(String url) {
        systemId = url;
        builder.setSystemId(url);
    }

    /**
    * Get the systemId of the document
    */

    public String getSystemId() {
        return systemId;
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
// Contributor(s): None
//

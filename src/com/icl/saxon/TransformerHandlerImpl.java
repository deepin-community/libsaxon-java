package com.icl.saxon;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TransformerHandler;


/**
  * <b>TransformerHandlerImpl</b> implements the javax.xml.transform.sax.TransformerHandler
  * interface. It acts as a ContentHandler and LexicalHandler which receives a stream of
  * SAX events representing an input document, and performs a transformation treating this
  * SAX stream as the source document of the transformation.
  * @author Michael H. Kay
  */

public class TransformerHandlerImpl extends ContentEmitter implements TransformerHandler {

    Controller controller;
    Builder builder;
    Result result;
    String systemId;

    /**
    * Create a TransformerHandlerImpl and initialise variables. The constructor is protected, because
    * the Filter should be created using newTransformerHandler() in the SAXTransformerFactory
    * class
    */

    protected TransformerHandlerImpl(Controller controller) {
        this.controller = controller;
        setNamePool(controller.getNamePool());
        builder = controller.makeBuilder();
        builder.setNamePool(controller.getNamePool());
        this.setEmitter(controller.makeStripper(builder));
    }

    /**
    * Get the Transformer used for this transformation
    */

    public Transformer getTransformer() {
        return controller;
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

    /**
    * Set the output destination of the transformation
    */

    public void setResult(Result result) {
        if (result==null) {
            throw new IllegalArgumentException("Result must not be null");
        }
        this.result = result;
    }

    /**
    * Get the output destination of the transformation
    */

    public Result getResult() {
        return result;
    }

    /**
    * Override the behaviour of endDocument() in ContentEmitter, so that it fires off
    * the transformation of the constructed document
    */

    public void endDocument() throws SAXException {
        // System.err.println("TransformerHandlerImpl " + this + " endDocument()");
        super.endDocument();
        DocumentInfo doc = builder.getCurrentDocument();
        if (doc==null) {
            throw new SAXException("No source document has been built");
        }
        controller.getDocumentPool().add(doc, null);
        try {
            controller.transformDocument(doc, result);
        } catch (TransformerException err) {
            //err.printStackTrace();
            throw new SAXException(err);
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
// Contributor(s): None
//

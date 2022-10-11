package com.icl.saxon.om;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.*;
import com.icl.saxon.ContentEmitter;
import com.icl.saxon.PreviewManager;
import com.icl.saxon.ExtendedInputSource;
import com.icl.saxon.output.Emitter;

import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.sax.SAXSource;

import java.util.*;
import java.io.*;
import java.net.URL;

/**
  * The abstract Builder class is responsible for taking a stream of SAX events
  * and constructing a Document tree. There is one concrete subclass for each
  * tree implementation.
  * @author Michael H. Kay
  */

public abstract class Builder extends Emitter
                    implements ErrorHandler, Locator, SourceLocator

{
    public final static int STANDARD_TREE = 0;
    public final static int TINY_TREE = 1;

    //protected static int nextSequenceNr = 0;
    protected int estimatedLength;                // Estimated size of document in bytes
                //TODO: currently not used
    protected Writer errorOutput = new PrintWriter(System.err);
                                                  // Destination for error messages
    protected Stripper stripper;                  // manages list of elements to be stripped
    protected PreviewManager previewManager = null;
    protected boolean discardComments;            // true if comments and PIs should be ignored

    protected DocumentInfo currentDocument;
    protected ErrorHandler errorHandler = this;   // SAX-compliant XML error handler
    protected ErrorListener errorListener = null; // JAXP-compliant XSLT error handler

    protected boolean failed = false;
    protected boolean started = false;
    protected boolean timing = false;

    protected boolean inDTD = false;
    protected boolean lineNumbering = false;
    protected int lineNumber = -1;
    protected int columnNumber = -1;

    private long startTime;

    protected Controller controller;

    public void setController(Controller c) {
        controller = c;
    }

    /**
    * create a Builder and initialise variables
    */

    public Builder() {}

    /////////////////////////////////////////////////////////////////////////
    // Methods setting and getting options for building the tree
    /////////////////////////////////////////////////////////////////////////

    /**
    * Set the root (document) node to use. This method is used to support
    * the JAXP facility to attach transformation output to a supplied Document
    * node. It must be called before startDocument(), and the type of document
    * node must be compatible with the type of Builder used.
    */

    public void setRootNode(DocumentInfo doc) {
        currentDocument = doc;
    }


    /**
    * Set timing option on or off
    */

    public void setTiming(boolean on) {
        timing = on;
    }

    /**
    * Get timing option
    */

    public boolean isTiming() {
        return timing;
    }

    /**
    * Set line numbering on or off
    */

    public void setLineNumbering(boolean onOrOff) {
        lineNumbering = onOrOff;
    }

    /**
    * Set the Stripper to use
    */

    public void setStripper(Stripper s) {
        stripper = s;
    }

    /**
    * Get the Stripper in use
    */

    public Stripper getStripper() {
        return stripper;
    }

    /**
    * Set the PreviewManager to use
    */

    public void setPreviewManager(PreviewManager pm) {
        previewManager = pm;
    }


    /**
    * Indicate whether comments and Processing Instructions should be discarded
    * @params discard true if comments and PIs are to be discarded, false if
    * they are to be added to the tree
    */

    public void setDiscardCommentsAndPIs(boolean discard) {
        discardComments = discard;
    }

    /**
    * Set the SAX error handler to use. If none is specified, SAXON supplies its own,
    * which writes error messages to the selected error output writer.
    * @param eh The error handler to use. It must conform to the interface
    * org.xml.sax.ErrorHandler
    */

    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    /**
    * Set the JAXP error listener to use, if no SAX errorHandler has been provided.
    * @param eh The error listener to use. It must conform to the interface
    * javax.xml.transform.ErrorListener
    */

    public void setErrorListener(ErrorListener eh) {
        this.errorListener = eh;
    }

    /**
    * Set output for error messages produced by the default error handler.<BR>
    * The default error handler does not throw an exception
    * for parse errors or input I/O errors, rather it returns a result code and
    * writes diagnostics to a user-specified output writer, which defaults to
    * System.err<BR>
    * This call has no effect if setErrorHandler() has been called to supply a
    * user-defined error handler
    * @param writer The Writer to use for error messages
    */

    public void setErrorOutput(Writer writer) {
        errorOutput = writer;
    }

    /**
    * Build the tree from an input source. After building the tree, it can
    * be walked as often as required using run(Document doc).
    * @param source The source to use. SAXSource is a SAX-defined class that
    * allows input from a URL, a byte stream, or a character stream. SAXON also
    * provides a subclass, ExtendedInputSource, that allows input directly from a File.
    * @return The DocumentInfo object that results from parsing the input.
    * @throws TransformerException if the input document could not be read or if it was not parsed
    * correctly.
    */

    public DocumentInfo build(SAXSource source) throws TransformerException
    {
        InputSource in = source.getInputSource();
        XMLReader parser = source.getXMLReader();

        // System.err.println("Builder " + this + " build using parser " + parser);
        if (timing) {
            System.err.println("Building tree for " + in.getSystemId() + " using " + getClass());
            startTime = (new Date()).getTime();
        }

        failed = true;  // until startDocument() called
        started = false;
        if (source.getSystemId() != null) {
            setSystemId(source.getSystemId());
        } else {
            setSystemId(in.getSystemId());
        }

        if (in instanceof ExtendedInputSource) {
            estimatedLength = ((ExtendedInputSource)in).getEstimatedLength();
            if (estimatedLength < 1) estimatedLength = 4096;
            if (estimatedLength > 1000000) estimatedLength = 1000000;
        } else {
            estimatedLength = 4096;
        }

        // parse the document

        if (parser==null) {
            try {
                parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                // This is a fallback, it shouldn't happen
            } catch (Exception err) {
                throw new TransformerException(err);
            }
        }

		ContentEmitter ce = new ContentEmitter();
		ce.setNamePool(namePool);
		parser.setContentHandler(ce);
		parser.setDTDHandler(ce);
        parser.setErrorHandler(errorHandler);

		if (!discardComments) {
			try {
            	parser.setProperty("http://xml.org/sax/properties/lexical-handler", ce);
            } catch (SAXNotSupportedException err) {    // this just means we won't see the comments
        	} catch (SAXNotRecognizedException err) {
        	}
        }

        if (stripper!=null) {
            ce.setEmitter(stripper);
            stripper.setUnderlyingEmitter(this);
        } else {
            ce.setEmitter(this);
        }

		try {
        	parser.setFeature("http://xml.org/sax/features/namespaces", true);
        	parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotSupportedException err) {    // SAX2 parsers MUST support this feature!
            throw new TransformerException(
                "The SAX2 parser does not recognize a required namespace feature");
    	} catch (SAXNotRecognizedException err) {
            throw new TransformerException(
                "The SAX2 parser does not support a required namespace feature");
    	}


        try {
            parser.parse(in);
        } catch (java.io.IOException err1) {
            throw new TransformerException("Failure reading " + in.getSystemId(), err1);
        } catch (SAXException err2) {
            Exception wrapped = err2.getException();
            if (wrapped != null && wrapped instanceof TransformerException) {
                throw (TransformerException)wrapped;
            }
            throw new TransformerException(err2);
        }


        if (!started) {
            // System.err.println("Builder " + this + " failed");
            throw new TransformerException("Source document not supplied");
        }

        if (failed) {
            // System.err.println("Builder " + this + " failed");
            throw new TransformerException("XML Parsing failed");
        }

        if (timing) {
            long endTime = (new Date()).getTime();
            System.err.println("Tree built in " + (endTime-startTime) + " milliseconds");
            startTime = endTime;
        }

        return currentDocument;
    }

    /**
    * Get the current document
    * @return the document that has been most recently built using this builder
    */

    public DocumentInfo getCurrentDocument() {
        return currentDocument;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implement the org.xml.sax.ErrorHandler interface.
    // The user can supply an alternative implementation using setErrorHandler()
    ////////////////////////////////////////////////////////////////////////////

    /**
    * Callback interface for SAX: not for application use
    */

    public void warning (SAXParseException e) {
        if (errorListener != null) {
            try {
                errorListener.warning(new TransformerException(e));
            } catch (Exception err) {}
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void error (SAXParseException e) throws SAXException {
        reportError(e, false);
        failed = true;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void fatalError (SAXParseException e) throws SAXException {
        reportError(e, true);
        failed = true;
        throw e;
    }

    /**
    * Common routine for errors and fatal errors
    */

    protected void reportError (SAXParseException e, boolean isFatal) {

        if (errorListener != null) {
            try {
                systemId = e.getSystemId();
                lineNumber = e.getLineNumber();
                columnNumber = e.getColumnNumber();
                TransformerException err =
                    new TransformerException("Error reported by XML parser", this, e);
                if (isFatal) {
                    errorListener.fatalError(err);
                } else {
                    errorListener.error(err);
                }
            } catch (Exception err) {}
        } else {

            try {
                String errcat = (isFatal ? "Fatal error" : "Error");
                errorOutput.write(errcat + " reported by XML parser: " + e.getMessage() + "\n");
                errorOutput.write("  URL:    " + e.getSystemId() + "\n");
                errorOutput.write("  Line:   " + e.getLineNumber() + "\n");
                errorOutput.write("  Column: " + e.getColumnNumber() + "\n");
                errorOutput.flush();
            } catch (Exception e2) {
                System.err.println(e);
                System.err.println(e2);
                e2.printStackTrace();
            };
        }
    }



    /**
    * Set the URI for an unparsed entity in the document.
    * Abstract method to be implemented in each subclass.
    */

    public abstract void setUnparsedEntity(String name, String uri);


    //////////////////////////////////////////////////////////////////////////////
    // Implement Locator interface (default implementation)
    //////////////////////////////////////////////////////////////////////////////

    //public void setSystemId(String uri) {
    //    baseURI = uri;
    //}

    //public String getSystemId() {
    //    return baseURI;
    //}

    public String getPublicId() {
        return null;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }


}   // end of outer class Builder

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

package com.icl.saxon.output;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.util.Properties;

/**
* A ContentHandlerProxy is an Emitter that filters data before passing it to an
* underlying SAX2 ContentHandler. Relevant events (notably comments) can also be
* fed to a LexicalHandler.

* Note that in general the output passed to an Emitter
* corresponds to an External General Parsed Entity. A SAX2 ContentHandler only expects
* to deal with well-formed XML documents, so we only pass it the contents of the first
* element encountered.
*/

public class ContentHandlerProxy extends Emitter implements Locator
{
    protected ContentHandler handler;
    protected LexicalHandler lexicalHandler;
    //protected AttributesImpl attributes = new AttributesImpl();
    protected Locator locator = this;
    private int depth = 0;
    protected boolean requireWellFormed = true;

    /**
    * Set the underlying content handler. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingContentHandler(ContentHandler handler) {
        this.handler = handler;
        if (handler instanceof LexicalHandler) {
            this.lexicalHandler = (LexicalHandler)handler;
        }
    }

    /**
     * Set the output properties
     */

    public void setOutputProperties(Properties props) {
        super.setOutputProperties(props);
        if ("no".equals(props.getProperty(SaxonOutputKeys.REQUIRE_WELL_FORMED))) {
            requireWellFormed = false;
        }
    }

    /**
    * Set the Lexical Handler to be used. If called, this must be called AFTER
    * setUnderlyingContentHandler()
    */

    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
    * Indicate whether the content handler can handle a stream of events that is merely
    * well-balanced, or whether it can only handle a well-formed sequence.
    */

    public void setRequireWellFormed(boolean wellFormed) {
        requireWellFormed = wellFormed;
    }

    /**
    * Set Document Locator
    */

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
    * Start of document
    */

    public void startDocument() throws TransformerException {
        // System.err.println(this + " startDocument(), handler = " + handler);
        if (handler==null) {
            throw new TransformerException("ContentHandlerProxy.startDocument(): no underlying handler provided");
        }
        try {
            handler.setDocumentLocator(locator);
            handler.startDocument();
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
        depth = 0;
    }

    /**
    * End of document
    */

    public void endDocument() throws TransformerException {
        try {
            handler.endDocument();
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }


    /**
    * Start of element
    */

    public void startElement(int nameCode, Attributes atts,
    						 int[] namespaces, int nscount) throws TransformerException {
        depth++;
        try {
            if (depth<=0 && requireWellFormed) {
                notifyNotWellFormed();
            }
            if (depth>0 || !requireWellFormed) {
            	for (int n=0; n<nscount; n++) {
            	    String prefix = namePool.getPrefixFromNamespaceCode(namespaces[n]);
            	    String uri = namePool.getURIFromNamespaceCode(namespaces[n]);
            		handler.startPrefixMapping(prefix, uri);
                }

                handler.startElement(
                    namePool.getURI(nameCode),
                    namePool.getLocalName(nameCode),
                    namePool.getDisplayName(nameCode),
                    atts);
            }
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * End of element
    */

    public void endElement(int nameCode) throws TransformerException {
        if (depth>0) {
            try {
                handler.endElement(
                    namePool.getURI(nameCode),
                    namePool.getLocalName(nameCode),
                    namePool.getDisplayName(nameCode));
            } catch (SAXException err) {
                throw new TransformerException(err);
            }
        }
        depth--;
        // if this was the outermost element, and well formed output is required
        // then no further elements will be processed
        if (requireWellFormed && depth<=0) {
            depth = Integer.MIN_VALUE;     // crude but effective
        }

    }

    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        try {
            if (depth<=0 && requireWellFormed) {
                boolean isWhite = new String(chars, start, len).trim().length()==0;
                if (isWhite) {
                    // ignore top-level white space
                } else {
                    notifyNotWellFormed();
                    if (!requireWellFormed) {
                        handler.characters(chars, start, len);
                    }
                }
            } else {
                handler.characters(chars, start, len);
            }
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * The following function notifies the content handler, by means of a processing
    * instruction, that the output is not a well-formed document. If the content
    * handler responds with an exception containing the message "continue"
    * (this is the only way it can get information back) then further events are
    * notified, otherwise they are suppressed.
    */

    protected void notifyNotWellFormed() throws SAXException {
        try {
            handler.processingInstruction(
                "saxon:warning", "Output suppressed because it is not well-formed");
        } catch (SAXException err) {
            if (err.getMessage().equals("continue")) {
                requireWellFormed = false;
            } else {
                throw err;
            }
        }
    }


    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data)
    throws TransformerException {
        try {
            handler.processingInstruction(target, data);
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Output a comment. Passes it on to the ContentHandler provided that the ContentHandler
    * is also a SAX2 LexicalHandler.
    */

    public void comment (char ch[], int start, int length)
    throws TransformerException {
        try {
            if (lexicalHandler != null) {
                lexicalHandler.comment(ch, start, length);
            }
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }


    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate. The action, as defined in JAXP 1.1, is
    * to notify the request to the Content Handler using a processing instruction.
    */

    public void setEscaping(boolean escaping) {
        try {
            handler.processingInstruction(
                (escaping ? Result.PI_ENABLE_OUTPUT_ESCAPING : PI_DISABLE_OUTPUT_ESCAPING),
                "");
        } catch (SAXException err) {}
    }

    ////////////////////////////////////////////////////////////////////
    // dummy implementation of Locator interface
    ////////////////////////////////////////////////////////////////////

    public String getPublicId() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
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
// Michael Kay  (michael.h.kay@ntlworld.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

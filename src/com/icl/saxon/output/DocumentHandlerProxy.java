package com.icl.saxon.output;
import com.icl.saxon.*;
//import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributeListImpl;
import java.io.*;
import java.util.*;
import javax.xml.transform.TransformerException;

/**
* A DocumentHandlerProxy is an Emitter that filters data before passing it to an
* underlying SAX DocumentHandler. Note that in general the output passed to an Emitter
* corresponds to an External General Parsed Entity. A SAX DocumentHandler only expects
* to deal with well-formed XML documents, so we only pass it the contents of the first
* element encountered.
*/

public class DocumentHandlerProxy extends Emitter
{
    protected DocumentHandler handler;
//    protected CharacterSet characterSet;
    protected AttributeListImpl outputAtts = new AttributeListImpl();
    private int depth = 0;

    /**
    * Set the underlying document handler. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingDocumentHandler(DocumentHandler handler) {
        this.handler = handler;
    }

    /**
    * Set Document Locator
    */

    public void setDocumentLocator(Locator locator) {
        if (handler!=null)
            handler.setDocumentLocator(locator);
    }

    /**
    * Start of document
    */

    public void startDocument() throws TransformerException {
        if (handler==null) {
            throw new TransformerException("DocumentHandlerProxy.startDocument(): no underlying handler provided");
        }
        try {
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

    public void startElement(int nameCode, Attributes attributes,
    						 int[] namespaces, int nscount) throws TransformerException {
        depth++;
        outputAtts.clear();
        for (int a=0; a<attributes.getLength(); a++) {
            outputAtts.addAttribute(
                attributes.getQName(a),
                attributes.getType(a),
                attributes.getValue(a) );
        }
        if (depth>0) { // only one top-level element allowed

            for (int n=0; n<nscount; n++) {
            	String prefix = namePool.getPrefixFromNamespaceCode(namespaces[n]);
        		String uri = namePool.getURIFromNamespaceCode(namespaces[n]);
                if (prefix.equals("")) {
                    outputAtts.addAttribute("xmlns", "NMTOKEN", uri);
                } else {
                    outputAtts.addAttribute("xmlns:" + prefix, "NMTOKEN", uri);
                }
            }
            try {
                handler.startElement(namePool.getDisplayName(nameCode), outputAtts);
            } catch (SAXException err) {
                throw new TransformerException(err);
            }
        }
    }

    /**
    * End of element
    */

    public void endElement(int nameCode) throws TransformerException {
        if (depth>0) {
            try {
                handler.endElement(namePool.getDisplayName(nameCode));
            } catch (SAXException err) {
                throw new TransformerException(err);
            }
        }
        depth--;
        // if this was the outermost element, no further elements will be processed
        if (depth<=0) {
            depth = Integer.MIN_VALUE;     // crude but effective
        }
    }

    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        if (depth>0) {
            try {
                handler.characters(chars, start, len);
            } catch (SAXException err) {
                throw new TransformerException(err);
            }
        }
    }

    /**
    * Ignorable Whitespace
    */

    //public void ignorableWhitespace(char[] chars, int start, int len) throws TransformerException {
    //    if (depth>0) {
    //        handler.ignorableWhitespace(chars, start, len);
    //    }
    //}

    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data) throws TransformerException {
        try {
            handler.processingInstruction(target, data);
        } catch (SAXException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Output a comment
    */

    public void comment (char ch[], int start, int length) {}


    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate.
    */

    //public void setEscaping(boolean escaping) throws TransformerException {}

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

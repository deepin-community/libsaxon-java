package com.icl.saxon.output;
import com.icl.saxon.om.NamePool;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import javax.xml.transform.TransformerException;
import java.io.Writer;
import java.util.Properties;

    /**
    * A ProxyEmitter is an Emitter that filters data before passing it to another
    * underlying Emitter.
    */

public abstract class ProxyEmitter extends Emitter
{
    protected Emitter baseEmitter;
    protected Properties outputProperties;

    /**
    * Set the underlying emitter. This call is mandatory before using the Emitter.
    */

    public void setUnderlyingEmitter(Emitter emitter) {
        baseEmitter = emitter;
        if (namePool!=null) {
        	baseEmitter.setNamePool(namePool);
        }
    }

	/**
	* Set the name pool to be used for all name codes
	*/

	public void setNamePool(NamePool pool) {
		super.setNamePool(pool);
		if (baseEmitter!=null) {
			baseEmitter.setNamePool(pool);
		}
	}

    /**
    * Set the result destination
    */

    public void setWriter (Writer writer) {
        this.writer = writer;
        if (baseEmitter!=null)
            baseEmitter.setWriter(writer);
    }

    /**
    * Start of document
    */

    public void startDocument() throws TransformerException {
        if (baseEmitter==null) {
            throw new TransformerException("ProxyEmitter.startDocument(): no underlying emitter provided");
        }
        baseEmitter.startDocument();
    }

    /**
    * End of document
    */

    public void endDocument() throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.endDocument();
        }
    }

    /**
    * Start of element
    */

    public void startElement(int nameCode, Attributes attributes,
    						 int[] namespaces, int nscount) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.startElement(nameCode, attributes, namespaces, nscount);
        }
    }

    /**
    * End of element
    */

    public void endElement(int nameCode) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.endElement(nameCode);
        }
    }

    /**
    * Character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.characters(chars, start, len);
        }
    }


    /**
    * Processing Instruction
    */

    public void processingInstruction(String target, String data) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.processingInstruction(target, data);
        }
    }

    /**
    * Output a comment
    */

    public void comment (char ch[], int start, int length) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.comment(ch, start, length);
        }
    }


    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is not called for other sections of output (e.g.
    * element names) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.setEscaping(escaping);
        }
    }

    /**
    * Set the output details.
    */

    public void setOutputProperties (Properties details) {
        outputProperties = details;
        if (baseEmitter!=null) {
            baseEmitter.setOutputProperties(details);
        }
    }

    /**
    * Set the URI for an unparsed entity in the document.
    */

    public void setUnparsedEntity(String name, String uri) throws TransformerException {
        if (baseEmitter!=null) {
            baseEmitter.setUnparsedEntity(name, uri);
        }
	}

	/**
	* Set the Document Locator
	*/

	public void setDocumentLocator(Locator locator) {
	    // System.err.println("ProxyEmitter.setDocumentLocator " + locator.getSystemId());
        if (baseEmitter!=null) {
            baseEmitter.setDocumentLocator(locator);
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

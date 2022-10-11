package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.NamePool;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import java.io.*;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.util.Properties;

/**
  * Emitter: This interface defines methods that must be implemented by
  * components that format SAXON output. There is one emitter for XML,
  * one for HTML, and so on. Additional methods are concerned with
  * setting options and providing a Writer.<p>
  *
  * The interface is deliberately designed to be as close as possible to the
  * standard SAX2 ContentHandler interface, however, it allows additional
  * information to be made available.
  */

public abstract class Emitter implements Result
{

    protected NamePool namePool;
    protected String systemId;
    protected Writer writer;
    protected OutputStream outputStream;
    protected Properties outputProperties;
    protected Locator locator;

	/**
	* Set the namePool in which all name codes can be found
	*/

	public void setNamePool(NamePool namePool) {
	    this.namePool = namePool;
	}

	/**
	* Get the namepool used for this document
	*/

	public NamePool getNamePool() {
		return namePool;
	}

	/**
	* Set the System ID
	*/

	public void setSystemId(String systemId) {
	    this.systemId = systemId;
	}

	/**
	* Get the System ID
	*/

	public String getSystemId() {
	    return systemId;
	}

    /**
    * Set the output properties
    */

    public void setOutputProperties(Properties props) {
        outputProperties = props;
    }

    /**
    * Get the output properties
    */

    public Properties getOutputProperties() {
        return outputProperties;
    }

    /**
    * Determine whether the Emitter wants a Writer for character output or
    * an OutputStream for binary output
    */

    public boolean usesWriter() {
        return true;
    }

    /**
    * Set the output destination as a character stream
    */

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
    * Get the output writer
    */

    public Writer getWriter() {
        return writer;
    }

    /**
    * Set the output destination as a byte stream
    */

    public void setOutputStream(OutputStream stream) {
        this.outputStream = stream;
    }

    /**
    * Get the output stream
    */

    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
    * Notify document start
    */

    public abstract void startDocument() throws TransformerException;

    /**
    * Notify document end
    */

    public abstract void endDocument() throws TransformerException;

    /**
    * Output an element start tag.
    * @params name The Name Code identifying the name of the Element within the name pool.
    * @params attributes The attributes (excluding namespace declarations) associated with
    * this element.
    * @param namespaces Array of namespace codes identifying the namespace prefix/uri
    * pairs associated with this element
    * @param nscount Number of significant entries within namespaces array
    */

    public abstract void startElement(int nameCode, Attributes attributes,
    						 int[] namespaces, int nscount) throws TransformerException;

    /**
    * Output an element end tag
    * @params name code The name code identifying the element.
    * Use the namePool.getDisplayName() method
    * to obtain the tag to display in XML output.
    */

    public abstract void endElement(int nameCode) throws TransformerException;

    /**
    * Output character data
    */

    public abstract void characters(char[] chars, int start, int len) throws TransformerException;

    /**
    * Output a processing instruction
    */

    public abstract void processingInstruction(String name, String data) throws TransformerException;

    /**
    * Output a comment. <br>
    * (The method signature is borrowed from the SAX2 LexicalHandler interface)
    */

    public abstract void comment (char[] chars, int start, int length) throws TransformerException;

    /**
    * Switch escaping on or off. This is called when the XSLT disable-output-escaping attribute
    * is used to switch escaping on or off. It is also called at the start and end of a CDATA section
    * It is not called for other sections of output (e.g. comments) where escaping is inappropriate.
    */

    public void setEscaping(boolean escaping) throws TransformerException {}

    /**
    * Set locator, to identify position in the document. Used only when supplying
    * input from a parser.
    */

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
    * Set unparsed entity URI Used only when supplying
    * input from a parser.
    */

    public void setUnparsedEntity(String name, String uri) throws TransformerException {}

    /**
    * load a named output emitter or document handler and check it is OK.
    */

    public static Emitter makeEmitter (String className) throws TransformerException
    {
        Object handler = Loader.getInstance(className);

        if (handler instanceof Emitter) {
            return (Emitter)handler;
        } else if (handler instanceof DocumentHandler) {
            DocumentHandlerProxy emitter = new DocumentHandlerProxy();
            emitter.setUnderlyingDocumentHandler((DocumentHandler)handler);
            return emitter;
        } else if (handler instanceof ContentHandler) {
            ContentHandlerProxy emitter = new ContentHandlerProxy();
            emitter.setUnderlyingContentHandler((ContentHandler)handler);
            return emitter;
        } else {
            throw new TransformerException("Failed to load emitter " + className +
                        ": it is not a SAX DocumentHandler or SAX2 ContentHandler");
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

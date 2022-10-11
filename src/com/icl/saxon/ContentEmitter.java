package com.icl.saxon;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Name;
import com.icl.saxon.output.Emitter;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.TransformerException;

import java.net.URL;

/**
  * ContentEmitter is a glue class that provides a standard SAX ContentHandler
  * interface to a Saxon Emitter. To achieve this it needs to map names supplied
  * as strings to numeric name codes, for which purpose it needs access to a name
  * pool. The class also performs the function of assembling adjacent text nodes.
  * @author Michael H. Kay 
  */

public class ContentEmitter implements ContentHandler, LexicalHandler, DTDHandler
{
    private NamePool pool;
    private Emitter emitter;
    private boolean inDTD = false;	// true while processing the DTD
    private Locator locator;

    // buffer for accumulating character data, until the next markup event is received

    private char[] buffer = new char[4096];
    private int used = 0;

    // array for accumulating namespace information

    private int[] namespaces = new int[50];
    private int namespacesUsed = 0;

    /**
    * create a ContentEmitter and initialise variables
    */

    public ContentEmitter() {
    }

	public void setEmitter(Emitter e) {
		emitter = e;
	}

	public void setNamePool(NamePool namePool) {
		pool = namePool;
	}

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws SAXException {
        // System.err.println("ContentEmitter#startDocument");
        try {
            used = 0;
            namespacesUsed = 0;
            emitter.setDocumentLocator(locator);
            emitter.startDocument();
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endDocument () throws SAXException {
        try {
            flush();
            emitter.endDocument();
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void setDocumentLocator (Locator locator) {
    	this.locator = locator;
        //emitter.setDocumentLocator(locator);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startPrefixMapping(String prefix, String uri) /*throws SAXException*/ {
    	if (namespacesUsed >= namespaces.length) {
    		int[] n2 = new int[namespacesUsed * 2];
    		System.arraycopy(namespaces, 0, n2, 0, namespacesUsed);
    		namespaces = n2;
    	}
    	namespaces[namespacesUsed++] = pool.allocateNamespaceCode(prefix, uri);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endPrefixMapping(String prefix) {}

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (String uri, String localname, String rawname, Attributes atts)
    throws SAXException
    {
        // System.err.println("ContentEmitter#startElement " + rawname);
        try {
            flush();
    		int nameCode = getNameCode(uri, localname, rawname);
            emitter.startElement(nameCode, atts, namespaces, namespacesUsed);
            namespacesUsed = 0;
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    private int getNameCode(String uri, String localname, String rawname) {
        String prefix = Name.getPrefix(rawname);
        return pool.allocate(prefix, uri, localname);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (String uri, String localname, String rawname) throws SAXException {
        try {
            flush();
            // TODO: it would be more efficient to maintain a stack of nameCodes
            String prefix = Name.getPrefix(rawname);
            int nameCode = pool.allocate(prefix, uri, localname);
            emitter.endElement(nameCode);
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int length) {

        // need to concatenate chunks of text before we can decide whether a node is all-white

        while (used + length > buffer.length) {
            char[] newbuffer = new char[buffer.length*2];
            System.arraycopy(buffer, 0, newbuffer, 0, used);
            buffer = newbuffer;
        }
        System.arraycopy(ch, start, buffer, used, length);
        used += length;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void ignorableWhitespace (char ch[], int start, int length) {
        characters(ch, start, length);
    }

    /**
    * Callback interface for SAX: not for application use<BR>
    */

    public void processingInstruction (String name, String remainder) throws SAXException
    {
        try {
            flush();
            if (!inDTD) {
                if (name==null) {
                	// trick used by some SAX1 parsers to notify a comment
                	comment(remainder.toCharArray(), 0, remainder.length());
                } else {
                    // some parsers allow through PI names containing colons
                    if (!Name.isNCName(name)) {
                        throw new SAXException("Invalid processing instruction name (" + name + ")");
                    }
                	emitter.processingInstruction(name, remainder);
                }
            }
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Callback interface for SAX (part of LexicalHandler interface): not for application use
    */

    public void comment (char ch[], int start, int length) throws SAXException {
        try {
            flush();
            if (!inDTD) {
            	emitter.comment(ch, start, length);
            }
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Flush buffer for accumulated character data, suppressing white space if appropriate
    */

    private void flush() throws TransformerException {
        if (used > 0) {
            emitter.characters(buffer, 0, used);
            used = 0;
        }
    }

    public void skippedEntity(String name) {}

    // No-op methods to satisfy lexical handler interface

	/**
	* Register the start of the DTD. Comments in the DTD are skipped because they
	* are not part of the XPath data model
	*/

    public void startDTD (String name, String publicId, String systemId) {
		inDTD = true;
    }

	/**
	* Register the end of the DTD. Comments in the DTD are skipped because they
	* are not part of the XPath data model
	*/

    public void endDTD ()  {
		inDTD = false;
    }

    public void startEntity (String name) {};

    public void endEntity (String name)	{};

    public void startCDATA () {};

    public void endCDATA ()	{};

    //////////////////////////////////////////////////////////////////////////////
    // Implement DTDHandler interface
    //////////////////////////////////////////////////////////////////////////////


    public void notationDecl(       String name,
                                    String publicId,
                                    String systemId)
    {}


    public void unparsedEntityDecl( String name,
                                    String publicId,
                                    String systemId,
                                    String notationName) throws SAXException
    {
        //System.err.println("Unparsed entity " + name + "=" + systemId);

        // Some SAX parsers report the systemId as written. We need to turn it into
        // an absolute URL.

        String uri = systemId;
        if (locator!=null) {
            try {
                String baseURI = locator.getSystemId();
                URL absoluteURI = new URL(new URL(baseURI), systemId);
                uri = absoluteURI.toString();
            } catch (Exception err) {}
        }
        try {
            emitter.setUnparsedEntity(name, uri);
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }


}   // end of class ContentEmitter

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

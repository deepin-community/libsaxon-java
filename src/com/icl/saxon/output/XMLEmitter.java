package com.icl.saxon.output;
import com.icl.saxon.charcode.CharacterSet;
import com.icl.saxon.charcode.CharacterSetFactory;
import com.icl.saxon.charcode.UnicodeCharacterSet;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.util.Properties;

/**
  * XMLEmitter is an Emitter that generates XML output
  * to a specified destination.
  */

public class XMLEmitter extends Emitter
{
    protected CharacterSet characterSet = null;

    protected boolean empty = true;
    protected boolean escaping = true;
    protected boolean openStartTag = false;
    protected boolean declarationIsWritten = false;

    protected boolean preferHex = false;

	// a little cache...
	protected int lastNameCode = -1;
	protected String lastDisplayName;
	protected String lastPrefix;
	protected String lastURI;

    static boolean[] specialInText;         // lookup table for special characters in text
    static boolean[] specialInAtt;          // lookup table for special characters in attributes
        // create look-up table for ASCII characters that need special treatment

    static {
        specialInText = new boolean[128];
        for (int i=0; i<=127; i++) specialInText[i] = false;
        specialInText['\r'] = true;
        specialInText['<'] = true;
        specialInText['>'] = true;
        specialInText['&'] = true;

        specialInAtt = new boolean[128];
        for (int i=0; i<=127; i++) specialInAtt[i] = false;
        specialInAtt['\r'] = true;
        specialInAtt['\n'] = true;
        specialInAtt['\t'] = true;
        specialInAtt['<'] = true;
        specialInAtt['>'] = true;
        specialInAtt['&'] = true;
        specialInAtt['\"'] = true;
    }

    /**
    * Set Document Locator. Provided merely to satisfy the interface.
    */

    public void setDocumentLocator(Locator locator) {}


    /**
    * Start of the document. Make the writer and write the XML declaration.
    */

    public void startDocument () throws TransformerException
    {
        if (characterSet==null) characterSet = new UnicodeCharacterSet();
        writeDeclaration();
        empty = true;
        String rep = outputProperties.getProperty(SaxonOutputKeys.CHARACTER_REPRESENTATION);
        if (rep!=null) {
        	preferHex = (rep.trim().equalsIgnoreCase("hex"));
        }
    }

    /**
    * Output the XML declaration
    */

    public void writeDeclaration() throws TransformerException {
        if (declarationIsWritten) return;
        declarationIsWritten = true;
        try {

            String omit = outputProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION);
            if (omit==null) omit = "no";

            String version = outputProperties.getProperty(OutputKeys.VERSION);
            if (version==null) version = "1.0";

            String encoding = outputProperties.getProperty(OutputKeys.ENCODING);
            if (encoding==null || encoding.equalsIgnoreCase("utf8")) {
                encoding = "utf-8";
            }

            if (!(encoding.equalsIgnoreCase("utf-8"))) {
                omit = "no";
            }

            String standalone = outputProperties.getProperty(OutputKeys.STANDALONE);

            if (omit.equals("no")) {
                writer.write("<?xml version=\"" + version + "\" " +
                              "encoding=\"" + encoding + "\"" +
                              (standalone!=null ? (" standalone=\"" + standalone + "\"") : "") +
                              "?>");
                    // no longer write a newline character: it's wrong if the output is an
                    // external general parsed entity
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }


    /**
    * Output the document type declaration
    */

    boolean docTypeWritten = false;
    protected void writeDocType(String type, String systemId, String publicId) throws TransformerException {
        if (docTypeWritten) return;
        docTypeWritten = true;
        try {
            writer.write("\n<!DOCTYPE " + type + "\n");
            if (systemId!=null && publicId==null) {
                writer.write("  SYSTEM \"" + systemId + "\">\n");
            } else if (systemId==null && publicId!=null) {     // handles the HTML case
                writer.write("  PUBLIC \"" + publicId + "\">\n");
            } else {
                writer.write("  PUBLIC \"" + publicId + "\" \"" + systemId + "\">\n");
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * End of the document. Close the output stream.
    */

    public void endDocument () throws TransformerException
    {
        try {
            writer.flush();
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Start of an element. Output the start tag, escaping special characters.
    */

    public void startElement (int nameCode, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException
    {
    	String prefix;
    	String uri;
    	String displayName;

    	if (nameCode==lastNameCode) {
    		prefix = lastPrefix;
    		uri = lastURI;
    		displayName = lastDisplayName;
    	} else {
	    	prefix = namePool.getPrefix(nameCode);
	    	uri = namePool.getURI(nameCode);
	    	displayName = namePool.getDisplayName(nameCode);

	    	lastNameCode = nameCode;
    		lastDisplayName = displayName;
    		lastPrefix = prefix;
    		lastURI = uri;
	    }

        try {
            if (empty) {
                String systemId = outputProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM);
                String publicId = outputProperties.getProperty(OutputKeys.DOCTYPE_PUBLIC);
                if (systemId!=null) {
                    writeDocType(displayName, systemId, publicId);
                }
                empty = false;
            }
            if (openStartTag) {
                closeStartTag(nameCode, false);
            }
            writer.write('<');
            testCharacters(displayName);
            writer.write(displayName);

            // output the namespaces

            for (int n=0; n<nscount; n++) {
                writer.write(' ');
                String nsprefix = namePool.getPrefixFromNamespaceCode(namespaces[n]);
                String nsuri = namePool.getURIFromNamespaceCode(namespaces[n]);

                if (nsprefix.equals("")) {
                    writeAttribute(nameCode, "xmlns", "CDATA", nsuri);
                } else {
                    writeAttribute(nameCode, "xmlns:" + nsprefix, "CDATA", nsuri);
                }

            }

            // output the attributes

            for (int i=0; i<attributes.getLength(); i++) {
                writer.write(' ');
                writeAttribute(
                    nameCode,
                    attributes.getQName(i),
                    attributes.getType(i),
                    attributes.getValue(i));
            }
            openStartTag = true;

        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    protected void closeStartTag(int nameCode, boolean emptyTag) throws TransformerException {
        try {
            if (openStartTag) {
                if (emptyTag) {
                    writer.write(emptyElementTagCloser(nameCode));
                } else {
                    writer.write('>');
                }
                openStartTag = false;
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Close an empty element tag. (This is overridden in XHTMLEmitter).
    */

    protected String emptyElementTagCloser(int nameCode) {
        return "/>";
    }

    /**
    * Write attribute name=value pair. The element name is not used in this version of the
    * method, but is used in the HTML subclass.
    */

    char[] attbuff1 = new char[256];
    protected void writeAttribute(int elCode, String attname, String type, String value) throws TransformerException {
        try {
            testCharacters(attname);
            writer.write(attname);
            if (type.equals("NO-ESC")) {
                // special attribute type to indicate that no escaping is needed
                writer.write('=');
                char delimiter = (value.indexOf('"') >= 0 ? '\'' : '"');
                writer.write(delimiter);
                writer.write(value);
                writer.write(delimiter);
            } else {
                writer.write("=\"");
                int len = value.length();
                if (len > attbuff1.length) {
                    attbuff1 = new char[len];
                }
                value.getChars(0, len, attbuff1, 0);
                writeEscape(attbuff1, 0, len, true);
                writer.write('\"');
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }


    /**
    * Test that all characters in a name are supported in the target encoding
    */

    protected void testCharacters(String name) throws TransformerException {
        for (int i=name.length()-1; i>=0; i--) {
            if (!characterSet.inCharset(name.charAt(i))) {
                throw new TransformerException("Invalid character in output name (" + name + ")");
            }
        }
    }

    protected boolean testCharacters(char[] array, int start, int len)
    //throws TransformerException
    {
        for (int i=start; i<len; i++) {
            if (!characterSet.inCharset(array[i])) {
                //throw new TransformerException("Invalid character in output ( &#" + (int)array[i] + "; )");
                return false;
            }
        }
        return true;
    }

    /**
    * End of an element.
    */

    public void endElement (int nameCode) throws TransformerException
    {
        try {
            if (openStartTag) {
                closeStartTag(nameCode, true);
            } else {
            	String displayName;
            	if (nameCode==lastNameCode) {
            		displayName = lastDisplayName;
            	} else {
            		displayName = namePool.getDisplayName(nameCode);
            	}
                writer.write("</");
                writer.write(displayName);
                writer.write('>');
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws TransformerException
    {
        try {
            if (openStartTag) {
                closeStartTag(-1, false);
            }
            if (!escaping) {
                if (testCharacters(ch, start, length)) {
                    writer.write(ch, start, length);
                } else {
                    // recoverable error - recover silently
                    writeEscape(ch, start, length, false);
                }
            } else {
                writeEscape(ch, start, length, false);
            }
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }


    /**
    * Handle a processing instruction.
    */

    public void processingInstruction (String target, String data)
        throws TransformerException
    {
        try {
            if (openStartTag) {
                closeStartTag(-1, false);
            }
            writer.write("<?" + target + (data.length()>0 ? ' ' + data : "") + "?>");
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Write contents of array to current writer, after escaping special characters
    * @param ch The character array containing the string
    * @param start The start position of the input string within the character array
    * @param length The length of the input string within the character array
    * This method converts the XML special characters (such as < and &) into their
    * predefined entities.
    */

    protected void writeEscape(char ch[], int start, int length, boolean inAttribute)
    throws java.io.IOException {
        int segstart = start;
        boolean[] specialChars = (inAttribute ? specialInAtt : specialInText);

        while (segstart < start+length) {
            int i = segstart;

            // find a maximal sequence of "ordinary" characters
            while (i < start+length &&
                     (ch[i]<128 ? !specialChars[ch[i]] : characterSet.inCharset(ch[i]))) {
                i++;
            }

            // write out this sequence
            writer.write(ch, segstart, i-segstart);

            // exit if this was the whole string
            if (i >= start+length) return;

            if (ch[i]>127) {

                // process characters not available in the current encoding

                int charval;

                //test for surrogate pairs
                //A surrogate pair is two consecutive Unicode characters.  The first
                //is in the range D800 to DBFF, the second is in the range DC00 to DFFF.
                //To compute the numeric value of the character corresponding to a surrogate
                //pair, use this formula (all numbers are hex):
        	    //(FirstChar - D800) * 400 + (SecondChar - DC00) + 10000

                if (ch[i]>=55296 && ch[i]<=56319) {
                    // we'll trust the data to be sound
                    charval = (((int)ch[i] - 55296) * 1024) + ((int)ch[i+1] - 56320) + 65536;
                    i++;
                } else {
                    charval = (int)ch[i];
                }

                outputCharacterReference(charval);

            } else {

                // process special ASCII characters

                if (ch[i]=='<') {
                    writer.write("&lt;");
                } else if (ch[i]=='>') {
                    writer.write("&gt;");
                } else if (ch[i]=='&') {
                    writer.write("&amp;");
                } else if (ch[i]=='\"') {
                    writer.write("&#34;");
                } else if (ch[i]=='\n') {
                    writer.write("&#xA;");
                } else if (ch[i]=='\r') {
                    writer.write("&#xD;");
                } else if (ch[i]=='\t') {
                    writer.write("&#x9;");
                }
            }
            segstart = ++i;
        }
    }

	/**
	* Output a decimal or hexadecimal character reference
	*/

    private char[] charref = new char[10];
    protected void outputCharacterReference(int charval) throws java.io.IOException {
		if (preferHex) {
	        int o = 0;
	        charref[o++]='&';
	        charref[o++]='#';
			charref[o++]='x';
	        String code = Integer.toHexString(charval);
	        int len = code.length();
	        for (int k=0; k<len; k++) {
	            charref[o++]=code.charAt(k);
	        }
	        charref[o++]=';';
	        writer.write(charref, 0, o);
		} else {
	        int o = 0;
	        charref[o++]='&';
	        charref[o++]='#';
	        String code = Integer.toString(charval);
	        int len = code.length();
	        for (int k=0; k<len; k++) {
	            charref[o++]=code.charAt(k);
	        }
	        charref[o++]=';';
	        writer.write(charref, 0, o);
	    }
    }

    /**
    * Set escaping on or off
    */

    public void setEscaping(boolean escaping) {
        this.escaping = escaping;
    }

    /**
    * Handle a comment.
    */

    public void comment (char ch[], int start, int length) throws TransformerException
    {
        try {
            if (openStartTag) {
                closeStartTag(-1, false);
            }
            writer.write("<!--");
            writer.write(ch, start, length);
            writer.write("-->");
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Set the result destination
    */

    public void setResult(Result result) {
        if (!(result instanceof StreamResult)) {
            throw new IllegalArgumentException("Destination for XMLEmitter must be a StreamResult");
        }
        writer = ((StreamResult)result).getWriter();
        if (writer==null) {
            throw new IllegalArgumentException("No writer supplied");
        }
        // TODO: must handle an OutputStream or a systemID
    }

    /**
    * Set output properties
    */

    public void setOutputProperties(Properties details) {
        characterSet = CharacterSetFactory.getCharacterSet(details);
        super.setOutputProperties(details);
    }

    /**
    * Set the URI for an unparsed entity in the document.
    */

    public void setUnparsedEntity(String name, String uri) throws TransformerException {
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

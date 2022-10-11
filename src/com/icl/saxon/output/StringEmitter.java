package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.charcode.UnicodeCharacterSet;
import java.util.*;
import java.io.*;
import org.xml.sax.Attributes;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

/**
  * This class outputs text content to a StringBuffer, and discards all other content.
  * @author Michael H. Kay
  */

final class StringEmitter extends Emitter {

    // element content is output for xsl:output method="text", but is suppressed for text
    // output to attribute, comment, or processing-instruction nodes

    private int ignoreElements = 0;
    private StringBuffer buffer;

    protected StringEmitter(StringBuffer buffer) {
        this.buffer = buffer;
    }

    /**
    * Start of the document.
    */

    public void startDocument () throws TransformerException {}

    /**
    * End of the document.
    */

    public void endDocument () throws TransformerException {}

    /**
    * Produce output using the current Writer. <BR>
    * Special characters are not escaped.
    * @param ch Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception TransformerException for any failure
    */

    public void characters(char ch[], int start, int length) throws TransformerException {
        if (ignoreElements == 0) {
            buffer.append(ch, start, length);
        }
    }

    /**
    * Output an element start tag. <br>
    * Does nothing with this output method.
    * @param name The element name (tag)
    */

    public void startElement(int nameCode, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException {
        ignoreElements++;
    }


    /**
    * Output an element end tag. <br>
    * Does nothing  with this output method.
    * @param name The element name (tag)
    */

    public void endElement(int nameCode) throws TransformerException {
        ignoreElements--;
    }

    /**
    * Output a processing instruction. <br>
    * Does nothing  with this output method.
    */

    public void processingInstruction(String name, String value) throws TransformerException {}

    /**
    * Output a comment. <br>
    * Does nothing with this output method.
    */

    public void comment(char ch[], int start, int length) throws TransformerException {}

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

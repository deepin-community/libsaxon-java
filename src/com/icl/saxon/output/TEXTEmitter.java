package com.icl.saxon.output;
import com.icl.saxon.charcode.UnicodeCharacterSet;
import java.io.IOException;
import org.xml.sax.Attributes;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

/**
  * This class generates TEXT output
  * @author Michael H. Kay
  */

public class TEXTEmitter extends XMLEmitter {

    // This class is no longer used for
    // output to attribute, comment, or processing-instruction nodes:
    // these use StringEmitter instead.

    private String mediaType = "text/plain";

    /**
    * Start of the document.
    */

    public void startDocument () throws TransformerException
    {
        String mime = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
        if (mime!=null) {
            mediaType = mime;
        }

        if (characterSet==null) {
            characterSet = UnicodeCharacterSet.getInstance();
        }
        empty = true;
    }

    /**
    * Produce output using the current Writer. <BR>
    * Special characters are not escaped.
    * @param ch Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception TransformerException for any failure
    */

    public void characters(char ch[], int start, int length) throws TransformerException {
        for (int i=start; i<start+length; i++) {
            if (!characterSet.inCharset(ch[i])) {
                throw new TransformerException("Output character not available in this encoding (decimal " + (int)ch[i] + ")");
            }
        }
        try {
            writer.write(ch, start, length);
        } catch (java.io.IOException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Output an element start tag. <br>
    * Does nothing with this output method.
    * @param name The element name (tag)
    */

    public void startElement(int nameCode, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException {
        // no-op
    }


    /**
    * Output an element end tag. <br>
    * Does nothing  with this output method.
    * @param name The element name (tag)
    */

    public void endElement(int nameCode) throws TransformerException {
        // no-op
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

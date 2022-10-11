package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Namespace;
import java.util.*;
import java.io.*;
import org.xml.sax.Attributes;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

/**
* XMLIndenter: This ProxyEmitter indents elements, by adding character data where appropriate.
* The character data is always added as "ignorable white space", that is, it is never added
* adjacent to existing character data.
*
* Author Michael H. Kay
*/


public class XMLIndenter extends ProxyEmitter {

    private int level = 0;
    private int indentSpaces = 3;
    private String indentChars = "                                                          ";
    private boolean sameline = false;
    private boolean afterTag = true;
    private boolean allWhite = true;
    private int suppressedAtLevel = -1;
        // records level at which xml:space="preserve" was found

    /**
    * Start of document
    */

    public void startDocument() throws TransformerException {
        super.startDocument();

        String s = outputProperties.getProperty(SaxonOutputKeys.INDENT_SPACES);
        if (s!=null) {
            try {
                indentSpaces = Integer.parseInt(s);
            } catch (Exception err) {
                indentSpaces = 3;
            }
        }

        String omit = outputProperties.getProperty(OutputKeys.OMIT_XML_DECLARATION);
        afterTag = omit==null || !omit.equals("yes") ||
                    outputProperties.getProperty(OutputKeys.DOCTYPE_SYSTEM)!=null ;
    }

    /**
    * Output element start tag
    */

    public void startElement(int tag, Attributes atts,
    						 int[] namespaces, int nscount) throws TransformerException {
        if (afterTag) {
            indent();
        }
        super.startElement(tag, atts, namespaces, nscount);
        if ("preserve".equals(atts.getValue(Namespace.XML, "space")) && suppressedAtLevel < 0) {
            suppressedAtLevel = level;
        }
        level++;
        sameline = true;
        afterTag = true;
        allWhite = true;
    }

    /**
    * Output element end tag
    */

    public void endElement(int tag) throws TransformerException {
        level--;
        if (afterTag && !sameline) indent();
        super.endElement(tag);
        sameline = false;
        afterTag = true;
        allWhite = true;
        if (level == (suppressedAtLevel - 1)) {
            suppressedAtLevel = -1;
            // remove the suppression of indentation
        }
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws TransformerException {
        super.processingInstruction(target, data);
        afterTag = true;
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        for (int i=start; i<start+len; i++) {
            if (chars[i]=='\n') {
                sameline = false;
            }
            if (!Character.isWhitespace(chars[i])) {
                allWhite = false;
            }
        }
        super.characters(chars, start, len);
        if (!allWhite) {
            afterTag = false;
        }
    }

    /**
    * Output ignorable white space
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws TransformerException {
        // ignore it
    }

    /**
    * Output a comment
    */

    public void comment(char[] chars, int start, int len) throws TransformerException {
        super.comment(chars, start, len);
        afterTag = true;
    }

    /**
    * End of document
    */

    public void endDocument() throws TransformerException {
        super.endDocument();
    }

    /**
    * Output white space to reflect the current indentation level
    */

    private void indent() throws TransformerException {
        if (suppressedAtLevel >= 0) {
            // indentation has been suppressed (by xmlspace="preserve")
            return;
        }
        int spaces = level * indentSpaces;
        while (spaces > indentChars.length()) {
            indentChars += indentChars;
        }
        char[] array = new char[spaces + 1];
        array[0] = '\n';
        indentChars.getChars(0, spaces, array, 1);
        super.characters(array, 0, spaces+1);
        sameline = false;
    }

};

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


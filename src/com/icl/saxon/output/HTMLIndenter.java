package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.sort.HashMap;
import java.io.*;
import org.xml.sax.Attributes;
import javax.xml.transform.TransformerException;

/**
* HTMLIndenter: This ProxyEmitter indents HTML elements, by adding whitespace
* character data where appropriate.
* The character data is never added when within an inline element.
* The string used for indentation defaults to four spaces, but may be set using the
* indent-chars property
*
* Author Michael H. Kay
*/


public class HTMLIndenter extends ProxyEmitter {

    private int level = 0;
    private int indentSpaces = 3;
    private String indentChars = "                                                          ";
    private boolean sameLine = false;
    private boolean isInlineTag = false;
    private boolean inFormattedTag = false;
    private boolean afterInline = false;
    private boolean afterFormatted = true;    // to prevent a newline at the start


    // the list of inline tags is from the HTML 4.0 (loose) spec. The significance is that we
    // mustn't add spaces immediately before or after one of these elements.

    private static String[] inlineTags = {
        "tt", "i", "b", "u", "s", "strike", "big", "small", "em", "strong", "dfn", "code", "samp",
         "kbd", "var", "cite", "abbr", "acronym", "a", "img", "applet", "object", "font",
         "basefont", "br", "script", "map", "q", "sub", "sup", "span", "bdo", "iframe", "input",
         "select", "textarea", "label", "button" };

    private static HashMap inlineTable = new HashMap(203);

    static {
        for (int j=0; j<inlineTags.length; j++) {
            inlineTable.set(inlineTags[j]);
        }
    }

    private static boolean isInline(String tag) {
        return inlineTable.get(tag);
    }

    // Table of preformatted elements

    private static HashMap formattedTable = new HashMap(51);

    static {
        formattedTable.set("pre");
        formattedTable.set("script");
        formattedTable.set("style");
        formattedTable.set("textarea");
        formattedTable.set("xmp");          // obsolete but still encountered!
    }

    private static boolean isFormatted(String tag) {
        return formattedTable.get(tag);
    }



    public HTMLIndenter() {}


    /**
    * Start of document
    */

    public void startDocument() throws TransformerException {
        super.startDocument();
        String s = outputProperties.getProperty(SaxonOutputKeys.INDENT_SPACES);
        if (s==null) {
            indentSpaces = 3;
        } else {
            try {
                indentSpaces = Integer.parseInt(s);
            } catch (Exception err) {
                indentSpaces = 3;
            }
        }
    }

    /**
    * Output element start tag
    */

    public void startElement(int nameCode, Attributes atts,
    						 int[] namespaces, int nscount) throws TransformerException {
        String tag = namePool.getDisplayName(nameCode);
        isInlineTag = isInline(tag);
        inFormattedTag = inFormattedTag || isFormatted(tag);
        if (!isInlineTag && !inFormattedTag &&
             !afterInline && !afterFormatted) {
            indent();
        }


        super.startElement(nameCode, atts, namespaces, nscount);
        level++;
        sameLine = true;
        afterInline = false;
        afterFormatted = false;
    }

    /**
    * Output element end tag
    */

    public void endElement(int nameCode) throws TransformerException {
        level--;
        String tag = namePool.getDisplayName(nameCode);
        boolean thisInline = isInline(tag);
        boolean thisFormatted = isFormatted(tag);
        if (!thisInline && !thisFormatted && !afterInline &&
                 !sameLine && !afterFormatted && !inFormattedTag) {
            indent();
            afterInline = false;
            afterFormatted = false;
        } else {
            afterInline = thisInline;
            afterFormatted = thisFormatted;
        }
        super.endElement(nameCode);
        inFormattedTag = inFormattedTag && !thisFormatted;
        sameLine = false;
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws TransformerException {
        super.processingInstruction(target, data);
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        if (inFormattedTag) {
            super.characters(chars, start, len);
        } else {
            int lastNL = start;

            for (int i=start; i<start+len; i++) {
                if (chars[i]=='\n' || (i-lastNL > 120 && chars[i]==' ')) {
                    sameLine = false;
                    super.characters(chars, lastNL, i-lastNL);
                    indent();
                    lastNL = i+1;
                    while (lastNL<len && chars[lastNL]==' ') lastNL++;
                }
            }
            if (lastNL<start+len) {
                super.characters(chars, lastNL, start+len-lastNL);
            }
        }
        afterInline = false;
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
        indent();
        super.comment(chars, start, len);
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
        int spaces = level * indentSpaces;
        while (spaces > indentChars.length()) {
            indentChars += indentChars;
        }
        char[] array = new char[spaces + 1];
        array[0] = '\n';
        indentChars.getChars(0, spaces, array, 1);
        super.characters(array, 0, spaces+1);
        sameLine = false;
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


package com.icl.saxon.output;
import com.icl.saxon.charcode.CharacterSet;
import com.icl.saxon.charcode.CharacterSetFactory;
import org.xml.sax.Attributes;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

/**
* CDATAFilter: This ProxyEmitter converts character data to CDATA sections,
* if the character data belongs to one of a set of element types to be handled this way.
*
* @author Michael H. Kay
*/


public class CDATAFilter extends ProxyEmitter {

    private StringBuffer buffer = new StringBuffer(100);
    private Stack stack = new Stack();
    private int[] nameList;             // fingerprints of cdata elements
    private CharacterSet characterSet;
    private boolean disableEscaping = false;

    /**
    * Output element start tag
    */

    public void startElement(int nameCode, Attributes atts,
    						 int[] namespaces, int nscount) throws TransformerException {
        // System.err.println("Start element " + nameCode);
        flush(buffer);
        stack.push(new Integer(nameCode & 0xfffff));
        super.startElement(nameCode, atts, namespaces, nscount);
    }

    /**
    * Output element end tag
    */

    public void endElement(int nameCode) throws TransformerException {
        // System.err.println("End element " + nameCode);
        flush(buffer);
        stack.pop();
        super.endElement(nameCode);
    }

    /**
    * Output a processing instruction
    */

    public void processingInstruction(String target, String data) throws TransformerException {
        flush(buffer);
        super.processingInstruction(target, data);
    }

    /**
    * Output character data
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        // System.err.println("Characters: '" + new String(chars, start, len) + "'");
        buffer.append(chars, start, len);
    }

    /**
    * Output ignorable white space
    */

    public void ignorableWhitespace(char[] chars, int start, int len) throws TransformerException {
        buffer.append(chars, start, len);
    }

    /**
    * Output a comment
    */

    public void comment(char[] chars, int start, int len) throws TransformerException {
        flush(buffer);
        super.comment(chars, start, len);
    }

    /**
    * Set escaping on or off
    */

    public void setEscaping(boolean escaping) throws TransformerException {
        //System.err.println("Set escaping " + escaping);
        boolean cdata;
        if (stack.isEmpty()) {
            cdata = false;      // text is not part of any element
        } else {
            int fprint = ((Integer)stack.peek()).intValue();
            cdata = isCDATA(fprint);
        }

        if (!cdata) {
            flush(buffer);
            disableEscaping = !escaping;
            super.setEscaping(escaping);
        } else {
            if (!escaping) {
                flush(buffer);
                disableEscaping = true;
                super.setEscaping(false);
            } else {
                flush(buffer);
                disableEscaping = false;
                super.setEscaping(true);
            }
        }
    }

    /**
    * Flush the buffer containing accumulated character data,
    * generating it as CDATA where appropriate
    */

    public void flush(StringBuffer buffer) throws TransformerException {
        boolean cdata;
        int end = buffer.length();
        if (end==0) return;

        if (stack.isEmpty()) {
            cdata = false;      // text is not part of any element
        } else {
            int fprint = ((Integer)stack.peek()).intValue();
            cdata = isCDATA(fprint);
        }
        // System.err.println("Flush cdata=" + cdata + " disable=" + disableEscaping + " content=" + buffer);
        if (cdata & !disableEscaping) {

            // Check that the buffer doesn't include a character not available in the current
            // encoding

            int start = 0;
            int k = 0;
            while ( k<end ) {
                int next = buffer.charAt(k);
                int skip = 1;
                if (isHighSurrogate((char)next)) {
                    next = supplemental((char)next, buffer.charAt(k+1));
                    skip = 2;
                }
                if (characterSet.inCharset(next)) {
                    k++;
                } else {

                    // flush out the preceding characters as CDATA

                    char[] array = new char[k-start];
                    buffer.getChars(start, k, array, 0);
                    flushCDATA(array, k-start);

                    while (k < end) {
                        // output consecutive non-encodable characters
                        // before restarting the CDATA section
                        array = new char[skip];
                        buffer.getChars(k, k+skip, array, 0);
                        super.characters(array, 0, skip);
                        k += skip;
                        next = buffer.charAt(k);
                        skip = 1;
                        if (isHighSurrogate((char)next)) {
                            next = supplemental((char)next, buffer.charAt(k+1));
                            skip = 2;
                        }
                        if (characterSet.inCharset(next)) {
                            break;
                        }
                    }
                    start=k;
                }
            }
            char[] rest = new char[end-start];
            buffer.getChars(start, end, rest, 0);
            flushCDATA(rest, end-start);

        } else {
            char[] array = new char[end];
            buffer.getChars(0, end, array, 0);
            super.characters(array, 0, end);
        }

        buffer.setLength(0);

    }

    private boolean isHighSurrogate(char c) {
        return (c & 0xFC00) == 0xD800;
    }

    private int supplemental(char high, char low) {
        return (high - 0xD800) * 0x400 + (low - 0xDC00) + 0x10000;
    }

    /**
    * Output an array as a CDATA section. At this stage we have checked that all the characters
    * are OK, but we haven't checked that there is no "]]>" sequence in the data
    */

    private void flushCDATA(char[] array, int len) throws TransformerException {
        super.setEscaping(false);
        super.characters(("<![CDATA[").toCharArray(), 0, 9);

        // Check that the character data doesn't include the substring "]]>"

        int i=0;
        int doneto=0;
        while (i<len-2) {
            if (array[i]==']' && array[i+1]==']' && array[i+2]=='>') {
                super.characters(array, doneto, i+2-doneto);
                super.characters(("]]><![CDATA[").toCharArray(), 0, 12);
                doneto=i+2;
            }
            i++;
        }
        super.characters(array, doneto, len-doneto);
        super.characters(("]]>").toCharArray(), 0, 3);
        super.setEscaping(true);
    }

    /**
    * Set output properties
    */

    public void setOutputProperties (Properties details) {
        nameList = getCdataElements(details);
        characterSet = CharacterSetFactory.getCharacterSet(details);
        super.setOutputProperties(details);
    }

    /**
    * See if a particular element is a CDATA element
    */

    public boolean isCDATA(int fingerprint) {
        for (int i=0; i<nameList.length; i++) {
            if (nameList[i]==fingerprint) return true;
        }
		return false;
	}

    /**
    * Extract the list of CDATA elements from the output properties
    */

    private int[] getCdataElements(Properties details) {
        String cdata = details.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS);
        if (cdata==null) {
            return new int[0];
        }
        // first count the number of names in the list
        int count=0;
        StringTokenizer st1 = new StringTokenizer(cdata);
        while (st1.hasMoreTokens()) {
            st1.nextToken();
            count++;
        }
        int[] array = new int[count];
        count = 0;
        StringTokenizer st2 = new StringTokenizer(cdata);
        while (st2.hasMoreTokens()) {
            String expandedName = st2.nextToken();
            array[count++] = getFingerprintForExpandedName(expandedName);
        }
        return array;
    }


    /**
    * Get fingerprint for expanded name in {uri}local format
    */

    private int getFingerprintForExpandedName(String expandedName) {
        String localName;
        String namespace;

        if (expandedName.charAt(0)=='{') {
            int closeBrace = expandedName.indexOf('}');
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in parameter name");
            }
            namespace = expandedName.substring(1, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in parameter name");
            }
            localName = expandedName.substring(closeBrace+1);
        } else {
            namespace = "";
            localName = expandedName;
        }

        return namePool.allocate("", namespace, localName);
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


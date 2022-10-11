package com.icl.saxon.output;
import com.icl.saxon.*;
//import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import org.xml.sax.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.TransformerException;

/**
  * DTDEmitter is an Emitter that generates output in DTD format from special elements
  * such as dtd:doctype and dtd:element.
  */

public class DTDEmitter extends ProxyEmitter
{
    private String current = null;
    private boolean openSquare = false;
    private StringBuffer buffer = null;

    /**
    * Start of an element.
    */

    public void startElement (int nameCode, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException
    {
        String uri = namePool.getURI(nameCode);
		String localname = namePool.getLocalName(nameCode);

		// Suppress the DTD namespace

		int dtdpos = -1;
		for (int n=0; n<nscount; n++) {
			if (namePool.getURIFromNamespaceCode(namespaces[n]).equals(Namespace.DTD)) {
				dtdpos = n;
				break;
			}
		}

		if (dtdpos>0) {
			// remove the entry at position dtdpos
			namespaces[dtdpos] = namespaces[nscount-1];
			nscount--;
		}

        if (uri.equals(Namespace.DTD)) {

            if ("doctype".equals(current) && !openSquare) {
                write(" [");
                openSquare = true;
            }

            if (localname.equals("doctype")) {
                buffer = new StringBuffer();
                if (current!=null) {
                    throw new TransformerException("dtd:doctype can only appear at top level of DTD");
                }
                String name = attributes.getValue("name");
                String system = attributes.getValue("system");
                String publicid = attributes.getValue("public");
                if (name==null) {
                    throw new TransformerException("dtd:doctype must have a name attribute");
                }

                write("<!DOCTYPE " + name + " ");
                if (system!=null) {
                    if (publicid!=null) {
                        write("PUBLIC \"" + publicid + "\" \"" + system + "\"");
                    } else {
                        write("SYSTEM \"" + system + "\"");
                    }
                }

            } else if (localname.equals("element")) {
                if (!("doctype".equals(current))) {
                    throw new TransformerException("dtd:element can only appear as child of dtd:doctype");
                }
                String name = attributes.getValue("name");
                String content = attributes.getValue("content");
                if (name==null) {
                    throw new TransformerException("dtd:element must have a name attribute");
                }
                if (content==null) {
                    throw new TransformerException("dtd:element must have a content attribute");
                }
                write("\n  <!ELEMENT " + name + " " + content + " ");

            } else if (localname.equals("attlist")) {
                if (!("doctype".equals(current))) {
                    throw new TransformerException("dtd:attlist can only appear as child of dtd:doctype");
                }
                String name = attributes.getValue("element");
                if (name==null) {
                    throw new TransformerException("dtd:attlist must have an attribute named 'element'");
                }
                write("\n  <!ATTLIST " + name + " " );

            } else if (localname.equals("attribute")) {
                if (!("attlist".equals(current))) {
                    throw new TransformerException("dtd:attribute can only appear as child of dtd:attlist");
                }
                String name = attributes.getValue("name");
                String type = attributes.getValue("type");
                String value = attributes.getValue("value");
                if (name==null) {
                    throw new TransformerException("dtd:attribute must have a name attribute");
                }
                if (type==null) {
                    throw new TransformerException("dtd:attribute must have a type attribute");
                }
                if (value==null) {
                    throw new TransformerException("dtd:attribute must have a value attribute");
                }
                write("\n    " + name + " " + type + " " + value);

            } else if (localname.equals("entity")) {
                if (!("doctype".equals(current))) {
                    throw new TransformerException("dtd:entity can only appear as child of dtd:doctype");
                }
                String name = attributes.getValue("name");
                String parameter = attributes.getValue("parameter");
                String system = attributes.getValue("system");
                String publicid = attributes.getValue("public");
                String notation = attributes.getValue("notation");

                if (name==null) {
                    throw new TransformerException("dtd:entity must have a name attribute");
                }

                // we could do a lot more checking now...

                write("\n  <!ENTITY ");
                if ("yes".equals(parameter)) {
                    write("% ");
                }
                write(name + " ");
                if (system!=null) {
                    if (publicid!=null) {
                        write("PUBLIC \"" + publicid + "\" \"" + system + "\" ");
                    } else {
                        write("SYSTEM \"" + system + "\" ");
                    }
                }
                if (notation!=null) {
                    write("NDATA " + notation + " ");
                }

            } else if (localname.equals("notation")) {
                if (!("doctype".equals(current))) {
                    throw new TransformerException("dtd:notation can only appear as a child of dtd:doctype");
                }
                String name = attributes.getValue("name");
                String system = attributes.getValue("system");
                String publicid = attributes.getValue("public");
                if (name==null) {
                    throw new TransformerException("dtd:notation must have a name attribute");
                }
                if ((system==null) && (publicid==null)) {
                    throw new TransformerException("dtd:notation must have a system attribute or a public attribute");
                }
                write("\n  <!NOTATION " + name);
                if (publicid!=null) {
                    write(" PUBLIC \"" + publicid + "\" ");
                    if (system!=null) {
                        write("\"" + system + "\" ");
                    }
                } else {
                    write(" SYSTEM \"" + system + "\" ");
                }
            } else {
                throw new TransformerException("Unrecognized element " + localname + " in DTD output");
            }

        } else {
            if (!(current.equals("entity"))) {
                throw new TransformerException("Unrecognized element " + localname + " in DTD output");
            }
            super.startElement(nameCode, attributes, namespaces, nscount);
        }
    	current = localname;

    }


    /**
    * End of an element.
    */

    public void endElement (int nameCode) throws TransformerException
    {
        String uri = namePool.getURI(nameCode);

        //try {
            if (uri.equals(Namespace.DTD)) {
        		String localname = namePool.getLocalName(nameCode);

                if (localname.equals("doctype")) {
                    if (openSquare) {
                        write("\n]");
                        openSquare = false;
                    }
                    write(">\n");
                    current=null;
                    flush();

                } else if (localname.equals("element")) {
                    write(">");
                    current="doctype";

                } else if (localname.equals("attlist")) {
                    write(">");
                    current="doctype";

                } else if (localname.equals("attribute")) {
                    current="attlist";

                } else if (localname.equals("entity")) {
                    write(">");
                    current="doctype";

                } else if (localname.equals("notation")) {
                    write(">");
                    current="doctype";
                }
            } else {
                super.endElement(nameCode);
            }


        //} catch (java.io.IOException err) {
        //    throw new TransformerException(err);
        //}

    }

    /**
    * Write character data (normally the value of an entity)
    */

    public void characters(char[] chars, int start, int len) throws TransformerException {
        if (buffer!=null) {
            buffer.append(chars, start, len);
        } else {
            super.characters(chars, start, len);
        }
    }

    /**
    * Write content to the buffer
    */

    private void write(String s) {
        buffer.append(s);
    }

    /**
    * Flush the finished DTD to the underlying XML emitter
    */

    private void flush() throws TransformerException {
        int len = buffer.length();
        char[] chars = new char[len];
        buffer.getChars(0, len, chars, 0);
        buffer = null;
        setEscaping(false);
        characters(chars, 0, len);
        setEscaping(true);
    }

    /**
    * On completion, get the finished DTD
    */

    //public String getDTD() {
    //    return ((StringWriter)writer).toString();
    //}

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

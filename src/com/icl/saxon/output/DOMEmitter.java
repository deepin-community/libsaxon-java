package com.icl.saxon.output;
import com.icl.saxon.om.Namespace;
import org.w3c.dom.*;
import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;


/**
  * DOMEmitter is an Emitter that attaches the result tree to a specified Node in a DOM Document
  */

public class DOMEmitter extends Emitter
{
    protected Node currentNode;
    protected Document document;
    private boolean canNormalize = true;

    /**
    * Start of the document.
    */

    public void startDocument ()
    {

    }

    /**
    * End of the document.
    */

    public void endDocument ()
    {

    }


    /**
    * Start of an element. Output the start tag, escaping special characters.
    */

    public void startElement (int nameCode, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException
    {
        String name = namePool.getDisplayName(nameCode);
        try {

            Element element = document.createElement(name);
            currentNode.appendChild(element);
            currentNode = element;

            // output the namespaces

            for (int n=0; n<nscount; n++) {
            	String prefix = namePool.getPrefixFromNamespaceCode(namespaces[n]);
        		String uri = namePool.getURIFromNamespaceCode(namespaces[n]);
                if (!(uri.equals(Namespace.XML))) {
                    if (prefix.equals("")) {
                        element.setAttribute("xmlns", uri);
                    } else {
                        element.setAttribute("xmlns:" + prefix, uri);
                    }
                }
            }

            // output the attributes

            for (int i=0; i<attributes.getLength(); i++) {
                element.setAttribute(
                    attributes.getQName(i),
                    attributes.getValue(i));
            }

        } catch (DOMException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * End of an element.
    */

    public void endElement (int nameCode) throws TransformerException
    {

		if (canNormalize) {
	        try {
	            currentNode.normalize();
	        } catch (Throwable err) {
	        	canNormalize = false;
	        }      // in case it's a Level 1 DOM
	    }

        currentNode = currentNode.getParentNode();

    }


    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws TransformerException
    {
        try {
            Text text = document.createTextNode(new String(ch, start, length));
            currentNode.appendChild(text);
        } catch (DOMException err) {
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
            ProcessingInstruction pi =
                document.createProcessingInstruction(target, data);
            currentNode.appendChild(pi);
        } catch (DOMException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Handle a comment.
    */

    public void comment (char ch[], int start, int length) throws TransformerException
    {
        try {
            Comment comment = document.createComment(new String(ch, start, length));
            currentNode.appendChild(comment);
        } catch (DOMException err) {
            throw new TransformerException(err);
        }
    }

    /**
    * Set output destination
    */

    public void setNode (Node node) {
        currentNode = node;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            document = (Document)node;
        } else {
            document = currentNode.getOwnerDocument();
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

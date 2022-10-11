package com.icl.saxon.tinytree;
import com.icl.saxon.Context;
import com.icl.saxon.expr.SingletonNodeSet;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;


/**
  * The TinyBuilder class is responsible for taking a stream of SAX events and constructing
  * a Document tree, using the "TinyTree" implementation.
  *
  * @author Michael H. Kay
  */

public class TinyBuilder extends Builder

{

    private int currentDepth = 0;
    private int nodeNr = 0;             // this is the local sequence within this document
    private int attributeNodeNr = 0;
    private int namespaceNodeNr = 0;
    private boolean ended = false;

    /*NEXT*/ private int[] prevAtDepth = new int[100];

    public void createDocument () {
        currentDocument = new TinyDocumentImpl();
        if (locator==null) {
            locator = this;
        }
        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        doc.setSystemId(locator.getSystemId());
        doc.setNamePool(namePool);
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws TransformerException
    {
        // System.err.println("Builder: " + this + " Start document");
        failed = false;
        if (started) {
            // this happens when using an IdentityTransformer
            return;
        }
        started = true;

        if (currentDocument==null) {
            // normal case
            createDocument();
        } else {
            // document node supplied by user
            if (!(currentDocument instanceof TinyDocumentImpl)) {
                throw new TransformerException("Root node supplied is of wrong type");
            }
            if (currentDocument.hasChildNodes()) {
                throw new TransformerException("Supplied document is not empty");
            }
            currentDocument.setNamePool(namePool);
        }

        //currentNode = currentDocument;
        currentDepth = 0;
        nodeNr = 0;

        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        if (lineNumbering) {
            doc.setLineNumbering();
        }

        doc.addNode(NodeInfo.ROOT, 0, 0, 0, -1);
        /*NEXT*/ prevAtDepth[0] = 0;
        /*NEXT*/ doc.next[0] = -1;

        currentDepth++;
        nodeNr++;

    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endDocument () throws TransformerException
    {
             // System.err.println("TinyBuilder: " + this + " End document");

        if (ended) return;  // happens when using an IdentityTransformer
        ended = true;

        /*NEXT*/ TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        /*NEXT*/ int prev = prevAtDepth[currentDepth];
        /*NEXT*/ if (prev > 0) {
        /*NEXT*/     doc.next[prev] = -1;
        /*NEXT*/ }
        /*NEXT*/ prevAtDepth[currentDepth] = -1;

        //namePool.diagnosticDump();

    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (
        int nameCode, Attributes attributes, int[] namespaces, int namespacesUsed) throws TransformerException
    {
         // System.err.println("TinyBuilder Start element (" + nameCode + ")");

        // Construct element name as a Name object

        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;

        // register the namespaces

        int firstNS = (namespacesUsed==0 ? -1 : doc.numberOfNamespaces);
        for (int n=0; n<namespacesUsed; n++) {
            doc.addNamespace(   nodeNr,
                                namespaces[n] );
        }
        namespacesUsed = 0;

        // register the attributes

        int numAtts = attributes.getLength();
        int firstAtt = (numAtts==0 ? -1 : doc.numberOfAttributes);

		doc.addNode(NodeInfo.ELEMENT, currentDepth, firstAtt, firstNS, nameCode);

        for (int i=0; i<numAtts; i++) {
        	int anamecode = namePool.allocate(
        						Name.getPrefix(attributes.getQName(i)),
                                attributes.getURI(i),
                                attributes.getLocalName(i));
            doc.addAttribute(   nodeNr,
            					anamecode,
                                attributes.getType(i),
                                attributes.getValue(i) );
        }



        /*NEXT*/ int prev = prevAtDepth[currentDepth];
        /*NEXT*/ if (prev > 0) {
        /*NEXT*/     doc.next[prev] = nodeNr;
        /*NEXT*/ }
        /*NEXT*/ prevAtDepth[currentDepth] = nodeNr;
        currentDepth++;

        /*NEXT*/ if (currentDepth == prevAtDepth.length) {
        /*NEXT*/     int[] p2 = new int[currentDepth*2];
        /*NEXT*/     System.arraycopy(prevAtDepth, 0, p2, 0, currentDepth);
        /*NEXT*/     prevAtDepth = p2;
        /*NEXT*/ }
        /*NEXT*/ prevAtDepth[currentDepth] = -1;

        if (locator!=null) {
            doc.setSystemId(nodeNr, locator.getSystemId());
            if (lineNumbering) {
                doc.setLineNumber(nodeNr, locator.getLineNumber());
            }
        }
        nodeNr++;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (int nameCode) throws TransformerException
    {
        // System.err.println("End element (" + nameCode + ")");

        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;

        // if a preview handler is registered, call it now
        if (previewManager != null) {

			int elementFP = nameCode & 0xfffff;
            if (previewManager.isPreviewElement(elementFP)) {
            	NodeInfo currentNode = doc.getNode(prevAtDepth[currentDepth-1]);
                //Controller c = previewManager.getController();
                Context context = controller.makeContext(currentNode);
                controller.applyTemplates(
                    context,
                    new SingletonNodeSet(currentNode),
                    controller.getRuleManager().getMode(previewManager.getPreviewMode()),
                    null);

                // reset nodeNr to effectively delete the node's descendants
                nodeNr = prevAtDepth[currentDepth-1] + 1;
                doc.truncate(nodeNr);
                // TODO: should truncate the text buffer, the attributes, etc.
            }
        }

        /*NEXT*/ int prev = prevAtDepth[currentDepth];
        /*NEXT*/ if (prev > 0) {
        /*NEXT*/     doc.next[prev] = -1;
        /*NEXT*/ }
        /*NEXT*/ prevAtDepth[currentDepth] = -1;

        currentDepth--;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int len) throws TransformerException
    {
         // System.err.println("Characters: " + new String(ch, start, len));
        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        if (len>0) {
            int bufferStart = doc.charBufferLength;
            doc.appendChars(ch, start, len);
            doc.addNode(NodeInfo.TEXT, currentDepth, bufferStart, len, -1);

            /*NEXT*/ int prev = prevAtDepth[currentDepth];
            /*NEXT*/ if (prev > 0) {
            /*NEXT*/     doc.next[prev] = nodeNr;
            /*NEXT*/ }
            /*NEXT*/ prevAtDepth[currentDepth] = nodeNr;

            nodeNr++;

        }


    }


    /**
    * Callback interface for SAX: not for application use<BR>
    * Note: because SAX1 does not deliver comment nodes, we get these in the form of a processing
    * instruction with a null name. This requires a specially-adapted SAX driver.
    */

    public void processingInstruction (String piname, String remainder) throws TransformerException
    {
    	// System.err.println("Builder: PI " + piname);
        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        if (!discardComments) {
            int s = doc.commentBuffer.length();
            doc.commentBuffer.append(remainder);
            int nameCode = namePool.allocate("", "", piname);
            doc.addNode(NodeInfo.PI, currentDepth, s, remainder.length(),
            			 nameCode);

            /*NEXT*/ int prev = prevAtDepth[currentDepth];
            /*NEXT*/ if (prev > 0) {
            /*NEXT*/     doc.next[prev] = nodeNr;
            /*NEXT*/ }
            /*NEXT*/ prevAtDepth[currentDepth] = nodeNr;

            nodeNr++;

                // TODO: handle PI Base URI
                //if (locator!=null) {
                //    pi.setLocation(locator.getSystemId(), locator.getLineNumber());
                //}
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void comment (char ch[], int start, int length) throws TransformerException
    {
        addComment(new String(ch, start, length));
    }

    private void addComment(String comment) throws TransformerException {
        TinyDocumentImpl doc = (TinyDocumentImpl)currentDocument;
        if (!discardComments && !inDTD) {
            int s = doc.commentBuffer.length();
            doc.commentBuffer.append(comment);
            doc.addNode(NodeInfo.COMMENT, currentDepth, s, comment.length(), -1);

            /*NEXT*/ int prev = prevAtDepth[currentDepth];
            /*NEXT*/ if (prev > 0) {
            /*NEXT*/     doc.next[prev] = nodeNr;
            /*NEXT*/ }
            /*NEXT*/ prevAtDepth[currentDepth] = nodeNr;

            nodeNr++;
        }

    }

    /**
    * Set an unparsed entity in the document
    */

    public void setUnparsedEntity(String name, String uri) {
        ((TinyDocumentImpl)currentDocument).setUnparsedEntity(name, uri);
    }



}   // end of outer class TinyBuilder

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

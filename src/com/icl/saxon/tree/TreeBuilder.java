package com.icl.saxon.tree;
import com.icl.saxon.Context;
import com.icl.saxon.expr.SingletonNodeSet;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import javax.xml.transform.TransformerException;
import java.util.Vector;

/**
  * The Builder class is responsible for taking a stream of SAX events and constructing
  * a Document tree.
  * @author Michael H. Kay
  */

public class TreeBuilder extends Builder

{
    private static AttributeCollection emptyAttributeCollection =
    				new AttributeCollection((NamePool)null);

    private ParentNodeImpl currentNode;

    private NodeFactory nodeFactory;
    private int[] size = new int[100];          // stack of number of children for each open node
    private int depth = 0;
    private Vector arrays = new Vector();       // reusable arrays for creating nodes
    private boolean previousText;
    private StringBuffer charBuffer;

    private int nextNodeNumber = 1;

    /**
    * create a Builder and initialise variables
    */

    public TreeBuilder() {
        nodeFactory = new DefaultNodeFactory();
    }

    /**
    * Set the Node Factory to use. If none is specified, the Builder uses its own.
    */

    public void setNodeFactory(NodeFactory factory) {
        nodeFactory = factory;
    }

  ////////////////////////////////////////////////////////////////////////////////////////
  // Implement the org.xml.sax.ContentHandler interface.
  ////////////////////////////////////////////////////////////////////////////////////////

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws TransformerException
    {
        // System.err.println("Builder: " + this + " Start document");
        failed = false;
        started = true;

        DocumentImpl doc;
        if (currentDocument==null) {
            // normal case
            doc = new DocumentImpl();
            currentDocument = doc;
        } else {
            // document node supplied by user
            if (!(currentDocument instanceof DocumentImpl)) {
                throw new TransformerException("Root node supplied is of wrong type");
            }
            doc = (DocumentImpl)currentDocument;
            if (doc.getFirstChild()!=null) {
                throw new TransformerException("Supplied document is not empty");
            }

        }
        if (locator==null || locator.getSystemId()==null) {
            locator = this;
        }
        doc.setSystemId(locator.getSystemId());
        doc.setNamePool(namePool);
        doc.setNodeFactory(nodeFactory);
        currentNode = doc;
        depth = 0;
        size[depth] = 0;
        doc.sequence = 0;
        charBuffer = new StringBuffer(estimatedLength);
        doc.setCharacterBuffer(charBuffer);
        if (lineNumbering) {
            doc.setLineNumbering();
        }

        //startTime = (new Date()).getTime();
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endDocument () throws TransformerException
    {
        if (currentNode==null) return;	// can be called twice on an error path
        currentNode.compact(size[depth]);
        currentNode = null;

        // we're not going to use this Builder again so give the garbage collector
        // something to play with
        arrays = null;

        //long endTime = (new Date()).getTime();
        //System.err.println("Build time: " + (endTime-startTime) + " milliseconds");

    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void setDocumentLocator (Locator locator)
    {
        this.locator = locator;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (
        int nameCode, Attributes attributes, int[] namespaces, int namespacesUsed) throws TransformerException
    {
        // System.err.println("Start element (" + uri + ", " + localname + ", " + rawname + ")");

        // Convert SAX2 Attributes object into an AttributeCollection
                    // the difference is historic, both classes perform the same function
                    // and could be combined.

        AttributeCollection atts;
        int numAtts = attributes.getLength();
        if (numAtts==0) {
            atts = emptyAttributeCollection;
        } else {
            atts = new AttributeCollection(namePool, attributes);
        }
                // System.err.println("TreeBuilder.locator = " + locator);
                // System.err.println("TreeBuilder.baseURI = " + baseURI);
        ElementImpl elem = nodeFactory.makeElementNode( currentNode,
                                                        nameCode,
                                                        atts,
                                                        namespaces,
                                                        namespacesUsed,
                                                        locator,
                                                        nextNodeNumber++);

        // the initial aray used for pointing to children will be discarded when the exact number
        // of children in known. Therefore, it can be reused. So we allocate an initial array from
        // a pool of reusable arrays. A nesting depth of >20 is so rare that we don't bother.

        while (depth >= arrays.size()) {
            arrays.addElement(new NodeImpl[20]);
        }
        elem.useChildrenArray((NodeImpl[])arrays.elementAt(depth));

        currentNode.addChild(elem, size[depth]++);
        if (depth >= size.length - 1) {
            int[] newsize = new int[size.length * 2];
            System.arraycopy(size, 0, newsize, 0, size.length);
            size = newsize;
        }
        size[++depth] = 0;

        namespacesUsed = 0;

    	if (currentNode instanceof DocumentInfo) {
    	    ((DocumentImpl)currentNode).setDocumentElement(elem);
    	}


        currentNode = elem;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (int nameCode) throws TransformerException
    {
        // System.err.println("End element " + namePool.getDisplayName(nameCode));
        currentNode.compact(size[depth]);

        // if a preview handler is registered, call it now
        if (previewManager != null) {

            if (previewManager.isPreviewElement(currentNode.getFingerprint())) {
                //Controller c = previewManager.getController();
                Context context = controller.makeContext(currentNode);
                controller.applyTemplates(
                    context,
                    new SingletonNodeSet(currentNode),
                    controller.getRuleManager().getMode(previewManager.getPreviewMode()),
                    null);
                currentNode.dropChildren();
            }
        }

        depth--;
        currentNode = (ParentNodeImpl)currentNode.getParentNode();
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int length) throws TransformerException
    {
        // System.err.println("Characters: " + new String(ch, start, length));
        if (length>0) {
            int bufferStart = charBuffer.length();
            //charBuffer.append(ch, start, length);

			// we rely on adjacent chunks of text having already been merged
            //TextImpl n = new TextImpl(currentNode, bufferStart, length);
			TextImpl n = new TextImpl(currentNode, new String(ch, start, length));
            currentNode.addChild(n, size[depth]++);
            previousText = true;

        }
    }

    /**
    * Callback interface for SAX: not for application use<BR>
    * Note: because SAX1 does not deliver comment nodes, we get these in the form of a processing
    * instruction with a null name. This requires a specially-adapted SAX driver.
    */

    public void processingInstruction (String name, String remainder)
    {
        if (!discardComments) {
        	int nameCode = namePool.allocate("", "", name);
            ProcInstImpl pi = new ProcInstImpl(nameCode, remainder);
            currentNode.addChild(pi, size[depth]++);
            if (locator!=null) {
                pi.setLocation(locator.getSystemId(), locator.getLineNumber());
            }
        }
    }

    /**
    * Callback interface for SAX (part of LexicalHandler interface): not for application use
    */

    public void comment (char ch[], int start, int length) throws TransformerException
    {
        if (!discardComments) {
            CommentImpl comment = new CommentImpl(new String(ch, start, length));
            currentNode.addChild(comment, size[depth]++);
        }
    }


    /**
    * graftElement() allows an element node to be transferred from one tree to another.
    * This is a dangerous internal interface which is used only to contruct a stylesheet
    * tree from a stylesheet using the "literal result element as stylesheet" syntax.
    * The supplied element is grafted onto the current element as its only child.
    */

    public void graftElement(ElementImpl element) throws TransformerException {
        currentNode.addChild(element, size[depth]++);
    }

    /**
    * Set an unparsed entity URI for the document
    */

    public void setUnparsedEntity(String name, String uri) {
        ((DocumentImpl)currentDocument).setUnparsedEntity(name, uri);
    }


    //////////////////////////////////////////////////////////////////////////////
    // Inner class DefaultNodeFactory. This creates the nodes in the tree.
    // It can be overridden, e.g. when building the stylesheet tree
    //////////////////////////////////////////////////////////////////////////////

    private class DefaultNodeFactory implements NodeFactory {

        public ElementImpl makeElementNode(
                NodeInfo parent,
                int nameCode,
                AttributeCollection attlist,
                int[] namespaces,
                int namespacesUsed,
                Locator locator,
                int sequenceNumber)

        {
            if (attlist.getLength()==0 && namespacesUsed==0) {

                // for economy, use a simple ElementImpl node

                ElementImpl e = new ElementImpl();
                String baseURI = null;
                int lineNumber = -1;

                if (locator!=null) {
                    baseURI = locator.getSystemId();
                    lineNumber = locator.getLineNumber();
                }

                e.initialise(nameCode, attlist, parent, baseURI, lineNumber, sequenceNumber);

                return e;

            } else {
                ElementWithAttributes e = new ElementWithAttributes();
                String baseURI = null;
                int lineNumber = -1;

                if (locator!=null) {
                    baseURI = locator.getSystemId();
                    lineNumber = locator.getLineNumber();
                }

                e.setNamespaceDeclarations(namespaces, namespacesUsed);

                e.initialise(nameCode, attlist, parent, baseURI, lineNumber, sequenceNumber);

                return e;
            }
        }
    }


}   // end of outer class TreeBuilder

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

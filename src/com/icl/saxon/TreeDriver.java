
package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.output.GeneralOutputter;
import org.xml.sax.*;
import org.w3c.dom.Document;


import java.util.Properties;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;


/**
* TreeDriver.java: (pseudo-)SAX driver for Saxon trees.<BR>
* Subclasses DOMDriver for the case where the tree is a Saxon tree (a DocumentInfo)
* This class simulates the action of a SAX Parser, taking an already-constructed
* DOM Document and walking around it in a depth-first traversal,
* calling a SAX-compliant ContentHandler to process the children as it does so.
*/

public class TreeDriver extends DOMDriver
{

    private Outputter outputter;

    /**
    * Set the DOM Document that will be walked
    */

    public void setDocument(Document doc) {
        root = doc;
        if (!(doc instanceof DocumentInfo)) {
            throw new IllegalArgumentException("TreeDriver can only be used with a Saxon tree");
        }
    }

    /**
    * Set the DOM Document that will be walked
    */

    //public void setDocumentInfo(DocumentInfo doc) {
    //    root = doc;
    //}

    /**
    * Walk a document (traversing the nodes depth first)
    * @param doc The (DOM) Document object to walk.
    * @exception SAXException On any error in the document
    */

    public void parse() throws SAXException
    {
        if (root==null) {
            throw new SAXException("TreeDriver: no start node defined");
        }
        if (contentHandler==null) {
            throw new SAXException("DOMDriver: no content handler defined");
        }
        contentHandler.setDocumentLocator(this);
        DocumentInfo doc = (DocumentInfo)root;
        try {
            GeneralOutputter outputter = new GeneralOutputter(doc.getNamePool());
            SAXResult result = new SAXResult(contentHandler);
            result.setSystemId(systemId);
            outputter.setOutputDestination(new Properties(), result);
            doc.copy(outputter);
            outputter.close();
        } catch (TransformerException err) {
            throw new SAXException(err);
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

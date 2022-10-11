package com.icl.saxon.output;
import com.icl.saxon.*;
//import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NamePool;

import org.xml.sax.Attributes;
//import org.w3c.dom.*;
import javax.xml.transform.TransformerException;

import java.io.Writer;


/**
  * ErrorEmitter is an Emitter that generates an error message if any attempt
  * is made to produce output. It is used while a saxon:function is active to
  * prevent functions writing to the result tree.
  */

public class ErrorEmitter extends Emitter
{
    /**
    * Start of the document.
    */

    public void startDocument () throws TransformerException {}

    /**
    * End of the document.
    */

    public void endDocument () throws TransformerException {}

    /**
    * Start of an element. Output the start tag, escaping special characters.
    */

    public void startElement (int name, Attributes attributes,
    						  int[] namespaces, int nscount) throws TransformerException
    {
        error();
    }

    /**
    * End of an element.
    */

    public void endElement (int name) throws TransformerException
    {
        error();
    }


    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws TransformerException
    {
        error();
    }


    /**
    * Handle a processing instruction.
    */

    public void processingInstruction (String target, String data)
        throws TransformerException
    {
        error();
    }

    /**
    * Handle a comment.
    */

    public void comment (char ch[], int start, int length) throws TransformerException
    {
        error();
    }

    /**
    * Report an error: can't write to result tree
    */

    private void error() throws TransformerException {
        throw new TransformerException("Cannot write to result tree while executing a function");
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

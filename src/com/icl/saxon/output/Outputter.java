package com.icl.saxon.output;
import com.icl.saxon.*;
import org.xml.sax.Attributes;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Result;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import java.io.*;

/**
  * This class allows output to be generated. It channels output requests to an
  * Emitter which does the actual writing. This is an abstract class, there are
  * concrete implementions for XML output and text output.
  *
  * @author Michael H. Kay
  */

public abstract class Outputter {

    protected Emitter emitter;

    /**
    * Get emitter. This is used by xsl:copy-of, a fragment is copied directly to the
    * Emitter rather than going via the Outputter.
    */

    public Emitter getEmitter() throws TransformerException {
        reset();
        return emitter;
    }

    /**
    * Synchronize the state of the Outputter with that of the underlying Emitter
    */

    public abstract void reset() throws TransformerException;

    public abstract Properties getOutputProperties();

    /**
    * Switch escaping (of special characters) on or off.
    * @param escaping: true if special characters are to be escaped, false if not.
    */

    public final void setEscaping(boolean escaping) throws TransformerException {
        emitter.setEscaping(escaping);
    }

    /**
    * Start the output process
    */

    public final void open() throws TransformerException {
        // System.err.println("Open " + this + " using emitter " + emitter.getClass());
        emitter.startDocument();
    }

    /**
    * Produce literal output. This is written as is, without any escaping.
    * The method is provided for Java applications that wish to output literal HTML text.
    * It is not used by the XSL system, which always writes using specific methods such as
    * writeStartTag().
    */

    public abstract void write(String s) throws TransformerException;

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param s The String to be output
    * @exception TransformerException for any failure
    */

    public abstract void writeContent(String s) throws TransformerException;

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception TransformerException for any failure
    */

    public abstract void writeContent(char[] chars, int start, int length)
        throws TransformerException;

    /**
    * Output an element start tag. <br>
    * The actual output of the tag is deferred until all attributes have been output
    * using writeAttribute().
    * @param nameCode The element name code
    */

    public abstract void writeStartTag(int nameCode) throws TransformerException;

	/**
	* Check that the prefix for an attribute is acceptable, returning a substitute
	* prefix if not. The prefix is acceptable unless a namespace declaration has been
	* written that assignes this prefix to a different namespace URI. This method
	* also checks that the attribute namespace has been declared, and declares it
	* if not.
	*/

	public abstract int checkAttributePrefix(int nameCode) throws TransformerException;

    /**
    * Output a namespace declaration. <br>
    * This is added to a list of pending namespaces for the current start tag.
    * If there is already another declaration of the same prefix, this one is
    * ignored.
    * Note that unlike SAX2 startPrefixMapping(), this call is made AFTER writing the start tag.
    * @param nscode The namespace code
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public abstract void writeNamespaceDeclaration(int nscode)
    throws TransformerException;

	/**
	* Copy a namespace node to the current element node
	* (Rules defined in XSLT 1.0 errata)
	*/

	public abstract void copyNamespaceNode(int nscode) throws TransformerException;

    /**
    * Test whether there is an open start tag. This determines whether it is
    * possible to write an attribute node at this point.
    */

    public abstract boolean thereIsAnOpenStartTag();

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.
    * @param nameCode The name code of the attribute
    * @param value The value of the attribute
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(int nameCode, String value) throws TransformerException {
        writeAttribute(nameCode, value, false);
    }

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.<br>
    * Before calling this, checkAttributePrefix() should be called to ensure the namespace
    * is OK.
    * @param name The name of the attribute
    * @param value The value of the attribute
    * @param noEscape True if it's known there are no special characters in the value. If
    * unsure, set this to false.
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public abstract void writeAttribute(int nameCode, String value, boolean noEscape)
    throws TransformerException;


    /**
    * Output an element end tag.<br>
    * @param nameCode The element name code
    */

    public abstract void writeEndTag(int nameCode) throws TransformerException;

    /**
    * Write a comment
    */

    public abstract void writeComment(String comment) throws TransformerException;

    /**
    * Write a processing instruction
    */

    public abstract void writePI(String target, String data) throws TransformerException;

    /**
    * Close the output
    */

    public abstract void close() throws TransformerException;


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

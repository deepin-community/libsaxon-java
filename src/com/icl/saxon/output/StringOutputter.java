package com.icl.saxon.output;
import com.icl.saxon.*;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;

/**
  * This class allows output to be generated. It channels output requests to an
  * Emitter which does the actual writing. This is a specialized and simplified version
  * that is used to handle xsl:attribute, xsl:comment, and xsl:processing-instruction.
  *
  * @author Michael H. Kay
  */

public final class StringOutputter extends Outputter {

    StringBuffer buffer;
    int ignoreElements = 0;
    ErrorListener errorListener = null;

	public StringOutputter(StringBuffer buffer) {
	    this.buffer = buffer;
	    // we need an Emitter to support xsl:copy-of; but they share the same string buffer.
        emitter = new StringEmitter(buffer);
	}

	public void setErrorListener(ErrorListener listener) {
	    errorListener = listener;
	}

    public void reset() throws TransformerException {
        // no-op
    }


    public Properties getOutputProperties() {
        return TextFragment.getProperties();
    }

    /**
    * Produce literal output. This is written as is, without any escaping.
    * The method is provided for Java applications that wish to output literal HTML text.
    * It is not used by the XSL system, which always writes using specific methods such as
    * writeStartTag().
    */

    public void write(String s) throws TransformerException {
        if (ignoreElements==0) {
            buffer.append(s);
        }
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param s The String to be output
    * @exception TransformerException for any failure
    */

    public void writeContent(String s) throws TransformerException {
        if (s==null) return;
        if (ignoreElements==0) {
            buffer.append(s);
        }
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception TransformerException for any failure
    */

    public void writeContent(char[] chars, int start, int length) throws TransformerException {
        if (ignoreElements==0) {
            buffer.append(chars, start, length);
        }
    }

    /**
    * Output an element start tag. With this outputter, this is a recoverable error.<br>
    * @param nameCode The element name code
    */

    public void writeStartTag(int nameCode) throws TransformerException {
        reportRecoverableError();
        ignoreElements++;
    }

    private void reportRecoverableError() throws TransformerException {
        if (errorListener!=null) {
            errorListener.warning(
                new TransformerException(
                "Non-text output nodes are ignored when writing an attribute, comment, or PI"));
        }
    }

	/**
	* Check that the prefix for an attribute is acceptable, returning a substitute
	* prefix if not. The prefix is acceptable unless a namespace declaration has been
	* written that assignes this prefix to a different namespace URI. This method
	* also checks that the attribute namespace has been declared, and declares it
	* if not.
	*/

	public int checkAttributePrefix(int nameCode) throws TransformerException {
        return nameCode;
    }

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

    public void writeNamespaceDeclaration(int nscode)
    throws TransformerException {
        // no-op
    }

	/**
	* Copy a namespace node to the current element node
	* (Rules defined in XSLT 1.0 errata)
	*/

	public void copyNamespaceNode(int nscode) throws TransformerException {
        // no-op
    }

    /**
    * Test whether there is an open start tag. This determines whether it is
    * possible to write an attribute node at this point.
    */

    public boolean thereIsAnOpenStartTag() {
        return false;
    }

    /**
    * Output an attribute value. <br>
    * No-op in this implementation.
    * @param name The name of the attribute
    * @param value The value of the attribute
    * @param noEscape True if it's known there are no special characters in the value. If
    * unsure, set this to false.
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(int nameCode, String value, boolean noEscape) throws TransformerException {
        reportRecoverableError();
    }


    /**
    * Output an element end tag.<br>
    * @param nameCode The element name code
    */

    public void writeEndTag(int nameCode) throws TransformerException {
        ignoreElements--;
    }

    /**
    * Write a comment.
    * No-op in this implementation
    */

    public void writeComment(String comment) throws TransformerException {
        reportRecoverableError();
    }

    /**
    * Write a processing instruction
    * No-op in this implementation
    */

    public void writePI(String target, String data) throws TransformerException {
        reportRecoverableError();
    }

    /**
    * Close the output
    */

    public void close() throws TransformerException {
        // no-op
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

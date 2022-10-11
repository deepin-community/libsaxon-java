package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.ProxyEmitter;
import com.icl.saxon.pattern.*;
import com.icl.saxon.tree.ElementImpl;

import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;


/**
  * The StylesheetStripper refines the Stripper class to do stripping of
  * whitespace nodes on a stylesheet. This is handled specially (a) because
  * it is done at compile time, so there is no Controller or Context available, and (b)
  * because the rules are very simple
  * @author Michael H. Kay
  */

public class StylesheetStripper extends Stripper
{

    int xsl_text;   // fingerprint of name "xsl:text"

	/**
	* Set the rules appropriate for whitespace-stripping in a stylesheet
	*/

	public void setStylesheetRules(NamePool namePool) {
	    xsl_text = namePool.getFingerprint(Namespace.XSLT, "text");
    }

    /**
    * Decide whether an element is in the set of white-space preserving element types
    * @param uri The namespace URI of the element name
    * @param localname The local part of the element name
    * @return true if the element is in the set of white-space preserving element types
    */

    public boolean isSpacePreserving(int nameCode) {
        return (nameCode & 0xfffff) == xsl_text;
    }


}   // end of class StylesheetStripper

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

package com.icl.saxon.expr;
import javax.xml.transform.TransformerException;

/**
* XPathException is used to indicate an error in an XPath expression.
* We don't distinguish compile-time errors from run-time errors because there are
* too many overlaps, e.g. constant expressions can be evaluated at compile-time, and
* expressions can be optimised either at compile-time or at run-time.
*/

public class XPathException extends TransformerException {

    public XPathException(String message) {
        super(message);
    }

    public XPathException(Exception err) {
        super(err);
    }

    public XPathException(String message, Exception err) {
        super(message, err);
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

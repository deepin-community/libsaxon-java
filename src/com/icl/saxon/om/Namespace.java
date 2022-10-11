package com.icl.saxon.om;

/**
  * An object representing a Namespace
  * @author Michael H. Kay
  */

public class Namespace {

	/**
	* Null namespace
	*/

	public static final String NULL = "";
	public static final short NULL_CODE = 0;

    /**
    * Fixed namespace name for XML: "http://www.w3.org/XML/1998/namespace".
    */
    public static final String XML = "http://www.w3.org/XML/1998/namespace";
    public static final short XML_CODE = 1;

    /**
    * Fixed namespace name for XMLNS: "http://www.w3.org/2000/xmlns/". Actually, no namespace URL
    * is defined for the "xmlns" prefix, in the namespaces recommendation, this value
    * is taken from the DOM Level 2 spec.
    */
    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";

    /**
    * Fixed namespace name for XSLT: "http://www.w3.org/1999/XSL/Transform"
    */
    public static final String XSLT = "http://www.w3.org/1999/XSL/Transform";
    public static final short XSLT_CODE = 2;

    /**
    * Fixed namespace name for SAXON: "http://icl.com/saxon"
    */
    public static final String SAXON = "http://icl.com/saxon";
    public static final short SAXON_CODE = 3;

    /**
    * Fixed namespace name for SAXON DTD extension: "http://icl.com/saxon/dtd"
    */
    public static final String DTD = "http://icl.com/saxon/dtd";

    /**
    * Fixed namespace name for EXSLT/Common: "http://exslt.org/common"
    */
    public static final String EXSLT_COMMON = "http://exslt.org/common";

    /**
    * Fixed namespace name for EXSLT/math: "http://exslt.org/math"
    */
    public static final String EXSLT_MATH = "http://exslt.org/math";

    /**
    * Fixed namespace name for EXSLT/sets: "http://exslt.org/sets"
    */
    public static final String EXSLT_SETS = "http://exslt.org/sets";

    /**
    * Fixed namespace name for EXSLT/date: "http://exslt.org/dates-and-times"
    */
    public static final String EXSLT_DATES_AND_TIMES = "http://exslt.org/dates-and-times";

    /**
    * Fixed namespace name for EXSLT/functions: "http://exslt.org/functions"
    */
    public static final String EXSLT_FUNCTIONS = "http://exslt.org/functions";
    public static final short EXSLT_FUNCTIONS_CODE = 4;
    /**
    * Recognize the Microsoft namespace so we can give a suitably sarcastic error message
    */

    public static final String MICROSOFT_XSL = "http://www.w3.org/TR/WD-xsl";

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

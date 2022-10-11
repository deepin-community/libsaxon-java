package com.icl.saxon.om;


/**
  * This class, a remnant of its former self, exists to contain some static methods
  * for validating the syntax of names.<br>
  *
  * @author Michael H. Kay
  */

public abstract class Name {

    /**
    * Validate whether a given string constitutes a valid NCName, as defined in XML Namespaces
    */

    public static boolean isNCName(String name) {
        return XMLChar.isValidNCName(name);
    }

    /**
    * Validate whether a given string constitutes a valid QName, as defined in XML Namespaces
    */

    public static boolean isQName(String name) {
        int colon = name.indexOf(':');
        if (colon<0) return isNCName(name);
        if (colon==0 || colon==name.length()-1) return false;
        if (!isNCName(name.substring(0, colon))) return false;
        if (!isNCName(name.substring(colon+1))) return false;
        return true;
    }

	/**
	* Extract the prefix from a QName. Note, the QName is assumed to be valid.
	*/

	public final static String getPrefix(String qname) {
		int colon = qname.indexOf(':');
		if (colon<0) {
			return "";
		}
		return qname.substring(0, colon);
	}

	/**
	* Extract the local name from a QName. The QName is assumed to be valid.
	*/

	public final static String getLocalName(String qname) {
		int colon = qname.indexOf(':');
		if (colon<0) {
			return qname;
		}
		return qname.substring(colon+1);
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

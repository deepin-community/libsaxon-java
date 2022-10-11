package com.icl.saxon.charcode;
import com.icl.saxon.Loader;
import javax.xml.transform.OutputKeys;
import java.util.Properties;

/**
* This class creates a CharacterSet object for a given named encoding.
*/


public class CharacterSetFactory {


    /**
    * Make a CharacterSet appropriate to the encoding
    */

    public static CharacterSet getCharacterSet(Properties details) {
        String encoding = details.getProperty(OutputKeys.ENCODING);
        if (encoding==null) encoding = "UTF8";
        if (encoding.equalsIgnoreCase("utf-8")) encoding = "UTF8";    // needed for Microsoft Java VM

        CharacterSet charSet = makeCharacterSet(encoding);
        if (charSet==null) {
        	charSet = new ASCIICharacterSet();
        }
        return charSet;
    }

	public static CharacterSet makeCharacterSet(String encoding) {

        if (encoding.equalsIgnoreCase("ASCII")) {
            return new ASCIICharacterSet();
        } else if (encoding.equalsIgnoreCase("US-ASCII")) {
            return new ASCIICharacterSet();
        } else if (encoding.equalsIgnoreCase("iso-8859-1")) {
            return new Latin1CharacterSet();
        } else if (encoding.equalsIgnoreCase("ISO8859_1")) {
            return new Latin1CharacterSet();
        } else if (encoding.equalsIgnoreCase("iso-8859-2")) {
            return new Latin2CharacterSet();
        } else if (encoding.equalsIgnoreCase("ISO8859_2")) {
            return new Latin2CharacterSet();
        } else if (encoding.equalsIgnoreCase("utf-8")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("UTF8")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("utf-16")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("utf16")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("KOI8-R")) {
            return new KOI8RCharacterSet();
        } else if (encoding.equalsIgnoreCase("cp1251")) {
            return new CP1251CharacterSet();
        } else if (encoding.equalsIgnoreCase("windows-1251")) {
            return new CP1251CharacterSet();
        } else if (encoding.equalsIgnoreCase("cp1250")) {
            return new CP1250CharacterSet();
        } else if (encoding.equalsIgnoreCase("windows-1250")) {
            return new CP1250CharacterSet();
        } else if (encoding.equalsIgnoreCase("cp852")) {
            return new CP852CharacterSet();

        } else {
            String csname = null;
    		try {
    		    // Allow an alias for the character set to be specified as a system property
                csname = System.getProperty(OutputKeys.ENCODING + "." + encoding);
    			if (csname == null) {
    			    csname = encoding;
    			}
		    	Object obj = Loader.getInstance(csname);
		        if (obj instanceof PluggableCharacterSet) {
		            return (PluggableCharacterSet)obj;
		        }
		    } catch (Exception err) {
		        System.err.println("Failed to load " + csname);
		    }
	    }

    	return null;

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
// Michael Kay  (michael.h.kay@ntlworld.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

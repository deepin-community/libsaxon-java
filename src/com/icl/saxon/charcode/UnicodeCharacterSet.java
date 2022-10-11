package com.icl.saxon.charcode;

/**
* This class defines properties of the Unicode character set
*/

public final class UnicodeCharacterSet implements CharacterSet {
    
    private static UnicodeCharacterSet theInstance = new UnicodeCharacterSet();
    
    public static UnicodeCharacterSet getInstance() {
        return theInstance;
    }

    public boolean inCharset(int c) {
        return true;
        
        // old code: return true unless the character is one half of a surrogate pair (D800 to DFFF)
        // this forces such characters to be output as numeric character references, but doesn't work
        // for method="text"
        // return (c<55296 || c>57343);

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
// Aleksei Makarov [makarov@iitam.omsk.net.ru]
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved. 
//
// Contributor(s): none. 
//

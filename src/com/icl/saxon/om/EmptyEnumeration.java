package com.icl.saxon.om;

/**
* EmptyEnumeration: an enumeration of an empty node-set
*/

public class EmptyEnumeration implements AxisEnumeration {

    private static EmptyEnumeration instance = new EmptyEnumeration();

    /**
    * Return an instance of an EmptyEnumeration
    */

    public static EmptyEnumeration getInstance() {
        return instance;
    }

    public boolean hasMoreElements() {
        return false;
    }

    public NodeInfo nextElement() {
        return null;
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted() {
        return true;
    }

    public boolean isPeer() {
        return true;
    }

    public int getLastPosition() {
        return 0;
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

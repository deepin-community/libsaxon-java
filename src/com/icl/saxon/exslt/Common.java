package com.icl.saxon.exslt;
import com.icl.saxon.expr.*;

/**
* This class implements extension functions in the
* http://exslt.org/common namespace. <p>
*/



public abstract class Common  {

    /**
    * Convert a result tree fragment to a node-set.
    */

    public static NodeSetValue nodeSet(Value frag) throws XPathException {
        if (frag instanceof SingletonNodeSet) {
            ((SingletonNodeSet)frag).allowGeneralUse();
        }
        if (frag instanceof NodeSetValue) {
            return (NodeSetValue)frag;
        } else {
            throw new XPathException("exslt:node-set(): argument must be a node-set or tree");
        }
    }

    /**
    * Return the type of the supplied value: "node-set", "string", "number", "boolean",
    * "RTF", or "external"
    */

    public static String objectType(Value value) {
        if (value instanceof FragmentValue || value instanceof TextFragmentValue) {
            return "RTF";
        } else if (value instanceof NodeSetValue) {
            return "node-set";
        } else if (value instanceof StringValue) {
            return "string";
        } else if (value instanceof NumericValue) {
            return "number";
        } else if (value instanceof BooleanValue) {
            return "boolean";
        } else {
            return "external";
        }
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

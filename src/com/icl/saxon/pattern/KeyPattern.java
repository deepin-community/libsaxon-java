package com.icl.saxon.pattern;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.expr.XPathException;

import java.util.*;

/**
* A KeyPattern is a pattern of the form key(keyname, keyvalue)
*/

public final class KeyPattern extends Pattern {

    private int keyfingerprint;          // the fingerprint of the key name
    private String keyvalue;                // the value of the key

    /**
    * Constructor
    * @param name the name of the key
    * @param value the value of the key
    */

    public KeyPattern(int namecode, String value) {
        keyfingerprint = namecode & 0xfffff;
        keyvalue = value;
    }

    /**
    * Determine whether this Pattern matches the given Node.
    * @param e The NodeInfo representing the Element or other node to be tested against the Pattern
    * @return true if the node matches the Pattern, false otherwise
    */

    public boolean matches(NodeInfo e, Context c) throws XPathException {
        DocumentInfo doc = e.getDocumentRoot();
        Controller controller = c.getController();
        KeyManager km = controller.getKeyManager();
        NodeEnumeration nodes = km.selectByKey(keyfingerprint, doc, keyvalue, controller);
        while(nodes.hasMoreElements()) {
            if (nodes.nextElement().isSameNodeInfo(e)) {
                return true;
            }
        }
        return false;
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

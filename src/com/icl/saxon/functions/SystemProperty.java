package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.expr.*;

import java.util.*;
import java.lang.Math;
import java.text.*;



public class SystemProperty extends Function {

    public String getName() {
        return "system-property";
    };

    /**
    * Determine the data type of the expression
    * @return Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.ANY;
    }

    /**
    * Validate and simplify
    */

    public Expression simplify() throws XPathException {
        checkArgumentCount(1, 1);
        argument[0] = argument[0].simplify();
        if (argument[0] instanceof Value) {
            return evaluate(null);
        }
        return this;
    }

    /**
    * Evaluate the function
    */

    public Value evaluate(Context context) throws XPathException {
        String name = argument[0].evaluateAsString(context);
        if (!Name.isQName(name)) {
        	throw new XPathException("Argument " + name + " is not a valid QName");
        }
        String prefix = Name.getPrefix(name);
        String lname = Name.getLocalName(name);
        String uri;
        if (prefix.equals("")) {
        	uri = "";
        } else {
        	uri = getStaticContext().getURIForPrefix(prefix);
        }
        return getProperty(uri, lname);
    }

    /**
    * Here's the real code:
    */

    public static Value getProperty(String uri, String local) {
        if (uri.equals(Namespace.XSLT)) {
            if (local.equals("version"))
                return new NumericValue(Version.getXSLVersion());
            if (local.equals("vendor"))
                return new StringValue(Version.getProductName());
            if (local.equals("vendor-url"))
                return new StringValue(Version.getWebSiteAddress());
            return new StringValue("");

        } else if (uri.equals("")) {
	        String val = System.getProperty(local);
	        if (val==null) val="";
	        return new StringValue(val);
	    } else {
	    	return new StringValue("");
	    }
    }

    /**
    * Get dependencies
    */

    public int getDependencies() {
        return argument[0].getDependencies();
    }

    /**
    * Remove dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        SystemProperty f = new SystemProperty();
        f.addArgument(argument[0].reduce(dep, c));
        f.setStaticContext(getStaticContext());
        return f;
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

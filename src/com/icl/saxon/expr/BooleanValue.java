package com.icl.saxon.expr;
import com.icl.saxon.*;


/**
* A boolean XPath value
*/

public final class BooleanValue extends Value {
    private boolean value;

    /**
    * Constructor: create a boolean value
    * @param value the initial value, true or false
    */

    public BooleanValue(boolean value) {
        this.value = value;
    }

    /**
    * Convert to string
    * @return "true" or "false"
    */

    public String asString() {
        return (value ? "true" : "false");
    }

    /**
    * Convert to number
    * @return 1 for true, 0 for false
    */

    public double asNumber() {
        return (value ? 1 : 0);
    }

    /**
    * Convert to boolean (null operation)
    * @return the value
    */

    public boolean asBoolean() {
        return value;
    }


    /**
    * Determine the data type of the exprssion
    * @return Value.BOOLEAN,
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Get conversion preference for this value to a Java class. A low result
    * indicates higher preference.
    */

    public int conversionPreference(Class required) {

        if (required==Object.class) return 50;
        if (required.isAssignableFrom(BooleanValue.class)) return 0;

        if (required==boolean.class) return 0;
        if (required==Boolean.class) return 0;
        if (required==byte.class) return 3;
        if (required==Byte.class) return 4;
        if (required==char.class) return Integer.MAX_VALUE;
        if (required==Character.class) return Integer.MAX_VALUE;
        if (required==double.class) return 3;
        if (required==Double.class) return 4;
        if (required==float.class) return 3;
        if (required==Float.class) return 4;
        if (required==int.class) return 3;
        if (required==Integer.class) return 4;
        if (required==long.class) return 3;
        if (required==Long.class) return 4;
        if (required==short.class) return 3;
        if (required==Short.class) return 4;
        if (required==String.class) return 2;

        return Integer.MAX_VALUE;
    }

    /**
    * Convert to Java object (for passing to external functions)
    */

    public Object convertToJava(Class target) throws XPathException {
        if (target==Object.class) {
            return new Boolean(value);
        } else if (target.isAssignableFrom(BooleanValue.class)) {
            return this;
        } else if (target==boolean.class) {
            return new Boolean(value);
        } else if (target==Boolean.class) {
            return new Boolean(value);
        } else if (target==Object.class) {
            return new Boolean(value);
        } else if (target==String.class) {
            return asString();
        } else if (target==double.class) {
            return new Double(asNumber());
        } else if (target==Double.class) {
            return new Double(asNumber());
        } else if (target==float.class) {
            return new Float(asNumber());
        } else if (target==Float.class) {
            return new Float(asNumber());
        } else if (target==long.class) {
            return new Long((value?1:0));
        } else if (target==Long.class) {
            return new Long((value?1:0));
        } else if (target==int.class) {
            return new Integer((value?1:0));
        } else if (target==Integer.class) {
            return new Integer((value?1:0));
        } else if (target==short.class) {
            return new Short((short)(value?1:0));
        } else if (target==Short.class) {
            return new Short((short)(value?1:0));
        } else if (target==byte.class) {
            return new Byte((byte)(value?1:0));
        } else if (target==Byte.class) {
            return new Byte((byte)(value?1:0));
        } else {
            throw new XPathException("Conversion of boolean to " + target.getName() +
                        " is not supported");
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "boolean (" + asString() + ")" );
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


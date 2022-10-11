package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.*;


/**
* An XPath value that encapsulates a Java object. Such a value can only be obtained by
* calling an extension function that returns it.
*/

public class ObjectValue extends Value {
    private Object value;

    /**
    * Constructor
    * @value the object to be encapsulated
    */

    public ObjectValue(Object object) {
        this.value = object;
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    public String asString() {
        return (value==null ? "" : value.toString());
    }

    /**
    * Get the value as a number
    * @return the numeric value
    */

    public double asNumber() {
        return (value==null ? Double.NaN : Value.stringToNumber(value.toString()));
    }

    /**
    * Convert the value to a boolean
    * @return the boolean value
    */

    public boolean asBoolean() {
        return (value==null ? false : value.toString().length() > 0);
    }

    /**
    * Determine the data type of the expression
    * @return Value.OBJECT
    */

    public int getDataType() {
        return Value.OBJECT;
    }

    /**
    * Get the encapsulated object
    */

    public Object getObject() {
        return value;
    }

    /**
    * Determine if two ObjectValues are equal
    */

    public boolean equals(ObjectValue other) {
        return this.value.equals(other.value);
    }

    /**
    * Get conversion preference for this value to a Java class. A low result
    * indicates higher preference.
    */

    public int conversionPreference(Class required) {
        if (required==boolean.class) return Integer.MAX_VALUE; // don't know why
        if (required==Boolean.class) return Integer.MAX_VALUE; // don't know why
        if (required==byte.class) return 4;
        if (required==Byte.class) return 5;
        if (required==char.class) return 2;
        if (required==Character.class) return 3;
        if (required==double.class) return 4;
        if (required==Double.class) return 5;
        if (required==float.class) return 4;
        if (required==Float.class) return 5;
        if (required==int.class) return 4;
        if (required==Integer.class) return 5;
        if (required==long.class) return 4;
        if (required==Long.class) return 5;
        if (required==short.class) return 4;
        if (required==Short.class) return 5;
        if (required==String.class) return 1;
        if (required==Object.class) return 8;
        if (required==value.getClass()) return -1;
            // this departs from the draft spec, but is useful to discriminate a method
            // for the exact class from one for a superclass.
        if (required.isAssignableFrom(value.getClass())) return 0;
        return Integer.MAX_VALUE;
    }

    /**
    * Convert to Java object (for passing to external functions)
    */

    public Object convertToJava(Class target) throws XPathException {

        if (value==null) return null;

        if (target.isAssignableFrom(value.getClass())) {
            return value;
        } else if (target==Value.class || target==ObjectValue.class) {
            return this;
        } else if (target==boolean.class) {
            return new Boolean(asBoolean());   // technically not allowed
        } else if (target==Boolean.class) {
            return new Boolean(asBoolean());   // technically not allowed
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
            return new Long((long)asNumber());
        } else if (target==Long.class) {
            return new Long((long)asNumber());
        } else if (target==int.class) {
            return new Integer((int)asNumber());
        } else if (target==Integer.class) {
            return new Integer((int)asNumber());
        } else if (target==short.class) {
            return new Short((short)asNumber());
        } else if (target==Short.class) {
            return new Short((short)asNumber());
        } else if (target==byte.class) {
            return new Byte((byte)asNumber());
        } else if (target==Byte.class) {
            return new Byte((byte)asNumber());
        } else if (target==char.class || target==Character.class) {
            String s = asString();
            if (s.length()==1) {
                return new Character(s.charAt(0));
            } else {
                throw new XPathException("Cannot convert string to Java char unless length is 1");
            }
        } else {
            throw new XPathException("Conversion of external object to " + target.getName() +
                        " is not supported");
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "** external object **" );
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


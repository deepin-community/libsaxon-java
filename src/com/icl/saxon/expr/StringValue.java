package com.icl.saxon.expr;
import com.icl.saxon.*;


/**
* A string value
*/

public final class StringValue extends Value {
    private String value;   // may be zero-length, will never be null

    /**
    * Constructor
    * @param value the String value. Null is taken as equivalent to "".
    */

    public StringValue(String value) {
        this.value = (value==null ? "" : value);
    }

    /**
    * Get the string value as a String
    */

    public String asString() {
        return value;
    }

    /**
    * Convert the string value to a number
    */

    public double asNumber() {
        return Value.stringToNumber(value);
    }

    /**
    * Convert the string value to a boolean
    * @return false if the string value is zero-length, true otherwise
    */

    public boolean asBoolean() {
        return (value.length()>0);
    }

    /**
    * Return the type of the expression (if known)
    * @return Value.STRING (always)
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Get the length of this string, as defined in XPath. This is not the same as the Java length,
    * as a Unicode surrogate pair counts as a single character
    */

    public int getLength() {
        return getLength(value);
    }

    /**
    * Get the length of a string, as defined in XPath. This is not the same as the Java length,
    * as a Unicode surrogate pair counts as a single character.
    * @param s The string whose length is required
    */

    public static int getLength(String s) {
        int n = 0;
        for (int i=0; i<s.length(); i++) {
            int c = (int)s.charAt(i);
            if (c<55296 || c>56319) n++;    // don't count high surrogates, i.e. D800 to DBFF
        }
        return n;
    }

    /**
    * Expand a string containing surrogate pairs into an array of 32-bit characters
    */

    public static int[] expand(String s) {
        int[] array = new int[getLength(s)];
        int o=0;
        for (int i=0; i<s.length(); i++) {
            int charval;
            int c = s.charAt(i);
            if (c>=55296 && c<=56319) {
                // we'll trust the data to be sound
                charval = ((c - 55296) * 1024) + ((int)s.charAt(i+1) - 56320) + 65536;
                i++;
            } else {
                charval = c;
            }
            array[o++] = charval;
        }
        return array;
    }

    /**
    * Determine if two StringValues are equal
    */

    public boolean equals(StringValue other) {
        return this.value.equals(other.value);
    }

    /**
    * Get conversion preference for this value to a Java class. A low result
    * indicates higher preference.
    */

    public int conversionPreference(Class required) {

        if (required==Object.class) return 50;
        if (required.isAssignableFrom(StringValue.class)) return 0;

        if (required==boolean.class) return 6;
        if (required==Boolean.class) return 7;
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
        if (required==String.class) return 0;
        return Integer.MAX_VALUE;
    }


    /**
    * Convert to Java object (for passing to external functions)
    */

    public Object convertToJava(Class target) throws XPathException {
        if (target==Object.class) {
            return value;
        } else if (target.isAssignableFrom(StringValue.class)) {
            return this;
        } else if (target==boolean.class) {
            return new Boolean(asBoolean());
        } else if (target==Boolean.class) {
            return new Boolean(asBoolean());
        } else if (target==String.class) {
            return value;
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
            if (value.length()==1) {
                return new Character(value.charAt(0));
            } else {
                throw new XPathException("Cannot convert string to Java char unless length is 1");
            }
        } else {
            throw new XPathException("Conversion of string to " + target.getName() +
                        " is not supported");
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "string (\"" + value + "\")" );
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


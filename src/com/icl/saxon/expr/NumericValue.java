package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.functions.*;

import java.text.*;

/**
* A numeric (floating point) value
*/

public final class NumericValue extends Value {
    private double value;

    /**
    * Constructor supplying a double
    * @value the value of the NumericValue
    */

    public NumericValue(double value) {
        this.value = value;
    }

    /**
    * Constructor supplying a String
    * @s the numeric value expressed as a String
    */

    public NumericValue(String s) {
        this.value = Value.stringToNumber(s);
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    // Algorithm used up to 5.3.1

    public String asStringOLD() {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return (value>0 ? "Infinity" : "-Infinity");
        if (value==0.0) return "0";

        double absvalue = Math.abs(value);
        StringBuffer sb = new StringBuffer();
        if (value<0) sb.append('-');
        int offset = (value<0 ? 1: 0);
        double intpart = Math.floor(absvalue);
        double fraction = absvalue - intpart;
        if (intpart>=1) {
            while (intpart>=1) {
                int nextdigit = (int)(intpart % 10);
                char digit = (char)(nextdigit + '0');
                sb.insert(offset, digit);
                intpart = Math.floor(intpart / 10);
            }

        } else {
            sb.append('0');
        }
        if (fraction > 0) {
            sb.append('.');
            while (fraction > 0) {
                double next = fraction * 10;
                if (next<1.000000000001 && next>0.999999999999) next=1.0;
                double nextdigit = Math.floor(next);
                char digit = (char)((int)nextdigit + '0');
                sb.append(digit);
                fraction = next % 1.0;
            }
        }
        return sb.toString();
    }

    /**
    * Get the value as a String
    * @return a String representation of the value
    */

    // Code copied from James Clark's xt

    public String asString() {
        if (!Double.isInfinite(value)
	        && (value >= (double)(1L << 53)
	                || -value >= (double)(1L << 53))) {
            return new java.math.BigDecimal(value).toString();
        }
        String s = Double.toString(value);
        int len = s.length();
        if (s.charAt(len - 2) == '.' && s.charAt(len - 1) == '0') {
            s = s.substring(0, len - 2);
            if (s.equals("-0"))
                return "0";
            return s;
        }
        int e = s.indexOf('E');
        if (e < 0)
            return s;
        int exp = Integer.parseInt(s.substring(e + 1));
        String sign;
        if (s.charAt(0) == '-') {
            sign = "-";
            s = s.substring(1);
            --e;
        }
        else
            sign = "";

        int nDigits = e - 2;
        if (exp >= nDigits) {
            return sign + s.substring(0, 1) + s.substring(2, e) + zeros(exp - nDigits);
        } else if (exp > 0) {
            return sign + s.substring(0, 1) + s.substring(2, 2 + exp) + "." + s.substring(2 + exp, e);
        } else {
            // following line added at 6.5.3
            while (s.charAt(e-1) == '0') e--;
            return sign + "0." + zeros(-1 - exp) + s.substring(0, 1) + s.substring(2, e);
        }
    }

    static private String zeros(int n) {
        char[] buf = new char[n];
        for (int i = 0; i < n; i++)
            buf[i] = '0';
        return new String(buf);
    }

    /**
    * Get the value as a number
    * @return the numeric value
    */

    public double asNumber() {
        return value;
    }

    /**
    * Convert the value to a boolean
    * @return false if zero, true otherwise
    */

    public boolean asBoolean() {
        return (value!=0.0 && !Double.isNaN(value));
    }

    /**
    * Determine the data type of the exprssion, if possible
    * @return one of the values Value.STRING, Value.BOOLEAN, Value.NUMBER, Value.NODESET,
    * Value.FRAGMENT, or Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        return Value.NUMBER;
    }


    /**
    * Get conversion preference for this value to a Java class. A low result
    * indicates higher preference.
    */

    public int conversionPreference(Class required) {

        if (required==Object.class) return 17;
        if (required.isAssignableFrom(NumericValue.class)) return 0;

        if (required==boolean.class) return 14;
        if (required==Boolean.class) return 15;
        if (required==byte.class) return 12;
        if (required==Byte.class) return 13;
        if (required==char.class) return 10;
        if (required==Character.class) return 11;
        if (required==double.class) return 0;
        if (required==Double.class) return 1;
        if (required==float.class) return 2;
        if (required==Float.class) return 3;
        if (required==int.class) return 6;
        if (required==Integer.class) return 7;
        if (required==long.class) return 4;
        if (required==Long.class) return 5;
        if (required==short.class) return 8;
        if (required==Short.class) return 9;
        if (required==String.class) return 16;
        return Integer.MAX_VALUE;
    }

    /**
    * Convert to Java object (for passing to external functions)
    */

    public Object convertToJava(Class target) throws XPathException {
        if (target==Object.class) {
            return new Double(value);
        } else if (target.isAssignableFrom(NumericValue.class)) {
            return this;
        } else if (target==boolean.class) {
            return new Boolean(asBoolean());
        } else if (target==Boolean.class) {
            return new Boolean(asBoolean());
        } else if (target==String.class) {
            return asString();
        } else if (target==double.class) {
            return new Double(value);
        } else if (target==Double.class) {
            return new Double(value);
        } else if (target==float.class) {
            return new Float(value);
        } else if (target==Float.class) {
            return new Float(value);
        } else if (target==long.class) {
            return new Long((long)value);
        } else if (target==Long.class) {
            return new Long((long)value);
        } else if (target==int.class) {
            return new Integer((int)value);
        } else if (target==Integer.class) {
            return new Integer((int)value);
        } else if (target==short.class) {
            return new Short((short)value);
        } else if (target==Short.class) {
            return new Short((short)value);
        } else if (target==byte.class) {
            return new Byte((byte)value);
        } else if (target==Byte.class) {
            return new Byte((byte)value);
        } else if (target==char.class) {
            return new Character((char)value);
        } else if (target==Character.class) {
            return new Character((char)value);
        } else {
            throw new XPathException("Conversion of number to " + target.getName() +
                        " is not supported");
        }
    }

    /**
    * Diagnostic print of expression structure
    */

    public void display(int level) {
        System.err.println(indent(level) + "number (" + asString() + ")" );
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
// The Original Code is: all this file except the asStringXT() and zeros() methods (not currently used).
//
// The Initial Developer of the Original Code is
// Michael Kay
//
// Portions created by (xt) are Copyright (C) (James Clark). All Rights Reserved.
//
// Contributor(s): none.
//


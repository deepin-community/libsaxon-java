package com.icl.saxon.exslt;
import com.icl.saxon.expr.*;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NodeEnumeration;
import java.util.Vector;

/**
* This class implements extension functions in the
* http://exslt.org/math namespace. <p>
*/

public abstract class Math  {

    /**
    * Get the maximum numeric value of the string-value of each of a set of nodes
    */

    public static double max (NodeEnumeration nsv) throws XPathException {
        double max = Double.NEGATIVE_INFINITY;
        while (nsv.hasMoreElements()) {
            double x = Value.stringToNumber(nsv.nextElement().getStringValue());
            if (Double.isNaN(x)) return x;
            if (x>max) max = x;
        }
        return max;
    }


    /**
    * Get the minimum numeric value of the string-value of each of a set of nodes
    */

    public static double min (NodeEnumeration nsv) throws XPathException {
        double min = Double.POSITIVE_INFINITY;
        while (nsv.hasMoreElements()) {
            double x = Value.stringToNumber(nsv.nextElement().getStringValue());
            if (Double.isNaN(x)) return x;
            if (x<min) min = x;
        }
        return min;
    }


    /**
    * Get the nodes with maximum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue highest (Context c, NodeEnumeration nsv) throws XPathException {
        double max = Double.NEGATIVE_INFINITY;
        Vector highest = new Vector();
        while (nsv.hasMoreElements()) {
            NodeInfo node = nsv.nextElement();
            double x = Value.stringToNumber(node.getStringValue());
            if (Double.isNaN(x)) return new EmptyNodeSet();
            if (x==max) {
                highest.addElement(node);
            } else if (x>max) {
                max = x;
                highest.removeAllElements();
                highest.addElement(node);
            }
        }
        return new NodeSetExtent(highest, c.getController());
    }



    /**
    * Get the node with minimum numeric value of the string-value of each of a set of nodes
    */

    public static NodeSetValue lowest (Context c, NodeEnumeration nsv) throws XPathException {
        double min = Double.POSITIVE_INFINITY;
        Vector lowest = new Vector();
        while (nsv.hasMoreElements()) {
            NodeInfo node = nsv.nextElement();
            double x = Value.stringToNumber(node.getStringValue());
            if (Double.isNaN(x)) return new EmptyNodeSet();
            if (x==min) {
                lowest.addElement(node);
            } else if (x<min) {
                min = x;
                lowest.removeAllElements();
                lowest.addElement(node);
            }
        }
        return new NodeSetExtent(lowest, c.getController());
    }

    /**
    * Get the absolute value of a numeric value (SStL)
    */

    public static double abs (double x) throws XPathException {

        return java.lang.Math.abs(x);
    }

    /**
    * Get the square root of a numeric value (SStL)
    */

    public static double sqrt (double x) throws XPathException {


        return java.lang.Math.sqrt(x);
    }

    /**
    * Get the power of two numeric values  (SStL)
    */

    public static double power (double x, double y) throws XPathException {

        return java.lang.Math.pow(x,y);
    }

    /**
    * Get a named constant to a given precision  (SStL)
    */

    public static double constant (String name, double precision) throws XPathException {
        //PI, E, SQRRT2, LN2, LN10, LOG2E, SQRT1_2

    String con=new String();

    if (name.equals("PI")) {
        con="3.1415926535897932384626433832795028841971693993751";
    } else if (name.equals("E")) {
        con="2.71828182845904523536028747135266249775724709369996";
    } else if (name.equals("SQRRT2")) {
        con="1.41421356237309504880168872420969807856967187537694";
    } else if (name.equals("LN2")) {
        con="0.69314718055994530941723212145817656807550013436025";
    } else if (name.equals("LN10")) {
        con="2.302585092994046";
    } else if (name.equals("LOG2E")) {
        con="1.4426950408889633";
    } else if (name.equals("SQRT1_2")) {
        con="0.7071067811865476";
    }
        int x = (int) precision;
        String returnVal=con.substring(0,x+2);
        double rV=new Double(returnVal).doubleValue();
        return rV;
    }

    /**
    * Get the logarithm of a numeric value (SStL)
    */

    public static double log (double x) throws XPathException {

        return java.lang.Math.log(x);
    }

    /**
    * Get a random numeric value (SStL)
    */

    public static double random () throws XPathException {


        return java.lang.Math.random();
    }

    /**
    * Get the sine of a numeric value (SStL)
    */

    public static double sin (double x) throws XPathException {

        return java.lang.Math.sin(x);
    }

    /**
    * Get the cosine of a numeric value (SStL)
    */

    public static double cos (double x) throws XPathException {

        return java.lang.Math.cos(x);
    }

    /**
    * Get the tangent of a numeric value  (SStL)
    */

    public static double tan (double x) throws XPathException {

        return java.lang.Math.tan(x);
    }

    /**
    * Get the arcsine of a numeric value  (SStL)
    */

    public static double asin (double x) throws XPathException {

        return java.lang.Math.asin(x);
    }

    /**
    * Get the arccosine of a numeric value  (SStL)
    */

    public static double acos (double x) throws XPathException {

        return java.lang.Math.acos(x);
    }

    /**
    * Get the arctangent of a numeric value  (SStL)
    */

    public static double atan (double x) throws XPathException {

        return java.lang.Math.atan(x);
    }

    /**
    * Converts rectangular coordinates to polar  (SStL)
    */

    public static double atan2 (double x, double y) throws XPathException {

        return java.lang.Math.atan2(x,y);
    }

    /**
    * Get the exponential of a numeric value  (SStL)
    */

    public static double exp (double x) throws XPathException {

        return java.lang.Math.exp(x);
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
// Portions marked SStL were provided by Simon St.Laurent [simonstl@simonstl.com]. All Rights Reserved.
//
// Contributor(s): none.
//

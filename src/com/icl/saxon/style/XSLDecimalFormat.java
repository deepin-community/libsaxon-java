package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.expr.*;
import javax.xml.transform.*;

import java.text.DecimalFormatSymbols;

/**
* Handler for xsl:decimal-format elements in stylesheet.<BR>
*/

public class XSLDecimalFormat extends StyleElement {

    String name;
    String decimalSeparator;
    String groupingSeparator;
    String infinity;
    String minusSign;
    String NaN;
    String percent;
    String perMille;
    String zeroDigit;
    String digit;
    String patternSeparator;

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

        for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		name = atts.getValue(a);
        	} else if (f==sn.DECIMAL_SEPARATOR) {
        		decimalSeparator = atts.getValue(a);
        	} else if (f==sn.GROUPING_SEPARATOR) {
        		groupingSeparator = atts.getValue(a);
        	} else if (f==sn.INFINITY) {
        		infinity = atts.getValue(a);
        	} else if (f==sn.MINUS_SIGN) {
        		minusSign = atts.getValue(a);
        	} else if (f==sn.NAN) {
        		NaN = atts.getValue(a);
        	} else if (f==sn.PERCENT) {
        		percent = atts.getValue(a);
        	} else if (f==sn.PER_MILLE) {
        		perMille = atts.getValue(a);
        	} else if (f==sn.ZERO_DIGIT) {
        		zeroDigit = atts.getValue(a);
        	} else if (f==sn.DIGIT) {
        		digit = atts.getValue(a);
        	} else if (f==sn.PATTERN_SEPARATOR) {
        		patternSeparator = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException
    {

        DecimalFormatSymbols d = new DecimalFormatSymbols();
        DecimalFormatManager.setDefaults(d);
        if (decimalSeparator!=null) {
            d.setDecimalSeparator(toChar(decimalSeparator));
        }
        if (groupingSeparator!=null) {
            d.setGroupingSeparator(toChar(groupingSeparator));
        }
        if (infinity!=null) {
        d.setInfinity(infinity);
        }
        if (minusSign!=null) {
            d.setMinusSign(toChar(minusSign));
        }
        if (NaN!=null) {
            d.setNaN(NaN);
        }
        if (percent!=null) {
            d.setPercent(toChar(percent));
        }
        if (perMille!=null) {
            d.setPerMill(toChar(perMille));
        }
        if (zeroDigit!=null) {
            d.setZeroDigit(toChar(zeroDigit));
        }
        if (digit!=null) {
            d.setDigit(toChar(digit));
        }
        if (patternSeparator!=null) {
            d.setPatternSeparator(toChar(patternSeparator));
        }

        DecimalFormatManager dfm = getPrincipalStyleSheet().getDecimalFormatManager();
        if (name==null) {
            dfm.setDefaultDecimalFormat(d);
        } else {
            if (!Name.isQName(name)) {
                compileError("Name of decimal-format must be a valid QName");
            }
            int fprint;
            try {
                fprint = makeNameCode(name, false) & 0xfffff;
            } catch (NamespaceException err) {
                compileError(err.getMessage());
                return;
            }
            dfm.setNamedDecimalFormat(fprint, d);
        }
    }

    public void process(Context context) {}

    private char toChar(String s) throws TransformerConfigurationException {
        if (s.length()!=1)
            compileError("Attribute \"" + s + "\" should be a single character");
        return s.charAt(0);
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

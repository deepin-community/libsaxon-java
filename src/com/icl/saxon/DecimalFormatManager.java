package com.icl.saxon;

import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import javax.xml.transform.TransformerConfigurationException;

/**
  * DecimalFormatManager manages the collection of named and unnamed decimal formats
  * @version 10 December 1999: extracted from Controller
  * @author Michael H. Kay
  */

public class DecimalFormatManager {

    private DecimalFormatSymbols defaultDFS;
    private Hashtable formatTable;            // table for named decimal formats
    private boolean usingOriginalDefault = true;

    /**
    * create a Controller and initialise variables
    */

    public DecimalFormatManager() {
        formatTable = new Hashtable();
        DecimalFormatSymbols d = new DecimalFormatSymbols();
        setDefaults(d);
        defaultDFS = d;
    }

    /**
    * Set up the XSLT-defined default attributes in a DecimalFormatSymbols
    */

    public static void setDefaults(DecimalFormatSymbols d) {
        d.setDecimalSeparator('.');
        d.setGroupingSeparator(',');
        d.setInfinity("Infinity");
        d.setMinusSign('-');
        d.setNaN("NaN");
        d.setPercent('%');
        d.setPerMill('\u2030');
        d.setZeroDigit('0');
        d.setDigit('#');
        d.setPatternSeparator(';');
    }

    /**
    * Register the default decimal-format.
    * Note that it is an error to register the same decimal-format twice, even with different
    * precedence
    */

    public void setDefaultDecimalFormat(DecimalFormatSymbols dfs)
    throws TransformerConfigurationException {
        if (!usingOriginalDefault) {
            if (!dfs.equals(defaultDFS)) {
                throw new TransformerConfigurationException(
                    "There are two conflicting definitions of the default decimal format");
            }
        }
        defaultDFS = dfs;
        usingOriginalDefault = false;
    }

    /**
    * Get the default decimal-format.
    */

    public DecimalFormatSymbols getDefaultDecimalFormat() {
        return defaultDFS;
    }

    /**
    * Set a named decimal format.
    * Note that it is an error to register the same decimal-format twice, even with different
    * precedence.
    */

    public void setNamedDecimalFormat(int fingerprint, DecimalFormatSymbols dfs)
    throws TransformerConfigurationException {
		Integer dfskey = new Integer(fingerprint);
    	DecimalFormatSymbols old = (DecimalFormatSymbols)formatTable.get(dfskey);
        if (old!=null) {
            if (!dfs.equals(old)) {
                throw new TransformerConfigurationException("Duplicate declaration of decimal-format");
            }
        }
        formatTable.put(dfskey, dfs);
    }

    /**
    * Get a named decimal-format registered using setNamedDecimalFormat
    * @param fingerprint The fingerprint of the name of the decimal format
    * @return the DecimalFormatSymbols object corresponding to the named locale, if any
    * or null if not set.
    */

    public DecimalFormatSymbols getNamedDecimalFormat(int fingerprint) {
        return (DecimalFormatSymbols)formatTable.get(new Integer(fingerprint));
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

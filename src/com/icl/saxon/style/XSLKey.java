package com.icl.saxon.style;
import com.icl.saxon.Binding;
import com.icl.saxon.Context;
import com.icl.saxon.KeyDefinition;
import com.icl.saxon.KeyManager;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
* Handler for xsl:key elements in stylesheet.<BR>
*/

public class XSLKey extends StyleElement  {

    private int fingerprint;     // the fingerprint of the key name
    private Pattern match;
    private Expression use;

    public void prepareAttributes() throws TransformerConfigurationException {

        String nameAtt = null;
        String matchAtt = null;
        String useAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
        	} else if (f==sn.USE) {
        		useAtt = atts.getValue(a);
        	} else if (f==sn.MATCH) {
        		matchAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
            return;
        }
        if (!Name.isQName(nameAtt)) {
            compileError("Name of key must be a valid QName");
            return;
        }
        try {
            fingerprint = makeNameCode(nameAtt, false) & 0xfffff;
        } catch (NamespaceException err) {
            compileError(err.getMessage());
        }

        if (matchAtt==null) {
            reportAbsence("match");
        } else {
            match = makePattern(matchAtt);
        }

        if (useAtt==null) {
            reportAbsence("use");
        } else {
            use = makeExpression(useAtt);
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkTopLevel();
        checkEmpty();
    }

    public void preprocess() throws TransformerConfigurationException
    {
        KeyManager km = getPrincipalStyleSheet().getKeyManager();
        km.setKeyDefinition(new KeyDefinition(fingerprint, match, use));
    }

    public void process(Context context) throws TransformerException
    {}

    /**
    * Disallow variable references in the match and use patterns
    */

    public Binding bindVariable(int fingerprint) throws XPathException {
        throw new XPathException("The expressions in xsl:key may not contain references to variables");
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

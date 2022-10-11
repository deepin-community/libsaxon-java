package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.Context;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.output.Outputter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

//import java.util.*;

/**
* An xsl:processing-instruction element in the stylesheet.
*/

public class XSLProcessingInstruction extends XSLStringConstructor {

    Expression name;

    public void prepareAttributes() throws TransformerConfigurationException {

        String nameAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        if (nameAtt==null) {
            reportAbsence("name");
        } else {
            name = makeAttributeValueTemplate(nameAtt);
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        optimize();
    }


    public void process(Context context) throws TransformerException
    {
        String expandedName = name.evaluateAsString(context);

        if (!(Name.isNCName(expandedName)) || expandedName.equalsIgnoreCase("xml")) {
            context.getController().reportRecoverableError(
                "Processing instruction name is invalid: " + expandedName, this);
            return;
        }

        String data = expandChildren(context);

        int hh = data.indexOf("?>");
        if (hh >= 0) {
            context.getController().reportRecoverableError(
                "Invalid characters (?>) in processing instruction", this);
            data = data.substring(0, hh+1) + " " + data.substring(hh+1);
        }

        context.getOutputter().writePI(expandedName, data);
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

package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.expr.ContextNodeExpression;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


/**
* An xsl:value-of element in the stylesheet.<BR>
* The xsl:value-of element takes attributes:<ul>
* <li>an mandatory attribute select="expression".
* This must be a valid String expression</li>
* <li>an optional disable-output-escaping attribute, value "yes" or "no"</li>
* </ul>
*/

public final class XSLValueOf extends StyleElement {

    private Expression select;
    private boolean disable = false;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    public Expression getSelectExpression() {
        if (select==null) {
            return new ContextNodeExpression();
        } else {
            return select;
        }
    }

    public boolean getDisableOutputEscaping() {
        return disable;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		String selectAtt = null;
		String disableAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.DISABLE_OUTPUT_ESCAPING) {
        		disableAtt = atts.getValue(a);
			} else if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (selectAtt==null) {
            reportAbsence("select");
            return;
        }
        if (selectAtt.trim().equals(".")) {
            select = null;  // optimization
        } else {
            select = makeExpression(selectAtt);
        }

        if (disableAtt != null) {
            if (disableAtt.equals("yes")) {
                disable = true;
            } else if (disableAtt.equals("no")) {
                disable = false;
            } else {
                compileError("disable-output-escaping attribute must be either yes or no");
            }
        }


    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        checkEmpty();
    }

    public void process(Context context) throws TransformerException
    {
        Outputter out = context.getOutputter();
        if (disable) out.setEscaping(false);

        if (select==null) {
            (context.getCurrentNodeInfo()).copyStringValue(out);
        } else {
            select.outputStringValue(out, context);
        }

        if (disable) out.setEscaping(true);
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

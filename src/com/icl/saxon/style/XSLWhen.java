package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import javax.xml.transform.*;


/**
* Handler for xsl:when elements in stylesheet.<BR>
* The xsl:while element has a mandatory attribute test, a boolean expression.
*/

public class XSLWhen extends StyleElement {

    private Expression test;

    public Expression getCondition() {
        return test;
    }

    /**
    * Determine whether this element does any processing after instantiating any children.
    * This implementation says it doesn't, thus enabling tail recursion.
    */

    public boolean doesPostProcessing() {
        return false;
    }

    public void prepareAttributes() throws TransformerConfigurationException {
        String testAtt=null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		//
        	} else if (f==sn.TEST) {
        		testAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (testAtt==null) {
            reportAbsence("test");
        } else {
            test = makeExpression(testAtt);
        }
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void validate() throws TransformerConfigurationException {
        if (!(getParentNode() instanceof XSLChoose)) {
            compileError("xsl:when must be immediately within xsl:choose");
        }
    }

    public void process(Context context) throws TransformerException
    {
        processChildren(context);   // the condition is tested from the outer xsl:choose element
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

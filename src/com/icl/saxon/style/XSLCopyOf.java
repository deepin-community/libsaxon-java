package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.Outputter;
import javax.xml.transform.*;


/**
* An xsl:copy-of element in the stylesheet.<BR>
* The xsl:copy-of element takes:<ul>
* <li>an optional attribute select="pattern", defaulting to "." (the current element).</li>
* </ul>
*/

public class XSLCopyOf extends StyleElement {

    Expression select;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();
		String selectAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        } else {
            reportAbsence("select");
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        checkEmpty();
    }

    public void process(Context context) throws TransformerException
    {

    	if (select instanceof NodeSetExpression) {
    		copyNodeSet(select, context);
    	} else {
	        Value value = select.evaluate(context);
	        if (value instanceof FragmentValue) {
	            ((FragmentValue)value).copy(context.getOutputter());

	        } else if (value instanceof TextFragmentValue) {
	            ((TextFragmentValue)value).copy(context.getOutputter());

	        } else if (value instanceof NodeSetValue) {
	            copyNodeSet((NodeSetValue)value, context);

	        } else {
	            context.getOutputter().writeContent(value.asString());
	        }
    	}
    }

    private void copyNodeSet(Expression nodeSet, Context c) throws TransformerException {
    	Outputter out = c.getOutputter();
    	NodeEnumeration enm = nodeSet.enumerate(c, true);
    	while (enm.hasMoreElements()) {
    		NodeInfo node = enm.nextElement();
    		node.copy(out);
    	}
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

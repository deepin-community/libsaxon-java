package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.LastPositionFinder;
import com.icl.saxon.expr.LookaheadEnumerator;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
* Handler for xsl:for-each elements in stylesheet.<BR>
*/

public class XSLForEach extends StyleElement {

    Expression select = null;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
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

        if (selectAtt==null) {
            reportAbsence("select");
        } else {
            select = makeExpression(selectAtt);
        }

    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        select = handleSortKeys(select);
    }

    public void process(Context context) throws TransformerException
    {
        XSLTemplate saveCurrent = context.getCurrentTemplate();
        context.setCurrentTemplate(null);
        NodeEnumeration selection = select.enumerate(context, false);
        if (!(selection instanceof LastPositionFinder)) {
            selection = new LookaheadEnumerator(selection);
        }

        Context c = context.newContext();
        c.setLastPositionFinder((LastPositionFinder)selection);
        int position = 1;

        if (context.getController().isTracing()) {
            TraceListener listener = context.getController().getTraceListener();
            while(selection.hasMoreElements()) {
                NodeInfo node = selection.nextElement();
                c.setPosition(position++);
                c.setCurrentNode(node);
                c.setContextNode(node);
                listener.enterSource(null, c);
                processChildren(c);
                listener.leaveSource(null, c);
                context.setReturnValue(c.getReturnValue());
            }
        } else {
            while(selection.hasMoreElements()) {
                NodeInfo node = selection.nextElement();
                c.setPosition(position++);
                c.setCurrentNode(node);
                c.setContextNode(node);
                processChildren(c);
                context.setReturnValue(c.getReturnValue());
            }
        }
        context.setCurrentTemplate(saveCurrent);
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

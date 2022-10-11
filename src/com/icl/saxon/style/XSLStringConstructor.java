package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.output.Outputter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
* Common superclass for XSLT elements whose content template produces a text
* value: xsl:attribute, xsl:comment, and xsl:processing-instruction
*/

public abstract class XSLStringConstructor extends StyleElement {

    private String stringValue = null;
    private Expression valueExpression = null;

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

    protected void optimize() throws TransformerConfigurationException {
        NodeImpl first = (NodeImpl)getFirstChild();
        if (first==null) {
            // there are no child nodes
            stringValue = "";
        } else {
            NodeImpl next = (NodeImpl)first.getNextSibling();
            if (next==null) {
                // there is exactly one child node
                if (first.getNodeType() == NodeInfo.TEXT) {
                    // it is a text node: optimize for this case
                    stringValue = first.getStringValue();
                } else if (first instanceof XSLValueOf) {
                    // it is an xsl:value-of instruction: optimize this case
                    XSLValueOf v = (XSLValueOf)first;
                    valueExpression = v.getSelectExpression();
                    if (v.getDisableOutputEscaping()) {
                        v.compileError("disable-output-escaping is not allowed for a non-text node");
                    }
                }
            }
        }
    }

    /**
    * Expand the stylesheet elements subordinate to this one, returning the result
    * as a string. The expansion must not generate any element or attribute nodes.
    * @param context The context in the source document
    */

    public String expandChildren(Context context) throws TransformerException {

        if (stringValue != null) {
            return stringValue;

        } else if (valueExpression != null) {
            return valueExpression.evaluateAsString(context);

        } else {
            Controller c = context.getController();
            Outputter old = c.getOutputter();
            StringBuffer buffer = new StringBuffer();
            c.changeToTextOutputDestination(buffer);
            processChildren(context);
            c.resetOutputDestination(old);
            return buffer.toString();

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

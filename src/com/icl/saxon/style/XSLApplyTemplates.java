package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.Mode;
import com.icl.saxon.ParameterSet;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.*;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
//import java.util.*;
//import java.io.*;

/**
* An xsl:apply-templates element in the stylesheet
*/

public class XSLApplyTemplates extends StyleElement {

    private Expression select;
    private boolean usesParams;
    private int modeNameCode = -1;            // -1 if no mode specified
    private Mode mode;
    private String modeAttribute;

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
			if (f==sn.MODE) {
        		modeAttribute = atts.getValue(a);
        	} else if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (modeAttribute!=null) {
            if (Name.isQName(modeAttribute)) {
                try {
                    modeNameCode = makeNameCode(modeAttribute, false);
                } catch (NamespaceException err) {
                    compileError(err.getMessage());
                }
            } else {
                if (forwardsCompatibleModeIsEnabled()) {
                    modeAttribute = null;
                } else {
                    compileError("Mode name is not a valid QName");
                }
            }
        }

        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        }
    }

    public void validate() throws TransformerConfigurationException {

        checkWithinTemplate();

        // get the Mode object
        mode = getPrincipalStyleSheet().getRuleManager().getMode(modeNameCode);

        // handle sorting if requested

        boolean sorted = false;
        NodeImpl child = (NodeImpl)getFirstChild();
        while (child!=null) {
            if (child instanceof XSLSort) {
                sorted = true;
            } else if (child instanceof XSLWithParam) {
                usesParams = true;
            } else {
                if (child.getNodeType() == NodeInfo.TEXT) {
                    // with xml:space=preserve, white space nodes may still be there
                    if (!Navigator.isWhite(child.getStringValue())) {
                        compileError(
                            "No character data allowed within xsl:apply-templates");
                    }
                } else {
                    compileError("Invalid element within xsl:apply-templates: ");
                }
            }
            child = (NodeImpl)child.getNextSibling();
        }

        if (select==null && sorted) {
            select = new PathExpression(
                            new ContextNodeExpression(),
                            new Step(Axis.CHILD, AnyNodeTest.getInstance()));
        }
        if (select!=null) {
            select = handleSortKeys(select);
        }
    }

    public void process(Context context) throws TransformerException
    {
        // handle parameters if any

        ParameterSet params = null;
        if (usesParams) {
            params = new ParameterSet();
            Node child = getFirstChild();
            while (child!=null) {
                if (child instanceof XSLWithParam) {
                    XSLWithParam param = (XSLWithParam)child;
                    params.put(param.getVariableFingerprint(), param.getParamValue(context));
                }
                child = child.getNextSibling();
            }
        }

        // Process the selected nodes in the source document

        try {
            context.getController().applyTemplates(context, select, mode, params);
        } catch (StackOverflowError e) {
            throw new XPathException("Too many nested apply-templates calls");
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

package com.icl.saxon.style;
import com.icl.saxon.Bindery;
import com.icl.saxon.Context;
import com.icl.saxon.ParameterSet;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.om.Navigator;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Vector;

/**
* An xsl:call-template element in the stylesheet
*/

public class XSLCallTemplate extends StyleElement {

    private int calledTemplateFingerprint = -1;   // the fingerprint of the called template
    private XSLTemplate template = null;
    private boolean useTailRecursion = false;
    private Expression calledTemplateExpression;    // allows name to be an AVT
    private String calledTemplateName = null;       // used only for diagnostics

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this element does any processing after instantiating any children.
    * This implementation says it doesn't, thus enabling tail recursion.
    */

    public boolean doesPostProcessing() {
        return false;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

        String allowAVTatt = null;
        String nameAttribute = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAttribute = atts.getValue(a);
        	} else if (f==sn.SAXON_ALLOW_AVT) {
        		allowAVTatt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAttribute==null) {
            reportAbsence("name");
            return;
        }

        boolean allowAVT = (allowAVTatt != null && allowAVTatt.equals("yes"));
        if (allowAVT) {
            calledTemplateExpression = makeAttributeValueTemplate(nameAttribute);
        } else {
            if (!Name.isQName(nameAttribute)) {
                compileError("Name of called template must be a valid QName");
            }
            calledTemplateName = nameAttribute;
            try {
                calledTemplateFingerprint =
            	    makeNameCode(nameAttribute, false) & 0xfffff;
            } catch (NamespaceException err) {
                compileError(err.getMessage());
            }
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();

        if (calledTemplateExpression==null) {
            template = findTemplate(calledTemplateFingerprint);

            // Use tail recursion if the template is calling itself, and if neither this instruction
            // nor any ancestor instruction has a following sibling. Avoid tail recursion if called
            // within any element that needs to do further processing after instantiating its children,
            // e.g. a literal result element, which needs to write out the end tag

            if (Navigator.isAncestor(template, this)) {
                useTailRecursion = true;
                StyleElement n = this;
                while (n!=template) {
                    if ((n.isInstruction() && n.getNextSibling()!=null) ||
                    		 n.doesPostProcessing()) {
                        useTailRecursion = false;
                        break;
                    }
                    n = (StyleElement)n.getParentNode();
                }
                // System.err.println((useTailRecursion ? "" : "NOT ") + "Using tail recursion at line " + getLineNumber());
            }
        }

    }

    private XSLTemplate findTemplate(int fingerprint)
    throws TransformerConfigurationException {

        XSLStyleSheet stylesheet = getPrincipalStyleSheet();
        Vector toplevel = stylesheet.getTopLevel();

        // search for a matching template name, starting at the end in case of duplicates.
        // this also ensures we get the one with highest import precedence.

        XSLTemplate found = null;
        for (int i=toplevel.size()-1; i>=0; i--) {
            if (toplevel.elementAt(i) instanceof XSLTemplate) {
                XSLTemplate t = (XSLTemplate)toplevel.elementAt(i);
                if (found != null) {
                    if (t.getPrecedence() < found.getPrecedence()) {
                        break;
                    } else if (t.getTemplateFingerprint() == fingerprint) {
                        compileError("There are several templates named '" + calledTemplateName +
                                "' with the same import precedence");
                    }
                }
                if (t.getTemplateFingerprint() == fingerprint) {
                    found = t;
                }
            }
        }
        if (found == null) {
            compileError("No template exists named " + calledTemplateName);
        }
        return found;
    }

    public void process(Context context) throws TransformerException
    {
        // if name is determined dynamically, determine it now

        XSLTemplate target = template;
        if (calledTemplateExpression != null) {
            String qname = calledTemplateExpression.evaluateAsString(context);
            if (!Name.isQName(qname)) {
                throw styleError("Invalid template name: " + qname);
            }
            int fprint;
            try {
                fprint = makeNameCode(qname, false) & 0xfffff;
            } catch (NamespaceException err) {
                throw styleError(err.getMessage());
            }
            target = findTemplate(fprint);
            if (target==null) {
            	throw styleError("Template " + qname + " has not been defined");
            }
        }

        // handle parameters if any

        ParameterSet params = null;

        if (hasChildNodes()) {
            NodeImpl child = (NodeImpl)getFirstChild();
            params = new ParameterSet();
            while (child != null) {
                if (child instanceof XSLWithParam) {    // currently always true
                    XSLWithParam param = (XSLWithParam)child;
                    params.put(param.getVariableFingerprint(), param.getParamValue(context));
                }
                child = (NodeImpl)child.getNextSibling();
            }
        }

        // Call the named template

        if (useTailRecursion) {
            if (params==null) {                 // bug 490967
                params = new ParameterSet();
            }
            context.setTailRecursion(params);
            // we now just let the stack unwind until we get back to the xsl:template element;
            // at that point the template will detect that there has been a tail-recursive call,
            // and iterate to achieve the effect of calling itself.
        } else {
            Bindery bindery = context.getBindery();
            bindery.openStackFrame(params);

        	if (context.getController().isTracing()) { // e.g. FIXME: trace tail recursion
                target.traceExpand(context);
            } else {
                target.expand(context);
            }

            bindery.closeStackFrame();
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

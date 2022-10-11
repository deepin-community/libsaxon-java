package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.functions.Concat;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import javax.xml.transform.*;
import java.io.*;
import java.util.*;

/**
* This class defines common behaviour across xsl:variable, xsl:param, and xsl:with-param
*/

public abstract class XSLGeneralVariable extends StyleElement  {

    protected int variableFingerprint = -1;
    protected Expression select = null;
    protected String simpleText = null;
    protected boolean global;
    protected Procedure procedure = null;  // used only for global variables
    protected boolean assignable = false;
    protected boolean redundant = false;

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public boolean isGlobal() {
        return (getParentNode() instanceof XSLStyleSheet);
    }

    /**
    * Test whether it is permitted to assign to the variable using the saxon:assign
    * extension element. This will only be true if the extra attribute saxon:assignable="yes"
    * is present.
    */

    public boolean isAssignable() {
        return assignable;
    }

    /**
    * Get the owning Procedure definition, if this is a local variable
    */

    public Procedure getOwningProcedure() throws TransformerConfigurationException {
        NodeInfo node = this;
        while (true) {
            NodeInfo next = (NodeInfo)node.getParent();
            if (next instanceof XSLStyleSheet) {
                if (node instanceof XSLTemplate) {
                    return ((XSLTemplate)node).getProcedure();
                } else if (node instanceof XSLGeneralVariable) {
                    return ((XSLGeneralVariable)node).getProcedure();
                } else if (node instanceof SAXONFunction) {
                    return ((SAXONFunction)node).getProcedure();
                } else if (node instanceof XSLAttributeSet) {
                    return ((XSLAttributeSet)node).getProcedure();
                } else {
                    compileError("Local variable must be declared within a template");
                    return new Procedure();     // for error recovery
                }
            }
            node=next;
        }
    }

    /**
    * Preprocess: this ensures space is available for local variables declared within
    * this global variable
    */

    public void preprocess() throws TransformerConfigurationException
    {
        if (global) {
            getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
        }
    }

    /**
    * Get the display name of the variable.
    */

    public String getVariableName() {
    	return getAttributeValue("", "name");
    }

    /**
    * Get the fingerprint of the variable name
    */

    public int getVariableFingerprint() {

        // if an expression has a forwards reference to this variable, getVariableFingerprint() can be
        // called before prepareAttributes() is called. We need to allow for this. But we'll
        // deal with any errors when we come round to processing this attribute, to avoid
        // duplicate error messages

        if (variableFingerprint==-1) {
        	StandardNames sn = getStandardNames();
            String nameAttribute = getAttributeValue(sn.NAME & 0xfffff);
            if (nameAttribute==null) {
                return -1;              // we'll report the error later
            }
            try {
                variableFingerprint = makeNameCode(nameAttribute, false) & 0xfffff;
            } catch (NamespaceException err) {
                variableFingerprint = -1;
            }
        }
        return variableFingerprint;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

        getVariableFingerprint();

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		String selectAtt = null;
        String assignAtt = null;
        String nameAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
        	} else if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else if (f==sn.SAXON_ASSIGNABLE) {
        		assignAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
        } else if (!Name.isQName(nameAtt)) {
            compileError("Variable name must be a valid QName");
        }


        if (selectAtt!=null) {
            select = makeExpression(selectAtt);
        }

        if (assignAtt!=null && assignAtt.equals("yes")) {
            assignable=true;
        }
    }

    public void validate() throws TransformerConfigurationException {
        global = (getParentNode() instanceof XSLStyleSheet);
        if (global) {
            procedure = new Procedure();
        }
        if (select!=null && getFirstChild()!=null) {
            compileError("An " + getDisplayName() + " element with a select attribute must be empty");
        }

        if (select==null) {
            NodeImpl first = (NodeImpl)getFirstChild();
            if (first==null) {
                select = new StringValue("");
            } else {
                NodeImpl next = (NodeImpl)first.getNextSibling();
                if (next==null) {
                    // there is exactly one child node
                    if (first.getNodeType() == NodeInfo.TEXT) {
                        // it is a text node: optimize for this case
                        simpleText = first.getStringValue();
                    }
                }
            }
        }
    }

    /**
    * Check whether this declaration duplicates another one
    */

    public void checkDuplicateDeclaration() throws TransformerConfigurationException {
        Binding binding = getVariableBinding(getVariableFingerprint());
        int thisPrecedence = this.getPrecedence();
        if (binding!=null) {
            if (global) {
                int otherPrecedence = ((XSLGeneralVariable)binding).getPrecedence();
                if (thisPrecedence == otherPrecedence) {
                    compileError("Duplicate global variable declaration");
                } else if (thisPrecedence < otherPrecedence) {
                    redundant = true;
                } else {
                   ((XSLGeneralVariable)binding).redundant = true;
                }
            } else {
                if (!binding.isGlobal()) {
                    //System.err.println("Clash with line " + ((StyleElement)binding).getLineNumber());
                    compileError("Variable is already declared in this template");
                }
            }
        }
    }


    /**
    * Get the value of the select expression if present or the content of the element otherwise
    */

    protected Value getSelectValue(Context context) throws TransformerException {
        if (select==null) {
            SingletonNodeSet fragment;
            if (simpleText != null) {
                fragment = new TextFragmentValue(simpleText,
                                             getSystemId(),
                                             context.getController());
            } else {
                Controller c = context.getController();
                FragmentValue frag = new FragmentValue(c);
                Outputter old = c.getOutputter();
                c.changeOutputDestination(null, frag.getEmitter());
                if (global && procedure.getNumberOfVariables()>0) {
                    Bindery bindery = context.getBindery();
                    bindery.openStackFrame(new ParameterSet());
                    processChildren(context);
                    bindery.closeStackFrame();
                } else {
                    processChildren(context);
                }
                c.resetOutputDestination(old);
                frag.setBaseURI(getSystemId());
                fragment = frag;
            }
            if (forwardsCompatibleModeIsEnabled()) {
                fragment.allowGeneralUse();
            }
            return fragment;

        } else {
            context.setStaticContext(staticContext);
            Value result = select.evaluate(context);
            if (assignable && (result instanceof NodeSetIntent)) {
                result = new NodeSetExtent(((NodeSetIntent)result).enumerate(),
                                            context.getController());
            }
            return result;
        }
    }

    /**
    * Get associated Procedure (for details of stack frame, if this is a global variable containing
    * local variable declarations)
    */

    public Procedure getProcedure() {
        return procedure;
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

package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.Bindery;
import com.icl.saxon.ParameterSet;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.output.ErrorEmitter;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.StringValue;
import com.icl.saxon.trace.TraceListener;   // e.g.
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
* Handler for saxon:function and exslt:function elements in stylesheet. <BR>
* Attributes: <br>
* name gives the name of the function
*/

public class SAXONFunction extends StyleElement {

    int functionFingerprint = -1;
    Procedure procedure = new Procedure();

    /**
    * Process the [xsl:]extension-element-prefixes attribute.
    * This overrides the standard method because saxon:function and exslt:function
    * implicitly declare saxon/exslt (respectively) as an extension namespace.
    * @param nc the name code of the attribute required (ignored)
    */

    protected void processExtensionElementAttribute(int nc)
    throws TransformerConfigurationException {
		extensionNamespaces = new short[1];
        NamePool pool = getNamePool();
	    short uriCode = pool.getURICode(getNameCode());
	    extensionNamespaces[0] = uriCode;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

        String nameAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
				nameAtt = atts.getValue(a);
				if (nameAtt.indexOf(':')<0) {
					compileError("Function name must have a namespace prefix");
				}
				try {
				    int functionCode = makeNameCode(nameAtt, false);
        		    functionFingerprint = functionCode & 0xfffff;
        		} catch (NamespaceException err) {
        		    compileError(err.getMessage());
        		}
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
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
        checkTopLevel();
    }

    public void preprocess() throws TransformerConfigurationException {
        getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
    }

    public void process(Context context) {}

    /**
    * Get associated Procedure (for details of stack frame)
    */

    public Procedure getProcedure() {
        return procedure;
    }

    public int getFunctionFingerprint() {
        if (functionFingerprint==-1) {
            // this is a forwards reference to the function
            try {
        	    prepareAttributes();
        	} catch (TransformerConfigurationException err) {
        	    return -1;              // we'll report the error later
        	}
        }
        return functionFingerprint;
    }

    /**
    * Get the name fingerprint of the n'th parameter (starting from 0).
    * Return -1 if there is none such.
    */

    public int getNthParameter(int n) {
        NodeImpl node = (NodeImpl)getFirstChild();
        int pos = 0;
        while (node!=null) {
            if (node instanceof XSLParam) {
                if (pos==n) {
                    return ((XSLParam)node).getVariableFingerprint();
                } else {
                    pos++;
                }
            }
            node = (NodeImpl)node.getNextSibling();
        }
        return -1;
    }

    /**
    * Call this function
    */

    public Value call(ParameterSet params, Context context) throws TransformerException {
        Bindery bindery = context.getBindery();
        bindery.openStackFrame(params);
        Controller controller = context.getController();
        Outputter old = controller.getOutputter();
        controller.changeOutputDestination(null, new ErrorEmitter());

    	if (controller.isTracing()) { // e.g.
    	    TraceListener listener = controller.getTraceListener();
    	    listener.enter(this, context);
    	    processChildren(context);
    	    listener.leave(this, context);
    	} else {
    	    processChildren(context);
    	}

        controller.resetOutputDestination(old);
        bindery.closeStackFrame();
        Value result = context.getReturnValue();
        if (result==null) {
            result = new StringValue("");
        }
        context.setReturnValue(null);
        return result;
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
// Contributor(s):
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//

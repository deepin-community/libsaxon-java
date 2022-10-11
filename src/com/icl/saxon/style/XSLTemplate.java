package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.trace.*;  // e.g.

import javax.xml.transform.*;

/**
* An xsl:template element in the style sheet.
*/

public class XSLTemplate extends StyleElement implements NodeHandler {

    protected int modeNameCode = -1;
    protected int templateFingerprint = -1;
    protected Pattern match;
    protected boolean prioritySpecified;
    protected double priority;
    protected Procedure procedure = new Procedure();
    protected boolean needsStackFrame;

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    /**
    * Return the fingerprint for the name of this template
    */

    public int getTemplateFingerprint() {

    	//We use -1 to mean "not yet evaluated"

        try {
        	if (templateFingerprint==-1) {
        		// allow for forwards references
        		StandardNames sn = getStandardNames();
        		String nameAtt = getAttributeList().getValue(sn.NAME);
        		if (nameAtt!=null) {
        			templateFingerprint =
                		makeNameCode(nameAtt, false) & 0xfffff;
                }
            }
            return templateFingerprint;
        } catch (NamespaceException err) {
            return -1;          // the error will be picked up later
        }
    }

    public int getMinImportPrecedence() {
        return ((XSLStyleSheet)getDocumentElement()).getMinImportPrecedence();
    }

    public boolean needsStackFrame() {
        return this.needsStackFrame;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		String modeAtt = null;
		String nameAtt = null;
		String priorityAtt = null;
		String matchAtt = null;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.MODE) {
        		modeAtt = atts.getValue(a);
			} else if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
			} else if (f==sn.MATCH) {
        		matchAtt = atts.getValue(a);
			} else if (f==sn.PRIORITY) {
        		priorityAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        try {
            if (modeAtt!=null) {
                if (!Name.isQName(modeAtt)) {
                    if (forwardsCompatibleModeIsEnabled()) {
                        modeAtt = null;
                    } else {
                        compileError("Mode name is not a valid QName");
                    }
                } else {
                    modeNameCode = makeNameCode(modeAtt, false);
                }
            }

            if (nameAtt!=null) {
                if (!Name.isQName(nameAtt)) {
                    compileError("Template name is not a valid QName");
                }
                templateFingerprint = makeNameCode(nameAtt, false) & 0xfffff;
            }
        } catch (NamespaceException err) {
            compileError(err.getMessage());
        }

        prioritySpecified = (priorityAtt != null);
        if (prioritySpecified) {
            try {
                priority = Double.parseDouble(priorityAtt.trim());
            } catch (NumberFormatException err) {
                compileError("Invalid numeric value for priority (" + priority + ')');
            }
        }

        if (matchAtt != null) {
            match = makePattern(matchAtt);
        }

        if (match==null && nameAtt==null)
            compileError("xsl:template must have a a name or match attribute (or both)");

	}


    public void validate() throws TransformerConfigurationException {
        checkTopLevel();

        // it is in error if there is another template with the same name and precedence

        if (templateFingerprint!=-1) {
            NodeImpl node = (NodeImpl)getPreviousSibling();
            while (node!=null) {
                if (node instanceof XSLTemplate) {
                    XSLTemplate t = (XSLTemplate)node;
                    if (t.getTemplateFingerprint()==templateFingerprint &&
                            t.getPrecedence()==this.getPrecedence()) {
                        compileError("There is another template with the same name and precedence");
                    }
                }
                node = (NodeImpl)node.getPreviousSibling();
            }
        }

    }

    /**
    * Preprocess: this registers the template with the rule manager, and ensures
    * space is available for local variables
    */

    public void preprocess() throws TransformerConfigurationException
    {
        RuleManager mgr = getPrincipalStyleSheet().getRuleManager();
        Mode mode = mgr.getMode(modeNameCode);

        if (match!=null) {
            NodeHandler handler = this;
            if (getFirstChild()==null) {
                // template is empty: use a no-op handler instead
                handler = new NoOpHandler();
            }
            if (prioritySpecified) {
                mgr.setHandler(match, handler, mode, getPrecedence(), priority);
            } else {
                mgr.setHandler(match, handler, mode, getPrecedence());
            }
        }

        getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
        needsStackFrame = (procedure.getNumberOfVariables() > 0);

    }

    /**
    * Process template. This is called while all the top-level nodes are being processed in order,
    * so it does nothing.
    */

    public void process(Context context) throws TransformerException {
    }

    /**
    * Process a node in the source document. This is called when the template
    * is invoked using xsl:apply-templates.
    */

    public void start( NodeInfo e, Context context ) throws TransformerException {
        context.setCurrentTemplate(this);

    	if (context.getController().isTracing()) { // e.g. FIXME: trace tail recursion
            traceExpand(context);
        } else {
            expand(context);
        }
    }

    /**
    * Expand the template, with tracing. Called when the template is invoked either
    * by xsl:apply-templates or from xsl:call-template
    */

    protected void traceExpand(Context context) throws TransformerException {

	    TraceListener listener = context.getController().getTraceListener();

	    listener.enter(this, context);
	    expand(context);
	    listener.leave(this, context);
    }

    /**
    * Expand the template. Called when the template is invoked either
    * by xsl:apply-templates or from xsl:call-template
    */

    protected void expand(Context context) throws TransformerException {
	    ParameterSet p;
	    do {
    		context.setTailRecursion(null);
    		processChildren(context);
    		p = context.getTailRecursion();
    		if (p!=null) {
    		    context.getBindery().closeStackFrame();
    		    context.getBindery().openStackFrame(p);
    		}
	    } while (p!=null);
    }

    /**
    * Disallow variable references in the match pattern
    */

    public Binding bindVariable(int fingerprint) throws XPathException {
        throw new XPathException("The match pattern in xsl:template may not contain references to variables");
    }

    /**
    * Get associated Procedure (for details of stack frame)
    */

    public Procedure getProcedure() {
        return procedure;
    }

    /**
    * Inner class: a no-op handler to provide a fast path for empty templates
    */

    private static final class NoOpHandler implements NodeHandler {
        public void start( NodeInfo e, Context context ) {}
        public boolean needsStackFrame() {
            return false;
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
// Contributor(s):
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//

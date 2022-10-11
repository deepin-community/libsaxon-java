package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import javax.xml.transform.*;

/**
* A saxon:handler element in the style sheet: defines a Java nodehandler that
* can be used to process a node in place of an XSLT template
*/

public class SAXONHandler extends XSLTemplate {

    private NodeHandler handler;

    public void checkUnknownAttribute(int nc)
    throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		int f = nc & 0xfffff;
		if (f==sn.HANDLER) {
        	String handlerAtt = atts.getValueByFingerprint(f);
        	handler = makeHandler(handlerAtt);
        } else {
        	super.checkUnknownAttribute(nc);
        }

        if (handler==null) {
            reportAbsence("handler");
        }

    }


    public void validate() throws TransformerConfigurationException {
		if (handler==null) {
            reportAbsence("handler");
        }
        checkTopLevel();
    }

    /**
    * Preprocess: this registers the node handler with the controller
    */

    public void preprocess() throws TransformerConfigurationException
    {
        RuleManager mgr = getPrincipalStyleSheet().getRuleManager();
        Mode mode = mgr.getMode(modeNameCode);
        if (match!=null) {
            if (prioritySpecified) {
                mgr.setHandler(match, handler, mode, getPrecedence(), priority);
            } else {
                mgr.setHandler(match, handler, mode, getPrecedence());
            }
        }

    }

    /**
    * Process saxon:handler element. This is called while all the top-level nodes are being
    * processed in order, so it does nothing.
    */

    public void process(Context context) throws TransformerException {
    }

    /**
    * Invoke the node handler. Called directly only when doing XSLCallTemplate
    */

    public void expand(Context context) throws TransformerException {
        handler.start(context.getCurrentNodeInfo(), context);
    }

    /**
    * Load a named node handler and check it is OK.
    */

    protected NodeHandler makeHandler (String className)
    throws TransformerConfigurationException
    {
        try {
            return (NodeHandler)(Loader.getInstance(className));
        } catch (TransformerException err) {
            compileError(err);
        } catch (ClassCastException e) {
            compileError("Node handler " + className +
                            " does not implement the NodeHandler interface");
        }
        return null;
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

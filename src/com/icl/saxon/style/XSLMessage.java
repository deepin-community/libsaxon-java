package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.output.Emitter;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
* An xsl:message element in the stylesheet.<BR>
*/

public class XSLMessage extends StyleElement {

    boolean terminate = false;

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

        String terminateAtt = null;
		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.TERMINATE) {
        		terminateAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (terminateAtt!=null) {
            if (terminateAtt.equals("yes")) {
                terminate = true;
            } else if (terminateAtt.equals("no")) {
                terminate = false;
            } else {
                styleError("terminate must be \"yes\" or \"no\"");
            }
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process(Context context) throws TransformerException
    {
        Controller c = context.getController();
        Emitter emitter = c.getMessageEmitter();
        if (emitter==null) {
            emitter = c.makeMessageEmitter();
        }
        if (emitter.getWriter()==null) {
            emitter.setWriter(new OutputStreamWriter(System.err));
        }

        Outputter old = c.getOutputter();
        Properties props = new Properties();
        props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        c.changeOutputDestination(props, emitter);

        processChildren(context);

        c.resetOutputDestination(old);

        if (terminate) {
            throw new TerminationException("Processing terminated by xsl:message at line " + getLineNumber());
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

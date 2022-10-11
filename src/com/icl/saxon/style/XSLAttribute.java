package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.tree.NodeImpl;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.Outputter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
* xsl:attribute element in stylesheet.<BR>
*/

public final class XSLAttribute extends XSLStringConstructor {

    private Expression attributeName;
    private Expression namespace=null;
    private boolean disable = false;

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		String nameAtt = null;
		String namespaceAtt = null;
		String disableAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
        	} else if (f==sn.NAMESPACE) {
        		namespaceAtt = atts.getValue(a);
        	} else if (f==sn.SAXON_DISABLE_OUTPUT_ESCAPING) {
        		disableAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
            return;
        } else {
            attributeName = makeAttributeValueTemplate(nameAtt);
            if (attributeName instanceof StringValue) {
                if (!Name.isQName(((StringValue)attributeName).asString())) {
                    compileError("Attribute name is not a valid QName");
                }
            }
        }

        if (namespaceAtt!=null) {
            namespace = makeAttributeValueTemplate(namespaceAtt);
        }

        disable = (disableAtt != null && disableAtt.equals("yes"));

    }

    public void validate() throws TransformerConfigurationException {
        if (!(getParentNode() instanceof XSLAttributeSet)) {
            checkWithinTemplate();
        }
        optimize();
    }

    public void process(Context context) throws TransformerException
    {
        String expandedName = attributeName.evaluateAsString(context);
        Controller controller = context.getController();
        NamePool pool = controller.getNamePool();

        if (!Name.isQName(expandedName)) {
            controller.reportRecoverableError(
                "Invalid attribute name: " + expandedName, this);
            return;
        }

        if (expandedName.equals("xmlns")) {
        	if (namespace==null) {
                controller.reportRecoverableError(
                    "Invalid attribute name: " + expandedName, this);
                return;
            }
        }
        if (expandedName.length()>6 && expandedName.substring(0,6).equals("xmlns:")) {
        	if (namespace==null) {
                controller.reportRecoverableError(
                    "Invalid attribute name: " + expandedName, this);
                return;
            } else {
                // ignore the prefix "xmlns"
                expandedName = expandedName.substring(6);
            }
        }

        String prefix = Name.getPrefix(expandedName);
        short uriCode;

        if (namespace==null) {

        	// NB, we can't just call makeNameCode() because that would use the wrong
        	// name pool

        	if (prefix.equals("")) {
        		uriCode = 0;
        	} else {
        	    try {
        		    uriCode = getURICodeForPrefix(prefix);  // fails if not declared
        		} catch (NamespaceException err) {
        		    //TODO: should be a recoverable error?
        		    throw styleError(err.getMessage());
        		}
        	}

        } else {

            // generate a name using the supplied namespace URI

            String uri = namespace.evaluateAsString(context);
            if (uri.equals("")) {
                // there is a special rule for this case in the specification;
                // we force the attribute to go in the null namespace
                prefix = "";

            } else {
                if (prefix.equals("")) {
                    prefix = getPrefixForURI(uri);
                    // prefix will be null if the namespace is undeclared, and "" if the
                    // namespace is the default namespace. In both cases, invent a prefix.
                    if (prefix==null || prefix=="") {
                        prefix="ns0";       // arbitrary generated prefix; will be changed later if
                                            // not unique
                    }
                }
            }

            uriCode = pool.allocateCodeForURI(uri);   // allocate code in run-time name pool


        }

        String localName = Name.getLocalName(expandedName);
        int nameCode = pool.allocate(prefix, uriCode, localName);

        Outputter out = controller.getOutputter();

    	// we may need to change the namespace prefix if the one we chose is
    	// already in use with a different namespace URI
        if (out.thereIsAnOpenStartTag()) {
            if (((nameCode>>20)&0xff) != 0) {	// prefix!=""
        	    nameCode = out.checkAttributePrefix(nameCode);
            }
            out.writeAttribute(nameCode, expandChildren(context), disable);
        } else {
            context.getController().reportRecoverableError(
                "Cannot write an attribute node when no element start tag is open",
                this);
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

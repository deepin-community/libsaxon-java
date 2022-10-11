package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import javax.xml.transform.*;


/**
* An xsl:element element in the stylesheet.<BR>
*/

public class XSLElement extends StyleElement {

    private Expression elementName;
    private Expression namespace = null;
    private String use;
    private boolean declared = false;       // used by compiler

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

		String nameAtt = null;
		String namespaceAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.NAME) {
        		nameAtt = atts.getValue(a);
        	} else if (f==sn.NAMESPACE) {
        		namespaceAtt = atts.getValue(a);
        	} else if (f==sn.USE_ATTRIBUTE_SETS) {
        		use = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (nameAtt==null) {
            reportAbsence("name");
        } else {
            elementName = makeAttributeValueTemplate(nameAtt);
            if (elementName instanceof StringValue) {
                if (!Name.isQName(((StringValue)elementName).asString())) {
                    compileError("Element name is not a valid QName");
                }
            }
        }

        if (namespaceAtt!=null) {
            namespace = makeAttributeValueTemplate(namespaceAtt);
        }

    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        if (use!=null) {
            findAttributeSets(use);        // find any referenced attribute sets
        }
    }

    public void process(Context context) throws TransformerException
    {
    	Controller controller = context.getController();
    	NamePool pool = controller.getNamePool();

        // produce (pending) output

        String expandedName = elementName.evaluateAsString(context);

        if (!Name.isQName(expandedName)) {
            controller.reportRecoverableError(
                "Invalid element name: " + expandedName, this);
                // don't write this element;
                // but signal the outputter to ignore any following attributes
            context.getOutputter().writeStartTag(-1);
            processChildren(context);
            return;
        }

        String prefix = Name.getPrefix(expandedName);
        short uriCode;

        if (namespace==null) {

        	// NB, we can't just call makeNameCode() because that would use the wrong
        	// name pool
            try {
      		    uriCode = getURICodeForPrefix(prefix);	// error if not present
      		} catch (NamespaceException err) {
      		    // TODO: should be a recoverable error?
      		    throw styleError(err.getMessage());
      		}

        } else {

            String uri = namespace.evaluateAsString(context);
            if (uri.equals("")) {
                // there is a special rule for this case in the specification;
                // we force the element to go in the null namespace
                prefix = "";
            }
            uriCode = pool.allocateCodeForURI(uri);
        }

        String localName = Name.getLocalName(expandedName);
        int nameCode = pool.allocate(prefix, uriCode, localName);

        Outputter out = context.getOutputter();
        out.writeStartTag(nameCode);
        out.writeNamespaceDeclaration(pool.allocateNamespaceCode(nameCode));

        // apply the content of any attribute sets mentioned in use-attribute-sets
        processAttributeSets(context);

        // process subordinate elements in stylesheet
        processChildren(context);

        // output the element end tag
        out.writeEndTag(nameCode);
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

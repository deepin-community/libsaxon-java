package com.icl.saxon.style;
import com.icl.saxon.Context;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
* This element is a surrogate for an extension element (or indeed an xsl element)
* for which no implementation is available.<BR>
*/

public class AbsentExtensionElement extends StyleElement {

    /**
    * Determine whether this type of element is allowed to contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {
    }

    /**
    * Process the attributes of this element and all its children. For an unrecognized top-level element
     * in forwards-compatibility mode, ignore this element and its children
    */

    public void processAllAttributes() throws TransformerConfigurationException {
        if (isTopLevel() && forwardsCompatibleModeIsEnabled()) {
            // do nothing
        } else {
            super.processAllAttributes();
        }
    }

    public void validate() throws TransformerConfigurationException {
    }

    /**
     * Recursive walk through the stylesheet to validate all nodes. For an unrecognized top-level element
     * in forwards-compatibility mode, ignore this element and its children
     */

    public void validateSubtree() throws TransformerConfigurationException {
        if (isTopLevel() && forwardsCompatibleModeIsEnabled()) {
            // do nothing
        } else {
            super.validateSubtree();
        }
    }

    public void process(Context context) throws TransformerException {
        if (!(isTopLevel() && forwardsCompatibleModeIsEnabled())) {
		    throw validationError;
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

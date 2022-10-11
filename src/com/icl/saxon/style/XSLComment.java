package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.Context;


import com.icl.saxon.output.Outputter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
//import java.util.*;

/**
* An xsl:comment elements in the stylesheet.<BR>
*/

public final class XSLComment extends XSLStringConstructor {

    public void prepareAttributes() throws TransformerConfigurationException {
		AttributeCollection atts = getAttributeList();
		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
        	checkUnknownAttribute(nc);
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        optimize();
    }

    public void process(Context context) throws TransformerException
    {
        String comment = expandChildren(context);

        //TODO: do this checking at compile time if the content is fixed
        while(true) {
            int hh = comment.indexOf("--");
            if (hh < 0) break;
            context.getController().reportRecoverableError("Invalid characters (--) in comment", this);
            comment = comment.substring(0, hh+1) + " " + comment.substring(hh+1);
        }
        if (comment.length()>0 && comment.charAt(comment.length()-1)=='-') {
            context.getController().reportRecoverableError("Invalid character (-) at end of comment", this);
            comment = comment + " ";
        }
        context.getOutputter().writeComment(comment);
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

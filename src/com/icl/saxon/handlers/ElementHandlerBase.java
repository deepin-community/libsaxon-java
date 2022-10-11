package com.icl.saxon.handlers;
import com.icl.saxon.Context;
import com.icl.saxon.om.NodeInfo;
import javax.xml.transform.TransformerException;

/**
 * This class is the default element handler from which
 * user-defined element handlers can inherit. It is provided for convenience:
 * use is optional. The individual methods of the default element handler
 * do nothing with the content; in a subclass it is therefore only necessary to implement
 * those methods that need to do something specific.<P>
 * The startElement() method calls applyTemplates(), so child elements will
 * always be processed.<P>
 * @author Michael H. Kay
 */

public class ElementHandlerBase extends ElementHandler {

    /**
    * implement start() method
    */

    public void start(NodeInfo node, Context context) throws TransformerException {
        if (node.getNodeType() != NodeInfo.ELEMENT)
            throw new TransformerException("Element Handler called for a node that is not an element");
        startElement(node, context);
    }

    /**
    * Define action to be taken before an element of this element type.<BR>
    * Default implementation does nothing, other than causing subordinate elements
    * to be processed in the same mode as the caller
    * @param e The NodeInfo object for the current element.
    */

    public void startElement( NodeInfo e, Context context ) throws TransformerException {
	    context.getController().applyTemplates(
	                context, null, context.getMode(), null);
    }

    public boolean needsStackFrame() {
        return false;
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

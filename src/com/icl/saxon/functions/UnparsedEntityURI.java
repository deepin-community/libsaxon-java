package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;
import java.util.*;




public class UnparsedEntityURI extends Function {

    DocumentInfo boundDocument = null;

    public String getName() {
        return "unparsed-entity-uri";
    };

    /**
    * Determine the data type of the expression
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    /**
    * Validate and simplify
    */

    public Expression simplify() throws XPathException {
        checkArgumentCount(1, 1);
        return this;
    }

    /**
    * Evaluate the expression in a string context
    */

    public String evaluateAsString(Context context) throws XPathException {
        String arg0 = argument[0].evaluateAsString(context);
        DocumentInfo doc = boundDocument;
        if (doc==null) doc = (context.getContextNodeInfo()).getDocumentRoot();
        return doc.getUnparsedEntity(arg0);
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return new StringValue(evaluateAsString(c));
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = argument[0].getDependencies();
        if (boundDocument==null) {
            dep |= Context.CONTEXT_NODE;
        }
        return dep;
    }

    /**
    * Remove dependencies.
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        UnparsedEntityURI f = new UnparsedEntityURI();
        f.addArgument(argument[0].reduce(dep, c));
        f.setStaticContext(getStaticContext());

        if (boundDocument==null && ((dep & Context.CONTEXT_NODE)!=0)) {
            f.boundDocument = (c.getContextNodeInfo()).getDocumentRoot();
        } else {
            f.boundDocument = this.boundDocument;
        }
        return f;
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

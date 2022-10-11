package com.icl.saxon.functions;
import com.icl.saxon.Controller;
import com.icl.saxon.Context;
import com.icl.saxon.KeyManager;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.EmptyEnumeration;
import com.icl.saxon.expr.Function;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.FragmentValue;
import com.icl.saxon.expr.TextFragmentValue;
import com.icl.saxon.expr.UnionEnumeration;
import com.icl.saxon.sort.LocalOrderComparer;

public class Key extends Function {

    private DocumentInfo boundDocument = null;
    private Controller boundController = null;

    public String getName() {
        return "key";
    };

    /**
    * Determine the data type of the expression
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }

    /**
    * Determine, in the case of an expression whose data type is Value.NODESET,
    * whether all the nodes in the node-set are guaranteed to come from the same
    * document as the context node. Used for optimization.
    */

    public boolean isContextDocumentNodeSet() {
        return true;
    }

    /**
    * Simplify and validate
    */

    public Expression simplify() throws XPathException {
    	// check that key() function is allowed in this context
    	if (!getStaticContext().allowsKeyFunction()) {
    		throw new XPathException("key() function cannot be used here");
    	}
        checkArgumentCount(2, 2);
        argument[0] = argument[0].simplify();
        argument[1] = argument[1].simplify();
        return this;
    }

    /**
    * Evaluate the expression
    */

    public Value evaluate(Context context) throws XPathException {
        NodeSetExtent nse = new NodeSetExtent(enumerate(context, true),
                                                LocalOrderComparer.getInstance());
        nse.setSorted(true);
        return nse;
    }

    /**
    * Enumerate the expression
    */

    public NodeEnumeration enumerate(Context context, boolean sorted) throws XPathException {
        String givenkeyname = argument[0].evaluateAsString(context);
        Value keyvalue = argument[1].evaluate(context);

        Controller controller = boundController;
        if (controller==null) controller = context.getController();

        DocumentInfo doc = boundDocument;
        if (doc==null) doc = (context.getContextNodeInfo()).getDocumentRoot();

        int fingerprint = getStaticContext().getFingerprint(givenkeyname, false);
        if (fingerprint==-1) {
            throw new XPathException("Key '" + givenkeyname + "' has not been defined");
        }
        return findKey(controller, doc, fingerprint, keyvalue);
    }

    /**
    * Construct an enumeration of nodes that satisfy the given key
    * @param controller The controller (to get the key definitions)
    * @param doc The document to search
    * @param keyname The absolute (expanded) name of the key
    * @param arg1 The value of the key (or nodeset containing the values)
    */

    private NodeEnumeration findKey(
                            Controller controller,
                            DocumentInfo doc,
                            int fingerprint,
                            Value arg1) throws XPathException {

        KeyManager keyManager = controller.getKeyManager();

        boolean sorted;

        if ((arg1 instanceof NodeSetValue) &&
        		!(arg1 instanceof FragmentValue || arg1 instanceof TextFragmentValue)) {

            NodeSetValue keyvals = (NodeSetValue)arg1;

            NodeEnumeration supplied = keyvals.enumerate();
            NodeEnumeration result = null;

            int inNodes = 0;
            while (supplied.hasMoreElements()) {
                inNodes++;
                NodeEnumeration onekeyresult =
                    keyManager.selectByKey(fingerprint,
                                           doc,
                                           supplied.nextElement().getStringValue(),
                                           controller);
                if (inNodes==1) {
                    result = onekeyresult;
                } else {
                    result = new UnionEnumeration(result, onekeyresult,
                                                  controller);
                }
            }
            if (inNodes==0) {
                return EmptyEnumeration.getInstance();
            } else {
                return result;
            }

        } else {
            return keyManager.selectByKey(fingerprint, doc, arg1.asString(), controller);
        }
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dependencies = argument[0].getDependencies();
        dependencies |= argument[1].getDependencies();
        if (boundDocument == null) {
            dependencies |= (Context.CONTEXT_NODE | Context.CONTEXT_DOCUMENT);
        }
        if (boundController == null) {
            dependencies |= Context.CONTROLLER;
        }
        return dependencies;
    }

    /**
    * Remove specified dependencies.
    */

    public Expression reduce(int dep, Context context)
            throws XPathException {
        Key key = new Key();
        key.addArgument(argument[0].reduce(dep, context));
        key.addArgument(argument[1].reduce(dep, context));
        if (boundDocument==null &&
                ((dep & (Context.CONTEXT_NODE | Context.CONTEXT_DOCUMENT))!=0)) {
            key.boundDocument = (context.getContextNodeInfo()).getDocumentRoot();
        } else {
            key.boundDocument = boundDocument;
        }
        if (boundController==null && ((dep & Context.CONTROLLER)!=0)) {
            key.boundController = context.getController();
        } else {
            key.boundController = boundController;
        }
        key.setStaticContext(getStaticContext());
        return key.simplify();

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

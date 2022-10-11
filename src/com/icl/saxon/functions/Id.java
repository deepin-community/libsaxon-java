package com.icl.saxon.functions;
import com.icl.saxon.Context;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.sort.LocalOrderComparer;

import java.util.StringTokenizer;
import java.util.Vector;



public class Id extends Function {

    private DocumentInfo boundDocument = null;

    public String getName() {
        return "id";
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
        checkArgumentCount(1, 1);
        argument[0] = argument[0].simplify();
        return this;
    }

    /**
    * Evaluate in a context where a node-set is required
    */

    public NodeSetValue evaluateAsNodeSet(Context context) throws XPathException {
        return findId(argument[0].evaluate(context), context);
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context context) throws XPathException {
        return evaluateAsNodeSet(context);
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = argument[0].getDependencies();
        if (boundDocument != null) {
            return dep;
        } else {
            return dep | Context.CONTEXT_NODE | Context.CONTEXT_DOCUMENT;
        }
    }

    /**
    * Remove specified dependencies.
    */

    public Expression reduce(int dependencies, Context context)
            throws XPathException {

        Id id = new Id();
        id.addArgument(argument[0].reduce(dependencies, context));
        id.setStaticContext(getStaticContext());
        id.boundDocument = boundDocument;

        if (boundDocument==null &&
                ((dependencies & (Context.CONTEXT_NODE | Context.CONTEXT_DOCUMENT)) != 0)) {
            id.boundDocument = (context.getContextNodeInfo()).getDocumentRoot();
        }
        return id;
    }

    /**
    * This method actually evaluates the function
    */

    private NodeSetValue findId(Value arg0, Context context) throws XPathException {
        Vector idrefresult = null;
        DocumentInfo doc;
        if (boundDocument==null) {
            doc = (context.getContextNodeInfo()).getDocumentRoot();
        } else {
            doc = boundDocument;
        }

        if ((arg0 instanceof NodeSetValue) &&
        		!(arg0 instanceof FragmentValue || arg0 instanceof FragmentValue)) {

            NodeEnumeration enm = ((NodeSetValue)arg0).enumerate();
            while (enm.hasMoreElements()) {
                NodeInfo node = enm.nextElement();
                String s = node.getStringValue();
                StringTokenizer st = new StringTokenizer(s);
                while (st.hasMoreTokens()) {
                    NodeInfo el = doc.selectID(st.nextToken());
                    if (el!=null) {
                        if (idrefresult==null) {
                            idrefresult = new Vector(2);
                        }
                        idrefresult.addElement(el);
                    }
                }
            }

        } else {

            String s = arg0.asString();
            StringTokenizer st = new StringTokenizer(s);
            while (st.hasMoreTokens()) {
                NodeInfo el = doc.selectID(st.nextToken());
                if (el!=null) {
                    if (idrefresult==null) {
                        idrefresult = new Vector(2);
                    }
                    idrefresult.addElement(el);
                }
            }
        }

        if (idrefresult==null) {
            return new EmptyNodeSet();
        }
        if (idrefresult.size() == 1) {
            return new SingletonNodeSet((NodeInfo)idrefresult.elementAt(0));
        }
        return new NodeSetExtent(idrefresult, LocalOrderComparer.getInstance());

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

package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NodeInfo;
import java.util.*;


public class Current extends Function {

    /**
    * Function name (for diagnostics)
    */

    public String getName() {
        return "current";
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
    * Simplify and validate.
    */

    public Expression simplify() throws XPathException {
        checkArgumentCount(0, 0);
        return this;
    }

    /**
    * Evaluate the function in a node-set context
    */

    public NodeSetValue evaluateAsNodeSet(Context c) throws XPathException {
        return new SingletonNodeSet(c.getCurrentNodeInfo());
    }

    /**
    * Evaluate in a general context
    */

    public Value evaluate(Context c) throws XPathException {
        return evaluateAsNodeSet(c);
    }

    /**
    * Determine the dependencies
    */

    public int getDependencies() {
       return Context.CURRENT_NODE;
    }

    /**
    * Reduce the dependencies
    */

    public Expression reduce(int dep, Context c) throws XPathException {
        if ((dep & Context.CURRENT_NODE) != 0) {
            return evaluateAsNodeSet(c);
        } else {
            return this;
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

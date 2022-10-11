package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;

/**
* A node-set value no nodes
*/

public final class EmptyNodeSet extends NodeSetValue {

    private static NodeInfo[] emptyArray = new NodeInfo[0];

    /**
    * Evaluate the Node Set. This guarantees to return the result in sorted order.
    * @param context The context for evaluation (not used)
    */

    public Value evaluate(Context context) {
        return this;
    }

    /**
    * Evaluate an expression as a NodeSet.
    * @param context The context in which the expression is to be evaluated
    * @return the value of the expression, evaluated in the current context
    */

    public NodeSetValue evaluateAsNodeSet(Context context) {
        return this;
    }

    /**
    * Set a flag to indicate whether the nodes are sorted. Used when the creator of the
    * node-set knows that they are already in document order.
    * @param isSorted true if the caller wishes to assert that the nodes are in document order
    * and do not need to be further sorted
    */

    public void setSorted(boolean isSorted) {}

    /**
    * Test whether the value is known to be sorted
    * @return true if the value is known to be sorted in document order, false if it is not
    * known whether it is sorted.
    */

    public boolean isSorted() {
        return true;
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
    * Convert to string value
    * @return an empty string
    */

    public String asString() {
        return "";
    }

    /**
    * Evaluate as a boolean.
    * @return false
    */

    public boolean asBoolean() {
        return false;
    }

    /**
    * Count the nodes in the node-set.
    * @return zero
    */

    public int getCount() {
        return 0;
    }


    /**
    * Sort the nodes into document order.
    * This does nothing if the nodes are already known to be sorted; to force a sort,
    * call setSorted(false)
    * @return the same NodeSetValue, after sorting. (Historic)
    */

    public NodeSetValue sort() {
        return this;
    }

    /**
    * Get the first node in the nodeset (in document order)
    * @return null
    */

    public NodeInfo getFirst() {
        return null;
    }


    /**
    * Test whether this nodeset "equals" another Value
    */

    public boolean equals(Value other) {
        if (other instanceof BooleanValue) {
            return !((BooleanValue)other).asBoolean();
        } else {
            return false;
        }
    }

    /**
    * Test whether this nodeset "not-equals" another Value
    */

    public boolean notEquals(Value other) {
        if (other instanceof BooleanValue) {
            return ((BooleanValue)other).asBoolean();
        } else {
            return false;
        }
    }

    /**
    * Return an enumeration of this nodeset value.
    */

    public NodeEnumeration enumerate() {
        return EmptyEnumeration.getInstance();
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


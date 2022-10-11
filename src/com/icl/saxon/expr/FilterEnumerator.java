package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.functions.*;

import java.util.*;
import java.lang.Math;

/**
* A FilterEnumerator filters an input NodeEnumeration using a filter expression.
* The complication is that on request, it must determine the value of the last() position,
* which requires a lookahead.
*/

public class FilterEnumerator implements NodeEnumeration {

    private NodeEnumeration base;
    private Expression filter;
    private int position = 0;
    private int last = -1;
    int min = 1;
    int max = Integer.MAX_VALUE;
    private NodeInfo current = null;
    private Context filterContext;
    private int dataType = Value.ANY;      // data type of filter expression
    private boolean positional = false;
    private boolean finished = false;      // allows early finish with a numeric filter
    private boolean finishAfterReject = false;
                                           // causes enumeration to terminate the first
                                           // time the predicate is false

    /**
    * Constructor
    * @param base A node-set expression denoting the absolute or relative set of nodes from which the
    * navigation path should start.
    * @param filter The expression defining the filter predicate
    * @param context The context in which the expression is being evaluated
    * @param finishAfterReject: terminate enumeration on first failure
    */

    public FilterEnumerator(NodeEnumeration base, Expression filter,
                            Context context, boolean finishAfterReject) throws XPathException {
        this.base = base;
        this.filter = filter;
        this.finishAfterReject = finishAfterReject;

        filterContext = context.newContext();

        this.dataType = filter.getDataType();

        if (filter instanceof NumericValue) {
            // if value is not an integer, it will never match
            double pos = ((NumericValue)filter).asNumber();
            if (Math.floor(pos)==pos) {
                min = (int)pos;
                max = min;
                positional = true;
            } else {
                finished = true;
            }
        } else if (filter instanceof PositionRange) {
            min = ((PositionRange)filter).getMinPosition();
            max = ((PositionRange)filter).getMaxPosition();
            positional = true;
        }

        if (base instanceof LastPositionFinder) {
            filterContext.setLastPositionFinder((LastPositionFinder)base);
        } else {
            // TODO: only need to do this if last() is used in the predicate
            this.base = new LookaheadEnumerator(base);
            filterContext.setLastPositionFinder((LastPositionFinder)this.base);
        }

        current = getNextMatchingElement();
    }

    /**
    * Test whether there are any more nodes available in the enumeration
    */

    public boolean hasMoreElements() {
        if (finished) return false;
        return current!=null;
    }

    /**
    * Get the next node if there is one
    */

    public NodeInfo nextElement() throws XPathException {
        NodeInfo node = current;
        current = getNextMatchingElement();
        return node;
    }

    /**
    * Get the next node that matches the filter predicate if there is one
    */

    private NodeInfo getNextMatchingElement() throws XPathException {
        while (!finished && base.hasMoreElements()) {
            NodeInfo next = base.nextElement();
            position++;
            if (matches(next)) {
                return next;
            } else if (finishAfterReject) {
                return null;
            }
        }
        return null;
    }

    /**
    * Determine whether a node matches the filter predicate
    */

    private boolean matches(NodeInfo node) throws XPathException {
        if (positional) {
            if (position<min) {
                return false;
            } else if (position>max) {
                finished = true;
                return false;
            } else {
                return true;
            }
        }
        filterContext.setPosition(position);
        filterContext.setContextNode(node);

        // If the data type is known at compile time, and cannot be numeric,
        // evaluate the expression directly as a boolean. This avoids expanding
        // a node-set unnecessarily.

        if (dataType==Value.NUMBER) {
            double req = (int)filter.evaluateAsNumber(filterContext);
            if ((double)position==req) {
                return true;
            } else {
                return false;
            }
        } else if (dataType==Value.ANY) {
            // have to determine the data type at run-time
            Value val = filter.evaluate(filterContext);
            if (val instanceof NumericValue) {
                return ((double)position==val.asNumber());
            } else {
                return val.asBoolean();
            }
        } else {
            // for any other type, evaluate the filter expression as a boolean
            return filter.evaluateAsBoolean(filterContext);
        }
    }

    /**
    * Determine whether the nodes are guaranteed to be in document order
    */

    public boolean isSorted() {
        return base.isSorted();
    }

    public boolean isReverseSorted() {
        return base.isReverseSorted();
    }

    /**
    * Determine whether the nodes are guaranteed to be peers
    */

    public boolean isPeer() {
        return base.isPeer();
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

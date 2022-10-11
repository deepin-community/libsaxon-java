package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.sort.*;
//import java.util.*;

/**
* A SortKeyEnumeration is NodeEnumeration that delivers the nodes sorted according to
* a specified sort key. <BR>
*
*/

public final class SortKeyEnumeration
        implements NodeEnumeration, LastPositionFinder, Sortable {


    // the nodes to be sorted
    protected NodeEnumeration base;

    // the sort key definitions
    private SortKeyDefinition[] sortkeys;

    // The nodes and keys are read into an array (nodeKeys) for sorting. This
    // array contains one "record" representing each node: the "record" contains
    // first, the NodeInfo itself, then an entry for each of its sort keys, in turn
    private int recordSize;
    private Object[] nodeKeys;

    // The number of nodes to be sorted. -1 means not yet known.
    private int count = -1;

    // The next node to be delivered from the sorted enumeration
    private int index = 0;

    // The context for the evaluation of sort keys
    private Context context;
    private Controller controller;
    private Comparer[] keyComparers;

    public SortKeyEnumeration(Context context, NodeEnumeration _base,
                                SortKeyDefinition[] sortkeys)
    throws XPathException {
        this.context = context.newContext();
        this.controller = context.getController();
        this.base = _base;
        this.sortkeys = sortkeys;
        recordSize = sortkeys.length + 1;

        keyComparers = new Comparer[sortkeys.length];
        for (int i=0; i<sortkeys.length; i++) {
            keyComparers[i] = sortkeys[i].getComparer(context);
        }

        // If any sortkey depends on position(), we must ensure the base enumeration is
        // in document order. If it uses last() (unlikely), we must ensure that the number
        // of nodes is known, so we sort it in this case also
        if (!base.isSorted()) {
            boolean mustBeSorted = false;
            for (int i=0; i<sortkeys.length; i++) {
                SortKeyDefinition sk = sortkeys[i];
                Expression k = sk.getSortKey();
                if ((k.getDependencies() & (Context.POSITION | Context.LAST)) != 0) {
                    mustBeSorted = true;
                    break;
                }
            }
            if (mustBeSorted) {
                NodeSetExtent nsv = new NodeSetExtent(base, controller);
                nsv.sort();
                base = nsv.enumerate();
            }
        }
    }

    /**
    * Determine whether there are more nodes
    */

    public boolean hasMoreElements() {
        if (count<0) {
            return base.hasMoreElements();
        } else {
            return index<count;
        }
    }

    /**
    * Get the next node, in sorted order
    */

    public NodeInfo nextElement() throws XPathException {
        if (count<0) {
            doSort();
        }
        return (NodeInfo)nodeKeys[(index++)*recordSize];
    }

    public boolean isSorted() {
        return true;
    }

    public boolean isReverseSorted()  {
        return false;
    }

    public boolean isPeer() {
        return base.isPeer();
    }

    public int getLastPosition() throws XPathException {
        if (base instanceof LastPositionFinder && !(base instanceof LookaheadEnumerator)) {
            return ((LastPositionFinder)base).getLastPosition();
        }
        if (count<0) doSort();
        return count;
    }

    private void buildArray() throws XPathException {
        int allocated;
        if (base instanceof LastPositionFinder && !(base instanceof LookaheadEnumerator)) {
            allocated = ((LastPositionFinder)base).getLastPosition();
            context.setLast(allocated);
        } else {
            allocated = 100;
        }
        nodeKeys = new Object[allocated * recordSize];
        count = 0;

        // initialise the array with data

        while (base.hasMoreElements()) {
            NodeInfo node = base.nextElement();
            if (count==allocated) {
                allocated *= 2;
                Object[] nk2 = new Object[allocated * recordSize];
                System.arraycopy(nodeKeys, 0, nk2, 0, count * recordSize);
                nodeKeys = nk2;
            }
            context.setCurrentNode(node);
            context.setContextNode(node);
            context.setPosition(count+1);

            int k = count*recordSize;
            nodeKeys[k] = node;
            for (int n=0; n<sortkeys.length; n++) {
                nodeKeys[k+n+1] = sortkeys[n].getSortKey().evaluateAsString(context);
            }
            count++;
        }
        //diag();
    }

    private void diag() {
        System.err.println("Diagnostic print of keys");
        for (int i=0; i<(count*recordSize); i++) {
            System.err.println(i + " : " + nodeKeys[i]);
        }
    }


    private void doSort() throws XPathException {
        buildArray();
        if (count<2) return;

        // sort the array

        QuickSort.sort(this, 0, count-1);
    }

    /**
    * Compare two nodes in sorted sequence
    * (needed to implement the Sortable interface)
    */

    public int compare(int a, int b) {
        int a1 = a*recordSize + 1;
        int b1 = b*recordSize + 1;
        for (int i=0; i<sortkeys.length; i++) {
            Comparer c = keyComparers[i];
            int comp = c.compare(nodeKeys[a1+i], nodeKeys[b1+i]);
            if (comp!=0) return comp;
        }
        // all sort keys equal: return the nodes in document order
        return controller.compare(
                    (NodeInfo)nodeKeys[a1-1],
                    (NodeInfo)nodeKeys[b1-1]);
    }

    /**
    * Swap two nodes (needed to implement the Sortable interface)
    */

    public void swap(int a, int b) {
        int a1 = a*recordSize;
        int b1 = b*recordSize;
        for (int i=0; i<recordSize; i++) {
            Object temp = nodeKeys[a1+i];
            nodeKeys[a1+i] = nodeKeys[b1+i];
            nodeKeys[b1+i] = temp;
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

package com.icl.saxon.sort;
import com.icl.saxon.om.NodeInfo;

/**
 * A Comparer used for comparing nodes in document order. This
 * comparer assumes that the nodes being compared come from the same document
 *
 * @author Michael H. Kay
 *
 */

public final class LocalOrderComparer implements NodeOrderComparer {

    private static LocalOrderComparer instance = new LocalOrderComparer();

    /**
    * Get an instance of a LocalOrderComparer. The class maintains no state
    * so this returns the same instance every time.
    */

    public static LocalOrderComparer getInstance() {
        return instance;
    }

    public int compare(NodeInfo a, NodeInfo b) {
        NodeInfo n1 = (NodeInfo)a;
        NodeInfo n2 = (NodeInfo)b;
        return n1.compareOrder(n2);
    }
}


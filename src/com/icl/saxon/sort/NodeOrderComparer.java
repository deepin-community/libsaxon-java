package com.icl.saxon.sort;
import com.icl.saxon.om.NodeInfo;

/**
 * A Comparer used for comparing nodes in document order
 *
 * @author Michael H. Kay
 *
 */

public interface NodeOrderComparer {

    /**
    * Compare two objects.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    */

    public int compare(NodeInfo a, NodeInfo b);

}

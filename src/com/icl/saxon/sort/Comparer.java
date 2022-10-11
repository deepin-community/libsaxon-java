package com.icl.saxon.sort;
import com.icl.saxon.*;

/**
 * A Comparer used for comparing keys
 *
 * @author Michael H. Kay
 *
 */

public abstract class Comparer {

    /**
    * Compare two objects.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are of the wrong type for this Comparer
    */

    public abstract int compare(Object a, Object b);

    /**
    * Set data type. The comparer has the option of returning a different comparer
    * once it knows the data type
    */

    public Comparer setDataType(String dataTypeURI, String dataTypeLocalName) {
        return this;
    }

    /**
    * Set order. The comparer has the option of returning a different comparer
    */

    public Comparer setOrder(boolean isAscending) {
        return (isAscending ? this : new DescendingComparer(this) );
    }

}

package com.icl.saxon;
import com.icl.saxon.expr.Value;


/**
* A ParameterSet is a set of parameters supplied when calling a template.
* It is a collection of name-value pairs
*/

public class ParameterSet 
{
	private int[] keys = new int[10];    
    private Value[] values = new Value[10];
    private int used = 0;


    /**
    * Add a parameter to the ParameterSet
    * @param fingerprint The fingerprint of the parameter name. 
    * @param value The value of the parameter
    */
    
    public void put (int fingerprint, Value value) {
        for (int i=0; i<used; i++) {
            if (keys[i]==fingerprint) {
                values[i]=value;
                return;
            }
        }
        if (used+1 > keys.length) {
        	int[] newkeys = new int[used*2];
            Value[] newvalues = new Value[used*2];
            System.arraycopy(values, 0, newvalues, 0, used);
            System.arraycopy(keys, 0, newkeys, 0, used);
            values = newvalues;
            keys = newkeys;
        }
        keys[used] = fingerprint;
        values[used++] = value;
    }

    /**
    * Get a parameter
    * @param fingerprint The fingerprint of the name. 
    * @return The value of the parameter, or null if not defined
    */

    public Value get (int fingerprint) {
        for (int i=0; i<used; i++) {
            if (keys[i]==fingerprint) {
                return values[i];
            }
        }
        return null;
    }

    /**
    * Clear all values
    */

    public void clear() {
        used = 0;
    }

}

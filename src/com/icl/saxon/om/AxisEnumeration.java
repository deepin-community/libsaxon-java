package com.icl.saxon.om;
import com.icl.saxon.expr.LastPositionFinder;

/**
* A NodeEnumeration is used to iterate over a list of nodes. An AxisEnumeration
* is a NodeEnumeration that throws no exceptions; it also supports the ability
* to find the last() position, again with no exceptions.
*/

public interface AxisEnumeration extends NodeEnumeration, LastPositionFinder  {

    /**
    * Determine whether there are more nodes to come. <BR>
    * (Note the term "Element" is used here in the sense of the standard Java Enumeration class,
    * it has nothing to do with XML elements).
    * @return true if there are more nodes
    */

    public boolean hasMoreElements();

    /**
    * Get the next node in sequence. <BR>
    * (Note the term "Element" is used here in the sense of the standard Java Enumeration class,
    * it has nothing to do with XML elements).
    * @return the next NodeInfo
    */

    public NodeInfo nextElement();

    /**
    * Get the last position
    */

    public int getLastPosition();

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

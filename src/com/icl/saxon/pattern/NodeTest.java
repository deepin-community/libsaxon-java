package com.icl.saxon.pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.Context;
import com.icl.saxon.expr.XPathException;

/**
  * A NodeTest is a simple kind of pattern that enables a context-free test of whether
  * a node has a particular
  * name. There are five kinds of name test: a full name test, a prefix test, and an
  * "any node of a given type" test, an "any node of any type" test, and a "no nodes"
  * test (used, e.g. for "@comment()")
  *
  * @author Michael H. Kay
  */

public abstract class NodeTest extends Pattern {

    /**
    * Test whether this node test is satisfied by a given node
    */

    public abstract boolean matches(NodeInfo node);

    /**
    * Test whether this node test is satisfied by a given node
    * @param nodeType The type of node to be matched
    * @param fingerprint identifies the expanded name of the node to be matched.
    * The value should be -1 for a node with no name.
    */

    public abstract boolean matches(short nodeType, int fingerprint);

    /**
    * Test whether this node test is satisfied by a given node, in a given Context
    */

    public final boolean matches(NodeInfo node, Context c) {
    	return matches(node);
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

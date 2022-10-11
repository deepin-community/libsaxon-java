package com.icl.saxon.om;
import com.icl.saxon.expr.XPathException;

/**
* An axis, that is a direction of navigation in the document structure.
*/

public final class Axis  {

    /**
    * Constants representing the axes
    */

    public static final byte ANCESTOR           = 0;
    public static final byte ANCESTOR_OR_SELF   = 1;
    public static final byte ATTRIBUTE          = 2;
    public static final byte CHILD              = 3;
    public static final byte DESCENDANT         = 4;
    public static final byte DESCENDANT_OR_SELF = 5;
    public static final byte FOLLOWING          = 6;
    public static final byte FOLLOWING_SIBLING  = 7;
    public static final byte NAMESPACE          = 8;
    public static final byte PARENT             = 9;
    public static final byte PRECEDING          = 10;
    public static final byte PRECEDING_SIBLING  = 11;
    public static final byte SELF               = 12;

    // preceding-or-ancestor axis gives all preceding nodes including ancestors,
    // in reverse document order

    public static final byte PRECEDING_OR_ANCESTOR = 13;


    /**
    * Table indicating the principal node type of each axis
    */

    public static final short[] principalNodeType =
    {
        NodeInfo.ELEMENT,       // ANCESTOR
        NodeInfo.ELEMENT,       // ANCESTOR_OR_SELF;
        NodeInfo.ATTRIBUTE,     // ATTRIBUTE;
        NodeInfo.ELEMENT,       // CHILD;
        NodeInfo.ELEMENT,       // DESCENDANT;
        NodeInfo.ELEMENT,       // DESCENDANT_OR_SELF;
        NodeInfo.ELEMENT,       // FOLLOWING;
        NodeInfo.ELEMENT,       // FOLLOWING_SIBLING;
        NodeInfo.NAMESPACE,     // NAMESPACE;
        NodeInfo.ELEMENT,       // PARENT;
        NodeInfo.ELEMENT,       // PRECEDING;
        NodeInfo.ELEMENT,       // PRECEDING_SIBLING;
        NodeInfo.ELEMENT,       // SELF;
        NodeInfo.ELEMENT,       // PRECEDING_OR_ANCESTOR;
    };

    /**
    * Table indicating for each axis whether it is in forwards document order
    */

    public static final boolean[] isForwards =
    {
        false,          // ANCESTOR
        false,          // ANCESTOR_OR_SELF;
        true,           // ATTRIBUTE;
        true,           // CHILD;
        true,           // DESCENDANT;
        true,           // DESCENDANT_OR_SELF;
        true,           // FOLLOWING;
        true,           // FOLLOWING_SIBLING;
        false,          // NAMESPACE;
        true,           // PARENT;
        false,          // PRECEDING;
        false,          // PRECEDING_SIBLING;
        true,           // SELF;
        false,          // PRECEDING_OR_ANCESTOR;
    };

    /**
    * Table indicating for each axis whether it is in reverse document order
    */

    public static final boolean[] isReverse =
    {
        true,           // ANCESTOR
        true,           // ANCESTOR_OR_SELF;
        false,          // ATTRIBUTE;
        false,          // CHILD;
        false,          // DESCENDANT;
        false,          // DESCENDANT_OR_SELF;
        false,          // FOLLOWING;
        false,          // FOLLOWING_SIBLING;
        false,          // NAMESPACE;
        true,           // PARENT;
        true,           // PRECEDING;
        true,           // PRECEDING_SIBLING;
        true,           // SELF;
        true,           // PRECEDING_OR_ANCESTOR;
    };

    /**
    * Table indicating for each axis whether it is a peer axis. An axis is a peer
    * axis if no node on the axis is an ancestor of another node on the axis.
    */

    public static final boolean[] isPeerAxis =
    {
        false,          // ANCESTOR
        false,          // ANCESTOR_OR_SELF;
        true,           // ATTRIBUTE;
        true,           // CHILD;
        false,          // DESCENDANT;
        false,          // DESCENDANT_OR_SELF;
        false,          // FOLLOWING;             # TODO: old code said true... #
        true,           // FOLLOWING_SIBLING;
        false,          // NAMESPACE;
        true,           // PARENT;
        false,          // PRECEDING;
        true,           // PRECEDING_SIBLING;
        true,           // SELF;
        false,          // PRECEDING_OR_ANCESTOR;
    };

    /**
    * Table indicating for each axis whether it is contained within the subtree
    * rooted at the origin node.
    */

    public static final boolean[] isSubtreeAxis =
    {
        false,          // ANCESTOR
        false,          // ANCESTOR_OR_SELF;
        true,           // ATTRIBUTE;
        true,           // CHILD;
        true,           // DESCENDANT;
        true,           // DESCENDANT_OR_SELF;
        false,          // FOLLOWING;
        false,          // FOLLOWING_SIBLING;
        false,          // NAMESPACE;
        false,          // PARENT;
        false,          // PRECEDING;
        false,          // PRECEDING_SIBLING;
        true,           // SELF;
        false,          // PRECEDING_OR_ANCESTOR;
    };

    /**
    * Table giving the name each axis
    */

    public static final String[] axisName =
    {
        "ancestor",             // ANCESTOR
        "ancestor-or-self",     // ANCESTOR_OR_SELF;
        "attribute",            // ATTRIBUTE;
        "child",                // CHILD;
        "descendant",           // DESCENDANT;
        "descendant-or-self",   // DESCENDANT_OR_SELF;
        "following",            // FOLLOWING;
        "following-sibling",    // FOLLOWING_SIBLING;
        "namespace",            // NAMESPACE;
        "parent",               // PARENT;
        "preceding",            // PRECEDING;
        "preceding-sibling",    // PRECEDING_SIBLING;
        "self",                 // SELF;
        "preceding-or-ancestor",// PRECEDING_OR_ANCESTOR;
    };

    /**
    * Resolve an axis name into a symbolic constant representing the axis
    */

    public static byte getAxisNumber(String name) throws XPathException {
        if (name.equals("ancestor"))                return ANCESTOR;
        if (name.equals("ancestor-or-self"))        return ANCESTOR_OR_SELF;
        if (name.equals("attribute"))               return ATTRIBUTE;
        if (name.equals("child"))                   return CHILD;
        if (name.equals("descendant"))              return DESCENDANT;
        if (name.equals("descendant-or-self"))      return DESCENDANT_OR_SELF;
        if (name.equals("following"))               return FOLLOWING;
        if (name.equals("following-sibling"))       return FOLLOWING_SIBLING;
        if (name.equals("namespace"))               return NAMESPACE;
        if (name.equals("parent"))                  return PARENT;
        if (name.equals("preceding"))               return PRECEDING;
        if (name.equals("preceding-sibling"))       return PRECEDING_SIBLING;
        if (name.equals("self"))                    return SELF;
        // preceding-or-ancestor cannot be used in an XPath expression
        throw new XPathException("Unknown axis name: " + name);
    }



}

/*
    // a list for any future cut-and-pasting...
    ANCESTOR
    ANCESTOR_OR_SELF;
    ATTRIBUTE;
    CHILD;
    DESCENDANT;
    DESCENDANT_OR_SELF;
    FOLLOWING;
    FOLLOWING_SIBLING;
    NAMESPACE;
    PARENT;
    PRECEDING;
    PRECEDING_SIBLING;
    SELF;
*/


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

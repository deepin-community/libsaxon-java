package com.icl.saxon.pattern;
import com.icl.saxon.Context;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.ExpressionParser;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.XPathException;


/**
* A Pattern represents the result of parsing an XSLT pattern string. <br>
* Patterns are created by calling the static method Pattern.make(string). <br>
* The pattern is used to test a particular node by calling match().
*/

public abstract class Pattern {

    protected StaticContext staticContext;
    protected String originalText;

    /**
    * Static method to make a Pattern by parsing a String. <br>
    * @param pattern The pattern text as a String
    * @param env An object defining the compile-time context for the expression
    * @return The pattern object
    */

    public static Pattern make(String pattern, StaticContext env) throws XPathException {

        Pattern pat = (new ExpressionParser()).parsePattern(pattern, env).simplify();
        // previously used a shared parser instance: this wasn't thread-safe (bug 4.5/005)
        pat.staticContext = env;

        // set the pattern text for use in diagnostics
        pat.setOriginalText(pattern);
        return pat;
    }

	/**
	* Set the original text of the pattern for use in diagnostics
	*/

	public void setOriginalText(String text) {
		originalText = text;
	}

    /**
    * Simplify the pattern by applying any context-independent optimisations.
    * Default implementation does nothing.
    * @return the optimised Pattern
    */

    public Pattern simplify() throws XPathException {
        return this;
    }

    /**
    * Set the static context used when the pattern was parsed
    */

    public final void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Determine the static context used when the pattern was parsed
    */

    public StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Determine whether this Pattern matches the given Node
    * @param node The NodeInfo representing the Element or other node to be tested against the Pattern
    * @param context The context in which the match is to take place. Only relevant if the pattern
    * uses variables.
    * @return true if the node matches the Pattern, false otherwise
    */

    public abstract boolean matches(NodeInfo node, Context context) throws XPathException;

    /**
    * Determine the types of nodes to which this pattern applies. Used for optimisation.
    * For patterns that match nodes of several types, return NodeInfo.NODE
    * @return the type of node matched by this pattern. e.g. NodeInfo.ELEMENT or NodeInfo.TEXT
    */

    public short getNodeType() {
        return NodeInfo.NODE;
    }

    /**
    * Determine the name fingerprint of nodes to which this pattern applies. Used for
    * optimisation.
    * @return A fingerprint that the nodes must match, or null
    * Otherwise return null.
    */

    public int getFingerprint() {
        return -1;
    }

    /**
    * Determine the default priority to use if this pattern appears as a match pattern
    * for a template with no explicit priority attribute.
    */

    public double getDefaultPriority() {
        return 0.5;
    }

    /**
    * Get the system id of the entity in which the pattern occurred
    */

    public String getSystemId() {
		return staticContext.getSystemId();
    }

    /**
    * Get the line number on which the pattern was defined
    */

    public int getLineNumber() {
		return staticContext.getLineNumber();
    }

    /**
    * Get the original pattern text
    */

    public String toString() {
    	return originalText;
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
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved.
//
// Contributor(s): Michael Kay, Peter Bryant
//

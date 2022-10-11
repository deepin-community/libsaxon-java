package com.icl.saxon.expr;
import com.icl.saxon.Binding;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NamespaceTest;
import javax.xml.transform.TransformerException;


/**
* A StaticContext contains the information needed while an expression or pattern
* is being parsed. The information is also sometimes needed at run-time.
*/

public interface StaticContext {

	/**
	* Copy the context with a different namepool
	*/

	public StaticContext makeRuntimeContext(NamePool pool);

    /**
    * Get the System ID of the container of the expression. This is the containing
    * entity (file) and is therefore useful for diagnostics. Use getBaseURI() to get
    * the base URI, which may be different.
    */

    public String getSystemId();

    /**
    * Get the line number of the expression within its containing entity
    * Returns -1 if no line number is available
    */

    public int getLineNumber();

    /**
    * Get the Base URI of the stylesheet element, for resolving any relative URI's used
    * in the expression.
    * Used by the document() function.
    */

    public String getBaseURI();

    /**
    * Get the URI for a prefix, using this Element as the context for namespace resolution
    * @param prefix The prefix
    * @throw XPathException if the prefix is not declared
    */

    public String getURIForPrefix(String prefix) throws XPathException;

    /**
    * Make a NameCode, using this Element as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    */

    public int makeNameCode(String qname, boolean useDefault) throws XPathException;

    /**
    * Get a fingerprint for a name, using this as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    * @return -1 if the name is not already present in the name pool
    */

    public int getFingerprint(String qname, boolean useDefault) throws XPathException;

	/**
	* Make a NameTest, using this element as the context for namespace resolution
	*/

	public NameTest makeNameTest(short nodeType, String qname, boolean useDefault)
		throws XPathException;

	/**
	* Make a NamespaceTest, using this element as the context for namespace resolution
	*/

	public NamespaceTest makeNamespaceTest(short nodeType, String prefix)
			throws XPathException;

    /**
    * Bind a variable to an object that can be used to refer to it
    * @param fingerprint The fingerprint of the variable name
    * @return a Binding object that can be used to identify it in the Bindery
    * @throws XPathException if the variable has not been declared, or if the context
    * does not allow the use of variables
    */

    public Binding bindVariable(int fingerprint) throws XPathException;

    /**
    * Determine whether a given URI code identifies an extension element namespace
    */

    public boolean isExtensionNamespace(short uriCode) throws XPathException;

    /**
    * Determine whether forwards-compatible mode is enabled
    */

    public boolean forwardsCompatibleModeIsEnabled() throws XPathException;

	/*
    * Get a Function declared using a saxon:function element in the stylesheet
    * @param fingerprint the fingerprint of the name of the function
    * @return the Function object represented by this saxon:function; or null if not found
    */

    public Function getStyleSheetFunction(int fingerprint) throws XPathException;

    /**
    * Get an external Java class corresponding to a given namespace prefix, if there is
    * one.
    * @param uri The namespace URI corresponding to the prefix used in the function call.
    * @return the Java class if a suitable class exists, otherwise return null.
    * @throws TransformerException if the class is found, but cannot be loaded.
    */

    public Class getExternalJavaClass(String uri) throws TransformerException;

    /**
    * Determine if an extension element is available
    */

    public boolean isElementAvailable(String qname) throws XPathException;

    /**
    * Determine if a function is available
    */

    public boolean isFunctionAvailable(String qname) throws XPathException;

	/**
	* Determine whether the key() function is permmitted in this context
	*/

	public boolean allowsKeyFunction();

    /**
    * Get the effective XSLT version in this region of the stylesheet
    */

    public String getVersion();

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

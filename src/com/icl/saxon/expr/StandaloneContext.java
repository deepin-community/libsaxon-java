package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NamespaceTest;

import java.util.Hashtable;

/**
* A StandaloneContext provides a context for parsing an expression or pattern appearing
* in a context other than a stylesheet.
*/

public class StandaloneContext implements StaticContext {

	private NamePool namePool;
	private Hashtable namespaces = new Hashtable();

	/**
	* Create a StandaloneContext using the default NamePool
	*/

	public StandaloneContext() {
	    this(NamePool.getDefaultNamePool());
	}

	/**
	* Create a StandaloneContext using a specific NamePool
	*/

	public StandaloneContext(NamePool pool) {
		namePool = pool;
		declareNamespace("xml", Namespace.XML);
		declareNamespace("xsl", Namespace.XSLT);
		declareNamespace("saxon", Namespace.SAXON);
		declareNamespace("", "");
	}

	/**
	* Declare a namespace whose prefix can be used in expressions
	*/

	public void declareNamespace(String prefix, String uri) {
		namespaces.put(prefix, uri);
		namePool.allocateNamespaceCode(prefix, uri);
	}


	/**
	* Copy the context with a different namepool. Not implemented, returns null.
	*/

	public StaticContext makeRuntimeContext(NamePool pool) {
		return null;
	}

    /**
    * Get the system ID of the container of the expression
    * @return "" always
    */

    public String getSystemId() {
        return "";
    }

    /**
    * Get the Base URI of the stylesheet element, for resolving any relative URI's used
    * in the expression.
    * Used by the document() function.
    * @return "" always
    */

    public String getBaseURI() {
        return "";
    }

    /**
    * Get the line number of the expression within that container
    * @return -1 always
    */

    public int getLineNumber() {
        return -1;
    }

    /**
    * Get the URI for a prefix, using this Element as the context for namespace resolution
    * @param prefix The prefix
    * @throw XPathException if the prefix is not declared
    */

    public String getURIForPrefix(String prefix) throws XPathException {
    	String uri = (String)namespaces.get(prefix);
    	if (uri==null) {
    		throw new XPathException("Prefix " + prefix + " has not been declared");
    	}
    	return uri;
    }

    /**
    * Make a NameCode, using this Element as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    */

    public final int makeNameCode(String qname, boolean useDefault) throws XPathException {
		String prefix = Name.getPrefix(qname);
		String localName = Name.getLocalName(qname);
		String uri;
		if (prefix.equals("") && useDefault) {
			uri = "";
		} else {
			uri = getURIForPrefix(prefix);
		}
		return namePool.allocate(prefix, uri, localName);
	}

    /**
    * Make a fingerprint, using this Element as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    * @throw XPathException if the name is not already present in the namepool.
    */

    public final int getFingerprint(String qname, boolean useDefault) throws XPathException {
		String prefix = Name.getPrefix(qname);
		String localName = Name.getLocalName(qname);
		String uri;
		if (prefix.equals("") && useDefault) {
			uri = "";
		} else {
			uri = getURIForPrefix(prefix);
		}
		return namePool.getFingerprint(uri, localName);

	}

	/**
	* Make a NameTest, using this element as the context for namespace resolution
	*/

	public NameTest makeNameTest(short nodeType, String qname, boolean useDefault)
		throws XPathException {
		return new NameTest(nodeType, makeNameCode(qname, useDefault));
	}

	/**
	* Make a NamespaceTest, using this element as the context for namespace resolution
	*/

	public NamespaceTest makeNamespaceTest(short nodeType, String prefix)
			throws XPathException {
		return new NamespaceTest(namePool, nodeType, getURICodeForPrefix(prefix));
	}

   /**
    * Search the NamespaceList for a given prefix, returning the corresponding URI code.
    * @param prefix The prefix to be matched. To find the default namespace, supply ""
    * @return The URI code corresponding to this namespace. If it is an unnamed default namespace,
    * return "".
    * @throws XPathException if the prefix has not been declared on this element or a containing
    * element.
    */

    private short getURICodeForPrefix(String prefix) throws XPathException {
		String uri = getURIForPrefix(prefix);
		return namePool.getCodeForURI(uri);
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    */

    public Binding bindVariable(int fingerprint) throws XPathException {
        throw new XPathException("Variables are not allowed in a standalone expression");
    }

    /**
    * Determine whether a given URI identifies an extension element namespace
    */

    public boolean isExtensionNamespace(short uriCode) {
        return false;
    }

    /**
    * Determine whether forwards-compatible mode is enabled
    */

    public boolean forwardsCompatibleModeIsEnabled() {
        return false;
    }

	/*
    * Get a Function declared using a saxon:function element in the stylesheet
    * @param fingerprint the fingerprint of the name of the function
    * @return the Function object represented by this saxon:function; or null if not found
    */

    public Function getStyleSheetFunction(int fingerprint) throws XPathException {
    	return null;
    }

    /**
    * Get an external Java class corresponding to a given namespace prefix, if there is
    * one.
    * @param uri The namespace URI corresponding to the prefix used in the function call.
    * @return the Java class name if a suitable class exists, otherwise return null. This
    * implementation always returns null.
    */

    public Class getExternalJavaClass(String uri) {
        return null;
    }

    /**
    * Determine if an extension element is available
    */

    public boolean isElementAvailable(String qname) throws XPathException {
    	return false;
    }

    /**
    * Determine if a function is available
    */

    public boolean isFunctionAvailable(String qname) throws XPathException {

    	String prefix = Name.getPrefix(qname);
    	if (prefix.equals("")) {
    		return ExpressionParser.makeSystemFunction(qname)!=null;
    	}

    	return false;   // no user functions allowed in standalone context.

    	//String uri = getURIForPrefix(prefix);
    	//String localName = Name.getLocalName(qname);
    	//
    	//FunctionProxy fp = new FunctionProxy();
    	//return fp.setMethod(uri, localName);

	}

	/**
	* Determine whether the key() function is permmitted in this context
	*/

	public boolean allowsKeyFunction() {
		return false;
	}

    /**
    * Get the effective XSLT version in this region of the stylesheet
    */

    public String getVersion() {
        return "1.1";
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

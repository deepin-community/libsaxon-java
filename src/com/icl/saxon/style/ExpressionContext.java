package com.icl.saxon.style;
import com.icl.saxon.Loader;
import com.icl.saxon.FeatureKeys;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.Function;
import com.icl.saxon.expr.ExpressionParser;
import com.icl.saxon.expr.FunctionProxy;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.Binding;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.pattern.NameTest;
import com.icl.saxon.pattern.NamespaceTest;
import javax.xml.transform.TransformerException;


/**
* An ExpressionContext represents the context for an XPath expression written
* in the stylesheet.
*/

public class ExpressionContext implements StaticContext {

	private StyleElement element;
	private NamePool namePool;

	public ExpressionContext(StyleElement styleElement) {
		element = styleElement;
		namePool = styleElement.getNamePool();	// the stylesheet namepool
	}

	/**
	* Create a context for parsing XPath expressions at runtime, using this styelsheet
	* element for namespace declarations, variables, etc, but using the runtime NamePool
	*/

	public StaticContext makeRuntimeContext(NamePool pool) {
		ExpressionContext ec = new ExpressionContext(element);
		ec.namePool = pool;
		return ec;
	}

    /**
    * Get the System ID of the entity containing the expression (used for diagnostics)
    */

    public String getSystemId() {
    	return element.getSystemId();
    }

    /**
    * Get the line number of the expression within its containing entity
    * Returns -1 if no line number is available
    */

    public int getLineNumber() {
    	return element.getLineNumber();
    }

    /**
    * Get the Base URI of the element containing the expression, for resolving any
    * relative URI's used in the expression.
    * Used by the document() function.
    */

    public String getBaseURI() {
        return element.getBaseURI();
    }

    /**
    * Get the URI for a prefix, using this Element as the context for namespace resolution
    * @param prefix The prefix
    * @throw XPathxception if the prefix is not declared
    */

    public String getURIForPrefix(String prefix) throws XPathException {
        try {
    	    short uriCode = element.getURICodeForPrefix(prefix);
    	    return namePool.getURIFromURICode(uriCode);
    	} catch (NamespaceException err) {
    	    throw new XPathException(err.getMessage());
    	}
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
        try {
            if (prefix.equals("")) {
                short uriCode = 0;

                if (!Name.isNCName(qname)) {
                    throw new XPathException("Name " + qname + " contains invalid characters");
                }

                if (useDefault) {
                    uriCode = element.getURICodeForPrefix(prefix);
                }

    			return namePool.allocate(prefix, uriCode, qname);

            } else {
                String localName = Name.getLocalName(qname);
                short uriCode = element.getURICodeForPrefix(prefix);
    			return namePool.allocate(prefix, uriCode, localName);
            }
        } catch (NamespaceException err) {
            throw new XPathException("Namespace prefix " + prefix + " has not been declared");
        }

	}

    /**
    * Get a fingerprint for a name, using this as the context for namespace resolution
    * @param qname The name as written, in the form "[prefix:]localname"
    * @boolean useDefault Defines the action when there is no prefix. If true, use
    * the default namespace URI (as for element names). If false, use no namespace URI
    * (as for attribute names).
    * @return -1 if the name is not already present in the name pool
    */

    public int getFingerprint(String qname, boolean useDefault) throws XPathException {

        String prefix = Name.getPrefix(qname);
        if (prefix.equals("")) {
            String uri = "";

            if (useDefault) {
                uri = getURIForPrefix(prefix);
            }

			return namePool.getFingerprint(uri, qname);

        } else {
            String localName = Name.getLocalName(qname);
            String uri = getURIForPrefix(prefix);
			return namePool.getFingerprint(uri, localName);
        }
    }

	/**
	* Make a NameTest, using this element as the context for namespace resolution
	*/

	public NameTest makeNameTest(short nodeType, String qname, boolean useDefault)
			throws XPathException {
        int nameCode = makeNameCode(qname, useDefault);
		NameTest nt = new NameTest(nodeType, nameCode);
		nt.setOriginalText(qname);
		return nt;
	}

	/**
	* Make a NamespaceTest, using this element as the context for namespace resolution
	*/

	public NamespaceTest makeNamespaceTest(short nodeType, String prefix)
			throws XPathException {
        try {
    	    short uriCode = element.getURICodeForPrefix(prefix);
    	    NamespaceTest nt = new NamespaceTest(namePool, nodeType, uriCode);
    	    nt.setOriginalText(prefix + ":*");
    	    return nt;
    	} catch (NamespaceException err) {
    	    throw new XPathException(err.getMessage());
    	}
	}

    /**
    * Bind a variable to an object that can be used to refer to it
    * @param fingerprint The fingerprint of the variable name
    * @return a Binding object that can be used to identify it in the Bindery
    * @throws XPathException if the variable has not been declared
    */

    public Binding bindVariable(int fingerprint) throws XPathException {
 	    return element.bindVariable(fingerprint);
    }

    /**
    * Determine whether a given URI code identifies an extension element namespace
    */

    public boolean isExtensionNamespace(short uriCode) throws XPathException {
    	return element.isExtensionNamespace(uriCode);
    }

    /**
    * Determine whether forwards-compatible mode is enabled
    */

    public boolean forwardsCompatibleModeIsEnabled() throws XPathException {
    	return element.forwardsCompatibleModeIsEnabled();
    }

	/*
    * Get a Function declared using a saxon:function element in the stylesheet
    * @param fingerprint the fingerprint of the name of the function
    * @return the Function object represented by this saxon:function; or null if not found
    */

    public Function getStyleSheetFunction(int fingerprint) throws XPathException {
    	return element.getStyleSheetFunction(fingerprint);
    }

    /**
    * Get an external Java class corresponding to a given namespace prefix, if there is
    * one.
    * @param uri The namespace URI corresponding to the prefix used in the function call.
    * @return the Java class name if a suitable class exists, otherwise return null.
    */

    public Class getExternalJavaClass(String uri) throws TransformerException {

        // First try to use an xsl:script element if there is one

        XSLStyleSheet sse = element.getPrincipalStyleSheet();
        Class c = sse.getExternalJavaClass(uri);
        if (c != null) {
            return c;
        }

        // Try well-known namespaces (Saxon and EXSLT extensions)

        if (uri.equals(Namespace.SAXON)) {
            return com.icl.saxon.functions.Extensions.class;

        } else if (uri.equals(Namespace.EXSLT_COMMON)) {
            return com.icl.saxon.exslt.Common.class;
        } else if (uri.equals(Namespace.EXSLT_SETS)) {
            return com.icl.saxon.exslt.Sets.class;
        } else if (uri.equals(Namespace.EXSLT_MATH)) {
            return com.icl.saxon.exslt.Math.class;
        } else if (uri.equals(Namespace.EXSLT_DATES_AND_TIMES)) {
            return com.icl.saxon.exslt.Date.class;

        // Failing that, try the old proprietary mechanisms from XSLT 1.0 days

        } else {
            if (!((Boolean)sse.getPreparedStyleSheet().getTransformerFactory().
                    getAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS)).booleanValue()) {
                throw new TransformerException("Calls to external functions have been disabled");
            }
            try {

                // support the URN format java:full.class.Name

                if (uri.startsWith("java:")) {
                    return Loader.getClass(uri.substring(5));
                }

                // extract the class name as anything in the URI after the last "/"
                // if there is one, or the whole class name otherwise

                int slash = uri.lastIndexOf('/');
                if (slash<0) {
                    return Loader.getClass(uri);
                } else if (slash==uri.length()-1) {
                    return null;
                } else {
                    return Loader.getClass(uri.substring(slash+1));
                }
            } catch (TransformerException err) {
                return null;
            }
        }
    }

    /**
    * Determine if an extension element is available
    * @throws XPathException if the name is invalid or the prefix is not declared
    */

    public boolean isElementAvailable(String qname) throws XPathException {
        if (!Name.isQName(qname)) {
            throw new XPathException("Invalid QName: " + qname);
        }

	    String prefix = Name.getPrefix(qname);
	    String localName = Name.getLocalName(qname);
	    String uri = getURIForPrefix(prefix);

    	return element.getPreparedStyleSheet().
    						getStyleNodeFactory().isElementAvailable(uri, localName);
    }

    /**
    * Determine if a function is available
    */

    public boolean isFunctionAvailable(String qname) throws XPathException {
        if (!Name.isQName(qname)) {
            throw new XPathException("Invalid QName: " + qname);
        }
      	String prefix = Name.getPrefix(qname);
      	String uri = getURIForPrefix(prefix);
      	try {
        	if (prefix.equals("")) {
        		return ExpressionParser.makeSystemFunction(qname)!=null;
        	}

    		int fingerprint = getFingerprint(qname, false);
    		if (fingerprint>=0) {
    			Function f = getStyleSheetFunction(fingerprint);
    			if (f!=null) return true;
    		}

          	Class theClass = getExternalJavaClass(uri);
          	if (theClass==null) {
          	    return false;
          	}

        	String localName = Name.getLocalName(qname);

        	FunctionProxy fp = new FunctionProxy();
        	return fp.setFunctionName(theClass, localName);
        } catch (Exception err) {
            return false;
        }
	}

	/**
	* Determine whether the key() function is permmitted in this context
	*/

	public boolean allowsKeyFunction() {
		return !(element instanceof XSLKey);
	}

    /**
    * Get the effective XSLT version in this region of the stylesheet
    */

    public String getVersion() {
        return element.getVersion();
    }

	/**
	* String representation for diagnostics
	*/

	public String toString() {
	    return "Expression Context at " + element.toString();
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

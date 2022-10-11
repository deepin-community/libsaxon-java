package com.icl.saxon.style;
import com.icl.saxon.Loader;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.ElementImpl;
import com.icl.saxon.tree.NodeFactory;
import org.xml.sax.Locator;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.util.Hashtable;

/**
  * Class StyleNodeFactory. <br>
  * A Factory for nodes in the stylesheet tree. <br>
  * Currently only allows Element nodes to be user-constructed.
  * @author Michael H. Kay
  */

public class StyleNodeFactory implements NodeFactory {

    Hashtable userStyles = new Hashtable();
    NamePool namePool;
    StandardNames sn;

    public StyleNodeFactory(NamePool pool) {

		namePool = pool;
		sn = pool.getStandardNames();
    }

    public StandardNames getStandardNames() {
    	return namePool.getStandardNames();
    }

    /**
    * Create an Element node. Note, if there is an error detected while constructing
    * the Element, we add the element anyway, and return success, but flag the element
    * with a validation error. In principle this should allow us to report more than
    * one error from a single compilation.
    * @param parent The parent of the element
    * @param nameCode The element name
    * @param attlist the attribute list
    * @param namespaces the list of namespace codes representing namespace declarations for this element
    * @param namespacesUsed the number of items in the namespaces array that are actually used
    * @param locator the location of this element in the XML source
    * @param sequence sequence number of this element
    */

    public ElementImpl makeElementNode(
                        NodeInfo parent,
                        int nameCode,
                        AttributeCollection attlist,
                        int[] namespaces,
                        int namespacesUsed,
                        Locator locator,
                        int sequence)
    {
    	// System.err.println("Make element node " + nameCode);
        boolean toplevel = (parent instanceof XSLStyleSheet);

        String baseURI = null;
        int lineNumber = -1;

        if (locator!=null) {
            baseURI = locator.getSystemId();
            lineNumber = locator.getLineNumber();
        }

    	int f = nameCode&0xfffff;
    	StyleElement e = makeXSLElement(f);

		if (e != null) {
			try {
	        	e.setNamespaceDeclarations(namespaces, namespacesUsed);
	            e.initialise(nameCode, attlist, parent, baseURI, lineNumber, sequence);
	            if (e instanceof XSLStyleSheet) {
                    e.processVersionAttribute(sn.VERSION);
		            e.processExtensionElementAttribute(sn.EXTENSION_ELEMENT_PREFIXES);
		            e.processExcludedNamespaces(sn.EXCLUDE_RESULT_PREFIXES);
		        }
	        } catch (TransformerException err) {
	            e.setValidationError(err, StyleElement.REPORT_UNLESS_FORWARDS_COMPATIBLE);
	        }
            return e;

        } else {

	        short uriCode = namePool.getURICode(nameCode);
	        String localname = namePool.getLocalName(nameCode);

	        Class assumedClass = LiteralResultElement.class;

	        // We can't work out the final class of the node until we've examined its attributes
	        // such as version and extension-element-prefixes; but we can have a good guess, and
	        // change it later if need be.

			StyleElement temp = null;
			boolean assumedSaxonElement = false;

			// map EXSLT func:function and func:result onto Saxon equivalents

			if (uriCode == Namespace.EXSLT_FUNCTIONS_CODE) {
	        	temp = makeExsltFunctionsElement(f);
	        	if (temp!=null) {
	        		assumedClass = temp.getClass();
	        		assumedSaxonElement = true;
	        	}

	        } else if (uriCode == Namespace.SAXON_CODE) {
	        	temp = makeSaxonElement(f);
	        	if (temp!=null) {
	        		assumedClass = temp.getClass();
	        		assumedSaxonElement = true;
	        	}
	        }
			if (temp==null) {
		        temp = new LiteralResultElement();
		    }

	        temp.setNamespaceDeclarations(namespaces, namespacesUsed);

	        try {
	            temp.initialise(nameCode, attlist, parent, baseURI, lineNumber, sequence);
	            temp.processExtensionElementAttribute(sn.XSL_EXTENSION_ELEMENT_PREFIXES);
	            temp.processExcludedNamespaces(sn.XSL_EXCLUDE_RESULT_PREFIXES);
	            temp.processVersionAttribute(sn.XSL_VERSION);
	        } catch (TransformerException err) {
	            temp.setValidationError(err, StyleElement.REPORT_UNLESS_FORWARDS_COMPATIBLE);
	        }

	        // Now we work out what class of element we really wanted, and change it if necessary

	        TransformerException reason = null;
	        Class actualClass = LiteralResultElement.class;

	        if (uriCode == Namespace.XSLT_CODE) {
                reason = new TransformerConfigurationException("Unknown XSLT element: " + localname);
                actualClass = AbsentExtensionElement.class;
                temp.setValidationError(reason, StyleElement.REPORT_UNLESS_FORWARDS_COMPATIBLE);
	        } else if (uriCode == Namespace.SAXON_CODE || uriCode == Namespace.EXSLT_FUNCTIONS_CODE) {
	        	if (toplevel || temp.isExtensionNamespace(uriCode)) {
	        		if (assumedSaxonElement) {
	        			// all is well
	        			actualClass = assumedClass;
	        		} else {
	        			actualClass = AbsentExtensionElement.class;
	        			reason = new TransformerConfigurationException(
	        			                "Unknown Saxon extension element: " + localname);
                        temp.setValidationError(reason, StyleElement.REPORT_IF_INSTANTIATED);
	        		}
	        	} else {
	        		actualClass = LiteralResultElement.class;
	        	}
	        } else if (temp.isExtensionNamespace(uriCode) && !toplevel) {
            	Integer nameKey = new Integer(nameCode&0xfffff);

                actualClass = (Class)userStyles.get(nameKey);
                if (actualClass==null) {
                     ExtensionElementFactory factory = getFactory(uriCode);
                     if (factory != null) {
                        actualClass = factory.getExtensionClass(localname);
                        if (actualClass != null) {
                            userStyles.put(nameKey, actualClass);             // for quicker access next time
                        }
                     }
                     if (actualClass == null) {

                        // if we can't instantiate an extension element, we don't give up
                        // immediately, because there might be an xsl:fallback defined. We
                        // create a surrogate element called AbsentExtensionElement, and
                        // save the reason for failure just in case there is no xsl:fallback

                        actualClass = AbsentExtensionElement.class;
                        reason = new TransformerConfigurationException("Unknown extension element");
                        temp.setValidationError(reason, StyleElement.REPORT_IF_INSTANTIATED);
                    }
                }
	        } else {
	        	actualClass = LiteralResultElement.class;
	        }

	        StyleElement node;
	        if (!actualClass.equals(assumedClass)) {
	            try {
	                node = (StyleElement)actualClass.newInstance();
	                //if (reason!=null) {
	                //    node.setValidationError(reason);
	                //}
	            } catch (java.lang.InstantiationException err1) {
	                throw new TransformerFactoryConfigurationError(err1, "Failed to create instance of " + actualClass.getName());
	            } catch (java.lang.IllegalAccessException err2) {
	                throw new TransformerFactoryConfigurationError(err2, "Failed to access class " + actualClass.getName());
	            }
	            node.substituteFor(temp);   // replace temporary node with the new one
	        } else {
	            node = temp;    // the original element will do the job
	        }
	        return node;
	    }
    }

	/**
	* Make an XSL element node
	*/

	private StyleElement makeXSLElement(int f) {

		StyleElement e = null;

		if (f==sn.XSL_APPLY_IMPORTS) 			e=new XSLApplyImports();
		else if (f==sn.XSL_APPLY_TEMPLATES) 	e=new XSLApplyTemplates();
		else if (f==sn.XSL_ATTRIBUTE) 			e=new XSLAttribute();
		else if (f==sn.XSL_ATTRIBUTE_SET) 		e=new XSLAttributeSet();
		else if (f==sn.XSL_CALL_TEMPLATE) 		e=new XSLCallTemplate();
		else if (f==sn.XSL_CHOOSE) 				e=new XSLChoose();
		else if (f==sn.XSL_COMMENT) 			e=new XSLComment();
		else if (f==sn.XSL_COPY) 				e=new XSLCopy();
		else if (f==sn.XSL_COPY_OF) 			e=new XSLCopyOf();
		else if (f==sn.XSL_DECIMAL_FORMAT) 		e=new XSLDecimalFormat();
		else if (f==sn.XSL_DOCUMENT) 			e=new XSLDocument();
		else if (f==sn.XSL_ELEMENT) 			e=new XSLElement();
		else if (f==sn.XSL_FALLBACK) 			e=new XSLFallback();
		else if (f==sn.XSL_FOR_EACH) 			e=new XSLForEach();
		else if (f==sn.XSL_IF) 					e=new XSLIf();
		else if (f==sn.XSL_IMPORT) 				e=new XSLImport();
		else if (f==sn.XSL_INCLUDE) 			e=new XSLInclude();
		else if (f==sn.XSL_KEY) 				e=new XSLKey();
		else if (f==sn.XSL_MESSAGE) 			e=new XSLMessage();
		else if (f==sn.XSL_NUMBER) 				e=new XSLNumber();
		else if (f==sn.XSL_NAMESPACE_ALIAS) 	e=new XSLNamespaceAlias();
		else if (f==sn.XSL_OTHERWISE) 			e=new XSLOtherwise();
		else if (f==sn.XSL_OUTPUT) 				e=new XSLOutput();
		else if (f==sn.XSL_PARAM) 				e=new XSLParam();
		else if (f==sn.XSL_PRESERVE_SPACE) 		e=new XSLPreserveSpace();
		else if (f==sn.XSL_PROCESSING_INSTRUCTION) e=new XSLProcessingInstruction();
		else if (f==sn.XSL_SCRIPT) 				e=new XSLScript();
		else if (f==sn.XSL_SORT) 				e=new XSLSort();
		else if (f==sn.XSL_STRIP_SPACE) 		e=new XSLPreserveSpace();
		else if (f==sn.XSL_STYLESHEET) 			e=new XSLStyleSheet();
		else if (f==sn.XSL_TEMPLATE) 			e=new XSLTemplate();
		else if (f==sn.XSL_TEXT) 				e=new XSLText();
		else if (f==sn.XSL_TRANSFORM) 			e=new XSLStyleSheet();
		else if (f==sn.XSL_VALUE_OF) 			e=new XSLValueOf();
		else if (f==sn.XSL_VARIABLE) 			e=new XSLVariable();
		else if (f==sn.XSL_WITH_PARAM) 			e=new XSLWithParam();
		else if (f==sn.XSL_WHEN) 				e=new XSLWhen();

		return e;
	}

	/**
	* Make a SAXON extension element
	*/

	private StyleElement makeSaxonElement(int f) {

		StyleElement e = null;

		if (f==sn.SAXON_ASSIGN) 			e=new SAXONAssign();
		else if (f==sn.SAXON_ENTITY_REF) 	e=new SAXONEntityRef();
		else if (f==sn.SAXON_DOCTYPE) 		e=new SAXONDoctype();
		else if (f==sn.SAXON_FUNCTION) 		e=new SAXONFunction();
		else if (f==sn.SAXON_GROUP) 		e=new SAXONGroup();
		else if (f==sn.SAXON_HANDLER) 		e=new SAXONHandler();
		else if (f==sn.SAXON_ITEM) 			e=new SAXONItem();
		else if (f==sn.SAXON_OUTPUT) 		e=new XSLDocument();    // synonym
		else if (f==sn.SAXON_PREVIEW) 		e=new SAXONPreview();
		else if (f==sn.SAXON_RETURN) 		e=new SAXONReturn();
		else if (f==sn.SAXON_SCRIPT) 		e=new XSLScript();      // synonym
		else if (f==sn.SAXON_WHILE) 		e=new SAXONWhile();
		return e;
	}

	/**
	* Make an EXSLT_FUNCTIONS extension element
	*/

	private StyleElement makeExsltFunctionsElement(int f) {

		if (f==sn.EXSLT_FUNC_FUNCTION)	    return new SAXONFunction();
		else if (f==sn.EXSLT_FUNC_RESULT) 	return new SAXONReturn();

		return null;
    }

    /**
    * Get the factory class for user extension elements
    * If there is no appropriate class, return null
    */

    private ExtensionElementFactory getFactory(short uriCode) {
    	String uri = namePool.getURIFromNamespaceCode(uriCode);
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash<0 || lastSlash==uri.length()-1) {
            return null;
        }
        String factoryClass = uri.substring(lastSlash+1);
        ExtensionElementFactory factory;

        try {
            factory = (ExtensionElementFactory)Loader.getInstance(factoryClass);
        } catch (Exception err) {
            return null;
        }
        return factory;
    }

    /**
    * Method to support the element-available() function
    */

    public boolean isElementAvailable(String uri, String localName) {
    	int fingerprint = namePool.getFingerprint(uri, localName);
    	if (uri.equals(Namespace.XSLT)) {
    		if (fingerprint==-1) return false; 	// all names are pre-registered
    		StyleElement e = makeXSLElement(fingerprint);
    		if (e!=null) return e.isInstruction();
    	}

		if (uri.equals(Namespace.SAXON)) {
			if (fingerprint==-1) return false;	// all names are pre-registered
	    	StyleElement e = makeSaxonElement(fingerprint);
	    	if (e!=null) return e.isInstruction();
	    }

    	short uriCode = namePool.getCodeForURI(uri);
        ExtensionElementFactory factory = getFactory(uriCode);
        if (factory==null) return false;
        Class actualClass = factory.getExtensionClass(localName);
        return (actualClass != null);

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

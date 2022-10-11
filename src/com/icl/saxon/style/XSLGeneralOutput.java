package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.output.Emitter;
import com.icl.saxon.output.SaxonOutputKeys;
import com.icl.saxon.tree.AttributeCollection;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

/**
* Common superclass for the xsl:output and xsl:document (formerly saxon:output)
* elements <br>
*/

abstract class XSLGeneralOutput extends StyleElement {

    Expression href = null;
    Expression userData = null;
    Expression method = null;
    Expression version = null;
    Expression indent = null;
    Expression encoding = null;
    Expression mediaType = null;
    Expression doctypeSystem = null;
    Expression doctypePublic = null;
    Expression omitDeclaration = null;
    Expression standalone = null;
    Expression cdataElements = null;
    Expression omitMetaTag = null;
    Expression nextInChain = null;
    Expression representation = null;
    Expression indentSpaces = null;
    Expression requireWellFormed = null;
    Hashtable userAttributes = null;

    Emitter handler = null;

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

        for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.HREF) {
        		href = makeAttributeValueTemplate(atts.getValue(a));
			} else if (f==sn.METHOD) {
        		method = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.VERSION) {
        		version = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.ENCODING) {
        		encoding = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.OMIT_XML_DECLARATION) {
        		omitDeclaration = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.STANDALONE) {
        		standalone = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.DOCTYPE_PUBLIC) {
        		doctypePublic = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.DOCTYPE_SYSTEM) {
        		doctypeSystem = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.CDATA_SECTION_ELEMENTS) {
        		cdataElements = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.INDENT) {
        		indent = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.MEDIA_TYPE) {
        		mediaType = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.SAXON_OMIT_META_TAG) {
        		omitMetaTag = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.SAXON_CHARACTER_REPRESENTATION) {
        		representation = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.SAXON_INDENT_SPACES) {
        		indentSpaces = makeAttributeValueTemplate(atts.getValue(a));
        	} else if (f==sn.SAXON_NEXT_IN_CHAIN) {
        		nextInChain = makeAttributeValueTemplate(atts.getValue(a));
            } else if (f==sn.SAXON_REQUIRE_WELL_FORMED) {
        		requireWellFormed = makeAttributeValueTemplate(atts.getValue(a));
        	} else {
        	    String attributeURI = getNamePool().getURI(nc);
        	    if ("".equals(attributeURI) ||
        	            Namespace.XSLT.equals(attributeURI) ||
        	            Namespace.SAXON.equals(attributeURI)) {
        		    checkUnknownAttribute(nc);
        		} else {
        		    String name = "{" + attributeURI + "}" + atts.getLocalName(a);
        		    Expression val = makeAttributeValueTemplate(atts.getValue(a));
        		    if (userAttributes==null) {
        		        userAttributes = new Hashtable(5);
        		    }
        		    userAttributes.put(name, val);
        		}
        	}
        }
    }

    /**
    * Evaluate any properties defined as attribute value templates; validate them;
    * and return the values as additions to a set of Properties
    */

    protected Properties updateOutputProperties(Properties details, Context context)
    throws TransformerException {
        if (method != null) {
            String data = method.evaluateAsString(context);
            if (data.equals("xml") || data.equals("html") || data.equals("text"))  {
                details.put(OutputKeys.METHOD, data);
            } else {
                int methodNameCode;
                NamePool pool = getNamePool();
                try {
                    methodNameCode = makeNameCode(data, false);
                } catch (NamespaceException err) {
                    throw styleError(err.getMessage());
                }
                if (pool.getURICode(methodNameCode)==0) {
                    throw styleError("method must be xml, html, or text, or a prefixed name");
                }
                details.put(OutputKeys.METHOD,
                    "{" + pool.getURI(methodNameCode) + "}" + pool.getLocalName(methodNameCode) );
            }
        }

        if (version != null) {
            String data = version.evaluateAsString(context);
            details.put(OutputKeys.VERSION, data);
        }

        if (indent != null) {
            String data = indent.evaluateAsString(context);
            if (data==null || data.equals("yes") || data.equals("no")) {
                details.put(OutputKeys.INDENT, data);
            } else {
                throw styleError("indent must be yes or no or an integer");
            }
        }

        if (indentSpaces != null) {
            String data = indentSpaces.evaluateAsString(context);
            try {
                int indentSpaces = Integer.parseInt(data);
                details.put(OutputKeys.INDENT, "yes");
                details.put(SaxonOutputKeys.INDENT_SPACES, data);
            } catch (NumberFormatException err) {
                throw styleError("indent-spaces must be an integer");
            }
        }

        if (encoding != null) {
            String data = encoding.evaluateAsString(context);
            details.put(OutputKeys.ENCODING, data);
        }

        if (mediaType != null) {
            String data = mediaType.evaluateAsString(context);
            details.put(OutputKeys.MEDIA_TYPE, data);
        }

        if (doctypeSystem != null) {
            String data = doctypeSystem.evaluateAsString(context);
            details.put(OutputKeys.DOCTYPE_SYSTEM, data);
        }

        if (doctypePublic != null) {
            String data = doctypePublic.evaluateAsString(context);
            details.put(OutputKeys.DOCTYPE_PUBLIC, data);
        }

        if (omitDeclaration != null) {
            String data = omitDeclaration.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.put(OutputKeys.OMIT_XML_DECLARATION, data);
            } else {
                throw styleError("omit-xml-declaration attribute must be yes or no");
            }
        }

        if (standalone != null) {
            String data = standalone.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.put(OutputKeys.STANDALONE, data);
            } else {
                throw styleError("standalone attribute must be yes or no");
            }
        }

        if (cdataElements != null) {
            String data = cdataElements.evaluateAsString(context);
            String existing = details.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS);
            String s = " ";
            StringTokenizer st = new StringTokenizer(data);
            NamePool pool = context.getController().getNamePool();
            while (st.hasMoreTokens()) {
                String displayname = st.nextToken();
                if (!Name.isQName(displayname)) {
                    throw styleError("CDATA element " + displayname + " is not a valid QName");
                }
                int namecode;
                try {
                    namecode = makeNameCode(displayname, true);
                } catch (NamespaceException err) {
                    throw styleError(err.getMessage());
                }
                s += " {" + pool.getURI(namecode) + '}' + pool.getLocalName(namecode);
                details.put(OutputKeys.CDATA_SECTION_ELEMENTS, existing+s);
            }
        }

        if (representation != null) {
            String data = representation.evaluateAsString(context);
            details.put(SaxonOutputKeys.CHARACTER_REPRESENTATION, data);
        }

        if (omitMetaTag != null) {
            String data = omitMetaTag.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.put(SaxonOutputKeys.OMIT_META_TAG, data);
            } else {
                throw styleError("saxon:omit-meta-tag attribute must be yes or no");
            }
        }

        if (requireWellFormed != null) {
            String data = requireWellFormed.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.put(SaxonOutputKeys.REQUIRE_WELL_FORMED, data);
            } else {
                throw styleError("saxon:require-well-formed attribute must be yes or no");
            }
        }

        if (nextInChain != null) {
            String data = nextInChain.evaluateAsString(context);
            details.put(SaxonOutputKeys.NEXT_IN_CHAIN, data);
            details.put(SaxonOutputKeys.NEXT_IN_CHAIN_BASE_URI, getSystemId());
        }

        // deal with user-defined attributes

        if (userAttributes!=null) {
            Enumeration enm = userAttributes.keys();
            while (enm.hasMoreElements()) {
                String attName = (String)enm.nextElement();
                Expression exp = (Expression)userAttributes.get(attName);
                String data = exp.evaluateAsString(context);
                details.put(attName, data);
            }
        }

        return details;
    }

    /**
    * Prepare another stylesheet to handle the output of this one
    */

    protected TransformerHandler prepareNextStylesheet(String href, Context context)
    throws TransformerException {

        //TODO: should cache the results, we are recompiling the referenced
        //stylesheet each time it's used

        TransformerFactoryImpl factory =
            getPreparedStyleSheet().getTransformerFactory();
        URIResolver resolver = context.getController().getURIResolver();
        Source source = resolver.resolve(href, getSystemId());
        SAXSource saxSource = factory.getSAXSource(source, true);

        Templates next = factory.newTemplates(source);
        TransformerHandler nextTransformer = factory.newTransformerHandler(next);
        return nextTransformer;
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

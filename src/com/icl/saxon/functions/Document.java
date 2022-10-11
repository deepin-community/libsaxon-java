package com.icl.saxon.functions;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.Builder;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import java.net.MalformedURLException;
import java.net.URL;


public class Document extends Function {

    private Controller boundController = null;

    public String getName() {
        return "document";
    };

    /**
    * Determine the data type of the expression
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }

    /**
    * Simplify and validate.
    */

    public Expression simplify() throws XPathException {
        int numArgs = checkArgumentCount(1, 2);
        argument[0] = argument[0].simplify();
        if (numArgs==2) {
            argument[1] = argument[1].simplify();
        }

        // in principle we could pre-evaluate any call of document() with a
        // single string argument. But we don't, because we don't yet have access
        // to the document pool.

        return this;
    }


    /**
    * evaluate() handles evaluation of the function
    */

    public Value evaluate(Context c) throws XPathException {
        int numArgs = getNumberOfArguments();

        Value arg0 = argument[0].evaluate(c);
        NodeSetValue arg1 = null;
        if (numArgs==2) {
            arg1 = argument[1].evaluateAsNodeSet(c);
        }

        String styleSheetURI = getStaticContext().getBaseURI();

        return getDocuments(arg0, arg1, styleSheetURI, c);
    }

    /**
    * getDocuments() evaluates the function.
    * @param arg0 The value of the first argument
    * @param arg1 The value of the second argument, if there is one; otherwise null
    * @param styleSheetURL The URI of the node in the stylesheet containing the expression.
    * Needed only when the first argument is not a nodeset and the second argument is omitted.
    * @param context The evaluation context
    * @return a NodeSetValue containing the root nodes of the selected documents (or element
    * nodes if the URI references contain fragment identifiers)
    */

    public NodeSetValue getDocuments(
                                Value arg0,
                                NodeSetValue arg1,
                                String styleSheetURL,
                                Context context) throws XPathException {
        String baseURL;

        if ((arg0 instanceof NodeSetValue) &&
        		!(arg0 instanceof FragmentValue || arg0 instanceof TextFragmentValue)) {

            NodeEnumeration supplied = ((NodeSetValue)arg0).enumerate();
            NodeSetExtent nv = new NodeSetExtent(context.getController());

            while (supplied.hasMoreElements()) {
                NodeInfo n = supplied.nextElement();
                if (arg1==null) {
                    baseURL = n.getBaseURI();
                } else {
                    NodeInfo first = arg1.getFirst();
                    if (first==null) {
                        // node set is empty; treat it as fatal error
                        throw new XPathException("Second argument to document() is empty node-set");
                    }
                    else {
                        baseURL = first.getBaseURI();
                    }
                }
                NodeInfo doc = makeDoc(n.getStringValue(), baseURL, context);
                if (doc!=null) {
                    nv.append(doc);
                }
            }
            return nv;

        } else {

            if (arg1==null) {
                baseURL = styleSheetURL;
            } else {
                NodeInfo first = arg1.getFirst();
                if (first==null) {
                    // node set is empty; this is potentially an error
                    // (see XSLT 1.0 errata)
                    baseURL = null;
                } else {
                    baseURL = first.getBaseURI();
                }
            }

            String href = arg0.asString();
            NodeInfo doc = makeDoc(href, baseURL, context);
            return new SingletonNodeSet(doc);
        }
    }

    /**
    * Supporting routine to load one external document given a URI (href) and a baseURI
    */

    private NodeInfo makeDoc(String href, String baseURL, Context c) throws XPathException {

        // If the href contains a fragment identifier, strip it out now

        int hash = href.indexOf('#');

        String fragmentId = null;
        if (hash>=0) {
            if (hash==href.length()-1) {
                // # sign at end - just ignore it
                href = href.substring(0, hash);
            } else {
                fragmentId = href.substring(hash+1);
                href = href.substring(0, hash);
            }
        }

        // Resolve relative URI


        String documentKey;
        if (baseURL==null) {    // no base URI available
            try {
                // the href might be an absolute URL
                documentKey = (new URL(href)).toString();
            } catch (MalformedURLException err) {
                // it isn't; but the URI resolver might know how to cope
                documentKey = baseURL + "/" + href;
                baseURL = "";
            }
        } else {
            try {
                URL url = new URL(new URL(baseURL), href);
                documentKey = url.toString();
            } catch (MalformedURLException err) {
                documentKey = baseURL + "/../" + href;
            }
        }

        Controller controller = boundController;
        if (controller==null) {
            controller = c.getController();
        }

        if (controller==null) {
            throw new XPathException("Internal error: no controller available for document() function");
        }

        // see if the document is already loaded


        DocumentInfo doc = controller.getDocumentPool().find(documentKey);
        if (doc!=null) return getFragment(doc, fragmentId);

        try {
            // Get a Source from the URIResolver

            URIResolver r = controller.getURIResolver();
            Source source = r.resolve(href, baseURL);

            // if a user URI resolver returns null, try the standard one
            // (Note, the standard URI resolver never returns null)
            if (source==null) {
                r = controller.getStandardURIResolver();
                source = r.resolve(href, baseURL);
            }

            DocumentInfo newdoc = null;
            if (source instanceof DocumentInfo) {
                newdoc = (DocumentInfo)source;
            } else {
                if (source instanceof DOMSource) {
                    DOMSource ds = (DOMSource)source;
                    if (ds.getNode() instanceof DocumentInfo) {
                        // If the URIResolver returns a DocumentInfo, it is
                        // responsible for doing any stripping of whitespace
                        newdoc = (DocumentInfo)ds.getNode();
                    }
                }
                if (newdoc==null) {
                    // Build a new tree
                    SAXSource saxSource =
                        controller.getTransformerFactory().getSAXSource(source, false);

                    Builder b = controller.makeBuilder();
                    newdoc = b.build(saxSource);
                }
            }

            // add the document to the pool
            controller.getDocumentPool().add(newdoc, documentKey);

            return getFragment(newdoc, fragmentId);

        } catch (TransformerException err) {
            try {
                controller.reportRecoverableError(err);
            } catch (TransformerException err2) {
                throw new XPathException(err);
            }
            return null;
        }
    }

    /**
    * Resolve the fragment identifier within a URI Reference.
    * Only "bare names" XPointers are recognized, that is, a fragment identifier
    * that matches an ID attribute value within the target document.
    * @return the element within the supplied document that matches the
    * given id value; or null if no such element is found.
    */

    private NodeInfo getFragment(DocumentInfo doc, String fragmentId) {
        if (fragmentId==null) {
            return doc;
        }
        return doc.selectID(fragmentId);
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = argument[0].getDependencies();
        if (getNumberOfArguments()==2) {
            dep |= argument[1].getDependencies();
        }
        if (boundController == null) {
            return dep | Context.CONTROLLER;
        } else {
            return dep;
        }
    }

    /**
    * Remove dependencies.
    */

    public Expression reduce(int dep, Context context)
            throws XPathException {

        Document doc = new Document();
        doc.addArgument(argument[0].reduce(dep, context));
        if (getNumberOfArguments()==2) {
            doc.addArgument(argument[1].reduce(dep, context));
        }
        doc.setStaticContext(getStaticContext());

        if ( boundController==null && ((dep & Context.CONTROLLER) != 0)) {
            doc.boundController = context.getController();
        }

        if (doc.getDependencies()==0) {
            return doc.evaluate(context);
        }

        return doc;
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

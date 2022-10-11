package com.icl.saxon;
import org.xml.sax.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;


/**
* This class provides the service of converting a URI into an InputSource.
* It is used to get stylesheet modules referenced by xsl:import and xsl:include,
* and source documents referenced by the document() function. The standard version
* handles anything that the java URL class will handle.
* You can write a subclass to handle other kinds of URI, e.g. references to things in
* a database.
* @author Michael H. Kay
*/

public class StandardURIResolver implements URIResolver {

    private TransformerFactoryImpl factory = null;

    protected StandardURIResolver() {
        this(null);
    }

    public StandardURIResolver(TransformerFactoryImpl factory) {
        this.factory = factory;
    }

    /**
    * Resolve a URI
    * @param baseURI The base URI that should be used. May be null if uri is absolute.
    * @params uri The relative or absolute URI. May be an empty string. May contain
    * a fragment identifier starting with "#", which must be the value of an ID attribute
    * in the referenced XML document.
    * @return a Source object representing an XML document
    */

    public Source resolve(String href, String base)
    throws TransformerException {

        String relativeURI = href;
        String id = null;
        int hash = href.indexOf('#');
        if (hash>=0) {
            relativeURI = href.substring(0, hash);
            id = href.substring(hash+1);
            // System.err.println("StandaredURIResolver, href=" + href + ", id=" + id);
        }

		URL url;
        try {
            if (base==null) {
                url = new URL(relativeURI);
                // System.err.println("Resolved " + relativeURI + " as " + url.toString());
            } else {
                // System.err.println("Resolving " + relativeURI + " against " + base);
                URL baseURL = new URL(base);
                url = (relativeURI.length()==0 ?
                                 baseURL :
                                 new URL(baseURL, relativeURI)
                             );
            }
        } catch (java.net.MalformedURLException err) {
            // System.err.println("Recovering from " + err);
            // last resort: if the base URI is null, or is itself a relative URI, we
            // try to expand it relative to the current working directory
            String expandedBase = tryToExpand(base);
            if (!expandedBase.equals(base)) { // prevent infinite recursion
                return resolve(href, expandedBase);
            }
            //err.printStackTrace();
            throw new TransformerException("Malformed URL [" + relativeURI + "] - base [" + base + "]", err);
        }


        SAXSource source = new SAXSource();
        source.setInputSource(new InputSource(url.toString()));

        if (id!=null) {
            IDFilter filter = new IDFilter(id);
            XMLReader parser;
            if (factory==null) {
                try {
                    parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                } catch (Exception err) {
                    throw new TransformerException(err);
                }
            } else {
                parser = factory.getSourceParser();
            }
            filter.setParent(parser);
            source.setXMLReader(filter);
        }
        return source;
    }


    /**
    * If a base URI is unknown, we'll try to expand the relative
    * URI using the current directory as the base URI.
    * (Code is identical to that in com.icl.saxon.aelfred.SAXDriver)
    */

    private String tryToExpand(String systemId) {
        if (systemId==null) {
            systemId = "";
        }
	    try {
	        URL u = new URL(systemId);
	        return systemId;   // all is well
	    } catch (MalformedURLException err) {
	        String dir = System.getProperty("user.dir");
	        if (dir.startsWith("/")) {
	            dir = "file://" + dir;
	        } else {
	            dir = "file:///" + dir;
	        }
	        if (!(dir.endsWith("/") || systemId.startsWith("/"))) {
	            dir = dir + "/";
	        }
	        String file = dir + systemId;
	        try {
	            URL u2 = new URL(file);
	            // System.err.println("URI Resolver: expanded " + systemId + " to " + file);
	            return file;    // it seems to be OK
	        } catch (MalformedURLException err2) {
	            // go with the original one
	            return systemId;
	        }
	    }
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

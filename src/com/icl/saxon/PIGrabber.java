package com.icl.saxon;
import com.icl.saxon.om.ProcInstParser;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.net.URL;
import java.util.Vector;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.*;

/**
  * The PIGrabber class is a SAX ContentHandler that looks for xml-stylesheet processing
  * instructions and tests whether they match specified criteria; for those that do, it creates
  * an InputSource object referring to the relevant stylesheet
  * @author Michael H. Kay
  */

public class PIGrabber extends DefaultHandler {

    private String reqMedia = null;
    private String reqTitle = null;
    private String baseURI = null;
    private URIResolver uriResolver = null;
    private Vector stylesheets = new Vector();

    public void setCriteria(String media, String title, String charset) {
        this.reqMedia = media;
        this.reqTitle = title;
    }

    /**
    * Set the base URI
    */

    public void setBaseURI(String uri) {
        baseURI = uri;
    }

    /**
    * Set the URI resolver to be used for the href attribute
    */

    public void setURIResolver(URIResolver resolver) {
        uriResolver = resolver;
    }

    /**
    * Abort the parse when the first start element tag is found
    */

    public void startElement (String uri, String localName,
			      String qName, Attributes attributes) throws SAXException {

	    // abort the parse when the first start element tag is found
        throw new SAXException("#start#");
    }

    /**
    * Handle xml-stylesheet PI
    */

    public void processingInstruction (String target, String data)
	throws SAXException
    {
        if (target.equals("xml-stylesheet")) {

            String piMedia = ProcInstParser.getPseudoAttribute(data, "media");
            String piTitle = ProcInstParser.getPseudoAttribute(data, "title");
            String piType = ProcInstParser.getPseudoAttribute(data, "type");
            String piAlternate = ProcInstParser.getPseudoAttribute(data, "alternate");

			if (piType==null) return;

			// System.err.println("Found xml-stylesheet media=" + piMedia + " title=" + piTitle);

            if ( (piType.equals("text/xml") || piType.equals("application/xml") ||
                    piType.equals("text/xsl") || piType.equals("applicaton/xsl")) &&

                    (reqMedia==null || piMedia==null || reqMedia.equals(piMedia)) &&

                    ( ( piTitle==null && (piAlternate==null || piAlternate.equals("no"))) ||
                      ( reqTitle==null ) ||
                      ( piTitle!=null && piTitle.equals(reqTitle) ) ) )
            {
                String href = ProcInstParser.getPseudoAttribute(data, "href");
                if (href==null) {
                    throw new SAXException("xml-stylesheet PI has no href attribute");
                }

				// System.err.println("Adding " + href);
                if (piTitle==null && (piAlternate==null || piAlternate.equals("no"))) {
                    stylesheets.insertElementAt(href, 0);
                } else {
                    stylesheets.addElement(href);
                }
            } else {
				//System.err.println("No match on required media=" + reqMedia + " title=" + reqTitle );
			}
        }
    }

    /**
    * Return list of stylesheets that matched, as an array of Source objects
    * @return null if there were no matching stylesheets.
    * @throws TransformerException if a URI cannot be resolved
    */

    public SAXSource[] getAssociatedStylesheets() throws TransformerException {
        if (stylesheets.size()==0) {
            return null;
        }
        if (uriResolver==null) {
            uriResolver = new StandardURIResolver();
        }
        SAXSource[] result = new SAXSource[stylesheets.size()];
        for (int i=0; i<stylesheets.size(); i++) {
            String href = (String)stylesheets.elementAt(i);
            Source s = uriResolver.resolve(href, baseURI);
            if (!(s instanceof SAXSource)) {
                throw new TransformerException("Associated stylesheet URI must yield a SAX source");
            }
            result[i] = (SAXSource)s;
        }
        return result;
    }

    /**
    * Get the stylesheet URIs as an array of Strings
    */

    public String[] getStylesheetURIs() throws SAXException {
        if (stylesheets.size()==0) {
            return null;
        }
        String[] result = new String[stylesheets.size()];
        for (int i=0; i<stylesheets.size(); i++) {
            result[i] = (String)stylesheets.elementAt(i);
        }
        return result;
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

package com.icl.saxon.style;
import com.icl.saxon.Context;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.StringValue;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tree.TreeBuilder;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


/**
* This class represents a literal result element in the style sheet
* (typically an HTML element to be output). <br>
* It is also used to represent unknown top-level elements, which are ignored.
*/

public class LiteralResultElement extends StyleElement {

    private int resultNameCode;
    private int[] attributeNames;
    private Expression[] attributeValues;
    private boolean[] attributeChecked;
    private int numberOfAttributes;
    private boolean toplevel;
    private int[] namespaceCodes;


    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    /**
    * Process the attribute list
    */

    public void prepareAttributes() throws TransformerConfigurationException {
        // processing of the attribute list is deferred until validate() time, so that
        // namespaces can be translated if necessary using the namespace aliases in force
        // for the stylesheet.
    }

    /**
    * Validate that this node is OK
    */

    public void validate() throws TransformerConfigurationException {

        toplevel = (getParentNode() instanceof XSLStyleSheet);

		StandardNames sn = getStandardNames();
        resultNameCode = getNameCode();

        NamePool namePool = getNamePool();
        short elementURICode = namePool.getURICode(resultNameCode);

        if (toplevel) {
            // A top-level element can never be a "real" literal result element,
            // but this class gets used for them anyway

            if (elementURICode == 0) {
                compileError("Top level elements must have a non-null namespace URI");
            }
        } else {

            // Build the list of output namespace nodes

            // Up to 5.3.1 we listed the namespace nodes associated with this element that were not also
            // associated with an ancestor literal result element (because those will already
            // have been output). Unfortunately this isn't true if the namespace was present on an outer
            // LRE, and was excluded at that level using exclude-result-prefixes, and is now used in an
            // inner element: bug 5.3.1/006

            // At 6.0 we'll try a different optimisation: if this LRE has a parent that is also
            // an LRE, and if this LRE has no namespace declarations or xsl:exclude-result-prefixes
            // attribute or xsl:extension-element-prefixes attribute of its own, and if this element
            // name is in the same namespace as its parent, and if there are no attributes in a
            // non-null namespace, then we don't need to output any namespace declarations to the
            // result.

            boolean optimizeNS = false;
            if ((getParent() instanceof LiteralResultElement) &&
                    (namespaceList==null || namespaceList.length==0) &&
                    ( elementURICode ==
                    	 namePool.getURICode(getParent().getFingerprint()))
                    ) {
                optimizeNS = true;
            }
            if (optimizeNS) {
                for (int a=0; a<attributeList.getLength(); a++ ) {
                    if (((attributeList.getNameCode(a)>>20)&0xff) != 0) {	// prefix != ""
                        optimizeNS = false;
                        break;
                    }
                }
            }

            if (optimizeNS) {
            	namespaceCodes = new int[0];
            } else {
                namespaceCodes = getNamespaceCodes();
	        }

            // apply any aliases required to create the list of output namespaces

            XSLStyleSheet sheet = getPrincipalStyleSheet();

            if (sheet.hasNamespaceAliases()) {
                for (int i=0; i<namespaceCodes.length; i++) {
                	// System.err.println("Examining namespace " + namespaceCodes[i]);
                	short scode = (short)(namespaceCodes[i]&0xffff);
                    short rcode = sheet.getNamespaceAlias(scode);
                    if (scode!=rcode) {
                        // keep the prefix but change the URI
                        int prefixCode = namespaceCodes[i] & 0xffff0000;
                        namespaceCodes[i] = prefixCode | rcode;
						// System.err.println("Aliased as " + namespaceCodes[i]);
                    }
                }

                // determine if there is an alias for the namespace of the element name

                short ercode = sheet.getNamespaceAlias(elementURICode);

                if (ercode!=elementURICode) {
                	elementURICode = ercode;
                    resultNameCode = namePool.allocate(getPrefix(), ercode, getLocalName());
                }
            }

            // establish the names to be used for all the output attributes

            int num = attributeList.getLength();
            attributeNames = new int[num];
            attributeValues = new Expression[num];
            attributeChecked = new boolean[num];
            short[] attributeURIs = new short[num];
            numberOfAttributes = 0;

            for (int i=0; i<num; i++) {

                int anameCode = attributeList.getNameCode(i);
                int alias = anameCode;
                int fp = anameCode & 0xfffff;
                short attURIcode = namePool.getURICode(anameCode);

                if (fp == sn.XSL_USE_ATTRIBUTE_SETS) {
                    findAttributeSets(attributeList.getValue(i));
                } else if (fp == sn.XSL_EXTENSION_ELEMENT_PREFIXES) {
                	// already dealt with
                } else if (fp == sn.XSL_EXCLUDE_RESULT_PREFIXES) {
                	// already dealt with
                } else if (fp == sn.XSL_VERSION) {
                    // already dealt with
                } else {

                    if (attURIcode==Namespace.XSLT_CODE) {
                    	compileError("Unknown XSL attribute " + namePool.getDisplayName(anameCode));
                    }
                    if (attURIcode!=0) {	// attribute has a namespace prefix
                        short attAlias = sheet.getNamespaceAlias(attURIcode);
                        if (attAlias != attURIcode) {
                            String qName = namePool.getDisplayName(anameCode);
                            String newPrefix = Name.getPrefix(qName);
                            String newLocalName = Name.getLocalName(qName);
                            // System.err.println("Aliasing " + namePool.getDisplayName(anameCode));
		                    String newURI = namePool.getURIFromNamespaceCode(attAlias);
		                    alias = namePool.allocate(newPrefix, newURI, newLocalName);
		                    // System.err.println("... as " + namePool.getDisplayName(alias));
		                    attURIcode = attAlias;
                        }
                    }

	                attributeNames[numberOfAttributes] = alias;
	                attributeURIs[numberOfAttributes] = attURIcode;
	                Expression exp = makeAttributeValueTemplate(attributeList.getValue(i));
	                attributeValues[numberOfAttributes] = exp;

                    // if we can be sure the attribute value contains no special XML/HTML characters,
                    // we can save the trouble of checking it each time it is output.
                    // Note that the check includes non-ASCII characters, as these might need to be escaped in a
                    // URL (we don't yet know which output method will be used!)

	                attributeChecked[numberOfAttributes] = false;
	                boolean special = false;
	                if (exp instanceof StringValue) {
	                    String val = ((StringValue)exp).asString();
	                    for (int k=0; k<val.length(); k++) {
	                        char c = val.charAt(k);
	                        if ((int)c<33 || (int)c>126 ||
	                                 c=='<' || c=='>' || c=='&' || c=='\"') {
	                            special = true;
	                            break;
	                         }
	                    }
	                    attributeChecked[numberOfAttributes] = !special;
	                }
	                numberOfAttributes++;
	            }
            }

            // remove any namespaces that are on the exclude-result-prefixes list, unless it is
            // the namespace of the element or an attribute

            for (int n=0; n<namespaceCodes.length; n++) {
            	short uricode = (short)(namespaceCodes[n] & 0xffff);
                if (isExcludedNamespace(uricode)) {

                    boolean exclude = true;

                    // check the element name

                    if (uricode==elementURICode) {
                        exclude = false;
                    }

                    // check the attribute names

                    for (int a=0; a<numberOfAttributes; a++) {
                    	if (uricode==attributeURIs[a]) {
                    		exclude = false;
                    		break;
                    	}
                    }

                    // if the name isn't in use, exclude it from the output namespace list

                    if (exclude) {
                        namespaceCodes[n] = -1;
                    }
                }
            }

        }
    }

    protected void validateChildren () throws TransformerConfigurationException {
        // don't validate subtree rooted at a top-level user-defined element
        if (!toplevel) {
            super.validateChildren();
        }
    }

	/**
	* Process the literal result element by copying it to the result tree
	*/

    public void process(Context context) throws TransformerException
    {
        // top level elements in the stylesheet are ignored
        if (toplevel) return;

        // output the start tag
        Outputter o = context.getOutputter();
        o.writeStartTag(resultNameCode);

        // output the namespace list

        for (int i=0; i<namespaceCodes.length; i++) {
        	if (namespaceCodes[i]!=-1) {
            	o.writeNamespaceDeclaration(namespaceCodes[i]);
            }
        }

        // output any attributes from xsl:use-attribute-set

        processAttributeSets(context);

        // evaluate AVT expressions and output the attributes (these may be overwritten later)

        for (int i=0; i<numberOfAttributes; i++) {
            int attname = attributeNames[i];
            String attval = attributeValues[i].evaluateAsString(context);
            o.writeAttribute(attname, attval, attributeChecked[i]);
        }

        // process the child elements in the stylesheet

        processChildren(context);

        // write the end tag

        o.writeEndTag(resultNameCode);

    }

    /**
    * Make a top-level literal result element into a stylesheet. This implements
    * the "Literal Result Element As Stylesheet" facility.
    */

    public DocumentImpl makeStyleSheet(PreparedStyleSheet pss) throws TransformerConfigurationException {

        // the implementation grafts the LRE node onto a containing xsl:template and
        // xsl:stylesheet

		NamePool pool = getNamePool();
		StandardNames sn = getStandardNames();
        String xslPrefix = getPrefixForURI(Namespace.XSLT);
        if (xslPrefix==null) {
            String message;
            if (getLocalName().equals("stylesheet") || getLocalName().equals("transform")) {
                if (getPrefixForURI(Namespace.MICROSOFT_XSL)!=null) {
                    message = "Saxon is not able to process Microsoft's WD-xsl dialect";
                } else {
                    message = "Namespace for stylesheet element should be " + Namespace.XSLT;
                }
            } else {
                message = "The supplied file does not appear to be a stylesheet";
            }
            TransformerConfigurationException err =
               new TransformerConfigurationException (message);
            try {pss.reportError(err);} catch(TransformerException err2) {}
            throw err;

        }

        // check there is an xsl:version attribute (it's mandatory), and copy
        // it to the new xsl:stylesheet element

        String version = getAttributeValue(sn.XSL_VERSION);
        if (version==null) {
            TransformerConfigurationException err =
               new TransformerConfigurationException (
                "Literal Result Element As Stylesheet: xsl:version attribute is missing");
            try {pss.reportError(err);} catch(TransformerException err2) {}
            throw err;
        }

        try {
            TreeBuilder builder = new TreeBuilder();
            builder.setDocumentLocator(null);
            builder.setNamePool(pool);
            builder.setNodeFactory(((DocumentImpl)getParentNode()).getNodeFactory());
            builder.setSystemId(this.getSystemId());

            builder.startDocument();
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "version", "version", "CDATA", version);
            int[] namespaces = new int[1];
            namespaces[0] = pool.getNamespaceCode("xsl", Namespace.XSLT);
            int st = pool.allocate("xsl", Namespace.XSLT, "stylesheet");
            builder.startElement(st, atts, namespaces, 1);

    		int te = pool.allocate("xsl", Namespace.XSLT, "template");
            atts.clear();
            atts.addAttribute("", "match", "match", "CDATA", "/");
            builder.startElement(te, atts, namespaces, 0);

            builder.graftElement(this);

            builder.endElement(te);
            builder.endElement(st);
            builder.endDocument();

            return (DocumentImpl)builder.getCurrentDocument();
        } catch (TransformerException err) {
            throw new TransformerConfigurationException(err);
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

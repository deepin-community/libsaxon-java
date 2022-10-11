package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.NodeImpl;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Properties;
import java.util.Vector;

/**
* An xsl:stylesheet or xsl:transform element in the stylesheet.<BR>
* Note this element represents a stylesheet module, not necessarily
* the whole stylesheet.
*/

public class XSLStyleSheet extends StyleElement {

                // true if diagnostic trace set
    //private boolean tracing = false;

                // true if this stylesheet was included by xsl:include, false if it is the
                // principal stylesheet or if it was imported
    private boolean wasIncluded = false;

                // the import precedence for top-level elements in this stylesheet
    private int precedence = 0;

                // the lowest precedence of any stylesheet imported by this one
    private int minImportPrecedence = 0;

                // the StyleSheet that included or imported this one; null for the principal stylesheet
    private XSLStyleSheet importer = null;

                // the PreparedStyleSheet object used to load this stylesheet
    private PreparedStyleSheet stylesheet;

                // the top-level elements in this logical stylesheet (after include/import)
    private Vector topLevel;

                // definitions of strip/preserve space action
    private Mode stripperRules = null;

                // definitions of template rules
    private RuleManager ruleManager;

                // definitions of keys
    private KeyManager keyManager = new KeyManager();

                // definitions of decimal formats
    private DecimalFormatManager decimalFormatManager = new DecimalFormatManager();

                // definitions of preview elements
    private PreviewManager previewManager = null;

                // media type (MIME type) of principal output
    //private String mediaType;

                // namespace aliases
    private int numberOfAliases = 0;
    private short[] aliasSCodes = new short[5];
    private short[] aliasRCodes = new short[5];

                // count of the number of global parameters and variables
    private int numberOfVariables = 0;

                // count of the maximum umber of local variables in any template
    private int largestStackFrame = 0;


    /**
    * Create link to the owning PreparedStyleSheet object
    */

    public void setPreparedStyleSheet(PreparedStyleSheet sheet) {
        stylesheet = sheet;
        ruleManager = new RuleManager(sheet.getNamePool());
    }

    /**
    * Get the owning PreparedStyleSheet object
    */

    public PreparedStyleSheet getPreparedStyleSheet() {
        if (importer!=null) return importer.getPreparedStyleSheet();
        return stylesheet;
    }

    /**
    * Get the RuleManager which handles template rules
    */

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    /**
    * Get the rules determining which nodes are to be stripped from the tree
    */

    protected Mode getStripperRules() {
        if (stripperRules==null) {
            stripperRules = new Mode();
        }
        return stripperRules;
    }


    /**
    * Create a Stripper which handles whitespace stripping definitions
    */

    public Stripper newStripper() {
        return new Stripper(stripperRules);
    }

    /**
    * Determine whether this stylesheet does any whitespace stripping
    */

    public boolean stripsWhitespace() {
        StandardNames sn = getStandardNames();
        for (int i=0; i<topLevel.size(); i++) {
            NodeInfo s = (NodeInfo)topLevel.elementAt(i);
            if (s.getFingerprint()==sn.XSL_STRIP_SPACE) {
                return true;
            }
        }
        return false;
    }

    /**
    * Get the KeyManager which handles key definitions
    */

    public KeyManager getKeyManager() {
        return keyManager;
    }

    /**
    * Get the DecimalFormatManager which handles decimal-format definitions
    */

    public DecimalFormatManager getDecimalFormatManager() {
        return decimalFormatManager;
    }

    /**
    * Get the PreviewManager which handles saxon:preview element definitions
    * @return null if there are no saxon:preview elements
    */

    public PreviewManager getPreviewManager() {
        return previewManager;
    }

    /**
    * Set the preview manager
    */

    public void setPreviewManager(PreviewManager pm) {
        previewManager = pm;
    }

    /**
    * Set the import precedence of this stylesheet
    */

    public void setPrecedence(int prec) {
        precedence = prec;
    }

    /**
    * Get the import precedence of this stylesheet
    */

    public int getPrecedence() {
        if (wasIncluded) return importer.getPrecedence();
        return precedence;
    }

    /**
    * Get the minimum import precedence of this stylesheet, that is, the lowest precedence
    * of any stylesheet imported by this one
    */

    public int getMinImportPrecedence() {
        return minImportPrecedence;
    }

    /**
    * Set the minimum import precedence of this stylesheet, that is, the lowest precedence
    * of any stylesheet imported by this one
    */

    public void setMinImportPrecedence(int precedence) {
        minImportPrecedence = precedence;
    }

    /**
    * Set the StyleSheet that included or imported this one.
    */

    public void setImporter(XSLStyleSheet importer) {
        this.importer = importer;
    }

    /**
    * Get the StyleSheet that included or imported this one.
    * @return null if this is the principal stylesheet
    */

    public XSLStyleSheet getImporter() {
        return importer;
    }

    /**
    * Indicate that this stylesheet was included (by its "importer") using an xsl:include
    * statement as distinct from xsl:import
    */

    public void setWasIncluded() {
        wasIncluded = true;
    }

    /**
    * Determine whether this stylesheet was included (by its "importer") using an xsl:include
    * statement as distinct from xsl:import.
    */

    public boolean wasIncluded() {
        return wasIncluded;
    }

    /**
    * Get the top level elements in this stylesheet, after applying include/import
    */

    public Vector getTopLevel() {
        return topLevel;
    }

    /**
    * Allocate a slot number for a global variable or parameter
    */

    public int allocateSlotNumber() {
        return numberOfVariables++;
    }

    /**
    * Ensure there is enuogh space for local variables or parameters in any template
    */

    public void allocateLocalSlots(int n) {
        if (n > largestStackFrame) {
            largestStackFrame = n;
        }
    }

    /**
    * Prepare the attributes on the stylesheet element
    */

    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();
		for (int a=0; a<atts.getLength(); a++) {

			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.VERSION) {
                version = atts.getValueByFingerprint(f);
			} else if (f==sn.ID) {
        		//
			} else if (f==sn.EXTENSION_ELEMENT_PREFIXES) {
        		//
			} else if (f==sn.EXCLUDE_RESULT_PREFIXES) {
        		//
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }
        if (version==null) {
            reportAbsence("version");
        }
    }

    /**
    * Process the version attribute - mandatory on this element (but checked elsewhere)
    */

    protected void processVersionAttribute(int nc) {
        version = getAttributeValue(nc & 0xfffff);
    }

    /**
    * Get the declared namespace alias for a given namespace URI code if there is one.
    * If there is more than one, we get the last.
    * @param uriCode The code of the uri used in the stylesheet.
    * @return The code of the result uri to be used: return the stylesheet uri unchanged
    * if no alias is defined
    */

    protected short getNamespaceAlias(short uriCode) {

		// if there are several matches, the last takes priority
		for (int i=numberOfAliases-1; i>=0; i--) {
			if (uriCode==aliasSCodes[i]) {
				return aliasRCodes[i];
			}
		}
		return uriCode;
	}

    /**
    * Validate this element
    */

    public void validate() throws TransformerConfigurationException {
        if (validationError != null) {
            compileError(validationError);
        }
        if (!(getParentNode() instanceof DocumentInfo)) {
            throw new TransformerConfigurationException(
                        getDisplayName() + " must be the outermost element");
        }
    }

    /**
    * Preprocess does all the processing possible before the source document is available.
    * It is done once per stylesheet, so the stylesheet can be reused for multiple source
    * documents.
    */

    public void preprocess() throws TransformerConfigurationException {

        // process any xsl:include and xsl:import elements

        spliceIncludes();


        // process the attributes of every node in the tree

        processAllAttributes();

        // collect any namespace aliases

        collectNamespaceAliases();

        // Validate the whole logical style sheet (i.e. with included and imported sheets)

        validate();
        for (int i=0; i<topLevel.size(); i++) {
            Object node = topLevel.elementAt(i);
            if (node instanceof StyleElement) {
                ((StyleElement)node).validateSubtree();
            }
        }

        // Preprocess definitions of top-level elements.

        for (int i=0; i<topLevel.size(); i++) {
            Object s = topLevel.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    ((StyleElement)s).preprocess();
                } catch (TransformerConfigurationException err) {
                    ((StyleElement)s).compileError(err);
                }
            }
        }

    }

    /**
    * Process xsl:include and xsl:import elements.
    */

    public void spliceIncludes() throws TransformerConfigurationException {

        boolean foundNonImport = false;
        topLevel = new Vector();
        minImportPrecedence = precedence;
        StyleElement previousElement = this;

        NodeImpl child = (NodeImpl)getFirstChild();

        while(child!=null) {

            if (child.getNodeType() == NodeInfo.TEXT) {
                // in an embedded stylesheet, white space nodes may still be there
                if (!Navigator.isWhite(child.getStringValue())) {
                    previousElement.compileError(
                        "No character data is allowed between top-level elements");
                }

            } else {
                previousElement = (StyleElement)child;
                if (child instanceof XSLGeneralIncorporate) {
                    XSLGeneralIncorporate xslinc = (XSLGeneralIncorporate)child;
                    xslinc.processAttributes();

                    if (xslinc.isImport()) {
                        if (foundNonImport) {
                            xslinc.compileError("xsl:import elements must come first");
                        }
                    } else {
                        foundNonImport = true;
                    }

                    // get the included stylesheet. This follows the URL, builds a tree, and splices
                    // in any indirectly-included stylesheets.

                    XSLStyleSheet inc =
                        xslinc.getIncludedStyleSheet(this, precedence);
                    if (inc==null) return;  // error has been reported

                    // after processing the imported stylesheet and any others it brought in,
                    // adjust the import precedence of this stylesheet if necessary

                    if (xslinc.isImport()) {
                        precedence = inc.getPrecedence() + 1;
                    } else {
                        precedence = inc.getPrecedence();
                        inc.setMinImportPrecedence(minImportPrecedence);
                        inc.setWasIncluded();
                    }

                    // copy the top-level elements of the included stylesheet into the top level of this
                    // stylesheet. Normally we add these elements at the end, in order, but if the precedence
                    // of an element is less than the precedence of the previous element, we promote it.
                    // This implements the requirement in the spec that when xsl:include is used to
                    // include a stylesheet, any xsl:import elements in the included document are moved
                    // up in the including document to after any xsl:import elements in the including
                    // document.

                    Vector incchildren = inc.topLevel;
                    for (int j=0; j<incchildren.size(); j++) {
                        StyleElement elem = (StyleElement)incchildren.elementAt(j);
                        int last = topLevel.size() - 1;
                        if (last < 0 || elem.getPrecedence() >= ((StyleElement)topLevel.elementAt(last)).getPrecedence()) {
                            topLevel.addElement(elem);
                        } else {
                            while (last >=0 && elem.getPrecedence() < ((StyleElement)topLevel.elementAt(last)).getPrecedence()) {
                                last--;
                            }
                            topLevel.insertElementAt(elem, last+1);
                        }
                    }
                } else {
                    foundNonImport = true;
                    topLevel.addElement(child);
                }
            }
            child = (NodeImpl)child.getNextSibling();
        }
    }


	/**
	* Collect any namespace aliases
	*/

	private void collectNamespaceAliases() {
        for (int i=0; i<topLevel.size(); i++) {
            Object node = topLevel.elementAt(i);
            if (node instanceof XSLNamespaceAlias) {
            	XSLNamespaceAlias xna = (XSLNamespaceAlias)node;

                if (numberOfAliases == aliasSCodes.length) {
                	short[] s2 = new short[numberOfAliases*2];
                	short[] r2 = new short[numberOfAliases*2];
                	System.arraycopy(aliasSCodes, 0, s2, 0, numberOfAliases);
                	System.arraycopy(aliasRCodes, 0, r2, 0, numberOfAliases);
                	aliasSCodes = s2;
                	aliasRCodes = r2;
                }
                aliasSCodes[numberOfAliases] = xna.getStylesheetURICode();
                aliasRCodes[numberOfAliases] = xna.getResultURICode();
                numberOfAliases++;
            }
        }
    }

    protected boolean hasNamespaceAliases() {
    	return numberOfAliases>0;
    }

    /**
    * Process the attributes of every node in the stylesheet
    */

    public void processAllAttributes() throws TransformerConfigurationException {
        prepareAttributes();
        Vector children = topLevel;
        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    ((StyleElement)s).processAllAttributes();
                } catch (TransformerConfigurationException err) {
                    ((StyleElement)s).compileError(err);
                }
            }
        }
    }

    /**
    * Allocate space in bindery for all the variables needed
    * This has to be done early to accommodate preview mode
    */

    public void initialiseBindery(Bindery bindery) {
       // ensure enough slots are available for global variables and for the largest stackframe

        bindery.allocateGlobals(numberOfVariables);
        bindery.allocateLocals(largestStackFrame);
    }

    /**
    * Update an output properties object using the xsl:output elements in the stylesheet.
    * This method can be called before the source document is available; all properties
    * will be returned as written, with attribute value templates and namespace prefixes
    * unexpanded, and no validation performed.
    */

    public void gatherOutputProperties(Properties details) {
        Vector children = topLevel;
        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof XSLOutput) {
                ((XSLOutput)s).gatherOutputProperties(details);
            }
        }
    }

    /**
    * Update an output properties object using the xsl:output elements in the stylesheet.
    * Note, as xsl:output now allows attribute value templates, this cannot be called until
    * the source document is available.
    */

    public void updateOutputProperties(Properties details, Context context)
    throws TransformerException {
        Vector children = topLevel;
        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof XSLOutput) {
                ((XSLOutput)s).updateOutputProperties(details, context);
            }
        }
    }

    /**
    * Get a Java class for a given namespace URI, if possible.
    * return null if none is found.
    * @throws TransformerException if a class is found but cannot
    * be loaded
    */

    public Class getExternalJavaClass(String uri) throws TransformerException {
        Vector children = topLevel;
        if (!((Boolean)getPreparedStyleSheet().getTransformerFactory().
                getAttribute(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS)).booleanValue()) {
            return null;
        }
        for (int i=children.size() - 1; i>=0; i--) {
            Object s = children.elementAt(i);
            if (s instanceof XSLScript) {
                XSLScript script = (XSLScript)s;
                Class c = script.getJavaClass(uri);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
    * Process() is called once the source document is available. It activates those top-level
    * stylesheet elements that were not dealt with at preprocessing stage, notably
    * global variables and parameters, and xsl:output elements
    */

    public void process(Context context) throws TransformerException {

        Controller sourceController = context.getController();

        String traceAtt = getAttributeValue(Namespace.SAXON, "trace");
        if (traceAtt!=null && traceAtt.equals("yes")) {
            sourceController.setTraceListener(new com.icl.saxon.trace.SimpleTraceListener());
        }

        // process all the top-level elements

        Vector children = topLevel;

        boolean tracing = sourceController.isTracing();
        TraceListener listener = null;

    	if (tracing) { // e.g.
    	    listener = sourceController.getTraceListener();
    	    for (int i=0; i<children.size(); i++) {
        		Object s = children.elementAt(i);
        		listener.toplevel((NodeInfo)s);
    	    }
    	}

        for (int i=0; i<children.size(); i++) {
            Object s = children.elementAt(i);
            if (s instanceof StyleElement) {
                try {
                    if (tracing && !(s instanceof XSLTemplate)) {
                        listener.enter((StyleElement)s, context);
                        ((StyleElement)s).process(context);
                        listener.leave((StyleElement)s, context);
                    } else {
                        ((StyleElement)s).process(context);
                    }
                } catch (TransformerException err) {
                    throw ((StyleElement)s).styleError(err);
                }
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
// Contributor(s):
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//

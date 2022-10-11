package com.icl.saxon.style;
import com.icl.saxon.Binding;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.pattern.NoNodeTest;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tree.ElementWithAttributes;
import com.icl.saxon.tree.NodeImpl;
import org.w3c.dom.Node;
import org.xml.sax.Locator;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
* Abstract superclass for all element nodes in the stylesheet. <BR>
* Note: this class implements Locator. The element
* retains information about its own location in the stylesheet, which is useful when
* an XSL error is found.
*/

public abstract class StyleElement extends ElementWithAttributes
        implements Locator {

    protected Vector attributeSets = null;
    protected short[] extensionNamespaces = null;		// a list of URI codes
    private short[] excludedNamespaces = null;		// a list of URI codes
    protected String version = null;
    protected StaticContext staticContext = null;
    protected TransformerConfigurationException validationError = null;
    protected int reportingCircumstances = REPORT_ALWAYS;

    // Conditions under which an error is to be reported

    public static final int REPORT_ALWAYS = 1;
    public static final int REPORT_UNLESS_FORWARDS_COMPATIBLE = 2;
    public static final int REPORT_IF_INSTANTIATED = 3;

    /**
    * Constructor
    */

    public StyleElement() {}

    /**
    * Make this node a substitute for a temporary one previously added to the tree. See
    * StyleNodeFactory for details. "A node like the other one in all things but its class".
    * Note that at this stage, the node will not yet be known to its parent, though it will
    * contain a reference to its parent; and it will have no children.
    */

    public void substituteFor(StyleElement temp) {
        this.parent = temp.parent;
        this.attributeList = temp.attributeList;
        this.namespaceList = temp.namespaceList;
        this.nameCode = temp.nameCode;
        this.sequence = temp.sequence;
        this.attributeSets = temp.attributeSets;
        this.extensionNamespaces = temp.extensionNamespaces;
        this.excludedNamespaces = temp.excludedNamespaces;
        this.version = temp.version;
        this.root = temp.root;
        this.staticContext = temp.staticContext;
        this.validationError = temp.validationError;
        this.reportingCircumstances = temp.reportingCircumstances;
    }

	/**
	* Set a validation error
	*/

	protected void setValidationError(TransformerException reason,
	                                  int circumstances) {
	    if (reason instanceof TransformerConfigurationException) {
		    validationError = (TransformerConfigurationException)reason;
		} else {
		    validationError = new TransformerConfigurationException(reason);
		}
		reportingCircumstances = circumstances;
	}

    /**
    * Determine whether this node is an instruction. The default implementation says it isn't.
    */

    public boolean isInstruction() {
        return false;
    }

    /**
    * Determine whether this element does any processing after instantiating any children.
    * The default implementation says it does. Tail recursion only works if call-template is
    * nested entirely in elements that do no such processing. (If the element is empty, this
    * property is irrelevant, because the element cannot contain an xsl:call-template).
    */

    public boolean doesPostProcessing() {
        return true;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return false;
    }

	/**
	* Get the containing XSLStyleSheet element
	*/

	public XSLStyleSheet getContainingStyleSheet() {
		NodeImpl next = this;
		while (!(next instanceof XSLStyleSheet)) {
			next = (NodeImpl)next.getParent();
		}
		return (XSLStyleSheet)next;
	}

    /**
    * Get the import precedence of this stylesheet element.
    */

    public int getPrecedence() {
        return getContainingStyleSheet().getPrecedence();
    }

	/**
	* Get the StandardNames object
	*/

	public final StandardNames getStandardNames() {
		DocumentImpl root = (DocumentImpl)getDocumentRoot();
		return ((StyleNodeFactory)root.getNodeFactory()).getStandardNames();
	}

    /**
    * Process the attributes of this element and all its children
    */

    public void processAllAttributes() throws TransformerConfigurationException {
        staticContext = new ExpressionContext(this);
        processAttributes();
        NodeImpl child = (NodeImpl)getFirstChild();
        while (child != null) {
            if (child instanceof XSLStyleSheet) {
                ((XSLStyleSheet)child).compileError(child.getDisplayName() +
                        " cannot appear as a child of another element");
            } else if (child instanceof StyleElement) {
                ((StyleElement)child).processAllAttributes();
            }
            child = (NodeImpl)child.getNextSibling();
        }
    }

    /**
    * Process the attribute list for the element. This is a wrapper method that calls
    * prepareAttributes (provided in the subclass) and traps any exceptions
    */

    public final void processAttributes() throws TransformerConfigurationException {
        try {
            prepareAttributes();
        } catch (TransformerConfigurationException err) {
        	if (forwardsCompatibleModeIsEnabled()) {
        		//setValidationError(err, REPORT_IF_INSTANTIATED);
        		setValidationError(err, REPORT_UNLESS_FORWARDS_COMPATIBLE);
        	} else {
            	compileError(err);
            }
        }
    }

    /**
    * Check whether an unknown attribute is permitted.
    * @param nc The name code of the attribute name
    */

    protected void checkUnknownAttribute(int nc) throws TransformerConfigurationException {
    	if (forwardsCompatibleModeIsEnabled()) {
    		// then unknown attributes are permitted and ignored
    		return;
    	}
    	String attributeURI = getNamePool().getURI(nc);
    	String elementURI = getURI();
    	int attributeFingerprint = nc & 0xfffff;
    	StandardNames sn = getStandardNames();

    	// allow xsl:extension-element-prefixes etc on an extension element

    	if (isInstruction() &&
    		 attributeURI.equals(Namespace.XSLT) &&
    		 !(elementURI.equals(Namespace.XSLT)) &&
    		 (attributeFingerprint == sn.XSL_EXTENSION_ELEMENT_PREFIXES ||
    		  attributeFingerprint == sn.XSL_EXCLUDE_RESULT_PREFIXES ||
    		  attributeFingerprint == sn.XSL_VERSION)) {
    		return;
    	}

    	if (attributeURI.equals("") || attributeURI.equals(Namespace.XSLT)) {
			compileError("Attribute " + getNamePool().getDisplayName(nc) +
				 " is not allowed on this element");
        }
    }


    /**
    * Set the attribute list for the element. This is called to process the attributes (note
    * the distinction from processAttributes in the superclass).
    * Must be supplied in a subclass
    */

    public abstract void prepareAttributes() throws TransformerConfigurationException;

	/**
	* Make an expression in the context of this stylesheet element
	*/

	public Expression makeExpression(String expression)
	throws TransformerConfigurationException {
	    try {
    		return Expression.make(expression, staticContext);
        } catch(XPathException err) {
            compileError(err);
            return new ErrorExpression(err);
        }
	}

	/**
	* Make a pattern in the context of this stylesheet element
	*/

	public Pattern makePattern(String pattern)
	throws TransformerConfigurationException {
	    try {
		    return Pattern.make(pattern, staticContext);
        } catch(XPathException err) {
            if (forwardsCompatibleModeIsEnabled()) {
                return NoNodeTest.getInstance();
            } else {
                compileError(err);
                return NoNodeTest.getInstance();
            }
        }
	}

	/**
	* Make an attribute value template in the context of this stylesheet element
	*/

	public Expression makeAttributeValueTemplate(String expression)
	throws TransformerConfigurationException {
	    try {
		    return AttributeValueTemplate.make(expression, staticContext);
        } catch(XPathException err) {
            compileError(err);
            return new StringValue(expression);
        }
	}

    /**
    * Process the [xsl:]extension-element-prefixes attribute if there is one
    * @param nc the name code of the attribute required
    */

    protected void processExtensionElementAttribute(int nc)
    throws TransformerConfigurationException {
        String ext = getAttributeValue(nc & 0xfffff);
        if (ext!=null) {
        	// go round twice, once to count the values and next to add them to the array
        	int count = 0;
            StringTokenizer st1 = new StringTokenizer(ext);
            while (st1.hasMoreTokens()) {
                st1.nextToken();
                count++;
            }
			extensionNamespaces = new short[count];
			count = 0;
            StringTokenizer st2 = new StringTokenizer(ext);
            while (st2.hasMoreTokens()) {
                String s = st2.nextToken();
                if (s.equals("#default")) {
                	s = "";
                }
                try {
                    short uriCode = getURICodeForPrefix(s);
                    extensionNamespaces[count++] = uriCode;
                } catch (NamespaceException err) {
                    extensionNamespaces = null;
                    compileError(err.getMessage());
                }
            }
        }
    }

    /**
    * Process the [xsl:]exclude-result-prefixes attribute if there is one
    * @param nc the name code of the attribute required
    */

    protected void processExcludedNamespaces(int nc)
    throws TransformerConfigurationException {
        String ext = getAttributeValue(nc & 0xfffff);
        if (ext!=null) {
        	// go round twice, once to count the values and next to add them to the array
        	int count = 0;
            StringTokenizer st1 = new StringTokenizer(ext);
            while (st1.hasMoreTokens()) {
                st1.nextToken();
                count++;
            }
			excludedNamespaces = new short[count];
			count = 0;
            StringTokenizer st2 = new StringTokenizer(ext);
            while (st2.hasMoreTokens()) {
                String s = st2.nextToken();
                if (s.equals("#default")) {
                	s = "";
                }
                try {
                    short uriCode = getURICodeForPrefix(s);
                    excludedNamespaces[count++] = uriCode;
                } catch (NamespaceException err) {
                    excludedNamespaces = null;
                    compileError(err.getMessage());
                }
            }
        }
    }

    /**
    * Process the [xsl:]version attribute if there is one
    * @param nc the name code of the attribute required
    */

    protected void processVersionAttribute(int nc) {
        version = getAttributeValue(nc & 0xfffff);
    }

    /**
    * Get the version number on this element, or inherited from its ancestors
    */

    public String getVersion() {
        if (version==null) {
            NodeInfo node = (NodeInfo)getParentNode();
            if (node instanceof StyleElement) {
                version = ((StyleElement)node).getVersion();
            } else {
                version = "1.0";    // defensive programming
            }
        }
        return version;
    }

    /**
    * Determine whether forwards-compatible mode is enabled for this element
    */

    public boolean forwardsCompatibleModeIsEnabled() {
        return !(getVersion().equals("1.0"));
    }

    /**
    * Check whether a particular extension element namespace is defined on this node.
    * This checks this node only, not the ancestor nodes.
    * The implementation checks whether the prefix is included in the
    * [xsl:]extension-element-prefixes attribute.
    * @param uriCode the namespace URI code being tested
    */

    protected boolean definesExtensionElement(short uriCode) {
    	if (extensionNamespaces==null) {
    		return false;
    	}
    	for (int i=0; i<extensionNamespaces.length; i++) {
    		if (extensionNamespaces[i] == uriCode) {
    			return true;
    		}
    	}
        return false;
    }

    /**
    * Check whether a namespace uri defines an extension element. This checks whether the
    * namespace is defined as an extension namespace on this or any ancestor node.
    * @param uriCode the namespace URI code being tested
    */

    public boolean isExtensionNamespace(short uriCode) {
        NodeImpl anc = this;
        while (anc instanceof StyleElement) {
            if (((StyleElement)anc).definesExtensionElement(uriCode)) {
                return true;
            }
            anc = (NodeImpl)anc.getParent();
        }
        return false;
    }

    /**
    * Check whether this node excludes a particular namespace from the result.
    * This method checks this node only, not the ancestor nodes.
    * @param uriCode the code of the namespace URI being tested
    */

    protected boolean definesExcludedNamespace(short uriCode) {
    	if (excludedNamespaces==null) {
    		return false;
    	}
    	for (int i=0; i<excludedNamespaces.length; i++) {
    		if (excludedNamespaces[i] == uriCode) {
    			return true;
    		}
    	}
        return false;
    }

    /**
    * Check whether a namespace uri defines an namespace excluded from the result.
    * This checks whether the namespace is defined as an excluded namespace on this
    * or any ancestor node.
    * @param uriCode the code of the namespace URI being tested
    */

    public boolean isExcludedNamespace(short uriCode) {
		if (uriCode==Namespace.XSLT_CODE) return true;
        if (isExtensionNamespace(uriCode)) return true;
        NodeImpl anc = this;
        while (anc instanceof StyleElement) {
            if (((StyleElement)anc).definesExcludedNamespace(uriCode)) {
                return true;
            }
            anc = (NodeImpl)anc.getParent();
        }
        return false;
    }

    /**
    * Check that the element is valid. This is called once for each element, after
    * the entire tree has been built. As well as validation, it can perform first-time
    * initialisation. The default implementation does nothing; it is normally overriden
    * in subclasses.
    */

    public void validate() throws TransformerConfigurationException {}

    /**
    * Default preprocessing method does nothing. It is implemented for those top-level elements
    * that can be evaluated before the source document is available, for example xsl:key,
    * xsl:attribute-set, xsl:template, xsl:locale
    */

    public void preprocess() throws TransformerConfigurationException {}

    /**
    * Recursive walk through the stylesheet to validate all nodes
    */

    public void validateSubtree() throws TransformerConfigurationException {
        if (validationError!=null) {
            if (reportingCircumstances == REPORT_ALWAYS) {
                compileError(validationError);
            } else if (reportingCircumstances == REPORT_UNLESS_FORWARDS_COMPATIBLE
                          && !forwardsCompatibleModeIsEnabled()) {
                compileError(validationError);
            }
        }
        try {
            validate();
        } catch (TransformerConfigurationException err) {
            if (forwardsCompatibleModeIsEnabled()) {
                setValidationError(err, REPORT_IF_INSTANTIATED);
            } else {
                compileError(err);
            }
        }

        validateChildren();

    }

    protected void validateChildren() throws TransformerConfigurationException {
        NodeImpl child = (NodeImpl)getFirstChild();
        while (child != null) {
            if (child instanceof StyleElement) {
                ((StyleElement)child).validateSubtree();
            }
            child = (NodeImpl)child.getNextSibling();
        }
    }

    /**
    * Get the principal XSLStyleSheet node. This gets the principal style sheet, i.e. the
    * one originally loaded, that forms the root of the import/include tree
    */

    protected XSLStyleSheet getPrincipalStyleSheet() {
        XSLStyleSheet sheet = getContainingStyleSheet();
        while (true) {
            XSLStyleSheet next = sheet.getImporter();
            if (next==null) return sheet;
            sheet = next;
        }
    }

    /**
    * Get the PreparedStyleSheet object.
    * @return the PreparedStyleSheet to which this stylesheet element belongs
    */

    public PreparedStyleSheet getPreparedStyleSheet() {
        return getPrincipalStyleSheet().getPreparedStyleSheet();
    }

    /**
    * Check that the stylesheet element is within a template body
    * @throws TransformerConfigurationException if not within a template body
    */

    public void checkWithinTemplate() throws TransformerConfigurationException {
        StyleElement parent = (StyleElement)getParentNode();
        if (!parent.mayContainTemplateBody()) {
            compileError("Element must only be used within a template body");
        }
    }

    /**
    * Convenience method to check that the stylesheet element is at the top level
    * @throws TransformerConfigurationException if not at top level
    */

    public void checkTopLevel() throws TransformerConfigurationException {
        if (!(getParentNode() instanceof XSLStyleSheet)) {
            compileError("Element must only be used at top level of stylesheet");
        }
    }

    /**
    * Convenience method to check that the stylesheet element is not at the top level
    * @throws TransformerConfigurationException if it is at the top level
    */

    public void checkNotTopLevel() throws TransformerConfigurationException {
        if (getParentNode() instanceof XSLStyleSheet) {
            compileError("Element must not be used at top level of stylesheet");
        }
    }

    /**
    * Convenience method to check that the stylesheet element is empty
    * @throws TransformerConfigurationException if it is not empty
    */

    public void checkEmpty() throws TransformerConfigurationException {
        if (getFirstChild()!=null) {
            compileError("Element must be empty");
        }
    }

    /**
    * Convenience method to report the absence of a mandatory attribute
    * @throws TransformerConfigurationException if the attribute is missing
    */

    public void reportAbsence(String attribute)
    throws TransformerConfigurationException {
        compileError("Element must have a \"" + attribute + "\" attribute");
    }

    /**
    * Process: called to do the real work of this stylesheet element. This method
    * must be implemented in each subclass.
    * @param context The context in the source XML document, giving access to the current node,
    * the current variables, etc.
    */

    public abstract void process(Context context) throws TransformerException;

    /**
    * Process the children of this node in the stylesheet
    * @param context The context in the source XML document, giving access to the current node,
    * the current variables, etc.
    */

    public void processChildren(Context context) throws TransformerException {

    	if (context.getController().isTracing()) { // e.g.
    	    TraceListener listener = context.getController().getTraceListener();

    	    NodeImpl node = (NodeImpl)getFirstChild();
    	    while (node!=null) {

        		listener.enter(node, context);

        		if (node.getNodeType() == NodeInfo.TEXT) {
        		    node.copy(context.getOutputter());
        		} else if (node instanceof StyleElement) {
        		    StyleElement snode = (StyleElement)node;
        		    if (snode.validationError != null) {
        		    	fallbackProcessing(snode, context);
        		    } else {
	        		    try {
	        		        context.setStaticContext(snode.staticContext);
	        			    snode.process(context);
	        		    } catch (TransformerException err) {
	        			    throw snode.styleError(err);
	        		    }
	        		}
        		}

        		listener.leave(node, context);
        		node = (NodeImpl)node.getNextSibling();
    	    }

    	} else {

    	    NodeImpl node = (NodeImpl)getFirstChild();
    	    while (node!=null) {

        		if (node.getNodeType() == NodeInfo.TEXT) {
        		    node.copy(context.getOutputter());
        		} else if (node instanceof StyleElement) {
        		    StyleElement snode = (StyleElement)node;
        		    if (snode.validationError != null) {
        		    	fallbackProcessing(snode, context);
        		    } else {
	        		    try {
	        		        context.setStaticContext(snode.staticContext);
	        			    snode.process(context);
	        		    } catch (TransformerException err) {
	        			    throw snode.styleError(err);
	        		    }
	        		}
        		}
        		node = (NodeImpl)node.getNextSibling();
    	    }

    	}
    }

    /**
	* Perform fallback processing
	*/

	protected void fallbackProcessing(StyleElement instruction, Context context) throws TransformerException {
        // process any xsl:fallback children; if there are none, report the original failure reason
        XSLFallback fallback = null;
        Node child = instruction.getFirstChild();
        while (child!=null) {
            if (child instanceof XSLFallback) {
                fallback = (XSLFallback)child;
                break;
            }
            child = child.getNextSibling();
        }

        if (fallback==null) {
        	throw instruction.styleError(instruction.validationError);
        }

        boolean tracing = context.getController().isTracing();

        while (child!=null) {
            if (child instanceof XSLFallback) {
                XSLFallback f = (XSLFallback)child;

                if (tracing) {
                    TraceListener listener = context.getController().getTraceListener();
            		listener.enter(f, context);
            		f.process(context);
            		listener.leave(f, context);
                } else {
                    f.process(context);
                }
            }
            child = child.getNextSibling();
        }


	}

    /**
    * Modify the "select" expression to include any sort keys specified. Used in XSLForEach
    * and XSLApplyTemplates
    */

    protected Expression handleSortKeys(Expression select) throws TransformerConfigurationException {
        // handle sort keys if any

        int numberOfSortKeys = 0;
        boolean sortAllowed = true;
        Node child = getFirstChild();

        while(child!=null) {
            if (child instanceof XSLSort) {
                if (!sortAllowed) {
                    compileError("An xsl:sort element is not allowed here");
                }
                numberOfSortKeys++;
            } else {
                if (!(this instanceof XSLApplyTemplates && child instanceof XSLWithParam)) {
                    sortAllowed = false;
                }
            }
            child = child.getNextSibling();
        }

        if (numberOfSortKeys > 0) {
            SortedSelection sortExpression = new SortedSelection(select, numberOfSortKeys);
            child = getFirstChild();

            int k=0;
            while(child!=null) {
                if (child instanceof XSLSort) {
                    sortExpression.setSortKey(
                        ((XSLSort)child).getSortKeyDefinition(),
                        k++);
                }
                child = child.getNextSibling();
            }
            return sortExpression;

        } else {
            return new NodeListExpression(select);  // sorts into document order
        }
    }

    /**
    * Determine the list of attribute-sets associated with this element.
    * This is used for xsl:element, xsl:copy, xsl:attribute-set, and on literal
    * result elements
    */

    protected void findAttributeSets(String use)
    throws TransformerConfigurationException {

        attributeSets = new Vector(5);

        XSLStyleSheet stylesheet = getPrincipalStyleSheet();
        Vector toplevel = stylesheet.getTopLevel();

        StringTokenizer st = new StringTokenizer(use);
        while (st.hasMoreTokens()) {
            String asetname = st.nextToken();
            int fprint;
            try {
                fprint = makeNameCode(asetname, false) & 0xfffff;
            } catch (NamespaceException err) {
                compileError(err.getMessage());
                fprint = -1;
            }
            boolean found = false;

            // search for the named attribute set, using all of them if there are several with the
            // same name

            for (int i=0; i<toplevel.size(); i++) {
                if (toplevel.elementAt(i) instanceof XSLAttributeSet) {
                    XSLAttributeSet t = (XSLAttributeSet)toplevel.elementAt(i);
                    if (t.getAttributeSetFingerprint() == fprint) {
                        attributeSets.addElement(t);
                        found = true;
                    }
                }
            }

            if (!found) {
                compileError("No attribute-set exists named " + asetname);
            }
        }
    }

    /**
    * Expand the attribute sets referenced in this element's use-attribute-sets attribute
    */

    protected void processAttributeSets(Context context)
    throws TransformerException {
        if (attributeSets==null) return;
        Controller c = context.getController();
        for (int i=0; i<attributeSets.size(); i++) {
            XSLAttributeSet aset = (XSLAttributeSet)attributeSets.elementAt(i);

            // detect circular references. TODO: Ideally we should do this at compile time
            // 2004-10-09: note patch in getUserData(), setUserData()
            Object isBeingExpanded = c.getUserData(aset, "is-being-expanded");
            if (isBeingExpanded!=null) {
                throw styleError("Circular reference to attribute set");
            }
            c.setUserData(aset, "is-being-expanded", "is-being-expanded");
            aset.expand(context);
            c.setUserData(aset, "is-being-expanded", null);
        }
    }

    /**
    * Construct an exception with diagnostic information
    */

    protected TransformerException styleError(TransformerException error) {
        if (error instanceof StyleException) return error;
        if (error instanceof TerminationException) return error;
        if (error.getLocator()==null) {
            return new TransformerException(error.getMessage(),
                                            this,
                                            error.getException());
        }
        return error;
    }

    protected TransformerException styleError(String message) {
        return new TransformerException(message, this);
    }

    /**
    * Construct an exception with diagnostic information
    */

    protected void compileError(TransformerException error)
    throws TransformerConfigurationException {
        if (error.getLocator()==null) {
            error.setLocator(this);
        }
        PreparedStyleSheet pss = getPreparedStyleSheet();
        try {
            if (pss==null) {
                // it is null before the stylesheet has been fully built
                throw error;
            } else {
                pss.reportError(error);
            }
        } catch (TransformerException err2) {
            if (err2 instanceof TransformerConfigurationException) {
                throw (TransformerConfigurationException)err2;
            }
            if (err2.getException() instanceof TransformerConfigurationException) {
                throw (TransformerConfigurationException)err2.getException();
            }
            TransformerConfigurationException tce = new TransformerConfigurationException(error);
            tce.setLocator(this);
            throw tce;
        }
    }

    protected void compileError(String message)
    throws TransformerConfigurationException {
        TransformerConfigurationException tce =
            new TransformerConfigurationException(message);
        tce.setLocator(this);
        compileError(tce);
    }

    /**
    * Test whether this is a top-level element
    */

    public boolean isTopLevel() {
        return (getParentNode() instanceof XSLStyleSheet);
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    * @param fingerprint The fingerprint of the name of the variable
    * @return a Binding for the variable
    * @throws XPathException if the variable has not been declared
    */

    public Binding bindVariable(int fingerprint) throws XPathException {
        Binding binding = getVariableBinding(fingerprint);
        if (binding==null) {
            throw new XPathException("Variable " + getNamePool().getDisplayName(fingerprint) + " has not been declared");
        }
        return binding;
    }

    /**
    * Bind a variable used in this element to the XSLVariable element in which it is declared
    * @param fprint The absolute name of the variable (as a namepool fingerprint)
    * @return a Binding for the variable, or null if it has not been declared
    */

    public Binding getVariableBinding(int fprint) {
        NodeImpl curr = this;
        NodeImpl prev = this;

        // first search for a local variable declaration
        if (!isTopLevel()) {
            while (true) {
                curr = (NodeImpl)curr.getPreviousSibling();
                while (curr==null) {
                    curr = (NodeImpl)prev.getParent();
                    prev = curr;
                    if (curr.getParent() instanceof XSLStyleSheet) break;   // top level
                    curr = (NodeImpl)curr.getPreviousSibling();
                }
                if (curr.getParent() instanceof XSLStyleSheet) break;
                if (curr instanceof Binding) {
                    int var = ((Binding)curr).getVariableFingerprint();
                    if (var==fprint) {
                        return (Binding)curr;
                    }
                }
            }
        }

        // Now check for a global variable
        // we rely on the search following the order of decreasing import precedence.

        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=toplevel.size()-1; i>=0; i--) {
            Object child = toplevel.elementAt(i);
            if (child instanceof Binding && child != this) {
                int var = ((Binding)child).getVariableFingerprint();
                if (var==fprint) {
                    return (Binding)child;
                }
            }
        }

        return null;
    }

    /**
    * List the variables that are in scope for this stylesheet element.
    * Designed for a debugger, not used by the processor.
    * @return two Enumeration of Strings, the global ones [0] and the local ones [1]
    */

    public Enumeration[] getVariableNames() {  // e.g.
        Hashtable local = new Hashtable();
        Hashtable global = new Hashtable();

        NodeImpl curr = this;
        NodeImpl prev = this;
        NamePool pool = getNamePool();

        // first collect the local variable declarations

        if (!isTopLevel()) {
            while (true) {
                curr = (NodeImpl)curr.getPreviousSibling();
                while (curr==null) {
                    curr = (NodeImpl)prev.getParent();
                    prev = curr;
                    if (curr.getParent() instanceof XSLStyleSheet) break;   // top level
                    curr = (NodeImpl)curr.getPreviousSibling();
                }
                if (curr.getParentNode() instanceof XSLStyleSheet) break;
                if (curr instanceof Binding) {
                	int fprint = ((Binding)curr).getVariableFingerprint();
                	String uri = pool.getURI(fprint);
                	String lname = pool.getLocalName(fprint);
                    String varname = uri + '^' + lname;
        		    if (local.get(varname)==null) {
        			    local.put(varname, varname);
        		    }
                }
            }
        }

        // Now collect the global variables
        // we rely on the search following the order of increasing import precedence.

        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=0; i<toplevel.size(); i++) {
            Object child = toplevel.elementAt(i);
            if (child instanceof Binding && child != this) {
            	int fprint = ((Binding)child).getVariableFingerprint();
            	String uri = pool.getURI(fprint);
            	String lname = pool.getLocalName(fprint);
                String varname = uri + '^' + lname;
        		if (local.get(varname)==null) {
        		    global.put(varname, varname);
        		}
            }
        }

    	Enumeration info[] = new Enumeration[2];
    	info[0] = global.keys();
    	info[1] = local.keys();
        return info;
    }


    /**
    * Get a Function declared using a saxon:function element in the stylesheet
    * @param fingerprint the fingerprint of the name of the function
    * @return the Function object represented by this saxon:function; or null if not found
    */

    public Function getStyleSheetFunction(int fingerprint) {

        // we rely on the search following the order of decreasing import precedence.

        XSLStyleSheet root = getPrincipalStyleSheet();
        Vector toplevel = root.getTopLevel();
        for (int i=toplevel.size()-1; i>=0; i--) {
            Object child = toplevel.elementAt(i);
            if (child instanceof SAXONFunction &&
                    ((SAXONFunction)child).getFunctionFingerprint() == fingerprint) {
                StyleSheetFunctionCall fc = new StyleSheetFunctionCall();
                fc.setFunction((SAXONFunction)child);
                return fc;
            }
        }
        return null;
    }

	/** needed to satisfy Locator interface
	*/

	//public int getColumnNumber() {
	//	return -1;
	//}
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

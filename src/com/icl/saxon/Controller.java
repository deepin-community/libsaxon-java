package com.icl.saxon;
import com.icl.saxon.expr.*;
import com.icl.saxon.om.*;
import com.icl.saxon.output.*;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.sort.NodeOrderComparer;
import com.icl.saxon.style.TerminationException;
import com.icl.saxon.style.XSLStyleSheet;
import com.icl.saxon.tinytree.TinyBuilder;
import com.icl.saxon.trace.SaxonEventMulticaster;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.tree.TreeBuilder;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;


/**
  * <B>Controller</B> processes an XML file, calling registered node handlers
  * when appropriate to process its elements, character content, and attributes. <P>
  * @version 10 December 1999: methods for building the tree extracted to class Builder,
  * methods for maintaining rulesets extracted to RuleManager.<p>
  * The Controller class now incorporates the previous <b>StylesheetInstance</b> class.
  * A StyleSheetInstance represents a single execution of a prepared stylesheet.
  * A PreparedStyleSheet can be used any number of times, in series or in parallel,
  * but each use of it to render a source document requires a separate Controller
  * object, which is not reusable or shareable.<p>
  * The Controller is capable of comparing whether nodes are in document order;
  * therefore it acts as a NodeOrderComparer.
  * @author Michael H. Kay
  */

public class Controller extends Transformer implements NodeOrderComparer {

    // Policies for handling recoverable errors

    public static final int RECOVER_SILENTLY = 0;
    public static final int RECOVER_WITH_WARNINGS = 1;
    public static final int DO_NOT_RECOVER = 2;

    private TransformerFactoryImpl factory;
    private Bindery bindery;                // holds values of global and local variables
    private NamePool namePool;
    private DecimalFormatManager decimalFormatManager;
    private Emitter messageEmitter;
    private RuleManager ruleManager;
    private Properties outputProperties;
    private Outputter currentOutputter;
    private ParameterSet parameters;
    private PreparedStyleSheet preparedStyleSheet;
    private TraceListener traceListener; // e.g.
    private boolean tracingIsSuspended = false;
    private URIResolver standardURIResolver;
    private URIResolver userURIResolver;
    private ErrorListener errorListener;
    private XSLStyleSheet styleSheetElement;
    private int recoveryPolicy = RECOVER_WITH_WARNINGS;
    private int treeModel = Builder.TINY_TREE;
    private boolean disableStripping = false;

    private DocumentPool sourceDocumentPool;
    private Hashtable userDataTable;
    private boolean lineNumbering;
    private boolean preview;
    private String diagnosticName = null;

    /**
    * Default constructor is provided for Java-only programs, i.e. applications
    * that use the RuleManager to set up Java handlers for nodes, without using
    * a stylesheet
    */

    public Controller() {
        this(new TransformerFactoryImpl());
        bindery = new Bindery();
    }

    /**
    * Create a Controller and initialise variables. Constructor is protected,
    * the Controller should be created using newTransformer() in the PreparedStyleSheet
    * class.
    */

    protected Controller(TransformerFactoryImpl factory) {
        this.factory = factory;
		namePool = NamePool.getDefaultNamePool();
        standardURIResolver = new StandardURIResolver(factory);
        userURIResolver = factory.getURIResolver();

        errorListener = factory.getErrorListener();
        if (errorListener instanceof StandardErrorListener) {
            ((StandardErrorListener)errorListener).setRecoveryPolicy(
                            ((Integer)factory.getAttribute(FeatureKeys.RECOVERY_POLICY))
                              .intValue());
        }
        sourceDocumentPool = new DocumentPool();
        userDataTable = new Hashtable();

        TraceListener tracer = (TraceListener)factory.getAttribute(FeatureKeys.TRACE_LISTENER);
        if (tracer!=null) {
            addTraceListener(tracer);
        }

        Boolean num = (Boolean)factory.getAttribute(FeatureKeys.LINE_NUMBERING);
        if (num!=null && num.booleanValue()) {
            setLineNumbering(true);
        }

        Integer model = (Integer)factory.getAttribute(FeatureKeys.TREE_MODEL);
        if (model!=null) {
            setTreeModel(model.intValue());
        }

    }

    /**
     * <p>Reset this <code>Transformer</code> to its original configuration.</p>
     * <p/>
     * <p><code>Transformer</code> is reset to the same state as when it was created with
     * {@link javax.xml.transform.TransformerFactory#newTransformer()},
     * {@link javax.xml.transform.TransformerFactory#newTransformer(javax.xml.transform.Source source)} or
     * {@link javax.xml.transform.Templates#newTransformer()}.
     * <code>reset()</code> is designed to allow the reuse of existing <code>Transformer</code>s
     * thus saving resources associated with the creation of new <code>Transformer</code>s.</p>
     * <p/>
     * <p>The reset <code>Transformer</code> is not guaranteed to have the same {@link javax.xml.transform.URIResolver}
     * or {@link javax.xml.transform.ErrorListener} <code>Object</code>s, e.g. {@link Object#equals(Object obj)}.
     * It is guaranteed to have a functionally equal <code>URIResolver</code>
     * and <code>ErrorListener</code>.</p>
     *
     * <p>NOTE: the Saxon implementation of this method does not clear the document pool. This is because the
     * reason for resetting an existing Transformer rather than creating a new one is to reuse resources, and
     * the document pool is the most important resource held by the Transformer. If there is a requirement to
     * clear the document pool, then it is possible either (a) to call the {@link #clearDocumentPool()} method,
     * or (b) to create a new Transformer.
     *
     * @since 1.5
     */
    public void reset() {
        clearParameters();
        namePool = NamePool.getDefaultNamePool();
        standardURIResolver = new StandardURIResolver(factory);
        userURIResolver = factory.getURIResolver();
        currentOutputter = null;
        messageEmitter = null;
        outputProperties = null;

        errorListener = factory.getErrorListener();
        if (errorListener instanceof StandardErrorListener) {
            ((StandardErrorListener)errorListener).setRecoveryPolicy(
                            ((Integer)factory.getAttribute(FeatureKeys.RECOVERY_POLICY))
                              .intValue());
        }
        userDataTable = new Hashtable();

        TraceListener tracer = (TraceListener)factory.getAttribute(FeatureKeys.TRACE_LISTENER);
        if (tracer!=null) {
            addTraceListener(tracer);
        }

        Boolean num = (Boolean)factory.getAttribute(FeatureKeys.LINE_NUMBERING);
        if (num!=null && num.booleanValue()) {
            setLineNumbering(true);
        }

        Integer model = (Integer)factory.getAttribute(FeatureKeys.TREE_MODEL);
        if (model!=null) {
            setTreeModel(model.intValue());
        }

    }

    public TransformerFactoryImpl getTransformerFactory() {
        return factory;
    }

    /**
    * Set a diagnostic name for this transformation (accessible through toString())
    */

    public void setDiagnosticName(String name) {
        diagnosticName = name;
    }

    public String toString() {
        if (diagnosticName==null) {
            return super.toString();
        } else {
            return diagnosticName;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // Methods to process the tree
    //////////////////////////////////////////////////////////////////////////


    /**
    * Process a Document.<p>
    * This method is intended for use when performing a pure Java transformation,
    * without a stylesheet. Where there is an XSLT stylesheet, use transformDocument()
    * or transform() instead: those methods set up information from the stylesheet before calling
    * run(). <p>
    * The process starts by calling the registered node
    * handler to process the supplied node. Note that the same document can be processed
    * any number of times, typically with different node handlers for each pass. The NodeInfo
    * will typically be the root of a tree built using com.icl.saxon.om.Builder.<p>
    */

    public void run(NodeInfo node) throws TransformerException
    {
        Context initialContext = makeContext(node);
        applyTemplates(
            initialContext,
            new SingletonNodeSet(node),
            getRuleManager().getMode(-1),
            null);
    }

    /**
    * ApplyTemplates to process selected nodes using the handlers registered for a particular
    * mode.<br>
    * @param select A node-set expression (or more accurately a node-list)
    * that determines which nodes are selected.
    * Note: if the nodes are to be sorted, the select Expression will take care of this.
    * @param mode Identifies the processing mode. It should match the mode defined when the
    * element handler was registered using setHandler with a mode parameter. Set this parameter to
    * null to invoke the default mode.
    * @param parameters A ParameterSet containing the parameters to the handler/template being invoked.
    * Specify null if there are no parameters.
    */

    public void applyTemplates(Context c, Expression select, Mode mode, ParameterSet parameters)
            throws TransformerException
    {
        // Get an enumerator to iterate through the selected nodes
        NodeEnumeration enm;
        if (select==null) {
            enm = c.getCurrentNodeInfo().getEnumeration(Axis.CHILD, AnyNodeTest.getInstance());
        } else {
            enm = select.enumerate(c, false);
        }

        // if the enumerator can't calculate last() position, we wrap it in one that can.

        if (!(enm instanceof LastPositionFinder)) {
            enm = new LookaheadEnumerator(enm);
        }

        int position = 1;
        Context context = c.newContext();
        context.setLastPositionFinder((LastPositionFinder)enm);
        context.setMode(mode);
        while(enm.hasMoreElements()) {
            NodeInfo node = enm.nextElement();
            //if (node==null) {
            //    System.err.println("Got null node from " + enm.getClass());
            //    node.getDisplayName();  // break it now
            //}

            context.setCurrentNode(node);
            context.setContextNode(node);
            context.setPosition(position++);

            // find the node handler for this node

            NodeHandler eh = ruleManager.getHandler(node, mode, context);

            if (eh==null) {             // Use the default action for the node
                                        // No need to open a new stack frame!
                defaultAction(node, context);

            } else {
                if (eh.needsStackFrame()) {
                    bindery.openStackFrame(parameters);
                    if (isTracing()) { // e.g.
                	    traceListener.enterSource(eh, context);
                 	    eh.start(node, context);
                	    traceListener.leaveSource(eh, context);
                	} else {
                 	    eh.start(node, context);
                	}
                    bindery.closeStackFrame();
                } else {
                    if (isTracing()) { // e.g.
                	    traceListener.enterSource(eh, context);
                 	    eh.start(node, context);
                	    traceListener.leaveSource(eh, context);
                	} else {
                 	    eh.start(node, context);
                	}
                }
            }

        }
    };

    /**
    * Perform the built-in template action for a given node
    */

    private void defaultAction(NodeInfo node, Context context) throws TransformerException {
        switch(node.getNodeType()) {
            case NodeInfo.ROOT:
            case NodeInfo.ELEMENT:
	            applyTemplates(context, null, context.getMode(), null);
	            return;
	        case NodeInfo.TEXT:
	        case NodeInfo.ATTRIBUTE:
	            node.copyStringValue(getOutputter());
	            return;
	        case NodeInfo.COMMENT:
	        case NodeInfo.PI:
	        case NodeInfo.NAMESPACE:
	            // no action
	            return;
        }
    }

    /**
    * Apply a template imported from the stylesheet containing the current template
    */

    public void applyImports(Context c, Mode mode, int min, int max, ParameterSet params)
    throws TransformerException {
        NodeInfo node = c.getCurrentNodeInfo();
        NodeHandler nh = ruleManager.getHandler(node, mode, min, max, c);

		if (nh==null) {             // use the default action for the node
            defaultAction(node, c);
        } else {
            bindery.openStackFrame(params);
            nh.start(node, c);
            bindery.closeStackFrame();
        }
    }

    /**
    * Compare the position of two nodes in document order
    * @param n1 The first node
    * @param n2 The second node
    * @return <0 if the first node is first in document order; >0 if
    * the second node comes first in document order; 0 if the two parameters
    * identify the same node
    */

    public int compare(NodeInfo n1, NodeInfo n2) {
        if (sourceDocumentPool.getNumberOfDocuments() == 1) {
            return n1.compareOrder(n2);
        }
        int doc1 = sourceDocumentPool.getDocumentNumber(n1.getDocumentRoot());
        int doc2 = sourceDocumentPool.getDocumentNumber(n2.getDocumentRoot());
        if (doc1==doc2) {
            return n1.compareOrder(n2);
        } else {
            return (doc1 - doc2);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Methods for managing output destinations and formatting
    ////////////////////////////////////////////////////////////////////////////////

    /**
    * Set the output properties for the transformation.  These
    * properties will override properties set in the templates
    * with xsl:output.
    */

    public void setOutputProperties(Properties properties) {
        Enumeration keys = properties.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            setOutputProperty(key, (String)properties.get(key));
        }
    }

    /**
    * Get the output properties for the transformation.
    */

    public Properties getOutputProperties() {
        if (outputProperties == null) {
            if (preparedStyleSheet==null) {
                return new Properties();
            } else {
                outputProperties = preparedStyleSheet.getOutputProperties();
            }
        }

        // Make a copy, so that modifications to the returned properties have no effect

        Properties newProps = new Properties();
        Enumeration keys = outputProperties.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            newProps.put(key, (String)outputProperties.get(key));
        }
        return newProps;
    }

    /**
    * Set an output property for the transformation.
    */

    public void setOutputProperty(String name, String value) {
        if (outputProperties == null) {
            outputProperties = getOutputProperties();
        }
        if (!SaxonOutputKeys.isValidOutputKey(name)) {
            throw new IllegalArgumentException(name);
        }
        outputProperties.put(name, value);
    }

    /**
    * Get the value of an output property
    */

    public String getOutputProperty(String name) {
        if (outputProperties == null) {
            outputProperties = getOutputProperties();
        }
        return outputProperties.getProperty(name);
        // TODO: validate that the name is a recognized property
    }

    /**
    * Set a new output destination, supplying the output format details. <BR>
    * This affects all further output until resetOutputDestination() is called. Note that
    * it is the caller's responsibility to close the Writer after use.
    * @param props Details of the new output format
    * @param result Details of the new output destination
    */

    public void changeOutputDestination(Properties props, Result result)
    throws TransformerException {
        GeneralOutputter out = new GeneralOutputter(namePool);
        out.setOutputDestination(props, result);
        currentOutputter = out;
    }

    /**
    * Set a simple StringBuffer output destination. Used during calls to
    * xsl:attribute, xsl:comment, xsl:processing-instruction
    */

    public void changeToTextOutputDestination(StringBuffer buffer) {
        StringOutputter out = new StringOutputter(buffer);
        out.setErrorListener(errorListener);
        currentOutputter = out;
    }

    /**
    * Get the current outputter
    */

    public Outputter getOutputter() {
        return currentOutputter;
    }

    /**
    * Close the current outputter, and revert to the previous outputter.
    * @param outputter The outputter to revert to
    */

    public void resetOutputDestination(Outputter outputter) throws TransformerException {
        //System.err.println("resetOutputDestination");
        if (currentOutputter==null) {
            throw new TransformerException("No outputter has been allocated");
        }
        currentOutputter.close();
        currentOutputter = outputter;
    }


    ///////////////////////////////////////////////////////////////////////////////

    /**
    * Make an Emitter to be used for xsl:message output
    */

    public Emitter makeMessageEmitter() throws TransformerException {
        String emitterClass = (String)factory.getAttribute(FeatureKeys.MESSAGE_EMITTER_CLASS);

        Object emitter = Loader.getInstance(emitterClass);
        if (!(emitter instanceof Emitter)) {
            throw new TransformerException(emitterClass + " is not an Emitter");
        }
        messageEmitter = (Emitter)emitter;
        return messageEmitter;
    }

    /**
    * Set the Emitter to be used for xsl:message output
    */

    public void setMessageEmitter(Emitter emitter) {
        messageEmitter = emitter;
    }

    /**
    * Get the Emitter used for xsl:message output
    */

    public Emitter getMessageEmitter() {
       return messageEmitter;
    }

    /**
    * Set the policy for handling recoverable errors
    */

    public void setRecoveryPolicy(int policy) {
        recoveryPolicy = policy;
        if (errorListener instanceof StandardErrorListener) {
            ((StandardErrorListener)errorListener).setRecoveryPolicy(policy);
        }
    }

    /**
    * Get the policy for handling recoverable errors
    */

    public int getRecoveryPolicy() {
        return recoveryPolicy;
    }

	/**
	* Set the error listener
	*/

	public void setErrorListener(ErrorListener listener) {
		errorListener = listener;
	}

	/**
	* Get the error listener
	*/

	public ErrorListener getErrorListener() {
		return errorListener;
	}

    /**
    * Report a recoverable error
    * @throws TransformerException if the error listener decides not to recover
    * from the error
    */

    public void reportRecoverableError(String message, SourceLocator location) throws TransformerException {
        if (location==null) {
            errorListener.warning(new TransformerException(message));
        } else {
            TransformerException err = new TransformerException(message, location);
            errorListener.warning(err);
        }
    }

    /**
    * Report a recoverable error
    * @throws TransformerException if the error listener decides not to recover
    * from the error
    */

    public void reportRecoverableError(TransformerException err) throws TransformerException {
        errorListener.warning(err);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods for managing the Context and Bindery objects
    /////////////////////////////////////////////////////////////////////////////////////////


    /**
    * Get the document pool. This is used only for source documents, not for stylesheet modules
    */

    public DocumentPool getDocumentPool() {
        return sourceDocumentPool;
    }

    /**
    * Clear the document pool. This is sometimes useful when using the same Transformer
    * for a sequence of transformations, but it isn't done automatically, because when
    * the transformations use common look-up documents, the caching is beneficial.
    */

    public void clearDocumentPool() {
        sourceDocumentPool = new DocumentPool();
    }

    /**
    * Set line numbering (of the source document) on or off
    */

    public void setLineNumbering(boolean onOrOff) {
        lineNumbering = onOrOff;
    }

    /**
    * Determine whether line numbering is enabled
    */

    public boolean isLineNumbering() {
        return lineNumbering;
    }

    /**
    * Create a new context with a given node as the current node and the only node in the current
    * node list.
    */

    public Context makeContext(NodeInfo node) {
        Context c = new Context(this);
        c.setCurrentNode(node);
        c.setContextNode(node);
        c.setPosition(1);
        c.setLast(1);
        return c;
    }

    /**
    * Get the current bindery
    */

    public Bindery getBindery() {
        return bindery;
    }

    /**
    * Get the primary URI resolver.
    * @return the user-supplied URI resolver if there is one, or the system-defined one
    * otherwise (Note, this isn't quite as JAXP specifies it).
    */

    public URIResolver getURIResolver() {
        return (userURIResolver==null ? standardURIResolver : userURIResolver);
    }

    /**
    * Get the fallback URI resolver.
    * @return the the system-defined URIResolver
    */

    public URIResolver getStandardURIResolver() {
        return standardURIResolver;
    }


    /**
    * Get the KeyManager
    */

    public KeyManager getKeyManager() {
        return styleSheetElement.getKeyManager();
    }

	/**
	* Set the name pool to be used
	*/

	public void setNamePool(NamePool pool) {
		namePool = pool;
	}

	/**
	* Get the name pool in use
	*/

	public NamePool getNamePool() {
		return namePool;
	}

    /**
    * Set the tree data model to use
    */

    public void setTreeModel(int model) {
        treeModel = model;
    }

    /**
    * Get the tree model in use
    */

    public int getTreeModel() {
        return treeModel;
    }

    /**
    * Disable whitespace stripping
    */

    public void disableWhitespaceStripping(boolean disable) {
        disableStripping = disable;
    }

    /**
    * Determine if whitespace stripping is disabled
    */

    public boolean isWhitespaceStrippingDisabled() {
        return disableStripping;
    }

    /**
    * Make a builder for the selected tree model
    */

    public Builder makeBuilder() {
        Builder b;
        if (treeModel==Builder.TINY_TREE)  {
            b = new TinyBuilder();
        } else {
            b = new TreeBuilder();
        }
        Boolean timing = (Boolean)factory.getAttribute(FeatureKeys.TIMING);
        b.setTiming((timing==null ? false : timing.booleanValue()));
        b.setNamePool(namePool);
        b.setLineNumbering(lineNumbering);
        b.setErrorListener(errorListener);

        Stripper stripper = makeStripper(b);
        return b;
    }

    public Stripper makeStripper(Builder b) {
        Stripper s;
        if (styleSheetElement==null) {
            s = new Stripper(new Mode());
        } else {
            s = styleSheetElement.newStripper();
        }
        s.setController(this);
        s.setUnderlyingEmitter(b);
		b.setStripper(s);
        return s;
    }

    //////////////////////////////////////////////////////////////////////
    // Methods for handling decimal-formats
    //////////////////////////////////////////////////////////////////////


    public void setDecimalFormatManager(DecimalFormatManager manager) {
        decimalFormatManager = manager;
    }

    public DecimalFormatManager getDecimalFormatManager() {
        return decimalFormatManager;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for registering and retrieving handlers for template rules
    ////////////////////////////////////////////////////////////////////////////////

    public void setRuleManager(RuleManager r) {
        ruleManager = r;
    }

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods for tracing
    /////////////////////////////////////////////////////////////////////////

    public void setTraceListener(TraceListener trace) { // e.g.
        traceListener = trace;
    }

    public TraceListener getTraceListener() { // e.g.
        return traceListener;
    }

    public final boolean isTracing() { // e.g.
        return traceListener != null && !tracingIsSuspended;
    }

    public void pauseTracing(boolean pause) {
        tracingIsSuspended = pause;
    }

    /**
    * Associate this Controller with a compiled stylesheet
    */

    public void setPreparedStyleSheet(PreparedStyleSheet sheet) {
        preparedStyleSheet = sheet;
        styleSheetElement = (XSLStyleSheet)sheet.getStyleSheetDocument().getDocumentElement();
        preview = (styleSheetElement.getPreviewManager() != null);
        //setOutputProperties(sheet.getOutputProperties());
        // above line deleted for bug 490964 - may have side-effects
    }

    /**
    * Does this transformation use preview mode?
    */

    protected boolean usesPreviewMode() {
        return preview;
    }

    /**
    * Internal method to create and initialize a controller
    */

    private void initializeController() {
        setRuleManager(styleSheetElement.getRuleManager());
        setDecimalFormatManager(styleSheetElement.getDecimalFormatManager());

        if (traceListener!=null) {
            traceListener.open();
        }

        // get a new bindery, to clear out any variables from previous runs

        bindery = new Bindery();
        styleSheetElement.initialiseBindery(bindery);

        // if parameters were supplied, set them up

        bindery.defineGlobalParameters(parameters);
    }

    /**
    * Adds the specified trace listener to receive trace events from
    * this instance.
    * Must be called before the invocation of the render method.
    * @param    trace the trace listener.
    */

    public void addTraceListener(TraceListener trace) { // e.g.
        traceListener = SaxonEventMulticaster.add(traceListener, trace);
    }

    /**
    * Removes the specified trace listener so that the next invocation
    * of the render method will not send trace events to the listener.
    * @param    trace the trace listener.
    */

    public void removeTraceListener(TraceListener trace) { // e.g.
        traceListener = SaxonEventMulticaster.remove(traceListener, trace);
    }

    /////////////////////////////////////////////////////////////////////////
    // Allow user data to be associated with nodes on a tree
    /////////////////////////////////////////////////////////////////////////

    /**
    * Get the named user data property for the node
    * @param name the name of the user data property to return
    * @return The value of the named user data property.
    * Returns null if no property of that name has been set using setUserData()
    * for this NodeInfo object.
    */

    public Object getUserData(NodeInfo node, String name)  {
        String key = name + ' ' + getDocumentPool().getDocumentNumber(node.getDocumentRoot()) + node.generateId();
// ABOVE LINE PATCHED 2004-10-08
        return userDataTable.get(key);
    }

    /**
    * Set a user data property for a node.
    * @param name The name of the user data property to be set. Any existing user data property
    * of the same name will be overwritten.
    * @param data an object to be saved with this element, which can be
    * retrieved later using getUserData().
    */

    public void setUserData(NodeInfo node, String name, Object data)  {
        String key = name + ' ' + getDocumentPool().getDocumentNumber(node.getDocumentRoot()) + node.generateId();
// ABOVE LINE PATCHED 2004-10-08
        if (data==null) {
            userDataTable.remove(key);
        } else {
            userDataTable.put(key, data);
        }
    }


    /////////////////////////////////////////////////////////////////////////
    // implement the javax.xml.transform.Transformer methods
    /////////////////////////////////////////////////////////////////////////

    /**
    * Process the source tree to SAX parse events.
    * @param source  The input for the source tree.
    * @param result The destination for the result tree.
    * @throws TransformerException if the transformation fails. As a special case,
    * the method throws a TerminationException (a subclass of TransformerException)
    * if the transformation was terminated using xsl:message terminate="yes".
    */

    public void transform(Source source, Result result) throws TransformerException {
        if (preparedStyleSheet==null) {
            throw new TransformerException("Stylesheet has not been prepared");
        }

        PreviewManager pm = styleSheetElement.getPreviewManager();
        preview = (pm!=null);

        String path = "/";

        try {
            if (source instanceof NodeInfo) {
                // Any Saxon NodeInfo can be used directly as a Source
                if (preview) {
                    throw new TransformerException("Preview mode requires serial input");
                }
                transformDocument((NodeInfo)source, result);
                return;
            }
            if (source instanceof DOMSource) {
                DOMSource ds = (DOMSource)source;

                if (preview) {
                    throw new TransformerException("Preview mode requires serial input");
                }
                if (disableStripping || !styleSheetElement.stripsWhitespace()) {

                    if ( ds.getNode() instanceof NodeInfo ) {

                        // bypass the tree building stage, and work on the tree as supplied

                        transformDocument((NodeInfo)ds.getNode(), result);
                        return;
                    }
                }
                path = getPathToNode(ds.getNode());
                // System.err.println("path = " + path);
            }

        	SAXSource in = factory.getSAXSource(source, false);

            // System.err.println("transform " + diagnosticName);

            if (preview) {
                // run the build in preview mode
                initializeController();
                //pm.setController(this);

                // in preview mode we don't try to use xsl:output properties
                // for the principal output file, because in XSLT 1.1 they can
                // be attribute value templates, and we can't evaluate these yet.

                if (outputProperties==null) {
                    outputProperties = new Properties();
                }
                changeOutputDestination(outputProperties, result);

                Builder sourceBuilder = makeBuilder();
                sourceBuilder.setController(this);
                sourceBuilder.setPreviewManager(pm);
                sourceBuilder.setNamePool(namePool);
                DocumentInfo doc = sourceBuilder.build(in);
                sourceDocumentPool.add(doc, null);
                sourceBuilder = null;   // give the garbage collector a chance

                transformDocument(doc, result);
                resetOutputDestination(null);

            } else {
                Builder sourceBuilder = makeBuilder();
                DocumentInfo doc = sourceBuilder.build(in);
                // ((com.icl.saxon.tinytree.TinyDocumentImpl)doc).diagnosticDump();
                sourceDocumentPool.add(doc, null);
                sourceBuilder = null;   // give the garbage collector a chance

                NodeInfo startNode = doc;
                if (!path.equals("/")) {
                    Expression exp = Expression.make(path, new StandaloneContext(namePool));
                    Context c = makeContext(doc);
                    NodeEnumeration enm = exp.enumerate(c, false);
                    if (enm.hasMoreElements()) {
                        startNode = enm.nextElement();
                    } else {
                        throw new TransformerException("Problem finding the start node after converting DOM to Saxon tree");
                    }
                }

                transformDocument(startNode, result);
            }
        } catch (TerminationException err) {
            //System.err.println("Processing terminated using xsl:message");
            throw err;
        } catch (TransformerException err) {
            Throwable cause = err.getException();
            if (cause != null && cause instanceof SAXParseException) {
                // already reported
            } else {
                errorListener.fatalError(err);
            }
            throw err;
        }
    }

    /**
    * Get an XPath expression referencing a node in a DOM
    */

    private String getPathToNode(Node startNode) throws TransformerException {
        short nodeType = startNode.getNodeType();
        String path;
        if ( nodeType == Node.DOCUMENT_NODE ) {
            path = "/";
        } else if ( nodeType == Node.ELEMENT_NODE ) {
            path = "";
            Node curr = startNode;
            while ( nodeType == Node.ELEMENT_NODE ) {
                int count = 1;
                Node prior = curr.getPreviousSibling();
                while (prior != null) {
                    short ptype = prior.getNodeType();
                    if (ptype == Node.ELEMENT_NODE) {
                        count++;
                    } else if (ptype == Node.CDATA_SECTION_NODE ||
                                 ptype == Node.ENTITY_REFERENCE_NODE) {
                        throw new TransformerException(
                            "Document contains CDATA or Entity nodes: can only transform starting at root");
                    }
                    prior = prior.getPreviousSibling();
                }
                if (!path.equals("")) {
                    path = '/' + path;
                }
                path = "*[" + count + ']' + path;

                curr = curr.getParentNode();
                if (curr==null) {
                    throw new TransformerException("Supplied element is not within a Document");
                }
                nodeType = curr.getNodeType();
                if (nodeType == Node.DOCUMENT_NODE) {
                    path = '/' + path;
                } else if (nodeType == Node.CDATA_SECTION_NODE ||
                            nodeType == Node.ENTITY_REFERENCE_NODE) {
                    throw new TransformerException(
                        "Document contains CDATA or Entity nodes: can only transform starting at root");
                }
            }
        } else {
            throw new TransformerException("Start node must be either the root or an element");
        }
        return path;
    }

    /**
    * Render a source XML document supplied as a tree. <br>
    * A new output destination should be created for each source document,
    * by using setOutputDetails(). <br>
    * @param startNode A Node that identifies the source document to be transformed and the
    * node where the transformation should start
    * @param result The output destination
    */

    public void transformDocument(NodeInfo startNode, Result result)
    throws TransformerException {

        DocumentInfo sourceDoc;
        if (startNode instanceof DocumentInfo) {
            sourceDoc = (DocumentInfo)startNode;
        } else {
            sourceDoc = startNode.getDocumentRoot();
        }

        if (styleSheetElement==null) {
            throw new TransformerException("Stylesheet has not been prepared");
        }

        if (sourceDoc.getNamePool()==null) {
            // must be a non-standard document implementation
            sourceDoc.setNamePool(preparedStyleSheet.getNamePool());
        }

        if (sourceDoc.getNamePool() != preparedStyleSheet.getNamePool()) {
        	throw new TransformerException("Source document and stylesheet must use the same name pool");
        }

        Context context = makeContext(sourceDoc);

        if (!preview) {
            initializeController();
            Properties xslOutputProps = new Properties();
            styleSheetElement.updateOutputProperties(xslOutputProps, context);
            // overlay the output properties defined via the API
            if (outputProperties!=null) {
                Enumeration enm = outputProperties.propertyNames();
                while (enm.hasMoreElements()) {
                    String p = (String)enm.nextElement();
                    String v = outputProperties.getProperty(p);
                    xslOutputProps.put(p, v);
                }
            }

            // deal with stylesheet chaining
            String nextInChain = xslOutputProps.getProperty(SaxonOutputKeys.NEXT_IN_CHAIN);
            if (nextInChain != null) {
                String baseURI = xslOutputProps.getProperty(SaxonOutputKeys.NEXT_IN_CHAIN_BASE_URI);
                result = prepareNextStylesheet(nextInChain, baseURI, result);
            }

            changeOutputDestination(xslOutputProps, result);
        }

        // process the stylesheet document
        // (The main function of this phase is to evaluate global variables)

        styleSheetElement.process(context);

        // Process the source document using the handlers that have been set up

        run(startNode);

        if (traceListener!=null) {
            traceListener.close();
        }

        if (!preview) {
            resetOutputDestination(null);
        }

    }

    /**
    * Prepare another stylesheet to handle the output of this one
    */

    private Result prepareNextStylesheet(String href, String baseURI, Result result)
    throws TransformerException {

        //TODO: should cache the results, we are recompiling the referenced
        //stylesheet each time it's used

        //TODO: combine with similar method in XSLGeneralOutput

        Source source = getURIResolver().resolve(href, baseURI);
        SAXSource saxSource = factory.getSAXSource(source, true);

        Templates next = factory.newTemplates(source);
        TransformerHandler nextTransformer = factory.newTransformerHandler(next);

        ContentHandlerProxy emitter = new ContentHandlerProxy();
        emitter.setUnderlyingContentHandler(nextTransformer);
        emitter.setSystemId(saxSource.getSystemId());   // pragmatic choice of system ID
        emitter.setRequireWellFormed(false);
        nextTransformer.setResult(result);

        return emitter;
    }

    //////////////////////////////////////////////////////////////////////////
    // Handle parameters to the transformation
    //////////////////////////////////////////////////////////////////////////

    /**
    * Set a parameter for the transformation.
    * @param expandedName The name of the parameter in {uri}local format
    * @param value The value object.  This can be any valid Java object
    * it follows the same conversion rules as a value returned from a Saxon extension function.
    */

    public void setParameter(String expandedName, Object value) {

        if (parameters == null) {
            parameters = new ParameterSet();
        }

        Value result;
        try {
            result = FunctionProxy.convertJavaObjectToXPath(value, this);
        } catch (TransformerException err) {
            result = new StringValue(value.toString());
        }
        int fingerprint = getFingerprintForExpandedName(expandedName);
        parameters.put(fingerprint, result);

    }

    /**
    * Set parameters supplied externally (typically, on the command line).
    * (non-TRAX method retained for backwards compatibility)
    * @param params A ParameterSet containing the (name, value) pairs.
    */

    public void setParams(ParameterSet params) {
        this.parameters = params;
    }

    /**
    * Get fingerprint for expanded name in {uri}local format
    */

    private int getFingerprintForExpandedName(String expandedName) {
        String localName;
        String namespace;

        if (expandedName.charAt(0)=='{') {
            int closeBrace = expandedName.indexOf('}');
            if (closeBrace < 0) {
                throw new IllegalArgumentException("No closing '}' in parameter name");
            }
            namespace = expandedName.substring(1, closeBrace);
            if (closeBrace == expandedName.length()) {
                throw new IllegalArgumentException("Missing local part in parameter name");
            }
            localName = expandedName.substring(closeBrace+1);
        } else {
            namespace = "";
            localName = expandedName;
        }

        return namePool.allocate("", namespace, localName);
    }

    /**
    * Reset the parameters to a null list.
    */

    public void clearParameters() {
        parameters = null;
    }

    /**
    * Get a parameter to the transformation
    */

    public Object getParameter(String expandedName) {
        if (parameters==null) return null;
        int f = getFingerprintForExpandedName(expandedName);
        return parameters.get(f);
    }

    /**
    * Set an object that will be used to resolve URIs used in
    * document(), etc.
    * @param resolver An object that implements the URIResolver interface,
    * or null.
    */

    public void setURIResolver(URIResolver resolver) {
        userURIResolver = resolver;
    }

}   // end of outer class Controller

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

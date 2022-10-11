package com.icl.saxon;

import com.icl.saxon.om.Builder;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.trace.TraceListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.helpers.ParserAdapter;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;


/**
 * A TransformerFactoryImpl instance can be used to create Transformer and Template
 * objects.
 *
 * <p>The system property that determines which Factory implementation
 * to create is named "javax.xml.transform.TransformerFactory". This
 * property names a concrete subclass of the TransformerFactory abstract
 * class. If the property is not defined, a platform default is be used.</p>
 *
 * <p>This implementation class implements the abstract methods on both the
 * javax.xml.transform.TransformerFactory and javax.xml.transform.sax.SAXTransformerFactory
 * classes.
 */

public class TransformerFactoryImpl extends SAXTransformerFactory {

	private URIResolver resolver = new StandardURIResolver(this);
	private ErrorListener listener = new StandardErrorListener();
	private int treeModel = Builder.TINY_TREE;
	private boolean lineNumbering = false;
	private TraceListener traceListener = null;
	private int recoveryPolicy = Controller.RECOVER_WITH_WARNINGS;
	private String messageEmitterClass = "com.icl.saxon.output.MessageEmitter";
	private String sourceParserClass;
	private String styleParserClass;
	private boolean timing = false;
	private boolean allowExternalFunctions = true;

    /**
     * Default constructor.
     */
    public TransformerFactoryImpl() {}

    /**
     * Process the Source into a Transformer object.  Care must
     * be given not to use this object in multiple threads running concurrently.
     * Different TransformerFactories can be used concurrently by different
     * threads.
     *
     * @param source An object that holds a URI, input stream, etc.
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     *
     * @exception TransformerConfigurationException May throw this during the parse
     *            when it is constructing the Templates object and fails.
     */

    public Transformer newTransformer(Source source)
        	throws TransformerConfigurationException {
        Templates templates = newTemplates(source);
        Transformer trans = templates.newTransformer();
        //trans.setURIResolver(resolver);
        //trans.setErrorListener(listener);
        return trans;
    }

    /**
     * Create a new Transformer object that performs a copy
     * of the source to the result.
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     *
     * @exception TransformerConfigurationException May throw this during
     *            the parse when it is constructing the
     *            Templates object and fails.
     */

    public Transformer newTransformer()
        			throws TransformerConfigurationException {

        return new IdentityTransformer(this);
	}


    /**
     * Process the Source into a Templates object, which is a
     * a compiled representation of the source. This Templates object
     * may then be used concurrently across multiple threads.  Creating
     * a Templates object allows the TransformerFactory to do detailed
     * performance optimization of transformation instructions, without
     * penalizing runtime transformation.
     *
     * @param source An object that holds a URL, input stream, etc.
     *
     * @return A Templates object capable of being used for transformation purposes,
     * never null.
     *
     * @exception TransformerConfigurationException May throw this during the parse when it
     *            is constructing the Templates object and fails.
     */

    public Templates newTemplates(Source source)
        throws TransformerConfigurationException {

        PreparedStyleSheet pss = new PreparedStyleSheet(this);
        SAXSource saxSource = getSAXSource(source, true);
        pss.prepare(saxSource);
        return pss;
	}


    /**
    * Convert a supplied Source to a SAXSource
    * @param source The supplied input source
    * @param isStyleSheet true if the source is a stylesheet
    * @return a SAXSource
    */

    public SAXSource getSAXSource(Source source, boolean isStyleSheet) {
        if (source instanceof SAXSource) {
            if (((SAXSource)source).getXMLReader()==null) {
                SAXSource ss = new SAXSource();
                ss.setInputSource(((SAXSource)source).getInputSource());
                ss.setSystemId(source.getSystemId());
                ss.setXMLReader((isStyleSheet ? getStyleParser() : getSourceParser()));
                return ss;
            } else {
                return (SAXSource)source;
            }
       // } else if (source instanceof NodeInfo) {
       //     DocumentInfo docInfo = ((NodeInfo)source).getDocumentRoot();
       //     TreeDriver driver = new TreeDriver();
       //     driver.setDocumentInfo(docInfo);
       //     InputSource is = new InputSource("dummy");
       //     is.setSystemId(source.getSystemId());
       //     driver.setSystemId(source.getSystemId());
       //     return new SAXSource(driver, is);

        } else if (source instanceof DOMSource) {
            InputSource is = new InputSource("dummy");
            Node startNode = ((DOMSource)source).getNode();
            Document doc;
            if (startNode instanceof Document) {
                doc = (Document)startNode;
            } else {
                doc = startNode.getOwnerDocument();
            }
            DOMDriver driver;
            if (startNode instanceof NodeInfo) {
                driver = new TreeDriver();
            } else {
                driver = new DOMDriver();
            }
            driver.setStartNode(doc);
            is.setSystemId(source.getSystemId());
            driver.setSystemId(source.getSystemId());
            return new SAXSource(driver, is);
        } else if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource)source;

            // The current (17 April 2001) version of JAXP 1.1 StreamSource
            // has a bug: if constructed with a File object representing
            // the file "/usr/my.xml", it produces the invalid system id
            // "file:////usr/my.xml", instead of "file:///usr/my.xml". The
            // following code gets round this:
            //[code now deleted, JAXP updated - 14 Nov 2001]

            String url = source.getSystemId();
            //if (url!=null && url.startsWith("file:////")) {
            //    url = "file:///" + url.substring(9);
            //}

            InputSource is = new InputSource(url);
            is.setCharacterStream(ss.getReader());
            is.setByteStream(ss.getInputStream());
            return new SAXSource(
                             (isStyleSheet ? getStyleParser() : getSourceParser()),
                             is);
        } else {
            throw new IllegalArgumentException("Unknown type of source");
        }
    }


    /**
     * Get the stylesheet specification(s) associated
     * via the xml-stylesheet processing instruction (see
     * http://www.w3.org/TR/xml-stylesheet/) with the document
     * document specified in the source parameter, and that match
     * the given criteria.  Note that it is possible to return several
     * stylesheets, in which case they are applied as if they were
     * a list of imports or cascades.
     *
     * @param source The XML source document.
     * @param media The media attribute to be matched.  May be null, in which
     *              case the prefered templates will be used (i.e. alternate = no).
     * @param title The value of the title attribute to match.  May be null.
     * @param charset The value of the charset attribute to match.  May be null.
     *
     * @return A Source object suitable for passing to the TransformerFactory.
     *
     * @throws TransformerConfigurationException
     */

    public Source getAssociatedStylesheet(
        Source source, String media, String title, String charset)
            throws TransformerConfigurationException {


        PIGrabber grabber = new PIGrabber();
        grabber.setCriteria(media, title, charset);
        grabber.setBaseURI(source.getSystemId());
        grabber.setURIResolver(resolver);

        SAXSource saxSource = getSAXSource(source, false);
        XMLReader parser = saxSource.getXMLReader();

        parser.setContentHandler(grabber);
        try {
            parser.parse(saxSource.getInputSource());   // this parse will be aborted when the first start tag is found
        } catch (SAXException err) {
            if (err.getMessage().equals("#start#")) {
            	// do nothing
            } else {
                // TODO: the error handling here is not very nice...
            	System.err.println("Failed while looking for xml-stylesheet PI");
            	System.err.println(err.getMessage());
            	if (err.getException()!=null) {
            		err.getException().printStackTrace();
            	}
            	if (err instanceof SAXParseException) {
            		SAXParseException pe = (SAXParseException)err;
            		System.err.println("At line " + pe.getLineNumber() + " in " + pe.getSystemId());
            	}
                throw new TransformerConfigurationException(err);
            }
        } catch (java.io.IOException ierr) {
            System.err.println(ierr.getMessage());
        	throw new TransformerConfigurationException(
        	    "XML parsing failure while looking for <?xml-stylesheet?>");
        }
        try {
            SAXSource[] sources = grabber.getAssociatedStylesheets();
            if (sources==null) {
                throw new TransformerConfigurationException(
                    "No matching <?xml-stylesheet?> processing instruction found");
            }
            return compositeStylesheet(sources);
        } catch (TransformerException err) {
            if (err instanceof TransformerConfigurationException) {
                throw (TransformerConfigurationException)err;
            } else {
                throw new TransformerConfigurationException(err);
            }
        }
    }

    /**
    * Process a series of stylesheet inputs, treating them in import or cascade
    * order.  This is mainly for support of the getAssociatedStylesheets
    * method, but may be useful for other purposes.
    *
    * @param sources An array of SAX InputSource objects.
    * @return A Source object representing a composite stylesheet.
    */

    public Source compositeStylesheet(SAXSource[] sources)
    					throws TransformerConfigurationException {

        // TODO: this will fail if any of the SAXSources uses a non-standard parser.

        if (sources.length == 1) {
            return sources[0];
        } else if (sources.length == 0) {
            throw new TransformerConfigurationException(
                            "No stylesheets were supplied");
        }

        // create a new top-level stylesheet that imports all the others

        StringBuffer sb = new StringBuffer();
        sb.append("<xsl:stylesheet version='1.0' ");
        sb.append(" xmlns:xsl='" + Namespace.XSLT + "'>");
        for (int i=0; i<sources.length; i++) {
            sb.append("<xsl:import href='" + sources[i].getInputSource().getSystemId() + "'/>");
        }
        sb.append("</xsl:stylesheet>");
        InputSource composite = new InputSource();
        composite.setCharacterStream(new StringReader(sb.toString()));
        return new SAXSource(getSourceParser(), composite);
    }

    /**
     * Set an object that is used by default during the transformation
     * to resolve URIs used in xsl:import, or xsl:include.
     *
     * @param resolver An object that implements the URIResolver interface,
     * or null.
     */

    public void setURIResolver(URIResolver resolver) {
    	this.resolver = resolver;
    }

    /**
     * Get the object that is used by default during the transformation
     * to resolve URIs used in document(), xsl:import, or xsl:include.
     *
     * @return The URIResolver that was set with setURIResolver.
     */

    public URIResolver getURIResolver() {
    	return resolver;
    }

    //======= CONFIGURATION METHODS =======

    /**
     * <p>Set a feature for this <code>TransformerFactory</code> and <code>Transformer</code>s
     * or <code>Template</code>s created by this factory.</p>
     * <p/>
     * <p/>
     * Feature names are fully qualified {@link java.net.URI}s.
     * Implementations may define their own features.
     * An {@link javax.xml.transform.TransformerConfigurationException} is thrown if this <code>TransformerFactory</code> or the
     * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature.
     * It is possible for an <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     * <p/>
     * <p>All implementations are required to support the {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} feature.
     * When the feature is:</p>
     * <ul>
     * <li>
     * <code>true</code>: the implementation will limit XML processing to conform to implementation limits
     * and behave in a secure fashion as defined by the implementation.
     * Examples include resolving user defined style sheets and functions.
     * If XML processing is limited for security reasons, it will be reported via a call to the registered
     * {@link javax.xml.transform.ErrorListener#fatalError(javax.xml.transform.TransformerException exception)}.
     * See {@link  #setErrorListener(javax.xml.transform.ErrorListener listener)}.
     * </li>
     * <li>
     * <code>false</code>: the implementation will processing XML according to the XML specifications without
     * regard to possible implementation limits.
     * </li>
     * </ul>
     *
     * <p><i>The Saxon implementation does not support the secure processing feature.</i></p>
     *
     * @param name  Feature name.
     * @param value Is feature state <code>true</code> or <code>false</code>.
     * @throws javax.xml.transform.TransformerConfigurationException
     *                              if this <code>TransformerFactory</code>
     *                              or the <code>Transformer</code>s or <code>Template</code>s it creates cannot support this feature.
     * @throws NullPointerException If the <code>name</code> parameter is null.
     */
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing") && !value) {
            return;
        }
        throw new TransformerConfigurationException("Unsupported feature: " + name);
    }

    /**
     * Look up the value of a feature.
     *
     * <p>The feature name is any absolute URI.</p>
     * @param name The feature name, which is an absolute URI.
     * @return The current state of the feature (true or false).
     */

    public boolean getFeature(String name) {
    	if (name.equals(SAXSource.FEATURE)) return true;
    	if (name.equals(SAXResult.FEATURE)) return true;
    	if (name.equals(DOMSource.FEATURE)) return true;
    	if (name.equals(DOMResult.FEATURE)) return true;
    	if (name.equals(StreamSource.FEATURE)) return true;
    	if (name.equals(StreamResult.FEATURE)) return true;
        if (name.equals(SAXTransformerFactory.FEATURE)) return true;
        if (name.equals(SAXTransformerFactory.FEATURE_XMLFILTER)) return true;
    	throw new IllegalArgumentException("Unknown feature " + name);
    }

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.  An attribute in this context is defined to
     * be an option that the implementation provides.
     *
     * @param name The name of the attribute. This must be one of the constants
     * defined in class FeatureKeys.
     * @param value The value of the attribute.
     * @throws IllegalArgumentException thrown if Saxon
     * doesn't recognize the attribute.
     */

    public void setAttribute(String name, Object value)
        							throws IllegalArgumentException {
        if (name.equals(FeatureKeys.TREE_MODEL)) {
        	if (!(value instanceof Integer)) {
        		throw new IllegalArgumentException("Tree model must be an Integer");
        	}
        	treeModel = ((Integer)value).intValue();

        } else if (name.equals(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS)) {
        	if (!(value instanceof Boolean)) {
        		throw new IllegalArgumentException("allow-external-functions must be a boolean");
        	}
        	allowExternalFunctions = ((Boolean)value).booleanValue();

        } else if (name.equals(FeatureKeys.TIMING)) {
        	if (!(value instanceof Boolean)) {
        		throw new IllegalArgumentException("Timing must be a boolean");
        	}
        	timing = ((Boolean)value).booleanValue();

        } else if (name.equals(FeatureKeys.TRACE_LISTENER)) {
        	if (!(value instanceof TraceListener)) {
        		throw new IllegalArgumentException("Trace listener is of wrong class");
        	}
        	traceListener = (TraceListener)value;

        } else if (name.equals(FeatureKeys.LINE_NUMBERING)) {
        	if (!(value instanceof Boolean)) {
        		throw new IllegalArgumentException("Line Numbering value must be Boolean");
        	}
        	lineNumbering = ((Boolean)value).booleanValue();

        } else if (name.equals(FeatureKeys.RECOVERY_POLICY)) {
        	if (!(value instanceof Integer)) {
        		throw new IllegalArgumentException("Recovery Policy value must be Integer");
        	}
        	recoveryPolicy = ((Integer)value).intValue();

        } else if (name.equals(FeatureKeys.MESSAGE_EMITTER_CLASS)) {
        	if (!(value instanceof String)) {
        		throw new IllegalArgumentException("Message Emitter class must be a String");
        	}
        	messageEmitterClass = (String)value;

        } else if (name.equals(FeatureKeys.SOURCE_PARSER_CLASS)) {
        	if (!(value instanceof String)) {
        		throw new IllegalArgumentException("Source Parser class must be a String");
        	}
        	sourceParserClass = (String)value;

        } else if (name.equals(FeatureKeys.STYLE_PARSER_CLASS)) {
        	if (!(value instanceof String)) {
        		throw new IllegalArgumentException("Style Parser class must be a String");
        	}
        	styleParserClass = (String)value;

        } else {
	        throw new IllegalArgumentException("Unknown attribute " + name);
	    }
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     * @throws IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the attribute.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException{
        if (name.equals(FeatureKeys.TREE_MODEL)) {
        	return new Integer(treeModel);

        } else if (name.equals(FeatureKeys.TIMING)) {
        	return new Boolean(timing);

        } else if (name.equals(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS)) {
        	return new Boolean(allowExternalFunctions);

        } else if (name.equals(FeatureKeys.TRACE_LISTENER)) {
        	return traceListener;

    	} else if (name.equals(FeatureKeys.LINE_NUMBERING)) {
    		return new Boolean(lineNumbering);

    	} else if (name.equals(FeatureKeys.RECOVERY_POLICY)) {
    		return new Integer(recoveryPolicy);

        } else if (name.equals(FeatureKeys.MESSAGE_EMITTER_CLASS)) {
        	return messageEmitterClass;

        } else if (name.equals(FeatureKeys.SOURCE_PARSER_CLASS)) {
        	return sourceParserClass;

        } else if (name.equals(FeatureKeys.STYLE_PARSER_CLASS)) {
        	return styleParserClass;

        } else {
	        throw new IllegalArgumentException("Unknown attribute " + name);
	    }
    }

    /**
     * Set the error event listener for the TransformerFactory, which
     * is used for the processing of transformation instructions,
     * and not for the transformation itself.
     *
     * @param listener The new error listener.
     * @throws IllegalArgumentException if listener is null.
     */

    public void setErrorListener(ErrorListener listener)
        	throws IllegalArgumentException {
        this.listener = listener;
    }

    /**
     * Get the error event handler for the TransformerFactory.
     *
     * @return The current error handler, which should never be null.
     */
    public ErrorListener getErrorListener() {
    	return listener;
    }

    /**
    * Get the parser for source documents
    */

    public XMLReader getSourceParser() throws TransformerFactoryConfigurationError {
        if (sourceParserClass!=null) {
            return makeParser(sourceParserClass);
        } else {
            try {
                return SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            } catch (Exception err) {
                throw new TransformerFactoryConfigurationError(err);
            }
        }
    }

    /**
    * Get the parser for stylesheet documents
    */

    public XMLReader getStyleParser() throws TransformerFactoryConfigurationError {
        if (styleParserClass!=null) {
            return makeParser(styleParserClass);
        } else {
            try {
                return SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            } catch (Exception err) {
                throw new TransformerFactoryConfigurationError(err);
            }
        }
    }

  /**
    * Create a new SAX XMLReader object using the class name provided.<br>
    *
    * The named class must exist and must implement the
    * org.xml.sax.XMLReader or Parser interface.<br>
    *
    * This method returns an instance of the parser named.
    *
    * @param className A string containing the name of the
    *   SAX parser class, for example "com.microstar.sax.LarkDriver"
    * @return an instance of the Parser class named, or null if it is not
    * loadable or is not a Parser.
    *
    */
    public static XMLReader makeParser (String className)
    throws TransformerFactoryConfigurationError
    {
        Object obj;
        try {
            obj = Loader.getInstance(className);
        } catch (TransformerException err) {
            throw new TransformerFactoryConfigurationError(err);
        }
        if (obj instanceof XMLReader) {
            return (XMLReader)obj;
        }
        if (obj instanceof Parser) {
            return new ParserAdapter((Parser)obj);
        }
        throw new TransformerFactoryConfigurationError("Class " + className +
                                 " is neither a SAX1 Parser nor a SAX2 XMLReader");
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Methods defined in class javax.xml.transform.sax.SAXTransformerFactory
    ///////////////////////////////////////////////////////////////////////////////

     /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the transformation
     * instructions specified by the argument.
     *
     * @param src The Source of the transformation instructions.
     *
     * @return TransformerHandler ready to transform SAX events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler can not be created.
     */

    public TransformerHandler newTransformerHandler(Source src)
    throws TransformerConfigurationException {
        Templates tmpl = newTemplates(src);
        return newTransformerHandler(tmpl);
    }

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the Templates argument.
     *
     * @param templates The compiled transformation instructions.
     *
     * @return TransformerHandler ready to transform SAX events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler can not be created.
     */

    public TransformerHandler newTransformerHandler(Templates templates)
    throws TransformerConfigurationException {
        if (!(templates instanceof PreparedStyleSheet)) {
            throw new TransformerConfigurationException("Templates object was not created by Saxon");
        }
        Controller controller = (Controller)templates.newTransformer();
        if (controller.usesPreviewMode()) {
            throw new TransformerConfigurationException("Preview mode is not available with a TransformerHandler");
        }
        TransformerHandlerImpl handler = new TransformerHandlerImpl(controller);
        return handler;
    }

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result. The transformation
     * is defined as an identity (or copy) transformation, for example
     * to copy a series of SAX parse events into a DOM tree.
     *
     * @return A non-null reference to a TransformerHandler, that may
     * be used as a ContentHandler for SAX parse events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler cannot be created.
     */

    public TransformerHandler newTransformerHandler()
    throws TransformerConfigurationException {
        Controller controller = new IdentityTransformer(this);
        return new IdentityTransformerHandler(controller);
    }

    /**
     * Get a TemplatesHandler object that can process SAX
     * ContentHandler events into a Templates object.
     *
     * @return A non-null reference to a TransformerHandler, that may
     * be used as a ContentHandler for SAX parse events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TemplatesHandler cannot be created.
     */

    public TemplatesHandler newTemplatesHandler()
    throws TransformerConfigurationException {
        return new TemplatesHandlerImpl(this);
    }

    /**
     * Create an XMLFilter that uses the given Source as the
     * transformation instructions.
     *
     * @param src The Source of the transformation instructions.
     *
     * @return An XMLFilter object, or null if this feature is not supported.
     *
     * @throws TransformerConfigurationException If for some reason the
     * XMLFilter cannot be created.
     */

    public XMLFilter newXMLFilter(Source src)
    throws TransformerConfigurationException {
        Templates tmpl = newTemplates(src);
        return newXMLFilter(tmpl);
    }

    /**
     * Create an XMLFilter, based on the Templates argument..
     *
     * @param templates The compiled transformation instructions.
     *
     * @return An XMLFilter object, or null if this feature is not supported.
     *
     * @throws TransformerConfigurationException If for some reason the
     * XMLFilter cannot be created.
     */

    public XMLFilter newXMLFilter(Templates templates)
    throws TransformerConfigurationException {
        if (!(templates instanceof PreparedStyleSheet)) {
            throw new TransformerConfigurationException("Supplied Templates object was not created using Saxon");
        }
        Controller controller = (Controller)templates.newTransformer();
        return new Filter(controller);
    }


}

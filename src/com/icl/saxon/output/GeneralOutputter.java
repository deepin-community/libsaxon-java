package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.charcode.*;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.Builder;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.tree.TreeBuilder;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tinytree.TinyBuilder;
//import com.icl.saxon.tinytree.TinyDocumentImpl;
import org.xml.sax.Attributes;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Result;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import java.io.*;
import java.lang.reflect.Constructor;

/**
  * This class allows output to be generated. It channels output requests to an
  * Emitter which does the actual writing.
  *
  * @author Michael H. Kay
  */



public class GeneralOutputter extends Outputter {

	private NamePool namePool;
    private Properties outputProperties;
    private Writer writer;
    private OutputStream outputStream;
    private boolean closeAfterUse = false;

    private int pendingStartTag = -1;
    private AttributeCollection pendingAttList;

    private int[] pendingNSList = new int[20];
    private int pendingNSListSize = 0;

    private boolean suppressAttributes = false;

	public GeneralOutputter(NamePool pool) {
		namePool = pool;
		pendingAttList = new AttributeCollection(namePool, 10);
	}

    /**
    * Initialise the outputter for a new output destination, supplying
    * the output format details. <BR>
    * @param outputProperties Details of the new output format
    * @param result Details of the new output destination
    */

    public void setOutputDestination(Properties props, Result result)
    throws TransformerException {
        setOutputProperties(props);

        Emitter emitter = makeEmitter(props, result);
        emitter.setNamePool(namePool);
        emitter.setOutputProperties(props);

        setEmitter(emitter);

        open();
    }

    /**
    * The following atrocious code is borrowed from Xalan, where it is commented simply:
    * // yuck.
    * The backslash variants added by MHK.
    */

    public static String urlToFileName(String base) {
        try {
            // try JDK 1.4+ approach: return new File(new URI(base)).toString()
            Class uriClass = Loader.getClass("java.net.URI");
            Class[] uriArgTypes = {String.class};
            Object[] uriArgValues = {base};
            Constructor constructor = uriClass.getConstructor(uriArgTypes);
            Object uriInstance = constructor.newInstance(uriArgValues);
            Class[] fileArgTypes = {uriClass};
            Object[] fileArgValues = {uriInstance};
            constructor = File.class.getConstructor(fileArgTypes);
            File file = (File)constructor.newInstance(fileArgValues);
            return file.toString();

        } catch (Exception err) {
            // continue
        }
        if(null != base) {
            if (base.startsWith("file:////")) {
                base = base.substring(7);
            } else if (base.startsWith("file:///")) {
                base = base.substring(6);
            } else if (base.startsWith("file://")) {
                base = base.substring(5); // absolute?
            } else if (base.startsWith("file:/")) {
                base = base.substring(5);
            } else if (base.startsWith("file:")) {
                base = base.substring(4);
            } if (base.startsWith("file:\\\\\\\\")) {
                base = base.substring(7);
            } else if (base.startsWith("file:\\\\\\")) {
                base = base.substring(6);
            } else if (base.startsWith("file:\\\\")) {
                base = base.substring(5);
            } else if (base.startsWith("file:\\")) {
                base = base.substring(5);
            }
        }
        if (File.separatorChar != '/') {
            base = base.replace('/', File.separatorChar);
        }
        return base;
    }

    /**
    * Create a new FileOutputStream, given a filename and a baseURI
    */

    public static FileOutputStream makeFileOutputStream(  String baseURI,
                                                    String fileName,
                                                    boolean mkdirs)
    throws TransformerException
    {
        try {
            File file = new File(fileName);

            if (!file.isAbsolute()) {
                String base = urlToFileName(baseURI);
                if(null != base) {
                    File baseFile = new File(base);
                    file = new File(baseFile.getParent(), fileName);
                }
            }

            if (mkdirs) {
                String dirStr = file.getParent();
                if((null != dirStr) && (dirStr.length() > 0)) {
                    File dir = new File(dirStr);
                    dir.mkdirs();
                }
            }

            FileOutputStream ostream = new FileOutputStream(file);
            return ostream;
        } catch (Exception err) {
            throw new TransformerException("Failed to create output file", err);
        }
  }


    /**
    * Make an emitter appropriate for a given set of output properties and
    * output destination. Also updates the output properties
    */

    public Emitter makeEmitter(Properties props, Result result)
    throws TransformerException {

        Emitter emitter;
        if (result instanceof DOMResult) {
            Node resultNode = ((DOMResult)result).getNode();
            if (resultNode!=null) {
                if (resultNode instanceof NodeInfo) {
                    // Writing to a SAXON tree is handled specially
                    if (resultNode instanceof DocumentInfo) {
                        DocumentInfo doc = (DocumentInfo)resultNode;
                        if (resultNode.getFirstChild() != null) {
                            throw new TransformerException("Target document must be empty");
                        } else {
                            Builder builder;
                            if (doc instanceof DocumentImpl) {
                                builder = new TreeBuilder();
                            } else {
                                builder = new TinyBuilder();
                            }
                            builder.setRootNode(doc);
                            builder.setSystemId(result.getSystemId());
                            builder.setNamePool(namePool);
                            emitter = builder;
                        }
                    } else {
                        throw new TransformerException("Cannot add to an existing Saxon document");
                    }
                } else {
                    // Non-Saxon DOM
                    emitter = new DOMEmitter();
                    ((DOMEmitter)emitter).setNode(resultNode);
                }
            } else {
                // no result node supplied; we must create our own
                TinyBuilder builder = new TinyBuilder();
                builder.setSystemId(result.getSystemId());
                builder.setNamePool(namePool);
                builder.createDocument();
                Document resultDoc = (Document)builder.getCurrentDocument();
                ((DOMResult)result).setNode(resultDoc);
                emitter = builder;
            }
        } else if (result instanceof SAXResult) {
            SAXResult sr = (SAXResult)result;
        	ContentHandlerProxy proxy = new ContentHandlerProxy();
        	proxy.setUnderlyingContentHandler(sr.getHandler());
        	if (sr.getLexicalHandler() != null) {
        	    proxy.setLexicalHandler(sr.getLexicalHandler());
        	}
        	emitter = proxy;

        } else if (result instanceof Emitter) {
        	emitter = (Emitter)result;

        } else if (result instanceof StreamResult) {

            String method = props.getProperty(OutputKeys.METHOD);
            if (method==null) {
            	emitter = new UncommittedEmitter();

            } else if (method.equals("html")) {
                emitter = new HTMLEmitter();
                if (!"no".equals(props.getProperty(OutputKeys.INDENT))) {
                    HTMLIndenter in = new HTMLIndenter();
                    in.setUnderlyingEmitter(emitter);
                    emitter=in;
                }

            } else if (method.equals("xml")) {
                emitter = new XMLEmitter();
                if ("yes".equals(props.getProperty(OutputKeys.INDENT))) {
                    XMLIndenter in = new XMLIndenter();
                    in.setUnderlyingEmitter(emitter);
                    emitter=in;
                }
                String cdataElements = props.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS);
                if (cdataElements!=null && cdataElements.length()>0) {
                    CDATAFilter filter = new CDATAFilter();
                    filter.setUnderlyingEmitter(emitter);
                    emitter=filter;
                }
            } else if (method.equals("text")) {
                emitter = new TEXTEmitter();

            } else {
                // TODO: externally supplied properties must be validated
                int brace = method.indexOf('}');
                String localName = method.substring(brace+1);
                int colon = localName.indexOf(':');
                localName = localName.substring(colon+1);

                if (localName.equals("fop")) {
                    // avoid an explicit external reference to avoid build problems
                    // when FOP is not present on the class path
                    emitter = Emitter.makeEmitter("com.icl.saxon.fop.FOPEmitter");
                } else if (localName.equals("xhtml")) {
                    emitter = new XHTMLEmitter();
                    if ("yes".equals(props.getProperty(OutputKeys.INDENT))) {
                        HTMLIndenter in = new HTMLIndenter();
                        in.setUnderlyingEmitter(emitter);
                        emitter=in;
                    }
                    String cdataElements = props.getProperty(OutputKeys.CDATA_SECTION_ELEMENTS);
                    if (cdataElements!=null && cdataElements.length()>0) {
                        CDATAFilter filter = new CDATAFilter();
                        filter.setUnderlyingEmitter(emitter);
                        emitter=filter;
                    }
                } else {
                    emitter = Emitter.makeEmitter(localName);
                }
            }
            if (emitter.usesWriter()) {
                writer = getStreamWriter((StreamResult)result, props);
                emitter.setWriter(writer);
            } else {
                outputStream = getOutputStream((StreamResult)result, props);
                emitter.setOutputStream(outputStream);
            }

        } else {
            throw new IllegalArgumentException("Unknown type of Result: " + result.getClass());
        }

		// add a filter to remove duplicate namespaces
		NamespaceEmitter ne = new NamespaceEmitter();
		ne.setUnderlyingEmitter(emitter);
		emitter = ne;
		return emitter;
    }

    /**
    * Get a Writer corresponding to the requested Result destination
    */

    private Writer getStreamWriter(StreamResult result, Properties props)
    throws TransformerException {

        closeAfterUse = false;
        Writer writer = result.getWriter();
        if (writer==null) {
            OutputStream outputStream = ((StreamResult)result).getOutputStream();
            if (outputStream == null) {
                String systemId = ((StreamResult)result).getSystemId();
                if (systemId == null) {
                    outputStream = System.out;
                } else {
                    outputStream = makeFileOutputStream("", urlToFileName(systemId), true);
                    closeAfterUse = true;
                }
            }

            CharacterSet charSet = CharacterSetFactory.getCharacterSet(props);

            String encoding = props.getProperty(OutputKeys.ENCODING);
            if (encoding==null) encoding = "UTF8";
            if (encoding.equalsIgnoreCase("utf-8")) encoding = "UTF8";
                 // needed for Microsoft Java VM

	        if (charSet instanceof PluggableCharacterSet) {
	        	encoding = ((PluggableCharacterSet)charSet).getEncodingName();
	        }

            while (true) {
                try {
                    writer = new BufferedWriter(
                                    new OutputStreamWriter(
                                        outputStream, encoding));
                    break;
                } catch (Exception err) {
                    if (encoding.equalsIgnoreCase("UTF8")) {
                        throw new TransformerException("Failed to create a UTF8 output writer");
                    }
                    System.err.println("Encoding " + encoding + " is not supported: using UTF8");
                    encoding = "UTF8";
                    charSet = new UnicodeCharacterSet();
                    props.put(OutputKeys.ENCODING, "utf-8");
                }
            }
        } else {
            // a writer was supplied by the user
            // if the writer uses a known encoding, change the encoding in the XML declaration
            // to match.

            if (writer instanceof OutputStreamWriter) {
                String enc = ((OutputStreamWriter)writer).getEncoding();
                //System.err.println("User-supplied writer, encoding=" + enc);
                props.put(OutputKeys.ENCODING, enc);
            }
        }
        return writer;
    }

    /**
    * Get an OutputStream corresponding to the requested Result destination
    */

    private OutputStream getOutputStream(StreamResult result, Properties props)
    throws TransformerException {

        closeAfterUse = false;

        OutputStream outputStream = ((StreamResult)result).getOutputStream();
        if (outputStream == null) {
            String systemId = ((StreamResult)result).getSystemId();
            if (systemId == null) {
                outputStream = System.out;
            } else {
                outputStream = makeFileOutputStream("", urlToFileName(systemId), true);
                closeAfterUse = true;
            }
        }

        if (outputStream==null) {
            throw new TransformerException("This output method requires a binary output destination");
        }

        return outputStream;
    }


    /**
    * Set the emitter that will deal with this output
    */

    private void setEmitter(Emitter handler) {
        this.emitter = handler;
    }

    public void reset() throws TransformerException {
        if (pendingStartTag != -1) flushStartTag();
    }

    private void setOutputProperties(Properties details) throws TransformerException {
        outputProperties = details;
    }

    public Properties getOutputProperties() {
        return outputProperties;
    }

    /**
    * Produce literal output. This is written as is, without any escaping.
    * The method is provided for Java applications that wish to output literal HTML text.
    * It is not used by the XSL system, which always writes using specific methods such as
    * writeStartTag().
    */

    char[] charbuffer = new char[1024];
    public void write(String s) throws TransformerException {
        if (pendingStartTag != -1) flushStartTag();
        emitter.setEscaping(false);
        int len = s.length();
        if (len>charbuffer.length) {
            charbuffer = new char[len];
        }
        s.getChars(0, len, charbuffer, 0);
        emitter.characters(charbuffer, 0, len);
        emitter.setEscaping(true);
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param s The String to be output
    * @exception TransformerException for any failure
    */

    public void writeContent(String s) throws TransformerException {
        if (s==null) return;
        int len = s.length();
        if (len>charbuffer.length) {
            charbuffer = new char[len];
        }
        s.getChars(0, len, charbuffer, 0);
        writeContent(charbuffer, 0, len);
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars Character array to be output
    * @param start start position of characters to be output
    * @param length number of characters to be output
    * @exception TransformerException for any failure
    */

    public void writeContent(char[] chars, int start, int length) throws TransformerException {
        //System.err.println("WriteContent " + this + ":" + new String(chars, start, length) );
        if (length==0) return;
        if (pendingStartTag != -1) {
            flushStartTag();
        }
        emitter.characters(chars, start, length);
    }

    /**
    * Produce text content output. <BR>
    * Special characters are escaped using XML/HTML conventions if the output format
    * requires it.
    * @param chars StringBuffer containing to be output
    * @param start start position of characters to be output
    * @param len number of characters to be output
    * @exception TransformerException for any failure
    */

    public void writeContent(StringBuffer chars, int start, int len) throws TransformerException {
        //System.err.println("WriteContent " + this + ":" + chars.substring(start, start+length) );
        if (len==0) return;
        if (pendingStartTag != -1) {
            flushStartTag();
        }
        if (len>charbuffer.length) {
            charbuffer = new char[len];
        }
        chars.getChars(start, start+len, charbuffer, 0);
        emitter.characters(charbuffer, 0, len);
    }

    /**
    * Output an element start tag. <br>
    * The actual output of the tag is deferred until all attributes have been output
    * using writeAttribute().
    * @param nameCode The element name code
    */

    public void writeStartTag(int nameCode) throws TransformerException {
        // System.err.println("Write start tag " + this + " : " + nameCode + " to emitter " + emitter);

        if (nameCode==-1) {
            suppressAttributes = true;
            return;
        } else {
            suppressAttributes = false;
        }

        if (pendingStartTag != -1) flushStartTag();
        pendingAttList.clear();
        pendingNSListSize = 0;
        pendingStartTag = nameCode;

        // make sure there is a namespace declaration for this start tag
        //writeNamespaceDeclaration(namePool.allocateNamespaceCode(nameCode));
        // ***** now done by the NamespaceEmitter
    }

	/**
	* Check that the prefix for an attribute is acceptable, returning a substitute
	* prefix if not. The prefix is acceptable unless a namespace declaration has been
	* written that assignes this prefix to a different namespace URI. This method
	* also checks that the attribute namespace has been declared, and declares it
	* if not.
	*/

	public int checkAttributePrefix(int nameCode) throws TransformerException {
		int nscode = namePool.allocateNamespaceCode(nameCode);
        for (int i=0; i<pendingNSListSize; i++) {
        	if ((nscode>>16) == (pendingNSList[i]>>16)) {
        		// same prefix
        		if ((nscode & 0xffff) == (pendingNSList[i] & 0xffff)) {
        			// same URI
        			return nameCode;	// all is well
        		} else {
        			String prefix = getSubstitutePrefix(nscode);
        			int newCode = namePool.allocate(
        								prefix,
        								namePool.getURI(nameCode),
        								namePool.getLocalName(nameCode));
        			writeNamespaceDeclaration(namePool.allocateNamespaceCode(newCode));
        			return newCode;
        		}
        	}
        }
        // no declaration of this prefix: declare it now
        writeNamespaceDeclaration(nscode);
        return nameCode;
    }

    /**
    * Output a namespace declaration. <br>
    * This is added to a list of pending namespaces for the current start tag.
    * If there is already another declaration of the same prefix, this one is
    * ignored.
    * Note that unlike SAX2 startPrefixMapping(), this call is made AFTER writing the start tag.
    * @param nscode The namespace code
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeNamespaceDeclaration(int nscode)
    throws TransformerException {

        if (suppressAttributes) return;

        // System.err.println("Write namespace prefix=" + (nscode>>16) + " uri=" + (nscode&0xffff));
        if (pendingStartTag==-1) {
            throw new TransformerException("Cannot write a namespace declaration when there is no open start tag");
        }

        // elimination of namespaces already present on an outer element of the
        // result tree is now done by the NamespaceEmitter.

        // Ignore declarations whose prefix is duplicated for this element.

        for (int i=0; i<pendingNSListSize; i++) {
        	if ((nscode>>16) == (pendingNSList[i]>>16)) {
        		// same prefix, do a quick exit
        		return;
        	}
        }

        // if it's not a duplicate namespace, add it to the list for this start tag

        if (pendingNSListSize+1 > pendingNSList.length) {
            int[] newlist = new int[pendingNSListSize * 2];
            System.arraycopy(pendingNSList, 0, newlist, 0, pendingNSListSize);
            pendingNSList = newlist;
        }
        pendingNSList[pendingNSListSize++] = nscode;
    }

	/**
	* Copy a namespace node to the current element node
	* (Rules defined in XSLT 1.0 errata)
	*/

	public void copyNamespaceNode(int nscode) throws TransformerException {
        if (pendingStartTag==-1) {
            throw new TransformerException("Cannot copy a namespace node when there is no containing element node");
        }
        if (pendingAttList.getLength()>0) {
        	throw new TransformerException("Cannot copy a namespace node to an element after attributes have been added");
        }

        // check for duplicates
        for (int i=0; i<pendingNSListSize; i++) {
        	if ((nscode>>16) == (pendingNSList[i]>>16)) {
        		// same prefix
        		if (nscode==pendingNSList[i]) {		// same URI
        			return;							// ignore the duplicate
        		} else {
					// may need to handle default namespace differently
        			throw new TransformerException("Cannot create two namespace nodes with the same name");
				}
        	}
        }
        writeNamespaceDeclaration(nscode);
    }

    /**
    * It is possible for a single output element to use the same prefix to refer to different
    * namespaces. In this case we have to generate an alternative prefix for uniqueness. The
    * one we generate is based on the uri part of the namespace code itself,
    * which is almost certain to be unique.
    */

    private String getSubstitutePrefix(int nscode) {
    	String prefix = namePool.getPrefixFromNamespaceCode(nscode);
        return prefix + "." + (nscode&0xffff);
    }

    /**
    * Test whether there is an open start tag. This determines whether it is
    * possible to write an attribute node at this point.
    */

    public boolean thereIsAnOpenStartTag() {
        return (pendingStartTag != -1);
    }

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.
    * @param nameCode The name code of the attribute
    * @param value The value of the attribute
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(int nameCode, String value) throws TransformerException {
        writeAttribute(nameCode, value, false);
    }

    /**
    * Output an attribute value. <br>
    * This is added to a list of pending attributes for the current start tag, overwriting
    * any previous attribute with the same name. <br>
    * This method should NOT be used to output namespace declarations.<br>
    * Before calling this, checkAttributePrefix() should be called to ensure the namespace
    * is OK.
    * @param name The name of the attribute
    * @param value The value of the attribute
    * @param noEscape True if it's known there are no special characters in the value. If
    * unsure, set this to false.
    * @throws TransformerException if there is no start tag to write to (created using writeStartTag),
    * or if character content has been written since the start tag was written.
    */

    public void writeAttribute(int nameCode, String value, boolean noEscape) throws TransformerException {

        if (suppressAttributes) return;

        // System.err.println("Write attribute " + nameCode + "=" + value + " (" + noEscape + ") to Outputter " + this);
        if (pendingStartTag==-1) {
            throw new TransformerException("Cannot write an attribute when there is no open start tag");
        }

        pendingAttList.setAttribute(nameCode,
              (noEscape ? "NO-ESC" : "CDATA"),  // dummy attribute type to indicate no special chars
              value);

    }


    /**
    * Output an element end tag.<br>
    * @param nameCode The element name code
    */

    public void writeEndTag(int nameCode) throws TransformerException {
        //System.err.println("Write end tag " + this + " : " + name);
        if (pendingStartTag != -1) {
            flushStartTag();
        }

        // write the end tag
        emitter.endElement(nameCode);
    }

    /**
    * Write a comment
    */

    public void writeComment(String comment) throws TransformerException {
        if (pendingStartTag != -1) flushStartTag();
        emitter.comment(comment.toCharArray(), 0, comment.length());
    }

    /**
    * Write a processing instruction
    */

    public void writePI(String target, String data) throws TransformerException {
        if (pendingStartTag != -1) flushStartTag();
        emitter.processingInstruction(target, data);
    }

    /**
    * Close the output
    */

    public void close() throws TransformerException {
        // System.err.println("Close " + this + " using emitter " + emitter.getClass());
        emitter.endDocument();
        if (closeAfterUse) {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (java.io.IOException err) {
                throw new TransformerException(err);
            }
        }
    }

    /**
    * Flush out a pending start tag
    */

    protected void flushStartTag() throws TransformerException {
        emitter.startElement(pendingStartTag, pendingAttList,
        						 pendingNSList, pendingNSListSize);
        pendingNSListSize = 0;
        pendingStartTag = -1;

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

package com.icl.saxon.om;
import com.icl.saxon.tinytree.*;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerException;

public class DocumentBuilderImpl extends DocumentBuilder {
   
    public boolean isNamespaceAware() {
        return true;
    }
    
    public boolean isValidating() {
        return false;
    }
    
    public Document newDocument() {
        // The returned document will be of little use, because it is immutable.
        // But it can be used in a DOMResult as the result of a transformation
        return new TinyDocumentImpl();
    }
    
    public Document parse(InputSource in) throws SAXException {
        try {
            Builder builder = new TinyBuilder();
            builder.setNamePool(NamePool.getDefaultNamePool());
            return (Document)builder.build(new SAXSource(in));
        } catch (TransformerException err) {
            throw new SAXException(err);
        }
    }
    
    public void setEntityResolver(EntityResolver er) {
        // TODO: not implemented
    }
    
    public void setErrorHandler(ErrorHandler er) {
        // TODO: not implemented
    }

    public DOMImplementation getDOMImplementation() {
        return new TinyDocumentImpl().getImplementation();
    }
}
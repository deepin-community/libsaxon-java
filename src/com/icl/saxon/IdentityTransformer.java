package com.icl.saxon;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.output.GeneralOutputter;
import com.icl.saxon.output.Emitter;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.dom.*;

import java.util.Properties;

class IdentityTransformer extends Controller {
    
    protected IdentityTransformer(TransformerFactoryImpl factory) {
        super(factory);
    }

    /**
    * Perform identify transformation from Source to Result
    */
    
    public void transform(Source source, Result result)
    throws TransformerException {
        SAXSource saxsource = getTransformerFactory().getSAXSource(source, false);
        XMLReader parser = saxsource.getXMLReader();
        
		try {
        	parser.setFeature("http://xml.org/sax/features/namespaces", true);
        	parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotSupportedException err) {    // SAX2 parsers MUST support this feature!
            throw new TransformerException(
                "The SAX2 parser does not recognize a required namespace feature");
    	} catch (SAXNotRecognizedException err) {
            throw new TransformerException(
                "The SAX2 parser does not support a required namespace feature");
    	}
    	        
        if (result instanceof SAXResult) {
            
            // if output is a SAX ContentHandler, we couple this directly 
            // to the SAX Source
            
            ContentHandler ch = ((SAXResult)result).getHandler();
            parser.setContentHandler(ch);
            if (ch instanceof LexicalHandler) {
                // then try to preserve the comments
                try {
            	    parser.setProperty("http://xml.org/sax/properties/lexical-handler", ch);
                } catch (SAXNotSupportedException err) {    // this just means we won't see the comments
        	    } catch (SAXNotRecognizedException err) {
        	    }
        	}
            try {
                parser.parse(saxsource.getInputSource());
            } catch (Exception err) {
                throw new TransformerException(err);
            }                

        } else {
        
            // If output is a DOM, a Stream, or an Emitter, we construct
            // a pipeline consisting of a ContentEmitter (which converts
            // SAX events to Emitter events), then the appropriate Emitter
            // for that kind of result, which we can obtain using the services
            // of the Outputter.
            
            NamePool pool = getNamePool();
            Properties props = getOutputProperties();        
            GeneralOutputter out = new GeneralOutputter(pool);
            out.setOutputDestination(props, result);
            Emitter emitter = out.getEmitter();
            ContentHandler ch = new ContentEmitter();
            ((ContentEmitter)ch).setNamePool(pool);
            ((ContentEmitter)ch).setEmitter(emitter);

            try {
                parser.setContentHandler(ch);
                try {
                    // try to preserve the comments
            	    parser.setProperty("http://xml.org/sax/properties/lexical-handler", ch);
                } catch (SAXNotSupportedException err) {    // this just means we won't see the comments
        	    } catch (SAXNotRecognizedException err) {
                }         	   
        	
                parser.parse(saxsource.getInputSource());
            } catch (Exception err) {
                throw new TransformerException(err);
            }
            
            out.close();
        }
    }
}

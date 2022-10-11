package org.w3c.xsl;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
* Interface to be used with XSLT extension functions written in Java
*/

public interface XSLTContext {
    
    /**
    * Return the context node from the XPath expression context
    */
    
    Node getContextNode();
    
    /**
    * Return the context position from the XPath expression context
    */
    
    int getContextPosition();

    /**
    * Return the context size from the XPath expression context
    */
    
    int getContextSize(); 
    
    /**
    * Return the current node from the XSLT context: the same as
    * the result of calling the current() function from the XPath
    * expression context
    */
    
    Node getCurrentNode();

    /**
    * Return a Document to be used for creating nodes
    */
    
    Document getOwnerDocument();
    
    /**
    * Return an object representing the value of the system property
    * whose expanded name has the specified URI and local part
    */

    Object systemProperty(String namespaceURI, String localName);
    
    /**
    * Return the string-value of the specified Node
    */
    
    String stringValue(Node n);
    
}    
        
           
    
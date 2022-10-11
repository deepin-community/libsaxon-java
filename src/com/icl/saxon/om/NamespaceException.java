package com.icl.saxon.om;

/**
 *  A NamespaceException represents an error condition whereby a QName (e.g. a variable
 * name or template name) uses a namespace prefix that is not declared
 */
 
public class NamespaceException extends Exception {

    String prefix;
    
    public NamespaceException (String prefix) {
       this.prefix = prefix;
    }

    public String getMessage() {
        return "Namespace prefix " + prefix + " has not been declared";
    }

}


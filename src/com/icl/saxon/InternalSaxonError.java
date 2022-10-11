package com.icl.saxon;

/**
 * An InternalError represents an condition detected during Saxon processing that
 * should never occur.
 */
 
public class InternalSaxonError extends Error {
    
    public InternalSaxonError (String message) {
       super(message);
    }

}


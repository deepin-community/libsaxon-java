package com.icl.saxon.om;
import java.util.Hashtable;

/**
  * An object representing the collection of documents handled during
  * a single transformation
  * @author Michael H. Kay
  */

public final class DocumentPool {

    // The document pool serves two purposes: it ensures that the document()
    // function, when called twice with the same URI, returns the same document
    // each time. Also, it allocates a unique number to each tree (including
    // result tree fragments). For the former purpose we use a hashtable from
    // URI to DocumentInfo object. For the latter we use a hashtable from the
    // hashcode of the DocumentInfo to the relevant integer. This is designed
    // so that presence in the pool does not stop the DocumentInfo being
    // garbage-collected.

    private Hashtable documentNameMap = new Hashtable(10);
    private Hashtable documentNumberMap = new Hashtable(10);
    private int numberOfDocuments = 0;

    /**
    * Add a document to the pool, and allocate a document number
    * @param doc The DocumentInfo for the document in question
    * @param name The name of the document. May be null, in the case of
    * the principal source document or a result tree fragment. Used for
    * the URI of a document loaded using the document() function.
    * @return the document number, unique within this document pool
    */

    public int add(DocumentInfo doc, String name) {

        Integer hash = new Integer(doc.hashCode());
        Integer nr = (Integer)documentNumberMap.get(hash);
        if (nr!=null) {
            return nr.intValue();
        } else {
            if (name!=null) {
                documentNameMap.put(name, doc);
            }
            int next = numberOfDocuments++;
            documentNumberMap.put(hash, new Integer(next));
            return next;
        }
    }

    /**
    * Get the document number of a document that is already in the pool.
    * If the document is not already in the pool, it is added, and a document
    * number is allocated. (This can happen when a Java application has built
    * the document independently of the Controller. In this case, it is still
    * necessary that all documents use the same NamePool, but we don't actually
    * check this).
    * @return the document number
    */

    public int getDocumentNumber(DocumentInfo doc) {
        Integer hash = new Integer(doc.hashCode());
        Integer nr = (Integer)documentNumberMap.get(hash);
        if (nr==null) {
            int next = numberOfDocuments++;
            nr = new Integer(next);
            documentNumberMap.put(hash, nr);
        }
        return nr.intValue();
    }

    /**
    * Get the document with a given name
    * @return the DocumentInfo with the given name if it exists,
    * or null if it is not found.
    */

    public DocumentInfo find(String name) {
        return (DocumentInfo)documentNameMap.get(name);
    }

    /**
    * Get the number of documents in the pool
    */

    public int getNumberOfDocuments() {
        return numberOfDocuments;
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
// Michael Kay  (michael.h.kay@ntlworld.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

package com.icl.saxon.jdom;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.KeyManager;
import org.jdom.Document;

import java.util.Hashtable;


/**
  * The root node of an XPath tree. (Or equivalently, the tree itself).<P>
  * This class should have been named Root; it is used not only for the root of a document,
  * but also for the root of a result tree fragment, which is not constrained to contain a
  * single top-level element.
  * @author Michael H. Kay
  */

public class DocumentWrapper extends NodeWrapper implements DocumentInfo {

    protected Hashtable keyTable = new Hashtable();
    protected NamePool namePool;
    protected String baseURI;

    public DocumentWrapper(Document doc, String baseURI) {
        super(doc, null, 0);
        node = doc;
        nodeType = NodeInfo.ROOT;
        this.baseURI = baseURI;
        docWrapper = this;
        namePool = NamePool.getDefaultNamePool();
    }

	/**
	* Set the name pool used for all names in this document
	*/

	public void setNamePool(NamePool pool) {
	    namePool = pool;
	}

	/**
	* Get the name pool used for the names in this document
	*/

	public NamePool getNamePool() {
	    return namePool;
	}

    /**
    * Get the element with a given ID, if any
    * @param id the required ID value
    * @return null: JDOM does not provide any information about attribute types.
    */

    public NodeInfo selectID(String id) {
        return null;
    }

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a list of nodes having that key value. If no index
    * has been built, returns null.
    */

    public Hashtable getKeyIndex(KeyManager keyManager, int fingerprint) {
        String key = keyManager.hashCode() + "#" + fingerprint;
        return (Hashtable)keyTable.get(key);
    }

    /**
    * Set the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @param index the index, in the form of a Hashtable that
    * maps the key value to a list of nodes having that key value
    */

    public void setKeyIndex(KeyManager keyManager, int fingerprint, Hashtable index) {
        String key = keyManager.hashCode() + "#" + fingerprint;
        keyTable.put(key, index);
    }

    /**
    * Get the unparsed entity with a given name
    * @param name the name of the entity
    * @return null: JDOM does not provide access to unparsed entities
    */

    public String getUnparsedEntity(String name) {
        return null;
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
// Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//

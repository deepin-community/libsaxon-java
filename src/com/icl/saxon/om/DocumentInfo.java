package com.icl.saxon.om;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.KeyManager;
import com.icl.saxon.PreparedStyleSheet;
import com.icl.saxon.Context;
import com.icl.saxon.expr.NodeSetValue;

import java.util.Hashtable;


/**
  * The root node of an XPath tree. (Or equivalently, the tree itself).<P>
  * This class should have been named Root; it is used not only for the root of a document,
  * but also for the root of a result tree fragment, which is not constrained to contain a
  * single top-level element.
  * @author Michael H. Kay
  */

public interface DocumentInfo extends NodeInfo {

	/**
	* Set the name pool used for all names in this document
	*/

	public void setNamePool(NamePool pool);

	/**
	* Get the name pool used for the names in this document
	*/

	public NamePool getNamePool();

    /**
    * Get the element with a given ID, if any
    * @param id the required ID value
    * @return the element with the given ID, or null if there is no such ID present (or if the parser
    * has not notified attributes as being of type ID)
    */

    public NodeInfo selectID(String id);

    /**
    * Get the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @return The index, if one has been built, in the form of a Hashtable that
    * maps the key value to a list of nodes having that key value. If no index
    * has been built, returns null.
    */

    public Hashtable getKeyIndex(KeyManager keymanager, int fingerprint);

    /**
    * Set the index for a given key
    * @param keymanager The key manager managing this key
    * @param fingerprint The fingerprint of the name of the key (unique with the key manager)
    * @param index the index, in the form of a Hashtable that
    * maps the key value to a list of nodes having that key value
    */

    public void setKeyIndex(KeyManager keymanager, int fingerprint, Hashtable index);

    /**
    * Get the unparsed entity with a given name
    * @param name the name of the entity
    * @return the URI of the entity if there is one, or null if not
    */

    public String getUnparsedEntity(String name);


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

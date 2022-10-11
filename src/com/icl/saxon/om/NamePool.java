package com.icl.saxon.om;
import com.icl.saxon.style.StandardNames;
import javax.xml.transform.TransformerException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
  * An object representing a collection of XML names, each containing a Namespace URI,
  * a Namespace prefix, and a local name; plus a collection of namespaces, each
  * consisting of a prefix/URI pair. <br>
  *
  * <p>The equivalence betweem names depends only on the URI and the local name.
  * The prefix is retained for documentary purposes only: it is useful when
  * reconstructing a document to use prefixes that the user is familiar with.</p>
  *
  * <p>The NamePool eliminates duplicate names if they have the same prefix, uri,
  * and local part. It retains duplicates if they have different prefixes</p>
  *
  *
  * @author Michael H. Kay
  */

public class NamePool {

	// The NamePool holds two kinds of entry: name entries, representing
	// expanded names (local name + prefix + URI), identified by a name code,
	// and namespace entries (prefix + URI) identified by a namespace code.
	//
	// The data structure of the name table is as follows.
	//
	// There is a fixed size hash table; names are allocated to slots in this
	// table by hashing on the local name. Each entry in the table is the head of
	// a chain of NameEntry objects representing names that have the same hash code.
	//
	// Each NameEntry represents a distinct name (same URI and local name). It contains
	// The local name as a string, plus a short integer representing the URI (as an
	// offset into the array uris[]).
	//
	// The fingerprint of a name consists of the hash slot number concatenated with
	// the depth of the entry down the chain of hash synonyms.
	//
	// A nameCode contains the fingerprint in the bottom 20 bits. It also contains
	// an 8-bit prefix index. This distinguishes the prefix used, among all the
	// prefixes that have been used with this namespace URI. If the prefix index is
	// zero, the prefix is null. Otherwise, it indexes an space-separated list of
	// prefix Strings associated with the namespace URI.


	// The default singular instance, used unless the user deliberately wants to
	// manage name pools himself

	private static NamePool defaultNamePool = new NamePool();
	static {
		defaultNamePool.loadStandardNames();
	}

	private StandardNames standardNames = null;

	/**
	* Get the singular default NamePool
	*/

	public static NamePool getDefaultNamePool() {
		return defaultNamePool;
	}


	private class NameEntry {
		String localName;
		short uriCode;
		NameEntry nextEntry;	// next NameEntry with the same hashcode

		public NameEntry(short uriCode, String localName) {
			this.uriCode = uriCode;
			this.localName = localName;
			this.nextEntry = null;
		}

	}

    NameEntry[] hashslots = new NameEntry[1024];

    String[] prefixes = new String[100];
    short prefixesUsed = 0;
    String[] uris = new String[100];
    String[] prefixesForUri = new String[100];
    short urisUsed = 0;
    Vector signatures = new Vector();	// records the stylesheets present in this namepool
	boolean sealed = false; 			// indicates that no new entries are allowed

// NOTE: signatures are no longer used in 6.5.2. However, the mechanism is retained "just in case".
// It's been deleted in the 7.1 code base.

    public NamePool() {

    	prefixes[Namespace.NULL_CODE] = "";
    	uris[Namespace.NULL_CODE] = Namespace.NULL;
    	prefixesForUri[Namespace.NULL_CODE] = "";

    	prefixes[Namespace.XML_CODE] = "xml";
    	uris[Namespace.XML_CODE] = Namespace.XML;
    	prefixesForUri[Namespace.XML_CODE] = "xml ";

    	prefixes[Namespace.XSLT_CODE] = "xsl";
    	uris[Namespace.XSLT_CODE] = Namespace.XSLT;
    	prefixesForUri[Namespace.XSLT_CODE] = "xsl ";

    	prefixes[Namespace.SAXON_CODE] = "saxon";
    	uris[Namespace.SAXON_CODE] = Namespace.SAXON;
    	prefixesForUri[Namespace.SAXON_CODE] = "saxon ";

    	prefixes[Namespace.EXSLT_FUNCTIONS_CODE] = "func";
    	uris[Namespace.EXSLT_FUNCTIONS_CODE] = Namespace.EXSLT_FUNCTIONS;
    	prefixesForUri[Namespace.EXSLT_FUNCTIONS_CODE] = "func ";

    	prefixesUsed = 5;
    	urisUsed = 5;

    }

    /**
    * Load the standard names that have a special meaning to XSLT
    */

    public synchronized void loadStandardNames() {
    	if (standardNames==null) {
    		standardNames = new StandardNames(this);
    		standardNames.allocateNames();
    	}
    }

    /**
    * Get the standard names
    */

    public StandardNames getStandardNames() {
    	return standardNames;
    }

    /**
    * Mark the NamePool to indicate that it contains names defined in a
    * particular stylesheet
    */

	public synchronized void setStylesheetSignature(Object sig) {
		   	 // System.err.println("Setting signature " + sig + " in namepool " + this);
		signatures.addElement(new Integer(sig.hashCode()));
		    // avoid keeping an object reference that locks the stylesheet into memory
	}

	/**
	* Test whether the namepool contains names defined in a particular
	* Stylesheet
	*/

	public boolean hasSignature(Object sig) {
		   	// System.err.println("Testing for signature " + sig + " in namepool " + this);
		   	// System.err.println("Found signature? " + signatures.contains(sig));

		return signatures.contains(new Integer(sig.hashCode()));
	}

	/**
	* Import the names defined in another namepool (typically the one used
	* to create the stylesheet: these names are imported into the namepool
	* used to build the source document).
	* No longer used unless name pools are managed manually
	*/

	public synchronized void importPool(NamePool other) throws TransformerException {

		   	// System.err.println("Importing namepool " + other + " into namepool " + this);

		if (signatures.size()>0) {
			throw new TransformerException("Cannot merge names into a non-empty namepool");
		}

		for (int s=0; s<other.signatures.size(); s++) {
			signatures.addElement(other.signatures.elementAt(s));
		}

		for (int i=0; i<1024; i++) {
			NameEntry entry = other.hashslots[i];
			NameEntry prev = null;
			while (entry != null) {
				NameEntry copy = new NameEntry(entry.uriCode, entry.localName);
				if (prev==null) {
					hashslots[i] = copy;
				} else {
					prev.nextEntry = copy;
				}
				prev = copy;
				entry = entry.nextEntry;
			}
		}

		this.prefixesUsed = other.prefixesUsed;
		this.urisUsed = other.urisUsed;
		if (prefixesUsed > 60) {
			this.prefixes = new String[prefixesUsed * 2];
		}
		if (urisUsed > 60) {
			this.uris = new String[urisUsed * 2];
			this.prefixesForUri = new String[urisUsed * 2];
		}
		System.arraycopy(other.prefixes, 0, this.prefixes, 0, prefixesUsed);
		System.arraycopy(other.uris, 0, this.uris, 0, urisUsed);
		System.arraycopy(other.prefixesForUri, 0, this.prefixesForUri, 0, urisUsed);

		other.sealed = true;
	}

	/**
	* Determine whether the namepool is sealed
	*/

	public boolean isSealed() {
		return sealed;
	}

	/**
	* Get a name entry corresponding to a given name code
	* @return null if there is none.
	*/

	private NameEntry getNameEntry(int nameCode) {
		int hash = nameCode & 0x3ff;
		int depth = (nameCode >> 10) & 0x3ff;
		NameEntry entry = hashslots[hash];

		for (int i=0; i<depth; i++) {
			if (entry==null) return null;
			entry = entry.nextEntry;
		}
		return entry;
	}

    /**
    * Allocate the namespace code for a namespace prefix/URI pair.
    * Create it if not already present
    */

    public synchronized int allocateNamespaceCode(String prefix, String uri) {
    			// System.err.println("allocate nscode for " + prefix + " = " + uri);

    	int prefixCode = allocateCodeForPrefix(prefix);
    	int uriCode = allocateCodeForURI(uri);

    	if (prefixCode!=0) {
    		// ensure the prefix is in the list of prefixes used with this URI
    		String key = prefix + " ";
    		if (prefixesForUri[uriCode].indexOf(key) < 0) {
    			prefixesForUri[uriCode] += key;
    		}
		}

    	return (prefixCode<<16) + uriCode;
    }

    /**
    * Get the existing namespace code for a namespace prefix/URI pair.
    * @return -1 if there is none present
    */

    public int getNamespaceCode(String prefix, String uri) {
    			//System.err.println("get nscode for " + prefix + " = " + uri);
    	int prefixCode = getCodeForPrefix(prefix);
    	if (prefixCode<0) return -1;
    	int uriCode = getCodeForURI(uri);
    	if (uriCode<0) return -1;

    	if (prefixCode!=0) {
    		// ensure the prefix is in the list of prefixes used with this URI
    		String key = prefix + " ";
    		if (prefixesForUri[uriCode].indexOf(key) < 0) {
    			return -1;
    		}
		}

    	return (prefixCode<<16) + uriCode;
    }

	/**
	* Allocate the uri code for a given URI;
	* create one if not found, unless the namepool is sealed
	*/

	public synchronized short allocateCodeForURI(String uri) {
                    //System.err.println("allocate code for URI " + uri);
    	for (short j=0; j<urisUsed; j++) {
    		if (uris[j].equals(uri)) {
    			return j;
    		}
    	}
    	if (sealed) {
    		throw new IllegalArgumentException("Namepool has been sealed");
    	}
		if (urisUsed >= uris.length) {
			if (urisUsed>32000) {
				throw new IllegalArgumentException("Too many namespace URIs");
			}
			String[] p = new String[urisUsed*2];
			String[] u = new String[urisUsed*2];
			System.arraycopy(prefixesForUri, 0, p, 0, urisUsed);
			System.arraycopy(uris, 0, u, 0, urisUsed);
			prefixesForUri = p;
			uris = u;
		}
		uris[urisUsed] = uri;
		prefixesForUri[urisUsed] = "";
		return urisUsed++;
    }



	/**
	* Get the uri code for a given URI
	* @return -1 if not present in the name pool
	*/

	public short getCodeForURI(String uri) {
    	for (short j=0; j<urisUsed; j++) {
    		if (uris[j].equals(uri)) {
    			return j;
    		}
    	}
		return -1;
    }

	/**
	* Allocate the prefix code for a given Prefix; create one if not found
	*/

	public synchronized short allocateCodeForPrefix(String prefix) {
    	for (short i=0; i<prefixesUsed; i++) {
    		if (prefixes[i].equals(prefix)) {
    			return i;
    		}
    	}
    	if (sealed) {
    		throw new IllegalArgumentException("Namepool has been sealed");
    	}
		if (prefixesUsed >= prefixes.length) {
			if (prefixesUsed>32000) {
				throw new IllegalArgumentException("Too many namespace prefixes");
			}
			String[] p = new String[prefixesUsed*2];
			System.arraycopy(prefixes, 0, p, 0, prefixesUsed);
			prefixes = p;
		}
		prefixes[prefixesUsed] = prefix;
		return prefixesUsed++;
    }


	/**
	* Get the prefix code for a given Prefix
	* @return -1 if not found
	*/

	public short getCodeForPrefix(String prefix) {
    	for (short i=0; i<prefixesUsed; i++) {
    		if (prefixes[i].equals(prefix)) {
    			return i;
    		}
    	}
		return -1;
    }

    /**
    * Get the index of a prefix among all the prefixes used with a given URI
    * @return -1 if not found
    */

    public int getPrefixIndex(short uriCode, String prefix) {

    	// look for quick wins
    	if (prefix.equals("")) return 0;
    	if (prefixesForUri[uriCode].equals(prefix+" ")) return 1;

    	// search for the prefix in the list
    	int i = 1;
    	StringTokenizer tok = new StringTokenizer(prefixesForUri[uriCode]);
    	while (tok.hasMoreElements()) {
    		if (prefix.equals(tok.nextElement())) {
    			return i;
    		}
    		if (i++==255) {
    			throw new IllegalArgumentException("Too many prefixes for one namespace URI");
    		}
    	}
    	return -1;
    }

    /**
    * Get a prefix among all the prefixes used with a given URI, given its index
    * @return null if not found
    */

    public String getPrefixWithIndex(short uriCode, int index) {
    	if (index==0) return "";
    	StringTokenizer tok = new StringTokenizer(prefixesForUri[uriCode]);
    	int i=1;
    	while (tok.hasMoreElements()) {
    		String prefix = (String)tok.nextElement();
    		if (i++ == index) {
    			return prefix;
    		}
    	}
    	return null;
    }

    /**
    * Allocate a name from the pool, or a new Name if there is not a matching one there
    * @param prefix
    * @param uri - the namespace URI
    * @param localName
    * @return an integer (the "namecode") identifying the name within the namepool.
    * The Name itself may be retrieved using the getName(int) method
    */

    public synchronized int allocate(String prefix, String uri, String localName) {
    	short uriCode = allocateCodeForURI(uri);
    	return allocate(prefix, uriCode, localName);
    }

    /**
    * Allocate a name from the pool, or a new Name if there is not a matching one there
    * @param prefix
    * @param uriCode - the code of the URI
    * @param localName
    * @return an integer (the "namecode") identifying the name within the namepool.
    */

    public synchronized int allocate(String prefix, short uriCode, String localName) {
    	        // System.err.println("Allocate " + prefix + " : " + uriCode + " : " + localName);
        int hash = (localName.hashCode() & 0x7fffffff) % 1023;
        int depth = 0;
        int prefixIndex = getPrefixIndex(uriCode, prefix);

        if (prefixIndex<0) {
        	prefixesForUri[uriCode] += (prefix + " ");
        	prefixIndex = getPrefixIndex(uriCode, prefix);
        }
        NameEntry entry = null;

        if (hashslots[hash]==null) {
	    	if (sealed) {
	    		throw new IllegalArgumentException("Namepool has been sealed");
	    	}
			entry = new NameEntry(uriCode, localName);
			hashslots[hash] = entry;
		} else {
			entry = hashslots[hash];
			while (true) {
				boolean sameLocalName = (entry.localName.equals(localName));
				boolean sameURI = (entry.uriCode==uriCode);

				if (sameLocalName && sameURI) {
							// may need to add a new prefix to the entry
					break;
				} else {
					NameEntry next = entry.nextEntry;
					depth++;
					if (depth >= 1024) {
						throw new java.lang.IllegalArgumentException("Saxon name pool is full");
					}
					if (next==null) {
				    	if (sealed) {
				    		throw new IllegalArgumentException("Namepool has been sealed");
				    	}
						NameEntry newentry = new NameEntry(uriCode, localName);
						entry.nextEntry = newentry;
						break;
					} else {
						entry = next;
					}
				}
			}
		}
		// System.err.println("name code = " + prefixIndex + "/" + depth + "/" + hash);
		return ((prefixIndex<<20) + (depth<<10) + hash);
	}

    /**
    * Allocate a namespace code for the prefix/URI of a given namecode
    */

    public synchronized int allocateNamespaceCode(int namecode) {
    	String prefix = getPrefix(namecode);
    	int uriCode = getURICode(namecode);
    	int prefixCode = allocateCodeForPrefix(prefix);
    	return (prefixCode<<16) + uriCode;
    }


    /**
    * Get a namespace code for the prefix/URI of a given namecode
    */

    public int getNamespaceCode(int namecode) {
    	String prefix = getPrefix(namecode);
    	int uriCode = getURICode(namecode);
    	int prefixCode = getCodeForPrefix(prefix);
    	return (prefixCode<<16) + uriCode;
    }

	/**
	* Get the namespace-URI of a name, given its name code or fingerprint
	*/

	public String getURI(int nameCode) {
		NameEntry entry = getNameEntry(nameCode);
		if (entry==null) {
			unknownNameCode(nameCode);
		}
		return uris[entry.uriCode];
	}

	/**
	* Get the URI code of a name, given its name code or fingerprint
	*/

	public short getURICode(int nameCode) {
		NameEntry entry = getNameEntry(nameCode);
		if (entry==null) {
			unknownNameCode(nameCode);
		}
		return entry.uriCode;
	}

	/**
	* Get the local part of a name, given its name code or fingerprint
	*/

	public String getLocalName(int nameCode) {
		NameEntry entry = getNameEntry(nameCode);
		if (entry==null) {
			unknownNameCode(nameCode);
		}
		return entry.localName;
	}

	/**
	* Get the prefix part of a name, given its name code or fingerprint
	*/

	public String getPrefix(int nameCode) {
		short uriCode = getURICode(nameCode);
		int prefixIndex = (nameCode >> 20) & 0xff;
		return getPrefixWithIndex(uriCode, prefixIndex);
	}

	/**
	* Get the display form of a name (the QName), given its name code or fingerprint
	*/

	public String getDisplayName(int nameCode) {
		NameEntry entry = getNameEntry(nameCode);
		if (entry==null) {
			unknownNameCode(nameCode);
		}
		int prefixIndex = (nameCode >> 20) & 0xff;
		if (prefixIndex==0) return entry.localName;
		return getPrefixWithIndex(entry.uriCode, prefixIndex) + ':' + entry.localName;
	}

	/**
	* Internal error: name not found in namepool
	* (Usual cause is allocating a name code from one name pool and trying to
	* find it in another)
	*/

	private void unknownNameCode(int nameCode) {
		System.err.println("Unknown name code " + nameCode);
		diagnosticDump();
		(new IllegalArgumentException("Unknown name")).printStackTrace();
		throw new IllegalArgumentException("Unknown name code " + nameCode);
	}

	/**
	* Get a fingerprint for the name with a given name code.
	* The signature has the property that if two signatures are the same, the names
	* are the same (ie. same local name and same URI)
	*/

	public int getFingerprint(int nameCode) {
		return nameCode & 0xfffff;	// 20 bits: mask out the prefix and node type parts
	}

	/**
	* Get a fingerprint for the name with a given uri and local name.
	* These must be present in the NamePool.
	* The signature has the property that if two signatures are the same, the names
	* are the same (ie. same local name and same URI).
	* @return -1 if not found
	*/

	public int getFingerprint(String uri, String localName) {
		// A read-only version of allocate()

		short uriCode = -1;
    	for (short j=0; j<urisUsed; j++) {
    		if (uris[j].equals(uri)) {
    			uriCode = j;
    			break;
    		}
    	}

    	if (uriCode==-1) return -1;

        int hash = (localName.hashCode() & 0x7fffffff) % 1023;
        int depth = 0;

        NameEntry entry = null;

        if (hashslots[hash]==null) {
			return -1;
		} else {
			entry = hashslots[hash];
			while (true) {
				boolean sameLocalName = (entry.localName.equals(localName));
				boolean sameURI = (entry.uriCode==uriCode);

				if (sameLocalName && sameURI) {
					break;
				} else {
					NameEntry next = entry.nextEntry;
					depth++;
					if (next==null) {
						return -1;
					} else {
						entry = next;
					}
				}
			}
		}
		return (depth<<10) + hash;
	}

	/**
	* Get the namespace URI from a namespace code
	*/

	public String getURIFromNamespaceCode(int code) {
		return uris[code&0xffff];
	}

	/**
	* Get the namespace URI from a URI code
	*/

	public String getURIFromURICode(short code) {
		return uris[code];
	}

	/**
	* Get the namespace prefix from a namespace code
	*/

	public String getPrefixFromNamespaceCode(int code) {
			// System.err.println("get prefix for " + code);
		return prefixes[code>>16];
	}



    /**
    * Diagnostic print of the namepool contents
    */

    public synchronized void diagnosticDump() {
    	System.err.println("Contents of NamePool " + this);
		for (int i=0; i<1024; i++) {
			NameEntry entry = hashslots[i];
			int depth = 0;
			while (entry != null) {
				System.err.println("Fingerprint " + depth + "/" + i);
				System.err.println("  local name = " + entry.localName +
									 " uri code = " + entry.uriCode);
				entry = entry.nextEntry;
				depth++;
			}
		}

		for (int p=0; p<prefixesUsed; p++) {
			System.err.println("Prefix " + p + " = " + prefixes[p]);
		}
		for (int u=0; u<urisUsed; u++) {
			System.err.println("URI " + u + " = " + uris[u]);
			System.err.println("Prefixes for URI " + u + " = " + prefixesForUri[u]);
		}
	}

    /**
    * The following code is used to create a list of Java declarations for
    * the fingerprints of standard names used in a stylesheet. This code is
    * executed while building Saxon, to create the constant definitions appearing
    * in the StandardNames module.
    */

    public void generateJavaConstants() {
    	System.out.println("// Declarations generated from NamePool");
		for (int i=0; i<1024; i++) {
			NameEntry entry = hashslots[i];
			int depth = 0;
			while (entry != null) {
				int fingerprint = (depth<<10) + i;
				String prefix="";
				if (entry.uriCode==Namespace.NULL_CODE) prefix="";
				else if (entry.uriCode==Namespace.XSLT_CODE) prefix="XSL_";
				else if (entry.uriCode==Namespace.XML_CODE) prefix="XML_";
				else if (entry.uriCode==Namespace.SAXON_CODE) prefix="SAXON_";
				String localname = entry.localName.toUpperCase();
				while (true) {
					int h = localname.indexOf('-');
					if (h<0) break;
					localname = localname.substring(0, h) + '_' + localname.substring(h+1);
				}

				System.out.println("public final static int " +
							 prefix + localname + " = " + fingerprint + ";");
				entry = entry.nextEntry;
				depth++;
			}
		}
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

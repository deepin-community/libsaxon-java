package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import org.xml.sax.Attributes;


    /**
    * AttributeCollection is an implementation of the SAX2 interface Attributes
    * that also provides the ability to manipulate namespaces and to convert attributes
    * into Nodes.
    *
    * It is extremely similar (both in interface and in implementation) to the SAX2 Attributes
    * class, but was defined before SAX2 was available.
    */

public final class AttributeCollection implements Attributes
{

    // we use a single array for economy. The elements of this array are arranged
    // in groups of three, being respectively the nameCode, the
    // type, and the value

	private NamePool namePool;
    private Object[] list = null;
    private int used = 0;

    private static int RECSIZE = 3;
    private static int NAMECODE = 0;
    private static int TYPE = 1;
    private static int VALUE = 2;

    /**
    * Create an empty attribute list.
    */

    public AttributeCollection (NamePool pool) {
    	namePool = pool;
    	list = null;
    	used = 0;
 	}

    /**
    * Create an empty attribute list with space for n attributes
    */

    public AttributeCollection (NamePool pool, int n) {
    	namePool = pool;
        list = new Object[n*RECSIZE];
        used = 0;
    }

    /**
    * Create a new attribute collection as a clone
    */

    public AttributeCollection (AttributeCollection atts) {
    	this.namePool = atts.namePool;
        this.list = new Object[atts.used];
        if (atts.used > 0 ) {
            System.arraycopy(atts.list, 0, this.list, 0, atts.used);
        }
        this.used = atts.used;
    }

    /**
    * Create a new attribute collection as a clone
    */

    public AttributeCollection (NamePool pool, Attributes atts) {
    	namePool = pool;
        int len = atts.getLength();
        used = len*RECSIZE;
        this.list = new Object[used];

        for (int a=0; a<len; a++) {
            int j = a*RECSIZE;
            String qname = atts.getQName(a);
            String prefix = Name.getPrefix(qname);
            String uri = atts.getURI(a);
            String localName = atts.getLocalName(a);
            int nameCode = namePool.allocate(prefix, uri, localName);
            list[j+NAMECODE] = new Integer(nameCode);
            list[j+TYPE] = atts.getType(a);
            list[j+VALUE] = atts.getValue(a);
        }
    }

    /**
    * Add an attribute to an attribute list.
    * @param name The attribute name.
    * @param type The attribute type ("NMTOKEN" for an enumeration).
    * @param value The attribute value (must not be null).
    * @see org.xml.sax.DocumentHandler#startElement
    */

    public void addAttribute (int nameCode, String type, String value)
    {
       if (list==null) {
            list = new Object[5*RECSIZE];
            used = 0;
        }
        if (list.length == used) {
        	int newsize = (used==0 ? 5*RECSIZE : used*2);
            Object[] newlist = new Object[newsize];
            System.arraycopy(list, 0, newlist, 0, used);
            list = newlist;
        }
        list[used++] = new Integer(nameCode);
        list[used++] = type;
        list[used++] = value;
    }

    /**
    * Add an attribute to an attribute list.
    * @param prefix The namespace prefix of the attribute name.
    * @param uri The namespace uri of the attribute name.
    * @param localname The local part of the attribute name.
    * @param type The attribute type (e.g. "NMTOKEN").
    * @param value The attribute value (must not be null).
    * @see org.xml.sax.DocumentHandler#startElement
    */

    public void addAttribute (String prefix, String uri, String localName, String type, String value)
    {
    	addAttribute(namePool.allocate(prefix, uri, localName), type, value);
    }

    /**
    * Set an attribute value
    * @param name the name of the attribute
    * @param type the type of the attribute (e.g. CDATA)
    * @param value the value of the attribute
    */

    public void setAttribute(String prefix, String uri, String localName, String type, String value)
    {
    	int nameCode = namePool.allocate(prefix, uri, localName);
    	int offset = findByFingerprint(nameCode&0xfffff);
        if (offset<0) {
            addAttribute(prefix, uri, localName, type, value);
        } else {
            list[offset + NAMECODE] = new Integer(nameCode);
			list[offset + TYPE] = type;
            list[offset + VALUE] = value;
        }
    }

    /**
    * Set an attribute value
    * @param name the name of the attribute
    * @param type the type of the attribute (e.g. CDATA)
    * @param value the value of the attribute
    */

    public void setAttribute(int nameCode, String type, String value)
    {
    	int offset = findByFingerprint(nameCode&0xfffff);
        if (offset<0) {
            addAttribute(nameCode, type, value);
        } else {
            list[offset + NAMECODE] = new Integer(nameCode);
			list[offset + TYPE] = type;
            list[offset + VALUE] = value;
        }
    }

    /**
    * Clear the attribute list.
    */

    public void clear ()
    {
        used = 0;
    }

    /**
    * Compact the attribute list to avoid wasting memory
    */

    public void compact() {
        if (used==0) {
            list = null;
        } else if (list.length > used) {
            Object[] newlist = new Object[used];
            System.arraycopy(list, 0, newlist, 0, used);
            list = newlist;
        }
    }


    //////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Attributes
    //////////////////////////////////////////////////////////////////////


    /**
    * Return the number of attributes in the list.
    * @return The number of attributes in the list.
    */

    public int getLength ()
    {
        return (list==null ? 0 : used / RECSIZE );
    }

    /**
    * Get the namecode of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The display name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public int getNameCode (int index)
    {
        int offset = index*RECSIZE;
        if (list==null) return -1;
        if (offset >= used) return -1;

        return ((Integer)list[offset+NAMECODE]).intValue();
    }

    /**
    * Get the display name of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The display name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public String getQName (int index)
    {
        int offset = index*RECSIZE;
        if (list==null) return null;
        if (offset >= used) return null;
        return namePool.getDisplayName(getNameCode(index));
    }

    /**
    * Get the local name of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The local name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public String getLocalName (int index)
    {
        if (list==null) return null;
        if (index*RECSIZE >= used) return null;
        return namePool.getLocalName(getNameCode(index));
    }

    /**
    * Get the namespace URI of an attribute (by position).
    *
    * @param index The position of the attribute in the list.
    * @return The local name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public String getURI (int index)
    {
        if (list==null) return null;
        if (index*RECSIZE >= used) return null;
        return namePool.getURI(getNameCode(index));
    }



    /**
    * Get the type of an attribute (by position).
    * @param index The position of the attribute in the list.
    * @return The attribute type as a string ("NMTOKEN" for an
    *         enumeration, and "CDATA" if no declaration was
    *         read), or null if there is no attribute at
    *         that position.
    */

    public String getType (int index)
    {
        int offset = index*RECSIZE;
        if (list==null) return null;
        if (offset >= used) return null;
        return (String)list[offset+TYPE];
    }

    /**
    * Get the type of an attribute (by name).
    *
    * @param uri The namespace uri of the attribute.
    * @param localname The local name of the attribute.
    * @return The index position of the attribute
    */

    public String getType (String uri, String localname)
    {
        int offset = findByName(uri, localname);
        return ( offset<0 ? null : (String)list[offset+TYPE]);
    }

    /**
    * Get the value of an attribute (by position).
    *
    * @param index The position of the attribute in the list.
    * @return The attribute value as a string, or null if
    *         there is no attribute at that position.
    */

    public String getValue (int index) {
        int offset = index*RECSIZE;
        if (list==null) return null;
        if (offset >= used) return null;
        return (String)list[offset+VALUE];
    }

    /**
    * Get the value of an attribute (by name).
    *
    * @param uri The namespace uri of the attribute.
    * @param localname The local name of the attribute.
    * @return The index position of the attribute
    */

    public String getValue (String uri, String localname)
    {
        int offset = findByName(uri, localname);
        return ( offset<0 ? null : (String)list[offset+VALUE]);
    }

	/**
	* Get the attribute value using its fingerprint
	*/

	public String getValueByFingerprint(int fingerprint) {
		int offset = findByFingerprint(fingerprint);
        return ( offset<0 ? null : (String)list[offset+VALUE]);
    }

    /**
    * Get the index of an attribute (by name).
    *
    * @param name The display name of the attribute.
    * @return The index position of the attribute
    */

    public int getIndex (String name)
    {
        int offset = findByDisplayName(name);
        return ( offset<0 ? -1 : offset / RECSIZE);
    }

    /**
    * Get the index of an attribute (by name).
    *
    * @param uri The namespace uri of the attribute.
    * @param localname The local name of the attribute.
    * @return The index position of the attribute
    */

    public int getIndex (String uri, String localname)
    {
        int offset = findByName(uri, localname);
        return ( offset<0 ? -1 : offset / RECSIZE);
    }

	/**
	* Get the index, given the fingerprint
	*/

	public int getIndexByFingerprint(int fingerprint) {
		int offset = findByFingerprint(fingerprint);
        return ( offset<0 ? -1 : offset / RECSIZE);
    }

    /**
    * Get the type of an attribute (by name).
    *
    * @param name The display name of the attribute.
    * @return The attribute type as a string ("NMTOKEN" for an
    *         enumeration, and "CDATA" if no declaration was
    *         read).
    */

    public String getType (String name)
    {
        int offset = findByDisplayName(name);
        return ( offset<0 ? null : (String)list[offset+TYPE]);
    }


    /**
    * Get the value of an attribute (by name).
    *
    * @param name The attribute name.
    */

    public String getValue (String name)
    {
        int offset = findByDisplayName(name);
        return ( offset<0 ? null : (String)list[offset+VALUE]);
    }

    //////////////////////////////////////////////////////////////////////
    // Additional methods for handling structured Names
    //////////////////////////////////////////////////////////////////////

    /**
    * Find an attribute by name
    * @return the offset of the attribute, or -1 if absent
    */

    private int findByName(String uri, String localName) {
    	if (namePool==null) return -1;		// indicates an empty attribute set
    	int f = namePool.getFingerprint(uri, localName);
    	if (f==-1) return -1;
    	return findByFingerprint(f);
    }

    /**
    * Find an attribute by fingerprint
    * @return the offset of the attribute, or -1 if absent
    */

    private int findByFingerprint(int fingerprint) {
        if (list==null) return -1;
        for (int i=0; i<used; i+=RECSIZE) {
            if (fingerprint==(((Integer)list[i+NAMECODE]).intValue()&0xfffff)) {
                return i;
            }
        }
        return -1;
    }

    /**
    * Find an attribute by display name
    * @return the offset of the attribute
    */

    private int findByDisplayName(String qname) {
        if (list==null) return -1;
        String prefix = Name.getPrefix(qname);
        if (prefix.equals("")) {
        	return findByName("", qname);
        } else {
            String localName = Name.getLocalName(qname);
            for (int i=0; i<getLength(); i++) {
            	String lname=namePool.getLocalName(getNameCode(i));
				String ppref=namePool.getPrefix(getNameCode(i));
                if (localName.equals(lname) && prefix.equals(ppref)) {
                    return i;
                }
            }
            return -1;
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

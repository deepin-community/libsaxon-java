package com.icl.saxon.om;
import com.icl.saxon.Mode;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.ContentEmitter;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.ProxyEmitter;
import com.icl.saxon.pattern.*;
import com.icl.saxon.tree.ElementImpl;

import org.xml.sax.Attributes;

import javax.xml.transform.TransformerException;

/**
  * The Stripper class maintains details of which elements need to be stripped.
  * The code is written to act as a SAX filter to do the stripping.
  * @author Michael H. Kay
  */

public class Stripper extends ProxyEmitter
{
    private boolean preserveAll;              // true if all elements have whitespace preserved
    private boolean stripAll;                 // true if all whitespace nodes are stripped

    // stripStack is used to hold information used while stripping nodes. We avoid allocating
    // space on the tree itself to keep the size of nodes down. Each entry on the stack is two
    // booleans, one indicates the current value of xml-space is "preserve", the other indicates
    // that we are in a space-preserving element.

    // We implement our own stack to avod the overhead of allocating objects. The two booleans
    // are held as the ls bits of a byte.

    private byte[] stripStack = new byte[100];
    private int top = 0;

	// We use a collection of rules to determine whether to strip spaces; a collection
	// of rules is known as a Mode. (We are reusing the code for template rule matching)

	private Mode stripperMode;

	// Mode expects to test an Element, so we create a dummy element for it to test
	private DummyElement element = new DummyElement();

	// Stripper needs a Controller (a) to create a dummy Context for evaluating patterns
	// and (b) to provide reporting of rule conflicts.
	private Context context;

	// Need the namePool to get URI codes from name codes
	private NamePool namePool;

    /**
    * Default constructor for use in subclasses
    */

    protected Stripper() {}

    /**
    * create a Stripper and initialise variables
    * @param stripperRules: defines which elements have whitespace stripped. If
    * null, all whitespace is preserved.
    */

    public Stripper(Mode stripperRules) {
        stripperMode = stripperRules;
        preserveAll = (stripperRules==null);
        stripAll = false;
    }

    /**
    * Specify that all whitespace nodes are to be preserved
    */

    public void setPreserveAll() {
        preserveAll = true;
        stripAll = false;
    }

    /**
    * Determine if all whitespace is to be preserved (in this case, no further testing
    * is needed)
    */

    public boolean getPreserveAll() {
    	return preserveAll;
    }

    /**
    * Specify that all whitespace nodes are to be stripped
    */

    public void setStripAll() {
        preserveAll = false;
        stripAll = true;
    }

    /**
    * Determine if all whitespace is to be stripped (in this case, no further testing
    * is needed)
    */

    public boolean getStripAll() {
    	return stripAll;
    }


	/**
	* Set the Controller to be used
	*/

	public void setController(Controller controller) {
		context = controller.makeContext(element);
		namePool = controller.getNamePool();
	}

    /**
    * Decide whether an element is in the set of white-space preserving element types
    * @param uri The namespace URI of the element name
    * @param localname The local part of the element name
    * @return true if the element is in the set of white-space preserving element types
    */

    public boolean isSpacePreserving(int nameCode) {
    	try {
	    	if (preserveAll) return true;
	    	if (stripAll) return false;
	    	element.setNameCode(nameCode);
	    	Object rule = stripperMode.getRule(element, context);
	    	if (rule==null) return true;
	    	return ((Boolean)rule).booleanValue();
	    } catch (TransformerException err) {
	    	return true;
	    }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws TransformerException
    {
        // System.err.println("Stripper#startDocument()");
        top = 0;
        stripStack[top]=0x01;             // {xml:preserve = false, preserve this element = true}
        super.startDocument();
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (int nameCode, Attributes atts, int[] namespaces, int nscount)
    throws TransformerException
    {
    	// System.err.println("startElement " + nameCode);
        super.startElement(nameCode, atts, namespaces, nscount);

        byte preserveParent = stripStack[top];

        String xmlspace = atts.getValue(Namespace.XML, "space");
        byte preserve = (byte)(preserveParent & 0x02);
        if (xmlspace!=null) {
            if (xmlspace.equals("preserve")) preserve = 0x02;
            if (xmlspace.equals("default")) preserve = 0x00;
        }
        if (isSpacePreserving(nameCode)) {
            preserve |= 0x01;
        }

        // put "preserve" value on top of stack

        top++;
        if (top >= stripStack.length) {
            byte[] newStack = new byte[top*2];
            System.arraycopy(stripStack, 0, newStack, 0, top);
            stripStack = newStack;
        }
        stripStack[top] = preserve;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (int nameCode) throws TransformerException
    {
        super.endElement(nameCode);
        top--;
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int length) throws TransformerException
    {
        // assume adjacent chunks of text are already concatenated

        if (length > 0) {
            if (stripStack[top]!=0 || !isWhite(ch, start, length)) {
                super.characters(ch, start, length);
            }
        }
    }

    /**
    * Decide whether the accumulated character data is all whitespace
    */

    private boolean isWhite(char[] ch, int start, int length) {
        for (int i=start; i<start+length; i++) {
            if ( (int)ch[i] > 0x20 ) {
                return false;
            }
        }
        return true;
    }



	private class DummyElement extends ElementImpl {
		public short getURICode() {
			return namePool.getURICode(getNameCode());
		}

	}

}   // end of class Stripper

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

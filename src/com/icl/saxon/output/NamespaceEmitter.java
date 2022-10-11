package com.icl.saxon.output;
import com.icl.saxon.*;
//import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.om.NamePool;
import org.xml.sax.Attributes;
import java.io.*;
import java.util.*;
import javax.xml.transform.TransformerException;

/**
  * NamespaceEmitter is a ProxyEmitter responsible for removing duplicate namespace
  * declarations. It also ensures that an xmlns="" undeclaration is output when
  * necessary.
  */

public class NamespaceEmitter extends ProxyEmitter
{
    protected NamePool namePool;
    protected int nscodeXML;
    protected int nscodeNull;

    // We keep track of namespaces to avoid outputting duplicate declarations. The namespaces
    // vector holds a list of all namespaces currently declared (organised as pairs of entries,
    // prefix followed by URI). The stack contains an entry for each element currently open; the
    // value on the stack is an Integer giving the size of the namespaces vector on entry to that
    // element.

    private int[] namespaces = new int[30];          // all namespace codes currently declared
    private int namespacesSize = 0;                  // all namespaces currently declared
    private int[] namespaceStack = new int[100];
    private int nsStackTop = 0;


	/**
	* Set the name pool to be used for all name codes
	*/

	public void setNamePool(NamePool pool) {
	            //pool.diagnosticDump();
		namePool = pool;
		nscodeXML = pool.getNamespaceCode("xml", Namespace.XML);
		nscodeNull = pool.getNamespaceCode("", "");
		super.setNamePool(pool);
	}


    /**
    * startElement. This call removes redundant namespace declarations, and
    * possibly adds an xmlns="" undeclaration.
    */

    public void startElement(int nameCode, Attributes attList,
    						 int[] namespaceCodes, int nrOfCodes) throws TransformerException {

                // System.err.println("NSEmitter startElement nameCode=" + nameCode);
                // for (int i=0; i<nrOfCodes; i++) {
                //     System.err.println("  NS " + namespaceCodes[i]);
                // }

        // Create an array to hold the unique namespaces

		int[] uniqueNamespaces = new int[namespaceCodes.length+1];
		int uniqueCount = 0;

        // Ensure that the element namespace is output

        int elementNamespace = namePool.allocateNamespaceCode(nameCode);
        if (isNeeded(elementNamespace)) {
            addToStack(elementNamespace);
            uniqueNamespaces[uniqueCount++] = elementNamespace;
        }

        // Now de-duplicate the supplied list of namespaces

		for (int n=0; n<nrOfCodes; n++) {
			int nscode = namespaceCodes[n];
            if (isNeeded(nscode)) {
                addToStack(nscode);
                uniqueNamespaces[uniqueCount++] = nscode;
            }
        }

        // remember how many namespaces there were so we can unwind the stack later

        if (nsStackTop>=namespaceStack.length) {
            int[] newstack = new int[nsStackTop*2];
            System.arraycopy(namespaceStack, 0, newstack, 0, nsStackTop);
            namespaceStack = newstack;
        }

        namespaceStack[nsStackTop++] = uniqueCount;

		// finally, pass the namespace on to the underlying Emitter
        super.startElement(nameCode, attList, uniqueNamespaces, uniqueCount);
    }

    /**
    * Determine whether a namespace declaration is needed
    */

    private boolean isNeeded(int nscode) {
        if (nscode==nscodeXML) {
        		// Ignore the XML namespace
            return false;
        }

        for (int i=namespacesSize-1; i>=0; i--) {
        	if (namespaces[i]==nscode) {
        		// it's a duplicate so we don't need it
        		return false;
        	}
        	if ((namespaces[i]>>16) == (nscode>>16)) {
        		// same prefix, different URI, so we do need it
        		return true;
            }
        }
        return (nscode != nscodeNull);
    }

    /**
    * Add a namespace declaration to the stack
    */

    private void addToStack(int nscode) {
		// expand the stack if necessary
        if (namespacesSize+1 >= namespaces.length) {
            int[] newlist = new int[namespacesSize*2];
            System.arraycopy(namespaces, 0, newlist, 0, namespacesSize);
            namespaces = newlist;
        }
        namespaces[namespacesSize++] = nscode;
    }


    /**
    * endElement: Discard the namespaces declared on this element.
    */


    public void endElement (int nameCode) throws TransformerException
    {
        if (nsStackTop-- == 0) {
            throw new TransformerException("Attempt to output end tag with no matching start tag");
        }

        int nscount = namespaceStack[nsStackTop];
        namespacesSize -= nscount;

        super.endElement(nameCode);

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

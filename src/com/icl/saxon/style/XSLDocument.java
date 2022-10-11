package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NamePool;
import com.icl.saxon.om.Namespace;

import com.icl.saxon.om.NamespaceException;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Properties;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;

/**
* An xsl:document (formerly saxon:output) element in the stylesheet. <BR>
* The xsl:document element takes an attribute href="filename". The filename will
* often contain parameters, e.g. {position()} to ensure that a different file is produced
* for each element instance. <BR>
* There is a further attribute method=xml|html|text which determines the format of the
* output file (default XML).
* Alternatively the xsl:document element may take a next-in-chain attribute in which case
* output is directed to another stylesheet.
* Any unrecognized namespaced attributes are interepreted as attribute value templates,
* and their values are added to the output properties, for use by a user-defined Emitter.
*/

public class XSLDocument extends XSLGeneralOutput {

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {
        super.prepareAttributes();
        if (href==null) {
            reportAbsence("href");
        }
    }

    public void validate() throws TransformerConfigurationException {
        if (getURI().equals(Namespace.XSLT)) {
            // saxon:output is OK, but xsl:document requires version="1.1"
            if (!forwardsCompatibleModeIsEnabled()) {
                compileError("To use xsl:document, set xsl:stylesheet version='1.1'");
            }
        }
        checkWithinTemplate();
    }

    public void process(Context context) throws TransformerException
    {
        Controller c = context.getController();
        Outputter oldOutputter = c.getOutputter();
        Properties prevProps = oldOutputter.getOutputProperties();
        Properties details =  new Properties(prevProps);
        updateOutputProperties(details, context);
        Result result = null;
        FileOutputStream stream;

        // following code to create any directory that doesn't exist is courtesy of
        // Brett Knights [brett@knightsofthenet.com]
        // Modified by MHK to work with JDK 1.1

        String outFileName = href.evaluateAsString(context);
        try {
		    File outFile = new File(outFileName);
	        if (!outFile.exists()) {
	            String parent = outFile.getParent();        // always returns null with Microsoft JVM?
	            if (parent!=null && !Version.isPreJDK12()) {
    				File parentPath = new File(parent);
    				if (parentPath != null && !parentPath.exists()) {
 						parentPath.mkdirs();
    				}
				    outFile.createNewFile();                // JDK 1.2 method
	            }
		    }
		    stream = new FileOutputStream(outFile);
            result = new StreamResult(stream);
        } catch (java.io.IOException err) {
            throw new TransformerException("Failed to create output file " + outFileName, err);
        }

        if (nextInChain != null) {
            String href = nextInChain.evaluateAsString(context);
            TransformerHandler nextStyleSheet = prepareNextStylesheet(href, context);
            ContentHandlerProxy emitter = new ContentHandlerProxy();
            emitter.setSystemId(this.getSystemId());    // pragmatic choice of URI
            emitter.setUnderlyingContentHandler(nextStyleSheet);
            emitter.setRequireWellFormed(false);
            nextStyleSheet.setResult(result);
            result = emitter;
        }

        c.changeOutputDestination(details, result);
        processChildren(context);
        c.resetOutputDestination(oldOutputter);
        try {
            stream.close();
        } catch (java.io.IOException err) {
            throw new TransformerException("Failed to close output file", err);
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
// Additional Contributor(s): Brett Knights [brett@knightsofthenet.com]
//

package com.icl.saxon;

import org.xml.sax.SAXException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMLocator;
import java.io.PrintStream;

/**
  * <B>StandardErrorListener</B> is the standard error handler for XSLT processing
  * errors, used if no other ErrorListener is nominated.
  * @author Michael H. Kay
  */

public class StandardErrorListener implements ErrorListener {

    int recoveryPolicy = Controller.RECOVER_WITH_WARNINGS;
    int warningCount = 0;
    PrintStream errorOutput = System.err;

    /**
    * Set output destination for error messages (default is System.err)
    * @param writer The PrintStream to use for error messages
    */

    public void setErrorOutput(PrintStream writer) {
        errorOutput = writer;
    }

    /**
    * Set the recovery policy
    */

    public void setRecoveryPolicy(int policy) {
        recoveryPolicy = policy;
    }

    /**
     * Receive notification of a warning.
     *
     * <p>Transformers can use this method to report conditions that
     * are not errors or fatal errors.  The default behaviour is to
     * take no action.</p>
     *
     * <p>After invoking this method, the Transformer must continue with
     * the transformation. It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */

    public void warning(TransformerException exception)
        throws TransformerException {

        if (recoveryPolicy==Controller.RECOVER_SILENTLY) {
            // do nothing
            return;
        }

        String message = "";
        if (exception.getLocator()!=null) {
            message = getLocationMessage(exception) + "\n  ";
        }
        message += getExpandedMessage(exception);

        if (recoveryPolicy==Controller.RECOVER_WITH_WARNINGS) {
            errorOutput.println("Recoverable error");
            errorOutput.println(message);
            warningCount++;
            if (warningCount > 25) {
            	System.err.println("No more warnings will be displayed");
            	recoveryPolicy = Controller.RECOVER_SILENTLY;
            	warningCount = 0;
            }
        } else {
            errorOutput.println("Recoverable error");
            errorOutput.println(message);
            errorOutput.println("Processing terminated because error recovery is disabled");
            throw new TransformerException(exception);
        }
    }

    /**
     * Receive notification of a recoverable error.
     *
     * <p>The transformer must continue to provide normal parsing events
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * <p>The action of the standard error listener depends on the
     * recovery policy that has been set, which may be one of RECOVER_SILENTLY,
     * RECOVER_WITH_WARNING, or DO_NOT_RECOVER
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */

    public void error(TransformerException exception) throws TransformerException {
        //new NullPointerException("dd").printStackTrace();
        String message = "Error " +
                         getLocationMessage(exception) +
                         "\n  " +
                         getExpandedMessage(exception);
        errorOutput.println(message);
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>The application must assume that the transformation cannot
     * continue after the Transformer has invoked this method,
     * and should continue (if at all) only to collect
     * addition error messages. In fact, Transformers are free
     * to stop reporting events once this method has been invoked.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax.xml.transform.TransformerException
     */

    public void fatalError(TransformerException exception) throws TransformerException {
        error(exception);
        throw exception;
    }

    /**
    * Get a string identifying the location of an error.
    */

    public static String getLocationMessage(TransformerException err) {
        SourceLocator loc = err.getLocator();
        if (loc==null) {
            return "";
        } else {
            String locmessage = "";
            if (loc instanceof DOMLocator) {
                locmessage += "at " + ((DOMLocator)loc).getOriginatingNode().getNodeName() + " ";
            }
            int line = loc.getLineNumber();
            int column = loc.getColumnNumber();
            if (line<0 && column>0) {
                locmessage += "at byte " + column + " ";
            } else {
                locmessage += "on line " + line + " ";
                if (loc.getColumnNumber() != -1) {
                    locmessage += "column " + column + " ";
                }
            }
            locmessage += "of " + loc.getSystemId() + ":";
            return locmessage;
        }
    }

    /**
    * Get a string containing the message for this exception and all contained exceptions
    */

    public static String getExpandedMessage(TransformerException err) {
        String message = "";
        Throwable e = err;
        while (true) {
            if (e == null) {
                break;
            }
            String next = e.getMessage();
	    if (next==null) next="";
            if (!next.equals("TRaX Transform Exception") && !message.endsWith(next)) {
                if (!message.equals("")) {
                    message += ": ";
                }
                message += e.getMessage();
            }
            if (e instanceof TransformerException) {
                e = ((TransformerException)e).getException();
            } else if (e instanceof SAXException) {
                e = ((SAXException)e).getException();
            } else {
                break;
            }
        }

        return message;
    }
}

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
// Contributor(s):
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//

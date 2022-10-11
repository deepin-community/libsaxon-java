package com.icl.saxon.sql;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.style.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.sql.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
* An sql:close element in the stylesheet.<BR>
*/

public class SQLClose extends StyleElement {

    Expression database;
    Expression driver;
    Expression user;
    Expression password;

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
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws TransformerException {

        // Prepare the SQL statement

		NodeInfo sourceDoc = (context.getCurrentNodeInfo()).getDocumentRoot();
        Connection connection = (Connection)context.getController().getUserData(
                                              sourceDoc, "sql:connection");
        if (connection==null) {
            throw styleError("No SQL connection has been established");
        }

		try {
            connection.close();
	    } catch (SQLException ex) {
			throw styleError("(SQL) Failed to close connection: " + ex.getMessage());
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
// Additional Contributor(s): Rick Bonnett [rbonnett@acadia.net]
//

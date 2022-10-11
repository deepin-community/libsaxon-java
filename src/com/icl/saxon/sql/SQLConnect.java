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
* An sql:connect element in the stylesheet.<BR>
*/

public class SQLConnect extends StyleElement {

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

        // Get mandatory database attribute

        String dbAtt = attributeList.getValue("database");
        if (dbAtt==null)
            reportAbsence("database");
        database = makeAttributeValueTemplate(dbAtt);

	    // Get driver attribute

        String dbDriver = attributeList.getValue("driver");
        if (dbDriver==null) {
            if (dbAtt.substring(0,9).equals("jdbc:odbc")) {
                dbDriver = "sun.jdbc.odbc.JdbcOdbcDriver";
            } else {
                reportAbsence("driver");
            }
        }
        driver = makeAttributeValueTemplate(dbDriver);


        // Get and expand user attribute, which defaults to empty string

        String userAtt = attributeList.getValue("user");
        if (userAtt==null) {
            user = new StringValue("");
        } else {
            user = makeAttributeValueTemplate(userAtt);
        }

        // Get and expand password attribute, which defaults to empty string

        String pwdAtt = attributeList.getValue("password");
        if (pwdAtt==null) {
            password = new StringValue("");
        } else {
            password = makeAttributeValueTemplate(pwdAtt);
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws TransformerException {

        // Establish the JDBC connection

        Connection connection = null;      // JDBC Database Connection
        Statement sql = null;              // JDBC SQL Statement

        String dbString = database.evaluateAsString(context);
	    String dbDriverString = driver.evaluateAsString(context);
        String userString = user.evaluateAsString(context);
        String pwdString = password.evaluateAsString(context);

        try {
            // the following hack is necessary to load JDBC drivers
	        Class.forName(dbDriverString);

            connection = DriverManager.getConnection(dbString, userString, pwdString);
            sql = connection.createStatement();
        } catch (Exception ex) {
            throw new TransformerException("JDBC Connection Failure: " + ex.getMessage());
        }

		NodeInfo sourceDoc = (context.getCurrentNodeInfo()).getDocumentRoot();
        context.getController().setUserData(sourceDoc, "sql:connection", connection);
        context.getController().setUserData(sourceDoc, "sql:statement", sql);

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

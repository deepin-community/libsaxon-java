package com.icl.saxon.sql;
import com.icl.saxon.*;
import com.icl.saxon.style.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;
import java.sql.*;
//import java.util.*;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
/**
* An sql:insert element in the stylesheet.<BR>
*/

public class SQLInsert extends StyleElement {

    String table;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return true: yes, it may contain a template-body (this is done only so that
    * it can contain xsl:fallback)
    */

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

		table = getAttribute("table");
		if (table==null) reportAbsence("table");

    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws TransformerException {

        // Prepare the SQL statement (only do this once)

        Controller controller = context.getController();
		NodeInfo sourceDoc = (context.getCurrentNodeInfo()).getDocumentRoot();
        Connection connection = (Connection)controller.getUserData(
                                              sourceDoc, "sql:connection");
        if (connection==null) {
            throw styleError("No SQL connection has been established");
        }

        PreparedStatement ps =(PreparedStatement)controller.getUserData(
                                              this, "sql:statement");

        try {
            if (ps==null) {

                StringBuffer statement = new StringBuffer();
                statement.append("INSERT INTO " + table + " (");

        		// Collect names of columns to be added

                Node child = getFirstChild();
        		int cols = 0;
        		while (child!=null) {
        		    if (child instanceof SQLColumn) {
            			if (cols++ > 0)	statement.append(',');
            			String colname = ((SQLColumn)child).getColumnName();
            			statement.append(colname);
        		    }
        			child = child.getNextSibling();
        		}
                statement.append(") VALUES (");

                // Add "?" marks for the variable parameters

        		for(int i=0; i<cols; i++) {
        			if (i!=0)
        			    statement.append(',');
        			statement.append('?');
        		};

        		statement.append(')');

                // Prepare the SQL statement

            	ps=connection.prepareStatement(statement.toString());
            	controller.setUserData(this, "sql:statement", ps);
            }

            // Add the actual column values to be inserted

            Node child2 = getFirstChild();
            int i=1;
		    while (child2!=null) {
		        if (child2 instanceof SQLColumn) {

        			// Get the column value: either from the select attribute or from content
        			Value v = ((SQLColumn)child2).getColumnValue(context);

        			// TODO: the values are all strings. There is no way of adding to a numeric column
        		    String val = v.asString();

        		    // another hack: setString() doesn't seem to like single-character string values
        		    if (val.length()==1) val += " ";
        			ps.setObject(i++, val);

		        }

    			// Get the next column and decide whether we've reached the last
    			child2 = child2.getNextSibling();
    		}

			int num = ps.executeUpdate();
			if (!connection.getAutoCommit()) {
                connection.commit();
            }

	    } catch (SQLException ex) {
			throw styleError("(SQL) " + ex.getMessage());
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

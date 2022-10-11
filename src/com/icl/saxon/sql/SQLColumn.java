package com.icl.saxon.sql;
import com.icl.saxon.*;
import com.icl.saxon.style.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.sql.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;


/**
* An sql:insert element in the stylesheet.<BR>
*/

public class SQLColumn extends XSLGeneralVariable {

    /**
    * Determine whether this node is an instruction.
    * @return false - it is not an instruction
    */

    public boolean isInstruction() {
        return false;
    }

    /**
    * Determine whether this type of element is allowed to contain a template-body
    * @return false: yes, it may not contain a template-body
    */

    public boolean mayContainTemplateBody() {
        return false;
    }

    public void validate() throws TransformerConfigurationException {
        if (!(getParentNode() instanceof SQLInsert)) {
            compileError("parent node must be sql:insert");
        }
    }

    public void process( Context context ) {

    }

    public String getColumnName() {
        return getAttributeValue("", "name");
    }

    public Value getColumnValue(Context context) throws TransformerException {
        return getSelectValue(context);
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

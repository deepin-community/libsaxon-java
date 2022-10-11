package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import javax.xml.transform.*;
import java.io.*;

/**
* An xsl:param elements in the stylesheet.<BR>
* The xsl:param element has mandatory attribute name and optional attribute select
*/

public class XSLParam extends XSLGeneralVariable implements Binding {

    private int slotNumber;

    public int getSlotNumber() {
        return slotNumber;
    }

    public void validate() throws TransformerConfigurationException {

        super.validate();

        NodeInfo parent = (NodeInfo)getParentNode();
        boolean local = (parent instanceof XSLTemplate || parent instanceof SAXONFunction);

        if (!local && !global) {
            compileError("xsl:param must be immediately within a template, function or stylesheet");
        }

        checkDuplicateDeclaration();

        if (global && !redundant) {
            slotNumber = getPrincipalStyleSheet().allocateSlotNumber();
        } else {
            Procedure p = getOwningProcedure();
            slotNumber = p.allocateSlotNumber();
        }

        if (!global) {
            NodeInfo preceding = (NodeInfo)getPreviousSibling();
            if (preceding!=null && !(preceding instanceof XSLParam)) {
                compileError("xsl:param must be the first element within a template");
            }
        }

    }

    public void process(Context context) throws TransformerException
    {
        if (redundant) return;

        Bindery bindery = context.getBindery();
        boolean wasSupplied;

        if (global) {
            wasSupplied = bindery.useGlobalParameter(variableFingerprint, this);
        } else {
            wasSupplied = bindery.useLocalParameter(variableFingerprint, this);
        }

        // don't evaluate the default if a value has been supplied or if it has already been
        // evaluated by virtue of a forwards reference

        if (!wasSupplied) {
            if (global) {
                if (!redundant) {
                    Value value = getSelectValue(context);
                    bindery.defineGlobalVariable(this, value);
                }
            } else {
                Value value = getSelectValue(context);
                bindery.defineLocalVariable(this, value);
            }
        }
    }

    /**
    * Get the data type, if known statically.
    * @return Value.ANY, because the data type of a parameter is never known in advance
    */

    public int getDataType() {
        return Value.ANY;
    }

    /**
    * Get the value, if known statically.
    * @return null, because the value of a parameter is never known in advance
    */

    public Value constantValue() {
        return null;
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

package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.sort.*;
import javax.xml.transform.*;
import java.io.*;
import java.util.*;

/**
* An xsl:sort element in the stylesheet.<BR>
*/

public class XSLSort extends StyleElement {

    private SortKeyDefinition sortKeyDefinition;

    public void prepareAttributes() throws TransformerConfigurationException {

        Expression select;
        Expression order;
        Expression dataType;
        Expression caseOrder;
        Expression lang;

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		String selectAtt = null;
        String orderAtt = null;
        String dataTypeAtt = null;
        String caseOrderAtt = null;
        String langAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.SELECT) {
        		selectAtt = atts.getValue(a);
        	} else if (f==sn.ORDER) {
        		orderAtt = atts.getValue(a);
        	} else if (f==sn.DATA_TYPE) {
        		dataTypeAtt = atts.getValue(a);
        	} else if (f==sn.CASE_ORDER) {
        		caseOrderAtt = atts.getValue(a);
        	} else if (f==sn.LANG) {
        		langAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (selectAtt==null) {
            select = new ContextNodeExpression();
        } else {
            select = makeExpression(selectAtt);
        }

        if (orderAtt == null) {
            order = new StringValue("ascending");
        } else {
            order = makeAttributeValueTemplate(orderAtt);
        }

        if (dataTypeAtt == null) {
            dataType = new StringValue("text");
        } else {
            dataType = makeAttributeValueTemplate(dataTypeAtt);
        }

        if (caseOrderAtt == null) {
            caseOrder = new StringValue("#default");
        } else {
            caseOrder = makeAttributeValueTemplate(caseOrderAtt);
        }

        if (langAtt == null) {
            lang = new StringValue(Locale.getDefault().getLanguage());
        } else {
            lang = makeAttributeValueTemplate(langAtt);
        }

        try {
            sortKeyDefinition = new SortKeyDefinition();
            sortKeyDefinition.setSortKey(select);
            sortKeyDefinition.setOrder(order);
            sortKeyDefinition.setDataType(dataType);
            sortKeyDefinition.setCaseOrder(caseOrder);
            sortKeyDefinition.setLanguage(lang);
            sortKeyDefinition.setStaticContext(new ExpressionContext(this));
            sortKeyDefinition.bindComparer();
        } catch (XPathException err) {
            compileError(err);
        }
    }

    public void validate() throws TransformerConfigurationException {
        NodeInfo parent = (NodeInfo)getParentNode();
        if (!((parent instanceof XSLApplyTemplates) ||
                 (parent instanceof XSLForEach) ||
                 (parent instanceof SAXONGroup) // inelegant, since saxon:group is an extension!
                 )) {
            compileError("xsl:sort must be child of xsl:apply-templates or xsl:for-each");
        }
    }

    public void process(Context context) throws TransformerException
    {}

    public SortKeyDefinition getSortKeyDefinition() {
        return sortKeyDefinition;
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

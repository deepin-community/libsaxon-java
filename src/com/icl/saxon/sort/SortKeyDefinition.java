package com.icl.saxon.sort;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;

import com.icl.saxon.expr.*;
import javax.xml.transform.TransformerException;

/**
* A SortKeyDefinition defines one component of a sort key. <BR>
*
* Note that most attributes defining the sort key can be attribute value templates,
* and can therefore vary from one invocation to another. We hold them as expressions,
* but optimise for the case where the attributes are all fixed strings: in this case
* we can use the same Comparer object each time.
*
*/


public class SortKeyDefinition  {

    private Expression sortKey;
    private Expression order;
    private Expression dataType;
    private Expression caseOrder;
    private Expression language;
    private StaticContext staticContext;

    private Comparer comparer = null;

    /**
    * Set the expression used as the sort key
    */

    public void setSortKey(Expression exp) {
        sortKey = exp;
    }

    /**
    * Set the order. This is supplied as an expression which must evaluate to "ascending"
    * or "descending". If the order is fixed, supply e.g. new StringValue("ascending").
    * Default is "ascending".
    */

    public void setOrder(Expression exp) {
        order = exp;
    }

    /**
    * Set the data type. This is supplied as an expression which must evaluate to "text",
    * "number", or a QName. If the data type is fixed, supply e.g. new StringValue("text").
    * Default is "text".
    */

    public void setDataType(Expression exp) {
        dataType = exp;
    }

    /**
    * Set the case order. This is supplied as an expression which must evaluate to "upper-first"
    * or "lower-first" or "#default". If the order is fixed, supply e.g. new StringValue("lower-first").
    * Default is "#default".
    */

    public void setCaseOrder(Expression exp) {
        caseOrder = exp;
    }

    /**
    * Set the language. This is supplied as an expression which evaluate to the language name.
    * If the order is fixed, supply e.g. new StringValue("de").
    * Default is "en".
    */

    public void setLanguage(Expression exp) {
        language = exp;
    }

    /**
    * Set the static context. This is used only for resolving any QName supplied in the data-type
    * property.
    */

    public void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }


    public Expression getSortKey() {
        return sortKey;
    }

    public Expression getOrder() {
        return (order==null ? new StringValue("ascending") : order);
    }

    public Expression getDataType() {
        return (dataType==null ? new StringValue("text") : dataType);
    }

    public Expression getCaseOrder() {
        return (caseOrder==null ? new StringValue("#default") : caseOrder);
    }

    public Expression getLanguage() {
        return (language==null ? new StringValue("en") : language);
    }

    /**
    * If possible, use the same comparer every time
    */

    public void bindComparer() throws XPathException {
        if ((dataType instanceof StringValue) &&
                (order instanceof StringValue) &&
                (caseOrder instanceof StringValue) &&
                (language instanceof StringValue)) {
            comparer = makeComparer(null);
        }
    }

    /**
    * Get a Comparer which can be used to compare two values according to this sort key.
    */

    public Comparer getComparer(Context context) throws XPathException {
        if (comparer==null) {
            return makeComparer(context);
        } else {
            return comparer;
            // note, we can't save it for later use, because the context might be different next time
        }
    }

    /**
    * Create a Comparer which can be used to compare two values according to this sort key.
    */

    private Comparer makeComparer(Context context) throws XPathException {

		Comparer comp;
        boolean isAscending;

        String orderAtt;
        if (order==null) {
            orderAtt="ascending";
        } else {
            orderAtt = order.evaluateAsString(context);
        }

        if (orderAtt.equals("ascending")) {
            isAscending = true;
        } else if (orderAtt.equals("descending")) {
            isAscending = false;
        } else {
            throw new XPathException("order must be ascending or descending");
        }

        String dataTypeURI;
        String dataTypeLocalName;

        String dataTypeAtt;
        if (dataType==null) {
            dataTypeAtt = "text";
        } else {
            dataTypeAtt = dataType.evaluateAsString(context);
        }

        if (dataTypeAtt.equals("text")) {
            dataTypeURI = null;
            dataTypeLocalName = null;
        } else if (dataTypeAtt.equals("number")) {
            dataTypeURI = null;
            dataTypeLocalName = null;
        } else {
        	String prefix = Name.getPrefix(dataTypeAtt);
            if (prefix.equals("")) {
                throw new XPathException("data-type must be text, number, or a prefixed name");
            }

            dataTypeURI = staticContext.getURIForPrefix(prefix);
            dataTypeLocalName = Name.getLocalName(dataTypeAtt);
        }

        int caseOrderValue;
        String caseOrderAtt;
        if (caseOrder==null) {
            caseOrderAtt = "#default";
        } else {
            caseOrderAtt = caseOrder.evaluateAsString(context);
        }

        if (caseOrderAtt.equals("#default")) {
            caseOrderValue = TextComparer.DEFAULT_CASE_ORDER;
        } else if (caseOrderAtt.equals("lower-first")) {
            caseOrderValue = TextComparer.LOWERCASE_FIRST;
        } else if (caseOrderAtt.equals("upper-first")) {
            caseOrderValue = TextComparer.UPPERCASE_FIRST;
        } else {
            throw new XPathException("case-order must be lower-first or upper-first");
        }



        if (dataTypeAtt.equals("text")) {
            if (language==null) {
                comp = new StringComparer();
            } else {
                String langValue = language.evaluateAsString(context);
                String userClassName = "com.icl.saxon.sort.Compare_";
                for (int i=0; i<langValue.length(); i++) {
                    if (Character.isLetter(langValue.charAt(i))) {
                        userClassName += langValue.charAt(i);
                    }
                }
                try {
                    comp = loadComparer(userClassName);
                } catch (Exception err) {
                    //System.err.println("Warning: no comparer " + userClassName + " found; using default");
                    comp = new Compare_en();
                }
            }

        } else if (dataTypeAtt.equals("number")) {
            comp = new DoubleComparer();
        } else {
            try {
                comp = loadComparer(dataTypeLocalName);
            } catch (Exception err) {
                System.err.println("Warning: no comparer " + dataTypeLocalName + " found; using default");
                comp = new StringComparer();
            }
        }

        comp = comp.setDataType(dataTypeURI, dataTypeLocalName);
        comp = comp.setOrder(isAscending);
        if (comp instanceof TextComparer) {
            comp = ((TextComparer)comp).setCaseOrder(caseOrderValue);
        }


        return comp;
    }

    /**
    * Load a named Comparer class and check it is OK.
    */

    private static TextComparer loadComparer (String className) throws XPathException
    {
        try {
            return (TextComparer)Loader.getInstance(className);
        } catch (ClassCastException e) {
            throw new XPathException("Failed to load TextComparer  " + className +
                            ": it does not implement the TextComparer interface");
        } catch (TransformerException err) {
            if (err instanceof XPathException) {
                throw (XPathException)err;
            } else {
                throw new XPathException(err);
            }
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

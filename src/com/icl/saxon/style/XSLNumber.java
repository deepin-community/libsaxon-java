package com.icl.saxon.style;
import com.icl.saxon.tree.AttributeCollection;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.number.*;
import com.icl.saxon.functions.Round;
import javax.xml.transform.*;
import java.util.*;

/**
* An xsl:number element in the stylesheet.<BR>
*/

public class XSLNumber extends StyleElement {

    private final static int SINGLE = 0;
    private final static int MULTI = 1;
    private final static int ANY = 2;
    private final static int SIMPLE = 3;

    private int level;
    private Pattern count = null;
    private Pattern from = null;
    private Expression expr = null;
    private Expression format = null;
    private Expression groupSize = null;
    private Expression groupSeparator = null;
    private Expression letterValue = null;
    private Expression lang = null;
    private NumberFormatter formatter = null;
    private Numberer numberer = null;

    private static Numberer defaultNumberer = new Numberer_en();

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws TransformerConfigurationException {

		StandardNames sn = getStandardNames();
		AttributeCollection atts = getAttributeList();

		String valueAtt = null;
		String countAtt = null;
		String fromAtt = null;
		String levelAtt = null;
		String formatAtt = null;
		String gsizeAtt = null;
		String gsepAtt = null;
		String langAtt = null;
		String letterValueAtt = null;

		for (int a=0; a<atts.getLength(); a++) {
			int nc = atts.getNameCode(a);
			int f = nc & 0xfffff;
			if (f==sn.VALUE) {
        		valueAtt = atts.getValue(a);
        	} else if (f==sn.COUNT) {
        		countAtt = atts.getValue(a);
        	} else if (f==sn.FROM) {
        		fromAtt = atts.getValue(a);
        	} else if (f==sn.LEVEL) {
        		levelAtt = atts.getValue(a);
        	} else if (f==sn.FORMAT) {
        		formatAtt = atts.getValue(a);
        	} else if (f==sn.LANG) {
        		langAtt = atts.getValue(a);
        	} else if (f==sn.LETTER_VALUE) {
        		letterValueAtt = atts.getValue(a);
        	} else if (f==sn.GROUPING_SIZE) {
        		gsizeAtt = atts.getValue(a);
        	} else if (f==sn.GROUPING_SEPARATOR) {
        		gsepAtt = atts.getValue(a);
        	} else {
        		checkUnknownAttribute(nc);
        	}
        }

        if (valueAtt!=null) {
            expr = makeExpression(valueAtt);
        }

        if (countAtt!=null) {
            count = makePattern(countAtt);
        }

        if (fromAtt!=null) {
            from = makePattern(fromAtt);
        }

        if (levelAtt==null) {
            level = SINGLE;
        } else if (levelAtt.equals("single")) {
            level = SINGLE;
        } else if (levelAtt.equals("multiple")) {
            level = MULTI;
        } else if (levelAtt.equals("any")) {
            level = ANY;
        } else {
            compileError("Invalid value for level attribute");
        }

        if (level==SINGLE && from==null && count==null) {
            level=SIMPLE;
        }

        if (formatAtt != null) {
            format = makeAttributeValueTemplate(formatAtt);
            if (format instanceof StringValue) {
                formatter = new NumberFormatter();
                formatter.prepare(((StringValue)format).asString());
            }
            // else we'll need to allocate the formatter at run-time
        } else {
            formatter = new NumberFormatter();
            formatter.prepare("1");
        }

        if (gsepAtt!=null && gsizeAtt!=null) {
            // the spec says that if only one is specified, it is ignored
            groupSize = makeAttributeValueTemplate(gsizeAtt);
            groupSeparator = makeAttributeValueTemplate(gsepAtt);
        }

        if (langAtt==null) {
            numberer = defaultNumberer;
        } else {
            lang = makeAttributeValueTemplate(langAtt);
            if (lang instanceof StringValue) {
                numberer = makeNumberer(((StringValue)lang).asString());
            }   // else we allocate a numberer at run-time
        }

        if (letterValueAtt != null) {
            letterValue = makeAttributeValueTemplate(letterValueAtt);
        }

    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
        checkEmpty();
    }

    public void process(Context context) throws TransformerException
    {
        NodeInfo source = context.getCurrentNodeInfo();
        int value = -1;
        Vector vec = null;

        if (expr!=null) {
        	double d = expr.evaluateAsNumber(context);
        	if (d < 0.5 || Double.isNaN(d) || Double.isInfinite(d) ||
        	                d > Integer.MAX_VALUE) {
				context.getOutputter().writeContent(new NumericValue(d).asString());
				return;
			} else {
            	value = (int)Round.round(d);
            }

        } else {

            if (level==SIMPLE) {
                value = Navigator.getNumberSimple(source, context);
            } else if (level==SINGLE) {
                value = Navigator.getNumberSingle(source, count, from, context);
                if (value==0) {
                	vec = new Vector(); 	// an empty list
                }
            } else if (level==ANY) {
                value = Navigator.getNumberAny(source, count, from, context);
                if (value==0) {
                	vec = new Vector(); 	// an empty list
                }
            } else if (level==MULTI) {
                vec = Navigator.getNumberMulti(source, count, from, context);
            }
        }

        int gpsize = 0;
        String gpseparator = "";
        String language;
        String letterVal;

        if (groupSize!=null) {
            String g = groupSize.evaluateAsString(context);
            try {
                gpsize = Integer.parseInt(g);
            } catch (NumberFormatException err) {
                throw styleError("group-size must be numeric");
            }
        }

        if (groupSeparator!=null) {
            gpseparator = groupSeparator.evaluateAsString(context);
        }

        // fast path for the simple case

        if (vec==null && format==null && gpsize==0 && lang==null) {
            context.getOutputter().writeContent("" + value);
            return;
        }

        if (numberer==null) {
            numberer = makeNumberer(lang.evaluateAsString(context));
        }

        if (letterValue==null) {
            letterVal = "";
        } else {
            letterVal = letterValue.evaluateAsString(context);
            if (!(letterVal.equals("alphabetic") || letterVal.equals("traditional"))) {
                throw styleError("letter-value must be \"traditional\" or \"alphabetic\"");
            }
        }

        if (vec==null) {
            vec = new Vector();
            vec.addElement(new Integer(value));
        }

        NumberFormatter nf;
        if (formatter==null) {              // format not known until run-time
            nf = new NumberFormatter();
            nf.prepare(format.evaluateAsString(context));
        } else {
            nf = formatter;
        }

        String s = nf.format(vec, gpsize, gpseparator, letterVal, numberer);
        context.getOutputter().writeContent(s);
    }

    /**
    * Load a Numberer class for a given language and check it is OK.
    */

    protected static Numberer makeNumberer (String language) //throws SAXException
    {
        Numberer numberer;
        if (language.equals("en")) {
            numberer = defaultNumberer;
        } else {
            String langClassName = "com.icl.saxon.number.Numberer_";
            for (int i=0; i<language.length(); i++) {
                if (Character.isLetter(language.charAt(i))) {
                    langClassName += language.charAt(i);
                }
            }
            try {
                numberer = (Numberer)(Loader.getInstance(langClassName));
            } catch (Exception err) {
                numberer = defaultNumberer;
            }
        }

        return numberer;
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

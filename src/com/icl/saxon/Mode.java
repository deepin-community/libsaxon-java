package com.icl.saxon;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.pattern.NoNodeTest;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Navigator;
import java.util.*;
import javax.xml.transform.TransformerException;
import com.icl.saxon.expr.XPathException;

    /**
    * A Mode is a collection of rules; the selection of a rule to apply to a given element
    * is determined by a Pattern.
    *
    * @author <A HREF="mhkay@iclway.co.uk>Michael H. Kay</A>
    */

public class Mode {
    private Rule[] ruleDict = new Rule[101 + NodeInfo.NUMBER_OF_TYPES];
    private int nameCode = -1;	// identifies the name of this mode
    private int sequence = 0;   // records sequence in which rules were added


    public Mode() {
        //for (int i=0; i<ruleDict.length; i++) {
        //    ruleDict[i] = null;
        //}
    }

	/**
	* Set the name of this mode (for tracing output)
	*/

	public void setNameCode(int nameCode) {
		this.nameCode = nameCode;
	}

	/**
	* Get the name of this mode (for tracing output)
	*/

	public int getNameCode() {
		return nameCode;
	}


    /**
    * Add a rule to the Mode. <br>
    * The rule effectively replaces any other rule for the same pattern/mode at the same or a lower
    * priority.
    * @param p a Pattern
    * @param obj the Object to return from getRule() when the supplied element matches this Pattern
    */

    public void addRule(Pattern p, Object obj, int precedence, double priority) {

        // System.err.println("Add rule, pattern = " + p.toString() + " class " + p.getClass() + ", priority=" + priority);

		// Ignore a pattern that will never match, e.g. "@comment"

		if (p instanceof NoNodeTest) {
			return;
		}

        // for fast lookup, we maintain one list for each element name for patterns that can only
        // match elements of a given name, one list for each node type for patterns that can only
        // match one kind of non-element node, and one generic list.
        // Each list is sorted in precedence/priority order so we find the highest-priority rule first

        int fingerprint = p.getFingerprint();
        short type = p.getNodeType();

        int key = getList(fingerprint, type);
        // System.err.println("Fingerprint " + fingerprint + " key " + key + " type " + type);

		Rule newRule = new Rule(p, obj, precedence, priority, sequence++);

        Rule rule = ruleDict[key];
        if (rule==null) {
            ruleDict[key] = newRule;
            return;
        }

        // insert the new rule into this list before others of the same precedence/priority

        Rule prev = null;
        while (rule != null) {
            if ((rule.precedence < precedence) ||
                	( rule.precedence == precedence && rule.priority <= priority)) {
                newRule.next = rule;
                if (prev==null) {
                	ruleDict[key] = newRule;
                } else {
                	prev.next = newRule;
                }
                break;
            } else {
            	prev = rule;
            	rule = rule.next;
            }
        }
        if (rule==null) {
        	prev.next = newRule;
        	newRule.next = null;
        }
    }

    /**
    * Determine which list to use for a given pattern (we must also search the generic list)
    */

    public int getList(int fingerprint, int type) {

        if (type==NodeInfo.ELEMENT) {
            if (fingerprint==-1) {
                return NodeInfo.NODE;   // the generic list
            } else {
                return NodeInfo.NUMBER_OF_TYPES +
                	(fingerprint % 101);
            }
        } else {
            return type;
        }
    }

    /**
    * Get the rule corresponding to a given Node, by finding the best Pattern match.
    * @param node the NodeInfo referring to the node to be matched
    * @return the object (e.g. a NodeHandler) registered for that element, if any (otherwise null).
    */

    public Object getRule(NodeInfo node, Context context) throws TransformerException {
    	// System.err.println("Get rule for " + Navigator.getPath(node));
        int fingerprint = node.getFingerprint();
        int type = node.getNodeType();
        int key = getList(fingerprint, type);
        int policy = context.getController().getRecoveryPolicy();

        Rule specificRule = null;
        Rule generalRule = null;
        int specificPrecedence = -1;
        double specificPriority = Double.NEGATIVE_INFINITY;

        // search the specific list for this node type / node name

		// System.err.println("Hash key = " + key);

        if (key!=NodeInfo.NODE) {
            Rule r = ruleDict[key];
            while(r!=null) {
            	// if we already have a match, and the precedence or priority of this
            	// rule is lower, quit the search for a second match
            	if (specificRule != null) {
            		if (r.precedence < specificPrecedence ||
            		     (r.precedence==specificPrecedence && r.priority < specificPriority)) {
            			break;
            		}
            	}
            	//System.err.println("Testing " + Navigator.getPath(node) + " against " + r.pattern);
                if (r.pattern.matches(node, context)) {
                	//System.err.println("Matches");

                    // is this a second match?
                    if (specificRule != null) {
                        if (r.precedence==specificPrecedence && r.priority==specificPriority) {
                            reportAmbiguity(node, specificRule.pattern, r.pattern, context);
                        }
                        break;
                    }
                    specificRule = r;
                    specificPrecedence = r.precedence;
                    specificPriority = r.priority;
                    if (policy==Controller.RECOVER_SILENTLY) {
                        break;                      // find the first; they are in priority order
                    }
                }
                r = r.next;
            }
        }

        // search the general list

        Rule r2 = ruleDict[NodeInfo.NODE];
        while (r2 != null) {
            if (r2.precedence < specificPrecedence ||
                 (r2.precedence == specificPrecedence && r2.priority < specificPriority)) {
                break;      // no point in looking at a lower priority rule than the one we've got
            }
            if (r2.pattern.matches(node, context)) {
                // is it a second match?
                if (generalRule != null) {
                    if (r2.precedence == generalRule.precedence && r2.priority ==generalRule.priority) {
                        reportAmbiguity(node, r2.pattern, generalRule.pattern, context);
                    }
                    break;
                } else {
                    generalRule = r2;
                    if (policy==Controller.RECOVER_SILENTLY) {
                        break;                      // find only the first; they are in priority order
                    }
                }
            }
            r2 = r2.next;
        }

        if (specificRule!=null && generalRule==null)
            return specificRule.object;
        if (specificRule==null && generalRule!=null)
            return generalRule.object;
        if (specificRule!=null && generalRule!=null) {
            if (specificRule.precedence == generalRule.precedence &&
                specificRule.priority == generalRule.priority ) {
                // This situation is exceptional: we have a "specific" pattern and
                // a "general" pattern with the same priority. We have to select
                // the one that was added last
                // (Bug reported by Norman Walsh Jan 2002)
                Object result = (specificRule.sequence > generalRule.sequence ?
                                    specificRule.object :
                                    generalRule.object);

                if (policy!=Controller.RECOVER_SILENTLY) {
                    reportAmbiguity(node, specificRule.pattern, generalRule.pattern, context);
                }
                return result;
            }
            if (specificRule.precedence > generalRule.precedence ||
                 (specificRule.precedence == generalRule.precedence &&
                    specificRule.priority >= generalRule.priority)) {
                return specificRule.object;
            } else {
                return generalRule.object;
            }
        }
        return null;
    }

    /**
    * Get the rule corresponding to a given Node, by finding the best Pattern match, subject to a minimum
    * and maximum precedence. (This supports xsl:apply-imports)
    * @param node the NodeInfo referring to the node to be matched
    * @return the object (e.g. a NodeHandler) registered for that element, if any (otherwise null).
    */

    public Object getRule(NodeInfo node, int min, int max, Context context) throws XPathException {
        int fing = node.getFingerprint();
        int type = node.getNodeType();
        int key = getList(fing, type);

        Rule specificRule = null;
        Rule generalRule = null;

        // search the the specific list for this node type / name

        if (key!=NodeInfo.NODE) {
            Rule r = ruleDict[key];
            while (r!=null) {
                if (r.precedence >= min && r.precedence <= max &&
                         r.pattern.matches(node, context)) {
                    specificRule = r;
                    break;                      // find the first; they are in priority order
                }
                r = r.next;
            }
        }

        // search the generic list

        Rule r2 = ruleDict[NodeInfo.NODE];
        while (r2!=null) {
            if (r2.precedence >= min && r2.precedence <= max && r2.pattern.matches(node, context)) {
                generalRule = r2;
                break;                      // find only the first; they are in priority order
            }
            r2 = r2.next;
        }
        if (specificRule!=null && generalRule==null)
            return specificRule.object;
        if (specificRule==null && generalRule!=null)
            return generalRule.object;
        if (specificRule!=null && generalRule!=null) {
            if (specificRule.precedence > generalRule.precedence ||
                 (specificRule.precedence == generalRule.precedence &&
                    specificRule.priority >= generalRule.priority)) {
                return specificRule.object;
            } else {
                return generalRule.object;
            }
        }
        return null;
    }

    /**
    * Report an ambiguity
    */

    private void reportAmbiguity(NodeInfo node, Pattern pat1, Pattern pat2, Context c)
        throws TransformerException
    {
    	// don't report an error if the conflict is between two branches of the same
    	// Union pattern
    	if (pat1.getStaticContext()==pat2.getStaticContext()) {
    		return;
    	}
        c.getController().reportRecoverableError(
            "Ambiguous rule match for " + Navigator.getPath(node) + "\n" +
            "Matches both \"" + pat1 + "\" on line " + pat1.getLineNumber() + " of " + pat1.getSystemId() +
            "\nand \"" + pat2 + "\" on line " + pat2.getLineNumber() + " of " + pat2.getSystemId(), null);

    }


    /**
    * Inner class Rule used to support the implementation
    */

    private static class Rule {
        public Pattern pattern;
        public Object object;
        public int precedence;
        public double priority;
        public int sequence;
        public Rule next;

        public Rule( Pattern p, Object o, int prec, double prio, int seq ) {
            pattern = p;
            object = o;
            precedence = prec;
            priority = prio;
            sequence = seq;
            next = null;
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

package com.icl.saxon;

import com.icl.saxon.om.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.pattern.AnyNodeTest;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.expr.NodeSetExtent;
import com.icl.saxon.om.Axis;
import com.icl.saxon.om.SingletonEnumeration;
import com.icl.saxon.om.EmptyEnumeration;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.sort.LocalOrderComparer;

import java.util.*;

/**
  * KeyManager manages the set of key definitions in a stylesheet, and the indexes
  * associated with these key definitions
  * @author Michael H. Kay
  */

public class KeyManager {

    private Hashtable keyList;      // one entry for each named key; the entry contains
                                    // a list of key definitions with that name

    /**
    * create a KeyManager and initialise variables
    */

    public KeyManager() {
        keyList = new Hashtable();
    }

    /**
    * Register a key definition. Note that multiple key definitions with the same name are
    * allowed
    * @param keyDefinition The details of the key's definition
    */

    public void setKeyDefinition(KeyDefinition keydef) {
        Integer keykey = new Integer(keydef.getFingerprint());
        Vector v = (Vector)keyList.get(keykey);
        if (v==null) {
            v = new Vector();
            keyList.put(keykey, v);
        }
        v.addElement(keydef);
    }

    /**
    * Get all the key definitions that match a particular fingerprint
    * @param fingerprint The fingerprint of the name of the required key
    * @return The key definition of the named key if there is one, or null otherwise.
    */

    public Vector getKeyDefinitions(int fingerprint) {
        return (Vector)keyList.get(new Integer(fingerprint));
    }

    /**
    * Build the index for a particular document for a named key
    * @param fingerprint The fingerprint of the name of the required key
    * @param doc The source document in question
    * @param controller The controller
    * @return the index in question, as a Hashtable mapping a key value onto a Vector of nodes
    */

    private synchronized Hashtable buildIndex(int fingerprint,
                                           DocumentInfo doc,
                                           Controller controller) throws XPathException {

        Vector definitions = getKeyDefinitions(fingerprint);
        if (definitions==null) {
            throw new XPathException("Key " +
            		controller.getNamePool().getDisplayName(fingerprint) +
            							" has not been defined");
        }

        Hashtable index = new Hashtable();

        for (int k=0; k<definitions.size(); k++) {
            constructIndex(doc, index, (KeyDefinition)definitions.elementAt(k),
             controller, k==0);
        }

        return index;

    }

    /**
    * Process one key definition to add entries to an index
    */

    private void constructIndex(    DocumentInfo doc,
                                    Hashtable index,
                                    KeyDefinition keydef,
                                    Controller controller,
                                    boolean isFirst) throws XPathException {

        Pattern match = keydef.getMatch();
        Expression use = keydef.getUse();
        NodeInfo sourceRoot = doc;
        NodeInfo curr = sourceRoot;
        Context c = controller.makeContext(doc);

        short type = match.getNodeType();

        NodeEnumeration all =
            doc.getEnumeration( Axis.DESCENDANT,
                                AnyNodeTest.getInstance());


        if (type==NodeInfo.ATTRIBUTE || type==NodeInfo.NODE) {
            while(all.hasMoreElements()) {
                curr = all.nextElement();
                if (curr.getNodeType()==NodeInfo.ELEMENT) {
                    NodeEnumeration atts =
                        curr.getEnumeration(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
                    while (atts.hasMoreElements()) {
                        processKeyNode(atts.nextElement(), match, use, index, c, isFirst);
                    }
                    if (type==NodeInfo.NODE) {
                        // index the element as well (bug 6.3/001)
                        processKeyNode(curr, match, use, index, c, isFirst);
                    }
                } else {
                    processKeyNode(curr, match, use, index, c, isFirst);
                }
            }

        } else {
            while(all.hasMoreElements()) {
                curr = all.nextElement();
                processKeyNode(curr, match, use, index, c, isFirst);
            }
        }
    }

    /**
    * Process one node, adding it to the index if appropriate
    */

    private void processKeyNode(NodeInfo curr, Pattern match, Expression use,
                                Hashtable index, Context c, boolean isFirst) throws XPathException {
        if (match.matches(curr, c)) {
            c.setContextNode(curr);
            c.setCurrentNode(curr);
            c.setPosition(1);
            c.setLast(1);
            Value useval = use.evaluate(c);
            if (useval instanceof NodeSetValue) {
                //c.setContextNode(curr);
            	NodeEnumeration enm = ((NodeSetValue)useval).enumerate();
                while (enm.hasMoreElements()) {
                    NodeInfo node = (NodeInfo)enm.nextElement();
                    String val = node.getStringValue();
                    NodeSetExtent nodes = (NodeSetExtent)index.get(val);
                    if (nodes==null) {
                        nodes = new NodeSetExtent(LocalOrderComparer.getInstance());
                        nodes.setSorted(true);
                        index.put(val, nodes);
                    }
                    nodes.append(curr);
                    if (!isFirst) {
                        // when adding nodes for a second key definition, we
                        // need to keep the list sorted. This is very inefficient,
                        // but hopefully not done too often.
                        nodes.setSorted(false);
                        nodes.sort();
                    }
                }
            } else {
                String val = useval.asString();
	            c.setContextNode(curr);
                NodeSetExtent nodes = (NodeSetExtent)index.get(val);
                if (nodes==null) {
                    nodes = new NodeSetExtent(LocalOrderComparer.getInstance());
                    nodes.setSorted(true);
                    index.put(val, nodes);
                }
                nodes.append(curr);
                if (!isFirst) {
                    // when adding nodes for a second key definition, we
                    // need to keep the list sorted. This is very inefficient,
                    // but hopefully not done too often.
                    nodes.setSorted(false);
                    nodes.sort();
                }
            }
        }
    }

    /**
    * Get the nodes with a given key value
    * @param fingerprint The fingerprint of the name of the required key
    * @param doc The source document in question
    * @param value The required key value
    * @param controller The controller, needed only the first time when the key is being built
    * @return an enumeration of nodes, always in document order
    */

    public NodeEnumeration selectByKey(  int fingerprint,
                                DocumentInfo doc,
                                String value,
                                Controller controller) throws XPathException {

        Hashtable index = doc.getKeyIndex(this, fingerprint);
        if (index==null) {
            index = buildIndex(fingerprint, doc, controller);
            doc.setKeyIndex(this, fingerprint, index);
        }
        NodeSetExtent nodes = (NodeSetExtent)index.get(value);
        return (nodes==null ? EmptyEnumeration.getInstance() : nodes.enumerate());
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

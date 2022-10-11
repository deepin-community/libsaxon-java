package com.icl.saxon;
import com.icl.saxon.expr.*;
import com.icl.saxon.functions.SystemProperty;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.style.XSLTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.xsl.XSLTContext;

import javax.xml.transform.TransformerException;
import java.util.Stack;


/**
* This class represents a context in which an expression is evaluated or a template is executed
* (as defined in the XSLT specification). It also provides a range of services to node handlers,
* for example access to the outputter and bindery, and the applyTemplates() function.
*/

public final class Context implements XSLTContext, LastPositionFinder {

    // Define the different kinds of context-dependency in an expression

    public static final int VARIABLES = 1;          // Expression depends on values of variables
    public static final int CURRENT_NODE = 4;       // Expression depends on current() node
    public static final int CONTEXT_NODE = 8;       // Expression depends on context node
    public static final int POSITION = 16;          // Expression depends on position()
    public static final int LAST = 32;              // Expression depends on last()
    public static final int CONTROLLER = 64;        // Expression evaluation needs the Controller
    public static final int CONTEXT_DOCUMENT = 128; // Expression depends on the document
                                                    //  containing the context node
    public static final int NO_DEPENDENCIES = 0;
    public static final int ALL_DEPENDENCIES = 255;
    public static final int XSLT_CONTEXT = CONTROLLER | VARIABLES | CURRENT_NODE;

    private NodeInfo contextNode;
    private NodeInfo currentNode;
    private int position = -1;
    private int last = -1;
    private LastPositionFinder lastPositionFinder;
    private Controller controller;
    //private Bindery bindery;
    private Mode currentMode;
    private XSLTemplate currentTemplate;
    private Stack groupActivationStack;     // holds stack of active saxon:group activations
    private StaticContext staticContext;
    private ParameterSet tailRecursion;     // set when a tail-recursive call is requested
    private NodeInfo lastRememberedNode = null;
    private int lastRememberedNumber = -1;
    private Value returnValue = null;
    private XPathException exception = null;

    private static Controller defaultController = null;

    /**
    * The default constructor is not used within Saxon itself, but is available to
    * applications (and is used in some samples). Because some expressions (for example
    * union expressions) cannot execute without a Controller, a system default Controller
    * is created. This is a quick fix, but is not entirely satisfactory, because it's
    * not thread-safe. Applications are encouraged to create a Controller explicitly and
    * use it only within a single thread.
    */

    public Context() {
        if (defaultController==null) {
            defaultController = new Controller();
        }
        controller = defaultController;
        lastPositionFinder = this;
    }


    /**
    * Constructor should only be called by the Controller, which acts as a Context factory.
    */

    public Context(Controller c) {
        controller = c;
        lastPositionFinder = this;
    }

    /**
    * Construct a new context as a copy of another
    */

    public Context newContext() {
        Context c = new Context(controller);
        c.staticContext = staticContext;
        c.currentNode = currentNode;
        c.contextNode = contextNode;
        c.position = position;
        c.last = last;
        c.lastPositionFinder = lastPositionFinder;
        c.currentMode = currentMode;
        c.currentTemplate = currentTemplate;
        //c.bindery = bindery;
        c.groupActivationStack = groupActivationStack;
        c.lastRememberedNode = lastRememberedNode;
        c.lastRememberedNumber = lastRememberedNumber;
        c.returnValue = null;
        return c;
    }

    /**
    * Set the controller for this Context
    */

    public void setController(Controller c) {
        controller = c;
    }

    /**
    * Get the controller for this Context
    */

    public Controller getController() {
        return controller;
    }

    /**
    * Get the Bindery used by this Context
    */

    public Bindery getBindery() {
        return controller.getBindery();
    }

    /**
    * Get the current Outputter. This gives access to the writeStartTag, writeAttribute,
    * and writeEndTag methods
    * @return the current Outputter
    */

    public Outputter getOutputter() {
        return controller.getOutputter();
    }

    /**
    * Set the mode (for use by the built-in handlers)
    */

    public void setMode(Mode mode) {
        currentMode = mode;
    }

    /**
    * Get the current mode (for use by the built-in handlers)
    */

    public Mode getMode() {
        return currentMode;
    }

    /**
    * Set the context node. <br>
    * Note that this has no effect on position() or last(), which must be set separately.
    * @param node the node that is to be the context node.
    */

    public void setContextNode(NodeInfo node) {
        this.contextNode = node;
    }

    /**
    * Get the context node
    * @return the context node
    */

    public NodeInfo getContextNodeInfo() {
        return contextNode;
    }

    /**
    * Get the context node, provided it is a DOM Node
    * @return the context node if it is a DOM Node, otherwise null
    */

    public Node getContextNode() {
        if (contextNode instanceof Node) {
            return (Node)contextNode;
        } else {
            return null;
        }
    }

    /**
    * Set the context position
    */

    public void setPosition(int pos) {
        position = pos;
    }

    /**
    * Get the context position (the position of the context node in the context node list)
    * @return the context position (starting at one)
    */

    public int getContextPosition() {
        return position;
    }

    /**
    * Set the context size; this also makes the Context object responisble for returning the last()
    * position.
    */

    public void setLast(int last) {
        this.last = last;
        lastPositionFinder = this;
    }

    /**
    * Set the LastPositionFinder, another object that will do the work of returning the last()
    * position
    */

    public void setLastPositionFinder(LastPositionFinder finder) {
        lastPositionFinder = finder;
    }

    /**
    * Get the context size (the position of the last item in the current node list)
    * @return the context size
    */

    public int getLast() throws XPathException {
        if (lastPositionFinder==null) return 1;     // fallback, shouldn't happen
        return lastPositionFinder.getLastPosition();
    }

    /**
    * Determine whether the context position is the same as the context size
    * that is, whether position()=last()
    */

    public boolean isAtLast() throws XPathException {
        // The code is designed to answer the question without searching to the end
        // of the node-set to determine the last position, wherever possible
        if (lastPositionFinder!=null && lastPositionFinder instanceof NodeEnumeration) {
            return !((NodeEnumeration)lastPositionFinder).hasMoreElements();
        } else {
            return getContextPosition() == getLast();
        }
    }

    /**
    * Get the context size (the position of the last item in the current node list).
    * This is the XSLTContext method: it differs from getLast() in that it cannot throw
    * an exception.
    * This method should be called only from within extension functions. If any error occurs,
    * it will be accessible via Context#getException().
    * @return the context size
    */

    public int getContextSize() {
        try {
            return getLast();
        } catch (XPathException err) {
            // The XSLTContext interfaces doesn't allow us to throw any exceptions.
            // We'll pick it up on return from the extension function.
            setException(err);
            return getContextPosition();    // for want of anything better
        }
    }

    /**
    * Get the last position, to be used only
    * when the context object is being used as the last position finder
    */

    public int getLastPosition() {
        return last;
    }

    /**
    * Set the current node. This is the node in the source document currently being processed
    * (e.g. by apply-templates).
    */

    public void setCurrentNode(NodeInfo node) {
        currentNode = node;
    }

    /**
    * Get the current node. This is the node in the source document currently being processed
    * (e.g. by apply-templates). It is not necessarily the same as the context node: the context
    * node can change in a sub-expression, the current node cannot.
    */

    public NodeInfo getCurrentNodeInfo() {
        return currentNode;
    }

    /**
    * Get the current node,provided it is a DOM Node.
    * This is the node in the source document currently being processed
    * (e.g. by apply-templates). It is not necessarily the same as the context node: the context
    * node can change in a sub-expression, the current node cannot.
    * @return the current node if it is a DOM Node; otherwise null.
    */

    public Node getCurrentNode() {
        if (currentNode instanceof Node) {
            return (Node)currentNode;
        } else {
            return null;
        }
    }

    /**
    * Set the current template. This is used to support xsl:apply-imports
    */

    public void setCurrentTemplate(XSLTemplate template) {
        currentTemplate = template;
    }

    /**
    * Get the current template. This is used to support xsl:apply-imports
    */

    public XSLTemplate getCurrentTemplate() {
        return currentTemplate;
    }

    /**
    * Get owner Document (enabling extension functions to create new Nodes)
    */

    public Document getOwnerDocument() {
        return (Document)(Node)contextNode.getDocumentRoot();
    }

    /**
    * Get the value of a system property
    */

    public Object systemProperty(String namespaceURI, String localName) {
        try {
            Value prop = SystemProperty.getProperty(namespaceURI, localName);
            if (prop==null) {
                return null;
            } else if (prop instanceof StringValue) {
                return prop.asString();
            } else if (prop instanceof NumericValue) {
                return new Double(prop.asNumber());
            } else if (prop instanceof BooleanValue) {
                return new Boolean(prop.asBoolean());
            } else {
                return prop;
            }
        } catch (Exception err) {
            return null;
        }
    }

    /**
    * Return the String value of a node
    * @throws IllegalArgumentException if it is not a Saxon node
    */

    public String stringValue(Node n) {
        if (n instanceof NodeInfo) {
            return ((NodeInfo)n).getStringValue();
        } else {
            throw new IllegalArgumentException("Node is not a Saxon node");
        }
    }

    /**
    * Set the static context
    */

    public void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }

    /**
    * Get the static context. This is currently available only while processing an
    * extension function
    */

    public StaticContext getStaticContext() {
        return staticContext;
    }

    /**
    * Set an exception value. This is useful when an extension function makes a call
    * such as getContextSize() that causes an error. The error is saved as part of the
    * context, and reported on return from the extension function
    */

    public void setException(XPathException err) {
        exception = err;
    }

    /**
    * Get the saved exception value.
    */

    public XPathException getException() {
        return exception;
    }

    /**
    * Get the saxon:group activation stack
    */

    public Stack getGroupActivationStack() {
        if (groupActivationStack==null) {
            groupActivationStack = new Stack();
        }
        return groupActivationStack;
    }

    /**
    * Set the last remembered node, for node numbering purposes
    */

    public void setRememberedNumber(NodeInfo node, int number) {
        lastRememberedNode = node;
        lastRememberedNumber = number;
    }

    /**
    * Get the number of a node if it is the last remembered one.
    * @return the number of this node if known, else -1.
    */

    public int getRememberedNumber(NodeInfo node) {
        if (lastRememberedNode == node) return lastRememberedNumber;
        return -1;
    }

    /**
    * Set tail recursion parameters
    */

    public void setTailRecursion(ParameterSet p) {
        tailRecursion = p;
    }

    /**
    * Get tail recursion parameters
    */

    public ParameterSet getTailRecursion() {
        return tailRecursion;
    }

    /**
    * Set return value from function
    */

    public void setReturnValue(Value value) throws TransformerException {
        if (value != null && returnValue != null) {
            throw new TransformerException("A function can only return one result");
        }
        returnValue = value;
    }

    /**
    * Get the return value from function
    */

    public Value getReturnValue() {
        return returnValue;
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

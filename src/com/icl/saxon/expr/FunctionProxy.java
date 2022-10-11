package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.xsl.XSLTContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;



/**
* This class acts as a proxy for an extension function defined as a method
* in a user-defined class
*/

public class FunctionProxy extends Function {

    private Class theClass;
    private Vector candidateMethods = new Vector();
    private XPathException theException = null;
    private String name;
    private Class resultClass = null;

    /**
    * Constructor: creates an uncommitted FunctionProxy
    */

    public FunctionProxy() {}

    /**
    * setFunctionName: locates the external class and the method (or candidate methods)
    * to which this function relates. At this
    * stage addArguments() will have normally been called, so the number of arguments is known. If no
    * method of the required name is located, an exception is saved, but it is not thrown until
    * an attempt is made to evaluate the function. All methods that match the required number of
    * arguments are saved in a list (candidateMethods), a decision among these methods is made
    * at run-time when the types of the actual arguments are known.<p>
    * The method is also used while calling
    * function-available(). In this case the number of arguments is not known (it will be
    * set to zero). We return true if a match was found, regardless of the number of arguments.
    * @param reqClass The Java class name
    * @param localName The local name used in the XPath function call
	* @return true if the function is available (with some number of arguments).
    */

    public boolean setFunctionName(Class reqClass, String localName) {
    	boolean isAvailable = false;
    	// System.err.println("FunctionProxy.setMethod(" + reqClass + ":" + localName);

        name = localName;
        int numArgs = getNumberOfArguments();
        int significantArgs = numArgs;

        theClass = reqClass;

        // if the method name is "new", look for a matching constructor

        if (name.equals("new")) {

            int mod = theClass.getModifiers();
            if (Modifier.isAbstract(mod)) {
                theException = new XPathException("Class " + theClass + " is abstract");
                return false;
            }
            if (Modifier.isInterface(mod)) {
                theException = new XPathException(theClass + " is an interface");
                return false;
            }
            if (Modifier.isPrivate(mod)) {
                theException = new XPathException("Class " + theClass + " is private");
                return false;
            }
            if (Modifier.isProtected(mod)) {
                theException = new XPathException("Class " + theClass + " is protected");
                return false;
            }

            resultClass = theClass;
            Constructor[] constructors = theClass.getConstructors();
            for (int c=0; c<constructors.length; c++) {
            	isAvailable = true;
                Constructor theConstructor = constructors[c];
                if (theConstructor.getParameterTypes().length == numArgs) {
                    isAvailable = true;
                    candidateMethods.addElement(theConstructor);
                }
            }
            if (isAvailable) {
                return true;
            } else {
                theException = new XPathException("No constructor with " + numArgs +
                                 (numArgs==1 ? " parameter" : " parameters") +
                                  " found in class " + theClass.getName());
                return false;
            }
        } else {

            // convert any hyphens in the name, camelCasing the following character

            StringBuffer buff = new StringBuffer();
            boolean afterHyphen = false;
            for (int n=0; n<name.length(); n++) {
                char c = name.charAt(n);
                if (c=='-') {
                    afterHyphen = true;
                } else {
                    if (afterHyphen) {
                        buff.append(Character.toUpperCase(c));
                    } else {
                        buff.append(c);
                    }
                    afterHyphen = false;
                }
            }

            name = buff.toString();

            // special case for saxon:if() (Java reserved word)

            if (name.equals("if")) {
                name = "IF";
            }

            // look through the methods of this class to find one that matches the local name

            Method[] methods = theClass.getMethods();
            boolean consistentReturnType = true;
            for (int m=0; m<methods.length; m++) {

                Method theMethod = methods[m];

				if (theMethod.getName().equals(name) &&
				        Modifier.isPublic(theMethod.getModifiers())) {
					isAvailable = true;
					if (consistentReturnType) {
					    if (resultClass==null) {
					        resultClass = theMethod.getReturnType();
					    } else {
					        consistentReturnType =
					            (theMethod.getReturnType()==resultClass);
					    }
					}
                    Class[] theParameterTypes = theMethod.getParameterTypes();
                    boolean isStatic = Modifier.isStatic(theMethod.getModifiers());

                    // if the method is not static, the first supplied argument is the instance, so
                    // discount it

                    significantArgs = (isStatic ? numArgs : numArgs - 1);

                    if (significantArgs>=0) {

                        //System.err.println("Looking for " + name + "(" + significantArgs +"), trying " +
                        //     methods[m].getName() + "(" + theParameterTypes.length + ")");

                        if (theParameterTypes.length == significantArgs &&
                                (significantArgs==0 || theParameterTypes[0]!=Context.class))
                                // TODO: ad XSLTContext.class
                                 {
                            isAvailable = true;
                            candidateMethods.addElement(theMethod);
                        }

                        // we allow the method to have an extra parameter if the first parameter is Context

                        if (theParameterTypes.length == significantArgs+1 &&
                                (theParameterTypes[0]==Context.class ||
                                 theParameterTypes[0]==XSLTContext.class)) {
                            isAvailable = true;
                            candidateMethods.addElement(theMethod);
                        }
                    }
                }
            }

            if (!consistentReturnType) {
                resultClass = null;     // different return type from different methods
            }

            // No method found?

            if (isAvailable) {
                return true;
            } else {
                theException = new XPathException("No method matching " + name +
                                     " with " + significantArgs +
                                     (significantArgs==1 ? " parameter" : " parameters") +
                                      " found in class " + theClass.getName());
                return false;
            }
        }

    }

    /**
    * Determine the data type of the expression, if possible
    * @return Value.ANY (meaning not known in advance)
    */

    public int getDataType() {
        if (resultClass==null || resultClass==Value.class) {
            return Value.ANY;
        } else if (resultClass.toString().equals("void")) {
            return Value.NODESET;
        } else if (resultClass==String.class || resultClass==StringValue.class) {
            return Value.STRING;
        } else if (resultClass==Boolean.class || resultClass==boolean.class ||
                    resultClass==BooleanValue.class) {
            return Value.BOOLEAN;
        } else if (resultClass==Double.class || resultClass==double.class ||
                    resultClass==Float.class || resultClass==float.class ||
                    resultClass==Long.class || resultClass==long.class ||
                    resultClass==Integer.class || resultClass==int.class ||
                    resultClass==Short.class || resultClass==short.class ||
                    resultClass==Byte.class || resultClass==byte.class ||
                    resultClass==NumericValue.class) {
            return Value.NUMBER;
        } else if (NodeSetValue.class.isAssignableFrom(resultClass) ||
                    NodeEnumeration.class.isAssignableFrom(resultClass) ||
                    NodeList.class.isAssignableFrom(resultClass) ||
                    Node.class.isAssignableFrom(resultClass)) {
            return Value.NODESET;
        } else {
            return Value.OBJECT;
        }
    }

    /**
    * Get the name of the function
    */

    public String getName() {
        return name;
    }

    /**
    * Simplify the function (by simplifying its arguments)
    */

    public Expression simplify() throws XPathException {
        for (int i=0; i<getNumberOfArguments(); i++) {
            argument[i] = argument[i].simplify();
        }

        // if the data type of all arguments is known at compile time,
        // we can choose the method now

        if (candidateMethods.size() > 1) {
            boolean allKnown = true;
            for (int i=0; i<getNumberOfArguments(); i++) {
                int type = argument[i].getDataType();
                if (type==Value.ANY || type==Value.OBJECT) {
                    allKnown = false;
                    break;
                }
            }
            if (allKnown) {
                // set up some dummy arguments: only the data type matters
                Value[] argValues = new Value[getNumberOfArguments()];
                for (int k=0; k<getNumberOfArguments(); k++) {
                    switch (argument[k].getDataType()) {
                        case Value.BOOLEAN:
                            argValues[k] = new BooleanValue(true);
                            break;
                        case Value.NUMBER:
                            argValues[k] = new NumericValue(1.0);
                            break;
                        case Value.STRING:
                            argValues[k] = new StringValue("");
                            break;
                        case Value.NODESET:
                            argValues[k] = new EmptyNodeSet();
                            break;
                    }
                }
                try {
                    Object method = getBestFit(argValues);
                    candidateMethods = new Vector();
                    candidateMethods.addElement(method);
                } catch (XPathException err) {
                    theException = err;
                }
            }
        }
        return this;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = 0;
        //if (usesContext) {
            dep = Context.CONTEXT_NODE | Context.POSITION | Context.LAST;
        //}
        for (int i=0; i<getNumberOfArguments(); i++) {
            dep |= argument[i].getDependencies();
        }
        return dep;
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws XPathException {

        // only safe thing is to evaluate it now
        if (//usesContext &&
            (dependencies & (Context.CONTEXT_NODE |
                                 Context.POSITION | Context.LAST)) != 0) {
            return evaluate(context);

        } else {

            FunctionProxy fp = new FunctionProxy();
            fp.theClass = theClass;
            fp.candidateMethods = candidateMethods;
            fp.theException = theException;
            fp.name = name;
            fp.argument = new Expression[getNumberOfArguments()];
            for (int a=0; a<getNumberOfArguments(); a++) {
                fp.addArgument(argument[a].reduce(dependencies, context));
            }
            return fp;
        }
    }

    /**
    * Get the best fit amongst all the candidate methods or constructors
    * @return the result is either a Method or a Constructor. In JDK 1.2 these
    * have a common superclass, AccessibleObject, but this is not available
    * in JDK 1.1, which we still support.
    */

    public Object getBestFit(Value[] argValues) throws XPathException {

        if (candidateMethods.size() == 1) {
            // short cut: there is only one candidate method
            return candidateMethods.elementAt(0);

        } else {
            // choose the best fit method or constructor
            // for each pair of candidate methods, eliminate either or both of the pair
            // if one argument is less-preferred
            int candidates = candidateMethods.size();
            boolean eliminated[] = new boolean[candidates];
            for (int i=0; i<candidates; i++) {
                eliminated[i] = false;
            }

            for (int i=0; i<candidates-1; i++) {
                int[] pref_i = getConversionPreferences(
                                    argValues,
                                    candidateMethods.elementAt(i));
                if (!eliminated[i]) {
                    for (int j=i+1; j<candidates; j++) {
                        if (!eliminated[j]) {
                            int[] pref_j = getConversionPreferences(
                                            argValues,
                                            candidateMethods.elementAt(j));
                            for (int k=0; k<pref_j.length; k++) {
                                if (pref_i[k] > pref_j[k]) { // high number means less preferred
                                    eliminated[i] = true;
                                }
                                if (pref_i[k] < pref_j[k]) {
                                    eliminated[j] = true;
                                }
                            }
                        }
                    }
                }
            }

            int remaining = 0;
            Object theMethod = null;        // could be AccessibleObject in JDK 1.2
            for (int r=0; r<candidates; r++) {
                if (!eliminated[r]) {
                    theMethod = candidateMethods.elementAt(r);
                    remaining++;
                }
            }

            if (remaining==0) {
                throw new XPathException("There is no Java method that is a unique best match");
            }

            if (remaining>1) {
                throw new XPathException("There are several Java methods that match equally well");
            }

            return theMethod;
        }
    }

    /**
    * Evaluate the function. <br>
    * @param context The context in which the function is to be evaluated
    * @return a Value representing the result of the function.
    * @throws XPathException if the function cannot be evaluated.
    */

    public Value evaluate(Context context) throws XPathException {
        Object result = call(context);
        return convertJavaObjectToXPath(result, context.getController());
    }

    public String evaluateAsString(Context context) throws XPathException {
        if (resultClass==String.class) {
            return (String)call(context);
        } else if (resultClass==NodeEnumeration.class) {
            NodeEnumeration enm = enumerate(context, true);
            if (enm.hasMoreElements()) {
                return enm.nextElement().getStringValue();
            } else {
                return "";
            }
        } else {
            return evaluate(context).asString();
        }
    }

    public double evaluateAsNumber(Context context) throws XPathException {
        if (resultClass==double.class) {
            return ((Double)call(context)).doubleValue();
        } else if (resultClass==NodeEnumeration.class) {
            NodeEnumeration enm = enumerate(context, true);
            if (enm.hasMoreElements()) {
                return Value.stringToNumber(enm.nextElement().getStringValue());
            } else {
                return Double.NaN;
            }
        } else {
            return evaluate(context).asNumber();
        }
    }

    public boolean evaluateAsBoolean(Context context) throws XPathException {
        if (resultClass==boolean.class) {
            return ((Boolean)call(context)).booleanValue();
        } else if (resultClass==NodeEnumeration.class) {
            NodeEnumeration enm = enumerate(context, false);
            return enm.hasMoreElements();
        } else {
            return evaluate(context).asBoolean();
        }
    }

    public NodeEnumeration enumerate(Context context, boolean requireSorted) throws XPathException {
        if (resultClass==NodeEnumeration.class) {
            NodeEnumeration result = (NodeEnumeration)call(context);
            if (requireSorted && !result.isSorted()) {
                NodeSetExtent extent = new NodeSetExtent(result, context.getController());
                extent.sort();
                return extent.enumerate();
            } else {
                return result;
            }
        } else {
            return super.enumerate(context, requireSorted);
        }
    }

    /**
    * Call the external function and return its result as a Java object
    */

    private Object call(Context context) throws XPathException {

        // Fail now if no method was found

        if (theException!=null) {
            throw theException;
        }
        context.setException(null);

        Value[] argValues = new Value[getNumberOfArguments()];
        for (int a=0; a<getNumberOfArguments(); a++) {
            argValues[a] = argument[a].evaluate(context);
        }

        // find the best fit method

        Object theMethod = getBestFit(argValues);
                            // could be an AccessibleObject in JDK 1.2

        // now call it

        Class[] theParameterTypes;

        if (theMethod instanceof Constructor) {
            Constructor constructor = (Constructor)theMethod;
            theParameterTypes = constructor.getParameterTypes();
            Object[] params = new Object[theParameterTypes.length];

            setupParams(argValues, params, theParameterTypes, 0, 0);

            try {
                Object obj = constructor.newInstance(params);
                if (context.getException() != null) {
                    throw context.getException();
                }
                return obj;
                //return new ObjectValue(obj);
            } catch (InstantiationException err0) {
                throw new XPathException ("Cannot instantiate class", err0);
            } catch (IllegalAccessException err1) {
                throw new XPathException ("Constructor access is illegal", err1);
            } catch (IllegalArgumentException err2) {
                throw new XPathException ("Argument is of wrong type", err2);
            } catch (InvocationTargetException err3) {
                Throwable ex = err3.getTargetException();
                if (ex instanceof XPathException) {
                    throw (XPathException)ex;
                } else {
                	if (context.getController().isTracing()) {
                		err3.getTargetException().printStackTrace();
                	}
                    throw new XPathException ("Exception in extension function " +
                                            err3.getTargetException().toString());
                }
            }
        } else {
            Method method = (Method)theMethod;
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            Object theInstance;
            theParameterTypes = method.getParameterTypes();
            boolean usesContext = theParameterTypes.length > 0 &&
                                  (theParameterTypes[0] == Context.class ||
                                   theParameterTypes[0] == XSLTContext.class);
            if (isStatic) {
                theInstance = null;
            } else {
                int actualArgs = getNumberOfArguments();
                if (actualArgs==0) {
                    throw new XPathException("Must supply an argument for an instance-level extension function");
                }
                Value arg0 = argument[0].evaluate(context);
                if (arg0 instanceof ObjectValue) {
                    // TODO: check it's the right type for this method
                    theInstance = ((ObjectValue)arg0).getObject();
                } else if (theClass==String.class) {
                    theInstance = arg0.asString();
                } else if (theClass==Boolean.class) {
                    theInstance = new Boolean(arg0.asBoolean());
                } else if (theClass==Double.class) {
                    theInstance = new Double(arg0.asNumber());
                } else {
                    throw new XPathException("First argument is not an object instance");
                }

            }

            int requireArgs = theParameterTypes.length -
                                 (usesContext ? 1 : 0) +
                                 (isStatic ? 0 : 1);

            checkArgumentCount(requireArgs, requireArgs);
            Object[] params = new Object[theParameterTypes.length];

            if (usesContext) {
                params[0] = context;
            }

            setupParams(argValues, params, theParameterTypes,
                            (usesContext ? 1 : 0),
                            (isStatic ? 0 : 1)
                        );

            try {
                Object result = method.invoke(theInstance, params);
                if (context.getException() != null) {
                    throw context.getException();
                }
                if (method.getReturnType().toString().equals("void")) {
                    // method returns void:
                    // tried (method.getReturnType()==Void.class) unsuccessfully
                    return new EmptyNodeSet();
                }
                return result;

            } catch (IllegalAccessException err1) {
                throw new XPathException ("Method access is illegal", err1);
            } catch (IllegalArgumentException err2) {
                throw new XPathException ("Argument is of wrong type", err2);
            } catch (InvocationTargetException err3) {
                Throwable ex = err3.getTargetException();
                if (ex instanceof XPathException) {
                    throw (XPathException)ex;
                } else {
                	if (context.getController().isTracing()) {
                		err3.getTargetException().printStackTrace();
                	}
                    throw new XPathException ("Exception in extension function " +
                                            err3.getTargetException().toString());
                }
            }
        }
    }

    /**
    * Convert a Java object to an XPath value. This method is called to handle the result
    * of an external function call (but only if the required type is not known),
    * and also to process global parameters passed to the stylesheet.
    */

    public static Value convertJavaObjectToXPath(Object result, Controller controller)
                                          throws XPathException {
        if (result==null) {
            return new ObjectValue(null);

        } else if (result instanceof String) {
            return new StringValue((String)result);

        } else if (result instanceof Boolean) {
            return new BooleanValue(((Boolean)result).booleanValue());

        } else if (result instanceof Double) {
            return new NumericValue(((Double)result).doubleValue());
        } else if (result instanceof Float) {
            return new NumericValue((double)((Float)result).floatValue());
        } else if (result instanceof Short) {
            return new NumericValue((double)((Short)result).shortValue());
        } else if (result instanceof Integer) {
            return new NumericValue((double)((Integer)result).intValue());
        } else if (result instanceof Long) {
            return new NumericValue((double)((Long)result).longValue());
        } else if (result instanceof Character) {
            return new NumericValue((double)((Character)result).charValue());
        } else if (result instanceof Byte) {
            return new NumericValue((double)((Byte)result).byteValue());

        } else if (result instanceof Value) {
            return (Value)result;

        } else if (result instanceof NodeInfo) {
            return new SingletonNodeSet((NodeInfo)result);
        } else if (result instanceof NodeEnumeration) {
            // TODO: should avoid breaking the pipeline at this point.
            // It's only necessary because a NodeEnumeration isn't a Value.
            return new NodeSetExtent((NodeEnumeration)result,
                                      controller);
        } else if (result instanceof org.w3c.dom.NodeList) {
            NodeList list = ((NodeList)result);
            NodeInfo[] nodes = new NodeInfo[list.getLength()];
            for (int i=0; i<list.getLength(); i++) {
                if (list.item(i) instanceof NodeInfo) {
                    nodes[i] = (NodeInfo)list.item(i);
                } else {
                    throw new XPathException("Supplied NodeList contains non-Saxon DOM Nodes");
                }

            }
            return new NodeSetExtent(nodes, controller);
        } else if (result instanceof org.w3c.dom.Node) {
            throw new XPathException("Result is a non-Saxon DOM Node");
        } else {
            return new ObjectValue(result);
        }
    }

    /**
    * Get an array of integers representing the conversion distances of each "real" argument
    * to a given method
    * @param argValues: the actual argumetn values supplied
    * @param method: the method or constructor. (Could be an AccessibleObject in JDK 1.2)
    * @return an array of integers, one for each argument, indicating the conversion
    * distances. A high number indicates low preference.
    */

    private int[] getConversionPreferences(Value[] argValues, Object method) {

        Class[] params;
        int firstArg;

        if (method instanceof Constructor) {
            firstArg = 0;
            params = ((Constructor)method).getParameterTypes();
        } else {
            boolean isStatic = Modifier.isStatic(((Method)method).getModifiers());
            firstArg = (isStatic ? 0 : 1);
            params = ((Method)method).getParameterTypes();
        }

        int preferences[] = new int[argValues.length];
        if (firstArg == 1) {
            preferences[0] = (argValues[0] instanceof ObjectValue ? 0 : 20);
        }
        int firstParam = 0;

        if (params[0] == Context.class || params[0] == XSLTContext.class) {
            firstParam = 1;
        }

        for (int i = firstArg; i<argValues.length; i++) {
            preferences[i] = argValues[i-firstArg].conversionPreference(params[i+firstParam-firstArg]);
        }

        return preferences;
    }


    private void setupParams(Value[] argValues,
                             Object[] params,
                             Class[] paramTypes,
                             int firstParam,
                             int firstArg) throws XPathException {
        int j=firstParam;
        for (int i=firstArg; i<getNumberOfArguments(); i++) {
            params[j] = argValues[i].convertToJava(paramTypes[j]);
            j++;
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

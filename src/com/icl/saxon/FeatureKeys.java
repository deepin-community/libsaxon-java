package com.icl.saxon;


/**
  * FeatureKeys defines a set of constants, names of Saxon configuration
  * options which can be supplied to the TransformerFactoryImpl interface
  *
  * @author Michael H. Kay
  */


public class FeatureKeys {

	/**
	* ALLOW_EXTERNAL_FUNCTIONS must be a Boolean()
	*/

	public final static String ALLOW_EXTERNAL_FUNCTIONS =
	        "http://icl.com/saxon/feature/allow-external-functions";


	/**
	* TIMING must be an Boolean()
	*/

	public final static String TIMING =
	        "http://icl.com/saxon/feature/timing";

	/**
	* TREE_MODEL must be an Integer(): Builder.STANDARD_TREE or Builder.TINY_TREE
	*/

	public final static String TREE_MODEL =
	        "http://icl.com/saxon/feature/treeModel";

	/**
	* TRACE_LISTENER must be a class that implements com.icl.saxon.trace.TraceListener
	*/

	public final static String TRACE_LISTENER =
	        "http://icl.com/saxon/feature/traceListener";

	/**
	* LINE_NUMBERING must be a Boolean()
	*/

	public final static String LINE_NUMBERING =
	        "http://icl.com/saxon/feature/linenumbering";

	/**
	* RECOVERY_POLICY must be an Integer: Controller.RECOVER_SILENTLY,
	* Controller.RECOVER_WITH_WARNINGS, or Controller.DO_NOT_RECOVER
	*/

	public final static String RECOVERY_POLICY =
	        "http://icl.com/saxon/feature/recoveryPolicy";

	/**
	* MESSAGE_EMITTER_CLASS must be the class name of an Emitter
	*/

	public final static String MESSAGE_EMITTER_CLASS =
	        "http://icl.com/saxon/feature/messageEmitterClass";

    /**
    * SOURCE_PARSER_CLASS must be the full class name of an XMLReader
    */

    public final static String SOURCE_PARSER_CLASS =
            "http://icl.com/saxon/feature/sourceParserClass";

    /**
    * STYLE_PARSER_CLASS must be the full class name of an XMLReader
    */

    public final static String STYLE_PARSER_CLASS =
            "http://icl.com/saxon/feature/styleParserClass";


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
// The Original Code is: all this file, other than fragments copied from the SAX distribution
// made available by David Megginson, and the line marked PB-SYNC.
//
// The Initial Developer of the Original Code is
// Michael Kay
//
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved.
//
// Contributor(s): Michael Kay, Peter Bryant, David Megginson
//

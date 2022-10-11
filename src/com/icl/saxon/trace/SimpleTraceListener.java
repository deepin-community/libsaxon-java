package com.icl.saxon.trace;

import com.icl.saxon.style.StyleElement;
import com.icl.saxon.Context;
import com.icl.saxon.Mode;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Navigator;

/**
* A Simple trace listener that writes messages to System.err
*/

public class SimpleTraceListener implements TraceListener {

  String indent = "";

  /**
  * Called at start
  */

  public void open() {
    System.err.println("<trace>");
  }

  /**
  * Called at end
  */

  public void close() {
    System.err.println("</trace>");
  }
  
  

  /**
   * Called for all top level elements
   */
  public void toplevel(NodeInfo element)
  {
    StyleElement e = (StyleElement)element;
    System.err.println("<Top-level element=\"" + e.getDisplayName() + "\" line=\"" + e.getLineNumber() +
       "\" file=\"" + e.getSystemId() + "\" precedence=\"" + e.getPrecedence() +"\"/>");
  }

  /**
   * Called when a node of the source tree gets processed
   */
  public void enterSource(NodeHandler handler, Context context)
  {
    NodeInfo curr = context.getContextNodeInfo();
    System.err.println(indent + "<Source node=\""  + Navigator.getPath(curr)
                        + "\" line=\"" + curr.getLineNumber()
		                + "\" mode=\"" + getModeName(context) + "\">");
    indent += " ";
  }

  /**
   * Called after a node of the source tree got processed
   */
  public void leaveSource(NodeHandler handler, Context context)
  {
    indent = indent.substring(0, indent.length() - 1);
    System.err.println(indent + "</Source><!-- "  +
         Navigator.getPath(context.getContextNodeInfo()) + " -->");
  }

  /**
   * Called when an element of the stylesheet gets processed
   */
  public void enter(NodeInfo element, Context context)
  {
    if (element.getNodeType()==NodeInfo.ELEMENT) {
        System.err.println(indent + "<Instruction element=\"" + element.getDisplayName() + "\" line=\"" + element.getLineNumber() + "\">");
        indent += " ";
    }
  }

  /**
   * Called after an element of the stylesheet got processed
   */
  	public void leave(NodeInfo element, Context context)
  	{
    	if (element.getNodeType()==NodeInfo.ELEMENT) {
        	indent = indent.substring(0, indent.length() - 1);
        	System.err.println(indent + "</Instruction> <!-- " +
        						 element.getDisplayName() + " -->");
    	}
  	}

	String getModeName(Context context)
	{
        Mode mode = context.getMode();
        if (mode==null) return "#none";
		int nameCode = mode.getNameCode();
		if (nameCode==-1) {
			return "#default";
		} else {
			return context.getController().getNamePool().getDisplayName(nameCode);
		}
	}
}

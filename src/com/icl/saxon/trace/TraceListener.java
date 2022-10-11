package com.icl.saxon.trace;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.Context;

import java.util.EventListener;

/**
* To get trace events, an application can add instances of
* this interface to a StyleSheetInstance
*/

public interface TraceListener extends EventListener {

  /**
  * Called at start
  */

  public void open();

  /**
  * Called at end
  */

  public void close();
  

  /**
   * Called for all top level elements
   */
  public void toplevel(NodeInfo element);

  /**
   * Called when a node of the source tree gets processed
   */
  public void enterSource(NodeHandler handler, Context context);

  /**
   * Called after a node of the source tree got processed
   */
  public void leaveSource(NodeHandler handler, Context context);

  /**
   * Called when a node in the stylesheet gets processed
   */
  public void enter(NodeInfo element, Context context);

  /**
   * Called after an element of the stylesheet got processed
   */
  public void leave(NodeInfo element, Context context);

}

// Contributor(s): 
// This module is from Edwin Glaser (edwin@pannenleiter.de)
// Modified by MHK for Saxon 6.1 to remove "throws SAXException" from all methods
//

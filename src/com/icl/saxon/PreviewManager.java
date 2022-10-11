package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;

import org.xml.sax.*;
import java.util.*;


/**
  * <B>PreviewManager</B> handles the registration of preview elements for use by the Builder
  * @author Michael H. Kay
  */

public class PreviewManager {

    private int previewModeNameCode = -1;
    private int[] previewElements = new int[10];
    private int used = 0;
    //private Controller controller = null;

    //public void setController(Controller c) {
    //    controller = c;
    //}

    //public Controller getController() {
    //    return controller;
    //}

    /**
    * Define the mode that will be used for previewing elements. The node handler for these
    * elements is called while the tree is being built, as soon as the element end tag is
    * encountered.
    * @param mode The nameCode of the mode to be used
    */

    public void setPreviewMode(int mode) {
        previewModeNameCode = mode;
    }

    public final int getPreviewMode() {
        return previewModeNameCode;
    }

    /**
    * Register an element as a preview element. If an element is so registered, the relevant
    * node handler for preview mode will be called as soon as the element's end tag is read.
    * The node handler must be registered in the normal way
    */

    public void setPreviewElement(int fingerprint) {
    	if (used>=previewElements.length) {
    		int[] n = new int[used*2];
    		System.arraycopy(previewElements, 0, n, 0, used);
    		previewElements = n;
    	}
        previewElements[used++] = fingerprint;
    }

    /**
    * Determine whether an element is a preview element.
    */

    public boolean isPreviewElement(int fingerprint) {
        for (int i=0; i<used; i++) {
        	if (fingerprint == previewElements[i]) return true;
        }
        return false;
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

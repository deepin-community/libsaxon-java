package com.icl.saxon;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.pattern.Pattern;

/**
  * Corresponds to a single key definition.<P>
  * @author Michael H. Kay
  */

public class KeyDefinition {

    private int fingerprint;    // the fingerprint of the name of the key definition
    private Pattern match;  // the match pattern
    private Expression use; // the use expression

    /**
    * Constructor to create a key definition
    */

    public KeyDefinition(int fingerprint, Pattern match, Expression use) {
        this.fingerprint = fingerprint;
        this.match = match;
        this.use = use;
    }

    /**
    * Get the fingerprint of the name of the key definition
    */

    public int getFingerprint() {
        return fingerprint;
    }

    /**
    * Get the match pattern for the key definition
    */

    public Pattern getMatch() {
        return match;
    }

    /**
    * Get the use expression for the key definition
    */

    public Expression getUse() {
        return use;
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
// The Original Code is: all this file except PB-SYNC section.
//
// The Initial Developer of the Original Code is
// Michael H. Kay.
//
// Portions marked PB-SYNC are Copyright (C) Peter Bryant (pbryant@bigfoot.com). All Rights Reserved.
//
// Contributor(s): Michael Kay, Peter Bryant.
//

package com.icl.saxon;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
//import org.xml.sax.ext.LexicalHandler;
//import org.xml.sax.helpers.ParserAdapter;
//import org.w3c.dom.Document;

/**
  * <p>This class allows a SAXON application to encapsulate information
  * about an input source in a single object, which may include
  * a public identifier, a system identifier, a byte stream (possibly
  * with a specified encoding), a character stream, or a file.</p>
  *
  * <p>Most of the functionality is inherited directly from the SAX
  * InputSource class; the additional functionality offered by
  * ExtendedInputSource is to allow the input source to be specified as
  * a File object.</p>
  *
  * @author Michael H. Kay
  */

public class ExtendedInputSource extends org.xml.sax.InputSource {

    private int estimatedLength = -1;

    /**
    * Default constructor
    */

    public ExtendedInputSource() {
        super();
    }

    /**
    * Create a new input source from a System ID
    */

    public ExtendedInputSource(String systemId) {
        super(systemId);
    }

    /**
    * Create a new input source from a character stream
    */

    public ExtendedInputSource(Reader reader) {
        super(reader);
    }

    /**
    * Create a new input source from a byte stream
    */

    public ExtendedInputSource(InputStream stream) {
        super(stream);
    }

    /**
    * Create a new input source from a File. Note that the directory
    * in which the file occurs will be used as the base for resolving any
    * system identifiers encountered within the XML document
    *
    * <p>Example of use:<BR>
    * parser.parse(new ExtendedInputSource(new File("test.xml")))</p>
    * @param file A File object identifying the XML input file
    *
    */

    public ExtendedInputSource (File file) {
        setFile(file);
    }

    /**
    * Create an ExtendedInputSource from an existing InputSource
    */

    public ExtendedInputSource (InputSource in) {
        setSystemId(in.getSystemId());
        setPublicId(in.getPublicId());
        setByteStream(in.getByteStream());
        setEncoding(in.getEncoding());
        setCharacterStream(in.getCharacterStream());
    }

    /**
    * Specify that input is to come from a given File.
    */

    public void setFile(File file) {
        super.setSystemId(createURL(file));
    }

    /**
    * Set the estimated length of the file (advisory only)
    */

    public void setEstimatedLength(int length) {
        estimatedLength = length;
    }

    /**
    * Get the estimated length of the file (advisory only; -1 if not known)
    */

    public int getEstimatedLength() {
        return estimatedLength;
    }

    /**
    * Create a URL that refers to a given File
    */

  public static String createURL(File file)
  {
    String path = file.getAbsolutePath();
    while (true) {
        int special = path.indexOf('#');
        if (special >= 0) {
            path = path.substring(0, special) + "%23" + path.substring(special+1);
        } else {
            break;
        }
    }


    // Following code is cribbed from MSXML
    URL url = null;
    try
        {
            url = new URL(path);
        }
    catch (MalformedURLException ex)
        {
            try
            {
                // This is a bunch of weird code that is required to
                // make a valid URL on the Windows platform, due
                // to inconsistencies in what getAbsolutePath returns.
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1)
                {
                    char sep = fs.charAt(0);
                    if (sep != '/')
                        path = path.replace(sep, '/');
                    if (path.charAt(0) != '/')
                        path = '/' + path;
                }
                path = "file://" + path;
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                return null;
            }
        }
     return( url.toString() );
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

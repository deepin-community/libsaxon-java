package com.icl.saxon.output;
import javax.xml.transform.OutputKeys;

/**
 * Provides string constants that can be used to set
 * output properties for a Transformer, or to retrieve
 * output properties from a Transformer or Templates object.
 *
 * These keys are private Saxon keys that supplement the standard keys
 * defined in javax.xml.transform.OutputKeys
 */

public class SaxonOutputKeys {

    /**
     * indentSpaces = integer.
     *
     * <p>Defines the number of spaces used for indentation of output</p>
     */

    public static final String INDENT_SPACES = "{http://icl.com/saxon}indent-spaces";

    /**
     * include-html-meta-tag = "yes" | "no".
     *
     * <p>Indicates whether the META tag is to be added to HTML output</p>
     */

    public static final String OMIT_META_TAG = "{http://icl.com/saxon}omit-meta-tag";

    /**
     * representation = rep1[;rep2].
     *
     * <p>Indicates the preferred way of representing non-ASCII characters in HTML
     * and XML output. rep1 is for characters in the range 128-256, rep2 for those
     * above 256.</p>
     */
    public static final String CHARACTER_REPRESENTATION = "{http://icl.com/saxon}character-representation";

    /**
     * saxon:next-in-chain = URI.
     *
     * <p>Indicates that the output is to be piped into another XSLT stylesheet
     * to perform another transformation. The auxiliary property NEXT_IN_CHAIN_BASE_URI
     * records the base URI of the stylesheet element where this attribute was found.</p>
     */
    public static final String NEXT_IN_CHAIN = "{http://icl.com/saxon}next-in-chain";
    public static final String NEXT_IN_CHAIN_BASE_URI = "{http://icl.com/saxon}next-in-chain-base-uri";

    /**
    * saxon:require-well-formed = yes|no.
    *
    * <p>Indicates whether a user-supplied ContentHandler requires the stream of SAX events to be
    * well-formed (that is, to have a single element node and no text nodes as children of the root).
    * The default is "no".</p>
    */

    public static final String REQUIRE_WELL_FORMED = "{http://saxon.sf.net/}require-well-formed";

    /**
    * Check that a supplied output key is valid
    */

    public static final boolean isValidOutputKey(String key) {
        if (key.startsWith("{")) {
            if (key.startsWith("{http://icl.com/saxon}")) {
                return
                    key.equals(INDENT_SPACES) ||
                    key.equals(OMIT_META_TAG) ||
                    key.equals(CHARACTER_REPRESENTATION) ||
                    key.equals(NEXT_IN_CHAIN) ||
                    key.equals(NEXT_IN_CHAIN_BASE_URI) ||
                    key.equals(REQUIRE_WELL_FORMED);
            } else {
                return true;
            }
        } else {
            return
                key.equals(OutputKeys.CDATA_SECTION_ELEMENTS) ||
                key.equals(OutputKeys.DOCTYPE_PUBLIC) ||
                key.equals(OutputKeys.DOCTYPE_SYSTEM) ||
                key.equals(OutputKeys.ENCODING) ||
                key.equals(OutputKeys.INDENT) ||
                key.equals(OutputKeys.MEDIA_TYPE) ||
                key.equals(OutputKeys.METHOD) ||
                key.equals(OutputKeys.OMIT_XML_DECLARATION) ||
                key.equals(OutputKeys.STANDALONE) ||
                key.equals(OutputKeys.VERSION);
        }
    }

}

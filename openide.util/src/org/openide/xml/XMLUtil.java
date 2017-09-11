/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.xml;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Utility class collecting library methods related to XML processing.
 */
public final class XMLUtil extends Object {

    /*
        public static String toCDATA(String val) throws IOException {

        }
    */
    private static final char[] DEC2HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /** Forbids creating new XMLUtil */
    private XMLUtil() {
    }

    // ~~~~~~~~~~~~~~~~~~~~~ SAX related ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Create a simple parser.
      * @return <code>createXMLReader(false, false)</code>
      */
    public static XMLReader createXMLReader() throws SAXException {
        return createXMLReader(false, false);
    }

    /** Create a simple parser, possibly validating.
     * @param validate if true, a validating parser is returned
     * @return <code>createXMLReader(validate, false)</code>
     */
    public static XMLReader createXMLReader(boolean validate)
    throws SAXException {
        return createXMLReader(validate, false);
    }

    private static SAXParserFactory[][] saxes  = new SAXParserFactory[2][2];
    /** Creates a SAX parser.
     *
     * <p>See {@link #parse} for hints on setting an entity resolver.
     *
     * @param validate if true, a validating parser is returned
     * @param namespaceAware if true, a namespace aware parser is returned
     *
     * @throws FactoryConfigurationError Application developers should never need to directly catch errors of this type.
     * @throws SAXException if a parser fulfilling given parameters can not be created
     *
     * @return XMLReader configured according to passed parameters
     */
    public static synchronized XMLReader createXMLReader(boolean validate, boolean namespaceAware)
    throws SAXException {
        SAXParserFactory factory = saxes[validate ? 0 : 1][namespaceAware ? 0 : 1];
        if (factory == null) {
            try {
                factory = SAXParserFactory.newInstance();
            } catch (FactoryConfigurationError err) {
                Exceptions.attachMessage(
                    err, 
                    "Info about thread context classloader: " + // NOI18N
                    Thread.currentThread().getContextClassLoader()
                );
                throw err;
            }
            factory.setValidating(validate);
            factory.setNamespaceAware(namespaceAware);
            saxes[validate ? 0 : 1][namespaceAware ? 0 : 1] = factory;
        }

        try {
            return factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create parser satisfying configuration parameters", ex); //NOI18N                        
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~ DOM related ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Creates an empty DOM document. E.g.:
     * <p><pre>
     * Document doc = createDocument("book", null, null, null);
     * </pre><p>
     * creates new DOM of a well-formed document with root element named book.
     *
     * @param rootQName qualified name of root element. e.g. <code>myroot</code> or <code>ns:myroot</code>
     * @param namespaceURI URI of root element namespace or <code>null</code>
     * @param doctypePublicID public ID of DOCTYPE or <code>null</code>
     * @param doctypeSystemID system ID of DOCTYPE or <code>null</code> if no DOCTYPE
     *        required and doctypePublicID is also <code>null</code>
     *
     * @throws DOMException if new DOM with passed parameters can not be created
     * @throws FactoryConfigurationError Application developers should never need to directly catch errors of this type.
     *
     * @return new DOM Document
     */
    public static Document createDocument(
        String rootQName, String namespaceURI, String doctypePublicID, String doctypeSystemID
    ) throws DOMException {
        DOMImplementation impl = getDOMImplementation();

        if ((doctypePublicID != null) && (doctypeSystemID == null)) {
            throw new IllegalArgumentException("System ID cannot be null if public ID specified. "); //NOI18N
        }

        DocumentType dtd = null;

        if (doctypeSystemID != null) {
            dtd = impl.createDocumentType(rootQName, doctypePublicID, doctypeSystemID);
        }

        return impl.createDocument(namespaceURI, rootQName, dtd);
    }

    /**
     * Obtains DOMImpementaton interface providing a number of methods for performing
     * operations that are independent of any particular DOM instance.
     *
     * @throw DOMException <code>NOT_SUPPORTED_ERR</code> if cannot get DOMImplementation
     * @throw FactoryConfigurationError Application developers should never need to directly catch errors of this type.
     *
     * @return DOMImplementation implementation
     */
    private static DOMImplementation getDOMImplementation()
    throws DOMException { //can be made public

        DocumentBuilderFactory factory = getFactory(false, false);

        try {
            return factory.newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            throw new DOMException(
                DOMException.NOT_SUPPORTED_ERR, "Cannot create parser satisfying configuration parameters"
            ); //NOI18N
        } catch (RuntimeException e) {
            // E.g. #36578, IllegalArgumentException. Try to recover gracefully.
            throw (DOMException) new DOMException(DOMException.NOT_SUPPORTED_ERR, e.toString()).initCause(e);
        }
    }

    private static DocumentBuilderFactory[][] doms = new DocumentBuilderFactory[2][2];
    private static synchronized DocumentBuilderFactory getFactory(boolean validate, boolean namespaceAware) {
        DocumentBuilderFactory factory = doms[validate ? 0 : 1][namespaceAware ? 0 : 1];
        if (factory == null) {
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validate);
            factory.setNamespaceAware(namespaceAware);
            doms[validate ? 0 : 1][namespaceAware ? 0 : 1] = factory;
        }
        return factory;
    }

    /**
     * Parses an XML document into a DOM tree.
     *
     * <div class="nonnormative">
     *
     * <p>Remember that when parsing XML files you often want to set an explicit
     * entity resolver. For example, consider a file such as this:</p>
     *
     * <pre>
     * &lt;?<font class="keyword">xml</font> <font class="variable-name">version</font>=<font class="string">"1.0"</font> <font class="variable-name">encoding</font>=<font class="string">"UTF-8"</font>?&gt;
     * &lt;!<font class="keyword">DOCTYPE</font> <font class="type">root</font> <font class="keyword">PUBLIC</font> <font class="string">"-//NetBeans//DTD Foo 1.0//EN"</font> <font class="string">"http://www.netbeans.org/dtds/foo-1_0.dtd"</font>&gt;
     * &lt;<font class="function-name">root</font>/&gt;
     * </pre>
     *
     * <p>If you parse this with a null entity resolver, or you use the
     * default resolver ({@link EntityCatalog#getDefault}) but do not do
     * anything special with this DTD, you will probably find the parse
     * blocking to make a network connection <em>even when you are not
     * validating</em>. That is because DTDs can be used to define
     * entities and other XML oddities, and are not a pure constraint
     * language like Schema or RELAX-NG.</p>
     *
     * <p>There are three basic ways to avoid the network connection.</p>
     *
     * <ol>
     *
     * <li><p>Register the DTD. This is generally the best thing to do. See
     * {@link EntityCatalog}'s documentation for details, but for example
     * in your layer use:</p>
     *
     * <pre>
     * &lt;<font class="function-name">filesystem</font>&gt;
     *   &lt;<font class="function-name">folder</font> <font class="variable-name">name</font>=<font class="string">"xml"</font>&gt;
     *     &lt;<font class="function-name">folder</font> <font class="variable-name">name</font>=<font class="string">"entities"</font>&gt;
     *       &lt;<font class="function-name">folder</font> <font class="variable-name">name</font>=<font class="string">"NetBeans"</font>&gt;
     *         &lt;<font class="function-name">file</font> <font class="variable-name">name</font>=<font class="string">"DTD_Foo_1_0"</font>
     *               <font class="variable-name">url</font>=<font class="string">"resources/foo-1_0.dtd"</font>&gt;
     *           &lt;<font class="function-name">attr</font> <font class="variable-name">name</font>=<font class="string">"hint.originalPublicID"</font>
     *                 <font class="variable-name">stringvalue</font>=<font class="string">"-//NetBeans//DTD Foo 1.0//EN"</font>/&gt;
     *         &lt;/<font class="function-name">file</font>&gt;
     *       &lt;/<font class="function-name">folder</font>&gt;
     *     &lt;/<font class="function-name">folder</font>&gt;
     *   &lt;/<font class="function-name">folder</font>&gt;
     * &lt;/<font class="function-name">filesystem</font>&gt;
     * </pre>
     *
     * <p>Now the default system entity catalog will resolve the public ID
     * to the local copy in your module, not the network copy.
     * Additionally, anyone who mounts the "NetBeans Catalog" in the XML
     * Entity Catalogs node in the Runtime tab will be able to use your
     * local copy of the DTD automatically, for validation, code
     * completion, etc. (The network URL should really exist, though, for
     * the benefit of other tools!)</p></li>
     *
     * <li><p>You can also set an explicit entity resolver which maps that
     * particular public ID to some local copy of the DTD, if you do not
     * want to register it globally in the system for some reason. If
     * handed other public IDs, just return null to indicate that the
     * system ID should be loaded.</p></li>
     *
     * <li><p>In some cases where XML parsing is very
     * performance-sensitive, and you know that you do not need validation
     * and furthermore that the DTD defines no infoset (there are no
     * entity or character definitions, etc.), you can speed up the parse.
     * Turn off validation, but also supply a custom entity resolver that
     * does not even bother to load the DTD at all:</p>
     *
     * <pre>
     * <font class="keyword">public</font> <font class="type">InputSource</font> <font class="function-name">resolveEntity</font>(<font class="type">String</font> <font class="variable-name">pubid</font>, <font class="type">String</font> <font class="variable-name">sysid</font>)
     *     <font class="keyword">throws</font> <font class="type">SAXException</font>, <font class="type">IOException</font> {
     *   <font class="keyword">if</font> (pubid.equals(<font class="string">"-//NetBeans//DTD Foo 1.0//EN"</font>)) {
     *     <font class="keyword">return</font> <font class="keyword">new</font> <font class="type">InputSource</font>(<font class="keyword">new</font> <font class="type">ByteArrayInputStream</font>(<font class="keyword">new</font> <font class="type">byte</font>[0]));
     *   } <font class="keyword">else</font> {
     *     <font class="keyword">return</font> EntityCatalog.getDefault().resolveEntity(pubid, sysid);
     *   }
     * }
     * </pre></li>
     *
     * </ol>
     *
     * </div>
     *
     * @param input a parser input (for URL users use: <code>new InputSource(url.toString())</code>
     * @param validate if true validating parser is used
     * @param namespaceAware if true DOM is created by namespace aware parser
     * @param errorHandler a error handler to notify about exception (such as {@link #defaultErrorHandler}) or <code>null</code>
     * @param entityResolver SAX entity resolver (such as {@link EntityCatalog#getDefault}) or <code>null</code>
     *
     * @throws IOException if an I/O problem during parsing occurs
     * @throws SAXException is thrown if a parser error occurs
     * @throws FactoryConfigurationError Application developers should never need to directly catch errors of this type.
     *
     * @return document representing given input
     */
    public static Document parse(
        InputSource input, boolean validate, boolean namespaceAware, ErrorHandler errorHandler,
        EntityResolver entityResolver
    ) throws IOException, SAXException {
        
        DocumentBuilder builder = null;
        DocumentBuilderFactory factory = getFactory(validate, namespaceAware);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create parser satisfying configuration parameters", ex); //NOI18N
        }
        
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }
        
        return builder.parse(input);
    }

    /**
     * Identity transformation in XSLT with indentation added.
     * Just using the identity transform and calling
     * t.setOutputProperty(OutputKeys.INDENT, "yes");
     * t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
     * does not work currently.
     * You really have to use this bogus stylesheet.
     * @see "JDK bug #5064280"
     */
    private static final String IDENTITY_XSLT_WITH_INDENT =
            "<xsl:stylesheet version='1.0' " + // NOI18N
            "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " + // NOI18N
            "xmlns:xalan='http://xml.apache.org/xslt' " + // NOI18N
            "exclude-result-prefixes='xalan'>" + // NOI18N
            "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" + // NOI18N
            "<xsl:template match='@*|node()'>" + // NOI18N
            "<xsl:copy>" + // NOI18N
            "<xsl:apply-templates select='@*|node()'/>" + // NOI18N
            "</xsl:copy>" + // NOI18N
            "</xsl:template>" + // NOI18N
            "</xsl:stylesheet>"; // NOI18N
    /** Workaround for JAXP bug 7150637 / XALANJ-1497. */
    private static final String ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";
    /**
     * Writes a DOM document to a stream.
     * The precise output format is not guaranteed but this method will attempt to indent it sensibly.
     * 
     * <p class="nonnormative"><b>Important</b>: There might be some problems with
     * <code>&lt;![CDATA[ ]]&gt;</code> sections in the DOM tree you pass into this method. Specifically,
     * some CDATA sections my not be written as CDATA section or may be merged with
     * other CDATA section at the same level. Also if plain text nodes are mixed with
     * CDATA sections at the same level all text is likely to end up in one big CDATA section.
     * <br/>
     * For nodes that only have one CDATA section this method should work fine.
     * </p>
     * 
     * @param doc DOM document to be written
     * @param out data sink
     * @param enc XML-defined encoding name (e.g. "UTF-8")
     * @throws IOException if JAXP fails or the stream cannot be written to
     */
    public static void write(Document doc, OutputStream out, String enc) throws IOException {
        if (enc == null) {
            throw new NullPointerException("You must set an encoding; use \"UTF-8\" unless you have a good reason not to!"); // NOI18N
        }
        Document doc2 = normalize(doc);
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() { // #195921
            @Override public ClassLoader run() {
                return new ClassLoader(ClassLoader.getSystemClassLoader().getParent()) {
                    @Override public InputStream getResourceAsStream(String name) {
                        if (name.startsWith("META-INF/services/")) {
                            return new ByteArrayInputStream(new byte[0]); // JAXP #6723276
                        }
                        return super.getResourceAsStream(name);
                    }
                };
            }
        }));
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer(
                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
            DocumentType dt = doc2.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                String sys = dt.getSystemId();
                if (sys != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, sys);
                }
            }
            t.setOutputProperty(OutputKeys.ENCODING, enc);
            try {
                t.setOutputProperty(ORACLE_IS_STANDALONE, "yes");
            } catch (IllegalArgumentException x) {
                // fine, introduced in JDK 7u4
            }

            // See #123816
            Set<String> cdataQNames = new HashSet<String>();
            collectCDATASections(doc2, cdataQNames);
            if (cdataQNames.size() > 0) {
                StringBuilder cdataSections = new StringBuilder();
                for(String s : cdataQNames) {
                    cdataSections.append(s).append(' '); //NOI18N
                }
                t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSections.toString());
            }

            Source source = new DOMSource(doc2);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    private static void collectCDATASections(Node node, Set<String> cdataQNames) {
        if (node instanceof CDATASection) {
            Node parent = node.getParentNode();
            if (parent != null) {
                String uri = parent.getNamespaceURI();
                if (uri != null) {
                    cdataQNames.add("{" + uri + "}" + parent.getNodeName()); //NOI18N
                } else {
                    cdataQNames.add(parent.getNodeName());
                }
            }
        }
        
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            collectCDATASections(children.item(i), cdataQNames);
        }
    }

    /**
     * Check whether a DOM tree is valid according to a schema.
     * Example of usage:
     * <pre>
     * Element fragment = ...;
     * SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
     * Schema s = f.newSchema(This.class.getResource("something.xsd"));
     * try {
     *     XMLUtil.validate(fragment, s);
     *     // valid
     * } catch (SAXException x) {
     *     // invalid
     * }
     * </pre>
     * @param data a DOM tree
     * @param schema a parsed schema
     * @throws SAXException if validation failed
     * @since org.openide.util 7.17
     */
    public static void validate(Element data, Schema schema) throws SAXException {
        Validator v = schema.newValidator();
        final SAXException[] error = {null};
        v.setErrorHandler(new ErrorHandler() {
            public @Override void warning(SAXParseException x) throws SAXException {}
            public @Override void error(SAXParseException x) throws SAXException {
                // Just rethrowing it is bad because it will also print it to stderr.
                error[0] = x;
            }
            public @Override void fatalError(SAXParseException x) throws SAXException {
                error[0] = x;
            }
        });
        try {
            v.validate(new DOMSource(fixupAttrs(data)));
        } catch (IOException x) {
            assert false : x;
        }
        if (error[0] != null) {
            throw error[0];
        }
    }
    private static Element fixupAttrs(Element root) { // #140905
        // #6529766/#6531160: some versions of JAXP reject attributes set using setAttribute
        // (rather than setAttributeNS) even though the schema calls for no-NS attrs!
        // JDK 5 is fine; JDK 6 broken; JDK 6u2+ fixed
        // #146081: xml:base attributes mess up validation too.
        Element copy = (Element) root.cloneNode(true);
        fixupAttrsSingle(copy);
        NodeList nl = copy.getElementsByTagName("*"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            fixupAttrsSingle((Element) nl.item(i));
        }
        return copy;
    }
    private static void fixupAttrsSingle(Element e) throws DOMException {
        removeXmlBase(e);
        Map<String, String> replace = new HashMap<String, String>();
        NamedNodeMap attrs = e.getAttributes();
        for (int j = 0; j < attrs.getLength(); j++) {
            Attr attr = (Attr) attrs.item(j);
            if (attr.getNamespaceURI() == null && !attr.getName().equals("xmlns")) { // NOI18N
                replace.put(attr.getName(), attr.getValue());
            }
        }
        for (Map.Entry<String, String> entry : replace.entrySet()) {
            e.removeAttribute(entry.getKey());
            e.setAttributeNS(null, entry.getKey(), entry.getValue());
        }
    }
    private static void removeXmlBase(Element e) {
        e.removeAttributeNS("http://www.w3.org/XML/1998/namespace", "base"); // NOI18N
        e.removeAttribute("xml:base"); // NOI18N
    }

    /**
     * Escape passed string as XML attibute value
     * (<code>&lt;</code>, <code>&amp;</code>, <code>'</code> and <code>"</code>
     * will be escaped.
     * Note: An XML processor returns normalized value that can be different.
     *
     * @param val a string to be escaped
     *
     * @return escaped value
     * @throws CharConversionException if val contains an improper XML character
     *
     * @since 1.40
     */
    public static String toAttributeValue(String val) throws CharConversionException {
        if (val == null) {
            throw new CharConversionException("null"); // NOI18N
        }

        if (checkAttributeCharacters(val)) {
            return val;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);

            if ('<' == ch) {
                buf.append("&lt;");

                continue;
            } else if ('&' == ch) {
                buf.append("&amp;");

                continue;
            } else if ('\'' == ch) {
                buf.append("&apos;");

                continue;
            } else if ('"' == ch) {
                buf.append("&quot;");

                continue;
            }

            buf.append(ch);
        }

        return buf.toString();
    }

    /**
     * Escape passed string as XML element content (<code>&lt;</code>,
     * <code>&amp;</code> and <code>><code> in <code>]]></code> sequences).
     *
     * @param val a string to be escaped
     *
     * @return escaped value
     * @throws CharConversionException if val contains an improper XML character
     *
     * @since 1.40
     */
    public static String toElementContent(String val) throws CharConversionException {
        if (val == null) {
            throw new CharConversionException("null"); // NOI18N
        }

        if (checkContentCharacters(val)) {
            return val;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);

            if ('<' == ch) {
                buf.append("&lt;");

                continue;
            } else if ('&' == ch) {
                buf.append("&amp;");

                continue;
            } else if (('>' == ch) && (i > 1) && (val.charAt(i - 2) == ']') && (val.charAt(i - 1) == ']')) {
                buf.append("&gt;");

                continue;
            }

            buf.append(ch);
        }

        return buf.toString();
    }

    /**
     * Can be used to encode values that contain invalid XML characters.
     * At SAX parser end must be used pair method to get original value.
     *
     * @param val data to be converted
     * @param start offset
     * @param len count
     *
     * @since 1.29
     */
    public static String toHex(byte[] val, int start, int len) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < len; i++) {
            byte b = val[start + i];
            buf.append(DEC2HEX[(b & 0xf0) >> 4]);
            buf.append(DEC2HEX[b & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Decodes data encoded using {@link #toHex(byte[],int,int) toHex}.
     *
     * @param hex data to be converted
     * @param start offset
     * @param len count
     *
     * @throws IOException if input does not represent hex encoded value
     *
     * @since 1.29
     */
    public static byte[] fromHex(char[] hex, int start, int len)
    throws IOException {
        if (hex == null) {
            throw new IOException("null");
        }

        int i = hex.length;

        if ((i % 2) != 0) {
            throw new IOException("odd length");
        }

        byte[] magic = new byte[i / 2];

        for (; i > 0; i -= 2) {
            String g = new String(hex, i - 2, 2);

            try {
                magic[(i / 2) - 1] = (byte) Integer.parseInt(g, 16);
            } catch (NumberFormatException ex) {
                throw new IOException(ex.getLocalizedMessage());
            }
        }

        return magic;
    }

    /**
     * Check if all passed characters match XML expression [2].
     * @return true if no escaping necessary
     * @throws CharConversionException if contains invalid chars
     */
    private static boolean checkAttributeCharacters(String chars)
    throws CharConversionException {
        boolean escape = false;

        for (int i = 0; i < chars.length(); i++) {
            char ch = chars.charAt(i);

            if (((int) ch) <= 93) { // we are UNICODE ']'

                switch (ch) {
                case 0x9:
                case 0xA:
                case 0xD:

                    continue;

                case '\'':
                case '"':
                case '<':
                case '&':
                    escape = true;

                    continue;

                default:

                    if (((int) ch) < 0x20) {
                        throw new CharConversionException("Invalid XML character &#" + ((int) ch) + ";.");
                    }
                }
            }
        }

        return escape == false;
    }

    /**
     * Check if all passed characters match XML expression [2].
     * @return true if no escaping necessary
     * @throws CharConversionException if contains invalid chars
     */
    private static boolean checkContentCharacters(String chars)
    throws CharConversionException {
        boolean escape = false;

        for (int i = 0; i < chars.length(); i++) {
            char ch = chars.charAt(i);

            if (((int) ch) <= 93) { // we are UNICODE ']'

                switch (ch) {
                case 0x9:
                case 0xA:
                case 0xD:

                    continue;

                case '>': // only ]]> is dangerous

                    if (escape) {
                        continue;
                    }

                    escape = (i > 0) && (chars.charAt(i - 1) == ']');

                    continue;

                case '<':
                case '&':
                    escape = true;

                    continue;

                default:

                    if (((int) ch) < 0x20) {
                        throw new CharConversionException("Invalid XML character &#" + ((int) ch) + ";.");
                    }
                }
            }
        }

        return escape == false;
    }

    /**
     * Try to normalize a document by removing nonsignificant whitespace.
     * @see "#62006"
     */
    private static Document normalize(Document orig) throws IOException {
        DocumentBuilder builder = null;
        DocumentBuilderFactory factory = getFactory(false, false);
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Cannot create parser satisfying configuration parameters: " + e, e); //NOI18N
        }

        DocumentType doctype = null;
        NodeList nl = orig.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof DocumentType) {
                // We cannot import DocumentType's, so we need to manually copy it.
                doctype = (DocumentType) nl.item(i);
            }
        }
        Document doc;
        if (doctype != null) {
            doc = builder.getDOMImplementation().createDocument(
                orig.getDocumentElement().getNamespaceURI(),
                orig.getDocumentElement().getTagName(),
                builder.getDOMImplementation().createDocumentType(
                    orig.getDoctype().getName(),
                    orig.getDoctype().getPublicId(),
                    orig.getDoctype().getSystemId()));
            // XXX what about entity decls inside the DOCTYPE?
            doc.removeChild(doc.getDocumentElement());
        } else {
            doc = builder.newDocument();
        }
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (!(node instanceof DocumentType)) {
                try {
                    doc.appendChild(doc.importNode(node, true));
                } catch (DOMException x) {
                    // Thrown in NB-Core-Build #2896 & 2898 inside GeneratedFilesHelper.applyBuildExtensions
                    throw new IOException("Could not import or append " + node + " of " + node.getClass(), x);
                }
            }
        }
        doc.normalize();
        nl = doc.getElementsByTagName("*"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            removeXmlBase(e);
            NodeList nl2 = e.getChildNodes();
            for (int j = 0; j < nl2.getLength(); j++) {
                Node n = nl2.item(j);
                if (n instanceof Text && ((Text) n).getNodeValue().trim().length() == 0) {
                    e.removeChild(n);
                    j--; // since list is dynamic
                }
            }
        }
        return doc;
    }

    /**
     * Append a child element to the parent at the specified location.
     *
     * Starting with a valid document, append an element according to the schema
     * sequence represented by the <code>order</code>.  All existing child elements must be
     * include as well as the new element.  The existing child element following
     * the new child is important, as the element will be 'inserted before', not
     * 'inserted after'.
     *
     * @param parent parent to which the child will be appended
     * @param el element to be added
     * @param order order of the elements which must be followed
     * @throws IllegalArgumentException if the order cannot be followed, either
     * a missing existing or new child element is not specified in order
     *
     * @since 8.4
     */
    public static void appendChildElement(Element parent, Element el, String[] order) throws IllegalArgumentException {
        List<String> l = Arrays.asList(order);
        int index = l.indexOf(el.getLocalName());

        // ensure the new new element is contained in the 'order'
        if (index == -1) {
            throw new IllegalArgumentException("new child element '"+ el.getLocalName() + "' not specified in order " + l); // NOI18N
        }

        List<Element> elements = findSubElements(parent);
        Element insertBefore = null;

        for (Element e : elements) {
            int index2 = l.indexOf(e.getLocalName());
            // ensure that all existing elements are in 'order'
            if (index2 == -1) {
                throw new IllegalArgumentException("Existing child element '" + e.getLocalName() + "' not specified in order " + l);  // NOI18N
            }
            if (index2 > index) {
                insertBefore = e;
                break;
            }
        }

        parent.insertBefore(el, insertBefore);
    }

    /**
     * Find all direct child elements of an element.
     * Children which are all-whitespace text nodes or comments are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     * 
     * @since 8.4
     */
    public static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text) n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // OK, ignore
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }

    /**
     * Search for an XML element in the direct children of parent only.
     *
     * This compares localName (nodeName if localName is null) to name,
     * and checks the tags namespace with the provided namespace.
     * A <code>null</code> namespace will match any namespace.
     *
     * <ul>This is differs from the DOM version by:
     * <li>not searching recursively</li>
     * <li>returns a single result</li>
     * </ul>
     *
     * @param parent a parent element
     * @param name the intended local name
     * @param namespace the intended namespace (or null)
     * @return the one child element with that name, or null if none
     * @throws IllegalArgumentException if there is multiple elements of the same name
     * 
     * @since 8.4
     */
    public static Element findElement(Element parent, String name, String namespace) throws IllegalArgumentException {
        Element result = null;
        NodeList l = parent.getChildNodes();
        int nodeCount = l.getLength();
        for (int i = 0; i < nodeCount; i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Node node = l.item(i);
                String localName = node.getLocalName();
                localName = localName == null ? node.getNodeName() : localName;
                
                if (name.equals(localName)
			&& (namespace == null || namespace.equals(node.getNamespaceURI()))) {
                    if (result == null) {
                        result = (Element)node;
                    } else {
                        throw new IllegalArgumentException("more than one element with same name found");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Extract nested text from a node.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     * 
     * @since 8.4
     */
    public static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }

    /**
     * Convert an XML fragment from one namespace to another.
     * 
     * @param from element to translate
     * @param namespace namespace to be translated to
     * @return
     * 
     * @since 8.4
     */
    public static Element translateXML(Element from, String namespace) {
        Element to = from.getOwnerDocument().createElementNS(namespace, from.getLocalName());
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            Node newNode;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                newNode = translateXML((Element) node, namespace);
            } else {
                newNode = node.cloneNode(true);
            }
            to.appendChild(newNode);
        }
        NamedNodeMap m = from.getAttributes();
        for (int i = 0; i < m.getLength(); i++) {
            Node attr = m.item(i);
            to.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
        return to;
    }

    /**
     * Copy elements from one document to another attaching at the specified element
     * and translating the namespace.
     *
     * @param from copy the children of this element (exclusive)
     * @param to where to attach the copied elements
     * @param newNamespace destination namespace
     * 
     * @since 8.4
     */
    public static void copyDocument(Element from, Element to, String newNamespace) {
        Document doc = to.getOwnerDocument();
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            Node newNode = null;
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                Element oldElement = (Element) node;
                newNode = doc.createElementNS(newNamespace, oldElement.getTagName());
                NamedNodeMap m = oldElement.getAttributes();
                Element newElement = (Element) newNode;
                for (int index = 0; index < m.getLength(); index++) {
                    Node attr = m.item(index);
                    newElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
                copyDocument(oldElement, newElement, newNamespace);
            } else {
                newNode = node.cloneNode(true);
                newNode = to.getOwnerDocument().importNode(newNode, true);
            }
            if (newNode != null) {
                to.appendChild(newNode);
            }
        }
    }

    /**
     * Create an XML error handler that rethrows errors and fatal errors and logs warnings.
     * @return a standard error handler
     *
     * @since 8.4
     */
    public static ErrorHandler defaultErrorHandler() {
        return new ErrHandler();
    }

    private static final class ErrHandler implements ErrorHandler {

        ErrHandler() {}

        private void annotate(SAXParseException exception) throws SAXException {
            Exceptions.attachMessage(exception, "Occurred at: " + exception.getSystemId() + ":" + exception.getLineNumber()); // NOI18N
        }

        public @Override void fatalError(SAXParseException exception) throws SAXException {
            annotate(exception);
            throw exception;
        }

        public @Override void error(SAXParseException exception) throws SAXException {
            annotate(exception);
            throw exception;
        }

        public @Override void warning(SAXParseException exception) throws SAXException {
            annotate(exception);
            Logger.getLogger(XMLUtil.class.getName()).log(Level.INFO, null, exception);
        }

    }
}

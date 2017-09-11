/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.updater;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class collecting library methods related to XML processing.
 * Stolen from nbbuild/antsrc and openide/.../xml.
 * @author Petr Kuzel, Jesse Glick
 */
public final class XMLUtil extends Object {
    static final Logger LOG = Logger.getLogger(XMLUtil.class.getPackage().getName());

    public static Document parse (
            InputSource input, 
            boolean validate, 
            boolean namespaceAware,
            ErrorHandler errorHandler,             
            EntityResolver entityResolver
        ) throws IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
        factory.setValidating(validate);
        factory.setNamespaceAware(namespaceAware);            
        DocumentBuilder builder = null;
        try {
             builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex);
        }
            
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }

        assert input != null : "InputSource cannot be null";
        
        try {
            return builder.parse(input);
        } catch (SAXException ex) {
            StringBuilder msg = new StringBuilder();
            msg.append("Cannot parse");
            msg.append("Thread.cCL: ").append(Thread.currentThread().getContextClassLoader());
            throw new SAXException(msg.toString(), ex);
        }
    }
    
    public static Document createDocument(String rootQName) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder ().getDOMImplementation ().createDocument (null, rootQName, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
    
    public static void write(Document doc, OutputStream out) throws IOException {
        // XXX note that this may fail to write out namespaces correctly if the document
        // is created with namespaces and no explicit prefixes; however no code in
        // this package is likely to be doing so
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() { // #208369
            @Override public ClassLoader run() {
                return new ClassLoader(ClassLoader.getSystemClassLoader().getParent()) {
                    @Override public InputStream getResourceAsStream(String name) {
                        if (name.startsWith("META-INF/services/")) { // NOI18N
                            return new ByteArrayInputStream(new byte[0]); // JAXP #6723276
                        }
                        return super.getResourceAsStream(name);
                    }
                };
            }
        }));
        Transformer t = null;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            LOG.info("Use XML Transformer: " + t);
            DocumentType dt = doc.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            t.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw new IOException("XML Transformer: " + t, e); // NOI18N
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException("XML Transformer: " + t, e); // NOI18N
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    /** Entity resolver that knows about AU DTDs, so no network is needed.
     * @author Jesse Glick
     */
    public static EntityResolver createAUResolver() {
        return new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicID, String systemID) throws IOException, SAXException {
                if ("-//NetBeans//DTD Autoupdate Catalog 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-1_0.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-1_0.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_0.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_0.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_2.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_2.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_3.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_3.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_4.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_4.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.5//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_5.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.5//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_5.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.6//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_6.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.7//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_7.dtd").toString()); // NOI18N
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.7//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_7.dtd").toString()); // NOI18N
                } else {
                    if (systemID.endsWith(".dtd")) { // NOI18N
                        return new InputSource(new ByteArrayInputStream(new byte[0]));
                    }
                    URL u = new URL(systemID);
                    URLConnection oc = u.openConnection();
                    oc.setConnectTimeout(5000);
                    return new InputSource(oc.getInputStream());
                }
            }
        };
    }
    
}

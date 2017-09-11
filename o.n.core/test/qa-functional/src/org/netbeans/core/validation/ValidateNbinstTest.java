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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.core.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.layers.LayerCacheManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.Log;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.modules.Dependency;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Checks consistency of System File System contents.
 */
public class ValidateNbinstTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("org.openide.util.lookup.level", "FINE");
    }

    private static final String SFS_LB = "SystemFileSystem.localizingBundle";

    private ClassLoader contextClassLoader;   
    
    public ValidateNbinstTest(String name) {
        super (name);
    }

    @Override
    protected int timeOut() {
        // sometimes can deadlock and then we need to see the thread dump
        return 1000 * 60 * 10;
    }
    
    public @Override void setUp() throws Exception {
        clearWorkDir();
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(Lookup.getDefault().lookup(ClassLoader.class));
                return null;
            }
        });
    }
    
    public @Override void tearDown() {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return null;
            }
        });
    }
    
    protected @Override boolean runInEQ() {
        return true;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(ValidateNbinstTest.class).
                clusters("(?!ergonomics).*").enableClasspathModules(false).enableModules(".*").gui(false).suite());
        return suite;
    }

    private void assertNoErrors(String message, Collection<String> warnings) {
        if (warnings.isEmpty()) {
            return;
        }
        StringBuilder b = new StringBuilder(message);
        for (String warning : new TreeSet<String>(warnings)) {
            b.append('\n').append(warning);
        }
        fail(b.toString());
    }

    private static class TestHandler extends Handler {
        List<String> errors = new ArrayList<String>();
        
        TestHandler () {}
        
        public @Override void publish(LogRecord rec) {
            if (Level.WARNING.equals(rec.getLevel()) || Level.SEVERE.equals(rec.getLevel())) {
                errors.add(MessageFormat.format(rec.getMessage(), rec.getParameters()));
            }
        }
        
        List<String> errors() {
            return errors;
        }

        public @Override void flush() {}

        public @Override void close() throws SecurityException {}
    }

    public void testNbinstHost() throws Exception {
        TestHandler handler = new TestHandler();
        final Logger LOG = Logger.getLogger("org.netbeans.core.startup.InstalledFileLocatorImpl");
        try {
            LOG.addHandler(handler);
            implNbinstHost(handler);
        } finally {
            LOG.removeHandler(handler);
        }
    }
    
    private void implNbinstHost(TestHandler handler) throws ParserConfigurationException, IOException, IllegalArgumentException, SAXException {
        FileObject libs = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");                
        if (libs != null) {
            final List<FileObject> schemas = new ArrayList<FileObject>(3);
            schemas.add(null);
            final FileObject schema2 = FileUtil.getConfigFile("ProjectXMLCatalog/library-declaration/2.xsd");
            if (schema2 != null) {
                schemas.add(schema2);
            }
            final FileObject schema3 = FileUtil.getConfigFile("ProjectXMLCatalog/library-declaration/3.xsd");
            if (schema3 != null) {
                schemas.add(schema3);
            }
next:       for (FileObject lib : libs.getChildren()) {
                SAXException lastException = null;
                for (FileObject schema : schemas) {
                    lastException = null;
                    try {
                        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                        docBuilderFactory.setValidating(true);
                        if (schema != null) {
                            docBuilderFactory.setNamespaceAware(true);
                            docBuilderFactory.setAttribute(
                                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",   //NOI18N
                                "http://www.w3.org/2001/XMLSchema");                        //NOI18N
                            docBuilderFactory.setAttribute(
                                "http://java.sun.com/xml/jaxp/properties/schemaSource",     //NOI18N
                                schema.toURI().toString());
                        }
                        final DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
                        builder.setErrorHandler(XMLUtil.defaultErrorHandler());
                        builder.setEntityResolver(EntityCatalog.getDefault());
                        Document doc = builder.parse(new InputSource(lib.toURL().toString()));
                        NodeList nl = doc.getElementsByTagName("resource");
                        for (int i = 0; i < nl.getLength(); i++) {
                            Element resource = (Element) nl.item(i);
                            validateNbinstURL(new URL(XMLUtil.findText(resource)), handler, lib);
                        }
                        continue next;
                    } catch (SAXException e) {
                        lastException = e;
                    }
                }
                throw lastException;
            }
        }
        for (FileObject f : NbCollections.iterable(FileUtil.getConfigRoot().getChildren(true))) {
            for (String attr : NbCollections.iterable(f.getAttributes())) {
                if (attr.equals("instanceCreate")) {
                    continue; // e.g. on Services/Hidden/org-netbeans-lib-jakarta_oro-antlibrary.instance prints stack trace
                }
                Object val = f.getAttribute(attr);
                if (val instanceof URL) {
                    validateNbinstURL((URL) val, handler, f);
                }
            }
        }
        assertNoErrors("No improper nbinst URLs", handler.errors());
    }
    
    private void validateNbinstURL(URL u, TestHandler handler, FileObject f) {
        URL u2 = FileUtil.getArchiveFile(u);
        if (u2 != null) {
            u = u2;
        }
        if ("nbinst".equals(u.getProtocol())) {
            List<String> errors = handler.errors();
            try {
                int len = errors.size();
                u.openStream().close();
                if (errors.size() == len + 1) {
                    errors.set(len, f.getPath() + ": " + errors.get(len));
                }
            } catch (IOException x) {
                errors.add(f.getPath() + ": cannot open " + u + ": " + x);
            }
        }
    }

}

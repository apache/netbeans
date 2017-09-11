/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.util;

import com.sun.tools.xjc.XJC2Task;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author lukas
 */
public class SchemaTestBase extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(SchemaTestBase.class.getName());
    private static final SchemaFactory sFactory;
    private static final DocumentBuilderFactory dbf;
    private Schema schema;
    private File file;
    private String schemaResource;


    static {
        // create a SchemaFactory capable of understanding WXS schemas
        sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    protected SchemaTestBase() {
        super(""); //NOI18N
    }

    private SchemaTestBase(String name, String resource) {
        super(name);
        schemaResource = resource;
    }

    private SchemaTestBase(String name, Schema s, File f) {
        super(name);
        schema = s;
        this.file = f;
    }

    protected String[] getSchemas() {
        return new String[0];
    }

    protected List<File> getTestCases() {
        List<File> testCases = new ArrayList<File>();
        File[] testFiles = getDataDir().listFiles();
        if (testFiles != null) {
            for (File f : testFiles) {
                if (f.isFile()) {
                    testCases.add(f); //NOI18N
                }
            }
        }
        return testCases;
    }

    @Override
    public void setUp() throws IOException {
        clearWorkDir();
    }

    public void validate() {
        assertNotNull("null schema", schema); //NOI18N
        LOG.info("Validating: " + file.getAbsolutePath()); //NOI18N
        try {
            // parse an XML document into a DOM tree
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(file);

            Validator validator = schema.newValidator();
            try {
                // validate the DOM tree
                validator.validate(new DOMSource(document));
            } catch (SAXException ex) {
                LOG.log(Level.SEVERE, null, ex);
                fail("validation of " + file.getName() + " failed: " + ex.getMessage()); //NOI18N
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
            fail("error parsing document " + file.getName()); //NOI18N
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void compileSchema() {
        try {
            XJC2Task xjc = new XJC2Task();
            xjc.setDestdir(getWorkDir());
            xjc.setPackage("some.testpackage"); //NOI18N
            xjc.setSchema(ClassLoader.getSystemResource(schemaResource).toString());
            xjc.execute();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            fail("Cannot compile schema: " + schemaResource); //NOI18N
        }
    }

    protected static TestSuite createTestSuite(SchemaTestBase t) {
        TestSuite ts = new NbTestSuite();
        List<File> testCases = t.getTestCases();
        Collections.sort(testCases);
        for (File f : testCases) {
            assertTrue("dir not allowed", f.isFile()); //NOI18N
            ts.addTest(new SchemaTestBase("validate", createSchema(t.getSchemas()), f)); //NOI18N
        }
        for (String s : t.getSchemas()) {
            ts.addTest(new SchemaTestBase("compileSchema", s)); //NOI18N
        }

        return ts;
    }

    private static Schema createSchema(String... resources) {
        Schema s = null;
        List<InputStream> iss = new ArrayList<InputStream>(resources.length);
        Source[] sources = new StreamSource[resources.length];
        for (int i = 0; i < resources.length; i++) {
            InputStream is = ClassLoader.getSystemResourceAsStream(resources[i]);
            sources[i] = new StreamSource(is);
            iss.add(is);
        }
        // load a WXS schema, represented by a Schema instance
        try {
            s = sFactory.newSchema(sources);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            for (InputStream is : iss) {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SchemaTestBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        assertNotNull("null schema", s); //NOI18N
        return s;
    }
}

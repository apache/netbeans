/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.driver;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.test.DOMCompare;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Andrei Badea
 */
public class JDBCDriverConvertorTest extends TestBase {
    
    public JDBCDriverConvertorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
        Util.deleteDriverFiles();
    }
    
    public void testReadXml() throws Exception {
        // DTD version 1.0
        FileObject fo = createDriverFile10("org_foo_FooDriver_10.xml", Util.getDriversFolder());
        DataObject dobj = DataObject.find(fo);
        InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        JDBCDriver driver = (JDBCDriver)ic.instanceCreate();
        assertEquals("foo_driver", driver.getName());
        assertEquals("org.foo.FooDriver", driver.getClassName());
        assertEquals(2, driver.getURLs().length);
        assertEquals(new URL("file:///foo1.jar"), driver.getURLs()[0]);
        assertEquals(new URL("file:///foo2.jar"), driver.getURLs()[1]);
        
        // DTD version 1.1
        fo = createDriverFile11("org_foo_FooDriver_11.xml", Util.getDriversFolder());
        dobj = DataObject.find(fo);
        ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        driver = (JDBCDriver)ic.instanceCreate();
        assertEquals("foo_driver", driver.getName());
        assertEquals("Foo Driver", driver.getDisplayName());
        assertEquals("org.foo.FooDriver", driver.getClassName());
        assertEquals(2, driver.getURLs().length);
        assertEquals(new URL("file:///foo1.jar"), driver.getURLs()[0]);
        assertEquals(new URL("file:///foo2.jar"), driver.getURLs()[1]);
    }
    
    public void testWriteXml() throws Exception {
        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[] { new URL("file:///bar1.jar"), new URL("file:///bar2.jar") });
        JDBCDriverConvertor.create(driver);
        
        FileObject fo = Util.getDriversFolder().getFileObject("org_bar_BarDriver.xml");
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertFalse("JDBCDriverConvertor generates invalid XML acc to the DTD!", errHandler.error);
        
        Document goldenDoc = null;
        input = getClass().getResourceAsStream("bar-driver.xml");
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, null, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
    }
    
    public void testLookup() throws Exception {
        FileObject parent = Util.getDriversFolder();
        createDriverFile11("org_foo_FooDriver.xml", parent);
        Lookup.Result result = Lookups.forPath(parent.getPath()).lookup(new Lookup.Template(JDBCDriver.class));
        Collection instances = result.allInstances();
        assertEquals(1, instances.size()); 
    }
    
    public void testEncodeURL() throws Exception {
        assertEquals(new URL("file:///test%20file#fragment"), JDBCDriverConvertor.encodeURL(new URL("file:///test file#fragment")));
        assertEquals(new URL("file:///test%20file"), JDBCDriverConvertor.encodeURL(new URL("file:///test file")));
    }
    
    private static FileObject createDriverFile10(String fileName, FileObject folder) throws Exception {
        URL[] urls = new URL[] {
            new URL("file:///foo1.jar"),
            new URL("file:///foo2.jar"),
        };
        return createDriverFile10(fileName, folder, urls);
    }
    
    private static FileObject createDriverFile10(String fileName, FileObject folder, URL[] urls) throws Exception {
        return createDriverFile(10, fileName, folder, urls);
    }
    
    private static FileObject createDriverFile11(String fileName, FileObject folder) throws Exception {
        URL[] urls = new URL[] {
            new URL("file:///foo1.jar"),
            new URL("file:///foo2.jar"),
        };
        return createDriverFile(11, fileName, folder, urls);
    }
    
    private static FileObject createDriverFile(int version, String fileName, FileObject folder, URL[] urls) throws Exception {
        String publicIdVer = version == 10 ? "1.0" : "1.1";
        String systemIdVer = version == 10 ? "1_0" : "1_1";
        
        FileObject fo = folder.createData(fileName);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver " + publicIdVer + "//EN' 'http://www.netbeans.org/dtds/jdbc-driver-" + systemIdVer + ".dtd'>");
                writer.write("<driver>");
                writer.write("<name value='foo_driver'/>");
                if (version == 11) {
                    writer.write("<display-name value='Foo Driver'/>");
                }
                writer.write("<class value='org.foo.FooDriver'/>");
                writer.write("<urls>");
                for (int i = 0; i < urls.length; i++) {
                    writer.write("<url value='" + urls[i].toExternalForm() + "'/>");
                }
                writer.write("</urls>");
                writer.write("</driver>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }           
        return fo;
    }
    
    private static final class ErrorHandlerImpl implements ErrorHandler {
        
        public boolean error = false;
        
        public void warning(SAXParseException exception) throws SAXException {
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            error = true;
        }

        public void error(SAXParseException exception) throws SAXException {
            error = true;
        }
    }
}

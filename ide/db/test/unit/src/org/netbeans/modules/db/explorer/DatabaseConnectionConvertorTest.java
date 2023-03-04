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

package org.netbeans.modules.db.explorer;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.db.test.DOMCompare;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
public class DatabaseConnectionConvertorTest extends TestBase {
    
    public DatabaseConnectionConvertorTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
        Util.clearConnections();
    }
    
    public void testReadXml() throws Exception {
        FileObject fo = createConnectionFile("connection.xml", Util.getConnectionsFolder());
        DataObject dobj = DataObject.find(fo);
        InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
        assertNotNull(ic);
        
        DatabaseConnection conn = (DatabaseConnection)ic.instanceCreate();
        assertEquals("org.foo.FooDriver", conn.getDriver());
        assertEquals("foo_driver", conn.getDriverName());
        assertEquals("jdbc:foo:localhost", conn.getDatabase());
        assertEquals("schema", conn.getSchema());
        assertEquals("user", conn.getUser());
        assertEquals("password", conn.getPassword());
        assertTrue(conn.rememberPassword());
    }
    
    public void testWriteNullPassword() throws Exception {
        testWriteXml(null, true, "null-pwd-connection.xml");
    }
    
    public void testWriteXml() throws Exception {
        testWriteXml("password", true, "bar-connection.xml");
    }
    
    private void testWriteXml(String password, boolean savePassword,
            String goldenFileName) throws Exception {
        
        DatabaseConnection conn = new DatabaseConnection("org.bar.BarDriver", 
                "bar_driver", "jdbc:bar:localhost", "schema", "user", password,
                savePassword);
        
        DatabaseConnectionConvertor.create(conn);
        
        FileObject fo = Util.getConnectionsFolder().getChildren()[0];
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertFalse("DatabaseConnectionConvertor generates invalid XML acc to the DTD!", errHandler.error);
        
        Document goldenDoc = null;
        input = getClass().getResourceAsStream(goldenFileName);

        errHandler = new ErrorHandlerImpl();
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
        assertFalse("DatabaseConnectionConvertor generates invalid XML acc to the DTD!", errHandler.error);
    }
    
    public void testSaveOnPropertyChange() throws Exception {
        DatabaseConnection dbconn = new DatabaseConnection("a", "b", "c", "d", "e", (String) null);
        FileObject fo = DatabaseConnectionConvertor.create(dbconn).getPrimaryFile();
        
        class FCL extends FileChangeAdapter {
            
            private final CountDownLatch latch = new CountDownLatch(1);
            
            @Override
            public void fileChanged(FileEvent fe) {
                latch.countDown();
            }
            
            public void await() throws InterruptedException {
                latch.await();
            }
        }
        
        FCL fcl = new FCL();
        fo.addFileChangeListener(fcl);
        
        dbconn.setDriver("org.bar.BarDriver");
        dbconn.setDriverName("bar_driver");
        dbconn.setDatabase("jdbc:bar:localhost");
        dbconn.setSchema("schema");
        dbconn.setUser("user");
        dbconn.setPassword("password");
        dbconn.setRememberPassword(true);
        
        fcl.await();
        
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl();
        Document doc = null;
        InputStream input = fo.getInputStream();
        try {
            doc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertFalse("Error while parsing XML", errHandler.error);
        errHandler = new ErrorHandlerImpl();
        Document goldenDoc = null;
        input = getClass().getResourceAsStream("bar-connection.xml");
        try {
            goldenDoc = XMLUtil.parse(new InputSource(input), true, true, errHandler, EntityCatalog.getDefault());
        } finally {
            input.close();
        }
        
        assertTrue(DOMCompare.compareDocuments(doc, goldenDoc));
        assertFalse("Error while parsing XML", errHandler.error);
    }
    
    public void testLookup() throws Exception {
        FileObject parent = Util.getConnectionsFolder();
        createConnectionFile("connection.xml", parent);
        Lookup lookup = Lookups.forPath(parent.getPath());
        Lookup.Result<DatabaseConnection> result = lookup.lookup(new Lookup.Template<DatabaseConnection>(DatabaseConnection.class));
        Collection<? extends DatabaseConnection> instances = result.allInstances();
        assertEquals(1, instances.size()); 
    }
    
    public void testDecodePassword() throws Exception {
        assertNotNull(DatabaseConnectionConvertor.decodePassword(new byte[0]));
        assertTrue(DatabaseConnectionConvertor.decodePassword(new byte[0]).isEmpty());
        assertEquals("password", DatabaseConnectionConvertor.decodePassword("password".getBytes(StandardCharsets.UTF_8)));
        try {
            DatabaseConnectionConvertor.decodePassword(new byte[] { (byte)0xff, (byte)0xff, (byte)0xff });
            fail();
        } catch (CharacterCodingException e) {}
    }
    
    public void testDecodeBase64() {
        final byte[] data = "P4ssw\u00f8rd".getBytes(StandardCharsets.UTF_8);
        final String encoded = "UDRzc3fDuHJk";
        final byte[] result = DatabaseConnectionConvertor.decodeBase64(encoded);
        assertEquals(data.length, result.length);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], result[i]);
        }
    }

    private static FileObject createConnectionFile(String name, FileObject folder) throws Exception {
        FileObject fo = folder.createData(name);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE connection PUBLIC '-//NetBeans//DTD Database Connection 1.0//EN' 'http://www.netbeans.org/dtds/connection-1_0.dtd'>");
                writer.write("<connection>");
                writer.write("<driver-class value='org.foo.FooDriver'/>");
                writer.write("<driver-name value='foo_driver'/>");
                writer.write("<database-url value='jdbc:foo:localhost'/>");
                writer.write("<schema value='schema'/>");
                writer.write("<user value='user'/>");
                char[] password = "password".toCharArray();

                // use Keyring API instead Base64.byteArrayToBase64
                assert name != null : "The parameter name cannot be null.";
                Keyring.save(name, password, NbBundle.getMessage(DatabaseConnectionConvertor.class, "DatabaseConnectionConvertor.password_description", name)); //NOI18N
                writer.write("</connection>");
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
        
        @Override
        public void warning(SAXParseException exception) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            error = true;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            error = true;
        }
    }
}

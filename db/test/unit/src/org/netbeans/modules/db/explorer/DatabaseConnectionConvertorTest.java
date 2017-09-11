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

package org.netbeans.modules.db.explorer;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharacterCodingException;
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
        assertEquals("password", DatabaseConnectionConvertor.decodePassword("password".getBytes("UTF-8")));
        try {
            DatabaseConnectionConvertor.decodePassword(new byte[] { (byte)0xff, (byte)0xff, (byte)0xff });
            fail();
        } catch (CharacterCodingException e) {}
    }
    
    private static FileObject createConnectionFile(String name, FileObject folder) throws Exception {
        FileObject fo = folder.createData(name);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
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

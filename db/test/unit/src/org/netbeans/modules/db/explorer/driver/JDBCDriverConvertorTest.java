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

package org.netbeans.modules.db.explorer.driver;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
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
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
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

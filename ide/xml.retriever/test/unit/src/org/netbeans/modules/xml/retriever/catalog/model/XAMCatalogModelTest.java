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

/*
 * XAMCatalogModelTest.java
 * JUnit based test
 *
 * Created on December 7, 2006, 3:03 PM
 */

package org.netbeans.modules.xml.retriever.catalog.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.swing.text.BadLocationException;
import junit.framework.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class XAMCatalogModelTest extends TestCase implements PropertyChangeListener{
    
    public XAMCatalogModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XAMCatalogModelTest.class);
        
        return suite;
    }
    
    
    int eventFired = 0;
    
    public void testCatalogModel() throws URISyntaxException, BadLocationException {
        File inputFile = null;
        
        try {
            inputFile = new File(XAMCatalogModelTest.class.getResource("catalog.xml").toURI());
        } catch (URISyntaxException ex) {
            assert false;
            ex.printStackTrace();
            return;
        }
        
        FileObject inputFO = FileUtil.toFileObject(FileUtil.
                normalizeFile(inputFile));
        
        ModelSource source = null;
        try {
            source = TestUtil.createModelSource(inputFO, true);
        } catch (CatalogModelException ex) {
            assert false;
            ex.printStackTrace();
            return;
        }
        
        CatalogModelFactory instance = CatalogModelFactory.getInstance();
        CatalogModel cm = instance.getModel(source);
        assertNotNull(cm);
        
        Catalog cat = cm.getRootComponent();
        assertNotNull(cat);
        
        cat.addPropertyChangeListener(this);
        
        /* system related tests*/
        List<System> systems = cat.getSystems();
        assertNotNull(systems);
        
        assertEquals(1, systems.size());
        System system = systems.get(0);
        
        system.addPropertyChangeListener(this);
        
        assertEquals("sysIDAttr", system.getSystemIDAttr());
        assertEquals("uriAttr", system.getURIAttr());
        assertEquals("xprojCatRefAttr", system.getXprojectCatalogFileLocationAttr());
        assertEquals("refFileAttr", system.getReferencingFileAttr());
        
        system.getModel().startTransaction();
        system.setSystemIDAttr(new URI("dummy"));
        system.setURIAttr(new URI("dummy"));
        system.setXprojectCatalogFileLocationAttr(new URI("dummy"));
        system.setReferencingFileAttr(new URI("dummy"));
        system.getModel().endTransaction();
        
        assertEquals(4, eventFired);
        eventFired = 0;
        
        assertEquals("dummy", system.getSystemIDAttr());
        assertEquals("dummy", system.getURIAttr());
        assertEquals("dummy", system.getXprojectCatalogFileLocationAttr());
        assertEquals("dummy", system.getReferencingFileAttr());
        
        System nsys = (System) system.copy(cat);
        
        cat.getModel().startTransaction();
        cat.removeSystem(system);
        cat.getModel().endTransaction();
        
        assertEquals(1, eventFired);
        eventFired = 0;
        
        assertEquals(0, cat.getSystems().size());
        
        cat.getModel().startTransaction();
        cat.addSystem(nsys);
        cat.getModel().endTransaction();
        
        assertEquals(1, eventFired);
        eventFired = 0;
        
        assertEquals(1, cat.getSystems().size());
        
        /* NextCatalog testcases*/
        List<NextCatalog> nextCatalogs = cat.getNextCatalogs();
        assertNotNull(nextCatalogs);
        
        assertEquals(1, nextCatalogs.size());
        NextCatalog nextCatalog = nextCatalogs.get(0);
        
        nextCatalog.addPropertyChangeListener(this);
        
        assertEquals("catalogAttr", nextCatalog.getCatalogAttr());
        
        cat.getModel().startTransaction();
        try {
            nextCatalog.setCatalogAttr(new URI("dummy"));
        } finally{
            cat.getModel().endTransaction();
        }
        
        assertEquals(1, eventFired);
        eventFired = 0;
        
        assertEquals("dummy", nextCatalog.getCatalogAttr());
        
        NextCatalog nncat = (NextCatalog) nextCatalog.copy(cat);
        
        cat.getModel().startTransaction();
        cat.removeNextCatalog(nextCatalog);
        cat.getModel().endTransaction();
        
        assertEquals(1, eventFired);
        eventFired = 0;
        
        assertEquals(0, cat.getNextCatalogs().size());
        
        cat.getModel().startTransaction();
        cat.addNextCatalog(nncat);
        cat.getModel().endTransaction();
        
        assertEquals(1, eventFired);
        eventFired = 0;
        
        assertEquals(1, cat.getNextCatalogs().size());
        
        /*Document doc = (Document) cat.getModel().getModelSource().getLookup().lookup(Document.class);;
        java.lang.System.out.println(doc.getText(0, doc.getLength()));*/
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        eventFired++;
    }
    
    
    public void testEmptyCatalogModel() throws URISyntaxException, BadLocationException {
        File inputFile = null;
        
        try {
            inputFile = new File(XAMCatalogModelTest.class.getResource("empty.xml").toURI());
        } catch (URISyntaxException ex) {
            assert false;
            ex.printStackTrace();
            return;
        }
        
        FileObject inputFO = FileUtil.toFileObject(FileUtil.
                normalizeFile(inputFile));
        
        ModelSource source = null;
        try {
            source = TestUtil.createModelSource(inputFO, true);
        } catch (CatalogModelException ex) {
            assert false;
            ex.printStackTrace();
            return;
        }
        
        CatalogModelFactory instance = CatalogModelFactory.getInstance();
        CatalogModel cm = instance.getModel(source);
        assertNotNull(cm);
        
        assertNotNull(cm.getRootComponent());
        
        NextCatalog nc = cm.getFactory().createNextCatalog();
        
        cm.startTransaction();
        try {
            cm.getRootComponent().addNextCatalog(nc);
            nc.setCatalogAttr(new URI("dummy"));
        } finally{
            cm.endTransaction();
        }
        
        assertEquals(1, cm.getRootComponent().getNextCatalogs().size());
        
        /*Document doc = (Document) cm.getModelSource().getLookup().lookup(Document.class);;
        java.lang.System.out.println(doc.getText(0, doc.getLength()));*/
    }
    
}

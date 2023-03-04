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
 * DepResolverFactoryImplTest.java
 * JUnit based test
 *
 * Created on January 18, 2006, 7:28 PM
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Formatter;
import java.util.logging.Level;
import javax.swing.text.Document;
import junit.framework.*;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.model.TestUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author girix
 */
public class CatalogModelTest extends TestCase {
    
    static {
        TestUtil.registerXMLKit();
    }
    
    public CatalogModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CatalogModelTest.class);
        
        return suite;
    }
    
    
    public void testDepResolver() throws URISyntaxException, CatalogModelException, IOException {
        
        Logger logger = Logger.getLogger(CatalogModelTest.class.getName());
        logger.setLevel(Level.ALL);
        StreamHandler sh = new MyHandler(System.out, new SimpleFormatter());
        sh.setLevel(logger.getLevel());
        //logger.addHandler(sh);
        CatalogFileWrapperDOMImpl.TEST_ENVIRONMENT = true;
        File catFile = new File(System.getProperty("java.io.tmpdir")+File.separator+CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION+".girish");
        catFile.delete();
        catFile.createNewFile();
        FileObject catFO = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
        URL url = getClass().getResource("dummyFile.txt");
        FileObject peerfo = FileUtil.toFileObject(new File(url.toURI()).getAbsoluteFile());
        System.out.println(catFile);
        CatalogWriteModel drz = new MyCatalogWriteModel(catFO);
        //CatalogWriteModel drz = new MyCatalogWriteModel(new File(System.getProperty("java.io.tmpdir")));
        drz.addURI(new URI("dummy/dummy"), peerfo);
        int length = drz.getCatalogEntries().size();
        
        assertEquals(1, length);
        
        //System.out.println("%%%%"+drz.getModelSource(new URI("dummy/dummy")).getFileObject());
        
        //System.out.println("$$$$"+LSResourceResolverFactory.getDefault().resolveResource(null, null, null, "dummy/dummy", url.toURI().toString()).getSystemId());
        
        //assertTrue(LSResourceResolverFactory.getDefault().resolveResource(null, null, null, "dummy/dummy", url.toURI().toString()).getSystemId().endsWith("dummyFile.txt"));
        
        FileObject fob = (FileObject) drz.getModelSource(new URI("dummy/dummy")).getLookup().lookup(FileObject.class);
        
        assertNotNull(fob);
        
        drz.removeURI(new URI("dummy/dummy"));
        
        length = drz.getCatalogEntries().size();
        
        assertEquals(0, length);
    }
    
    class MyCatalogWriteModel extends CatalogWriteModelImpl {
        MyCatalogWriteModel(File file) throws IOException{
            super(file);
        }
        MyCatalogWriteModel(FileObject fo) throws IOException{
            super(fo);
        }
        
        /**
         * This method could be overridden by the Unit testcase to return a special
         * ModelSource object for a FileObject with custom impl of classes added to the lookup.
         * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
         */
        protected ModelSource createModelSource(final FileObject thisFileObj, boolean editable) throws CatalogModelException{
            assert thisFileObj != null : "Null file object.";
            final CatalogModel catalogModel = createCatalogModel(thisFileObj);
            final DataObject dobj;
            try {
                dobj = DataObject.find(thisFileObj);
            } catch (DataObjectNotFoundException ex) {
                throw new CatalogModelException(ex);
            }
            Lookup proxyLookup = Lookups.proxy(
                    new Lookup.Provider() {
                public Lookup getLookup() {
                    Document document = null;
                    Logger l = Logger.getLogger(getClass().getName());
                    document = getDocument(thisFileObj);
                    return Lookups.fixed(new Object[] {
                        thisFileObj,
                        document,
                        dobj,
                        catalogModel
                    });
                }
            }
            );
            return new ModelSource(proxyLookup, editable);
        }
        
        private Document getDocument(FileObject fo){
            Document result = null;
            try {
                DataObject dObject = DataObject.find(fo);
                EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
                Document doc = ec.openDocument();
                if(doc instanceof BaseDocument)
                    return doc;
                result = new org.netbeans.editor.BaseDocument(true, fo.getMIMEType());
                String str = doc.getText(0, doc.getLength());
                result.insertString(0,str,null);
                
            } catch (Exception dObjEx) {
                return null;
            }
            return result;
        }
    }
    
    class MyHandler extends StreamHandler{
        public MyHandler(OutputStream out, Formatter fmt){
            super(out, fmt);
        }
        public void publish(java.util.logging.LogRecord record) {
            super.publish(record);
            flush();
        }
        
    }
    
}

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
 * XAMCatalogWriteModelTest.java
 * JUnit based test
 *
 * Created on December 14, 2006, 4:19 PM
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModelFactory;
import org.netbeans.modules.xml.retriever.catalog.model.TestUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class XAMCatalogWriteModelTest extends TestCase {
    
    public XAMCatalogWriteModelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XAMCatalogWriteModelTest.class);
        
        return suite;
    }
    
    public void testSearchURI() {
        URI locationURI = null;
        try {
            locationURI = new URI("sysIDAttr");
        } catch (URISyntaxException ex) {
        }
        
        XAMCatalogWriteModelImpl instance = getTestCatModelInstance();
        
        URI result = instance.searchURI(locationURI);
        assertNotNull(result);
    }
    
    public void testAddAndRemoveURI() throws Exception {
        URI leftURI = new URI("girish");
        URI rightURI = new URI("kumar");
        FileObject fileObj = null;
        MyXAMCatalogWriteModel instance = getTestCatModelInstance();
        
        int start = instance.getCatalogEntries().size();
        
        instance.addURI(leftURI, rightURI);
        
        assertEquals(start+1, instance.getCatalogEntries().size());
        
        instance.removeURI(leftURI);
        
        assertEquals(start, instance.getCatalogEntries().size());
    }
    
    public MyXAMCatalogWriteModel getTestCatModelInstance(){
        FileObject inputFile = null;
        
        try {
            inputFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(XAMCatalogWriteModelTest.class.
                    getResource("catalog.xml").toURI())));
        } catch (URISyntaxException ex) {
            assert false;
            ex.printStackTrace();
            return null;
        }
        
        MyXAMCatalogWriteModel instance = null;
        try {
            instance = new MyXAMCatalogWriteModel(inputFile);
        } catch (IOException ex) {
            assert false;
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
            assert false;
        }
        
        return instance;
    }
    
    public void testAddNextCatalog() throws Exception {
        URI leftURI = new URI("girish");
        FileObject fileObj = null;
        MyXAMCatalogWriteModel instance = getTestCatModelInstance();
        
        int start = instance.getCatalogEntries().size();
        
        instance.addNextCatalog(leftURI, true);
        
        assertEquals(start+1, instance.getCatalogEntries().size());
        
        instance.removeNextCatalog(leftURI);
        
        assertEquals(start, instance.getCatalogEntries().size());
    }
    
    
    
    class MyXAMCatalogWriteModel extends XAMCatalogWriteModelImpl{
        public MyXAMCatalogWriteModel(FileObject catFile) throws IOException, CatalogModelException{
            super(catFile);
        }
        
        protected ModelSource createModelSource(FileObject catFileObject) throws CatalogModelException {
            ModelSource source = null;
            try {
                source = TestUtil.createModelSource(super.catalogFileObject, true);
            } catch (CatalogModelException ex) {
                assert false;
                ex.printStackTrace();
                return null;
            }
            return source;
        }
        
        
        public String getContent(){
            Document doc = (Document) getCatalogModel().getModelSource().getLookup().lookup(Document.class);;
            try {
                return doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}

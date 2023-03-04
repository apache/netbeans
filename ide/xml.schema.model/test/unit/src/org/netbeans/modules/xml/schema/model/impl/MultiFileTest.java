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
 * MultiFileTest.java
 * JUnit based test
 *
 * Created on December 8, 2005, 12:08 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import junit.framework.*;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Disabled as referenced files were partly not donated by oracle to apache
 */
//public class MultiFileTest extends TestCase {
//    
//    private static String TEST_XSD = "resources/OrgChart.xsd";
//    
//    public MultiFileTest(String testName) {
//        super(testName);
//    }
//    
//    protected void setUp() throws Exception {
//    }
//    
//    protected void tearDown() throws Exception {
//        TestCatalogModel.getDefault().clearDocumentPool();
//    }
//
//    public void testGetImportedModelSources() throws Exception {
//        if ( ! NamespaceLocation.ADDRESS.getResourceFile().exists() ) {
//            NamespaceLocation.ADDRESS.refreshResourceFile();
//        }
//        SchemaModel sm = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.ORGCHART);
//        // get imported model sources
//        SchemaImpl schema = (SchemaImpl)sm.getSchema();
//        Collection<Import> importedModelSources = new LinkedList<Import>(schema.getImports());
//	assertEquals("should be six imports", 6 ,importedModelSources.size());
//	Iterator<Import> itr = importedModelSources.iterator();
//	while(itr.hasNext()) {
//	    Import i = itr.next();
//	    try {
//		SchemaModel sm2 = i.resolveReferencedModel();
//	    } catch (CatalogModelException ex) {
//		itr.remove();
//	    } 
//	}
//        assertEquals("only two imports are reachable", 1,importedModelSources.size());
//        
//        ModelSource importedModelSource = importedModelSources.iterator().next().resolveReferencedModel().getModelSource();
//        assertEquals("address.xsd",((FileObject)importedModelSource.getLookup().lookup(FileObject.class)).getNameExt());
//        // get imported model
//        ModelSource testImportedModelSource = TestCatalogModel.getDefault().createTestModelSource((FileObject) importedModelSource.getLookup().lookup(FileObject.class), false);
//        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
//        assertNotNull(sm1);
//        assertEquals("http://www.altova.com/IPO",sm1.getSchema().getTargetNamespace());
//    }
//    
//    public void testGetIncludedModelSources() throws Exception {
//        // get the model for OrgChart.xsd
//        URL orgChartUrl = getClass().getResource("../resources/ipo.xsd");
//        File orgChartFile = new File(orgChartUrl.toURI());
//        FileObject orgChartFileObj = FileUtil.toFileObject(orgChartFile);
//        //ModelSource localTestModelSource = new TestModelSource(orgChartFileObj,false);
//        ModelSource testModelSource = TestCatalogModel.getDefault().createTestModelSource(orgChartFileObj, false);
//        SchemaModel sm = SchemaModelFactory.getDefault().getModel(testModelSource);
//        
//        //register address.xsd with relative location (this is to be done only once
//        URL addressUrl = getClass().getResource("../resources/address.xsd");
//        TestCatalogModel.getDefault().addURI(new URI("address.xsd"),addressUrl.toURI());
//        
//        // get included model sources
//        SchemaImpl schema = (SchemaImpl)sm.getSchema();
//        Collection<Include> includedModelSources = schema.getIncludes();
//        assertEquals(1,includedModelSources.size());
//        
//        ModelSource importedModelSource = includedModelSources.iterator().next().resolveReferencedModel().getModelSource();
//        assertEquals("address.xsd",((FileObject)importedModelSource.getLookup().lookup(FileObject.class)).getNameExt());
//        
//        // get included model
//        ModelSource testImportedModelSource = TestCatalogModel.getDefault().createTestModelSource((FileObject) importedModelSource.getLookup().lookup(FileObject.class), false);
//        SchemaModel sm1 = SchemaModelFactory.getDefault().getModel(testImportedModelSource);
//        assertNotNull(sm1);
//        assertEquals(schema.getTargetNamespace(),sm1.getSchema().getTargetNamespace());
//    }
//}

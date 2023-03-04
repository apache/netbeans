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
package org.netbeans.modules.xml.axi;

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.openide.filesystems.FileUtil;
        
/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractTestCase extends TestCase {

    //make it true if you want to see System.out.println messages.
    public static final boolean printUnitTestResults = false;    
    
    protected String schemaFileName;
    protected String globalElementName;    
    protected AXIModel axiModel;
    protected Element globalElement;
    protected URL referenceXML;
    protected boolean canCompareExpectedResultWithActual = true;
    
    
    /**
     * AbstractTestCase
     */
    public AbstractTestCase(String testName, 
            String schemaFileName, String globalElementName) {
        super(testName);
        this.schemaFileName = schemaFileName;
        this.globalElementName = globalElementName;
    }

    protected void setUp() throws Exception {
        loadModel(this.schemaFileName);
    }
	
    protected void loadModel(String schemaFileName) throws Exception {
        this.schemaFileName = schemaFileName;
        this.axiModel = getModel(schemaFileName);
        this.globalElement = findAXIGlobalElement(globalElementName);        
        String compareAgainst = schemaFileName.substring(0, schemaFileName.indexOf(".xsd")) + ".xml";
        referenceXML = AbstractTestCase.class.getResource(compareAgainst);
        if(referenceXML == null) {
            canCompareExpectedResultWithActual = false;
            return;
        }
    }

    protected AXIModel getModel(String schemaFileName) throws Exception {
        URL url = AbstractTestCase.class.getResource(schemaFileName);
        File file = new File(url.toURI());
        file = FileUtil.normalizeFile(file);
        return TestCatalogModel.getDefault().
                getAXIModel(FileUtil.toFileObject(file));                
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
            
    protected AXIModel getAXIModel() {
        return axiModel;
    }
    
    protected SchemaModel getSchemaModel() {
        return getAXIModel().getSchemaModel();
    }
    
    protected Element findAXIGlobalElement(String name) {
        if(name == null)
            return null;
        
        for(Element e : axiModel.getRoot().getElements()) {
            if(e.getName().equals(name)) {
                return e;
            }
        }
        
        return null;
    }
    
    protected ContentModel findContentModel(String name) {
        for(ContentModel cm : axiModel.getRoot().getContentModels()) {
            if(cm.getName().equals(name)) {
                return cm;
            }
        }
        
        return null;
    }
    
    protected void validateSchema(SchemaModel sm) {
        boolean status = 
			((AXIModelImpl)getAXIModel()).getState()==Model.State.VALID;//((AXIModelImpl)getAXIModel()).validate();
        assertTrue("Schema Validation failed", status);
    }
    
    public final void print(String message) {
        if(printUnitTestResults) {        
            System.out.println(message);
        }
    }
}

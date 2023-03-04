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
 * WSDLInlineSchemaValidatorTest.java
 * JUnit based test
 *
 * Created on January 29, 2007, 10:47 AM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.net.URL;
import java.util.Set;
import junit.framework.*;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.validation.Schema;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author radval
 */
public class WSDLInlineSchemaValidatorTest extends TestCase {

    public WSDLInlineSchemaValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getName method, of class org.netbeans.modules.xml.wsdl.validator.WSDLInlineSchemaValidator.
     */
    public void testGetName() {
        System.out.println("getName");
        
        WSDLInlineSchemaValidator instance = new WSDLInlineSchemaValidator();
        
        String expResult = "WSDLInlineSchemaValidator";
        String result = instance.getName();
        assertEquals(expResult, result);
        

    }

    /**
     * Test of validate method, of class org.netbeans.modules.xml.wsdl.validator.WSDLInlineSchemaValidator.
     */
    public void testValidate() throws Exception {
        System.out.println("validate");
        
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/visitor/resources/valid/AccountTransaction.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        Set<String> expectedErrors = new HashSet<String>();
        validate(uri, expectedErrors);
        
    }

    /**
     * Test of getSchema method, of class org.netbeans.modules.xml.wsdl.validator.WSDLInlineSchemaValidator.
     */
    public void testGetSchema() {
        System.out.println("getSchema");
        
        Model model = null;
        WSDLInlineSchemaValidator instance = new WSDLInlineSchemaValidator();
        
        Schema expResult = null;
        Schema result = instance.getSchema(model);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of resolveResource method, of class org.netbeans.modules.xml.wsdl.validator.WSDLInlineSchemaValidator.
     */
//    public void testResolveResource() {
//        System.out.println("resolveResource");
//        
//        String systemId = "";
//        Model currentModel = null;
//        WSDLInlineSchemaValidator instance = new WSDLInlineSchemaValidator();
//        
//        DocumentModel expResult = null;
//        DocumentModel result = instance.resolveResource(systemId, currentModel);
//        assertEquals(expResult, result);
//        
//        
//    }
    
    public void testSapInlineCrossReferenceValid() throws Exception {
         String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/Z_Flight.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 0);
    }
    
    
    public void testInlineCrossReferenceValid() throws Exception {
          String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/InlineSchemaCrossReference.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 0);
    }
    
      public void testInlineSchemaImportingAnotherSchemaValid() throws Exception {
          String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/Z_FlightWSD_EUC_SAP.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 0);
    }
      
            
      public void testInlineSchemaImportingAnotherSchemaUsingCatalogValid() throws Exception {
          //this test is to mimic wsdl importing xsd which imports another xsd from different projevt
           //so it uses catalog.xml at project level
           String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/InventoryService.wsdl";
          URL url = getClass().getResource(fileName);
          URI uri = url.toURI();
          
          validate(uri, 0);
      }       
    
    private void validate(URI uri, int expectedErrorCount)
        throws Exception {
        Validation v = new Validation();
        
        ModelSource ms = TestCatalogModel.getDefault().getModelSource(uri);
        MyModelSource source = new MyModelSource(ms.getLookup(), ms.isEditable(), uri);
        
        WSDLModel model = WSDLModelFactory.getDefault().getModel(source);
        
        WSDLSchemaValidator instance = new WSDLSchemaValidator();
        ValidationResult vr = instance.validate(model, v, Validation.ValidationType.COMPLETE);
        assertNotNull(vr.getValidationResult());
        
        ValidationHelper.dumpErrors(vr);
        assertTrue("expect error " + expectedErrorCount,  vr.getValidationResult().size() == expectedErrorCount);
     }
     
    
    private ValidationResult validate(URI relativePath) throws Exception {
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(relativePath);
        Validation validation = new Validation();
        ValidationType validationType = Validation.ValidationType.COMPLETE;
        WSDLInlineSchemaValidator instance = new WSDLInlineSchemaValidator();
        
        ValidationResult result = 
            instance.validate(model, validation, validationType);
        return result;
    }
    
    private void validate(URI relativePath, Set<String> expectedErrors)
        throws Exception {
        System.out.println(relativePath);
        ValidationResult result = validate(relativePath);
        Iterator<ResultItem> it = result.getValidationResult().iterator();
        ValidationHelper.dumpExpecedErrors(expectedErrors);
        while (it.hasNext()) {
            ResultItem item = it.next();
//            System.out.println("    " + item.getDescription());
            assertTrue("Actual Error "+ item.getDescription() + "in " +relativePath, ValidationHelper.containsExpectedError(expectedErrors, item.getDescription()));
        }
        if (result.getValidationResult().size() == 0 && expectedErrors.size() > 0) {
            fail("Expected at least " + expectedErrors.size() + " error(s).  Got 0 errors instead");
        }
    }
}

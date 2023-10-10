/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * WSDLSchemaValidatorTest.java
 * JUnit based test
 *
 * Created on January 29, 2007, 10:47 AM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.net.URI;
import java.net.URL;
import junit.framework.*;
import javax.xml.validation.Schema;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author radval
 */
public class WSDLSchemaValidatorTest extends TestCase {

    public WSDLSchemaValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getName method, of class org.netbeans.modules.xml.wsdl.validator.WSDLSchemaValidator.
     */
    public void testGetName() {
        System.out.println("getName");
        
        WSDLSchemaValidator instance = new WSDLSchemaValidator();
        
        String expResult = "WSDLSchemaValidator";
        String result = instance.getName();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getSchema method, of class org.netbeans.modules.xml.wsdl.validator.WSDLSchemaValidator.
     */
    public void testGetSchema() throws Exception {
        System.out.println("getSchema");
        
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/visitor/resources/valid/AccountTransaction.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(uri);
        
        WSDLSchemaValidator instance = new WSDLSchemaValidator();
        
        Schema expResult = null;
        Schema result = instance.getSchema(model);
        assertNotNull(result);
        
    }
    
    
    public void testValidateImportMultiLocation() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importMultiLocation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
    }
    
    public void testValidateImportMultiNamespace() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importMultiNamespace_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
    }
    
    public void testValidateImportNoLocation() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importNoLocation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
    }
    
    
    public void testValidateImportNoNamespace() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importNoNamespace_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 2);
    }
    
    public void testValidateMessageMultiDocumentation() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messageMultiDocumentation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
    }
    
    public void testValidateMessageMultiName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messageMultiName_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
    }
    
    public void testValidateMessageNoName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messageNoName_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 2);
    }
   
     public void testValidateMessageNonUniqueName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messageNonUniqueName_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        validate(uri, 1);
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
        
        // some missing element errors appear twice on newer JDKs
        int uniqueErrors = (int) vr.getValidationResult().stream()
                .map(Validator.ResultItem::getDescription)
                .distinct().count();

        assertTrue("expect error " + expectedErrorCount,  uniqueErrors == expectedErrorCount);
     }
     
     
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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

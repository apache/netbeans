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
        assertTrue("expect error " + expectedErrorCount,  vr.getValidationResult().size() == expectedErrorCount);
     }
     
     
    
}

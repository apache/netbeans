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
 * WSDLSemanticValidatorTest.java
 * JUnit based test
 *
 * Created on January 29, 2007, 10:47 AM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.visitor.WSDLSemanticsVisitor;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author radval
 */
public class WSDLSemanticValidatorTest extends TestCase {

    private static final ResourceBundle mMessages =
        ResourceBundle.getBundle(WSDLSemanticsVisitor.class.getPackage().getName()+".Bundle");

    public WSDLSemanticValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {}

    protected void tearDown() throws Exception {}

    public void testGetName() {
        System.out.println("getName");
        
        WSDLSemanticValidator instance = new WSDLSemanticValidator();
        
        String expResult = "WSDLSemanticValidator";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    public void testValidate() throws Exception {
        System.out.println("validate");
        
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/visitor/resources/valid/AsynchronousSample.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        Set<String> expectedErrors = new HashSet<String>();
        validate(uri, expectedErrors);
    }
    
    public void testValidateDefinitionShouldHaveTargetNamespaceWarning() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/definitionsTests/definitionsNoTargetN_invalid.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateDefinitionMissingTargetNamespaceWarning() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/definitionsTests/definitionsNoTargetN_valid_warning.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_ERROR_WSDL_DEFINITIONS_NO_TARGETNAMESPACE")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateImportBogusLocation() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importBogusLocation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_IMPORTED_DOCUMENT")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateImportNoLocation() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importNoLocation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_IMPORTED_DOCUMENT")));
        validate(uri, expectedErrors);
    }
    
    
    public void testValidateImportNoNamespace() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importNoNamespace_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_IMPORTED_DOCUMENT")));
        validate(uri, expectedErrors);
    }
    
    public void testValidatePortTypeOperationDuplicateInputName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/ptMultiOpsDupInput_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE")));
        validate(uri, expectedErrors);
        
                
    }
    
    public void testValidatePortTypeOperationInputInvalidMessage() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/inputBogusMsg_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT")));
        validate(uri, expectedErrors);
        
                
    }
    
    public void testValidatePortTypeOperationDuplicateOutputName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/ptMultiOpsDupOutput_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE")));
        validate(uri, expectedErrors);
        
                
    }
    
    public void testValidatePortTypeOperationOutputInvalidMessage() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/outputBogusMsg_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT")));
        validate(uri, expectedErrors);
        
                
    }
    
    public void testValidateRequestResponseOperationDuplicateFaultName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/ptMultiOpsDupFaultSameOps_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_DUPLICATE_OPRATION_FAULT_NAME")));
        validate(uri, expectedErrors);
    }
     
    public void testValidateRequestResponseOperationFaultInvalidMessage() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/faultBogusMsg_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateSolicitResponseOperationDuplicateFaultName() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/solrep/multiOpsDupFaultSameOps_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_DUPLICATE_OPRATION_FAULT_NAME")));
        validate(uri, expectedErrors);
    }
     
    public void testValidateSolicitResponseOperationFaultInvalidMessage() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/solrep/faultBogusMsg_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateOnewayOperationFaultShouldNotBeDefined() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/OneWay/faultDefined_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_FAULT_NOT_ALLOWED_IN_OPERATION")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateNotificationOperationFaultShouldNotBeDefined() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/notify/faultDefined_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_FAULT_NOT_ALLOWED_IN_OPERATION")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateMessageNoPartWarning() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messageNoParts.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_WARNING_WSDL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED")));
        validate(uri, expectedErrors);
    }
    
    public void testValidatePartNoElementOrTypeAttribute() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messagePartNoTypeOrElement_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART")));
        validate(uri, expectedErrors);
    }
    
    public void testValidatePartInvalidElementAttribute() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messagePartBadElement_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID")));
        validate(uri, expectedErrors);
    }
    
    public void testValidatePartInvalidTypeAttribute() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messagePartBadType_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID")));
        validate(uri, expectedErrors);
    }
    
    public void testValidatePartBothTypeAndAttributeError() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/messageTests/messagePartElementAndType_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART")));
        validate(uri, expectedErrors);
    }
        
    public void testValidateImport() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/importWSDLTests/importBogusLocation_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_IMPORTED_DOCUMENT")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingInvalidType() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/bindingRRFBogusType_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_PORTTYPE_IN_BINDING")));
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingOperationOverloadedValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/operations/bindingRRFMultiOpsNonUniqueOpName_valid.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingOperationOverloadedNoMatchingOperationError() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/operations/bindingRRFMultiOpsNonUniqueOpNameNoMatch_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE")));
        
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingOperationOverloadedNoMatchingOperationInputOutputFaultError1() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/operations/bindingRRFMultiOpsNonUniqueOpNameNoMatch_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE")));
        
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingOperationOverloadedNoMatchingOperationInputOutputFaultError2() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/operations/bindingRRFMultiOpsNonUniqueOpNameNoMatch2_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE")));
        expectedErrors.add(format(mMessages.getString("VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE")));
       
        validate(uri, expectedErrors);
    }
    
    public void testValidateBindingOperationSignatureSameAsPortTypeOperationValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/bindingTests/operations/bindingOperationSignatureSameAsPortTypeOperation_valid.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        validate(uri, expectedErrors);
    }
    
    public void testServicePortInvalidBinding() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/servicePortTests/servicePortBogusBinding_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(format(mMessages.getString("VAL_MISSING_BINDING_IN_SERVICE_PORT")));
        validate(uri, expectedErrors);
    }
    
    public void testSapInlineCrossReferenceValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/Z_Flight.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       
        validate(uri, expectedErrors);
    }
     
    public void testEmptyInlineSchemaValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/emptyInlineSchema.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       
        validate(uri, expectedErrors);
    }
    
     public void testInlineCrossReferenceAttributeValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/inlineSchemaTests/MultipleInlineSchemaReferingAttributes.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       
        validate(uri, expectedErrors);
     }
    
     public void testMultipleTypesInValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/typesTests/typesMultiTypes_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       expectedErrors.add(format(mMessages.getString("VAL_MULTIPLE_TYPES_IN_DEFINITION")));
        
        validate(uri, expectedErrors);
     }
      
     public void testRequestReplyOperationParameterOrderValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/prmOrd_valid.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
        
        validate(uri, expectedErrors);
     }
     
     public void testRequestReplyOperationParameterOrderBogusPartsInvalid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/prmOrdBogusPartNames_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       expectedErrors.add(format(mMessages.getString("VAL_PARMETER_ORDER_CHECK_PART_EXISTENCE")));
        
        validate(uri, expectedErrors);
     }
      
     public void testRequestReplyOperationParameterOrderMoreThanOnePartFromOutputMessageMissingInvalid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/reqrep/prmOrdOpPartMissing_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       expectedErrors.add(format(mMessages.getString("VAL_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING")));
        
        validate(uri, expectedErrors);
     }
    
     public void testSolicitResponseOperationParameterOrderValid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/solrep/prmOrd_valid.wsdl";
                          
        URL url = getClass().getResource(fileName);                                                                        
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
        
        validate(uri, expectedErrors);
     }
     
     public void testSolicitResponseOperationParameterOrderBogusPartsInvalid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/solrep/prmOrdBogusPartNames_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       expectedErrors.add(format(mMessages.getString("VAL_PARMETER_ORDER_CHECK_PART_EXISTENCE")));
        
        validate(uri, expectedErrors);
     }
      
     public void testSolicitResponseOperationParameterOrderMoreThanOnePartFromOutputMessageMissingInvalid() throws Exception {
        String fileName = "/org/netbeans/modules/xml/wsdl/validator/resources/ptTests/opTests/solrep/prmOrdOutputPartMissing_error.wsdl";
        URL url = getClass().getResource(fileName);
        URI uri = url.toURI();
        HashSet<String> expectedErrors = new HashSet<String>();
       expectedErrors.add(format(mMessages.getString("VAL_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING")));
        
        validate(uri, expectedErrors);
     }
     
    private ValidationResult validate(URI relativePath) throws Exception {
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(relativePath);
        Validation validation = new Validation();
        ValidationType validationType = Validation.ValidationType.COMPLETE;
        WSDLSemanticValidator instance = new WSDLSemanticValidator();
        
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
            assertTrue("Actual Error "+ item.getDescription() + "in " +relativePath, ValidationHelper.containsExpectedError(expectedErrors, item.getDescription()));
        }
        if (result.getValidationResult().size() == 0 && expectedErrors.size() > 0) {
            fail("Expected at least " + expectedErrors.size() + " error(s).  Got 0 errors instead");
        }
    }

    private String format(String value) {
        return MessageFormat.format(value, new Object[]{});
    }
}

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
package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import java.io.File;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class SecurityPolicyTest extends TestCase {
    
    public SecurityPolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception { }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testSecurityPolicy() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");

        File f = new File("SecurityPolicyTest.wsdl");
        if (f.exists()) {
            f.delete();
        }
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];

        for (ConfigVersion cfgV : ConfigVersion.values()) {

            assertFalse("WSS10 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isWss10(b));
            assertFalse("WSS11 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isWss11(b));
            assertFalse("Trust10 enabled indicated on empty WSDL", SecurityPolicyModelHelper.isTrust(b, cfgV));

            assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isEncryptSignature(b));
            assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isRequireSignatureConfirmation(b));
            assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));

            assertFalse("Enabled indicated on empty WSDL", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));

            SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(cfgV);

            //WSS10
            WssElement wss = spmh.enableWss(b, false);
            assertTrue("WSS10 Not enabled correctly", SecurityPolicyModelHelper.isWss10(b));

            spmh.disableWss(b);
            assertFalse("WSS10 enabled indicated", SecurityPolicyModelHelper.isWss10(b));

            //WSS11
            wss = spmh.enableWss(b, true);
            assertTrue("WSS11 Not enabled correctly", SecurityPolicyModelHelper.isWss11(b));

            spmh.disableWss(b);
            assertFalse("WSS11 enabled indicated", SecurityPolicyModelHelper.isWss11(b));

            //TRUST10
            TrustElement trust = spmh.enableTrust(b, cfgV);
            assertTrue("Trust10 Not enabled correctly", SecurityPolicyModelHelper.isTrust(b, cfgV));

            spmh.disableTrust(b);
            assertFalse("Trust10 enabled indicated", SecurityPolicyModelHelper.isTrust(b, cfgV));

            spmh.setSecurityBindingType(b, ComboConstants.SYMMETRIC);

                WSDLComponent bindingType = SecurityPolicyModelHelper.getSecurityBindingTypeElement(b);

                // Encrypt Signature
                spmh.enableEncryptSignature(bindingType, true);
                assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isEncryptSignature(b));
                spmh.enableEncryptSignature(bindingType, false);
                assertFalse("enabled indicated", SecurityPolicyModelHelper.isEncryptSignature(b));

                // Sign Entire Headers And Body
                spmh.enableSignEntireHeadersAndBody(bindingType, true);
                assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));
                spmh.enableSignEntireHeadersAndBody(bindingType, false);
                assertFalse("enabled indicated", SecurityPolicyModelHelper.isSignEntireHeadersAndBody(b));

                // Encrypt Before Signing
                spmh.enableEncryptBeforeSigning(bindingType, true);
                assertTrue("Not enabled correctly", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));
                spmh.enableEncryptBeforeSigning(bindingType, false);
                assertFalse("enabled indicated", SecurityPolicyModelHelper.isEncryptBeforeSigning(b));

                // Message Layout
                spmh.setLayout(bindingType, ComboConstants.STRICT);
                assertEquals("Message Layout", ComboConstants.STRICT, SecurityPolicyModelHelper.getMessageLayout(b));
                spmh.setLayout(bindingType, ComboConstants.LAX);
                assertEquals("Message Layout", ComboConstants.LAX, SecurityPolicyModelHelper.getMessageLayout(b));
                spmh.setLayout(bindingType, ComboConstants.LAXTSFIRST);
                assertEquals("Message Layout", ComboConstants.LAXTSFIRST, SecurityPolicyModelHelper.getMessageLayout(b));
                spmh.setLayout(bindingType, ComboConstants.LAXTSLAST);
                assertEquals("Message Layout", ComboConstants.LAXTSLAST, SecurityPolicyModelHelper.getMessageLayout(b));

                AlgoSuiteModelHelper asmh = AlgoSuiteModelHelper.getInstance(cfgV);
                // Algorithm Suite
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC128);
                assertEquals("Algorithm Suite", ComboConstants.BASIC128, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC192);
                assertEquals("Algorithm Suite", ComboConstants.BASIC192, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC256);
                assertEquals("Algorithm Suite", ComboConstants.BASIC256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDES);
                assertEquals("Algorithm Suite", ComboConstants.TRIPLEDES, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC256RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC192RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC192RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC128RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC128RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESRSA15);
                assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESRSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC256SHA256);
                assertEquals("Algorithm Suite", ComboConstants.BASIC256SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));

                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC192SHA256);
                assertEquals("Algorithm Suite", ComboConstants.BASIC192SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC128SHA256);
                assertEquals("Algorithm Suite", ComboConstants.BASIC128SHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESSHA256);
                assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESSHA256, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC256SHA256RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC256SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC192SHA256RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC192SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.BASIC128SHA256RSA15);
                assertEquals("Algorithm Suite", ComboConstants.BASIC128SHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));
                asmh.setAlgorithmSuite(bindingType, ComboConstants.TRIPLEDESSHA256RSA15);
                assertEquals("Algorithm Suite", ComboConstants.TRIPLEDESSHA256RSA15, AlgoSuiteModelHelper.getAlgorithmSuite(b));

            spmh.setSecurityBindingType(b, ComboConstants.ASYMMETRIC);

            spmh.setSecurityBindingType(b, ComboConstants.NOSECURITY);

                // FIRST CHECK DEFAULTS - those should be set when binding is switched to this value
                assertNull("Default Algorithm Suite", AlgoSuiteModelHelper.getAlgorithmSuite(b));
                assertNull("Default Message Layout", SecurityPolicyModelHelper.getMessageLayout(b));
                assertFalse("Default WSS", SecurityPolicyModelHelper.isWss10(b));
                assertFalse("Default WSS", SecurityPolicyModelHelper.isWss11(b));
                assertFalse("Default Trust", SecurityPolicyModelHelper.isTrust(b, cfgV));
        }

        TestUtil.dumpToFile(model.getBaseDocument(), f);
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}

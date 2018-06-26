/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

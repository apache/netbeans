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

package org.netbeans.modules.websvc.wsitmodelext;

import java.io.File;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitmodelext.mtom.MtomQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunClientQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySCClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryTrustClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySCServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySecurityPolicyServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietaryTrustServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.FIQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.TCPQName;
import org.netbeans.modules.websvc.wsitmodelext.trust.TrustQName;
import org.netbeans.modules.websvc.wsitmodelext.tx.TxQName;
import org.netbeans.modules.websvc.wsitmodelext.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitmodelext.util.Util;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class PolicyTest extends NbTestCase {

    public PolicyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testWrite() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = Util.loadWSDLModel("../resources/policy.xml");
        WSDLComponentFactory fact = model.getFactory();

        model.startTransaction();

        Definitions d = model.getDefinitions();
        ExtensibilityElement e = null;
        ArrayList<QName> qnames = new ArrayList<QName>();

        qnames.addAll(PolicyQName.getQNames(ConfigVersion.CONFIG_1_0));
        qnames.addAll(PolicyQName.getQNames(ConfigVersion.CONFIG_1_3));

        qnames.addAll(MtomQName.getQNames());
        qnames.addAll(FIQName.getQNames());
        qnames.addAll(TCPQName.getQNames());
        qnames.addAll(AddressingQName.getQNames());
        qnames.addAll(Addressing10QName.getQNames());
        qnames.addAll(Addressing10WsdlQName.getQNames());

        qnames.addAll(RMQName.getQNames(ConfigVersion.CONFIG_1_0));
        qnames.addAll(RMQName.getQNames(ConfigVersion.CONFIG_1_3));

        qnames.addAll(RMSunQName.getQNames());
        qnames.addAll(RMSunClientQName.getQNames());
        qnames.addAll(RMMSQName.getQNames());
        qnames.addAll(SecurityQName.getQNames());

        qnames.addAll(SecurityPolicyQName.getQNames(ConfigVersion.CONFIG_1_0));
        qnames.addAll(SecurityPolicyQName.getQNames(ConfigVersion.CONFIG_1_3));

        qnames.addAll(ProprietaryPolicyQName.getQNames());
        qnames.addAll(ProprietarySCClientQName.getQNames());
        qnames.addAll(ProprietarySecurityPolicyQName.getQNames());
        qnames.addAll(ProprietarySecurityPolicyServiceQName.getQNames());
        qnames.addAll(ProprietaryTrustClientQName.getQNames());
        qnames.addAll(ProprietaryTrustServiceQName.getQNames());
        qnames.addAll(ProprietarySCServiceQName.getQNames());

        qnames.addAll(TrustQName.getQNames(ConfigVersion.CONFIG_1_0));
        qnames.addAll(TrustQName.getQNames(ConfigVersion.CONFIG_1_3));

        qnames.addAll(TxQName.getQNames());

        for (QName qname : qnames) {
            e = (ExtensibilityElement)fact.create(d, qname);
            d.addExtensibilityElement(e);
        }

        model.endTransaction();

        File output = new File("WSITModelTestOutput.wsdl");

        System.out.println("Generating file: " + output.getAbsolutePath());

        File golden = new File(getDataDir().getAbsolutePath() + "/goldenfiles/" + "WSITModelTestOutput.wsdl");

        Util.dumpToFile(model.getBaseDocument(), output);

        System.out.println(getWorkDir().getAbsolutePath());
        assertFile(output, golden, getWorkDir());
    }

    public String getTestResourcePath() {
        return "../resources/policy.xml";
    }

}

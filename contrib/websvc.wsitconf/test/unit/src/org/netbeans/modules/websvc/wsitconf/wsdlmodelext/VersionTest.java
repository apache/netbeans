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

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class VersionTest extends TestCase {
    
    public static boolean justGenerate = true;

    public VersionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testVersionChange() throws Exception {
        File f = new File("VersionTest-VersionChange.wsdl");
        if (f.exists()) {
            f.delete();
        }

        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];

        // set CFG 10, set features, then change to 13 and back
        ConfigVersion cfgV = ConfigVersion.CONFIG_1_0;

        PolicyModelHelper pmh = PolicyModelHelper.getInstance(cfgV);
        pmh.createPolicy(b, true);

        AddressingModelHelper.getInstance(cfgV).enableAddressing(b, false);

        RMModelHelper rmh10 = RMModelHelper.getInstance(cfgV);
        RMModelHelper.getInstance(cfgV).enableRM(b, true);
        RMModelHelper.getInstance(cfgV).enableOrdered(b, true);

        TransportModelHelper.enableMtom(b, true);
        TransportModelHelper.enableFI(b, true);
        TransportModelHelper.enableTCP(b, true);

        ProfilesModelHelper.getInstance(cfgV).setSecurityProfile(b, ComboConstants.PROF_USERNAME, false);

        WSITModelSupport.moveCurrentConfig(b, ConfigVersion.CONFIG_1_0, ConfigVersion.CONFIG_1_3, null);
        assertTrue(PolicyModelHelper.getConfigVersion(b).equals(ConfigVersion.CONFIG_1_3));
        assertTrue(AddressingModelHelper.isAddressingEnabled(b));
        RMModelHelper rmh13 = RMModelHelper.getInstance(ConfigVersion.CONFIG_1_3);
        assertTrue(rmh13.isRMEnabled(b));
        assertTrue(rmh13.isOrderedEnabled(b));
        assertTrue(TransportModelHelper.isFIEnabled(b));
        assertTrue(TransportModelHelper.isTCPEnabled(b));
        assertTrue(TransportModelHelper.isMtomEnabled(b));
        assertTrue(ProfilesModelHelper.getSecurityProfile(b).equals(ComboConstants.PROF_USERNAME));

        WSITModelSupport.moveCurrentConfig(b, ConfigVersion.CONFIG_1_3, ConfigVersion.CONFIG_1_0, null);
        assertTrue(PolicyModelHelper.getConfigVersion(b).equals(ConfigVersion.CONFIG_1_0));
        assertTrue(AddressingModelHelper.isAddressingEnabled(b));
        assertTrue(rmh10.isRMEnabled(b));
        assertTrue(rmh10.isOrderedEnabled(b));
        assertTrue(TransportModelHelper.isFIEnabled(b));
        assertTrue(TransportModelHelper.isTCPEnabled(b));
        assertTrue(TransportModelHelper.isMtomEnabled(b));
        assertTrue(ProfilesModelHelper.getSecurityProfile(b).equals(ComboConstants.PROF_USERNAME));
        
        TestUtil.dumpToFile(model.getBaseDocument(), f);
        PolicyModelHelper.cleanPolicies(b);
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}

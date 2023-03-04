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
import java.util.ArrayList;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.util.TestCatalogModel;
import org.netbeans.modules.websvc.wsitconf.util.TestUtil;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Grebac
 */
public class ProfileTest extends NbTestCase {

    public static boolean justGenerate = false;
    
    public ProfileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(false);
    }

    public void testSecurityProfilesLayout() throws Exception {
        TestCatalogModel.getDefault().setDocumentPooling(true);
        WSDLModel model = TestUtil.loadWSDLModel("../wsdlmodelext/resources/policy.xml");
        
        Definitions d = model.getDefinitions();
        Binding b = (Binding) d.getBindings().toArray()[0];

        Set<SecurityProfile> secProfiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();

        for (ConfigVersion cfgV : ConfigVersion.values()) {
            String cfgStr = ConfigVersion.CONFIG_1_0.equals(cfgV) ? "10-" : "13-";
            ArrayList<String> profiles = new ArrayList<>();
            PolicyModelHelper.setConfigVersion(b, cfgV, null);
            for (SecurityProfile sP : secProfiles) {
                if (sP.isProfileSupported(null, b, false)) {
                    profiles.add(sP.getDisplayName());
                }
            }
            for (int i=1; i<profiles.size(); i++) {
                String profile = profiles.get(i);
                String profFileName = profile.replace(" ", "");

                //default profile set
                ProfilesModelHelper.getInstance(cfgV).setSecurityProfile(b, profile, false);
                SecurityProfile readSP = SecurityProfileRegistry.getDefault().getProfile(profile);
                assertEquals("Security profile set and read don't match", profile, readSP.getDisplayName());

                String extension = justGenerate ? ".pass" : "";
                
                File profWorkDir = new File(getWorkDirPath() + File.separator + profFileName);
                FileUtil.createFolder(profWorkDir);
                String fStr = cfgStr + "test";
                File dumpFile = new File(profWorkDir.getAbsolutePath() + File.separator + fStr + extension);
                FileUtil.createData(dumpFile);
                
                TestUtil.dumpToFile(model.getBaseDocument(), dumpFile);
                if (!justGenerate) {
                    assertFile(dumpFile, TestUtil.getGoldenFile(getDataDir(), profFileName, fStr));
                }

                if (readSP instanceof SecureConversationFeature) {
                    ProfilesModelHelper.getInstance(cfgV).setSecureConversation(b, true);     // enable SC

                    dumpFile = new File(profWorkDir.getAbsolutePath() + File.separator + fStr + "SC" + extension);
                    TestUtil.dumpToFile(model.getBaseDocument(), dumpFile);
                    if (!justGenerate) {
                        assertFile(dumpFile, TestUtil.getGoldenFile(getDataDir(), profFileName, fStr + "SC"));
                    }

                    readSP = SecurityProfileRegistry.getDefault().getProfile(profile);
                    assertEquals("Security profile with SC set and read don't match", profile, readSP.getDisplayName());
                    
                    ProfilesModelHelper.getInstance(cfgV).setSecureConversation(b, false);     // disable SC

                    dumpFile = new File(profWorkDir.getAbsolutePath() + File.separator + fStr + "AfterSC" + extension);
                    TestUtil.dumpToFile(model.getBaseDocument(), dumpFile);
                    if (!justGenerate) {
                        assertFile(dumpFile, TestUtil.getGoldenFile(getDataDir(), profFileName, fStr + "AfterSC"));
                    }
                }

                readAndCheck(model, profile);
            }
        }

    }

    private void readAndCheck(WSDLModel model, String profile) {
        
        // the model operation is not enclosed in transaction inorder to catch 
        // whether the operations do not try to create non-existing elements        
        
    }

    public String getTestResourcePath() {
        return "../wsdlmodelext/resources/policy.xml";
    }
    
}

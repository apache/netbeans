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
            ArrayList<String> profiles = new ArrayList();
            PolicyModelHelper.setConfigVersion(b, cfgV, null);
            for (SecurityProfile sP : secProfiles) {
                if (sP.isProfileSupported(null, b, false)) {
                    profiles.add(sP.getDisplayName());
                }
            }
            for (int i=1; i<profiles.size(); i++) {
                String profile = profiles.get(i);
                String profFileName = profile.replaceAll(" ", "");

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

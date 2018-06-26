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

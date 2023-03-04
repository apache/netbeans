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

package org.netbeans.modules.ide.ergonomics.fod;

import java.util.List;
import junit.framework.Test;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;



/**
 *
 * @author Jiri Rechtacek <jrechtacek@netbeans.org>
 */
public class DisableJ2EEKeepsMobilityEnabledTest extends NbTestCase {

    private FeatureInfo j2ee = null;
    private FeatureInfo mobility = null;
    private UpdateElement j2eeUE = null;
    private UpdateElement mobilityUE = null;
    
    public DisableJ2EEKeepsMobilityEnabledTest(String name) {
        super(name);
    }

    public static Test suite() {
        Test test = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(DisableJ2EEKeepsMobilityEnabledTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
        return test;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.j2ee.kit")) {
                j2ee = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.j2me.kit")) {
                mobility = f;
            }
        }
        if (j2ee == null || mobility == null) {
            return;
        }

        assertFalse("j2ee disabled", j2ee.isEnabled());
        assertFalse("mobility disabled", mobility.isEnabled());

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        
        for (UpdateUnit uu : units) {
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2ee.kit")) {
                j2eeUE = uu.getInstalled();
            }
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2me.kit")) {
                mobilityUE = uu.getInstalled();
            }
        }
        assertNotNull("J2EE found", j2eeUE);
        assertNotNull("Mobility found", mobilityUE);
        
        OperationContainer<OperationSupport> cc = OperationContainer.createForEnable();
        OperationInfo<OperationSupport> info;
        info = cc.add(j2eeUE);
        cc.add(info.getRequiredElements());
        info = cc.add(mobilityUE);
        cc.add(info.getRequiredElements());
        cc.getSupport().doOperation(null);

        assertTrue("j2ee enabled", j2ee.isEnabled());
        assertTrue("mobility enabled", mobility.isEnabled());
    }

    public void testEnablingJ2EEEnablesJavaViaAutoUpdateManager() throws Exception {
        if (j2ee == null || mobility == null) {
            return;
        }
        OperationContainer<OperationSupport> cc = OperationContainer.createForDisable();
        OperationInfo<OperationSupport> info;
        info = cc.add(j2eeUE);
        assertFalse("Mobility remains enabled so far", cc.listAll().toString().contains("org.netbeans.modules.mobility.kit"));
        cc.add(info.getRequiredElements());
        assertFalse("Mobility remains enabled finally", cc.listAll().toString().contains("org.netbeans.modules.mobility.kit"));
    }

}

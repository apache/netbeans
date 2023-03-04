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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.TestUtils.CustomItemsProvider;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;

/**
 *
 * @author Radek Matous
 */
public class UpdateFromNbmTest extends OperationsTestImpl {

    public UpdateFromNbmTest(String testName) {
        super(testName);
    }

    protected String moduleCodeNameBaseForTest() {
        return "org.yourorghere.engine";
    }

    public void testSelf() throws Exception {
        UpdateUnit toUpdate = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUpdate);
        assertEquals(2, toUpdate.getAvailableUpdates ().size());
        UpdateElement engine1_0 = toUpdate.getAvailableUpdates ().get (1);
        assertNotNull(engine1_0);
        assertEquals("1.0",engine1_0.getSpecificationVersion().toString());
        installModule(toUpdate, engine1_0);
        toUpdate = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUpdate.getInstalled());

        MockServices.setServices(MyProvider.class, CustomItemsProvider.class);
        URL engineURL = TestUtils.class.getResource("data/org-yourorghere-engine-1-2.nbm");
        assertNotNull(engineURL);
        File engineFile = TestUtils.getFile(this, engineURL);
        assertTrue(engineFile.exists());

        URL independentURL = TestUtils.class.getResource("data/org-yourorghere-independent-1-1.nbm");
        assertNotNull(independentURL);
        File independentFile = TestUtils.getFile(this, independentURL);
        assertTrue(independentFile.exists());

        String source = "local-downloaded";
        List<UpdateUnit> units =  UpdateUnitProviderFactory.getDefault ().create (source, new File[] {engineFile, independentFile}).
                getUpdateUnits (UpdateManager.TYPE.MODULE);
        assertEquals(2, units.size());
        UpdateUnit nbmsEngine =  null;
        if (units.get(0).getCodeName().indexOf("engine") != -1) {
            nbmsEngine = units.get (0);
        } else if (units.get(1).getCodeName().indexOf("engine") != -1) {
            nbmsEngine = units.get (1);
        }
        assertNotNull (nbmsEngine);
        assertNotNull(nbmsEngine.getInstalled());
        assertEquals(1, nbmsEngine.getAvailableUpdates().size());
        UpdateElement engine1_2 = nbmsEngine.getAvailableUpdates().get(0);
        assertEquals(source,engine1_2.getSource());
        assertEquals("1.2",engine1_2.getSpecificationVersion().toString());
        OperationContainer<InstallSupport> oc =  OperationContainer.createForUpdate();
        OperationContainer.OperationInfo info = oc.add(nbmsEngine, engine1_2);
        assertNotNull(info);
        final Set brokeDeps = info.getBrokenDependencies();
        assertEquals("One broken dep: " + brokeDeps, 1, brokeDeps.size());
        String brokenDep = (String)brokeDeps.toArray()[0];
        assertEquals("module org.yourorghere.independent > 1.1",brokenDep);
        assertEquals(0, info.getRequiredElements().size());
        UpdateUnit independentEngine =  null;
        if (units.get(0).getCodeName().indexOf("independent") != -1) {
            independentEngine = units.get (0);
        } else if (units.get(1).getCodeName().indexOf("independent") != -1) {
            independentEngine = units.get (1);
        }
        assertNotNull (independentEngine);
        assertNotNull(independentEngine.getInstalled());

        UpdateElement independent1_1 = independentEngine.getAvailableUpdates().get(0);
        assertEquals(source,independent1_1.getSource());
        assertEquals("1.1",independent1_1.getSpecificationVersion().toString());

        OperationContainer.OperationInfo info2 = oc.add(independentEngine, independent1_1);
        assertEquals(0, info.getBrokenDependencies().size());
        assertEquals(0, info.getRequiredElements().size());
        assertEquals(0, info2.getBrokenDependencies().size());
        assertEquals(0, info2.getRequiredElements().size());

        InstallSupport support = oc.getSupport();
        assertNotNull(support);

        InstallSupport.Validator v = support.doDownload(null, false);
        assertNotNull(v);
        InstallSupport.Installer i = support.doValidate(v, null);
        assertNotNull(i);
        //assertNotNull(support.getCertificate(i, upEl));
        Restarter r = null;
        try {
            r = support.doInstall(i, null);
        } catch (OperationException ex) {
            if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                // can ingore
                // module system cannot load the module either
            } else {
                fail (ex.toString ());
            }
        }
        assertNotNull ("Install update " + engine1_2 + " needs restart.", r);
        support.doRestartLater (r);

        MockServices.setServices(MyProvider.class, CustomItemsProvider.class);
        assertTrue (nbmsEngine + " is waiting for Restart IDE.", nbmsEngine.isPending ());
    }

}

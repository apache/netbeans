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
package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.autoupdate.services.OperationsTestImpl;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/**
 *
 * @author Radek Matous
 */
public class RequiredElementsForUninstallTest extends OperationsTestImpl {

    public RequiredElementsForUninstallTest(String testName) {
        super(testName);
    }
    public UpdateUnit getUpdateUnit(String codeNameBase) {
        UpdateUnit uu = UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
        assertNotNull(uu);
        return uu;
    }
    public UpdateElement getAvailableUpdate(UpdateUnit updateUnit, int idx) {
        List<UpdateElement> available = updateUnit.getAvailableUpdates();
        assertTrue(available.size() > idx);
        return available.get(idx);

    }

    @RandomlyFails
    public void testSelf() throws Exception {
        OperationContainer<OperationSupport> installContainer = OperationContainer.createForDirectInstall();
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);
        OperationInfo engineInfo = installContainer.add(engineElement);
        assertNotNull(engineInfo);

        UpdateUnit independentUnit = getUpdateUnit("org.yourorghere.independent");
        assertNull("cannot be installed",independentUnit.getInstalled());
        UpdateElement independentElement = getAvailableUpdate(independentUnit,0);
        OperationInfo independentInfo = installContainer.add(independentElement);
        assertNotNull(independentInfo);

        UpdateUnit dependingUnit = getUpdateUnit("org.yourorghere.depending");
        assertNull("cannot be installed",dependingUnit.getInstalled());
        UpdateElement dependingElement = getAvailableUpdate(dependingUnit,0);
        OperationInfo dependingInfo = installContainer.add(dependingElement);
        assertNotNull(dependingInfo);

        assertEquals(0, installContainer.listInvalid().size());
        assertEquals(3, installContainer.listAll().size());
        installModule(independentUnit, null);
        installModule(engineUnit, null);
        installModule(dependingUnit, null);

        Module independentModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(independentUnit.getCodeName(), null);
        assertTrue(independentModule.isEnabled());        
        Module engineModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(engineUnit.getCodeName(), null);
        assertTrue(engineModule.isEnabled());
        Module dependingModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(dependingUnit.getCodeName(), null);
        assertTrue(dependingModule.isEnabled());
        OperationContainer<OperationSupport> uninstallContainer = OperationContainer.createForDirectUninstall();
        independentInfo = uninstallContainer.add(independentUnit.getInstalled());
        assertEquals("engine && depending needs independent",2, independentInfo.getRequiredElements().size());
        
        uninstallContainer.add(engineUnit.getInstalled());
        assertEquals("engine && depending needs independent",1, independentInfo.getRequiredElements().size());
        
        uninstallContainer.add(dependingUnit.getInstalled());
        assertEquals("engine && depending needs independent",0, independentInfo.getRequiredElements().size());        
    }
}

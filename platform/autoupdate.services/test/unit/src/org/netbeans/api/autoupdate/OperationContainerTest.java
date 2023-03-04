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
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/**
 *
 * @author Radek Matous
 */
public class OperationContainerTest extends DefaultTestCase {
    
    public OperationContainerTest(String testName) {
        super(testName);
    }
    public void testCreateFor() {
        OperationContainer<InstallSupport> install = OperationContainer.createForInstall();
        assertNotNull(install);
        assertNull("empty container",install.getSupport());
        
        OperationContainer<OperationSupport> install2 = OperationContainer.createForDirectInstall();
        assertNotNull(install2);
        assertNull("empty container",install2.getSupport());
        
        OperationContainer<InstallSupport> update = OperationContainer.createForUpdate();
        assertNotNull(update);
        assertNull("empty container",update.getSupport());
        
        OperationContainer<OperationSupport> uninstall = OperationContainer.createForDirectUninstall();
        assertNotNull(uninstall);
        assertNull("empty container",uninstall.getSupport());
        
        OperationContainer<OperationSupport> update2 = OperationContainer.createForDirectUpdate();
        assertNotNull(update2);
        assertNull("empty container",update2.getSupport());
        
        OperationContainer<OperationSupport> enable = OperationContainer.createForEnable();
        assertNotNull(enable);
        assertNull("empty container",enable.getSupport());
        
        OperationContainer<OperationSupport> disable = OperationContainer.createForDirectDisable();
        assertNotNull(disable);
        assertNull("empty container",disable.getSupport());
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
    public void testAdd() {
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);
        
        assertNull("empty container",installContainer.getSupport());
        assertNotNull(installContainer.add(engineElement));
        assertNull("cannot add the same twice",installContainer.add(engineElement));
        assertNotNull(installContainer.getSupport());
        
        
        UpdateElement engineelement2 = getAvailableUpdate(engineUnit,1);
        assertNull("two available updates cannot be installed",installContainer.add(engineelement2));
        
        UpdateUnit independentUnit = getUpdateUnit("org.yourorghere.independent");
        assertNull("cannot be installed",independentUnit.getInstalled());
        UpdateElement independentElement = getAvailableUpdate(independentUnit,0);
        assertNotNull(installContainer.add(independentElement));
        assertNotNull(installContainer.getSupport());
        
        OperationContainer[] containers = new OperationContainer[]{
            OperationContainer.createForUpdate(),
            OperationContainer.createForDirectUpdate(),
            OperationContainer.createForEnable(),
            OperationContainer.createForDirectDisable(),
            OperationContainer.createForDirectUninstall()
        };
        for (OperationContainer container : containers) {
            try {
                container.add(engineElement);
                fail("must be installed when update should be processed else IllegalArgumentException should be fired");
            } catch(IllegalArgumentException iax) {}
            assertNull("empty container",container.getSupport());
        }
    }
    public void testOperationInfo() {
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit independentUnit = getUpdateUnit("org.yourorghere.independent");
        assertNull("cannot be installed",independentUnit.getInstalled());
        UpdateElement independentElement = getAvailableUpdate(independentUnit,0);
        OperationInfo info = installContainer.add(independentElement);
        assertNotNull(info);
        assertEquals(0,installContainer.listInvalid().size());
        assertEquals(0, info.getRequiredElements().size());
        assertEquals(0, info.getBrokenDependencies().size());
        
        UpdateUnit dependingUnit = getUpdateUnit("org.yourorghere.depending");
        assertNull("cannot be installed",dependingUnit.getInstalled());
        UpdateElement dependingElement = getAvailableUpdate(dependingUnit,0);
        info = installContainer.add(dependingElement);
        assertNotNull(info);
        assertEquals(0,installContainer.listInvalid().size());
        assertEquals(1, info.getRequiredElements().size());
        assertEquals(0, info.getBrokenDependencies().size());

        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineUnitElement = getAvailableUpdate(engineUnit,0);        
        assertEquals(engineUnitElement, info.getRequiredElements().toArray()[0]);
        assertEquals(0, info.getBrokenDependencies().size());
        
        UpdateUnit brokenUnit = getUpdateUnit("org.yourorghere.brokendepending");
        assertNull("cannot be installed",brokenUnit.getInstalled());
        UpdateElement brokenElement = getAvailableUpdate(brokenUnit,0);
        info = installContainer.add(brokenElement);
        assertNotNull(info);
        assertEquals(0,installContainer.listInvalid().size());
        assertEquals(0, info.getRequiredElements().size());
        assertEquals(1, info.getBrokenDependencies().size());
    }
    public void testAdditionalRequiredElements() {
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);        
        OperationInfo<InstallSupport> engineInfo = installContainer.add(engineElement);
        assertNotNull(engineInfo);
        
        UpdateUnit independentUnit = getUpdateUnit("org.yourorghere.independent");
        assertNull("cannot be installed",independentUnit.getInstalled());
        UpdateElement independentElement = getAvailableUpdate(independentUnit,0);
        OperationInfo<InstallSupport> independentInfo = installContainer.add(independentElement);
        assertNotNull(independentInfo);
        
        UpdateUnit dependingUnit = getUpdateUnit("org.yourorghere.depending");
        assertNull("cannot be installed",dependingUnit.getInstalled());
        UpdateElement dependingElement = getAvailableUpdate(dependingUnit,0);
        OperationInfo dependingInfo = installContainer.add(dependingElement);
        assertNotNull(dependingInfo);
        assertEquals(0,installContainer.listInvalid().size());
        assertEquals(0, dependingInfo.getRequiredElements().size());
        assertEquals(0, dependingInfo.getBrokenDependencies().size());
        
        installContainer.remove(engineInfo);
        assertEquals(1, dependingInfo.getRequiredElements().size());
        installContainer.remove(independentInfo);
        assertEquals(2, dependingInfo.getRequiredElements().size());                
        assertEquals(0, dependingInfo.getBrokenDependencies().size());

        installContainer.add(independentInfo.getUpdateElement());
        assertEquals(1, dependingInfo.getRequiredElements().size());                
        assertEquals(0, dependingInfo.getBrokenDependencies().size());

        installContainer.add(engineInfo.getUpdateElement());
        assertEquals(0, dependingInfo.getRequiredElements().size());        
        assertEquals(0, dependingInfo.getBrokenDependencies().size());
    }
    
    private OperationInfo<InstallSupport> addElemenetForInstall (String codeName, OperationContainer<InstallSupport> installContainer) {
        UpdateUnit unit = getUpdateUnit(codeName);
        assertNull("can install " + unit, unit.getInstalled ());
        UpdateElement el = getAvailableUpdate (unit, 0);
        OperationInfo<InstallSupport> info = installContainer.add (el);
        assertNotNull ("OperationInfo not null for " + el, info);
        return info;
    }
    
    public void testListAllEquals () {
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        assertEquals(0, installContainer.listAll().size());
        
        addElemenetForInstall ("org.yourorghere.independent", installContainer);
        addElemenetForInstall ("org.yourorghere.depending", installContainer);
        addElemenetForInstall ("org.yourorghere.engine", installContainer);
        
        List<? extends OperationInfo> infosI = installContainer.listAll ();
        List<? extends OperationInfo> infosII = installContainer.listAll ();
        assertEquals ("Both list have same size.", infosI.size (), infosII.size ());
        for (int i = 0; i < infosI.size (); i++) {
            assertEquals (i + ". item is equal.", System.identityHashCode (infosI.get(i)), System.identityHashCode (infosII.get(i)));
            assertEquals (i + ". item is equal.", infosI.get(i), infosII.get(i));
        }
        assertEquals ("Both list are equals.", installContainer.listAll(), installContainer.listAll());
    }
    
    public void testListAll() {
        OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
        assertEquals(0, installContainer.listAll().size());
        
        addElemenetForInstall ("org.yourorghere.independent", installContainer);

        assertEquals(1, installContainer.listAll().size());
        
        addElemenetForInstall ("org.yourorghere.depending", installContainer);

        assertEquals(2, installContainer.listAll().size());
        
        OperationInfo<InstallSupport> info = addElemenetForInstall ("org.yourorghere.brokendepending", installContainer);

        assertEquals(3, installContainer.listAll ().size());
        
        installContainer.remove(info);
        assertEquals(2, installContainer.listAll().size());
    }
}

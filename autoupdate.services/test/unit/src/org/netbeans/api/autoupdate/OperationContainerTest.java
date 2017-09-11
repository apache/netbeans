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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

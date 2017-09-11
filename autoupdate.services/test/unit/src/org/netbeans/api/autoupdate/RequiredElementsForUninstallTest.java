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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

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


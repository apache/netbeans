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
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jirka Rechtacek
 */
public class NbmsInDownloadedTabTest extends NbmAdvancedTestCase {
    
    public NbmsInDownloadedTabTest(String testName) {
        super(testName);
    }
    
    public void testNbmDependsOnLowerVersion () throws IOException {
        URL higherEngineURL = TestUtils.class.getResource ("data/org-yourorghere-engine-1-2.nbm");
        assertNotNull ("URL data/org-yourorghere-engine-1-2.nbm exits", higherEngineURL);
        File higherEngineNbm = TestUtils.getFile(this, higherEngineURL);
        assertTrue ("File data/org-yourorghere-engine-1-2.nbm exits.", higherEngineNbm.exists ());
        
        URL dependingURL = TestUtils.class.getResource ("data/org-yourorghere-depending.nbm");
        assertNotNull ("URL data/org-yourorghere-depending.nbm exits", higherEngineURL);
        File dependingNbm = TestUtils.getFile(this, dependingURL);
        assertTrue ("File data/org-yourorghere-depending.nbm exits.", dependingNbm.exists ());
        
        URL independentURL = TestUtils.class.getResource ("data/org-yourorghere-independent-1-1.nbm");
        assertNotNull ("URL data/org-yourorghere-independent-1-1.nbm exits", independentURL);
        File independentNbm = TestUtils.getFile(this, independentURL);
        assertTrue ("File data/org-yourorghere-independent-1-1.nbm exits.", independentNbm.exists ());
        
        List<UpdateUnit> units =  UpdateUnitProviderFactory.getDefault ().create (
                "test",
                new File[] {higherEngineNbm, dependingNbm, independentNbm}).getUpdateUnits (UpdateManager.TYPE.MODULE);
        assertNotNull ("Update units found.", units);
        assertFalse ("Update units are not empty.", units.isEmpty ());
        assertEquals ("Three units.", 3, units.size ());
        
        Collection<UpdateElement> toInstall = new HashSet<UpdateElement> (units.size ());
        for (UpdateUnit u : units) {
            assertFalse (u + " has available updates.", u.getAvailableUpdates ().isEmpty ());
            toInstall.add (u.getAvailableUpdates ().get (0));
        }
        
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall ();
        oc.add (toInstall);
        assertTrue ("valid items in install container.", oc.listInvalid ().isEmpty ());
        assertEquals ("Three items.", 3, oc.listAll ().size ());
        
        for (OperationContainer.OperationInfo<InstallSupport> info : oc.listAll ()) {
            assertTrue (info.getUpdateElement () + " doesn't requires others.", info.getRequiredElements ().isEmpty ());
            assertEquals (info.getUpdateElement () + " doesn't have any broken dependencies.",
                    Collections.emptySet (),
                    info.getBrokenDependencies ());
            assertTrue (info.getUpdateElement () + " doesn't have any broken dependencies.", info.getBrokenDependencies ().isEmpty ());
        }
    }

    @RandomlyFails // NB-Core-Build #2131
    public void testRequiresDependency () throws IOException {
        String content = generateInfo (NbmAdvancedTestCase.generateModuleElementWithRequires ("o.n.m.requiresA", "1.0", "tokenA"));
        File requiresA = generateNBM ("o.n.m.requiresA", content);
        content = generateInfo (NbmAdvancedTestCase.generateModuleElementWithProviders ("o.n.m.providesA", "1.0", "tokenA"));
        File providesA = generateNBM ("o.n.m.providesA", content);
        
        List<UpdateUnit> units =  UpdateUnitProviderFactory.getDefault ().create (
                "test",
                new File[] { requiresA, providesA }).getUpdateUnits (UpdateManager.TYPE.MODULE);
        assertNotNull ("Update units found.", units);
        assertEquals ("Two units.", 2, units.size ());
        
        Collection<UpdateElement> toInstall = new HashSet<UpdateElement> (units.size ());
        for (UpdateUnit u : units) {
            assertFalse (u + " has available updates.", u.getAvailableUpdates ().isEmpty ());
            toInstall.add (u.getAvailableUpdates ().get (0));
        }
        
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall ();
        oc.add (toInstall);
        assertTrue ("valid items in install container.", oc.listInvalid ().isEmpty ());
        assertEquals ("Two items.", 2, oc.listAll ().size ());
        
        for (OperationContainer.OperationInfo<InstallSupport> info : oc.listAll ()) {
            assertTrue (info.getUpdateElement () + " doesn't requires others.", info.getRequiredElements ().isEmpty ());
            assertEquals (info.getUpdateElement () + " doesn't have any broken dependencies.",
                    Collections.emptySet (),
                    info.getBrokenDependencies ());
            assertTrue (info.getUpdateElement () + " doesn't have any broken dependencies.", info.getBrokenDependencies ().isEmpty ());
        }
    }
    
    
}


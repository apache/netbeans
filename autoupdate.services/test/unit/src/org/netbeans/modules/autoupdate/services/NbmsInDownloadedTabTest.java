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


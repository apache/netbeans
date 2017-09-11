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

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateInfoParserTest;
import org.netbeans.modules.autoupdate.updateprovider.LocalNBMsProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitFactoryTest extends NbTestCase {
    
    public UpdateUnitFactoryTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private static File NBM_FILE = null;
    
    @Override
    protected void setUp () throws IOException, URISyntaxException {
        clearWorkDir ();
        System.setProperty ("netbeans.user", getWorkDirPath ());
        Lookup.getDefault ().lookup (ModuleInfo.class);
        try {
            p = new MyProvider ();
        } catch (Exception x) {
            x.printStackTrace ();
        }
        p.refresh (true);
        URL urlToFile = AutoupdateInfoParserTest.class.getResource ("data/org-yourorghere-depending.nbm");
        NBM_FILE = Utilities.toFile(urlToFile.toURI ());
        assertNotNull ("data/org-yourorghere-depending.nbm file must found.", NBM_FILE);
    }
    
    public void testAppendInstalledModule () {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, ModuleInfo> modules = InstalledModuleProvider.getInstalledModules ();
        assertNotNull ("Some modules are installed.", modules);
        assertFalse ("Some modules are installed.", modules.isEmpty ());
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (
                unitImpls,
                InstalledModuleProvider.getDefault());
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        int modulesC = 0;
        int features = 0;
        
        for (UpdateUnit unit : newImpls.values ()) {
            switch (unit.getType ()) {
            case KIT_MODULE :
                modulesC ++;
                break;
            case MODULE :
                modulesC ++;
                break;
            case LOCALIZATION :
            case FEATURE :
                features ++;
                break;
            }
        }
        assertEquals ("Same size of installed modules and UpdateUnit (except FeatureElement).", modules.size (), modulesC);
    }
    
    public void testAppendUpdateItems () throws IOException {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some upadtes are present.", updates);
        assertFalse ("Some upadtes are present.", updates.isEmpty ());
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (
                unitImpls,
                p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        int modules = 0;
        int features = 0;
        int kits = 0;
        int installed = 0;
        
        for (UpdateUnit unit : newImpls.values ()) {
            switch (unit.getType ()) {
            case MODULE :
                modules ++;
                if (unit.getInstalled () != null) {
                    installed ++;
                }
                break;
            case KIT_MODULE :
                kits ++;
                if (unit.getInstalled () != null) {
                    installed ++;
                }
                break;
            case LOCALIZATION :
            case FEATURE :
                features ++;
                break;
            }
        }
        assertEquals ("Same size of upadtes (modules + features) and UpdateUnit", updates.size () - installed, kits + modules + features);
    }
    
    public void testGroupInstalledAndUpdates () {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateUnit> installedImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (
                unitImpls,
                InstalledModuleProvider.getDefault());
        Map<String, UpdateUnit> updatedImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (
                installedImpls, p);
        boolean isInstalledAndHasUpdates = false;
        for (String id : updatedImpls.keySet ()) {
            UpdateUnit impl = updatedImpls.get (id);
            UpdateElement installed = impl.getInstalled ();
            List<UpdateElement> updates = impl.getAvailableUpdates ();
            isInstalledAndHasUpdates = isInstalledAndHasUpdates || installed != null && updates != null && ! updates.isEmpty ();
            if (installed != null && updates != null && ! updates.isEmpty ()) {
                assertTrue ("Updates of module " + id + " contain newer one.", updates.get (0).getSpecificationVersion ().compareTo (installed.getSpecificationVersion ()) > 0);
            }
        }
        assertTrue ("Some module is installed and has updates.", isInstalledAndHasUpdates);
    }
    
    public void testGetUpdateUnitsInNbmFile () {
        UpdateProvider localFilesProvider = new LocalNBMsProvider ("test-local-file-provider", NBM_FILE);
        assertNotNull ("LocalNBMsProvider found for file " + NBM_FILE, localFilesProvider);
        Map<String, UpdateUnit> units = UpdateUnitFactory.getDefault().getUpdateUnits (localFilesProvider);
        assertNotNull ("UpdateUnit found in provider " + localFilesProvider.getDisplayName (), units);
        assertEquals ("Provider providers only once unit in provider" + localFilesProvider.getName (), 1, units.size ());
        String id = units.keySet ().iterator ().next ();
        assertNotNull (localFilesProvider.getName () + " gives UpdateUnit.", units.get (id));
        UpdateUnit u = units.get (id);
        assertNull ("Unit is not installed.", u.getInstalled ());
        assertNotNull ("Unit has update.", u.getAvailableUpdates ());
        assertFalse ("Unit.getAvailableUpdates() is not empty.", u.getAvailableUpdates ().isEmpty ());
        assertEquals ("Unit has only one update.", 1, u.getAvailableUpdates ().size ());
        UpdateElement el = u.getAvailableUpdates ().get (0);
        assertEquals ("org.yourorghere.depending", el.getCodeName ());
        assertEquals ("1.0", el.getSpecificationVersion ());
        assertEquals (0, el.getDownloadSize ());
    }
    
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
    
}

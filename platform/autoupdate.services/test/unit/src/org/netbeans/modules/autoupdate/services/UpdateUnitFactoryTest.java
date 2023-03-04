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

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
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
        URL urlToFile = TestUtils.class.getResource ("data/org-yourorghere-depending.nbm");
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
        for (Map.Entry<String, UpdateUnit> entry : updatedImpls.entrySet ()) {
            String id = entry.getKey ();
            UpdateUnit impl = entry.getValue();
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
            super ("test-updates-provider", "test-updates-provider", TestUtils.class.getResource ("data/catalog.xml"));
        }
    }

}

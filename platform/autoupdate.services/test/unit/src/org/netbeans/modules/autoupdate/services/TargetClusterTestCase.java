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
import java.util.Arrays;
import java.util.Map;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class TargetClusterTestCase extends NbmAdvancedTestCase {

    public TargetClusterTestCase (String testName) {
        super (testName);
    }
    
    @Override
    protected void setUp () throws IOException, Exception {
        super.setUp ();
        Lookup.getDefault ().lookup (ModuleInfo.class);
        MockClusterCreator.installDir = installDir;
        MockServices.setServices (MockClusterCreator.class);
    }
    
    protected String getCodeName (String target, Boolean global) {
        return "org.yourorghere." + target + "." + global;
    }
    
    protected UpdateElement getInstalledUpdateElement () throws IOException, OperationException {
        return null;
    }

    protected File getTargetCluster (String target, Boolean global) throws IOException, OperationException {
        assertTrue (target + " cannot be empty.", target == null || target.length () > 0);
        String module = getCodeName (target, global);

        String catalog = generateCatalog (generateModuleElement (module, "1.1", global, target));
        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());

        // check being
        assertTrue (module + " found in parsed items.", updates.keySet ().contains (module + "_1.1"));

        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);

        UpdateUnit uu = UpdateManagerImpl.getInstance ().getUpdateUnit (module);
        assertNotNull (module + " - UpdateUnit found.", uu);
        assertFalse ("Available updates " + uu, uu.getAvailableUpdates ().isEmpty ());
        UpdateElement ue = uu.getAvailableUpdates ().get (0);
        ModuleUpdateElementImpl impl = (ModuleUpdateElementImpl) Trampoline.API.impl (ue);
        assertNotNull ("Impl " + ue + " found and is instanceof ModuleUpdateElementImpl.", impl);

        File targetDir = InstallManager.findTargetDirectory (getInstalledUpdateElement (), impl, global, false);
        assertNotNull ("Target cluster cannot be null for " + impl, targetDir);

        return targetDir;
    }

    protected UpdateElement installModule (String codeName) throws IOException, OperationException {
        String catalog = generateCatalog (generateModuleElement (codeName, "1.0", true, platformDir.getName ()));
        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());

        // check being
        assertTrue (codeName + " found in parsed items.", updates.keySet ().contains (codeName + "_1.0"));

        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);

        UpdateUnit uu = UpdateManagerImpl.getInstance ().getUpdateUnit (codeName);
        assertNotNull (codeName + " - UpdateUnit found.", uu);

        assertFalse ("Available updates " + uu, uu.getAvailableUpdates ().isEmpty ());

        assertEquals (codeName + " goes into platformDir.", platformDir.getName (), InstallManager.findTargetDirectory (null, Trampoline.API.impl (uu.getAvailableUpdates ().get (0)), null, false).getName ());

        UpdateElement installed = installUpdateUnit (uu);

        assertNotNull (codeName + " is installed.", installed);
        assertEquals (codeName + " is as same as installed in " + installed.getUpdateUnit (), installed.getUpdateUnit ().getInstalled (), installed);

        // XXX: workaround missing real NBM for codeName
        File modules = new File (new File (platformDir, "config"), "Modules");
        modules.mkdirs ();
        new File (modules, installed.getCodeName ().replace ('.', '-') + ".xml").createNewFile ();

        return installed;
    }

    public static final class MockClusterCreator extends AutoupdateClusterCreator {

        static File installDir = null;

        public MockClusterCreator () {}

        protected File findCluster (String clusterName) {
            assertNotNull ("installDir found.", installDir);
            assertTrue (installDir + " directory exists.", installDir.isDirectory () && installDir.exists ());
            assertNotNull ("clusterName " + clusterName + " is not null.", clusterName);
            for (int i = 0; i < installDir.listFiles ().length; i++) {
                if (clusterName.equals (installDir.listFiles () [i].getName ())) {
                    return installDir.listFiles () [i];
                }
            }
            return new File (installDir, clusterName);
        }

        protected File[] registerCluster (String clusterName, File f) {
            try {
                f.createNewFile ();
            } catch (Exception x) {
                fail ("While registerCluster (" + clusterName + ", " + f + ") thrown " + x);
            }
            assertTrue (Arrays.asList (installDir.listFiles ()).contains (f));
            assertTrue (f.exists ());
            return installDir.listFiles ();
        }
    }
}

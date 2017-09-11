/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

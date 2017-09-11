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

import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureIncompleteTest extends NbTestCase {

    protected boolean modulesOnly = true;
    List<UpdateUnit> keepItNotToGC;

    public FeatureIncompleteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(FeatureIncompleteTest.class).gui(false).suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        TestUtils.setUserDir(getWorkDirPath());
        TestUtils.testInit();
        // Note: the FolderLookup could refresh in half-state, with just MyProvider.instance visible,
        // so it will refuse to load classes from default package.
        FileUtil.getConfigRoot().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "Services/MyProvider.instance");
                fo.setAttribute("instanceCreate", new MyProvider());
            }
        });
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        /* XXX for some reason this does not work:
         MockLookup.setInstances(new MyProvider());
         */
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
    }

    public void testIncompleteFeature() throws OperationException {
        List<UpdateUnit> features = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        List<UpdateUnit> modules = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        assertNotNull("A feature found.", features);
        assertEquals("Only once feature there.", 1, features.size());
        UpdateUnit feature = features.get(0);
        assertNotNull(feature + " is installed.", feature.getInstalled());
        assertFalse("Not all modules are enabled as such the feature shall be in "
                + "disabled state:\n" + modules, feature.getInstalled().isEnabled());
    }

    private static class MyProvider implements UpdateProvider {

        @Override
        public String getName() {
            return FeatureNotUpToDateTest.class.getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public String getDescription() {
            return getName();
        }

        @Override
        public Map<String, UpdateItem> getUpdateItems() throws IOException {
            Map<String, UpdateItem> items = InstalledModuleProvider.getDefault().getUpdateItems();
            assertNotNull("Installed modules must found.", items);

            Map<String, UpdateItem> res = new HashMap<String, UpdateItem> (); //InstalledModuleProvider.getDefault().getUpdateItems();

            Set<String> deps = new HashSet<String>(items.size());
            for (String id : items.keySet()) {
                String dep;
                UpdateItem item = items.get(id);
                assertNotNull("Impl of " + item + " available", Trampoline.SPI.impl(item));
                UpdateItemImpl itemImpl = Trampoline.SPI.impl(item);
                assertTrue("Impl of " + item + "is ModuleItem", itemImpl instanceof ModuleItem);
                ModuleItem moduleItem = (ModuleItem) itemImpl;
                Module m = Utilities.toModule(moduleItem.getModuleInfo());
                if (m != null && m.getProblems().isEmpty()) {
                    dep = moduleItem.getModuleInfo().getCodeNameBase() + " > " + moduleItem.getSpecificationVersion();
                    deps.add(dep);
                }
            }

            res.put("testFeatureVsStandaloneModules",
                    UpdateItem.createFeature(
                    "testFeatureVsStandaloneModules",
                    "1.0",
                    deps,
                    null,
                    null,
                    null));
            return res;
        }

        @Override
        public boolean refresh(boolean force) throws IOException {
            return true;
        }

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    } // end of MyProvider
}

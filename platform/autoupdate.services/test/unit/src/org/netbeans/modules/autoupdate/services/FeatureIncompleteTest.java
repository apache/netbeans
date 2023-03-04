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
            for (UpdateItem item : items.values()) {
                String dep;
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

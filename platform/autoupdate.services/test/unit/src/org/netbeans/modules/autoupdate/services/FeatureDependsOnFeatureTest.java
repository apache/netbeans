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
import org.netbeans.modules.autoupdate.updateprovider.*;
import org.netbeans.api.autoupdate.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class FeatureDependsOnFeatureTest extends NbTestCase {

    protected boolean modulesOnly = true;
    List<UpdateUnit> keepItNotToGC;

    public FeatureDependsOnFeatureTest(String testName) {
        super(testName);
    }

    public static class MyProvider implements UpdateProvider {

        @Override
        public String getName() {
            return FeatureDependsOnFeatureTest.class.getName();
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
            int size = items.size();
            assertTrue("Count of installed modules are more then once.", size > 1);
            String pilotName = items.keySet().iterator().next();
            assertNotNull(pilotName + "must found", items.get(pilotName));
            UpdateItem pilotItem = items.get(pilotName);
            assertNotNull("Impl of " + pilotItem + " available", Trampoline.SPI.impl(pilotItem));
            UpdateItemImpl pilotItemImpl = Trampoline.SPI.impl(pilotItem);
            assertTrue("Impl of " + pilotItem + "is ModuleItem", pilotItemImpl instanceof ModuleItem);
            ModuleItem pilotModuleItem = (ModuleItem) pilotItemImpl;
            SpecificationVersion pilotSV = new SpecificationVersion(pilotModuleItem.getSpecificationVersion());
            assertTrue("a dot is present in " + pilotSV, pilotSV.toString().indexOf('.') != -1);
            int dot = pilotSV.toString().indexOf('.');
            String postSpec = pilotSV.toString().substring(dot + 1);
            String preSpec = pilotSV.toString().substring(0, dot);
            Integer digit = 0;
            try {
                digit = Integer.parseInt(preSpec) + 1;
            } catch (NumberFormatException nfe) {
                fail(nfe.getLocalizedMessage());
            }
            SpecificationVersion higherSV = new SpecificationVersion(digit + "." + postSpec);
            assertTrue(higherSV + " is more then " + pilotSV, higherSV.compareTo(pilotSV) > 0);
            String higherDep = pilotModuleItem.getModuleInfo().getCodeNameBase() + " > " + higherSV;

            Set<String> deps = new HashSet<String>(items.size());
            for (String id : items.keySet()) {
                String dep = null;
                if (!pilotName.equals(id)) {
                    UpdateItem item = items.get(id);
                    assertNotNull("Impl of " + item + " available", Trampoline.SPI.impl(item));
                    UpdateItemImpl itemImpl = Trampoline.SPI.impl(item);
                    assertTrue("Impl of " + item + "is ModuleItem", itemImpl instanceof ModuleItem);
                    ModuleItem moduleItem = (ModuleItem) itemImpl;
                    Module m = Utilities.toModule(moduleItem.getModuleInfo());
                    if (m != null && m.getProblems().isEmpty()) {
                        dep = moduleItem.getModuleInfo().getCodeNameBase() + " > " + moduleItem.getSpecificationVersion();
                    }
                } else {
                    dep = higherDep;
                }
                if (dep != null) {
                    deps.add(dep);
                }
            }
            Map<String, UpdateItem> res = InstalledModuleProvider.getDefault().getUpdateItems();
            ModuleInfo info = pilotModuleItem.getModuleInfo();
            UpdateItemImpl higherItemImpl = new InstalledModuleItem(
                    info.getCodeNameBase(),
                    higherSV.toString(),
                    new HackedModuleInfo(info, higherSV),
                    null, // XXX author
                    null, // installed cluster
                    null);
            UpdateItem higherModuleItem = Utilities.createUpdateItem(higherItemImpl);

            res.put("testFeatureDependsOnModules",
                    UpdateItem.createFeature(
                    "testFeatureDependsOnModules",
                    "1.0",
                    deps,
                    null,
                    null,
                    null));
            res.put(pilotName, higherModuleItem);
            res.put("testDependsOnFeature", UpdateItem.createFeature(
                    "testDependsOnFeature", "1.3",
                    Collections.singleton("testFeatureDependsOnModules"),
                    null, null, null));
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
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        TestUtils.setUserDir(getWorkDirPath());
        TestUtils.testInit();
        MockServices.setServices(MyProvider.class);
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
    }

    public void testFeatureDependsOnInstalledFeature() {
        assertNotNull("A feature found.", UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE));
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        assertEquals("Two features there.", 2, units.size());
        UpdateUnit testDependsOnFeature;
        UpdateUnit testFeatureDependsOnModules;
        if ("testFeatureDependsOnModules".equals(units.get(0).toString())) {
            testFeatureDependsOnModules = units.get(0);
            testDependsOnFeature = units.get(1);
        } else {
            testDependsOnFeature = units.get(0);
            testFeatureDependsOnModules = units.get(1);
        }
        assertNotNull(testFeatureDependsOnModules + " is installed.", testFeatureDependsOnModules.getInstalled());
        assertFalse(testFeatureDependsOnModules + " has some available updates.", testFeatureDependsOnModules.getAvailableUpdates().isEmpty());
        assertNotNull(testDependsOnFeature + " is installed.", testDependsOnFeature.getInstalled());
        assertFalse(testDependsOnFeature + " has some available updates.", testDependsOnFeature.getAvailableUpdates().isEmpty());
    }

    static final class HackedModuleInfo extends ModuleInfo {

        private ModuleInfo info;
        private SpecificationVersion hackedVersion;

        public HackedModuleInfo(ModuleInfo info, SpecificationVersion hackedVersion) {
            this.info = info;
            this.hackedVersion = hackedVersion;
        }

        @Override
        public String getCodeNameBase() {
            return info.getCodeNameBase();
        }

        @Override
        public int getCodeNameRelease() {
            return info.getCodeNameRelease();
        }

        @Override
        public String getCodeName() {
            return info.getCodeName();
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return hackedVersion;
        }

        @Override
        public boolean isEnabled() {
            return info.isEnabled();
        }

        @Override
        public Object getAttribute(String attr) {
            return info.getAttribute(attr);
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            return info.getLocalizedAttribute(attr);
        }

        @Override
        public Set<Dependency> getDependencies() {
            return info.getDependencies();
        }

        @Override
        public boolean owns(Class<?> clazz) {
            return info.owns(clazz);
        }
    }
}

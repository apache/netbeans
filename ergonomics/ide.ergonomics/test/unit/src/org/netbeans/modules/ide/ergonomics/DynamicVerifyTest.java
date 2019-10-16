/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FoDUpdateUnitProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.project.ProjectFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DynamicVerifyTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    public DynamicVerifyTest(String n) {
        super(n);
    }

    public static Test suite() {
        NbTestSuite all = new NbTestSuite();
        Test full = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(DynamicVerifyTest.class).
            addTest(ProjectTemplatesCheck.class).
            addTest(LayersCheck.class).
            addTest(FilesAndAttributesCheck.class, "testGetAllTemplates", "testCheckAllTemplatesPretest").
            addTest(DebuggerAttachTypesCheck.class, "testGetAllDebuggers").
            addTest(AvailableJ2EEServerCheck.class, "testGetAllServerWizardsReal").
            addTest(CloudNodeCheck.class, "testGetAllCloudWizardsReal").
            addTest(ServersNodeActionsCheck.class, "testGetAll", "testCheckAllPretest").
            addTest(MenuProfileActionsCheck.class, "testGetAll", "testCheckAllPretest").
            addTest(LibrariesCheck.class, "testGetLibraries", "testCheckLibrariesPretest").
            addTest(ProjectConvertorCheck.class, "testGetConvertors", "testCheckConvertorsPretest").
            addTest(OptionsCheck.class, "testGetKeywords", "testCheckKeywordsPretest").
            gui(false).
            clusters("ergonomics.*").
            clusters("^(?!(mobility)).*$").
            enableModules(".*").
            honorAutoloadEager(true)
        );
        Test ergonomics = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(ProjectTemplatesCheck.class).
            addTest(AllClustersProcessedCheck.class).
            addTest(FilesAndAttributesCheck.class, "testCheckAllTemplatesReal", "testTemplateHTMLDescriptions").
            addTest(DebuggerAttachTypesCheck.class, "testGetAllDebuggersReal").
            addTest(AvailableJ2EEServerCheck.class, "testGetAllServerWizardsErgo").
            addTest(CloudNodeCheck.class, "testGetAllCloudWizardsErgo").
            addTest(ServersNodeActionsCheck.class, "testCheckAllReal").
            addTest(MenuProfileActionsCheck.class, "testCheckAllReal").
            addTest(LibrariesCheck.class, "testCheckLibrariesReal").
            addTest(ProjectConvertorCheck.class, "testCheckConvertorsReal").
            addTest(OptionsCheck.class, "testCheckKeywordsReal").
            addTest(BundleFormatCheck.class).
            gui(false).
            clusters("ergonomics.*").
            clusters("^(?!(mobility)).*$").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );

        all.addTest(full);
        all.addTest(new WarningsCheck("testEnable"));
        all.addTest(ergonomics);
        all.addTest(new WarningsCheck("testNoWarnings"));

        return all;
    }

    public void testNoUserDefinedFeaturesInStandardBuild() throws Exception {
        FoDUpdateUnitProvider instance = new FoDUpdateUnitProvider();

        Map<String, UpdateItem> items = instance.getUpdateItems();
        UpdateItem updateItem = items.get("fod.user.installed");
        if(updateItem == null) {
            return; // The best case
        }
        UpdateItemImpl updateItemImpl = Trampoline.SPI.impl(updateItem);
        assertTrue("Unexpected UpdateItemImpl: " + updateItemImpl.getClass().getName(), updateItemImpl instanceof FeatureItem);
        FeatureItem featureItem = (FeatureItem) updateItemImpl;

        Set<String> moduleNames = featureItem.getModuleCodeNames();

        // OpenJFX can't be distributed with netbeans as GPL2-CP is not
        // whitelisted by Apache legal. To make it easier update items with
        // only references to maven central are provided, that make it easy
        // for the end user to install the necessary dependencies
        //
        // This results in these modules to be reported as user installable
        // from a base installation:
        moduleNames.removeAll(Arrays.asList(
            "org.netbeans.libs.javafx.linux",
            "org.netbeans.libs.javafx.macosx",
            "org.netbeans.libs.javafx.win"));

        System.out.println(featureItem.getModuleCodeNames());
        assertTrue(
                "No user installed modules should be in standard build. If this happens,\n" +
                "like in case of http://openide.netbeans.org/issues/show_bug.cgi?id=174052\n" +
                "then you probably added new module and did not categorize it properly,\n" +
                "or you have additional modules (not part of regular build) " +
                "in your installation. Found: " + moduleNames, moduleNames.isEmpty());
    }

    public void testGetAllProjectFactories() throws Exception {
        StringBuilder sb = new StringBuilder();
        Map<String,String> all = FeatureManager.projectFiles();

        all.put("Fine", "org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton");
        all.put("FineToo", "org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory");
        all.put("MostlyOK", "org.netbeans.modules.java.openjdk.project.FilterStandardProjects");

        iterateRegistrations(sb, ProjectFactory.class, null, all);
        
        if (!all.isEmpty()) {
            fail("Not all IDE projects are registered for ergonomics mode, see the list below.\n" +
                "This may mean that you are not using @AntBasedProjectRegistration to register\n" +
                "your projects, or that you need to hardcode the nature of your project into\n" +
                "ide.ergonomics/*.properties using XPath. For more information see\n" +
                "http://wiki.netbeans.org/FitnessForever " +
                "The list of differences follows:\n" + sb
            );
        }
    }
    
    public void testGetAllNbProjects() throws Exception {
        Map<String,String> all = FeatureManager.nbprojectTypes();
        StringBuilder sb = new StringBuilder();

        Class<?> ant = Class.forName(
            "org.netbeans.spi.project.support.ant.AntBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(sb, ant, ant.getDeclaredMethod("getType"), all);

        if (!all.isEmpty()) {
            fail("Not all IDE projects are registered for ergonomics mode, see the list below.\n" +
                "This may mean that you are not using @AntBasedProjectRegistration to register\n" +
                "your projects. For more information see\n" +
                "http://wiki.netbeans.org/FitnessForever " +
                "The list of differences follows:\n" + sb
            );
        }
    }

    private void iterateRegistrations(
        StringBuilder sb, Class<?> what, Method info, Map<String,String> all
    ) throws Exception {
        for (Object f : Lookup.getDefault().lookupAll(what)) {
            if (f.getClass().getPackage().getName().equals("org.netbeans.modules.ide.ergonomics.fod")) {
                continue;
            }
            sb.append(f.getClass().getName());
            if (info != null) {
                Object more = info.invoke(f);
                sb.append(" info: ").append(more);
                Object value = all.get(more);
                if (f.getClass().getName().equals(value)) {
                    sb.append(" OK");
                    all.remove(more);
                } else {
                    sb.append(" not present");
                    all.put("FAIL", more.toString());
                }
            } else {
                if (all.values().remove(f.getClass().getName())) {
                    sb.append(" OK");
                } else {
                    all.put("FAIL", f.getClass().getName());
                    sb.append(" not present");
                }
            }
            sb.append('\n');
        }
        if (all.isEmpty()) {
            return;
        }
        sb.append("\nShould be empty: ").append(all).append("\n");

        for (Map.Entry<String, String> entry : all.entrySet()) {
            sb.append("\nNot processed: ").append(entry.getKey()).append(" = ").append(entry.getValue());
        }
    }

}

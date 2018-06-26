/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
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

        assertNull("No user installed modules should be in standard build. If this happens,\n" +
                "like in case of http://openide.netbeans.org/issues/show_bug.cgi?id=174052\n" +
                "then you probably added new module and did not categorize it properly,\n" +
                "or you have additional modules (not part of regular build) " +
                "in your installation.", items.get("fod.user.installed"));
    }

    public void testGetAllProjectFactories() throws Exception {
        StringBuilder sb = new StringBuilder();
        Map<String,String> all = FeatureManager.projectFiles();

        all.put("Fine", "org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton");
        all.put("FineToo", "org.netbeans.modules.project.ui.convertor.ProjectConvertorFactory");

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

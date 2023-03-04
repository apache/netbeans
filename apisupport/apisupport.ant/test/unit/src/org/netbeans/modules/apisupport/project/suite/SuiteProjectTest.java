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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

/**
 * Test basic {@link SuiteProject} stuff.
 * @author Jesse Glick
 */
public class SuiteProjectTest extends NbTestCase {
    
    public SuiteProjectTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }
    
    public void testProjectInformation() throws Exception {
        SuiteProject p = TestBase.generateSuite(getWorkDir(), "Sweet Stuff");
        ProjectInformation i = ProjectUtils.getInformation(p);
        assertEquals("Sweet_Stuff", i.getName());
        assertEquals("Sweet Stuff", i.getDisplayName());
        BrandingModel model = new SuiteBrandingModel(new SuiteProperties(p, p.getHelper(), p.getEvaluator(), Collections.<NbModuleProject>emptySet()));
        model.init();
        assertEquals("sweet_stuff", model.getName());
        assertEquals("Sweet Stuff", model.getTitle());
        TestBase.TestPCL l = new TestBase.TestPCL();
        i.addPropertyChangeListener(l);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "sweetness");
        ep.setProperty("app.title", "Sweetness is Now!");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertEquals(new HashSet<String>(Arrays.asList(ProjectInformation.PROP_NAME, ProjectInformation.PROP_DISPLAY_NAME)), l.changed);
        assertEquals("Sweet_Stuff", i.getName());
        assertEquals("Sweetness is Now!", i.getDisplayName());
        model = new SuiteBrandingModel(new SuiteProperties(p, p.getHelper(), p.getEvaluator(), Collections.<NbModuleProject>emptySet()));
        model.init();
        assertEquals("sweetness", model.getName());
        assertEquals("Sweetness is Now!", model.getTitle());
    }
    
    public void testSuiteEvaluatorReturnsUpToDateValues() throws Exception { // #67314, #72981
        SuiteProject suite1 = TestBase.generateSuite(getWorkDir(), "suite1");
        PropertyEvaluator eval = suite1.getEvaluator();
        TestBase.generateSuiteComponent(suite1, "module1");
        
        FileObject suiteEPFO = suite1.getProjectDirectory().getFileObject("nbproject/project.properties");
        EditableProperties suiteEP = Util.loadProperties(suiteEPFO);
        assertEquals("modules property", "${project.org.example.module1}", suiteEP.getProperty("modules"));
        assertEquals("project.org.example.module1 property", "module1", suiteEP.getProperty("project.org.example.module1"));
        // #67314
        assertEquals("up-to-date 'modules' property from suite evaluator", "module1", eval.getProperty("modules"));
        
        // branding.dir check (#72981)
        assertEquals("branding.dir has a default value", "branding", eval.getProperty("branding.dir"));
        suiteEP.setProperty("custom.dir", "custom");
        suiteEP.setProperty("branding.dir", "${custom.dir}/nonDefaultBrandingDir");
        suite1.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, suiteEP);
        assertEquals("branding dir evaluated", "custom/nonDefaultBrandingDir", eval.getProperty("branding.dir"));
    }
    
    public void testPlatformPropertiesFromEvaluatorAreUpToDate() throws Exception {
        final SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite1", "custom");
        PropertyEvaluator eval = suite.getEvaluator();
        assertEquals("custom", eval.getProperty("nbplatform.active"));
        assertEquals(NbPlatform.getPlatformByID("custom").getDestDir(), suite.getHelper().resolveFile(eval.getProperty(ModuleList.NETBEANS_DEST_DIR)));
        
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                // simulate change (e.g. through suite properties)
                FileObject plafProps = suite.getProjectDirectory().getFileObject("nbproject/platform.properties");
                EditableProperties ep = Util.loadProperties(plafProps);
                ep.setProperty("nbplatform.active", "default");
                Util.storeProperties(plafProps, ep);
                return null;
            }
        });
        
        assertEquals("nbplatform.active change took effect", "default", eval.getProperty("nbplatform.active"));
        assertEquals("#67628: netbeans.dest.dir change did as well", NbPlatform.getDefaultPlatform().getDestDir(), suite.getHelper().resolveFile(eval.getProperty(ModuleList.NETBEANS_DEST_DIR)));
    }

    public void testGetPlatformVersionedLocation() throws Exception {
        File plafdir = new File(getWorkDir(), "plaf");
        TestFileUtils.writeZipFile(new File(plafdir, "platform/core/core.jar"), "j:unk");
        File harnessdir = new File(getWorkDir(), "harness");
        TestFileUtils.writeZipFile(new File(harnessdir, "modules/org-netbeans-modules-apisupport-harness.jar"), "META-INF/MANIFEST.MF:OpenIDE-Module-Specification-Version: 1.23\n");
        File suitedir = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(suitedir, "_", false);
        FileObject suitedirFO = FileUtil.toFileObject(suitedir);
        FileObject plafProps = suitedirFO.getFileObject("nbproject/platform.properties");
        EditableProperties ep = Util.loadProperties(plafProps);
        ep.setProperty("suite.dir", "${basedir}");
        ep.remove("nbplatform.active");
        ep.setProperty("nbplatform.active.dir", "${suite.dir}/../plaf");
        ep.setProperty("harness.dir", "${suite.dir}/../harness");
        ep.setProperty("cluster.path", new String[] {"${nbplatform.active.dir}/platform:", "${harness.dir}"});
        Util.storeProperties(plafProps, ep);
        SuiteProject p = (SuiteProject) ProjectManager.getDefault().findProject(suitedirFO);
        NbPlatform plaf = p.getPlatform(true);
        assertEquals(plafdir, plaf.getDestDir());
        assertEquals(harnessdir, plaf.getHarnessLocation());
        assertEquals(HarnessVersion.V70, plaf.getHarnessVersion());
    }

}

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

package org.netbeans.modules.apisupport.project.ui;

import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test {@link SuiteOperations}.
 *
 * @author Martin Krauskopf
 */
public class SuiteOperationsTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    public SuiteOperationsTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    @RandomlyFails // NB-Core-Build #9919: Unstable, NB-Core-Build #9918 on the same sources passed
    public void testDeleteOfEmptySuite() throws Exception {
        SuiteProject suite = generateSuite("suite");
        suite.open();
        SuiteActions ap = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = suite.getProjectDirectory();
        
        // build project
        ap.invokeActionImpl(ActionProvider.COMMAND_BUILD, suite.getLookup()).waitFinished();
        assertNotNull("suite was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = {
            prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(suite));
        assertTrue("no data files", ProjectOperations.getDataFiles(suite).isEmpty());
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(suite);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
    }

    // TODO test deleting of JNLP descriptors and behavior vs. lib wrapper projects (#72981) 
    public void testDeleteOfNonEmptySuite() throws Exception {
        SuiteProject suite = generateSuite("suite");
        FileObject prjDir = suite.getProjectDirectory();
        prjDir.createFolder("branding");
        NbModuleProject module1 = TestBase.generateSuiteComponent(suite, "module1");
        NbModuleProject module2 = TestBase.generateSuiteComponent(suite, "module2");
        assertEquals("module1 is suite component", NbModuleType.SUITE_COMPONENT, module1.getModuleType());
        assertEquals("module2 is suite component", NbModuleType.SUITE_COMPONENT, module2.getModuleType());
        module1.open();
        module2.open();
        
        suite.open();
        SuiteActions ap = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        
        // build project
        ap.invokeActionImpl(ActionProvider.COMMAND_BUILD, suite.getLookup()).waitFinished();
        prjDir.refresh();
        assertNotNull("suite was built", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = new FileObject[] {
            prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(suite));
        FileObject[] expectedDataFiles = new FileObject[] {
            prjDir.getFileObject("branding"),
        };
        assertEquals("correct data files", Arrays.asList(expectedDataFiles), ProjectOperations.getDataFiles(suite));
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(suite);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
        
        assertEquals("module1 became standalone module", NbModuleType.STANDALONE, module1.getModuleType());
        assertEquals("module2 became standalone module", NbModuleType.STANDALONE, module2.getModuleType());
    }
    
    public void testMoveModule() throws Exception {
        SuiteProject suite = generateSuite("suite");
        NbModuleProject outer = generateSuiteComponent(suite, getWorkDir(), "outer");
        outer.open();
        NbModuleProject customOuter = generateSuiteComponent(suite, getWorkDir(), "customOuter");
        FileUtil.createData(customOuter.getProjectDirectory(), "mydocs/index.html");
        customOuter.open();
        
        Project inner = SuiteOperations.moveModule(outer, suite.getProjectDirectory());
        assertNotNull("inner successfully moved", inner);
        assertSame("inner is still in the same suite component", suite, SuiteUtils.findSuite(inner));
        assertFalse(ProjectManager.getDefault().isValid(outer));
        assertFalse(outer.getProjectDirectory().isValid());
        
        Project customInner = SuiteOperations.moveModule(customOuter, suite.getProjectDirectory());
        assertNotNull("customInner successfully moved", customInner);
        assertSame("customInner is still in the same suite component", suite, SuiteUtils.findSuite(customInner));
        assertFalse(ProjectManager.getDefault().isValid(customOuter));
        assertFalse("customOuter directory is not valid anymore", customOuter.getProjectDirectory().isValid());
        FileObject mydocsIndex = customInner.getProjectDirectory().getFileObject("mydocs/index.html");
        assertNotNull("mydocs was also moved", mydocsIndex);
        assertTrue("mydocs is valid", mydocsIndex.isValid());
    }
    
}

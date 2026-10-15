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

package org.netbeans.modules.apisupport.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.Reader;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Petr Zajac
 */
public class CompilationDependencyTest extends TestBase {
    
    private static final String WINDOWS = "org.openide.windows";
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public CompilationDependencyTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
        
    }
    
    protected @Override void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }
    
    public void testInvalidSpecVersion() throws Exception {
        NbModuleProject testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
        
        FileObject buildScript = findBuildXml(testingProject);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"jar"}, null);
        et.waitFinished();
        assertEquals("Error during ant ...",0,et.result());
        SpecificationVersion invalid = new SpecificationVersion("1000");
        ApisupportAntUtils.addDependency(testingProject, WINDOWS, null, invalid, true, null);
        ProjectManager.getDefault().saveProject(testingProject);
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","jar"}, null);
        et.waitFinished();
        
        // it must fail but I don't know why it passed
        assertFalse("Error during ant ...", 0 == et.result());
        assertFalse("Successfully compiled when is invalid specification version",
                testingProject.getModuleJarLocation().exists());
    }

    // TODO fixme
    public void fails_on_11_testCompileAgainstPublicPackage() throws Exception {
        NbModuleProject testingProject = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        testingProject.open();
        FileObject buildScript = findBuildXml(testingProject);
        assertNotNull(buildScript);
        
        ApisupportAntUtils.addDependency(testingProject, WINDOWS, null, null, true, null);
        ProjectManager.getDefault().saveProject(testingProject);
        
        FileObject javaFo = testingProject.getSourceDirectory().getFileObject("org/example/testing").createData("JavaFile.java");
        PrintStream ps = new PrintStream(javaFo.getOutputStream());
        ps.println("package org.example.testing;");
        ps.println("import org.netbeans.modules.openide.windows.*;");
        ps.println("public class JavaFile {}");
        ps.close();
        
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        et.waitFinished();
        
        assertFalse("project was successfully compiled against non public package",
                testingProject.getModuleJarLocation().exists());
        
        ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        testingPXM.removeDependency(WINDOWS);
        ModuleEntry module = testingProject.getModuleList().getEntry(WINDOWS);
        ModuleDependency newDep = new ModuleDependency(module,module.getReleaseVersion(),module.getSpecificationVersion(),true,true);
        testingPXM.addDependency(newDep);
        ProjectManager.getDefault().saveProject(testingProject);
        
        et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        Reader reader = et.getInputOutput().getIn();
        BufferedReader breader = new BufferedReader(reader);
        et.waitFinished();
        String line = null;
        while ((line = breader.readLine()) != null ) {
            log(line);
            System.out.println(line);
        }
        assertTrue("compilation failed for implementation dependency: no file " + testingProject.getModuleJarLocation(),
                testingProject.getModuleJarLocation().exists());
    }
    
    public void testCompileAgainstRemovedModule68716() throws Exception {
        SuiteProject suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        suite.open();
        ApisupportAntUtils.addDependency(proj, WINDOWS, null, null, true, null);
        
        // remove WINDOWS from platform
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.DISABLED_MODULES_PROPERTY, WINDOWS);
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(proj);
        ProjectManager.getDefault().saveProject(suite);
        
        // build project
        FileObject buildScript = proj.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"clean","netbeans"}, null);
        et.waitFinished();
        assertFalse("project was successfully compiled against removed module from platform",proj.getModuleJarLocation().exists());
    }
    
    private static FileObject findBuildXml(final NbModuleProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
}


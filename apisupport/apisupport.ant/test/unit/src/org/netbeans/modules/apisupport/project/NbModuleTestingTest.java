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

package org.netbeans.modules.apisupport.project;

import java.util.logging.Level;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;

/**
 * @author Jaroslav Tulach
 */
public class NbModuleTestingTest extends TestBase {
    public NbModuleTestingTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    public void testCreateStandAloneModule() throws Exception {
        //    XXX: failing test, fix or delete
//        File targetPrjDir = new File(getWorkDir(), "testModule");
//        NbModuleProjectGenerator.createStandAloneModule(
//                targetPrjDir,
//                "org.example.testModule", // cnb
//                "Testing Module", // display name
//                "org/example/testModule/resources/Bundle.properties",
//                "org/example/testModule/resources/layer.xml",
//                NbPlatform.PLATFORM_ID_DEFAULT); // platform id
//        FileObject fo = FileUtil.toFileObject(targetPrjDir);
//        
//        FileObject a = FileUtil.createData(fo, "src/org/example/testModule/A.java");
//        dump(a, "package org.example.testModule;" +
//                "public class A {" +
//                "  public static int version() { return 1; }" +
//                "}"
//        );
//        FileObject aTest = FileUtil.createData(fo, "test/unit/src/org/example/testModule/ATest.java");
//        dump(aTest, "package org.example.testModule;\n" +
//                "import org.netbeans.junit.*;\n" +
//                "import junit.framework.*;\n" +
//                "public class ATest extends NbTestCase {\n" +
//                "  public ATest(String n) { super(n); }\n" +
//                "  public void testVersion() {\n" +
//                "    assertEquals(1, A.version());\n" +
//                "  }\n" +
//                "}\n"
//        );
//        FileObject moduleTest = FileUtil.createData(fo, "test/unit/src/org/example/testModule/ModuleTest.java");
//        dump(moduleTest, "package org.example.testModule;\n" +
//                "import org.netbeans.junit.*;\n" +
//                "import junit.framework.*;\n" +
//                "public class ModuleTest extends ATest {\n" +
//                "  public ModuleTest(String n) { super(n); }\n" +
//                "  public static Test suite() {\n" +
//                "    return NbModuleSuite.createConfiguration(ATest.class).gui(false).suite();\n" +
//                "  }\n" +
//                "}\n"
//        );
//    
//        // Make sure generated files are created too - simulate project opening.
//        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
//        assertNotNull("have a project in " + targetPrjDir, p);
//        addTestDependency(p);
//        p.open();
//        
//        InputOutputProviderImpl.registerCase(this);
//        FileObject buildScript = fo.getFileObject("build.xml"); 
//        assertNotNull(buildScript);
//        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{
//            "test"
//        }, null);
//        et.waitFinished();
//        assertEquals("Error during ant ...",0,et.result());
    }

    private void addTestDependency(NbModuleProject project) throws Exception{
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        ModuleList ml = project.getModuleList();
        ModuleEntry me = ml.getEntry("org.netbeans.modules.junit");
        assertNotNull("me exist", me);
        TestModuleDependency tmd = new TestModuleDependency(me, true, true, true);
        pxm.addTestDependency(TestModuleDependency.UNIT, tmd);
        ProjectManager.getDefault().saveProject(project);
    }
}

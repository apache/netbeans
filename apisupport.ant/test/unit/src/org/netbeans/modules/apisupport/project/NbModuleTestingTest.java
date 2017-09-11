/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

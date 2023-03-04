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

package org.netbeans.modules.apisupport.project.universe;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Tomas Musil
 */
public class TestModuleDependencyTest extends TestBase {
    
    private static final String ANT_PROJECT_SUPPORT = "org.netbeans.modules.project.ant";
    private static final String DIALOGS = "org.openide.dialogs";
    private  TestModuleDependency tdJP_001;
    private TestModuleDependency tdJP_101;
    private TestModuleDependency tdJP_101otherInstance;
    private TestModuleDependency tdAnt_111;
    
    public TestModuleDependencyTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        final NbModuleProject testingProject = generateTestingProject();
        final String cnb = "org.netbeans.modules.java.project";
        final String cnb2 = "org.netbeans.modules.project.ant";
        ModuleEntry meJP = testingProject.getModuleList().getEntry(cnb);
        ModuleEntry meAnt = testingProject.getModuleList().getEntry(cnb2);
        tdJP_001 = new TestModuleDependency(meJP, false, false, true);
        tdJP_101 = new TestModuleDependency(meJP, true, false, true);
        tdJP_101otherInstance = new TestModuleDependency(meJP, true, false, true);
        tdAnt_111 = new TestModuleDependency(meAnt, true, true, true);
    }
    
    
    public void testEquals() throws Exception{
        assertFalse("001!=101" , tdJP_001.equals(tdJP_101));
        assertTrue("these are equal", tdJP_101.equals(tdJP_101otherInstance));
        assertFalse(tdAnt_111.equals(null));
        assertFalse(tdAnt_111.equals(""));
    }
    
    public void testCompareTo() {
        assertEquals("equals", 0, tdJP_101.compareTo(tdJP_101otherInstance));
        assertTrue("o.n.m.java.project < o.n.m.project.ant", tdJP_001.compareTo(tdAnt_111) < 0);
        assertTrue("o.n.m.project.ant > o.n.m.java.project", tdAnt_111.compareTo(tdJP_101) > 0);
    }
    
    public void testHashCode() {
        assertEquals("the same hashcodes", tdJP_101.hashCode(),tdJP_101otherInstance.hashCode());
        assertTrue("the same hashcodes", tdJP_101.hashCode() == tdJP_001.hashCode());
        assertTrue("different hashcodes", tdJP_101.hashCode() != tdAnt_111.hashCode());
    }
    
    private NbModuleProject generateTestingProject() throws Exception {
        FileObject fo = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "testing");
        FileObject projectXMLFO = fo.getFileObject("nbproject/project.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
                "<code-name-base>org.example.testing</code-name-base>\n" +
                "<standalone/>\n" +
                "<module-dependencies>\n" +
                "<dependency>\n" +
                "<code-name-base>" + DIALOGS + "</code-name-base>\n" +
                "<build-prerequisite/>\n" +
                "<compile-dependency/>\n" +
                "<run-dependency>\n" +
                "<specification-version>6.2</specification-version>\n" +
                "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                "<code-name-base>" + ANT_PROJECT_SUPPORT + "</code-name-base>\n" +
                "<build-prerequisite/>\n" +
                "<compile-dependency/>\n" +
                "<run-dependency>\n" +
                "<release-version>1</release-version>\n" +
                "<specification-version>1.10</specification-version>\n" +
                "</run-dependency>\n" +
                "</dependency>\n" +
                "</module-dependencies>\n" +
                "<test-dependencies/>\n" +
                "<friend-packages>\n" +
                "<friend>org.module.examplemodule</friend>\n" +
                "<package>org.netbeans.examples.modules.misc</package>\n" +
                "</friend-packages>\n" +
                "<class-path-extension>\n" +
                "<runtime-relative-path>ext/jsr88javax.jar</runtime-relative-path>\n" +
                "<binary-origin>../external/deployment-api-1.2-rev-1.jar</binary-origin>\n" +
                "</class-path-extension>\n" +
                "</data>\n" +
                "</configuration>\n" +
                "</project>\n";
        TestBase.dump(projectXMLFO, xml);
        return (NbModuleProject) ProjectManager.getDefault().findProject(fo);
    }
    
    
}

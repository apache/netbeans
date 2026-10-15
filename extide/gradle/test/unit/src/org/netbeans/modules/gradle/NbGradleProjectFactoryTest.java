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
package org.netbeans.modules.gradle;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

public class NbGradleProjectFactoryTest extends AbstractGradleProjectTestCase {
    private FileObject root;

    public NbGradleProjectFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        root = fs.getRoot();
    }

    public void testNull() throws Exception {
        assertFalse(NbGradleProjectFactory.isProjectCheck(null, false));
        assertFalse(NbGradleProjectFactory.isProjectCheck(null, true));
    }

    public void testNonProject() throws Exception {
        FileObject prj = root;
        assertFalse(NbGradleProjectFactory.isProjectCheck(prj, false));
    }

    public void testSubProject() throws Exception {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "include 'projectB'\n");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\n", null);
        assertTrue(NbGradleProjectFactory.isProjectCheck(a, false));
        assertTrue(NbGradleProjectFactory.isProjectCheck(b, false));
    }
    
    public void testNonProjectSubDir() throws Exception {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "include 'projectB'\n");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\n", null);
        FileObject as = a.createFolder("docs");
        FileObject bs = b.createFolder("src");

        assertFalse(NbGradleProjectFactory.isProjectCheck(as, false));
        assertFalse(NbGradleProjectFactory.isProjectCheck(bs, false));
    }

    public void testBuildSrcProject() throws Exception {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        FileObject b = createGradleProject("projectA-" + rnd + "/buildSrc",
                null, null);
        assertTrue(NbGradleProjectFactory.isProjectCheck(a, false));
        assertTrue(NbGradleProjectFactory.isProjectCheck(b, false));
    }

    public void testPomAndGradle() throws Exception {
        FileObject prj = root;
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertTrue("Gradle wins", NbGradleProjectFactory.isProjectCheck(prj, false));
        assertFalse("Pom wins", NbGradleProjectFactory.isProjectCheck(prj, true));
    }

    public void testPomNestedAndGradleNot() throws Exception {
        FileObject parentPrj = root;
        FileObject parentPom = FileUtil.createData(parentPrj, "pom.xml");
        FileObject prj = FileUtil.createFolder(parentPrj, "child");
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertFalse("Pom wins on settings", NbGradleProjectFactory.isProjectCheck(prj, true));
        assertFalse("Pom wins on parent pom", NbGradleProjectFactory.isProjectCheck(prj, false));
    }

    public void testPomAndGradleBothNested() throws Exception {
        FileObject parentPrj = root;
        FileObject parentPom = FileUtil.createData(parentPrj, "pom.xml");
        FileObject parentGradle = FileUtil.createData(parentPrj, "build.gradle");
        FileObject prj = FileUtil.createFolder(parentPrj, "child");
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertFalse("Parent Pom wins on settings", NbGradleProjectFactory.isProjectCheck(parentPrj, true));
        assertTrue("Parent Gradle wins", NbGradleProjectFactory.isProjectCheck(parentPrj, false));

        assertFalse("Pom wins on settings", NbGradleProjectFactory.isProjectCheck(prj, true));
        assertTrue("Gradle wins on parent build.gradle", NbGradleProjectFactory.isProjectCheck(prj, false));
    }

    public void testGradle70JavaInit() throws Exception {
        FileObject parentPrj = root;
        FileObject settings = FileUtil.createData(parentPrj, "settings.gradle");
        try (OutputStream os = settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'example'\n"
                    + "include('app')\n"
            ).getBytes(StandardCharsets.UTF_8));
        }
        FileObject app = FileUtil.createFolder(parentPrj, "app");
        FileObject gradle = FileUtil.createData(app, "build.gradle");
        assertProjectsRecognized(parentPrj, app);
    }
    
    private void assertProjectsRecognized(FileObject parentPrj, FileObject app) {
        assertTrue("Parent Gradle recognized", NbGradleProjectFactory.isProjectCheck(parentPrj, false));
        assertTrue("Child Gradle recognized", NbGradleProjectFactory.isProjectCheck(app, false));
        NbGradleProjectFactory factoryInstance = new NbGradleProjectFactory();
        assertEquals("Gradle project type of main project", NbGradleProject.GRADLE_PROJECT_TYPE, factoryInstance.isProject2(parentPrj).getProjectType());
        assertEquals("Gradle project type of subproject", NbGradleProject.GRADLE_PROJECT_TYPE, factoryInstance.isProject2(app).getProjectType());
    }
    
    /**
     * Checks that project with just settings.gradle and no build.gradle is recognized as a project.
     */
    public void testNoBuildFileProject() throws Exception {
        FileObject parentPrj = root;
        FileObject settings = FileUtil.createData(parentPrj, "settings.gradle");
        try (OutputStream os = settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'example'\n"
                    + "include('app')\n"
            ).getBytes(StandardCharsets.UTF_8));
        }
        FileObject app = FileUtil.createFolder(parentPrj, "app");
        assertProjectsRecognized(parentPrj, app);
    }

}

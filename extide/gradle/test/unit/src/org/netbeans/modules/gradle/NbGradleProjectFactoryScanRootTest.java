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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

public class NbGradleProjectFactoryScanRootTest extends NbGradleProjectFactoryTest {

    private FileObject root;
    private FileObject forbiddenTestsRoot;

    public NbGradleProjectFactoryScanRootTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        root = fs.getRoot();
        File parentDir = getWorkDir().getParentFile();
        File forbiddenTestsDir = new File(parentDir, "forbiddenTests");
        File fd1 = new File(forbiddenTestsDir, "forbidden1");
        File fd2 = new File(new File(forbiddenTestsDir, "f2"), "forbidden2");
        System.setProperty("project.limitScanRoot", parentDir.getAbsolutePath());
        System.setProperty("project.forbiddenFolders", fd1.getAbsolutePath() + ";" + fd2.getAbsolutePath());
        fd1.mkdirs();
        fd2.mkdirs();
        fs = new LocalFileSystem();
        fs.setRootDirectory(forbiddenTestsDir);
        forbiddenTestsRoot = fs.getRoot();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Enumeration<? extends FileObject> files = forbiddenTestsRoot.getData(false);
        while (files.hasMoreElements()) {
            try {
                files.nextElement().delete();
            } catch (IOException ignore) {
            }
        }
        Enumeration<? extends FileObject> folders = forbiddenTestsRoot.getFolders(false);
        while (folders.hasMoreElements()) {
            try {
                folders.nextElement().delete();
            } catch (IOException ignore) {
            }
        }
    }

    public void testPomAndGradleBothNotNested() throws Exception {
        FileObject parentPrj = root;
        FileObject prj = FileUtil.createFolder(parentPrj, "child");
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertFalse("Pom wins", NbGradleProjectFactory.isProjectCheck(prj, true));
        assertTrue("Gradle wins", NbGradleProjectFactory.isProjectCheck(prj, false));
    }

    public void testPomNestedAboveRootAndGradleNotNested() throws Exception {
        File rootDir = FileUtil.toFile(root);
        FileObject rootParentPrj = FileUtil.toFileObject(rootDir.getParentFile().getParentFile());
        FileObject parentPom = FileUtil.createData(rootParentPrj, "pom.xml");
        FileObject prj = FileUtil.toFileObject(rootDir.getParentFile());
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");
        try {
            assertFalse("Pom wins", NbGradleProjectFactory.isProjectCheck(prj, true));
            assertTrue("Gradle wins", NbGradleProjectFactory.isProjectCheck(prj, false));
            prj = FileUtil.toFileObject(rootDir);
            gradle.delete();
            gradle = null;
            FileUtil.createData(prj, "pom.xml");
            FileUtil.createData(prj, "build.gradle");
            assertFalse("Pom wins sub", NbGradleProjectFactory.isProjectCheck(prj, true));
            assertFalse("Pom wins sub nested", NbGradleProjectFactory.isProjectCheck(prj, false));
        } finally {
            try {
                parentPom.delete();
            } finally {
                try {
                    pom.delete();
                } finally {
                    if (gradle != null) gradle.delete();
                }
            }
        }
    }

    public void testGetSiblingScanRoot() throws IOException {
        File rootDir = FileUtil.toFile(root);
        File scanRootDir = rootDir.getParentFile();
        FileObject aboveScanRoot = FileUtil.toFileObject(scanRootDir.getParentFile());
        FileObject project = aboveScanRoot.createFolder(scanRootDir.getName() + "2");
        try {
            project.createData("build.gradle.kts");
            assertFalse("No gradle project scanned", NbGradleProjectFactory.isProjectCheck(project, false));
            assertFalse("No gradle project scanned with no pom", NbGradleProjectFactory.isProjectCheck(project, true));
            project.createData("pom.xml");
            assertFalse("No gradle project scanned with pom", NbGradleProjectFactory.isProjectCheck(project, false));
            assertFalse("No pom scanned", NbGradleProjectFactory.isProjectCheck(project, true));
        } finally {
            project.delete();
        }
    }

    /**
     * Checks that project scanning does not go above root
     */
    public void testAboveRootProject() throws Exception {
        FileObject parentPrj = root;
        parentPrj.createData("build.gradle");
        File rootDir = FileUtil.toFile(root);
        String dirName = rootDir.getParentFile().getName() + '/' + rootDir.getName();
        File rootParentDir = rootDir.getParentFile().getParentFile();
        FileObject rootParentPrj = FileUtil.toFileObject(rootParentDir);
        FileObject settings = FileUtil.createData(rootParentPrj, "settings.gradle");
        File likeRootDir = new File(rootParentDir, rootDir.getParentFile().getName() + "2");
        likeRootDir.mkdirs();
        File likeRootDirBuild = new File(likeRootDir, "build.gradle");
        likeRootDirBuild.createNewFile();
        try {
            try (OutputStream os = settings.getOutputStream()) {
                os.write(("\n"
                        + "rootProject.name = 'example'\n"
                        + "include('" + dirName + "/app')\n").getBytes(StandardCharsets.UTF_8));
            }
            FileObject app = FileUtil.createFolder(parentPrj, "app");
            assertFalse("app not project", NbGradleProjectFactory.isProjectCheck(app, false));
            assertFalse("above root is not project", NbGradleProjectFactory.isProjectCheck(rootParentPrj, false));
            assertFalse("likeRoot is also not project", NbGradleProjectFactory.isProjectCheck(FileUtil.toFileObject(likeRootDir), false));
            assertTrue("root is project", NbGradleProjectFactory.isProjectCheck(parentPrj, false));
        } finally {
            try {
                settings.delete();
            } finally {
                likeRootDirBuild.delete();
                likeRootDir.delete();
            }
        }
    }

    public void testForbiddenPomAndGradle() throws Exception {
        FileObject parentPrj = forbiddenTestsRoot;
        FileObject settings = FileUtil.createData(parentPrj, "settings.gradle");
        try (OutputStream os = settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'example'\n"
                    + "include('forbidden1')\n"
                    + "include('forbidden2')\n").getBytes(StandardCharsets.UTF_8));
        }
        FileObject fo1 = FileUtil.createFolder(parentPrj, "forbidden1");
        FileObject fo2 = FileUtil.createFolder(parentPrj, "forbidden2");
        FileObject fo3 = FileUtil.createFolder(parentPrj, "forbidden3");
        FileUtil.createData(fo1, "pom.xml");
        FileUtil.createData(fo2, "pom.xml");
        FileUtil.createData(fo3, "pom.xml");
        assertTrue("root is project without pom", NbGradleProjectFactory.isProjectCheck(parentPrj, true));
        assertTrue("root is project", NbGradleProjectFactory.isProjectCheck(parentPrj, false));

        assertTrue("forbidden1 is project with pom", NbGradleProjectFactory.isProjectCheck(fo1, true));
        assertFalse("forbidden2 is not project with pom", NbGradleProjectFactory.isProjectCheck(fo2, true));
        assertFalse("forbidden3 is not project with pom", NbGradleProjectFactory.isProjectCheck(fo3, true));
        assertTrue("forbidden1 is project", NbGradleProjectFactory.isProjectCheck(fo1, false));
        assertTrue("forbidden2 is project", NbGradleProjectFactory.isProjectCheck(fo2, false));
        assertFalse("forbidden3 is not project", NbGradleProjectFactory.isProjectCheck(fo3, false));
    }

    public void testForbiddenNestedPomAndGradle() throws Exception {
        FileObject parentPrj = forbiddenTestsRoot.getFileObject("f2");
        FileObject settings = FileUtil.createData(parentPrj, "settings.gradle");
        try (OutputStream os = settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'example'\n"
                    + "include('forbidden1')\n"
                    + "include('forbidden2')\n").getBytes(StandardCharsets.UTF_8));
        }
        FileObject fo1 = FileUtil.createFolder(parentPrj, "forbidden1");
        FileObject fo2 = FileUtil.createFolder(parentPrj, "forbidden2");
        FileUtil.createData(fo1, "pom.xml");
        FileUtil.createData(fo2, "pom.xml");
        FileUtil.createData(parentPrj, "pom.xml");
        FileUtil.createData(forbiddenTestsRoot, "pom.xml");
        assertFalse("root is not project with pom", NbGradleProjectFactory.isProjectCheck(parentPrj, true));
        assertFalse("root is not project with parent pom", NbGradleProjectFactory.isProjectCheck(parentPrj, false));

        assertFalse("forbidden1 is not project with pom", NbGradleProjectFactory.isProjectCheck(fo1, true));
        assertTrue("forbidden2 is project even with parent pom and prefer maven", NbGradleProjectFactory.isProjectCheck(fo2, true));
        assertFalse("forbidden1 is not project with parent pom", NbGradleProjectFactory.isProjectCheck(fo1, false));
        assertTrue("forbidden2 is project even with parent pom without prefer maven", NbGradleProjectFactory.isProjectCheck(fo2, false));
    }

    public void testForbiddenSubProject() throws Exception {
        FileObject parentPrj = forbiddenTestsRoot;
        FileObject settings = FileUtil.createData(parentPrj, "settings.gradle");
        try (OutputStream os = settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'example'\n"
                    + "include('forbidden1')\n"
                    + "include('forbidden2')\n").getBytes(StandardCharsets.UTF_8));
        }
        FileObject fo1 = FileUtil.createFolder(parentPrj, "forbidden1");
        FileObject fo2 = FileUtil.createFolder(parentPrj, "forbidden2");
        FileObject fo3 = FileUtil.createFolder(parentPrj, "forbidden3");
        FileObject f2fo2 = FileUtil.createFolder(parentPrj, "f2/forbidden2");
        FileObject f2fo2app = FileUtil.createFolder(parentPrj, "f2/forbidden2/app");
        FileObject f2f2A = FileUtil.createFolder(parentPrj, "f2/forbidden2App");
        FileUtil.createData(f2fo2, "build.gradle");
        FileObject f2fo2Settings = FileUtil.createData(f2fo2, "settings.gradle");
        try (OutputStream os = f2fo2Settings.getOutputStream()) {
            os.write(("\n"
                    + "rootProject.name = 'eg'\n"
                    + "include('app')\n").getBytes(StandardCharsets.UTF_8));
        }

        assertTrue("root is project", NbGradleProjectFactory.isProjectCheck(parentPrj, false));
        assertTrue("forbidden1 is project", NbGradleProjectFactory.isProjectCheck(fo1, false));
        assertTrue("forbidden2 is project", NbGradleProjectFactory.isProjectCheck(fo2, false));
        assertFalse("forbidden3 is not project", NbGradleProjectFactory.isProjectCheck(fo3, false));
        assertFalse("f2/forbidden2App is not project", NbGradleProjectFactory.isProjectCheck(f2f2A, false));
        FileUtil.createData(f2f2A, "build.gradle");
        assertTrue("f2/forbidden2App is project with build", NbGradleProjectFactory.isProjectCheck(f2f2A, false));
        assertFalse("f2/forbidden2 is not project", NbGradleProjectFactory.isProjectCheck(f2fo2, false));
        assertFalse("f2/forbidden2/app is not project", NbGradleProjectFactory.isProjectCheck(f2fo2app, false));
        FileUtil.createData(f2fo2app, "build.gradle");
        assertTrue("f2/forbidden2/app is project with build", NbGradleProjectFactory.isProjectCheck(f2fo2app, false));
    }
}

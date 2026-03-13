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
package org.netbeans.modules.gradle.spi;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import org.openide.util.Utilities;

import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class GradleFilesScanRootTest {

    @Rule
    public final TemporaryFolder root = new TemporaryFolder();

    private File scanRoot;
    private File forbiddenTestsDir;

    @Before
    public void setup() {
        scanRoot = new File(root.getRoot(), "scanRoot");
        scanRoot.mkdirs();
        forbiddenTestsDir = new File(scanRoot, "forbiddenTests");
        File fd1 = new File(forbiddenTestsDir, "forbidden1");
        File fd2 = new File(new File(forbiddenTestsDir, "f2"), "forbidden2");
        System.setProperty("project.limitScanRoot", scanRoot.getAbsolutePath());
        System.setProperty("project.forbiddenFolders", fd1.getAbsolutePath() + ";" + fd2.getAbsolutePath());
        fd1.mkdirs();
        fd2.mkdirs();
    }

    private static File normalizeTempDir(File root) {
        if (root != null && Utilities.isMac()) {
            String absolutePath = root.getAbsolutePath();
            if (absolutePath.startsWith("/private/")) {
                return new File(absolutePath.substring(8));
            }
        }
        return root;
    }

    @Test
    public void testAllScanRootTests() throws IOException {
        // Due to the use of System.properties and the static initialization of GradleFiles,
        // only a single @Test is used and individual tests are invoked from here.
        SingleTestRunner runner = new SingleTestRunner(root.getRoot());
        runner.runOneTest(() -> testGetProjectAboveRoot());
        runner.runOneTest(() -> testGetSiblingScanRoot());
        runner.runOneTest(() -> testGetForbiddenSubProject());
        if (runner.getException() != null) {
            throw runner.getException();
        }
    }

    private interface SingleUnit {
        void run() throws IOException;
    }

    private static class SingleTestRunner {
        private IOException exception;
        private final File root;

        public SingleTestRunner(File root) {
            this.root = root;
        }
        
        public void runOneTest(SingleUnit test) {
            try {
                test.run();
            } catch (IOException e) {
                e.printStackTrace(System.err);
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            } finally {
                cleanup();
            }
        }

        public IOException getException() {
            return exception;
        }

        private void cleanup() {
            try {
                Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        return file.toFile().delete() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }

        }
    }

    private void testGetProjectAboveRoot() throws IOException {
        File dirAboveScanRoot = root.getRoot();
        File projectRoot = new File(scanRoot, "project");
        File settings = new File(dirAboveScanRoot, "settings.gradle");
        File build = new File(dirAboveScanRoot, "build.gradle.kts");

        projectRoot.mkdirs();
        String subPath = projectRoot.getAbsolutePath().substring(dirAboveScanRoot.getAbsolutePath().length() + 1);
        build.createNewFile();
        settings.createNewFile();
        Files.write(settings.toPath(), List.of(
                "projectRootProject.name = 'example'",
                "include('" + subPath + "/app')"
        ));
        File app = new File(projectRoot, "app");
        app.mkdirs();
        // Check that the project is not resolved
        GradleFiles gf = new GradleFiles(app);
        assertNull(gf.getBuildScript());
        assertNull(gf.getSettingsScript());
        assertNull(gf.getParentScript());
        assertFalse(gf.isProject());
        assertFalse(gf.isRootProject());

        // Also check the projectRoot project
        File projectRootBuild = new File(projectRoot, projectRoot.getName() + ".gradle.kts");
        projectRootBuild.createNewFile();
        GradleFiles projectRootGf = new GradleFiles(projectRoot);
        assertEquals(normalizeTempDir(projectRootBuild), normalizeTempDir(projectRootGf.getBuildScript()));
        assertNull(projectRootGf.getSettingsScript());
        assertNull(projectRootGf.getParentScript());
        assertTrue(projectRootGf.isProject());
        assertTrue(projectRootGf.isRootProject());

        // Now ensure that the above project structure does work when below the scanRoot.
        File newRoot = new File(projectRoot, subPath);
        newRoot.mkdirs();
        File newSettings = Files.move(settings.toPath(), projectRoot.toPath().resolve(settings.getName())).toFile();
        File newBuild = Files.move(build.toPath(), projectRoot.toPath().resolve(build.getName())).toFile();
        File newApp = Files.move(app.toPath(), newRoot.toPath().resolve(app.getName())).toFile();
        GradleFiles newGf = new GradleFiles(newApp);
        assertNull(newGf.getBuildScript());
        assertEquals(normalizeTempDir(newSettings), normalizeTempDir(newGf.getSettingsScript()));
        assertEquals(normalizeTempDir(newBuild), normalizeTempDir(newGf.getParentScript()));
        assertTrue(newGf.isProject());
        assertFalse(gf.isRootProject());
    }

    private void testGetSiblingScanRoot() throws IOException {
        File dirAboveScanRoot = root.getRoot();
        File project = new File(dirAboveScanRoot, scanRoot.getName() + "2");
        File settings = new File(project, "settings.gradle");
        File build = new File(project, "build.gradle.kts");
        project.mkdirs();
        build.createNewFile();
        settings.createNewFile();
        GradleFiles gf = new GradleFiles(project);
        assertNull(gf.getBuildScript());
        assertNull(gf.getSettingsScript());
        assertNull(gf.getParentScript());
        assertFalse(gf.isProject());
        assertFalse(gf.isRootProject());
    }

    private void testGetForbiddenSubProject() throws IOException {
        File parentPrj = forbiddenTestsDir;
        File settings = new File(parentPrj, "settings.gradle");
        settings.createNewFile();
        Files.write(settings.toPath(), List.of(
                "rootProject.name = 'example'",
                "include('forbidden1')",
                "include('forbidden2')"
        ));
        File fo1 = new File(parentPrj, "forbidden1");
        fo1.mkdirs();
        File fo2 = new File(parentPrj, "forbidden2");
        fo2.mkdirs();
        File f2f2A = new File(new File(parentPrj, "f2"), "forbidden2App");
        f2f2A.mkdirs();
        File f2f2ABuild = new File(f2f2A, "build.gradle");
        f2f2ABuild.createNewFile();
        File f2fo2a = new File(new File(new File(parentPrj, "f2"), "forbidden2"), "app");
        f2fo2a.mkdirs();
        File f2fo2a2 = new File(new File(new File(parentPrj, "f2"), "forbidden2"), "app2");
        f2fo2a2.mkdirs();
        File f2fo2a2Build = new File(f2fo2a2, "build.gradle");
        f2fo2a2Build.createNewFile();

        File f2fo2Settings = new File(f2fo2a.getParentFile(), "settings.gradle");
        f2fo2Settings.createNewFile();
        Files.write(f2fo2Settings.toPath(), List.of(
                "rootProject.name = 'eg'",
                "include('app')",
                "include('app2')"
        ));
        File f2fo2Build = new File(f2fo2a.getParentFile(), "build.gradle.kts");
        f2fo2Build.createNewFile();


        GradleFiles gf;
        gf = new GradleFiles(parentPrj);
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertTrue("root is project", gf.isProject());
        assertTrue("root is rootProject", gf.isRootProject());

        gf = new GradleFiles(fo1);
        assertNull("buildScript null for forbidden1", gf.getBuildScript());
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertTrue("forbidden1 is project", gf.isProject());
        assertFalse("forbidden1 is not rootProject", gf.isRootProject());
        assertTrue("forbidden1 is scriptless subproject", gf.isScriptlessSubProject());

        gf = new GradleFiles(fo2);
        assertNull("buildScript null for forbidden2", gf.getBuildScript());
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertTrue("forbidden2 is project", gf.isProject());
        assertFalse("forbidden2 is not rootProject", gf.isRootProject());
        assertTrue("forbidden2 is scriptless subproject", gf.isScriptlessSubProject());

        gf = new GradleFiles(f2f2A);
        assertEquals(normalizeTempDir(f2f2ABuild), normalizeTempDir(gf.getBuildScript()));
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertTrue("f2/forbidden2App is project", gf.isProject());
        assertFalse("f2/forbidden2App is not rootProject", gf.isRootProject());

        gf = new GradleFiles(f2fo2a);
        assertNull("buildScript null for f2/forbidden2/app", gf.getBuildScript());
        assertEquals("super-parent settingsScript for f2/forbidden2/app", normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertNull("parentScript null for f2/forbidden2/app", gf.getParentScript());
        assertFalse("f2/forbidden2/app is not project", gf.isProject());
        assertFalse("f2/forbidden2/app is not scriptless subproject", gf.isScriptlessSubProject());

        gf = new GradleFiles(f2fo2a2);
        assertEquals(normalizeTempDir(f2fo2a2Build), normalizeTempDir(gf.getBuildScript()));
        assertEquals("super-parent settingsScript for f2/forbidden2/app2", normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertNull("parentScript null for f2/forbidden2/app2", gf.getParentScript());
        assertTrue("f2/forbidden2/app2 is project", gf.isProject());
        assertFalse("f2/forbidden2/app2 is project", gf.isRootProject());
        assertFalse("f2/forbidden2/app2 is not scriptless subproject", gf.isScriptlessSubProject());

        new File(fo1, "build.gradle").createNewFile();
        gf = new GradleFiles(fo1);
        assertNull("buildScript null for forbidden1 with build also", gf.getBuildScript());
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertTrue("forbidden1 is project", gf.isProject());
        assertFalse("forbidden1 is not rootProject", gf.isRootProject());
        assertTrue("forbidden1 is scriptless subproject", gf.isScriptlessSubProject());
    }
}

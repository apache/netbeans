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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openide.util.Utilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleFilesTest {

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    private static File normalizeTempDir(File root) {
        if (root != null && Utilities.isMac()) {
            String absolutePath = root.getAbsolutePath();
            if (absolutePath.startsWith("/private/")) {
                return new File(absolutePath.substring(8));
            }
        }
        return root;
    }

    /**
     * Test of getBuildScript method, of class GradleFiles.
     */
    @Test
    public void testGetBuildScript() throws IOException {
        File build = root.newFile("build.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(normalizeTempDir(build), normalizeTempDir(gf.getBuildScript()));
    }

    @Test
    public void testGetBuildScript2() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(null, gf.getBuildScript());
    }

    @Test
    public void testGetBuildScript3() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File subBuild = root.newFile("module/module.gradle");
        GradleFiles gf = new GradleFiles(module);
        assertEquals("module.gradle", gf.getBuildScript().getName());
    }

    @Test
    public void testGetBuildScript4() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File subBuild = root.newFile("module/module.gradle");
        File subBuild2 = root.newFile("module/build.gradle");
        GradleFiles gf = new GradleFiles(module);
        assertEquals("build.gradle", gf.getBuildScript().getName());
    }

    @Test
    public void testGetBuildScript5() throws IOException {
        File settings = root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(settings.getParentFile());
        assertEquals("It is project", true, gf.isProject());
        assertEquals("It has settings", normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
        assertNull("No build script", gf.getBuildScript());
    }

    @Test
    public void testGetBuildScriptKotlin() throws IOException {
        File build = root.newFile("build.gradle.kts");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(normalizeTempDir(build), normalizeTempDir(gf.getBuildScript()));
    }

    @Test
    public void testGetBuildScriptKotlin2() throws IOException {
        File build = root.newFile("build.gradle.kts");
        File settings = root.newFile("settings.gradle.kts");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(null, gf.getBuildScript());
    }

    @Test
    public void testGetBuildScriptKotlin3() throws IOException {
        File build = root.newFile("build.gradle.kts");
        File settings = root.newFile("settings.gradle.kts");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include(\":module\")"));
        File subBuild = root.newFile("module/module.gradle.kts");
        GradleFiles gf = new GradleFiles(module);
        assertEquals("module.gradle.kts", gf.getBuildScript().getName());
    }

    @Test
    public void testGetBuildScriptKotlin4() throws IOException {
        File build = root.newFile("build.gradle.kts");
        File settings = root.newFile("settings.gradle.kts");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include(\":module\")"));
        File subBuild = root.newFile("module/module.gradle.kts");
        File subBuild2 = root.newFile("module/build.gradle.kts");
        GradleFiles gf = new GradleFiles(module);
        assertEquals("build.gradle.kts", gf.getBuildScript().getName());
    }

    @Test
    public void testGetParentScript() throws IOException{
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(null, gf.getParentScript());
    }

    @Test
    public void testGetParentScriptKotlin() throws IOException{
        File build = root.newFile("build.gradle.kts");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(null, gf.getParentScript());
    }

    @Test
    public void testGetParentScript2() throws IOException{
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(normalizeTempDir(build), normalizeTempDir(gf.getParentScript()));
    }

    @Test
    public void testGetParentScriptKotlin2() throws IOException{
        File build = root.newFile("build.gradle.kts");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(normalizeTempDir(build), normalizeTempDir(gf.getParentScript()));
    }

    @Test
    public void testGetSettingsScript() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getSettingsScript()));
    }

    /**
     * Test of getProjectDir method, of class GradleFiles.
     */
    @Test
    public void testGetProjectDir() throws  IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertEquals(normalizeTempDir(module), normalizeTempDir(gf.getProjectDir()));
    }

    @Test
    public void testGetProjectDir2() throws  IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module2'"));
        GradleFiles gf = new GradleFiles(module);
        assertNull(gf.getProjectDir());
    }

    /**
     * Test of getRootDir method, of class GradleFiles.
     */
    @Test
    public void testNonExistingGetRootDir() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(new File(root.getRoot(), "module"));
        assertEquals(normalizeTempDir(root.getRoot()), normalizeTempDir(gf.getRootDir()));
    }

    @Test
    public void testNonExistingGetRootDir2() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(new File(root.getRoot(), "module"));
        assertEquals(normalizeTempDir(root.getRoot()).getAbsolutePath(), normalizeTempDir(gf.getRootDir()).getAbsolutePath());
    }
    /**
     * Test of getGradlew method, of class GradleFiles.
     */
    @Test
    public void testGetGradlew() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        File gradlew = root.newFile("gradlew");
        File gradlewBat = root.newFile("gradlew.bat");
        File wrapperProps = new File(root.newFolder("gradle", "wrapper"), "gradle-wrapper.properties");
        wrapperProps.createNewFile();
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(normalizeTempDir(Utilities.isWindows() ? gradlewBat : gradlew), normalizeTempDir(gf.getGradlew()));
    }

    /**
     * Test of getWrapperProperties method, of class GradleFiles.
     */
    @Test
    public void testGetWrapperProperties() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        root.newFile("gradlew");
        root.newFile("gradlew.bat");
        File wrapperProps = new File(root.newFolder("gradle", "wrapper"), "gradle-wrapper.properties");
        wrapperProps.createNewFile();
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(normalizeTempDir(wrapperProps), normalizeTempDir(gf.getWrapperProperties()));
    }

    @Test
    public void testHasWrapper() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        root.newFile("gradlew");
        root.newFile("gradlew.bat");
        File wrapperProps = new File(root.newFolder("gradle", "wrapper"), "gradle-wrapper.properties");
        wrapperProps.createNewFile();
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertTrue(gf.hasWrapper());
    }

    @Test
    public void testHasWrapper2() throws IOException {
        root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File module = root.newFolder("module");
        root.newFile("gradlew");
        root.newFile("gradlew.bat");
        File wrapperProps = new File(root.newFolder("gradle", "wrapper"), "gradle-wrapper.properties");
        wrapperProps.createNewFile();
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.hasWrapper());
    }

    /**
     * Test of isRootProject method, of class GradleFiles.
     */
    @Test
    public void testIsRootProject() {
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertFalse(gf.isRootProject());
    }

    @Test
    public void testIsSubProject() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isSubProject());
    }

    @Test
    public void testIsSubProject2() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include 'module'"));
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isSubProject());
    }

    @Test
    public void testIsSubProject3() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module2'"));
        GradleFiles gf = new GradleFiles(module);
        assertFalse(gf.isSubProject());
    }

    @Test
    public void testIsSubProject4() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("subprojects", "module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File subBuild = root.newFile("subprojects/module/build.gradle");
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isSubProject());
    }

    @Test
    public void testIsSubProject5() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("modules", "module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File subBuild = root.newFile("modules/module/module.gradle");
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isSubProject());
    }

    @Test
    public void testIsSubProject6() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("subprojects", "language-java");
        Files.write(settings.toPath(), Arrays.asList("include 'languageJava'"));
        File subBuild = root.newFile("subprojects/language-java/language-java.gradle");
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isSubProject());
    }

    /**
     * Test of isScriptlessSubProject method, of class GradleFiles.
     */
    @Test
    public void testIsScriptlessSubProject() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertTrue(gf.isScriptlessSubProject());
    }

    @Test
    public void testIsScriptlessSubProject2() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        new File(module, "build.gradle").createNewFile();
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        GradleFiles gf = new GradleFiles(module);
        assertFalse(gf.isScriptlessSubProject());
    }

    /**
     * Test of isProject method, of class GradleFiles.
     */
    @Test
    public void testNonProjectIsProject() {
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertFalse(gf.isProject());
    }

    @Test
    public void testNonProjectIsRootProject() {
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertFalse(gf.isRootProject());
    }

    @Test
    public void testNonExisitingIsProject() {
        File prj = new File(root.getRoot(), "shared");
        GradleFiles gf = new GradleFiles(prj);
        assertFalse(gf.isProject());
    }

    @Test
    public void testNonExisitingIsRootProject() {
        File prj = new File(root.getRoot(), "shared");
        GradleFiles gf = new GradleFiles(prj);
        assertFalse(gf.isRootProject());
    }

    @Test
    public void testSimpleIsProject() throws IOException {
        root.newFile("build.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertTrue(gf.isProject());
    }

    @Test
    public void testSimpleIsProject2() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertTrue(gf.isProject());
    }

    @Test
    public void testSimpleIsRootProject() throws IOException {
        root.newFile("build.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertTrue(gf.isRootProject());
    }

    @Test
    public void testSimpleIsRootProject2() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        assertTrue(gf.isRootProject());
    }

    @Test
    public void testGetBuildSrc() throws IOException {
        root.newFile("build.gradle");
        root.newFile("settings.gradle");
        File buildSrc = root.newFolder("buildSrc");
        GradleFiles gf = new GradleFiles(buildSrc);
        assertTrue(gf.isBuildSrcProject());
        assertEquals(null, gf.getFile(GradleFiles.Kind.BUILD_SRC));
    }

    @Test
    public void testGetBuildSrcProperties() throws IOException {
        root.newFile("settings.gradle");

        File buildSrc = root.newFolder("buildSrc");
        GradleFiles gf = new GradleFiles(buildSrc);
        assertTrue(gf.isBuildSrcProject());
        int expectedSize = gf.getFile(GradleFiles.Kind.USER_PROPERTIES).exists() ? 1 : 0;
        assertEquals(expectedSize, gf.getPropertyFiles().size());
    }

    /**
     * Test of getProjectFiles method, of class GradleFiles.
     */
    @Test
    public void testGetFile() throws IOException {
        File build = root.newFile("build.gradle");
        File buildProps = root.newFile("gradle.properties");
        File settings = root.newFile("settings.gradle");
        File module = root.newFolder("module");
        Files.write(settings.toPath(), Arrays.asList("include ':module'"));
        File subBuild = new File(module, "build.gradle");
        subBuild.createNewFile();
        GradleFiles gf = new GradleFiles(module);
        assertEquals(normalizeTempDir(subBuild), normalizeTempDir(gf.getFile(GradleFiles.Kind.BUILD_SCRIPT)));
        assertEquals(normalizeTempDir(build), normalizeTempDir(gf.getFile(GradleFiles.Kind.ROOT_SCRIPT)));
        assertEquals(normalizeTempDir(settings), normalizeTempDir(gf.getFile(GradleFiles.Kind.SETTINGS_SCRIPT)));
        assertEquals(normalizeTempDir(buildProps), normalizeTempDir(gf.getFile(GradleFiles.Kind.ROOT_PROPERTIES)));
        assertEquals(normalizeTempDir(new File(module, "gradle.properties")), normalizeTempDir(gf.getFile(GradleFiles.Kind.PROJECT_PROPERTIES)));
    }

    /**
     * Test of getFile method, of class GradleFiles.
     */
    @Test
    public void testGetProjectFiles() throws IOException {
        File build = root.newFile("build.gradle");
        File settings = root.newFile("settings.gradle");
        GradleFiles gf = new GradleFiles(root.getRoot());
        Set<File> projectFiles = gf.getProjectFiles();
        assertEquals(2, projectFiles.size());
        projectFiles = projectFiles.stream().map(GradleFilesTest::normalizeTempDir).collect(Collectors.toSet());
        assertTrue(projectFiles.contains(normalizeTempDir(build)));
        assertTrue(projectFiles.contains(normalizeTempDir(settings)));
    }

    /**
     * Test of lastChanged method, of class GradleFiles.
     */
    @Test
    public void testLastChanged() throws IOException, InterruptedException {
        File build = root.newFile("build.gradle");
        Thread.sleep(500);
        File settings = root.newFile("settings.gradle");

        GradleFiles gf = new GradleFiles(root.getRoot());
        assertEquals(settings.lastModified(), gf.lastChanged());
    }


}

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
package org.netbeans.modules.web.clientproject.validation;

import java.io.File;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.common.api.ValidationResult;

public class ProjectFoldersValidatorTest extends NbTestCase {

    private File projectDir;
    private File sourceDir;
    private File siteRootDir;
    private File testDir;
    private File testSeleniumDir;


    public ProjectFoldersValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        projectDir = new File(getWorkDir(), "project123");
        assertTrue(projectDir.mkdir());
        sourceDir = new File(projectDir, "src");
        assertTrue(sourceDir.mkdir());
        siteRootDir = new File(projectDir, "public_html");
        assertTrue(siteRootDir.mkdir());
        testDir = new File(projectDir, "tests");
        assertTrue(testDir.mkdir());
        testSeleniumDir = new File(projectDir, "selenium");
        assertTrue(testSeleniumDir.mkdir());
    }

    public void testValidate() {
        ValidationResult result = new ProjectFoldersValidator()
                .validate(sourceDir, siteRootDir, testDir, testSeleniumDir)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoSourceFolder() {
        ValidationResult result = new ProjectFoldersValidator()
                .validateSourceFolder(null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoSiteRootFolder() {
        ValidationResult result = new ProjectFoldersValidator()
                .validateSiteRootFolder(null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoFolders() {
        ValidationResult result = new ProjectFoldersValidator()
                .validate(null, null, null, null)
                .getResult();
        List<ValidationResult.Message> errors = result.getErrors();
        assertFalse(errors.isEmpty());
        assertEquals(ProjectFoldersValidator.SOURCE_OR_SITE_ROOT_FOLDER, errors.get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidSourceFolder() throws Exception {
        File readme = new File(projectDir, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new ProjectFoldersValidator()
                .validateSourceFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.SOURCE_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidSiteRootFolder() throws Exception {
        File readme = new File(projectDir, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new ProjectFoldersValidator()
                .validateSiteRootFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.SITE_ROOT_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingSourceFolder() throws Exception {
        File subdir = new File(projectDir, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new ProjectFoldersValidator()
                .validateSourceFolder(subdir)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.SOURCE_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingSiteRootFolder() throws Exception {
        File subdir = new File(projectDir, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new ProjectFoldersValidator()
                .validateSiteRootFolder(subdir)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.SITE_ROOT_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoTestFolder() {
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestFolder(null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidTestFolder() throws Exception {
        File readme = new File(projectDir, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.TEST_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNotProjectTestFolder() throws Exception {
        testDir = new File(getWorkDir(), "topLevelDir");
        assertTrue(testDir.mkdir());
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestFolder(testDir)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoTestSeleniumFolder() {
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestFolder(null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidTestSeleniumFolder() throws Exception {
        File readme = new File(projectDir, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestSeleniumFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ProjectFoldersValidator.TEST_SELENIUM_FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNotProjectTestSeleniumFolder() throws Exception {
        testSeleniumDir = new File(getWorkDir(), "topLevelDir");
        assertTrue(testSeleniumDir.mkdir());
        ValidationResult result = new ProjectFoldersValidator()
                .validateTestFolder(testSeleniumDir)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testSiteRootFolderUnderneathSourceFolder() throws Exception {
        File newSiteRootDir = new File(sourceDir, "public");
        assertTrue(newSiteRootDir.mkdirs());
        ValidationResult result = new ProjectFoldersValidator()
                .validate(sourceDir, newSiteRootDir, null, null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());

    }

    public void testSourceFolderEqualsSiteRootFolder() throws Exception {
        ValidationResult result = new ProjectFoldersValidator()
                .validate(siteRootDir, siteRootDir, null, null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.getWarnings().isEmpty());
        assertEquals(ProjectFoldersValidator.SOURCE_FOLDER, result.getWarnings().get(0).getSource());
    }

    public void testSourceFolderUnderneathSiteRootFolder() throws Exception {
        File newSourceDir = new File(siteRootDir, "src");
        assertTrue(newSourceDir.mkdirs());
        ValidationResult result = new ProjectFoldersValidator()
                .validate(newSourceDir, siteRootDir, null, null)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertFalse(result.getWarnings().isEmpty());
        assertEquals(ProjectFoldersValidator.SOURCE_FOLDER, result.getWarnings().get(0).getSource());
    }

}

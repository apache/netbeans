/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

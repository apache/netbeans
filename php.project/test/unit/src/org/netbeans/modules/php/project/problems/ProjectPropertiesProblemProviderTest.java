/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.problems;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem;
import org.openide.filesystems.FileUtil;

public class ProjectPropertiesProblemProviderTest extends PhpTestCase {

    public ProjectPropertiesProblemProviderTest(String name) {
        super(name);
    }

    public void testNoProjectProblems() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
    }

    public void testProjectProblemsSourceRoot() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.singletonMap(PhpProjectProperties.SRC_DIR, "nondir"), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsTestRoot() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.singletonMap(PhpProjectProperties.TEST_SRC_DIR, "nondir"), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsTestRoots() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        String projectDir = FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)).getAbsolutePath();
        final String testDir1 = "nondir";
        final String testDir2 = projectDir + File.separatorChar + "nbproject" + File.separatorChar + "private";
        final String testDir3 = projectDir;
        final String testDir100 = projectDir;
        Map<String, String> testDirs = new HashMap<>();
        testDirs.put(PhpProjectProperties.TEST_SRC_DIR + "1", testDir1);
        testDirs.put(PhpProjectProperties.TEST_SRC_DIR + "2", testDir2);
        testDirs.put(PhpProjectProperties.TEST_SRC_DIR + "3", testDir3);
        // ignored, not in a row
        testDirs.put(PhpProjectProperties.TEST_SRC_DIR + "100", testDir100);
        PhpProjectProperties.save(project, testDirs, Collections.<String, String>emptyMap());
        assertEquals(3, problemsProvider.getProblems().size());
        Set<String> contained = new HashSet<>();
        for (ProjectProblem projectProblem : problemsProvider.getProblems()) {
            assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(), projectProblem.getDisplayName());
            String description = projectProblem.getDescription();
            if (description.contains(testDir1 + "\"")) {
                assertTrue(description, contained.add(testDir1));
            }
            if (description.contains("\"" + testDir2 + "\"")) {
                assertTrue(description, contained.add(testDir2));
            }
            if (description.contains("\"" + testDir3 + "\"")) {
                assertTrue(description, contained.add(testDir3));
            }
        }
        assertEquals(contained.toString(), 3, contained.size());
    }

    public void testProjectProblemsSeleniumRoot() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.singletonMap(PhpProjectProperties.SELENIUM_SRC_DIR, "nondir"), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsWebRoot() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.singletonMap(PhpProjectProperties.WEB_ROOT, "nondir"), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsPublicIncludePath() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.singletonMap(PhpProjectProperties.INCLUDE_PATH, "nondir"), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsPrivateIncludePath() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, Collections.<String, String>emptyMap(), Collections.singletonMap(PhpProjectProperties.PRIVATE_INCLUDE_PATH, "nondir"));
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsWholeIncludePath() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project,
                Collections.singletonMap(PhpProjectProperties.INCLUDE_PATH, "nondir"),
                Collections.singletonMap(PhpProjectProperties.PRIVATE_INCLUDE_PATH, "nondir"));
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsAll() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        PhpProjectProperties.save((PhpProject) project, getBrokenProperties(), Collections.<String, String>emptyMap());
        assertEquals(1, problemsProvider.getProblems().size());
        ProjectProblem projectProblem = problemsProvider.getProblems().iterator().next();
        assertEquals(Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_title(), projectProblem.getDisplayName());
    }

    public void testProjectProblemsAllButSourceRoot() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull(problemsProvider);
        assertTrue(problemsProvider.getProblems().isEmpty());
        Map<String, String> brokenProperties = getBrokenProperties();
        brokenProperties.remove(PhpProjectProperties.SRC_DIR);
        PhpProjectProperties.save((PhpProject) project, brokenProperties, Collections.<String, String>emptyMap());
        assertEquals(4, problemsProvider.getProblems().size());
        final List<String> problemTitles = Arrays.asList(
                Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(),
                Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_title(),
                Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_title(),
                Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_title());
        for (ProjectProblem projectProblem : problemsProvider.getProblems()) {
            assertTrue(projectProblem.getDisplayName(), problemTitles.contains(projectProblem.getDisplayName()));
        }
    }

    private Map<String, String> getBrokenProperties() {
        Map<String, String> brokenProperties = new HashMap<>();
        brokenProperties.put(PhpProjectProperties.SRC_DIR, "nondir");
        brokenProperties.put(PhpProjectProperties.TEST_SRC_DIR, "nondir");
        brokenProperties.put(PhpProjectProperties.SELENIUM_SRC_DIR, "nondir");
        brokenProperties.put(PhpProjectProperties.WEB_ROOT, "nondir");
        brokenProperties.put(PhpProjectProperties.INCLUDE_PATH, "nondir");
        brokenProperties.put(PhpProjectProperties.PRIVATE_INCLUDE_PATH, "nondir2");
        return brokenProperties;
    }

}

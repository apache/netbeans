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
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.queries.AccessibilityQuery.Accessibility;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2.Result;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author peterhull
 */
public class ModuleInfoAccessibilityQueryImplTest extends NbTestCase {

    private static final String MODULE_INFO_JAVA = "module-info.java";  //NOI18N

    // Module definition which exports mymodule but not myinternal
    private static final String MODULE_INFO = """
                                              module mymodule {
                                              exports mypackage;
                                              }
                                              """;

    private FileObject src;
    private FileObject test;
    private FileObject workDir;
    private TestProject testProject;
    private FileObject mypackage;
    private FileObject myinternal;

    public ModuleInfoAccessibilityQueryImplTest(String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType());
        workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src = workDir.createFolder("src"); //NOI18N
        test = workDir.createFolder("test"); //NOI18N
        mypackage = src.createFolder("mypackage"); //NOI18N
        myinternal = src.createFolder("myinternal"); //NOI18N
        try (OutputStream os = src.createAndOpen(MODULE_INFO_JAVA)) {
            os.write(MODULE_INFO.getBytes(StandardCharsets.UTF_8));
        }
        Project prj = TestProject.createProject(workDir, src, test);
        testProject = prj.getLookup().lookup(TestProject.class);
        // Using modules, so project has to be source level 9 or later.
        UpdateHelper helper = testProject.getUpdateHelper();
        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        properties.putAll(Map.of("javac.source", "9", "javac.target", "9"));
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
        /* Modular project has the structure
        ROOT
        +-src
        | +-mypackage
        | | `-*.java
        | +-myinternal
        | + `-*.java
        | `-module-info.java
        `-test
          `-mypackage
            `-MyClassTest.java
         */
    }

    /**
     * Test of isPubliclyAccessible method, of class
     * ModuleInfoAccessibilityQueryImpl. Tests if an exported package
     * 'mypackage' is accessible.
     *
     * @throws java.io.IOException if test file creation failed.
     */
    @Test
    public void testIsPubliclyAccessible1() throws IOException {
        ModuleInfoAccessibilityQueryImpl instance = new ModuleInfoAccessibilityQueryImpl(null, testProject.getSourceRoots(), null, testProject.getTestRoots());
        Result result = instance.isPubliclyAccessible(mypackage);
        // Return value can be null, that's a fail
        if (result == null) {
            fail("AccessibilityQuery returned null"); //NOI18N
        } else {
            Accessibility accessibility = result.getAccessibility();
            Accessibility expAccessibility = Accessibility.EXPORTED;
            assertEquals(expAccessibility, accessibility);
        }
    }

    /**
     * Test of isPubliclyAccessible method, of class
     * ModuleInfoAccessibilityQueryImpl. Tests if a not-exported package
     * 'myinternal' is not accessible.
     *
     * @throws java.io.IOException if test file creation failed.
     */
    @Test
    public void testIsPubliclyAccessible2() throws IOException {
        ModuleInfoAccessibilityQueryImpl instance = new ModuleInfoAccessibilityQueryImpl(null, testProject.getSourceRoots(), null, testProject.getTestRoots());
        Result result = instance.isPubliclyAccessible(myinternal);
        // Return value can be null, that's a fail
        if (result == null) {
            fail("AccessibilityQuery returned null"); //NOI18N
        } else {
            Accessibility accessibility = result.getAccessibility();
            Accessibility expAccessibility = Accessibility.PRIVATE;
            assertEquals(expAccessibility, accessibility);
        }
    }
}

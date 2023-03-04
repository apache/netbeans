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
package org.netbeans.modules.gradle.queries;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public class ParentRootProviderTest extends AbstractGradleProjectTestCase {
    /**
     * The root project, usually
     */
    private Project projectA;   
    private Project projectB;
    private Project projectFolder;
    private Project projectC;
    
    public ParentRootProviderTest(String name) {
        super(name);
    }
    
    private boolean trustRootProject = true;

    private void setupComplexMultiProject(boolean openRoot) throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'\n include ':folder:projectC'");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\ndependencies {implementation project(':folder:projectC')}", null);
        FileObject c = createGradleProject("projectA-" + rnd + "/folder/projectC",
                "apply plugin: 'java'", null);

        projectA = ProjectManager.getDefault().findProject(a);
        if (trustRootProject) {
            ProjectTrust.getDefault().trustProject(projectA);
        }
        
        // must open projectA, otherwise projectFolder won't be recognized as a project.
        if (openRoot) {
            openProject(projectA);
        }
        projectFolder = ProjectManager.getDefault().findProject(c.getParent());
        projectB = ProjectManager.getDefault().findProject(b);
        projectC = ProjectManager.getDefault().findProject(c);
        
    }
    
    private void setupSimpleMultiProject(boolean openRoot) throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'\n include ':projectC'");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\ndependencies {implementation project(':projectC')}", null);
        FileObject c = createGradleProject("projectA-" + rnd + "/projectC",
                "apply plugin: 'java'", null);

        projectA = ProjectManager.getDefault().findProject(a);
        if (trustRootProject) {
            ProjectTrust.getDefault().trustProject(projectA);
        }
        
        // must open projectA, otherwise projectFolder won't be recognized as a project.
        if (openRoot) {
            openProject(projectA);
        }
        projectFolder = ProjectManager.getDefault().findProject(c.getParent());
        projectB = ProjectManager.getDefault().findProject(b);
        projectC = ProjectManager.getDefault().findProject(c);
        
    }
    
    private void assertParent(Project parent, Project child) {
        assertSame(parent, ProjectUtils.parentOf(child));
    }
    
    protected Project openProject(Project prj) throws IOException {
        return openProject(prj.getProjectDirectory());
    }
    
    protected Project openProject(FileObject projectDir) throws IOException {
        Project prj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(prj);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(prj.getLookup().lookup(ProjectOpenedHook.class));
        return prj;
    }

    public void testComplexMultiProjectClosed() throws IOException {
        setupComplexMultiProject(false);
        
        assertParent(projectA, projectB);
        
        // folders are not recognized as projects unless their parent opens, see NETBEANS-5468
        assertNull(projectFolder);
        assertParent(projectFolder, projectC);
        assertParent(null, projectA);
        
        
        Set<Project> subprojects = ProjectUtils.getContainedProjects(projectA, true);
        
        // unopened projects do not enumerate the contents; see NETBEANS-5468
        assertFalse(subprojects.contains(projectB));
        assertFalse(subprojects.contains(projectC));
    }

    public void testComplexMultiProjectOpened() throws IOException {
        setupComplexMultiProject(true);
        
        assertParent(projectA, projectB);
        assertParent(projectFolder, projectC);
        assertParent(null, projectA);
        
        Set<Project> subprojects = ProjectUtils.getContainedProjects(projectA, true);
        assertTrue(subprojects.contains(projectB));
        assertTrue(subprojects.contains(projectC));
    }

    public void testSimpleMultiProjectClosed() throws IOException {
        setupSimpleMultiProject(false);
        
        assertParent(projectA, projectB);
        assertParent(projectA, projectC);
        assertParent(null, projectA);
        
        
        Set<Project> subprojects = ProjectUtils.getContainedProjects(projectA, true);
        
        // unopened projects do not enumerate the contents; see NETBEANS-5468
        assertFalse(subprojects.contains(projectB));
        assertFalse(subprojects.contains(projectC));
    }

    public void testSimpleMultiProjectOpened() throws IOException {
        setupSimpleMultiProject(true);
        
        assertParent(projectA, projectB);
        assertParent(projectA, projectC);
        assertParent(null, projectA);
        
        Set<Project> subprojects = ProjectUtils.getContainedProjects(projectA, true);
        assertTrue(subprojects.contains(projectB));
        assertTrue(subprojects.contains(projectC));
    }

}

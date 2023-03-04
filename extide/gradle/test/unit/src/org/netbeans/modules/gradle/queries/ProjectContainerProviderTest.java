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
import static junit.framework.TestCase.assertFalse;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public class ProjectContainerProviderTest extends AbstractGradleProjectTestCase {

    public ProjectContainerProviderTest(String name) {
        super(name);
    }

    public void testSimpleRootProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        ProjectContainerProvider pvd = prjA.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        assertFalse(pvd.getContainedProjects().isRecursive());
        assertTrue(pvd.getContainedProjects().getProjects().isEmpty());
    }

    public void testSimpleMultiProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        Project prjB = ProjectManager.getDefault().findProject(b);
        openProject(a);
        ProjectContainerProvider pvd = prjA.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        assertFalse(pvd.getContainedProjects().isRecursive());
        assertEquals(1, pvd.getContainedProjects().getProjects().size());
        assertEquals(prjB, pvd.getContainedProjects().getProjects().iterator().next());
    }

    public void testComplexMultiProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'\n include ':folder:projectC'");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "", null);
        FileObject c = createGradleProject("projectA-" + rnd + "/folder/projectC",
                "", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        Project prjC = ProjectManager.getDefault().findProject(c);
        Project prjFolder = ProjectManager.getDefault().findProject(c.getParent());
        ProjectContainerProvider pvd = prjA.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        Set<? extends Project> projects = pvd.getContainedProjects().getProjects();
        assertFalse(pvd.getContainedProjects().isRecursive());
        assertEquals(2, projects.size());
        assertTrue(projects.contains(prjB));
        assertTrue(projects.contains(prjFolder));
        assertFalse(projects.contains(prjC));

        pvd = prjFolder.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        projects = pvd.getContainedProjects().getProjects();
        assertEquals(1, projects.size());
        assertTrue(projects.contains(prjC));

        pvd = prjB.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        projects = pvd.getContainedProjects().getProjects();
        assertTrue(projects.isEmpty());

    }

    public void testWeirdMultiProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'\n include ':folder:projectC'\nproject(':folder:projectC').projectDir = new File(\"$rootDir/projectC\")");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "", null);
        FileObject c = createGradleProject("projectA-" + rnd + "/projectC",
                "", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        Project prjC = ProjectManager.getDefault().findProject(c);
        ProjectContainerProvider pvd = prjA.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        Set<? extends Project> projects = pvd.getContainedProjects().getProjects();
        assertFalse(pvd.getContainedProjects().isRecursive());
        // There is a Gradle project defined as ':folder' though it's directory
        // does not exists it cannot be represented as a NetBeans Project at the moment.
        assertEquals(1, projects.size());
        assertTrue(projects.contains(prjB));
        assertFalse(projects.contains(prjC));

        pvd = prjB.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pvd);
        projects = pvd.getContainedProjects().getProjects();
        assertTrue(projects.isEmpty());

    }
}

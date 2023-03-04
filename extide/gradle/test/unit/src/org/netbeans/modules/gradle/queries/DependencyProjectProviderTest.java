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
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.spi.project.DependencyProjectProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public class DependencyProjectProviderTest extends AbstractGradleProjectTestCase {

    public DependencyProjectProviderTest(String name) {
        super(name);
    }

    public void testSimpleRootProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        DependencyProjectProvider pvd = prjA.getLookup().lookup(DependencyProjectProvider.class);
        assertNotNull(pvd);
        assertFalse(pvd.getDependencyProjects().isRecursive());
        assertTrue(pvd.getDependencyProjects().getProjects().isEmpty());
    }

    public void testComplexMultiProject() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "", "include ':projectB'\n include ':folder:projectC'");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\ndependencies {implementation project(':folder:projectC')}", null);
        FileObject c = createGradleProject("projectA-" + rnd + "/folder/projectC",
                "apply plugin: 'java'", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        openProject(b);
        openProject(c);
        Project prjB = ProjectManager.getDefault().findProject(b);
        Project prjC = ProjectManager.getDefault().findProject(c);
        Project prjFolder = ProjectManager.getDefault().findProject(c.getParent());
        DependencyProjectProvider pvd = prjA.getLookup().lookup(DependencyProjectProvider.class);
        assertNotNull(pvd);
        Set<? extends Project> projects = pvd.getDependencyProjects().getProjects();
        assertFalse(pvd.getDependencyProjects().isRecursive());
        assertTrue(projects.isEmpty());

        pvd = prjB.getLookup().lookup(DependencyProjectProvider.class);
        assertNotNull(pvd);
        projects = pvd.getDependencyProjects().getProjects();
        assertEquals(1, projects.size());
        assertTrue(projects.contains(prjC));

    }

}

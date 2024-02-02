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
package org.netbeans.modules.gradle.api;

import java.io.File;
import java.util.List;
import static junit.framework.TestCase.assertNotNull;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Assume;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.GradleBaseProjectInternal;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class GradleBaseProjectTest extends AbstractGradleProjectTestCase {

    public GradleBaseProjectTest(String name) {
        super(name);
    }
    
    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    File destDirF;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        System.setProperty("test.reload.sync", "true");
        
        destDirF = getTestNBDestDir();
        clearWorkDir();
    
        DummyInstalledFileLocator.registerDestDir(destDirF);
        GradleExperimentalSettings.getDefault().setOpenLazy(false);
    }

    FileObject projectDir;

    private Project makeProject(String subdir) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        projectDir = FileUtil.copyFile(src, FileUtil.toFileObject(getWorkDir()), src.getNameExt());
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        ProjectTrust.getDefault().trustProject(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();
        
        NbGradleProject.get(p).toQuality("Load data", NbGradleProject.Quality.FULL, false).toCompletableFuture().get();
        return p;
    }
    
    private void assertConsistent(GradleBaseProject gbp, GradleTask gt, boolean shallow) throws Exception {
        assertNotNull(gt);
        
        List<GradleTask> tasks = gbp.getTaskPredecessors(gt, shallow);
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
        
        assertFalse(tasks.contains(gt));
        
        for (GradleTask t : tasks) {
            assertNotNull(t);
            assertSame(gbp.getTaskByName(t.getName()), t);
            assertEquals(gbp.getPath(), t.getProjectPath());
        }
        
        if (shallow) {
            return;
        }
        
        // checks that for each task, its predecessors appear before the task in the deep task list.
        for (GradleTask t : tasks) {
            List<GradleTask> predecessors = gbp.getTaskPredecessors(t, true);
            assertTrue(tasks.containsAll(predecessors));
            
            int taskIndex = tasks.indexOf(t);
            assertTrue(taskIndex >= 0);
            for (GradleTask pred : predecessors) {
                int predIndex = tasks.indexOf(pred);
                assertTrue(predIndex < taskIndex);
            }
        }
    }
    
    /**
     * Checks that the task dependencies for 'jar' can be enumerated and are the same
     * instances as GradleTasks served by name by GradleBaseProject
     * @throws Exception 
     */
    public void testSimpleTaskDependenciesShallowConsistent() throws Exception {
        Project p = makeProject("projects/simple");
        assertNotNull(p);
        
        GradleBaseProject gbp = GradleBaseProject.get(p);
        assertNotNull(gbp);
        
        GradleTask gt = gbp.getTaskByName("assemble");
        assertConsistent(gbp, gt, true);
    }
    
    /**
     * Checks that the task dependencies for 'jar' can be enumerated and are the same
     * instances as GradleTasks served by name by GradleBaseProject
     * @throws Exception 
     */
    public void testSimpleTaskDependenciesDeepConsistent() throws Exception {
        Project p = makeProject("projects/simple");
        assertNotNull(p);
        
        GradleBaseProject gbp = GradleBaseProject.get(p);
        assertNotNull(gbp);
        
        GradleTask gt = gbp.getTaskByName("assemble");
        assertConsistent(gbp, gt, false);
    }
    /**
     * Checks that the task dependencies for 'jar' can be enumerated and are the same
     * instances as GradleTasks served by name by GradleBaseProject
     * @throws Exception 
     */
    public void testMultiTaskDependenciesShallowConsistent() throws Exception {
        Project p = makeProject("projects/multi");
        assertNotNull(p);
        
        Project sub = openProject(p.getProjectDirectory().getFileObject("p2"));
        
        GradleBaseProject gbp = GradleBaseProject.get(sub);
        assertNotNull(gbp);
        
        GradleTask gt = gbp.getTaskByName("assemble");
        assertConsistent(gbp, gt, true);
    }
    
    /**
     * Checks that the task dependencies for 'jar' can be enumerated and are the same
     * instances as GradleTasks served by name by GradleBaseProject
     * @throws Exception 
     */
    public void testMultiTaskDependenciesDeepConsistent() throws Exception {
        Project p = makeProject("projects/multi");
        assertNotNull(p);
        
        Project sub = openProject(p.getProjectDirectory().getFileObject("p2"));
        
        GradleBaseProject gbp = GradleBaseProject.get(sub);
        assertNotNull(gbp);
        
        GradleTask gt = gbp.getTaskByName("assemble");
        assertConsistent(gbp, gt, false);
    }
    
    public void testExternalDependnecies() throws Exception {
        Project p = makeProject("projects/externaldeps");
        assertNotNull(p);
        
        Project sub = openProject(p.getProjectDirectory().getFileObject("p2"));

        GradleBaseProject gbp = GradleBaseProject.get(sub);
        assertNotNull(gbp);
        
        GradleTask gt = gbp.getTaskByName("assemble");
        List<GradleTask> tasks = gbp.getTaskPredecessors(gt, false);
        
        GradleTask external = tasks.stream().filter(GradleTask::isExternal).findAny().orElse(null);
        assertNotNull(external);
        assertTrue(external.isExternal());
        assertEquals("jar", external.getName());
        assertEquals(":p1:jar", external.getPath());
        assertEquals(":p1", external.getProjectPath());
    }
    
    private void assertProjectLoadedWithNoProblems(Project p, String expectedVersion) {
        GradleBaseProject gbp = GradleBaseProject.get(p);
        assertNotNull(gbp);
        assertTrue(NbGradleProject.get(p).getQuality().atLeast(NbGradleProject.Quality.FULL));
        assertEquals(0, gbp.getProblems().size());
        GradleBaseProjectInternal baseInternal = NbGradleProject.get(p).projectLookup(GradleBaseProjectInternal.class);
        assertNotNull(baseInternal);
        assertEquals(expectedVersion, baseInternal.getGradleVersion());
    }
    
    private Project makeProjectWithWrapper(String subdir, String gradleVersion) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        projectDir = FileUtil.copyFile(src, FileUtil.toFileObject(getWorkDir()), src.getNameExt());
        
        GradleDistribution dist = GradleDistributionManager.get().defaultDistribution();
        GradleConnector gconn = GradleConnector.newConnector();
        gconn = gconn.useGradleUserHomeDir(dist.getGradleUserHome());
        if (dist.isAvailable()) {
            gconn = gconn.useInstallation(dist.getDistributionDir());
        } else {
            gconn = gconn.useDistribution(dist.getDistributionURI());
        }
        try (ProjectConnection c = gconn.forProjectDirectory(FileUtil.toFile(projectDir)).connect()) {
            c.newBuild().forTasks("wrapper").addArguments("wrapper", "--gradle-version", gradleVersion).run();
        }
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        ProjectTrust.getDefault().trustProject(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();
        
        NbGradleProject.get(p).toQuality("Load data", NbGradleProject.Quality.FULL, false).toCompletableFuture().get();
        gconn.disconnect();
        return p;
    }
    
    public void testOldGradle611ProjectLoads() throws Exception {
        String s = System.getProperty("java.specification.version");
        Assume.assumeTrue(s.startsWith("1.") || Integer.parseInt(s) < 17);
        Project p = makeProjectWithWrapper("projects/oldgradle/basic", "6.1.1");
        assertProjectLoadedWithNoProblems(p, "6.1.1");
    }

    public void testOldGradle683ProjectLoads() throws Exception {
        String s = System.getProperty("java.specification.version");
        Assume.assumeTrue(s.startsWith("1.") || Integer.parseInt(s) < 17);
        Project p = makeProjectWithWrapper("projects/oldgradle/basic", "6.8.3");
        assertProjectLoadedWithNoProblems(p, "6.8.3");
    }

    public void testOldGradle700ProjectLoads() throws Exception {
        String s = System.getProperty("java.specification.version");
        Assume.assumeTrue(s.startsWith("1.") || Integer.parseInt(s) < 17);
        Project p = makeProjectWithWrapper("projects/oldgradle/basic", "7.0");
        assertProjectLoadedWithNoProblems(p, "7.0");
    }

    public void testOldGradle710ProjectLoads() throws Exception {
        String s = System.getProperty("java.specification.version");
        Assume.assumeTrue(s.startsWith("1.") || Integer.parseInt(s) < 17);
        Project p = makeProjectWithWrapper("projects/oldgradle/basic", "7.1");
        assertProjectLoadedWithNoProblems(p, "7.1");
    }
}


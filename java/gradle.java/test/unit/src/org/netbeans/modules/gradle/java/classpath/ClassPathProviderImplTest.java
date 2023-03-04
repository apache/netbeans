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
package org.netbeans.modules.gradle.java.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lkishalmi
 */
public class ClassPathProviderImplTest extends AbstractGradleProjectTestCase {

    public ClassPathProviderImplTest(String name) {
        super(name);
    }

    @Test
    public void testRuntimeClassPath() throws Exception {
        System.out.println("find runtime ClassPath");
        FileObject fo = createGradleProject("apply plugin: 'java'");
        FileObject src = FileUtil.createFolder(fo, "src/main/java");
        FileObject java = src.createData("Whatever.java");
        Project prj = openProject(fo);
        ClassPathProvider cpp = prj.getLookup().lookup(ClassPathProvider.class);
        assertNotNull(cpp);
        GradleJavaProject gjp = GradleJavaProject.get(prj);


        Set<FileObject> outputs = new HashSet<>();
        outputs.add(FileUtil.createFolder(gjp.getMainSourceSet().getOutputResources()));
        for (File dir : gjp.getMainSourceSet().getOutputClassDirs()) {
            outputs.add(FileUtil.createFolder(dir));
        }

        ClassPath rt = cpp.findClassPath(java, ClassPath.EXECUTE);
        for (FileObject output : outputs) {
            assertTrue(rt.contains(output));
        }
    }
    
    /**
     * The created java file
     */
    FileObject java;
    
    /**
     * The created buildscript
     */
    FileObject project;
    
    String path;
    
    private void createSimpleApp() throws IOException {
        FileObject d = FileUtil.toFileObject(getDataDir());
        FileObject main = d.getFileObject("javasimple/src/main/java/test/App.java");
        project = createGradleProject(path, String.join("\n", 
                "apply plugin: 'java'",
                "sourceSets {",
                "    main {",
                "        java {",
                "            srcDirs = ['src/main/java', 'src/main/java2']",
                "        }",
                "    }",
                " }"
            ), null);
        FileObject src = FileUtil.createFolder(project, "src/main/java/test");
        java = FileUtil.copyFile(main, src, main.getName());
        // create an additional source folder - this will be only recognized after true project load.
        FileObject java2 = FileUtil.createFolder(project, "src/main/java2/test");
    }
    
    public void testUntrustedSourcePath() throws Exception {
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        assertNotNull(prj);
        // declare the project as trusted.
        
        ClassPath cp = ClassPath.getClassPath(java, ClassPath.SOURCE);
        assertNotNull("Source classpath must exist for a (standard) source folder", cp);
        FileObject r = cp.findResource("test/App.java");
        assertSame("Class source must be on source classpath", java, r);
        
        // check that the project is STILL not even EVALUATED so it did not invoke Gradle:
        NbGradleProject ngp = NbGradleProject.get(prj);
        assertTrue("Untrusted new project must not leave FALLBACK", ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    public void testUntrustedSourcesNoCompile() throws Exception {
        createSimpleApp();
        ClassPath cp = ClassPath.getClassPath(java, ClassPath.COMPILE);
        assertEquals("No compile roots for untrusted new project", 0, cp.getRoots().length);
        Project prj = FileOwnerQuery.getOwner(java);
        NbGradleProject ngp = NbGradleProject.get(prj);
        assertTrue("Untrusted new project must not leave FALLBACK", ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    public void testUntrustedSourceHasJDK() throws Exception {
        createSimpleApp();
        ClassPath bcp = ClassPath.getClassPath(java, ClassPath.BOOT);
        assertTrue("Even untrusted project must have a JDK", bcp.getRoots().length > 0);
    }
    
    private void compileProject(Project prj) throws Exception {
        ProjectTrust.getDefault().trustProject(prj);
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        
        CountDownLatch latch = new CountDownLatch(1);
        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.fixed(new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                latch.countDown();
            }
        }));
        latch.await();
    }
    
    /**
     * Checks that the compile path is nonempty after successful compilaiton.
     */
    public void testTrustedSourcesCompilePath() throws Exception {
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        compileProject(prj);
        ClassPath cp = ClassPath.getClassPath(java, ClassPath.COMPILE);
        assertEquals(1, cp.getRoots().length);
    }
    
    /**
     * Checks that the project is released.
     */
    public void testProjectReleased() throws Exception {
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        Reference<Project> refProject = new WeakReference<>(prj);
        prj = null;
        
        // because of TimedWeakReference in Project API, need to wait > 15 seconds
        Thread.sleep(20 *1000);
        assertGC("Project must GC", refProject);
    }
    
    
    private void openAndCloseProject(Project prj) throws Exception {
        OpenProjects.getDefault().open(new Project[] { prj }, false);
        OpenProjects.getDefault().openProjects().get();
        OpenProjects.getDefault().close(new Project[] { prj });
        OpenProjects.getDefault().openProjects().get();
    }
    
    /**
     * Checks that the project is released.
     */
    public void testOpenedProjectReleased() throws Exception {
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        openAndCloseProject(prj);
        
        Reference<Project> refProject = new WeakReference<>(prj);
        prj = null;
        
        // because of TimedWeakReference in Project API, need to wait > 15 seconds
        Thread.sleep(20 *1000);
        assertGC("Project must GC", refProject);
    }

    /**
     * Checks that a trusted, once compiled project will provide nonempty compile classpath
     * just after open.
     */
    public void testCompileNonemptyOnceCompiled() throws Exception {
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        compileProject(prj);
        
        // open the project - and then close it, so the project is ditched by Gradle module.
        openAndCloseProject(prj);
        
        ProjectTrust.getDefault().distrustProject(prj);
        
        NbGradleProject ngp = NbGradleProject.get(prj);
        assertTrue("Closed project should be at least EVALUATED", ngp.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
        
        ClassPath cp = ClassPath.getClassPath(java, ClassPath.COMPILE);
        assertFalse("Cached project must know its COMPILE path", cp.getRoots().length == 0);
    }

    @Override
    protected void tearDown() throws Exception {
        // TEMPORARY: enable logging for CI failure diagnostics:
        Logger.getLogger("org.netbeans.modules.gradle").setLevel(Level.WARNING);
        setLevel(Level.INFO);
        super.tearDown();
    }


    public static void setLevel(Level targetLevel) {
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(targetLevel);
        }
    }

    /**
     * Checks that a trusted, once compiled project will provide nonempty compile classpath
     * just after open.
     */
    public void testCompilePreTrusted() throws Exception {
        Project prj2 = testReopenedProjectHasClasspath(false);

        FileObject j2 = prj2.getProjectDirectory().getFileObject("src/main/java/test/App.java");
        
        NbGradleProject ngp = NbGradleProject.get(prj2);
        assertTrue("Closed project should be at least EVALUATED", ngp.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));

        ClassPathProviderImpl gradleCPImpl = (ClassPathProviderImpl)prj2.getLookup().lookup(ClassPathProvider.class);
        // Hack: relies on that ClassPathProviderImpl.updateGroups locks on the provider's instance; allows
        // to check CP properties before the asynchronous task updates the project.
        synchronized (gradleCPImpl) {
            ClassPath cp = ClassPath.getClassPath(j2, ClassPath.COMPILE);
            assertFalse("Cached project must know its COMPILE path", cp.getRoots().length == 0);
            assertContainsJava2(ClassPath.getClassPath(j2, ClassPath.SOURCE));
        }
    }
    
    /**
     * Checks that a trusted, but NOT compiled (without caches) project will provide nonempty compile classpath
     * just after open.
     */
    public void testCompiledTrustedWithoutCaches() throws Exception {
        Project prj2 = testReopenedProjectHasClasspath(true);
        NbGradleProject ngp = NbGradleProject.get(prj2);
        assertTrue("Closed project should not be FULL or FULL_ONLINE", ngp.getQuality().worseThan(NbGradleProject.Quality.FULL));
        
        // without caches the project may only load to a FALLBACK state, one need to explicitly ask for FULL or FULL_ONLINE (i.e. project open).
        // there's a test in Gradle core which checks the buildscript is not implicitly executed.

        FileObject j2 = prj2.getProjectDirectory().getFileObject("src/main/java/test/App.java");

        CountDownLatch cdl = new CountDownLatch(1);
        CountDownLatch cdl2 = new CountDownLatch(1);
        ClassPath cp;
        ClassPath scp;
        ClassPathProviderImpl gradleCPImpl = (ClassPathProviderImpl)prj2.getLookup().lookup(ClassPathProvider.class);
        
        // Hack: relies on that ClassPathProviderImpl.updateGroups locks on the provider's instance; allows
        // to check CP properties before the asynchronous task updates the project.
        synchronized (gradleCPImpl) {
            assertTrue("Closed project with no caches should not reach EVALUATED", ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
            cp = ClassPath.getClassPath(j2, ClassPath.COMPILE);
            scp = ClassPath.getClassPath(j2, ClassPath.SOURCE);
            
            assertEquals("The project in fallback state should have no compile path", 0, cp.getRoots().length);
            
            for (FileObject fo : scp.getRoots()) {
                if (fo.getName().endsWith("java2")) {
                    fail("java2 must not be listed in fallback mode");
                }
            }

            cp.addPropertyChangeListener((e) -> {
                if (!ClassPath.PROP_ROOTS.equals(e.getPropertyName())) {
                    return;
                }
                cdl.countDown();
            });
            
            scp.addPropertyChangeListener((e) -> {
                if (!ClassPath.PROP_ROOTS.equals(e.getPropertyName())) {
                    return;
                }
                cdl2.countDown();
            });
            
        }
        // but, there should be a reload scheduled, if one asks for a classpath - so wait for the event for some time
        
        assertTrue("Compile path roots must change", cdl.await(100, TimeUnit.SECONDS));
        assertFalse("The project should auto-initialize and report changes", cp.getRoots().length == 0);
        assertTrue("Source path roots must change", cdl2.await(100, TimeUnit.SECONDS));
        assertContainsJava2(scp);
    }
    
    private void assertContainsJava2(ClassPath scp) {
        boolean found = false;
        for (FileObject fo : scp.getRoots()) {
            if (fo.getName().endsWith("java2")) {
                found = true;
            }
        }
        assertTrue("java2 must be among sources after full load", found);
    }
    
    private Project testReopenedProjectHasClasspath(boolean destroyCaches) throws Exception {
        // TEMPORARY: enable logging for CI failure diagnostics. The test was failing on CI, let it produce more logs
        // remove the logging again when the test proves stable.
        Logger.getLogger("org.netbeans.modules.gradle").setLevel(Level.FINER);
        setLevel(Level.FINER);
        
        path = "first";
        createSimpleApp();
        Project prj = FileOwnerQuery.getOwner(java);
        compileProject(prj);
        GradleFiles files = prj.getLookup().lookup(NbGradleProjectImpl.class).getGradleFiles();
        
        // open the project - and then close it, so the project is ditched by Gradle module.
        openAndCloseProject(prj);

        // we need to flush Gradle disk cache, as it retains loaded data identified by j.io.File
        // In addition, we need to delete the serialized state so that only trust remains.

        if (destroyCaches) {
            ProjectInfoDiskCache.testDestroyCache(files);
        }
        ProjectInfoDiskCache.testFlushCaches();
        
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject s = wd.createFolder("second");
        
        copy(project, s);
        
        // rename the files; the cache encodes full path to the project. The renamed project will
        // be in the same directory, but its FileObject is different - project cache will not list it.
        try (FileLock fl = project.lock()) {
            project.rename(fl, "old", null);
        }
        try (FileLock fl = project.lock()) {
            s.rename(fl, "first", null);
        }
        
        
        Project prj2 = FileOwnerQuery.getOwner(s);
        assertNotSame(prj, prj2);
        
        return prj2;
    }
    
    private void copy(FileObject orig, FileObject to) throws IOException {
        for (FileObject x : orig.getChildren()) {
            if (x.isFolder()) {
                FileObject cp = FileUtil.createFolder(to, x.getNameExt());
                copy(x, cp);
            } else {
                x.copy(to, x.getName(), x.getExt());
            }
        }
    }
    
    /**
     * Classpath in a new project (not loaded) project can be just guessed. But should force project load
     * and refresh itself afterwards.
     * /
     * @throws Exception 
     */
    public void testClasspathWillRefreshLate() throws Exception {
        FileObject d = FileUtil.toFileObject(getDataDir());
        FileObject main = d.getFileObject("javasimple/src/main/java/test/App.java");
        project = createGradleProject(getName(), "apply plugin: 'java'\n"
                + "\n"
                + "sourceSets {\n"
                + "    main {\n"
                + "        java {\n"
                + "            srcDirs(\"src/main/java\", \"src/main/kava\" )\n"
                + "        }\n"
                + "    }\n"
                + "}\n", null);
        
        FileObject src = FileUtil.createFolder(project, "src/main/java/test");
        java = FileUtil.copyFile(main, src, main.getName());
        
        // add some additional sources (tests):
        FileObject src2 = FileUtil.createFolder(project, "src/main/kava/test2");

        Project prj = FileOwnerQuery.getOwner(java);
        ProjectTrust.getDefault().trustProject(prj);

        NbGradleProject gp = NbGradleProject.get(prj);
        gp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (ClassPathProviderImplTest.this) {
                    // just block the project reload in the async RP
                }
            }
        });

        CountDownLatch cpChange = new CountDownLatch(1);

        ClassPath cp;
        synchronized (this) {
            cp = ClassPath.getClassPath(java, ClassPath.SOURCE);
            // uninitialized project, no roots reported at this time
            assertEquals(1, cp.getRoots().length);

            cp.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                        cpChange.countDown();
                    }
                }
            });
        }

        cpChange.await();
        FileObject secondSource = cp.findResource("test2");
        assertSame(src2, secondSource);
    }
}

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
package org.netbeans.modules.gradle.loaders;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency.ModuleDependency;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.modules.projectapi.nb.NbProjectManager;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class DiskCacheProjectLoaderTest extends AbstractGradleProjectTestCase{
    private FileObject projectDir;
    
    public DiskCacheProjectLoaderTest(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        TestSuite ts = new TestSuite();
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration().
                addTest(DiskCacheProjectLoaderTest.class).
                addTest("testInitialLoadProject").
                reuseUserDir(false).
                gui(false).
                enableModules(".*gradle.*").
                reuseUserDir(false);
        
        Test run1 = NbModuleSuite.create(conf);

        conf = NbModuleSuite.emptyConfiguration().
                        addTest(DiskCacheProjectLoaderTest.class).
                        addTest("testLoadCachedProject").
                        reuseUserDir(false).
                        gui(false).
                        enableModules(".*gradle.*").
                        reuseUserDir(false);
        
        Test run2 = NbModuleSuite.create(conf);
        ts.addTest(run1);
        ts.addTest(run2);
        
        return ts;
    }
    
    public void testLoadCachedProject() throws Exception {
        GradleBaseProject gbp = openBaseProject();
        assertCompressHasArtifacts(gbp);
    }
    
    private FileObject createProject(String name, String resource) throws IOException {
        FileObject ret = FileUtil.toFileObject(getWorkDir());
        projectDir = ret.createFolder(name);
        projectDir.getFileSystem().runAtomicAction(() -> {;
            FileObject bs = projectDir.createData("build.gradle");
            try (OutputStream os = bs.getOutputStream()) {
                FileUtil.copy(getClass().getResourceAsStream(resource), os);
            }
        });
        return projectDir;
    }
    
    private GradleBaseProject openBaseProject() throws Exception {
        String s = System.getProperty("gradle.test.project.path");
        FileObject a = FileUtil.toFileObject(new File(s));
        assertNotNull(a);
        
        // HACK HACK HACK: If the following is uncommented, a race condition happens
        // between the test main thread and Git module that runs SimpleFileOwnerQueryImplementation on
        // a project directory before the 'build.gradle' has been materialized, caching 'no project' answer until
        // FS events are delivered & processed by project system (that reset the cache).
        // 
        // Remove the hack after NETBEANS-6305 is fixed.
        SimpleFileOwnerQueryImplementation.reset();
        Method m = NbProjectManager.class.getDeclaredMethod("reset");
        m.setAccessible(true);
        m.invoke(Lookup.getDefault().lookup(ProjectManagerImplementation.class));
        // END HACK
        
        Object[] arr = Lookup.getDefault().lookupAll(ProjectFactory.class).toArray();
        Project proj = ProjectManager.getDefault().findProject(a);
        assertNotNull(proj);
        
        ActionProvider ap = proj.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);
        
        final CountDownLatch primeLatch = new CountDownLatch(1);
        AtomicBoolean status = new AtomicBoolean(false);
        ActionProgress prog = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                status.set(success);
                primeLatch.countDown();
            }
        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(prog));
        primeLatch.await();
        
        assertTrue(status.get());
        GradleBaseProject gbp = GradleBaseProject.get(proj);
        
        return gbp;
    }
    
    private void assertCompressHasArtifacts(GradleBaseProject gbp) {
        GradleConfiguration compC = gbp.getConfigurations().get("compileClasspath");
        Set<ModuleDependency> moDep = compC.findModules("org.apache.commons:commons-compress:1.20");
        assertNotNull(moDep != null);
        assertEquals(1, moDep.size());
        
        ModuleDependency compress = moDep.iterator().next();
        Set<File> artifacts = compress.getArtifacts();
        assertFalse(artifacts.isEmpty());
    }
    
    public void testInitialLoadProject() throws Exception {
        FileObject a = createProject("projectA", "testReloadProject.gradle");
        
        // The other test will run in a different NbModuleSuite, loaded by a different ClassLoader -- sharing in a static field is not possible.
        System.setProperty("gradle.test.project.path", FileUtil.toFile(a).toString());
        
        GradleBaseProject gbp = openBaseProject();
        assertCompressHasArtifacts(gbp);
    }
}

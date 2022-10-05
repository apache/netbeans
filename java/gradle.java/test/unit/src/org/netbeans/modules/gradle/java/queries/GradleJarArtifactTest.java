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
package org.netbeans.modules.gradle.java.queries;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery.ArtifactsResult;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class GradleJarArtifactTest extends NbTestCase {

    public GradleJarArtifactTest(String name) {
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
    
    public void testSimpleProject() throws Exception {
        Project p = makeProject("dependencies/simple1");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs);
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("simple1", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertNull(art.getClassifier());
    }
    
    public void testPickClassesAsDefault() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs);
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("withTests", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertNull(art.getClassifier());
    }

    public void testSelectTestsAndSources() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_TESTS, null));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs);
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("withTests", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertEquals("tests", art.getClassifier());
        
        res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_TEST_SOURCES, null));
        specs = res.getArtifacts();
        art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("withTests", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertEquals("test-sources", art.getClassifier());
    }
    
    /**
     * Checks that all source, javadoc, main jars are enumerated for classifier any.
     * @throws Exception 
     */
    public void testProvideAllArtifacts() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        assertEquals(3, specs.size());
        int found = 0;
        int foundSources = 0;
        int foundTestSources = 0;
        
        for (ArtifactSpec a : specs) {
            assertEquals("withTests", a.getArtifactId());
            assertEquals("", a.getGroupId());
            if (null == a.getClassifier()) {
                found++;
            } else if ("tests".equals(a.getClassifier())) {
                foundSources++;
            } else if ("test-sources".equals(a.getClassifier())) {
                foundTestSources++;
            }
            URI loc = a.getLocation();
            assertNotNull(loc);
            String path = loc.getPath();
            int slash = path.lastIndexOf('/');
            String name = path.substring(slash + 1);
            assertEquals("withTests" + (a.getClassifier() == null ? "" : "-" + a.getClassifier()) + ".jar", name);
        }
        assertEquals(1, found);
        assertEquals(1, foundSources);
        assertEquals(1, foundTestSources);
    }
    
    public void testNoJarsInDifferentType() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery("executable", ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        assertEquals(Collections.emptyList(), specs);
    }
    
    /**
     * Checks that if a task is not included in the build sequence, its artifact is not reported. Here
     * the 'testSources' task is included in the complete build, but not in 'test' task predecessors.
     */
    public void testNotIncludedTaskArtifactMissing() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, 
                    ProjectActionContext.newBuilder(p).forProjectAction(ActionProvider.COMMAND_TEST).context()
        ));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        // only testJar is explicitly built
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("withTests", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertEquals("tests", art.getClassifier());
    }

    /**
     * In the 'assemble' sequence, jar is present, but the 'testJar' is not.
     */
    public void testNotIncludedTaskArtifactMissing2() throws Exception {
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, 
                    ProjectActionContext.newBuilder(p).forProjectAction("assemble").context()
        ));
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        // only testJar is explicitly built
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("withTests", art.getArtifactId());
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertNull(art.getClassifier());
    }
    
    /**
     * Checks that when the buildscript changes, the project eventually reloads and the
     * computed result fires and changes contents.
     * @throws Exception 
     */
    public void testGradleBuildchanged() throws Exception {
        clearWorkDir();
        Project p = makeProject("artifacts/withTests");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_SOURCES, 
                    ProjectActionContext.newBuilder(p).forProjectAction("assemble").context()
        ));
        AtomicInteger numberOfEvents = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        res.addChangeListener((e) -> {
            numberOfEvents.incrementAndGet();
            latch.countDown();
        });
        
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        // only testJar is explicitly built
        assertEquals(0, specs.size());
        
        NbGradleProject project = NbGradleProject.get(p);
        File buildscript = project.getGradleFiles().getBuildScript();
        Thread.sleep(2000);
        Files.write(buildscript.toPath(), 
                Arrays.asList(
                    "",
                    "",
                    "task sourcesJar(type: Jar) {",
                    "    classifier = 'sources'",
                    "    from sourceSets.main.allSource",
                    "}",
                    "assemble.dependsOn sourcesJar"
                ),
                StandardOpenOption.APPEND);
        // force refresh - project is not opened.
        project.toQuality("refresh", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, numberOfEvents.get());
    }
    
    /**
     * The regular JAR should be still reported, even though the shadow jar is used, unless
     * a specific instruction requests the shadow.
     */
    public void testNormalJarWithShadowUsed() throws Exception {
        clearWorkDir();
        Project p = makeProject("artifacts/shadowed");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null)
        );
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        // only testJar is explicitly built
        assertEquals(1, specs.size());
        
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("shadowed", art.getArtifactId());
        assertTrue(art.hasTag(ArtifactSpec.TAG_BASE));
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertNull(art.getClassifier());
    }
    
    public void testShadowedJar() throws Exception {
        clearWorkDir();
        Project p = makeProject("artifacts/shadowed");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery("jar", null, null, ArtifactSpec.TAG_SHADED)
        );
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        // only testJar is explicitly built
        assertEquals(1, specs.size());
 
        ArtifactSpec art = specs.get(0);
        assertEquals("jar", art.getType());
        assertEquals("shadowed", art.getArtifactId());
        assertTrue(art.hasTag(ArtifactSpec.TAG_SHADED));
        assertEquals("", art.getGroupId());
        assertNull(art.getVersionSpec());
        assertEquals("all", art.getClassifier());
    }
    
    public void testNormalAndShadowedJars1() throws Exception {
        clearWorkDir();
        Project p = makeProject("artifacts/shadowed");
        ArtifactsResult res = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null)
        );
        List<ArtifactSpec> specs = res.getArtifacts();
        assertNotNull(specs); 
        
        assertEquals(4, specs.size());
        
        ArtifactSpec art = specs.stream().filter(a -> a.getClassifier() == null).findFirst().orElse(null);
        assertNotNull(art);
        assertEquals("jar", art.getType());
        assertEquals("shadowed", art.getArtifactId());
        assertTrue(art.hasTag(ArtifactSpec.TAG_BASE));

        art = specs.stream().filter(a -> "all".equals(a.getClassifier())).findFirst().orElse(null);
        assertEquals("jar", art.getType());
        assertEquals("shadowed", art.getArtifactId());
        assertTrue(art.hasTag(ArtifactSpec.TAG_SHADED));
    }
}

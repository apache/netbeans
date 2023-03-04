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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Exchanger;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MavenArtifactsImplementationTest extends NbTestCase {
    private FileObject d;
    private File repo;
    private FileObject repoFO;
    private FileObject dataFO;

    public MavenArtifactsImplementationTest(String name) {
        super(name);
    }

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "true");
        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        repoFO = FileUtil.toFileObject(repo);
        dataFO = FileUtil.toFileObject(getDataDir());
        
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
    }
    
    private void installCompileResources() throws Exception {
        FileUtil.copyFile(dataFO.getFileObject("projects/dependencies/repo"), repoFO, "nbtest");
    }

    @Override
    protected void tearDown() throws Exception {
        FileObject nbtest = repoFO.getFileObject("nbtest");
        if (nbtest != null && nbtest.isValid()) {
            nbtest.delete();
        }
    }
    
    /**
     * Checks that the project can report its output artifact even though
     * it was not built yet.
     */
    public void testProjectArtifactWithoutBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec);
        URI u = spec.getLocation();
        assertNotNull(u);
        assertNull(spec.getLocalFile());
    }
    
    public void testProjectArtifactSpecificType() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p,
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR)
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec);
        URI u = spec.getLocation();
        assertNotNull(u);
        assertNull(spec.getLocalFile());
    }

    /**
     * Checks that a compiled project will report its artifact including the  local file.
     * @throws Exception 
     */
    public void testCompileProjectArtifactWithFile() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        Exchanger<Boolean> status = new Exchanger<>();
        
        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.fixed(new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                try {
                    status.exchange(success);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }));
        
        Boolean s = status.exchange(true);
        assertTrue(s);
        // the project has been built
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec.getLocation());
        assertNotNull(spec.getLocalFile());
        
        assertEquals(spec.getLocation(), URLMapper.findURL(spec.getLocalFile(), URLMapper.EXTERNAL).toURI());
    }
    
    
    public void testArtifactTypeNotProvided() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_EAR)
        );
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }

    public void testArtifactClassifierNotProvided() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, "tests", null)
        );
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }
    
    public void testArtifactForCompileAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_BUILD)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }

    public void testArtifactForRebuildAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_REBUILD)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }
    
    public void testArtifactForRunAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_RUN)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }
    
    public void testArtifactForCleanAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_CLEAN)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }

    /**
     * Checks that the default artifact for shaded plugin does not change, but is annotated
     * by an appropriate tag.
     */
    public void testShadedDefault() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-default/");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-default");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR)
        );
        
        assertNotNull(ar);
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        
        ArtifactSpec uberJar = specs.get(0);
        assertReplacementUberjar(uberJar);
    }
    
    /**
     * Checks that the original jar can be queried, but has no GAV.
     */
    public void testShadedDefaultOriginal() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-default/");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-default");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, null, ArtifactSpec.TAG_BASE)
        );
        
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec origJar = specs.get(0);
        assertNotAttachedOriginal(origJar);
    }
    
    private void assertReplacementUberjar(ArtifactSpec uberJar) {
        assertReplacementUberjar(uberJar, "shaded-default");
    }
    
    private void assertReplacementUberjar(ArtifactSpec uberJar, String prjName) {
        assertEquals("Uber-jar has a proper artifactId", prjName, uberJar.getArtifactId());
        assertEquals("Uber-jar has a proper groupId", "nbtest.grp", uberJar.getGroupId());
        assertNull("Uber-jar should have no classifier", uberJar.getClassifier());
        assertTrue(uberJar.hasTag("<shaded>"));
        assertFalse(uberJar.hasTag(ArtifactSpec.TAG_BASE));
    }
    
    void assertNotAttachedOriginal(ArtifactSpec origJar) {
        assertNull("Orig-jar has no artifactId", origJar.getArtifactId());
        assertNull("Orig-jar has no groupId", origJar.getGroupId());
        assertNull("Orig-jar should have no classifier", origJar.getClassifier());
        assertTrue(origJar.hasTag(ArtifactSpec.TAG_BASE));
        assertFalse(origJar.hasTag("<shaded>"));
    }
    
    /**
     * Checks that 'any classifier' produces all jars, original and shaded.
     */
    public void testShadedAllCodeJars() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-default/");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-default");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(2, specs.size());
        
        boolean shadedFound = false;
        boolean origFound = false;
        for (ArtifactSpec spec : specs) {
            if (spec.hasTag(ArtifactSpec.TAG_BASE)) {
                assertFalse("Single base artifact expected", origFound);
                origFound = true;
                assertNotAttachedOriginal(spec);
            } else if (spec.hasTag(ArtifactSpec.TAG_SHADED)) {
                assertFalse("Single shaded artifact expected", shadedFound);
                shadedFound = true;
                assertReplacementUberjar(spec);
            } else {
                fail("Artifact should be either base or shaded");
            }
        }
    }
    
    /**
     * Checks that for attached shaded artifact, the default output is not affected and has no tag.
     */
    public void testShadedAttachedDefaultOutput() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-attached");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-attached");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertDefaultArtifactWithAttached(out);
    }

    /**
     * Checks that for attached shaded artifact, the default output is not affected and has no tag.
     */
    public void testShadedAttachedClassifiedOutput() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-attached");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-attached");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, "shaded", null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedUberjar(out, "shaded");
    }
    
    /**
     * Checks that for attached shaded artifact, the default output is not affected and has no tag.
     */
    public void testShadedAttachedClassifiedOriginalOutput() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-attached");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-attached");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, "shaded", null, ArtifactSpec.TAG_BASE)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedOriginal(out, "shaded");
    }
    
    private void assertDefaultArtifactWithAttached(ArtifactSpec out) {
        assertEquals("Output has a proper artifactId", "shaded-attached", out.getArtifactId());
        assertEquals("Output has a proper groupId", "nbtest.grp", out.getGroupId());
        assertNull("Output has no classifier", out.getClassifier());
        assertFalse(out.hasTag("<shaded>"));
        assertFalse(out.hasTag(ArtifactSpec.TAG_BASE));
    }

    private void assertAttachedUberjar(ArtifactSpec uberJar, String classifier) {
        assertEquals("Uber-jar has a proper artifactId", "shaded-attached", uberJar.getArtifactId());
        assertEquals("Uber-jar has a proper groupId", "nbtest.grp", uberJar.getGroupId());
        assertEquals("Uber-jar has a classifier", classifier, uberJar.getClassifier());
        assertTrue(uberJar.hasTag("<shaded>"));
        assertFalse(uberJar.hasTag(ArtifactSpec.TAG_BASE));
    }
    
    void assertAttachedOriginal(ArtifactSpec origJar, String classifier) {
        assertEquals("Orig-jar has an artifactId", "shaded-attached", origJar.getArtifactId());
        assertEquals("Orig-jar has a groupId", "nbtest.grp", origJar.getGroupId());
        assertEquals("Orig-jar has a classifier", classifier, origJar.getClassifier());
        assertTrue(origJar.hasTag(ArtifactSpec.TAG_BASE));
        assertFalse(origJar.hasTag(ArtifactSpec.TAG_SHADED));
    }
    
    /**
     * Checks that for attached shaded artifact, the default output is not affected and has no tag.
     */
    public void testShadedAttachedClassifiedAllOutput() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-attached");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-attached");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(3, specs.size());
        for (ArtifactSpec out : specs) {
            if (out.hasTag(ArtifactSpec.TAG_BASE)) {
                assertAttachedOriginal(out, "shaded");
            } else if (out.hasTag(ArtifactSpec.TAG_SHADED)) {
                assertAttachedUberjar(out, "shaded");
            } else {
                assertDefaultArtifactWithAttached(out);
            }
        }
    }
    
    FileObject prjCopy;
    
    private void assertAttachedClassifiedArtifact(String prjName, ArtifactSpec out, String classifier, String type) {
        assertEquals("Output has a proper artifactId", prjName, out.getArtifactId());
        assertEquals("Output has a proper groupId", "nbtest.grp", out.getGroupId());
        assertEquals("Output has a classifier", classifier, out.getClassifier());
        assertEquals("Output has a type", type, out.getType());
        assertFalse(out.hasTag("<shaded>"));
        assertFalse(out.hasTag(ArtifactSpec.TAG_BASE));
        
        String suffix = classifier == null ? "" : "-" + classifier;
        
        URI expected = FileUtil.toFile(prjCopy).toPath().resolve(Paths.get("target", prjName + "-16" + suffix + ".jar")).toUri();
        assertEquals(expected, out.getLocation());
    }
    
    boolean attached;

    private void assertShadedAttachedClassifiedArtifact(String prjName, ArtifactSpec out, String classifier, String type, Boolean shaded) {
        if (Boolean.TRUE == shaded) {
            assertTrue(out.hasTag("<shaded>"));
            assertFalse(out.hasTag(ArtifactSpec.TAG_BASE));
            if (attached) {
                classifier = classifier + "-shaded";
            }
        } else if (Boolean.FALSE == shaded) {
            assertFalse(out.hasTag(ArtifactSpec.TAG_SHADED));
            assertTrue(out.hasTag(ArtifactSpec.TAG_BASE));
            prjName = "original-" + prjName;
        } else {
            assertFalse(out.hasTag(ArtifactSpec.TAG_SHADED));
            assertFalse(out.hasTag(ArtifactSpec.TAG_BASE));
        }
        if (attached || Boolean.FALSE != shaded) {
            assertEquals("Output has a proper artifactId", prjName, out.getArtifactId());
            assertEquals("Output has a proper groupId", "nbtest.grp", out.getGroupId());
        }
        assertEquals("Output has a classifier", classifier, out.getClassifier());
        assertEquals("Output has a type", type, out.getType());
        
        URI expected = FileUtil.toFile(prjCopy).toPath().resolve(Paths.get("target", prjName + "-16-" + classifier + ".jar")).toUri();
        assertEquals(expected, out.getLocation());
    }
    
    public void testSourceAttachment() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_SOURCES, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedClassifiedArtifact("sources", out, "sources", "sources");
    }

    public void testTestsAttachment() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_TESTS, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedClassifiedArtifact("sources", out, "tests", "test-jar");
    }

    public void testTestsSourcesAttachment() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_TEST_SOURCES, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedClassifiedArtifact("sources", out, "test-sources", "sources");
    }
    
    public void testDefaultAllAttachments() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(4, specs.size());
        for (ArtifactSpec out : specs) {
            if (ArtifactSpec.CLASSIFIER_SOURCES.equals(out.getClassifier())) {
                assertAttachedClassifiedArtifact("sources", out, "sources", "sources");
                assertFalse(out.hasTag("test"));
            } else if (ArtifactSpec.CLASSIFIER_TESTS.equals(out.getClassifier())) {
                assertAttachedClassifiedArtifact("sources", out, "tests", "test-jar");
            } else if (ArtifactSpec.CLASSIFIER_TEST_SOURCES.equals(out.getClassifier())) {
                assertAttachedClassifiedArtifact("sources", out, "test-sources", "sources");
                assertTrue(out.hasTag("test"));
            } else {
                assertAttachedClassifiedArtifact("sources", out, null, "jar");
            }
        }
    }

    public void testShadedSourceAttachment() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ArtifactSpec.CLASSIFIER_SOURCES, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(1, specs.size());
        ArtifactSpec out = specs.get(0);
        assertAttachedClassifiedArtifact("shaded-sources", out, "sources", "sources");
        assertSame(prjCopy.getFileObject("target/shaded-sources-16-sources.jar"), out.getLocalFile());
    }

    public void testShadedAllAttachments() throws Exception {
        FileObject testApp = dataFO.getFileObject("artifacts/shaded-sources");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "shaded-sources");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, ProjectArtifactsQuery.Filter.CLASSIFIER_ANY, null)
        );
        List<ArtifactSpec> specs = ar.getArtifacts();
        assertEquals(8, specs.size());
        for (ArtifactSpec out : specs) {
            Boolean b;
            if (out.hasTag(ArtifactSpec.TAG_SHADED)) {
                b = true;
            } else if (out.hasTag(ArtifactSpec.TAG_BASE)) {
                b = false;
            } else {
                fail("Only base and tagged artifacts expected");
                return; // not reached
            }
            if (ArtifactSpec.CLASSIFIER_SOURCES.equals(out.getClassifier())) {
                assertShadedAttachedClassifiedArtifact("shaded-sources", out, "sources", "sources", b);
                assertFalse(out.hasTag("test"));
            } else if (ArtifactSpec.CLASSIFIER_TESTS.equals(out.getClassifier())) {
                assertShadedAttachedClassifiedArtifact("shaded-sources", out, "tests", "test-jar", b);
                assertTrue(out.hasTag("test"));
            } else if (ArtifactSpec.CLASSIFIER_TEST_SOURCES.equals(out.getClassifier())) {
                assertShadedAttachedClassifiedArtifact("shaded-sources", out, "test-sources", "sources", b);
                assertTrue(out.hasTag("test"));
            } else {
                if (Boolean.TRUE == b) {
                    assertReplacementUberjar(out, "shaded-sources");
                } else if (Boolean.FALSE == b) {
                    assertNotAttachedOriginal(out);
                }
            }
        }
    }
}

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
package org.netbeans.modules.gradle.dependencies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.GradleDependency.ModuleDependency;
import static org.netbeans.modules.gradle.api.GradleDependency.Type.MODULE;
import static org.netbeans.modules.gradle.api.GradleDependency.Type.UNRESOLVED;
import org.netbeans.modules.gradle.api.GradleDependency.UnresolvedDependency;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectModificationResult;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class DependencyModifierImplTest extends NbTestCase {
    FileObject projectDir;
    File destDirF;
    
    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
    }

   public DependencyModifierImplTest(String name) {
        super(name);
    }

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private Project project;
    
    private Project makeProject(String subdir) throws Exception {
        return makeProject(subdir, null);
    }
    
    private Project makeProject(String subdir, String alternateBuildscript) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        projectDir = FileUtil.copyFile(src, FileUtil.toFileObject(getWorkDir()), src.getNameExt());
        if (alternateBuildscript != null) {
            FileObject fo = src.getFileObject(alternateBuildscript);
            FileObject target = projectDir.getFileObject("build.gradle");
            if (target != null) {
                target.delete();
            }
            fo.copy(projectDir, "build", "gradle");
        }
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        ProjectTrust.getDefault().trustProject(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();
        
        NbGradleProject.get(p).toQuality("Load data", NbGradleProject.Quality.FULL, false).toCompletableFuture().get();
        this.project = p;
        return p;
    }
    
    private void assertDependencyAddFails(String msg, ArtifactSpec art) throws Exception {
        Dependency toAdd = Dependency.make(art, Scopes.COMPILE);
        try {
            DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                    dependency(toAdd).
                    create();
            ProjectDependencies.modifyDependencies(project, change);
            fail(msg);
        } catch (DependencyChangeException ex) {
            assertEquals(DependencyChangeException.Reason.CONFLICT, ex.getReason());
        }        
    }
    
    public void testDependencyConflict() throws Exception {
        Project p = makeProject("projects/micronaut");
        assertDependencyAddFails("Versionless artifacts should cause conflict", ArtifactSpec.make("io.micronaut", "micronaut-http-client"));
        assertDependencyAddFails("Versioned artifact added on top of versionless should cause conflict", ArtifactSpec.make("io.micronaut", "micronaut-http-client", "1.0"));
        assertDependencyAddFails("Versionless artifact added on top of versioned should conflict", ArtifactSpec.make("org.apache.logging.log4j", "log4j-core"));
        assertDependencyAddFails("Different versions should conflict", ArtifactSpec.make("org.apache.logging.log4j", "log4j-core", "2.17.1"));
    }
    
    private void assertNoChange(String reason, ArtifactSpec spec) throws Exception {
        Dependency toAdd = Dependency.make(spec, Scopes.COMPILE);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                option(DependencyChange.Options.skipConflicts).
                create();
        ProjectModificationResult res = ProjectDependencies.modifyDependencies(project, change);
        assertNotNull(res);
        assertTrue(reason, res.getWorkspaceEdit().getDocumentChanges().isEmpty());
    }
    
    public void testMatchingDependencySkips() throws Exception {
        makeProject("projects/micronaut");
        assertNoChange("Exactly matching artifact should be no-op", ArtifactSpec.make("io.micronaut", "micronaut-http-client"));
    }
    
    public void testAddUnknownScopeFails() throws Exception {
        Project p = makeProject("projects/micronaut");
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-http-client2");
        Dependency toAdd = Dependency.make(art, Scope.named("microfiber"));
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        try {
            ProjectModificationResult res = ProjectDependencies.modifyDependencies(p, change);
            fail("Should have failed because of unknown scope");
        } catch (DependencyChangeException ex) {
            assertEquals(DependencyChangeException.Reason.MALFORMED, ex.getReason());
            assertSame(toAdd, ex.getFailedDependencies().iterator().next());
        }
    }
    
    public void testAbstractScopesMappedToGradle() throws Exception {
        Project p = makeProject("projects/micronaut");
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-http-client2");
        Dependency toAdd;
        DependencyChange change;

        List<Scope> scopes = Arrays.asList(
                Scopes.COMPILE,
                Scopes.PROCESS,
                Scopes.RUNTIME,
                Scopes.TEST,
                Scopes.TEST_COMPILE,
                Scopes.TEST_RUNTIME,
                Scopes.EXTERNAL
        );
        
        for (Scope s :scopes) {
            toAdd = Dependency.make(art, s);
            change = DependencyChange.builder(DependencyChange.Kind.ADD).
                    dependency(toAdd).
                    create();
            try {
                ProjectModificationResult res = ProjectDependencies.modifyDependencies(p, change);
            } catch (DependencyChangeException ex) {
                fail("Failed to add dependency for scope: " + s);
            }
        }
    }
    
    public void testAddGenericCompilation() throws Exception {
        Project p = makeProject("projects/micronaut");
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-http-client2");
        Dependency toAdd = Dependency.make(art, Scopes.COMPILE);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        ProjectModificationResult res = ProjectDependencies.modifyDependencies(p, change);
        
        assertTrue("No new files should be created", res.getNewFiles().isEmpty());
        assertEquals("A single file should be changed", 1, res.getWorkspaceEdit().getDocumentChanges().size());
        List<TextEdit> edits = res.getWorkspaceEdit().getDocumentChanges().get(0).first().getEdits();
        assertEquals("A single file should be changed", 1, edits.size());
        String text = edits.get(0).getNewText();
        
        assertTrue(text.contains("implementation("));
        
        
        toAdd = Dependency.make(art, Scope.named("implementation"));
        change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        res = ProjectDependencies.modifyDependencies(p, change);
        assertEquals("A single file should be changed", 1, res.getWorkspaceEdit().getDocumentChanges().size());
        edits = res.getWorkspaceEdit().getDocumentChanges().get(0).first().getEdits();
        assertEquals("A single file should be changed", 1, edits.size());
        text = edits.get(0).getNewText();
        assertTrue(text.contains("implementation("));
    }
    
    /**
     * Checks generation of the "dependency" block, if there's not one.
     * @throws Exception 
     */
    public void testValidAddFirstDependency() throws Exception {
        makeProject("projects/micronaut", "build3.gradle");
        assertAddRouterValid();
    }
    
    /**
     * Checks that the generation succeeds after simple-style dependency like "implementation 'foo:bar:ver'"
     * @throws Exception 
     */
    public void testValidAfterSingleDependency() throws Exception {
        makeProject("projects/micronaut");
        assertAddRouterValid();
    }
    
    private void executeChangeAndWait(DependencyChange change) throws Exception {
        Project p = this.project;
        ProjectModificationResult res = ProjectDependencies.modifyDependencies(p, change);
        NbGradleProject gp = NbGradleProject.get(project);
        CountDownLatch l = new CountDownLatch(1);
        gp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    l.countDown();
                }
            }
        });
        res.commit();
        l.await(10, TimeUnit.SECONDS);
        gp = NbGradleProject.get(p);
        assertTrue("Should successfuly load the project", gp.getQuality().atLeast(NbGradleProject.Quality.FULL));
    }
    
    private void assertContainsArtifacts(boolean shouldContainDirect, String configuration, ArtifactSpec... artifacts) throws Exception {
        Project p = this.project;
        List<String> artifactIds = Arrays.asList(artifacts).stream().map(a -> a.getGroupId() + ":" + a.getArtifactId()).collect(Collectors.toList());
        List<String> matches = new ArrayList<>(artifactIds);
        List<String> found = new ArrayList<>();
        
        GradleBaseProject gbp = GradleBaseProject.get(p);
        GradleConfiguration cfg = gbp.getConfigurations().get(configuration);
        Collection<? extends GradleDependency> deps = cfg.getConfiguredDependencies();

        F: for (GradleDependency d  : deps) {
            String id;
            
            switch (d.getType()) {
                case MODULE:
                case UNRESOLVED:
                    id = d.getId();
                    break;
                default:
                    // skip the rest of the cycle
                    continue;
            }
            String[] split = id.split(":");
            String ga = split[0] + ":" + split[1];
            artifactIds.remove(ga);
            found.add(ga);
        }
        found.retainAll(Arrays.asList(matches));
        
        if (shouldContainDirect) {
            assertTrue("Must contain " + artifactIds, artifactIds.isEmpty());
        } else {
            assertTrue("Must not contain " + artifactIds, found.isEmpty());
        }
    }
    
    private void assertContainsDependency(boolean containsInScope, Scope scope, ArtifactSpec... artifacts) throws Exception {
        Project p = this.project;
        List<String> artifactIds = Arrays.asList(artifacts).stream().map(a -> a.getGroupId() + ":" + a.getArtifactId()).collect(Collectors.toList());
        List<String> matches = new ArrayList<>(artifactIds);
        Set<String> found = new LinkedHashSet<>();
        
        DependencyResult r = ProjectDependencies.findDependencies(project, ProjectDependencies.newBuilder().scope(scope).build());
        
        Deque<Dependency> toProcess = new ArrayDeque<>();
        toProcess.add(r.getRoot());
        while (!toProcess.isEmpty()) {
            Dependency d = toProcess.poll();
            toProcess.addAll(d.getChildren());
            
            String ga = d.getArtifact().getGroupId() + ":" + d.getArtifact().getArtifactId();
            artifactIds.remove(ga);
            found.add(ga);
        }
        found.retainAll(matches);
        if (containsInScope) {
            assertTrue("Must contain " + artifactIds, artifactIds.isEmpty());
        } else {
            assertTrue("Must not contain " + artifactIds, found.isEmpty());
        }
        
        if (scope == null) {
            return;
        }
    }
    
    void assertAddRouterValid() throws Exception {
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-tracing");
        Dependency toAdd = Dependency.make(art, Scopes.COMPILE);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        executeChangeAndWait(change);
        assertContainsArtifacts(true, "implementation", art);
        assertContainsDependency(true, Scopes.COMPILE, art);
        assertContainsDependency(true, Scopes.RUNTIME, art);
        assertContainsDependency(false, Scopes.PROCESS, art);
    }
    
    
    /**
     * Checks that the generation succeeds when dependency is generated after a dependency block
     * 
     * @throws Exception 
     */
    public void testValidAfterDependencyBlock() throws Exception {
        makeProject("projects/micronaut", "build2.gradle");
        assertAddRouterValid();
    }
    
    public void testInsertMultipleDependencies() throws Exception {
        makeProject("projects/micronaut", "build2.gradle");
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-messaging");
        ArtifactSpec art2 = ArtifactSpec.make("io.micronaut", "micronaut-tracing");
        Dependency toAdd = Dependency.make(art, Scopes.RUNTIME);
        Dependency toAdd2 = Dependency.make(art2, Scopes.RUNTIME);
        
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd, toAdd2).
                create();
        executeChangeAndWait(change);
        // artifacts are present in the buildfile / appropriate configuration
        assertContainsArtifacts(true, "runtimeOnly", art, art2);
        
        // dependency is listed for runtime
        assertContainsDependency(true, Scopes.RUNTIME, art);
        // dependency is NOT listed for compile
        assertContainsDependency(false, Scopes.COMPILE, art);
    }
}

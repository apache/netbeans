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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class GradleDependenciesImplementationTest extends NbTestCase {
    FileObject projectDir;
    File destDirF;
    
    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
    }

   public GradleDependenciesImplementationTest(String name) {
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
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        
        assertNull("Project has no group or version, no artifact should be given", r.getRoot().getArtifact());
        assertNotNull("Project specification must be present", r.getRoot().getProject());
        assertSame("Project location is reported", projectDir, r.getRoot().getProject().getLocation());
        assertSame("Project is passed as project data - internal", p, r.getRoot().getProjectData());
    }
    
    public void testSimpleWithGav() throws Exception {
        Project p = makeProject("dependencies/simple2");
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        
        ArtifactSpec a = r.getRoot().getArtifact();
        assertNotNull("Project has GAV, should be manifested as artifact", a);
        GradleBaseProject gbp = GradleBaseProject.get(p);
        assertEquals(gbp.getName(), a.getArtifactId());
        assertEquals(gbp.getGroup(), a.getGroupId());
        assertEquals(gbp.getVersion(), a.getVersionSpec());
        assertNotNull("Project specification must be present", r.getRoot().getProject());
        assertSame("Project location is reported", projectDir, r.getRoot().getProject().getLocation());
        assertSame("Project is passed as project data - internal", p, r.getRoot().getProjectData());
    }
    
    public void testDependencyNoDependencies() throws Exception {
        Project p = makeProject("dependencies/simple2");
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        SourceLocation loc = r.getDeclarationRange(null, DependencyResult.PART_CONTAINER);
        assertNull(loc);
        
        loc = r.getDeclarationRange(r.getRoot(), DependencyResult.PART_CONTAINER);
        assertNull(loc);
    }
    
    public void testDependencyBlockRange() throws Exception {
        Project p = makeProject("dependencies/micronaut");
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        
        SourceLocation loc = r.getDeclarationRange(null, DependencyResult.PART_CONTAINER);
        assertNotNull(loc);
        
        loc = r.getDeclarationRange(r.getRoot(), DependencyResult.PART_CONTAINER);
        assertNotNull(loc);
      
        FileObject buildGradle = projectDir.getFileObject("build.gradle");
        EditorCookie cake = buildGradle.getLookup().lookup(EditorCookie.class);
        LineDocument doc = LineDocumentUtils.asRequired(cake.openDocument(), LineDocument.class);
        
        int start = loc.getStartOffset();
        int depStart = LineDocumentUtils.getLineFirstNonWhitespace(doc, start);
        int depEnd = LineDocumentUtils.getLineEnd(doc, start);
        
        String s = doc.getText(depStart, depEnd - depStart);
        assertEquals("dependencies {", s);
        
        int end = loc.getEndOffset();
        s = doc.getText(end - 1, 1);
        assertEquals("}", s);
    }
    
    public void testLocationOfBlockDependencyList() throws Exception {
        Project p = makeProject("dependencies/micronaut");
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        
        Dependency pin1 = r.getRoot().getChildren().stream().filter(d -> d.toString().contains("io.micronaut:micronaut-validation")).findAny().get();
        Dependency pin2 = r.getRoot().getChildren().stream().filter(d -> d.toString().contains("ch.qos.logback:logback-classic")).findAny().get();
        Dependency pin3 = r.getRoot().getChildren().stream().filter(d -> d.toString().contains("io.micronaut:micronaut-jackson-databind")).findAny().get();
        
        SourceLocation loc = r.getDeclarationRange(pin2, DependencyResult.PART_CONTAINER);
        assertNull(loc);
        
        loc = r.getDeclarationRange(pin3, DependencyResult.PART_CONTAINER);
        assertNull(loc);
        
        loc = r.getDeclarationRange(pin1, DependencyResult.PART_CONTAINER);
        assertNotNull(loc);
      
        FileObject buildGradle = projectDir.getFileObject("build.gradle");
        EditorCookie cake = buildGradle.getLookup().lookup(EditorCookie.class);
        LineDocument doc = LineDocumentUtils.asRequired(cake.openDocument(), LineDocument.class);
        
        int start = loc.getStartOffset();
        int depStart = LineDocumentUtils.getLineFirstNonWhitespace(doc, start);
        int depEnd = LineDocumentUtils.getLineEnd(doc, start);
        
        String s = doc.getText(depStart, depEnd - depStart);
        assertEquals("implementation(", s);
        
        int end = loc.getEndOffset();
        s = doc.getText(end - 1, 1);
        assertEquals(")", s);
    }
    
    public void testMicronautProject() throws Exception {
        Project p = makeProject("dependencies/micronaut");
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.RUNTIME)
        );
        assertNotNull("Dependency service is supported", r);
        
        assertEquals(9, r.getRoot().getChildren().size());
        Optional<Dependency> dep = r.getRoot().getChildren().stream().filter(d -> d.getArtifact().toString().equals("io.micronaut:micronaut-bom:3.6.0")).findFirst();
        assertTrue("Plugin - injected dependency should be present", dep.isPresent());
        
        SourceLocation srcLoc = r.getDeclarationRange(dep.get(), null);
        
        assertNull("Implied dependencies do not have source location(s) - yet!", srcLoc);

        dep = r.getRoot().getChildren().stream().filter(d -> d.getArtifact().toString().equals("org.apache.logging.log4j:log4j-core:2.17.0")).findFirst();
        assertTrue("Explicit dependency is present", dep.isPresent());
        
        srcLoc = r.getDeclarationRange(dep.get(), null);
        
        assertNotNull("Explicit dependency should have a location");
        assertNull("Explicit dependencies are not implied", srcLoc.getImpliedBy());
        
        // there are more paths to io.micronaut:micronaut-websocket:3.6.0; some of them are through an explicit dependency,
        // some through an injected one, micronaut-bom, which does not have any SourceLocation atm.
        List<Dependency> deps = r.getRoot().getChildren().stream().flatMap(d -> d.getChildren().stream()).filter(d -> 
                d.getArtifact().toString().equals("io.micronaut:micronaut-websocket:3.6.0")).collect(Collectors.toList());
        assertFalse("Implied dependency is present", deps.isEmpty());
        
        Dependency rd = null;
        for (Dependency d : deps) {
            srcLoc = r.getDeclarationRange(d, null);
            if (srcLoc != null) {
                assertNotNull("4th party artifact should not have null location", srcLoc);
                assertNotNull("4th party artifacts should report 'implied'", srcLoc.getImpliedBy());
                for (rd = d.getParent(); rd.getParent() != r.getRoot(); rd = rd.getParent() ) ;
                break;
            }
        }
        assertNotNull("Implied dependency should have a root dep", rd);
        assertSame(rd, srcLoc.getImpliedBy());
    }
    
    private void assertContainsDependency(List<Dependency> deps, String groupAndArtifact) {
        for (Dependency d : deps) {
            ArtifactSpec a = d.getArtifact();
            if (a != null) {
                String ga = a.getGroupId() + ":" + a.getArtifactId();
                if (groupAndArtifact.equals(ga)) {
                    return;
                }
            }
        }
        fail("Artifact not found: " +  groupAndArtifact);
    }
    
    private static final List<String> ALL_DEPS =   Arrays.asList(
            "io.micronaut:micronaut-http-validation",
            "io.micronaut:micronaut-http-client",
            "io.micronaut:micronaut-jackson-databind",
            "jakarta.annotation:jakarta.annotation-api",
            "ch.qos.logback:logback-classic",
            "io.micronaut:micronaut-validation",
            "org.apache.logging.log4j:log4j-core"
    );
    
    public void testMicronautProjectDeclaredDependencies() throws Exception {
        Project p = makeProject("dependencies/micronaut");
 
        DependencyResult r = ProjectDependencies.findDependencies(p, 
            ProjectDependencies.newQuery(Scopes.DECLARED)
        );
        assertNotNull("Dependency service is supported", r);
        
        List<Dependency> deps = r.getRoot().getChildren();
        for (String d : ALL_DEPS) {
            assertContainsDependency(deps, d);
        }
        
        assertTrue("Contains versioned log4j dependency", deps.stream().filter(d -> d.getArtifact().toString().contains(".log4j:log4j-core:2.17.0")).findAny().isPresent());
    }
}

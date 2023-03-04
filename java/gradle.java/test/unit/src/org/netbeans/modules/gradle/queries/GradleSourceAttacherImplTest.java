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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Assume;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency.ModuleDependency;
import org.netbeans.modules.gradle.api.execute.GradleExecApiTrampoline;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class GradleSourceAttacherImplTest extends AbstractGradleProjectTestCase{
    private Locale defLocale;
    
    public GradleSourceAttacherImplTest(String name) {
        super(name);
        defLocale = Locale.getDefault();
    }

    @Override
    protected void tearDown() throws Exception {
        GradleSettings.getDefault().setGradleUserHome(null);
        Locale.setDefault(defLocale);
        super.tearDown();
    }
    
    /**
     * This test downloads org.netbeans.api:org-openide-util:RELEASE124 jar from the Maven Central repository.
     * A fresh Gradle user home in the test work directory is used, so that no source .jar is present of this
     * library. Then the test checks that Gradle is used to download the source artifact.
     */
    public void testSourceAttachActionInvokesGradle() throws Exception {
        clearWorkDir();
        Locale.setDefault(new Locale("DA"));
        
        Path wp = getWorkDir().toPath();
        Path gradleHome = Files.createDirectories(wp.resolve("gradle-home"));

        // change the gradle home. Need to change in GradleSettings as well,
        // since the cache artifacts are constructored based on GradleSettings,
        // not project connection.
        GradleExecApiTrampoline.setGradleHomeProvider(() -> gradleHome);
        GradleSettings.getDefault().setGradleUserHome(gradleHome.toFile());
        
        try (InputStream is = new URL("http://www.netbeans.org").openStream()) {
        } catch (IOException ex) {
            // bail out without failing the test suite, but leave a message in the test log.
            Assume.assumeTrue("This test needs connectivity", false);
        }


        FileObject fo = createGradleProject(
                "apply plugin: 'java'\n"
                        + "repositories {\n" +
                        "    mavenCentral()\n"
                       + "}\n"
                       + "\n"
                       + "dependencies {\n"
                       + "    implementation(\"org.netbeans.api:org-openide-util:RELEASE124\")\n"
                       + "}"
               );
        FileObject src = FileUtil.createFolder(fo, "src/main/java");
        FileObject java = src.createData("Whatever.java");
        Project prj = ProjectManager.getDefault().findProject(fo);
        assertNotNull(prj);
        ProjectTrust.getDefault().trustProject(prj);

        OpenProjects.getDefault().open(new Project[] { prj }, false);
        Project[] opened = OpenProjects.getDefault().openProjects().get();
        
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        
        
        class AP extends ActionProgress {
            final CountDownLatch l = new CountDownLatch(1);
            volatile boolean success;
            
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                this.success = success;
                l.countDown();
            }
        }
        
        
        AP prg1 = new AP();
        
        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.singleton(prg1));
        prg1.l.await();
        
        assertTrue("Build, including jar download succeeded - requires maven central access", prg1.success);

        GradleJavaProject gp = GradleJavaProject.get(prj);
        GradleBaseProject gbp = GradleBaseProject.get(prj);
        GradleConfiguration cfg = gbp.getConfigurations().get("compileClasspath");
        Set<ModuleDependency> dep = cfg.findModules("org.netbeans.api:org-openide-util:RELEASE124");
        assertNotNull(dep);
        assertEquals(1, dep.size());
        Set<File> files = dep.iterator().next().getArtifacts();
        assertNotNull(files);
        assertEquals(1, files.size());
        
        File utilJar = files.iterator().next();
        
        // now check that the javadoc attacher is applied:
        class L implements SourceJavadocAttacher.AttachmentListener {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            
            @Override
            public void attachmentSucceeded() {
                result.complete(true);
            }

            @Override
            public void attachmentFailed() {
                result.complete(false);
            }
        }
        
        // need to get the classpath root inside the archive.
        URL archiveRoot = FileUtil.getArchiveRoot(utilJar.toURI().toURL());
        L l = new L();
        SourceJavadocAttacher.attachSources(archiveRoot, l);
        // wait at most 20 seconds for a download.
        boolean r = l.result.get(20, TimeUnit.SECONDS);
        assertTrue(r);
    }
}

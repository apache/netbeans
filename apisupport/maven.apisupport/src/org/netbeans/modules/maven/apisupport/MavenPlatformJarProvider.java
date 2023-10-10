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

package org.netbeans.modules.maven.apisupport;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle.Messages;

@ProjectServiceProvider(service=PlatformJarProvider.class, projectType={
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM_APPLICATION
})
public class MavenPlatformJarProvider implements PlatformJarProvider {
    private final Project project;

    public MavenPlatformJarProvider(Project project) {
        this.project = project;
    }

    @Override public Set<File> getPlatformJars() throws IOException {
        NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
        if (nbmp == null) {
            return Collections.emptySet();
        }
        assert !EventQueue.isDispatchThread() : "should not be called from EQ";
        NbMavenProject app;
        if (nbmp.getPackagingType().equals(NbMavenProject.TYPE_NBM)) {
            Project parent = MavenNbModuleImpl.findAppProject(project);
            app = parent != null ? parent.getLookup().lookup(NbMavenProject.class) : null;
            if (app == null) { // #202946: standalone or suite component
                File ide = MavenNbModuleImpl.findIDEInstallation(project);
                return ide != null ? allModulesIn(ide) : Collections.<File>emptySet();
            }
        } else {
            app = nbmp;
        }
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        MavenProject mp = app.getMavenProject();
        List<Artifact> arts = new ArrayList<Artifact>();
        for (Artifact dep : mp.getArtifacts()) {
            String type = dep.getType();
            if ("jar".equals(type)) {
                // XXX how to eliminate non-module deps? does it matter?
                if (!dep.isSnapshot()) {
                    arts.add(dep);
                } // else a snapshot is probably from this "suite"... crude heuristic, rethink
            } else if ("nbm-file".equals(type)) { // usually via org.netbeans.cluster:*:*:pom
                arts.add(online.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), "jar"));
            }
        }
        try {
            download(arts, online, mp.getRemoteArtifactRepositories());
        } finally {
            Set<File> jars = new LinkedHashSet<File>();
            for (Artifact art : arts) {
                if (art.getFile() != null && art.getFile().exists()) {
                    jars.add(FileUtil.normalizeFile(art.getFile()));
                }
            }
            return jars;
        }
        // XXX as a fallback could use findPlatformFolder and just scan for $plaf/*/{lib,core,modules}/{,locale/}*.jar
    }

    @Messages("MavenPlatformJarProvider_downloading=Downloading NetBeans platform JARs")
    private void download(List<Artifact> arts, MavenEmbedder online, List<ArtifactRepository> remoteRepos) throws IOException {
        List<Artifact> toDownload = new ArrayList<Artifact>();
        for (Artifact art : arts) {
            File jar = art.getFile();
            if (jar == null || !jar.exists()) {
                toDownload.add(art);
            }
        }
        if (!toDownload.isEmpty()) {
            int cnt = 0;
            final AtomicBoolean canceled = new AtomicBoolean();
            ProgressHandle h = ProgressHandleFactory.createHandle(Bundle.MavenPlatformJarProvider_downloading(), new Cancellable() {
                @Override public boolean cancel() {
                    canceled.set(true);
                    return true;
                }
            });
            h.start(toDownload.size());
            try {
                for (Artifact art : toDownload) {
                    if (canceled.get()) {
                        throw new IOException("download canceled");
                    }
                    try {
                        online.resolveArtifact(art, remoteRepos, online.getLocalRepository());
                    } catch (ThreadDeath td) {
                    } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
                        if (!(ise.getCause() instanceof ThreadDeath)) {
                            throw ise;
                        }
                    } catch (AbstractArtifactResolutionException x) {
                        throw new IOException(x);
                    }
                    File jar = art.getFile();
                    if (jar == null || !jar.exists()) {
                        throw new IOException("failed to download " + art);
                    }
                    h.progress(art.toString(), ++cnt);
                }
            } finally {
                h.finish();
            }
        }
    }

    private Set<File> allModulesIn(File ide) {
        Set<File> jars = new HashSet<File>();
        File[] clusters = ide.listFiles();
        if (clusters != null) {
            for (File cluster : clusters) {
                File[] modules = new File(cluster, "modules").listFiles();
                if (modules != null) {
                    for (File module : modules) {
                        if (module.getName().endsWith(".jar")) {
                            jars.add(module);
                        }
                    }
                }
            }
        }
        return jars;
    }

}

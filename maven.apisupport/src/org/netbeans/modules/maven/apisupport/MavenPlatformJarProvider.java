/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                        online.resolve(art, remoteRepos, online.getLocalRepository());
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

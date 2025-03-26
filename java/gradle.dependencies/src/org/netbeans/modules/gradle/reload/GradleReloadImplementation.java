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
package org.netbeans.modules.gradle.reload;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import static org.netbeans.modules.project.dependency.ProjectReload.Quality.CONSISTENT;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Implementation of Project State + Reload API for Gradle projects.
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectReloadImplementation.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleReloadImplementation implements ProjectReloadImplementation {
    private static final Logger LOG = Logger.getLogger(GradleReloadImplementation.class.getName());
    
    private final Project project;
    private final PropertyChangeListener reloadL;

    private Reference<ProjectStateData> last = new WeakReference<>(null);

    
    public GradleReloadImplementation(Project project) {
        this.project = project;
        this.reloadL = (pch) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(pch.getPropertyName())) {
                LOG.log(Level.FINE, "Project {0} reloaded", project);
            }
        };
        NbGradleProject gp = NbGradleProject.get(project);
        gp.addPropertyChangeListener(WeakListeners.propertyChange(reloadL, gp));
    }
    
    // tests only
    public ProjectStateData getLastCachedData() {
        return last.get();
    }
    
    private static void includeFile(File f, Collection<FileObject> into) {
        if (f == null) {
            return;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            return;
        }
        into.add(fo);
    }

    private Set<FileObject> findProjectFiles(boolean forReload) {
        NbGradleProject nbgp = NbGradleProject.get(project);
        GradleFiles gf = nbgp.getGradleFiles();
        Set<FileObject> files = new HashSet<>();
        
        if (forReload) {
            gf.getProjectFiles().forEach(f -> includeFile(f, files));
            includeFile(gf.getFile(GradleFiles.Kind.ROOT_PROPERTIES), files);
            includeFile(gf.getFile(GradleFiles.Kind.USER_PROPERTIES), files);
            includeFile(gf.getFile(GradleFiles.Kind.ROOT_SCRIPT), files);
        } else {
            File f = gf.getSettingsScript();
            if (gf.isRootProject() || f.getParentFile().equals(gf.getProjectDir())) {
                includeFile(gf.getSettingsScript(), files);
            }
            includeFile(gf.getBuildScript(), files);
        }
        includeFile(gf.getFile(GradleFiles.Kind.PROJECT_PROPERTIES), files);
        return files;
    }

    public ProjectStateData getProjectData() {
        NbGradleProject nbgp = NbGradleProject.get(project);
        long time = nbgp.getEvaluateTime();
        
        synchronized (this) {
            ProjectStateData d = last.get();
            if (d != null && d.getTimestamp() == time) {
                return d;
            }
        }
        
        // PENDING: should also watch out for files that do not exist, but Gradle still looks for them.
        // For example, buildscript may be initially missing, but can be created which should make to ProjectState inconsistent.
        Collection<FileObject> files = findProjectFiles(true);
        NbGradleProject.Quality pq = nbgp.getQuality();
        Quality q;
        
        switch (pq) {
            case FALLBACK:
                q = Quality.FALLBACK;
                if (ProjectTrust.getDefault().isTrusted(project)) {
                    q = Quality.FALLBACK;
                } else {
                    q = Quality.UNTRUSTED;
                }
                break;
            case EVALUATED:
                q = Quality.BROKEN;
                break;
            case SIMPLE:
                q = Quality.SIMPLE;
                break;
            case FULL:
                q = Quality.LOADED;
                break;
            case FULL_ONLINE:
                q = Quality.RESOLVED;
                break;
            default:
                q = Quality.BROKEN;
                break;
        }
        ProjectStateData sd = ProjectStateData.builder(q).
                files(files).
                attachLookup(NbGradleProject.get(project).curretLookup()).
                timestamp(time).
                build();
        synchronized (this) {
            last = new WeakReference<>(sd);
        }
        return sd;
    }
    
    /**
     * Exception classes known to be thrown if an artifact cannot be downloaded. For Plugins, the resolution against
     * local repository fails, so we have to guess from the quality + "unknown plugin" information.
     */
    private static final String[] OFFLINE_EXCEPTION_CLASSES = {
        "org.gradle.api.plugins.UnknownPluginException",    // NOI18N
        "org.netbeans.modules.gradle.tooling.NeedOnlineModeException"   // NOI18N
    };
    
    /**
     * Process the known exception classes into match pattern at startup.
     */
    private static final Pattern OFFLINE_EXCEPTIONS_PATTERN = Pattern.compile(String.join("|", OFFLINE_EXCEPTION_CLASSES));
    
    @NbBundle.Messages({
        "# {0} - number of files",
        "ERROR_Project_Out_Of_Sync=Project files ({0}) are not saved",
        "# {0} - project name",
        "TEXT_RefreshProject=Reloading project {0}",
        "# {0} - project name",
        "# {1} - error message",
        "ERRROR_ProjectNotLoadable=Project {0} cannot be loaded: {1}",
        "# {0} - project name",
        "ERROR_NeedOnlineOperation=Could not resolve project {0} in offline mode."
    })
    @Override
    public CompletableFuture reload(Project project, StateRequest stateRequest, LoadContext context) {
        if (!(project instanceof NbGradleProjectImpl)) {
            return null;
        }
        NbGradleProjectImpl nbgp = (NbGradleProjectImpl)project;
        NbGradleProject.Quality aimQuality;
        CompletionStage<GradleProject> loadFuture;
        boolean offline = false;
        switch (stateRequest.getMinQuality()) {
            case NONE:
                // at least try :)
                if (stateRequest.getTargetQuality().isAtLeast(Quality.SIMPLE)) {
                    aimQuality = NbGradleProject.Quality.FALLBACK;
                    offline = true;
                    break;
                } else {
                    return CompletableFuture.completedFuture(getProjectData());
                }
            case FALLBACK:
                aimQuality = NbGradleProject.Quality.FALLBACK;
                break;
            case BROKEN:
                aimQuality = NbGradleProject.Quality.EVALUATED;
                break;
            case SIMPLE:
                aimQuality = NbGradleProject.Quality.SIMPLE;
                break;
            case LOADED:
                aimQuality = NbGradleProject.Quality.FULL;
                break;
            case CONSISTENT:
            case RESOLVED:
                aimQuality = stateRequest.isOfflineOperation() ? NbGradleProject.Quality.FULL : NbGradleProject.Quality.FULL_ONLINE;
                break;
            default:
                throw new AssertionError(stateRequest.getMinQuality().name());
            
        }
        if (stateRequest.isGrantTrust()) {
            ProjectTrust.getDefault().isTrustedPermanently(project);
        }
        LOG.log(Level.FINE, "Request to reload {0}: aimedQuality={1}, force={2}", new Object[] { project, aimQuality, stateRequest.isForceReload() });
        CompletableFuture<ProjectStateData> content = new CompletableFuture<>();
        
        loadFuture = nbgp.projectWithQualityTask(NbGradleProject.loadOptions(aimQuality).
            setDescription(stateRequest.getReason()).
            setForce(stateRequest.isForceReload()).
            setCheckFiles(stateRequest.isConsistent()).
            setOffline(stateRequest.isOfflineOperation() || offline)
        );
        loadFuture.thenAccept((p) -> {
            NbGradleProject gp = NbGradleProject.get(project);
            LOG.log(Level.FINE, "Project {0} reload complete, quality: {1}, time: {2}", new Object[] { project, gp.getQuality(), gp.getEvaluateTime() });
            boolean failed = gp.getQuality().worseThan(aimQuality);
            if (stateRequest.getMinQuality() == Quality.RESOLVED && gp.getQuality().atLeast(NbGradleProject.Quality.FULL)) {
                failed = false;
            }
            if (failed) {
                GradleBaseProject gbp = GradleBaseProject.get(project);
                boolean offlineError = gbp.getProblems().stream().anyMatch(prb -> OFFLINE_EXCEPTIONS_PATTERN.matcher(prb.getErrorClass()).find());
                
                // handle online error specially:
                if (offlineError) {
                    PartialLoadException partialLoad = new PartialLoadException(getProjectData(), Bundle.ERROR_NeedOnlineOperation(ProjectUtils.getInformation(project).getDisplayName()), 
                            new ProjectOperationException(project, ProjectOperationException.State.OFFLINE, Bundle.ERROR_NeedOnlineOperation(ProjectUtils.getInformation(project).getDisplayName())
                    ));
                    content.completeExceptionally(partialLoad);
                    return;
                }
            }
            content.complete(getProjectData());
        });
        
        return content;
    }
}

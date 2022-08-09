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
package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectArtifactsImplementation.class, projectType = NbMavenProject.TYPE)
public class MavenArtifactsImplementation implements ProjectArtifactsImplementation {
    private static final Logger LOG = Logger.getLogger(ProjectArtifactsImplementation.class.getName());
    
    private final Project project;

    public MavenArtifactsImplementation(Project project) {
        this.project = project;
    }
    @Override
    public Result findArtifacts(ProjectArtifactsQuery.Filter query) {
        ProjectActionContext ctx = query.getBuildContext();
        if (ctx != null) {
            if (ctx.getProjectAction() != null) {
                switch (ctx.getProjectAction()) {
                    case ActionProvider.COMMAND_BUILD:
                    case ActionProvider.COMMAND_REBUILD:
                    case ActionProvider.COMMAND_RUN:
                    case ActionProvider.COMMAND_DEBUG:
                    case ActionProvider.COMMAND_RUN_SINGLE:
                    case ActionProvider.COMMAND_DEBUG_SINGLE:
                    case ActionProvider.COMMAND_TEST:
                    case ActionProvider.COMMAND_TEST_SINGLE:
                    case ActionProvider.COMMAND_DEBUG_STEP_INTO:
                        break;

                    default:
                        return null;
                }
            }
            if (ctx.getProfiles() != null && !ctx.getProfiles().isEmpty()) {
                LOG.log(Level.WARNING, "Custom action profiles are not supported yet by Maven projects"); // NOI18N
            }
            if (ctx.getConfiguration() != null) {
                LOG.log(Level.WARNING, "Custom action configurations are not supported yet by Maven projects"); // NOI18N
            }
            if (ctx.getProperties() != null && !ctx.getProperties().isEmpty()) {
                LOG.log(Level.WARNING, "Custom action properties are not supported yet by Maven projects"); // NOI18N
            }
        }
        return new Res(project, query);
    }
    
    static class MavenQuery {
        final Project project;
        final NbMavenProject nbMavenProject;
        final ProjectArtifactsQuery.Filter filter;
        
        List<ArtifactSpec> specs;

        public MavenQuery(Project project, NbMavenProject nbMavenProject, ProjectArtifactsQuery.Filter filter) {
            this.project = project;
            this.nbMavenProject = nbMavenProject;
            this.filter = filter;
        }
        
        private void appendPluginOutput(MavenProject evalProject, String pluginId, String goal, String packagingAndType) {
            if (filter != null) {
                if (filter.getArtifactType() == null) {
                    if (!evalProject.getPackaging().equals(packagingAndType)) {
                        return;
                    }
                } else if (!filter.getArtifactType().equals(packagingAndType)) {
                    if (!ProjectArtifactsQuery.Filter.TYPE_ALL.equals(filter.getArtifactType())) {
                        return;
                    }
                }
            }
            Artifact mA = evalProject.getArtifact();
            Model mdl = evalProject.getModel();
            
            Plugin plugin = evalProject.getBuild().getPluginsAsMap().get(Constants.GROUP_APACHE_PLUGINS + ":" + pluginId); // NOI18N
            if (plugin == null) {
                return;
            }
            
            for (PluginExecution exec : plugin.getExecutions()) {
                if (exec.getGoals().contains(goal)) {
                    Xpp3Dom dom = evalProject.getGoalConfiguration(
                            Constants.GROUP_APACHE_PLUGINS, pluginId, exec.getId(), goal);
                    Xpp3Dom domClassifier = dom == null ? null : dom.getChild("classifier"); // NOI18N
                    Xpp3Dom domOutputDir = dom == null ? null : dom.getChild("outputDirectory"); // NOI18N

                    String classifier = domClassifier == null ? null : domClassifier.getValue();

                    if (filter != null && !ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(filter.getClassifier())) {
                        if (!Objects.equals(classifier, filter.getClassifier())) {
                            return;
                        }
                    }
                    StringBuilder finalNameExt = new StringBuilder(mdl.getBuild().getFinalName());
                    if (domClassifier != null) {
                        finalNameExt.append("-").append(domClassifier.getValue());
                    }
                    finalNameExt.append(".").append(packagingAndType);

                    ArtifactSpec.Builder builder = ArtifactSpec.builder(mA.getGroupId(), mA.getArtifactId(), mA.getVersion(), 
                            nbMavenProject.getMavenProject().getArtifact())
                            .classifier(classifier)
                            .type(packagingAndType);
                    try {
                        Path dir = Paths.get(mdl.getBuild().getDirectory());
                        if (domOutputDir != null) {
                            dir = mdl.getProjectDirectory().toPath().resolve(domOutputDir.getValue());
                        }

                        Path artPath = dir.resolve(finalNameExt.toString());
                        URI uri = artPath.toUri();
                        builder.location(uri);
                        if (Files.exists(artPath)) {
                            builder.forceLocalFile(FileUtil.toFileObject(artPath.toFile()));
                        }
                    } catch (InvalidPathException ex) {
                        // TODO: log 
                    }
                    specs.add(builder.build());
                }
            }
        }
        
        public void run() {
            specs = new ArrayList<>();
            Model mdl;
            ProjectActionContext buildCtx;
            
            if (filter != null && filter.getBuildContext() != null) {
                if (filter.getBuildContext().getProjectAction() == null) {
                    buildCtx = filter.getBuildContext().newDerivedBuilder().forProjectAction(ActionProvider.COMMAND_BUILD).context();
                } else {
                    buildCtx = filter.getBuildContext();
                }
            } else {
                buildCtx = ProjectActionContext.newBuilder(project).forProjectAction(ActionProvider.COMMAND_BUILD).context();
            }
            MavenProject mp = nbMavenProject.getEvaluatedProject(buildCtx);
            mdl = mp.getModel();
            
            String packaging = mdl.getPackaging();
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            
            appendPluginOutput(mp, Constants.PLUGIN_JAR, NbMavenProject.TYPE_JAR, NbMavenProject.TYPE_JAR);
            appendPluginOutput(mp, Constants.PLUGIN_WAR, NbMavenProject.TYPE_WAR, NbMavenProject.TYPE_WAR);
            appendPluginOutput(mp, Constants.PLUGIN_EAR, NbMavenProject.TYPE_EAR, NbMavenProject.TYPE_EAR);
            appendPluginOutput(mp, Constants.PLUGIN_EJB, NbMavenProject.TYPE_EJB, NbMavenProject.TYPE_EJB);
        }
    }
    
    private static final RequestProcessor MAVEN_ARTIFACTS_RP = new RequestProcessor(MavenArtifactsImplementation.class);

    static class Res implements ProjectArtifactsImplementation.Result, PropertyChangeListener {
        private final Project project;
        private final ProjectArtifactsQuery.Filter filter;
        
        // @GuardedBy(this)
        private List<ArtifactSpec> artifacts;
        // @GuardedBy(this)
        private List<ChangeListener> listeners;
        
        private RequestProcessor.Task refreshTask;
        
        public Res(Project project, ProjectArtifactsQuery.Filter filter) {
            this.project = project;
            this.filter = filter;
        }
        
        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public List<ArtifactSpec> getArtifacts() {
            synchronized (this) {
                if (artifacts != null) {
                    return artifacts;
                }
            }
            NbMavenProject mvnProject = project.getLookup().lookup(NbMavenProject.class);
            MavenQuery q = new MavenQuery(project, mvnProject, filter);
            q.run();
            synchronized (this) {
                if (artifacts == null) {
                    artifacts = q.specs;
                }
            }
            return q.specs;
        }
        
        private void update(List<ArtifactSpec> copy, RequestProcessor.Task self) {
            NbMavenProject mvnProject = project.getLookup().lookup(NbMavenProject.class);
            MavenQuery q = new MavenQuery(project, mvnProject, filter);
            q.run();
            
            ChangeListener[] ll;
            synchronized (this) {
                if (artifacts == null) {
                    artifacts = q.specs;
                    return;
                } else if (this.artifacts.equals(q.specs)) {
                    return;
                } else {
                    ll = listeners.toArray(new ChangeListener[listeners.size()]);
                }
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }
        
        @Override
        public Collection<ArtifactSpec> getExcludedArtifacts() {
            return null;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (listeners == null) {
                    NbMavenProject mvnProject = project.getLookup().lookup(NbMavenProject.class);
                    mvnProject.addPropertyChangeListener(WeakListeners.propertyChange(this, NbMavenProject.PROP_PROJECT, mvnProject));
                }
                listeners.add(l);
            }
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            synchronized (this) {
                if (listeners == null) {
                    return;
                }
                listeners.remove(l);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                return;
            }
            ChangeListener[] ll;
            final List<ArtifactSpec> copy;
            
            synchronized (this) {
                artifacts = null;
                if (listeners == null && listeners.isEmpty()) {
                    return;
                }
                if (refreshTask != null) {
                    refreshTask.cancel();
                }
                copy = artifacts == null ? Collections.emptyList() : new ArrayList<>(this.artifacts);
                RequestProcessor.Task[] arr = new RequestProcessor.Task[1];
                
                arr[0] = refreshTask = MAVEN_ARTIFACTS_RP.create(() -> update(copy, arr[0]));
                
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }

        @Override
        public boolean supportsChanges() {
            return true;
        }
    }
}

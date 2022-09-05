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
package org.netbeans.modules.micronaut.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class MicronautPackagingArtifactsImpl implements ProjectArtifactsImplementation<MicronautPackagingArtifactsImpl.R> {
    // TBD: possibly configure the N-I plugin coordinates in the IDE settings.
    
    /**
     * sharedLibrary plugin parameter. Will build a DLL or .so 
     */
    public static final String PLUGIN_PARAM_SHAREDLIBRARY = "sharedLibrary";
    
    private static final Set<String> SUPPORTED_ARTIFACT_TYPES = new HashSet<>(Arrays.asList(
            MicronautMavenConstants.TYPE_DYNAMIC_LIBRARY, MicronautMavenConstants.TYPE_EXECUTABLE
    ));
    
    /**
     * imageName plugin parameter; defines the executable name.
     */
    public static final String PLUGIN_PARAM_IMAGENAME = "imageName";
    
    private final Project project;
    private final NbMavenProject mavenProject;
    
    public MicronautPackagingArtifactsImpl(Project project) {
        this.project = project;
        mavenProject = project.getLookup().lookup(NbMavenProject.class);
    }

    @Override
    public R evaluate(ProjectArtifactsQuery.Filter query) {
        return new R(project, mavenProject, query);
    }

    @Override
    public Project findProject(R r) {
        return r.getProject();
    }

    @Override
    public List<ArtifactSpec> findArtifacts(R r) {
        return r.getArtifacts();
    }

    @Override
    public Collection<ArtifactSpec> findExcludedArtifacts(R r) {
        return r.getExcludedArtifacts();
    }

    @Override
    public void handleChangeListener(R r, ChangeListener l, boolean add) {
        if (add) {
            r.addChangeListener(l);
        } else {
            r.removeChangeListener(l);
        }
    }

    @Override
    public boolean computeSupportsChanges(R r) {
        return true;
    }

   static class R implements PropertyChangeListener {
        private final Project project;
        private final NbMavenProject mavenProject;
        private final ProjectArtifactsQuery.Filter query;

        // @GuardedBy(this)
        private final List<ChangeListener> listeners = new ArrayList<>();
        // @GuardedBy(this)
        private List<ArtifactSpec> artifacts;
        
        private PropertyChangeListener projectL;

        public R(Project project, NbMavenProject mavenProject, ProjectArtifactsQuery.Filter query) {
            this.project = project;
            this.mavenProject = mavenProject;
            this.query = query;
        }

        public Project getProject() {
            return project;
        }
        
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (projectL == null) {
                    projectL = WeakListeners.propertyChange(this, project);
                    mavenProject.addPropertyChangeListener(projectL);
                }
                listeners.add(l);
            }
        }
        
        public void removeChangeListener(ChangeListener l) {
            synchronized (this) {
                listeners.remove(l);
            }
        }

        public List<ArtifactSpec> getArtifacts() {
            synchronized (this) {
                if (artifacts != null) {
                    return artifacts;
                }
            }
            List<ArtifactSpec> as = update();
            synchronized (this) {
                if (artifacts == null) {
                    artifacts = as;
                }
            }
            return as;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                return;
            }
            List<ArtifactSpec> old;
            
            synchronized (this) {
                if (artifacts == null) {
                    return;
                }
                old = artifacts;
                artifacts = null;
                if (listeners.isEmpty()) {
                    return;
                }
            }
            
            List<ArtifactSpec> n = update();
            ChangeListener[] ll;
            
            synchronized (this) {
                if (artifacts == null) {
                    this.artifacts = n;
                }
                if (old != null && n.equals(old)) {
                    return;
                }
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }

        public Collection<ArtifactSpec> getExcludedArtifacts() {
            return null;
        }
        
        private List<ArtifactSpec> update() {
            ProjectActionContext buildCtx;
            
            if (query.getBuildContext() != null) {
                if (query.getBuildContext().getProjectAction() == null) {
                    buildCtx = query.getBuildContext().newDerivedBuilder().forProjectAction(ActionProvider.COMMAND_BUILD).context();
                } else {
                    buildCtx = query.getBuildContext();
                }
            } else {
                buildCtx = ProjectActionContext.newBuilder(project).forProjectAction(ActionProvider.COMMAND_BUILD).context();
            }
            if (query.getArtifactType() != null && 
                !SUPPORTED_ARTIFACT_TYPES.contains(query.getArtifactType()) &&
                !ProjectArtifactsQuery.Filter.TYPE_ALL.equals(query.getArtifactType())) {
                return Collections.emptyList();
            }
            if (query.getClassifier()!= null && !ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(query.getClassifier())) {
                return Collections.emptyList();
            }
            MavenProject model = mavenProject.getEvaluatedProject(buildCtx);
            if (!MicronautMavenConstants.PACKAGING_NATIVE.equals(model.getPackaging())) {
                return Collections.emptyList();
            }
            List<ArtifactSpec> nativeStuff = new ArrayList<>();
            for (Plugin p : model.getBuild().getPlugins()) {
                if (!(MicronautMavenConstants.NATIVE_BUILD_PLUGIN_GROUP.equals(p.getGroupId()) && MicronautMavenConstants.NATIVE_BUILD_PLUGIN_ID.equals(p.getArtifactId()))) {
                    continue;
                }
                for (PluginExecution pe : p.getExecutions()) {
                    if (pe.getGoals().contains(MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK)) { // NOI18N
                        Xpp3Dom dom = model.getGoalConfiguration(MicronautMavenConstants.NATIVE_BUILD_PLUGIN_GROUP, MicronautMavenConstants.NATIVE_BUILD_PLUGIN_ID, pe.getId(), MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK); // NOI18N
                        if (dom != null) {
                            Xpp3Dom imageName = dom.getChild(PLUGIN_PARAM_IMAGENAME); // NOI18N
                            Xpp3Dom sharedLib = dom.getChild(PLUGIN_PARAM_SHAREDLIBRARY); // NOI18N

                            String name;
                            if (imageName == null) {
                                // project default, but should be injected / interpolated by Maven already.
                                name = model.getArtifactId();
                            } else {
                                name = imageName.getValue();
                            }
                            
                            Path full = Paths.get(model.getBuild().getDirectory()).resolve(name);
                            nativeStuff.add(ArtifactSpec.builder(model.getGroupId(), model.getArtifactId(), model.getVersion(), pe)
                                    .type(sharedLib != null && Boolean.parseBoolean(sharedLib.getValue()) ? MicronautMavenConstants.TYPE_DYNAMIC_LIBRARY : MicronautMavenConstants.TYPE_EXECUTABLE)
                                    .location(full.toUri())
                                    .forceLocalFile(FileUtil.toFileObject(full.toFile()))
                                    .build()
                            );
                        }
                    }
                }
            }
            return nativeStuff;
        }
    }
    
    @NbBundle.Messages({
        "DN_MicronautArtifacts=Micronaut artifact support"
    })
    public static LookupProvider projectLookup(Map<Object, Object> attrs) {
        return new LookupProvider() {
            @Override
            public Lookup createAdditionalLookup(Lookup baseContext) {
                Project p = baseContext.lookup(Project.class);
                if (p == null) {
                    return Lookup.EMPTY;
                }
                return Lookups.fixed(new MicronautPackagingArtifactsImpl(p));
                /*
                return Lookups.fixed(ProjectArtifactsImplementation.class, new InstanceContent.Convertor<Class<ProjectArtifactsImplementation>, ProjectArtifactsImplementation>() {
                    @Override
                    public ProjectArtifactsImplementation convert(Class<ProjectArtifactsImplementation> obj) {
                        return new MicronautPackagingArtifactsImpl(p);
                    }

                    @Override
                    public Class<? extends ProjectArtifactsImplementation> type(Class<ProjectArtifactsImplementation> obj) {
                        return MicronautPackagingArtifactsImpl.class;
                    }

                    @Override
                    public String id(Class<ProjectArtifactsImplementation> obj) {
                        return obj.getName();
                    }

                    @Override
                    public String displayName(Class<ProjectArtifactsImplementation> obj) {
                        return Bundle.DN_MicronautArtifacts();
                    }
                });
                */
            }
        };
    }
}

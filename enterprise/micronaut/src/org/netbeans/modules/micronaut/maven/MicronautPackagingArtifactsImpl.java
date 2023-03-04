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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.micronaut.AbstractMicronautArtifacts;
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
    private static final Logger LOG = Logger.getLogger(MicronautPackagingArtifactsImpl.class.getName());
    
    // TBD: possibly configure the N-I plugin coordinates in the IDE settings.
    
    /**
     * sharedLibrary plugin parameter. Will build a DLL or .so 
     */
    public static final String PLUGIN_PARAM_SHAREDLIBRARY = "sharedLibrary"; // NOI18N
    
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
        LOG.log(Level.FINE, "Created for project {0}", project.getProjectDirectory());
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
    
    /**
     * Goals that actually trigger native artifact generation. As they are compared by name with
     * configurations, use both unprefixed and prefixed forms
     */
    private static final String[] NATIVE_ARTIFACT_BUILDERS = new String[] {
        MicronautMavenConstants.PLUGIN_GOAL_COMPILE,
        MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK,
        "native:" + MicronautMavenConstants.PLUGIN_GOAL_COMPILE,    // NOI18N
        "native:" + MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK, // NOI18N
        
        // Compatibility with plugin 0.9.13 and earlier:
        MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK_OLD,
        "native:" + MicronautMavenConstants.PLUGIN_GOAL_COMPILE_NOFORK_OLD, // NOI18N
    };

    static class R extends AbstractMicronautArtifacts {

        private final NbMavenProject mavenProject;

        public R(Project project, NbMavenProject mavenProject, ProjectArtifactsQuery.Filter query) {
            super(project, query);
            this.mavenProject = mavenProject;
        }

        @Override
        protected void attach(PropertyChangeListener l) {
            mavenProject.addPropertyChangeListener(l);
        }

        @Override
        protected void detach(PropertyChangeListener l) {
        }

        @Override
        protected boolean accept(PropertyChangeEvent e) {
            return NbMavenProject.PROP_PROJECT.equals(e.getPropertyName());
        }

        @Override
        protected List<ArtifactSpec> compute() {
            ProjectActionContext buildCtx;

            if (query.getBuildContext() != null) {
                if (query.getBuildContext().getProjectAction() == null) {
                    buildCtx = query.getBuildContext().newDerivedBuilder().forProjectAction(ActionProvider.COMMAND_BUILD).context();
                } else {
                    buildCtx = query.getBuildContext();
                }
            } else {
                buildCtx = ProjectActionContext.newBuilder(getProject()).forProjectAction(ActionProvider.COMMAND_BUILD).context();
            }
            if (query.getArtifactType() != null && 
                !SUPPORTED_ARTIFACT_TYPES.contains(query.getArtifactType()) && 
                !ProjectArtifactsQuery.Filter.TYPE_ALL.equals(query.getArtifactType())) {
                LOG.log(Level.FINE, "Unsupported type: {0}", query.getArtifactType());
                return Collections.emptyList();
            }
            if (query.getClassifier()!= null && !ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(query.getClassifier())) {
                LOG.log(Level.FINE, "Unsupported classifier: {0}", query.getClassifier());
                return Collections.emptyList();
            }
            MavenProject model = mavenProject.getEvaluatedProject(buildCtx);
            boolean explicitGraalvmGoal = false;
            if (buildCtx.getProjectAction() != null) {
                RunConfig cfg = RunUtils.createRunConfig(buildCtx.getProjectAction(), getProject(), null, Lookup.EMPTY);
                Set<String> triggerGoals = new HashSet<>(Arrays.asList(NATIVE_ARTIFACT_BUILDERS));
                if (cfg != null) {
                    triggerGoals.retainAll(cfg.getGoals());
                    if (!triggerGoals.isEmpty()) {
                        LOG.log(Level.FINE, "Go explicit native compilation goal from the action");
                        explicitGraalvmGoal = true;
                    }
                }
            }
            if (!explicitGraalvmGoal && !MicronautMavenConstants.PACKAGING_NATIVE.equals(model.getPackaging())) {
                LOG.log(Level.FINE, "Unsupported packaging: {0}", model.getPackaging());
                return Collections.emptyList();
            }
            List<ArtifactSpec> nativeStuff = new ArrayList<>();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Configured build plugins: {0}", model.getBuild().getPlugins());
            }
            boolean foundExecution = false;

            for (Plugin p : model.getBuild().getPlugins()) {
                if (!(MicronautMavenConstants.NATIVE_BUILD_PLUGIN_GROUP.equals(p.getGroupId()) && MicronautMavenConstants.NATIVE_BUILD_PLUGIN_ID.equals(p.getArtifactId()))) {
                    continue;
                }
                LOG.log(Level.FINE, "Configured executions: {0}", p.getExecutions());
                for (PluginExecution pe : p.getExecutions()) {
                    Set<String> triggerGoals = new HashSet<>(Arrays.asList(NATIVE_ARTIFACT_BUILDERS));
                    triggerGoals.retainAll(pe.getGoals());
                    if (!triggerGoals.isEmpty()) {
                        String goalName = triggerGoals.iterator().next();
                        Xpp3Dom dom = model.getGoalConfiguration(MicronautMavenConstants.NATIVE_BUILD_PLUGIN_GROUP, MicronautMavenConstants.NATIVE_BUILD_PLUGIN_ID, pe.getId(), goalName); // NOI18N
                        if (dom != null) {
                            LOG.log(Level.FINE, "Found bound execution for goals {0}", pe.getGoals());
                            addNativeExecutable(nativeStuff, model, dom, pe);
                            foundExecution = true;
                        }
                    }
                }
            }

            if (!foundExecution && explicitGraalvmGoal) {
                LOG.log(Level.FINE, "No bound execution found, but explicit native compilation requested, trying to search for plugin base config");
                // try to get the configuration from PluginManagement, since the plugin is not directly in the build sequence.
                Plugin p = model.getPluginManagement().getPluginsAsMap().get(MicronautMavenConstants.NATIVE_BUILD_PLUGIN_GROUP + ":" + MicronautMavenConstants.NATIVE_BUILD_PLUGIN_ID);
                if (p != null && p.getConfiguration() != null) {
                    LOG.log(Level.FINE, "Found plugin configuration");
                    Xpp3Dom dom = (Xpp3Dom) p.getConfiguration();
                    addNativeExecutable(nativeStuff, model, dom, p);
                }
            }
            return nativeStuff;
        }

        private void addNativeExecutable(List<ArtifactSpec> nativeStuff, MavenProject model, Xpp3Dom dom, Object data) {
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
           nativeStuff.add(ArtifactSpec.builder(model.getGroupId(), model.getArtifactId(), model.getVersion(), data)
                   .type(sharedLib != null && Boolean.parseBoolean(sharedLib.getValue()) ? MicronautMavenConstants.TYPE_DYNAMIC_LIBRARY : MicronautMavenConstants.TYPE_EXECUTABLE)
                   .location(full.toUri())
                   .forceLocalFile(FileUtil.toFileObject(full.toFile()))
                   .build()
           );
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

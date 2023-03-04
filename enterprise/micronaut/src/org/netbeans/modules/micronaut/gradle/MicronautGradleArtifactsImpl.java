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
package org.netbeans.modules.micronaut.gradle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.micronaut.AbstractMicronautArtifacts;
import org.netbeans.modules.micronaut.maven.MicronautMavenConstants;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service=ProjectArtifactsImplementation.class,
        projectType = {
            NbGradleProject.GRADLE_PLUGIN_TYPE + "/io.micronaut.application",
            NbGradleProject.GRADLE_PLUGIN_TYPE + "/org.graalvm.buildtools.native"
        }
)
public class MicronautGradleArtifactsImpl implements ProjectArtifactsImplementation<MicronautGradleArtifactsImpl.R>{
    private static final String EXTENSION_GRAAL_VM_NATIVE = "graalVmNative";
    private static final String TASK_NATIVE_COMPILE = "nativeCompile";

    private final Project project;

    public MicronautGradleArtifactsImpl(Project project) {
        this.project = project;
    }

    @Override
    public int getOrder() {
        return 9000;
    }
    
    private static final class TaskActionMapping implements ActionMapping {
        private final String task;

        public TaskActionMapping(String task) {
            this.task = task;
        }
        
        @Override
        public String getName() {
            return task;
        }

        @Override
        public String getDisplayName() {
            return task;
        }

        @Override
        public String getArgs() {
            return task;
        }

        @Override
        public ActionMapping.ReloadRule getReloadRule() {
            return ActionMapping.ReloadRule.DEFAULT;
        }

        @Override
        public String getReloadArgs() {
            return "";
        }

        @Override
        public boolean isApplicable(Set<String> plugins) {
            return true;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public int compareTo(ActionMapping o) {
            return -1;
        }
    }
    
    @Override
    public R evaluate(ProjectArtifactsQuery.Filter filter) {
        NbGradleProject gp = NbGradleProject.get(project);
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gp == null || gbp == null) {
            return null;
        }
        
        if (filter.getClassifier() != null && !ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(filter.getClassifier())) {
            if (!MicronautMavenConstants.CLASSIFIER_NATIVE.equals(filter.getClassifier())) {
                return null;
            }
        }
        if (filter.getArtifactType()!= null && !ProjectArtifactsQuery.Filter.TYPE_ALL.equals(filter.getArtifactType())) {
            if (!MicronautMavenConstants.TYPE_EXECUTABLE.equals(filter.getArtifactType())) {
                return null;
            }
        }
        
        String action = ActionProvider.COMMAND_BUILD;
        GradleExecConfiguration cfg = null;
        Lookup lkp = Lookup.EMPTY;
        if (filter.getBuildContext() != null) {
            ProjectActionContext pac = filter.getBuildContext();
            if (pac.getProjectAction() != null) {
                action = pac.getProjectAction();
            }
            if (pac.getConfiguration() != null) {
                cfg = (GradleExecConfiguration)pac.getConfiguration();
            }
        }
        ActionMapping mapping = RunUtils.findActionMapping(project, action, cfg);
        if (mapping == null) {
            // let's support gradle specific action verbs:
            mapping = new TaskActionMapping(action);
        }
        final String[] args = RunUtils.evaluateActionArgs(project, action, mapping.getArgs(), lkp);
        RunConfig rc = RunUtils.createRunConfig(project, action, "Searching for artifacts", Lookup.EMPTY, cfg, Collections.emptySet(), args);
        
        if (!rc.getCommandLine().getTasks().contains(TASK_NATIVE_COMPILE)) {
            boolean found = false;
            for (String t : rc.getCommandLine().getTasks()) {
                GradleTask task = gbp.getTaskByName(t);
                if (task != null) {
                    for (GradleTask pred : gbp.getTaskPredecessors(task, false)) {
                        if (!pred.isExternal() && TASK_NATIVE_COMPILE.equals(pred.getName())) {
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                return null;
            }
        }
        return new R(project, gbp, gp, filter);
    }

    @Override
    public Project findProject(R r) {
        return r.project;
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
    
    static class R extends AbstractMicronautArtifacts {
        private final Project project;
        private final GradleBaseProject gbp;
        private final NbGradleProject gp;
        private List<ChangeListener> listeners;

        public R(Project project, GradleBaseProject gbp, NbGradleProject gp, ProjectArtifactsQuery.Filter query) {
            super(project, query);
            this.project = project;
            this.gbp = gbp;
            this.gp = gp;
        }

        @Override
        protected List<ArtifactSpec> compute() {
            BuildPropertiesSupport props = BuildPropertiesSupport.get(project);
            if (props == null) {
                return Collections.emptyList();
            }
            BuildPropertiesSupport.Property outFile = props.findTaskProperty(TASK_NATIVE_COMPILE, "outputFile"); // NOI18N
            Path filename;
            
            if (outFile != null && outFile.getStringValue() != null) {
                filename = Paths.get(outFile.getStringValue());
            } else {
                BuildPropertiesSupport.Property extensionImageName = props.findExtensionProperty(EXTENSION_GRAAL_VM_NATIVE, "binaries.main.imageName"); // NOI18N
                BuildPropertiesSupport.Property taskImageName = props.findTaskProperty(TASK_NATIVE_COMPILE, "executableName"); // NOI18N
                BuildPropertiesSupport.Property outputDir = props.findTaskProperty(TASK_NATIVE_COMPILE, "outputDirectory"); // NOI18N
                
                Path dir;
                Path  fn;
                
                if (outputDir != null && outputDir.getStringValue() != null) {
                    dir = Paths.get(outputDir.getStringValue());
                } else {
                    dir = gbp.getBuildDir().toPath().resolve(Paths.get("native", "nativeCompile")); // NOI18N
                }
                if (taskImageName != null && taskImageName.getStringValue() != null) {
                    fn = Paths.get(taskImageName.getStringValue());
                } else if (extensionImageName != null && extensionImageName.getStringValue() != null) {
                    String s = extensionImageName.getStringValue();
                    if (BaseUtilities.isWindows()) {
                        s += ".exe"; // NOI18N
                    }
                    fn = Paths.get(s);
                } else {
                    String s = gbp.getName();
                    if (BaseUtilities.isWindows()) {
                        s += ".exe"; // NOI18N
                    }
                    fn = Paths.get(s);
                }
                filename = dir.resolve(fn);
            }
            
            ArtifactSpec spec = ArtifactSpec.builder(gbp.getGroup(), gbp.getName(), gbp.isVersionSpecified() ? gbp.getVersion() : null, project).
                    classifier(MicronautMavenConstants.CLASSIFIER_NATIVE).
                    type(MicronautMavenConstants.TYPE_EXECUTABLE).
                    location(filename.toUri()).
                    build();
            return Collections.singletonList(spec);
        }

        @Override
        protected void attach(PropertyChangeListener l) {
            gp.addPropertyChangeListener(l);
        }

        @Override
        protected void detach(PropertyChangeListener l) {
            gp.removePropertyChangeListener(l);
        }

        @Override
        protected boolean accept(PropertyChangeEvent e) {
            return NbGradleProject.PROP_PROJECT_INFO.equals(e.getPropertyName());
        }
    }
}

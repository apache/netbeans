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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport.Property;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * This query implementation handles artifacts produced by jar-type and specifically shadowJar tasks. Outputs from
 * general JAR tasks (if they are included in build sequence) are reported as artifacts type "jar" with a classifier
 * from the task. shadowJar is handled specifically: if present, its -all jar gets an additional {@link ArtifactSpec#TAG_SHADED}
 * tag to signal it is a bundle, and the original jar gets {@link ArtifactSpec#TAG_BASE}.
 * <p>
 * Tasks are identified using their types, as the user may create his own subclasses (e.g. <code>task testJar(type: Jar)</code>
 * for source archive.
 * 
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectArtifactsImplementation.class,  projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleJarArtifacts implements ProjectArtifactsImplementation<GradleJarArtifacts.Result> {
    private static final RequestProcessor GRADLE_ARTIFACTS_RP = new RequestProcessor(GradleJarArtifacts.class);
    private final Project   project;
    
    /**
     * Type of the supported 'shadowJar' task.
     */
    private static final String TASKCLASS_SHADOW_JAR = "com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar";

    /**
     * Type of the standard 'jar' task.
     */
    private static final String TASKCLASS_JAR = "org.gradle.jvm.tasks.Jar";

    /**
     * Name of the 'jar' task
     */
    private static final String TASK_JAR = "jar";

    /**
     * Name of the 'shadowJar' task
     */
    private static final String TASK_SHADOW_JAR = "shadowJar";

    public GradleJarArtifacts(Project project) {
        this.project = project;
    }
    
    @Override
    public Result evaluate(ProjectArtifactsQuery.Filter query) {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        NbGradleProject proj = NbGradleProject.get(project);
        if (gbp == null) {
            return null;
        }
        if (proj == null) {
            return null;
        }
        
        if (!(query.getArtifactType() == null || query.getArtifactType().equals(ProjectArtifactsQuery.Filter.TYPE_ALL))) {
            if (!"jar".equals(query.getArtifactType())) {
                return null;
            }
        }
        return new Result(project, query, proj);
    }

    @Override
    public Project findProject(Result r) {
        return r.project;
    }

    @Override
    public List<ArtifactSpec> findArtifacts(Result r) {
        return r.getArtifacts();
    }

    @Override
    public Collection<ArtifactSpec> findExcludedArtifacts(Result r) {
        return Collections.emptySet();
    }

    @Override
    public void handleChangeListener(Result r, ChangeListener l, boolean add) {
        r.addListener(l, add);
    }

    @Override
    public boolean computeSupportsChanges(Result r) {
        return true;
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
    
    static class Result implements PropertyChangeListener {
        private static final List<ArtifactSpec> PENDING = new ArrayList<>();
        
        private final Project project;
        private final ProjectArtifactsQuery.Filter filter;
        private final NbGradleProject gradleProject;
        private final List<String> buildTasks;
        private List<ChangeListener> listeners;
        private List<ArtifactSpec> artifacts;

        private RequestProcessor.Task refreshTask;

        public Result(Project project, ProjectArtifactsQuery.Filter filter, NbGradleProject gradleProject) {
            this.project = project;
            this.filter = filter;
            this.gradleProject = gradleProject;
            
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
            buildTasks = new ArrayList<>(rc.getCommandLine().getTasks());
        }
        
        private void addListener(ChangeListener l, boolean add) {
            synchronized (this) {
                if (add) {
                    if (listeners == null) {
                        listeners = new ArrayList<>();
                        gradleProject.addPropertyChangeListener(WeakListeners.propertyChange(this, project));
                    }
                    listeners.add(l);
                } else if (listeners == null) {
                    return;
                } else {
                    listeners.remove(l);
                }
            }
        }
        
        public List<ArtifactSpec> getArtifacts() {
            List<ArtifactSpec> as = this.artifacts;
            if (as != null && as != PENDING) {
                return as;
            }
            as = createArtifactList();
            synchronized (this) {
                if (this.artifacts == null || this.artifacts == PENDING) {
                    this.artifacts = as;
                }
            }
            return as;
        }
        
        private void update(List<ArtifactSpec> oldCopy) {
            List<ArtifactSpec> specs = createArtifactList();
            ChangeListener[] ll;
            synchronized (this) {
                if (artifacts == null) {
                    artifacts = specs;
                    return;
                }
                if (artifacts == PENDING) {
                    artifacts = specs;
                }
                if (specs.equals(oldCopy)) {
                    return;
                } else {
                    ll = listeners.toArray(new ChangeListener[0]);
                }
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }
        
        private List<ArtifactSpec> createArtifactList() {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            Set<ArtifactSpec> result = new LinkedHashSet<>();

            for (String taskName : buildTasks) {
                GradleTask gt = gbp.getTaskByName(taskName);
                if (gt == null) {
                    continue;
                }
                boolean shadowApplied = false;
                List<GradleTask> jarTasks = new ArrayList<>();
                List<GradleTask> tasks = new ArrayList<>(gbp.getTaskPredecessors(gt, false));
                tasks.add(gt);
                for (GradleTask dep : tasks) {
                    if (gbp.isTaskInstanceOf(dep.getName(), TASKCLASS_SHADOW_JAR)) {
                        addShadowJarArtifacts(dep.getName(), gbp, result);
                        shadowApplied = true;
                    } else if (gbp.isTaskInstanceOf(dep.getName(), TASKCLASS_JAR)) {
                        jarTasks.add(dep);
                    }
                }
                for (GradleTask dep : jarTasks) {
                    if (!shadowApplied || !TASK_JAR.equals(dep.getName())) {
                        addJarArtifacts(dep.getName(), gbp, result);
                    }
                }
            }
            return new ArrayList<>(result);
        }
        
        private void addShadowJarArtifacts(String taskName, GradleBaseProject gbp, Collection<ArtifactSpec> results) {
            boolean addShadow = false;
            boolean addJar = true;
            if (ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(filter.getClassifier()) || "all".equals(filter.getClassifier())) {
                addShadow = true;
            } else {
                if (filter.getClassifier() == null) {
                    // regular output even in shadowJar presence is the classes jar
                    addShadow = false;
                    if (filter.hasTag(ArtifactSpec.TAG_SHADED)) {
                        // ... but not if shaded was explicitly requested.
                        addShadow = true;
                        addJar = false;
                    }
                    if (filter.hasTag(ArtifactSpec.TAG_BASE)) {
                        addJar = true;
                    }
                }
            }
            if (addShadow) {
                ArtifactSpec.Builder b = artifactBuilder(taskName, gbp);
                if (b != null) {
                    results.add(b.tag(ArtifactSpec.TAG_SHADED).build());
                }
            }
            if (addJar) {
                ArtifactSpec.Builder b = artifactBuilder(TASK_JAR, gbp);
                if (b != null) {
                    results.add(b.tag(ArtifactSpec.TAG_BASE).build());
                }
            }
        }
        
        private ArtifactSpec.Builder artifactBuilder(String task, GradleBaseProject gbp) {
            String name = gbp.getName();
            String group = gbp.getGroup();
            String version = gbp.isVersionSpecified() ? gbp.getVersion() : null;
            String baseName = name;
            String filename;
            Path path = null;
            String dir = null;
            String classifier = null;
            String appendix = null;
            
            BuildPropertiesSupport props = BuildPropertiesSupport.get(project);
            Property p;
            p = props.findTaskProperty(task, "archiveFile");
            if (p != null && p.getStringValue() != null) {
                path = Paths.get(p.getStringValue());
            } else {
                p = props.findTaskProperty(task, "archiveFileName");
                if (p != null && p.getStringValue() != null) {
                    filename = p.getStringValue();
                } else {
                    if (gbp.isVersionSpecified()) {
                        filename = String.format("%s-%s.jar", baseName, version);
                    } else {
                        filename = baseName + ".jar";
                    }
                }

                p = props.findTaskProperty(task, "destinationDirectory");
                if (p != null && p.getStringValue() != null) {
                    dir = p.getStringValue();
                }
                if (dir == null) {
                    dir = gbp.getBuildDir().toPath().toString();
                }
                if (dir != null && filename != null) {
                    path = Paths.get(dir).resolve(filename);
                }
            }
            
            p = props.findTaskProperty(task, "archiveAppendix");
            if (p != null && p.getStringValue() != null) {
                appendix = p.getStringValue();
            }
            p = props.findTaskProperty(task, "archiveClassifier");
            if (p != null && p.getStringValue() != null) {
                classifier = p.getStringValue();
                if (classifier.isEmpty()) {
                    classifier = null;
                }
            }
            String artName = appendix == null ? name : name + "-" + appendix;
            ArtifactSpec.Builder b = ArtifactSpec.builder(group, artName, version, project).
                    classifier(classifier).
                    type("jar");
            if (path != null) {
                b.location(path.toUri());
            }
            return b;
        }
        
        private void addJarArtifacts(String name, GradleBaseProject gbp, Collection<ArtifactSpec> results) {
            ArtifactSpec.Builder b = artifactBuilder(name, gbp);
            if (b == null) {
                return;
            }
            ArtifactSpec a = b.build();
            if (ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(filter.getClassifier()) ||
                Objects.equals(filter.getClassifier(), a.getClassifier())) {
                results.add(a);
            }
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                return;
            }
            ChangeListener[] ll;
            boolean wasInitialized;
            
            synchronized (this) {
                wasInitialized = artifacts != null;
                List<ArtifactSpec> copy = this.artifacts;
                artifacts = PENDING;
                if (listeners == null && listeners.isEmpty()) {
                    return;
                }
                if (refreshTask != null) {
                    refreshTask.cancel();
                }
                if (wasInitialized) {
                    refreshTask = GRADLE_ARTIFACTS_RP.post(() -> update(copy));
                    return;
                }
                ll = listeners.toArray(new ChangeListener[0]);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }
    }
}

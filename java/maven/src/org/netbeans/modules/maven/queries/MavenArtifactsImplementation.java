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
public class MavenArtifactsImplementation implements ProjectArtifactsImplementation<MavenArtifactsImplementation.Res> {

    private static final Logger LOG = Logger.getLogger(ProjectArtifactsImplementation.class.getName());

    private final Project project;

    public MavenArtifactsImplementation(Project project) {
        this.project = project;
    }

    @Override
    public Res evaluate(ProjectArtifactsQuery.Filter query) {
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

    @Override
    public Project findProject(Res r) {
        return r.getProject();
    }

    @Override
    public List<ArtifactSpec> findArtifacts(Res r) {
        return r.getArtifacts();
    }

    @Override
    public Collection<ArtifactSpec> findExcludedArtifacts(Res r) {
        return r.getExcludedArtifacts();
    }

    @Override
    public void handleChangeListener(Res r, ChangeListener l, boolean add) {
        if (add) {
            r.addChangeListener(l);
        } else {
            r.removeChangeListener(l);
        }
    }

    @Override
    public boolean computeSupportsChanges(Res r) {
        return r.supportsChanges();
    }

    static class MavenQuery {

        final Project project;
        final NbMavenProject nbMavenProject;
        final ProjectArtifactsQuery.Filter filter;
        MavenProject evalProject;

        List<ArtifactSpec> specs;
        
        boolean skipDefaultOutput;

        public MavenQuery(Project project, NbMavenProject nbMavenProject, ProjectArtifactsQuery.Filter filter) {
            this.project = project;
            this.nbMavenProject = nbMavenProject;
            this.filter = filter;
        }

        void addArtifactSpec(ArtifactSpec spec) {
            if (specs.contains(spec)) {
                return;
            }
            specs.add(spec);
        }

        /**
         * Evaluator that processes one execution of the shade plugin. Note that multiple artifacts may be produced in multiple
         * executions, with different classifiers.
         */
        private class ShadedEvaluator {
            /**
             * Default classifier for attached artifacts produced by shade plugin
             */
            private static final String DEFAULT_SHADE_CLASSIFIER = "shaded";

            private final MavenProject evalProject;
            private final PluginExecution exec;
            private String t;
            private String c;

            String classifier;
            String outputDir;
            String outputFile;
            String finalName;
            String artifactId;
            boolean attached;
            boolean createSourcesJar;
            boolean createTestJar;
            boolean createTestSourcesJar;
            String outD;
            String name;
            Path basePath;
            Artifact mA;
            boolean any;
            boolean tagBase;
            boolean tagShaded;
            boolean renamed;

            public ShadedEvaluator(MavenProject evalProject, PluginExecution exec) {
                this.evalProject = evalProject;
                this.exec = exec;
            }

            public void process() {
                Xpp3Dom dom = evalProject.getGoalConfiguration(
                        Constants.GROUP_APACHE_PLUGINS, "maven-shade-plugin", exec.getId(), "shade"); // NOI18N
                mA = evalProject.getArtifact();
                t = filter.getArtifactType();
                c = filter.getClassifier();
                classifier = getValueOrNull(dom, "shadedClassifierName"); // NOI18N
                outputDir = getValueOrNull(dom, "outputDirectory"); // NOI18N
                outputFile = getValueOrNull(dom, "outputFile"); // NOI18N
                finalName = getValueOrNull(dom, "finalName"); // NOI18N
                artifactId = getValueOrNull(dom, "shadedArtifactId"); // NOI18N

                attached = Boolean.valueOf(getValueOrNull(dom, "shadedArtifactAttached")); // NOI18N
                createSourcesJar = Boolean.valueOf(getValueOrNull(dom, "createSourcesJar")); // NOI18N
                createTestJar = Boolean.valueOf(getValueOrNull(dom, "shadeTestJar")); // NOI18N
                createTestSourcesJar = Boolean.valueOf(getValueOrNull(dom, "createTestSourcesJar")); // NOI18N

                outD = evalProject.getModel().getBuild().getDirectory();
                name = evalProject.getBuild().getFinalName();

                if (artifactId == null) {
                    artifactId = mA.getArtifactId();
                }

                if (outputDir != null) {
                    outD = outputDir;
                }
                if (finalName != null && finalName.length() > 0) {
                    name = finalName;
                }
                basePath = Paths.get(outD, name);
                any = ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(c);

                tagShaded = filter.hasTag(ArtifactSpec.TAG_SHADED) || filter.hasTag(classifier);
                tagBase = filter.hasTag(ArtifactSpec.TAG_BASE);

                // either the caller selects the classifier, or no classifier (this produces artifact tagged with 'shaded' or
                // explicitly tagged with original to get the unshaded version
                if (attached) {
                    if (classifier == null) {
                        classifier = DEFAULT_SHADE_CLASSIFIER;
                    }
                }
                boolean classifierMatch = any || (classifier != null && classifier.equals(c)) || (c == null && !attached) || tagBase;
                String suffix;

                String gID = mA.getGroupId();
                if (outputFile != null) {
                    addExplicitOutputFile();
                    suffix = "-" + classifier;
                } else {
                    if (name != null && !name.equals(evalProject.getBuild().getFinalName())) {
                        renamed = true;
                        basePath = basePath.resolveSibling(name);
                        suffix = "";
                    } else {
                        suffix = "";
                    }
                    if (attached) {
                        suffix = "-" + classifier;
                    }
                    if (classifierMatch) {
                        // do not report base unless the base is explicitly requested
                        if ((any && !tagShaded) || tagBase) {
                            skipDefaultOutput = !attached;
                            Path file = basePath.resolveSibling("original-" + basePath.getFileName() + suffix + ".jar");
                            ArtifactSpec spec = ArtifactSpec.builder(attached ? gID : null, attached ? artifactId : null, mA.getVersion(), evalProject).
                                    classifier(classifier).
                                    location(file.toUri()).
                                    type("jar").
                                    tag(ArtifactSpec.TAG_BASE).
                                    build();
                            addArtifactSpec(spec);
                        }
                        boolean reportShaded;
                        if (any) {
                            reportShaded = !tagBase || tagShaded;
                        } else {
                            // if no tag is present the default is to report the shaded artifact
                            reportShaded = !tagBase;
                        }
                        if (reportShaded) {
                            Path file = basePath.resolveSibling(basePath.getFileName() + suffix + ".jar");
                            ArtifactSpec spec = ArtifactSpec.builder(gID, artifactId, mA.getVersion(), evalProject).
                                    classifier(classifier).
                                    location(file.toUri()).
                                    type("jar").
                                    tag(ArtifactSpec.TAG_SHADED).
                                    build();
                            addArtifactSpec(spec);
                        }
                    }
                }

                if (classifierMatch) {
                    if (createSourcesJar) {
                        addClassifiedArtifact("sources", suffix, "sources", ArtifactSpec.CLASSIFIER_SOURCES, "java-source");
                    }
                    if (createTestJar) {
                        addClassifiedArtifact("test-jar", suffix, "tests", ArtifactSpec.CLASSIFIER_TESTS, "test");
                    }
                    if (createTestSourcesJar) {
                        addClassifiedArtifact("sources", suffix, "test-sources", ArtifactSpec.CLASSIFIER_TEST_SOURCES, "java-source", "test");
                    }
                }
            }

            private void addClassifiedArtifact(String type, String suffix, String typeSuffix, String defaultClassifier, String... tags) {
                if (!(ProjectArtifactsQuery.Filter.TYPE_ALL.equals(t) || type.equals(t) || (any && t == null))) {
                    return;
                }

                Path file;
                ArtifactSpec spec;
                
                String clas = classifier;
                if (clas == null) {
                    clas = defaultClassifier;
                }
                
                if (any || !tagBase || tagShaded) {
                    file = basePath.resolveSibling(basePath.getFileName() + suffix + "-" + typeSuffix + ".jar");
                    spec = ArtifactSpec.builder(mA.getGroupId(), artifactId, mA.getVersion(), evalProject).
                        classifier(clas).
                        location(file.toUri()).
                        type(type).
                        tag(ArtifactSpec.TAG_SHADED).
                        tags(tags).
                        build();
                    addArtifactSpec(spec);
                }

                if (any || tagBase) {
                    file = basePath.resolveSibling("original-" + basePath.getFileName() + suffix + "-" + typeSuffix + ".jar");
                    spec = ArtifactSpec.builder(attached ? mA.getGroupId() : null, attached ? artifactId : null, mA.getVersion(), evalProject).
                            classifier(clas).
                            location(file.toUri()).
                            type(type).
                            tag(ArtifactSpec.TAG_BASE).
                            tags(tags).
                            build();
                    addArtifactSpec(spec);
                }
            }

            /**
             * Include only if shaded was explicitly requested, or ALL
             * classifiers. Otherwise the shaded artifact does not replace the
             * main artifact and is not 'officially' attached, so it should be
             * probably not reported at all, as it is invisible outside the
             * project.
             */
            void addExplicitOutputFile() {
                if ((ProjectArtifactsQuery.Filter.TYPE_ALL.equals(t) || t == null || "jar".equals(t))
                        && (any || (c == null && filter.hasTag(ArtifactSpec.TAG_SHADED)))) {
                    Path file = null;
                    try {
                        file = Paths.get(outputFile);
                        ArtifactSpec spec = ArtifactSpec.builder(null, null, mA.getVersion(), evalProject).
                                classifier(classifier).
                                location(file.toUri()).
                                type("jar").
                                tag(ArtifactSpec.TAG_SHADED).
                                build();
                        addArtifactSpec(spec);
                    } catch (InvalidPathException ex) {
                        // no main artifact produced.
                    }
                } else {

                }
                if ((ProjectArtifactsQuery.Filter.TYPE_ALL.equals(t) || t == null || "jar".equals(t))
                        && (any || (c == null && (filter.hasTag(ArtifactSpec.TAG_BASE) || filter.hasTag(DEFAULT_SHADE_CLASSIFIER))))) {
                    // include the original, but tag it with base
                    Path p = basePath.resolveSibling("original-" + basePath.getFileName() + ".jar"); // NOI18N
                    ArtifactSpec spec = ArtifactSpec.builder(null, null, mA.getVersion(), evalProject).
                            classifier(classifier).
                            location(p.toUri()).
                            type("jar").
                            tag(ArtifactSpec.TAG_BASE).
                            build();
                    addArtifactSpec(spec);
                }
                // the other possible attachments are not afffected by 'outputFile'
            }

        }

        private void appendShadePluginOutput(MavenProject evalProject) {
            Plugin plugin = evalProject.getBuild().getPluginsAsMap().get(Constants.GROUP_APACHE_PLUGINS + ":" + "maven-shade-plugin"); // NOI18N
            if (plugin == null) {
                return;
            }
            for (PluginExecution exec : plugin.getExecutions()) {
                if (exec.getGoals().contains("shade")) {
                    ShadedEvaluator shadedEval = new ShadedEvaluator(evalProject, exec);
                    shadedEval.process();
                }
            }
        }

        private static String getValueOrNull(Xpp3Dom parent, String childName) {
            return getChildValue(parent, childName, null);
        }

        private static String getChildValue(Xpp3Dom parent, String childName, String defValue) {
            Xpp3Dom child = null;
            if (parent != null) {
                child = parent.getChild(childName);
            }
            return child != null ? child.getValue() : defValue;
        }
        
        private void appendPluginOutput(MavenProject evalProject, String pluginId, String goal, String packagingAndType) {
            appendPluginOutput(evalProject, pluginId, goal, packagingAndType, packagingAndType, null);
        }

        private void appendPluginOutput(MavenProject evalProject, String pluginId, String goal, String type, String packaging, String defaultClassifier, String... tags) {
            if (filter != null) {
                if (filter.getArtifactType() == null) {
                    if (!evalProject.getPackaging().equals(packaging)) {
                        return;
                    }
                } else if (!filter.getArtifactType().equals(type)) {
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
                    if (classifier == null) {
                        classifier = defaultClassifier;
                    }

                    if (filter != null && !ProjectArtifactsQuery.Filter.CLASSIFIER_ANY.equals(filter.getClassifier())) {
                        if (!Objects.equals(classifier, filter.getClassifier())) {
                            return;
                        }
                    }
                    StringBuilder finalNameExt = new StringBuilder(mdl.getBuild().getFinalName());
                    if (classifier != null) {
                        finalNameExt.append("-").append(classifier);
                    }
                    finalNameExt.append(".").append(packaging);

                    ArtifactSpec.Builder builder = ArtifactSpec.builder(mA.getGroupId(), mA.getArtifactId(), mA.getVersion(),
                            nbMavenProject.getMavenProject().getArtifact())
                            .classifier(classifier)
                            .tags(tags)
                            .type(type);
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
                    addArtifactSpec(builder.build());
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
            evalProject = mp;

            String packaging = mdl.getPackaging();
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            
            appendShadePluginOutput(evalProject);

            if (!skipDefaultOutput) {
                appendPluginOutput(mp, Constants.PLUGIN_JAR, NbMavenProject.TYPE_JAR, NbMavenProject.TYPE_JAR);
            }
            appendPluginOutput(mp, Constants.PLUGIN_JAR, "test-jar", "test-jar", NbMavenProject.TYPE_JAR, ArtifactSpec.CLASSIFIER_TESTS);
            appendPluginOutput(mp, "maven-source-plugin", "jar", "sources", NbMavenProject.TYPE_JAR, ArtifactSpec.CLASSIFIER_SOURCES, "java-source");
            appendPluginOutput(mp, "maven-source-plugin", "test-jar", "sources", NbMavenProject.TYPE_JAR, ArtifactSpec.CLASSIFIER_TEST_SOURCES, "java-source", "test");
            
            appendPluginOutput(mp, Constants.PLUGIN_WAR, NbMavenProject.TYPE_WAR, NbMavenProject.TYPE_WAR);
            appendPluginOutput(mp, Constants.PLUGIN_EAR, NbMavenProject.TYPE_EAR, NbMavenProject.TYPE_EAR);
            appendPluginOutput(mp, Constants.PLUGIN_EJB, NbMavenProject.TYPE_EJB, NbMavenProject.TYPE_EJB);
        }
    }

    private static final RequestProcessor MAVEN_ARTIFACTS_RP = new RequestProcessor(MavenArtifactsImplementation.class);

    static class Res implements PropertyChangeListener {

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

        public Project getProject() {
            return project;
        }

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
                    ll = listeners.toArray(new ChangeListener[0]);
                }
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
        }

        public Collection<ArtifactSpec> getExcludedArtifacts() {
            return null;
        }

        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (listeners == null) {
                    NbMavenProject mvnProject = project.getLookup().lookup(NbMavenProject.class);
                    mvnProject.addPropertyChangeListener(WeakListeners.propertyChange(this, NbMavenProject.PROP_PROJECT, mvnProject));
                }
                listeners.add(l);
            }
        }

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
                artifacts = null;

                arr[0] = refreshTask = MAVEN_ARTIFACTS_RP.create(() -> update(copy, arr[0]));
            }
        }

        public boolean supportsChanges() {
            return true;
        }
    }
}

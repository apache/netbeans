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
package org.netbeans.modules.maven.refactoring.dependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scopes;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class DependencyAdder {
    private static final String ELEMENT_VERSION = "version"; // NOI18N
    private static final String ELEMENT_ARTIFACT_ID = "artifactId"; // NOI18N
    private static final String ELEMENT_GROUP_ID = "groupId"; // NOI18N
    private static final String ELEMENT_PATH = "path"; // NOI18N
    private static final String ELEMENT_PROCESSOR_PATHS = "annotationProcessorPaths"; // NOI18N

    private final Project project;
    private final DependencyChange request;
    private final RewriteContext rewrite;
    // will be initialized during execute()
    private POMModel mutableModel;
    
    public DependencyAdder(Project project, DependencyChange request, RewriteContext rewrite) {
        this.project = project;
        this.request = request;
        this.rewrite = rewrite;
    }
    
    private List<Dependency> accepted = new ArrayList<>();
    protected Map<Dependency, Dependency> offending = new HashMap<>();
    
    protected void throwDependencyConflicts() throws DependencyChangeException {
        if (!offending.isEmpty()) {
            throw new DependencyChangeException(request, DependencyChangeException.Reason.CONFLICT, offending);
        }
    }

    protected void throwUnknownScope(Dependency d) throws DependencyChangeException {
        throw new DependencyChangeException(request, d, DependencyChangeException.Reason.MALFORMED);
    }
    
    protected void recordConflict(Dependency requested, Dependency existing) {
        if (!request.getOptions().contains(DependencyChange.Options.skipConflicts)) {
            offending.putIfAbsent(requested, existing);
        }
    }
    
    private ArtifactSpec mavenToArtifactSpec(Artifact a) {
        FileObject f = a.getFile() == null ? null : FileUtil.toFileObject(a.getFile());
        if (a.isSnapshot()) {
            return ArtifactSpec.createSnapshotSpec(a.getGroupId(), a.getArtifactId(), 
                    a.getType(), a.getClassifier(), a.getVersion(), a.isOptional(), f, a);
        } else {
            return ArtifactSpec.createVersionSpec(a.getGroupId(), a.getArtifactId(), 
                    a.getType(), a.getClassifier(), a.getVersion(), a.isOptional(), f, a);
        }
    }
    
    protected boolean checkDependencyConflicts(ArtifactSpec existingA, ArtifactSpec toAdd) throws DependencyChangeException {
        if (!(existingA.getGroupId().equals(toAdd.getGroupId()) &&
            existingA.getArtifactId().equals(toAdd.getArtifactId()))) {
            // different artifacts -> no conflicts
            return true;
        }
        String existingC = existingA.getClassifier();
        if (existingC != null) {
            if (!Objects.equals(existingC, toAdd.getClassifier())) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean checkDependencyConflicts(Dependency existing, Dependency d) throws DependencyChangeException {
        if (checkDependencyConflicts(existing.getArtifact(), d.getArtifact())) {
            return true;
        }
        String mavenScope = rewrite.mavenScope(d);
        String existingScope = rewrite.mavenScope(existing);
        if (!mavenScope.equals(existingScope)) {
            // second chance -- the specified scope could be a meta-scope that maps to Gradle configuration
            return true;
        }
        recordConflict(d, existing);
        return false;
    }
    
    DependencyResult current;
    
    private void addProcessorDependency(Dependency d) {
        ArtifactSpec a = d.getArtifact();
        Build pBuild = mutableModel.getProject().getBuild();
        if (pBuild == null) {
            pBuild = mutableModel.getFactory().createBuild();
            mutableModel.getProject().setBuild(pBuild);
        }
        Plugin plg = pBuild.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        if (plg == null) {
            plg = mutableModel.getFactory().createPlugin();
            plg.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plg.setArtifactId(Constants.PLUGIN_COMPILER);
            plg.setVersion(MavenVersionSettings.getDefault().getVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER));
            pBuild.addPlugin(plg);
        }
        Configuration config = plg.getConfiguration();
        if (config == null) {
            config = mutableModel.getFactory().createConfiguration();
            plg.setConfiguration(config);
        }
        Configuration fConfig = config;
        POMExtensibilityElement annoPathList = 
                config.getConfigurationElements().stream().filter(e -> e.getQName().getLocalPart().equals(ELEMENT_PROCESSOR_PATHS)).
                findAny().orElseGet(() -> {
            POMExtensibilityElement e = mutableModel.getFactory().createPOMExtensibilityElement(new QName(ELEMENT_PROCESSOR_PATHS));
            fConfig.addExtensibilityElement(e);
            return e;
        });
        POMExtensibilityElement path = mutableModel.getFactory().createPOMExtensibilityElement(new QName(ELEMENT_PATH));
        path.setChildElementText(null, a.getGroupId(), new QName(ELEMENT_GROUP_ID));
        path.setChildElementText(null, a.getArtifactId(), new QName(ELEMENT_ARTIFACT_ID));
        if (a.getVersionSpec() != null) {
            path.setChildElementText(null, a.getVersionSpec(), new QName(ELEMENT_VERSION));
        }
        annoPathList.addExtensibilityElement(path);
    }
    
    private void addCodeDependency(Dependency d) {
        ArtifactSpec a = d.getArtifact();
        org.netbeans.modules.maven.model.pom.Dependency mavenDep = mutableModel.getFactory().createDependency();
        mavenDep.setGroupId(a.getGroupId());
        mavenDep.setArtifactId(a.getArtifactId());
        if (a.getVersionSpec() != null && !a.getVersionSpec().isEmpty()) {
            mavenDep.setVersion(a.getVersionSpec());
        }
        if (a.getClassifier() != null) {
            mavenDep.setClassifier(a.getClassifier());
        }
        String scope = rewrite.mavenScope(d);
        if (!"compile".equals(scope)) {
            mavenDep.setScope(scope);
        }
        mutableModel.getProject().addDependency(mavenDep);
    }
    
    @NbBundle.Messages({
        "ERR_AddingDependency=Error adding dependency"
    })
    public void execute() throws DependencyChangeException {
        current = ProjectDependencies.findDependencies(project, ProjectDependencies.newQuery(Scopes.DECLARED));
        NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject mp = nbmp.getMavenProject();
        Map<String, org.apache.maven.model.Plugin> plugins = mp.getBuild().getPluginsAsMap();
        // lazy initialized if PROCESS dependency is found
        List<ArtifactSpec> annotationPath = null;
        try {
            mutableModel = rewrite.getWriteModel();
            for (Dependency d : request.getDependencies()) {
                boolean toAccept = true;
                for (Dependency c : current.getRoot().getChildren()) {
                    if (!checkDependencyConflicts(c, d)) {
                        toAccept = false;
                        break;
                    }
                }
                // check annotation processors, they are elsewhere in the POM
                if (d.getScope().equals(Scopes.PROCESS)) {
                    if (annotationPath == null) {
                        // lazy init
                        PluginPropertyUtils.PluginConfigPathParams params = new PluginPropertyUtils.PluginConfigPathParams(
                            Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, ELEMENT_PROCESSOR_PATHS, ELEMENT_PATH);
                        List<Artifact> arts = PluginPropertyUtils.getPluginPathProperty(project, params, true, null);
                        if (arts != null) {
                            annotationPath = PluginPropertyUtils.getPluginPathProperty(project, params, true, null).stream().map(this::mavenToArtifactSpec).toList();
                        }
                        if (annotationPath == null) {
                            annotationPath = Collections.emptyList();
                        }
                    }
                    for (ArtifactSpec a : annotationPath) {
                        if (d.getArtifact() == null) {
                            // TODO: handle projects
                            continue;
                        }
                        if (!checkDependencyConflicts(a, d.getArtifact())) {
                            toAccept = false;
                            break;
                        }
                    }
                }
                if (toAccept) {
                    accepted.add(d);
                }
            }

            throwDependencyConflicts();

            mutableModel.sync();            
            mutableModel.startTransaction();
            boolean ok = false;
            try {
                for (Dependency d : accepted) {
                    if (d.getScope().equals(Scopes.PROCESS)) {
                        addProcessorDependency(d);
                    } else {
                        addCodeDependency(d);
                    }
                }
                mutableModel.endTransaction();
                ok = true;
            } finally {
                if (!ok) {
                    mutableModel.rollbackTransaction();
                }
            }
        } catch (IOException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.UNSUPPORTED, Bundle.ERR_CannotModifyProject(rewrite.getPomFile()));
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_AddingDependency(), ex);
        }
    }
}

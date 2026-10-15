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
package org.netbeans.modules.fish.payara.micro.plugin;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import static java.util.stream.Collectors.toSet;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import org.apache.maven.model.Model;
import org.netbeans.modules.maven.model.pom.Resource;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.fish.payara.micro.plugin.TemplateUtil.loadResource;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyContainer;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryPolicy;
import org.openide.filesystems.FileObject;
import static org.openide.filesystems.FileUtil.toFileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class POMManager {

    private Project project;
    
    private FileObject pomFileObject;
    
    private POMModel pomModel;
    
    private NbMavenProject mavenProject;

    private Model sourceModel;
    
    private List<ModelOperation<POMModel>> operations;
    
    private static final RequestProcessor RP = new RequestProcessor(POMManager.class);

    private BiFunction<Xpp3Dom, POMExtensibilityElement, Boolean> extensionOverrideFilter;

    public POMManager(Project project){
        this(project, false);
    }
    
    public POMManager(Project project, boolean readonly) {
        this.project = project;
        mavenProject = project.getLookup().lookup(org.netbeans.modules.maven.api.NbMavenProject.class);
        pomFileObject = toFileObject(mavenProject.getMavenProject().getFile());
        pomModel = POMModelFactory.getDefault().createFreshModel(Utilities.createModelSource(pomFileObject));
        operations = new ArrayList<>();
        if(!readonly) {
            pomModel.startTransaction();
        }
    }

    public POMManager(String inputResource, Project project) {
        this(project, false);
        try {
            //source
            ModelReader reader = EmbedderFactory.getProjectEmbedder().lookupComponent(ModelReader.class);
            sourceModel = reader.read(loadResource(inputResource), Collections.singletonMap(ModelReader.IS_STRICT, false));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public POMManager(Reader inputResource, Project project) {
        this(project, false);
        try {
            //source
            ModelReader reader = EmbedderFactory.getProjectEmbedder().lookupComponent(ModelReader.class);
            sourceModel = reader.read(inputResource, Collections.singletonMap(ModelReader.IS_STRICT, false));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void fixDistributionProperties() {
        org.netbeans.modules.maven.model.pom.Project pomProject = getPOMProject();
        if (pomProject.getGroupId() != null) {
            pomProject.setGroupId(pomProject.getGroupId().toLowerCase());
        }
        if (pomProject.getArtifactId() != null) {
            pomProject.setArtifactId(pomProject.getArtifactId().toLowerCase());
        }
        if (pomProject.getVersion() != null) {
            pomProject.setVersion(pomProject.getVersion().toLowerCase());
        }
    }

    private org.netbeans.modules.maven.model.pom.Project getPOMProject() {
        return pomModel.getProject();
    }

    public String getGroupId() {
        return getPOMProject().getGroupId();
    }

    public String getArtifactId() {
        return getPOMProject().getArtifactId();
    }

    public String getVersion() {
        return getPOMProject().getVersion();
    }

    public void addProperties(java.util.Properties prop) {
        addProperties(null, prop);
    }

    public void addProperties(String profile, java.util.Properties prop) {
        boolean tx = pomModel.isIntransaction();
        try {
            if (!tx) {
                pomModel.startTransaction();
            }
            org.netbeans.modules.maven.model.pom.Project pomProject = getPOMProject();
            if (profile != null) {
                Profile targetProfile = pomProject.findProfileById(profile);
                if (targetProfile == null) {
                    throw new IllegalArgumentException(String.format("Profile[%s] not exist", profile));
                }
                if (targetProfile.getProperties() == null) {
                        targetProfile.setProperties(pomModel.getFactory().createProperties());
                    }
                registerProperties(prop, targetProfile.getProperties());
            } else {
                registerProperties(prop, pomProject.getProperties());
            }
        } finally {
            if (!tx) {
                pomModel.endTransaction();
            }
        }
    }

    public void setExtensionOverrideFilter(BiFunction<Xpp3Dom, POMExtensibilityElement, Boolean> extensionOverrideFilter) {
        this.extensionOverrideFilter = extensionOverrideFilter;
    }

    public static boolean isMavenProject(Project project) {
        return project.getLookup().lookup(NbMavenProject.class) != null;
    }

    public void reload() {
        reload(project);
    }
    
    public static void reload(Project project) {
        NbMavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class);
        try {
            FileObject pomFileObject = toFileObject(mavenProject.getMavenProject().getFile());
            POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pomFileObject));
            Utilities.saveChanges(model);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        RP.post(() -> {
            mavenProject.triggerDependencyDownload();
        });

        SwingUtilities.invokeLater(() -> NbMavenProject.fireMavenProjectReload(project));
    }

    private void execute() {
        if (sourceModel != null) {
            org.netbeans.modules.maven.model.pom.Project pomProject = getPOMProject();
            if (pomProject.getProperties() == null) {
                pomProject.setProperties(pomModel.getFactory().createProperties());
            }
            registerProperties(sourceModel.getProperties(), pomProject.getProperties());
            pomProject.setDependencyManagement(registerDependencyManagement(sourceModel.getDependencyManagement(), pomProject.getDependencyManagement()));
            registerDependency(sourceModel.getDependencies(), pomProject);
            registerRepository();
            registerBuild();
            registerProfile();
        }
    }

    private Properties registerProperties(java.util.Properties source, Properties target) {
        if (source != null && !source.isEmpty()) {
            if (target == null) {
                target = pomModel.getFactory().createProperties();
            }
            for (String sourceKey : source.stringPropertyNames()) {
                String sourceValue = source.getProperty(sourceKey);
                target.setProperty(sourceKey, sourceValue);
            }
        }
        return target;
    }

    private void registerBuild() {
        if (sourceModel.getBuild() != null) {
            registerBuildBase(sourceModel.getBuild(), getPOMProject().getBuild());
        }
    }

    private BuildBase registerBuildBase(org.apache.maven.model.BuildBase sourceBuild, BuildBase targetBuild) {
        if (sourceBuild == null) {
            return targetBuild;
        }
        if (targetBuild == null) {
            targetBuild = pomModel.getFactory().createBuild();
        }
        if (sourceBuild.getFinalName() != null) {
            targetBuild.setFinalName(sourceBuild.getFinalName());
        }
        if (sourceBuild.getResources()!= null && !sourceBuild.getResources().isEmpty()) {
            for (org.apache.maven.model.Resource sourceResource : sourceBuild.getResources()) {
                Resource targetResource = pomModel.getFactory().createResource();
                targetResource.setFiltering(Boolean.parseBoolean(sourceResource.getFiltering()));
                targetResource.setDirectory(sourceResource.getDirectory());
                targetBuild.addResource(targetResource);
            }
        }
        if (sourceBuild.getPlugins() != null && !sourceBuild.getPlugins().isEmpty()) {
            for (org.apache.maven.model.Plugin sourcePlugin : sourceBuild.getPlugins()) {
                Plugin targetPlugin = targetBuild.findPluginById(sourcePlugin.getGroupId(), sourcePlugin.getArtifactId());
                if (targetPlugin == null) {
                    targetPlugin = pomModel.getFactory().createPlugin();
                    targetPlugin.setGroupId(sourcePlugin.getGroupId());
                    targetPlugin.setArtifactId(sourcePlugin.getArtifactId());
                    targetBuild.addPlugin(targetPlugin);
                }
                if (sourcePlugin.getExtensions() != null) {
                    targetPlugin.setExtensions(Boolean.TRUE);
                }
                registerDependency(sourcePlugin.getDependencies(), targetPlugin);
                targetPlugin.setConfiguration(registerConfiguration(sourcePlugin.getConfiguration(), targetPlugin.getConfiguration()));
                if (sourcePlugin.getExecutions() != null && !sourcePlugin.getExecutions().isEmpty()) {
                    for (org.apache.maven.model.PluginExecution sourceExecution : sourcePlugin.getExecutions()) {
                        PluginExecution targetExecution = targetPlugin.findExecutionById(sourceExecution.getId());
                        if (targetExecution == null) {
                            targetExecution = pomModel.getFactory().createExecution();
                            targetExecution.setId(sourceExecution.getId());
                            sourceExecution.getGoals().forEach(targetExecution::addGoal);
                            targetPlugin.addExecution(targetExecution);
                        }
                        targetExecution.setPhase(sourceExecution.getPhase());
                        targetExecution.setConfiguration(registerConfiguration(sourceExecution.getConfiguration(), targetExecution.getConfiguration()));

                    }
                }
                targetPlugin.setVersion(sourcePlugin.getVersion());
            }
        }
        return targetBuild;
    }

    private Configuration registerConfiguration(Object sourceConfig, Configuration targetConfig) {
        if (sourceConfig != null) {
            Xpp3Dom parentDOM = (Xpp3Dom) sourceConfig;
            if (targetConfig == null) {
                targetConfig = pomModel.getFactory().createConfiguration();
            }
            loadDom(parentDOM, targetConfig);
        }
        return targetConfig;
    }

    private void loadDom(Xpp3Dom source, POMComponent target) {

        for (Xpp3Dom childDOM : source.getChildren()) {
            if (childDOM.getValue() != null) {
                if (target instanceof Configuration) {
                    ((Configuration) target).setSimpleParameter(childDOM.getName(), childDOM.getValue());
                } else if (target instanceof POMExtensibilityElement) {
                    Optional<POMComponent> targetComponentOptioal = ((POMExtensibilityElement) target).getChildren()
                            .stream()
                            .filter(targetElement -> {
                                String nodeName = targetElement.getPeer().getNodeName();
                                nodeName = nodeName.substring(nodeName.lastIndexOf(':') + 1);
                                String nodeValue = targetElement.getPeer().getChildNodes().item(0).getNodeValue();
                                return nodeName != null && nodeName.equals(childDOM.getName())
                                        && nodeValue != null && nodeValue.equals(childDOM.getValue());
                            })
                            .findAny();
                    if (!targetComponentOptioal.isPresent()) {
                        POMExtensibilityElement element = pomModel.getFactory().createPOMExtensibilityElement(new QName(childDOM.getName()));
                        element.setElementText(childDOM.getValue());
                        target.addExtensibilityElement(element);
                    }
                } else {
                    target.setChildElementText("propertyName", childDOM.getValue(), new QName(childDOM.getName()));
                }
            } else {
                Optional<POMExtensibilityElement> targetElementOptioal = target.getExtensibilityElements()
                        .stream()
                        .filter(targetElement -> targetElement.getQName().getLocalPart().equals(childDOM.getName()))
                        .filter(targetElement -> extensionOverrideFilter == null || extensionOverrideFilter.apply(childDOM, targetElement))
                        .findAny();
                
                POMExtensibilityElement element;
                if (targetElementOptioal.isPresent()) {
                    element = targetElementOptioal.get();
                } else {
                    element = pomModel.getFactory().createPOMExtensibilityElement(new QName(childDOM.getName()));
                    target.addExtensibilityElement(element);
                }
                loadDom(childDOM, element);
            }
        }
    }

    private void registerProfile() {
        if (!sourceModel.getProfiles().isEmpty()) {
            org.netbeans.modules.maven.model.pom.Project pomProject = getPOMProject();
            for (org.apache.maven.model.Profile sourceProfile : sourceModel.getProfiles()) {
                Profile targetProfile = pomProject.findProfileById(sourceProfile.getId());
                if (targetProfile == null) {
                    targetProfile = pomModel.getFactory().createProfile();
                    pomProject.addProfile(targetProfile);
                    targetProfile.setId(sourceProfile.getId());
                    targetProfile.setProperties(registerProperties(sourceProfile.getProperties(), targetProfile.getProperties()));
                    if (sourceProfile.getActivation() != null) {
                        Activation activation = pomModel.getFactory().createActivation();
                        targetProfile.setActivation(activation);
                        if (sourceProfile.getActivation().getProperty() != null) {
                            org.apache.maven.model.ActivationProperty sourceProperty = sourceProfile.getActivation().getProperty();
                            ActivationProperty targetProperty = pomModel.getFactory().createActivationProperty();
                            activation.setActivationProperty(targetProperty);
                            targetProperty.setName(sourceProperty.getName());
                            targetProperty.setValue(sourceProperty.getValue());
                        } else {
                            activation.setChildElementText("activeByDefault", Boolean.toString(sourceProfile.getActivation().isActiveByDefault()), new QName("activeByDefault"));
                        }
                    }
                }
                targetProfile.setDependencyManagement(registerDependencyManagement(sourceProfile.getDependencyManagement(), targetProfile.getDependencyManagement()));
                registerDependency(sourceProfile.getDependencies(), targetProfile);
                targetProfile.setBuildBase(registerBuildBase(sourceProfile.getBuild(), targetProfile.getBuildBase()));
            }
        }
    }

    private DependencyManagement registerDependencyManagement(org.apache.maven.model.DependencyManagement source, DependencyManagement target) {
        if (source != null) {
            if (target == null) {
                target = pomModel.getFactory().createDependencyManagement();
            }
            registerDependency(source.getDependencies(), target);
        }
        return target;
    }

    private void registerDependency(List<org.apache.maven.model.Dependency> source, DependencyContainer target) {
        source.forEach((sourceDependency) -> {
            org.netbeans.modules.maven.model.pom.Dependency targetDependency = target.findDependencyById(sourceDependency.getGroupId(), sourceDependency.getArtifactId(), null);
            if (targetDependency == null) {
                targetDependency = createDependency(sourceDependency);
                target.addDependency(targetDependency);
            }
            updateDependency(sourceDependency, targetDependency);
        });
    }

    public void registerDependency(String groupId, String artifactId, String version, String type, String classifier, String scope) {
        org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        dependency.setClassifier(classifier);
        dependency.setScope(scope);
        registerDependency(Collections.singletonList(dependency), getPOMProject());
    }

    private Dependency createDependency(org.apache.maven.model.Dependency source) {
        Dependency target = pomModel.getFactory().createDependency();
        target.setGroupId(source.getGroupId());
        target.setArtifactId(source.getArtifactId());
        target.setClassifier(source.getClassifier());
        if (!"jar".equals(source.getType())) {
            target.setType(source.getType());
        }
        target.setScope(source.getScope());
        return target;
    }

    private void updateDependency(org.apache.maven.model.Dependency source, Dependency target) {
        target.setVersion(source.getVersion());
        if (source.getExclusions() != null && !source.getExclusions().isEmpty()) {
            for (org.apache.maven.model.Exclusion sourceExclusion : source.getExclusions()) {
                Exclusion targetExclusion = target.findExclusionById(sourceExclusion.getGroupId(), sourceExclusion.getArtifactId());
                if (targetExclusion == null) {
                    targetExclusion = pomModel.getFactory().createExclusion();
                    targetExclusion.setGroupId(sourceExclusion.getGroupId());
                    targetExclusion.setArtifactId(sourceExclusion.getArtifactId());
                    target.addExclusion(targetExclusion);
                }
            }
        }
    }

    private void registerRepository() {
        if (sourceModel.getRepositories().size() > 0) {
            operations.add(pomModel -> {
                Set<String> existingRepositories = getPOMProject().getRepositories() != null ? 
                        getPOMProject().getRepositories()
                                .stream()
                                .map(Repository::getId)
                                .collect(toSet()) 
                        : Collections.emptySet();
                for (org.apache.maven.model.Repository repository : sourceModel.getRepositories()) {
                    if (!existingRepositories.contains(repository.getId())) {
                        Repository repo = pomModel.getFactory().createRepository();
                        repo.setId(repository.getId());
                        repo.setName(repository.getName());
                        repo.setLayout(repository.getLayout());
                        repo.setUrl(repository.getUrl());
                        if (repository.getSnapshots() != null) {
                            RepositoryPolicy policy = pomModel.getFactory().createReleaseRepositoryPolicy();
                            policy.setEnabled(Boolean.valueOf(repository.getSnapshots().getEnabled()));
                            repo.setReleases(policy);
                        }
                        getPOMProject().addRepository(repo);
                    }
                }
            });
        }
    }

    public void setSourceVersion(final String version) {
        operations.add(pomModel -> ModelUtils.setSourceLevel(pomModel, version));
    }

    public void commit() {
        execute();
        if (operations.size() > 0) {
            Utilities.performPOMModelOperations(pomFileObject, operations);
        }
        pomModel.endTransaction();
    }

}

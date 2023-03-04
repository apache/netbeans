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
package org.netbeans.modules.maven.groovy.extender;

import java.util.List;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;

/**
 * Add maven-compiler-plugin into the pom model.
 *
 * This is necessary for compiling both Java and Groovy files together and also
 * for running mixed Java/Groovy JUnit tests.
 *
 * @author Martin Janicek
 */
public class AddMavenCompilerPlugin implements ModelOperation<POMModel> {

    private POMComponentFactory factory;
    private Project project;

    @Override
    public void performOperation(final POMModel model) {
        factory = model.getFactory();
        project = model.getProject();
        Build build = project.getBuild();
        if (build == null) {
            build = factory.createBuild();
            project.setBuild(build);
        }

        Plugin plugin = searchMavenCompilerPlugin(build);
        if (plugin == null) {
            build.addPlugin(createMavenEclipseCompilerPlugin());
        } else {
            Plugin newPlugin = createMavenEclipseCompilerPlugin(plugin);

            build.removePlugin(plugin);
            build.addPlugin(newPlugin);
        }
    }

    private Plugin searchMavenCompilerPlugin(final Build build) {
        List<Plugin> plugins = build.getPlugins();
        if (plugins != null) {
            for (Plugin plugin : plugins) {
                if (MavenConstants.MAVEN_COMPILER_GROUP_ID.equals(plugin.getGroupId())
                        && MavenConstants.MAVEN_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private Plugin createMavenEclipseCompilerPlugin() {
        Plugin plugin = factory.createPlugin();
        plugin.setGroupId(MavenConstants.MAVEN_COMPILER_GROUP_ID);
        plugin.setArtifactId(MavenConstants.MAVEN_COMPILER_ARTIFACT_ID);
        plugin.setVersion(MavenConstants.MAVEN_COMPILER_VERSION);
        plugin.setConfiguration(createConfiguration());
        createCompilerDependency(plugin);
        createBatchDependency(plugin);

        return plugin;
    }

    private Configuration createConfiguration() {
        Configuration configuration = factory.createConfiguration();
        configuration.setSimpleParameter(MavenConstants.COMPILER_ID_PROPERTY, MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID);
        return configuration;
    }

    private void createCompilerDependency(Plugin plugin) {
        Dependency compilerDependency = factory.createDependency();
        compilerDependency.setGroupId(MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID);
        compilerDependency.setArtifactId(MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID);
        compilerDependency.setVersion(MavenConstants.GROOVY_ECLIPSE_COMPILER_VERSION);
        plugin.addDependency(compilerDependency);
    }

    private void createBatchDependency(Plugin plugin) {
        Dependency batchDependency = factory.createDependency();
        batchDependency.setGroupId(MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID);
        batchDependency.setArtifactId(MavenConstants.GROOVY_ECLIPSE_BATCH_ARTIFACT_ID);
        batchDependency.setVersion(MavenConstants.GROOVY_ECLIPSE_BATCH_VERSION);
        plugin.addDependency(batchDependency);
    }

    private Plugin createMavenEclipseCompilerPlugin(final Plugin plugin) {
        Plugin newPlugin = factory.createPlugin();

        newPlugin.setGroupId(plugin.getGroupId());
        newPlugin.setArtifactId(plugin.getArtifactId());
        newPlugin.setVersion(plugin.getVersion());

        updateCompilerDependency(plugin, newPlugin);
        updateBatchDependency(plugin, newPlugin);
        updateConfiguration(plugin, newPlugin);

        return newPlugin;
    }

    /**
     * Just find out if the groovy-eclipse-compiler dependency is already
     * present in maven-compiler-plugin and if not, create on and put it into
     * pom.xml.
     *
     * @param oldPlugin where we search for existing groovy-eclipse-compiler
     * dependency
     * @param newPlugin where we want to update groovy-eclipse-compiler
     * dependency
     */
    private void updateCompilerDependency(Plugin oldPlugin, Plugin newPlugin) {

        List<Dependency> dependencies = oldPlugin.getDependencies();
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                if (MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID.equals(dependency.getGroupId())
                        && MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID.equals(dependency.getArtifactId())) {

                    // Reuse already existing dependency.
                    Dependency newDependency = factory.createDependency();
                    newDependency.setGroupId(dependency.getGroupId());
                    newDependency.setArtifactId(dependency.getArtifactId());
                    newDependency.setVersion(dependency.getVersion());
                    newPlugin.addDependency(newDependency);
                }
            }
        }

        // groovy-eclipse-compiler dependency doesn't exist at the moment, let's create it
        createCompilerDependency(newPlugin);
    }

    /**
     * Just find out if the groovy-eclipse-batch dependency is already present
     * in maven-compiler-batch and if not, create on and put it into pom.xml.
     *
     * @param oldPlugin where we search for existing groovy-eclipse-batch
     * dependency
     * @param newPlugin where we want to update groovy-eclipse-batch dependency
     */
    private void updateBatchDependency(Plugin oldPlugin, Plugin newPlugin) {

        List<Dependency> dependencies = oldPlugin.getDependencies();
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                if (MavenConstants.GROOVY_ECLIPSE_COMPILER_GROUP_ID.equals(dependency.getGroupId())
                        && MavenConstants.GROOVY_ECLIPSE_BATCH_ARTIFACT_ID.equals(dependency.getArtifactId())) {

                    // Reuse already existing dependency.
                    Dependency newDependency = factory.createDependency();
                    newDependency.setGroupId(dependency.getGroupId());
                    newDependency.setArtifactId(dependency.getArtifactId());
                    newDependency.setVersion(dependency.getVersion());
                    newPlugin.addDependency(newDependency);
                }
            }
        }

        // groovy-eclipse-batch dependency doesn't exist at the moment, let's create it
        createBatchDependency(newPlugin);
    }

    private void updateConfiguration(Plugin oldPlugin, Plugin newPlugin) {

        Configuration currentConfiguration = oldPlugin.getConfiguration();
        Configuration newConfiguration = factory.createConfiguration();

        if (currentConfiguration != null) {
            for (POMExtensibilityElement element : currentConfiguration.getConfigurationElements()) {
                POMExtensibilityElement newElement = factory.createPOMExtensibilityElement(element.getQName());
                newElement.setElementText(element.getElementText());

                int position = 0;
                for (POMExtensibilityElement childElement : element.getAnyElements()) {
                    POMExtensibilityElement newChildElement = factory.createPOMExtensibilityElement(childElement.getQName());
                    newChildElement.setElementText(childElement.getElementText());
                    newElement.addAnyElement(newChildElement, position++);
                }

                newConfiguration.addExtensibilityElement(newElement);
            }
        }

        String compilerId = newConfiguration.getSimpleParameter(MavenConstants.COMPILER_ID_PROPERTY);
        if (compilerId == null) {
            newConfiguration.setSimpleParameter(MavenConstants.COMPILER_ID_PROPERTY, MavenConstants.GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID);
        } else {
            newConfiguration.setSimpleParameter(MavenConstants.COMPILER_ID_PROPERTY, compilerId);
        }

        newPlugin.setConfiguration(newConfiguration);
    }
}

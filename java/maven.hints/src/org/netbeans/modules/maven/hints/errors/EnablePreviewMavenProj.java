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
package org.netbeans.modules.maven.hints.errors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginContainer;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Maven type project.
 *
 * @author arusinha
 */
public class EnablePreviewMavenProj implements PreviewEnabler {

    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N

    private final Project prj;

    private EnablePreviewMavenProj(@NonNull final Project prj) {
        Parameters.notNull("prj", prj); //NOI18N
        this.prj = prj;
    }

    @Override
    public void enablePreview(String newSourceLevel) throws Exception {
        final FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
        pom.getFileSystem().runAtomicAction(() -> {
            List<ModelOperation<POMModel>> operations = new ArrayList<>();
            operations.add(new AddMvnCompilerPluginForEnablePreview(newSourceLevel));
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, operations);
        });

        ProjectConfiguration cfg = prj.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();

        ActionConfig[] actions = new ActionConfig[] {
            ActionConfig.runAction("run"), // NOI18N
            ActionConfig.runAction("debug"), // NOI18N
            ActionConfig.runAction("profile"), // NOI18N
            ActionConfig.runAction("run.single.main"), // NOI18N
            ActionConfig.runAction("debug.single.main"), // NOI18N
            ActionConfig.runAction("profile.single.main"), // NOI18N
            ActionConfig.testAction("test"), // NOI18N
            ActionConfig.testAction("test.single"), // NOI18N
            ActionConfig.testAction("debug.test.single"), // NOI18N
            ActionConfig.testAction("profile.test.single"), // NOI18N
        };
        for (ActionConfig action : actions) {
            NetbeansActionMapping mapp = ModelHandle2.getMapping(action.actionName, prj, cfg);
            Map<String, String> properties = mapp.getProperties();
            String existingValue = properties.getOrDefault(action.propertyName, "");

            if (!existingValue.contains(ENABLE_PREVIEW_FLAG)) {
                properties.put(action.propertyName, ENABLE_PREVIEW_FLAG + (existingValue .isEmpty() ? "" : " ") + existingValue);
                ModelHandle2.putMapping(mapp, prj, cfg);
            }

        }
    }

    @Override
    public boolean canChangeSourceLevel() {
        CheckCanChangeSourceLevel canChange = new CheckCanChangeSourceLevel();
        try {
            final FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
            pom.getFileSystem().runAtomicAction(() -> {
                List<ModelOperation<POMModel>> operations = Collections.singletonList(canChange);
                org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, operations);
            });
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
        }
        return canChange.canChangeSourceLevel;
    }
    private static final Logger LOG = Logger.getLogger(EnablePreviewMavenProj.class.getName());

    private static final class ActionConfig {
        public final String actionName;
        public final String propertyName;

        public ActionConfig(String actionName, String propertyName) {
            this.actionName = actionName;
            this.propertyName = propertyName;
        }
        public static ActionConfig runAction(String actionName) {
            return new ActionConfig(actionName, "exec.args");
        }
        public static ActionConfig testAction(String actionName) {
            return new ActionConfig(actionName, "argLine");
        }
    }

    private static class BaseMvnCompilerPluginForEnablePreview {

        protected static final String MAVEN_COMPILER_GROUP_ID = "org.apache.maven.plugins"; // NOI18N
        protected static final String MAVEN_COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
        protected static final String COMPILER_ID_PROPERTY = "compilerId"; // NOI18N
        protected static final String RELEASE = "release"; // NOI18N
        protected static final String RELEASE_PROPERTY = "maven.compiler.release"; // NOI18N
        protected static final String SOURCE = "source"; // NOI18N
        protected static final String SOURCE_PROPERTY = "maven.compiler.source"; // NOI18N
        protected static final String TARGET = "target"; // NOI18N
        protected static final String TARGET_PROPERTY = "maven.compiler.target"; // NOI18N
        protected static final String COMPILER_ARG = "compilerArgs"; // NOI18N
        protected static final String MAVEN_COMPILER_VERSION = "3.11.0"; // NOI18N
        protected static final String ARG = "arg";// NOI18N

        protected Pair<PluginContainer, Plugin> searchMavenCompilerPlugin(final Build build) {
            for (PluginContainer container : new PluginContainer[] {build.getPluginManagement(), build}) {
                if (container == null) {
                    continue;
                }

                List<Plugin> plugins = container.getPlugins();

                if (plugins == null) {
                    continue;
                }

                for (Plugin plugin : plugins) {
                    if ((plugin.getGroupId() == null || MAVEN_COMPILER_GROUP_ID.equals(plugin.getGroupId()))
                        && MAVEN_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                        return Pair.of(container, plugin);
                    }
                }
            }
            return null;
        }

        protected POMExtensibilityElement findElement(Configuration configuration, String requiredName) {
            for (POMExtensibilityElement element : configuration.getConfigurationElements()) {
                if (element.getQName().getLocalPart().equals(requiredName)) {
                    return element;
                }
            }

            return null;
        }
    }

    private static class AddMvnCompilerPluginForEnablePreview extends BaseMvnCompilerPluginForEnablePreview implements ModelOperation<POMModel> {

        private final String newSourceLevel;
        private POMComponentFactory factory;

        public AddMvnCompilerPluginForEnablePreview(String newSourceLevel) {
            this.newSourceLevel = newSourceLevel;
        }

        @Override
        public void performOperation(final POMModel model) {
            factory = model.getFactory();
            org.netbeans.modules.maven.model.pom.Project proj = model.getProject();
            Build build = model.getProject().getBuild();
            if (build == null) {
                build = factory.createBuild();
                proj.setBuild(build);
            }
            
            Pair<PluginContainer, Plugin> containerAndPlugin = searchMavenCompilerPlugin(build);

            if (containerAndPlugin == null) {
                build.addPlugin(createMavenCompilerPlugin());
            } else {
                PluginContainer container = containerAndPlugin.first();
                Plugin oldPlugin = containerAndPlugin.second();
                Plugin newPlugin = updateMavenCompilerPlugin(oldPlugin);
                container.removePlugin(oldPlugin);
                container.addPlugin(newPlugin);
            }
        }

        private Plugin createMavenCompilerPlugin() {
            Plugin plugin = factory.createPlugin();
            plugin.setGroupId(MAVEN_COMPILER_GROUP_ID);
            plugin.setArtifactId(MAVEN_COMPILER_ARTIFACT_ID);
            plugin.setVersion(MAVEN_COMPILER_VERSION);
            plugin.setConfiguration(updateMavenCompilerPluginConfiguration(createConfiguration(), MAVEN_COMPILER_VERSION));
            return plugin;
        }

        private Configuration createConfiguration() {
            Configuration configuration = factory.createConfiguration();
            return configuration;
        }

        private Plugin updateMavenCompilerPlugin(final Plugin oldPlugin) {
            Configuration currenConfig = oldPlugin.getConfiguration();
            Plugin newPlugin = factory.createPlugin();
            newPlugin.setGroupId(oldPlugin.getGroupId());
            newPlugin.setArtifactId(oldPlugin.getArtifactId());
            newPlugin.setVersion(oldPlugin.getVersion());
            newPlugin.setConfiguration(updateMavenCompilerPluginConfiguration(currenConfig, oldPlugin.getVersion()));
            return newPlugin;
        }

        private Configuration updateMavenCompilerPluginConfiguration(Configuration currenConfig, String version) {
            Configuration newConfiguration = createConfiguration();

            if (currenConfig == null) {
                currenConfig = createConfiguration();
            }

            boolean supportsRelease = version == null
                    || new ComparableVersion(version).compareTo(new ComparableVersion("3.6")) >= 0;

            Map<POMExtensibilityElement, POMExtensibilityElement> old2New = new HashMap<>();

            if (newSourceLevel != null) {
                POMExtensibilityElement releaseConfig = findElement(currenConfig, RELEASE);
                POMExtensibilityElement sourceConfig = findElement(currenConfig, SOURCE);
                POMExtensibilityElement targetConfig = findElement(currenConfig, TARGET);
                Properties properties = currenConfig.getModel().getProject().getProperties();

                if (releaseConfig != null && supportsRelease) {
                    //TODO: should check re-writability earlier
                    try {
                        new SpecificationVersion(releaseConfig.getElementText().trim());
                        //OK to update
                        POMExtensibilityElement newReleaseElement = factory.createPOMExtensibilityElement(releaseConfig.getQName());
                        newReleaseElement.setElementText(newSourceLevel);
                        old2New.put(releaseConfig, newReleaseElement);
                    } catch (NumberFormatException ex) {
                        //not safe to upgrade
                    }
                } else if (sourceConfig != null) {
                    try {
                        new SpecificationVersion(sourceConfig.getElementText().trim());
                        new SpecificationVersion(targetConfig.getElementText().trim());
                        //OK to update
                        POMExtensibilityElement newSourceElement = factory.createPOMExtensibilityElement(sourceConfig.getQName());
                        newSourceElement.setElementText(newSourceLevel);
                        old2New.put(sourceConfig, newSourceElement);
                        POMExtensibilityElement newTargetElement = factory.createPOMExtensibilityElement(targetConfig.getQName());
                        newTargetElement.setElementText(newSourceLevel);
                        old2New.put(targetConfig, newTargetElement);
                    } catch (NumberFormatException ex) {
                        //not safe to upgrade
                    }
                } else if (properties != null && properties.getProperty(RELEASE_PROPERTY) != null && supportsRelease) {
                    properties.setProperty(RELEASE_PROPERTY, newSourceLevel);
                } else if (properties != null && properties.getProperty(SOURCE_PROPERTY) != null) {
                    properties.setProperty(SOURCE_PROPERTY, newSourceLevel);
                    properties.setProperty(TARGET_PROPERTY, newSourceLevel);
                } else {
                    if (properties == null) {
                        properties = factory.createProperties();
                        currenConfig.getModel().getProject().setProperties(properties);
                    }

                    if (supportsRelease) {
                        properties.setProperty(RELEASE_PROPERTY, newSourceLevel);
                    } else {
                        properties.setProperty(SOURCE_PROPERTY, newSourceLevel);
                        properties.setProperty(TARGET_PROPERTY, newSourceLevel);
                    }
                }
            }

            POMExtensibilityElement compilerArgsConfig = findElement(currenConfig, COMPILER_ARG);
            if (compilerArgsConfig != null) {
                POMExtensibilityElement newElement = factory.createPOMExtensibilityElement(POMQName.createQName(COMPILER_ARG));

                for (POMExtensibilityElement nested : compilerArgsConfig.getAnyElements()) {
                    newElement.addExtensibilityElement((POMExtensibilityElement) nested.copy(newElement));
                }

                newElement.setChildElementText(COMPILER_ID_PROPERTY, ENABLE_PREVIEW_FLAG, POMQName.createQName(ARG));
                old2New.put(compilerArgsConfig, newElement);
            }

            for (POMExtensibilityElement element : currenConfig.getConfigurationElements()) {
                POMExtensibilityElement replacement = old2New.get(element);

                if (replacement == null) {
                    replacement = (POMExtensibilityElement) element.copy(newConfiguration);
                }

                newConfiguration.addExtensibilityElement(replacement);
            }

            if (compilerArgsConfig == null) {
                POMExtensibilityElement compilerArgs = factory.createPOMExtensibilityElement(POMQName.createQName(COMPILER_ARG));
                compilerArgs.setChildElementText(COMPILER_ID_PROPERTY, ENABLE_PREVIEW_FLAG, POMQName.createQName(ARG));
                newConfiguration.addExtensibilityElement(compilerArgs);
            }

            return newConfiguration;
        }

    }

    private static class CheckCanChangeSourceLevel extends BaseMvnCompilerPluginForEnablePreview implements ModelOperation<POMModel> {

        private boolean canChangeSourceLevel;

        @Override
        public void performOperation(final POMModel model) {
            Build build = model.getProject().getBuild();
            Pair<PluginContainer, Plugin> containerAndPlugin = build != null ? searchMavenCompilerPlugin(build) : null;
            Plugin plugin = containerAndPlugin != null ? containerAndPlugin.second() : null;
            Configuration configuration = plugin != null ? plugin.getConfiguration() : null;

            if (configuration != null) {
                POMExtensibilityElement releaseConfig = findElement(configuration, RELEASE);
                POMExtensibilityElement sourceConfig = findElement(configuration, SOURCE);
                POMExtensibilityElement targetConfig = findElement(configuration, TARGET);

                try {
                    if (releaseConfig != null) {
                        new SpecificationVersion(releaseConfig.getElementText().trim());
                    } else if (sourceConfig != null) {
                        new SpecificationVersion(sourceConfig.getElementText().trim());
                        new SpecificationVersion(targetConfig.getElementText().trim());
                    }
                    //safe to upgrade:
                    canChangeSourceLevel = true;
                } catch (NumberFormatException ex) {
                    //not safe to upgrade
                }
            } else {
                //safe to upgrade:
                canChangeSourceLevel = true;
            }
        }

    }

    @ServiceProvider(service=Factory.class, position=1000)
    public static class FactoryImpl implements Factory {

        @Override
        public PreviewEnabler enablerFor(FileObject file) {
            final Project prj = FileOwnerQuery.getOwner(file);
            if (isMavenProject(prj)) {
                return new EnablePreviewMavenProj(prj);
            } else {
                return null;
            }
        }

        private boolean isMavenProject(Project prj) {
            if (prj == null) {
                return false;
            }
            FileObject prjDir = prj.getProjectDirectory();
            if (prjDir == null) {
                return false;
            }

            FileObject pom = prjDir.getFileObject("pom.xml");
            return (pom != null) && pom.isValid();

        }

    }

}

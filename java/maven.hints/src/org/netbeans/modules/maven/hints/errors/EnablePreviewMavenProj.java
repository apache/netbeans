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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.SourceVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
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
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileSystem;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Maven type project.
 *
 * @author arusinha
 */
public class EnablePreviewMavenProj implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.preview.feature.disabled.plural")); // NOI18N
    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N

    @Override
    public Set<String> getCodes() {
        return Collections.unmodifiableSet(ERROR_CODES);
    }

    @Override
    @NonNull
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {

        if (SourceVersion.latest() != compilationInfo.getSourceVersion()) {
            return Collections.<Fix>emptyList();
        }

        Fix fix = null;
        final FileObject file = compilationInfo.getFileObject();
        if (file != null) {
            final Project prj = FileOwnerQuery.getOwner(file);
            if (isMavenProject(prj)) {
                fix = new EnablePreviewMavenProj.ResolveMvnFix(prj);
            } else {
                fix = null;
            }

        }
        return (fix != null) ? Collections.<Fix>singletonList(fix) : Collections.<Fix>emptyList();
    }

    @Override
    public String getId() {
        return EnablePreviewMavenProj.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EnablePreviewMavenProj.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(EnablePreviewMavenProj.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class ResolveMvnFix implements Fix {

        private final Project prj;

        ResolveMvnFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(EnablePreviewMavenProj.class, "FIX_EnablePreviewFeature");
        }

        @Override
        public ChangeInfo implement() throws Exception {

            try {

                final FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
                pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
                        operations.add(new AddMvnCompilerPluginForEnablePreview());
                        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, operations);
                    }
                });

            } catch (IOException ex) {
            }
            ProjectConfiguration cfg = prj.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();

            for (String action : new String[]{"run", "debug", "profile"}) { // NOI18N

                NetbeansActionMapping mapp = ModelHandle2.getMapping(action, prj, cfg);
                Map<String, String> properties = mapp.getProperties();

                for (Entry<String, String> entry : properties.entrySet()) {
                    if (entry.getKey().equals("exec.args")) { // NOI18Nl
                        if (!entry.getValue().contains(ENABLE_PREVIEW_FLAG + " ")) {
                            properties.put(entry.getKey(), ENABLE_PREVIEW_FLAG + " " + entry.getValue());
                        }
                    }
                }
                if (mapp != null) {
                    ModelHandle2.putMapping(mapp, prj, cfg);
                }

            }

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

    private static class AddMvnCompilerPluginForEnablePreview implements ModelOperation<POMModel> {

        private static final String MAVEN_COMPILER_GROUP_ID = "org.apache.maven.plugins"; // NOI18N
        private static final String MAVEN_COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
        private static final String COMPILER_ID_PROPERTY = "compilerId"; // NOI18N
        private static final String COMPILER_ARG = "compilerArgs"; // NOI18N
        private static final String MAVEN_COMPILER_VERSION = "3.3"; // NOI18N
        private static final String ARG = "arg";// NOI18N
        private POMComponentFactory factory;

        @Override
        public void performOperation(final POMModel model) {
            factory = model.getFactory();
            org.netbeans.modules.maven.model.pom.Project proj = model.getProject();
            Build build = model.getProject().getBuild();
            if (build == null) {
                build = factory.createBuild();
                proj.setBuild(build);
            }

            Plugin oldPlugin = searchMavenCompilerPlugin(build);
            if (oldPlugin == null) {
                build.addPlugin(createMavenCompilerPlugin());
            } else {

                Plugin newPlugin = updateMavenCompilerPlugin(oldPlugin);

                build.removePlugin(oldPlugin);
                build.addPlugin(newPlugin);
            }
        }

        private Plugin searchMavenCompilerPlugin(final Build build) {
            List<Plugin> plugins = build.getPlugins();
            if (plugins != null) {
                for (Plugin plugin : plugins) {
                    if (MAVEN_COMPILER_GROUP_ID.equals(plugin.getGroupId())
                            && MAVEN_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                        return plugin;
                    }
                }
            }
            return null;
        }

        private Plugin createMavenCompilerPlugin() {
            Plugin plugin = factory.createPlugin();
            plugin.setGroupId(MAVEN_COMPILER_GROUP_ID);
            plugin.setArtifactId(MAVEN_COMPILER_ARTIFACT_ID);
            plugin.setVersion(MAVEN_COMPILER_VERSION);
            plugin.setConfiguration(createConfiguration());
            Configuration config = factory.createConfiguration();
            POMExtensibilityElement compilerArgs = factory.createPOMExtensibilityElement(POMQName.createQName(COMPILER_ARG));
            compilerArgs.setChildElementText(COMPILER_ID_PROPERTY, ENABLE_PREVIEW_FLAG, POMQName.createQName(ARG));
            config.addExtensibilityElement(compilerArgs);
            plugin.setConfiguration(config);
            return plugin;
        }

        private Configuration createConfiguration() {
            Configuration configuration = factory.createConfiguration();
            return configuration;
        }

        private Plugin updateMavenCompilerPlugin(final Plugin oldPlugin) {

            Configuration currenConfig = oldPlugin.getConfiguration();
            Configuration newConfiguration = createConfiguration();

            boolean isCompilerArgsElementPresent = false;
            if (currenConfig != null) {
                for (POMExtensibilityElement element : currenConfig.getConfigurationElements()) {
                    POMExtensibilityElement newElement = factory.createPOMExtensibilityElement(element.getQName());
                    String elementText = element.getElementText();
                    if (elementText.trim().length() > 0) {
                        newElement.setElementText(element.getElementText());
                    }
                    if (newElement.getQName().getLocalPart().equals(COMPILER_ARG)) {
                        isCompilerArgsElementPresent = true;
                        POMExtensibilityElement compilerArgs = factory.createPOMExtensibilityElement(POMQName.createQName(COMPILER_ARG));
                        newElement.setChildElementText(COMPILER_ID_PROPERTY, ENABLE_PREVIEW_FLAG, POMQName.createQName(ARG));
                    }
                    for (POMExtensibilityElement childElement : element.getAnyElements()) {

                        POMExtensibilityElement newChildElement = factory.createPOMExtensibilityElement(childElement.getQName());

                        newChildElement.setElementText(childElement.getElementText());
                        newElement.addExtensibilityElement(newChildElement);
                    }

                    newConfiguration.addExtensibilityElement(newElement);

                }
                if (!isCompilerArgsElementPresent) {
                    POMExtensibilityElement compilerArgs = factory.createPOMExtensibilityElement(POMQName.createQName(COMPILER_ARG));
                    compilerArgs.setChildElementText(COMPILER_ID_PROPERTY, ENABLE_PREVIEW_FLAG, POMQName.createQName(ARG));
                    newConfiguration.addExtensibilityElement(compilerArgs);
                }
            }

            Plugin newPlugin = factory.createPlugin();
            newPlugin.setGroupId(oldPlugin.getGroupId());
            newPlugin.setArtifactId(oldPlugin.getArtifactId());
            newPlugin.setVersion(oldPlugin.getVersion());
            newPlugin.setConfiguration(newConfiguration);
            return newPlugin;

        }

    }

}

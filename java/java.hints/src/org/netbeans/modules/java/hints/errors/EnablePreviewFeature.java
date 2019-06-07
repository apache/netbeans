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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Maven and Ant type project.
 *
 * @author arusinha
 */
public class EnablePreviewFeature implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.preview.feature.disabled.plural")); // NOI18N
    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String JAVAC_COMPILER_ARGS = "javac.compilerargs"; // NOI18N
    private static final String RUN_JVMARGS = "run.jvmargs"; // NOI18N

    @Override
    public Set<String> getCodes() {
        float jvmVersion = Float.parseFloat(System.getProperty("java.specification.version")); // NOI18N

        if (jvmVersion >= 12f) {
            return Collections.unmodifiableSet(ERROR_CODES);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    enum PROJ_TYPES {
        ANT, MAVEN
    };

    @Override
    @NonNull
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        final FileObject file = compilationInfo.getFileObject();
        Fix fix = null;
        if (file != null) {
            final Project prj = FileOwnerQuery.getOwner(file);
            PROJ_TYPES projType = getProjectType(prj);

            if (prj != null) {
                switch (projType) {
                    case MAVEN:
                        fix = new EnablePreviewFeature.ResolveMvnFix(prj);
                        break;

                    case ANT:
                        fix = new EnablePreviewFeature.ResolveAntFix(prj);
                        break;

                    default:
                        fix = null;
                }
            }
        }
        return (fix != null) ? Collections.<Fix>singletonList(fix) : Collections.<Fix>emptyList();
    }

    @Override
    public String getId() {
        return EnablePreviewFeature.class.getName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EnablePreviewFeature.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(EnablePreviewFeature.class, "FIX_EnablePreviewFeature"); // NOI18N
    }

    @Override
    public void cancel() {
    }

    private static final class ResolveAntFix implements Fix {

        private final Project prj;

        ResolveAntFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(EnablePreviewFeature.class, "FIX_EnablePreviewFeature");
        }

        @Override
        public ChangeInfo implement() throws Exception {

            EditableProperties ep = getEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH);

            String compilerArgs = ep.getProperty(JAVAC_COMPILER_ARGS);
            compilerArgs = compilerArgs != null ? compilerArgs + " " + ENABLE_PREVIEW_FLAG : ENABLE_PREVIEW_FLAG;

            String runJVMArgs = ep.getProperty(RUN_JVMARGS);
            runJVMArgs = runJVMArgs != null ? runJVMArgs + " " + ENABLE_PREVIEW_FLAG : ENABLE_PREVIEW_FLAG;

            ep.setProperty(JAVAC_COMPILER_ARGS, compilerArgs);
            ep.setProperty(RUN_JVMARGS, runJVMArgs);
            storeEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            return null;
        }
    }

    private static final class ResolveMvnFix implements Fix {

        private final Project prj;

        ResolveMvnFix(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(EnablePreviewFeature.class, "FIX_EnablePreviewFeature");
        }

        @Override
        public ChangeInfo implement() throws Exception {

            try {

                FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
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

    private static void storeEditableProperties(final Project prj, final String propertiesPath, final EditableProperties ep)
            throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo != null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }

    private static EditableProperties getEditableProperties(final Project prj, final String propertiesPath)
            throws IOException {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                @Override
                public EditableProperties run() throws IOException {
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo != null) {
                        InputStream is = null;
                        ep = new EditableProperties(false);
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }

    private boolean isMavenProject(Project prj) {

        FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml");
        return (pom != null) && pom.isValid();

    }

    private boolean isAntProject(Project prj) {

        FileObject buildFile = prj.getProjectDirectory().getFileObject("build.xml");
        return (buildFile != null) && buildFile.isValid();

    }

    PROJ_TYPES getProjectType(Project prj) {

        if (isAntProject(prj)) {
            return PROJ_TYPES.ANT;
        }
        if (isMavenProject(prj)) {
            return PROJ_TYPES.MAVEN;
        } else {
            return null;
        }

    }

    private static class AddMvnCompilerPluginForEnablePreview implements ModelOperation<POMModel> {

        private static final String MAVEN_COMPILER_GROUP_ID = "org.apache.maven.plugins"; // NOI18N
        private static final String MAVEN_COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
        private static final String COMPILER_ID_PROPERTY = "compilerId"; // NOI18N
        private static final String COMPILER_ARG = "compilerArgs"; // NOI18N
        private static final String MAVEN_COMPILER_VERSION = "3.3" ; // NOI18N
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
                build.addPlugin(createMavenEclipseCompilerPlugin());
            } else {

                Plugin newPlugin = updateMavenEclipseCompilerPlugin(oldPlugin);

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

        private Plugin createMavenEclipseCompilerPlugin() {
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

        private Plugin updateMavenEclipseCompilerPlugin(final Plugin oldPlugin) {

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
                    newConfiguration.addExtensibilityElement(compilerArgs);;
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

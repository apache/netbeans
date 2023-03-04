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
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.javascript.nodejs.exec.ExpressExecutable;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class NewProjectWizardIterator extends BaseWizardIterator {

    private final Wizard wizard;


    private NewProjectWizardIterator(Wizard wizard) {
        assert wizard != null;
        this.wizard = wizard;
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newNodeJsProject.displayName",
            description = "../resources/NewNodeJsProjectDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 150)
    @NbBundle.Messages("NewProjectWizardIterator.newNodeJsProject.displayName=Node.js Application")
    public static NewProjectWizardIterator newNodeJsProject() {
        return new NewProjectWizardIterator(new NewNodeJsProject());
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newHtml5ProjectWithNodeJs.displayName",
            description = "../resources/NewHtml5ProjectWithNodeJsDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 160)
    @NbBundle.Messages("NewProjectWizardIterator.newHtml5ProjectWithNodeJs.displayName=HTML5/JS Application with Node.js")
    public static NewProjectWizardIterator newHtml5ProjectWithNodeJs() {
        return new NewProjectWizardIterator(new NewHtml5ProjectWithNodeJs());
    }

    @Override
    String getWizardTitle() {
        return wizard.getTitle();
    }

    @Override
    WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        return wizard.createPanels();
    }

    @Override
    String[] createSteps() {
        return wizard.createSteps();
    }

    @Override
    void uninitializeInternal() {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
        wizard.uninitialize(wizardDescriptor);
    }

    @NbBundle.Messages("NewProjectWizardIterator.progress.creating=Creating project")
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.NewProjectWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        File projectDir = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_DIRECTORY));
        if (!projectDir.isDirectory()
                && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory: " + projectDir);
        }
        FileObject projectDirectory = FileUtil.toFileObject(projectDir);
        assert projectDirectory != null : "FileObject must be found for " + projectDir;
        files.add(projectDirectory);

        wizard.readSettings(wizardDescriptor);

        CreateProjectProperties createProperties = new CreateProjectProperties(projectDirectory, (String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME))
                .setSourceFolder(wizard.getSources())
                .setSiteRootFolder(wizard.getSiteRoot())
                .setPlatformProvider(NodeJsPlatformProvider.IDENT)
                .setStartFile(wizard.getStartFile())
                .setProjectUrl(wizard.getProjectUrl());
        Project project = ClientSideProjectGenerator.createProject(createProperties);

        wizard.instantiate(files, handle, wizardDescriptor, project);

        handle.finish();
        return files;
    }

    static FileObject createMainJsFile(FileObject sources) throws IOException {
        assert sources != null;
        FileObject template = FileUtil.getConfigFile("Templates/Other/javascript.js"); // NOI18N
        DataFolder dataFolder = DataFolder.findFolder(sources);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, "main").getPrimaryFile(); // NOI18N
    }

    static FileObject createIndexHtmlFile(FileObject siteRoot) throws IOException {
        assert siteRoot != null;
        FileObject template = FileUtil.getConfigFile("Templates/Other/html.html"); // NOI18N
        DataFolder dataFolder = DataFolder.findFolder(siteRoot);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, "index").getPrimaryFile(); // NOI18N
    }

    //~ Inner classes

    private interface Wizard {

        String DEFAULT_SOURCE_FOLDER = ""; // NOI18N
        String DEFAULT_SITE_ROOT_FOLDER = "public"; // NOI18N


        String getTitle();

        void readSettings(WizardDescriptor wizardDescriptor);

        @CheckForNull
        String getSources();

        @CheckForNull
        String getSiteRoot();

        @CheckForNull
        String getStartFile();

        @CheckForNull
        String getProjectUrl();

        WizardDescriptor.Panel<WizardDescriptor>[] createPanels();

        String[] createSteps();

        void instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, Project project) throws IOException;

        void uninitialize(WizardDescriptor wizardDescriptor);


    }

    private static final class NewNodeJsProject implements Wizard {

        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;


        public NewNodeJsProject() {
            baseWizard = CreateProjectUtils.createBaseWizardPanel("NodeJsApplication"); // NOI18N
            toolsWizard = CreateProjectUtils.createToolsWizardPanel(new CreateProjectUtils.Tools()
                    .setNpm(true));
        }

        @Override
        public String getTitle() {
            return Bundle.NewProjectWizardIterator_newNodeJsProject_displayName();
        }

        @Override
        public void readSettings(WizardDescriptor wizardDescriptor) {
            // noop
        }

        @Override
        public String getSources() {
            return DEFAULT_SOURCE_FOLDER;
        }

        @Override
        public String getSiteRoot() {
            return null;
        }

        @Override
        public String getStartFile() {
            return null;
        }

        @Override
        public String getProjectUrl() {
            return null;
        }

        @Override
        public WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
            return new WizardDescriptor.Panel[] {
                baseWizard.first(),
                toolsWizard.first(),
            };
        }

        @Override
        public String[] createSteps() {
            return new String[] {
                baseWizard.second(),
                toolsWizard.second(),
            };
        }

        @Override
        public void instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, Project project) throws IOException {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject sources = projectDirectory.getFileObject(getSources());
            assert sources != null;
            // create main.js file
            FileObject mainJsFile = createMainJsFile(sources);
            files.add(mainJsFile);

            // tools
            CreateProjectUtils.instantiateTools(project, toolsWizard.first());

            // set proper node.js start file
            NodeJsSupport.forProject(project).getPreferences().setStartFile(FileUtil.toFile(mainJsFile).getAbsolutePath());

            // set node.js run config only for server-side node.js project (since project URL is not known)
            ProjectSetup.setupRun(project);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            // noop
        }

    }

    static final class NewHtml5ProjectWithNodeJs implements Wizard {

        public static final String EXPRESS_ENABLED = "EXPRESS_ENABLED"; // NOI18N
        public static final String EXPRESS_LESS = "EXPRESS_LESS"; // NOI18N

        private static final String EXPRESS_MAIN_JS_FILE = "app.js"; // NOI18N
        private static final String EXPRESS_MAIN_VIEW_FILE = "views/index.jade"; // NOI18N
        private static final String EXPRESS_RUN_FILE = "bin/www"; // NOI18N
        private static final String EXPRESS_START_FILE = ""; // NOI18N
        private static final String EXPRESS_PROJECT_URL = "http://localhost:3000/"; // NOI18N

        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;
        private final WizardDescriptor.FinishablePanel<WizardDescriptor> expressWizard;

        private WizardDescriptor descriptor;


        public NewHtml5ProjectWithNodeJs() {
            baseWizard = CreateProjectUtils.createBaseWizardPanel("NodeJsWebApplication"); // NOI18N
            toolsWizard = CreateProjectUtils.createToolsWizardPanel(new CreateProjectUtils.Tools()
                    .setNpm(true));
            expressWizard = new ExpressPanel();
        }

        @Override
        public String getTitle() {
            return Bundle.NewProjectWizardIterator_newHtml5ProjectWithNodeJs_displayName();
        }

        @Override
        public void readSettings(WizardDescriptor wizardDescriptor) {
            descriptor = wizardDescriptor;
        }

        @Override
        public String getSources() {
            return DEFAULT_SOURCE_FOLDER;
        }

        @Override
        public String getSiteRoot() {
            return DEFAULT_SITE_ROOT_FOLDER;
        }

        @Override
        public String getStartFile() {
            if (isExpress()) {
                return EXPRESS_START_FILE;
            }
            return null;
        }

        @Override
        public String getProjectUrl() {
            if (isExpress()) {
                return EXPRESS_PROJECT_URL;
            }
            return null;
        }

        @Override
        public WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
            return new WizardDescriptor.Panel[] {
                baseWizard.first(),
                expressWizard,
                toolsWizard.first(),
            };
        }

        @NbBundle.Messages("NewHtml5ProjectWithNodeJs.express.title=Express")
        @Override
        public String[] createSteps() {
            return new String[] {
                baseWizard.second(),
                Bundle.NewHtml5ProjectWithNodeJs_express_title(),
                toolsWizard.second(),
            };
        }

        @Override
        public void instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, Project project) throws IOException {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject sources = projectDirectory.getFileObject(getSources());
            assert sources != null;
            // express?
            if (isExpress()) {
                ExpressExecutable express = ExpressExecutable.getDefault(project, false);
                assert express != null;
                Future<Integer> task = express.generate(projectDirectory, getBoolean(wizardDescriptor, EXPRESS_LESS, true));
                try {
                    task.get(1, TimeUnit.MINUTES);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | TimeoutException ex) {
                    throw new IOException(ex);
                }

                for (String mainFile : new String[] {EXPRESS_MAIN_JS_FILE, EXPRESS_MAIN_VIEW_FILE}) {
                    FileObject file = projectDirectory.getFileObject(mainFile);
                    if (file != null) {
                        files.add(file);
                    }
                }

                // set proper node.js start file
                FileObject runFile = projectDirectory.getFileObject(EXPRESS_RUN_FILE);
                if (runFile != null) {
                    NodeJsSupport.forProject(project).getPreferences().setStartFile(FileUtil.toFile(runFile).getAbsolutePath());
                }
                ProjectSetup.setupRun(project);
            } else {
                // create main.js file
                FileObject mainJsFile = createMainJsFile(sources);
                files.add(mainJsFile);
                // create index.html
                FileObject siteRoot = projectDirectory.getFileObject(getSiteRoot());
                assert siteRoot != null;
                FileObject indexHtmlFile = createIndexHtmlFile(siteRoot);
                files.add(indexHtmlFile);

                // set proper node.js start file
                NodeJsSupport.forProject(project).getPreferences().setStartFile(FileUtil.toFile(mainJsFile).getAbsolutePath());
            }

            // tools
            CreateProjectUtils.instantiateTools(project, toolsWizard.first());
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(EXPRESS_ENABLED, null);
            wizardDescriptor.putProperty(EXPRESS_LESS, null);
        }

        private boolean getBoolean(WizardDescriptor wizardDescriptor, String propertyName, boolean defaultValue) {
            Boolean value = (Boolean) wizardDescriptor.getProperty(propertyName);
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        private boolean isExpress() {
            assert descriptor != null;
            return getBoolean(descriptor, EXPRESS_ENABLED, false);
        }

    }

}

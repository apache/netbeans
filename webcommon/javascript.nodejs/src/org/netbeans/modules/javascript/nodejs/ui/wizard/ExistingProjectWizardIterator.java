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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class ExistingProjectWizardIterator extends BaseWizardIterator {

    static final String SOURCE_ROOT = "SOURCE_ROOT"; // NOI18N
    static final String SITE_ROOT = "SITE_ROOT"; // NOI18N
    static final String TEST_ROOT = "TEST_ROOT"; // NOI18N
    static final String[] MAIN_JS_FILE_NAMES = new String[] {
        "app.js", // NOI18N
        "main.js", // NOI18N
        "index.js", // NOI18N
    };

    private final String displayName;


    public ExistingProjectWizardIterator(String displayName) {
        assert displayName != null;
        this.displayName = displayName;
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#ExistingProjectWizardIterator.displayName",
            description = "../resources/ExistingNodeJsProjectDescription.html",
            iconBase = NODEJS_PROJECT_ICON,
            position = 320)
    @NbBundle.Messages("ExistingProjectWizardIterator.displayName=Node.js Application with Existing Sources")
    public static ExistingProjectWizardIterator existingNodeJsProject() {
        return new ExistingProjectWizardIterator(Bundle.ExistingProjectWizardIterator_displayName());
    }

    @Override
    String getWizardTitle() {
        return displayName;
    }

    @Override
    WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ExistingProjectPanel(),
        };
    }

    @NbBundle.Messages("ExistingProjectWizardIterator.nameLocation.title=Name and Location")
    @Override
    String[] createSteps() {
        return new String[] {
            Bundle.ExistingProjectWizardIterator_nameLocation_title(),
        };
    }

    @Override
    void uninitializeInternal() {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
        wizardDescriptor.putProperty(SOURCE_ROOT, null);
        wizardDescriptor.putProperty(SITE_ROOT, null);
        wizardDescriptor.putProperty(TEST_ROOT, null);
    }

    @NbBundle.Messages("ExisitngProjectWizardIterator.progress.creating=Creating project")
    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.ExisitngProjectWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        FileObject projectDirectory = getDirectory(CreateProjectUtils.PROJECT_DIRECTORY, true);
        assert projectDirectory != null;
        files.add(projectDirectory);

        FileObject sourceRoot = getDirectory(SOURCE_ROOT, true);
        assert sourceRoot != null;
        files.add(sourceRoot);

        FileObject siteRoot = getDirectory(SITE_ROOT, false);
        if (siteRoot != null) {
            files.add(siteRoot);
        }

        CreateProjectProperties createProperties = new CreateProjectProperties(projectDirectory, (String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME))
                .setSourceFolder(relativizePath(projectDirectory, sourceRoot))
                .setSiteRootFolder(relativizePath(projectDirectory, siteRoot))
                .setPlatformProvider(NodeJsPlatformProvider.IDENT);
        Project project = ClientSideProjectGenerator.createProject(createProperties);

        FileObject mainJsFile = findMainJsFile(project, sourceRoot);
        if (mainJsFile != null) {
            files.add(mainJsFile);
            // set proper node.js start file
            NodeJsSupport.forProject(project).getPreferences().setStartFile(FileUtil.toFile(mainJsFile).getAbsolutePath());
            if (siteRoot == null) {
                // set node.js run config only for server-side node.js project (since project URL is not known)
                ProjectSetup.setupRun(project);
            }
        }

        handle.finish();
        return files;
    }

    @CheckForNull
    private FileObject getDirectory(String key, boolean compulsory) {
        File dir = (File) wizardDescriptor.getProperty(key);
        if (!compulsory
                && dir == null) {
            return null;
        }
        assert dir != null;
        FileObject directory = FileUtil.toFileObject(dir);
        assert directory != null : "FileObject must be found for " + dir;
        return directory;
    }

    private String relativizePath(FileObject projectDir, @NullAllowed FileObject dir) {
        assert projectDir != null;
        if (dir == null) {
            return null;
        }
        String relativePath = FileUtil.getRelativePath(projectDir, dir);
        if (relativePath != null) {
            return relativePath;
        }
        relativePath = PropertyUtils.relativizeFile(FileUtil.toFile(projectDir), FileUtil.toFile(dir));
        if (relativePath != null) {
            return relativePath;
        }
        // cannot relativize
        return FileUtil.toFile(dir).getAbsolutePath();
    }

    @CheckForNull
    private FileObject findMainJsFile(Project project, FileObject sourceRoot) {
        FileObject projectDir = project.getProjectDirectory();
        // first, try package.json
        String main = NodeJsSupport.forProject(project)
                .getPackageJson()
                .getContentValue(String.class, PackageJson.FIELD_MAIN);
        FileObject mainFile = getFileObject(projectDir, main);
        if (mainFile != null) {
            return mainFile;
        }
        // project name
        String projectName = ProjectUtils.getInformation(project).getName();
        mainFile = getFileObject(projectDir, projectName + ".js"); // NOI18N
        if (mainFile != null) {
            return mainFile;
        }
        mainFile = getFileObject(projectDir, projectName.toLowerCase() + ".js"); // NOI18N
        if (mainFile != null) {
            return mainFile;
        }
        // children
        HashSet<String> names = new HashSet<>(Arrays.asList(MAIN_JS_FILE_NAMES));
        FileObject first = null;
        for (FileObject child : sourceRoot.getChildren()) {
            if (!FileUtils.isJavaScriptFile(child)) {
                continue;
            }
            if (first == null) {
                first = child;
            }
            if (names.contains(child.getNameExt())) {
                return child;
            }
        }
        return first;
    }

    @CheckForNull
    private FileObject getFileObject(FileObject dir, String name) {
        if (StringUtilities.hasText(name)) {
            FileObject fo = dir.getFileObject(name);
            if (fo != null
                    && FileUtils.isJavaScriptFile(fo)) {
                return fo;
            }
        }
        return null;
    }

}

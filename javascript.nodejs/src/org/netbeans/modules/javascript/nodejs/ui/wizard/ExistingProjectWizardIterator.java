/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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

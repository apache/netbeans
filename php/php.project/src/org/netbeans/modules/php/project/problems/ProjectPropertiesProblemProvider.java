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
package org.netbeans.modules.php.project.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.SeleniumTestDirectoriesPathSupport;
import org.netbeans.modules.php.project.ui.customizer.TestDirectoriesPathSupport;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Problems in project properties.
 */
public final class ProjectPropertiesProblemProvider implements ProjectProblemsProvider {

    // set would be better but it is fine to use a list for small number of items
    static final List<String> WATCHED_PROPERTIES = new CopyOnWriteArrayList<>(Arrays.asList(
            PhpProjectProperties.SRC_DIR,
            PhpProjectProperties.SELENIUM_SRC_DIR,
            PhpProjectProperties.WEB_ROOT,
            PhpProjectProperties.INCLUDE_PATH,
            PhpProjectProperties.PRIVATE_INCLUDE_PATH));
    static final List<String> WATCHED_PROPERTY_PREFIXES = new CopyOnWriteArrayList<>(Arrays.asList(
            PhpProjectProperties.TEST_SRC_DIR));

    final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    private final PhpProject project;
    private final PropertyChangeListener projectPropertiesListener = new ProjectPropertiesListener();

    private volatile FileChangeListener fileChangesListener = new FileChangesListener();


    private ProjectPropertiesProblemProvider(PhpProject project) {
        this.project = project;
    }

    public static ProjectPropertiesProblemProvider createForProject(PhpProject project) {
        ProjectPropertiesProblemProvider projectProblems = new ProjectPropertiesProblemProvider(project);
        projectProblems.addProjectPropertiesListeners();
        projectProblems.addFileChangesListeners();
        return projectProblems;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<ProjectProblemsProvider.ProjectProblem> collectProblems() {
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>(5);
                checkSrcDir(currentProblems);
                if (currentProblems.isEmpty()) {
                    // check other problems only if sources are correct (other problems are fixed in customizer but customizer needs correct sources)
                    checkTestDirs(currentProblems);
                    checkSeleniumDir(currentProblems);
                    checkWebRoot(currentProblems);
                    checkIncludePath(currentProblems);
                }
                return currentProblems;
            }
        });
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSrcDir.title=Invalid Source Files",
        "# {0} - src dir path",
        "ProjectPropertiesProblemProvider.invalidSrcDir.description=The directory \"{0}\" does not exist and cannot be used for Source Files.",
        "# {0} - project name",
        "ProjectPropertiesProblemProvider.invalidSrcDir.dialog.title=Select Source Files for {0}"
    })
    void checkSrcDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(ProjectPropertiesSupport.getSourcesDirectory(project), PhpProjectProperties.SRC_DIR);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_description(invalidDirectory.getAbsolutePath()),
                    new DirectoryProblemResolver(project, PhpProjectProperties.SRC_DIR, Bundle.ProjectPropertiesProblemProvider_invalidSrcDir_dialog_title(project.getName())));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidTestDir.title=Invalid Test Files",
        "# {0} - test dir path",
        "ProjectPropertiesProblemProvider.invalidTestDir.description=The directory \"{0}\" cannot be used for Test Files."
    })
    void checkTestDirs(Collection<ProjectProblem> currentProblems) {
        TestDirectoriesPathSupport testDirectoriesPathSupport = new TestDirectoriesPathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getRefHelper(), project.getHelper());
        Enumeration<BasePathSupport.Item> items = new PhpProjectProperties(project, null, null, testDirectoriesPathSupport, null)
                .getTestDirectoriesListModel()
                .elements();
        int i = 0;
        while (items.hasMoreElements()) {
            BasePathSupport.Item item = items.nextElement();
            ValidationResult result = new TestDirectoriesPathSupport.Validator()
                    .validatePath(project, item)
                    .getResult();
            if (!result.hasErrors()) {
                continue;
            }
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_description(item.getAbsoluteFilePath(project.getProjectDirectory())),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.TESTING, PhpProjectProperties.TEST_SRC_DIR + i));
            currentProblems.add(problem);
            i++;
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.title=Invalid Selenium Test Files",
        "# {0} - selenium dir path",
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.description=The directory \"{0}\" does not exist and cannot be used for Selenium Test Files.",
        "# {0} - project name",
        "ProjectPropertiesProblemProvider.invalidSeleniumDir.dialog.title=Select Selenium Test Files for {0}"
    })
    void checkSeleniumDir(Collection<ProjectProblem> currentProblems) {
        SeleniumTestDirectoriesPathSupport seleniumTestDirectoriesPathSupport = new SeleniumTestDirectoriesPathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getRefHelper(), project.getHelper());
        Enumeration<BasePathSupport.Item> items = new PhpProjectProperties(project, null, null, null, seleniumTestDirectoriesPathSupport)
                .getSeleniumTestDirectoriesListModel()
                .elements();
        int i = 0;
        while (items.hasMoreElements()) {
            BasePathSupport.Item item = items.nextElement();
            ValidationResult result = new SeleniumTestDirectoriesPathSupport.Validator()
                    .validatePath(project, item)
                    .getResult();
            if (!result.hasErrors()) {
                continue;
            }
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSeleniumDir_description(item.getAbsoluteFilePath(project.getProjectDirectory())),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.TESTING_SELENIUM, PhpProjectProperties.SELENIUM_SRC_DIR + i));
            currentProblems.add(problem);
            i++;
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidWebRoot.title=Invalid Web Root",
        "# {0} - web root path",
        "ProjectPropertiesProblemProvider.invalidWebRoot.description=The directory \"{0}\" does not exist and cannot be used for Web Root."
    })
    void checkWebRoot(Collection<ProjectProblem> currentProblems) {
        File webRoot = getWebRoot();
        if (webRoot == null) {
            // project fatally broken => do not validate web root
            return;
        }
        File invalidDirectory = getInvalidDirectory(FileUtil.toFileObject(webRoot), PhpProjectProperties.WEB_ROOT);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidWebRoot_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES, PhpProjectProperties.WEB_ROOT));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidIncludePath.title=Invalid Include Path",
        "ProjectPropertiesProblemProvider.invalidIncludePath.description=Some directories on project's Include Path are invalid."
    })
    void checkIncludePath(Collection<ProjectProblem> currentProblems) {
        // public first
        ProjectProblem projectProblem = checkIncludePath(PhpProjectProperties.INCLUDE_PATH);
        if (projectProblem != null) {
            currentProblems.add(projectProblem);
            return;
        }
        // private now
        projectProblem = checkIncludePath(PhpProjectProperties.PRIVATE_INCLUDE_PATH);
        if (projectProblem != null) {
            currentProblems.add(projectProblem);
        }
    }

    @CheckForNull
    private ProjectProblem checkIncludePath(String includePathProperty) {
        IncludePathSupport includePathSupport = new IncludePathSupport(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getRefHelper(), project.getHelper());
        List<BasePathSupport.Item> items = includePathSupport.itemsList(
                ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(includePathProperty));
        ValidationResult result = new IncludePathSupport.Validator()
                .validateBroken(items)
                .validatePaths(project, items)
                .getResult();
        if (!result.hasErrors()) {
            return null;
        }
        return ProjectProblem.createError(
                Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_title(),
                Bundle.ProjectPropertiesProblemProvider_invalidIncludePath_description(),
                new CustomizerProblemResolver(project, CompositePanelProviderImpl.PHP_INCLUDE_PATH, includePathProperty));
    }

    private File getInvalidDirectory(FileObject directory, String propertyName) {
        assert isWatchedProperty(propertyName) : "Property '" + propertyName + "' should be watched for changes";
        if (directory != null) {
            if (directory.isValid()) {
                // ok
                return null;
            } else {
                // invalid fo
                return FileUtil.toFile(directory);
            }
        }
        String propValue = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(propertyName);
        if (propValue == null) {
            return null;
        }
        File dir = ProjectPropertiesSupport.getSubdirectory(project, project.getProjectDirectory(), propValue);
        if (dir.isDirectory()) {
            // #217030 - directory renamed in file chooser in project problems dialog
            return null;
        }
        return dir;
    }

    // XXX put somewhere and use everywhere (copied to more places)
    private File getWebRoot() {
        if (PhpProjectValidator.isFatallyBroken(project)) {
            return null;
        }
        // ProjectPropertiesSupport.getWebRootDirectory(project) cannot be used since it always returns a valid fileobject (even if webroot is invalid, then sources are returned)
        return ProjectPropertiesSupport.getSourceSubdirectory(project, ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(PhpProjectProperties.WEB_ROOT));
    }

    private void addProjectPropertiesListeners() {
        ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, projectPropertiesListener);
    }

    private void addFileChangesListeners() {
        addFileChangesListener(project.getSourceRoots());
        addFileChangesListener(project.getTestRoots());
        addFileChangesListener(project.getSeleniumRoots());
        File webRoot = getWebRoot();
        if (webRoot != null) {
            addFileChangeListener(webRoot);
        }
    }

    private void addFileChangesListener(SourceRoots sourceRoots) {
        for (FileObject root : sourceRoots.getRoots()) {
            File file = FileUtil.toFile(root);
            if (file != null) {
                addFileChangeListener(file);
            }
        }
    }

    private void addFileChangeListener(File file) {
        try {
            FileUtil.addFileChangeListener(fileChangesListener, file);
        } catch (IllegalArgumentException ex) {
            // already listenening, ignore
        }
    }

    boolean isWatchedProperty(String propertyName) {
        if (WATCHED_PROPERTIES.contains(propertyName)) {
            return true;
        }
        for (String prefix : WATCHED_PROPERTY_PREFIXES) {
            if (propertyName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    void propertiesChanged() {
        // release the current listener
        fileChangesListener = new FileChangesListener();
        addFileChangesListeners();
    }

    //~ Inner classes

    private final class ProjectPropertiesListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (isWatchedProperty(evt.getPropertyName())) {
                problemsProviderSupport.fireProblemsChange();
                propertiesChanged();
            }
        }

    }

    private final class FileChangesListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            problemsProviderSupport.fireProblemsChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

    }

}

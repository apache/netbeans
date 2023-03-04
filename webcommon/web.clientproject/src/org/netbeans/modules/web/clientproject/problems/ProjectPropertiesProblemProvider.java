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
package org.netbeans.modules.web.clientproject.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.env.Values;
import org.netbeans.modules.web.clientproject.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Problems in project properties.
 */
public final class ProjectPropertiesProblemProvider implements ProjectProblemsProvider {

    // set would be better but it is fine to use a list for small number of items
    static final List<String> WATCHED_PROPERTIES = new CopyOnWriteArrayList<>(Arrays.asList(
            ClientSideProjectConstants.PROJECT_SOURCE_FOLDER,
            ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER,
            ClientSideProjectConstants.PROJECT_TEST_FOLDER,
            ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER));

    final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    private final ClientSideProject project;
    private final PropertyChangeListener projectPropertiesListener = new ProjectPropertiesListener();

    private volatile FileChangeListener fileChangesListener = new FileChangesListener();


    private ProjectPropertiesProblemProvider(ClientSideProject project) {
        assert project != null;
        this.project = project;
    }

    public static ProjectPropertiesProblemProvider createForProject(ClientSideProject project) {
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
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                checkSourceDir(currentProblems);
                checkSiteRootDir(currentProblems);
                checkTestDir(currentProblems);
                checkTestSeleniumDir(currentProblems);
                return currentProblems;
            }
        });
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSourceDir.title=Invalid Sources",
        "# {0} - src dir path",
        "ProjectPropertiesProblemProvider.invalidSourceDir.description=The directory \"{0}\" does not exist and cannot be used for Sources."
    })
    private void checkSourceDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(project.getSourcesFolder(), ClientSideProjectConstants.PROJECT_SOURCE_FOLDER);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSourceDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSourceDir_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES, ClientSideProjectConstants.PROJECT_SOURCE_FOLDER));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidSiteRootDir.title=Invalid Site Root",
        "# {0} - src dir path",
        "ProjectPropertiesProblemProvider.invalidSiteRootDir.description=The directory \"{0}\" does not exist and cannot be used for Site Root."
    })
    private void checkSiteRootDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(project.getSiteRootFolder(), ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidSiteRootDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidSiteRootDir_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES, ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidTestDir.title=Invalid Unit Tests",
        "# {0} - test dir path",
        "ProjectPropertiesProblemProvider.invalidTestDir.description=The directory \"{0}\" does not exist and cannot be used for Unit Tests."
    })
    private void checkTestDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(project.getTestsFolder(false), ClientSideProjectConstants.PROJECT_TEST_FOLDER);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidTestDir_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES, ClientSideProjectConstants.PROJECT_TEST_FOLDER));
            currentProblems.add(problem);
        }
    }

    @NbBundle.Messages({
        "ProjectPropertiesProblemProvider.invalidTestSeleniumDir.title=Invalid Selenium Tests",
        "# {0} - test dir path",
        "ProjectPropertiesProblemProvider.invalidTestSeleniumDir.description=The directory \"{0}\" does not exist and cannot be used for Selenium Tests."
    })
    private void checkTestSeleniumDir(Collection<ProjectProblem> currentProblems) {
        File invalidDirectory = getInvalidDirectory(project.getTestsSeleniumFolder(false), ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER);
        if (invalidDirectory != null) {
            ProjectProblem problem = ProjectProblem.createError(
                    Bundle.ProjectPropertiesProblemProvider_invalidTestSeleniumDir_title(),
                    Bundle.ProjectPropertiesProblemProvider_invalidTestSeleniumDir_description(invalidDirectory.getAbsolutePath()),
                    new CustomizerProblemResolver(project, CompositePanelProviderImpl.SOURCES, ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER));
            currentProblems.add(problem);
        }
    }

    private File getInvalidDirectory(FileObject directory, String propertyName) {
        assert WATCHED_PROPERTIES.contains(propertyName) : "Property '" + propertyName + "' should be watched for changes";
        if (directory != null) {
            if (directory.isValid()) {
                // ok
                return null;
            }
            // invalid fo
            return FileUtil.toFile(directory);
        }
        File dir = resolveFile(propertyName);
        if (dir != null && dir.isDirectory()) {
            // meanwhile resolved
            return null;
        }
        return dir;
    }

    private File resolveFile(String propertyName) {
        String propValue = project.getEvaluator().getProperty(propertyName);
        if (propValue == null) {
            return null;
        }
        return project.getProjectHelper().resolveFile(propValue);
    }

    private void addProjectPropertiesListeners() {
        Values evaluator = project.getEvaluator();
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(projectPropertiesListener, evaluator));
    }

    private void addFileChangesListeners() {
        addFileChangeListener(resolveFile(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER));
        addFileChangeListener(resolveFile(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER));
        addFileChangeListener(resolveFile(ClientSideProjectConstants.PROJECT_TEST_FOLDER));
        addFileChangeListener(resolveFile(ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER));
    }

    private void addFileChangeListener(File file) {
        if (file == null) {
            return;
        }
        try {
            FileUtil.addFileChangeListener(fileChangesListener, file);
        } catch (IllegalArgumentException ex) {
            // already listenening, ignore
        }
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
            if (WATCHED_PROPERTIES.contains(evt.getPropertyName())) {
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

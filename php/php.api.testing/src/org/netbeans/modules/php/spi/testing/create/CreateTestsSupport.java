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
package org.netbeans.modules.php.spi.testing.create;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.netbeans.modules.php.api.PhpConstants;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.queries.PhpVisibilityQuery;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Support for creating new tests using
 * {@link org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider} and
 * {@link org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration}.
 * @since 0.19
 */
public final class CreateTestsSupport {

    private static final Logger LOGGER = Logger.getLogger(CreateTestsSupport.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(CreateTestsSupport.class.getName(), 2);

    @NonNull
    private final PhpTestingProvider testingProvider;
    @NonNull
    private final FileObject[] activatedFileObjects;

    // @GuardedBy("this")
    @NullAllowed
    private PhpModule phpModule;
    // @GuardedBy("this")
    @NullAllowed
    private Project project;


    private CreateTestsSupport(@NonNull PhpTestingProvider testingProvider, @NonNull FileObject[] activatedFileObjects) {
        assert testingProvider != null;
        assert activatedFileObjects != null;
        this.testingProvider = testingProvider;
        this.activatedFileObjects = activatedFileObjects;
    }

    /**
     * Creates new support.
     * @param testingProvider PHP testing provider to be used
     * @param activatedFileObjects source files tests are being created for
     * @return new support for creating tests
     */
    public static CreateTestsSupport create(@NonNull PhpTestingProvider testingProvider, @NonNull FileObject[] activatedFileObjects) {
        Parameters.notNull("testingProvider", testingProvider); // NOI18N
        Parameters.notNull("activatedFileObjects", activatedFileObjects); // NOI18N
        return new CreateTestsSupport(testingProvider, activatedFileObjects);
    }

    /**
     * Gets PHP module in which tests are going to be created.
     * @return PHP module or {@code null} if no source files or files from more projects are given
     */
    @CheckForNull
    public synchronized PhpModule getPhpModule() {
        assert Thread.holdsLock(this);
        if (phpModule == null) {
            if (activatedFileObjects.length == 0) {
                return null;
            }

            PhpModule onlyOnePhpModuleAllowed = null;
            for (FileObject fileObj : activatedFileObjects) {
                if (fileObj == null) {
                    return null;
                }

                PhpModule module = PhpModule.Factory.forFileObject(fileObj);
                if (module == null) {
                    return null;
                }
                if (onlyOnePhpModuleAllowed == null) {
                    onlyOnePhpModuleAllowed = module;
                } else if (!onlyOnePhpModuleAllowed.equals(module)) {
                    // files from more projects given
                    return null;
                }
            }
            phpModule = onlyOnePhpModuleAllowed;
        }
        return phpModule;
    }

    @CheckForNull
    private synchronized Project getProject() {
        assert Thread.holdsLock(this);
        if (project == null) {
            PhpModule module = getPhpModule();
            if (module != null) {
                project = FileOwnerQuery.getOwner(module.getProjectDirectory());
            }
        }
        return project;
    }

    /**
     * Creates new "empty" configuration for new tests.
     * <p>
     * Such configuration does not provide any additional panel, does not store any properties,
     * does not show source or target class.
     * @param framework framework identifier to be used, typically identifier of the current PHP testing provider
     * @return new "empty" configuration for new tests
     */
    public TestCreatorConfiguration createEmptyConfiguration(@NonNull String framework) {
        Parameters.notNull("framework", framework); // NOI18N
        return EmptyTestCreatorConfiguration.create(framework, this);
    }

    /**
     * See {@link org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider#getTestSourceRoots(Collection, FileObject)}.
     */
    public Object[] getTestSourceRoots(@NonNull Collection<SourceGroup> createdSourceRoots, @NonNull FileObject fileObject) {
        PhpModule phpModuleRef = getPhpModule();
        assert phpModuleRef != null : Arrays.toString(activatedFileObjects);
        Project projectRef = getProject();
        assert projectRef != null : Arrays.toString(activatedFileObjects) + " :: " + phpModuleRef.getProjectDirectory();
        List<Object> folders = new ArrayList<>();
        List<FileObject> testDirectories = phpModuleRef.getTestDirectories();
        SourceGroup[] sourceGroups = ProjectUtils.getSources(projectRef).getSourceGroups(PhpConstants.SOURCES_TYPE_PHP);
        for (SourceGroup sg : sourceGroups) {
            if (!sg.contains(fileObject)) {
                if (testDirectories.contains(sg.getRootFolder())) {
                    folders.add(sg);
                }
            }
        }
        return folders.toArray();
    }

    /**
     * Checks whether Create Tests action is enabled or nor.
     * @return {@code true} if Create Tests action is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        if (activatedFileObjects.length == 0) {
            return false;
        }

        PhpModule phpModuleRef = getPhpModule();
        if (phpModuleRef == null) {
            return false;
        }
        if (phpModuleRef.isBroken()) {
            return false;
        }
        if (!PhpTesting.isTestingProviderEnabled(testingProvider.getIdentifier(), phpModuleRef)) {
            return false;
        }

        for (FileObject fileObj : activatedFileObjects) {
            if (fileObj == null) {
                return false;
            }

            // only php files or folders allowed
            if (fileObj.isData()
                    && !FileUtils.isPhpFile(fileObj)) {
                return false;
            }

            if (!isUnderSources(phpModuleRef, fileObj)
                    || isUnderTests(phpModuleRef, fileObj)
                    // XXX no way to get selenium here...
                    /*|| isUnderSelenium(phpModuleRef, fileObj)*/) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates tests.
     * @param configurationPanelProperties properties from the configuration panel
     */
    @NbBundle.Messages("CreateTestsSupport.creating=Creating tests...")
    public void createTests(Map<String, Object> configurationPanelProperties) {
        final Map<String, Object> properties = Collections.synchronizedMap(configurationPanelProperties);
        RP.post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(Bundle.CreateTestsSupport_creating());
                handle.start();
                try {
                    LifecycleManager.getDefault().saveAll();
                    generateTests(properties);
                } finally {
                    handle.finish();
                }
            }
        });
    }

    private boolean isUnderSources(PhpModule phpModule, FileObject fileObj) {
        return FileUtil.isParentOf(phpModule.getSourceDirectory(), fileObj);
    }

    private boolean isUnderTests(PhpModule phpModule, FileObject fileObj) {
        for (FileObject dir : phpModule.getTestDirectories()) {
            if (FileUtil.isParentOf(dir, fileObj)) {
                return true;
            }
        }
        return false;
    }

    void generateTests(final Map<String, Object> configurationPanelProperties) {
        assert !EventQueue.isDispatchThread();
        final PhpModule phpModuleRef = getPhpModule();
        assert phpModuleRef != null : Arrays.toString(activatedFileObjects);

        List<FileObject> files = Arrays.asList(activatedFileObjects);
        assert !files.isEmpty() : "No files for tests?!";
        final List<FileObject> sanitizedFiles = new ArrayList<>(files.size() * 2);
        sanitizeFiles(sanitizedFiles, files, phpModuleRef, phpModuleRef.getLookup().lookup(PhpVisibilityQuery.class));
        if (sanitizedFiles.isEmpty()) {
            LOGGER.info("No visible files for creating tests -> exiting.");
            return;
        }

        final Set<FileObject> succeeded = new HashSet<>();
        final Set<FileObject> failed = new HashSet<>();
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                assert phpModuleRef != null;
                CreateTestsResult result = testingProvider.createTests(phpModuleRef, sanitizedFiles, configurationPanelProperties);
                succeeded.addAll(result.getSucceeded());
                failed.addAll(result.getFailed());
            }
        });
        showFailures(failed);
        reformat(succeeded);
        open(succeeded);
        refreshTests(phpModuleRef.getTestDirectories());
    }

    private void sanitizeFiles(List<FileObject> sanitizedFiles, List<FileObject> files, PhpModule phpModule, PhpVisibilityQuery phpVisibilityQuery) {
        for (FileObject fo : files) {
            if (fo.isData()
                    && FileUtils.isPhpFile(fo)
                    && !isUnderTests(phpModule, fo)
                    // XXX no way to test selenium here
                    //&& !isUnderSelenium(phpModule, fo)
                    && phpVisibilityQuery.isVisible(fo)) {
                sanitizedFiles.add(fo);
            }
            FileObject[] children = fo.getChildren();
            if (children.length > 0) {
                sanitizeFiles(sanitizedFiles, Arrays.asList(children), phpModule, phpVisibilityQuery);
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - file names",
        "CreateTestsSupport.failed=Tests were not generated for the following files:\n\n{0}\nReview the log in Output window."
    })
    private void showFailures(Set<FileObject> files) {
        if (files.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder(50);
        for (FileObject file : files) {
            sb.append(file.getNameExt());
            sb.append("\n"); // NOI18N
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                Bundle.CreateTestsSupport_failed(sb.toString()), NotifyDescriptor.WARNING_MESSAGE));
    }

    private void reformat(Set<FileObject> files) {
        for (FileObject file : files) {
            try {
                FileUtils.reformatFile(FileUtil.toFile(file));
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot reformat file " + file, ex);
            }
        }
    }

    private void open(Set<FileObject> files) {
        for (FileObject file : files) {
            assert file.isData() : "File must be given to open: " + file;
            FileUtils.openFile(FileUtil.toFile(file));
        }
    }

    private void refreshTests(List<FileObject> testDirs) {
        final List<FileObject> dirs = new CopyOnWriteArrayList<>(testDirs);
        RP.post(new Runnable() {
            @Override
            public void run() {
                for (FileObject dir : dirs) {
                    FileUtil.refreshFor(FileUtil.toFile(dir));
                }
            }
        });
    }

}

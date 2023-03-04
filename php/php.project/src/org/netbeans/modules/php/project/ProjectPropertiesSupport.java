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

package org.netbeans.modules.php.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.ui.BrowseTestSources;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Helper class for getting <b>all</b> the properties of a PHP project.
 * <p>
 * <b>This class is the preferred way to get PHP project properties.</b>
 * @author Tomas Mysik
 */
public final class ProjectPropertiesSupport {

    private ProjectPropertiesSupport() {
    }

    // XXX use it everywhere!
    /**
     * Produce a machine-independent relativized version of a filename from a dir.
     * If path cannot be relative, the full path of the given file is returned.
     * @param dir base directory
     * @param file file to be relativized
     * @return relativized version of a filename from a dir or full path of the given file if the path cannot be relativized
     * @see PropertyUtils#relativizeFile(File, File)
     */
    public static String relativizeFile(File dir, File file) {
        String relativePath = PropertyUtils.relativizeFile(dir, file);
        if (relativePath == null) {
            // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
            relativePath = file.getAbsolutePath();
        }
        return relativePath;
    }

    /**
     * <b>This method should not be used, use other methods in this class.</b>
     * <p>
     * Use this method only if you don't want to show customizer automatically
     * or if you understand what you are doing ;)
     * @see #addWeakPropertyEvaluatorListener(org.netbeans.modules.php.project.PhpProject, java.beans.PropertyChangeListener)
     */
    public static PropertyEvaluator getPropertyEvaluator(PhpProject project) {
        return project.getEvaluator();
    }

    public static void addWeakPropertyEvaluatorListener(PhpProject project, PropertyChangeListener listener) {
        project.addWeakPropertyEvaluatorListener(listener);
    }

    public static void addWeakIgnoredFilesListener(PhpProject project, ChangeListener listener) {
        project.addWeakIgnoredFilesListener(listener);
    }

    public static boolean addWeakProjectPropertyChangeListener(PhpProject project, PropertyChangeListener listener) {
        return project.addWeakPropertyChangeListener(listener);
    }

    public static void addProjectPropertyChangeListener(PhpProject project, PropertyChangeListener listener) {
        project.addPropertyChangeListener(listener);
    }

    public static void removeProjectPropertyChangeListener(PhpProject project, PropertyChangeListener listener) {
        project.removePropertyChangeListener(listener);
    }

    public static FileObject getProjectDirectory(PhpProject project) {
        return project.getProjectDirectory();
    }

    @CheckForNull
    public static FileObject getSourcesDirectory(PhpProject project) {
        return project.getSourcesDirectory();
    }

    @NbBundle.Messages({
        "ProjectPropertiesSupport.browse.tests=Select a directory with project test files.",
        "ProjectPropertiesSupport.browse.tests.info=More directories can be added in Project Properties.",
    })
    public static List<FileObject> getTestDirectories(final PhpProject project, boolean showFileChooser) {
        List<FileObject> testDirs = filterValid(project.getTestsDirectories());
        if (!testDirs.isEmpty()) {
            return testDirs;
        }
        if (!showFileChooser) {
            return Collections.emptyList();
        }
        // show ui
        BrowseTestSources panel = Mutex.EVENT.readAccess(new Mutex.Action<BrowseTestSources>() {
            @Override
            public BrowseTestSources run() {
                return new BrowseTestSources(project, Bundle.ProjectPropertiesSupport_browse_tests(),
                        Bundle.ProjectPropertiesSupport_browse_tests_info());
            }
        });
        if (!panel.open()) {
            return Collections.emptyList();
        }
        File tests = new File(panel.getTestSources());
        assert tests.isDirectory();
        FileObject testsDirectory = FileUtil.toFileObject(tests);
        saveTestSources(project, PhpProjectProperties.TEST_SRC_DIR, tests);
        return Collections.singletonList(testsDirectory);
    }

    @CheckForNull
    public static FileObject getTestDirectory(PhpProject project, FileObject file, boolean showFileChooser) {
        List<FileObject> testDirectories = getTestDirectories(project, showFileChooser);
        if (testDirectories.isEmpty()) {
            return null;
        }
        // XXX find closest root
        FileObject testsDirectory = findClosestDir(testDirectories, file);
        assert testsDirectory != null && testsDirectory.isValid() : testsDirectory;
        return testsDirectory;
    }

    

    @NbBundle.Messages({
        "ProjectPropertiesSupport.browse.selenium.test=Select a directory with project selenium test files.",
    })
    public static List<FileObject> getSeleniumDirectories(final PhpProject project, boolean showFileChooser) {
        List<FileObject> testDirs = filterValid(project.getSeleniumDirectories());
        if (!testDirs.isEmpty()) {
            return testDirs;
        }
        if (!showFileChooser) {
            return Collections.emptyList();
        }
        // show ui
        BrowseTestSources panel = Mutex.EVENT.readAccess(new Mutex.Action<BrowseTestSources>() {
            @Override
            public BrowseTestSources run() {
                return new BrowseTestSources(project, Bundle.ProjectPropertiesSupport_browse_selenium_test(),
                        Bundle.ProjectPropertiesSupport_browse_tests_info());
            }
        });
        if (!panel.open()) {
            return Collections.emptyList();
        }
        File tests = new File(panel.getTestSources());
        assert tests.isDirectory();
        FileObject testsDirectory = FileUtil.toFileObject(tests);
        saveTestSources(project, PhpProjectProperties.SELENIUM_SRC_DIR, tests);
        return Collections.singletonList(testsDirectory);
    }

    /**
     * @return selenium test sources directory or <code>null</code> (if not set up yet e.g.)
     */
    public static FileObject getSeleniumDirectory(final PhpProject project, boolean showFileChooser) {
        FileObject seleniumDirectory = project.getSeleniumDirectory();
        if (seleniumDirectory != null && seleniumDirectory.isValid()) {
            return seleniumDirectory;
        }
        if (showFileChooser) {
            BrowseTestSources panel = Mutex.EVENT.readAccess(new Mutex.Action<BrowseTestSources>() {
                @Override
                public BrowseTestSources run() {
                    return new BrowseTestSources(project, NbBundle.getMessage(ProjectPropertiesSupport.class, "LBL_BrowseSelenium"));
                }
            });
            if (panel.open()) {
                File selenium = new File(panel.getTestSources());
                assert selenium.isDirectory();
                seleniumDirectory = FileUtil.toFileObject(selenium);
                saveTestSources(project, PhpProjectProperties.SELENIUM_SRC_DIR, selenium);
            }
        }
        return seleniumDirectory;
    }

    @CheckForNull
    public static WebBrowser getWebBrowser(PhpProject project) {
        String browserId = project.getEvaluator().getProperty(PhpProjectProperties.BROWSER_ID);
        if (browserId == null) {
            return null;
        }
        return BrowserUISupport.getBrowser(browserId);
    }

    public static boolean getBrowserReloadOnSave(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.BROWSER_RELOAD_ON_SAVE, true);
    }

    public static String getWebRoot(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.WEB_ROOT);
    }

    public static FileObject getWebRootDirectory(PhpProject project) {
        return project.getWebRootDirectory();
    }

    public static File getSourceSubdirectory(PhpProject project, String subdirectoryPath) {
        return getSubdirectory(project, project.getSourcesDirectory(), subdirectoryPath);
    }

    public static File getSubdirectory(PhpProject project, FileObject rootDirectory, String subdirectoryPath) {
        File rootDir = FileUtil.toFile(rootDirectory);
        if (!StringUtils.hasText(subdirectoryPath)) {
            return rootDir;
        }
        // first try to resolve fileobject
        FileObject fo = rootDirectory.getFileObject(subdirectoryPath);
        if (fo != null) {
            return FileUtil.toFile(fo);
        }
        // fallback for OS specific paths (should be changed everywhere, my fault, sorry)
        return PropertyUtils.resolveFile(FileUtil.toFile(rootDirectory), subdirectoryPath);
    }

    public static PhpInterpreter getValidPhpInterpreter(PhpProject project) throws InvalidPhpExecutableException {
        String interpreter = project.getEvaluator().getProperty(PhpProjectProperties.INTERPRETER);
        if (StringUtils.hasText(interpreter)) {
            return PhpInterpreter.getCustom(interpreter);
        }
        return PhpInterpreter.getDefault();
    }

    public static String getPhpInterpreter(PhpProject project) {
        String interpreter = project.getEvaluator().getProperty(PhpProjectProperties.INTERPRETER);
        if (StringUtils.hasText(interpreter)) {
            return interpreter;
        }
        return PhpOptions.getInstance().getPhpInterpreter();
    }

    public static boolean isCopySourcesEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.COPY_SRC_FILES, false);
    }

    public static boolean isCopySourcesOnOpen(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.COPY_SRC_ON_OPEN, false);
    }

    /**
     * @return file or <code>null</code>.
     */
    public static File getCopySourcesTarget(PhpProject project) {
        String targetString = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_TARGET);
        if (targetString != null && targetString.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(targetString));
        }
        return null;
    }

    public static String getEncoding(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.SOURCE_ENCODING);
    }

    public static boolean areShortTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.SHORT_TAGS, PhpLanguageProperties.SHORT_TAGS_ENABLED);
    }

    public static boolean areAspTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.ASP_TAGS, PhpLanguageProperties.ASP_TAGS_ENABLED);
    }

    public static PhpVersion getPhpVersion(PhpProject project) {
        return getPhpVersion(project.getEvaluator().getProperty(PhpProjectProperties.PHP_VERSION));
    }

    public static PhpVersion getPhpVersion(String value) {
        if (value != null) {
            try {
                return PhpVersion.valueOf(value);
            } catch (Exception iae) {
                // ignored
            }
        }
        return PhpVersion.getDefault();
    }

    /**
     * @return run as type, {@link PhpProjectProperties.RunAsType#LOCAL} is the default.
     */
    public static PhpProjectProperties.RunAsType getRunAs(PhpProject project) {
        PhpProjectProperties.RunAsType runAsType = null;
        String runAs = project.getEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        if (runAs != null) {
            try {
                runAsType = PhpProjectProperties.RunAsType.valueOf(runAs);
            } catch (Exception iae) {
                // ignored
            }
        }
        return runAsType != null ? runAsType : PhpProjectProperties.RunAsType.LOCAL;
    }

    /**
     * @return url or <code>null</code>.
     */
    public static String getUrl(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.URL);
    }

    /**
     * @return index file or <code>null</code>.
     */
    public static String getIndexFile(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.INDEX_FILE);
    }

    /**
     * @return arguments or <code>null</code>.
     */
    public static String getArguments(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.ARGS);
    }

    /**
     * @return PHP arguments or <code>null</code>.
     */
    public static String getPhpArguments(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.PHP_ARGS);
    }

    /**
     * @return working directory or <code>null</code>.
     */
    public static String getWorkDir(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.WORK_DIR);
    }

    /**
     * @return remote connection (configuration) name or <code>null</code>.
     */
    public static String getRemoteConnection(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_CONNECTION);
    }

    /**
     * @return remote (upload) directory or <code>null</code>.
     */
    public static String getRemoteDirectory(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_DIRECTORY);
    }

    /**
     * @return <code>true</code> if permissions should be preserved; default is <code>false</code>.
     */
    public static boolean areRemotePermissionsPreserved(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.REMOTE_PERMISSIONS, false);
    }

    /**
     * @return <code>true</code> if upload is direct (and not using a temporary file); default is <code>false</code>.
     */
    public static boolean isRemoteUploadDirectly(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.REMOTE_UPLOAD_DIRECTLY, false);
    }

    /**
     * @return remote upload or <code>null</code>.
     */
    public static PhpProjectProperties.UploadFiles getRemoteUpload(PhpProject project) {
        PhpProjectProperties.UploadFiles uploadFiles = null;
        String remoteUpload = project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_UPLOAD);
        assert remoteUpload != null;
        try {
            uploadFiles = PhpProjectProperties.UploadFiles.valueOf(remoteUpload);
        } catch (Exception iae) {
            // ignored
        }
        return uploadFiles;
    }

    /**
     * @return debug url (default is DEFAULT_URL).
     */
    public static PhpProjectProperties.DebugUrl getDebugUrl(PhpProject project) {
        String debugUrl = project.getEvaluator().getProperty(PhpProjectProperties.DEBUG_URL);
        if (debugUrl == null) {
            return PhpProjectProperties.DebugUrl.DEFAULT_URL;
        }
        return PhpProjectProperties.DebugUrl.valueOf(debugUrl);
    }

    /**
     * @return list of pairs of remote path (as a String) and local path (absolute path, as a String); empty remote paths are skipped
     *         as well as invalid local paths
     */
    public static List<Pair<String, String>> getDebugPathMapping(PhpProject project) {
        List<String> remotes = StringUtils.explode(
                getString(project, PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE, null), PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
        List<String> locals = StringUtils.explode(
                getString(project, PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL, null), PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
        int remotesSize = remotes.size();
        int localsSize = locals.size();
        List<Pair<String, String>> paths = new ArrayList<>(remotesSize);
        for (int i = 0; i < remotesSize; ++i) {
            String remotePath = remotes.get(i);
            if (StringUtils.hasText(remotePath)) {
                // if user has only 1 path and local == sources => property is not stored at all!
                String l = ""; // NOI18N
                if (i < localsSize) {
                    l = locals.get(i);
                }
                String localPath = null;
                File local = new File(l);
                if (local.isAbsolute()) {
                    if (local.isDirectory()) {
                        localPath = local.getAbsolutePath();
                    }
                } else {
                    File subDir = getSourceSubdirectory(project, l);
                    if (subDir.exists()) {
                        localPath = subDir.getAbsolutePath();
                    }
                }

                if (localPath != null) {
                    paths.add(Pair.of(remotePath, localPath));
                }
            }
        }
        Pair<String, String> copySupportPair = getCopySupportPair(project);
        if (copySupportPair != null) {
            paths.add(copySupportPair);
        }
        return paths;
    }

    /**
     * Get debugger proxy (as pair of host, port) or <code>null</code> if it's not set.
     * @return debugger proxy (as pair of host, port) or <code>null</code> if it's not set.
     */
    public static Pair<String, Integer> getDebugProxy(PhpProject project) {
        String host = getString(project, PhpProjectProperties.DEBUG_PROXY_HOST, null);
        if (!StringUtils.hasText(host)) {
            return null;
        }
        return Pair.of(host, getInt(project, PhpProjectProperties.DEBUG_PROXY_PORT, PhpProjectProperties.DEFAULT_DEBUG_PROXY_PORT));
    }

    public static String getHostname(PhpProject project) {
        return getString(project, PhpProjectProperties.HOSTNAME, null);
    }

    public static String getPort(PhpProject project) {
        return getString(project, PhpProjectProperties.PORT, null);
    }

    public static String getInternalRouter(PhpProject project) {
        return getString(project, PhpProjectProperties.ROUTER, null);
    }

    /**
     * @return instance of Pair<String, String> or null
     */
    private static Pair<String, String> getCopySupportPair(PhpProject project) {
        Pair<String, String> copySupportPair = null;
        if (ProjectPropertiesSupport.isCopySourcesEnabled(project)) {
            File copyTarget = ProjectPropertiesSupport.getCopySourcesTarget(project);
            if (copyTarget != null && copyTarget.exists()) {
                FileObject copySourceFo = ProjectPropertiesSupport.getSourcesDirectory(project);
                if (copySourceFo != null) {
                    File copySource = FileUtil.toFile(copySourceFo);
                    if (copySource != null && copySource.exists()) {
                        copySupportPair = Pair.of(copyTarget.getAbsolutePath(), copySource.getAbsolutePath());
                    }
                }
            }
        }
        return copySupportPair;
    }

    private static boolean getBoolean(PhpProject project, String property, boolean defaultValue) {
        String boolValue = project.getEvaluator().getProperty(property);
        if (StringUtils.hasText(boolValue)) {
            return Boolean.parseBoolean(boolValue);
        }
        return defaultValue;
    }

    private static String getString(PhpProject project, String property, String defaultValue) {
        String stringValue = project.getEvaluator().getProperty(property);
        if (stringValue == null) {
            return defaultValue;
        }
        return stringValue;
    }

    private static int getInt(PhpProject project, String property, int defaultValue) {
        String stringValue = project.getEvaluator().getProperty(property);
        if (stringValue != null) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException exc) {
                // ignored
            }
        }
        return defaultValue;
    }

    @NbBundle.Messages("ProjectPropertiesSupport.project.metadata.saving=Saving project metadata...")
    private static void saveTestSources(final PhpProject project, final String propertyName, final File testDir) {
        BaseProgressUtils.showProgressDialogAndRun(new Runnable() {
            @Override
            public void run() {
                // XXX reference helper
                // relativize text path
                File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
                String testPath = PropertyUtils.relativizeFile(projectDirectory, testDir);
                if (testPath == null) {
                    // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
                    testPath = testDir.getAbsolutePath();
                }
                PhpProjectProperties.save(project, Collections.singletonMap(propertyName, testPath), Collections.<String, String>emptyMap());
            }
        }, Bundle.ProjectPropertiesSupport_project_metadata_saving());
    }

    private static List<FileObject> filterValid(FileObject[] files) {
        List<FileObject> validFiles = new ArrayList<>(files.length);
        for (FileObject file : files) {
            if (file.isValid()) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }

    static FileObject findClosestDir(List<FileObject> directories, FileObject fo) {
        assert !directories.isEmpty();
        if (fo == null) {
            return directories.get(0);
        }
        File file = FileUtil.toFile(fo);
        int idx = 0;
        String bestRelPath = null;
        for (int i = 0; i < directories.size(); i++) {
            File dir = FileUtil.toFile(directories.get(i));
            String relPath = PropertyUtils.relativizeFile(dir, file);
            if (relPath == null) {
                // no relative path possible
                continue;
            }
            if (bestRelPath == null) {
                bestRelPath = relPath;
                idx = i;
            } else {
                assert bestRelPath != null;
                if (".".equals(relPath)) { // NOI18N
                    // the folder itself
                    idx = i;
                    break;
                } else if (!relPath.startsWith("../") // NOI18N
                        && bestRelPath.startsWith("../")) { // NOI18N
                    // subdir
                    bestRelPath = relPath;
                    idx = i;
                } else {
                    int relPathLength = relPath.length() - relPath.replace("../", "").length(); // NOI18N
                    int bestRelPathLength = bestRelPath.length() - bestRelPath.replace("../", "").length(); // NOI18N
                    if (relPathLength < bestRelPathLength) {
                        bestRelPath = relPath;
                        idx = i;
                    } else if (relPathLength == bestRelPathLength) {
                        // same number of "../" => compare length
                        if (relPath.length() < bestRelPath.length()) {
                            bestRelPath = relPath;
                            idx = i;
                        }
                    }
                }
            }
        }
        return directories.get(idx);
    }

}

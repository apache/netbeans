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

package org.netbeans.modules.javascript.karma.exec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.javascript.karma.coverage.CoverageProcessor;
import org.netbeans.modules.javascript.karma.coverage.CoverageWatcher;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.run.KarmaRunInfo;
import org.netbeans.modules.javascript.karma.ui.KarmaErrorsDialog;
import org.netbeans.modules.javascript.karma.util.KarmaUtils;
import org.netbeans.modules.javascript.karma.util.StringUtils;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class KarmaServer implements PropertyChangeListener {

    static final Logger LOGGER = Logger.getLogger(KarmaServer.class.getName());

    private static final String SERVER_URL = "http://localhost:%d/"; // NOI18N

    private final int port;
    private final Project project;
    private final Coverage coverage;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ConfigFileChangeListener configFileChangeListener;

    volatile boolean started = false;
    volatile boolean starting = false;

    // @GuardedBy("this")
    private Future<Integer> server;
    // @GuardedBy("this")
    private BrowserSupport browserSupport;
    // @GuardedBy("this")
    private File netBeansKarmaCoverageDir = null;
    // @GuardedBy("this")
    private CoverageWatcher coverageWatcher = null;
    private volatile File netBeansKarmaReporter = null;
    private volatile File netBeansKarmaConfig = null;
    private volatile URL debugUrl = null;
    private volatile KarmaRunInfo karmaRunInfo = null;

    // @GuardedBy("this")
    private String debugPageContent = null;


    KarmaServer(int port, Project project) {
        assert project != null;
        this.port = port;
        this.project = project;
        coverage = Coverage.forProject(project);
        configFileChangeListener = new ConfigFileChangeListener(project);
    }

    @NbBundle.Messages("KarmaServer.start.error=Karma cannot start (incorrect Karma set?), review IDE log for details")
    public synchronized boolean start() {
        assert Thread.holdsLock(this);
        initCoverageWatcher();
        if (isStarted()) {
            return true;
        }
        starting = true;
        fireChange();
        KarmaExecutable karmaExecutable = KarmaExecutable.getDefault(project, true);
        if (karmaExecutable == null) {
            // some error
            starting = false;
            fireChange();
            return false;
        }
        karmaRunInfo = getKarmaRunInfo();
        if (karmaRunInfo == null) {
            // some error
            return false;
        }
        server = karmaExecutable.start(port, karmaRunInfo);
        starting = false;
        if (server != null) {
            started = true;
            addCoverageListener();
            addConfigFileListener();
        } else {
            KarmaErrorsDialog.getInstance().show(Bundle.KarmaServer_start_error());
        }
        fireChange();
        return started;
    }

    public synchronized void runTests() {
        assert Thread.holdsLock(this);
        if (!isStarted()) {
            start();
        }
        if (server == null) {
            // some error
            return;
        }
        KarmaExecutable karmaExecutable = KarmaExecutable.getDefault(project, true);
        assert karmaExecutable != null;
        karmaExecutable.runTests(port);
        if (isDebug()) {
            openDebugUrl();
        }
    }

    void rerunTests() {
        runTests();
    }

    public synchronized void stop() {
        assert Thread.holdsLock(this);
        stopCoverageWatcher();
        removeCoverageListener();
        removeConfigFileListener();
        karmaRunInfo = null;
        if (server == null) {
            return;
        }
        closeDebugUrl();
        if (server.isDone()
                || server.isCancelled()) {
            return;
        }
        server.cancel(true);
        server = null;
        started = false;
        fireChange();
    }

    public boolean isStarting() {
        return starting;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return isStarting()
                || isStarted();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public int getPort() {
        return port;
    }

    public Project getProject() {
        return project;
    }

    public String getServerUrl(@NullAllowed String path) {
        assert path == null || !path.startsWith("/") : path;
        String url = SERVER_URL;
        if (StringUtils.hasText(path)) {
            url += path;
        }
        return String.format(url, getPort());
    }

    public boolean servesUrl(URL url) {
        String externalForm = url.toExternalForm();
        String serverUrl = getServerUrl(null);
        if (externalForm.startsWith(serverUrl)) {
            externalForm = externalForm.substring(serverUrl.length() - 1); // keep the leading "/"
        }
        // first, try karma.files object
        if (getDebugPageContent().contains("'" + externalForm + "'")) { // NOI18N
            return true;
        }
        // now, try <script> tag
        return getDebugPageContent().contains("\"" + externalForm + "\""); // NOI18N
    }

    private synchronized String getDebugPageContent() {
        if (debugPageContent == null) {
            debugPageContent = KarmaUtils.readContent(getDebugUrl());
            assert debugPageContent != null;
        }
        return debugPageContent;
    }

    private synchronized void openDebugUrl() {
        assert server != null : this;
        initBrowserSupport();
        URL url = getDebugUrl();
        assert url != null;
        if (browserSupport.canReload(url)) {
            browserSupport.reload(url);
        } else {
            browserSupport.load(url, project.getProjectDirectory());
        }
    }

    public synchronized void closeDebugUrl() {
        if (browserSupport != null) {
            browserSupport.close(true);
        }
    }

    private void initBrowserSupport() {
        assert Thread.holdsLock(this);
        if (browserSupport == null) {
            String browserId = KarmaPreferences.getDebugBrowserId(project);
            assert browserId != null;
            browserSupport = BrowserSupport.create(BrowserUISupport.getBrowser(browserId));
        }
    }

    private void initCoverageWatcher() {
        assert Thread.holdsLock(this);
        if (coverage == null) {
            // not supported
            return;
        }
        if (coverageWatcher == null) {
            coverageWatcher = new CoverageWatcher(coverage, FileUtil.toFile(project.getProjectDirectory()), getNetBeansKarmaCoverageDir());
            coverageWatcher.start();
        }
    }

    private void stopCoverageWatcher() {
        assert Thread.holdsLock(this);
        if (coverageWatcher != null) {
            coverageWatcher.stop();
            coverageWatcher = null;
        }
    }

    private void addCoverageListener() {
        if (coverage != null) {
            coverage.addPropertyChangeListener(this);
        }
    }

    private void removeCoverageListener() {
        if (coverage != null) {
            coverage.removePropertyChangeListener(this);
        }
    }

    private void addConfigFileListener() {
        configFileChangeListener.startListen(getProjectConfigFile());
    }

    private void removeConfigFileListener() {
        configFileChangeListener.stopListen();
    }

    private URL getDebugUrl() {
        if (debugUrl == null) {
            try {
                String url = KarmaServers.getInstance().getServerUrl(project, "debug.html"); // NOI18N
                assert url != null;
                debugUrl = new URL(url);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            assert debugUrl != null;
        }
        return debugUrl;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @CheckForNull
    private KarmaRunInfo getKarmaRunInfo() {
        File projectConfig = getProjectConfigFile();
        return new KarmaRunInfo.Builder(project)
                .setProjectConfigFile(projectConfig.getAbsolutePath())
                .setNbConfigFile(getNetBeansKarmaConfig().getAbsolutePath())
                .setRerunHandler(new RerunHandlerImpl(this))
                .setFailOnBrowserError(KarmaPreferences.isFailOnBrowserError(project))
                .addEnvVars(getEnvVars(projectConfig))
                .build();
    }

    private Map<String, String> getEnvVars(File projectConfigFile) {
        Map<String, String> envVars = new HashMap<>();
        envVars.put("FILE_SEPARATOR", File.separator); // NOI18N
        envVars.put("PROJECT_CONFIG", projectConfigFile.getAbsolutePath()); // NOI18N
        envVars.put("BASE_DIR", projectConfigFile.getParentFile().getAbsolutePath()); // NOI18N
        envVars.put("AUTOWATCH", KarmaPreferences.isAutowatch(project) ? "1" : ""); // NOI18N
        envVars.put("KARMA_NETBEANS_REPORTER", getNetBeansKarmaReporter().getAbsolutePath()); // NOI18N
        envVars.put("COVERAGE", isCoverageEnabled() ? "1" : ""); // NOI18N
        envVars.put("COVERAGE_DIR", getNetBeansKarmaCoverageDir().getAbsolutePath()); // NOI18N
        envVars.put("DEBUG", isDebug() ? "1" : ""); // NOI18N
        return envVars;
    }

    private File getProjectConfigFile() {
        String config = KarmaPreferences.getConfig(project);
        assert config != null : project.getProjectDirectory();
        return new File(config);
    }

    private File getNetBeansKarmaReporter() {
        if (netBeansKarmaReporter == null) {
            netBeansKarmaReporter = InstalledFileLocator.getDefault().locate(
                    "karma/karma-netbeans-reporter", "org.netbeans.modules.javascript.karma", false); // NOI18N
            assert netBeansKarmaReporter != null;
        }
        return netBeansKarmaReporter;
    }

    private File getNetBeansKarmaConfig() {
        if (netBeansKarmaConfig == null) {
            netBeansKarmaConfig = InstalledFileLocator.getDefault().locate(
                    "karma/karma-netbeans.conf.js", "org.netbeans.modules.javascript.karma", false); // NOI18N
            assert netBeansKarmaConfig != null;
        }
        return netBeansKarmaConfig;
    }

    private synchronized File getNetBeansKarmaCoverageDir() {
        assert Thread.holdsLock(this);
        if (netBeansKarmaCoverageDir == null) {
            FileObject nbproject = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
            assert nbproject != null;
            netBeansKarmaCoverageDir = new File(FileUtil.toFile(nbproject), "private" + File.separatorChar + "karma-coverage"); // NOI18N
        }
        return netBeansKarmaCoverageDir;
    }

    private boolean isDebug() {
        return KarmaPreferences.isDebug(project);
    }

    private boolean isCoverageEnabled() {
        boolean enabled = coverage != null
                && coverage.isEnabled();
        if (isDebug()) {
            if (enabled) {
                CoverageProcessor.warnDebugCoverage();
            }
            return false;
        }
        return enabled;
    }

    @Override
    public String toString() {
        return "KarmaServer{" + "port=" + port + ", project=" + project.getProjectDirectory() + '}'; // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Coverage.PROP_ENABLED.equals(evt.getPropertyName())) {
            if (isRunning()) {
                // XXX ugly
                KarmaServers.getInstance().restartServer(project);
            }
        }
    }

    //~ Inner classes

    private static final class RerunHandlerImpl implements RerunHandler {

        private final KarmaServer karmaServer;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private volatile boolean enabled = true;


        public RerunHandlerImpl(KarmaServer karmaServer) {
            assert karmaServer != null;
            this.karmaServer = karmaServer;
        }

        @Override
        public void rerun() {
            setEnabled(false);
            karmaServer.rerunTests();
            setEnabled(true);
        }

        @Override
        public void rerun(Set<Testcase> tests) {
            throw new UnsupportedOperationException("Not supported by Karma");
        }

        @Override
        public boolean enabled(RerunType type) {
            switch (type) {
                case ALL:
                    return enabled;
                case CUSTOM:
                    return false;
                default:
                    assert false : "Unknown rerun type: " + type;
            }
            return false;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        private void setEnabled(boolean newEnabled) {
            if (enabled != newEnabled) {
                enabled = newEnabled;
                changeSupport.fireChange();
            }
        }

    }

    private static final class ConfigFileChangeListener extends FileChangeAdapter {

        private final Project project;

        // @GuardedBy("this")
        private File projectConfigFile = null;


        public ConfigFileChangeListener(Project project) {
            assert project != null;
            this.project = project;
        }

        public synchronized void startListen(File projectConfigFile) {
            assert Thread.holdsLock(this);
            assert projectConfigFile != null;
            stopListen();
            assert this.projectConfigFile == null : this.projectConfigFile;
            this.projectConfigFile = projectConfigFile;
            FileUtil.addFileChangeListener(this, projectConfigFile);
        }

        public synchronized void stopListen() {
            assert Thread.holdsLock(this);
            if (projectConfigFile != null) {
                FileUtil.removeFileChangeListener(this, projectConfigFile);
            }
            projectConfigFile = null;
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // XXX ugly
            // #245548
            if (KarmaServers.getInstance().isServerRunning(project)) {
                KarmaServers.getInstance().restartServer(project);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            // XXX ugly
            if (KarmaServers.getInstance().isServerRunning(project)) {
                KarmaServers.getInstance().stopServer(project, false);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // XXX ugly
            if (KarmaServers.getInstance().isServerRunning(project)) {
                KarmaServers.getInstance().stopServer(project, false);
            }
        }

    }

}

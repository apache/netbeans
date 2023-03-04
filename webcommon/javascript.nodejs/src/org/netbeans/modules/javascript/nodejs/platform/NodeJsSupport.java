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
package org.netbeans.modules.javascript.nodejs.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.Notifications;
import org.netbeans.modules.javascript.nodejs.ui.actions.NodeJsActionProvider;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsRunPanel;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

public final class NodeJsSupport {

    static final Logger LOGGER = Logger.getLogger(NodeJsSupport.class.getName());

    static final RequestProcessor RP = new RequestProcessor(NodeJsSupport.class);

    final Project project;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    final PreferenceChangeListener optionsListener = new OptionsListener();
    final PreferenceChangeListener preferencesListener = new PreferencesListener();
    private final PropertyChangeListener packageJsonListener = new PackageJsonListener();
    private final FileChangeListener nodeSourcesListener = new NodeSourcesListener();
    final NodeJsPreferences preferences;
    private final ActionProvider actionProvider;
    final NodeJsSourceRoots sourceRoots;
    final PackageJson packageJson;


    private NodeJsSupport(Project project) {
        assert project != null;
        this.project = project;
        actionProvider = new NodeJsActionProvider(project);
        sourceRoots = new NodeJsSourceRoots(project);
        preferences = new NodeJsPreferences(project);
        packageJson = new PackageJson(project.getProjectDirectory());
    }

    @ProjectServiceProvider(service = NodeJsSupport.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static NodeJsSupport create(Project project) {
        NodeJsSupport support = new NodeJsSupport(project);
        // listeners
        NodeJsOptions nodeJsOptions = NodeJsOptions.getInstance();
        nodeJsOptions.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, support.optionsListener, nodeJsOptions));
        return support;
    }

    public static NodeJsSupport forProject(Project project) {
        NodeJsSupport support = project.getLookup().lookup(NodeJsSupport.class);
        assert support != null : "NodeJsSupport should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return support;
    }

    public NodeJsPreferences getPreferences() {
        return preferences;
    }

    public ActionProvider getActionProvider() {
        return actionProvider;
    }

    public List<URL> getSourceRoots() {
        return sourceRoots.getSourceRoots();
    }

    public PackageJson getPackageJson() {
        return packageJson;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(project, propertyName, oldValue, newValue));
    }

    public void fireSourceRootsChanged() {
        sourceRoots.resetSourceRoots();
        firePropertyChanged(NodeJsPlatformProvider.PROP_SOURCE_ROOTS, null, null);
    }

    void projectOpened() {
        FileUtil.addFileChangeListener(nodeSourcesListener, NodeJsUtils.getNodeSources());
        preferences.addPreferenceChangeListener(preferencesListener);
        packageJson.addPropertyChangeListener(packageJsonListener);
        // init node version
        NodeExecutable node = NodeExecutable.forProject(project, false);
        if (node != null) {
            node.getVersion();
        }
    }

    void projectClosed() {
        FileUtil.removeFileChangeListener(nodeSourcesListener, NodeJsUtils.getNodeSources());
        preferences.removePreferenceChangeListener(preferencesListener);
        packageJson.removePropertyChangeListener(packageJsonListener);
        // cleanup
        packageJson.cleanup();
    }

    //~ Inner classes

    private final class OptionsListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "Change event in node.js options ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            String key = evt.getKey();
            LOGGER.log(Level.FINE, "Processing change event {0} in node.js options in project {1}", new Object[] {key, projectName});
            if (preferences.isDefaultNode()
                    && (NodeJsOptions.NODE_PATH.equals(key) || NodeJsOptions.NODE_SOURCES_PATH.equals(key))) {
                fireSourceRootsChanged();
            }
        }

    }

    private final class PreferencesListener implements PreferenceChangeListener {

        // #248870 - 2 events fired in a row (one for 'file', second for 'args')
        private final RequestProcessor.Task startScriptSyncTask = RP.create(new Runnable() {
            @Override
            public void run() {
                startScriptChanged(preferences.getStartFile(), preferences.getStartArgs());
            }
        });


        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            boolean enabled = preferences.isEnabled();
            String key = evt.getKey();
            LOGGER.log(Level.FINE, "Processing change event {0} in node.js preferences in project {1}", new Object[] {key, projectName});
            if (NodeJsPreferences.ENABLED.equals(key)) {
                firePropertyChanged(NodeJsPlatformProvider.PROP_ENABLED, !enabled, enabled);
                if (enabled) {
                    if (NodeJsUtils.isJsLibrary(project)) {
                        // enable node.js run config
                        preferences.setRunEnabled(true);
                        firePropertyChanged(NodeJsPlatformProvider.PROP_RUN_CONFIGURATION, null, NodeJsRunPanel.IDENTIFIER);
                    } else if (preferences.isAskRunEnabled()) {
                        Notifications.notifyRunConfiguration(project);
                    }
                }
            } else if (!enabled) {
                LOGGER.log(Level.FINE, "Change event in node.js preferences ignored, node.js not enabled in project {0}", projectName);
            } else if (NodeJsPreferences.NODE_DEFAULT.equals(key)) {
                fireSourceRootsChanged();
            } else if (!preferences.isDefaultNode()
                    && (NodeJsPreferences.NODE_PATH.equals(key) || NodeJsPreferences.NODE_SOURCES_PATH.equals(key))) {
                fireSourceRootsChanged();
            } else if (NodeJsPreferences.START_FILE.equals(key)
                    || NodeJsPreferences.START_ARGS.equals(key)) {
                startScriptSyncTask.schedule(100);
            }
        }

        @NbBundle.Messages({
            "# {0} - project name",
            "PreferencesListener.sync.title=Node.js ({0})",
            "PreferencesListener.sync.error=Cannot write changed start file/arguments to package.json.",
            "PreferencesListener.sync.done=Start file/arguments synced to package.json.",
        })
        void startScriptChanged(String newStartFile, final String newStartArgs) {
            final String projectDir = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, node.js not enabled in project {0}", projectDir);
                return;
            }
            if (!preferences.isSyncEnabled()) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, sync not enabled", projectDir);
                return;
            }
            if (!StringUtils.hasText(newStartFile)
                    && !StringUtils.hasText(newStartArgs)) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, new file and args are empty", projectDir);
                return;
            }
            String relNewStartFile = newStartFile;
            String relPath = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), new File(newStartFile));
            if (relPath != null) {
                relNewStartFile = relPath;
            }
            if (!packageJson.exists()) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, package.json not exist", projectDir);
                return;
            }
            LOGGER.log(Level.FINE, "Processing Start file/args change in project {0}", projectDir);
            Map<String, Object> content = packageJson.getContent();
            if (content == null) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, package.json has no or invalid content", projectDir);
                return;
            }
            String startFile = null;
            String startArgs = null;
            String startScript = packageJson.getContentValue(String.class, PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START);
            if (startScript != null) {
                Pair<String, String> startInfo = NodeJsUtils.parseStartFile(startScript);
                startFile = startInfo.first();
                startArgs = startInfo.second();
            }
            if (Objects.equals(startFile, relNewStartFile)
                    && Objects.equals(startArgs, newStartArgs)) {
                LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, file and args same as in package.json", projectDir);
                return;
            }
            final String projectName = NodeJsUtils.getProjectDisplayName(project);
            if (preferences.isAskSyncEnabled()) {
                final String relNewStartFileRef = relNewStartFile;
                Notifications.askSyncChanges(project, new Runnable() {
                    @Override
                    public void run() {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                changeStartScript(relNewStartFileRef, newStartArgs, projectName, projectDir);
                            }
                        });
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        preferences.setSyncEnabled(false);
                        LOGGER.log(Level.FINE, "Start file/args change ignored in project {0}, cancelled by user", projectDir);
                    }
                });
            } else {
                changeStartScript(relNewStartFile, newStartArgs, projectName, projectDir);
            }
        }

        void changeStartScript(String relNewStartFile, String newStartArgs, String projectName, String projectDir) {
            StringBuilder sb = new StringBuilder();
            sb.append(NodeJsUtils.START_FILE_NODE_PREFIX);
            sb.append(relNewStartFile);
            if (StringUtils.hasText(newStartArgs)) {
                sb.append(" "); // NOI18N
                sb.append(newStartArgs);
            }
            try {
                packageJson.setContent(Arrays.asList(PackageJson.FIELD_SCRIPTS, PackageJson.FIELD_START), sb.toString());
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                Notifications.informUser(Bundle.PreferencesListener_sync_error());
                return;
            }
            Notifications.notifyUser(Bundle.PreferencesListener_sync_title(projectName), Bundle.PreferencesListener_sync_done());
            LOGGER.log(Level.FINE, "Start file/args change synced to package.json in project {0}", projectDir);
        }

    }

    private final class PackageJsonListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "Property change event in package.json ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            if (!preferences.isSyncEnabled()) {
                LOGGER.log(Level.FINE, "Property change event in package.json ignored, node.js sync not enabled in project {0}", projectName);
                return;
            }
            String propertyName = evt.getPropertyName();
            LOGGER.log(Level.FINE, "Processing property change event {0} in package.json in project {1}", new Object[] {propertyName, projectName});
            if (PackageJson.PROP_NAME.equals(propertyName)) {
                projectNameChanged(evt.getOldValue(), evt.getNewValue());
            } else if (PackageJson.PROP_SCRIPTS_START.equals(propertyName)) {
                startScriptChanged((String) evt.getNewValue());
            }
        }

        private void projectNameChanged(final Object oldName, final Object newName) {
            if (!(newName instanceof String)) {
                LOGGER.log(Level.FINE, "Project name change ignored, not a string: {0}", newName);
                // ignore
                return;
            }
            if (preferences.isAskSyncEnabled()) {
                Notifications.askSyncChanges(project, new Runnable() {
                    @Override
                    public void run() {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                firePropertyChanged(NodeJsPlatformProvider.PROP_PROJECT_NAME, oldName, newName);
                            }
                        });
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        preferences.setSyncEnabled(false);
                        LOGGER.log(Level.FINE, "Project name change ignored in project {0}, cancelled by user", project.getProjectDirectory().getNameExt());
                    }
                });
            } else {
                firePropertyChanged(NodeJsPlatformProvider.PROP_PROJECT_NAME, oldName, newName);
            }
        }

        @NbBundle.Messages({
            "# {0} - project name",
            "PackageJsonListener.sync.title=Node.js ({0})",
            "PackageJsonListener.sync.done=Start file/arguments synced to Project Properties.",
        })
        private void startScriptChanged(String newStartScript) {
            final String projectDir = project.getProjectDirectory().getNameExt();
            if (!StringUtils.hasText(newStartScript)) {
                LOGGER.log(Level.FINE, "Start script change ignored in project {0}, it has no text", projectDir);
                return;
            }
            Pair<String, String> newStartInfo = NodeJsUtils.parseStartFile(newStartScript);
            if (newStartInfo.first() == null) {
                LOGGER.log(Level.FINE, "Start script change ignored in project {0}, no 'file' found", projectDir);
                return;

            }
            final String newStartFile = new File(FileUtil.toFile(project.getProjectDirectory()), newStartInfo.first()).getAbsolutePath();
            String startFile = preferences.getStartFile();
            final boolean syncFile = !Objects.equals(startFile, newStartFile);
            String startArgs = preferences.getStartArgs();
            final String newStartArgs = newStartInfo.second();
            final boolean syncArgs = !Objects.equals(startArgs, newStartArgs);
            if (!syncFile
                    && !syncArgs) {
                LOGGER.log(Level.FINE, "Start script change ignored in project {0}, same values already set", projectDir);
                return;
            }
            final String projectName = NodeJsUtils.getProjectDisplayName(project);
            if (preferences.isAskSyncEnabled()) {
                Notifications.askSyncChanges(project, new Runnable() {
                    @Override
                    public void run() {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                changeStartScript(syncFile, newStartFile, syncArgs, newStartArgs, projectName, projectDir);
                            }
                        });
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        preferences.setSyncEnabled(false);
                        LOGGER.log(Level.FINE, "Start script change ignored in project {0}, cancelled by user", projectDir);
                    }
                });
            } else {
                changeStartScript(syncFile, newStartFile, syncArgs, newStartArgs, projectName, projectDir);
            }
        }

        void changeStartScript(boolean syncFile, String newStartFile, boolean syncArgs, String newStartArgs, String projectName, String projectDir) {
            if (syncFile) {
                preferences.setStartFile(newStartFile);
            }
            if (syncArgs) {
                preferences.setStartArgs(newStartArgs);
            }
            Notifications.notifyUser(Bundle.PackageJsonListener_sync_title(projectName), Bundle.PackageJsonListener_sync_done());
            LOGGER.log(Level.FINE, "Start file/args change synced to project.properties in project {0}", projectDir);
        }

    }

    private final class NodeSourcesListener extends FileChangeAdapter {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "File change event in node sources ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            NodeExecutable node = NodeExecutable.forProject(project, false);
            if (node == null) {
                return;
            }
            Version version = node.getVersion();
            if (version == null) {
                return;
            }
            if (fe.getFile().getNameExt().equals(version.toString())) {
                LOGGER.log(Level.FINE, "Processing file change event in node sources in project {0}", projectName);
                fireSourceRootsChanged();
            }
        }

    }

}

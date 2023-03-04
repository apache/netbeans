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
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.Notifications;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsRunPanel;
import org.netbeans.modules.javascript.nodejs.util.GraalVmUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.modules.web.clientproject.api.BadgeIcon;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProviders;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = PlatformProviderImplementation.class, path = PlatformProviders.PLATFORM_PATH, position = 100)
public final class NodeJsPlatformProvider implements PlatformProviderImplementation, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(NodeJsPlatformProvider.class.getName());

    public static final String IDENT = "node.js"; // NOI18N

    static final RequestProcessor RP = new RequestProcessor(NodeJsPlatformProvider.class);

    @StaticResource
    private static final String ICON_PATH = "org/netbeans/modules/javascript/nodejs/ui/resources/nodejs-badge.png"; // NOI18N

    private final BadgeIcon badgeIcon;
    private final PlatformProviderImplementationListener.Support listenerSupport = new PlatformProviderImplementationListener.Support();


    public NodeJsPlatformProvider() {
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                NodeJsPlatformProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @Override
    public String getIdentifier() {
        return IDENT;
    }

    @NbBundle.Messages("NodeJsPlatformProvider.name=Node.js")
    @Override
    public String getDisplayName() {
        return Bundle.NodeJsPlatformProvider_name();
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isEnabled(Project project) {
        assert project != null;
        return NodeJsSupport.forProject(project).getPreferences().isEnabled();
    }

    @Override
    public List<URL> getSourceRoots(Project project) {
        assert project != null;
        assert isEnabled(project) : "Node.je support must be enabled in this project: " + project.getProjectDirectory().getNameExt();
        return NodeJsSupport.forProject(project).getSourceRoots();
    }

    @Override
    public ActionProvider getActionProvider(Project project) {
        assert project != null;
        return NodeJsSupport.forProject(project).getActionProvider();
    }

    @Override
    public List<CustomizerPanelImplementation> getRunCustomizerPanels(Project project) {
        return Collections.<CustomizerPanelImplementation>singletonList(new NodeJsRunPanel(project));
    }

    @Override
    public void projectOpened(Project project) {
        assert project != null;
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        nodeJsSupport.addPropertyChangeListener(this);
        nodeJsSupport.projectOpened();
        detectNodeJs(project);
        GraalVmUtils.detectOptions();
    }

    @Override
    public void projectClosed(Project project) {
        assert project != null;
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        nodeJsSupport.projectClosed();
        nodeJsSupport.removePropertyChangeListener(this);
    }

    @Override
    public void notifyPropertyChanged(final Project project, final PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (PROP_ENABLED.equals(propertyName)) {
            NodeJsSupport.forProject(project).getPreferences().setEnabled((boolean) event.getNewValue());
        } else if (PROP_PROJECT_NAME.equals(propertyName)) {
            projectNameChanged(project, (String) event.getNewValue());
        } else if (PROP_RUN_CONFIGURATION.equals(propertyName)) {
            runConfigurationChanged(project, event.getNewValue());
        }
    }

    @Override
    public void addPlatformProviderImplementationListener(PlatformProviderImplementationListener listener) {
        listenerSupport.addPlatformProviderImplementationsListener(listener);
    }

    @Override
    public void removePlatformProviderImplementationListener(PlatformProviderImplementationListener listener) {
        listenerSupport.removePlatformProviderImplementationsListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        listenerSupport.firePropertyChanged((Project) evt.getSource(), this,
                new PropertyChangeEvent(this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
    }

    private void detectNodeJs(Project project) {
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        NodeJsPreferences preferences = nodeJsSupport.getPreferences();
        if (preferences.isEnabled()) {
            // already enabled => noop
            return;
        }
        PackageJson packageJson = nodeJsSupport.getPackageJson();
        if (!packageJson.exists()) {
            return;
        }
        Map<String, Object> content = packageJson.getContent();
        if (content == null) {
            // some error
            return;
        }
        Object engines = content.get(PackageJson.FIELD_ENGINES);
        if (engines instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> engines2 = (Map<String, Object>) engines;
            if (engines2.containsKey(PackageJson.FIELD_NODE)) {
                Notifications.notifyNodeJsDetected(project);
            }
        }
    }

    void projectNameChanged(Project project, final String newName) {
        final String projectDir = project.getProjectDirectory().getNameExt();
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        final NodeJsPreferences preferences = nodeJsSupport.getPreferences();
        if (!preferences.isEnabled()) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, node.js not enabled", projectDir);
            return;
        }
        if (!preferences.isSyncEnabled()) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, sync not enabled", projectDir);
            return;
        }
        final PackageJson packageJson = nodeJsSupport.getPackageJson();
        if (!packageJson.exists()) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, package.json not exist", projectDir);
            return;
        }
        LOGGER.log(Level.FINE, "Processing project name change in project {0}", projectDir);
        Map<String, Object> content = packageJson.getContent();
        if (content == null) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, package.json has no or invalid content", projectDir);
            return;
        }
        if (!StringUtils.hasText(newName)) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, new name is empty", projectDir);
            return;
        }
        String name = (String) content.get(PackageJson.FIELD_NAME);
        if (Objects.equals(name, newName)) {
            LOGGER.log(Level.FINE, "Project name change ignored in project {0}, new name same as current name in package.json", projectDir);
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
                            changeProjectName(packageJson, newName, projectName, projectDir);
                        }
                    });
                }
            }, new Runnable() {
                @Override
                public void run() {
                    preferences.setSyncEnabled(false);
                    LOGGER.log(Level.FINE, "Project name change ignored in project {0}, cancelled by user", projectDir);
                }
            });
        } else {
            changeProjectName(packageJson, newName, projectName, projectDir);
        }
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "NodeJsPlatformProvider.sync.title=Node.js ({0})",
        "NodeJsPlatformProvider.sync.error=Cannot write changed project name to package.json.",
        "# {0} - project name",
        "NodeJsPlatformProvider.sync.done=Project name {0} synced to package.json.",
    })
    void changeProjectName(PackageJson packageJson, String newName, String projectName, String projectDir) {
        try {
            packageJson.setContent(Collections.singletonList(PackageJson.FIELD_NAME), newName);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            Notifications.informUser(Bundle.NodeJsPlatformProvider_sync_error());
            return;
        }
        Notifications.notifyUser(Bundle.NodeJsPlatformProvider_sync_title(projectName), Bundle.NodeJsPlatformProvider_sync_done(projectName));
        LOGGER.log(Level.FINE, "Project name change synced to package.json in project {0}", projectDir);
    }

    private void runConfigurationChanged(Project project, Object activeRunConfig) {
        boolean runEnabled = false;
        for (CustomizerPanelImplementation panel : getRunCustomizerPanels(project)) {
            if (panel.getIdentifier().equals(activeRunConfig)) {
                runEnabled = true;
                break;
            }
        }
        NodeJsSupport.forProject(project).getPreferences().setRunEnabled(runEnabled);
    }

}

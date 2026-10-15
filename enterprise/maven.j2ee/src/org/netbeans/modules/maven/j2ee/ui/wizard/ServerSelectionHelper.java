/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.j2ee.ui.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.openide.WizardDescriptor;

/**
 * This class wraps all code which need to be use when creating new Maven project with Server selection.
 * It could be used for all types of projects (Ear, Ejb, War, Client App) and it wraps all the logic
 * that is related to Server-Profile cooperation. For example if the user select Java EE 6 as a target
 * profile, server model needs to be refreshed and show only servers that are deployable on that platform.
 *
 * @author Martin Janicek
 */
public class ServerSelectionHelper {

    private final JComboBox serverModel;
    private final JComboBox j2eeVersion;
    private final ListCellRenderer delegate;
    private final J2eeModule.Type projectType;


    /**
     * Creates new Helper instance for specific project type.
     * It also initiate server model and platform version model combo boxes so they are filled only with supported options
     *
     * @param serverModel combo box for all possible servers
     * @param j2eeVersion combo box for J2ee version specification
     * @param projectType project type
     */
    public ServerSelectionHelper(JComboBox serverModel, JComboBox j2eeVersion, J2eeModule.Type projectType) {
        this.serverModel = serverModel;
        this.projectType = projectType;
        this.delegate = j2eeVersion.getRenderer();

        this.j2eeVersion = j2eeVersion;
        this.j2eeVersion.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String profileName = null;
                if (value instanceof Profile) {
                    profileName = ((Profile) value).getDisplayName();
                }
                if (value instanceof String) {
                    profileName = (String) value;
                }
                return delegate.getListCellRendererComponent(list, profileName, index, isSelected, cellHasFocus);
            }

        });
        initServerModel(null);
        updatePlatformVersionModel();
    }

    /**
     * Initiate servers in comboBox (adds all valid servers plus <No Server> option)
     *
     * @param serverToSelectInstanceID ServerID for server which should be select in comboBox model or <code>null</code>
     * if <No Server> option should be selected
     */
    private void initServerModel(String serverToSelectInstanceID) {
        Server serverToSelect = null;
        List<Server> servers = new ArrayList<Server>();

        // Iterate trought all registered servers
        for (String instanceID : Deployment.getDefault().getServerInstanceIDs()) {
            // We want to add only servers with support for defined projectType
            if (isServerInstanceValid(instanceID)) {
                Server server = new Server(instanceID);
                servers.add(server);

                if (serverToSelectInstanceID != null && instanceID.equals(serverToSelectInstanceID)) {
                    serverToSelect = server;
                }
            }
        }

        // Use last selected server if it's available
        String lastUsedServer = UserProjectSettings.getDefault().getLastUsedServer();
        if (lastUsedServer != null) {
            serverToSelect = new Server(lastUsedServer);
        }

        // Sort the server list
        Collections.sort(servers);

        // We want to provide Maven project without server
        servers.add(new Server(ExecutionChecker.DEV_NULL));

        serverModel.setModel(new DefaultComboBoxModel(servers.toArray()));
        if (serverToSelect != null) {
            serverModel.setSelectedItem(serverToSelect);
        }
        // And we need to change J2eeVersion comboBox when changing Server selection
        serverModel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updatePlatformVersionModel();
            }
        });
    }

    /**
     * Update the platform profile model in dependence of server selection
     * If <No Server Selected> option was chosen then we fill all possible profiles in.
     * If any server was chosen then we fill supported profiles only
     */
    private void updatePlatformVersionModel() {
        Profile lastSelectedProfile = getSelectedProfile();
        String serverInstance = getSelectedServer().getServerInstanceID();
        Set<Profile> profiles = new TreeSet<Profile>(Profile.UI_COMPARATOR);

        // If <No Server> option was selected, show all supported profiles except Java EE 7 profiles
        if (ExecutionChecker.DEV_NULL.equals(serverInstance)) {
            if (J2eeModule.Type.WAR.equals(projectType)) {
                profiles.add(Profile.JAKARTA_EE_11_WEB);
                profiles.add(Profile.JAKARTA_EE_10_WEB);
                profiles.add(Profile.JAKARTA_EE_9_1_WEB);
                profiles.add(Profile.JAKARTA_EE_9_WEB);
                profiles.add(Profile.JAKARTA_EE_8_WEB);
                profiles.add(Profile.JAVA_EE_8_WEB);
                profiles.add(Profile.JAVA_EE_7_WEB);
                profiles.add(Profile.JAVA_EE_6_WEB);
            } else {
                profiles.add(Profile.JAKARTA_EE_11_FULL);
                profiles.add(Profile.JAKARTA_EE_10_FULL);
                profiles.add(Profile.JAKARTA_EE_9_1_FULL);
                profiles.add(Profile.JAKARTA_EE_9_FULL);
                profiles.add(Profile.JAKARTA_EE_8_FULL);
                profiles.add(Profile.JAVA_EE_8_FULL);
                profiles.add(Profile.JAVA_EE_7_FULL);
                profiles.add(Profile.JAVA_EE_6_FULL);
            }
            profiles.add(Profile.JAVA_EE_5);
        } else {
            try {
                J2eePlatform pfm = findServerInstance(serverInstance).getJ2eePlatform();
                Set<Profile> supported = pfm.getSupportedProfiles(projectType);
                profiles.addAll(supported);
            } catch (InstanceRemovedException ex) {
                // If selected instance was removed during the process we can easily refresh Server model list and update versions again
                initServerModel(null);
            }

            // We don't support J2EE 1.3 and J2EE 1.4 anymore
            profiles.remove(Profile.J2EE_13);
            profiles.remove(Profile.J2EE_14);

            // We want to have Java EE 6 Full profile for all project types except Web project
            if (J2eeModule.Type.WAR.equals(projectType)) {
                profiles.remove(Profile.JAKARTA_EE_11_FULL);
                profiles.remove(Profile.JAKARTA_EE_10_FULL);
                profiles.remove(Profile.JAKARTA_EE_9_1_FULL);
                profiles.remove(Profile.JAKARTA_EE_9_FULL);
                profiles.remove(Profile.JAKARTA_EE_8_FULL);
                profiles.remove(Profile.JAVA_EE_8_FULL);
                profiles.remove(Profile.JAVA_EE_7_FULL);
                profiles.remove(Profile.JAVA_EE_6_FULL);
            } else {
                profiles.remove(Profile.JAKARTA_EE_11_WEB);
                profiles.remove(Profile.JAKARTA_EE_10_WEB);
                profiles.remove(Profile.JAKARTA_EE_9_1_WEB);
                profiles.remove(Profile.JAKARTA_EE_9_WEB);
                profiles.remove(Profile.JAKARTA_EE_8_WEB);
                profiles.remove(Profile.JAVA_EE_8_WEB);
                profiles.remove(Profile.JAVA_EE_7_WEB);
                profiles.remove(Profile.JAVA_EE_6_WEB);
            }
        }

        j2eeVersion.setModel(new DefaultComboBoxModel(profiles.toArray()));
        if (lastSelectedProfile != null && profiles.contains(lastSelectedProfile)) {
            j2eeVersion.setSelectedItem(lastSelectedProfile);
        }
    }

    /**
     * Shows UI for adding new server and handles it.
     */
    public void addServerButtonPressed() {
        // If new server were added then we want to set it as selected
        String addedServerInstanceID = ServerManager.showAddServerInstanceWizard();

        if (addedServerInstanceID != null) {
            initServerModel(addedServerInstanceID);
            updatePlatformVersionModel();
        }
    }

    public Profile getSelectedProfile() {
        return (Profile) j2eeVersion.getSelectedItem();
    }

    /**
     * Store all necessary Server settings and J2EE Version into the WizardDescriptor.
     *
     * @param d descriptor used for storing values
     */
    public void storeServerSettings(WizardDescriptor d) {
        Server wrapper = getSelectedServer();
        Profile profile = getSelectedProfile();

        String instanceID = null;
        String serverID = null;
        String version = null;

        if (wrapper != null) {
            instanceID = wrapper.getServerInstanceID();
            serverID = wrapper.getServerID();
        }

        if (profile != null) {
            version = profile.toPropertiesString();
        }

        if (ExecutionChecker.DEV_NULL.equals(instanceID)) {
            instanceID = null;
            serverID = null;
        }
        d.putProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, instanceID);
        d.putProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, serverID);
        d.putProperty(MavenJavaEEConstants.HINT_J2EE_VERSION, version);
    }

    public Server getSelectedServer() {
        return (Server) serverModel.getSelectedItem();
    }

    /**
     * @param instance which need to be validated
     * @return true if the server instance is valid and supports EAR projects, otherwise returns false
     */
    private boolean isServerInstanceValid(String instanceID) {
        ServerInstance instance = findServerInstance(instanceID);

        try {
            if (instance != null &&
                instance.getDisplayName() != null &&
                instance.getJ2eePlatform().getSupportedTypes().contains(projectType)) {

                return true;
            }
        } catch (InstanceRemovedException ex) {
            return false;
        }
        return false;
    }

    private ServerInstance findServerInstance(String instanceID) {
        return Deployment.getDefault().getServerInstance(instanceID);
    }
}

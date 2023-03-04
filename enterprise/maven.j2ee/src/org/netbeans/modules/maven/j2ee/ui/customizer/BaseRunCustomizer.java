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
package org.netbeans.modules.maven.j2ee.ui.customizer;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.utils.Server;
import static org.netbeans.modules.maven.j2ee.ui.customizer.Bundle.*;
import org.netbeans.modules.maven.j2ee.ui.util.WarningPanel;
import org.netbeans.modules.maven.j2ee.ui.util.WarningPanelSupport;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.utils.ServerUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Janicek
 */
public abstract class BaseRunCustomizer extends JPanel implements ApplyChangesCustomizer, HelpCtx.Provider {

    private final J2eeModule.Type type;
    protected final Project project;
    protected final ModelHandle2 handle;
    protected CheckBoxUpdater deployOnSaveUpdater;
    protected ComboBoxUpdater<Server> serverUpdater;


    public BaseRunCustomizer(ModelHandle2 handle, Project project, J2eeModule.Type type) {
        this.handle = handle;
        this.project = project;
        this.type = type;
    }

    @Messages({
        "WARNING_ChangingAutomaticBuild=<html>You are trying to turn deploy on save feature off. <b>Please be aware about "
            + "possible consequences</b>. Your files won't be redeployed immediately after the save which means you "
            + "will be responsible for redeployment of the application content every time when you want to see actual "
            + "state of sources on server. <br><br> Because of that it is highly recommended to turn option called \"Always "
            + "perform build\" on, so you will need only to Run application and NetBeans will take care about automatic rebuild "
            + "of sources before deployment.</html>."
    })
    protected void initDeployOnSave(final JCheckBox dosCheckBox, final JLabel dosDescription) {
        boolean isDoS = MavenProjectSupport.isDeployOnSave(project);

        CheckBoxUpdater.Store store = new CheckBoxUpdater.Store() {

            @Override
            public void storeValue(boolean value) {
                MavenProjectSupport.setDeployOnSave(project, value);
            }
        };

        CheckBoxUpdater.Verify verifier = new CheckBoxUpdater.Verify() {

            @Override
            public boolean verifyValue(boolean value) {
                if (!value && WarningPanelSupport.isAutomaticBuildWarningActivated() && type == J2eeModule.Type.WAR) {
                    WarningPanel panel = new WarningPanel(WARNING_ChangingAutomaticBuild());
                    NotifyDescriptor dd = new NotifyDescriptor.Confirmation(panel, NotifyDescriptor.OK_CANCEL_OPTION);
                    DialogDisplayer.getDefault().notify(dd);

                    if (dd.getValue() == NotifyDescriptor.CANCEL_OPTION) {
                        return false;
                    }
                    if (panel.disabledWarning()) {
                        WarningPanelSupport.dontShowAutomaticBuildWarning();
                    }
                }
                return true;
            }
        };

        deployOnSaveUpdater = CheckBoxUpdater.create(dosCheckBox, isDoS, store, verifier);

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                updateDoSEnablement(dosCheckBox, dosDescription);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    protected void initServerModel(JComboBox serverCBox, JLabel serverLabel) {
        final List<Server> servers = ServerUtils.findServersFor(type);
        final Server defaultServer = ServerUtils.findServer(project);

        serverCBox.setModel(new DefaultComboBoxModel(servers.toArray()));
        
        serverUpdater = ComboBoxUpdater.create(serverCBox, serverLabel, defaultServer, new ComboBoxUpdater.Store() {

            @Override
            public void storeValue(Object newServer) {
                if (newServer == null) {
                    JavaEEProjectSettings.setServerInstanceID(project, defaultServer.getServerInstanceID());
                } else {
                    if (newServer instanceof Server) {
                        Server selectedServer = (Server) newServer;

                        String serverID = selectedServer.getServerID();
                        String serverInstanceID = selectedServer.getServerInstanceID();

                        // User is trying to set <No Server> option
                        if (ExecutionChecker.DEV_NULL.equals(serverInstanceID)) {
                            MavenProjectSupport.setServerID(project, null);
                            JavaEEProjectSettings.setServerInstanceID(project, null);

                        } else {
                            MavenProjectSupport.setServerID(project, serverID);
                            JavaEEProjectSettings.setServerInstanceID(project, serverInstanceID);
                        }
                        MavenProjectSupport.changeServer(project, false);
                    }
                }
            }
        });
    }

    @Messages({
        "DosDescription.text=<html>If selected, files are compiled and deployed when you save them.<br>This option saves you time when you run or debug your application in the IDE.</html>",
        "DosDescriptionIfDisabled.text=<html>If selected, files are compiled and deployed when you save them.<br>This option saves you time when you run or debug your application in the IDE.<br>NOTE: If you want to enable Deploy on Save feature you will need to change Compile on Save first (you can do that in Build/Compile project properties)</html>"
    })
    private void updateDoSEnablement(JCheckBox dosCheckBox, JLabel dosDescription) {
        String cos = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
        boolean enabled = cos == null || "all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos); // NOI18N
        dosCheckBox.setEnabled(enabled);
        dosDescription.setEnabled(enabled);

        if (enabled) {
            dosDescription.setText(DosDescription_text());
        } else {
            dosDescription.setText(DosDescriptionIfDisabled_text());
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("maven_settings");
    }     
}

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

package org.netbeans.modules.maven.j2ee.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.OneTimeDeployment;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class SelectAppServerPanel extends javax.swing.JPanel {

    private NotificationLineSupport nls;
    private Project project;


    private SelectAppServerPanel(boolean showIgnore, Project project) {
        this.project = project;
        initComponents();
        loadComboModel();
        if (showIgnore) {
            checkIgnoreEnablement();
            comServer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkIgnoreEnablement();
                }
            });
            rbIgnore.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    printIgnoreWarning();
                }

            });
        } else {
            rbIgnore.setVisible(false);
        }
        updateProjectLbl();
        rbPermanentStateChanged(null);
    }

    public static boolean showServerSelectionDialog(Project project, J2eeModuleProvider provider, RunConfig config) {
        if (ExecutionChecker.DEV_NULL.equals(provider.getServerInstanceID())) {
            SelectAppServerPanel panel = new SelectAppServerPanel(isGoalOverridden(config), project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(SelectAppServerPanel.class, "TIT_Select"));
            panel.setNLS(dd.createNotificationLineSupport());
            Object obj = DialogDisplayer.getDefault().notify(dd);
            if (obj == NotifyDescriptor.OK_OPTION) {
                String instanceId = panel.getSelectedServerInstance();
                String serverId = panel.getSelectedServerType();
                if (!ExecutionChecker.DEV_NULL.equals(instanceId)) {
                    boolean permanent = panel.isPermanent();
                    boolean doNotRemember = panel.isDoNotRemember();

                    // The server should be used only for this deployment
                    if (doNotRemember) {
                        OneTimeDeployment oneTimeDeployment = project.getLookup().lookup(OneTimeDeployment.class);
                        if (oneTimeDeployment != null) {
                            oneTimeDeployment.setServerInstanceId(instanceId);
                        }

                        MavenProjectSupport.changeServer(project, true);
                    } else if (permanent) {
                        persistServer(project, instanceId, serverId, panel.getChosenProject());
                    } else {
                        SessionContent sc = project.getLookup().lookup(SessionContent.class);
                        if (sc != null) {
                            sc.setServerInstanceId(instanceId);
                        }

                        // We want to initiate context path to default value if there isn't related deployment descriptor yet
                        MavenProjectSupport.changeServer(project, true);
                    }

                    LoggingUtils.logUsage(ExecutionChecker.class, "USG_PROJECT_CONFIG_MAVEN_SERVER", new Object[] { MavenProjectSupport.obtainServerName(project) }, "maven"); //NOI18N

                    return true;
                } else {
                    //ignored used now..
                    if (panel.isIgnored() && config != null) {
                        removeNetbeansDeployFromActionMappings(project, config.getActionName());
                        return true;
                    }
                }
            }
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(SelectAppServerPanel.class, "ERR_Action_without_deployment_server"));
            return false;
        }
        return true;
    }

    private static void removeNetbeansDeployFromActionMappings(Project project, String actionName) {
        try {
            ProjectConfiguration cfg = project.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
            NetbeansActionMapping mapp = ModelHandle2.getMapping(actionName, project, cfg);
            if (mapp != null) {
                mapp.getProperties().remove(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY);
                ModelHandle2.putMapping(mapp, project, cfg);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Finds out if the run configuration uses different goal than the default one.
     *
     * <p>
     * The purpose of this method is to find out if there is some 'special' action
     * defined by the user instead of default run process. That might be for example
     * running jetty:run goal which starts temporary server which doesn't need to be
     * defined inside of NetBeans.
     * </p>
     *
     * @param config configuration of this project
     * @return {@code true} in case user overrides the goal, {@code false} otherwise
     */
    private static boolean isGoalOverridden(RunConfig config) {
        if (config == null) {
            return false;
        }
        return !config.getGoals().isEmpty();
    }

    private static void persistServer(Project project, final String iID, final String sID, final Project targetPrj) {
        JavaEEProjectSettings.setServerInstanceID(project, iID);
        MavenProjectSupport.setServerID(project, sID);

        // We want to initiate context path to default value if there isn't related deployment descriptor yet
        MavenProjectSupport.changeServer(project, true);

        // refresh all subprojects
        //TODO replace with DependencyProjectProvider or ProjectContainerProvider
        SubprojectProvider spp = targetPrj.getLookup().lookup(SubprojectProvider.class);
        //mkleint: we are assuming complete result (transitive projects included)
        //that's ok as far as the current maven impl goes afaik, but not according to the
        //documentation for SubProjectprovider
        Set<? extends Project> childrenProjs = spp.getSubprojects();
        if (!childrenProjs.contains(project)) {
            NbMavenProject.fireMavenProjectReload(project);
        }
        for (Project curPrj : childrenProjs) {
            NbMavenProject.fireMavenProjectReload(curPrj);
        }

    }

    private String getSelectedServerType() {
        return ((Server) comServer.getSelectedItem()).getServerID();
    }

    private String getSelectedServerInstance() {
        return ((Server) comServer.getSelectedItem()).getServerInstanceID();
    }

    private boolean isPermanent() {
        return rbPermanent.isSelected();
    }

    private boolean isIgnored() {
        return rbIgnore.isSelected();
    }

    private boolean isDoNotRemember() {
        return rbDontRemember.isSelected();
    }

    private Project getChosenProject() {
        return project;
    }

    private void loadComboModel() {
        Ear ear = Ear.getEar(project.getProjectDirectory());
        EjbJar ejb = EjbJar.getEjbJar(project.getProjectDirectory());
        WebModule war = WebModule.getWebModule(project.getProjectDirectory());
        J2eeModule.Type type = ear != null ? J2eeModule.Type.EAR :
                                     ( war != null ? J2eeModule.Type.WAR :
                                           (ejb != null ? J2eeModule.Type.EJB : J2eeModule.Type.CAR));
        Profile profile = ear != null ? ear.getJ2eeProfile() :
                                     ( war != null ? war.getJ2eeProfile() :
                                           (ejb != null ? ejb.getJ2eeProfile() : Profile.JAVA_EE_6_FULL));
        String[] ids = Deployment.getDefault().getServerInstanceIDs(Collections.singletonList(type), profile);
        Collection<Server> col = new ArrayList<>();
        col.add(new Server(ExecutionChecker.DEV_NULL));

        for (int i = 0; i < ids.length; i++) {
            Server wr = new Server(ids[i]);
            col.add(wr);
        }
        comServer.setModel(new DefaultComboBoxModel(col.toArray()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblServer = new javax.swing.JLabel();
        comServer = new javax.swing.JComboBox();
        rbSession = new javax.swing.JRadioButton();
        rbPermanent = new javax.swing.JRadioButton();
        rbIgnore = new javax.swing.JRadioButton();
        lblProject = new javax.swing.JLabel();
        btChange = new javax.swing.JButton();
        rbDontRemember = new javax.swing.JRadioButton();

        lblServer.setLabelFor(comServer);
        org.openide.awt.Mnemonics.setLocalizedText(lblServer, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.lblServer.text")); // NOI18N

        buttonGroup1.add(rbSession);
        org.openide.awt.Mnemonics.setLocalizedText(rbSession, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbSession.text")); // NOI18N

        buttonGroup1.add(rbPermanent);
        org.openide.awt.Mnemonics.setLocalizedText(rbPermanent, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbPermanent.text")); // NOI18N
        rbPermanent.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbPermanentStateChanged(evt);
            }
        });

        buttonGroup1.add(rbIgnore);
        org.openide.awt.Mnemonics.setLocalizedText(rbIgnore, org.openide.util.NbBundle.getBundle(SelectAppServerPanel.class).getString("SelectAppServerPanel.rbIgnore.text")); // NOI18N

        lblProject.setFont(lblProject.getFont().deriveFont(lblProject.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(lblProject, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.lblProject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btChange, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.btChange.text")); // NOI18N
        btChange.setToolTipText(org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.btChange.toolTipText")); // NOI18N
        btChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btChangeActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbDontRemember);
        rbDontRemember.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbDontRemember, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbDontRemember.text")); // NOI18N
        rbDontRemember.setToolTipText(org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbDontRemember.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblServer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comServer, 0, 443, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbPermanent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btChange))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbIgnore)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(lblProject))
                            .addComponent(rbDontRemember)
                            .addComponent(rbSession))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServer)
                    .addComponent(comServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(rbDontRemember)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbSession)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btChange, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbPermanent))
                .addGap(18, 18, 18)
                .addComponent(lblProject)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbIgnore)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbPermanentStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbPermanentStateChanged
        boolean isSel = rbPermanent.isSelected();
        btChange.setEnabled(isSel);
        lblProject.setEnabled(isSel);
        if (nls != null) {
            if (isSel) {
                nls.setInformationMessage(NbBundle.getMessage(
                        SelectAppServerPanel.class, "MSG_ParentHint"));
            } else {
                nls.clearMessages();
            }
        }
    }//GEN-LAST:event_rbPermanentStateChanged

    private void btChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btChangeActionPerformed
        SelectProjectPanel spp = new SelectProjectPanel(project);
        final DialogDescriptor dd = new DialogDescriptor(spp, NbBundle.getMessage(SelectAppServerPanel.class, "TIT_ChooseParent"));
        spp.attachDD(dd);

        Object obj = DialogDisplayer.getDefault().notify(dd);
        if (obj == NotifyDescriptor.OK_OPTION) {
            project = spp.getSelectedProject();
            updateProjectLbl();
        }

    }//GEN-LAST:event_btChangeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btChange;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comServer;
    private javax.swing.JLabel lblProject;
    private javax.swing.JLabel lblServer;
    private javax.swing.JRadioButton rbDontRemember;
    javax.swing.JRadioButton rbIgnore;
    javax.swing.JRadioButton rbPermanent;
    javax.swing.JRadioButton rbSession;
    // End of variables declaration//GEN-END:variables

    private void checkIgnoreEnablement() {
        String selectedServer = getSelectedServerType();
        if (selectedServer == null || ExecutionChecker.DEV_NULL.equals(selectedServer)) {
            rbIgnore.setEnabled(true);
        } else {
            if (rbIgnore.isSelected()) {
                rbSession.setSelected(true);
            }
            rbIgnore.setEnabled(false);
        }
    }

    private void setNLS(NotificationLineSupport notif) {
        nls = notif;
    }

    private void printIgnoreWarning() {
        if (rbIgnore.isSelected()) {
            nls.setWarningMessage(NbBundle.getMessage(SelectAppServerPanel.class, "WARN_Ignore_Server"));
        } else {
            nls.clearMessages();
        }
    }

    private void updateProjectLbl() {
        ProjectInformation pi = ProjectUtils.getInformation(project);
        if (pi != null) {
            lblProject.setText(NbBundle.getMessage(SelectAppServerPanel.class,
                    "MSG_InProject", pi.getDisplayName()));
        }
    }

}

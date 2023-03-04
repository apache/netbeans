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

package org.netbeans.modules.gradle.javaee.customizer;

import org.netbeans.modules.gradle.javaee.api.ui.support.DisplayNameListCellRenderer;
import org.netbeans.modules.gradle.javaee.api.ui.support.JavaEEServerComboBoxModel;
import org.netbeans.modules.gradle.javaee.api.ui.support.CheckBoxUpdater;
import org.netbeans.modules.gradle.javaee.api.ui.support.ComboBoxUpdater;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.gradle.javaee.web.WebModuleProviderImpl;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.api.BrowserUISupport.BrowserComboBoxModel;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
public class CustomizerRunWar extends javax.swing.JPanel {

    public static final String PROP_SHOW_IN_BROWSER = "netbeans.deploy.show";
    public static final String PROP_SHOW_PAGE = "netbeans.deploy.show.page";

    private final Project project;

    final ComboBoxUpdater serverUpdater;
    final CheckBoxUpdater showInBrowserUpdater;
    final BrowserComboBoxModel browserModel;

    /**
     * Creates new form CustomizerRunWar
     */
    @Messages("NO_PROFILE=<No Profile Could be Detected>")
    public CustomizerRunWar(Project project) {
        this.project = project;
        initComponents();

        String instanceId = JavaEEProjectSettings.getServerInstanceID(project);
        Profile profile = JavaEEProjectSettings.getProfile(project);
        tfVersion.setText(profile != null ? profile.getDisplayName() : Bundle.NO_PROFILE());
        if (profile != null) {
            cbServer.setModel(JavaEEServerComboBoxModel.createJavaEEServerComboBoxModel(instanceId, J2eeModule.Type.WAR, profile));
            cbServer.setRenderer(new DisplayNameListCellRenderer<>(cbServer.getRenderer()));

            serverUpdater = ComboBoxUpdater.create(cbServer, lbServer, getServer(instanceId), (Object value) -> {
                String serverId = JavaEEServerComboBoxModel.getServerInstanceID(cbServer.getItemAt(cbServer.getSelectedIndex()));
                JavaEEProjectSettings.setServerInstanceID(project, serverId);
            });
        } else {
            serverUpdater = null;
        }

        WebModuleProviderImpl moduleProviderImpl = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (moduleProviderImpl != null) {
            tfContextPath.setText(moduleProviderImpl.getModuleImpl().getContextPath());
        }
        String selectedBrowser = JavaEEProjectSettings.getBrowserID(project);
        browserModel = BrowserUISupport.createBrowserModel(selectedBrowser, true);
        cbBrowser.setModel(browserModel);
        cbBrowser.setRenderer(BrowserUISupport.createBrowserRenderer());

        Preferences prefs = NbGradleProject.getPreferences(project, false);
        Boolean showInBrowser = prefs.getBoolean(PROP_SHOW_IN_BROWSER, true);
        showInBrowserUpdater = CheckBoxUpdater.create(cbBrowserOnRun, showInBrowser, (boolean value) -> {
            boolean show = cbBrowserOnRun.isSelected();
            prefs.putBoolean(PROP_SHOW_IN_BROWSER, show);
            prefs.put(PROP_SHOW_PAGE, tfRelativeURL.getText().trim());
        });

        String relativeUrl = prefs.get(PROP_SHOW_PAGE, "");
        tfRelativeURL.setText(relativeUrl);
    }

    private void saveContextPath() {
        String newPath = tfContextPath.getText().trim();
        WebModuleProviderImpl moduleProviderImpl = project.getLookup().lookup(WebModuleProviderImpl.class);
        if (moduleProviderImpl != null && !newPath.equals(moduleProviderImpl.getModuleImpl().getContextPath())) {
            moduleProviderImpl.getModuleImpl().setContextPath(newPath);
        }
    }

    void save() {
        if (serverUpdater != null) {
            serverUpdater.storeValue();
        }
        showInBrowserUpdater.storeValue();
        saveContextPath();
        JavaEEProjectSettings.setBrowserID(project, browserModel.getSelectedBrowserId());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbServer = new javax.swing.JLabel();
        cbServer = new javax.swing.JComboBox<>();
        lbVersion = new javax.swing.JLabel();
        lbContextPath = new javax.swing.JLabel();
        lbRelativeURL = new javax.swing.JLabel();
        lbBrowser = new javax.swing.JLabel();
        cbBrowserOnRun = new javax.swing.JCheckBox();
        tfContextPath = new javax.swing.JTextField();
        tfRelativeURL = new javax.swing.JTextField();
        cbBrowser = new javax.swing.JComboBox<>();
        tfVersion = new javax.swing.JTextField();

        lbServer.setLabelFor(cbServer);
        org.openide.awt.Mnemonics.setLocalizedText(lbServer, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.lbServer.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbVersion, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.lbVersion.text")); // NOI18N

        lbContextPath.setLabelFor(tfContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(lbContextPath, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.lbContextPath.text")); // NOI18N

        lbRelativeURL.setLabelFor(tfRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(lbRelativeURL, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.lbRelativeURL.text")); // NOI18N

        lbBrowser.setLabelFor(cbBrowser);
        org.openide.awt.Mnemonics.setLocalizedText(lbBrowser, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.lbBrowser.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbBrowserOnRun, org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.cbBrowserOnRun.text")); // NOI18N

        tfContextPath.setText(org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.tfContextPath.text")); // NOI18N

        tfRelativeURL.setText(org.openide.util.NbBundle.getMessage(CustomizerRunWar.class, "CustomizerRunWar.tfRelativeURL.text")); // NOI18N

        tfVersion.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbServer, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfVersion)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbContextPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfContextPath))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbBrowser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbBrowser, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(lbRelativeURL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfRelativeURL, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbBrowserOnRun))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lbBrowser, lbContextPath, lbRelativeURL, lbVersion});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbServer)
                    .addComponent(cbServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbVersion)
                    .addComponent(tfVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbContextPath)
                    .addComponent(tfContextPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbBrowserOnRun, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbRelativeURL)
                    .addComponent(tfRelativeURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbBrowser)
                    .addComponent(cbBrowser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(110, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<WebBrowser> cbBrowser;
    private javax.swing.JCheckBox cbBrowserOnRun;
    private javax.swing.JComboBox<J2eePlatform> cbServer;
    private javax.swing.JLabel lbBrowser;
    private javax.swing.JLabel lbContextPath;
    private javax.swing.JLabel lbRelativeURL;
    private javax.swing.JLabel lbServer;
    private javax.swing.JLabel lbVersion;
    private javax.swing.JTextField tfContextPath;
    private javax.swing.JTextField tfRelativeURL;
    private javax.swing.JTextField tfVersion;
    // End of variables declaration//GEN-END:variables

    private J2eePlatform getServer(String id) {
        if (id == null) {
            return null;
        }
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(id);
        try {
            return serverInstance != null ? serverInstance.getJ2eePlatform() : null;
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }
}

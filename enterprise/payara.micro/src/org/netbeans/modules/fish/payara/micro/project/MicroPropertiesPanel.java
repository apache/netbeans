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
package org.netbeans.modules.fish.payara.micro.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.AUTO_DEPLOY;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEPLOY_WAR;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEV_MODE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.EXPLODED;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.HOT_DEPLOY;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.KEEP_STATE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.LIVE_RELOAD;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.TRIM_LOG;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.VERSION;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class MicroPropertiesPanel extends JPanel {

    private final Preferences pref;

    private final ComboBoxUpdater<PayaraPlatformVersionAPI> microVersionComboBoxUpdater;

    private PayaraPlatformVersionAPI selectedPayaraVersion;

    public MicroPropertiesPanel(ModelHandle2 handle, Project project) {
        pref = getPreferences(project, MicroApplication.class, true);
        initComponents();
        String microVersionText = pref.get(VERSION, "");
        PayaraPlatformVersionAPI microVersion = PayaraPlatformVersion.toValue(microVersionText);
        microVersionComboBoxUpdater = new ComboBoxUpdater<PayaraPlatformVersionAPI>(microVersionCombobox, microVersionLabel) {
            @Override
            public PayaraPlatformVersionAPI getValue() {
                return microVersion != null ? microVersion : PayaraPlatformVersion.EMPTY;
            }

            @Override
            public PayaraPlatformVersionAPI getDefaultValue() {
                return null;
            }

            @Override
            public void setValue(PayaraPlatformVersionAPI microVersion) {
                selectedPayaraVersion = microVersion;
            }
        };
        hotDeployCheckBox.setSelected(pref.getBoolean(HOT_DEPLOY, false));
        devModeCheckBox.setSelected(pref.getBoolean(DEV_MODE, true));
        deployWarCheckBox.setSelected(pref.getBoolean(DEPLOY_WAR, true));
        explodedCheckBox.setSelected(pref.getBoolean(EXPLODED, true));
        trimLogCheckBox.setSelected(pref.getBoolean(TRIM_LOG, true));
        autoDeployCheckBox.setSelected(pref.getBoolean(AUTO_DEPLOY, true));
        keepStateCheckBox.setSelected(pref.getBoolean(KEEP_STATE, true));
        liveReloadCheckBox.setSelected(pref.getBoolean(LIVE_RELOAD, true));
    }

    private PayaraPlatformVersionAPI[] getPayaraVersion() {
        List<PayaraPlatformVersionAPI> microVersions = new ArrayList<>();
        microVersions.add(PayaraPlatformVersion.EMPTY);
        microVersions.addAll(
                PayaraPlatformVersion.getVersions()
                        .stream()
                        .sorted(Collections.reverseOrder())
                        .collect(toList())
        );
        return microVersions.toArray(new PayaraPlatformVersionAPI[]{});
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        microVersionLabel = new javax.swing.JLabel();
        microVersionCombobox = new javax.swing.JComboBox();
        hotDeployLabel = new javax.swing.JLabel();
        hotDeployCheckBox = new javax.swing.JCheckBox();
        devModeLabel = new javax.swing.JLabel();
        devModeCheckBox = new javax.swing.JCheckBox();
        explodedCheckBox = new javax.swing.JCheckBox();
        explodedLabel = new javax.swing.JLabel();
        deployWarLabel = new javax.swing.JLabel();
        deployWarCheckBox = new javax.swing.JCheckBox();
        autoDeployLabel = new javax.swing.JLabel();
        liveReloadCheckBox = new javax.swing.JCheckBox();
        liveReloadLabel = new javax.swing.JLabel();
        autoDeployCheckBox = new javax.swing.JCheckBox();
        trimLogLabel = new javax.swing.JLabel();
        keepStateCheckBox = new javax.swing.JCheckBox();
        keepStateLabel = new javax.swing.JLabel();
        trimLogCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(microVersionLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.microVersionLabel.text")); // NOI18N

        microVersionCombobox.setModel(new DefaultComboBoxModel(getPayaraVersion()));

        org.openide.awt.Mnemonics.setLocalizedText(hotDeployLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.hotDeployLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hotDeployCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.hotDeployCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devModeLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.devModeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(devModeCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.devModeCheckBox.text")); // NOI18N
        devModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                devModeCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(explodedCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.explodedCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(explodedLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.explodedLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deployWarLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.deployWarLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deployWarCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.deployWarCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoDeployLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.autoDeployLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(liveReloadCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.liveReloadCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(liveReloadLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.liveReloadLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoDeployCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.autoDeployCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(trimLogLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.trimLogLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepStateCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.keepStateCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepStateLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.keepStateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(trimLogCheckBox, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.trimLogCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microVersionLabel)
                    .addComponent(hotDeployLabel)
                    .addComponent(devModeLabel)
                    .addComponent(deployWarLabel)
                    .addComponent(explodedLabel)
                    .addComponent(autoDeployLabel)
                    .addComponent(liveReloadLabel)
                    .addComponent(keepStateLabel)
                    .addComponent(trimLogLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microVersionCombobox, 0, 212, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(explodedCheckBox)
                            .addComponent(deployWarCheckBox)
                            .addComponent(liveReloadCheckBox)
                            .addComponent(autoDeployCheckBox)
                            .addComponent(trimLogCheckBox)
                            .addComponent(keepStateCheckBox)
                            .addComponent(devModeCheckBox)
                            .addComponent(hotDeployCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microVersionCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(microVersionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hotDeployLabel)
                    .addComponent(hotDeployCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(devModeLabel)
                    .addComponent(devModeCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deployWarLabel)
                    .addComponent(deployWarCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(explodedLabel)
                    .addComponent(explodedCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autoDeployLabel)
                    .addComponent(autoDeployCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(liveReloadLabel)
                    .addComponent(liveReloadCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keepStateLabel)
                    .addComponent(keepStateCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(trimLogLabel)
                    .addComponent(trimLogCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void devModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_devModeCheckBoxActionPerformed
        boolean selected = devModeCheckBox.isSelected();
        deployWarCheckBox.setSelected(selected);
        explodedCheckBox.setSelected(selected);
        trimLogCheckBox.setSelected(selected);
        autoDeployCheckBox.setSelected(selected);
        keepStateCheckBox.setSelected(selected);
        liveReloadCheckBox.setSelected(selected);
    }//GEN-LAST:event_devModeCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoDeployCheckBox;
    private javax.swing.JLabel autoDeployLabel;
    private javax.swing.JCheckBox deployWarCheckBox;
    private javax.swing.JLabel deployWarLabel;
    private javax.swing.JCheckBox devModeCheckBox;
    private javax.swing.JLabel devModeLabel;
    private javax.swing.JCheckBox explodedCheckBox;
    private javax.swing.JLabel explodedLabel;
    private javax.swing.JCheckBox hotDeployCheckBox;
    private javax.swing.JLabel hotDeployLabel;
    private javax.swing.JCheckBox keepStateCheckBox;
    private javax.swing.JLabel keepStateLabel;
    private javax.swing.JCheckBox liveReloadCheckBox;
    private javax.swing.JLabel liveReloadLabel;
    private javax.swing.JComboBox microVersionCombobox;
    private javax.swing.JLabel microVersionLabel;
    private javax.swing.JCheckBox trimLogCheckBox;
    private javax.swing.JLabel trimLogLabel;
    // End of variables declaration//GEN-END:variables

    public void applyChanges() {
        pref.put(VERSION, selectedPayaraVersion != null ? selectedPayaraVersion.toString() : "");
        pref.put(HOT_DEPLOY, Boolean.toString(hotDeployCheckBox.isSelected()));
        pref.put(DEV_MODE, Boolean.toString(devModeCheckBox.isSelected()));
        pref.put(DEPLOY_WAR, Boolean.toString(deployWarCheckBox.isSelected()));
        pref.put(EXPLODED, Boolean.toString(explodedCheckBox.isSelected()));
        pref.put(TRIM_LOG, Boolean.toString(trimLogCheckBox.isSelected()));
        pref.put(AUTO_DEPLOY, Boolean.toString(autoDeployCheckBox.isSelected()));
        pref.put(KEEP_STATE, Boolean.toString(keepStateCheckBox.isSelected()));
        pref.put(LIVE_RELOAD, Boolean.toString(liveReloadCheckBox.isSelected()));
    }

}

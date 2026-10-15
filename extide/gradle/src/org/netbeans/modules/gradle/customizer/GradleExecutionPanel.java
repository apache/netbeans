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
package org.netbeans.modules.gradle.customizer;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.customizer.GradleExecutionPanel.TrustLevel.*;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager.JavaRuntime;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;

/**
 *
 * @author lkishalmi
 */
public class GradleExecutionPanel extends javax.swing.JPanel {
    public static final String HINT_JDK_PLATFORM = "hint.jdkPlatform"; //NOI18N
    
    enum TrustLevel {
        PERMANENT,
        TEMPORARY,
        NONE;
        
        @Override
        public String toString() {
            return NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.cbTrustLevel." + name()); // NOI18N
        }
        
    }

    private Project project;
    private final JavaRuntimeManager runtimeManager;
    private final ChangeListener runtimeChangeListener = (evt) -> managedRuntimeSetup();
    private boolean readOnly = true;

    /**
     * Creates new form GradleExecutionPanel
     */
    @Messages({
        "NO_RUNTIME_SUPPORT_HINT=Runtime Change is not Supported",
        "NO_RUNTIME_MANAGEMENT_HINT=Runtime Management is not Supported",
    })
    public GradleExecutionPanel() {
        initComponents();
        runtimeManager = Lookup.getDefault().lookup(JavaRuntimeManager.class);
        if (runtimeManager == null) {
            cbRuntime.setToolTipText(Bundle.NO_RUNTIME_SUPPORT_HINT());
            btManageRuntimes.setToolTipText(Bundle.NO_RUNTIME_SUPPORT_HINT());
        } else {
            runtimeManager.addChangeListener(WeakListeners.change(runtimeChangeListener, runtimeManager));
            managedRuntimeSetup();
            if (!runtimeManager.manageRuntimesAction().isPresent()) {
                btManageRuntimes.setToolTipText(Bundle.NO_RUNTIME_MANAGEMENT_HINT());
            }
        }

    }

    public GradleExecutionPanel(Project project) {
        this();
        this.project = project;
        GradleBaseProject gbp = GradleBaseProject.get(project);
        readOnly = (gbp != null) && !gbp.isRoot();

        lbReadOnly.setVisible(readOnly);
        lbTrustLevel.setEnabled(!readOnly);
        cbTrustLevel.setEnabled(!readOnly);
        lbTrustTerms.setEnabled(!readOnly);

        cbTrustLevel.setModel(new DefaultComboBoxModel<>(TrustLevel.values()));

        if (ProjectTrust.getDefault().isTrustedPermanently(project)) {
            cbTrustLevel.setSelectedItem(PERMANENT);
            setJavaSettingsEnabled(!readOnly);
        } else if (ProjectTrust.getDefault().isTrusted(project)) {
            cbTrustLevel.setSelectedItem(TEMPORARY);
            setJavaSettingsEnabled(!readOnly);
        } else {
            cbTrustLevel.setSelectedItem(NONE);
            setJavaSettingsEnabled(false);
        }

        cbRuntime.setRenderer(new RuntimeRenderer());
        selectRuntime(RunUtils.getActiveRuntime(project));

        setupCheckBox(cbAugmentedBuild, RunUtils.PROP_AUGMENTED_BUILD, true);
        setupCheckBox(cbIncludeOpenProjects, RunUtils.PROP_INCLUDE_OPEN_PROJECTS, false);
    }

    private void selectRuntime(JavaRuntime selected) {
        ComboBoxModel<JavaRuntime> model = cbRuntime.getModel();
        if (selected == null || selected.isBroken()) {
            model.setSelectedItem(selected);
        } else {
            for (int i = 0; i < model.getSize(); i++) {
                JavaRuntime rt = model.getElementAt(i);
                if (rt.equals(selected)) {
                    model.setSelectedItem(model.getElementAt(i));
                    break;
                }
            }
        }
    }
    
    @Messages({
        "# {0} - the name of the setting property",
        "COMPILE_DISABLED_HINT=<html>This option is currently specificly controlled"
        + " through your Gradle project (most likely through "
        + "<b>gradle.properties</b>) by <br/> <b>netbeans.{0}</b> property."
    })
    private void setupCheckBox(JCheckBox check, String property, boolean defaultValue) {
        Project root = ProjectUtils.rootOf(project);
        GradleBaseProject gbp = GradleBaseProject.get(root);
        if (gbp != null) {
            if (gbp.getNetBeansProperty(property) != null) {
                check.setEnabled(!readOnly);
                check.setSelected(Boolean.parseBoolean(gbp.getNetBeansProperty(property)));
                check.setToolTipText(Bundle.COMPILE_DISABLED_HINT(property));
            } else {
                Preferences prefs = NbGradleProject.getPreferences(root, false);
                check.setSelected(prefs.getBoolean(property, defaultValue));
            }
        }
    }

    private void managedRuntimeSetup() {
        int selected = cbRuntime.getSelectedIndex();
        JavaRuntime runtime = selected != -1 ? cbRuntime.getModel().getElementAt(selected) : null;
        Map<String, JavaRuntime> availabeRuntimes = runtimeManager != null ? runtimeManager.getAvailableRuntimes() : Collections.emptyMap();

        JavaRuntime[] runtimes = availabeRuntimes.values().toArray(new JavaRuntime[0]);
        Arrays.sort(runtimes);
        
        DefaultComboBoxModel<JavaRuntime> model = new DefaultComboBoxModel<>(runtimes);
        cbRuntime.setModel(model);
        selectRuntime(runtime);
    }

    private void setJavaSettingsEnabled(boolean b) {
        boolean enableRuntime = b && (runtimeManager != null);

        lbRuntime.setEnabled(enableRuntime);
        cbRuntime.setEnabled(enableRuntime);
        btManageRuntimes.setEnabled(enableRuntime && runtimeManager.manageRuntimesAction().isPresent());

        cbAugmentedBuild.setEnabled(b);
        lbAugmentedBuild.setEnabled(b);

        cbIncludeOpenProjects.setEnabled(b);
        lbIncludeOpenProjects.setEnabled(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbRuntime = new javax.swing.JLabel();
        cbRuntime = new javax.swing.JComboBox<>();
        btManageRuntimes = new javax.swing.JButton();
        cbAugmentedBuild = new javax.swing.JCheckBox();
        lbAugmentedBuild = new javax.swing.JLabel();
        cbIncludeOpenProjects = new javax.swing.JCheckBox();
        lbIncludeOpenProjects = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lbTrustLevel = new javax.swing.JLabel();
        cbTrustLevel = new javax.swing.JComboBox<>();
        lbTrustTerms = new javax.swing.JLabel();
        lbReadOnly = new javax.swing.JLabel();

        lbRuntime.setLabelFor(cbRuntime);
        org.openide.awt.Mnemonics.setLocalizedText(lbRuntime, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbRuntime.text")); // NOI18N

        cbRuntime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRuntimeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btManageRuntimes, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.btManageRuntimes.text")); // NOI18N
        btManageRuntimes.setActionCommand(org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.btManageRuntimes.actionCommand")); // NOI18N
        btManageRuntimes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btManageRuntimesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbAugmentedBuild, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.cbAugmentedBuild.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbAugmentedBuild, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbAugmentedBuild.text")); // NOI18N
        lbAugmentedBuild.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(cbIncludeOpenProjects, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.cbIncludeOpenProjects.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbIncludeOpenProjects, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbIncludeOpenProjects.text")); // NOI18N
        lbIncludeOpenProjects.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(lbTrustLevel, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbTrustLevel.text")); // NOI18N

        cbTrustLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTrustLevelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lbTrustTerms, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbTrustTerms.text")); // NOI18N
        lbTrustTerms.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        lbReadOnly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/gradle/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbReadOnly, org.openide.util.NbBundle.getMessage(GradleExecutionPanel.class, "GradleExecutionPanel.lbReadOnly.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lbReadOnly))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbTrustLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbTrustLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(317, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lbIncludeOpenProjects, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbRuntime, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbRuntime, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btManageRuntimes)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbIncludeOpenProjects, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(lbAugmentedBuild, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addComponent(cbAugmentedBuild, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator1)
                            .addComponent(lbTrustTerms, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cbRuntime, cbTrustLevel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbRuntime)
                    .addComponent(btManageRuntimes)
                    .addComponent(cbRuntime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbAugmentedBuild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbAugmentedBuild, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbIncludeOpenProjects)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbIncludeOpenProjects, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbTrustLevel)
                    .addComponent(cbTrustLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbTrustTerms, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbReadOnly)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btManageRuntimesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btManageRuntimesActionPerformed
        if ((runtimeManager != null) && runtimeManager.manageRuntimesAction().isPresent()) {
            runtimeManager.manageRuntimesAction().get().run();
        }
    }//GEN-LAST:event_btManageRuntimesActionPerformed

    private void cbTrustLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTrustLevelActionPerformed
        setJavaSettingsEnabled(!readOnly && (cbTrustLevel.getSelectedItem() != TrustLevel.NONE));
    }//GEN-LAST:event_cbTrustLevelActionPerformed

    private void cbRuntimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRuntimeActionPerformed
        JavaRuntime rt = (JavaRuntime) cbRuntime.getSelectedItem();
        String fore = (rt != null) && rt.isBroken() ? "nb.errorForeground" : "ComboBox.foreground"; //NOI18N
        cbRuntime.setForeground(UIManager.getColor(fore));
    }//GEN-LAST:event_cbRuntimeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btManageRuntimes;
    private javax.swing.JCheckBox cbAugmentedBuild;
    private javax.swing.JCheckBox cbIncludeOpenProjects;
    private javax.swing.JComboBox<JavaRuntime> cbRuntime;
    private javax.swing.JComboBox<TrustLevel> cbTrustLevel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbAugmentedBuild;
    private javax.swing.JLabel lbIncludeOpenProjects;
    private javax.swing.JLabel lbReadOnly;
    private javax.swing.JLabel lbRuntime;
    private javax.swing.JLabel lbTrustLevel;
    private javax.swing.JLabel lbTrustTerms;
    // End of variables declaration//GEN-END:variables

    private void saveTrustLevel(Project project) {
        TrustLevel v = (TrustLevel)cbTrustLevel.getSelectedItem();
        if (v == null) {
            v = NONE;
        }
        switch (v) {
            case NONE:
                ProjectTrust.getDefault().distrustProject(project);
                break;

            case PERMANENT:
                ProjectTrust.getDefault().trustProject(project, true);
                break;

            case TEMPORARY:
                if (ProjectTrust.getDefault().isTrustedPermanently(project)) {
                    ProjectTrust.getDefault().distrustProject(project);
                }
                ProjectTrust.getDefault().trustProject(project, false);
                break;
        }
    }

    private void saveJavaRuntime(Project project) {
        RunUtils.setActiveRuntime(project, (JavaRuntime) cbRuntime.getSelectedItem());
    }

    private void saveCheckBox(JCheckBox check, String property) {
        GradleBaseProject gbp = project != null ? GradleBaseProject.get(project) : null;
        if ((gbp != null) && (gbp.getNetBeansProperty(property) == null)) {
            Preferences prefs = NbGradleProject.getPreferences(project, false);
            prefs.putBoolean(property, check.isSelected());
        }
    }

    void save() {
        if (project != null) {
            saveTrustLevel(project);
            saveJavaRuntime(project);

            saveCheckBox(cbAugmentedBuild, RunUtils.PROP_AUGMENTED_BUILD);
            saveCheckBox(cbIncludeOpenProjects, RunUtils.PROP_INCLUDE_OPEN_PROJECTS);
        }
    }

    private static class RuntimeRenderer extends JLabel implements ListCellRenderer, UIResource {

        @Override
        @NbBundle.Messages({
            "# {0} - runtimeId", 
            "LBL_MissingRuntime=Missing Runtime: {0}"
        })
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            setOpaque(true);
            if (value instanceof JavaRuntime) {
                JavaRuntime rt = (JavaRuntime)value;
                setText(rt.getDisplayName());
                if ( isSelected ) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                if (rt.isBroken()) {
                    setText(Bundle.LBL_MissingRuntime(value));
                }
            } else {
                if (value == null) {
                    setText("");
                }
            }
            return this;
        }
    }
}

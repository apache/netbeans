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

package org.netbeans.modules.gradle.configurations;

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.execute.GradleExecAccessor;
import org.netbeans.modules.gradle.execute.ProjectConfigurationUpdater;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  sdedic
 */
@NbBundle.Messages({
    "# {0} - configuration name",
    "CONFIRM_RevertConfigurationToDefault=Revert configuration {0} settings to default ?"
})
@SuppressWarnings("serial")
public class ConfigurationsPanel extends javax.swing.JPanel implements HelpCtx.Provider {
    private final Project project;
    private final ProjectConfigurationUpdater updater;
    private GradleExecConfiguration activeConfig;
    private ConfigurationSnapshot handle;

    public void setActiveConfig(GradleExecConfiguration activeConfig) {
        this.activeConfig = activeConfig;
        handle.setActiveConfiguration(activeConfig);
    }

    @NbBundle.Messages({
        "CONF_DisplayNameAndId={0} ({1})",
        "CONF_DisplayNameOnly={0}"
    })
    ConfigurationsPanel(ProjectConfigurationUpdater updater, ConfigurationSnapshot handle,  Project project) {
        initComponents();
        this.handle = handle;
        this.project = project;
        this.updater = updater;
        
        activeConfig = handle.getActiveConfiguration();

        initListUI();
        checkButtonEnablement();
        lstConfigurations.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component supers = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                GradleExecConfiguration conf = (GradleExecConfiguration)value;
                if (supers instanceof JLabel) {
                    JLabel jl = (JLabel)supers;
                    String dn = conf.getName();
                    if (!conf.isDefault()) {
                        if (dn == null || dn.equals(conf.getId())) {
                            dn = Bundle.CONF_DisplayNameOnly(conf.getId());
                        } else {
                            dn = Bundle.CONF_DisplayNameAndId(dn, conf.getId());
                        }
                    }
                    jl.setText(dn);
                }
                if (conf == activeConfig) {
                    supers.setFont(supers.getFont().deriveFont(Font.BOLD));
                }
                return supers;
            }
        });
        
        lstConfigurations.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkButtonEnablement();
            }

        });
        createListModel();
    }

    private void checkButtonEnablement() {
        GradleExecConfiguration conf = (GradleExecConfiguration) lstConfigurations.getSelectedValue();
        btnEdit.setEnabled(conf != null);
        boolean canRemove = conf != null && !handle.isFixed(conf);
        btnRemove.setEnabled(canRemove);
        btnRevert.setEnabled(!canRemove && handle.isOverriden(conf));
        btnActivate.setEnabled(conf != handle.getActiveConfiguration());
        btnClone.setEnabled(conf != null);
    }

    private void createListModel() {
        DefaultListModel<GradleExecConfiguration> model = new DefaultListModel<>();
        for (GradleExecConfiguration c : handle.getConfigurations()) {
            model.addElement(c);
        }
        lstConfigurations.setModel(model);
        lstConfigurations.setSelectedValue(activeConfig, true);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblConfigurations = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstConfigurations = new javax.swing.JList();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnActivate = new javax.swing.JButton();
        btnClone = new javax.swing.JButton();
        btnRevert = new javax.swing.JButton();

        lblConfigurations.setLabelFor(lstConfigurations);
        org.openide.awt.Mnemonics.setLocalizedText(lblConfigurations, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.lblConfigurations.text")); // NOI18N

        lstConfigurations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lstConfigurations);
        lstConfigurations.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.lstConfigurations.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnAdd.text")); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnEdit, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnEdit.text")); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnRemove.text")); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnActivate, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnActivate.text")); // NOI18N
        btnActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActivateActionPerformed(evt);
            }
        });

        btnClone.setText(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnClone.text")); // NOI18N
        btnClone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloneActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnRevert, org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnRevert.text")); // NOI18N
        btnRevert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevertActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblConfigurations)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnActivate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnEdit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRevert, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnActivate, btnAdd, btnClone, btnEdit, btnRemove});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblConfigurations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnActivate)
                        .addGap(18, 18, 18)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRevert)
                        .addGap(0, 13, Short.MAX_VALUE))))
        );

        lblConfigurations.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.lblConfigurations.AccessibleContext.accessibleDescription")); // NOI18N
        btnAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnAdd.AccessibleContext.accessibleDescription")); // NOI18N
        btnEdit.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnEdit.AccessibleContext.accessibleDescription")); // NOI18N
        btnRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnRemove.AccessibleContext.accessibleDescription")); // NOI18N
        btnActivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsPanel.class, "ConfigurationsPanel.btnActivate.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
    GradleExecConfiguration cfg = handle.createNew(null);
    makeNewConfiguration(cfg, Bundle.TIT_Add_Config(), Bundle.ACSD_Add_Config());
}//GEN-LAST:event_btnAddActionPerformed

@NbBundle.Messages({
    "TIT_Add_Config=Add a new Configuration",
    "ACSD_Add_Config=Add a new Configuration",

    "TIT_Clone_Config=Add a new Configuration",
    "ACSD_Clone_Config=Add a new Configuration",
    
    "# {0} - original ID",
    "FMT_ConfigurationCloneId={0}_clone",
    "CONF_CloneOfDefault=default_clone"
})
private void makeNewConfiguration(GradleExecConfiguration cfg, String title, String accDescr) {
    NewConfigurationPanel pnl = new NewConfigurationPanel(project, () -> handle.getConfigurations(), true);
    pnl.setShared(false);
    pnl.setConfigurationId(cfg.getId());
    pnl.setDisplayName(cfg.getName());
    pnl.setProperties(cfg.getProjectProperties());
    pnl.setParameters(cfg.getCommandLineArgs());
    
    pnl.getAccessibleContext().setAccessibleDescription(accDescr);
    DialogDescriptor dd = new DialogDescriptor(pnl, title);

    NotificationLineSupport supp = dd.createNotificationLineSupport();
    pnl.setNotifications(supp);
    pnl.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("panelValid".equals(evt.getPropertyName())) { // NOI18N
                dd.setValid(pnl.isPanelValid());
            }
        }
    });

    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (ret != DialogDescriptor.OK_OPTION) {
        return;
    }
    cfg = handle.createNew(pnl.getConfigurationId());
    handle.updateConfiguration(cfg, 
            pnl.getLabel(),
            pnl.getProperties(),
            pnl.getParameters()
    );
    handle.add(cfg);
    handle.setShared(cfg, pnl.isShared());
    createListModel();
    lstConfigurations.setSelectedValue(cfg, true);
}

private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
    GradleExecConfiguration cfg = (GradleExecConfiguration)lstConfigurations.getSelectedValue();
    if (cfg == null) {
        return;
    }
    NewConfigurationPanel pnl = new NewConfigurationPanel(project, () -> handle.getConfigurations(), false);
    pnl.setShared(handle.isShared(cfg));
    pnl.setConfigurationId(cfg.getId());
    pnl.setDisplayName(cfg.getName());
    pnl.setProperties(cfg.getProjectProperties());
    pnl.setParameters(cfg.getCommandLineArgs());
    
    pnl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ConfigurationsPanel.class, "ACSD_Add_Config"));
    DialogDescriptor dd = new DialogDescriptor(pnl, NbBundle.getMessage(ConfigurationsPanel.class, "TIT_Add_Config"));

    NotificationLineSupport supp = dd.createNotificationLineSupport();
    pnl.setNotifications(supp);
    pnl.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("panelValid".equals(evt.getPropertyName())) { // NOI18N
                dd.setValid(pnl.isPanelValid());
            }
        }
    });

    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (ret != DialogDescriptor.OK_OPTION) {
        return;
    }
    handle.updateConfiguration(cfg, 
            pnl.getLabel(),
            pnl.getProperties(),
            pnl.getParameters()
    );
    handle.setShared(cfg, pnl.isShared());
    createListModel();
    lstConfigurations.setSelectedValue(cfg, true);
}//GEN-LAST:event_btnEditActionPerformed

private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
    GradleExecConfiguration sel = (GradleExecConfiguration)lstConfigurations.getSelectedValue();
    if (sel == null) {
        return;
    }
    handle.removeConfiguration(sel);
    if (sel == activeConfig) {
        activeConfig = handle.getConfigurations().stream().filter(GradleExecConfiguration::isDefault).
                findAny().orElse(null);
        handle.setActiveConfiguration(activeConfig);
    }
    createListModel();
    checkButtonEnablement();
    lstConfigurations.repaint();
}//GEN-LAST:event_btnRemoveActionPerformed

private void btnActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActivateActionPerformed
    GradleExecConfiguration sel = (GradleExecConfiguration)lstConfigurations.getSelectedValue();
    if (sel == null) {
        return;
    }
    activeConfig = sel;
    handle.setActiveConfiguration(activeConfig);
    checkButtonEnablement();
    lstConfigurations.repaint();
    
}//GEN-LAST:event_btnActivateActionPerformed

    private void btnCloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloneActionPerformed
        GradleExecConfiguration sel = (GradleExecConfiguration)lstConfigurations.getSelectedValue();
        if (sel == null) {
            return;
        }
        String id = sel.isDefault() ? Bundle.CONF_CloneOfDefault() : Bundle.FMT_ConfigurationCloneId(sel.getId());
        GradleExecConfiguration cfg = GradleExecAccessor.instance().copy(sel, id);
        makeNewConfiguration(cfg, Bundle.TIT_Clone_Config(), Bundle.ACSD_Clone_Config());
    }//GEN-LAST:event_btnCloneActionPerformed

    private void btnRevertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevertActionPerformed
        GradleExecConfiguration sel = (GradleExecConfiguration)lstConfigurations.getSelectedValue();
        if (sel == null) {
            return;
        }
        NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(Bundle.CONFIRM_RevertConfigurationToDefault(sel.getDisplayName()));
        if (DialogDisplayer.getDefault().notify(confirm) != NotifyDescriptor.YES_OPTION) {
            return;
        }
        handle.revert(sel);
        createListModel();
        lstConfigurations.setSelectedValue(sel, true);
    }//GEN-LAST:event_btnRevertActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActivate;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClone;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRevert;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblConfigurations;
    private javax.swing.JList lstConfigurations;
    // End of variables declaration//GEN-END:variables

    private void initListUI() {
        lstConfigurations.setEnabled(true);
        btnActivate.setEnabled(true);
        btnAdd.setEnabled(true);
        btnEdit.setEnabled(true);
        btnClone.setEnabled(true);
        btnRemove.setEnabled(true);
    }
    // End of variables declaration

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}

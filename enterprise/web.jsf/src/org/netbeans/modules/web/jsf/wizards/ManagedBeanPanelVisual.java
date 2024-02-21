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

package org.netbeans.modules.web.jsf.wizards;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.wizards.ManagedBeanIterator.NamedScope;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;


@SuppressWarnings("serial") // not used to be serialized
public class ManagedBeanPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider {

    private final DefaultComboBoxModel scopeModel = new DefaultComboBoxModel();
    private boolean isCDIEnabled = false;
    /**
     * Creates new form PropertiesPanelVisual
     */
    public ManagedBeanPanelVisual(Project proj) {
        initComponents();
        setVisibleBeanDescription(false);
        boolean addToFacesConfig = false;

        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        Profile profile = null;
        if (wm != null){
            String[] configFiles = JSFConfigUtilities.getConfigFiles(wm);
            if (configFiles.length > 0){
                FileObject documentBase = wm.getDocumentBase();
                ArrayList<String> files = new ArrayList<String>();
                for (int i = 0; i < configFiles.length; i++){
                    if (documentBase.getFileObject(configFiles[i]) != null)
                        files.add(configFiles[i]);
                }
                configFiles = (String[])files.toArray(new String[0]);
            }
            jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
            //No config files found
            if (configFiles.length==0) {
                addToConfigCheckBox.setEnabled(false);
                jComboBoxConfigFile.setEnabled(false);
            } else {
                profile = wm.getJ2eeProfile();
                if (profile != null && !profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                    addToFacesConfig = true;
                    addToConfigCheckBox.setSelected(true);
                    setVisibleBeanDescription(true);
                    addToConfigCheckBox.setEnabled(false);
                }
            }
        }
        Object[] scopes;
        if(profile != null && (profile.isAtLeast(Profile.JAKARTA_EE_9_WEB))){
            org.netbeans.modules.jakarta.web.beans.CdiUtil cdiUtil = proj.getLookup().lookup(org.netbeans.modules.jakarta.web.beans.CdiUtil.class);
            isCDIEnabled = cdiUtil != null && cdiUtil.isCdiEnabled();
        } else {
            org.netbeans.modules.web.beans.CdiUtil cdiUtil = proj.getLookup().lookup(org.netbeans.modules.web.beans.CdiUtil.class);
            isCDIEnabled = cdiUtil != null && cdiUtil.isCdiEnabled();
        }
        if (isCDIEnabled && !addToFacesConfig) {
            scopes = ManagedBeanIterator.NamedScope.values();
        } else {
            scopes = ManagedBean.Scope.values();
        }

        for (Object scope : scopes) {
            scopeModel.addElement(scope);
        }

        jTextFieldName.setText("newJSFManagedBean");
        jTextFieldName.getDocument().addDocumentListener(new PanelDocumentListener());
        jComboBoxScope.addActionListener(new PanelActionListener());

//        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FormBeanNewPanelVisual.class, "ACS_BeanFormProperties"));  // NOI18N
    }

    private void updateScopeModel(boolean addToConfig) {
        if (isCDIEnabled && addToConfig) {
            scopeModel.removeAllElements();
            for (ManagedBean.Scope scope : ManagedBean.Scope.values()) {
                scopeModel.addElement(scope);
            }
        } else if (isCDIEnabled && !addToConfig) {
            scopeModel.removeAllElements();
            for (ManagedBeanIterator.NamedScope scope : ManagedBeanIterator.NamedScope.values()) {
                scopeModel.addElement(scope);
            }
        } else {
            return;
        }
        jComboBoxScope.setModel(scopeModel);
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelConfigFile = new javax.swing.JLabel();
        jComboBoxConfigFile = new javax.swing.JComboBox();
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelScope = new javax.swing.JLabel();
        jComboBoxScope = new javax.swing.JComboBox();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();
        addToConfigCheckBox = new javax.swing.JCheckBox();

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_ConfigFile").charAt(0));
        jLabelConfigFile.setLabelFor(jComboBoxConfigFile);
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_ConfigFile")); // NOI18N

        jComboBoxConfigFile.setEnabled(addToConfigCheckBox.isSelected());
        jComboBoxConfigFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxConfigFileActionPerformed(evt);
            }
        });

        jLabelName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Name").charAt(0));
        jLabelName.setLabelFor(jTextFieldName);
        jLabelName.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Name")); // NOI18N

        jLabelScope.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_Scope").charAt(0));
        jLabelScope.setLabelFor(jComboBoxScope);
        jLabelScope.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Scope")); // NOI18N

        jComboBoxScope.setModel(scopeModel);

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_BeanDescription")); // NOI18N

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BeanDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addToConfigCheckBox, org.openide.util.NbBundle.getMessage(ManagedBeanPanelVisual.class, "LBL_Add_data_to_conf_file")); // NOI18N
        addToConfigCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                addToConfigCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelName)
                    .addComponent(jLabelScope)
                    .addComponent(jLabelDesc)
                    .addComponent(jLabelConfigFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(jComboBoxScope, 0, 302, Short.MAX_VALUE)
                    .addComponent(jScrollPaneDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .addComponent(jComboBoxConfigFile, 0, 302, Short.MAX_VALUE)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(addToConfigCheckBox)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addToConfigCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelConfigFile)
                    .addComponent(jComboBoxConfigFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelScope))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelDesc)
                    .addComponent(jScrollPaneDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)))
        );

        jComboBoxConfigFile.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ConfigurationFile")); // NOI18N
        jComboBoxScope.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ManagedBeanScope")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxConfigFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxConfigFileActionPerformed
        fireChange();
    }//GEN-LAST:event_jComboBoxConfigFileActionPerformed

    private void addToConfigCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_addToConfigCheckBoxItemStateChanged
        boolean addToConfig = isAddBeanToConfig();
        jComboBoxConfigFile.setEnabled(addToConfig);
        updateScopeModel(addToConfig);
        setVisibleBeanDescription(addToConfig);
    }//GEN-LAST:event_addToConfigCheckBoxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addToConfigCheckBox;
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxScope;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelScope;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables

    @Messages({
        "ManagedBeanPanelVisual.warn.flowScoped.low.version=FlowScoped bean can be used only in projects with JSF2.2+"
    })
    boolean valid(WizardDescriptor wizardDescriptor) {
        String configFile = (String) jComboBoxConfigFile.getSelectedItem();

        Project project = Templates.getProject(wizardDescriptor);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        SourceGroup[] sources = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sources.length == 0) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_No_Sources_found")); //NOI18N
            return false;
        }

        if (configFile == null) {
            if (!Utilities.isJavaEE6Plus((TemplateWizard) wizardDescriptor) && !isAddBeanToConfig()
                    && !(JSFUtils.isJavaEE5((TemplateWizard) wizardDescriptor) && JSFUtils.isJSF20Plus(wm, true))) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_NoConfigFile")); //NOI18N
                return false;
            }
            return true;
        }

        FileObject dir = wm.getDocumentBase();
        FileObject fo = dir.getFileObject(configFile);
        FacesConfig facesConfig = ConfigurationUtils.getConfigModel(fo, true).getRootComponent();
        if (facesConfig == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_InvalidConfigFile")); //NOI18N
            return false;
        }

        String name = jTextFieldName.getText();
        if (name.trim().equals("")) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_InvalidBeanName")); //NOI18N
            return false;
        }

        Object scope = jComboBoxScope.getSelectedItem();
        if (scope instanceof NamedScope && scope == NamedScope.FLOW) {
            JsfVersion jsfVersion = JsfVersionUtils.forWebModule(wm);
            if (jsfVersion != null && !jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        Bundle.ManagedBeanPanelVisual_warn_flowScoped_low_version());
                return false;
            }
        }

        /* XXX not ready yet, more interactions need to be considered before finalized.
        Collection<ManagedBean> beans = facesConfig.getManagedBeans();
        for (ManagedBean managedBean : beans) {
            if (name.equals(managedBean.getManagedBeanName())) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanelVisual.class, "MSG_ExistBeanName"));
                return false;
            }
        }
        */

        return true;
    }

    void read(WizardDescriptor settings) {
    }

    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.CONFIG_FILE, jComboBoxConfigFile.getSelectedItem());
        settings.putProperty(WizardProperties.NAME, jTextFieldName.getText());
        settings.putProperty(WizardProperties.SCOPE, jComboBoxScope.getSelectedItem());
        settings.putProperty(WizardProperties.DESCRIPTION, jTextAreaDesc.getText());
    }

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.ManagedBeanPanelVisual");
    }

    private final Set<ChangeListener> listeners = new HashSet<>(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    public final void setVisibleBeanDescription(boolean visible) {
        jLabelDesc.setVisible(visible);
        jScrollPaneDesc.setVisible(visible);
        jTextAreaDesc.setVisible(visible);
        repaint();
    }

    public void setManagedBeanName(String name) {
        jTextFieldName.setText(name);
    }

    public String getManagedBeanName() {
        return jTextFieldName.getText();
    }

    public boolean isAddBeanToConfig() {
        return addToConfigCheckBox.isSelected();
    }

    private class PanelActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fireChange();
        }
    }

    private class PanelDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            fireChange();
        }
    }
}

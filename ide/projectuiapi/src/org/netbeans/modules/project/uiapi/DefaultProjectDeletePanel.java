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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation.InvalidablePanel;

/**
 * @author Jan Lahoda
 */
final class DefaultProjectDeletePanel extends javax.swing.JPanel implements InvalidablePanel {

    private String projectDisplaName;
    private String projectFolder;
    private boolean enableCheckbox;
    private ProgressHandle handle;
    private JComponent progressComponent;
    private ProgressBar progressBar;
    
    public DefaultProjectDeletePanel(ProgressHandle handle, String projectDisplaName, String projectFolder, boolean enableCheckbox) {
        this.projectDisplaName = projectDisplaName;
        this.projectFolder = projectFolder;
        this.enableCheckbox = enableCheckbox;
        this.handle = handle;
        initComponents();
        
        if (Boolean.getBoolean("org.netbeans.modules.project.uiapi.DefaultProjectOperations.showProgress")) {
            ((CardLayout) progress.getLayout()).show(progress, "progress");
        }
    }
    
    private static class ProgressBar extends JPanel {

        private JLabel label;

        private static ProgressBar create(JComponent progress) {
            ProgressBar instance = new ProgressBar();
            instance.setLayout(new BorderLayout());
            instance.label = new JLabel(" "); //NOI18N
            instance.label.setBorder(new EmptyBorder(0, 0, 2, 0));
            instance.add(instance.label, BorderLayout.NORTH);
            instance.add(progress, BorderLayout.CENTER);
            return instance;
        }

        public void setString(String value) {
            label.setText(value);
        }

        private ProgressBar() {
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        warningText = new javax.swing.JTextArea();
        deleteSourcesCheckBox = new javax.swing.JCheckBox();
        progress = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        progressImpl = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        questionLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        warningText.setEditable(false);
        warningText.setFont(javax.swing.UIManager.getFont("Label.font"));
        warningText.setLineWrap(true);
        warningText.setText(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "LBL_Pre_Delete_Warning", new Object[] {projectDisplaName})); // NOI18N
        warningText.setWrapStyleWord(true);
        warningText.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
        warningText.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(warningText, gridBagConstraints);
        warningText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "ASCN_Pre_Delete_Warning", new Object[] {})); // NOI18N
        warningText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "ACSD_Pre_Delete_Warning", new Object[] {projectDisplaName})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deleteSourcesCheckBox, org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "LBL_Delete_Also_Sources", new Object[] {projectFolder})); // NOI18N
        deleteSourcesCheckBox.setEnabled(enableCheckbox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(deleteSourcesCheckBox, gridBagConstraints);
        deleteSourcesCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "ACSN_Delete_Also_Sources", new Object[] {projectFolder})); // NOI18N
        deleteSourcesCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "ACSD_Delete_Also_Sources", new Object[] {})); // NOI18N

        progress.setLayout(new java.awt.CardLayout());
        progress.add(jPanel4, "not-progress");

        progressImpl.add(progressComponent = ProgressHandleFactory.createProgressComponent(handle));
        progressImpl.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "LBL_Deleting_Project", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        progressImpl.add(jLabel5, gridBagConstraints);

        progress.add(progressImpl, "progress");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(progress, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(questionLabel, org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "LBL_Pre_Delete_Question")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        add(questionLabel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DefaultProjectDeletePanel.class, "ACSD_Delete_Panel", new Object[] {})); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deleteSourcesCheckBox;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel progress;
    private javax.swing.JPanel progressImpl;
    private javax.swing.JLabel questionLabel;
    private javax.swing.JTextArea warningText;
    // End of variables declaration//GEN-END:variables
    
    public boolean isDeleteSources() {
        return deleteSourcesCheckBox.isSelected();
    }

    void setDeleteSources(boolean value) {
        deleteSourcesCheckBox.setSelected(value);
    }

    public @Override void addChangeListener(ChangeListener l) {}

    public @Override void removeChangeListener(ChangeListener l) {}

    public @Override void showProgress() {
        deleteSourcesCheckBox.setEnabled(false);
        ((CardLayout) progress.getLayout()).last(progress);
    }

    public @Override boolean isPanelValid() {
        return true;
    }
    
    protected void addProgressBar() {
        progressBar = ProgressBar.create(progressComponent); //NOI18N
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        progressImpl.add(progressBar, gridBagConstraints);
    }
    
    protected void removeProgressBar() {
        progressImpl.remove(progressBar);
    }

}

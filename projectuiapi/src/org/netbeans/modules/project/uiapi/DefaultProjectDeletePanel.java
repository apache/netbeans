/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

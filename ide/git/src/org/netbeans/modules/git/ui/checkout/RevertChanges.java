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

package org.netbeans.modules.git.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class RevertChanges implements ActionListener {
    private RevertChangesPanel panel;
    private final File[] roots;

    RevertChanges (File[] roots) {
        this.roots = roots;
        panel = new RevertChangesPanel();
        loadSettings();
        
        panel.revertAllRadioButton.addActionListener(this);
        panel.revertWTRadioButton.addActionListener(this);
        panel.revertIndexRadioButton.addActionListener(this);
        
        enableFields();
    }

    private void loadSettings() {
        GitModuleConfig config = GitModuleConfig.getDefault();
        panel.revertWTRadioButton.setSelected(config.getRevertWT());        
        panel.removeWTNewCheckBox.setSelected(config.getRemoveWTNew());        
        panel.revertAllRadioButton.setSelected(config.getRevertAll());
        panel.removeAllNewCheckBox.setSelected(config.getRemoveAllNew());
        panel.revertIndexRadioButton.setSelected(config.getRevertIndex());
    }
    
    void storeSettings() {
        GitModuleConfig config = GitModuleConfig.getDefault();
        config.putRevertAll(panel.revertAllRadioButton.isSelected());
        config.putRevertIndex(panel.revertIndexRadioButton.isSelected());
        config.putRevertWT(panel.revertWTRadioButton.isSelected());        
        config.putRemoveAllNew(panel.removeAllNewCheckBox.isSelected());        
        config.putRemoveWTNew(panel.removeWTNewCheckBox.isSelected());        
    }
    
    boolean show() {        
        JButton okButton = new JButton(NbBundle.getMessage(RevertChanges.class, "LBL_RevertChanges.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        String label;
        if (roots.length != 1) {
            label = NbBundle.getMessage(RevertChanges.class, "CTL_RevertChanges.title.files", roots.length); //NOI18N
        } else if (Git.getInstance().getFileStatusCache().getStatus(roots[0]).isDirectory()) {
            label = NbBundle.getMessage(RevertChanges.class, "CTL_RevertChanges.title.dir", roots[0].getName()); //NOI18N
        } else {
            label = NbBundle.getMessage(RevertChanges.class, "CTL_RevertChanges.title.file", roots[0].getName()); //NOI18N
        }
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(RevertChanges.class, "CTL_RevertChanges.title", label), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RevertChanges.class), null);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }

    boolean isRevertWT() {
        return panel.revertWTRadioButton.isSelected();
    }
    
    boolean isRevertAll() {
        return panel.revertAllRadioButton.isSelected();
    }
    
    boolean isRevertIndex() {
        return panel.revertIndexRadioButton.isSelected();
    }        
    
    boolean isRemove() {
        return panel.removeAllNewCheckBox.isEnabled() && panel.removeAllNewCheckBox.isSelected() || 
               panel.removeWTNewCheckBox.isEnabled() && panel.removeWTNewCheckBox.isSelected();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.revertAllRadioButton ||
           e.getSource() == panel.revertWTRadioButton  ||
           e.getSource() == panel.revertIndexRadioButton)
        {
            enableFields();
        }
    }
    
    private void enableFields() {        
        panel.removeAllNewCheckBox.setEnabled(panel.revertAllRadioButton.isSelected()); 
        panel.removeWTNewCheckBox.setEnabled(panel.revertWTRadioButton.isSelected()); 
    }
    
}

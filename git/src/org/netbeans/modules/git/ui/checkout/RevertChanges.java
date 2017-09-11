/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

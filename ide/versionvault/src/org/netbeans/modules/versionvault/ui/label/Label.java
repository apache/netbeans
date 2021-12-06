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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.label;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class Label implements ActionListener, DocumentListener {
    private final String contextTitle;
    private final File[] files;
    
    private LabelPanel panel;
    private JButton labelButton;
    
    Label(String contextDisplayName, File[] files) {
        this.contextTitle = contextDisplayName;
        this.files = files;
    }

    boolean show() {
        panel = new LabelPanel();        
        
        labelButton = new JButton(); 
        labelButton.setEnabled(false);
                
        panel.recurseCheckBox.setSelected(ClearcaseModuleConfig.getLabelRecurse());      
        panel.replaceCheckBox.setSelected(ClearcaseModuleConfig.getLabelReplace());      
        panel.followCheckBox.setSelected(ClearcaseModuleConfig.getLabelFollow());      
        
        panel.labelTextField.getDocument().addDocumentListener(this);
        panel.recentMessagesButton.addActionListener(this);
        panel.browseButton.addActionListener(this);
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(LabelAction.class, "CTL_LabelDialog_Title", contextTitle)); // NOI18N
        dd.setModal(true);        
        org.openide.awt.Mnemonics.setLocalizedText(labelButton, org.openide.util.NbBundle.getMessage(LabelAction.class, "CTL_LabelDialog_Label"));
        
        panel.putClientProperty("contentTitle", contextTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        
        dd.setOptions(new Object[] {labelButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx(LabelAction.class));
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "label.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Label.class, "ACSD_LabelDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);                
                
        Object value = dd.getValue();
        if (value != labelButton) {
            return false;
        }  else {        
            ClearcaseModuleConfig.setLabelFollow(getFollow());
            ClearcaseModuleConfig.setLabelRecurse(getRecurse());
            ClearcaseModuleConfig.setLabelReplace(getReplace());            
            Utils.insert(ClearcaseModuleConfig.getPreferences(), LabelAction.RECENT_LABEL_MESSAGES, getComment().trim(), 20);
            
            return true;
        }               
    }

    String getLabel() {
        return panel.labelTextField.getText();
    }
    
    boolean getReplace() {
        return panel.replaceCheckBox.isSelected();
    }
    
    boolean getRecurse() {
        return panel.recurseCheckBox.isSelected();
    }
        
    boolean getFollow() {
        return panel.followCheckBox.isSelected();
    }
    
    String getVersion() {
        return panel.versionTextField.getText();
    }
    
    String getComment() {
        return panel.commentTextArea.getText();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.recentMessagesButton) {
            onBrowseRecentMessages();
        } else if(e.getSource() == panel.browseButton) {
            onBrowseLabels();
        }
    }

    private void onBrowseLabels() {
        String label = LabelSelector.select(NbBundle.getMessage(Label.class, "CTL_LabelsListForm_RecentTitle"), 
                                            NbBundle.getMessage(Label.class, "CTL_LabelsListForm_RecentPrompt"),
                                            files);
        if (label != null) {
            panel.labelTextField.setText(label);
        }
    }
    
    private void onBrowseRecentMessages() {
        String message = StringSelector.select(NbBundle.getMessage(Label.class, "CTL_RecentLabelCommentForm_RecentTitle"), 
                                               NbBundle.getMessage(Label.class, "CTL_RecentLabelCommentForm_RecentPrompt"), 
                                               Utils.getStringList(ClearcaseModuleConfig.getPreferences(), LabelAction.RECENT_LABEL_MESSAGES));
        if (message != null) {
            panel.commentTextArea.replaceSelection(message);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        validate();
    }

    public void removeUpdate(DocumentEvent e) {
        validate();        
    }

    public void changedUpdate(DocumentEvent e) {
        validate();        
    }
    
    private void validate() {
        boolean enabled = true;
        String labelText = panel.labelTextField.getText();
        if(labelText == null || labelText.trim().equals("")) {
            enabled = false;
        }                
        labelButton.setEnabled(enabled);
    }    
}

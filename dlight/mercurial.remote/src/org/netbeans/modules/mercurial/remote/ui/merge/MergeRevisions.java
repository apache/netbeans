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
package org.netbeans.modules.mercurial.remote.ui.merge;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class MergeRevisions implements PropertyChangeListener {

    private final MergeRevisionsPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    
    public MergeRevisions(VCSFileProxy repository, VCSFileProxy [] roots) {
        panel = new MergeRevisionsPanel(repository, roots);
         okButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                     "CTL_MergeForm_Action_Merge")); // NOI18N
         okButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Merge")); // NOI18N
         cancelButton = new JButton(NbBundle.getMessage(MergeRevisions.class, 
                 "CTL_MergeForm_Action_Cancel")); // NOI18N
         cancelButton.getAccessibleContext().setAccessibleDescription(
                 NbBundle.getMessage(MergeRevisions.class, 
                 "ACSD_MergeForm_Action_Cancel")); // NOI18N
        okButton.setEnabled(false);
        panel.addPropertyChangeListener(this);
        panel.loadRevisions();
    } 
    
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, 
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N

        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(MergeRevisions.class, "ASCD_MERGE_DIALOG")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (okButton != null && MergeRevisionsPanel.PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            okButton.setEnabled(valid);
        }       
    }

    public String getSelectionRevision() {
        if (panel == null) {
            return null;
        }
        return panel.getSelectedRevisionCSetId();
    }
}

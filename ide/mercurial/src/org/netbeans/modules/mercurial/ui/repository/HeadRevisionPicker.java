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
package org.netbeans.modules.mercurial.ui.repository;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;

public final class HeadRevisionPicker implements PropertyChangeListener {

    private final RevisionsPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    
    @NbBundle.Messages({
        "ACSD_HeadRevisionPicker_Cancel=Cancel",
        "CTL_HeadRevisionPicker_Cancel=Cancel",
        "ACSD_HeadRevisionPicker_Select=Select",
        "CTL_HeadRevisionPicker_Select=Select"
    })
    public HeadRevisionPicker(File repository, File [] roots) {
        panel = new RevisionsPanel(repository, roots);
        okButton = new JButton(Bundle.CTL_HeadRevisionPicker_Select());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_HeadRevisionPicker_Select());
        cancelButton = new JButton(Bundle.CTL_HeadRevisionPicker_Cancel());
        cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_HeadRevisionPicker_Cancel());
        okButton.setEnabled(false);
        panel.addPropertyChangeListener(this);
        panel.loadRevisions();
    } 
    
    @NbBundle.Messages("ASCD_HeadRevisionPicker.dialog=Select Revision")
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, Bundle.ASCD_HeadRevisionPicker_dialog());

        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.ASCD_HeadRevisionPicker_dialog());
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (okButton != null && RevisionsPanel.PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            okButton.setEnabled(valid);
        }       
    }

    public HgLogMessage getSelectionRevision() {
        if (panel == null) return null;
        return panel.getSelectedRevision();
    }
    
    public static class RevisionsPanel extends ChangesetPickerPanel {

        public RevisionsPanel (File repo,File [] roots) {
            super(repo, roots);
            initComponents();
            setInitMessageInfoFetcher(new HeadsInfoFetcher());
        }

        @Override
        @NbBundle.Messages("MSG_Refreshing_Update_Versions=Getting revisions...")
        protected String getRefreshLabel() {
            return Bundle.MSG_Refreshing_Update_Versions();
        }

        @Override
        protected boolean acceptSelection (HgLogMessage rev) {
            return rev != null && rev.getMessage() != null; // skip predefined revisions
        }

        @Override
        protected void loadRevisions () {
            super.loadRevisions();
        }

        private void initComponents() {
            jLabel1.setVisible(false);
            jLabel2.setVisible(false);
        }

        private static final class HeadsInfoFetcher extends ChangesetPickerPanel.MessageInfoFetcher {
            @Override
            protected HgLogMessage[] getMessageInfo(File repository, Set<File> setRoots, int fetchRevisionLimit, OutputLogger logger) {
                HgLogMessage[] messages;
                try {
                    messages = HgCommand.getHeadRevisionsInfo(repository, false, logger);
                } catch (HgException ex) {
                    Logger.getLogger(HeadRevisionPicker.class.getName()).log(Level.INFO, null, ex);
                    messages = new HgLogMessage[0];
                }
                return messages;
            }
        }
    }
}

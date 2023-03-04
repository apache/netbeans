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

package org.netbeans.modules.subversion.ui.update;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JButton;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author ondra
 */
public class UpdateTo implements PropertyChangeListener {

    private UpdateToPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private final RepositoryPaths revisionPath;

    /** Creates a new instance of UpdateTo */
    public UpdateTo(RepositoryFile repositoryFile, boolean localChanges) {
        revisionPath = new RepositoryPaths(repositoryFile, null, null, getPanel().revisionTextField, getPanel().revisionSearchButton);
        revisionPath.setupBehavior(null, 0, null, SvnSearch.SEARCH_HELP_ID_UPDATE);
        revisionPath.addPropertyChangeListener(this);
        getPanel().warningLabel.setVisible(localChanges);
        okButton = new JButton(org.openide.util.NbBundle.getMessage(UpdateTo.class, "CTL_UpdateToForm_Action_Update")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateTo.class, "ACSD_UpdateToForm_Action_Update")); // NOI18N
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(UpdateTo.class, "CTL_UpdateToForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateTo.class, "ACSD_UpdateToForm_Action_Cancel")); // NOI18N
    }

    private UpdateToPanel getPanel() {
        if(panel == null) {
            panel = new UpdateToPanel();
        }
        return panel;
    }

    public SVNRevision getSelectedRevision () {
        try {
            return revisionPath.getRepositoryFiles()[0].getRevision();
        } catch (NumberFormatException ex) {
            // should be already checked and
            // not happen at this place anymore
            Subversion.LOG.log(Level.INFO, null, ex);
        } catch (MalformedURLException ex) {
            // should be already checked and
            // not happen at this place anymore
            Subversion.LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(UpdateTo.class, "CTL_UpdateToDialog")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});

        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateTo.class, "ACSD_UpdateToDialog")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            if(okButton != null) {
                boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
                okButton.setEnabled(valid);
            }
        }
    }

}

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

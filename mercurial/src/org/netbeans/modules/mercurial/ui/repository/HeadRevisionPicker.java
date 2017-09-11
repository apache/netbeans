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

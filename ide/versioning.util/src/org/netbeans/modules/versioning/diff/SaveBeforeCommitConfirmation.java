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

package org.netbeans.modules.versioning.diff;

import java.awt.Dimension;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.cookies.SaveCookie;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import static org.openide.NotifyDescriptor.OK_CANCEL_OPTION;
import static org.openide.NotifyDescriptor.WARNING_MESSAGE;

/**
 *
 * @author Marian Petras
 */
public class SaveBeforeCommitConfirmation extends FilesModifiedConfirmation {

    private String saveAllAndCommitText;
    private String commitText;

    public static boolean allSaved(SaveCookie[] saveCookies) {
        SaveBeforeCommitConfirmation confirmation
                = new SaveBeforeCommitConfirmation(saveCookies);
        return confirmation.displayDialog() == confirmation.btnSaveAll;
    }

    private SaveBeforeCommitConfirmation(SaveCookie[] saveCookies) {
        super(saveCookies);
    }

    @Override
    protected JButton createSaveAllButton() {
        return new JButton() {
            private Dimension prefSize = null;
            @Override
            public Dimension getPreferredSize() {
                if (prefSize == null) {
                    Mnemonics.setLocalizedText(this, getLblCommit());
                    Dimension dim1 = super.getPreferredSize();
                    Mnemonics.setLocalizedText(this, getLblSaveAllAndCommit());
                    Dimension dim2 = super.getPreferredSize();
                    prefSize = new Dimension(Math.max(dim1.width, dim2.width),
                                             Math.max(dim1.height, dim2.height));
                }
                return prefSize;
            }
            @Override
            public void setUI(ButtonUI ui) {
                prefSize = null;
                super.setUI(ui);
            }
            @Override
            protected void setUI(ComponentUI newUI) {
                prefSize = null;
                super.setUI(newUI);
            }
        };
    }

    @Override
    protected String getInitialSaveAllButtonText() {
        return getLblSaveAllAndCommit();
    }

    private String getLblSaveAllAndCommit() {
        if (saveAllAndCommitText == null) {
            saveAllAndCommitText = getMessage("LBL_SaveAllCommit");     //NOI18N
        }
        return saveAllAndCommitText;
    }

    private String getLblCommit() {
        if (commitText == null) {
            commitText = getMessage("LBL_Commit");                      //NOI18N
        }
        return commitText;
    }

    @Override
    protected void savedLastFile() {
        Mnemonics.setLocalizedText(btnSaveAll, commitText);
    }

    @Override
    protected void handleSaveAllFailed(Collection<String> errMsgs) {
        if (confirmContinueCommit(errMsgs)) {
            closeDialog(btnSaveAll);
        }
    }

    private boolean confirmContinueCommit(Collection<String> errMsgs) {
        if ((errMsgs == null) || errMsgs.isEmpty()) {
            return true;
        }

        JButton btnShowMoreInfo = new JButton();
        JComponent info = new ExpandableMessage(
                "MSG_ExceptionWhileSavingMoreFiles_Intro",              //NOI18N
                errMsgs,
                "MSG_ExceptionWhileSavingMoreFiles_Question",           //NOI18N
                btnShowMoreInfo);
        String commitLbl = getMessage("LBL_ProceedWithCommit");         //NOI18N

        DialogDescriptor errDialog = new DialogDescriptor(
                        info,
                        getMessage("MSG_Title_SavingError"),   //title  //NOI18N
                        true,                                  //modal
                        OK_CANCEL_OPTION,
                        WARNING_MESSAGE,
                        null);                                 //button listener
        errDialog.setOptions(new Object[] {commitLbl, CANCEL_OPTION});
        errDialog.setAdditionalOptions(new Object[] {btnShowMoreInfo});
        errDialog.setValue(CANCEL_OPTION);             //default option
        errDialog.setClosingOptions(new Object[] {commitLbl, CANCEL_OPTION});
        Object value = DialogDisplayer.getDefault().notify(errDialog);
        return (value == commitLbl);
    }

}

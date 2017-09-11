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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

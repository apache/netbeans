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

import javax.swing.JButton;
import org.openide.awt.Mnemonics;
import org.openide.cookies.SaveCookie;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;

/**
 *
 * @author Marian Petras
 */
public class SaveBeforeClosingDiffConfirmation extends FilesModifiedConfirmation {

    private final JButton btnKeepModifications;

    public static boolean allSaved(SaveCookie[] saveCookies) {
        SaveBeforeClosingDiffConfirmation confirmation
                = new SaveBeforeClosingDiffConfirmation(saveCookies);
        Object selectedOption = confirmation.displayDialog();
        return (selectedOption == confirmation.btnSaveAll)
                || (selectedOption == confirmation.btnKeepModifications);
    }

    private SaveBeforeClosingDiffConfirmation(SaveCookie[] saveCookies) {
        super(saveCookies);
        btnKeepModifications = new JButton();
        Mnemonics.setLocalizedText(btnKeepModifications,
                                   getMessage("LBL_KeepModifications"));//NOI18N
    }

    @Override
    protected Object[] getDialogOptions() {
        return new Object[] { btnSave, btnSaveAll, btnKeepModifications };
    }

    @Override
    protected Object[] getDialogClosingOptions() {
        return new Object[] { btnKeepModifications, CANCEL_OPTION };
    }

}

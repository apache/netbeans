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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class PackagingAdditionalInfoPanel extends ListEditorPanel<String> {

    private final PackagingConfiguration packagingConfiguration;

    public PackagingAdditionalInfoPanel(List<String> infoList, PackagingConfiguration packagingConfiguration) {
        super(infoList);
        this.packagingConfiguration = packagingConfiguration;

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);
        getCopyButton().setVisible(false);
    }

    @Override
    public String getListLabelText() {
        return getString("AdditionalInfoLabel_txt");
    }

    @Override
    public char getListLabelMnemonic() {
        return getString("AdditionalInfoLabel_mn").toCharArray()[0];
    }

    @Override
    public String addAction() {
        NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine("", getString("ADD_DIALOG_LABEL_TXT"));
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
            return null;
        }
        String newS = notifyDescriptor.getInputText().trim();
        if (newS.length() == 0) {
            return null;
        }
        return newS;
    }

    @Override
    public String getAddButtonText() {
        return getString("ADD_BUTTON_LBL");
    }

    @Override
    public char getAddButtonMnemonics() {
        return getString("ADD_BUTTON_MNEMONIC").toCharArray()[0];
    }

    @Override
    public String getRemoveButtonText() {
        return getString("REMOVE_BUTTON_LBL");
    }

    @Override
    public char getRemoveButtonMnemonics() {
        return getString("REMOVE_BUTTON_MNEMONIC").toCharArray()[0];
    }

    @Override
    public String getUpButtonText() {
        return getString("UP_BUTTON_LBL");
    }

    @Override
    public char getUpButtonMnemonics() {
        return getString("UP_BUTTON_MNEMONIC").toCharArray()[0];
    }

    @Override
    public String getDownButtonText() {
        return getString("DOWN_BUTTON_LBL");
    }

    @Override
    public char getDownButtonMnemonics() {
        return getString("DOWN_BUTTON_MNEMONIC").toCharArray()[0];
    }

    private static String getString(String s) {
        return NbBundle.getBundle(PackagingAdditionalInfoPanel.class).getString(s);
    }
}

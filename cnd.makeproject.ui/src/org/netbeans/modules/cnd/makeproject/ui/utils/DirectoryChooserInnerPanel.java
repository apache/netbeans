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
package org.netbeans.modules.cnd.makeproject.ui.utils;

import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class DirectoryChooserInnerPanel extends ListEditorPanel<String> {

    private final String baseDir;
    private boolean addPathPanel;
    private final ExecutionEnvironment executionEnvironment;

    public DirectoryChooserInnerPanel(String baseDir, List<String> list, ExecutionEnvironment execEnv) {
        super(list);
        this.baseDir = baseDir;
        this.executionEnvironment = execEnv;
        getDefaultButton().setVisible(false);
    }

    @Override
    public String addAction() {
        final String chooser_key = "makeproject.DirectoryChooser"; //NOI18N
        String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, executionEnvironment);
        if (seed == null) {
            seed = baseDir;
        }
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(
                executionEnvironment,
                getString("ADD_DIRECTORY_DIALOG_TITLE"),
                getString("ADD_DIRECTORY_BUTTON_TXT"),
                JFileChooser.DIRECTORIES_ONLY, null, seed, true);

        PathPanel pathPanel = null;
        if (addPathPanel) {
            pathPanel = new PathPanel();
        }
        fileChooser.setAccessory(pathPanel);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        String itemPath = fileChooser.getSelectedFile().getPath();
        itemPath = CndPathUtilities.naturalizeSlashes(itemPath);
        RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, itemPath, executionEnvironment);
        String bd = baseDir;
        bd = CndPathUtilities.naturalizeSlashes(bd);
        itemPath = CndPathUtilities.toRelativePath(bd, itemPath);
//        if (pathPanel != null && pathPanel.getMode() == PathPanel.REL_OR_ABS) {
//            itemPath = CndPathUtilities.toAbsoluteOrRelativePath(bd, itemPath);
//        } else if (pathPanel != null && pathPanel.getMode() == PathPanel.REL) {
//            itemPath = CndPathUtilities.toRelativePath(bd, itemPath);
//        } else {
//            itemPath = itemPath;
//        }
        itemPath = CndPathUtilities.normalizeSlashes(itemPath);
        return itemPath;
    }

    @Override
    public String getListLabelText() {
        return getString("DIRECTORIES_LABEL_TXT");
    }

    @Override
    public char getListLabelMnemonic() {
        return getString("DIRECTORIES_LABEL_MN").charAt(0);
    }

    @Override
    public String getAddButtonText() {
        return getString("ADD_BUTTON_TXT");
    }

    @Override
    public char getAddButtonMnemonics() {
        return getString("ADD_BUTTON_MN").charAt(0);
    }

    @Override
    public String getRenameButtonText() {
        return getString("EDIT_BUTTON_TXT");
    }

    @Override
    public char getRenameButtonMnemonics() {
        return getString("EDIT_BUTTON_MN").charAt(0);
    }

    @Override
    public String getDownButtonText() {
        return getString("DOWN_BUTTON_TXT");
    }

    @Override
    public char getDownButtonMnemonics() {
        return getString("DOWN_BUTTON_MN").charAt(0);
    }

    @Override
    public String copyAction(String o) {
        return o;
    }

    @Override
    public void editAction(String o, int i) {
        String s = o;

        NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
        notifyDescriptor.setInputText(s);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String newS = notifyDescriptor.getInputText().trim();
        replaceElement(o, newS, i);
    }

    private static String getString(
            String key) {
        return NbBundle.getMessage(DirectoryChooserInnerPanel.class, key);
    }
}

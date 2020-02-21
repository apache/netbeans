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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

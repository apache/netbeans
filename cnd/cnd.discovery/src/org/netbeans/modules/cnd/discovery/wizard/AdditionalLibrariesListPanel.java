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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.util.NbBundle;


/**
 *
 */
public class AdditionalLibrariesListPanel extends ListEditorPanel<String> {
    
    public static JPanel wrapPanel(ListEditorPanel<String> innerPanel) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(innerPanel, gridBagConstraints);
        outerPanel.setPreferredSize(new Dimension(500, 250));
        return outerPanel;
    }
    
    public AdditionalLibrariesListPanel(List<String> objects) {
        super(objects);
        getDefaultButton().setVisible(false);
        getUpButton().setVisible(false);
        getDownButton().setVisible(false);
        getCopyButton().setVisible(false);
    }

    @Override
    public String addAction() {
        FileFilter[] filters = FileFilterFactory.getBinaryFilters();
        FileChooser fileChooser = new FileChooser(
                getString("LIBRARY_CHOOSER_TITLE_TXT"),
                getString("LIBRARY_CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                filters,
                "",
                false);
        fileChooser.setMultiSelectionEnabled(true);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (File item : fileChooser.getSelectedFiles()){
            String itemPath = item.getPath();
            itemPath = CndPathUtilities.normalizeSlashes(itemPath);
            if (buf.length() > 0) {
                buf.append(';');
            }
            buf.append(itemPath);
        }
        if (buf.length()==0) {
            return null;
        }
        return buf.toString();
    }

    @Override
    public void addObjectAction(String newObject) {
        if (newObject != null) {
            List<String> list = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(newObject, ";"); // NOI18N
            while(st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
            addObjectsAction(list);
        }
    }
    
    @Override
    public String getListLabelText() {
        return getString("LIBRARY_LIST_TXT");
    }
    @Override
    public char getListLabelMnemonic() {
        return getString("LIBRARY_LIST_MN").charAt(0);
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
    public String copyAction(String o) {
        return o;
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    @Override
    public void editAction(String o, int i) {
        String s = o;
        
        InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
        notifyDescriptor.setInputText(s);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String newS = notifyDescriptor.getInputText().trim();
        replaceElement(o, newS, i);
    }
    
    private String getString(String key) {
        return NbBundle.getMessage(AdditionalLibrariesListPanel.class, key);
    }
}

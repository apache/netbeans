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

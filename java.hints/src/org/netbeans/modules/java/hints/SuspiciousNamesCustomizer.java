/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Allows to configure groups of names. 
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "CAPTION_EditGroupOfCompatibleNames=Update name group",
    "TEXT_EditGroupOfCompatibleNames=Names:",
    "ERR_NameGroupCantBeEmpty=Name group cannot be empty",
    "# {0} - offending text",
    "ERR_NotJavaIdentifier={0} is not a valid Java identifier"
})
public class SuspiciousNamesCustomizer extends javax.swing.JPanel implements ActionListener, ListSelectionListener {
    private Preferences prefs;
    private DefaultListModel listModel = new DefaultListModel();
    /**
     * Creates new form SuspiciousNamesCustomizer
     */
    public SuspiciousNamesCustomizer(Preferences prefs) {
        initComponents();
        this.prefs = prefs;
        load();
        editButton.addActionListener(this);
        removeButton.addActionListener(this);
        addButton.addActionListener(this);
        groupList.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateEditRemove();
    }
    
    private void load() {
        String val = prefs.get(SuspiciousNamesCombination.GROUP_KEY, SuspiciousNamesCombination.DEFAULT_GROUPS);
        prefs.put(SuspiciousNamesCombination.GROUP_KEY, val);
        String[] groups = val == null ? new String[0] : val.split(Pattern.quote(SuspiciousNamesCombination.GROUP_SEPARATOR));
        for (String g : groups) {
            listModel.addElement(g);
        }
    }
    
    private void save() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listModel.size(); i++) {
            if (sb.length() > 0) {
                sb.append(SuspiciousNamesCombination.GROUP_SEPARATOR);
            }
            sb.append(listModel.get(i).toString());
        }
        prefs.put(SuspiciousNamesCombination.GROUP_KEY, sb.toString());
    }
    
    private void editPerformed() {
        int selIndex = groupList.getSelectedIndex();
        if (selIndex == -1) {
            return;
        }
        IdListInputLine il = new IdListInputLine(Bundle.TEXT_EditGroupOfCompatibleNames(), 
                Bundle.CAPTION_EditGroupOfCompatibleNames());
        il.setInputText(groupList.getModel().getElementAt(selIndex).toString());

        Object res = DialogDisplayer.getDefault().notify(il);
        if (res != DialogDescriptor.OK_OPTION) {
            return;
        }
        String normalized = normalize(il.getInputText());
        listModel.set(selIndex, normalized);
        save();
    }
    
    private void addPerformed() {
        IdListInputLine il = new IdListInputLine(Bundle.TEXT_EditGroupOfCompatibleNames(), 
                Bundle.CAPTION_EditGroupOfCompatibleNames());
        il.setInputText(""); // NOI18N

        Object res = DialogDisplayer.getDefault().notify(il);
        if (res != DialogDescriptor.OK_OPTION) {
            return;
        }
        String normalized = normalize(il.getInputText());
        int atIndex = listModel.size();
        listModel.add(atIndex, normalized);
        save();
    }
    
    private void removePerformed() {
        int selIndex = groupList.getSelectedIndex();
        if (selIndex == -1) {
            return;
        }
        int size = listModel.size();
        listModel.removeElementAt(selIndex);
        save();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == editButton) {
            editPerformed();
        } else if (src == addButton) {
            addPerformed();
        } else if (src == removeButton) {
            removePerformed();
        }
    }
    
    private String normalize(String text) {
        String[] names = text.split(SuspiciousNamesCombination.SEPARATORS_REGEX);
        StringBuilder sb = new StringBuilder();
        for (String s : names) {
            if (s.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) { sb.append(", "); }
            sb.append(s);
        }
        return sb.toString();
    }
    
    private static class IdListInputLine extends DialogDescriptor.InputLine implements DocumentListener {
        private NotificationLineSupport nls;
        
        public IdListInputLine(String text, String title) {
            super(text, title);
            if ((nls = getNotificationLineSupport()) == null) {
                nls = createNotificationLineSupport();
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            textChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            textChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            textChanged();
        }
        
        private void textChanged() {
            String s = textField.getText();
            String[] names = s.split(SuspiciousNamesCombination.SEPARATORS_REGEX);
            if (names.length == 0) {
                nls.setErrorMessage(Bundle.ERR_NameGroupCantBeEmpty());
                setValid(false);
                return;
            }
            for (String check : names) {
                if (!Utilities.isJavaIdentifier(check)) {
                    nls.setErrorMessage(Bundle.ERR_NotJavaIdentifier(check));
                    setValid(false);
                    return;
                }
            }
            
            setValid(true);
        }
    }
    
    private void updateEditRemove() {
        boolean enable = groupList.getSelectedIndex() != -1;
        editButton.setEnabled(enable);
        removeButton.setEnabled(enable);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        groupList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        groupList.setModel(listModel);
        groupList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(groupList);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SuspiciousNamesCustomizer.class, "SuspiciousNamesCustomizer.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SuspiciousNamesCustomizer.class, "SuspiciousNamesCustomizer.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(SuspiciousNamesCustomizer.class, "SuspiciousNamesCustomizer.editButton.text")); // NOI18N
        editButton.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SuspiciousNamesCustomizer.class, "SuspiciousNamesCustomizer.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeButton)
                            .addComponent(editButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel1))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)
                        .addGap(18, 18, 18)
                        .addComponent(removeButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JList groupList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}

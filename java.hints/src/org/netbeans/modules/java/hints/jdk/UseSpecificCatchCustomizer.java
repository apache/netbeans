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
package org.netbeans.modules.java.hints.jdk;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.Utilities;

/**
 * Configures the 'Use Specific Catch' inspection.
 * 
 * @author sdedic
 */
public class UseSpecificCatchCustomizer extends javax.swing.JPanel 
    implements ActionListener, DocumentListener, ListSelectionListener {
    private final Preferences prefs;
    private Color textBkColor;
    
    /**
     * Creates new form BroadCatchCustomizer
     */
    public UseSpecificCatchCustomizer(Preferences prefs) {
        this.prefs = prefs;
        initComponents();
        lstGenericTypes.setModel(new DefaultListModel());
        tfNewType.getDocument().addDocumentListener(this);
        lstGenericTypes.addListSelectionListener(this);
        btnAddGeneric.addActionListener(this);
        btnRemoveGeneric.addActionListener(this);
        btnRemoveGeneric.setEnabled(false);
        initList(lstGenericTypes, 
            prefs.get(UseSpecificCatch.OPTION_EXCEPTION_LIST, UseSpecificCatch.DEFAULT_EXCEPTION_LIST));
    }
    
    private void initList(JList l, String val) {
        StringTokenizer tukac = new StringTokenizer(val, ", ");
        DefaultListModel m = (DefaultListModel)l.getModel();
        while (tukac.hasMoreTokens()) {
            String s = tukac.nextToken();
            if (s.isEmpty()) {
                continue;
            }
            m.addElement(s);
        }
        prefs.put(UseSpecificCatch.OPTION_EXCEPTION_LIST, val);
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        JList lst = (JList)lse.getSource();
        boolean sel = lst.isEnabled() && !lst.isSelectionEmpty();
        btnRemoveGeneric.setEnabled(sel);
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        Document d = de.getDocument();
        updateControls(d, checkIdentifier(d));
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        Document d = de.getDocument();
        updateControls(d, checkIdentifier(d));
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
    }
    
    private void removeSelected(JList list, String prefKey) {
        DefaultListModel m = (DefaultListModel)list.getModel();
        while (!list.isSelectionEmpty()) {
            m.remove(list.getSelectionModel().getLeadSelectionIndex());
        }
        updatePreference(list, prefKey);
    }
    
    private void addNewType(String t, JList list, String prefKey) {
        ((DefaultListModel)list.getModel()).addElement(t);
        list.setSelectedIndex(list.getModel().getSize() - 1);
        updatePreference(list, prefKey);
    }
    
    private void updatePreference(JList list, String prefKey) {
        StringBuilder sb = new StringBuilder(35);
        for (Enumeration en = ((DefaultListModel)list.getModel()).elements(); en.hasMoreElements(); ) {
            String s = (String)en.nextElement();
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        prefs.put(prefKey, sb.toString());
    }
    
    private void updateControls(JTextField tf, JButton addButton, int state) {
        if (textBkColor == null) {
            textBkColor = tf.getBackground();
        }
        switch (state) {
            default:
                tf.setBackground(Color.pink);
                addButton.setEnabled(false);
                break;
            case -1:
                tf.setBackground(textBkColor);
                addButton.setEnabled(false);
                break;
            case 0:
                tf.setBackground(textBkColor);
                addButton.setEnabled(true);
                break;
        }
    }
    
    private void updateControls(Document d, int state) {
        updateControls(tfNewType, btnAddGeneric, state);
    }
    
    private int checkIdentifier(Document d) {
        String text;
        
        try {
            text = d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            return -1;
        }
        
        text = text.trim();
        if (text.isEmpty()) {
            return -1;
        }
        String[] parts = text.split("\\.", -1);
        for (String s : parts) {
            if (s.isEmpty() || !Utilities.isJavaIdentifier(s)) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == btnRemoveGeneric) {
            removeSelected(lstGenericTypes, UseSpecificCatch.OPTION_EXCEPTION_LIST);
        } else if (src == btnAddGeneric) {
            addNewType(tfNewType.getText(), lstGenericTypes, 
                    UseSpecificCatch.OPTION_EXCEPTION_LIST);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblGenericList = new javax.swing.JLabel();
        scrGenericTypes = new javax.swing.JScrollPane();
        lstGenericTypes = new javax.swing.JList();
        btnRemoveGeneric = new javax.swing.JButton();
        tfNewType = new javax.swing.JTextField();
        btnAddGeneric = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(360, 121));

        org.openide.awt.Mnemonics.setLocalizedText(lblGenericList, org.openide.util.NbBundle.getMessage(UseSpecificCatchCustomizer.class, "UseSpecificCatchCustomizer.lblGenericList.text")); // NOI18N

        lstGenericTypes.setVisibleRowCount(5);
        scrGenericTypes.setViewportView(lstGenericTypes);

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveGeneric, org.openide.util.NbBundle.getMessage(UseSpecificCatchCustomizer.class, "UseSpecificCatchCustomizer.btnRemoveGeneric.text")); // NOI18N

        tfNewType.setText(org.openide.util.NbBundle.getMessage(UseSpecificCatchCustomizer.class, "UseSpecificCatchCustomizer.tfNewType.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAddGeneric, org.openide.util.NbBundle.getMessage(UseSpecificCatchCustomizer.class, "UseSpecificCatchCustomizer.btnAddGeneric.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrGenericTypes, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfNewType, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnRemoveGeneric, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddGeneric, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblGenericList)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblGenericList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrGenericTypes, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfNewType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAddGeneric)))
                    .addComponent(btnRemoveGeneric)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddGeneric;
    private javax.swing.JButton btnRemoveGeneric;
    private javax.swing.JLabel lblGenericList;
    private javax.swing.JList lstGenericTypes;
    private javax.swing.JScrollPane scrGenericTypes;
    private javax.swing.JTextField tfNewType;
    // End of variables declaration//GEN-END:variables
}

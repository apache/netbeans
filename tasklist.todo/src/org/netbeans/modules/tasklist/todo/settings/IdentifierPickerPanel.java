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
package org.netbeans.modules.tasklist.todo.settings;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class IdentifierPickerPanel extends javax.swing.JPanel {

    private final List<MimeIdentifier> availableMimes;
    private final List<ExtensionIdentifier> supportedExtensions;
    private boolean lastValid = false;

    /**
     * Creates new form IdentifierPickerPanel
     */
    public IdentifierPickerPanel(List<MimeIdentifier> availableMimes, List<ExtensionIdentifier> supportedExtensions) {
        this.availableMimes = availableMimes;
        this.supportedExtensions = supportedExtensions;
        initComponents();
        initList();
    }

    List<FileIdentifier> getSelectedMimeTypes() {
        List<FileIdentifier> selectedIdentifiers = new ArrayList<FileIdentifier>();
        if (rbMime.isSelected()) {
            int[] selectedIndices = listMime.getSelectedIndices();
            for (int i : selectedIndices) {
                selectedIdentifiers.add(availableMimes.get(i));
            }
        } else {
            selectedIdentifiers.add(new ExtensionIdentifier(textExtension.getText()));
        }
        return selectedIdentifiers;
    }

    private void initList() {
        listMime.setModel(new AbstractListModel() {
            @Override
            public int getSize() {
                return availableMimes.size();
            }

            @Override
            public Object getElementAt(int index) {
                return availableMimes.get(index).getDisplayName();
            }
        });
    }

    /** Returns true if extension does not exist in model. */
    private boolean isSelectionValid() {
        if (rbExtension.isSelected()) {
            String extension = textExtension.getText();
            return extension != null && extension.length() != 0 && extension.indexOf(".") == -1 && isExtensionAvailable(extension);  //NOI18N
        } else {
           return listMime.getSelectedIndex() != -1;
        }
    }

    private boolean isExtensionAvailable(String extension) {
        for (ExtensionIdentifier extensionIdentifier : supportedExtensions) {
            if (extensionIdentifier.getDisplayName().equalsIgnoreCase(extension)) {
                return false;
            }
        }
        return true;
    }

    void addValidityListener(final NotifyDescriptor descriptor) {
        descriptor.setValid(isSelectionValid());
        textExtension.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                descriptor.setValid(isSelectionValid());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                descriptor.setValid(isSelectionValid());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                descriptor.setValid(isSelectionValid());
            }
        });

        listMime.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                descriptor.setValid(isSelectionValid());
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        listMime = new javax.swing.JList();
        rbMime = new javax.swing.JRadioButton();
        rbExtension = new javax.swing.JRadioButton();
        textExtension = new javax.swing.JTextField();

        listMime.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listMime);

        buttonGroup1.add(rbMime);
        rbMime.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbMime, NbBundle.getMessage(IdentifierPickerPanel.class, "IdentifierPickerPanel.rbMime.text")); // NOI18N
        rbMime.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbMimeStateChanged(evt);
            }
        });

        buttonGroup1.add(rbExtension);
        org.openide.awt.Mnemonics.setLocalizedText(rbExtension, NbBundle.getMessage(IdentifierPickerPanel.class, "IdentifierPickerPanel.rbExtension.text")); // NOI18N
        rbExtension.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbExtensionStateChanged(evt);
            }
        });

        textExtension.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbExtension)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textExtension, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(rbMime)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbMime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbExtension)
                    .addComponent(textExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbMimeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbMimeStateChanged
        listMime.setEnabled(rbMime.isSelected());
    }//GEN-LAST:event_rbMimeStateChanged

    private void rbExtensionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbExtensionStateChanged
        textExtension.setEnabled(rbExtension.isSelected());
    }//GEN-LAST:event_rbExtensionStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList listMime;
    private javax.swing.JRadioButton rbExtension;
    private javax.swing.JRadioButton rbMime;
    private javax.swing.JTextField textExtension;
    // End of variables declaration//GEN-END:variables
}

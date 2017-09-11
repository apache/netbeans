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

package org.netbeans.modules.versioning.util;

import java.awt.BorderLayout;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;

import javax.swing.*;
import java.util.*;
import java.awt.Dialog;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

/**
 * Provides chooser from list of strings.
 *
 * @author  Maros Sandor
 */
public class StringSelector extends javax.swing.JPanel implements MouseListener {

    public static String select(String title, String prompt, List<String> strings) {
        StringSelector panel = new StringSelector();
        DialogDescriptor descriptor = panel.prepare(title, prompt, strings, null);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return null;
        return (String) panel.listValues.getSelectedValue();
    }

    private List<String> choices;

    private DialogDescriptor prepare (String title, String prompt, List<String> strings, JPanel options) {
        Mnemonics.setLocalizedText(promptLabel, prompt);
        listValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setChoices(strings);
        if (options != null) {
            optionsPanel.add(BorderLayout.WEST, options);
        }

        DialogDescriptor descriptor = new DialogDescriptor(this, title);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StringSelector.class, "ACSD_StringSelectorDialog"));  // NOI18N
        putClientProperty(Dialog.class, dialog);
        putClientProperty(DialogDescriptor.class, descriptor);
        dialog.setVisible(true);
        return descriptor;
    }
    
    private void setChoices(List<String> strings) {
        choices = strings;
        
        listValues.setModel(new AbstractListModel() {
            public int getSize() {
                return choices.size();
            }

            public Object getElementAt(int index) {
                return choices.get(index);
            }
        });
    }

    /** Creates new form StringSelector */
    public StringSelector() {
        initComponents();
        listValues.addMouseListener(this);
    }
    
    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() == 2) {
            Dialog dialog = (Dialog) getClientProperty(Dialog.class);
            DialogDescriptor descriptor = (DialogDescriptor) getClientProperty(DialogDescriptor.class);
            descriptor.setValue(DialogDescriptor.OK_OPTION);
            dialog.dispose();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    public static class RecentMessageSelector extends StringSelector {

        private final Preferences prefs;
        private static final String KEY = "recentmessages.autofill"; //NOI18N

        public RecentMessageSelector (Preferences prefs) {
            this.prefs = prefs;
        }

        public boolean isAutoFill () {
            return prefs.getBoolean(KEY, true);
        }

        public void setAutoFill (boolean value) {
            prefs.putBoolean(KEY, value);
        }

        public String getRecentMessage (String title, String prompt, List<String> strings) {
            StringSelector panel = new StringSelector();
            JPanel options = new JPanel(new BorderLayout());
            JCheckBox autoFillCheckBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(autoFillCheckBox, NbBundle.getMessage(RecentMessageSelector.class, "RecentMessageSelector.autoFill")); //NOI18N
            autoFillCheckBox.setSelected(isAutoFill());
            options.add(BorderLayout.LINE_START, autoFillCheckBox);
            DialogDescriptor descriptor = panel.prepare(title, prompt, strings, options);
            if (descriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return null;
            }
            setAutoFill(autoFillCheckBox.isSelected());
            return (String) panel.listValues.getSelectedValue();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        promptLabel = new javax.swing.JLabel();
        jScrollPane = new javax.swing.JScrollPane();
        listValues = new javax.swing.JList();
        optionsPanel = new javax.swing.JPanel();

        promptLabel.setLabelFor(listValues);
        promptLabel.setText(org.openide.util.NbBundle.getMessage(StringSelector.class, "StringSelector.promptLabel.text")); // NOI18N

        listValues.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane.setViewportView(listValues);
        listValues.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StringSelector.class, "ACSN_StringSelectorMessages")); // NOI18N
        listValues.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StringSelector.class, "ACSD_StringSelectorMessages")); // NOI18N

        optionsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(promptLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .addGap(96, 96, 96))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(promptLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JList listValues;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel promptLabel;
    // End of variables declaration//GEN-END:variables
}

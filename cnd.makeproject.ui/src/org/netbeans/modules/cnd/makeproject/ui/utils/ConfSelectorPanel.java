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

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class ConfSelectorPanel extends javax.swing.JPanel {

    private final Configuration[] configurationItems;
    private final JCheckBox[] checkBoxes;
    private final JButton[] actionButtons;

    public ConfSelectorPanel(String labelText, char mn, Configuration[] configurationItems, JButton[] actionButtons) {
        initComponents();
        GridBagConstraints gridBagConstraints;

        this.configurationItems = configurationItems;
        this.actionButtons = actionButtons;

        // Set the label
        label.setText(labelText);
        label.setDisplayedMnemonic(mn);

        // Add the comboboxes
        CheckBoxActionListener checkBoxActionListener = new CheckBoxActionListener();
        checkBoxes = new JCheckBox[configurationItems.length];
        for (int i = 0; i < configurationItems.length; i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.addActionListener(checkBoxActionListener);
            checkBox.setBackground(new java.awt.Color(255, 255, 255));
            checkBox.setText(configurationItems[i].toString());
            checkBox.setSelected(true);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            if (i == configurationItems.length - 1) {
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
            }
            innerPanel.add(checkBox, gridBagConstraints);
            checkBoxes[i] = checkBox;
            checkBox.getAccessibleContext().setAccessibleDescription(""); // NOI18N
        }

        // Add the action buttons
        if (actionButtons != null) {
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
            for (int i = 0; i < actionButtons.length; i++) {
                gridBagConstraints.gridy++;
                buttonPanel.add(actionButtons[i], gridBagConstraints);
            }
        }

        // Set background
        innerPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Set size
        setPreferredSize(new java.awt.Dimension(350, 250));

        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("SELECTED_CONF_AD"));
        label.setDisplayedMnemonic(getString("SELECTED_CONF_MN").charAt(0));
        selectAllButton.getAccessibleContext().setAccessibleDescription(getString("SELECT_ALL_BUTTON_AD"));
        deselectAllButton.getAccessibleContext().setAccessibleDescription(getString("DESELECT_ALL_BUTTON_AD"));

        // Verify any checked
        checkCheckBoxes();
    }

    public void restoreSelection(String recentSelectionKey) {
        storeOrRestoreSelection(recentSelectionKey, false);
    }
    public void storeSelection(String recentSelectionKey) {
        storeOrRestoreSelection(recentSelectionKey, true);
    }
    
    private void storeOrRestoreSelection(String recentSelectionKey, boolean store) {
        if (checkBoxes == null || configurationItems == null || checkBoxes.length != configurationItems.length) {
            return;
        }
        Preferences prefs = NbPreferences.forModule(ConfSelectorPanel.class);
        for (int i = 0; i < configurationItems.length; i++) {            
            String key = recentSelectionKey + configurationItems[i].getName();
            if (store) {
                String value = Boolean.toString(checkBoxes[i].isSelected());                    
                prefs.put(key, value);
            } else {
                String value = prefs.get(key, Boolean.TRUE.toString());
                checkBoxes[i].setSelected(Boolean.parseBoolean(value));
            }
        }
    }

    private class CheckBoxActionListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            checkCheckBoxes();
        }
    }

    public final void checkCheckBoxes() {
        boolean oneSelected = false;
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                oneSelected = true;
                break;
            }
        }
        if (actionButtons != null) {
            for (int i = 0; i < actionButtons.length; i++) {
                actionButtons[i].setEnabled(oneSelected);
            }
        }
    }

    public Configuration[] getSelectedConfs() {
        List<Configuration> vector = new ArrayList<>();
        for (int i = 0; i < configurationItems.length; i++) {
            if (checkBoxes[i].isSelected()) {
                vector.add(configurationItems[i]);
            }
        }

        return vector.toArray(new Configuration[vector.size()]);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        confPanel = new javax.swing.JPanel();
        scrollPanel = new javax.swing.JScrollPane();
        innerPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        label.setLabelFor(innerPanel);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(label, bundle.getString("SELECTED_CONF_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 0);
        add(label, gridBagConstraints);

        confPanel.setLayout(new java.awt.GridBagLayout());

        innerPanel.setBackground(new java.awt.Color(255, 255, 255));
        innerPanel.setLayout(new java.awt.GridBagLayout());
        scrollPanel.setViewportView(innerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(scrollPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(confPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(selectAllButton, bundle.getString("SELECT_ALL_BUTTON_TXT")); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        buttonPanel.add(selectAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deselectAllButton, bundle.getString("DESELECT_ALL_BUTTON_TXT")); // NOI18N
        deselectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(deselectAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 12);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void deselectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllButtonActionPerformed
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setSelected(false);
        }
        checkCheckBoxes();
    }//GEN-LAST:event_deselectAllButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setSelected(true);
        }
        checkCheckBoxes();
    }//GEN-LAST:event_selectAllButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel confPanel;
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JLabel label;
    private javax.swing.JScrollPane scrollPanel;
    private javax.swing.JButton selectAllButton;
    // End of variables declaration//GEN-END:variables
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ConfSelectorPanel.class);
        }
        return bundle.getString(s);
    }
}

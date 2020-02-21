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
package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Replaces the old project system options panel.
 */
@OptionsPanelController.Keywords(keywords={"#ProjectsOptionsKeywords"}, location=CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID, tabTitle= "#TAB_ProjectsTab")
public class ProjectOptionsPanel extends JPanel {

    private boolean changed;
    private ArrayList<JCheckBox> checkBoxes;

    /** Creates new form ProjectOptionsPanel */
    public ProjectOptionsPanel() {
        initComponents();
        initAdditionalComponents();
        
        // Accessible Description
        filePathcomboBox.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_AD"));
        makeOptionsTextField.getAccessibleContext().setAccessibleDescription(getString("MAKE_OPTIONS_AD"));
        filePathTxt.getAccessibleContext().setAccessibleDescription(getString("FILE_PATH_TXT_AD"));
        filePathTxt.getAccessibleContext().setAccessibleName(getString("FILE_PATH_TXT_AN"));
        
        setName("TAB_ProjectsTab"); // NOI18N (used as a pattern...)

        Color c = getBackground();
        Color cc = new Color(c.getRed(), c.getGreen(), c.getBlue());
        filePathTxt.setBackground(cc);
        filePathcomboBox.addActionListener((ActionEvent e) -> {
            changed = areMakeOptionsChanged();
        });
        makeOptionsTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed = areMakeOptionsChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed = areMakeOptionsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed = areMakeOptionsChanged();
            }
        });
    }

    public void update() {
        makeOptionsTextField.setText(MakeProjectOptions.getMakeOptions());
        filePathcomboBox.removeAllItems();
        for (MakeProjectOptions.PathMode pathMode : MakeProjectOptions.PathMode.values()) {
            filePathcomboBox.addItem(pathMode);
        }
        filePathcomboBox.setSelectedItem(MakeProjectOptions.getPathMode());
        checkBoxes.forEach((cb) -> {
            NamedOption entry = (NamedOption) cb.getClientProperty("MakeOptionNamedEntity"); //NOI18N
            cb.setSelected(NamedOption.getAccessor().getBoolean(entry.getName()));
        });

        changed = false;
    }

    /** Apply changes */
    public void applyChanges() {
        MakeProjectOptions.setMakeOptions(makeOptionsTextField.getText());
        MakeProjectOptions.setPathMode((MakeProjectOptions.PathMode) filePathcomboBox.getSelectedItem());
        checkBoxes.forEach((cb) -> {
            NamedOption entry = (NamedOption) cb.getClientProperty("MakeOptionNamedEntity"); //NOI18N
            NamedOption.getAccessor().setBoolean(entry.getName(), cb.isSelected());
        });

        changed = false;
    }

    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }

    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     * 
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        return true;
    }

    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }
    
    private boolean areMakeOptionsChanged() {
        boolean isChanged = false;
        isChanged |= !MakeProjectOptions.getMakeOptions().equals(makeOptionsTextField.getText())
                || !MakeProjectOptions.getPathMode().equals((MakeProjectOptions.PathMode) filePathcomboBox.getSelectedItem());
        if (isChanged) { // no need to iterate further
            return true;
        }
        for (JCheckBox cb : checkBoxes) {
            NamedOption entry = (NamedOption) cb.getClientProperty("MakeOptionNamedEntity"); //NOI18N
            isChanged |= NamedOption.getAccessor().getBoolean(entry.getName()) != cb.isSelected();
            if (isChanged) { // no need to iterate further
                return true;
            }
        }
        return isChanged;
    }

    private void initAdditionalComponents() {
        checkBoxes = new ArrayList<>();
        int row = 20;
        GridBagConstraints gridBagConstraints;
        for (NamedOption entry : getEntries()) {
            JCheckBox wrapper = getWrapper(entry);
            checkBoxes.add(wrapper);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = row++;
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 6, 6, 6);
            add(wrapper, gridBagConstraints);
            wrapper.addActionListener((ActionEvent e) -> {
                changed = areMakeOptionsChanged();
            });
        }
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = row;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new JSeparator(), gridBagConstraints);
    }
    
    private List<NamedOption> getEntries() {
        List<NamedOption> result = new ArrayList<>();
        for(NamedOption option: Lookups.forPath(NamedOption.MAKE_PROJECT_CATEGORY).lookupAll(NamedOption.class)) {
            if (option.isVisible()) {
                result.add(option);
            }
        }
        return result;
    }
    
    private JCheckBox getWrapper(final NamedOption entry) {
        JCheckBox cb = new JCheckBox();
        Mnemonics.setLocalizedText(cb, entry.getDisplayName());
        if (entry.getDescription() != null) {
            cb.setToolTipText(entry.getDescription());
        }
        cb.setOpaque(false);
        cb.setSelected(NamedOption.getAccessor().getBoolean(entry.getName()));
        cb.putClientProperty("MakeOptionNamedEntity", entry); //NOI18N
        return cb;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ProjectOptionsPanel.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        makeOptionsLabel = new javax.swing.JLabel();
        makeOptionsTextField = new javax.swing.JTextField();
        makeOptionsTxt = new javax.swing.JLabel();
        filePathLabel = new javax.swing.JLabel();
        filePathcomboBox = new javax.swing.JComboBox();
        filePathTxt = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        makeOptionsLabel.setLabelFor(makeOptionsTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(makeOptionsLabel, bundle.getString("MAKE_OPTIONS")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(makeOptionsLabel, gridBagConstraints);

        makeOptionsTextField.setColumns(45);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 6);
        add(makeOptionsTextField, gridBagConstraints);

        makeOptionsTxt.setText(bundle.getString("MAKE_OPTIONS_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 12);
        add(makeOptionsTxt, gridBagConstraints);

        filePathLabel.setLabelFor(filePathcomboBox);
        org.openide.awt.Mnemonics.setLocalizedText(filePathLabel, bundle.getString("FILE_PATH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(filePathLabel, gridBagConstraints);

        filePathcomboBox.setMinimumSize(new java.awt.Dimension(75, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 6);
        add(filePathcomboBox, gridBagConstraints);

        filePathTxt.setEditable(false);
        filePathTxt.setLineWrap(true);
        filePathTxt.setText(bundle.getString("FILE_PATH_MODE_TXT")); // NOI18N
        filePathTxt.setWrapStyleWord(true);
        filePathTxt.setBorder(null);
        filePathTxt.setMinimumSize(new java.awt.Dimension(100, 60));
        filePathTxt.setPreferredSize(new java.awt.Dimension(100, 60));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(filePathTxt, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filePathLabel;
    private javax.swing.JTextArea filePathTxt;
    private javax.swing.JComboBox filePathcomboBox;
    private javax.swing.JLabel makeOptionsLabel;
    private javax.swing.JTextField makeOptionsTextField;
    private javax.swing.JLabel makeOptionsTxt;
    // End of variables declaration//GEN-END:variables
}

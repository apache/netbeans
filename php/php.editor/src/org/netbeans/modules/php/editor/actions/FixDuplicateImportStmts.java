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

package org.netbeans.modules.php.editor.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.netbeans.modules.php.editor.actions.ImportData.DataItem;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * JTable with custom renderer, so second column looks editable (JComboBox).
 * Second column also has CellEditor (also a JComboBox).
 *
 * @author  eakle, Martin Roskanin
 */
public class FixDuplicateImportStmts extends javax.swing.JPanel {
    private JComboBox[] combos;
    private JCheckBox checkUnusedImports;
    private ItemVariant[] defaultVariants;
    private ItemVariant[] dontUseVariants;

    public FixDuplicateImportStmts() {
        initComponents();
    }

    public void initPanel(ImportData importData, boolean removeUnusedImports) {
        initComponentsMore(importData, removeUnusedImports);
        initButtons(importData);
        setAccessible();
    }

    private void initComponentsMore(ImportData importData, boolean removeUnusedImports) {
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(UIManager.getColor("Table.background")); //NOI18N
        jScrollPane1.setBorder(UIManager.getBorder("ScrollPane.border")); //NOI18N
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(new JLabel("X").getPreferredSize().height);
        jScrollPane1.getVerticalScrollBar().setBlockIncrement(new JLabel("X").getPreferredSize().height * 10);
        int numberOfItems = importData.getItems().size();
        if (numberOfItems > 0) {
            int row = 0;
            combos = new JComboBox[numberOfItems];
            Font monoSpaced = new Font("Monospaced", Font.PLAIN, new JLabel().getFont().getSize());
            FocusListener focusListener = new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    Component c = e.getComponent();
                    Rectangle r = c.getBounds();
                    contentPanel.scrollRectToVisible(r);
                }
                @Override
                public void focusLost(FocusEvent arg0) {
                }
            };
            for (int i = 0; i < numberOfItems; i++) {
                DataItem dataItem = importData.getItems().get(i);
                combos[i] = createComboBox(dataItem, monoSpaced, focusListener);
                JLabel lblSimpleName = new JLabel(dataItem.getTypeName());
                lblSimpleName.setOpaque(false);
                lblSimpleName.setFont(monoSpaced);
                lblSimpleName.setLabelFor(combos[i]);
                contentPanel.add(lblSimpleName, new GridBagConstraints(0, row, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 5, 2, 5), 0, 0));
                contentPanel.add(combos[i], new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 2, 5), 0, 0));
            }

            contentPanel.add(new JLabel(), new GridBagConstraints(2, row, 2, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            Dimension d = contentPanel.getPreferredSize();
            d.height = getRowHeight() * Math.min(combos.length, 6);
            jScrollPane1.getViewport().setPreferredSize(d);
        } else {
            contentPanel.add(
                    new JLabel(getBundleString("FixDupImportStmts_NothingToFix")),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 20, 20, 20), 0, 0));
        }

        // load localized text into widgets:
        lblTitle.setText(getBundleString("FixDupImportStmts_IntroLbl")); //NOI18N
        lblHeader.setText(getBundleString("FixDupImportStmts_Header")); //NOI18N

        checkUnusedImports = new JCheckBox();
        Mnemonics.setLocalizedText(checkUnusedImports, getBundleString("FixDupImportStmts_UnusedImports")); //NOI18N
        bottomPanel.add(checkUnusedImports, BorderLayout.WEST);
        checkUnusedImports.setEnabled(true);
        checkUnusedImports.setSelected(removeUnusedImports);
    }

    private JComboBox createComboBox(DataItem item, Font font, FocusListener listener) {
        List<ItemVariant> variants = item.getVariants();
        JComboBox combo = new JComboBox(variants.toArray());
        combo.setSelectedItem(item.getDefaultVariant());
        combo.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_Combo_ACSD")); //NOI18N
        combo.getAccessibleContext().setAccessibleName(getBundleString("FixDupImportStmts_Combo_Name_ACSD")); //NOI18N
        combo.setOpaque(false);
        combo.setFont(font);
        combo.addFocusListener(listener);
        combo.setEnabled(variants.size() > 1);
        combo.setRenderer(new DelegatingRenderer(combo.getRenderer(), variants, item.getVariantIcons()));
        InputMap inputMap = combo.getInputMap(JComboBox.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "showPopup"); //NOI18N
        combo.getActionMap().put("showPopup", new TogglePopupAction()); //NOI18N
        return combo;
    }

    private int getRowHeight() {
        return combos.length == 0 ? 0 : combos[0].getPreferredSize().height + 6;
    }

    private static String getBundleString(String s) {
        return NbBundle.getMessage(FixDuplicateImportStmts.class, s);
    }

    @NbBundle.Messages({
        "ClearSuggestionsButton=&Clear Suggestions",
        "RestoreDefaultsButton=Restore &Defaults"
    })
    private void initButtons(ImportData importData) {
        int numberOfItems = importData.getItems().size();
        defaultVariants = new ItemVariant[numberOfItems];
        dontUseVariants = new ItemVariant[numberOfItems];
        for (int i = 0; i < numberOfItems; i++) {
            DataItem dataItem = importData.getItems().get(i);
            defaultVariants[i] = dataItem.getDefaultVariant();
            dontUseVariants[i] = dataItem.getDefaultVariant();
            for (ItemVariant variant : dataItem.getVariants()) {
                if (!variant.canBeUsed()) {
                    dontUseVariants[i] = variant;
                }
            }
        }

        bottomPanel.add(Box.createHorizontalStrut(150));

        JPanel buttonsPanel = new JPanel(new BorderLayout(5, 3));
        bottomPanel.add(buttonsPanel, BorderLayout.LINE_END);

        JButton changeNothingButton = new JButton();
        Mnemonics.setLocalizedText(changeNothingButton, Bundle.ClearSuggestionsButton());
        changeNothingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deselectAllButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(changeNothingButton, BorderLayout.LINE_START);

        JButton restoreDefaultsButton = new JButton();
        Mnemonics.setLocalizedText(restoreDefaultsButton, Bundle.RestoreDefaultsButton());
        restoreDefaultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                restoreDefaultsButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(restoreDefaultsButton, BorderLayout.LINE_END);
    }

    private void deselectAllButtonActionPerformed(ActionEvent evt) {
        for (int i = 0; i < combos.length; i++) {
            combos[i].setSelectedItem(dontUseVariants[i]);
        }
    }

    private void restoreDefaultsButtonActionPerformed(ActionEvent evt) {
        for (int i = 0; i < combos.length; i++) {
            combos[i].setSelectedItem(defaultVariants[i]);
        }
    }

    private void setAccessible() {
        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
        checkUnusedImports.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_checkUnusedImports_a11y")); // NOI18N
    }

    public List<ItemVariant> getSelections() {
        List<ItemVariant> result = new ArrayList<>();
        int numberOfCombos = combos == null ? 0 : combos.length;
        for (int i = 0; i < numberOfCombos; i++) {
            Object selectedItem = combos[i].getSelectedItem();
            assert (selectedItem instanceof ItemVariant);
            result.add((ItemVariant) selectedItem);
        }
        return result;
    }

    public boolean getRemoveUnusedImports() {
        return checkUnusedImports.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        lblHeader = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(null);
        setLayout(new java.awt.GridBagLayout());

        lblTitle.setText("~Select the fully qualified name to use in the import statement.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(lblTitle, gridBagConstraints);

        jScrollPane1.setBorder(null);

        contentPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(contentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(bottomPanel, gridBagConstraints);

        lblHeader.setText("~Import Statements:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(lblHeader, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblHeader;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables

    private static class DelegatingRenderer implements ListCellRenderer {
        private ListCellRenderer orig;
        private Icon[] icons;
        private List<ItemVariant> values;
        public DelegatingRenderer(ListCellRenderer orig, List<ItemVariant> values, Icon[] icons) {
            this.orig = orig;
            this.icons = icons;
            this.values = values;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component res = orig.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (res instanceof JLabel && null != icons) {
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).equals(value)) {
                        ((JLabel) res).setIcon(icons[i]);
                        break;
                    }
                }
            }
            return res;
        }
    }

    private static class TogglePopupAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                combo.setPopupVisible(!combo.isPopupVisible());
            }
        }
    }
}

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.netbeans.modules.php.editor.actions.ImportData.DataItem;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.openide.util.NbBundle;

/**
 * JTable with custom renderer, so second column looks editable (JComboBox).
 * Second column also has CellEditor (also a JComboBox).
 *
 * @author eakle, Martin Roskanin
 */
public class FixDuplicateImportStmts extends JPanel {

    private JComboBox[] combos;
    private FixImportsBottomPanel fixImportsBottomPanel;
    private static final long serialVersionUID = 7865344850303958108L;

    public FixDuplicateImportStmts() {
        initComponents();
    }

    public void initPanel(ImportData importData, boolean removeUnusedImports, boolean putInPSR12Order) {
        initComponentsMore(importData, removeUnusedImports, putInPSR12Order);
        setAccessible();
    }

    private void initComponentsMore(ImportData importData, boolean removeUnusedImports, boolean putInPSR12Order) {
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
        fixImportsBottomPanel = new FixImportsBottomPanel(combos, importData);
        fixImportsBottomPanel.setRemoveUnusedImports(removeUnusedImports);
        fixImportsBottomPanel.setPSR12Order(putInPSR12Order);
        bottomPanel.add(fixImportsBottomPanel);
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

    private void setAccessible() {
        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
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
        return fixImportsBottomPanel.removeUnusedImports();
    }

    public boolean isPSR12Order() {
        return fixImportsBottomPanel.isPSR12Order();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        lblTitle = new JLabel();
        jScrollPane1 = new JScrollPane();
        contentPanel = new JPanel();
        bottomPanel = new JPanel();
        lblHeader = new JLabel();

        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new GridBagLayout());

        lblTitle.setText("~Select the fully qualified name to use in the import statement.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 6, 0);
        add(lblTitle, gridBagConstraints);

        jScrollPane1.setBorder(null);

        contentPanel.setLayout(new GridBagLayout());
        jScrollPane1.setViewportView(contentPanel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        bottomPanel.setLayout(new BorderLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(bottomPanel, gridBagConstraints);

        lblHeader.setText("~Import Statements:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 0, 3, 0);
        add(lblHeader, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel bottomPanel;
    private JPanel contentPanel;
    private JScrollPane jScrollPane1;
    private JLabel lblHeader;
    private JLabel lblTitle;
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

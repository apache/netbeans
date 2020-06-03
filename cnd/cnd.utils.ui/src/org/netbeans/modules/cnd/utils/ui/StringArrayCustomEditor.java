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

package org.netbeans.modules.cnd.utils.ui;

import java.awt.Font;
import java.util.Vector;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;


/** A custom editor for array of Strings.
 *
 * 
 * Improved to have isEmptyAllowed and default value 
 */
public final class StringArrayCustomEditor extends javax.swing.JPanel {

    // the bundle to use
    private final ResourceBundle bundle = NbBundle.getBundle (
                                       StringArrayCustomEditor.class);

    private final Vector<String> itemsVector;
    private final static int DEFAULT_WIDTH = 400;

    static final long serialVersionUID =-4347656479280614636L;

    private String[] array;
    private final boolean isEmptyAllowed;
    private String defaultValue;

    /**
     *
     * @param array
     * @param defaultValue
     * @param customItemLabel could be null
     * @param customItemLabelMnemonic pass '\0' if customItemLabel is null
     * @param customItemListLabel could be null
     * @param customItemListLabelMnemonic pass '\0' if customItemListLabel is null
     * @param isEmptyAllowed
     */
    public StringArrayCustomEditor(String[] array, String defaultValue, 
            String customItemLabel, char customItemLabelMnemonic, 
            String customItemListLabel, char customItemListLabelMnemonic, 
            boolean isEmptyAllowed) {
        this.defaultValue = defaultValue;
        this.isEmptyAllowed = isEmptyAllowed;
        itemsVector = new Vector<String>();
        this.array = array;
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                itemsVector.addElement(array[i]);
            }
        }
        initComponents ();
        itemList.setCellRenderer (new EmptyStringListCellRenderer ());
        itemList.setListData (itemsVector);
        itemList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(16, 8, 8, 0)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));

        Mnemonics.setLocalizedText(itemLabel, customItemLabel != null ? customItemLabel : bundle.getString("CTL_Item")); // NOI18N
        Mnemonics.setLocalizedText(itemListLabel, customItemListLabel != null ? customItemListLabel : bundle.getString("CTL_ItemList")); // NOI18N
        Mnemonics.setLocalizedText(addButton, bundle.getString("CTL_Add_StringArrayCustomEditor")); // NOI18N
        Mnemonics.setLocalizedText(changeButton, bundle.getString("CTL_Change_StringArrayCustomEditor")); // NOI18N
        Mnemonics.setLocalizedText(removeButton, bundle.getString("CTL_Remove")); // NOI18N
        Mnemonics.setLocalizedText(setDefaultButton, bundle.getString("CTL_SetDefault")); // NOI18N

        if (customItemLabel != null) {
            itemLabel.setDisplayedMnemonic(customItemLabelMnemonic);
        }
        if (customItemListLabel != null) {
            itemListLabel.setDisplayedMnemonic(customItemListLabelMnemonic);
        }
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_StringArrayCustomEditor")); // NOI18N
        itemField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Item")); // NOI18N
        itemList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_ItemList")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Add_StringArrayCustomEditor")); // NOI18N
        changeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Change_StringArrayCustomEditor")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Remove")); // NOI18N
        setDefaultButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_SetDefault")); // NOI18N
        
        updateButtons ();
    }

    @Override
    public java.awt.Dimension getPreferredSize () {
        // ensure minimum width
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (sup.width, DEFAULT_WIDTH), sup.height);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        itemField.requestFocusInWindow();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        editPanel = new javax.swing.JPanel();
        itemListScroll = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList();
        itemLabel = new javax.swing.JLabel();
        itemField = new javax.swing.JTextField();
        itemListLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        changeButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        setDefaultButton = new javax.swing.JButton();
        paddingPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        editPanel.setLayout(new java.awt.GridBagLayout());

        itemList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                itemListValueChanged(evt);
            }
        });
        itemListScroll.setViewportView(itemList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editPanel.add(itemListScroll, gridBagConstraints);

        itemLabel.setLabelFor(itemField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 12);
        editPanel.add(itemLabel, gridBagConstraints);

        itemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        editPanel.add(itemField, gridBagConstraints);

        itemListLabel.setLabelFor(itemList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        editPanel.add(itemListLabel, gridBagConstraints);

        add(editPanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        buttonsPanel.add(addButton, gridBagConstraints);

        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        buttonsPanel.add(changeButton, gridBagConstraints);

        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        buttonsPanel.add(removeButton, gridBagConstraints);

        setDefaultButton.setEnabled(false);
        setDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDefaultButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        buttonsPanel.add(setDefaultButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        buttonsPanel.add(paddingPanel, gridBagConstraints);

        add(buttonsPanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

private void itemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemFieldActionPerformed
        if (itemList.getSelectedIndex() >= 0) {
            changeButtonActionPerformed(evt);
        } else {
            addButtonActionPerformed(evt);
        }
    }//GEN-LAST:event_itemFieldActionPerformed

    private void changeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (itemField.getText (), sel);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void setDefaultButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDefaultButtonActionPerformed
        defaultValue = (String)itemList.getSelectedValue();
        setDefaultButton.setEnabled (false);
        itemList.repaint ();
}//GEN-LAST:event_setDefaultButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        int currentIndex = itemList.getSelectedIndex ();
        itemsVector.removeElementAt (currentIndex);
        itemList.setListData (itemsVector);

        // set new selection
        if (!itemsVector.isEmpty()) {
            if (currentIndex >= itemsVector.size ()) {
                currentIndex = itemsVector.size() - 1;
            }
            itemList.setSelectedIndex (currentIndex);
            if (!itemsVector.contains(defaultValue)) {
                defaultValue = itemsVector.firstElement();
            }
        }

        itemList.repaint ();

        updateValue ();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void itemListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_itemListValueChanged
        // Add your handling code here:
        updateButtons ();
        int sel = itemList.getSelectedIndex ();
        if (sel != -1) {
            itemField.setText (itemsVector.elementAt (sel));
        }
    }//GEN-LAST:event_itemListValueChanged

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        String value = itemField.getText();
        if (value.length() == 0 || itemsVector.contains(value)) {
            return;
        }
        itemsVector.addElement (value);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (-1);
        itemField.setSelectionStart(0);
        itemField.setSelectionEnd(itemField.getText().length());
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtons () {
        int sel = itemList.getSelectedIndex ();
        if (sel == -1) {
            removeButton.setEnabled (false);
            setDefaultButton.setEnabled (false);
            changeButton.setEnabled (false);
        } else {
            removeButton.setEnabled (isEmptyAllowed || itemsVector.size() > 1);
            setDefaultButton.setEnabled (!itemList.getSelectedValue().equals(defaultValue));
            changeButton.setEnabled (true);
        }
    }

    private void updateValue () {
        array = new String [itemsVector.size()];
        itemsVector.copyInto (array);
    }
    
    public String[] getItemList() {
        updateValue();
        return array;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton changeButton;
    private javax.swing.JPanel editPanel;
    private javax.swing.JTextField itemField;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JList itemList;
    private javax.swing.JLabel itemListLabel;
    private javax.swing.JScrollPane itemListScroll;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton setDefaultButton;
    // End of variables declaration//GEN-END:variables

    private final class EmptyStringListCellRenderer extends JLabel implements ListCellRenderer {

        protected Border hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        protected Font normalFont, boldFont;

        static final long serialVersionUID =487512296465844339L;
        /** Creates a new NodeListCellRenderer */
        public EmptyStringListCellRenderer () {
            setOpaque (true);
            setBorder (noFocusBorder);
            normalFont = getFont();
            boldFont = getFont().deriveFont(Font.BOLD);
        }

        /** This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
        @Override
        public java.awt.Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof String)) {
                return this;
            }
            String text = (String)value;
            setFont(defaultValue.equals (text) ? boldFont : normalFont);
            setText(text);
            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}

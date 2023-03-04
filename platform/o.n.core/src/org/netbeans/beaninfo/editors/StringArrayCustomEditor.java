/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import org.openide.awt.Mnemonics;

//import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A custom editor for array of Strings.
*
* @author  Ian Formanek
*/
public class StringArrayCustomEditor extends javax.swing.JPanel {
    private Vector<String> itemsVector;
    private StringArrayCustomizable editor;

    static final long serialVersionUID =-4347656479280614636L;

    /** Initializes the Form */
    public StringArrayCustomEditor(StringArrayCustomizable sac) {
        editor = sac;
        itemsVector = new Vector<String> ();
        String[] array = editor.getStringArray ();
        if (array != null)
            for (int i = 0; i < array.length; i++)
                itemsVector.addElement (array[i]);
        initComponents ();
        itemList.setCellRenderer (new EmptyStringListCellRenderer ());
        itemList.setListData (itemsVector);
        itemList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(16, 8, 8, 0)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));

        Mnemonics.setLocalizedText (itemLabel, NbBundle.getMessage(
            StringArrayCustomEditor.class, "CTL_Item")); //NOI18N
        Mnemonics.setLocalizedText (itemListLabel, NbBundle.getMessage(
            StringArrayCustomEditor.class, "CTL_ItemList")); //NOI18N
        Mnemonics.setLocalizedText (addButton, NbBundle.getMessage(StringArrayCustomEditor.class, 
            "CTL_Add_StringArrayCustomEditor")); //NOI18N
        Mnemonics.setLocalizedText (changeButton, NbBundle.getMessage(StringArrayCustomEditor.class,
            "CTL_Change_StringArrayCustomEditor")); //NOI18N
        Mnemonics.setLocalizedText (removeButton, NbBundle.getMessage(StringArrayCustomEditor.class,
            "CTL_Remove")); //NOI18N
        Mnemonics.setLocalizedText (moveUpButton, NbBundle.getMessage(StringArrayCustomEditor.class,
            "CTL_MoveUp")); //NOI18N
        Mnemonics.setLocalizedText (moveDownButton, NbBundle.getMessage(
            StringArrayCustomEditor.class, "CTL_MoveDown")); //NOI18N

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
            StringArrayCustomEditor.class, "ACSD_StringArrayCustomEditor")); //NOI18N
        itemField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(
            StringArrayCustomEditor.class, "ACSD_CTL_Item")); //NOI18N
        itemList.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(
            StringArrayCustomEditor.class, "ACSD_CTL_ItemList")); //NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(StringArrayCustomEditor.class, 
            "ACSD_CTL_Add_StringArrayCustomEditor")); //NOI18N
        changeButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(StringArrayCustomEditor.class, 
            "ACSD_CTL_Change_StringArrayCustomEditor")); //NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(StringArrayCustomEditor.class, 
            "ACSD_CTL_Remove")); //NOI18N
        moveUpButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(StringArrayCustomEditor.class, 
            "ACSD_CTL_MoveUp")); //NOI18N
        moveDownButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(StringArrayCustomEditor.class, 
            "ACSD_CTL_MoveDown")); //NOI18N

        updateButtons ();
        itemField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                boolean containsCurrent = containsCurrent();
                String txt = itemField.getText().trim();
                boolean en = itemField.isEnabled() &&
                    txt.length() > 0 &&
                    !containsCurrent;
                addButton.setEnabled(en);
                changeButton.setEnabled(en && itemList.getSelectedIndex() != -1);
                if (containsCurrent) {
                    itemList.setSelectedIndex(idxOfCurrent());
                }
           }
        });
        itemField.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent ae) {
                if (addButton.isEnabled()) {
                    addButtonActionPerformed(ae);
                }
            }
        }); 
        addButton.setEnabled(false);
        changeButton.setEnabled(false);
//        HelpCtx.setHelpIDString (this, StringArrayCustomEditor.class.getName ());
        setMinimumSize(new Dimension (200, 400));
    }
    
    /** Determine if the text of the text field matches an item in the 
     * list */
    private boolean containsCurrent() {
        return idxOfCurrent() != -1;
    }
    
    private int idxOfCurrent() {
        String txt = itemField.getText().trim();
        if (txt.length() > 0) {
            int max = itemList.getModel().getSize();
            for (int i=0; i < max; i++) {
                if (txt.equals(itemList.getModel().getElementAt(i))) return i;
            }
        }
        return -1;
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
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
        jSeparator1 = new javax.swing.JSeparator();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        paddingPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        editPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;

        itemList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                itemListValueChanged(evt);
            }
        });

        itemListScroll.setViewportView(itemList);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 2;
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        editPanel.add(itemListScroll, gridBagConstraints2);

        itemLabel.setText("item");
        itemLabel.setLabelFor(itemField);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 11, 12);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        editPanel.add(itemLabel, gridBagConstraints2);

        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 11, 0);
        editPanel.add(itemField, gridBagConstraints2);

        itemListLabel.setText("jLabel1");
        itemListLabel.setLabelFor(itemList);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        editPanel.add(itemListLabel, gridBagConstraints2);

        add(editPanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;

        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(0, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        buttonsPanel.add(addButton, gridBagConstraints1);

        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        buttonsPanel.add(changeButton, gridBagConstraints1);

        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 8, 8, 8);
        gridBagConstraints1.weightx = 1.0;
        buttonsPanel.add(removeButton, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 4, 0, 4);
        buttonsPanel.add(jSeparator1, gridBagConstraints1);

        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        buttonsPanel.add(moveUpButton, gridBagConstraints1);

        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 8, 0, 8);
        gridBagConstraints1.weightx = 1.0;
        buttonsPanel.add(moveDownButton, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.weighty = 1.0;
        buttonsPanel.add(paddingPanel, gridBagConstraints1);

        add(buttonsPanel, java.awt.BorderLayout.EAST);

    }//GEN-END:initComponents

    private void changeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = itemsVector.elementAt(sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (itemField.getText (), sel);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void moveDownButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = itemsVector.elementAt(sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (s, sel + 1);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel + 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = itemsVector.elementAt(sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (s, sel - 1);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel - 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        int currentIndex = itemList.getSelectedIndex ();
        itemsVector.removeElementAt (currentIndex);
        itemList.setListData (itemsVector);

        // set new selection
        if (itemsVector.size () != 0) {
            if (currentIndex >= itemsVector.size ())
                currentIndex = itemsVector.size () - 1;
            itemList.setSelectedIndex (currentIndex);
        }

        itemList.repaint ();

        updateValue ();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void itemListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_itemListValueChanged
        // Add your handling code here:
        updateButtons ();
        int sel = itemList.getSelectedIndex ();
        if (sel != -1) {
            itemField.setText(itemsVector.elementAt(sel));
        }
    }//GEN-LAST:event_itemListValueChanged

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        itemsVector.addElement (itemField.getText ());
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (itemsVector.size () - 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_addButtonActionPerformed

    @Override
    public void setEnabled (boolean val) {
        Component[] c = getComponents();
        super.setEnabled(val);
        setChildrenEnabled (this, val);
    }
    
    private void setChildrenEnabled(JPanel parent, boolean val) {
        Component[] c = parent.getComponents();
        for (int i=0; i < c.length; i++) {
            c[i].setEnabled(val);
            if (c[i] instanceof JPanel) {
                setChildrenEnabled((JPanel) c[i], val);
            }
        }
    }
    
    private void updateButtons () {
        int sel = itemList.getSelectedIndex ();
        boolean enVal = isEnabled();
        if (sel == -1) {
            removeButton.setEnabled (false);
            moveUpButton.setEnabled (false);
            moveDownButton.setEnabled (false);
            changeButton.setEnabled (false);
        } else {
            removeButton.setEnabled (enVal && true);
            moveUpButton.setEnabled (enVal && (sel != 0));
            moveDownButton.setEnabled (enVal && (sel != itemsVector.size () - 1));
            changeButton.setEnabled (enVal && true);
        }
        itemField.setEnabled(enVal);
        // #62803: String[] editor keeps text in the textfield after removing all items
        boolean containsCurrent = containsCurrent();
        String txt = itemField.getText().trim();
        boolean en = itemField.isEnabled() &&
            txt.length() > 0 &&
            !containsCurrent;
        addButton.setEnabled(en);
    }

    private void updateValue () {
        String [] value = new String [itemsVector.size()];
        itemsVector.copyInto (value);
        editor.setStringArray (value);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editPanel;
    private javax.swing.JScrollPane itemListScroll;
    private javax.swing.JList itemList;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JTextField itemField;
    private javax.swing.JLabel itemListLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton changeButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JPanel paddingPanel;
    // End of variables declaration//GEN-END:variables

    static class EmptyStringListCellRenderer extends JLabel implements ListCellRenderer {

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        static final long serialVersionUID =487512296465844339L;
        /** Creates a new NodeListCellRenderer */
        public EmptyStringListCellRenderer () {
            setOpaque (true);
            setBorder (noFocusBorder);
        }

        /** This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
        public java.awt.Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof String)) return this;
            String text = (String)value;
            if ("".equals (text)) text = NbBundle.getMessage(StringArrayCustomEditor.class, "CTL_Empty");

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

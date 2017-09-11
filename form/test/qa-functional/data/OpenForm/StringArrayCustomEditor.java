/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.editors;

import java.util.Vector;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;

//import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A custom editor for array of Strings.
*
* @author  Ian Formanek
*/
public class StringArrayCustomEditor extends javax.swing.JPanel {

    // the bundle to use
    private ResourceBundle bundle = NbBundle.getBundle (
                                       StringArrayCustomEditor.class);

    private Vector itemsVector;
    private StringArrayCustomizable editor;

    private final static int DEFAULT_WIDTH = 400;

    static final long serialVersionUID =-4347656479280614636L;

    /** Initializes the Form */
    public StringArrayCustomEditor(StringArrayCustomizable sac) {
        editor = sac;
        itemsVector = new Vector ();
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

        itemLabel.setText (bundle.getString ("CTL_Item"));
        itemListLabel.setText(bundle.getString ("CTL_ItemList"));
        addButton.setText (bundle.getString ("CTL_Add_StringArrayCustomEditor"));
        changeButton.setText (bundle.getString ("CTL_Change_StringArrayCustomEditor"));
        removeButton.setText (bundle.getString ("CTL_Remove"));
        moveUpButton.setText (bundle.getString ("CTL_MoveUp"));
        moveDownButton.setText (bundle.getString ("CTL_MoveDown"));

        itemLabel.setDisplayedMnemonic(bundle.getString("CTL_Item_Mnemonic").charAt(0));
        itemListLabel.setDisplayedMnemonic(bundle.getString("CTL_ItemList_Mnemonic").charAt(0));
        addButton.setMnemonic(bundle.getString("CTL_Add_StringArrayCustomEditor_Mnemonic").charAt(0));
        changeButton.setMnemonic(bundle.getString("CTL_Change_StringArrayCustomEditor_Mnemonic").charAt(0));
        removeButton.setMnemonic(bundle.getString("CTL_Remove_Mnemonic").charAt(0));
        moveUpButton.setMnemonic(bundle.getString("CTL_MoveUp_Mnemonic").charAt(0));
        moveDownButton.setMnemonic(bundle.getString("CTL_MoveDown_Mnemonic").charAt(0));

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_StringArrayCustomEditor"));
        itemField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Item"));
        itemList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_ItemList"));
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Add_StringArrayCustomEditor"));
        changeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Change_StringArrayCustomEditor"));
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Remove"));
        moveUpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_MoveUp"));
        moveDownButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_MoveDown"));

        updateButtons ();

//        HelpCtx.setHelpIDString (this, StringArrayCustomEditor.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        // ensure minimum width
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (sup.width, DEFAULT_WIDTH), sup.height);
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
        String s = (String) itemsVector.elementAt (sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (itemField.getText (), sel);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void moveDownButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = (String) itemsVector.elementAt (sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (s, sel + 1);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel + 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = (String) itemsVector.elementAt (sel);
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
            itemField.setText ((String) itemsVector.elementAt (sel));
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

    private void updateButtons () {
        int sel = itemList.getSelectedIndex ();
        if (sel == -1) {
            removeButton.setEnabled (false);
            moveUpButton.setEnabled (false);
            moveDownButton.setEnabled (false);
            changeButton.setEnabled (false);
        } else {
            removeButton.setEnabled (true);
            moveUpButton.setEnabled (sel != 0);
            moveDownButton.setEnabled (sel != itemsVector.size () - 1);
            changeButton.setEnabled (true);
        }
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
            if ("".equals (text)) text = NbBundle.getMessage (EmptyStringListCellRenderer.class, "CTL_Empty");

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

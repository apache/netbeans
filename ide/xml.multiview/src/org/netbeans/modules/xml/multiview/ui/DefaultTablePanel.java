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

package org.netbeans.modules.xml.multiview.ui;

// Swing
import java.awt.Font;
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

// Netbeans
import org.openide.util.NbBundle;

/** Generic panel containing the table and NEW - EDIT - DELETE buttons.
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class DefaultTablePanel extends javax.swing.JPanel {
    protected JButton moveUpButton, moveDownButton, sourceButton;
    private boolean reordable;
    private AbstractTableModel model;
    
    /** Creates a new TablePanel.
    * @param model AbstractTableModel for included table
    */
    public DefaultTablePanel(AbstractTableModel model) {
        this(model, false);
    }

    /** Creates a new TablePanel.
    * @param model AbstractTableModel for included table
    * @param reordable specifies whether the order of the rows is important(in DD filter-mappings for example the order of elements is important)
    * @param isSource specifies if there is a reasonable source file/link related to the table row
    */ 
    public DefaultTablePanel(AbstractTableModel model, final boolean reordable) {
        this.model=model;
        this.reordable=reordable;
        initComponents();
        jTable1.setModel(model);

        /* accomodate row height so that characters can fit: */
        java.awt.Component cellSample
                = jTable1.getDefaultRenderer(String.class)
                  .getTableCellRendererComponent(
                          jTable1,          //table
                          "N/A",            //value                     //NOI18N
                          false,            //isSelected
                          false,            //hasFocus
                          0, 0);            //row, column
        int cellHeight = cellSample.getPreferredSize().height;
        int rowHeight = cellHeight + jTable1.getRowMargin();
        jTable1.setRowHeight(Math.max(16, rowHeight));

        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        javax.swing.table.JTableHeader header = jTable1.getTableHeader();
        add(header, java.awt.BorderLayout.NORTH);
        
        jTable1.getSelectionModel().addListSelectionListener
        (
                new ListSelectionListener()
                {
                        public void valueChanged(ListSelectionEvent e)
                        {
                                // ignore extra messages
                                if (e.getValueIsAdjusting())
                                {
                                        return;
                                }

                                if (((ListSelectionModel)e.getSource()).isSelectionEmpty())
                                {
                                        editButton.setEnabled(false);
                                        removeButton.setEnabled(false);
                                        if (reordable) {
                                            moveUpButton.setEnabled(false);
                                            moveDownButton.setEnabled(false);
                                        }
                                }
                                else
                                {
                                        editButton.setEnabled(true);
                                        removeButton.setEnabled(true);
                                        if (reordable) {
                                            int row = jTable1.getSelectedRow();
                                            if (row<jTable1.getModel().getRowCount()-1) moveDownButton.setEnabled(true);
                                            else moveDownButton.setEnabled(false);
                                            if (row>0) moveUpButton.setEnabled(true);
                                            else moveUpButton.setEnabled(false);
                                        }
                                }
                        }
                }
        );
        if (reordable) {
            moveUpButton = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Move_Up")); // NOI18N
            moveUpButton.setToolTipText(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "HINT_Move_Up")); // NOI18N
            moveDownButton = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(moveDownButton,NbBundle.getMessage(DefaultTablePanel.class, "LBL_Move_Down"));
            moveDownButton.setToolTipText(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "HINT_Move_Down")); // NOI18N
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            buttonPanel.add(moveUpButton);
            buttonPanel.add(moveDownButton);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTable1 = new javax.swing.JTable();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Add")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "HINT_Add")); // NOI18N
        addButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        buttonPanel.add(addButton);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Edit")); // NOI18N
        editButton.setToolTipText(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "HINT_Edit")); // NOI18N
        editButton.setEnabled(false);
        editButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        buttonPanel.add(editButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Remove")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "HINT_Remove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        buttonPanel.add(removeButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jTable1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(jTable1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    protected javax.swing.JButton editButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTable jTable1;
    protected javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    public AbstractTableModel getModel() {
        return model;
    }
    
    public void setButtons(boolean b1, boolean b2, boolean b3) {
        addButton.setEnabled(b1);
        editButton.setEnabled(b2);
        removeButton.setEnabled(b3);
    }
    
    public void setButtons(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6) {
        this.setButtons(b1,b2,b3);
        moveUpButton.setEnabled(b4);
        moveDownButton.setEnabled(b5);
    }
    
    public boolean isReordable() {
        return reordable;
    }
    
    public void setSelectedRow(int row) {
        jTable1.setRowSelectionInterval(row,row);
    }
    
    public void setTitle(String title) {
        javax.swing.JLabel label = new javax.swing.JLabel(title);
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD, font.getSize() + 2));
        label.setBorder(new javax.swing.border.EmptyBorder(5,5,5,0));
        add(label, java.awt.BorderLayout.NORTH);
    }
    
    public javax.swing.JTable getTable() {
        return jTable1;
    }
}

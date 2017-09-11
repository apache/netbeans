/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * PropertyEditorPanel.java
 *
 * Created on 01.04.2011, 20:25:24
 */
package org.netbeans.modules.db.util;

import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Custom implentation for a property editor, as the build in doesn't work to
 * well with international characters
 *
 * @author Matthias BlÃ¤sing
 */
public class PropertyEditorPanel extends javax.swing.JPanel {

    public static final String PROP_VALUE = "value";
    private Properties value;
    private boolean editable;
    private boolean updateing;

    public PropertyEditorPanel(Properties initalValue, boolean editable) {
        initComponents();
        this.value = initalValue;
        this.editable = editable;
        propertyTable.putClientProperty(
                "terminateEditOnFocusLost", Boolean.TRUE);              //NOI18N
        updateTableFromEditor();
        final TableModel tm = propertyTable.getModel();
        tm.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                synchronized (PropertyEditorPanel.this) {
                    if (updateing) {
                        return;
                    }
                    updateing = true;
                    Properties p = new Properties();
                    for (int i = 0; i < tm.getRowCount(); i++) {
                        p.setProperty((String) tm.getValueAt(i, 0), (String) tm.getValueAt(i, 1));
                    }
                    Properties oldValue = value;
                    value = p;
                    firePropertyChange(PROP_VALUE, oldValue, value);
                    updateing = false;
                }
            }
        });
        propertyTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent lse) {
                        updateRemoveButtonSensible();
                    }
                });
        updateAddButtonSensible();
        updateRemoveButtonSensible();
    }

    private void updateAddButtonSensible() {
        if (this.editable) {
            addRowButton.setEnabled(true);
        } else {
            addRowButton.setEnabled(false);
        }
    }

    private void updateRemoveButtonSensible() {
        if (this.editable && propertyTable.getSelectedRowCount() > 0) {
            removeRowButton.setEnabled(true);
        } else {
            removeRowButton.setEnabled(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateTableFromEditor() {
        synchronized (this) {
            if (updateing) {
                return;
            }
            updateing = true;
            DefaultTableModel dtm = (DefaultTableModel) propertyTable.getModel();
            Vector columns = new Vector(2);
            Vector values = new Vector();
            columns.add(dtm.getColumnName(0));
            columns.add(dtm.getColumnName(1));
            if (value != null) {
                for (String key : value.stringPropertyNames()) {
                    Vector row = new Vector(2);
                    row.add(key);
                    row.add(value.getProperty(key, ""));
                    values.add(row);
                }
            }
            dtm.setDataVector(values, columns);
            updateing = false;
        }
    }

    public Properties getValue() {
        return value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        addRowButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();
        propertyScrollPane = new javax.swing.JScrollPane();
        propertyTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        addRowButton.setText(org.openide.util.NbBundle.getMessage(PropertyEditorPanel.class, "PropertyEditorPanel.addRowButton.text")); // NOI18N
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(addRowButton);

        removeRowButton.setText(org.openide.util.NbBundle.getMessage(PropertyEditorPanel.class, "PropertyEditorPanel.removeRowButton.text")); // NOI18N
        removeRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRowButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(removeRowButton);

        add(buttonPanel, java.awt.BorderLayout.PAGE_END);

        propertyTable.setAutoCreateRowSorter(true);
        propertyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return PropertyEditorPanel.this.editable;
            }
        });
        propertyTable.setColumnSelectionAllowed(true);
        propertyScrollPane.setViewportView(propertyTable);
        propertyTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        propertyTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(PropertyEditorPanel.class, "PropertyEditorPanel.propertyTable.columnModel.title0")); // NOI18N
        propertyTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(PropertyEditorPanel.class, "PropertyEditorPanel.propertyTable.columnModel.title1")); // NOI18N

        add(propertyScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed
        DefaultTableModel dtm = (DefaultTableModel) propertyTable.getModel();
        dtm.addRow(new Object[]{"", ""});
    }//GEN-LAST:event_addRowButtonActionPerformed

    private void removeRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowButtonActionPerformed
        int[] viewRows = propertyTable.getSelectedRows();
        int[] modelRows = new int[viewRows.length];

        for (int i = 0; i < viewRows.length; i++) {
            modelRows[i] = propertyTable.convertRowIndexToModel(viewRows[i]);
        }

        Arrays.sort(modelRows);

        DefaultTableModel dtm = (DefaultTableModel) propertyTable.getModel();

        for (int i = modelRows.length - 1; i >= 0; i--) {
            dtm.removeRow(modelRows[i]);
        }
    }//GEN-LAST:event_removeRowButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRowButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JScrollPane propertyScrollPane;
    private javax.swing.JTable propertyTable;
    private javax.swing.JButton removeRowButton;
    // End of variables declaration//GEN-END:variables
}

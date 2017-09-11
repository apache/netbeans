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

package org.netbeans.modules.j2ee.core.support.java.method;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Martin Adamek
 */
public final class ParametersPanel extends javax.swing.JPanel {
    
    private static final int COL_NAME_INDEX = 0;
    private static final int COL_TYPE_INDEX = 1;
    private static final int COL_FINAL_INDEX = 2;
    
    private static final String[] columnNames = {
        NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.LBL_Name"),  // NOI18N
        NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.LBL_Type"),  // NOI18N
        NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.LBL_Final"),  // NOI18N
    };
    
    public static final String SELECTION = "selection"; // NOI18N
    
    private final ParamsTableModel tableModel;

    public ParametersPanel(ClasspathInfo cpInfo, List<MethodModel.Variable> parameters) {
        initComponents();
        
        tableModel = new ParamsTableModel(parameters);
        table.setModel(tableModel);
        tableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                firePropertyChange("parameters", null, null); // NOI18N
            }
        });

        if (parameters.isEmpty()) {
            table.setFocusable(false);
        }
        
        JComboBox typeCombo = new JComboBox();
        
        ReturnTypeUIHelper.connect(typeCombo, cpInfo);
        TableColumn typeTableColumn = table.getColumnModel().getColumn(COL_TYPE_INDEX);
        typeTableColumn.setCellEditor(new DefaultCellEditor(typeCombo));

        // #147884 fix
        typeCombo.addActionListener(new TypeComboListener(typeCombo));
        
        table.setRowHeight(typeCombo.getPreferredSize().height);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        
        ListSelectionListener listSelectionListener = new ListSelectionListenerImpl();
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listSelectionListener);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                updateButtons();
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateButtons();
            }
        });
        
        updateButtons();
    }
    
    public List<MethodModel.Variable> getParameters() {
        return tableModel.getParameters();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(table);
        table.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSN_ParametersTab")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSD_ParametersTab")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.upButton.text")); // NOI18N
        upButton.setEnabled(false);
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.downButton.text")); // NOI18N
        downButton.setEnabled(false);
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(upButton)
                    .addComponent(downButton))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, downButton, removeButton, upButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addGap(22, 22, 22)
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                .addContainerGap())
        );

        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSD_AddParameterIntoTab")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSD_RemoveParameterIntoTab")); // NOI18N
        upButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSD_MoveUpInParamTab")); // NOI18N
        downButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ACSD_MoveDownInParamTab")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
    int selIndex = table.getSelectedRow();
    int newIndex = selIndex - 1;
    if (newIndex >= 0) {
        tableModel.set(newIndex, tableModel.set(selIndex, tableModel.getParameter(newIndex)));
        table.setRowSelectionInterval(newIndex, newIndex);
        updateButtons();
    }
}//GEN-LAST:event_upButtonActionPerformed

private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
    int selIndex = table.getSelectedRow();
    int newIndex = selIndex + 1;
    if (newIndex < tableModel.getParameters().size()) {
        tableModel.set(newIndex, tableModel.set(selIndex, tableModel.getParameter(newIndex)));
        table.setRowSelectionInterval(newIndex, newIndex);
        updateButtons();
    }
}//GEN-LAST:event_downButtonActionPerformed

private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    int selectedRow = table.getSelectedRow();
    if (selectedRow > -1) {
        tableModel.removeParameter(selectedRow);
    }
    if (selectedRow == table.getRowCount()) {
        selectedRow--;
    }
    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    updateButtons();
}//GEN-LAST:event_removeButtonActionPerformed

private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    int index = tableModel.addParameter();
    table.getSelectionModel().setSelectionInterval(index, index);
    table.getColumnModel().getSelectionModel().setSelectionInterval(COL_NAME_INDEX, COL_NAME_INDEX);
    updateButtons();
}//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JTable table;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    
    private void updateButtons() {
        int selIndex = table.getSelectedRow();
        boolean oneSelected = table.getSelectedRowCount() == 1;

        removeButton.setEnabled(oneSelected);
        upButton.setEnabled(oneSelected && (selIndex > 0));
        downButton.setEnabled(oneSelected && (selIndex < tableModel.getRowCount() - 1));
    }
    
    // accessible for test
    static class ParamsTableModel extends AbstractTableModel {
        
        private final List<MethodModel.Variable> parameters;
        
        public ParamsTableModel(List<MethodModel.Variable> parameters) {
            this.parameters = new ArrayList<MethodModel.Variable>(parameters);
        }
        
        public List<MethodModel.Variable> getParameters() {
            return parameters;
        }
        
        public int addParameter() {
            String name = generateUniqueName("parameter");  // NOI18N
            MethodModel.Variable parameter = MethodModel.Variable.create("java.lang.String", name, false);  // NOI18N
            int index = parameters.size();
            parameters.add(parameter);
            fireTableRowsInserted(index, index);
            return index;
        }
        
        public void removeParameter(int index) {
            parameters.remove(index);
            fireTableRowsDeleted(index, index);
        }
        
        public MethodModel.Variable getParameter(int index) {
            return parameters.get(index);
        }
        
        public MethodModel.Variable set(int index, MethodModel.Variable parameter) {
            return parameters.set(index, parameter);
        }
        
        public int getRowCount() {
            return parameters.size();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public Object getValueAt(int row, int column) {
            Object result = null;
            if (row >= 0) {
                MethodModel.Variable parameter = parameters.get(row);
                if (parameter != null) {
                    switch (column) {
                        case COL_NAME_INDEX: result = parameter.getName(); break;
                        case COL_TYPE_INDEX: result = parameter.getType(); break;
                        case COL_FINAL_INDEX: result = parameter.getFinalModifier(); break;
                        default:
                    }
                }
            }
            return result;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int row, int column) {
            // check if inserted name is valid Java identifier
            // if not, fall back to old value
            if (column == COL_NAME_INDEX) {
                String insertedName = (String) aValue;
                if (!Utilities.isJavaIdentifier(insertedName.trim())) {
                    return;
                }
            }
            MethodModel.Variable parameter = parameters.get(row);
            MethodModel.Variable changedParameter = MethodModel.Variable.create(
                    column == COL_TYPE_INDEX ? chooseType(aValue, parameter.getType()) : parameter.getType(),
                    column == COL_NAME_INDEX ? chooseName(aValue, parameter.getName()) : parameter.getName(),
                    column == COL_FINAL_INDEX ? (Boolean) aValue : parameter.getFinalModifier()
                    );
            parameters.set(row, changedParameter);
            fireTableCellUpdated(row, column);
        }
        
        // JTable uses this method to determine the default renderer/editor for each cell.
        // If we didn't implement this method, then the last column would contain
        // text ("true"/"false"), rather than a check box.
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        private String generateUniqueName(String name) {
            List<Integer> numberSuffixes = new ArrayList<Integer>();
            for (MethodModel.Variable variable : parameters) {
                if (!name.equals(variable.getName()) && variable.getName().startsWith(name)) {
                    String suffix = variable.getName().substring(name.length());
                    if (isNumber(suffix)) {
                        numberSuffixes.add(Integer.parseInt(suffix));
                    }
                }
            }
            Collections.sort(numberSuffixes);
            String result = name;
            if (numberSuffixes.size() > 0) {
                int newSuffix = numberSuffixes.get(numberSuffixes.size() - 1) + 1;
                result = name + newSuffix;
            } else if (parameters.size() > 0) {
                result = name + 1;
            }
            return result;
        }
        
        private boolean isNumber(String value) {
            for (char character : value.toCharArray()) {
                if (!Character.isDigit(character)) {
                  return false;
                }
            }
          return true;//!value.trim().equals("");
        }
        
    }

    private static String chooseType(Object aValue, String typeName) {
        if (!(aValue instanceof String)) {
            return "*";  // NOI18N
        }
        String aValueString = ((String)aValue).trim();
        if (aValueString.equals("")) {  // NOI18N
            return typeName;
        }
        return aValueString;
    }

    private static String chooseName(Object aValue, String name) {
        String aValueString = ((String)aValue).trim();
        if (aValueString.equals("")) {  // NOI18N
            return name;
        }
        return aValueString;
    }
        
    private class ListSelectionListenerImpl implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
            Component editor = table.getEditorComponent();
            if (editor instanceof JComboBox) {
                editor = ((JComboBox) editor).getEditor().getEditorComponent();
            }
            if (editor != null) {
                editor.requestFocus();
            }
            if (editor instanceof JTextComponent) {
                JTextComponent textComp = (JTextComponent) editor;
                textComp.selectAll();
            }
        }

    }

    private class TypeComboListener implements ActionListener {
        private JComboBox typeCombo;
        
        TypeComboListener(JComboBox combo) {
            this.typeCombo = combo;
        }
        
        public void actionPerformed(ActionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String typeInModel = tableModel.getValueAt(selectedRow, COL_TYPE_INDEX).toString();
                if ("*".equals(typeInModel) && typeCombo.getSelectedItem() != null) {  // NOI18N
                    tableModel.setValueAt(typeCombo.getSelectedItem(), selectedRow, COL_TYPE_INDEX);
                }
            }
        }
    }
    
}

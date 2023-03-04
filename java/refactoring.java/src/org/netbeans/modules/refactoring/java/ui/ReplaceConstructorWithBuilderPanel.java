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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithBuilderRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderPanel extends javax.swing.JPanel implements CustomRefactoringPanel {

    private static final String DEFAULT_PREFIX = "set";
    private static final String[] columnNames = {
        getString("LBL_BuilderParameter"), // NOI18N
        getString("LBL_BuilderSetterName"), // NOI18N
        getString("LBL_BuilderDefaultValue"), // NOI18N
        getString("LBL_BuilderOptionalSetter") // NOI18N
    };
    private static final boolean[] columnCanEdit = new boolean[]{
        false, true, true, true
    };
    private static final Class[] columnTypes = new Class[]{
        String.class, String.class, String.class, Boolean.class
    };
    private final List<String> parameterTypes;
    private final List<Boolean> parameterTypeVars;
    private final List<String> parameterNames;

    public ReplaceConstructorWithBuilderPanel(final @NonNull ChangeListener parent, String initialFQN,
            String initialBuildMethodName,
            List<String> paramaterNames, List<String> parameterTypes, List<Boolean> parameterTypeVars) {
        initComponents();
        this.parameterTypes = parameterTypes;
        this.parameterNames = paramaterNames;
        prefixField.setText(DEFAULT_PREFIX);
        buildMethodNameField.setText(initialBuildMethodName);
        nameField.setText(initialFQN);
        nameField.setSelectionStart(0);
        nameField.setSelectionEnd(nameField.getText().length());

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        buildMethodNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });        
        DefaultTableModel model = (DefaultTableModel) paramTable.getModel();
        Iterator<String> typesIt = parameterTypes.iterator();
        for (String name : paramaterNames) {
            model.addRow(new Object[]{typesIt.next() + " " + name, DEFAULT_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1), null, false}); //NOI18N
        }
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                parent.stateChanged(new ChangeEvent(ReplaceConstructorWithBuilderPanel.this));
            }
        });
        prefixField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                updateSetters(de);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                updateSetters(de);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                updateSetters(de);
            }

            private void updateSetters(DocumentEvent de) {
                try {
                    String prefix = de.getDocument().getText(0, de.getDocument().getLength());
                    updateSetterNames(prefix);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        this.parameterTypeVars = parameterTypeVars;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buildName = new javax.swing.JLabel();
        buildMethodNameField = new javax.swing.JTextField();
        paramScrollPane = new javax.swing.JScrollPane();
        paramTable = new JTable() {

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 2 || column == 3) {
                    return !parameterTypeVars.get(row);
                }
                return super.isCellEditable(row, column);
            }
        };
        prefixLabel = new javax.swing.JLabel();
        prefixField = new javax.swing.JTextField();
        buildMethodName = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(buildName, org.openide.util.NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, "ReplaceConstructorWithBuilder.jLabel1.text")); // NOI18N

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{}, columnNames) {
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnCanEdit[columnIndex];
            }
        });
        paramScrollPane.setViewportView(paramTable);

        org.openide.awt.Mnemonics.setLocalizedText(prefixLabel, org.openide.util.NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, "ReplaceConstructorWithBuilder.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buildMethodName, org.openide.util.NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, "ReplaceConstructorWithBuilder.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(paramScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(prefixLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prefixField, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buildMethodName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buildName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buildMethodNameField)
                    .addComponent(nameField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prefixLabel)
                    .addComponent(prefixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paramScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildMethodName)
                    .addComponent(buildMethodNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildName)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel buildMethodName;
    private javax.swing.JTextField buildMethodNameField;
    private javax.swing.JLabel buildName;
    private javax.swing.JTextField nameField;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JTable paramTable;
    private javax.swing.JTextField prefixField;
    private javax.swing.JLabel prefixLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void initialize() {
    }

    public String getBuilderName() {
        return nameField.getText();
    }
    
    public String getBuildMethodName() {
        return buildMethodNameField.getText();
    }

    @Override
    public boolean requestFocusInWindow() {
        nameField.requestFocusInWindow();
        return true;
    }

    public List<ReplaceConstructorWithBuilderRefactoring.Setter> getSetters() {
        List<ReplaceConstructorWithBuilderRefactoring.Setter> result = new ArrayList<>();
        int size = parameterTypes.size();
        for (int i = 0; i < size; i++) {
            final String name = (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 0);
            result.add(new ReplaceConstructorWithBuilderRefactoring.Setter(
                    (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 1),
                    parameterTypes.get(i),
                    (String) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 2),
                    name.substring(name.lastIndexOf(' ')).trim(),
                    (Boolean) ((DefaultTableModel) paramTable.getModel()).getValueAt(i, 3)));
        }
        return result;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ReplaceConstructorWithBuilderPanel.class, key);
    }
    
    private void updateSetterNames(String prefix) {
        DefaultTableModel model = (DefaultTableModel) paramTable.getModel();
        
        for (int k = 0;k < parameterNames.size();k ++) {
            if (prefix == null || prefix.isEmpty()) {
                model.setValueAt(parameterNames.get(k),k,1);
            } else {
                model.setValueAt(prefix + Character.toUpperCase(parameterNames.get(k).charAt(0)) 
                        + parameterNames.get(k).substring(1),k,1);
            }
        }
        
    }
}

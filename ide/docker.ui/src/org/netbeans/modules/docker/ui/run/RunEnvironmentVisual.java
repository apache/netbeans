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
package org.netbeans.modules.docker.ui.run;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;


public class RunEnvironmentVisual extends javax.swing.JPanel {

    @SuppressWarnings("this-escape")
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final EnvModel model = new EnvModel();

    /**
     * Creates new form RunNetworkVisual
     */
    @SuppressWarnings("this-escape")
    public RunEnvironmentVisual() {
        initComponents();

        UiUtils.configureRowHeight(envTable);

        envTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        envTable.setModel(model);

        model.addTableModelListener((TableModelEvent e) -> {
            changeSupport.fireChange();
        });

        envFileTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public List<EnvEntry> getEnvironment() {
        return model.getMappings();
    }

    public void setEnvironment(List<EnvEntry> mapping) {
        model.setMappings(mapping);
    }

    public String getEnvFilePath() {
        return envFileTextField.getText();
    }

    public void setEnvFilePath(String value) {
        envFileTextField.setText(value);
    }

    @NbBundle.Messages("LBL_Environment=Environment")
    @Override
    public String getName() {
        return Bundle.LBL_Environment();
    }

    private static final class EnvModel extends AbstractTableModel {

        private final List<EnvEntry> entries = new ArrayList<>();

        public EnvModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EnvEntry single = entries.get(rowIndex);
            switch (columnIndex) {
                case 0 -> { return single.key(); }
                case 1 -> { return single.value(); }
                default -> throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex > entries.size() - 1 || rowIndex < 0) {
                return;
            }
            EnvEntry single = entries.get(rowIndex);
            switch (columnIndex) {
                case 0 -> entries.set(rowIndex, new EnvEntry((String) aValue, single.value()));
                case 1 -> entries.set(rowIndex, new EnvEntry(single.key(), (String) aValue));
                default -> throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @NbBundle.Messages({
            "LBL_EnvKey=Key",
            "LBL_EnvValue=Value",
        })
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0 -> { return Bundle.LBL_EnvKey(); }
                case 1 -> { return Bundle.LBL_EnvValue(); }
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0 -> { return String.class; }
                case 1 -> { return String.class; }
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow(EnvEntry entry) {
            entries.add(entry);
            fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
        }

        public void removeRow(int row) {
            entries.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public List<EnvEntry> getMappings() {
            return new ArrayList<>(entries);
        }

        public void setMappings(List<EnvEntry> mappings) {
            this.entries.clear();
            this.entries.addAll(mappings);
            fireTableDataChanged();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked", "this-escape"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        portMappingLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        envTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        envFileLabel = new javax.swing.JLabel();
        envFileTextField = new javax.swing.JTextField();
        envFileSelect = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(portMappingLabel, org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.environmentLabel.text")); // NOI18N

        jScrollPane1.setViewportView(envTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(envFileLabel, org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.envFileLabel.text")); // NOI18N

        envFileTextField.setText(org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.envFileTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(envFileSelect, org.openide.util.NbBundle.getMessage(RunEnvironmentVisual.class, "RunEnvironmentVisual.envFileSelect.text")); // NOI18N
        envFileSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                envFileSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(envFileLabel)
                        .addGap(10, 10, 10)
                        .addComponent(envFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(envFileSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(portMappingLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removeButton)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(envFileLabel))
                    .addComponent(envFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(envFileSelect))
                .addGap(10, 10, 10)
                .addComponent(portMappingLabel)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addGap(10, 10, 10)
                        .addComponent(removeButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        model.addRow(new EnvEntry("", ""));
        if (envTable.editCellAt(envTable.getRowCount() - 1, 0)) {
            Component editor = envTable.getEditorComponent();
            if (editor != null) {
                editor.requestFocusInWindow();
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] selectedRows = envTable.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            model.removeRow(selectedRows[i]);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void envFileSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_envFileSelectActionPerformed
        JFileChooser chooser = new JFileChooser(envFileTextField.getText());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(".env-File", "env"));
        chooser.setAcceptAllFileFilterUsed(true);
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.envFileTextField.setText(chooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_envFileSelectActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel envFileLabel;
    private javax.swing.JButton envFileSelect;
    private javax.swing.JTextField envFileTextField;
    private javax.swing.JTable envTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel portMappingLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}

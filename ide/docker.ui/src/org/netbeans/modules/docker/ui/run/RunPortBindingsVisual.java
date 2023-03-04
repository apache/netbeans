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
package org.netbeans.modules.docker.ui.run;

import org.netbeans.modules.docker.api.PortMapping;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.netbeans.modules.docker.api.DockerImageDetail;
import org.netbeans.modules.docker.api.ExposedPort;
import org.netbeans.modules.docker.api.ExposedPort.Type;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class RunPortBindingsVisual extends javax.swing.JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final DockerImageDetail info;

    private final PortMappingModel model = new PortMappingModel();

    /**
     * Creates new form RunNetworkVisual
     */
    public RunPortBindingsVisual(DockerImageDetail info) {
        initComponents();
        this.info = info;

        addExposedButton.setEnabled(info != null && !info.getExposedPorts().isEmpty());
        portMappingTable.setModel(model);
        UiUtils.configureRowHeight(portMappingTable);

        TableColumn typeColumn = portMappingTable.getColumnModel().getColumn(0);
        JComboBox typeCombo = new JComboBox(ExposedPort.Type.values());
        typeColumn.setCellEditor(new DefaultCellEditor(typeCombo));
        typeColumn.setPreferredWidth(typeColumn.getPreferredWidth() / 2);

        TableColumn portColumn = portMappingTable.getColumnModel().getColumn(2);
        portColumn.setCellRenderer(new CellRenderer("<random>", false));

        TableColumn addressColumn = portMappingTable.getColumnModel().getColumn(3);
        JComboBox addressCombo = new JComboBox(UiUtils.getAddresses(false, false).toArray());
        addressCombo.setEditable(true);
        addressColumn.setCellEditor(new DefaultCellEditor(addressCombo));
        addressColumn.setCellRenderer(new CellRenderer("<any>", false));
        addressColumn.setPreferredWidth(addressColumn.getPreferredWidth() * 2);

        portMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
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

    public boolean isRandomBind() {
        return randomBindCheckBox.isSelected();
    }

    public void setRandomBind(boolean randomBind) {
        randomBindCheckBox.setSelected(randomBind);
    }

    public List<PortMapping> getPortMapping() {
        return model.getMappings();
    }

    public void setPortMapping(List<PortMapping> mapping) {
        model.setMappings(mapping);
    }

    @NbBundle.Messages("LBL_RunPortBindings=Port Bindings")
    @Override
    public String getName() {
        return Bundle.LBL_RunPortBindings();
    }

    private static final class PortMappingModel extends AbstractTableModel {

        private final List<PortMapping> mappings = new ArrayList<>();

        public PortMappingModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PortMapping single = mappings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return single.getType();
                case 1:
                    return single.getPort();
                case 2:
                    return single.getHostPort();
                case 3:
                    return single.getHostAddress();
                default:
                    throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex > mappings.size() - 1 || rowIndex < 0) {
                return;
            }
            PortMapping single = mappings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    mappings.set(rowIndex, new PortMapping(
                            (Type) aValue,
                            single.getPort(),
                            single.getHostPort(),
                            single.getHostAddress()));
                    break;
                case 1:
                    Integer val1 = null;
                    if (aValue != null) {
                        String str = aValue.toString();
                        if (!str.isEmpty()) {
                            val1 = Integer.parseInt(str);
                        }
                    }
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            val1,
                            single.getHostPort(),
                            single.getHostAddress()));
                    break;
                case 2:
                    Integer val2 = null;
                    if (aValue != null) {
                        String str = aValue.toString();
                        if (!str.isEmpty()) {
                            val2 = Integer.parseInt(str);
                        }
                    }
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            single.getPort(),
                            val2,
                            single.getHostAddress()));
                    break;
                case 3:
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            single.getPort(),
                            single.getHostPort(),
                            aValue != null ? aValue.toString() : null));
                    break;
                default:
                    throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @NbBundle.Messages({
            "LBL_PortMappingType=Type",
            "LBL_PortMappingPort=Port",
            "LBL_PortMappingTargetPort=Host Port",
            "LBL_PortMappingTargetAddres=Host Address",
        })
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Bundle.LBL_PortMappingType();
                case 1:
                    return Bundle.LBL_PortMappingPort();
                case 2:
                    return Bundle.LBL_PortMappingTargetPort();
                case 3:
                    return Bundle.LBL_PortMappingTargetAddres();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Type.class;
                case 1:
                    return Integer.class;
                case 2:
                    return Integer.class;
                case 3:
                    return String.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void addRow(PortMapping mapping) {
            mappings.add(mapping);
            fireTableRowsInserted(mappings.size() - 1, mappings.size() - 1);
        }

        public void removeRow(int row) {
            mappings.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public List<PortMapping> getMappings() {
            return new ArrayList<>(mappings);
        }

        public void setMappings(List<PortMapping> mappings) {
            this.mappings.clear();
            this.mappings.addAll(mappings);
            fireTableDataChanged();
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {

        private final String emptyValue;

        private final boolean italic;

        public CellRenderer(String emptyValue, boolean italic) {
            this.emptyValue = emptyValue;
            this.italic = italic;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Object toRender = value;
            if (emptyValue != null
                    && (toRender == null || ((toRender instanceof String) && ((String) toRender).trim().isEmpty()))) {
                toRender = emptyValue;
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, toRender, isSelected, hasFocus, row, column);
            if (toRender != null && Number.class.isAssignableFrom(toRender.getClass())) {
                label.setHorizontalAlignment(TRAILING);
            } else {
                label.setHorizontalAlignment(LEADING);
            }
            if (italic) {
                if (toRender != value) {
                    Font italic = new Font(label.getFont().getName(), Font.ITALIC, label.getFont().getSize());
                    label.setFont(italic);
                } else {
                    label.setFont(table.getFont());
                }
            }
            return label;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        portMappingLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        portMappingTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addExposedButton = new javax.swing.JButton();
        randomBindCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(portMappingLabel, org.openide.util.NbBundle.getMessage(RunPortBindingsVisual.class, "RunPortBindingsVisual.portMappingLabel.text")); // NOI18N

        jScrollPane1.setViewportView(portMappingTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RunPortBindingsVisual.class, "RunPortBindingsVisual.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RunPortBindingsVisual.class, "RunPortBindingsVisual.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addExposedButton, org.openide.util.NbBundle.getMessage(RunPortBindingsVisual.class, "RunPortBindingsVisual.addExposedButton.text")); // NOI18N
        addExposedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addExposedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(randomBindCheckBox, org.openide.util.NbBundle.getMessage(RunPortBindingsVisual.class, "RunPortBindingsVisual.randomBindCheckBox.text")); // NOI18N
        randomBindCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                randomBindCheckBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(portMappingLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(randomBindCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addExposedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(randomBindCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(portMappingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addExposedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addExposedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addExposedButtonActionPerformed
        List<PortMapping> current = model.getMappings();
        for (ExposedPort p : info.getExposedPorts()) {
            boolean present = false;
            for (PortMapping m : current) {
                if (p.getType() == m.getType()
                        && m.getPort() != null && p.getPort() == m.getPort()) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                model.addRow(new PortMapping(p.getType(), p.getPort(), p.getPort(), null));
            }
        }
    }//GEN-LAST:event_addExposedButtonActionPerformed

    @NbBundle.Messages("LBL_Add=Add")
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        model.addRow(new PortMapping(Type.TCP, null, null, null));
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] selectedRows = portMappingTable.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            model.removeRow(selectedRows[i]);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void randomBindCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_randomBindCheckBoxItemStateChanged
        boolean selected = randomBindCheckBox.isSelected();
        portMappingTable.setEnabled(!selected);
        addExposedButton.setEnabled(!selected);
        addButton.setEnabled(!selected);
        removeButton.setEnabled(!selected);

        TableCellEditor editor = portMappingTable.getCellEditor();
        if (editor != null) {
            editor.cancelCellEditing();
        }
        portMappingTable.clearSelection();
    }//GEN-LAST:event_randomBindCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addExposedButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel portMappingLabel;
    private javax.swing.JTable portMappingTable;
    private javax.swing.JCheckBox randomBindCheckBox;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}

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

/*
 * OptionsPanel.java
 *
 * Created on Sep 12, 2008, 6:32:13 PM
 */
package org.netbeans.modules.websvc.customization.jaxwssettings.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author rico
 */
public class OptionsPanel extends javax.swing.JPanel {

    private List<String> reservedOptions;
    private Object[] columnNames;
    private List<WsimportOption> options;
    private OptionsTableModel optionsTableModel;
    private AddButtonActionListener addListener;
    private RemoveButtonActionListener removeListener;
    private WsimportOptions wsimportOptions;

    public OptionsPanel() {
        this(new String[]{"", ""}, new ArrayList<WsimportOption>(), new ArrayList<String>(), null);
    }

    /** Creates new form OptionsPanel */
    public OptionsPanel(Object[] columnNames, List<WsimportOption> options, List<String> reservedOptions, WsimportOptions wsimportOptions) {
        initComponents();
        this.reservedOptions = reservedOptions;
        this.columnNames = columnNames;
        this.options = options;
        this.wsimportOptions = wsimportOptions;
        optionsTableModel = new OptionsTableModel(columnNames, options);
        optionsTable.setModel(optionsTableModel);
        optionsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        ListSelectionListener listSelectionListener = new ListSelectionListenerImpl();
        optionsTable.getSelectionModel().addListSelectionListener(listSelectionListener);
        optionsTable.getColumnModel().getSelectionModel().addListSelectionListener(listSelectionListener);
        addListener = new AddButtonActionListener();
        ActionListener al = (ActionListener) WeakListeners.create(ActionListener.class, addListener,
                addBtn);
        addBtn.addActionListener(al);
        removeListener = new RemoveButtonActionListener();
        ActionListener rl = (ActionListener) WeakListeners.create(ActionListener.class, removeListener,
                removeBtn);
        removeBtn.addActionListener(rl);
        removeBtn.setEnabled(false);

    }

    class RemoveButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            String option = (String) optionsTableModel.getValueAt(selectedRow, 0);
            if (confirmDeletion(option)) {
                if (selectedRow > -1) {
                    optionsTableModel.removeOption(selectedRow);
                }
                if (selectedRow == optionsTable.getRowCount()) {
                    selectedRow--;
                }
                optionsTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            }
            updateButtons();
        }

        private boolean confirmDeletion(String option) {
            NotifyDescriptor.Confirmation notifyDesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(WsimportOptionsPanel.class, "MSG_CONFIRM_DELETE", option), NbBundle.getMessage(WsimportOptionsPanel.class, "TTL_CONFIRM_DELETE"), NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return notifyDesc.getValue() == NotifyDescriptor.YES_OPTION;
        }
    }

    private void updateButtons() {
        boolean oneSelected = optionsTable.getSelectedRowCount() == 1;
        removeBtn.setEnabled(oneSelected);
    }

    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel) optionsTable.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        } else {
            return lsm.getMinSelectionIndex();
        }
    }

    public void setReservedOptions(List<String> reservedOptions) {
        this.reservedOptions = reservedOptions;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
        optionsTableModel.setColumnIdentifiers(columnNames);
    }

    public void setOptions(List<WsimportOption> options) {
        this.options = options;
        for (WsimportOption option : options) {
            optionsTableModel.addRow(new String[]{option.getWsimportOptionName(), option.getWsimportOptionValue()});
        }
    }

    public List<WsimportOption> getOptions() {
        return  optionsTableModel.getOptions();
    }

    public TableModel getOptionsTableModel() {
        return optionsTableModel;
    }

    class AddButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            int index = optionsTableModel.addOption();
            optionsTable.getSelectionModel().setSelectionInterval(index, index);
            optionsTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
            updateButtons();
        }
    }

    private class ListSelectionListenerImpl implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            optionsTable.editCellAt(optionsTable.getSelectedRow(), optionsTable.getSelectedColumn());
            Component editor = optionsTable.getEditorComponent();

            if (editor != null) {
                editor.requestFocus();
            }
            if (editor instanceof JTextComponent) {
                JTextComponent textComp = (JTextComponent) editor;
                textComp.selectAll();
            }
            updateButtons();
        }
    }

    class OptionsTableModel extends DefaultTableModel {

        private List<WsimportOption> options;
        private String[] columnNames;

        public OptionsTableModel(Object[] columnNames, List<WsimportOption> options) {
            super(columnNames, options.size());
            this.columnNames = (String[]) columnNames;
            this.options = options;

        }

        @Override
        public Class getColumnClass(int c) {
            //return getValueAt(0, c).getClass();
            return super.getColumnClass(c);

        }

        public List<WsimportOption> getOptions() {
            return options;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        private String generateUniqueName(final String name) {
            String uniqueName = name;
            int suffix = 1;
            Set<String> names = new HashSet<String>();
            for (WsimportOption option : options) {
                names.add(option.getWsimportOptionName());
            }
            while (names.contains(uniqueName)) {
                uniqueName = name + ++suffix;
            }
            return uniqueName;
        }

        public int addOption() {
            String name = generateUniqueName(NbBundle.getMessage(OptionsPanel.class, "DEFAULT_OPTION_NAME"));
            int index = options.size();
            String defaultValue = NbBundle.getMessage(OptionsPanel.class, "DEFAULT_VALUE");
            this.addRow(new String[]{name, defaultValue});
            WsimportOption opt = wsimportOptions.newWsimportOption();
            opt.setWsimportOptionName(name);
            opt.setWsimportOptionValue(defaultValue);
            options.add(opt);
            fireTableRowsInserted(index, index);
            return index;
        }

        public void removeOption(int index) {
            options.remove(index);
            this.removeRow(index);
            fireTableRowsDeleted(index, index);
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object result = null;
            if (row >= 0) {
                WsimportOption option = options.get(row);
                switch (column) {
                    case 0:
                        result = option.getWsimportOptionName();
                        break;
                    case 1:
                        result = option.getWsimportOptionValue();
                        break;
                    default:
                }
            }
            return result;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            WsimportOption opt = null;
            String insertedValue = (String) aValue;
            String oldValue = (String) getValueAt(row, 1);
            // check if option name is reserved
            // if so, fall back to old value
            if (column == 0) {
                String oldKey = (String) getValueAt(row, 0);
                if (oldKey.equals(insertedValue)) {
                    return;
                }
                if (reservedOptions != null) {
                    for (String option : reservedOptions) {
                        if (insertedValue.trim().equals(option)) {
                            NotifyDescriptor descriptor =
                                    new NotifyDescriptor.Message(NbBundle.getMessage(WsimportOptionsPanel.class, "ERR_RESERVED_OPTION", insertedValue));
                            DialogDisplayer.getDefault().notify(descriptor);
                            WsimportOption op = wsimportOptions.newWsimportOption();
                            op.setWsimportOptionName(oldKey);
                            op.setWsimportOptionValue(oldValue);
                            options.set(row, op);
                            fireTableCellUpdated(row, column);
                            return;
                        }
                    }
                }
                opt = wsimportOptions.newWsimportOption();
                opt.setWsimportOptionName(insertedValue);
                opt.setWsimportOptionValue((String) getValueAt(row, 1));
            } else if (column == 1) {
                if (oldValue != null && oldValue.equals(insertedValue)) {
                    return;
                }
                opt = wsimportOptions.newWsimportOption();
                opt.setWsimportOptionName((String) getValueAt(row, 0)) ;
                opt.setWsimportOptionValue(insertedValue);
            }
            options.set(row, opt);
            fireTableCellUpdated(row, column);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        optionsTable = new javax.swing.JTable();
        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();

        jScrollPane1.setViewportView(optionsTable);

        addBtn.setText("Add");
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "HINT_Add")); // NOI18N

        removeBtn.setText("Remove");
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "HINT_Remove")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(removeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removeBtn)))
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(addBtn, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "Add_DotDotDot_label")); // NOI18N
        addBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "Add_DotDotDot_label")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(OptionsPanel.class, "Remove_label")); // NOI18N
        removeBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "Remove_label")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "Remove_label")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable optionsTable;
    private javax.swing.JButton removeBtn;
    // End of variables declaration//GEN-END:variables
}

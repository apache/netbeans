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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.awt.BorderLayout;
import java.awt.Component;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

/**
 * @author pfiala
 */
public class InnerTablePanel extends SectionNodeInnerPanel {

    private final TablePanel tablePanel;
    private JTable table;

    protected void setButtonListeners(final InnerTableModel model) {
        final JTable table = getTable();
        getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopCellEditing(table);
                selectCell(model.addRow(), 0);
                model.modelUpdatedFromUI();
                org.netbeans.modules.xml.multiview.Utils.scrollToVisible(tablePanel);
            }
        });
        getEditButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editCell(table.getSelectedRow(), table.getSelectedColumn());
            }
        });
        getRemoveButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                final int column = table.getSelectedColumn();
                final TableCellEditor cellEditor = table.getCellEditor(row, column);
                if (cellEditor != null) {
                    cellEditor.cancelCellEditing();
                }
                int rowCount = model.getRowCount() - 1;
                model.removeRow(row);
                model.modelUpdatedFromUI();
                if (row >= rowCount) {
                    row = rowCount - 1;
                }
                if (row >= 0) {
                    final int n = row;
                    Utils.runInAwtDispatchThread(new Runnable() {
                        public void run() {
                            selectCell(n, column);
                        }
                    });
                }
            }
        });
    }

    private void stopCellEditing(final JTable table) {
        table.editCellAt(-1, -1); // finish possible editing
    }

    private class TablePanel extends DefaultTablePanel {

        /**
         * Creates a new InnerTablePanel.
         *
         * @param model DefaultTableModel for included table
         */
        public TablePanel(final AbstractTableModel model) {
            super(model);
            final JTable table = getTable();
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int selectedRowCount = table.getSelectedRowCount();
                    removeButton.setEnabled(selectedRowCount > 0);
                    editButton.setEnabled(selectedRowCount == 1);
                }
            });
        }

        public JButton getAddButton() {
            return addButton;
        }

        public JButton getEditButton() {
            return editButton;
        }

        public JButton getRemoveButton() {
            return removeButton;
        }
    }

    public InnerTablePanel(SectionNodeView sectionNodeView, final InnerTableModel model) {
        super(sectionNodeView);
        tablePanel = new TablePanel(model);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                int type = e.getType();
                if (type == TableModelEvent.INSERT || type == TableModelEvent.DELETE) {
                    scheduleRefreshView();
                }
            }
        });
        table = tablePanel.getTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.addMouseListener(new MouseAdapter() {
            /**
             * Invoked when the mouse has been clicked on a component.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editCell(table.getSelectedRow(), table.getSelectedColumn());
                }
            }
        });
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "selectNextColumnCell"); //NOI18N
        inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "selectPreviousColumnCell");    //NOI18N
        table.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InnerTablePanel.class, "ACSN_Table")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InnerTablePanel.class, "ACSD_Table")); // NOI18N
        setLayout(new BorderLayout());
        add(tablePanel, BorderLayout.WEST);
        setColumnWidths();
        setButtonListeners(model);
        setColumnEditors(model);
        scheduleRefreshView();
        final TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
            final TableCellEditor tableCellEditor = model.getTableCellEditor(i);
            if (tableCellEditor != null) {
                columnModel.getColumn(i).setCellEditor(tableCellEditor);
            }
        }
    }

    private void setColumnEditors(InnerTableModel model) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableCellEditor cellEditor = model.getCellEditor(i);
            if (cellEditor != null) {
                columnModel.getColumn(i).setCellEditor(cellEditor);
            }
        }
    }

    public JTable getTable() {
        return table;
    }

    public JButton getAddButton() {
        return tablePanel.getAddButton();
    }

    public JButton getEditButton() {
        return tablePanel.getEditButton();
    }

    public JButton getRemoveButton() {
        return tablePanel.getRemoveButton();
    }

    public void setColumnWidths() {
        final JTable table = tablePanel.getTable();
        InnerTableModel tableModel = (InnerTableModel) table.getModel();
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
            int width = tableModel.getDefaultColumnWidth(i);
            final TableColumn column = columnModel.getColumn(i);
            column.setPreferredWidth(width);
            column.setWidth(width);
        }
    }

    protected void editCell(final int row, final int column) {
        if (table.isCellEditable(row, column)) {
            selectCell(row, column);
            Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    final JTable table = getTable();
                    table.editCellAt(row, column);
                    Component editorComponent = table.getEditorComponent();
                    editorComponent.requestFocus();
                }
            });
        }
    }

    private void selectCell(final int row, final int column) {
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                final JTable table = getTable();
                table.getSelectionModel().setLeadSelectionIndex(row);
                table.getColumnModel().getSelectionModel().setLeadSelectionIndex(column);
                table.requestFocus();
            }
        });
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void setValue(JComponent source, Object value) {
    }

    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        ((InnerTableModel) getTable().getModel()).tableChanged();
        scheduleRefreshView();
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

}

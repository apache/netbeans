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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

/**
 *
 * @author Jiri Sedlacek
 */
class ProfilerTableActions {
    
    static void install(ProfilerTable table) {
        new ProfilerTableActions(table).install();
    }
    
    
    private final ProfilerTable table;
    
    private ProfilerTableActions(ProfilerTable table) { this.table = table; }
    
    private void install() {
        ActionMap map = table.getActionMap();
        
        map.put("selectNextColumn", selectNextColumnAction()); // NOI18N
        map.put("selectPreviousColumn", selectPreviousColumnAction()); // NOI18N
        map.put("selectNextColumnCell", createNextCellAction()); // NOI18N
        map.put("selectPreviousColumnCell", createPreviousCellAction()); // NOI18N
        map.put("selectFirstColumn", selectFirstColumnAction()); // NOI18N
        map.put("selectLastColumn", selectLastColumnAction()); // NOI18N
        map.put("selectNextRowCell", selectNextRowAction()); // NOI18N
        map.put("selectPreviousRowCell", selectPreviousRowAction()); // NOI18N
        
        map.put("selectNextRowExtendSelection", map.get("selectNextRow")); // NOI18N
        map.put("selectPreviousRowExtendSelection", map.get("selectPreviousRow")); // NOI18N
        map.put("selectNextColumnExtendSelection", map.get("selectNextColumn")); // NOI18N
        map.put("selectPreviousColumnExtendSelection", map.get("selectPreviousColumn")); // NOI18N
        map.put("selectLastColumnExtendSelection", map.get("selectLastColumn")); // NOI18N
        map.put("selectFirstColumnExtendSelection", map.get("selectFirstColumn")); // NOI18N
    }
    
    private Action selectNextRowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int row = table.getSelectedRow();
                if (row == -1) {
                    table.selectColumn(cModel.getFirstVisibleColumn(), false);
                    table.selectRow(0, true);
                } else {
                    if (++row == table.getRowCount()) {
                        row = 0;
                        int column = table.getSelectedColumn();
                        if (column == -1) column = cModel.getFirstVisibleColumn();
                        column = cModel.getNextVisibleColumn(column);
                        table.selectColumn(column, false);
                    }
                    table.selectRow(row, true);
                }
            }
        };
    }
    
    private Action selectPreviousRowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int row = table.getSelectedRow();
                if (row == -1) {
                    table.selectColumn(cModel.getLastVisibleColumn(), false);
                    table.selectRow(table.getRowCount() - 1, true);
                } else {
                    if (--row == -1) {
                        row = table.getRowCount() - 1;
                        int column = table.getSelectedColumn();
                        if (column == -1) column = cModel.getLastVisibleColumn();
                        column = cModel.getPreviousVisibleColumn(column);
                        table.selectColumn(column, false);
                    }
                    table.selectRow(row, true);
                }
            }
        };
    }
    
    private Action selectFirstColumnAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int row = table.getSelectedRow();
                table.selectColumn(cModel.getFirstVisibleColumn(), row != -1);
                if (row == -1) table.selectRow(0, true);
            }
        };
    }
    
    private Action selectLastColumnAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int row = table.getSelectedRow();
                table.selectColumn(cModel.getLastVisibleColumn(), row != -1);
                if (row == -1) table.selectRow(0, true);
            }
        };
    }
    
    private Action selectNextColumnAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int column = table.getSelectedColumn();
                if (column == -1) {
                    table.selectColumn(cModel.getFirstVisibleColumn(), false);
                    table.selectRow(0, true);
                } else {
                    int nextColumn = cModel.getNextVisibleColumn(column);
                    if (nextColumn > column) table.selectColumn(nextColumn, true);
                }
            }
        };
    }
    
    private Action selectPreviousColumnAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;
                
                int column = table.getSelectedColumn();
                if (column == -1) {
                    table.selectColumn(cModel.getFirstVisibleColumn(), false);
                    table.selectRow(0, true);
                } else {
                    int previousColumn = cModel.getPreviousVisibleColumn(column);
                    if (previousColumn < column) table.selectColumn(previousColumn, true);
                }
            }
        };
    }
    
    private Action createNextCellAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;

                int column = table.getSelectedColumn();
                if (column == -1) {
                    table.selectColumn(cModel.getFirstVisibleColumn(), false);
                    table.selectRow(0, true);
                } else {
                    int nextColumn = cModel.getNextVisibleColumn(column);
                    boolean differentRow = nextColumn <= column && table.getRowCount() > 1;
                    if (nextColumn != column) table.selectColumn(nextColumn, !differentRow);
                    if (differentRow) {
                        int row = table.getSelectedRow();
                        int newRow = getNextRow(row);
                        if (row != newRow) table.selectRow(newRow, true);
                    }
                }
            }
        };
    }
    
    private Action createPreviousCellAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ProfilerColumnModel cModel = table._getColumnModel();
                if (table.getRowCount() == 0 || cModel.getVisibleColumnCount() == 0) return;

                int column = table.getSelectedColumn();
                if (column == -1) {
                    table.selectColumn(cModel.getFirstVisibleColumn(), false);
                    table.selectRow(0, true);
                } else {
                    int previousColumn = cModel.getPreviousVisibleColumn(column);
                    boolean differentRow = previousColumn >= column && table.getRowCount() > 1;
                    if (previousColumn != column) table.selectColumn(previousColumn, !differentRow);
                    if (differentRow) {
                        int row = table.getSelectedRow();
                        int newRow = getPreviousRow(row);
                        if (row != newRow) table.selectRow(newRow, true);
                    }
                }
            }
        };
    }
    
    private int getNextRow(int row) {
        return ++row == table.getRowCount() ? 0 : row;
    }
    
    private int getPreviousRow(int row) {
        return --row == -1 ? table.getRowCount() - 1 : row;
    }
    
}

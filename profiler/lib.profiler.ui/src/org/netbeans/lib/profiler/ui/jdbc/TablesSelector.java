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
package org.netbeans.lib.profiler.ui.jdbc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.lib.profiler.ui.swing.FilteringToolbar;
import org.netbeans.lib.profiler.ui.swing.ProfilerPopup;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.SmallButton;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class TablesSelector {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.jdbc.Bundle"); // NOI18N
    private static final String SELECT_TABLES = messages.getString("TablesSelector_SelectTables"); // NOI18N
    private static final String FILTER_TABLES = messages.getString("TablesSelector_FilterTables"); // NOI18N
    private static final String COLUMN_SELECTED = messages.getString("TablesSelector_ColumnSelected"); // NOI18N
    private static final String COLUMN_TABLE = messages.getString("TablesSelector_ColumnTable"); // NOI18N
    private static final String COLUMN_SELECTED_TOOLTIP = messages.getString("TablesSelector_ColumnSelectedToolTip"); // NOI18N
    private static final String COLUMN_TABLE_TOOLTIP = messages.getString("TablesSelector_ColumnTableToolTip"); // NOI18N
    private static final String ACT_SELECT_ALL = messages.getString("TablesSelector_ActSelectAll"); // NOI18N
    private static final String ACT_UNSELECT_ALL = messages.getString("TablesSelector_ActUnselectAll"); // NOI18N
    // -----
    
    
    private final String[] tables;
    private final Collection<String> selected;
    
    
    public TablesSelector(Collection<String> tables, Collection<String> selected) {
        this.tables = tables.toArray(new String[0]);
        this.selected = new HashSet(selected);
    }
    
    
    public void show(Component invoker) {
        UI ui = new UI();
        ui.show(invoker);
    }
    
    
    protected abstract void selectionChanged(Collection<String> selected);
    
    
    private class UI {
        
        private JPanel panel;
        private SmallButton selectAll;
        private SmallButton unselectAll;
        
        UI() {
            populatePopup();
        }
        
        void show(Component invoker) {
            int resizeMode = ProfilerPopup.RESIZE_TOP | ProfilerPopup.RESIZE_LEFT;
            ProfilerPopup.createRelative(invoker, panel, SwingConstants.NORTH_EAST, resizeMode).show();
        }
        
        private void populatePopup() {
            JPanel content = new JPanel(new BorderLayout());
            
            JLabel hint = new JLabel(SELECT_TABLES, JLabel.LEADING);
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            content.add(hint, BorderLayout.NORTH);

            final SelectedTablesModel tablesModel = new SelectedTablesModel();
            final ProfilerTable tablesTable = new ProfilerTable(tablesModel, true, false, null);
            tablesTable.setColumnToolTips(new String[] {
                COLUMN_SELECTED_TOOLTIP,
                COLUMN_TABLE_TOOLTIP });
            tablesTable.setMainColumn(1);
            tablesTable.setFitWidthColumn(1);
            tablesTable.setDefaultSortOrder(1, SortOrder.ASCENDING);
            tablesTable.setSortColumn(1);
            tablesTable.setFixedColumnSelection(0); // #268298 - make sure SPACE always hits the Boolean column
            tablesTable.setColumnRenderer(0, new CheckBoxRenderer());
            LabelRenderer projectRenderer = new LabelRenderer();
            tablesTable.setColumnRenderer(1, projectRenderer);
            int w = new JLabel(tablesTable.getColumnName(0)).getPreferredSize().width;
            tablesTable.setDefaultColumnWidth(0, w + 15);
            int h = tablesTable.getRowHeight() * 8;
            h += tablesTable.getTableHeader().getPreferredSize().height;
            projectRenderer.setText("A LONGEST EXPECTED TABLE NAME A LONGEST EXPECTED TABLE NAME"); // NOI18N
            Dimension prefSize = new Dimension(w + projectRenderer.getPreferredSize().width, h);
            tablesTable.setPreferredScrollableViewportSize(prefSize);
            ProfilerTableContainer tableContainer = new ProfilerTableContainer(tablesTable, true, null);
            JPanel tableContent = new JPanel(new BorderLayout());
            tableContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            tableContent.add(tableContainer, BorderLayout.CENTER);
            content.add(tableContent, BorderLayout.CENTER);

            JToolBar controls = new FilteringToolbar(FILTER_TABLES) {
                protected void filterChanged() {
                    if (isAll()) tablesTable.setRowFilter(null);
                    else tablesTable.setRowFilter(new RowFilter() {
                        public boolean include(RowFilter.Entry entry) {
                            return passes(entry.getStringValue(1));
                        }
                    });
                }
            };
            
            controls.add(Box.createHorizontalStrut(2));
            controls.addSeparator();
            controls.add(Box.createHorizontalStrut(3));
            
            selectAll = new SmallButton(" " + ACT_SELECT_ALL + " ") { // NOI18N
                protected void fireActionPerformed(ActionEvent e) {
                    super.fireActionPerformed(e);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            selected.clear();
                            tablesModel.fireTableDataChanged();
                            doSelectionChanged(selected);
                        }
                    });
                }
            };
            controls.add(selectAll);
            unselectAll = new SmallButton(" " + ACT_UNSELECT_ALL + " ") { // NOI18N
                protected void fireActionPerformed(ActionEvent e) {
                    super.fireActionPerformed(e);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            selected.clear();
                            selected.addAll(Arrays.asList(tables));
                            tablesModel.fireTableDataChanged();
                            doSelectionChanged(selected);
                        }
                    });
                }
            };
            controls.add(unselectAll);

            content.add(controls, BorderLayout.SOUTH);

            panel = content;
            
            updateSelectionButtons();
        }
        
        private void updateSelectionButtons() {
            selectAll.setEnabled(selected.size() > 0);
            unselectAll.setEnabled(selected.size() < tables.length);
        }
        
        private void doSelectionChanged(Collection<String> selected) {
            updateSelectionButtons();
            selectionChanged(selected);
        }
        
        private class SelectedTablesModel extends AbstractTableModel {
            
            public String getColumnName(int columnIndex) {
                if (columnIndex == 0) {
                    return COLUMN_SELECTED;
                } else if (columnIndex == 1) {
                    return COLUMN_TABLE;
                }
                return null;
            }

            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else if (columnIndex == 1) {
                    return Lookup.Provider.class;
                }
                return null;
            }

            public int getRowCount() {
                return tables.length;
            }

            public int getColumnCount() {
                return 2;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return !selected.contains(tables[rowIndex]);
                } else if (columnIndex == 1) {
                    return tables[rowIndex];
                }
                return null;
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (Boolean.TRUE.equals(aValue)) {
                    if (selected.remove(tables[rowIndex])) doSelectionChanged(selected);
                } else {
                    if (selected.add(tables[rowIndex])) doSelectionChanged(selected);
                }
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

        }
        
    }
    
}

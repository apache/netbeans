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

package org.netbeans.modules.maven.api.customizer.support;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister;


public final class SelectedItemsTable extends JTable {

    private static final int CHECKBOX_WIDTH = new JCheckBox().getWidth();

    public SelectedItemsTable(SelectedItemsTableModel model) {
        super(model);

        getColumnModel().getColumn(0).setMaxWidth(CHECKBOX_WIDTH + 20);
        setRowHeight(getFontMetrics(getFont()).getHeight() + (2 * getRowMargin()));
        setTableHeader(null);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setShowGrid(false);

        final Action switchAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = getSelectedRow();
                if (row == -1) {
                    // Nothing selected; e.g. user has tabbed into the table but not pressed Down key.
                    return;
                }
                Boolean b = (Boolean) getValueAt(row, 0);
                setValueAt(Boolean.valueOf(!b.booleanValue()), row, 0);
            }
        };

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchAction.actionPerformed(null);
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing"); // NOI18N
        getActionMap().put("startEditing", switchAction); // NOI18N
    }

    public SelectedItemsTable() {
        this(null);
    }


    public static final class SelectedItemsTableModel extends AbstractTableModel {

        private Boolean[] selected;
        private Boolean[] originalSelected;
        private String[] pkgNames;

        private final SelectedItemsTablePersister persister;

        public SelectedItemsTableModel(SelectedItemsTablePersister persister) {
            this.persister = persister;
            reloadData(persister.read());
        }

        void reloadData(SortedMap<String, Boolean> items) {
            selected = new Boolean[items.size()];
            items.values().toArray(selected);
            if (originalSelected == null) {
                originalSelected = new Boolean[items.size()];
                System.arraycopy(selected, 0, originalSelected, 0, selected.length);
            }
            pkgNames = new String[items.size()];
            items.keySet().toArray(pkgNames);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return pkgNames.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selected[rowIndex];
            } else {
                return pkgNames[rowIndex];
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 0 : "Who is trying to modify second column?"; // NOI18N
            selected[rowIndex] = (Boolean) aValue;
            persister.write(getItemsMap());
            fireTableCellUpdated(rowIndex, 0);
        }

        private SortedMap<String, Boolean> getItemsMap() {
            SortedMap<String, Boolean> itemsMap = new TreeMap<String, Boolean>();
            for (int i = 0; i < pkgNames.length; i++) {
                itemsMap.put(pkgNames[i], selected[i]);
            }
            return itemsMap;
        }

        public boolean isChanged() {
            return !Arrays.asList(selected).equals(Arrays.asList(originalSelected));
        }

    }
}

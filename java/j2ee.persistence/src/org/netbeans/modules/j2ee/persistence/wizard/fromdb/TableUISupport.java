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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class TableUISupport {

    private TableUISupport() {
    }

    public static JList createTableList() {
        return new TableJList();
    }

    public static void connectAvailable(JList availableTablesList, TableClosure tableClosure, FilterAvailable filter) {
        availableTablesList.setModel(new AvailableTablesModel(tableClosure));

        if (!(availableTablesList.getCellRenderer() instanceof AvailableTableRenderer)) {
            availableTablesList.setCellRenderer(new AvailableTableRenderer(filter));
        } else {
            AvailableTableRenderer renderer = (AvailableTableRenderer) availableTablesList.getCellRenderer();
            renderer.updateFilter(filter);
        }
    }

    public static void connectSelected(JList selectedTablesList, TableClosure tableClosure) {
        selectedTablesList.setModel(new SelectedTablesModel(tableClosure));

        if (!(selectedTablesList.getCellRenderer() instanceof SelectedTableRenderer)) {
            selectedTablesList.setCellRenderer(new SelectedTableRenderer());
        }
    }

    public static Set<Table> getSelectedTables(JList list){
        return getSelectedTables(list, false);
    }

    public static Set<Table> getSelectedTables(JList list, boolean enabledOnly) {
        Set<Table> result = new HashSet<>();

        int[] selected = list.getSelectedIndices();
        for (int i = 0; i < selected.length; i++) {
            Table table = (Table)list.getModel().getElementAt(selected[i]);
            if(enabledOnly){
                if(!list.getCellRenderer().getListCellRendererComponent(list, table, selected[i], false, false).isEnabled()) {
                    continue;
                }
            }
            result.add(table);
        }

        return result;
    }

    public static Set<Table> getEnabledTables(JList list) {
        Set<Table> result = new HashSet<>();

        for (int i = 0; i < list.getModel().getSize(); i++) {
            Table table = (Table)list.getModel().getElementAt(i);
            if(!list.getCellRenderer().getListCellRendererComponent(list, table, i, false, false).isEnabled()) {
                continue;
            }
            result.add(table);
        }

        return result;
    }

    public static void connectClassNames(JTable table, SelectedTables selectedTables) {
        table.setModel(new TableClassNamesModel(selectedTables));
        setRenderer(table.getColumnModel().getColumn(0));
        setRenderer(table.getColumnModel().getColumn(1));
    }

    private static void setRenderer(TableColumn column) {
        if (!(column.getCellRenderer() instanceof TableClassNameRenderer)) {
            column.setCellRenderer(new TableClassNameRenderer());
        }
    }

    private abstract static class TableModel extends AbstractListModel {

        @Override
        public abstract Table getElementAt(int index);
    }

    private static final class AvailableTablesModel extends TableModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public AvailableTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        @Override
        public Table getElementAt(int index) {
            return displayTables.get(index);
        }

        @Override
        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getAvailableTables());
            Collections.sort(displayTables);
            fireIntervalRemoved(this, 0, oldSize);
            fireIntervalAdded(this, 0, getSize());
        }
    }

    private static final class SelectedTablesModel extends TableModel implements ChangeListener {

        private final TableClosure tableClosure;

        private List<Table> displayTables;

        public SelectedTablesModel(TableClosure tableClosure) {
            this.tableClosure = tableClosure;
            tableClosure.addChangeListener(this);
            refresh();
        }

        @Override
        public Table getElementAt(int index) {
            return displayTables.get(index);
        }

        @Override
        public int getSize() {
            return displayTables != null ? displayTables.size() : 0;
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            displayTables = new ArrayList<Table>(tableClosure.getSelectedTables());
            Collections.sort(displayTables);
            fireIntervalRemoved(this, 0, oldSize);
            fireIntervalAdded(this, 0, getSize());
        }

        public TableClosure getTableClosure() {
            return tableClosure;
        }
    }

    private static final class AvailableTableRenderer extends DefaultListCellRenderer {
        private FilterAvailable filter;

        public AvailableTableRenderer(FilterAvailable filter){
            this.filter = filter;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DisabledReason disabledReason = null;
            Object displayName = null;

            if (value instanceof Table) {
                Table tableItem = (Table)value;
                disabledReason = tableItem.getDisabledReason();
                if (disabledReason!= null) {
                    displayName = NbBundle.getMessage(TableUISupport.class, "LBL_TableNameWithDisabledReason", tableItem.getName(), disabledReason.getDisplayName());
                } else {
                    if(tableItem.isTable()) {
                        displayName = tableItem.getName();
                    } else {
                        displayName = tableItem.getName() + NbBundle.getMessage(TableUISupport.class, "LBL_DB_VIEW");
                    }
                }
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            boolean needDisable = (disabledReason instanceof Table.NoPrimaryKeyDisabledReason) || (disabledReason instanceof Table.ExistingNotInSourceDisabledReason) || 
                    (filter == FilterAvailable.NEW && (disabledReason instanceof Table.ExistingDisabledReason)) ||
                    (filter == FilterAvailable.UPDATE && (disabledReason==null));
            component.setEnabled(!needDisable);
            component.setToolTipText(disabledReason != null ? disabledReason.getDescription() : null);

            return component;
        }

        void updateFilter (FilterAvailable filter){
            this.filter = filter;
        }

    }

    private static final class SelectedTableRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Table table = null;
            Object displayName = null;
            boolean referenced = false;
            TableClosure tableClosure = null;
            DisabledReason disabledReason = null;
            if (value instanceof Table) {
                table = (Table)value;

                disabledReason = table.getDisabledReason();
                if (disabledReason!= null) {
                    displayName = NbBundle.getMessage(TableUISupport.class, "LBL_TableNameWithDisabledReason", table.getName(), disabledReason.getDisplayName());
                } else {
                    if(((Table)value).isTable()) {
                        displayName = table.getName();
                    } else {
                        displayName = table.getName() + NbBundle.getMessage(TableUISupport.class, "LBL_DB_VIEW");
                    }
                }


                if (list.getModel() instanceof SelectedTablesModel) {
                    SelectedTablesModel model = (SelectedTablesModel)list.getModel();
                    tableClosure = model.getTableClosure();
                    referenced = tableClosure.getReferencedTables().contains(table);
                }
            } else {
                displayName = value;
            }

            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setEnabled(!referenced && !(table.isDisabled()));
            String tooltip = referenced ? getTableTooltip(table, tableClosure) : null;
            if (table.isDisabled()){
                String descr = table.getDisabledReason().getDescription();
                tooltip = tooltip == null ? descr : tooltip.concat("<br>" + descr); //NOI18N
            } 
            component.setToolTipText(tooltip); 

            return component;
        }

        private static String getTableTooltip(Table table, TableClosure tableClosure) {
            List<Table> tables = new ArrayList<>();
            Set<Table> relatedTables;
            String bundleKey;

            if (table.isJoin()) {
                relatedTables = table.getReferencedTables();
                bundleKey = "LBL_RelatedTableJoin"; // NOI18N
            } else {
                relatedTables = table.getReferencedByTables();
                bundleKey = "LBL_RelatedTableRefBy"; // NOI18N
            }
            for (Iterator<Table> i = relatedTables.iterator(); i.hasNext();) {
                Table refTable = i.next();
                if (tableClosure.getSelectedTables().contains(refTable)) {
                    tables.add(refTable);
                }
            }
            if(tables.isEmpty()){ // issue 149542
                return null;
            } else {
                return NbBundle.getMessage(TableUISupport.class, bundleKey, createTableList(tables));
            }
        }

        private static String createTableList(List<Table> tables) {
            assert !tables.isEmpty();

            if (tables.size() == 1) {
                return tables.iterator().next().getName();
            }

            Collections.sort(tables);

            String separator = NbBundle.getMessage(TableUISupport.class, "LBL_TableListSep");
            Iterator<Table> i = tables.iterator();
            StringBuilder builder = new StringBuilder(i.next().getName());
            String lastTable = i.next().getName();
            while (i.hasNext()) {
                builder.append(separator);
                builder.append(lastTable);
                lastTable = i.next().getName();
            }
            return NbBundle.getMessage(TableUISupport.class, "LBL_TableList", builder.toString(), lastTable);
        }
    }

    private static final class TableClassNamesModel extends AbstractTableModel {

        private SelectedTables selectedTables;
        private final List<Table> tables;

        public TableClassNamesModel(SelectedTables selectedTables) {
            this.selectedTables = selectedTables;
            this.tables = selectedTables.getTables();
        }

        SelectedTables getSelectedTables(){
            return selectedTables;
        }

        public Table getTableAt(int rowIndex) {
            return tables.get(rowIndex);
        }

        public boolean isValidClass(Table table) {
            return !selectedTables.hasProblem(table);
        }

        public String getProblemDisplayName(Table table) {
            return selectedTables.getProblemDisplayNameForTable(table);
        }

        @Override
        public int getRowCount() {
            return tables.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return tables.get(rowIndex).getName();

                case 1:
                    Table table = tables.get(rowIndex);
                    return selectedTables.getClassName(table);

                case 2:
                    return selectedTables.getUpdateType(tables.get(rowIndex)).getName();

                default:
                    assert false;
            }

            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Table table = tables.get(rowIndex);
            switch(columnIndex){
                case 1:{
                    selectedTables.setClassName(table, (String)value);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                    return;
                }
                case 2:{
                    selectedTables.setUpdateType(table, (UpdateType)value);
                    return;
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            Table table = tables.get(rowIndex);
            UpdateType ut = selectedTables.getUpdateType(tables.get(rowIndex));
            return !table.isJoin() && (columnIndex == 1 || columnIndex == 2) && (UpdateType.NEW.equals(ut) || columnIndex == 2);
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_DatabaseTable");

                case 1:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_ClassName");

                case 2:
                    return NbBundle.getMessage(TableUISupport.class, "LBL_GenerationType");

                default:
                    assert false;
            }

            return null;
        }
    }

    static final class ClassNamesTable extends JTable{
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 2){
                if (getModel() instanceof TableClassNamesModel) {
                    TableClassNamesModel model = (TableClassNamesModel)getModel();
                    Table table = model.getTableAt(row);
                    DisabledReason dr = table.getDisabledReason();
                    boolean existing = dr instanceof Table.ExistingDisabledReason;
                    if (existing){
                        return new DefaultCellEditor(new JComboBox(new UpdateType[]{UpdateType.UPDATE, UpdateType.RECREATE}));
                    } else {
                        return new DefaultCellEditor(new JComboBox(new UpdateType[]{UpdateType.NEW}));
                    }
                }
            } 
            return super.getCellEditor(row, column);
        }
    }


    private static final class TableClassNameRenderer extends DefaultTableCellRenderer {
        private static Color errorForeground;
        private static Color nonErrorForeground;

        static {
            errorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
            if (errorForeground == null) {
                errorForeground = Color.RED;
            }
            nonErrorForeground = UIManager.getColor("Label.foreground"); // NOI18N
        }

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean joinTable = false;
            boolean validClass = true;
            boolean existentUpdate = false;
            String problemDisplayName = null;

            if (jTable.getModel() instanceof TableClassNamesModel) {
                TableClassNamesModel model = (TableClassNamesModel)jTable.getModel();
                Table table = model.getTableAt(row);
                joinTable = table.isJoin();
                if (column == 1) {
                    existentUpdate = table.getDisabledReason() instanceof Table.ExistingDisabledReason;
                    validClass = model.isValidClass(table);
                    if (!validClass) {
                        problemDisplayName = model.getProblemDisplayName(table);
                    }
                }
            }

            Object realValue = null;
            if (joinTable && column == 1) {
                realValue = NbBundle.getMessage(TableUISupport.class, "LBL_JoinTable");
            } else {
                realValue = value;
            }
            JComponent component = (JComponent)super.getTableCellRendererComponent(jTable, realValue, isSelected, hasFocus, row, column);
            component.setEnabled(!joinTable && !existentUpdate);
            component.setToolTipText(joinTable ? NbBundle.getMessage(TableUISupport.class, "LBL_JoinTableDescription") : problemDisplayName);
            component.setForeground((validClass) ? nonErrorForeground : errorForeground);
           
            return component;
        }
    }

    private static final class TableJList extends JList {

        @Override
        public int getNextMatch(String prefix, int startIndex, Bias bias) {
            ListModel model = getModel();
            if (!(model instanceof TableModel)) {
                return super.getNextMatch(prefix, startIndex, bias);
            }
            TableModel tablesModel = (TableModel)model;
            int max = tablesModel.getSize();
            int increment = (bias == Bias.Forward) ? 1 : -1;
            int index = startIndex;
            prefix = prefix.toUpperCase();
            do {
                Table table = tablesModel.getElementAt(index);
                String tableName = table.getName().toUpperCase();
                if (tableName.startsWith(prefix)) {
                    return index;
                }
                index = (index + increment + max) % max;
            } while (index != startIndex);
            return -1;
        }
    }

    public enum FilterAvailable{ANY,NEW,UPDATE};
}

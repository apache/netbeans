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

package org.netbeans.modules.versioning.util.common;

import java.awt.Color;
import java.io.File;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.SortedTable;
import org.netbeans.modules.versioning.util.TableSorter;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.table.TableCellRenderer;
import org.openide.awt.Mnemonics;

/**
 * {@link #getComponent Table} that displays nodes in the commit dialog.
 * 
 * @author Maros Sandor
 */
public class VCSCommitTable<F extends VCSFileNode> implements AncestorListener, TableModelListener, MouseListener {    
    
    private VCSCommitTableModel<F> tableModel;
    private JTable              table;
    private JComponent          component;
    
    private TableSorter         sorter;
    private String[]            columns;
    private String[]            sortByColumns;
    private Set<File> modifiedFiles = Collections.<File>emptySet();
    private VCSCommitPanel commitPanel;

    private String errroMessage;
    private final boolean editable;
    private final VCSCommitPanelModifier modifier;
        
    public VCSCommitTable(VCSCommitTableModel<F> tableModel) {
        this(tableModel, true);
    }

    public VCSCommitTable (VCSCommitTableModel<F> tableModel, boolean isEditable) {
        this.editable = isEditable;
        this.modifier = tableModel.getCommitModifier();
        init(tableModel);
        this.sortByColumns = new String[] { VCSCommitTableModel.COLUMN_NAME_PATH };
        setSortingStatus();
    }

    private void init(VCSCommitTableModel<F> tableModel) {
        this.tableModel = tableModel;
        tableModel.addTableModelListener(this);
        sorter = new TableSorter(tableModel);
        
        table = new SortedTable(this.sorter);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new CommitStringsCellRenderer());
        table.setDefaultRenderer(Boolean.class, new CheckboxCellRenderer());
        table.setDefaultEditor(Boolean.class, new CheckboxCellEditor());
        table.getTableHeader().setReorderingAllowed(true);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        table.addAncestorListener(this);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        table.getAccessibleContext().setAccessibleName(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_ACCESSIBLE_NAME));
        table.getAccessibleContext().setAccessibleDescription(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_ACCESSIBLE_DESCRIPTION));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
        table.addMouseListener(this);
        setColumns(VCSCommitTableModel.COMMIT_COLUMNS);
    }

    void labelFor(JLabel label) {
        label.setLabelFor(table);
    }
    
    @Override
    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    public boolean containsCommitable() {
        List<F> list = getCommitFiles();
        for(F file : list) {
            if(file.getCommitOptions() != modifier.getExcludedOption()) {
                errroMessage = null;
                return true;
            }
        }
        errroMessage = modifier.getMessage(VCSCommitPanelModifier.BundleMessage.MESSAGE_NO_FILES);
        return false;
    }

    public String getErrorMessage() {
        return errroMessage;
    }
    
    /**
     * Sets sizes of Commit table columns, kind of hardcoded.
     */ 
    private void setDefaultColumnSizes() {
        int width = table.getWidth();
        TableColumnModel columnModel = table.getColumnModel();
        if (columns == null || columnModel == null) return; // unsure when this methed will be called (component realization) 
        if (columnModel.getColumnCount() != columns.length) return; 
        if (columns.length == 4) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];                                
                sorter.setColumnComparator(i, null);                    
                if (col.equals(VCSCommitTableModel.COLUMN_NAME_COMMIT)) {
                    columnModel.getColumn(i).setMinWidth(new JCheckBox().getMinimumSize().width);
                    columnModel.getColumn(i).setPreferredWidth(new JCheckBox().getPreferredSize().width);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_NAME)) {
                    sorter.setColumnComparator(i, new FileNameComparator());
                    columnModel.getColumn(i).setPreferredWidth(width * 30 / 100);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_ACTION)) {
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else {
                    columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
                }                
            }
        } else if (columns.length == 5) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];                                
                sorter.setColumnComparator(i, null);                    
                if (col.equals(VCSCommitTableModel.COLUMN_NAME_COMMIT)) {
                    columnModel.getColumn(i).setMinWidth(new JCheckBox().getMinimumSize().width);
                    columnModel.getColumn(i).setPreferredWidth(new JCheckBox().getPreferredSize().width);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_NAME)) {
                    sorter.setColumnComparator(i, new FileNameComparator());
                    columnModel.getColumn(i).setPreferredWidth(width * 25 / 100);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_STATUS)) {
                    sorter.setColumnComparator(i, new StatusComparator());                    
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_ACTION)) {
                    columnModel.getColumn(i).setPreferredWidth(width * 20 / 100);
                } else {
                    columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
                }                
            }
        } else if (columns.length == 6) {
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i];
                sorter.setColumnComparator(i, null);                
                if (col.equals(VCSCommitTableModel.COLUMN_NAME_COMMIT)) {
                    columnModel.getColumn(i).setMinWidth(new JCheckBox().getMinimumSize().width);
                    columnModel.getColumn(i).setPreferredWidth(new JCheckBox().getPreferredSize().width);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_NAME)) {
                    sorter.setColumnComparator(i, new FileNameComparator());
                    columnModel.getColumn(i).setPreferredWidth(width * 25 / 100);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_STATUS)) {
                    sorter.setColumnComparator(i, new StatusComparator());
                    sorter.setSortingStatus(i, TableSorter.ASCENDING);
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else if (col.equals(VCSCommitTableModel.COLUMN_NAME_ACTION)) {
                    columnModel.getColumn(i).setPreferredWidth(width * 15 / 100);
                } else {
                    columnModel.getColumn(i).setPreferredWidth(width * 30 / 100);
                }
            }
        }
    }

    private void setSortingStatus() {
        for (int i = 0; i < sortByColumns.length; i++) {
            String sortByColumn = sortByColumns[i];        
            for (int j = 0; j < columns.length; j++) {
                String column = columns[j];
                if(column.equals(sortByColumn)) {
                    sorter.setSortingStatus(j, column.equals(sortByColumn) ? TableSorter.ASCENDING : TableSorter.NOT_SORTED);                       
                    break;
                }                    
            }                        
        }        
    }
    
    public TableSorter getSorter() {
        return sorter;
    }
    
    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
    }
    
    void setColumns(String[] cols) {
        if (Arrays.equals(columns, cols)) return;
        columns = cols;
        tableModel.setColumns(cols);
        setDefaultColumnSizes();
    }

    public void setNodes(F[] nodes) {
        tableModel.setNodes(nodes);
    }

    /**
     * @return Map&lt;HgFileNode, CommitOptions>
     */
    public List<F> getCommitFiles() {
        return tableModel.getCommitFiles();
    }

    /**
     * @return table in a scrollpane 
     */
    public JComponent getComponent() {
        return component;
    }

    JTable getTable() {
        return table;
    }
    
    void dataChanged() {
        int idx = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (idx != -1) table.getSelectionModel().addSelectionInterval(idx, idx);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        // change in commit options may alter name rendering (strikethrough)
        table.repaint();
    }

    public void setRootFile(String repositoryPath, String rootLocalPath) {
        tableModel.setRootFile(repositoryPath, rootLocalPath);
    }

    private void showPopup(final MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = table.getSelectedRows();
            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }
        if (col != -1) {
            boolean makeColSelected = true;
            int [] selectedcols = table.getSelectedColumns();
            for (int i = 0; i < selectedcols.length; i++) {
                if (col == selectedcols[i]) {
                    makeColSelected = false;
                    break;
                }
            }
            if (makeColSelected) {
                table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // invoke later so the selection on the table will be set first
                if (table.isShowing()) {
                    JPopupMenu menu = getPopup();
                    menu.show(table, e.getX(), e.getY());
                }
            }
        });
    }

    private void showPopup (Point p) {
        JPopupMenu menu = getPopup();
        menu.show(table, p.x, p.y);
    }
    
    private JPopupMenu getPopup() {

        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;

        boolean containsExcluded = false;
        boolean containsIncluded = false;
        for (int rowIndex : table.getSelectedRows()) {
            if (modifier.getExcludedOption().equals(tableModel.getOption(sorter.modelIndex(rowIndex)))) {
                containsExcluded = true;
            } else {
                containsIncluded = true;
            }
        }
        if (containsExcluded) {
            item = menu.add(new AbstractAction(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_INCLUDE_ACTION_NAME)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setIncluded(true);
                }

                @Override
                public boolean isEnabled() {
                    return editable;
                }
            });
            Mnemonics.setLocalizedText(item, item.getText());
        }
        if (containsIncluded) {
            item = menu.add(new AbstractAction(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_EXCLUDE_ACTION_NAME)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setIncluded(false);
                }

                @Override
                public boolean isEnabled() {
                    return editable;
                }
            });
            Mnemonics.setLocalizedText(item, item.getText());
        }
        item = menu.add(new AbstractAction(NbBundle.getMessage(VCSCommitTable.class, "CTL_CommitTable_DiffAction")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                openDiff();
            }
        });
        Mnemonics.setLocalizedText(item, item.getText());
        item.setEnabled(commitPanel != null);
        return menu;
    }

    private void setIncluded (boolean included) {
        int[] rows = table.getSelectedRows();
        int rowCount = table.getRowCount();
        for (int i = 0; i < rows.length; ++i) {
            rows[i] = sorter.modelIndex(rows[i]);
        }
        tableModel.setIncluded(rows, included);
        // WA for table sorter, keep the selection
        if (rowCount == table.getRowCount()) {
            for (int i = 0; i < rows.length; ++i) {
                table.getSelectionModel().addSelectionInterval(sorter.viewIndex(rows[i]), sorter.viewIndex(rows[i]));
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            openDiff();
        }
    }

    public void setCommitPanel(VCSCommitPanel panel) {
        this.commitPanel = panel;
    }

    public void setModifiedFiles(Set<File> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    VCSCommitPanelModifier getCommitModifier () {
        return modifier;
    }

    private void openDiff () {
        int[] rows = table.getSelectedRows();
        F[] nodes = (F[]) java.lang.reflect.Array.newInstance((Class<F>) tableModel.getNodes().getClass().getComponentType(), rows.length);
        for (int i = 0; i < rows.length; ++i) {
            nodes[i] = tableModel.getNode(sorter.modelIndex(rows[i]));
        }
        commitPanel.openDiff(nodes, tableModel.getCommitFiles());
    }

    private class CommitStringsCellRenderer extends DefaultTableCellRenderer {

        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int col = table.convertColumnIndexToModel(column);
            if (VCSCommitTableModel.COLUMN_NAME_NAME.equals(columns[col])) {
                TableSorter sorter = (TableSorter) table.getModel();
                VCSCommitTableModel model = (VCSCommitTableModel) sorter.getTableModel();
                VCSFileNode node = model.getNode(sorter.modelIndex(row));
                VCSCommitOptions options = model.getOption(sorter.modelIndex(row));
                if (!isSelected) {
                    value = node.getInformation().annotateNameHtml(node.getFile().getName()); 
                }
                if (options == modifier.getExcludedOption()) {
                    value = "<s>" + value + "</s>"; // NOI18N
                }
                if (modifiedFiles.contains(node.getFile())) {
                    value = "<strong>" + value + "</strong>"; //NOI18N
                }
                value = "<html>" + value + "</html>"; //NOI18N
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (VCSCommitTableModel.COLUMN_NAME_PATH.equals(columns[col])) {
                return pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }

    private class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
        private final DefaultTableCellRenderer renderer;

        public CheckboxCellRenderer() {
            renderer = new DefaultTableCellRenderer();
            setToolTipText(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.FILE_TABLE_HEADER_COMMIT_DESC));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected(value == null ? false : (Boolean) value);
            setEnabled(editable);
            Color c = renderer.getTableCellRendererComponent(table, "value", isSelected, hasFocus, row, column).getBackground();
            setBackground(new Color(c.getRGB()));
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    private class CheckboxCellEditor extends DefaultCellEditor {
        
        public CheckboxCellEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JCheckBox checkbox = (JCheckBox) editorComponent;
            checkbox.setSelected(value == null ? false : (Boolean) value);
            checkbox.setHorizontalAlignment(SwingConstants.LEFT);
            checkbox.setEnabled(editable);
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
    
    private class StatusComparator extends VCSFileInformation.ByImportanceComparator {
        public int compare(Object o1, Object o2) {
            Integer row1 = (Integer) o1;
            Integer row2 = (Integer) o2;
            return super.compare(tableModel.getNode(row1.intValue()).getInformation(),
                                 tableModel.getNode(row2.intValue()).getInformation());
        }
    }
    
    private class FileNameComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Integer row1 = (Integer) o1;
            Integer row2 = (Integer) o2;
            return tableModel.getNode(row1.intValue()).getName().compareToIgnoreCase(
                    tableModel.getNode(row2.intValue()).getName());
        }
    }    

}

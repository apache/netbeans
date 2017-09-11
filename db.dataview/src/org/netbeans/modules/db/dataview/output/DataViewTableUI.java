/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.renderer.JRendererCheckBox;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.table.ResultSetCellRenderer;
import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import org.netbeans.modules.db.dataview.util.ColorHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.WindowManager;

/**
 * Renders the current result page
 *
 * @author Ahimanikya Satapathy
 */
final class DataViewTableUI extends ResultSetJXTable {

    private JPopupMenu tablePopupMenu;
    private final DataViewUI dataviewUI;
    private final DataViewActionHandler handler;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private final TableModelListener dataChangedListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            dataviewUI.handleColumnUpdated();
        }
    };

    public DataViewTableUI(DataViewUI dataviewUI, DataViewActionHandler handler, DataView dataView, DataViewPageContext pageContext) {
        this.dataviewUI = dataviewUI;
        this.handler = handler;

        // Make sure sorting can be disabled by taking unsorted into the sort order cycle
        this.setSortOrderCycle(SortOrder.ASCENDING, SortOrder.DESCENDING, SortOrder.UNSORTED);

        TableSelectionListener listener = new TableSelectionListener(this);
        this.getSelectionModel().addListSelectionListener(listener);
        this.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        addKeyListener(createControKeyListener());
        createPopupMenu(handler, dataView, pageContext);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void setModel(TableModel dataModel) {
        if (!(dataModel instanceof DataViewTableUIModel)) {
            throw new IllegalArgumentException("DataViewTableUI only supports"
                    + " instances of DataViewTableUIModel");
        }
        RowFilter<?, ?> oldFilter = getRowFilter();
        if (getModel() != null) {
            getModel().removeTableModelListener(dataChangedListener); // Remove ChangeListener on replace
        }
        super.setModel(dataModel);
        dataModel.addTableModelListener(dataChangedListener); // Add new change listener
        setRowFilter((RowFilter) oldFilter);
        if (dataviewUI != null) {
            dataviewUI.handleColumnUpdated();
        }
    }

    @Override
    public DataViewTableUIModel getModel() {
        return (DataViewTableUIModel) super.getModel();
    }

    @Override
    protected TableModel createDefaultDataModel() {
        return new DataViewTableUIModel(new DBColumn[0]);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        try {
            if (getModel().hasUpdates(
                    convertRowIndexToModel(row),
                    convertColumnIndexToModel(column))) {
                return new UpdatedResultSetCellRenderer();
            }
        } catch (IndexOutOfBoundsException ex) {
            // Swallow it, caused by pack from JXTable - Bug #228753
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    protected KeyListener createControKeyListener() {
        return new Control0KeyListener();
    }

    private static class UpdatedResultSetCellRenderer extends ResultSetCellRenderer {

        static int borderThickness = 1;
        static Color selectedForeground;
        static Color unselectedForeground;
        private final JComponent holder = new JComponent() {};

        static {
            selectedForeground = ColorHelper.getTablecellEditedSelectedForeground();
            unselectedForeground = ColorHelper.getTablecellEditedUnselectedForeground();
        }

        public UpdatedResultSetCellRenderer() {
            holder.setLayout(new BorderLayout());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            assert (table.getModel() instanceof DataViewTableUIModel) : "Assuming usage with DataViewTableUIModel";

            Color color = isSelected ? selectedForeground : unselectedForeground;

            if (c instanceof JRendererCheckBox) {
                holder.removeAll();
                holder.setBorder(new LineBorder(color, borderThickness));
                holder.add(c);
                return holder;
            } else {
                c.setForeground(color);
            }
            return c;
        }
    }

    private class Control0KeyListener implements KeyListener {

        public Control0KeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE) {
                TableCellEditor editor = getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
                handler.deleteRecordActionPerformed();
                e.consume();
            } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (row == -1) {
                    return;
                }

                setCellToNull(row, col);
                setRowSelectionInterval(row, row);
                e.consume();
            } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_1) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (row == -1) {
                    return;
                }
                setCellToDefault(row, col);
                setRowSelectionInterval(row, row);
                e.consume();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private void setCellToNull(int row, int col) {
        int modelColumn = convertColumnIndexToModel(col);
        DBColumn dbcol = getModel().getColumn(modelColumn);
        if (dbcol.isGenerated() || !dbcol.isNullable()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            setValueAt(null, row, col);
        }
    }

    private void setCellToDefault(int row, int col) {
        int modelColumn = convertColumnIndexToModel(col);
        DBColumn dbcol = getModel().getColumn(modelColumn);
        if (dbcol.isGenerated() || !dbcol.hasDefault()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            setValueAt(SQLConstant.DEFAULT, row, col);
        }
        setRowSelectionInterval(row, row);
    }

    private class TableSelectionListener implements ListSelectionListener {

        JTable table;

        TableSelectionListener(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (dataviewUI == null) {
                return;
            }

            if (e.getSource() == table.getSelectionModel()
                    && table.getRowSelectionAllowed()) {
                boolean rowSelected = table.getSelectedRows().length > 0;
                if (rowSelected && getModel().isEditable()) {
                    dataviewUI.enableDeleteBtn(!dataviewUI.isDirty());
                } else {
                    dataviewUI.enableDeleteBtn(false);
                }
            }
        }
    }

    private void createPopupMenu(final DataViewActionHandler handler, final DataView dataView, final DataViewPageContext pageContext) {
        // content popup menu on table with results
        tablePopupMenu = new JPopupMenu();
        final JMenuItem miInsertAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_insert"));
        miInsertAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.insertActionPerformed();
            }
        });
        tablePopupMenu.add(miInsertAction);

        final JMenuItem miDeleteAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_deleterow"));
        miDeleteAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.deleteRecordActionPerformed();
            }
        });
        tablePopupMenu.add(miDeleteAction);

        final JMenuItem miCommitAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_commit"));
        miCommitAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.commitActionPerformed(true);
            }
        });
        tablePopupMenu.add(miCommitAction);

        final JMenuItem miCancelEdits = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_cancel_edits"));
        miCancelEdits.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.cancelEditPerformed(true);
            }
        });
        tablePopupMenu.add(miCancelEdits);

        final JMenuItem miTruncateRecord = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_truncate_table"));
        miTruncateRecord.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.truncateActionPerformed();
            }
        });
        tablePopupMenu.add(miTruncateRecord);
        tablePopupMenu.addSeparator();

        final JMenuItem miCopyValue = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_cell_value"));
        miCopyValue.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Object o = getValueAt(selectedRow, selectedColumn);
                    // Limit 1 MB/1 Million Characters.
                    String output = convertToClipboardString(o, 1024 * 1024);

                    ExClipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
                    StringSelection strSel = new StringSelection(output);
                    clipboard.setContents(strSel, strSel);
                } catch (ArrayIndexOutOfBoundsException exc) {
                }
            }
        });
        tablePopupMenu.add(miCopyValue);

        final JMenuItem miCopyRowValues = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_row_value"));
        miCopyRowValues.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                copyRowValues(false);
            }
        });
        tablePopupMenu.add(miCopyRowValues);

        final JMenuItem miCopyRowValuesH = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_copy_row_header"));
        miCopyRowValuesH.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                copyRowValues(true);
            }
        });
        tablePopupMenu.add(miCopyRowValuesH);
        tablePopupMenu.addSeparator();

        final JMenuItem miCreateSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_create_sql"));
        miCreateSQLScript.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DBTable table = pageContext.getTableMetaData().getTable(0);
                    String createSQL = dataView.getSQLStatementGenerator().generateCreateStatement(table);
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    dialog.setText(createSQL + ";\n"); // NOI18N
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        tablePopupMenu.add(miCreateSQLScript);

        final JMenuItem miInsertSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_insert_sql"));
        miInsertSQLScript.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int[] rows = getSelectedRows();
                    String insertSQL = "";
                    for (int j = 0; j < rows.length; j++) {
                        int modelIndex = convertRowIndexToModel(rows[j]);
                        Object[] insertRow = getModel().getRowData(modelIndex);
                        // @todo make table configurable
                        DBTable table = pageContext.getTableMetaData().getTable(0);
                        String sql = dataView.getSQLStatementGenerator()
                                .generateRawInsertStatement(table, insertRow);
                        insertSQL += sql + ";\n"; // NOI18N
                    }
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    dialog.setText(insertSQL);
                    dialog.setVisible(true);
                } catch (DBException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        tablePopupMenu.add(miInsertSQLScript);

        final JMenuItem miDeleteSQLScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_delete_sql"));
        miDeleteSQLScript.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = getSelectedRows();
                String rawDeleteStmt = "";
                for (int j = 0; j < rows.length; j++) {
                    SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                    int modelIndex = convertRowIndexToModel(rows[j]);
                    // @todo make table configurable
                    DBTable table = pageContext.getTableMetaData().getTable(0);
                    final String deleteStmt = generator.generateDeleteStatement(table, modelIndex, getModel());
                    rawDeleteStmt += deleteStmt + ";\n"; // NOI18N
                }
                ShowSQLDialog dialog = new ShowSQLDialog();
                dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                dialog.setText(rawDeleteStmt);
                dialog.setVisible(true);
            }
        });
        tablePopupMenu.add(miDeleteSQLScript);

        final JMenuItem miUpdateScript = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_show_update_sql"));
        miUpdateScript.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String rawUpdateStmt = "";
                SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
                // @todo make table configurable
                DBTable table = pageContext.getTableMetaData().getTable(0);

                try {
                    for (Integer row : getModel().getUpdateKeys()) {
                        Map<Integer, Object> changedData = getModel().getChangedData(row);
                        rawUpdateStmt += generator.generateUpdateStatement(table, row, changedData, getModel()) + ";\n"; // NOI18N
                    }
                    ShowSQLDialog dialog = new ShowSQLDialog();
                    dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    dialog.setText(rawUpdateStmt);
                    dialog.setVisible(true);
                } catch (DBException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        tablePopupMenu.add(miUpdateScript);

        tablePopupMenu.addSeparator();

        JMenuItem printTable = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_print_data"));

        printTable.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Logger mLogger = Logger.getLogger(DataViewTableUI.class.getName());
                try {
                    if (!print()) {
                        mLogger.log(Level.INFO, NbBundle.getMessage(DataViewTableUI.class, "MSG_cancel_printing"));
                    }
                } catch (java.awt.print.PrinterException ex) {
                    mLogger.log(Level.INFO, NbBundle.getMessage(DataViewTableUI.class, "MSG_failure_to_print" + ex.getMessage()));
                }
            }
        });
        tablePopupMenu.add(printTable);

        JMenuItem miRefreshAction = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_refresh"));
        miRefreshAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handler.refreshActionPerformed();
            }
        });
        tablePopupMenu.add(miRefreshAction);

        tablePopupMenu.addSeparator();

        final JMenuItem miSetNull = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_set_cell_to_null"));
        miSetNull.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int col : getSelectedColumns()) {
                    int modelColumn = convertColumnIndexToModel(col);
                    DBColumn dbcol = getModel().getColumn(modelColumn);
                    for (int row : getSelectedRows()) {
                        if ((!dbcol.isGenerated()) && dbcol.isNullable()) {
                            setCellToNull(row, col);
                        }
                    }
                }
            }
        });
        tablePopupMenu.add(miSetNull);

        final JMenuItem miSetDefault = new JMenuItem(NbBundle.getMessage(DataViewTableUI.class, "TOOLTIP_set_cell_to_default"));
        miSetDefault.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int col : getSelectedColumns()) {
                    int modelColumn = convertColumnIndexToModel(col);
                    DBColumn dbcol = getModel().getColumn(modelColumn);
                    for (int row : getSelectedRows()) {
                        if ((!dbcol.isGenerated()) && dbcol.hasDefault()) {
                            setCellToDefault(row, col);
                        }
                    }
                }
            }
        });
        tablePopupMenu.add(miSetDefault);

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    selectedRow = rowAtPoint(e.getPoint());
                    selectedColumn = columnAtPoint(e.getPoint());
                    boolean inSelection = false;

                    int[] rows = getSelectedRows();
                    for (int a = 0; a < rows.length; a++) {
                        if (rows[a] == selectedRow) {
                            inSelection = true;
                            break;
                        }
                    }
                    if (!getRowSelectionAllowed()) {
                        inSelection = false;
                        int[] columns = getSelectedColumns();
                        for (int a = 0; a < columns.length; a++) {
                            if (columns[a] == selectedColumn) {
                                inSelection = true;
                                break;
                            }
                        }
                    }
                    if (!inSelection) {
                        changeSelection(selectedRow, selectedColumn, false, false);
                    }

                    boolean commitEnabled = dataviewUI.isCommitEnabled();
                    boolean modelEditable = getModel().isEditable();
                    boolean rowsSelected = getSelectedRows().length > 0;
                    boolean cellUnderCursor = selectedColumn >= 0 && selectedRow >= 0;

                    miCommitAction.setEnabled(commitEnabled);
                    miCancelEdits.setEnabled(commitEnabled);
                    miUpdateScript.setEnabled(commitEnabled);

                    miInsertAction.setEnabled(modelEditable);
                    miTruncateRecord.setEnabled(modelEditable);
                    miCreateSQLScript.setEnabled(modelEditable);

                    miInsertSQLScript.setEnabled(modelEditable && rowsSelected);
                    miDeleteSQLScript.setEnabled(modelEditable && rowsSelected);
                    miDeleteAction.setEnabled(modelEditable && rowsSelected);

                    boolean enableSetToNull = false;
                    boolean enableSetToDefault = false;

                    if (modelEditable && rowsSelected) {
                        for (int col : getSelectedColumns()) {
                            int modelColumn = convertColumnIndexToModel(col);
                            DBColumn dbcol = getModel().getColumn(modelColumn);
                            if (!dbcol.isGenerated()) {
                                if (dbcol.isNullable()) {
                                    enableSetToNull = true;
                                }
                                if (dbcol.hasDefault()) {
                                    enableSetToDefault = true;
                                }
                            }
                        }
                    }

                    miSetDefault.setEnabled(enableSetToDefault);
                    miSetNull.setEnabled(enableSetToNull);

                    // Enable copy if one or more rows are selected
                    miCopyRowValues.setEnabled(rowsSelected);
                    miCopyRowValuesH.setEnabled(rowsSelected);

                    miCopyValue.setEnabled(cellUnderCursor);

                    tablePopupMenu.show(DataViewTableUI.this, e.getX(), e.getY());
                }
            }
        });
    }
}

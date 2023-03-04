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
package org.netbeans.modules.db.dataview.table;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.*;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.table.celleditor.*;
import org.netbeans.modules.db.dataview.util.BinaryToStringConverter;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.DateType;
import org.netbeans.modules.db.dataview.util.TimeType;
import org.netbeans.modules.db.dataview.util.TimestampType;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/**
 * A better-looking table than JTable, implements JXTable and a decorator to draw empty rows 
 *
 * @author Ahimanikya Satapathy
 */
public class ResultSetJXTable extends JXTableDecorator {
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE."; // NOI18N
    private static final Logger mLogger = Logger.getLogger(ResultSetJXTable.class.getName());
    private static final int MAX_COLUMN_WIDTH = 25;
    private static final DateFormat timeFormat = new SimpleDateFormat(TimeType.DEFAULT_FOMAT_PATTERN);
    private static final DateFormat dateFormat = new SimpleDateFormat(DateType.DEFAULT_FOMAT_PATTERN);
    private static final DateFormat timestampFormat = new SimpleDateFormat(TimestampType.DEFAULT_FORMAT_PATTERN);
    
    private final int multiplier;
    
    private final StringFallbackRowSorter sorter = new StringFallbackRowSorter(null);

    private Set<Integer> visibleColumns = new HashSet<>();
    
    // If structure changes, enforce relayout
    private final TableModelListener dataExchangedListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            if(e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                updateHeader();
            }
        }
    };

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ResultSetJXTable() {
        this.setRowSorter(sorter);
        this.setAutoCreateColumnsFromModel(false);
        this.setTransferHandler(new TableTransferHandler());

        setShowGrid(true);
        setGridColor(GRID_COLOR);

        getTableHeader().setReorderingAllowed(false);
        setFillsViewportHeight(true);

        setDefaultCellRenderers();
        setDefaultCellEditors();

        multiplier = getFontMetrics(getFont()).stringWidth(data) / data.length() + 4;
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        this.setModel(createDefaultDataModel());

        getActionMap().put("selectNextColumnCell", new EditingAwareAction(getActionMap().get("selectNextColumnCell")));
        getActionMap().put("selectPreviousColumnCell", new EditingAwareAction(getActionMap().get("selectPreviousColumnCell")));
        getActionMap().put("selectNextRowCell", new EditingAwareAction(getActionMap().get("selectNextRowCell")));
        getActionMap().put("selectNextPreviousCell", new EditingAwareAction(getActionMap().get("selectPreviousRowCell")));

        setSurrendersFocusOnKeystroke(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeaderImpl(columnModel);
    }

    @Override
    protected TableModel createDefaultDataModel() {
        return new ResultSetTableModel(new DBColumn[0]);
    }

    @Override
    public void setModel(TableModel dataModel) {
        if(! (dataModel instanceof ResultSetTableModel)) {
            throw new IllegalArgumentException(
                    "TableModel for ResultSetJXTable must be an "  // NOI18N
                    + " instance of ResultSetTableModel"           // NOI18N
            );
        }
        if(getModel() != null) {
            getModel().removeTableModelListener(dataExchangedListener);
        }
        if(sorter != null) {
            sorter.setModel((ResultSetTableModel) dataModel);
        }
        super.setModel(dataModel);
        if(visibleColumns != null) {
            visibleColumns.clear();
            for(int i = 0; i < dataModel.getColumnCount(); i++) {
                visibleColumns.add(i);
            }
        }
        updateHeader();
        dataModel.addTableModelListener(dataExchangedListener);
    }

    public Set<Integer> getVisibleColumns() {
        return new HashSet<>(visibleColumns);
    }

    public void setVisibleColumns(Set<Integer> visibleColumns) {
        this.visibleColumns.addAll(visibleColumns);
        this.visibleColumns.retainAll(visibleColumns);
        updateHeader();
    }

    @Override
    public ResultSetTableModel getModel() {
        return (ResultSetTableModel) super.getModel();
    }

    @SuppressWarnings("deprecation")
    protected void setDefaultCellRenderers() {
        setDefaultRenderer(Object.class, new ResultSetCellRenderer());
        setDefaultRenderer(String.class, new ResultSetCellRenderer());
        setDefaultRenderer(Number.class, new ResultSetCellRenderer());
        setDefaultRenderer(Boolean.class, new ResultSetCellRenderer());
        setDefaultRenderer(java.sql.Date.class, new ResultSetCellRenderer(ResultSetCellRenderer.Date_TO_STRING));
        setDefaultRenderer(java.sql.Time.class, new ResultSetCellRenderer(ResultSetCellRenderer.TIME_TO_STRING));
        setDefaultRenderer(java.sql.Timestamp.class, new ResultSetCellRenderer(ResultSetCellRenderer.DATETIME_TO_STRING));
        setDefaultRenderer(java.util.Date.class, new ResultSetCellRenderer(ResultSetCellRenderer.DATETIME_TO_STRING));
    }

    protected void setDefaultCellEditors() {

        KeyListener kl = createControKeyListener();
        JTextField txtFld = new JTextField();
        txtFld.addKeyListener(kl);

        setDefaultEditor(Object.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(String.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(java.sql.Time.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(Blob.class, new BlobFieldTableCellEditor());
        setDefaultEditor(Clob.class, new ClobFieldTableCellEditor());
        
        JTextField numFld = new JTextField();
        txtFld.addKeyListener(kl);
        setDefaultEditor(Number.class, new NumberFieldEditor(numFld));

        JCheckBox b = new JCheckBox();
        b.addKeyListener(kl);
        setDefaultEditor(Boolean.class, new BooleanTableCellEditor(b));

        try {
            DateTimePickerCellEditor dateEditor = new DateTimePickerCellEditor(new SimpleDateFormat (DateType.DEFAULT_FOMAT_PATTERN));
            setDefaultEditor(java.sql.Date.class, dateEditor);
        } catch (NullPointerException npe) {
            mLogger.log(Level.WARNING, "While creating DatePickerCellEditor was thrown " + npe, npe);
        }

        try{
            DateTimePickerCellEditor dateTimeEditor = new DateTimePickerCellEditor(new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN));
            dateTimeEditor.addKeyListener(kl);
            setDefaultEditor(Timestamp.class, dateTimeEditor);
            setDefaultEditor(java.util.Date.class, dateTimeEditor);
        } catch (NullPointerException npe) {
            mLogger.log(Level.WARNING, "While creating DateTimePickerCellEditor was thrown " + npe, npe);
        }
    }

    protected KeyListener createControKeyListener() {
        return new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }
        };
    }

    protected void updateHeader() {
        TableColumnModel dtcm = createDefaultColumnModel();

        DBColumn[] columns = getModel().getColumns();

        List<Integer> columnWidthList = getColumnWidthList(columns);

        for (int i = 0; i < columns.length; i++) {
            if(! (visibleColumns.isEmpty() || visibleColumns.contains(i))) {
                continue;
            }
            
            TableColumn tc = new TableColumn(i);
            tc.setPreferredWidth(columnWidthList.get(i));

            DBColumn col = columns[i];
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");                                    //NOI18N
            if (col.getDisplayName() != null) {
                sb.append(DataViewUtils.escapeHTML(col.getDisplayName()));
            }
            sb.append("</html>");                                  // NOI18N
            tc.setHeaderValue(sb.toString());
            tc.setIdentifier(col.getDisplayName() == null
                    ? "COL_" + i : col.getDisplayName());           //NOI18N

            dtcm.addColumn(tc);
        }

        setColumnModel(dtcm);
    }

    private List<Integer> getColumnWidthList(DBColumn[] columns) {
        List<Integer> result = new ArrayList<>();

        for (DBColumn col : columns) {
            int fieldWidth = col.getDisplaySize();
            int labelWidth = col.getDisplayName().length();
            int colWidth = Math.max(fieldWidth, labelWidth) * multiplier;
            if (colWidth < 5) {
                colWidth = 15 * multiplier;
            }
            if (colWidth > MAX_COLUMN_WIDTH * multiplier) {
                colWidth = MAX_COLUMN_WIDTH * multiplier;
            }
            result.add(colWidth);
        }
        return result;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (getCellEditor(row, column) instanceof AlwaysEnable) {
            return true;
        }
        try {
            if (getModel() != null) {
                int modelRow = convertRowIndexToModel(row);
                int modelColumn = convertColumnIndexToModel(column);
                return getModel().isCellEditable(modelRow, modelColumn);
            }
        } catch (IndexOutOfBoundsException ex) {
            // Swallow it silently - its unclear under which circumstances
            // the problem happens, but in case an illegal row/column combination
            // is requested its saver/saner to just mark cell as not editable
        }
        return false;
    }
    
    /**
     * Quote string for use in TSV (tab-separated values file
     *
     * Assumptions: column separator is \t and row separator is \n
     */
    protected String quoteIfNecessary(String value) {
        if (value == null || value.isEmpty()) {
            return "\"\""; //NOI18N
        } else if (value.contains("\t") || value.contains("\n") //NOI18N
                || value.contains("\"")) { //NOI18N
            return "\"" + value.replace("\"", "\"\"") + "\""; //NOI18N
        } else {
            return value;
        }
    }

    /**
     * Convert object to string representation
     *
     * @param o object to convert
     * @param limitSize in case of CLOBs and BLOBs limit to limitSize
     * bytes/chars
     * @return string representation of o
     */
    protected String convertToClipboardString(Object o, int limitSize) {
        if (o instanceof Blob) {
            Blob b = (Blob) o;
            try {
                if (b.length() <= limitSize) {
                    return BinaryToStringConverter.convertToString(
                            b.getBytes(1, (int) b.length()), 16, false);
                }
            } catch (SQLException ex) {
            }
        } else if (o instanceof Clob) {
            Clob c = (Clob) o;
            try {
                if (c.length() <= limitSize) {
                    return c.getSubString(1, (int) c.length());
                }
            } catch (SQLException ex) {
            }
        } else if (o instanceof java.sql.Time) {
            synchronized(timeFormat) {
                return timeFormat.format((java.util.Date) o);
            }
        } else if (o instanceof java.sql.Date) {
            synchronized(dateFormat) {
                return dateFormat.format((java.util.Date) o);
            }
        } else if (o instanceof java.util.Date) {
            synchronized(timestampFormat) {
                return timestampFormat.format((java.util.Date) o);
            }
        } else if (o == null) {
            return "";  //NOI18N
        }
        return o.toString();
    }

    /**
     * Create TSV (tab-separated values) string from row data
     *
     * @param withHeader include column headers?
     * @return Transferable for clipboard transfer
     */
    private StringSelection createTransferableTSV(boolean withHeader) {
        try {
            int[] rows = getSelectedRows();
            int[] columns;
            if (getRowSelectionAllowed()) {
                columns = new int[getColumnCount()];
                for (int a = 0; a < columns.length; a++) {
                    columns[a] = a;
                }
            } else {
                columns = getSelectedColumns();
            }
            if (rows != null && columns != null) {
                StringBuilder output = new StringBuilder();

                if (withHeader) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N

                        }
                        Object o = getColumnModel().getColumn(column).
                                getIdentifier();
                        String s = o != null ? o.toString() : "";
                        output.append(quoteIfNecessary(s));
                    }
                    output.append('\n'); //NOI18N

                }

                for (int row = 0; row < rows.length; row++) {
                    for (int column = 0; column < columns.length; column++) {
                        if (column > 0) {
                            output.append('\t'); //NOI18N

                        }
                        Object o = getValueAt(rows[row], columns[column]);
                        // Limit 1 MB/1 Million Characters.
                        String s = convertToClipboardString(o, 1024 * 1024);
                        output.append(quoteIfNecessary(s));

                    }
                    output.append('\n'); //NOI18N

                }
                return new StringSelection(output.toString());
            }
            return null;
        } catch (ArrayIndexOutOfBoundsException exc) {
            Exceptions.printStackTrace(exc);
            return null;
        }
    }

    protected void copyRowValues(boolean withHeader) {
        ExClipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
        StringSelection selection = createTransferableTSV(withHeader);
        clipboard.setContents(selection, selection);
    }

    // This is mainly used for set Tooltip for column headers
    private class JTableHeaderImpl extends JTableHeader {

        public JTableHeaderImpl(TableColumnModel cm) {
            super(cm);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            return getColumnToolTipText(e);
        }

        protected String getColumnToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            try {
                int realIndex = columnModel.getColumn(index).getModelIndex();
                ResultSetTableModel tm = getModel();
                if (tm != null) {
                    return tm.getColumnTooltip(realIndex);
                } else {
                    return "";
                }
            } catch (ArrayIndexOutOfBoundsException aio) {
                return null;
            }
        }
    }

    private class TableTransferHandler extends TransferHandler
            implements UIResource {

        /**
         * Map Transferable to createTransferableTSV from ResultSetJXTable
         *
         * This is needed so that CTRL-C Action of JTable gets the same
         * treatment as the transfer via the copy Methods of DataTableUI
         */
        @Override
        protected Transferable createTransferable(JComponent c) {
            return createTransferableTSV(false);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    private class EditingAwareAction extends AbstractAction {

        private final Action delegate;

        public EditingAwareAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean editing = isEditing();
            delegate.actionPerformed(e);
            if (editing) {
                editCellAt(getSelectedRow(), getSelectedColumn());
            }
        }
    }

}

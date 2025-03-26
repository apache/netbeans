/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.swing.etable;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.openide.util.ImageUtilities;

/**
 * Extended JTable (ETable) adds these features to JTable:
 * <UL>
 *     <LI> The notion of fully editable (non-editable) table. </LI>
 *     <LI> <strong>Sorting</strong> the rows of the table by clicking the header.
 *          Shift-Click allows to use more columns for the sort. The sort is
 *          based on the values implementing Comparable interface. </LI>
 *     <LI> Automatic <strong>column width</strong> after init or after
 *          the model is changed (or triggered by "Ctrl-+" shortcut). 
 *          Automatic resize the column after double-click
 *          in the header column divider area. </LI>
 *     <LI> <strong>Persistence</strong> of the user customized settings via
 *          methods readSettings and writeSettings.
 *     <LI> <strong>Quick-Filter</strong> features allowing to show only
 *          certain rows from the model (see setQuickFilter()). </LI>
 * </UL>
 * 
 * @author David Strupl
 */
public class ETable extends JTable {
    
    private static final Logger LOG = Logger.getLogger(ETable.class.getName());
    
    /** Property that is fired when calling {@link #setQuickFilter(int, java.lang.Object)} or
     * {@link #unsetQuickFilter()}. 
     * @since 1.13
     */
    public static final String PROP_QUICK_FILTER = "quickFilter";
    
    
    /** Action key for up/down focus action */
    private static final String ACTION_FOCUS_NEXT = "focusNext"; //NOI18N

    /** Possible value for editing property */
    private static final int FULLY_EDITABLE = 1;
    /** Possible value for editing property */
    private static final int FULLY_NONEDITABLE = 2;
    /** Possible value for editing property */
    private static final int DEFAULT = 3;

    /** Key for storing the currently searched column's index. */
    private static final String SEARCH_COLUMN = "SearchColumn";
    
    // icon of column button
    private static final String DEFAULT_COLUMNS_ICON = "org/netbeans/swing/etable/columns.gif"; // NOI18N
    
    /**
     * Property allowing to make the table FULLY_NONEDITABLE and
     * FULLY_EDITABLE.
     */
    private int editing = DEFAULT;
    
    private boolean sortable = true;    // ETable is sortable by default.
    
    /** 
     * Array with size exactly same as the number of rows in the data model
     * or null. If it is not null the row originally at index i will be
     * displayed on index sortingPermutation[i].
     */
    protected transient int [] sortingPermutation;
    
    /** Inverse of the above */
    protected transient int [] inverseSortingPermutation;
    
    /**
     *
     */
    private transient int filteredRowCount;
    
    /**
     *
     */
    private Object quickFilterObject;
    
    /**
     *
     */
    private int quickFilterColumn = -1;

    // Search text field related variables:
    /** */
    private String maxPrefix;
    /** */
    int SEARCH_FIELD_PREFERRED_SIZE = 160;
    /** */
    int SEARCH_FIELD_SPACE = 3;
    /** */
    private final JTextField searchTextField = new SearchTextField();
    /** */
    private final int heightOfTextField = searchTextField.getPreferredSize().height;
    
    /** */
    private JPanel searchPanel = null;
    
    /** */
    private JComboBox searchCombo = null;
    
    /** */
    private ETableColumn searchColumn = null;
    
    /**
     * This text can be customized using {@link #setSelectVisibleColumnsLabel(java.lang.String)} method.
     */
    private String selectVisibleColumnsLabel =
            java.util.ResourceBundle.getBundle("org/netbeans/swing/etable/Bundle").getString("LBL_SelectVisibleColumns");

    private boolean inEditRequest = false;
    private boolean inRemoveRequest=false;
    
    private static String COMPUTING_TOOLTIP = "ComputingTooltip";

    /**
     * Default formatting strings for the Quick Filter feature.
     * Can be customized by setQuickFilterFormatStrings(...) method.
     */
    private String[] quickFilterFormatStrings = new String [] {
        "{0} == {1}", "{0} <> {1}", "{0} > {1}", 
        "{0} < {1}", "{0} >= {1}", "{0} <= {1}",
        java.util.ResourceBundle.getBundle("org/netbeans/swing/etable/Bundle").getString("LBL_NoFilter")
    };
    
    /**
     * Listener reacting to the user clicks on the header.
     */
    private MouseListener headerMouseListener = new HeaderMouseListener();

    /**
     * Listener reacting to the user clicks on the table invoking the
     * column selection dialog.
     */
    private MouseListener columnSelectionMouseListener = new ColumnSelectionMouseListener();
    
    /**
     * Allows to supply alternative implementation of the column
     * selection functionality in ETable.
     */
    private TableColumnSelector columnSelector;
    
    /**
     * Allows to supply alternative implementation of the column
     * selection functionality globally for all instances.
     */
    private static TableColumnSelector defaultColumnSelector;
    
    private final Object columnSelectionOnMouseClickLock = new Object();
    private ColumnSelection[] columnSelectionOnMouseClick = new ColumnSelection[] {
        ColumnSelection.NO_SELECTION,           // no button
        ColumnSelection.DIALOG,                 // dialog on left-click
        ColumnSelection.NO_SELECTION,           // no action on middle button
        ColumnSelection.POPUP,                  // popup on right-click
    };
    
    private boolean columnHidingAllowed = true;
    
    /**
     * The visible column selection methods.
     * @since 1.17
     */
    public enum ColumnSelection {
        NO_SELECTION, POPUP, DIALOG
    }
    
    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     *
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public ETable() {
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, a default column model,
     * and a default selection model.
     *
     * @param dm        the data model for the table
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public ETable(TableModel dm) {
        super(dm);
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code>
     * as the column model, and a default selection model.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @see #createDefaultSelectionModel
     */
    public ETable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code> as the
     * column model, and <code>sm</code> as the selection model.
     * If any of the parameters are <code>null</code> this method
     * will initialize the table with the corresponding default model.
     * The <code>autoCreateColumnsFromModel</code> flag is set to false
     * if <code>cm</code> is non-null, otherwise it is set to true
     * and the column model is populated with suitable
     * <code>TableColumns</code> for the columns in <code>dm</code>.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @param sm        the row selection model for the table
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public ETable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells using
     * <code>DefaultTableModel</code>.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           the number of rows the table holds
     * @param numColumns        the number of columns the table holds
     * @see javax.swing.table.DefaultTableModel
     */
    public ETable(int numRows, int numColumns) {
        super(numRows, numColumns);
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> to display the values in the
     * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>,
     * with column names, <code>columnNames</code>.  The
     * <code>Vectors</code> contained in <code>rowData</code>
     * should contain the values for that row. In other words,
     * the value of the cell at row 1, column 5 can be obtained
     * with the following code:
     * <pre>((Vector)rowData.elementAt(1)).elementAt(5);</pre>
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public ETable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        updateMouseListener();
    }
    
    /**
     * Constructs a <code>JTable</code> to display the values in the two dimensional array,
     * <code>rowData</code>, with column names, <code>columnNames</code>.
     * <code>rowData</code> is an array of rows, so the value of the cell at row 1,
     * column 5 can be obtained with the following code:
     * <pre> rowData[1][5]; </pre>
     * <p>
     * All rows must be of the same length as <code>columnNames</code>.
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public ETable(final Object[][] rowData, final Object[] columnNames) {
        super(rowData, columnNames);
        updateMouseListener();
    }

    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
        return new ETableSelectionModel();
    }

    /**
     * Returns true if the cell at <code>row</code> and <code>column</code>
     * is editable.  Otherwise, invoking <code>setValueAt</code> on the cell
     * will have no effect.
     * <p>
     * Returns true always if the <code>ETable</code> is fully editable.
     * <p>
     * Returns false always if the <code>ETable</code> is fully non-editable.
     *
     * @param   row      the row whose value is to be queried
     * @param   column   the column whose value is to be queried
     * @return  true if the cell is editable
     * @see #setValueAt
     * @see #setFullyEditable
     * @see #setFullyNonEditable
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        if(editing == FULLY_EDITABLE) {
            return true;
        }
        if(editing == FULLY_NONEDITABLE) {
            return false;
        }
        return super.isCellEditable(row, column);
    }

    /*
     * Overridden to call convertRowIndexToModel(...).
     * @see javax.swing.JTable#getCellRenderer(int, int)
     *
     * NOT NECESSARY - JTable does not use the "row" argument.
     *
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        int modelRow = convertRowIndexToModel(row);
        return super.getCellRenderer(modelRow, column);
    }
     */

    /*
     * Overridden to call convertRowIndexToModel(...).
     * @see javax.swing.JTable#getCellEditor(int, int)
     *
     * NOT NECESSARY - JTable does not use the "row" argument.
     *
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        int modelRow = convertRowIndexToModel(row);
        return super.getCellEditor(modelRow, column);
    }
     */

    /**
     * Sets all the cells in the <code>ETable</code> to be editable if
     * <code>fullyEditable</code> is true.
     * if <code>fullyEditable</code> is false, sets the table cells into
     * their default state as in JTable.
     *
     * @param   fullyEditable   true if the table is meant to be fully editable.
     *                          false if the table is meant to take the defalut
     *                          state for editing.
     * @see #isFullyEditable()
     */
    public void setFullyEditable(boolean fullyEditable) {
        if (fullyEditable) {
            editing = FULLY_EDITABLE;
            if(!getShowHorizontalLines()) {
                setShowHorizontalLines(true);
            }
            Color colorBorderAllEditable = UIManager.getColor("Table.borderAllEditable");
            Border border = null;
            if (colorBorderAllEditable != null) {
                border = BorderFactory.createLineBorder(colorBorderAllEditable);
            } else {
                border = BorderFactory.createLineBorder(Color.GRAY);
            }
            Border filler = BorderFactory.createLineBorder(getBackground());
            CompoundBorder compound = new CompoundBorder(border, filler);
            setBorder(new CompoundBorder(compound, border));
        } else {
            editing = DEFAULT;
            setBorder( null );
        }
        Color c = UIManager.getColor("Table.defaultGrid");
        if (c != null) {
            setGridColor(c);
        }
        if (isFullyNonEditable()) {
            setupSearch();
        }
    }
    
    /**
     * Sets all the cells in the <code>ETable</code> to be non-editable if
     * <code>fullyNonEditable</code> is true.
     * If <code>fullyNonEditable</code> is false, sets the table cells into
     * their default state as in <code>JTable</code>.
     *
     * @param fullyNonEditable  true if the table is meant to be fully non-editable.
     *                          false if the table is meant to take the defalut
     *                          state for editing.
     * @see #isFullyNonEditable
     */
    public void setFullyNonEditable(boolean fullyNonEditable) {
        if (fullyNonEditable) {
            editing = FULLY_NONEDITABLE;
            if(getShowHorizontalLines())
                setShowHorizontalLines(false);
            Color lineBorderColor = UIManager.getColor("Table.border");
            if (lineBorderColor == null) {
                lineBorderColor = Color.GRAY;
            }
            setBorder(BorderFactory.createLineBorder(lineBorderColor));
            Color c = UIManager.getColor("Table.noneditableGrid");
            if (c != null) {
                setGridColor(c);
            }
        } else {
            editing = DEFAULT;
            setBorder( null );
            if(!getShowHorizontalLines())
                setShowHorizontalLines(true);
            Color defaultGridColor = UIManager.getColor("Table.defaultGrid");
            if (defaultGridColor != null) {
                setGridColor(defaultGridColor);
            }
        }
        if (isFullyNonEditable()) {
            setupSearch();
        }
    }
    
    /**
     * Returns true if <code>ETable</code> is fully editable.
     *
     * @return  true if the the table is fully editable.
     * @see #setFullyEditable
     */
    public boolean isFullyEditable() {
        return editing == FULLY_EDITABLE;
    }
    
    /**
     * Returns true if <code>ETable</code> is fully non-editable.
     *
     * @return  true if the the table is fully non-editable.
     * @see #setFullyNonEditable
     */
    public boolean isFullyNonEditable() {
        return editing == FULLY_NONEDITABLE;
    }
    
    /**
     * Sets the table cell background colors accodring to NET UI guidelines.
     * <p>
     * This is needed in case where the user does not use the NET Look and Feel,
     * but still wants to paint the cell background colors accoring to NET L&amp;F.
     * <p>
     * This needs to be called also in case where the user has custom table cell
     * renderer (that is not a <code>DefaultTableCellRenderer</code> or a
     * sub-class of it) for a cell even though NET L&amp;F package is used, if the
     * cell background colors need to be consistent for the custom renderer.
     *
     * @param   renderer   the custom cell renderer to be painted
     * @param   isSelected true if the custom cell is selected
     * @param   row        the row, the custom cell corresponds to
     * @param   column     the column, the custom cell corresponds to
     */
    public void setCellBackground(Component renderer, boolean isSelected,
            int row, int column) {
        Color c = null;
        if (row%2 == 0) { //Background 2
            if(isSelected) {
                c = UIManager.getColor("Table.selectionBackground2");
            } else {
                c = UIManager.getColor("Table.background2");
            }
        } else { // Background 1
            if(isSelected) {
                c = UIManager.getColor("Table.selectionBackground1");
            } else {
                c = UIManager.getColor("Table.background1");
            }
        }
        if (c != null) {
            renderer.setBackground(c);
        }
    }

    /**
     * Overridden to use ETableColumns instead of the original TableColumns.
     * @see javax.swing.JTable#createDefaultColumnModel()
     */
    @Override
    public void createDefaultColumnsFromModel() {
        TableModel model = getModel();
        if (model != null) {
            firePropertyChange("createdDefaultColumnsFromModel", null, null);
            TableColumnModel colModel = getColumnModel();
            int modelColumnCount = model.getColumnCount();
            List<TableColumn> oldHiddenColumns = null;
            int[] hiddenColumnIndexes = null;
            int[] sortedColumnIndexes = null;
            if (colModel instanceof ETableColumnModel) {
                ETableColumnModel etcm = (ETableColumnModel)colModel;
                oldHiddenColumns = etcm.hiddenColumns;
                hiddenColumnIndexes = new int[oldHiddenColumns.size()];
                for (int hci = 0; hci < hiddenColumnIndexes.length; hci++) {
                    Object name = oldHiddenColumns.get(hci).getHeaderValue();
                    int index = -1;
                    if (name != null) {
                        for (int i = 0; i < modelColumnCount; i++) {
                            if (name.equals(model.getColumnName(i))) {
                                index = i;
                                break;
                            }
                        }
                    }
                    hiddenColumnIndexes[hci] = index;
                }
                List<TableColumn> sortedColumns = (sortable) ? etcm.getSortedColumns() : Collections.<TableColumn>emptyList();

                int ns = sortedColumns.size();
                if (ns > 0) {
                    sortedColumnIndexes = new int[ns];
                    for (int sci = 0; sci < ns; sci++) {
                        Object name = sortedColumns.get(sci).getHeaderValue();
                        int index = -1;
                        if (name != null) {
                            for (int i = 0; i < modelColumnCount; i++) {
                                if (name.equals(model.getColumnName(i))) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        sortedColumnIndexes[sci] = index;
                    }
                }
            }
            TableColumn newColumns[] = new TableColumn[modelColumnCount];
            int oi = 0;
            class Sorting {
                boolean ascending;
                int sortRank;
                Sorting(boolean ascending, int sortRank) {
                    this.ascending = ascending;
                    this.sortRank = sortRank;
                }
            }
            Sorting[] columnSorting = new Sorting[modelColumnCount];
            for (int i = 0; i < modelColumnCount; i++) {
                newColumns[i] = createColumn(i);
                boolean isHidden = false;
                for (int hci = 0; hci < hiddenColumnIndexes.length; hci++) {
                    if (hiddenColumnIndexes[hci] == i) {
                        isHidden = true;
                    }
                }
                if (oi < colModel.getColumnCount()) {
                    TableColumn tc = colModel.getColumn(oi++);
                    if (!isHidden) {
                        newColumns[i].setPreferredWidth(tc.getPreferredWidth());
                        newColumns[i].setWidth(tc.getWidth());
                    }
                    if (sortable && tc instanceof ETableColumn && newColumns[i] instanceof ETableColumn) {
                        ETableColumn etc = (ETableColumn) tc;
                        ETableColumn enc = (ETableColumn) newColumns[i];
                        enc.nestedComparator = etc.nestedComparator;
                        if (enc.isSortingAllowed()) {
                            columnSorting[i] = new Sorting(etc.isAscending(), etc.getSortRank());
                        }
                    }
                }
            }
            for (int cc = colModel.getColumnCount(); cc > 0; ) {
                colModel.removeColumn(colModel.getColumn(--cc));
            }
            if (colModel instanceof ETableColumnModel) {
                ETableColumnModel etcm = (ETableColumnModel)colModel;
                etcm.hiddenColumns = new ArrayList<TableColumn>();
                etcm.hiddenColumnsPosition = new ArrayList<Integer>();
                etcm.clearSortedColumns();
            }

            for (int i = 0; i < newColumns.length; i++) {
                addColumn(newColumns[i]);
            }
            if (colModel instanceof ETableColumnModel) {
                ETableColumnModel etcm = (ETableColumnModel) colModel;
                if (hiddenColumnIndexes != null) {
                    for (int hci = 0; hci < hiddenColumnIndexes.length; hci++) {
                        int index = hiddenColumnIndexes[hci];
                        if (index >= 0) {
                            etcm.setColumnHidden(newColumns[index], true);
                        }
                    }
                }
                if (sortedColumnIndexes != null) {
                    for (int sci = 0; sci < sortedColumnIndexes.length; sci++) {
                        int index = sortedColumnIndexes[sci];
                        if (index >= 0) {
                            Sorting sorting = columnSorting[index];
                            if (newColumns[index] instanceof ETableColumn && sorting != null) {
                                ETableColumn etc = (ETableColumn) newColumns[index];
                                etcm.setColumnSorted(etc, sorting.ascending, sorting.sortRank);
                            }
                        }
                    }
                }
            }
            firePropertyChange("createdDefaultColumnsFromModel", null, newColumns);
        }
    }

    /**
     * Returns string used to delimit entries when
     * copying into clipboard. The default implementation
     * returns new line character ("\n") if the line
     * argument is true. If it is false the
     * tab character ("\t") is returned.
     */
    public String getTransferDelimiter(boolean line) {
        if (line) {
            return "\n";
        }
        return "\t";
    }
    
    /**
     * Used when copying into clipboard. The value
     * passed to this method is obtained by calling
     * getValueAt(...). The resulting string is put
     * into clipboard. The default implementation returns
     * an empty string ("") if the value is <code>null</code>
     * and value.toString() otherwise. The method
     * <code> transformValue(value)</code> is called prior
     * to the string conversion.
     */
    public String convertValueToString(Object value) {
        value = transformValue(value);
        if (value == null) {
            return "";
        }
        return value.toString();
    }
    
    /**
     * Allow to plug own TableColumn implementation.
     * This implementation returns ETableColumn.
     * Called from createDefaultColumnsFromModel().
     */
    protected TableColumn createColumn(int modelIndex) {
        return new ETableColumn(modelIndex, this);
    }
   
    /**
     * Overridden to use ETableColumnModel as TableColumnModel.
     * @see javax.swing.JTable#createDefaultColumnModel()
     */
    @Override
    protected TableColumnModel createDefaultColumnModel() {
        return new ETableColumnModel();
    }

    /**
     * Overridden to call convertRowIndexToModel(...).
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column) {
        int modelRow = row;
        return super.getValueAt(modelRow, column);
    }

    /**
     * Overridden to call convertRowIndexToModel(...).
     * @see javax.swing.JTable#setValueAt(Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
    }

    /**
     * If the quick-filter is applied the number of rows do not
     * match the number of rows in the model.
     */
    @Override
    public int getRowCount() {
        if ((quickFilterColumn != -1) && (quickFilterObject != null)) {
            if (filteredRowCount == -1) {
                computeFilteredRowCount();
            }
            return filteredRowCount;
        }
        return super.getRowCount();
    }

    /**
     * Makes the table display only the rows that match the given "quick-filter".
     * Filtering is done according to values from column with index column and
     * according to filterObject. There are 2 possibilities for the filterObject
     * parameter
     * <OL> <LI> filterObject implements <strong>QuickFilter</strong> 
     *           interface: the method <code>accept(Object)</code> 
     *           of the QuickFilter is called to determine whether the 
     *           row will be shown</LI>
     *      <LI> if filterObject does not implement the interface the value
     *           is compared using method equals(Object) with the filterObject.
     *           If they are equal the row will be shown.
     * </OL>
     */
    public void setQuickFilter(int column, Object filterObject) {
        quickFilterColumn = column;
        quickFilterObject = filterObject;
        resetPermutation ();
        filteredRowCount = -1; // force to recompute the rowCount
        super.tableChanged(new TableModelEvent(getModel()));
        firePropertyChange(PROP_QUICK_FILTER, null, null);
    }

    /**
     * Get the "quick-filter" object that is currently active.
     *
     * @return the filter object or <code>null</code>
     * @see #setQuickFilter(int, java.lang.Object)
     */
    public Object getQuickFilterObject() {
        return quickFilterObject;
    }

    /**
     * Get the column which is currently filtered by "quick-filter" object.
     *
     * @return the filtered column or <code>-1</code>
     * @see #setQuickFilter(int, java.lang.Object)
     */
    public int getQuickFilterColumn() {
        return quickFilterColumn;
    }
    
    /**
     * Makes the table show all the rows, resetting the filter state
     * (to no filter).
     */
    public void unsetQuickFilter() {
        quickFilterObject = null;
        quickFilterColumn = -1;
        filteredRowCount = -1;
        resetPermutation ();
        super.tableChanged(new TableModelEvent(getModel()));
        firePropertyChange(PROP_QUICK_FILTER, null, null);
    }
    
    /**
     * Overridden to update the header listeners and also to adjust the
     * preferred width of the columns.
     * @see javax.swing.JTable#setModel(TableModel)
     */
    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        
        // force recomputation
        filteredRowCount = -1;
        resetPermutation ();
        quickFilterColumn = -1;
        quickFilterObject = null;
        
        updateMouseListener();
        if (defaultRenderersByColumnClass != null) {
            updatePreferredWidths();
        }
    }
    
    /**
     * Overridden to make a speed optimization.
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        try {
            putClientProperty(COMPUTING_TOOLTIP, Boolean.TRUE);
            return super.getToolTipText(event);
        } finally {
            putClientProperty(COMPUTING_TOOLTIP, Boolean.FALSE);
        }
    }
    
    /**
     * Test if the column hiding is allowed.
     * @return True if column hiding is allowed.
     */
    public boolean isColumnHidingAllowed() {
        return columnHidingAllowed;
    }
    
    /**
     * Turn column hiding on/off
     * @param allowColumnHiding false to turn column hiding off
     */
    public void setColumnHidingAllowed( boolean allowColumnHiding ) {
        if( allowColumnHiding != this.columnHidingAllowed ) {
            this.columnHidingAllowed = allowColumnHiding;
            configureEnclosingScrollPane();
        }
    }
    
    /**
     * Overridden to do additional initialization.
     * @see javax.swing.JTable#initializeLocalVars()
     */
    @Override
    protected void initializeLocalVars() {
        super.initializeLocalVars();
        updatePreferredWidths();
        setSurrendersFocusOnKeystroke(true);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new STPolicy());
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        Set<AWTKeyStroke> emptySet = Collections.emptySet();
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptySet);
        setFocusCycleRoot(false);
        //Next two lines do not work using inputmap/actionmap, but do work
        //using the older API.  We will process ENTER to skip to next row,
        //not next cell
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK));

        InputMap imp = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();
        
        //Issue 37919, reinstate support for up/down cycle focus transfer.
        //being focus cycle root mangles this in some dialogs
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
            KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, false), ACTION_FOCUS_NEXT);
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
            KeyEvent.CTRL_MASK, false), ACTION_FOCUS_NEXT);
        
        Action ctrlTab = new CTRLTabAction();
        am.put(ACTION_FOCUS_NEXT, ctrlTab);
        
        
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0,
        false), "beginEdit");
        getActionMap().put("beginEdit", new EditAction());
        
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,
        false), "cancelEdit");
        getActionMap().put("cancelEdit", new CancelEditAction());
        
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,
        false), "enter");
        getActionMap().put("enter", new EnterAction());
        
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "next");
        
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
            KeyEvent.SHIFT_DOWN_MASK), "previous");
        
        am.put("next", new NavigationAction(true));
        am.put("previous", new NavigationAction(false));
        
        setTransferHandler(new ETableTransferHandler());
    }
    
    /**
     * Overridden to implement CTRL-+ for resizing of all columns,
     * CTRL-- for clearing the quick filter and CTRL-* for invoking the
     * column selection dialog.
     * @see javax.swing.JTable#processKeyBinding(KeyStroke, KeyEvent, int, boolean)
     */
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
					int condition, boolean pressed) {
        // This is here because the standard way using input map and action map
        // did not work since the event was "eaten" by the code in JTable that
        // forwards it to the CellEditor (the code resides in the
        // super.processKeyBinding method).
        if (pressed) {
            if (e.getKeyChar() == '+' && ( (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
                updatePreferredWidths();
                e.consume();
                return true;
            }
            if (e.getKeyChar() == '-' && ( (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
                unsetQuickFilter();
                e.consume();
                return true;
            }
            if (e.getKeyChar() == '*' && ( (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
                ColumnSelectionPanel.showColumnSelectionDialog(this);
                e.consume();
                return true;
            }
        }
	boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
        return retValue;
    }

    /**
     * Make the column sorted. Value of columnIndex is in the model coordinates.
     * <strong>Be careful</strong> with the columnIndes parameter: again, it
     * is in the <strong>model</strong> coordinates.
     * @param columnIndex column in ETable column model
     * @param ascending true means ascending
     * @param rank value 1 means that this is the most important sorted
     *        column, number 2 means second etc. Value 0 means that this column
     *        is not sorted.
     */
    public void setColumnSorted(int columnIndex, boolean ascending, int rank) {
        int ii = convertColumnIndexToView(columnIndex);
        if (ii < 0) {
            return;
        }
        sortable = true;
        TableColumnModel tcm = getColumnModel();
        if (tcm instanceof ETableColumnModel) {
            ETableColumnModel etcm = (ETableColumnModel)tcm;
            TableColumn tc = tcm.getColumn(ii);
            if (tc instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn)tc;
                if (! etc.isSortingAllowed()) {
                    return;
                }
                SelectedRows selectedRows;
                int wasSelectedColumn;
                if (getUpdateSelectionOnSort()) {
                    selectedRows = getSelectedRowsInModel();
                    wasSelectedColumn = getSelectedColumn();
                } else {
                    selectedRows = null;
                    wasSelectedColumn = -1;
                }
                etcm.setColumnSorted(etc, ascending, rank);
                resetPermutation ();
                ETable.super.tableChanged(new TableModelEvent(getModel(), -1, getRowCount()));
                if (selectedRows != null) {
                    changeSelectionInModel(selectedRows, wasSelectedColumn);
                }
            }
        }
    }
    
    /**
     * Overridden to install special button into the upper right hand corner.
     * @see javax.swing.JTable#configureEnclosingScrollPane()
     */
    @Override
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();
        
        if (isFullyNonEditable()) {
            setupSearch();
        }
        
        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                if( isColumnHidingAllowed() ) {
                    Icon ii = UIManager.getIcon("Table.columnSelection");
                    if (ii == null) {
                        ii = ImageUtilities.image2Icon(ImageUtilities.loadImage(DEFAULT_COLUMNS_ICON));
                    }
                    final JButton b = new JButton(ii);
                    // For HiDPI support.
                    b.setDisabledIcon(ImageUtilities.createDisabledIcon(ii));
                    b.setToolTipText(selectVisibleColumnsLabel);
                    b.getAccessibleContext().setAccessibleName(selectVisibleColumnsLabel);
                    b.getAccessibleContext().setAccessibleDescription(selectVisibleColumnsLabel);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            ColumnSelectionPanel.showColumnSelectionPopupOrDialog(b, ETable.this);
                        }
                    });
                    b.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent me) {
                            ColumnSelection cs = getColumnSelectionOn(me.getButton());
                            switch (cs) {
                                case POPUP:
                                    ColumnSelectionPanel.showColumnSelectionPopup (b, ETable.this);
                                    break;
                                case DIALOG:
                                    ColumnSelectionPanel.showColumnSelectionDialog(ETable.this);
                                    break;
                            }
                        }
                    });
                b.setFocusable(false);
                    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, b);
                } else {
                    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
                }
            }
        }
        updateColumnSelectionMouseListener();
    }
    
    /**
     * Convert indices of selected rows to model.
     */
    private SelectedRows getSelectedRowsInModel() {
        SelectedRows sr = new SelectedRows();
        int rows[] = getSelectedRows();
        sr.rowsInView = rows;
        int[] modelRows = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            modelRows[i] = convertRowIndexToModel(rows[i]);
        }
        //System.err.println("getSelectedRowsInModel() sets new rows from view: "+
        //        Arrays.toString(rows)+" to model rows: "+Arrays.toString(modelRows));
        sr.rowsInModel = modelRows;
        ListSelectionModel rsm = getSelectionModel();
        sr.anchorInView = rsm.getAnchorSelectionIndex();
        sr.leadInView = rsm.getLeadSelectionIndex();
        int rc = getRowCount();
        sr.anchorInModel = (sr.anchorInView < rc) ? convertRowIndexToModel(sr.anchorInView) : -1;
        sr.leadInModel = (sr.leadInView < rc) ? convertRowIndexToModel(sr.leadInView) : -1;
        return sr;
    }
    
    /**
     * Selects given rows (the rows coordinates are the model's space).
     */
    private void changeSelectionInModel(SelectedRows selectedRows/*int selectedRows[]*/, int selectedColumn) {
        //System.err.println("changeSelectionInModel("+selectedRows+")");
        boolean wasAutoScroll = getAutoscrolls();
        setAutoscrolls(false);
        DefaultListSelectionModel rsm = (DefaultListSelectionModel) getSelectionModel();
        rsm.setValueIsAdjusting(true);
        ListSelectionModel csm = getColumnModel().getSelectionModel();
        csm.setValueIsAdjusting(true);

        //System.err.println("Orig lead and anchor selection indexes: "+rsm.getLeadSelectionIndex()+", "+rsm.getAnchorSelectionIndex());

        int leadRow = convertRowIndexToView(selectedRows.leadInModel);
        int anchorRow = convertRowIndexToView(selectedRows.anchorInModel);
        
        Arrays.sort(selectedRows.rowsInView);
        //System.err.println("OrigSelectedRows in view = "+Arrays.toString(selectedRows.rowsInView));
        int[] newRowsInView = new int[selectedRows.rowsInModel.length];
        int n = getModel().getRowCount();
        for (int i = 0; i < selectedRows.rowsInModel.length; i++) {
            if (i < selectedRows.rowsInView.length &&
                ((selectedRows.rowsInView[i] < 0) || (selectedRows.rowsInView[i] >= n))) {
                newRowsInView[i] = -1;
                continue;
            }
            int viewIndex = convertRowIndexToView(selectedRows.rowsInModel[i]);
            newRowsInView[i] = viewIndex;

        }
        Arrays.sort(newRowsInView);
        //System.err.println("NewSelectedRows in view = "+Arrays.toString(newRowsInView));
        int[] rowsInView = selectedRows.rowsInView;
        int i = 0;
        int j = 0;
        while (i < rowsInView.length || j < newRowsInView.length) {
            int selected = (i < rowsInView.length) ? rowsInView[i] : Integer.MAX_VALUE;
            int toSelect = (j < newRowsInView.length) ? newRowsInView[j] : Integer.MAX_VALUE;
            if (selected == toSelect) {
                i++;
                j++;
                continue;
            }
            if (selected < toSelect) {
                if (selected > -1) {
                    int selected2 = selected;
                    while ((i + 1) < rowsInView.length && rowsInView[i+1] == (selected2 + 1) && (selected2 + 1) < toSelect) {
                        selected2++;
                        i++;
                    }
                    rsm.removeSelectionInterval(selected, selected2);
                    //System.err.println("  removing selection ("+selected+", "+selected2+")");
                }
                i++;
            } else {
                if (toSelect > -1) {
                    int toSelect2 = toSelect;
                    while ((j + 1) < newRowsInView.length && newRowsInView[j + 1] == (toSelect2 + 1) && (toSelect2 + 1) < selected) {
                        toSelect2++;
                        j++;
                    }
                    rsm.addSelectionInterval(toSelect, toSelect2);
                    //System.err.println("  adding selection ("+toSelect+", "+toSelect2+")");
                }
                j++;
            }
        }
        
        if (anchorRow != selectedRows.anchorInView) {
            //System.err.println("  Setting anchor selection index: "+anchorRow);
            rsm.setAnchorSelectionIndex(anchorRow);
        }
        if (leadRow != selectedRows.leadInView) {
            if (leadRow == -1) {
                for (int k = 0; k < newRowsInView.length; k++) {
                    if (newRowsInView[k] == -1) {
                        while((k + 1) < newRowsInView.length && newRowsInView[k+1] == -1) {
                            k++;
                        }
                        if ((k + 1) < newRowsInView.length) {
                            leadRow = newRowsInView[k+1];
                            break;
                        }
                        if (k > 0) {
                            leadRow = newRowsInView[0];
                            break;
                        }
                    }
                }
            }
            //System.err.println("  Setting lead selection index: "+leadRow);
            // DO NOT CALL setLeadSelectionIndex() as it screws up selection!
            rsm.moveLeadSelectionIndex(leadRow);
        }
         
        rsm.setValueIsAdjusting(false);
        csm.setValueIsAdjusting(false);
        if (wasAutoScroll) {
            setAutoscrolls(true);
        }
    }
    
    private void updateSelectedLines(int[] currentLineSelections,
                                     int currentLeadRow, int currentAnchorRow,
                                     int[] newLineSelections,
                                     int newLeadRow, int newAnchorRow) {
        //System.err.println("updateSelectedLines("+Arrays.toString(currentLineSelections)+" => "+
        //                   Arrays.toString(newLineSelections));
        boolean wasAutoScroll = getAutoscrolls();
        setAutoscrolls(false);
        DefaultListSelectionModel rsm = (DefaultListSelectionModel) getSelectionModel();
        rsm.setValueIsAdjusting(true);
        ListSelectionModel csm = getColumnModel().getSelectionModel();
        csm.setValueIsAdjusting(true);

        //System.err.println("Orig lead and anchor selection indexes: "+rsm.getLeadSelectionIndex()+", "+rsm.getAnchorSelectionIndex());

        //int leadRow = convertRowIndexToView(selectedRows.leadInModel);
        //int anchorRow = convertRowIndexToView(selectedRows.anchorInModel);
        
        int i = 0;
        int j = 0;
        while (i < currentLineSelections.length || j < newLineSelections.length) {
            int selected = (i < currentLineSelections.length) ? currentLineSelections[i] : Integer.MAX_VALUE;
            int toSelect = (j < newLineSelections.length) ? newLineSelections[j] : Integer.MAX_VALUE;
            if (selected == toSelect) {
                i++;
                j++;
                continue;
            }
            if (selected < toSelect) {
                if (selected > -1) {
                    int selected2 = selected;
                    while ((i + 1) < currentLineSelections.length && currentLineSelections[i+1] == (selected2 + 1) && (selected2 + 1) < toSelect) {
                        selected2++;
                        i++;
                    }
                    rsm.removeSelectionInterval(selected, selected2);
                    //System.err.println("  removing selection ("+selected+", "+selected2+")");
                }
                i++;
            } else {
                if (toSelect > -1) {
                    int toSelect2 = toSelect;
                    while ((j + 1) < newLineSelections.length && newLineSelections[j + 1] == (toSelect2 + 1) && (toSelect2 + 1) < selected) {
                        toSelect2++;
                        j++;
                    }
                    rsm.addSelectionInterval(toSelect, toSelect2);
                    //System.err.println("  adding selection ("+toSelect+", "+toSelect2+")");
                }
                j++;
            }
        }
        
        if (newAnchorRow != currentAnchorRow) {
            //System.err.println("  Setting anchor selection index: "+anchorRow);
            rsm.setAnchorSelectionIndex(newAnchorRow);
        }
        if (newLeadRow != currentLeadRow) {
            if (newLeadRow == -1) {
                for (int k = 0; k < newLineSelections.length; k++) {
                    if (newLineSelections[k] == -1) {
                        while((k + 1) < newLineSelections.length && newLineSelections[k+1] == -1) {
                            k++;
                        }
                        if ((k + 1) < newLineSelections.length) {
                            newLeadRow = newLineSelections[k+1];
                            break;
                        }
                        if (k > 0) {
                            newLeadRow = newLineSelections[0];
                            break;
                        }
                    }
                }
            }
            //System.err.println("  Setting lead selection index: "+leadRow);
            // DO NOT CALL setLeadSelectionIndex() as it screws up selection!
            rsm.moveLeadSelectionIndex(newLeadRow);
        }
         
        rsm.setValueIsAdjusting(false);
        csm.setValueIsAdjusting(false);
        if (wasAutoScroll) {
            setAutoscrolls(true);
        }
    }
    
    /**
     * This method update mouse listener on the scrollPane if it is needed.
     * It also recomputes the model of searchCombo. Both actions are needed after
     * the set of visible columns is changed.
     */
    void updateColumnSelectionMouseListener() {
        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                scrollPane.removeMouseListener(columnSelectionMouseListener);
                if (getColumnModel().getColumnCount() == 0) {
                    scrollPane.addMouseListener(columnSelectionMouseListener);
                }
            }
        }
        if (searchCombo != null) {
            searchCombo.setModel(getSearchComboModel());
        }
    }
    
    private SelectedRows selectedRowsWhenTableChanged = null;
    private int[][] sortingPermutationsWhenTableChanged = null;
    private int selectedColumnWhenTableChanged = -1;
    
    /**
     * If the table data model is changed we reset (and then recompute)
     * the sorting permutation and the row count. The selection is restored
     * when needed.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        boolean needsTotalRefresh = true;
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
            resetPermutation ();
            filteredRowCount = -1;
            super.tableChanged(e);
            return;
        }

        boolean areMoreEventsPending = false;
        Object source = e.getSource();
        if (source instanceof DefaultOutlineModel) {
            areMoreEventsPending = ((DefaultOutlineModel) source).areMoreEventsPending();
        }
        //System.err.println("tableChanged("+e+"): e.type = "+e.getType()+", first row = "+e.getFirstRow()+", last row = "+e.getLastRow()+
        //                   ", areMoreEventsPending = "+areMoreEventsPending);
        if (e.getType() == TableModelEvent.INSERT ||
            e.getType() == TableModelEvent.DELETE) {
            
            if (selectedRowsWhenTableChanged == null) {
                selectedRowsWhenTableChanged = getSelectedRowsInModel();
                sortingPermutationsWhenTableChanged = new int[2][];
                sortingPermutationsWhenTableChanged[0] = sortingPermutation;
                sortingPermutationsWhenTableChanged[1] = inverseSortingPermutation;
                selectedColumnWhenTableChanged = getSelectedColumn();
                resetPermutation();
                filteredRowCount = -1;
                //System.err.println("selected rows in model = "+Arrays.toString(selectedRowsWhenTableChanged.rowsInModel));
            }
            TableModelEvent se = e;
            /* Not necessary to transform, as we're still in the UI coordinates.
            int[] invSortPermutation = sortingPermutationsWhenTableChanged[1];
            if (invSortPermutation != null) {
                int fr = e.getFirstRow();
                if (fr >= 0 && fr < invSortPermutation.length) {
                    fr = invSortPermutation[fr];
                }
                int lr = e.getLastRow();
                if (lr >= 0 && lr < invSortPermutation.length) {
                    lr = invSortPermutation[lr];
                }
                se = new TableModelEvent((TableModel) e.getSource(), fr, lr, e.getColumn(), e.getType());
            }*/
            super.tableChanged(se);
            int first = e.getFirstRow();
            int last = e.getLastRow();
            if (first > last) {
                int r = last;
                last = first;
                first = r;
            }
            int count = last - first + 1;
            if (count > 0) {
                int[] wasSelectedRows = selectedRowsWhenTableChanged.rowsInModel;
                if (e.getType() == TableModelEvent.INSERT) {
                    for (int i = 0; i < wasSelectedRows.length; i++) {
                        if (wasSelectedRows[i] >= first) {
                            wasSelectedRows[i] += count;
                        }
                    }
                } else {
                    for (int i = 0; i < wasSelectedRows.length; i++) {
                        if (wasSelectedRows[i] >= first) {
                            if (wasSelectedRows[i] <= last) {
                                wasSelectedRows[i] = -1;
                            } else {
                                wasSelectedRows[i] -= count;
                            }
                        }
                    }
                }
            }
            if (!areMoreEventsPending) {
                //System.err.println(" => after all events selecting Rows = "+selectedRowsWhenTableChanged);
                if (selectedRowsWhenTableChanged.rowsInModel.length > 0) {
                    selectedRowsWhenTableChanged.rowsInView = getSelectedRows(); // Actual selected rows in the view
                    //System.err.println("     adjusted selected rows to "+selectedRowsWhenTableChanged);
                    changeSelectionInModel(selectedRowsWhenTableChanged, selectedColumnWhenTableChanged);
                }
                selectedRowsWhenTableChanged = null;
                sortingPermutationsWhenTableChanged = null;
                selectedColumnWhenTableChanged = -1;
            }
            return ;
        }

        int modelColumn = e.getColumn();
        int start = e.getFirstRow();
        int end = e.getLastRow();

        //System.err.println("ETable.tableChanged("+e+"):\n              modelColumn = "+modelColumn+", start = "+start+", end = "+end);
        if (start != -1) {
            start = convertRowIndexToView(start);
            if (start == -1) {
                start = 0;
            }
        }
        if (end != -1) {
            end = convertRowIndexToView(end);
            if (end == -1) {
                end = Integer.MAX_VALUE;
            }
        }
        //System.err.println("  => convert: modelColumn = "+modelColumn+", start = "+start+", end = "+end);
        if (start == end) {
            final TableModelEvent tme = new TableModelEvent(
                    (TableModel)e.getSource(),
                    start, end, modelColumn);
            super.tableChanged(tme);
            return ;
        }

        if (modelColumn != TableModelEvent.ALL_COLUMNS) {
            Enumeration<TableColumn> enumeration = getColumnModel().getColumns();
            TableColumn aColumn;
            while (enumeration.hasMoreElements()) {
                aColumn = enumeration.nextElement();
                if (aColumn.getModelIndex() == modelColumn) {
                    ETableColumn etc = (ETableColumn)aColumn;
                    if ((! etc.isSorted()) && (quickFilterColumn != modelColumn)){
                        needsTotalRefresh = false;
                    }
                }
            }
        }
        if (needsTotalRefresh) { // update the whole table
            SelectedRows selectedRows = getSelectedRowsInModel();
            int wasSelectedColumn = getSelectedColumn();
        
            //System.err.println("NEEDS TOTAL REFRESH: selectedRows = "+selectedRows);
            resetPermutation ();
            filteredRowCount = -1;
            super.tableChanged(new TableModelEvent(getModel()));
            changeSelectionInModel(selectedRows, wasSelectedColumn);
        } else { // update only one column
            TableModelEvent tme = new TableModelEvent(
                (TableModel)e.getSource(), 
                0, getModel().getRowCount(), modelColumn);
            super.tableChanged(tme);
        }
    }

    private void resetPermutation () {
        assert SwingUtilities.isEventDispatchThread () : "Do resetting of permutation only in AWT queue!";
        /*
        System.err.print(" PERMUT. RESET, old PERMUT. = ");
        if (sortingPermutation == null) {
            System.err.println("null");
        } else {
            System.err.println(Arrays.toString(sortingPermutation));
        }
        System.err.print("                old INV. P. = ");
        if (inverseSortingPermutation == null) {
            System.err.println("null");
        } else {
            System.err.println(Arrays.toString(inverseSortingPermutation));
        }*/
        sortingPermutation = null;
        inverseSortingPermutation = null;
    }

    /**
     * When the user clicks the header this method returns either
     * the column that should be resized or null.
     */
    private TableColumn getResizingColumn(Point p) {
        JTableHeader header = getTableHeader();
        if (header == null) {
            return null;
        }
        int column = header.columnAtPoint(p);
        if (column == -1) {
            return null;
        }
        Rectangle r = header.getHeaderRect(column);
        r.grow(-3, 0);
        if (r.contains(p)) {
            return null;
        }
        int midPoint = r.x + r.width/2;
        int columnIndex;
        if( header.getComponentOrientation().isLeftToRight() ) {
            columnIndex = (p.x < midPoint) ? column - 1 : column;
        } else {
            columnIndex = (p.x < midPoint) ? column : column - 1;
        }
        if (columnIndex == -1) {
            return null;
        }
        return header.getColumnModel().getColumn(columnIndex);
    }

    /**
     * Adds mouse listener to the header for sorting and auto-sizing
     * of the columns.
     */
    private void updateMouseListener() {
        JTableHeader jth = getTableHeader();
        if (jth != null) {
            jth.removeMouseListener(headerMouseListener); // not to add it twice
            jth.addMouseListener(headerMouseListener);
        }
    }
    
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new ETableHeader(columnModel);
    }

    /**
     * Updates the value of filteredRowCount variable.
     */
    private void computeFilteredRowCount() {
        if ((quickFilterColumn == -1) || (quickFilterObject == null) ) {
            filteredRowCount = -1;
            return;
        }
        if (sortingPermutation != null) {
            filteredRowCount = sortingPermutation.length;
            return;
        }
        sortAndFilter();
        if (sortingPermutation != null) {
            filteredRowCount = sortingPermutation.length;
        }
    }
    
    /**
     * Helper method converting the row index according to the active sorting
     * columns.
     */
    @Override
    public int convertRowIndexToModel(int row) {
        if (!(getColumnModel() instanceof ETableColumnModel)) {
            // Use JDK 6 sorting and filtering with custom column models.
            return super.convertRowIndexToModel(row);
        }
        if (sortingPermutation == null) {
            sortAndFilter();
        }
        if (sortingPermutation != null) {
            if ((row >= 0) && (row < sortingPermutation.length)) {
                return sortingPermutation[row];
            }
            if (row > 0 && row != Integer.MAX_VALUE && LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Row "+row+" is bigger than sortingPermutation.length = "+sortingPermutation.length,
                        new IllegalStateException("Row = "+row+", sortingPermutation = "+Arrays.toString(sortingPermutation)));
                //System.err.println("Row "+row+" is bigger than sortingPermutation length, sortingPermutation = "+Arrays.toString(sortingPermutation));
            }
            return row;
        }
        return row;
    }
    
    /**
     * Helper method converting the row index according to the active sorting
     * columns.
     */
    @Override
    public int convertRowIndexToView(int row) {
        if (!(getColumnModel() instanceof ETableColumnModel)) {
            // Use JDK 6 sorting and filtering with custom column models.
            return super.convertRowIndexToView(row);
        }
        if (inverseSortingPermutation == null) {
            sortAndFilter();
        }
        if (inverseSortingPermutation != null) {
            if ((row >= 0) && (row < inverseSortingPermutation.length)) {
                return inverseSortingPermutation[row];
            }
            return -1;
        }
        return row;
    }
    
    /**
     * Allows customization of the text appearing in the column
     * customization dialog.
     */
    public void setSelectVisibleColumnsLabel(String localizedLabel) {
        selectVisibleColumnsLabel = localizedLabel;
    }
    
    String getSelectVisibleColumnsLabel() {
        return selectVisibleColumnsLabel;
    }
    
    /**
     * Replaces the quickFilterFormatStrings by the given array. The
     * new array must have the same length as the old one.
     */
    public void setQuickFilterFormatStrings(String []newFormats) {
        if ((newFormats == null) || (newFormats.length != quickFilterFormatStrings.length)) {
            return;
        }
        quickFilterFormatStrings = newFormats.clone();
    }

    /**
     * Returns the quickFilterFormatStrings array.
     */
    public String[] getQuickFilterFormatStrings() {
        return quickFilterFormatStrings.clone();
    }

    /**
     * Allows subclasses to localize the column headers. This method
     * is called by the header renderer. The default implementation just
     * returns passed in columnName.
     */
    public String getColumnDisplayName(String columnName) {
        return columnName;
    }
    
    /**
     * Allows subclasses to transform the value (usually obtained by calling
     * getValueAt(...)) to another object that is used for
     * sorting, comparison via quick filters etc. The default implementation
     * just returns the original value.
     */
    public Object transformValue(Object value) {
        return value;
    }
    
    /**
     * Creates a menu item usable in a popup that will trigger
     * QuickFilter functionality for given column and value of the
     * cell it was invoked on (returned from 
     * transformValue(getValueAt(column, row))).<p> <strong>Note:</strong> do not
     * forget to call transformValue before passing the value to this method
     * otherwise the quickfilters will not work.<p>
     * The label should be localized version of the string the user will
     * see in the popup menu, e.g. "Filter Column".
     */
    public JMenuItem getQuickFilterPopup(int column, Object value, String label) {
        JMenu menu = new JMenu(label);
        String columnDisplayName = getColumnDisplayName(getColumnName(column));
        JMenuItem equalsItem = getQuickFilterEqualsItem(column, value, 
                columnDisplayName, quickFilterFormatStrings[0], true);
        menu.add(equalsItem);
        JMenuItem notequalsItem = getQuickFilterEqualsItem(column, value, 
                columnDisplayName, quickFilterFormatStrings[1], false);
        menu.add(notequalsItem);
        JMenuItem greaterItem = getQuickFilterCompareItem(column, value, 
                columnDisplayName, quickFilterFormatStrings[2], true, false);
        menu.add(greaterItem);
        JMenuItem lessItem = getQuickFilterCompareItem(column, value, 
                columnDisplayName, quickFilterFormatStrings[3], false, false);
        menu.add(lessItem);
        JMenuItem greaterEqualsItem = getQuickFilterCompareItem(column, value,
                columnDisplayName, quickFilterFormatStrings[4], true, true);
        menu.add(greaterEqualsItem);
        JMenuItem lessEqualsItem = getQuickFilterCompareItem(column, value,
                columnDisplayName, quickFilterFormatStrings[5], false, true);
        menu.add(lessEqualsItem);
        JMenuItem noFilterItem = getQuickFilterNoFilterItem(quickFilterFormatStrings[6]);
        menu.add(noFilterItem);
        return menu;
    }

    /**
     * Creates the menu item for setting the quick filter that filters
     * the objects using equality (or non-equality).
     */
    public JMenuItem getQuickFilterEqualsItem(final int column, Object value,
            String columnName, String text, boolean equals) {
        
        String s = MessageFormat.format(text, new Object[] { columnName, value});
        JMenuItem res = new JMenuItem(s);
        int modelColumn = convertColumnIndexToModel(column);
        res.addActionListener(new EqualsQuickFilter(modelColumn, value, equals));
        return res;
    }
    
    /**
     * Private implementation of the equality quick filter.
     */
    private class EqualsQuickFilter implements ActionListener, QuickFilter {
        private int column;
        private Object value;
        private boolean equals;
        public EqualsQuickFilter(int column, Object value, boolean equals) {
            this.column = column;
            this.value = value;
            this.equals = equals;
        }
        @Override
        public boolean accept(Object aValue) {
            if ((value == null) && (aValue == null)) {
                return equals;
            }
            if ((value == null) || (aValue == null)) {
                return ! equals;
            }
            if (equals) {
                return value.equals(aValue);
            } else {
                return ! value.equals(aValue);
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setQuickFilter(column, this);
        }
    }
    
    /**
     * Creates the menu item for resetting the quick filter (to no filter).
     */
    public JMenuItem getQuickFilterNoFilterItem(String label) {
        JMenuItem res = new JMenuItem(label);
        res.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                unsetQuickFilter();
            }
        });
        return res;
    }
    
    /**
     * Creates the menu item for setting the quick filter that filters
     * the objects using Comparable interface.
     */
    public JMenuItem getQuickFilterCompareItem(
            final int column, Object value, String columnName, 
            String text, boolean greater, boolean equalsCounts) {
        
        String s = MessageFormat.format(text, new Object[] { columnName, value});
        JMenuItem res = new JMenuItem(s);
        int modelColumn = convertColumnIndexToModel(column);
        res.addActionListener(new CompareQuickFilter(modelColumn, value, greater, equalsCounts));
        return res;
    }
    
    /**
     * Private quick filter implementation using Comparable interface.
     */
    private class CompareQuickFilter implements ActionListener, QuickFilter {
        private int column;
        private Object value;
        private boolean greater;
        private boolean equalsCounts;
        public CompareQuickFilter(int column, Object value, boolean greater, boolean equalsCounts) {
            this.column = column;
            this.value = value;
            this.greater = greater;
            this.equalsCounts = equalsCounts;
        }
        @Override
        public boolean accept(Object aValue) {
            if (equalsCounts) {
                if (greater) {
                    return doCompare(value, aValue) <= 0;
                } else {
                    return doCompare(value, aValue) >= 0;
                }
            } else {
                if (greater) {
                    return doCompare(value, aValue) < 0;
                } else {
                    return doCompare(value, aValue) > 0;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private int doCompare(Object obj1, Object obj2) {
            if (obj1 == null && obj2 == null) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            if ((obj1 instanceof Comparable) && (obj1.getClass().isAssignableFrom(obj2.getClass()))){
                Comparable c1 = (Comparable) obj1;
                return c1.compareTo(obj2);
            }
            return obj1.toString().compareTo(obj2.toString());
        }
        
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setQuickFilter(column, this);
        }
    }
    
    /**
     * Sorts the rows of the table.
     */
    protected void sortAndFilter() {
        TableColumnModel tcm = getColumnModel();
        if (tcm instanceof ETableColumnModel) {
            ETableColumnModel etcm = (ETableColumnModel) tcm;
            Comparator<RowMapping> c = etcm.getComparator();
            if (c != null) {
                TableModel model = getModel();
                int noRows = model.getRowCount();
                //System.err.println("ETable.sortAndFilter(): noRows = "+noRows);
                List<RowMapping> rows = new ArrayList<RowMapping>();
                for (int i = 0; i < noRows; i++) {
                    if (acceptByQuickFilter(model, i)) {
                        rows.add(new RowMapping(i, model, this));
                    }
                }
                rows.sort(c);
                int [] res = new int[rows.size()];
                int [] invRes = new int[noRows]; // carefull - this one is bigger!
                for (int i = 0; i < res.length; i++) {
                    RowMapping rm = rows.get(i);
                    int rmi = rm.getModelRowIndex();
                    res[i] = rmi;
                    invRes[rmi] = i;
                }
                int[] oldRes = sortingPermutation;
                int[] oldInvRes = inverseSortingPermutation;
                //System.err.println(" SETTING PERMUTATION = "+Arrays.toString(res));
                //System.err.println(" SETTING INV.PERMUT. = "+Arrays.toString(invRes));
                sortingPermutation = res;
                inverseSortingPermutation = invRes;
                //adjustSelectedRows(oldRes, oldInvRes, res, invRes);
            }
        }
    }
    
    /**
     * Adjusts selected rows when sorting changes.
     *
    protected final void adjustSelectedRows(int[] oldSortingPermutation, int[] oldInverseSortingPermutation,
                                            int[] newSortingPermutation, int[] newInverseSortingPermutation) {
        if (true) return ;
        int[] oldSelectedRows = getSelectedRows();
        int n = oldSelectedRows.length;
        //System.err.println("adjustSelectedRows("+n+")");
        if (n == 0) {
            return ;
        }
        int[] newSelectedRows = new int[n];
        System.arraycopy(oldSelectedRows, 0, newSelectedRows, 0, n);
        for (int i = 0; i < n; i++) {
            int vr = oldSelectedRows[i];
            int mr;
            if (oldSortingPermutation == null) {
                mr = vr;
            } else {
                mr = oldSortingPermutation[vr];
            }
            vr = newInverseSortingPermutation[mr];
            newSelectedRows[i] = vr;
        }
        ListSelectionModel rsm = getSelectionModel();
        int oldAnchor = rsm.getAnchorSelectionIndex();
        int oldLead = rsm.getLeadSelectionIndex();
        int newAnchor;
        if (oldAnchor < 0) {
            newAnchor = oldAnchor;
        } else {
            int mr;
            if (oldSortingPermutation == null) {
                mr = oldAnchor;
            } else {
                mr = oldSortingPermutation[oldAnchor];
            }
            newAnchor = newInverseSortingPermutation[mr];
        }
        int newLead;
        if (oldLead < 0) {
            newLead = oldLead;
        } else {
            int mr;
            if (oldSortingPermutation == null) {
                mr = oldLead;
            } else {
                mr = oldSortingPermutation[oldLead];
            }
            newLead = newInverseSortingPermutation[mr];
        }
        
        updateSelectedLines(oldSelectedRows, oldLead, oldAnchor,
                            newSelectedRows, newLead, newAnchor);
        
    }
    */
    
    /**
     * Determines whether the given row should be displayed or not.
     */
    protected boolean acceptByQuickFilter(TableModel model, int row) {
        if ((quickFilterColumn == -1) || (quickFilterObject == null) ) {
            return true;
        }
        Object value = model.getValueAt(row, quickFilterColumn);
        value = transformValue(value);
        if (quickFilterObject instanceof QuickFilter) {
            QuickFilter filter = (QuickFilter) quickFilterObject;
            return filter.accept(value);
        }
        if (value == null) {
            return false;
        }
        // fallback test for equality with the filter object
        return value.equals(quickFilterObject);
    }
    
    /**
     * Compute the preferredVidths of all columns.
     */
    void updatePreferredWidths() {
        Enumeration<TableColumn> en = getColumnModel().getColumns();
        while (en.hasMoreElements()) {
            TableColumn obj = en.nextElement();
            if (obj instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn) obj;
                etc.updatePreferredWidth(this, false);
            }
        }
    }
  
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, String propertyPrefix) {
        ETableColumnModel etcm = (ETableColumnModel)createDefaultColumnModel();
        etcm.readSettings(p, propertyPrefix, this);
        setColumnModel(etcm);
        
        String scs = p.getProperty(propertyPrefix + SEARCH_COLUMN);
        if (scs != null) {
            try {
                int index = Integer.parseInt(scs);
                for (int i = 0; i < etcm.getColumnCount(); i++) {
                    TableColumn tc = etcm.getColumn(i);
                    if (tc.getModelIndex() == index) {
                        searchColumn = (ETableColumn)tc;
                        break;
                    }
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        filteredRowCount = -1;
        resetPermutation ();
        super.tableChanged(new TableModelEvent(getModel()));
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, String propertyPrefix) {
        TableColumnModel tcm = getColumnModel();
        if (tcm instanceof ETableColumnModel) {
            ETableColumnModel etcm = (ETableColumnModel) tcm;
            etcm.writeSettings(p, propertyPrefix);
        }
        if (searchColumn != null) {
            p.setProperty(
                propertyPrefix + SEARCH_COLUMN,
                Integer.toString(searchColumn.getModelIndex()));
        }
    }
    
    /** searchTextField manages focus because it handles VK_TAB key */
    private class SearchTextField extends JTextField {
        @Override
        @SuppressWarnings("deprecation")
        public boolean isManagingFocus() {
            return true;
        }
        
        @Override
        public void processKeyEvent(KeyEvent ke) {
            //override the default handling so that
            //the parent will never receive the escape key and
            //close a modal dialog
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                // bugfix #32909, reqest focus when search field is removed
                SwingUtilities.invokeLater(new Runnable() {
                    //additional bugfix - do focus change later or removing
                    //the component while it's focused will cause focus to
                    //get transferred to the next component in the
                    //parent focusTraversalPolicy *after* our request
                    //focus completes, so focus goes into a black hole - Tim
                    @Override
                    public void run() {
                        ETable.this.requestFocus();
                    }
                });
            } else {
                super.processKeyEvent(ke);
            }
        }
    }

    /**
     * Searches the rows by comparing the values with the given prefix.
     */
    private List<Integer> doSearch(String prefix) {
        List<Integer> results = new ArrayList<Integer>();
        
        int startIndex = 0;
        int size = getRowCount();
        if ( (size == 0) || (getColumnCount() == 0)) {
            // Empty table; cannot match anything.
            return results;
        }
        
        int column = 0;
        if (searchColumn != null) {
            column = convertColumnIndexToView(searchColumn.getModelIndex());
        }
        if (column < 0) {
            // wrong column
            return results;
        }
        while (startIndex < size) {
            Object val = getValueAt(startIndex, column);
            String s = null;
            if (val != null) {
                s = convertValueToString(val);
            }   
            if ((s != null) && (s.toUpperCase().indexOf(prefix.toUpperCase()))!= -1   ) {
                results.add(new Integer(startIndex));
            
                // initialize prefix
                if (maxPrefix == null) {
                    maxPrefix = s;
                }

                maxPrefix = findMaxPrefix(maxPrefix, s);
            }
            
            startIndex++;
        }
        return results;
    }
    
    /**
     * Finds maximum common prefix of 2 strings.
     */
    private static String findMaxPrefix(String str1, String str2) {
        int i = 0;
        while (str1.regionMatches(true, 0, str2, 0, i)) {
            i++;
        }
        i--;
        if (i >= 0) {
            return str1.substring(0, i);    
        }
        return null;
    }

    /**
     * Shows the search text field.
     */
    private void setupSearch() {
        // Remove the default key listeners
        KeyListener keyListeners[] = (getListeners (KeyListener.class));
        for (int i = 0; i < keyListeners.length; i++) {
            removeKeyListener(keyListeners[i]);
        }
        // Add new key listeners
        addKeyListener(new KeyAdapter() {
            private boolean armed = false;
            @Override
            public void keyPressed(KeyEvent e) {
                int modifiers = e.getModifiers();
                int keyCode = e.getKeyCode();
                if ((modifiers > 0 && modifiers != KeyEvent.SHIFT_MASK) || e.isActionKey())
                    return ;
                char c = e.getKeyChar();
                if (!Character.isISOControl(c) && keyCode != KeyEvent.VK_SHIFT && keyCode != KeyEvent.VK_ESCAPE) {
                    armed = true;
                    e.consume();
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                if (armed) {
                    final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                    searchTextField.setText(String.valueOf(stroke.getKeyChar()));
                    
                    displaySearchField();
                    e.consume();
                    armed = false;
                }
            }
        });
        // Create a the "multi-event" listener for the text field. Instead of
        // adding separate instances of each needed listener, we're using a
        // class which implements them all. This approach is used in order
        // to avoid the creation of 4 instances which takes some time
        SearchFieldListener searchFieldListener = new SearchFieldListener();
        searchTextField.addKeyListener(searchFieldListener);
        searchTextField.addFocusListener(searchFieldListener);
        searchTextField.getDocument().addDocumentListener(searchFieldListener);
    }

    /**
     * Listener showing and operating the search box.
     */
    private class SearchFieldListener extends KeyAdapter
            implements DocumentListener, FocusListener {
        
        /** The last search results */
        private List<Integer> results = new ArrayList<>();
        /** The last selected index from the search results. */
        private int currentSelectionIndex;
        
        /**
         * Default constructor.
         */
        SearchFieldListener() {
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            searchForRow();
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            searchForRow();
        }
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            searchForRow();
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                ETable.this.requestFocus();
            } else if (keyCode == KeyEvent.VK_UP) {
                currentSelectionIndex--;
                displaySearchResult();
                // Stop processing the event here. Otherwise it's dispatched
                // to the table too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                currentSelectionIndex++;
                displaySearchResult();
                // Stop processing the event here. Otherwise it's dispatched
                // to the table too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_TAB) {
                if (maxPrefix != null) {
                    searchTextField.setText(maxPrefix);
                }
                e.consume();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                removeSearchField();
                
                // TODO: do something on hitting enter???
                e.consume();
                ETable.this.requestFocus();
            }
        }
        
        /** Searches for a row. */
        private void searchForRow() {
            currentSelectionIndex = 0;
            results.clear();
            maxPrefix = null;
            String text = searchTextField.getText().toUpperCase();
            if (text.length() > 0) {
                results = doSearch(text);
                // do search forward the selected index
                int rows[] = getSelectedRows();
                int selectedRowIndex = (rows == null || rows.length == 0) ? 0 : rows[0];
                int r = 0;
                for (Iterator<Integer> it = results.iterator(); it.hasNext(); r++) {
                    int curResult = it.next().intValue();
                    if (selectedRowIndex <= curResult) {
                        currentSelectionIndex = r;
                        break;
                    }
                }
                displaySearchResult();
            }
        }
        
        private void displaySearchResult() {
            int sz = results.size();
            if (sz > 0) {
                if (currentSelectionIndex < 0) {
                    currentSelectionIndex = 0;
                }
                if (currentSelectionIndex >= sz) {
                    currentSelectionIndex = sz - 1;
                }
                int selRow = results.get(currentSelectionIndex).intValue();
                setRowSelectionInterval(selRow, selRow);
                Rectangle rect = getCellRect(selRow, 0, true);
                scrollRectToVisible(rect);
                displaySearchField();
            } else {
                clearSelection();
            }
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            // Do nothing
        }
        
        @Override
        public void focusLost(FocusEvent e) {
            Component c = e.getOppositeComponent();
            if (c != searchCombo) {
                removeSearchField();
            }
        }
    }
    
    /**
     * Listener showing and operating the search box.
     */
    private class SearchComboListener extends KeyAdapter
            implements FocusListener, ItemListener {
        
        /**
         * Default constructor.
         */
        SearchComboListener() {
        }
        
        @Override
        public void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
            Object selItem = searchCombo.getSelectedItem();
            for (Enumeration<TableColumn> en = getColumnModel().getColumns(); en.hasMoreElements(); ) {
                TableColumn column = en.nextElement();
                if (column instanceof ETableColumn) {
                    ETableColumn etc = (ETableColumn)column;
                    Object value = etc.getHeaderValue();
                    String valueString = "";
                    if (value != null) {
                        valueString = value.toString();
                    }
                    valueString = getColumnDisplayName(valueString);
                    if (valueString.equals(selItem)) {
                        searchColumn = etc;
                    }
                }
            }

            String text = searchTextField.getText();
            searchTextField.setText("");
            searchTextField.setText(text);
            searchTextField.requestFocus();
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                ETable.this.requestFocus();
            }
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            // Do nothing
        }
        
        @Override
        public void focusLost(FocusEvent e) {
            Component c = e.getOppositeComponent();
            if (c != searchTextField) {
                removeSearchField();
            }
        }
    }
    
    private void prepareSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new JPanel();
            String s = UIManager.getString("LBL_QUICKSEARCH");
            if (s == null) {
                s = "Quick search in";
            }
            JLabel lbl = new JLabel(s); //NOI18N
            searchPanel.setLayout (
                new BoxLayout(searchPanel, BoxLayout.X_AXIS));
            searchPanel.add (lbl);
            searchCombo = new JComboBox(getSearchComboModel());
            
            if (searchColumn != null) {
                Object value = searchColumn.getHeaderValue();
                String valueString = "";
                if (value != null) {
                    valueString = value.toString();
                }
                valueString = getColumnDisplayName(valueString);
                searchCombo.setSelectedItem(valueString);
            }
            SearchComboListener scl = new SearchComboListener();
            searchCombo.addItemListener(scl);
            searchCombo.addFocusListener(scl);
            searchCombo.addKeyListener(scl);
            searchPanel.add(searchCombo);
            searchPanel.add (searchTextField);
            lbl.setLabelFor(searchTextField);
            searchPanel.setBorder (BorderFactory.createRaisedBevelBorder());
            lbl.setBorder (BorderFactory.createEmptyBorder (0, 0, 0, 5));
        }
    }
    
    private ComboBoxModel<String> getSearchComboModel() {
        DefaultComboBoxModel<String> result = new DefaultComboBoxModel<>();
        for (Enumeration<TableColumn> en = getColumnModel().getColumns(); en.hasMoreElements(); ) {
            TableColumn column = en.nextElement();
            if (column instanceof ETableColumn) {
                ETableColumn etc = (ETableColumn)column;
                Object value = etc.getHeaderValue();
                String valueString = "";
                if (value != null) {
                    valueString = value.toString();
                }
                valueString = getColumnDisplayName(valueString);
                result.addElement(valueString);
            }
        }
        return result;
    }

    /**
     * Shows the search field.
     */
    public void displaySearchField() {
        if (!searchTextField.isDisplayable()) {
            searchTextField.setFont(ETable.this.getFont());
            prepareSearchPanel();
            add(searchPanel);
        }
        doLayout();
        invalidate();
        validate();
        repaint();
        searchTextField.requestFocus();
    }

    /**
     * Overridden to place the search text field.
     * @see javax.swing.JTable#doLayout()
     */
    @Override
    public void doLayout() {
        super.doLayout();
        Rectangle visibleRect = getVisibleRect();
        if (searchPanel != null && searchPanel.isDisplayable()) {
             int width = Math.min (
                visibleRect.width - SEARCH_FIELD_SPACE * 2,
                searchPanel.getPreferredSize().width - searchTextField.getPreferredSize().width + 
                     SEARCH_FIELD_PREFERRED_SIZE - SEARCH_FIELD_SPACE);

             searchPanel.setBounds(
                Math.max (SEARCH_FIELD_SPACE, 
                visibleRect.x + visibleRect.width - width),
                visibleRect.y + SEARCH_FIELD_SPACE,
                Math.min (visibleRect.width, width) - SEARCH_FIELD_SPACE,
                heightOfTextField);
        }
    }
    
    /**
     * Removes the search field from the table.
     */
    private void removeSearchField() {
        if (searchPanel.isDisplayable()) {
            remove(searchPanel);
            Rectangle r = searchPanel.getBounds();
            this.repaint(r);
        }
    }
    
    /**
     * Item to the collection when doing the sorting of table rows.
     */
    public static final class RowMapping {
        // index (of the row) in the TableModel
        private final int originalIndex;
        // table model of my table
        private final TableModel model;
        // The table
        private final ETable table;
        // The cached transformed value
        private Object transformed = TRANSFORMED_NONE;
        // The column of the transformed value
        private int transformedColumn;
        // When values from more columns are requested, the transformed values
        // are stored in this array
        private Object[] allTransformed = null;
        
        private static final Object TRANSFORMED_NONE = new Object();

        /**
         * Create a new table row mapping.
         * @param index The row index
         * @param model The table model
         */
        public RowMapping(int index, TableModel model) {
            originalIndex = index;
            this.model = model;
            this.table = null;
        }
        
        /**
         * Create a new table row mapping.
         * @param index The row index
         * @param model The table model
         * @param table The table. This argument needs to be set when
         * {@link #getTransformedValue(int)} is to be used.
         * @since 1.19
         */
        public RowMapping(int index, TableModel model, ETable table) {
            originalIndex = index;
            this.model = model;
            this.table = table;
        }
        
        /**
         * Get the model row index.
         * @return The model row index
         */
        public int getModelRowIndex() {
            return originalIndex;
        }
        
        /**
         * Get the model object at the row index of this mapping and the given column.
         * @param column The column
         * @return The model object
         * @see #getTransformedValue(int)
         */
        public Object getModelObject(int column) {
            return model.getValueAt(originalIndex, column);
        }
        
        /**
         * Get the transformed value. This method assures that we retrieve every
         * value only once per any number of calls. The table needs to be set
         * in order to be able to transform the value, see
         * {@link #RowMapping(int, javax.swing.table.TableModel, org.netbeans.swing.etable.ETable)}.
         * 
         * @param column The column
         * @return The transformed value. It returns the cached value of
         * <code>table.transformValue(getModelObject(column))</code>.
         * @throws IllegalStateException when table is not set
         * @since 1.19
         */
        public Object getTransformedValue(int column) {
            if (column >= model.getColumnCount()) { // #239045
                return null;
            }
            if (table == null) {
                throw new IllegalStateException("The table was not set.");
            }
            Object value;
            if (TRANSFORMED_NONE == transformed) {
                value = transformed = table.transformValue(getModelObject(column));
                transformedColumn = column;
            } else if (allTransformed != null) {
                if (allTransformed.length <= column) {
                    Object[] newTransformed = new Object[column + 1];
                    System.arraycopy(allTransformed, 0, newTransformed, 0, allTransformed.length);
                    for (int i = allTransformed.length; i < newTransformed.length; i++) {
                        newTransformed[i] = TRANSFORMED_NONE;
                    }
                    allTransformed = newTransformed;
                }
                if (TRANSFORMED_NONE == allTransformed[column]) {
                    value = allTransformed[column] = table.transformValue(getModelObject(column));
                } else {
                    value = allTransformed[column];
                }
            } else if (transformedColumn != column) {
                int n = Math.max(transformedColumn, column) + 1;
                allTransformed = new Object[n];
                for (int i = 0; i < n; i++) {
                    allTransformed[i] = TRANSFORMED_NONE;
                }
                allTransformed[transformedColumn] = transformed;
                value = allTransformed[column] = table.transformValue(getModelObject(column));
            } else {
                value = transformed;
            }
            return value;
        }
        
    }
    
    /** 
     * Comparator for RowMapping objects that sorts according
     * to the original indices of the rows in the model.
     */
    static class OriginalRowComparator implements Comparator<RowMapping> {
        public OriginalRowComparator() {
        }
        @Override
        public int compare(RowMapping rm1, RowMapping rm2) {
            int i1 = rm1.getModelRowIndex();
            int i2 = rm2.getModelRowIndex();
            return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
        }
    }
    
    private void showColumnSelection(MouseEvent me) {
        ColumnSelection cs = getColumnSelectionOn(me.getButton());
        switch (cs) {
            case POPUP:
                ColumnSelectionPanel.showColumnSelectionPopup (me.getComponent (), me.getX(), me.getY(), ETable.this);
                break;
            case DIALOG:
                ColumnSelectionPanel.showColumnSelectionDialog(ETable.this);
                break;
        }
    }
    
    /**
     * Mouse listener attached to the scroll pane of this table, handles the case
     * when no columns are displayed.
     */
    private class ColumnSelectionMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.getButton() != MouseEvent.BUTTON1) {
                showColumnSelection(me);
            }
        }
    }
    /**
     * Mouse listener attached to the JTableHeader of this table. Single
     * click on the table header should trigger sorting on that column.
     * Double click on the column divider automatically resizes the column.
     */
    private class HeaderMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.getButton() == MouseEvent.BUTTON3) { // Other buttons are reserved for sorting
                showColumnSelection(me);
                return;
            }
            TableColumn resColumn = getResizingColumn(me.getPoint());
            if (sortable && (resColumn == null) && (me.getClickCount() == 1)) {
                // ok, do the sorting
                int column = columnAtPoint(me.getPoint());
                if (column < 0) return ;
                TableColumnModel tcm = getColumnModel();
                if (tcm instanceof ETableColumnModel) {
                    ETableColumnModel etcm = (ETableColumnModel)tcm;
                    TableColumn tc = tcm.getColumn(column);
                    if (tc instanceof ETableColumn) {
                        ETableColumn etc = (ETableColumn)tc;
                        if (! etc.isSortingAllowed()) {
                            return;
                        }
                        SelectedRows selectedRows;
                        int wasSelectedColumn;
                        if (getUpdateSelectionOnSort()) {
                            selectedRows = getSelectedRowsInModel();
                            wasSelectedColumn = getSelectedColumn();
                        } else {
                            selectedRows = null;
                            wasSelectedColumn = -1;
                        }
                        boolean clear = ((me.getModifiers() & InputEvent.SHIFT_MASK) != InputEvent.SHIFT_MASK);
                        etcm.toggleSortedColumn(etc, clear);
                        resetPermutation ();
                        ETable.super.tableChanged(new TableModelEvent(getModel(), 0, getRowCount() - 1));
                        if (selectedRows != null) {
                            changeSelectionInModel(selectedRows, wasSelectedColumn);
                        }
                        getTableHeader().resizeAndRepaint();
                    }
                }
            }
            if ((resColumn != null) && (me.getClickCount() == 2)) {
                // update the column width
                if (resColumn instanceof ETableColumn) {
                    ETableColumn etc = (ETableColumn)resColumn;
                    etc.updatePreferredWidth(ETable.this, true);
                }
            }
        }
    }

    /**
     * Overridden to force requesting the focus after the user starts editing.
     * @see javax.swing.JTable#editCellAt(int, int, EventObject)
     */
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        inEditRequest = true;
        if (editingRow == row && editingColumn == column && isEditing()) {
            //discard edit requests if we're already editing that cell
            inEditRequest = false;
            return false;
        }
        
        if (isEditing()) {
            removeEditor();
            changeSelection(row, column, false, false);
        }
        
        try {
            boolean ret = super.editCellAt(row, column, e);
            if (ret) {
                editorComp.requestFocus();
            }

            return ret;
        } finally {
            inEditRequest = false;
        }
    }

    /**
     * Overridden to track whether the remove request is in progress.
     * @see javax.swing.JTable#removeEditor()
     */
    @Override
    public void removeEditor() {
        inRemoveRequest = true;
        try {
            synchronized (getTreeLock()) {
                super.removeEditor();
            }
        } finally {
            inRemoveRequest = false;
        }
    }    
    
    /**
     * Checks whether the given component is "our".
     */
    private boolean isKnownComponent(Component c) {
        if (c == null) return false;
        if (isAncestorOf (c)) {
            return true;
        }
        if (c == editorComp) {
            return true;
        }
        if (editorComp instanceof Container &&
            ((Container) editorComp).isAncestorOf(c)) {
                return true;
        }
        return false;
    }
    
    /**
     * Focus transfer policy that retains focus after closing an editor.
     * Copied wholesale from org.openide.explorer.view.TreeTable.
     */
    private class STPolicy extends ContainerOrderFocusTraversalPolicy {
        
        @Override
        public Component getComponentAfter(Container focusCycleRoot,
        Component aComponent) {
            
            if (inRemoveRequest) {
                return ETable.this;
            } else {
                Component result = super.getComponentAfter(focusCycleRoot, aComponent);
                return result;
            }
        }
        
        @Override
        public Component getComponentBefore(Container focusCycleRoot,
        Component aComponent) {
            if (inRemoveRequest) {
                return ETable.this;
            } else {
                return super.getComponentBefore(focusCycleRoot, aComponent);
            }
        }
        
        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            if (!inRemoveRequest && isEditing()) {
                return editorComp;
            } else {
                return ETable.this;
            }
        }
        
        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            if (inRemoveRequest && isEditing() && editorComp.isShowing()) {
                return editorComp;
            } else {
                return ETable.this;
            }
        }
        
        @Override
        protected boolean accept(Component aComponent) {
            //Do not allow focus to go to a child of the editor we're using if
            //we are in the process of removing the editor
            if (isEditing() && inEditRequest) {
                return isKnownComponent (aComponent);
            }
            return super.accept(aComponent) && aComponent.isShowing();
        }
    }
    
    /**
     * Enables tab keys to navigate between rows but also exit the table
     * to the next focusable component in either direction.
     */
    private final class NavigationAction extends AbstractAction {
        
        /** true is forward direction */
        private boolean direction;
        
        public NavigationAction(boolean direction) {
            this.direction = direction;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEditing()) {
                removeEditor();
            }
            int targetRow;
            int targetColumn;
            if (direction) {
                if (getSelectedColumn() == getColumnCount()-1) {
                    targetColumn=0;
                    targetRow = getSelectedRow()+1;
                } else {
                    targetColumn = getSelectedColumn()+1;
                    targetRow = getSelectedRow();
                }
            } else {
                if (getSelectedColumn() == 0) {
                    targetColumn = getColumnCount()-1;
                    targetRow = getSelectedRow()-1;
                } else {
                    targetRow = getSelectedRow();
                    targetColumn = getSelectedColumn() -1;
                }
            }
            
            //if we're off the end, try to find a sibling component to pass
            //focus to
            if (targetRow >= getRowCount() || targetRow < 0) {
                //This code is a bit ugly, but works
                Container ancestor = getFocusCycleRootAncestor();
                //Find the next component in our parent's focus cycle
                Component sibling = direction ?
                    ancestor.getFocusTraversalPolicy().getComponentAfter(ancestor,
                    ETable.this) :
                    ancestor.getFocusTraversalPolicy().getComponentBefore(ancestor,
                    ETable.this);
                    

                //Often LayoutFocusTranferPolicy will return ourselves if we're
                //the last.  First try to find a parent focus cycle root that
                //will be a little more polite
                if (sibling == ETable.this) {
                    Container grandcestor = ancestor.getFocusCycleRootAncestor();
                    if (grandcestor != null) {
                        sibling = direction ? grandcestor.getFocusTraversalPolicy().getComponentAfter(grandcestor, ancestor) :
                            grandcestor.getFocusTraversalPolicy().getComponentBefore(grandcestor, ancestor);
                        ancestor = grandcestor;
                    }
                }
                 
                //Okay, we still ended up with ourselves, or there is only one focus
                //cycle root ancestor.  Try to find the first component according to
                //the policy
                if (sibling == ETable.this) {
                    if (ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor) != null) {
                        sibling = ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor);
                    }
                }
                
                //If we're *still* getting ourselves, find the default button and punt
                if (sibling == ETable.this) {
                    JRootPane rp = getRootPane();
                    JButton jb = rp.getDefaultButton();
                    if (jb != null) {
                        sibling = jb;
                    }
                }
                    
                //See if it's us, or something we know about, and if so, just
                //loop around to the top or bottom row - there's noplace
                //interesting for focus to go to
                if (sibling != null) {
                    if (sibling == ETable.this) {
                        //set the selection if there's nothing else to do
                        changeSelection(direction ? 0 : getRowCount()-1, 
                            direction ? 0 : getColumnCount()-1,false,false);
                    } else {
                        //Request focus on the sibling
                        sibling.requestFocus();
                    }
                    return;
                }
            }
            changeSelection (targetRow, targetColumn, false, false);
        }
    }

    /** Used to explicitly invoke editing from the keyboard */
    private class EditAction extends AbstractAction {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = getSelectedRow();
            int col = getSelectedColumn();
            int[] selectedRows = getSelectedRows();
            boolean edited = editCellAt(row, col, e);
            if (!edited) {
                for (int r : selectedRows) {
                    if (r != row) {
                        edited = editCellAt(r, col, e);
                        if (edited) {
                            break;
                        }
                    }
                }
            }
        }
        
        @Override
        public boolean isEnabled() {
            return getSelectedRow() != -1 && getSelectedColumn() != -1 && !isEditing();
        }
    }
    
    /**
     * Either cancels an edit, or closes the enclosing dialog if present.
     */
    private class CancelEditAction extends AbstractAction {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEditing() || editorComp != null) {
                removeEditor();
                return;
            } else {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                
                InputMap imp = getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = getRootPane().getActionMap();
                
                KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                Object key = imp.get(escape);
                if (key == null) {
                    //Default for NbDialog
                    key = "Cancel";
                }
                if (key != null) {
                    Action a = am.get(key);
                    if (a != null) {
                        String commandKey = (String)a.getValue(Action.ACTION_COMMAND_KEY);
                        if (commandKey == null) {
                            commandKey = key.toString();
                        }
                        a.actionPerformed(new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, commandKey)); //NOI18N
                    }
                }
            }
        }
        
        @Override
        public boolean isEnabled() {
            //            return isEditing();
            return true;
        }
    }
    
    /**
     * Action for the keyboard event of hitting the Enter key.
     */
    private class EnterAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            JRootPane jrp = getRootPane();
            if (jrp != null) {
                JButton b = getRootPane().getDefaultButton();
                if (b != null && b.isEnabled()) {
                    b.doClick();
                }
            }
        }
        
        @Override
        public boolean isEnabled() {
            return !isEditing() && !inRemoveRequest;
        }
    }
    
    /**
     * CTRL-Tab transfers focus up.
     */
    private class CTRLTabAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            setFocusCycleRoot(false);
            try {
                Container con = ETable.this.getFocusCycleRootAncestor();
                if (con != null) {
                    /*
                    Component target = ETable.this;
                    if (getParent() instanceof JViewport) {
                        target = getParent().getParent();
                        if (target == con) {
                            target = ETable.this;
                        }
                    }
                    */

                    EventObject eo = EventQueue.getCurrentEvent();
                    boolean backward = false;
                    if (eo instanceof KeyEvent) {
                        backward = 
                            (((KeyEvent) eo).getModifiers() 
                            & KeyEvent.SHIFT_MASK) 
                            != 0 && (((KeyEvent) eo).getModifiersEx() & 
                            KeyEvent.SHIFT_DOWN_MASK) != 0;
                    }

                    Component c = ETable.this;
                    Component to;
                    Container parentWithFTP = null;
                    do {
                        FocusTraversalPolicy ftp = con.getFocusTraversalPolicy();
                        to = backward ? ftp.getComponentBefore(con, c)
                                      : ftp.getComponentAfter(con, c);


                        if (to == ETable.this) {
                            to = backward ? ftp.getFirstComponent(con)
                                          : ftp.getLastComponent(con);
                        }
                        if (to == ETable.this) {
                            parentWithFTP = con.getParent();
                            if (parentWithFTP != null) {
                                parentWithFTP = parentWithFTP.getFocusCycleRootAncestor();
                            }
                            if (parentWithFTP != null) {
                                c = con;
                                con = parentWithFTP;
                            }
                        }
                    } while (to == ETable.this && parentWithFTP != null);
                    if (to != null) {
                        to.requestFocus();
                    }
                }
            } finally {
                setFocusCycleRoot(true);
            }
        }
    }
    
    /**
     * Allows to supply alternative implementation of the column
     * selection functionality in ETable. If columnSelector is null
     * the defaultColumnSelector is returned by this method.
     */
    public TableColumnSelector getColumnSelector() {
        return columnSelector != null ? columnSelector : defaultColumnSelector;
    }

    /**
     * Allows to supply alternative implementation of the column
     * selection functionality in ETable.
     */
    public void setColumnSelector(TableColumnSelector columnSelector) {
        this.columnSelector = columnSelector;
    }

    /**
     * The column selection corner can use either dialog or popup menu.
     * 
     * @return <code>true</code>, when left mouse click invokes a popup menu,
     * or <code>false</code>, when left mouse click opens a dialog for column selection.
     */
    public boolean isPopupUsedFromTheCorner() {
        synchronized (columnSelectionOnMouseClickLock) {
            ColumnSelection cs = columnSelectionOnMouseClick[1];
            return cs == ColumnSelection.POPUP;
        }
    }

    /**
     * The column selection corner can use either dialog or popup menu.<br>
     * This method is equivalent to {@link #setColumnSelectionOn(int, org.netbeans.swing.etable.ETable.ColumnSelection)}
     * with arguments <code>1</code> and appropriate column selection constant.
     * 
     * @param popupUsedFromTheCorner When <code>true</code>, left mouse click invokes a popup menu,
     * when <code>false</code>, left mouse click opens a dialog for column selection.
     */
    public void setPopupUsedFromTheCorner(boolean popupUsedFromTheCorner) {
        synchronized (columnSelectionOnMouseClickLock) {
            columnSelectionOnMouseClick[1] = popupUsedFromTheCorner ? ColumnSelection.POPUP : ColumnSelection.DIALOG;
        }
    }
    
    /**
     * Get the column selection method, that is displayed as a response to the
     * mouse event. A popup with column selection menu, or column selection
     * dialog can be displayed.<br>
     * By default, popup menu is displayed on button3 mouse click
     * and dialog or popup menu is displayed on the corner
     * button1 mouse action, depending on the value of {@link #isPopupUsedFromTheCorner()}
     * 
     * @param mouseButton The button of the mouse event
     * @return The column selection method.
     * @since 1.17
     */
    public ColumnSelection getColumnSelectionOn(int mouseButton) {
        if (mouseButton < 0) {
            throw new IllegalArgumentException("Button = "+mouseButton);
        }
        synchronized (columnSelectionOnMouseClickLock) {
            if (mouseButton >= columnSelectionOnMouseClick.length) {
                return null;
            }
            return columnSelectionOnMouseClick[mouseButton];
        }
    }
    
    /**
     * Set if popup with column selection menu or column selection dialog
     * should be displayed as a response to the mouse event.<br>
     * 
     * @param mouseButton The button of the mouse event
     * @param selection The column selection method.
     * @since 1.17
     */
    public void setColumnSelectionOn(int mouseButton, ColumnSelection selection) {
        if (mouseButton < 0) {
            throw new IllegalArgumentException("Button = "+mouseButton);
        }
        synchronized (columnSelectionOnMouseClickLock) {
            if (mouseButton >= columnSelectionOnMouseClick.length) {
                ColumnSelection[] csp = new ColumnSelection[mouseButton + 1];
                System.arraycopy(columnSelectionOnMouseClick, 0, csp, 0, columnSelectionOnMouseClick.length);
                columnSelectionOnMouseClick = csp;
            }
            columnSelectionOnMouseClick[mouseButton] = selection;
        }
    }
    
    /**
     * Shows dialog that allows to show/hide columns.
     * @since 1.17
     */
    public final void showColumnSelectionDialog() {
        ColumnSelectionPanel.showColumnSelectionDialog(this);
    }

    /**
     * Default column selector is used when columnSelector is null.
     */
    public static TableColumnSelector getDefaultColumnSelector() {
        return defaultColumnSelector;
    }

    /**
     * Allows to supply the default column selector for all instances
     * of ETable.
     */
    public static void setDefaultColumnSelector(TableColumnSelector aDefaultColumnSelector) {
        defaultColumnSelector = aDefaultColumnSelector;
    }

    private static final class SelectedRows {
        int anchorInView;
        int anchorInModel;
        int leadInView;
        int leadInModel;
        int[] rowsInView;
        int[] rowsInModel;

        @Override
        public String toString() {
            return "SelectedRows[anchorInView="+anchorInView+", anchorInModel="+anchorInModel+
                    ", leadInView="+leadInView+", leadInModel="+leadInModel+
                    ", rowsInView="+Arrays.toString(rowsInView)+", rowsInModel="+Arrays.toString(rowsInModel)+"]";
        }
        
    }
    
    // JDK 6 methods added to JTable:

    @Override
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        if (getColumnModel() instanceof ETableColumnModel) {
            if (autoCreateRowSorter) {
                throw new UnsupportedOperationException("ETable with ETableColumnModel has it's own sorting mechanism. JTable's RowSorter can not be used.");
            }
        }
        super.setAutoCreateRowSorter(autoCreateRowSorter);
    }

    /* Added and not needed to be overidden.
    @Override
    public void setUpdateSelectionOnSort(boolean update) {
        super.setUpdateSelectionOnSort(update);
    }

    @Override
    public boolean getUpdateSelectionOnSort() {
        return super.getUpdateSelectionOnSort();
    }
     */

    /**
     * When ETable has ETableColumnModel set, only <code>null</code> sorter
     * is accepted, which turns off sorting. Otherwise UnsupportedOperationException is thrown.
     * RowSorter can be used when a different TableColumnModel is set.
     * 
     * @param sorter {@inheritDoc}
     */
    @Override
    public void setRowSorter(RowSorter<? extends TableModel> sorter) {
        if (getColumnModel() instanceof ETableColumnModel) {
            if (sorter == null) {
                sortable = false;
                ((ETableColumnModel) getColumnModel()).clearSortedColumns();
            } else {
                throw new UnsupportedOperationException(
                        "ETable with ETableColumnModel has it's own sorting mechanism. Use ETableColumnModel to define sorting, or set a different TableColumnModel.");
            }
        } else {
            super.setRowSorter(sorter);
        }
    }

    /**
     * Get the RowSorter in case that the ETable does not have ETableColumnModel set.
     * @return {@inheritDoc}
     */
    @Override
    public RowSorter<? extends TableModel> getRowSorter() {
        if (getColumnModel() instanceof ETableColumnModel) {
            return null;
        } else {
            return super.getRowSorter();
        }
    }
    
    
    
}

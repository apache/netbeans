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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;


import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

import java.awt.event.*;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.sql.visualeditor.Log;

import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Where;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Value;
import org.netbeans.modules.db.sql.visualeditor.querymodel.SortSpecification;
import org.netbeans.modules.db.sql.visualeditor.querymodel.SQLQueryFactory;
import org.netbeans.modules.db.sql.visualeditor.querymodel.OrderBy;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Expression;
import org.netbeans.modules.db.sql.visualeditor.querymodel.And;
import org.netbeans.modules.db.sql.visualeditor.querymodel.ExpressionList;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Literal;

/**
 *
 * @author  Sanjay Dhamankar, Jim Davidson
 */

// One of the panes of the QueryBuilder
// The model has columns as laid out by the constants QueryBuilderGraphFrame.
// There are 9 columns, of which the most important are
// - Column (column name)
// - Output (selected status)
// - Table (table spec).

public class QueryBuilderInputTable extends JTable
    implements ActionListener, ItemListener, KeyListener
    // , MouseListener
{
    // Constants for offsets into the inputTableModel
    // These aren't used much; they should probably be defined on the QBInputTableModel class

    public static final int Column_COLUMN =     0;
    public static final int Alias_COLUMN  =     1;
    public static final int Table_COLUMN  =     2;
    public static final int Output_COLUMN =     3;
    public static final int SortType_COLUMN =   4;
    public static final int SortOrder_COLUMN =  5;
    public static final int Criteria_COLUMN =   6;
    public static final int CriteriaOrder_COLUMN =  7;


    public static final int Column_COLUMN_WIDTH =     140;
    public static final int Alias_COLUMN_WIDTH  =      70;
    public static final int Table_COLUMN_WIDTH  =     180;
    // this is checkbox, no need to set the width
    // public static final int Output_COLUMN_WIDTH =      80;
    public static final int SortType_COLUMN_WIDTH =    80;
    public static final int SortOrder_COLUMN_WIDTH =   80;
    public static final int Criteria_COLUMN_WIDTH =   100;
    public static final int CriteriaOrder_COLUMN_WIDTH =  40;

    public static final String Criteria_Uneditable_String =  "*****";
    public static final String CriteriaOrder_Uneditable_String =  "*";
    // Private variables

    private static final boolean        DEBUG = false;
    private QueryBuilder                _queryBuilder;
    private JPopupMenu                  _inputTablePopup;
    private int                         _inputTablePopupRow;
    private int                         _inputTablePopupColumn;
    private JComboBox                   _sortOrderComboBox;  // this instancevar, not localvar
    private JComboBox                   _criteriaOrderComboBox;

    AddQueryParameterDlg                _addQueryParameterDlg = null;


    // Constructor

    public QueryBuilderInputTable(QueryBuilder queryBuilder) {

        super();

	Log.getLogger().entering("QueryBuilderInputTable", "constructor"); // NOI18N

        _queryBuilder = queryBuilder;

        QueryBuilderInputTableModel queryBuilderInputTableModel = new QueryBuilderInputTableModel();

        super.setModel( queryBuilderInputTableModel );

        TableColumn column = this.getColumnModel().getColumn(Column_COLUMN);
        column.setPreferredWidth(Column_COLUMN_WIDTH);

        column = this.getColumnModel().getColumn(Alias_COLUMN);
        column.setPreferredWidth(Alias_COLUMN_WIDTH);
        column.setCellEditor(new FocusCellEditor(new JTextField()));

        column = this.getColumnModel().getColumn(Table_COLUMN);
        column.setPreferredWidth(Table_COLUMN_WIDTH);

        column = this.getColumnModel().getColumn(SortType_COLUMN);
        column.setPreferredWidth(SortType_COLUMN_WIDTH);

        column = this.getColumnModel().getColumn(SortOrder_COLUMN);
        column.setPreferredWidth(SortOrder_COLUMN_WIDTH);

        column = this.getColumnModel().getColumn(Criteria_COLUMN);
        column.setPreferredWidth(Criteria_COLUMN_WIDTH);
        column.setCellEditor(new FocusCellEditor(new JTextField()));

        column = this.getColumnModel().getColumn(CriteriaOrder_COLUMN);
        column.setPreferredWidth(CriteriaOrder_COLUMN_WIDTH);

        this.getColumnModel().getColumn(0).setCellEditor(
            new FocusCellEditor(new JTextField()));

        final Object[] sortTypeItems = {
            "", 
            NbBundle.getMessage(QueryBuilderInputTable.class, "ASCENDING"), // NOI18N
            NbBundle.getMessage(QueryBuilderInputTable.class, "DESCENDING") // NOI18N
        }; 
        TableColumn sortTypeColumn = this.getColumnModel().getColumn(SortType_COLUMN);
        JComboBox sortTypeComboBox = new JComboBox(sortTypeItems);
        sortTypeColumn.setCellEditor(new DefaultCellEditor(sortTypeComboBox));
        sortTypeComboBox.addItemListener(this);

        final Object[] sortOrderItems = {""};       // NOI18N
        TableColumn sortOrderColumn = this.getColumnModel().getColumn(SortOrder_COLUMN);
        _sortOrderComboBox = new JComboBox(sortOrderItems);
        sortOrderColumn.setCellEditor(new DefaultCellEditor(_sortOrderComboBox));
        _sortOrderComboBox.addItemListener(this);

        final Object[] criteriaOrderItems = {""};       // NOI18N
        TableColumn criteriaOrderColumn = this.getColumnModel().getColumn(CriteriaOrder_COLUMN);
        _criteriaOrderComboBox = new JComboBox(criteriaOrderItems);
        criteriaOrderColumn.setCellEditor(new DefaultCellEditor(_criteriaOrderComboBox));
//        _criteriaOrderComboBox.addItemListener(this);

        this.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
        _inputTablePopup = createInputTablePopup();
        MouseListener inputTablePopupListener = new InputTablePopupListener();
        super.addMouseListener(inputTablePopupListener);
        this.setMinimumSize(new Dimension (200, 200) );
        this.setBackground(Color.white);
        this.getTableHeader().setReorderingAllowed (false);

        addKeyListener(this);

//        this.getModel().addTableModelListener(this);

// Listen for checkbox selections in output column; handled by tableChange event instead
//          TableColumn outputColumn = this.getColumnModel().getColumn(Output_COLUMN);
//          JCheckBox outputCheckBox = new JCheckBox();
//          outputColumn.setCellEditor(new DefaultCellEditor(outputCheckBox));
//          outputCheckBox.addItemListener(this);
    }

    // cell editor to handle focus lost events on particular
    // cells.
    private class FocusCellEditor  extends DefaultCellEditor {
        Component c;
        public FocusCellEditor(JTextField jtf) {
	        super(jtf);
	        addFocusListener(jtf);
        }
        private void addFocusListener(Component C) {
	        super.getComponent().addFocusListener(new java.awt.event.FocusAdapter() {
	        public void focusLost(java.awt.event.FocusEvent fe) { lostFocus(); }
	    });
        }
        public void lostFocus() { 
            stopCellEditing(); 
        }
    }


    /** ignore */
    public void keyTyped(KeyEvent e) {
    }

    /** ignore */
    public void keyReleased(KeyEvent e) {
    }

    /** Handle the key pressed event and change the focus if a particular
     * key combination is pressed. */
    public void keyPressed(KeyEvent e) {
        if( e.isShiftDown() ) {
             int code = e.getKeyCode();
             switch(code) {
                 // diagram pane
                 case KeyEvent.VK_F10: 
                    JTable source = (JTable)(e.getSource());

                    if (DEBUG)
                        System.out.println( "QBIT : keyPressed called Shift+F10 Down source.isEnabled() returns : " + source.isEnabled() + "\n" );

                    if ( ! source.isEnabled () ) return;

                    // _inputTablePopupRow = source.getEditingRow();
                    _inputTablePopupRow = source.getSelectedRow();
                    _inputTablePopupColumn = source.getEditingColumn();
                    if (_inputTablePopupColumn == (Criteria_COLUMN-1)) {
                        source.setEditingColumn(Column_COLUMN);
                    }
        if (DEBUG) 
            System.out.println( "QBIT : keyPressed called\n" 
                    + " inputTablePopupRow = " + _inputTablePopupRow  // NOI18N
                    + " inputTablePopupColumn == Criteria_COLUMN " + (_inputTablePopupRow == Criteria_COLUMN ) // NOI18N
                    + " inputTablePopupColumn = " + _inputTablePopupColumn  );  // NOI18N
                // Make sure the row where click occurred is selected.
                    if (_inputTablePopupRow != -1) {
                        source.setRowSelectionInterval (_inputTablePopupRow,
                                                        _inputTablePopupRow);
                    }
                    _inputTablePopup.show ( source, source.getWidth() / 2, 
                                                    source.getHeight() / 2 );
                    break;
             }
        }
        _queryBuilder.handleKeyPress(e);
    }

    JComboBox getSortOrderComboBox () {
        return _sortOrderComboBox;  // this instancevar, not localvar
    }

    JComboBox getCriteriaOrderComboBox () {
        return _criteriaOrderComboBox; 
    }

    JPopupMenu createInputTablePopup()
    {
        JPopupMenu inputTablePopup;
        JMenu menu, subMenu;
        JMenuItem menuItem;
        JMenuItem subMenuItem;

        //Create the popup menu.
        inputTablePopup = new JPopupMenu();

        // remove for time being to avoid confusion.
        /*
          menuItem = new JMenuItem("Run Query");
          menuItem.addActionListener(this);
          inputTablePopup.add(menuItem);
        */

        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_QUERY_CRITERIA"));      // NOI18N
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

/*
        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_AND_QUERY_CRITERIA"));      // NOI18N
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_OR_QUERY_CRITERIA"));      // NOI18N
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);
*/

//         menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_SORT_SPECIFICATION"));
//         menuItem.addActionListener(this);
//         inputTablePopup.add(menuItem);

        /*
        menuItem = new JMenuItem(
            NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_CUT") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_COPY") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_PASTE") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_DELETE") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_GROUP_BY") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_CHANGE_TYPE") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);

        menuItem = new JMenuItem ( NbBundle.getMessage(QueryBuilderInputTable.class, "INPUT_TABLE_COLLAPSE_PANE") );
        menuItem.addActionListener(this);
        inputTablePopup.add(menuItem);
*/
//         menuItem = new JMenuItem("Properties");
//         menuItem.addActionListener(this);
//         inputTablePopup.add(menuItem);

        return ( inputTablePopup );
    }


    // Inner classes for handling events
    

    // Mouse listener -- bring up background menu
    class InputTablePopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            JTable source = (JTable)(e.getSource());

            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JTable source = (JTable)(e.getSource());

                if ( ! source.isEnabled () ) return;

                _inputTablePopupRow = 
                        source.rowAtPoint(new Point (e.getX(), e.getY()));
                _inputTablePopupColumn = 
                        source.columnAtPoint(new Point (e.getX(), e.getY()));
                // Make sure the row where click occurred is selected.
                if (_inputTablePopupRow != -1) {
                    source.setRowSelectionInterval (_inputTablePopupRow,
                                                    _inputTablePopupRow);
                }
//                 if  ( _inputTablePopupColumn != Criteria_COLUMN )
//                 {
//                     // return without showing popup
//                     return;
//                 }

                _inputTablePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    // Specified by ActionListener.
    // Respond to menu selections. The only menu item currently supported is "Add Criteria".
    
    public void actionPerformed(ActionEvent e) {

        JMenuItem source = (JMenuItem)(e.getSource());

        if (source.getText().equals(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_QUERY_CRITERIA")))       // NOI18N
        {    
            // Bring up a dialog for adding a query parameter, and update the table with the result
            _addQueryParameterDlg =
                new AddQueryParameterDlg(true, (String)(this.getValueAt(getSelectedRow(), Column_COLUMN)));

        if (DEBUG) 
            System.out.println( "QBIT : actionPerformed called\n_addQueryParameterDlg returns : " + (_addQueryParameterDlg.getReturnStatus() ==  AddQueryParameterDlg.RET_OK)  );  // NOI18N

            if ( _addQueryParameterDlg.getReturnStatus() == AddQueryParameterDlg.RET_OK )
            {
                // Suppress bogus text regeneration
                _queryBuilder._updateText=false;
                try {
                
                    // Split the string on " "
                    String result =  (String)(_addQueryParameterDlg.getCriteria());
        if (DEBUG) 
            System.out.println( "QBIT : actionPerformed called\nresult " + result + " length : " + (result.trim().length() ) + " inputTablePopupRow = " + _inputTablePopupRow  + " inputTablePopupColumn = " + _inputTablePopupColumn  );  // NOI18N);  // NOI18N

                    if ( result.trim().length() == 0 )
                        return;

                    this.getCellEditor ( _inputTablePopupRow, Criteria_COLUMN ).stopCellEditing();

                    // this.setValueAt ( result, _inputTablePopupRow, _inputTablePopupColumn );
                    this.setValueAt ( result, _inputTablePopupRow, Criteria_COLUMN );
                }
                finally {
                    _queryBuilder._updateText=true;
                }

                // Now regenerate the text query
                _queryBuilder.generateText();
            }
        }
        else if (source.getText().equals(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_AND_QUERY_CRITERIA")))       // NOI18N
        {    
            int row=getSelectedRow();
            String tableSpec=(String)this.getValueAt(row,Table_COLUMN);
            String columnName=(String)this.getValueAt(row,Column_COLUMN);
            String result0 = (String) this.getValueAt ( _inputTablePopupRow, _inputTablePopupColumn );
            if ( _queryBuilder._queryModel.getWhere() == null ) {
                String msg = NbBundle.getMessage(QueryBuilderInputTable.class, "EMPTY_QUERY_CRITERIA");
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            Column col = _queryBuilder._queryModel.findSelectColumn(tableSpec, columnName);
            String derColName = col.getDerivedColName();

            // add a row identical to the current row 
            Object[] rowData = { 
                (columnName.equals("* { All Columns }")) ? "*" : columnName, // * or column name        // NOI18N
                (derColName==null) ? "" : derColName,   // derived col name (not corrName)   // NOI18N
                tableSpec,                              // tableSpec
                Boolean.TRUE,                           // output: true = selected
                "",                                     // sort type        // NOI18N
                "",                                     // sort order       // NOI18N
                "",                                     // criteria     // NOI18N
                "",                                     // criteria order       // NOI18N
                /* "", "", "" */ };                      // or...        // NOI18N


            _addQueryParameterDlg =
                new AddQueryParameterDlg(true, (String)(this.getValueAt(getSelectedRow(), Column_COLUMN)));

            if ( _addQueryParameterDlg.getReturnStatus() == AddQueryParameterDlg.RET_OK )
            {
                ((DefaultTableModel)this.getModel()).insertRow ( row+1, rowData );
                // Bring up a dialog for adding a query parameter, and update the table with the result
                this.setRowSelectionInterval (row+1, row+1);
                // Split the string on " "
                String result =  (String)(_addQueryParameterDlg.getCriteria());

                if ( result.trim().length() == 0 ) return;

                if (DEBUG)
                System.out.println(
                        "    Table Row = " + row  +
                        "    Table = " + tableSpec +
                        "    Column = " + columnName +
                        "    Result = " + result 
                        + "\n" ); // NOI18N
                // Also need to update the Query Model with the new condition
                                       
                this.setValueAt ( result, getSelectedRow(), Criteria_COLUMN );

                int i = result.indexOf(" ");        // NOI18N
                String op = result.substring(0, i);
                String val = result.substring(i+1);
                
                // Also need to update the Query Model with the new condition
                                       
                Column col1 = SQLQueryFactory.createColumn(tableSpec, columnName);
                Literal lit2 = new Literal(val);
                Predicate pred = SQLQueryFactory.createPredicate(col1, lit2, op);

                _queryBuilder._queryModel.addOrCreateAndExpression(pred);

                // And regenerate the text query
                _queryBuilder.generateText();
            }
        }
        else if (source.getText().equals(NbBundle.getMessage(QueryBuilderInputTable.class, "ADD_OR_QUERY_CRITERIA")))       // NOI18N
        {    
            int row=getSelectedRow();
            String tableSpec=(String)this.getValueAt(row,Table_COLUMN);
            String columnName=(String)this.getValueAt(row,Column_COLUMN);
            String result0 = (String) this.getValueAt ( _inputTablePopupRow, _inputTablePopupColumn );
            if ( _queryBuilder._queryModel.getWhere() == null ) {
                String msg = NbBundle.getMessage(QueryBuilderInputTable.class, "EMPTY_QUERY_CRITERIA");
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            Column col = _queryBuilder._queryModel.findSelectColumn(tableSpec, columnName);
            String derColName = col.getDerivedColName();

            // add a row identical to the current row 
            Object[] rowData = { 
                (columnName.equals("* { All Columns }")) ? "*" : columnName, // * or column name  // NOI18N
                (derColName==null) ? "" : derColName,   // derived col name (not corrName)   // NOI18N
                tableSpec,                              // tableSpec
                Boolean.TRUE,                           // output: true = selected
                "",                                     // sort type        // NOI18N
                "",                                     // sort order       // NOI18N
                "",                                     // criteria     // NOI18N
                ""                                     // criteria order       // NOI18N
                /* "", "", "" */ };                           // or...        // NOI18N


            _addQueryParameterDlg =
                new AddQueryParameterDlg(true, (String)(this.getValueAt(getSelectedRow(), Column_COLUMN)));

            if ( _addQueryParameterDlg.getReturnStatus() == AddQueryParameterDlg.RET_OK )
            {
                ((DefaultTableModel)this.getModel()).addColumn ("Or..." );
                int orColumn = ((DefaultTableModel)this.getModel()).getColumnCount ();
                ((DefaultTableModel)this.getModel()).addColumn ("Order" );
                // Bring up a dialog for adding a query parameter, and update the table with the result
                // Split the string on " "
                String result =  (String)(_addQueryParameterDlg.getCriteria());

                if ( result.trim().length() == 0 ) return;

                if (DEBUG)
                System.out.println(
                        "    OR called \n " +
                        "    Table Row = " + row  +
                        "    Table = " + tableSpec +
                        "    Column = " + columnName +
                        "    orColumn = " + orColumn +
                        "    Result = " + result 
                        + "\n" ); // NOI18N
                // Also need to update the Query Model with the new condition
                                       
                ((DefaultTableModel)this.getModel()).setValueAt ( result, row, orColumn-1);

                int i = result.indexOf(" ");        // NOI18N
                String op = result.substring(0, i);
                String val = result.substring(i+1);

                // Also need to update the Query Model with the new condition
                                       
                Column col1 = SQLQueryFactory.createColumn(tableSpec, columnName);
                Literal lit2 = new Literal(val);
                Predicate pred = SQLQueryFactory.createPredicate(col1, lit2, op);

                _queryBuilder._queryModel.addOrCreateOrExpression(pred);

                // And regenerate the text query
                _queryBuilder.generateText();
            }
        }
    }


    // Specified by ItemListener
    // Detects menu selections for the sort menus
    // Also detects changes to checkbox for output status
    
    public void itemStateChanged (ItemEvent e) {

        int row=getSelectedRow();

        if (DEBUG) 
            System.out.println( "QBIT : itemStateChanged called\nTable Row: " + row  + // NOI18N
                        "  Item: " + e.getItem() + "  Item Selectable: " + e.getItemSelectable()); // NOI18N

        if  (_queryBuilder._updateModel &&
             (row!=-1)                  &&
             (e.getStateChange() == ItemEvent.SELECTED))
        {
            // Save the value of _updateText
            boolean updateText = _queryBuilder._updateText;
            _queryBuilder._updateText=false;
            try {
                String item=    (String)e.getItem();
                String tableSpec=    (String)(getValueAt(row, Table_COLUMN));
                String columnName=    (String)(getValueAt(row, Column_COLUMN));
                int sortCount=    _queryBuilder._queryModel.getSortCount();

                if (item.equals("")) {      // NOI18N
                    // This could come from either SortOrder or SortType
                    // Action is the same in either case -- remove sort spec
                    setValueAt("", row, SortType_COLUMN);       // NOI18N
                    setValueAt("", row, SortOrder_COLUMN);      // NOI18N
                    _queryBuilder._queryModel.
                        removeSortSpecification(tableSpec, columnName);
                }

                else if ( item.equals("ASC")  ||  // NOI18N
                          item.equals ( NbBundle.getMessage(QueryBuilderInputTable.class, "ASCENDING") ) )         // NOI18N
                {
                    // Sort Type specification
                    String order = (String)getValueAt(row, SortOrder_COLUMN);
                    int orderNum = order.equals("") ? sortCount+1 : Integer.parseInt(order);        // NOI18N
                    _queryBuilder._queryModel.
                        addSortSpecification(tableSpec, columnName, "ASC", orderNum);
                } 
                else if ( item.equals("DESC") ||  // NOI18N
                          item.equals ( NbBundle.getMessage(QueryBuilderInputTable.class, "DESCENDING") ) )      // NOI18N
                {
                    // Sort Type specification
                    String order = (String)getValueAt(row, SortOrder_COLUMN);
                    int orderNum = order.equals("") ? sortCount+1 : Integer.parseInt(order);        // NOI18N
                    _queryBuilder._queryModel.
                        addSortSpecification(tableSpec, columnName, "DESC", orderNum);
                } else {
                    // Must be a Sort Order specification
                    String type=(String)getValueAt(row, SortType_COLUMN);
                    int orderNum = Integer.parseInt(item);
                    if (!type.equals("")) {       // NOI18N
                        // this implies that the type is either Ascending or
                        // Descending ( I18N ) string.
                        String sortType = new String ("ASC");
                        if (type.equals ( NbBundle.getMessage(QueryBuilderInputTable.class, "DESCENDING") ) ) {         // NOI18N
                            sortType = new String ("DESC");
                        }
                        // When the user clicks in the Sort Order column for 
                        // a query column that's already sorted, he should 
                        // not be allowed to select the N+1 case.
                        // add sort specifcation only if the selected value is
                        // less than sort count.
                        if ( orderNum <= sortCount ) {
                            _queryBuilder._queryModel.
                                addSortSpecification(tableSpec, columnName, sortType, orderNum);
                        }
                    }
                    // 5064209 Order By is difficult to use - can't enter 
                    // "sort order" without "
                    else {
                        _queryBuilder._queryModel.
                            addSortSpecification(tableSpec, columnName, "ASC", orderNum);    // NOI18N
                    }
                }

                // Important. Without this, we generate bogus events on active row
                clearSelection();
                
                // Update the InputTable from scratch, in any case
                // This will also update the sortOrder dropdown
                generateTableOrderBy(_queryBuilder._queryModel);
            }
            finally {
                // Restore the value of updateText
                _queryBuilder._updateText=updateText;
            }
                    
            // And regenerate the text query if we're not driven by someone else
            if (_queryBuilder._updateText)
                _queryBuilder.generateText();
        }
    }


    void selectColumn(String tableSpec, String columnName, Boolean select) {
        int row = findRow(tableSpec, columnName);
        if (row == -1)
            return;
        if ((select==Boolean.TRUE) && 
                              (getValueAt(row,Output_COLUMN)!=Boolean.TRUE))
                setValueAt(Boolean.TRUE,row,Output_COLUMN);
        else if ((select==Boolean.FALSE) && 
                              (getValueAt(row,Output_COLUMN)!=Boolean.FALSE))
                setValueAt(Boolean.FALSE,row,Output_COLUMN);
    }

    // Add a row to the InputTable, or mark an existing row for output

    void addRow(String tableSpec, String columnName) {
        
        if (DEBUG) 
            System.out.println("QBIT.addRow, tableSpec: "+tableSpec + " columnName: "+columnName); //NOI18N
        
    // We used to have an existence check here, which may have been intended to break
        // event loops between the two tables; we may need to reinstate that.

        // Check whether this column is already represented by a row in the table
        int i = findRow(tableSpec, columnName);
        if (i!=-1) {

            // Found an existing row; mark for output
            if (this.getValueAt(i, Output_COLUMN)!=Boolean.TRUE)
                this.setValueAt(Boolean.TRUE, i, Output_COLUMN);

        } else {
        
            // Temporary hack -- find the select column in the model, and see if there's a derived name
            // This will go away once we start updating the table independent of the graph
            Column col = _queryBuilder._queryModel.findSelectColumn(tableSpec, columnName);
            String derColName = null;
            if ( col != null )
                derColName = col.getDerivedColName();

            // Note that all the operations below will cause new events to be fired
            // by the inputTableModel.  So, we get the rowData set up the way we want it,
            // and do the entire insertion as a single event
            Object[] rowData = { 
                (columnName.equals("* { All Columns }")) ? "*" : columnName, // * or column name  // NOI18N
                (derColName==null) ? "" : derColName,   // derived col name (not corrName)   // NOI18N
                tableSpec,                              // tableSpec
                Boolean.TRUE,                           // output: true = selected
                "",                                     // sort type        // NOI18N
                "",                                     // sort order       // NOI18N
                "",                                     // criteria     // NOI18N
                ""                                     // criteria order       // NOI18N
                /* "", "", "" */ };                           // or...        // NOI18N

            // Add the new row to the table
            ((DefaultTableModel)dataModel).addRow(rowData);

        }
    }


    // Remove the row that matches this tableSpec and columnName, if any
    // There should be only one
    // Only remove the row if none of the other columns are non-empty
    void removeRow(String tableSpec, String columnName) {

        // Iterate through the current columns
        // ToDo: see if we should iterate back to front for stability
        for (int i=0; i < this.getRowCount(); i++) {

            // First check for match on tablename
            if (this.getValueAt(i, Table_COLUMN).equals(tableSpec)) {

                // Now check for match on column
                if (this.getValueAt(i, Column_COLUMN).equals(columnName))
                {
                    // We have found the row to update.

                    // This handles the corner case where it appears in a sort specification
                    // (requires some cleanup afterwards)
                    boolean sortOrderRow = 
                        ! (((String)this.getValueAt(i,SortType_COLUMN)).trim().equals("") &&  // NOI18N
                           ((String)this.getValueAt(i,SortOrder_COLUMN)).trim().equals(""));   // NOI18N

                    // If this row also has a sort spec, clear the two cells; not a common corner
                    if (sortOrderRow) {
                        setValueAt("", i, SortType_COLUMN);       // NOI18N
                        setValueAt("", i, SortOrder_COLUMN);
                    }// NOI18N

                    // If all of the other important columns are empty, we can remove the row
                    if (((String)this.getValueAt(i,CriteriaOrder_COLUMN)).trim().equals("") &&      // NOI18N
                        ((String)this.getValueAt(i,Criteria_COLUMN)).trim().equals(""))     // NOI18N
                    {
                        ((DefaultTableModel)this.getModel()).removeRow(i);
                    }
                    else {
                        // If it's still in use, just mark it as non-selected
                        if (this.getValueAt(i, Output_COLUMN)!=Boolean.FALSE)
                            this.setValueAt(Boolean.FALSE, i, Output_COLUMN);
                    }
                    
                    // If we changed the overall sort ordering, clean up
                    if (sortOrderRow)
                        generateTableOrderBy(_queryBuilder._queryModel);
                }
                /*
                if ((this.getValueAt(i, Column_COLUMN).equals(columnName)) ||
                    ((columnName.equals("* { All Columns }")) &&
                     (this.getValueAt(i, Column_COLUMN).equals("*"))))
                {
                    // Check that none of the other columns are non-empty
                    if (this.getValueAt(i,SortType_COLUMN).equals("") &&
                        this.getValueAt(i,SortOrder_COLUMN).equals("") &&
                        this.getValueAt(i,Criteria_COLUMN).equals(""))
                    {
                        // Remove the row
                        ((DefaultTableModel)this.getModel()).removeRow(i);
                    }
                    else {
                        // Just mark it as non-selected
                        if (this.getValueAt(i, Output_COLUMN)!=Boolean.FALSE)
                            this.setValueAt(Boolean.FALSE, i, Output_COLUMN);
                    }
                }
                */
            }
        }
    }


    // Remove the rows that refer to this tableSpec

    void removeRows(String tableSpec) {

	Log.getLogger().entering("QueryBuilderInputTable", "removeRows", tableSpec);        // NOI18N

        for (int i=this.getRowCount()-1; i>=0; i--) {
            if (this.getValueAt(i, Table_COLUMN).equals(tableSpec)) {
                ((DefaultTableModel)this.getModel()).removeRow(i);
            }   
        }
    }


    // Add a criterion or parameter to the table. Does not update the Model.
    
    void addCriterion(String tableSpec, String columnName, String val, String order) {
        
	Log.getLogger().entering("QueryBuilderInputTable", "addCriterion", new Object[] { tableSpec, columnName, val }); // NOI18N

        // Get the Table object representing this table
//      ITable tbl = _queryBuilder._queryModel.findTable(tableSpec);
//      String tableName = tbl.getTableName();
//      String corrName = tbl.getCorrName();

        // search for the column (row) in the table
        boolean foundIt=false;
        int row = 0;
        for (int i=0; i<this.getRowCount(); i++) {
            if (((String)this.getValueAt(i, Table_COLUMN)).equals(tableSpec) &&
                ((String)this.getValueAt(i, Column_COLUMN)).equals(columnName))
//              && ((corrName==null) || ((String)this.getValueAt(i, Alias_COLUMN)).equals(corrName)))
            {
                // Found it -- modify the row
                foundIt=true;
                row = i;
                if ( ! this.getValueAt( i, Criteria_COLUMN ).equals ( 
                         Criteria_Uneditable_String ) ) {
                    this.setValueAt(val, i, Criteria_COLUMN );
                    this.setValueAt(order, i, CriteriaOrder_COLUMN );
                    break;
                }
            }
            /*
            else {
                this.setValueAt("", i, Criteria_COLUMN );       // NOI18N
                this.setValueAt("", i, CriteriaOrder_COLUMN );  // NOI18N
            }
            */
        }
                        
        Object[] rowData = { columnName,
                             "",                 // derived col name
                             tableSpec,
                             Boolean.FALSE, "", "", // NOI18N
                             val,                   // the new criterion/parameter
                             order
                             /* "", "", "" */ };      // NOI18N
        
        if (!foundIt) {
            // Not there -- add a new row
            ((DefaultTableModel)this.getModel()).addRow(rowData);
        }
        /*
        else {
            ((DefaultTableModel)this.getModel()).insertRow(row+1,rowData);

        }
         */
    }


    // Generate InputTable entries from criteria information in the WHERE clause
    // Does not update the model

    void generateTableWhere (QueryModel query) {

        if (DEBUG) 
            System.out.println(
                "Entering QueryBuilderInputTable.generateTableWhere"); // NOI18N

        boolean needsCriteriaOrder = false;
        int criteriaCount=0;
        String[] criteriaOrderItems = null;
        Where where=query.getWhere();
        if (where!=null) {

            // Iterate through the where condition list
            // For each one
            //     - get the tableSpec.columnName
            //     - find it in the list, and update the criteria column
            //     - if not found, insert a row  (shouldn't happen)
            Expression expr = where.getExpression();
            if (expr != null) {
                criteriaCount = 1; // there is something, if it's not an AND it's a 1 expression item in terms of criteria count
                if (expr instanceof Predicate) {
                    needsCriteriaOrder = true;
                    generatePredicateInTableWhere((Predicate)expr, 0, needsCriteriaOrder);                    
                }
                else if (expr instanceof And) {
                    needsCriteriaOrder = true;
                    ExpressionList andExpr = (ExpressionList)expr;
                    criteriaCount=andExpr.size();
                    for (int i=0; i<criteriaCount; i++) {
                        expr = andExpr.getExpression(i);
                        if (expr instanceof Predicate) {
                            Predicate pred = (Predicate)expr;
                            generatePredicateInTableWhere(pred, i, needsCriteriaOrder);
                        }
                    }
                }
            }
        }

        // Update the Criteria Order combobox if necessary
        //if (_criteriaOrderComboBox.getItemCount() != criteriaCount+1) {
            criteriaOrderItems = new String[criteriaCount+1];
            criteriaOrderItems[0]="";       // NOI18N
            TableColumn criteriaOrderColumn = this.getColumnModel().getColumn(CriteriaOrder_COLUMN);
            if (needsCriteriaOrder) {
                for (int i=0; i<criteriaCount; i++) 
                    criteriaOrderItems[i+1]=String.valueOf(i+1);
            }
            _criteriaOrderComboBox = new JComboBox(criteriaOrderItems);
            criteriaOrderColumn.setCellEditor(new DefaultCellEditor(_criteriaOrderComboBox));
            // _criteriaOrderComboBox.addItemListener(this);
        //}
    }

    private void generatePredicateInTableWhere (Predicate pred, int order, boolean needsCriteriaOrder) {
        Value val1 = pred.getVal1();
        Value val2 = pred.getVal2();

        // Assume that the right hand side is a literal value 
        // This will result in an entry into the InputTable
        String marker = pred.getVal2().toString();

        // Treat this like any other literal value now
        // if (marker.equals("?")) {}

        // We can only count on the tableSpec; tableName might 
        // contain corrName
        if ( (val1 instanceof Column) && !(val2 instanceof Column) ) {
            Column col = (Column)val1;
            String tableSpec=col.getTableSpec();
            String columnName=col.getColumnName();
            // Create the value that we're going to put into the table
            String val = pred.getOp() + " " + marker; // NOI18N

            int row = findRow(tableSpec, columnName);
            if (row!=-1) {
                setValueAt(val, row, Criteria_COLUMN);
                if (needsCriteriaOrder)
                    setValueAt(String.valueOf(order+1), row, CriteriaOrder_COLUMN);
            }
        }
    }

    // Generate any table entries that are specified in the Orderby clause -- sort specifications
    
    void generateTableOrderBy (QueryModel query) {

        OrderBy orderBy=query.getOrderBy();
        if (orderBy!=null) {

            // Iterate through the sortSpecificationList
            // For each one
            //     - get the tableSpec.columnName
            //     - find it in the list, and update the sort column
            //     - if not found, insert a row  (shouldn't happen)
            int size=orderBy.getSortSpecificationCount();
            for (int i=0; i<size; i++) {
                SortSpecification sortSpec = orderBy.getSortSpecification(i);
                int row = findRow(sortSpec.getColumn().getTableSpec(), sortSpec.getColumn().getColumnName());
                if (row!=-1) {
                    // Fix for 5081347 
                    // I18N - setting sort type in another row causes existing 
                    // sort type's change
                    if ( sortSpec.getDirection().equals("ASC") ) {
                        setValueAt (
                            ( NbBundle.getMessage(QueryBuilderInputTable.class, 
                                                "ASCENDING") ) , // NOI18N
                            row, SortType_COLUMN );
                    }
                    else if ( sortSpec.getDirection().equals("DESC") ) {
                        setValueAt (
                            ( NbBundle.getMessage(QueryBuilderInputTable.class, 
                                                "DESCENDING") ) , // NOI18N
                            row, SortType_COLUMN );

                    }
                    setValueAt(String.valueOf(i+1), row, SortOrder_COLUMN);
                }
            }
        }

        // Update the Sort Order combobox if necessary
        // Changed to fix 
        // 5064209 Order By is difficult to use - can't enter "sort order" 
        // without "
        int sortCount=_queryBuilder._queryModel.getSortCount();
        if ( sortCount < this.getRowCount() ) {
                sortCount += 1;
        }
        if (_sortOrderComboBox.getItemCount() != sortCount+1) {
            final String[] sortOrderItems = new String[sortCount+1];
            sortOrderItems[0]="";       // NOI18N
            for (int i=1; i<sortCount+1; i++)
                sortOrderItems[i]=String.valueOf(i);
            TableColumn sortOrderColumn = this.getColumnModel().getColumn(SortOrder_COLUMN);
            _sortOrderComboBox = new JComboBox(sortOrderItems);
            sortOrderColumn.setCellEditor(new DefaultCellEditor(_sortOrderComboBox));
            _sortOrderComboBox.addItemListener(this);
        }
    }


    // Find the row that describes this column
    private int findRow(String tableSpec, String columnName)
    {
        for (int i=0; i<this.getRowCount(); i++) {
            if ((this.getValueAt(i, Table_COLUMN).equals(tableSpec)) &&
                (this.getValueAt(i, Column_COLUMN).equals(columnName)))
                return i;
        }
        return -1;
    }
        
        
    // Returns just the class name -- no package info.

    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");        // NOI18N
        return classString.substring(dotIndex+1);
    }


    /**
     * Clears the model
     */
    void clearModel() {
        ((DefaultTableModel)this.getModel()).setRowCount(0);
    }

}

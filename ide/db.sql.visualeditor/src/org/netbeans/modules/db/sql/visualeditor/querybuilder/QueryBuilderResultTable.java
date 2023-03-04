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


import java.awt.Color;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.text.DateFormat ;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

import org.netbeans.modules.db.sql.visualeditor.Log;

/**
 * A table for displaying query results in the query editor
 * @author  Sanjay Dhamankar, Jim Davidson
 */
public class QueryBuilderResultTable extends JTable
                        implements ActionListener, KeyListener {

    private DefaultTableModel resultTableModel = null;
    private QueryBuilder                _queryBuilder;
    private JPopupMenu                  resultTablePopup;

    public QueryBuilderResultTable() {
        this(null);
    }

    /** Constructor which takes the parent as parameter */
    public QueryBuilderResultTable(QueryBuilder queryBuilder) {

        super();

        _queryBuilder = queryBuilder;
        resultTableModel = new DefaultTableModel() {
            public boolean isCellEditable ( int row, int column ) {
                return false;
            }
        };
        this.setModel(resultTableModel);
        
        resultTablePopup = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "LBL_CopyCellValue"));      // NOI18N
        menuItem.addActionListener(this);
        resultTablePopup.add(menuItem);
        menuItem = new JMenuItem(NbBundle.getMessage(QueryBuilderInputTable.class, "LBL_CopyRowValues"));      // NOI18N
        menuItem.addActionListener(this);
        resultTablePopup.add(menuItem);

        MouseListener resultTablePopupListener = new ResultTablePopupListener();
        super.addMouseListener(resultTablePopupListener);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        this.setMinimumSize(new Dimension(200, 30) );
//        this.setPreferredSize(new Dimension(200, 30) );

        // change foreground color to Dark Gray ...
        this.setForeground(Color.DARK_GRAY);
        addKeyListener(this);
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
        if ( _queryBuilder != null ) _queryBuilder.handleKeyPress(e);
    }

    /**
     * Clear the model
     */
    void clearModel() {
        ((DefaultTableModel)this.getModel()).setRowCount(0);
    }

    /**
     * Update the table model from the ResultSet
     * @param the result set associated with the table
     */
    public void displayResultSet(ResultSet rs) {
        displayResultSet( rs, 40, true ) ;
    }
    public boolean displayResultSet(ResultSet rs, int maxEntries, boolean infoMsgIfTruncated ) {
        // Check validity of ResultSet
        ResultSetMetaData rsmd;
        boolean resultsTruncated = false ;
        try {
            if ((rs == null) ||
            ((rsmd=rs.getMetaData())==null)) {
                //Thread.dumpStack();
		Log.getLogger().warning("Exception - unable to get query result ! "); // NOI18N
                return resultsTruncated;
            }

            // Get Column Names
            int numberOfColumns = rsmd.getColumnCount();
            Log.getLogger().finest(" rsmd.getColumnCount(): " + numberOfColumns); // NOI18N

            // Create a vector of column names, for headers
            String[] dbColumnNames = new String[numberOfColumns];
            for (int i = 1; i <= numberOfColumns; i++) {
                dbColumnNames[i - 1] = rsmd.getColumnLabel(i);
            }

            // Set Column Headers; this only works with DefaultTableModel
            resultTableModel.setColumnIdentifiers(dbColumnNames);

            // Discard all rows in the current model
            resultTableModel.setRowCount(0);
            
            // Process the result set, producing a vector
            // For each row, produce a 1-d array that gets added to the Model
            
            // Add a check to diaplay ONLY first 40 entries
            // Add buttons "PREVIOUS", "NEXT" under the result table and
            // show the proper entires. This is to avoid the
            // OutOfMemoryException
            
            // int maxEntries = 40;
            int count = 0;
            while (rs.next() && count < maxEntries + 1 ) {
                if ( count >= maxEntries ) {
                    resultsTruncated = true ;
                    break ;
                }
                Object[] row = new Object[numberOfColumns];
                for (int i = 1; i <= numberOfColumns; i++) {
                    // since this is an array, we start at 0
                    
                    // Do not show the values of Blob & Clob
                    if(rsmd.getColumnType(i) == java.sql.Types.BLOB){
                        Blob blobData = rs.getBlob(i);
                        // Added check to fix
                        // 5064319 : ServerNav> View Data on a Db2 Table with 
                        // BLOB column throws NPE
                        if ( blobData != null )
                            row[i - 1] = "[BLOB of size " + blobData.length() + "]"; //NOI18N
                    }else if(rsmd.getColumnType(i) == java.sql.Types.CLOB){
                        Clob clobData = rs.getClob(i);
                        // Added check to fix
                        // 5064319 : ServerNav> View Data on a Db2 Table with 
                        // BLOB column throws NPE
                        if ( clobData != null )
                            row[i - 1] = "[CLOB of size " + clobData.length() + "]"; //NOI18N
                    }
                    // convert timestamp to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP){
                        Timestamp timeStampData = rs.getTimestamp(i);

                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( timeStampData != null ) {
                            row[i - 1] = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format( timeStampData ) ;
                        }
                    }
                    // convert date to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.DATE){
                        Date dateData = rs.getDate(i);
                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( dateData != null ) {
                            row[i - 1] = DateFormat.getDateInstance(DateFormat.SHORT).format( dateData ) ;
                        }
                    }
                    // convert time to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.TIME){
                        Time timeData = rs.getTime(i);
                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( timeData != null ) {
                            row[i - 1] = java.text.DateFormat.getTimeInstance(DateFormat.MEDIUM).format(timeData );
                        }
                    }
                    else {
                          row[i - 1] = rs.getObject(i);
                    }
                }
                resultTableModel.addRow(row);
                count++;
            }
            if ( resultsTruncated && infoMsgIfTruncated ) {
                String msg = NbBundle.getMessage(QueryBuilderResultTable.class, "MAX_ENTRIES_DISPLAYED", Integer.toString(maxEntries));       // NOI18N
                NotifyDescriptor d =
                new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.INFORMATION_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
	    Log.getLogger().warning("Exception - unable to build table"); // NOI18N
        }finally{
            if (rs != null){
                try{
                    rs.close();
                }catch (Exception exc){
                    
                }
            }
        }
        return resultsTruncated ;
        
    }

    // Mouse listener -- bring up background menu
    class ResultTablePopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JTable source = (JTable)(e.getSource());
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());
                // Make sure the row where click occurred is selected.
                if (row != -1) {
                    source.setRowSelectionInterval (row, row);
                }
                resultTablePopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    // Respond to a selection from the popup menu
    public void actionPerformed(ActionEvent e) {
        
        JMenuItem source = (JMenuItem)(e.getSource());
        if (source.getText().equals(NbBundle.getMessage(QueryBuilderInputTable.class, "LBL_CopyCellValue")))       // NOI18N
        {
            Object o = getValueAt(getSelectedRow(), getSelectedColumn());
            setClipboard(o.toString());
        }
        else if (source.getText().equals(NbBundle.getMessage(QueryBuilderInputTable.class, "LBL_CopyRowValues")))       // NOI18N
        {
            int[] rows = getSelectedRows();
            StringBuffer output = new StringBuffer();
            for (int i = 0; i < rows.length; i++) {
                for (int col = 0; col < getColumnCount(); col++) {
                    if (col > 0) {
                        output.append('\t');
                    }
                    Object o = getValueAt(rows[i], col);
                    output.append(o.toString());
                }
                output.append('\n');
            }
            setClipboard(output.toString());
        }
    }
    
    private void setClipboard(String contents) {
        ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
        StringSelection strSel = new StringSelection(contents);
        clipboard.setContents(strSel, strSel);
    }
}

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

/**
 * A dialog that presents and asks user to input the parameter values
 * for parameterized SQL
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.table.*;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class ParameterizedQueryDialog extends javax.swing.JPanel {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RETURNED_CANCEL = 0;

    /** A return status code - returned if OK button has been pressed */
    public static final int RETURNED_OK = 1;

    public static final int PARAMETER_COLUMN = 0;

    public static final int VALUE_COLUMN = 1;

    private int returnStatus = RETURNED_CANCEL;

    private Dialog dialog;

    private DialogDescriptor dlg = null;

    ParameterizedTableModel _pTableModel;

    public ParameterizedQueryDialog() {
        this(null, true);
    }

    /** Creates new form ParameterizedQueryDialog */
    public ParameterizedQueryDialog(String[] parameters, boolean modal) {
        _pTableModel = new ParameterizedTableModel();
        initComponents();

        setParameters(parameters);

        TableColumn column = parameterValueTable.getColumnModel().getColumn(
            VALUE_COLUMN);
        column.setCellEditor(new FocusCellEditor(new JTextField()));
        parameterValueTable.setRowSelectionAllowed(true);
        parameterValueTable.setColumnSelectionAllowed(true);
        parameterValueTable.setColumnSelectionInterval(VALUE_COLUMN,VALUE_COLUMN);
        parameterValueTable.changeSelection(0, VALUE_COLUMN, false, false);
        parameterValueTable.setRowSelectionInterval(0,0);

        DefaultCellEditor dce =
            (DefaultCellEditor)parameterValueTable.getDefaultEditor(Object.class);
        dce.setClickCountToStart(1);

        final JTable fTable = parameterValueTable;
        Runnable r = new Runnable() {
                public void run() {
                    // Re-focus the table
                    fTable.requestFocusInWindow();
                }
            };

        // Put Focus-Runnable into the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(r);

        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Object o = evt.getSource();

                    Object[] option = dlg.getOptions();

                    if (o == option[1]) {
                        returnStatus = RETURNED_CANCEL;
                        dialog.dispose();
                    }
                    if (o == option[0]) {
                        returnStatus = RETURNED_OK;
                        dialog.dispose();
                    }
                }
            };

        dlg = new DialogDescriptor(this,
                                   // Dialog Title : "Specify Parameter Values",
                                   NbBundle.getMessage(ParameterizedQueryDialog.class,
                                                       "SPECIFY_PARAMETER_VALUES"),     // NOI18N
                                   modal, listener);

        String okString = NbBundle.getMessage(ParameterizedQueryDialog.class, "OK");
        String cancelString = NbBundle.getMessage(ParameterizedQueryDialog.class, "CANCEL");
        dlg.setOptions(new Object[] {okString, cancelString});
        dlg.setClosingOptions(new Object[] {okString, cancelString});

        dlg.setHelpCtx (
             new HelpCtx( "projrave_ui_elements_editors_about_query_editor" ) );        // NOI18N

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);

        dialog.setResizable(true);
        // dialog.setPreferredSize(new java.awt.Dimension (500,350));
        dialog.pack();
        dialog.show();
    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** @return the values input by the user
     * starting first row.
     **/
    public String[] getParameterValues() {
        int rows = _pTableModel.getRowCount();
        String[] values = new String[rows];
        for (int i = 0; i < rows; i++) {
            values[i] = (String) _pTableModel.getValueAt(i, VALUE_COLUMN);
        }
        return values;
    }

    /**
     * set the parameter values in the VALUE column 
     **/
    public void setParameterValues(String[] values) {
        for (int i = 0; i < values.length; i++) {
            _pTableModel.setValueAt(values[i], i, VALUE_COLUMN);
        }
    }

    /**
     * get the parameter values from the VALUE column 
     **/
    public String[] getParameters() {
        int rows = _pTableModel.getRowCount();
        String[] parameters = new String[rows];
        for (int i = 0; i < rows; i++) {
            parameters[i] = (String) _pTableModel.getValueAt(i,
                                                             PARAMETER_COLUMN);
        }
        return parameters;
    }

    public void setParameters(String[] parameters) {
        if (parameters == null || parameters.length == 0)
            return;
        _pTableModel.setRowCount(0);
        for (int i = 0; i < parameters.length; i++) {
            Object[] rowData = { parameters[i], "" // criteria order       // NOI18N
            }; // or...        // NOI18N
            _pTableModel.addRow(rowData);
        }
    }

    class ParameterizedTable extends JTable {
        // make sure to get the focus on keystroke. This will make sure
        // all the values entered by users are captured. Otherwise the
        // last cell value is not updated in the model.
        public boolean getSurrendersFocusOnKeystroke() {
            return true;
        }	

        // This achieves the focus/selection traversal using keyboard.
        // Here we need to traverse only through the second column
        // as the first column is not selectable/editable.
        // This is a general implementation and should work for
        // any JTable.
        // Case 1 : toggle: false, extend: false. 
        //      Clear previous selection and ensure the new cell is selected.
        // Case 2 : toggle: false, extend: true. 
        //      Extend the previous selection to include the specified cell.
        // Case 3 : toggle: true, extend: false. 
        //      If the specified cell is selected, deselect it. If it is not 
        //      selected, select it.
        // Case 4 : toggle: true, extend: true. 
        //      Leave the selection state as it is, but move the anchor 
        //      index to the specified location.
        //
        public void changeSelection(int row,int col,boolean toggle,
                                    boolean expand) {
            // This method is called when the user tries to move to a 
            // different cell.
            // If the cell they're trying to move to is not editable, 
            // we look for then next cell in the proper direction that 
            // is editable.
            if (!this.getModel().isCellEditable(row,col)) {
                // Find the current row and column 
                int currentRow = getEditingRow();
                int currentCol = getEditingColumn();
                if (currentRow == -1) {
                    currentRow = getSelectedRow();
                }
                if (currentCol == -1) {
                    currentCol = getSelectedColumn();
                }

                // need to wrap-around.
                int numberOfRows = getRowCount();
                int numberOfCols = getColumnCount();

                // If no cell is found to move to, stay here.
                int nextRow = row;
                int nextCol = col;

                if (col==currentCol) {
                    // Up or down motion - go only up or down.
                    int direction = row-currentRow;
                    if (direction>1) {
                        direction=1;
                    }
                    if (direction<-1) {
                        direction=-1;
                    }
                    nextRow = getNextEditableRow(row,col,direction,
                                                 numberOfRows,numberOfCols);
                } else if (row == currentRow) {
                    int direction = col-currentCol;
                    if (direction>1) {
                        direction=1;
                    }
                    if (direction<-1) {
                        direction=-1;
                    }
                    int[] nextCell = getNextEditableCell(row,col,direction,
                                                         numberOfRows,numberOfCols);
                    nextRow = nextCell[0];
                    nextCol = nextCell[1];
                } else {
                    int direction = row-currentRow;
                    if (direction>1) {
                        direction=1;
                    }
                    if (direction<-1) {
                        direction=-1;
                    }
                    if ((row==0) && (currentRow==numberOfRows-1)) {
                        direction=1;
                    }
                    int[] nextCell = getNextEditableCell(row,col,
                                                         direction,numberOfRows,numberOfCols);
                    nextRow = nextCell[0];
                    nextCol = nextCell[1];
                }
                // Go to the found cell.
                super.changeSelection(nextRow,nextCol,toggle,expand);
            } else {
                // This is an editable cell, so leave the selection here.
                super.changeSelection(row,col,toggle,expand);
            }
        }

        // Search for the next  editable cell starting at row,col 
        // Return array containing Row and Column
        int[] getNextEditableCell(int row,int col,
                                  int direction,int numberOfRows,int numberOfCols) {
            int originalRow=row;
            int originalCol=col;
            // traverse till row/col are not equal to the original row/col
            do {
                col = col+direction;
                if (col>=numberOfCols) {
                    col = 0;
                    row += direction;
                }
                if (col<0) {
                    col = numberOfCols-1;
                    row += direction;
                }
                if (row>=numberOfRows) {
                    row = 0;
                }
                if (row<0) {
                    row = numberOfRows-1;
                }
                if (isCellEditable(row,col)) {
                    return new int[]{row,col};
                }
            } while (!((row==originalRow)&&(col==originalCol)));

            // Nothing editable found, stay here.
            return new int[]{originalRow,originalCol};

        }

        // Search up/down for an editable cell.
        int getNextEditableRow(int row,int col,int direction,int numberOfRows,int numberOfCols) {
            int originalRow = row;
            // traverse till row is not equal to the original row
            do {
                row = row+direction;
                if (row<0) {
                    row = numberOfRows-1;
                }
                if (row>=numberOfRows) {
                    row=0;
                }
                if (isCellEditable(row,col)) {
                    return row;
                }
            } while (row != originalRow);
            // Nothing editable found, stay here.
            return originalRow;
        } 
    }

    class ParameterizedTableModel extends DefaultTableModel {

        final String[] columnNames = { 
            // "Parameter",
            NbBundle.getMessage(ParameterizedQueryDialog.class, 
                                "PARAMETER"),       // NOI18N
            // "Value"
            NbBundle.getMessage(ParameterizedQueryDialog.class, 
                                "VALUE") // NOI18N
        };

        Object[][] data = { { "", "" } };

        public ParameterizedTableModel() {
            super(0, 2);
            setColumnIdentifiers(columnNames);
        }

        public boolean isCellEditable(int row, int col) {
            if (col < 1)
                return false;
            else
                return true;
        }
    }

    // cell editor to handle focus lost events on particular
    // cells.
    private class FocusCellEditor extends DefaultCellEditor {
        Component c;

        public FocusCellEditor(JTextField jtf) {
            super(jtf);
            addFocusListener(jtf);
        }

        private void addFocusListener(Component C) {
            C.getClass();
            super.getComponent().addFocusListener(
                new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent fe) {
                        lostFocus();
                    }
                });
        }

        public void lostFocus() {
            stopCellEditing();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        messageAreaTablePanel = new javax.swing.JPanel();
        messageAreaPanel = new javax.swing.JPanel();
        messageArea = new javax.swing.JTextArea();
        parameterValueTablePanel = new javax.swing.JPanel();
        parameterValueTableScrollPane = new javax.swing.JScrollPane();
        parameterValueTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.GridLayout(1, 0));

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainPanel.setMinimumSize(new java.awt.Dimension(400, 300));
        mainPanel.setPreferredSize(new java.awt.Dimension(450, 350));
        mainPanel.setLayout(new java.awt.BorderLayout());

        messageAreaTablePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        messageAreaTablePanel.setPreferredSize(new java.awt.Dimension(450, 350));
        messageAreaTablePanel.setLayout(new java.awt.BorderLayout());

        messageAreaPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        messageAreaPanel.setMinimumSize(new java.awt.Dimension(300, 70));
        messageAreaPanel.setPreferredSize(new java.awt.Dimension(300, 70));
        messageAreaPanel.setLayout(new java.awt.BorderLayout());

        messageArea.setBackground(new java.awt.Color(212, 208, 200));
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Arial", 0, 12));
        messageArea.setLineWrap(true);
        messageArea.setText("You have chosen to run a parameterized query. It  requires parameter values to run. Please fill in the required values.");
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        messageAreaPanel.add(messageArea, java.awt.BorderLayout.CENTER);

        messageAreaTablePanel.add(messageAreaPanel, java.awt.BorderLayout.NORTH);

        parameterValueTablePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        parameterValueTablePanel.setMinimumSize(new java.awt.Dimension(300, 200));
        parameterValueTablePanel.setLayout(new java.awt.GridLayout(1, 0));

        parameterValueTableScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        parameterValueTableScrollPane.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        parameterValueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Parameter", "Value"
            }
        ));
        parameterValueTableScrollPane.setViewportView(parameterValueTable);
        parameterValueTable.setRowHeight(18);
        java.awt.Dimension dim = parameterValueTable.getTableHeader().getPreferredSize();
        java.awt.Dimension newDim = new java.awt.Dimension((int)dim.getWidth(),25);
        parameterValueTable.getTableHeader().setPreferredSize(newDim);
        parameterValueTableScrollPane.setViewportView(parameterValueTable);
        parameterValueTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParameterizedQueryDialog.class, "ACS_ParameterizedTableName")); // NOI18N
        parameterValueTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParameterizedQueryDialog.class, "ACS_ParameterizedTableDescription")); // NOI18N

        parameterValueTablePanel.add(parameterValueTableScrollPane);

        messageAreaTablePanel.add(parameterValueTablePanel, java.awt.BorderLayout.CENTER);

        mainPanel.add(messageAreaTablePanel, java.awt.BorderLayout.CENTER);

        add(mainPanel);
    }// </editor-fold>//GEN-END:initComponents

//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                    String[] parameters = new String[5];
//                    String[] values = new String[5];
//                    for (int i = 0; i < parameters.length; i++) {
//                        parameters[i] = ("Table.column" + i);
//                    }
//                    ParameterizedQueryDialog pqDlg = new ParameterizedQueryDialog(
//                        parameters, true);
//                    System.out.println(pqDlg.getReturnStatus());
//                    if (pqDlg.getReturnStatus() == ParameterizedQueryDialog.RETURNED_OK) {
//                        values = pqDlg.getParameterValues();
//
//                        for (int i = 0; i < values.length; i++) {
//                            System.out.println(values[i] + "\n");
//                        }
//                    }
//                }
//            });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JPanel messageAreaPanel;
    private javax.swing.JPanel messageAreaTablePanel;
    private javax.swing.JTable parameterValueTable;
    private javax.swing.JPanel parameterValueTablePanel;
    private javax.swing.JScrollPane parameterValueTableScrollPane;
    // End of variables declaration//GEN-END:variables

}

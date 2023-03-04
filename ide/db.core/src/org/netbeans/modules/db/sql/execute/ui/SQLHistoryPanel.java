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
package org.netbeans.modules.db.sql.execute.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.RowFilter.Entry;
import javax.swing.RowSorter.SortKey;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.db.sql.history.SQLHistory;
import org.netbeans.modules.db.sql.history.SQLHistoryEntry;
import org.netbeans.modules.db.sql.history.SQLHistoryManager;
import org.netbeans.modules.db.sql.loader.SQLDataLoader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author John Baker
 */
public class SQLHistoryPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP =
            new RequestProcessor(SQLHistoryPanel.class);
    private TableRowSorter<HistoryTableModel> rowSorter;
    private HistoryTableModel htm = new HistoryTableModel();
    public static final String SAVE_STATEMENTS_CLEARED = ""; // NOI18N  
    public static final Logger LOGGER = Logger.getLogger(SQLHistoryPanel.class.getName());
    private JEditorPane editorPane;
    private final ListSelectionModel sqlTableSelektion;
    private final DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /** Creates new form SQLHistoryPanel */
    public SQLHistoryPanel(final JEditorPane editorPane) {
        this.editorPane = editorPane;

        initComponents();

        rowSorter = new TableRowSorter<HistoryTableModel>(htm);
        sqlHistoryTable.setRowSorter(rowSorter);
        List<SortKey> sortKeys = new ArrayList<SortKey>();
        sortKeys.add(new SortKey(2, SortOrder.DESCENDING));
        rowSorter.setSortKeys(sortKeys);
        rowSorter.setSortsOnUpdates(true);
        rowSorter.sort();

        rowSorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
                updateRowCount();
            }
        });
        sqlTableSelektion = sqlHistoryTable.getSelectionModel();

        updateURLList();

        htm.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                updateURLList();
                updateRowCount();
            }
        });

        searchTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }
        });

        sqlHistoryTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseUtils.isDoubleClick(e)) {
                    insertSQL();
                    e.consume();
            }
    }
        });

        sqlTableSelektion.addListSelectionListener(
                new ListSelectionListener() {

            @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (sqlTableSelektion.isSelectionEmpty()) {
                            insertSQLButton.setEnabled(false);
                            deleteSQLButton.setEnabled(false);
                        } else {
                            insertSQLButton.setEnabled(true);
                            deleteSQLButton.setEnabled(true);
            }
    }
                });

        connectionUrlComboBox.addActionListener(new ActionListener() {

                @Override
            public void actionPerformed(ActionEvent e) {
                updateFilter();
                }
            });
        sqlHistoryTable.setTransferHandler(new TableTransferHandler());
    }

    private void updateRowCount() {
        matchingRowsLabel.setText(Integer.toString(sqlHistoryTable.getRowCount()));
    }

    private void updateFilter() {
        List<RowFilter<HistoryTableModel, Integer>> rowFilter = new ArrayList<RowFilter<HistoryTableModel, Integer>>();

        ConnectionHistoryItem connectionItem =
                (ConnectionHistoryItem) connectionUrlComboBox.getSelectedItem();
        String url = connectionItem.getUrl();
        if (url != null && !url.equals(ConnectionHistoryItem.ALL_URLS)) {
            rowFilter.add(new EqualsFilter(url, 0));
    }

        if (!searchTextField.getText().equals("")) {
            rowFilter.add(new ContainsInsensitiveFilter(searchTextField.getText(), 1));
        }

        if (rowFilter.size() > 0) {
            rowSorter.setRowFilter(RowFilter.andFilter(rowFilter));
        } else {
            rowSorter.setRowFilter(null);
                }
            }

    private void updateURLList() {
        ConnectionHistoryItem selectedItem =
                (ConnectionHistoryItem) connectionUrlComboBox.getSelectedItem();
        final String selectedUrl = selectedItem == null
                ? null : selectedItem.getUrl();
        final List<String> urls = new ArrayList<String>(htm.getJdbcURLs());
        urls.add(0, ConnectionHistoryItem.ALL_URLS);
        RP.post(new Runnable() {
            @Override
            public void run() {
                int selectedIndex = 0;
                int w = urls.size();
                final ConnectionHistoryItem[] comboItems =
                        new ConnectionHistoryItem[w];
                for (int i = 0; i < w; i++) {
                    comboItems[i] = new ConnectionHistoryItem(urls.get(i));
                    if (urls.get(i) != null && urls.get(i).equals(selectedUrl)) {
                        selectedIndex = i;
                    }
                }
                Arrays.sort(comboItems);
                final int selectedIndexFinal = selectedIndex;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        connectionUrlComboBox.setModel(new DefaultComboBoxModel(comboItems));
                        connectionUrlComboBox.setSelectedIndex(selectedIndexFinal);
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        connectionUrlComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        insertSQLButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sqlHistoryTable = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(getSQLHistoryTooltipValue(sqlHistoryTable, rowIndex, vColIndex));
                }
                return c;
            }
        };
        sqlLimitLabel = new javax.swing.JLabel();
        sqlLimitTextField = new javax.swing.JTextField();
        sqlLimitButton = new javax.swing.JButton();
        inputWarningLabel = new javax.swing.JLabel();
        deleteSQLButton = new javax.swing.JButton();
        deleteAllSQLButton = new javax.swing.JButton();
        matchingRowsLabel = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Connection")); // NOI18N

        jLabel2.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Match")); // NOI18N

        searchTextField.setMinimumSize(new java.awt.Dimension(20, 22));

        org.openide.awt.Mnemonics.setLocalizedText(insertSQLButton, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Insert")); // NOI18N
        insertSQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Insert")); // NOI18N
        insertSQLButton.setEnabled(false);
        insertSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSQLButtonActionPerformed(evt);
            }
        });

        sqlHistoryTable.setAutoCreateColumnsFromModel(false);
        sqlHistoryTable.setModel(htm);
        jScrollPane1.setViewportView(sqlHistoryTable);
        sqlHistoryTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_History")); // NOI18N
        sqlHistoryTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_History")); // NOI18N
        int dateColumnWidth1 = new JTextField(dateTimeFormat.format(new Date())).getPreferredSize().width;
        int dateColumnWidth2 = new JTextField(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DateTableTitle") + "XXXXX").getPreferredSize().width;
        int dateColumnWidth = Math.max(dateColumnWidth1, dateColumnWidth2);

        TableColumnModel sqlHistoryTableTCM = sqlHistoryTable.getColumnModel();

        TableColumn sqlHistoryTableColumn;

        sqlHistoryTableColumn = new TableColumn(1);
        sqlHistoryTableColumn.setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SQLTableTitle"));

        sqlHistoryTableTCM.addColumn(sqlHistoryTableColumn);

        sqlHistoryTableColumn = new TableColumn(2);
        sqlHistoryTableColumn.setMinWidth(dateColumnWidth);
        sqlHistoryTableColumn.setPreferredWidth(dateColumnWidth);
        sqlHistoryTableColumn.setMaxWidth(dateColumnWidth);
        sqlHistoryTableColumn.setHeaderValue(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DateTableTitle"));
        sqlHistoryTableColumn.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if(value instanceof Date) {
                    value = dateTimeFormat.format((Date) value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        sqlHistoryTableTCM.addColumn(sqlHistoryTableColumn);

        sqlLimitLabel.setLabelFor(sqlLimitTextField);
        org.openide.awt.Mnemonics.setLocalizedText(sqlLimitLabel, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_SqlLimit")); // NOI18N

        sqlLimitTextField.setText(Integer.toString(SQLHistoryManager.getInstance().getListSize()));
        sqlLimitTextField.setMinimumSize(new java.awt.Dimension(18, 22));

        org.openide.awt.Mnemonics.setLocalizedText(sqlLimitButton, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_ApplyButton")); // NOI18N
        sqlLimitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sqlLimitButtonActionPerformed(evt);
            }
        });

        inputWarningLabel.setForeground(java.awt.Color.red);
        inputWarningLabel.setFocusable(false);
        inputWarningLabel.setRequestFocusEnabled(false);
        inputWarningLabel.setVerifyInputWhenFocusTarget(false);

        org.openide.awt.Mnemonics.setLocalizedText(deleteSQLButton, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_Delete")); // NOI18N
        deleteSQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Delete")); // NOI18N
        deleteSQLButton.setEnabled(false);
        deleteSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSQLButtonActionPerformed(evt);
            }
        });

        deleteAllSQLButton.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DeleteAll")); // NOI18N
        deleteAllSQLButton.setToolTipText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_DeleteAll")); // NOI18N
        deleteAllSQLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllSQLButtonActionPerformed(evt);
            }
        });

        matchingRowsLabel.setText(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "SQLHistoryPanel.matchingRowsLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputWarningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                        .addGap(493, 493, 493))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sqlLimitLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sqlLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sqlLimitButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(matchingRowsLabel))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(connectionUrlComboBox, 0, 218, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(insertSQLButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteSQLButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteAllSQLButton, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(connectionUrlComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(insertSQLButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                        .addComponent(deleteAllSQLButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteSQLButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sqlLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sqlLimitButton)
                    .addComponent(sqlLimitLabel)
                    .addComponent(matchingRowsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        connectionUrlComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ASCN_ConnectionCombo")); // NOI18N
        connectionUrlComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_ConnectionCombo")); // NOI18N
        searchTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Match")); // NOI18N
        searchTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Match")); // NOI18N
        insertSQLButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Insert")); // NOI18N
        insertSQLButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Insert")); // NOI18N
        sqlLimitTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Save")); // NOI18N
        sqlLimitTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Save")); // NOI18N
        sqlLimitTextField.setInputVerifier(new InputVerifier() {

            public boolean verify(JComponent input) {
                JTextField tf = (JTextField) input;
                return tf.getText().matches("^\\d+$");
            }
        });
        sqlLimitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSN_Apply")); // NOI18N
        sqlLimitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "ACSD_Apply")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void insertSQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSQLButtonActionPerformed
    insertSQL();
}//GEN-LAST:event_insertSQLButtonActionPerformed

private void sqlLimitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sqlLimitButtonActionPerformed
    verifySQLLimit();
}//GEN-LAST:event_sqlLimitButtonActionPerformed

    private void deleteSQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSQLButtonActionPerformed
        deleteSQL();
    }//GEN-LAST:event_deleteSQLButtonActionPerformed

    private void deleteAllSQLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllSQLButtonActionPerformed
        deleteAllSQL();
    }//GEN-LAST:event_deleteAllSQLButtonActionPerformed

    private void insertSQL() {
        try {
            JEditorPane pane = (JEditorPane) EditorRegistry.lastFocusedComponent();
                    String mime = pane.getContentType();
                    if (mime.equals(SQLDataLoader.SQL_MIME_TYPE)) {
                        editorPane = pane;
                    }
            int min = sqlTableSelektion.getMinSelectionIndex();
            int max = sqlTableSelektion.getMaxSelectionIndex();
            for (int i = min; i <= max; i++) {
                if (sqlHistoryTable.isRowSelected(i)) {
                    int modelIndex = sqlHistoryTable.convertRowIndexToModel(i);
                    String sql = ((String) htm.getValueAt(modelIndex, 1)).trim();
                    insertIntoDocument(sql, editorPane);
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void deleteSQL() {
        SQLHistoryManager shm = SQLHistoryManager.getInstance();
        SQLHistory history = shm.getSQLHistory();
        int min = sqlTableSelektion.getMinSelectionIndex();
        int max = sqlTableSelektion.getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (sqlHistoryTable.isRowSelected(i)) {
                int modelIndex = sqlHistoryTable.convertRowIndexToModel(i);
                SQLHistoryEntry sql = (SQLHistoryEntry) htm.getValueAt(modelIndex, 3);
                history.remove(sql);
            }
        }
        shm.save();
        htm.refresh();
    }

    private void deleteAllSQL() {
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(SQLHistoryPanel.class, "DESC_DeleteAll"),
                NbBundle.getMessage(SQLHistoryPanel.class, "LBL_DeleteAll"),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            SQLHistoryManager shm = SQLHistoryManager.getInstance();
            SQLHistory history = shm.getSQLHistory();
            history.clear();
            shm.save();
            htm.refresh();
        }
    }

    private void verifySQLLimit() {
        String enteredLimitString = sqlLimitTextField.getText();
        String currentLimit = Integer.toString(SQLHistoryManager.getInstance().getListSize());
        String maxLimit = Integer.toString(SQLHistoryManager.MAX_SQL_STATEMENTS_SAVED_FOR_HISTORY);
        if (enteredLimitString.equals(SAVE_STATEMENTS_CLEARED)) {
            sqlLimitTextField.setText(currentLimit);
            return;
    }
        try {
            Integer enteredLimit = Integer.valueOf(enteredLimitString);
            if (enteredLimit > SQLHistoryManager.MAX_SQL_STATEMENTS_SAVED_FOR_HISTORY) {
                inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_NumberInputWarningLabel"));
                sqlLimitTextField.setText(maxLimit);
                SQLHistoryManager.getInstance().setListSize(SQLHistoryManager.MAX_SQL_STATEMENTS_SAVED_FOR_HISTORY);
                } else {
                inputWarningLabel.setText(""); // NOI18N
                SQLHistoryManager.getInstance().setListSize(enteredLimit);
                }
        } catch (NumberFormatException ex) {
                inputWarningLabel.setText(NbBundle.getMessage(SQLHistoryPanel.class, "LBL_NumberInputWarningLabel"));
            sqlLimitTextField.setText(currentLimit);
        }
        htm.refresh();
                }

    public String getSQLHistoryTooltipValue(JTable historyTable, int row, int col) {
        HistoryTableModel historyTableModel = (HistoryTableModel) historyTable.getModel();
        int modelColumn = historyTable.convertColumnIndexToModel(col);
        int modelRow = historyTable.convertRowIndexToModel(row);

        if (String.class.isAssignableFrom(historyTableModel.getColumnClass(modelColumn))) {
            String data = (String) historyTableModel.getValueAt(modelRow, modelColumn);
            return "<html>" + data.trim().replace("\n", "<br>") + "</html>";       // NOI18N
        } else if (Date.class.isAssignableFrom(historyTableModel.getColumnClass(modelColumn))) {
            Date data = (Date) historyTableModel.getValueAt(modelRow, modelColumn);
            return DateFormat.getInstance().format(data);
            } else {
            return null;
                }
            }

    private int insertIntoDocument(String s, JEditorPane target)
            throws BadLocationException {
        Document doc = target.getDocument();
        if (s == null) {
            s = "";
            }
        if (doc == null) {
            return -1;
        }        
        int start = -1;
        try {
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            start = caret.getDot();
            doc.insertString(start, s + ";\n", null); // NOI18N
        } catch (BadLocationException ble) {
            LOGGER.log(Level.WARNING, org.openide.util.NbBundle.getMessage(SQLHistoryPanel.class, "LBL_InsertAtLocationError") + ble);
    }
        return start;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionUrlComboBox;
    private javax.swing.JButton deleteAllSQLButton;
    private javax.swing.JButton deleteSQLButton;
    private javax.swing.JLabel inputWarningLabel;
    private javax.swing.JButton insertSQLButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel matchingRowsLabel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JTable sqlHistoryTable;
    private javax.swing.JButton sqlLimitButton;
    private javax.swing.JLabel sqlLimitLabel;
    private javax.swing.JTextField sqlLimitTextField;
    // End of variables declaration//GEN-END:variables

    private final class TableTransferHandler extends TransferHandler {

        /**
         * Map Transferable to createTransferableTSV from ResultSetJXTable
         *
         * This is needed so that CTRL-C Action of JTable gets the same
         * treatment as the transfer via the copy Methods of DataTableUI
         */
        @Override
        protected Transferable createTransferable(JComponent c) {
            StringBuilder sb = new StringBuilder();
            for (int id : sqlHistoryTable.getSelectedRows()) {
                int modelIndex = sqlHistoryTable.convertRowIndexToModel(id);
                if (sb.length() != 0) {
                    sb.append(System.lineSeparator());
                }
                // Column 1 => Column of SQL
                String sql = (String) htm.getValueAt(modelIndex, 1);
                sb.append(sql);
                sb.append(";");
            }
            return new StringSelection(sb.toString());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    private final class HistoryTableModel extends AbstractTableModel {
        
        private List<SQLHistoryEntry> sqlList;
        private List<String> jdbcURLs = new ArrayList<String>();
        private SQLHistoryManager shm = SQLHistoryManager.getInstance();
             
        public HistoryTableModel() {
            refresh();
            }

        public void refresh() {
            sqlList = new ArrayList<SQLHistoryEntry>(shm.getSQLHistory());
            for (SQLHistoryEntry sqe : sqlList) {
                String url = sqe.getUrl();
                if (!jdbcURLs.contains(url)) {
                    jdbcURLs.add(url);
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return sqlList.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case 0:
                return String.class;
                case 1:
                    return String.class;
                case 2:
                    return Date.class;
                case 3:
                    return SQLHistoryEntry.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return sqlList.get(row).getUrl();
                case 1:
                    return sqlList.get(row).getSql();
                case 2:
                    return sqlList.get(row).getDate();
                case 3:
                    return sqlList.get(row);
                default:
                    return null;
            } 
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            throw new NotImplementedException();
    }

        public List<String> getJdbcURLs() {
            return jdbcURLs;
        }
        }

    private class EqualsFilter extends RowFilter<HistoryTableModel, Integer> {

        private String referenz;
        private int referenzColumn;

        public EqualsFilter(String referenz, int referenzColumn) {
            this.referenz = referenz;
            this.referenzColumn = referenzColumn;
        }

            @Override
        public boolean include(Entry<? extends HistoryTableModel, ? extends Integer> entry) {
            return ((String) entry.getModel().getValueAt(entry.getIdentifier(), referenzColumn)).equals(referenz);
            }
        };

    private class ContainsInsensitiveFilter extends RowFilter<HistoryTableModel, Integer> {

        private String referenz;
        private int referenzColumn;

        public ContainsInsensitiveFilter(String referenz, int referenzColumn) {
            this.referenz = referenz.toLowerCase();
            this.referenzColumn = referenzColumn;
        }

        @Override
        public boolean include(Entry<? extends HistoryTableModel, ? extends Integer> entry) {
            return ((String) entry.getModel().getValueAt(entry.getIdentifier(), referenzColumn)).toLowerCase().contains(referenz);
        }
    };

    private static class ConnectionHistoryItem implements Comparable<ConnectionHistoryItem> {

        private static final String ALL_URLS = "*";                     //NOI18N
        private String url;
        private String name;

        public ConnectionHistoryItem(String url) {
            this.url = url == null ? ALL_URLS : url;
            if (ALL_URLS.equals(url)) {
                name = NbBundle.getMessage(SQLHistoryPanel.class,
                        "LBL_URLComboBoxAllConnectionsItem");           //NOI18N
            } else {
                for (DatabaseConnection dc
                        : ConnectionManager.getDefault().getConnections()) {
                    if (dc.getDatabaseURL().equals(url)) {
                        name = dc.getDisplayName();
                        break;
                    }
                }
                if (name == null) {
                    name = url;
                }
            }
            assert(this.name != null);
            assert(this.url != null);
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(ConnectionHistoryItem o) {
            boolean o1IsAll = this.url.equals(ALL_URLS);
            boolean o2IsAll = o.url.equals(ALL_URLS);
            if(Objects.equals(o1IsAll, o2IsAll)) {
                return name.compareToIgnoreCase(o.name);
            } else if (o1IsAll) {
                return -1;
            } else {
                return 1;
    }
}
    }
}

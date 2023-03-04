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
package org.netbeans.modules.db.dataview.output;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Rectangle;
import org.netbeans.modules.db.dataview.table.JXTableRowHeader;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.text.CloneableEditorSupport;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Nithya Radhakrishanan
 * @author Ahimanikya Satapathy
 *
 */
class InsertRecordDialog extends javax.swing.JDialog {
    private static final Logger LOG = Logger.getLogger(InsertRecordDialog.class.getName());

    private final ResultSetTableModel insertDataModel;
    private final DBTable insertTable;
    private final DataView dataView;
    private final DataViewPageContext pageContext;
    InsertRecordTableUI insertRecordTableUI;
    private JXTableRowHeader rowHeader;

    public InsertRecordDialog(DataView dataView, DataViewPageContext pageContext, DBTable insertTable) {
        super(WindowManager.getDefault().getMainWindow(), true);
        this.pageContext = pageContext;
        this.dataView = dataView;
        this.insertTable = insertTable;

        insertDataModel = new ResultSetTableModel(
                insertTable.getColumnList().toArray(new DBColumn[0]));
        insertDataModel.setEditable(true);

        insertRecordTableUI = new InsertRecordTableUI() {

            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (rowIndex != -1 && columnIndex != -1 && getModel().getRowCount() > 1) {
                    removeBtn.setEnabled(true);
                }
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        };
        insertRecordTableUI.setModel(insertDataModel);

        initComponents();
        addInputFields();
        insertRecordTableUI.addKeyListener(new TableKeyListener());

        insertRecordTableUI.getModel().addTableModelListener(new TableListener());

        jSplitPane1.setBottomComponent(null);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                executeBtnActionPerformed(null);
            }
        };

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); // NOI18N
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER"); // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction); // NOI18N
        getRootPane().getActionMap().put("ENTER", enterAction); // NOI18N

        Rectangle screenBounds = Utilities.getUsableScreenBounds();
        Dimension prefSize = getPreferredSize();

        if (prefSize.width > screenBounds.width - 100
                || prefSize.height > screenBounds.height - 100) {
            Dimension sz = new Dimension(prefSize);
            if (sz.width > screenBounds.width - 100) {
                sz.width = screenBounds.width * 3 / 4;
            }
            if (sz.height > screenBounds.height - 100) {
                sz.height = screenBounds.height * 3 / 4;
            }
            setPreferredSize(sz);
        }
        pack();
        setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextArea1 = new javax.swing.JTextArea();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        btnPanel = new javax.swing.JPanel();
        previewBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        executeBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.title")); // NOI18N
        setBackground(java.awt.Color.white);
        setFont(new java.awt.Font("Dialog", 0, 12));
        setForeground(java.awt.Color.black);
        setLocationByPlatform(true);
        setModal(true);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(jTextArea1.getFont());
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(jTextArea1, java.awt.BorderLayout.NORTH);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "insertRecodrDialog.jTextArea")); // NOI18N
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "insertRecord.textarea.desc")); // NOI18N

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setLastDividerLocation(250);
        jSplitPane1.setRequestFocusEnabled(false);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)));
        jScrollPane1.setFont(jScrollPane1.getFont());

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setForeground(new java.awt.Color(204, 204, 255));
        jPanel3.setFont(jPanel3.getFont().deriveFont(jPanel3.getFont().getSize()+1f));
        jPanel3.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanel3);

        jSplitPane1.setTopComponent(jScrollPane1);

        jScrollPane2.setFont(jScrollPane2.getFont());

        jEditorPane1.setEditable(false);
        jEditorPane1.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-sql"));
        jEditorPane1.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.jEditorPane1.toolTipText")); // NOI18N
        jEditorPane1.setOpaque(false);
        jScrollPane2.setViewportView(jEditorPane1);

        jSplitPane1.setBottomComponent(jScrollPane2);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        btnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 20, 10));
        btnPanel.setFont(btnPanel.getFont());
        btnPanel.setPreferredSize(new java.awt.Dimension(550, 50));
        btnPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        previewBtn.setFont(previewBtn.getFont());
        previewBtn.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(previewBtn, org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N
        previewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewBtnActionPerformed(evt);
            }
        });
        btnPanel.add(previewBtn);
        previewBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N
        previewBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.previewBtn.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addBtn, org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.addBtn.text_1")); // NOI18N
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.addBtn.toolTipText")); // NOI18N
        addBtn.setMaximumSize(previewBtn.getMaximumSize());
        addBtn.setMinimumSize(previewBtn.getMinimumSize());
        addBtn.setPreferredSize(previewBtn.getPreferredSize());
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });
        btnPanel.add(addBtn);

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.removeBtn.text_1")); // NOI18N
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.removeBtn.toolTipText")); // NOI18N
        removeBtn.setEnabled(false);
        removeBtn.setMaximumSize(previewBtn.getMaximumSize());
        removeBtn.setMinimumSize(previewBtn.getMinimumSize());
        removeBtn.setPreferredSize(previewBtn.getPreferredSize());
        removeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBtnActionPerformed(evt);
            }
        });
        btnPanel.add(removeBtn);

        executeBtn.setFont(executeBtn.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(executeBtn, org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N
        executeBtn.setMaximumSize(previewBtn.getMaximumSize());
        executeBtn.setMinimumSize(previewBtn.getMinimumSize());
        executeBtn.setPreferredSize(previewBtn.getPreferredSize());
        executeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeBtnActionPerformed(evt);
            }
        });
        btnPanel.add(executeBtn);
        executeBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N
        executeBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.executeBtn.text")); // NOI18N

        cancelBtn.setFont(cancelBtn.getFont());
        org.openide.awt.Mnemonics.setLocalizedText(cancelBtn, org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N
        cancelBtn.setMaximumSize(previewBtn.getMaximumSize());
        cancelBtn.setMinimumSize(previewBtn.getMinimumSize());
        cancelBtn.setPreferredSize(previewBtn.getPreferredSize());
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        btnPanel.add(cancelBtn);
        cancelBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N
        cancelBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.cancelBtn.text")); // NOI18N

        getContentPane().add(btnPanel, java.awt.BorderLayout.SOUTH);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InsertRecordDialog.class, "InsertRecordDialog.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleParent(null);
    }// </editor-fold>//GEN-END:initComponents

private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
    insertRecordTableUI.appendEmptyRow();
}//GEN-LAST:event_addBtnActionPerformed

private void removeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBtnActionPerformed
    insertRecordTableUI.removeRows();
    removeBtn.setEnabled(false);
}//GEN-LAST:event_removeBtnActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private class TableListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            if (SwingUtilities.isEventDispatchThread()) {
                refreshSQL();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshSQL();
                    }
                });
            }
        }
    }

    private void executeBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (insertRecordTableUI.isEditing()) {
            insertRecordTableUI.getCellEditor().stopCellEditing();
        }

        final int rows = insertRecordTableUI.getRowCount();
        final Object[][] insertedRows = new Object[rows][insertTable.getColumnList().size()];

        try {
            for (int i = 0; i < rows; i++) {
                insertedRows[i] = getInsertValues(i);
            }
        } catch (DBException ex) {
            LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(ex.getLocalizedMessage()));
            return;
        }

        // Get out of AWT thread
        new SwingWorker<Integer, Void>() {

            @Override
            protected Integer doInBackground() throws Exception {
                SQLStatementGenerator stmtBldr = dataView.getSQLStatementGenerator();
                SQLExecutionHelper execHelper = dataView.getSQLExecutionHelper();
                String inserts[] = new String[rows];

                for (int i = 0; i < rows; i++) {
                    inserts[i] = stmtBldr.generateInsertStatement(insertTable, insertedRows[i]);
                }

                return execHelper.executeInsertRow(pageContext, insertTable, inserts, insertedRows);
            }

            @Override
            protected void done() {
                Integer doneCount;
                try {
                    doneCount = get();

                    if (doneCount == rows) {
                        dispose();
                    } else {
                        // remove i already inserted
                        for (int j = 0; j < doneCount; j++) {
                            insertRecordTableUI.getModel().removeRow(0);
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    LOG.log(Level.INFO, ex.getCause().getLocalizedMessage(), ex);
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(
                                    ex.getCause().getLocalizedMessage()));
                }
            }
        }.execute();
    }

    private void previewBtnActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand().equalsIgnoreCase(NbBundle.getMessage(InsertRecordDialog.class, "LBL_show_sql"))) {
            jSplitPane1.setDividerLocation(jSplitPane1.getHeight() / 2);
            jSplitPane1.setBottomComponent(jScrollPane2);
            refreshSQL();
            previewBtn.setText(NbBundle.getMessage(InsertRecordDialog.class, "LBL_hide_sql"));
        } else {
            jSplitPane1.setBottomComponent(null);
            previewBtn.setText(NbBundle.getMessage(InsertRecordDialog.class, "LBL_show_sql"));
        }
    }

    public void refreshSQL() {
        try {
            String sqlText = "";
            if (jSplitPane1.getBottomComponent() != null) {
                SQLStatementGenerator stmtBldr = dataView.getSQLStatementGenerator();
                for (int i = 0; i < insertDataModel.getRowCount(); i++) {
                    String sql = stmtBldr.generateRawInsertStatement(insertTable, getInsertValues(i));
                    sqlText = sqlText + sql + "\n";
                }
                jEditorPane1.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-sql")); // NOI18N
                jEditorPane1.setText(sqlText);
                jScrollPane2.setViewportView(jEditorPane1);
            }
        } catch (DBException ex) {
            JLabel errorLabel = new JLabel(
                    "<html><body><font color=\"#FF0000\">" //NOI18N
                    + ex.getMessage().replaceAll("\\n", "<br>") //NOI18N
                    + "</font></body></html>"); //NOI18N
            errorLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            errorLabel.setVerticalAlignment(SwingConstants.TOP);
            jScrollPane2.setViewportView(errorLabel);
            jScrollPane2.revalidate();
            jScrollPane2.repaint();
        }
    }

    private void addInputFields() {
        insertRecordTableUI.appendEmptyRow();
        jScrollPane1.setViewportView(insertRecordTableUI);
        rowHeader = new JXTableRowHeader(insertRecordTableUI);
        final Component order[] = new Component[]{rowHeader, insertRecordTableUI};
        FocusTraversalPolicy policy = new FocusTraversalPolicy() {

            List<Component> componentList = Arrays.asList(order);

            @Override
            public Component getFirstComponent(Container focusCycleRoot) {
                return order[0];
            }

            @Override
            public Component getLastComponent(Container focusCycleRoot) {
                return order[order.length - 1];
            }

            @Override
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {              
                if (aComponent instanceof JXTableRowHeader) {
                    int rowIndex = insertRecordTableUI.getRowCount() - 1;
                    insertRecordTableUI.editCellAt(rowIndex, 0);
                    insertRecordTableUI.setRowSelectionInterval(rowIndex, 0);
                }
                return insertRecordTableUI;
            }

            @Override
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                int index = componentList.indexOf(aComponent);
                return order[(index - 1 + order.length) % order.length];
            }

            @Override
            public Component getDefaultComponent(Container focusCycleRoot) {
                return order[0];
            }
        };
        setFocusTraversalPolicy(policy);
        jScrollPane1.setRowHeaderView(rowHeader);
        jScrollPane1.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader());
    }

    private Object[] getInsertValues(int row) throws DBException {
        Object[] insertData = new Object[insertDataModel.getColumnCount()];
        if (insertDataModel.getRowCount() <= 0) {
            return insertData;
        }
        for (int i = 0; i < insertDataModel.getColumnCount(); i++) {
            DBColumn col = insertDataModel.getColumn(i);
            Object val = insertDataModel.getValueAt(row, i);

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (val instanceof SQLConstant) {
                insertData[i] = val;
            } else { // ELSE literals
                insertData[i] = DBReadWriteHelper.validate(val, col);
            }

        }
        return insertData;
    }

    private class TableKeyListener implements KeyListener {

        public TableKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
            processKeyEvents(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            processKeyEvents(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            processKeyEvents(e);
        }
    }

    private void processKeyEvents(KeyEvent e) {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

        if (KeyStroke.getKeyStrokeForEvent(e).equals(copy)) {
            copy();
        } else if (KeyStroke.getKeyStrokeForEvent(e).equals(paste)) {
            paste();
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE) {
            insertRecordTableUI.removeRows();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            setFocusable(false);
        } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_0) {
            control0Event();
            e.consume();
        } else if (e.isControlDown() && e.getKeyChar() == KeyEvent.VK_1) {
            control1Event();
            e.consume();
        } else if (KeyStroke.getKeyStrokeForEvent(e).equals(tab)) {
        }
    }
    private Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private void copy() {
        StringBuilder strBuffer = new StringBuilder();
        int numcols = insertRecordTableUI.getSelectedColumnCount();
        int numrows = insertRecordTableUI.getSelectedRowCount();
        int[] rowsselected = insertRecordTableUI.getSelectedRows();
        int[] colsselected = insertRecordTableUI.getSelectedColumns();
        if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length) &&
                (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0] && numcols == colsselected.length))) {
            JOptionPane.showMessageDialog(Utilities.findDialogParent(), "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                strBuffer.append(insertRecordTableUI.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1) {
                    strBuffer.append("\t");
                }
            }
            strBuffer.append("\n");
        }
        StringSelection stringSelection = new StringSelection(strBuffer.toString());
        clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(stringSelection, stringSelection);
    }

    private void paste() {
        int startRow = (insertRecordTableUI.getSelectedRows())[0];
        int startCol = (insertRecordTableUI.getSelectedColumns())[0];
        try {
            String trstring = (String) (clipBoard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            StringTokenizer st1 = new StringTokenizer(trstring, "\n");
            for (int i = 0; st1.hasMoreTokens(); i++) {
                int rowIdx = startRow + i;
                String rowstring = st1.nextToken();
                StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
                for (int j = 0; st2.hasMoreTokens(); j++) {
                    int colIdx = startCol + j;
                    String value = st2.nextToken();
                    if (colIdx < insertRecordTableUI.getColumnCount()) {
                        // If more data is pasted than currently rows exists
                        // empty rows are added to take the additional data
                        if (rowIdx >= insertRecordTableUI.getRowCount()) {
                            insertRecordTableUI.appendEmptyRow();
                        }
                        insertRecordTableUI.setValueAt(value, rowIdx, colIdx);
                    }
                }
            }
        } catch (UnsupportedFlavorException | IOException | RuntimeException ex) {
            LOG.log(Level.INFO, "Failed to paste the contents ", ex);
        }
    }

    private void control0Event() {
        int row = insertRecordTableUI.getSelectedRow();
        int col = insertRecordTableUI.getSelectedColumn();
        if (row == -1) {
            return;
        }
        int modelColumn = insertRecordTableUI.convertColumnIndexToModel(col);
        DBColumn dbcol = insertRecordTableUI.getModel().getColumn(modelColumn);
        if (dbcol.isGenerated() || !dbcol.isNullable()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            insertRecordTableUI.setValueAt(null, row, col);
        }
        insertRecordTableUI.setRowSelectionInterval(row, row);
    }

    private void control1Event() {
        int row = insertRecordTableUI.getSelectedRow();
        int col = insertRecordTableUI.getSelectedColumn();
        if (row == -1) {
            return;
        }
        int modelColumn = insertRecordTableUI.convertColumnIndexToModel(col);
        DBColumn dbcol = insertRecordTableUI.getModel().getColumn(modelColumn);
        if (dbcol.isGenerated() || !dbcol.isNullable()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            insertRecordTableUI.setValueAt(SQLConstant.DEFAULT, row, col);
        }
        insertRecordTableUI.setRowSelectionInterval(row, row);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton executeBtn;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton previewBtn;
    private javax.swing.JButton removeBtn;
    // End of variables declaration//GEN-END:variables
}

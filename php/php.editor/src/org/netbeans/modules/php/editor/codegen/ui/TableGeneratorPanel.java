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

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Dialog;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.editor.codegen.DatabaseURL;
import org.netbeans.modules.php.editor.codegen.DatabaseURL.Server;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Andrei Badea
 */
public final class TableGeneratorPanel extends javax.swing.JPanel {

    private DialogDescriptor descriptor;
    private DatabaseConnection databaseConnection;
    private Connection conn;
    private DatabaseMetaData dmd;
    private String newTable;
    private String lastErrorMessage;

    public static TableAndColumns selectTableAndColumns(String connVariable) {
        TableGeneratorPanel panel = new TableGeneratorPanel();
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(TableGeneratorPanel.class, "MSG_SelectTableAndColumns"));
        desc.createNotificationLineSupport();
        panel.initialize(desc, connVariable);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableGeneratorPanel.class, "ACSD_SelectColumns"));
        dialog.setVisible(true);
        dialog.dispose();
        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            Quoter quoter = SQLIdentifiers.createQuoter(panel.dmd);
            return new TableAndColumns(quoter, panel.newTable, panel.getAllColumns(), panel.getSelectedColumns(), panel.getConnVariable()); // NOI18N
        }
        return null;
    }

    private TableGeneratorPanel() {
        initComponents();
        columnList.setCellRenderer(new CheckRenderer());
        CheckListener checkListener = new CheckListener();
        columnList.addKeyListener(checkListener);
        columnList.addMouseListener(checkListener);
        connVariableTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateErrorState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateErrorState();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateErrorState();
            }
        });
    }

    private void initialize(DialogDescriptor descriptor, String connVariable) {
        this.descriptor = descriptor;
        DatabaseExplorerUIs.connect(dbconnComboBox, ConnectionManager.getDefault());
        connVariableTextField.setText(connVariable);
        updateErrorState();
    }

    private String changeDatabaseConnection(DatabaseConnection newDBConn) {
        databaseConnection = null;
        conn = null;
        dmd = null;
        tableComboBox.setModel(new DefaultComboBoxModel());
        columnList.setModel(new DefaultListModel());
        if (newDBConn == null) {
            return null;
        }
        DatabaseURL url = DatabaseURL.detect(newDBConn.getDatabaseURL());
        if (url == null || url.getServer() != Server.MYSQL) {
            return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_UnknownServer");
        }
        Connection newConn = newDBConn.getJDBCConnection();
        if (newConn == null) {
            ConnectionManager.getDefault().showConnectionDialog(newDBConn);
            newConn = newDBConn.getJDBCConnection();
        }
        String password = newDBConn.getPassword();
        if (password == null || newConn == null) {
            if (password == null) {
                return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_NoPassword");
            } else {
                return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_CouldNotConnect");
            }
        }
        String catalog;
        DatabaseMetaData newDmd;
        try {
            catalog = newConn.getCatalog();
            newDmd = newConn.getMetaData();
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
            return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
        }
        List<String> tables = new ArrayList<>();
        String errorMessage = extractTables(tables, newDmd, catalog, newDBConn.getSchema());
        if (errorMessage != null) {
            return errorMessage;
        }
        Collections.<String>sort(tables);
        databaseConnection = newDBConn;
        conn = newConn;
        dmd = newDmd;
        DefaultComboBoxModel tableModel = new DefaultComboBoxModel();
        for (String table : tables) {
            tableModel.addElement(table);
        }
        tableComboBox.setModel(tableModel);
        return null;
    }

    private String extractTables(final List<? super String> tables, final DatabaseMetaData dmd, final String catalog, final String schema) {
        return doWithProgress(NbBundle.getMessage(TableGeneratorPanel.class, "MSG_ExtractingTables"), new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    try (ResultSet rs = dmd.getTables(catalog, schema, "%", new String[] {"TABLE"})) {
                        while (rs.next()) {
                            tables.add(rs.getString("TABLE_NAME")); // NOI18N
                        }
                    }
                    return null;
                } catch (SQLException e) {
                    Exceptions.printStackTrace(e);
                    return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
                }
            }
        });
    }

    private String changeTable(final String newTable) {
        List<String> columns = new ArrayList<>();
        String errorMessage = extractColumns(newTable, columns);
        if (errorMessage != null) {
            return errorMessage;
        }
        this.newTable = newTable;
        ColumnModel model = new ColumnModel(columns);
        columnList.setModel(model);
        int selectedIndex = model.getSize() > 0 ? 0 : -1;
        columnList.setSelectedIndex(selectedIndex);
        return null;
    }

    private String extractColumns(final String table, final List<? super String> columns) {
        return doWithProgress(NbBundle.getMessage(TableGeneratorPanel.class, "MSG_ExtractingColumns"), new Callable<String>() {
            @Override
            public String call() {
                try {
                    try (ResultSet rs = dmd.getColumns(conn.getCatalog(), databaseConnection.getSchema(), table, "%")) {
                        while (rs.next()) {
                            columns.add(rs.getString("COLUMN_NAME")); // NOI18N
                        }
                    }
                    // Do not sort the columns, we need them in the order they
                    // are defined in the database.
                    return null;
                } catch (SQLException e) {
                    Exceptions.printStackTrace(e);
                    return NbBundle.getMessage(TableGeneratorPanel.class, "ERR_DatabaseMetadata");
                }
            }
        });
    }

    private void tableComboBoxSelectionChanged() {
        String table = (String) tableComboBox.getSelectedItem();
        lastErrorMessage = changeTable(table);
        updateErrorState();
    }

    private List<String> getSelectedColumns() {
        List<String> result = new ArrayList<>();
        ListModel model = columnList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object element = model.getElementAt(i);
            if (!(element instanceof Selectable)) {
                continue;
            }
            Selectable columnEl = (Selectable) element;
            if (!columnEl.isSelected()) {
                continue;
            }
            result.add(columnEl.getDisplayName());
        }
        return result;
    }

    private List<String> getAllColumns() {
        List<String> result = new ArrayList<>();
        ListModel model = columnList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object element = model.getElementAt(i);
            if (!(element instanceof Selectable)) {
                continue;
            }
            Selectable columnEl = (Selectable) element;
            result.add(columnEl.getDisplayName());
        }
        return result;
    }

    private String getConnVariable() {
        return connVariableTextField.getText().trim();
    }

    private void updateErrorState() {
        tableComboBox.setEnabled(databaseConnection != null);
        columnList.setEnabled(tableComboBox.getSelectedItem() != null);
        if (lastErrorMessage != null) {
            setErrorMessage(lastErrorMessage);
            return;
        }
        if (databaseConnection == null) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_SelectConnection"));
            return;
        }
        if (tableComboBox.getSelectedItem() == null) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_SelectTable"));
            return;
        }
        if (getConnVariable().trim().length() == 0) {
            setErrorMessage(NbBundle.getMessage(TableGeneratorPanel.class, "ERR_EnterConnVariable"));
            return;
        }
        setErrorMessage(null);
    }

    private void setErrorMessage(String message) {
        descriptor.getNotificationLineSupport().setErrorMessage(message);
        descriptor.setValid(message == null);
    }

    private static <T> T doWithProgress(String message, final Callable<? extends T> run) {
        final ProgressPanel panel = new ProgressPanel();
        panel.setCancelVisible(false);
        panel.setText(message);
        ProgressHandle handle = ProgressHandle.createHandle(null);
        JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
        handle.start();
        final List<T> result = new ArrayList<>(1);
        try {
            Task task = RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        try {
                            result.add(run.call());
                        } catch (Exception e) {
                            result.add(null);
                            Exceptions.printStackTrace(e);
                        } finally {
                            SwingUtilities.invokeLater(this);
                        }
                    } else {
                        panel.close();
                    }
                }
            });
            panel.open(progress);
            task.waitFinished();
        } finally {
            handle.finish();
        }
        return result.get(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dbconnLabel = new javax.swing.JLabel();
        dbconnComboBox = new javax.swing.JComboBox();
        tableLabel = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox();
        columnLabel = new javax.swing.JLabel();
        columnScrollPane = new javax.swing.JScrollPane();
        columnList = new javax.swing.JList();
        connVariableLabel = new javax.swing.JLabel();
        connVariableTextField = new javax.swing.JTextField();

        setFocusTraversalPolicy(null);

        dbconnLabel.setLabelFor(dbconnComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(dbconnLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "ConnectionGeneratorPanel.dbconnLabel.text")); // NOI18N

        dbconnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbconnComboBoxActionPerformed(evt);
            }
        });

        tableLabel.setLabelFor(tableComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableLabel.text")); // NOI18N

        tableComboBox.setEnabled(false);
        tableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableComboBoxActionPerformed(evt);
            }
        });

        columnLabel.setLabelFor(columnList);
        org.openide.awt.Mnemonics.setLocalizedText(columnLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnLabel.text")); // NOI18N

        columnList.setEnabled(false);
        columnScrollPane.setViewportView(columnList);
        columnList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnList.AccessibleContext.accessibleName")); // NOI18N
        columnList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnList.AccessibleContext.accessibleDescription")); // NOI18N

        connVariableLabel.setLabelFor(connVariableLabel);
        org.openide.awt.Mnemonics.setLocalizedText(connVariableLabel, org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableLabel.text")); // NOI18N

        connVariableTextField.setColumns(16);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(columnScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                    .addComponent(dbconnLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbconnComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 437, Short.MAX_VALUE)
                    .addComponent(tableLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 437, Short.MAX_VALUE)
                    .addComponent(columnLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connVariableTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connVariableLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbconnLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbconnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connVariableLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connVariableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        dbconnLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnLabel.AccessibleContext.accessibleName")); // NOI18N
        dbconnLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnLabel.AccessibleContext.accessibleDescription")); // NOI18N
        dbconnComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnComboBox.AccessibleContext.accessibleName")); // NOI18N
        dbconnComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.dbconnComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        tableLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableLabel.AccessibleContext.accessibleName")); // NOI18N
        tableLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableLabel.AccessibleContext.accessibleDescription")); // NOI18N
        tableComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableComboBox.AccessibleContext.accessibleName")); // NOI18N
        tableComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.tableComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        columnLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnLabel.AccessibleContext.accessibleName")); // NOI18N
        columnLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnLabel.AccessibleContext.accessibleDescription_1")); // NOI18N
        columnScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnScrollPane.AccessibleContext.accessibleName")); // NOI18N
        columnScrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.columnScrollPane.AccessibleContext.accessibleDescription_1")); // NOI18N
        connVariableLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableLabel.AccessibleContext.accessibleName")); // NOI18N
        connVariableTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableTextField.AccessibleContext.accessibleName")); // NOI18N
        connVariableTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.connVariableTextField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableGeneratorPanel.class, "TableGeneratorPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void dbconnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbconnComboBoxActionPerformed
        DatabaseConnection dbconn = null;
        Object selected = dbconnComboBox.getSelectedItem();
        if (selected instanceof DatabaseConnection) {
            dbconn = (DatabaseConnection) selected;
        }
        lastErrorMessage = changeDatabaseConnection(dbconn);
        if (lastErrorMessage == null && this.databaseConnection != null) {
            tableComboBoxSelectionChanged();
        }
        updateErrorState();
}//GEN-LAST:event_dbconnComboBoxActionPerformed

private void tableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboBoxActionPerformed
        tableComboBoxSelectionChanged();
}//GEN-LAST:event_tableComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnLabel;
    private javax.swing.JList columnList;
    private javax.swing.JScrollPane columnScrollPane;
    private javax.swing.JLabel connVariableLabel;
    private javax.swing.JTextField connVariableTextField;
    private javax.swing.JComboBox dbconnComboBox;
    private javax.swing.JLabel dbconnLabel;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables

    private static final class ColumnModel extends AbstractListModel implements ChangeListener {

        private final List<Selectable> elements;

        public ColumnModel(List<String> columns) {
            elements = new ArrayList<>(columns.size());
            for (String table : columns) {
                Selectable element = new Selectable(table);
                element.addChangeListener(this);
                elements.add(element);
            }
        }

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public Selectable getElementAt(int index) {
            return elements.get(index);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i) == e.getSource()) {
                    fireContentsChanged(this, i, i);
                    break;
                }
            }
        }
    }

    public static final class TableAndColumns {

        private final Quoter identifierQuoter;
        private final String table;
        private final List<String> allColumns;
        private final List<String> selectedColumns;
        private final String connVariable;

        private TableAndColumns(Quoter identifierQuoter, String table, List<String> allColumns, List<String> selectedColumns, String connVariable) {
            this.identifierQuoter = identifierQuoter;
            this.table = table;
            this.allColumns = allColumns;
            this.selectedColumns = selectedColumns;
            this.connVariable = connVariable;
        }

        public Quoter getIdentifierQuoter() {
            return identifierQuoter;
        }

        public String getTable() {
            return table;
        }

        public List<String> getAllColumns() {
            return allColumns;
        }

        public List<String> getSelectedColumns() {
            return selectedColumns;
        }

        public String getConnVariable() {
            return connVariable;
        }
    }
}

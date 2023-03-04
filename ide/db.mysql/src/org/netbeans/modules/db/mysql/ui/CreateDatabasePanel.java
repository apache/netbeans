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

package org.netbeans.modules.db.mysql.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseUser;
import org.netbeans.modules.db.mysql.impl.SampleManager;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author  rob
 */
public class CreateDatabasePanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(
            CreateDatabasePanel.class.getName());

    private DialogDescriptor descriptor;
    private Dialog dialog;
    private final DatabaseServer server;
    private final DatabaseComboModel databaseComboModel;
    private DatabaseConnection dbconn;
    private JButton okButton;
    private JButton cancelButton;
    
    private boolean validatePanel(String databaseName) {
        if (descriptor == null) {
            return false;
        }

        String error = null;

        comboUsers.setEnabled(this.isGrantAccess());
        
        if ( Utils.isEmpty(databaseName) ) {
            error = NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateNewDatabasePanel.MSG_SpecifyDatabase");
        }

        if (this.isGrantAccess() && 
                (comboUsers.getSelectedItem() == null ||
                Utils.isEmpty(((DatabaseUser)comboUsers.getSelectedItem()).getUser ()))) {
            error = NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatbasePanel.MSG_NoGrantUserSelected");
        }
        try {
            if (server.databaseExists(databaseName)) {
                error = NbBundle.getMessage(CreateDatabasePanel.class,
                                            "CreateNewDatabasePanel.MSG_DatabaseAlreadyExists",
                                            databaseName);
            }
        } catch (DatabaseException ex) {
            // probably not connected server, ignore here
        }

        if (error != null) {
            messageLabel.setText(error);
            okButton.setEnabled(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            okButton.setEnabled(true);
        }

        return error == null;
    }

    private void startProgress() {
        setProgress(true);
    }

    private void stopProgress() {
        setProgress(false);
    }

    private void setProgress(final boolean start) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                comboDatabaseName.setEnabled(!start);
                okButton.setEnabled(okButton.isEnabled() && !start);
                if (! start) {
                    cancelButton.setEnabled(true);
                }
                progressBar.setVisible(start);
                progressLabel.setVisible(start);
                progressBar.setIndeterminate(start);
                resize();
            }
        });

    }

    public DatabaseConnection showCreateDatabaseDialog() throws DatabaseException {
        assert SwingUtilities.isEventDispatchThread();

        okButton = new JButton(NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.CTL_OKButton")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(okButton.getAccessibleContext().getAccessibleName());
        okButton.setEnabled(false);
        cancelButton = new JButton(NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.CTL_CancelButton")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(cancelButton.getAccessibleContext().getAccessibleName());

        ActionListener listener = new ActionListener() {
            Task task;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(okButton)) {
                    if (task != null && !task.isFinished()) {
                        // Don't start a new task if we're still running the old one
                        return;
                    }
                    
                    startProgress();

                    task = RequestProcessor.getDefault().create(new Runnable() {
                        @Override
                        public void run() {
                            createDatabase();
                            if (isGrantAccess()) {
                                grantAccess();
                            }
                        }
                    });

                    task.addTaskListener(new TaskListener() {
                        @Override
                        public void taskFinished(org.openide.util.Task task) {
                            stopProgress();
                            dialog.dispose();
                        }
                    });
                    
                    task.schedule(0);

                } else if (e.getSource().equals(cancelButton)) {
                    dialog.dispose();
                }
            }
        };

        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);

        String title = NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.LBL_CreateDatabaseTitle");

        descriptor = new DialogDescriptor(this, title, true, new JButton[] {okButton, cancelButton},
                okButton, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.findHelp(CreateDatabasePanel.class), null);
        descriptor.setClosingOptions(new Object[0]);

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        String acsd = NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.ACSD_CreateNewDatabasePanel");
        dialog.getAccessibleContext().setAccessibleDescription(acsd);

        // This needs to be done after the dialog is created because stopProgress()
        // resizes the window to hide the progress bar, and the window
        // isn't available until the dialog is created.
        stopProgress();

        descriptor.setValid(validatePanel(getDatabaseName()));
        dialog.setVisible(true);

        return dbconn;
    }

    private void grantAccess() {
        String dbname = getDatabaseName();
        DatabaseUser user = (DatabaseUser)comboUsers.getSelectedItem();
        assert(user != null);
        server.grantFullDatabaseRights(dbname, user);
    }

    /**
     * Create a database based on settings of the dialog.  Set the member
     * variable dbconn to the resulting Database Connection.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private void createDatabase() {

        dbconn = null;
        String dbname = getDatabaseName();
        boolean dbCreated = false;

        try {
            if (! ensureConnected()) {
                return;
            }

            if ( ! checkExistingDatabase(server, dbname) ) {
                return;
            }

            server.createDatabase(dbname);
            
            int timeout = 30;
            
            while (timeout-- > 0 && ! server.databaseExists(dbname)) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINEST, ex.getMessage(), ex);
                }
            }
            
            dbCreated = server.databaseExists(dbname);
                           
            if (dbCreated) {
                dbconn = createConnection(server, dbname);
            }

            if (dbconn == null) {
                return;
            }

            boolean create = false;

            if (SampleManager.isSample(dbname)) {
                if (! databaseComboModel.isSelectedSample()) {
                    // This is a sample name the user typed in - make sure they want to
                    // actually create the sample tables, objects, etc.
                    create = Utils.displayYesNoDialog(NbBundle.getMessage(CreateDatabasePanel.class,
                            "CreateDatabasePanel.MSG_ConfirmCreateSample", dbname));
                } else {
                    create = true;
                }
            }

            if (create) {
                // Disable cancel
                // TODO - make it possible to cancel a long-running sample creation task
                cancelButton.setEnabled(false);
                SampleManager.createSample(dbname, dbconn);
            }
        } catch ( DatabaseException ex ) {
            displayCreateFailure(server, ex, dbname, dbCreated);
            dbconn = null;
        }
    }

    private boolean ensureConnected() throws DatabaseException {
        try {
            server.validateConnection();
        } catch (DatabaseException dbe) {
            LOGGER.log(Level.INFO, null, dbe);
            Utils.displayError(dbe.getMessage(), dbe);
        }
        
        if (server.isConnected()) {
            return true;
        }

        try {
            server.reconnect();
        } catch (TimeoutException te) {
            Utils.displayError(te.getMessage(), te);
            LOGGER.log(Level.INFO, null, te);
        }

        return server.isConnected();
    }
        
    private static void displayCreateFailure(DatabaseServer server,
            DatabaseException ex, String dbname, boolean dbCreated) {
        LOGGER.log(Level.INFO, null, ex);
        Utils.displayError(NbBundle.getMessage(CreateDatabasePanel.class,
                "CreateNewDatabasePanel.MSG_CreateFailed"), ex);

        if ( dbCreated ) {
            NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateNewDatabasePanel.MSG_DeleteCreatedDatabase",
                        dbname),
                    NbBundle.getMessage(CreateDatabasePanel.class,
                        "CreateNewDatabasePanel.STR_DeleteCreatedDatabaseTitle"),
                    NotifyDescriptor.YES_NO_OPTION);
            
            Object response = DialogDisplayer.getDefault().notify(ndesc);
            
            if ( response == NotifyDescriptor.YES_OPTION ) {
                server.dropDatabase(dbname);
            }
        }
    }

    
    /**
     * Check to see if a database already exists, and raise a message to
     * the user if it does.
     * 
     * @return true if it's OK to continue or false to cancel
     */
    private static boolean checkExistingDatabase(
            DatabaseServer server, String dbname) throws DatabaseException {
        if ( ! server.databaseExists(dbname)) {
            return true;
        }
             
       NotifyDescriptor ndesc = new NotifyDescriptor.Message(
                NbBundle.getMessage(CreateDatabasePanel.class, 
                        "CreateNewDatabasePanel.MSG_DatabaseAlreadyExists",
                    dbname));
       ndesc.setTitle(NbBundle.getMessage(CreateDatabasePanel.class,
                    "CreateNewDatabasePanel.STR_DatabaseExistsTitle"));

       DialogDisplayer.getDefault().notify(ndesc);

       return false;
    }
    
    private static DatabaseConnection createConnection(final DatabaseServer server, final String dbname) throws DatabaseException {
        
        List<DatabaseConnection> conns = DatabaseUtils.findDatabaseConnections(server.getURL(dbname));
        if ( ! conns.isEmpty() ) {
            // We already have a connection, no need to create one
            return conns.get(0);
        }
        
        DatabaseConnection dbconn = DatabaseConnection.create(
            DatabaseUtils.getJDBCDriver(), server.getURL(dbname), server.getUser(), null, server.getPassword(), false);
        
        ConnectionManager.getDefault().addConnection(dbconn);
        ConnectionManager.getDefault().connect(dbconn);

        return dbconn;
    }

    
    /** Creates new form CreateDatabasePanel */
    public CreateDatabasePanel(DatabaseServer server) throws DatabaseException {
        this.server = server;
        initComponents();

        databaseComboModel = new DatabaseComboModel();
        comboDatabaseName.setModel(databaseComboModel);
        
        comboDatabaseName.getEditor().getEditorComponent().addKeyListener(
            new KeyListener() {

            @Override
            public void keyTyped(KeyEvent event) {
                // Get the actual key.  apparently getItem() doesn't return the current
                // string typed until *after* this event finishes :(
                String keyStr = Character.toString(event.getKeyChar()).trim();
                String dbname;
                if ( Utils.isEmpty(keyStr)) {
                    dbname = comboDatabaseName.getEditor().getItem().toString().trim();
                } else {
                    // We know the database name has at least this character in it
                    dbname = keyStr;
                }
                
                validatePanel(dbname);
            }

            @Override
            public void keyPressed(KeyEvent event) {
            }

            @Override
            public void keyReleased(KeyEvent event) {
                // Get the actual key.  apparently getItem() doesn't return the current
                // string typed until *after* this event finishes :(
                String keyStr = Character.toString(event.getKeyChar()).trim();
                if ( ! Utils.isEmpty(keyStr)) {
                    String dbname = comboDatabaseName.getEditor().getItem().toString().trim();
                    validatePanel(dbname);
                }

            }
        });
                        
        comboUsers.setModel(new UsersComboModel(server));
        
        if ( comboUsers.getItemCount() == 0 ) {
            comboUsers.setVisible(false);
            chkGrantAccess.setVisible(false);
        } else {
            comboUsers.setSelectedIndex(0);
            setGrantAccess(false);
        }
                
        setBackground(getBackground());
        messageLabel.setBackground(getBackground());
        messageLabel.setText(" ");
    }

    private void resize() {
        revalidate();
        if (dialog != null) {
            dialog.pack();
        }
    }


    private String getDatabaseName() {
        String dbname = (String)comboDatabaseName.getSelectedItem();
        if ( dbname != null ) {
            dbname = dbname.trim();
        }
        return dbname;
    }
        
    private void setGrantAccess(boolean grant) {
        this.chkGrantAccess.setSelected(grant);
        comboUsers.setEnabled(grant);
    }
    
    private boolean isGrantAccess() {
        return chkGrantAccess.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        chkGrantAccess = new javax.swing.JCheckBox();
        comboDatabaseName = new javax.swing.JComboBox();
        comboUsers = new javax.swing.JComboBox();
        messageLabel = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();

        nameLabel.setLabelFor(comboDatabaseName);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.nameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkGrantAccess, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.chkGrantAccess.text")); // NOI18N
        chkGrantAccess.setToolTipText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.chkGrantAccess.AccessibleContext.accessibleDescription")); // NOI18N
        chkGrantAccess.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkGrantAccessItemStateChanged(evt);
            }
        });

        comboDatabaseName.setEditable(true);
        comboDatabaseName.setToolTipText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboDatabaseName.AccessibleContext.accessibleDescription")); // NOI18N
        comboDatabaseName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboDatabaseNameItemStateChanged(evt);
            }
        });

        comboUsers.setToolTipText(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboUsers.AccessibleContext.accessibleDescription")); // NOI18N
        comboUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboUsersActionPerformed(evt);
            }
        });

        messageLabel.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.messageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(progressLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateDatabasePanel.progressLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(chkGrantAccess, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(comboUsers, 0, 323, Short.MAX_VALUE)
                            .addComponent(comboDatabaseName, 0, 323, Short.MAX_VALUE)))
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(comboDatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkGrantAccess)
                    .addComponent(comboUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(messageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        comboDatabaseName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboDatabaseName.AccessibleContext.accessibleName")); // NOI18N
        comboDatabaseName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboDatabaseName.AccessibleContext.accessibleDescription")); // NOI18N
        comboUsers.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboUsers.AccessibleContext.accessibleName")); // NOI18N
        comboUsers.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.comboUsers.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void comboDatabaseNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboDatabaseNameItemStateChanged
    
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            validatePanel(evt.getItem().toString().trim());
        }
    }//GEN-LAST:event_comboDatabaseNameItemStateChanged

    private void chkGrantAccessItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkGrantAccessItemStateChanged
        comboUsers.setEnabled(isGrantAccess());
    }//GEN-LAST:event_chkGrantAccessItemStateChanged

    private void comboUsersActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboUsersActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboUsersActionPerformed
    
    private static class DatabaseComboModel implements ComboBoxModel {
        private final List<String> sampleNames = SampleManager.getSampleNames();
        private static final String SAMPLE_PREFIX =
                NbBundle.getMessage(CreateDatabasePanel.class, "CreateNewDatabasePanel.STR_SampleDatabase") + ": ";
        
        private String selected = null;

        @Override
        public void setSelectedItem(Object item) {
            selected = (String)item;
        }

        public boolean isSelectedSample() {
            return selected != null && selected.startsWith(SAMPLE_PREFIX);
        }

        @Override
        public Object getSelectedItem() {
            if (isSelectedSample()) {
                // trim off the "Sample database: " string
                return selected.replace(SAMPLE_PREFIX, "");
            } else if ( selected != null ) {
                return selected; 
            } else {
                return "";
            }
        }

        @Override
        public int getSize() {
            return sampleNames.size();
        }

        @Override
        public Object getElementAt(int index) {
            if (index < 0) {
                return null;
            }
            return SAMPLE_PREFIX + sampleNames.get(index).toString();
        }

        @Override
        public void addListDataListener(ListDataListener listener) {
        }

        @Override
        public void removeListDataListener(ListDataListener listener) {
        }
                
    }
    
    private static class UsersComboModel implements ComboBoxModel {

        private List<DatabaseUser> users= new ArrayList<DatabaseUser>();
        private DatabaseUser selected;

        public UsersComboModel(DatabaseServer server) throws DatabaseException {
            try {
                users.addAll(server.getUsers());
                
                // Remove the root user, this user always has full access
                DatabaseUser rootUser = null;
                for ( DatabaseUser user : users ) {
                    if ( user.getUser() != null && user.getUser().equals("root")) {
                        // Note the user.  We can't remove it while iterating,
                        // that's a concurrent modification...
                        rootUser = user;
                        break;
                    }
                }
                if (users.size () > 0) {
                    selected = users.get (0);
                }
                
                if ( rootUser != null ) {
                    users.remove(rootUser);
                }
            } catch ( DatabaseException dbe )  {
                if (! server.isConnected()) {
                    throw dbe;
                }

                // If we're still connected, log the error
                // and continue with an empty user list
                LOGGER.log(Level.INFO, null, dbe);
                users.clear();
            }
        }

        @Override
        public void setSelectedItem(Object item) {
            if (item == null || item.toString().trim().length() == 0) {
                return;
            }
            assert item instanceof DatabaseUser : item + "[" + item.getClass() + "] must be instanceof DatabaseUser";
            if (item instanceof DatabaseUser) {
                selected = (DatabaseUser)item;
            }
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public int getSize() {
            return users.size();
        }

        @Override
        public Object getElementAt(int index) {
            return users.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener arg0) {
        }

        @Override
        public void removeListDataListener(ListDataListener arg0) {
        }

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkGrantAccess;
    private javax.swing.JComboBox comboDatabaseName;
    private javax.swing.JComboBox comboUsers;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel progressLabel;
    // End of variables declaration//GEN-END:variables
    
}

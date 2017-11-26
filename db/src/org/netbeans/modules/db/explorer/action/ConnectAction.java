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

package org.netbeans.modules.db.explorer.action;



import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnection.State;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.ConnectPanel;
import org.netbeans.modules.db.explorer.dlg.ConnectProgressDialog;
import org.openide.nodes.Node;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.modules.db.explorer.node.ConnectionNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ConnectAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectAction.class.getName());

    ConnectionDialog dlg;
    boolean advancedPanel = false;

    @Override
    public String getName() {
        return NbBundle.getMessage (ConnectAction.class, "Connect"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;
        if (activatedNodes.length == 1) {
            Lookup lookup = activatedNodes[0].getLookup();
            ConnectionNode node = lookup.lookup(ConnectionNode.class);
            if (node != null) {
                DatabaseConnection dbconn = lookup.lookup(DatabaseConnection.class);
                enabled = ! dbconn.isConnected();
            }
        }

        return enabled;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        
        ConnectionNode node = activatedNodes[0].getLookup().lookup(ConnectionNode.class);
                
        // Don't show the dialog if all information is already available, 
        // just make the connection
        new ConnectionDialogDisplayer().showDialog(node, false);
    }

   
    public static final class ConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        ConnectionDialog dlg;
        
        // This flag is used to detect whether there was a failure to connect
        // when using the progress bar.  The flag is set in the property
        // change listener when the status changes to "failed".          
        boolean failed = false;

        /** Shows notification if DatabaseConnection fails. */
        final ExceptionListener excListener = new ExceptionListener() {

            @Override
            public void exceptionOccurred(Exception exc) {
                if (exc instanceof DDLException) {
                    LOGGER.log(Level.INFO, null, exc.getCause());
                } else {
                    LOGGER.log(Level.INFO, null, exc);
                }

                String message = null;
                if (exc instanceof ClassNotFoundException) {
                    message = MessageFormat.format(NbBundle.getMessage(ConnectAction.class, "EXC_ClassNotFound"), exc.getMessage()); //NOI18N
                } else {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(DbUtilities.formatError(NbBundle.getMessage(ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage())); //NOI18N
                    if (exc instanceof DDLException && exc.getCause() instanceof SQLException) {
                        SQLException sqlEx = ((SQLException) exc.getCause()).getNextException();
                        while (sqlEx != null) {
                            buffer.append("\n\n").append(sqlEx.getMessage()); // NOI18N
                            sqlEx = sqlEx.getNextException();
                        }
                    }
                    message = buffer.toString();
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        };

        private static HelpCtx CONNECT_ACTION_HELPCTX = new HelpCtx(ConnectAction.class);

        public void showDialog(final ConnectionNode model, boolean showDialog) {
            DatabaseConnection dbcon = model.getLookup().lookup(DatabaseConnection.class);
            showDialog(dbcon, showDialog);
        }

        public void showDialog(final DatabaseConnection dbcon, boolean showDialog) {
            String user = dbcon.getUser();
            boolean remember = dbcon.rememberPassword();

            dbcon.addExceptionListener(excListener);

            // If showDialog is true, show the dialog even if we have all 
            // the connection info
            //
            // Note that we don't have to show the dialog if the password is 
            // null and remember is true; null is often a valid password
            // (and is the default password for MySQL and PostgreSQL).
            if (((!supportsConnectWithoutUsername(dbcon))
                    && (user == null || !remember)) || showDialog) {
                final ConnectPanel basePanel = new ConnectPanel(this, dbcon);

                final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if ("state".equals(event.getPropertyName())) {
                            if (event.getNewValue() == State.connecting) {
                            fireConnectionStarted();
                            } else if (event.getNewValue() == State.failed) {
                            fireConnectionFailed();
                            } else if (event.getNewValue() == State.connected) {
                            fireConnectionFinished();
                            dbcon.setSchema(dbcon.getSchema());
                            
                            DatabaseConnection realDbcon = ConnectionList.getDefault().getConnection(dbcon);
                            if (realDbcon != null) {
                                realDbcon.setPassword(dbcon.getPassword());
                                realDbcon.setRememberPassword(dbcon.rememberPassword());
                            }
                            
                            dbcon.setRememberPassword(basePanel.rememberPassword());

                            if (dlg != null) {
                                dlg.close();
                            }
                        }
                    }
                    }
                };

                dbcon.addPropertyChangeListener(connectionListener);

                ActionListener actionListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (event.getSource() == DialogDescriptor.OK_OPTION) {
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                            dbcon.setRememberPassword(basePanel.rememberPassword());

                            if (! dbcon.isVitalConnection()) {
                                dbcon.connectAsync();
                            } else {
                                DatabaseConnection realDbcon = ConnectionList.getDefault().getConnection(dbcon);
                                if (realDbcon != null) {
                                    realDbcon.setPassword(dbcon.getPassword());
                                    realDbcon.setRememberPassword(
                                            basePanel.rememberPassword());
                                }
                                dbcon.setRememberPassword(basePanel.rememberPassword());

                                if (dlg != null) {
                                    dlg.close();
                                }
                            }
                        }
                    }
                };

                dlg = new ConnectionDialog(this, basePanel, basePanel.getTitle(), CONNECT_ACTION_HELPCTX, actionListener);
                dlg.setVisible(true);
            } else { // without dialog with connection data (username, password), just with progress dlg
                try {
                    DialogDescriptor descriptor = null;
                    ProgressHandle progress = null;
                    
                    progress = ProgressHandleFactory.createHandle("handle");
                    JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
                    progressComponent.setPreferredSize(new Dimension(350, 20));
                    ConnectProgressDialog panel = new ConnectProgressDialog(progressComponent, null);
                    panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ConnectAction.class, "ACS_ConnectingDialogTextA11yDesc"));
                    descriptor = new DialogDescriptor(panel, NbBundle.getMessage (ConnectAction.class, "ConnectingDialogTitle"), true, new Object[] {},
                            null, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    
                    final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                        @Override
                        public void propertyChange(final PropertyChangeEvent event) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    handlePropertyChange(event);
                                }
                            });
                        }

                        private void handlePropertyChange(PropertyChangeEvent event) {
                            if ("state".equals(event.getPropertyName())) {
                                if (event.getNewValue() == State.connected) { //NOI18N
                                    if (dialog != null) {
                                        dialog.setVisible(false);
                                    }
                                } else if (event.getNewValue() == State.failed) { // NOI18N
                                if (dialog != null) {
                                    dialog.setVisible(false);
                                }
                                
                                // We want to bring up the Connect dialog if the
                                // attempt to connect using the progress bar fails.
                                // But we can't do it here because we can't control
                                // what processing the DatabaseConnection does 
                                // after posting this failure notification.  So
                                // we set a flag and wait for the connect process
                                // to fully complete, and *then* raise the Connect
                                // dialog.
                                failed = true;
                            }
                        }
                        }
                    };
                    
                    failed = false;
                    
                    dbcon.addPropertyChangeListener(connectionListener);
                    dbcon.connectAsync();
                    
                    progress.start();
                    progress.switchToIndeterminate();
                    dialog.setVisible(true);
                    progress.finish();                    
                    dialog.dispose();
                    
                    if ( failed ) {
                        // If the connection fails with a progress bar only, then 
                        // display the full Connect dialog so the user can give it
                        // another shot after changing some values, like the username
                        // or password.
                        showDialog(dbcon, true);
                    }
                } catch (Exception exc) {
                    String message = MessageFormat.format(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    
                    // If the connection fails with a progress bar only, then 
                    // display the full Connect dialog so the user can give it
                    // another shot after changing some values, like the username
                    // or password.
                    showDialog(dbcon, true);
                }
            }

            dbcon.removeExceptionListener(excListener);
        }

        @Override
        protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
            fireConnectionStep(NbBundle.getMessage (ConnectAction.class, "ConnectionProgress_Schemas")); // NOI18N
            List<String> schemas = new ArrayList<String> ();
            try {
                DatabaseMetaData dbMetaData = dbcon.getJDBCConnection().getMetaData();
                if (dbMetaData.supportsSchemasInTableDefinitions()) {
                    ResultSet rs = dbMetaData.getSchemas();
                    if (rs != null) {
                        while (rs.next()) {
                            schemas.add(rs.getString(1).trim());
                        }
                    }
                }
            } catch (SQLException exc) {
                String message = NbBundle.getMessage(ConnectAction.class, "ERR_UnableObtainSchemas", exc.getMessage()); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }

            return schemaPanel.setSchemas(schemas, defaultSchema);
        }

        private boolean supportsConnectWithoutUsername(DatabaseConnection dc) {
            try {
                return dc.findJDBCDriver().getClassName().equals("org.sqlite.JDBC") //NOI18N
                        || dc.findJDBCDriver().getClassName().equals("org.h2.Driver"); //NOI18N
            } catch (NullPointerException ex) {
                // Most probably findJDBCDriver failed to find a driver
                return false;
            }
        }
    }

}

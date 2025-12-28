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
import java.awt.GraphicsEnvironment;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.JDBCDriver;
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
import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.modules.db.util.DriverListUtil;
import org.netbeans.modules.db.util.JdbcUrl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@ActionRegistration(
        displayName = "#Connect", 
        lazy = true,
        asynchronous = true,
        enabledOn = @ActionState(type = DatabaseConnection.class, useActionInstance = true)
)
@ActionID(category = "Database", id = "netbeans.db.explorer.action.Connect")
@ActionReference(path = "Databases/Explorer/Connection/Actions", position = 100)
public class ConnectAction extends AbstractAction implements ContextAwareAction, PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(ConnectAction.class.getName());
    private final DatabaseConnection connection;
    
    public ConnectAction(DatabaseConnection connection) {
        super(NbBundle.getMessage (ConnectAction.class, "Connect")); // NOI18N
        this.connection = connection;
        if (connection != null) {
            connection.addPropertyChangeListener(WeakListeners.propertyChange(this, connection));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (connection == null || connection.isConnected()) {
            return;
        }
        // Don't show the dialog if all information is already available, 
        // just make the connection
        new ConnectionDialogDisplayer().showDialog(connection, false);
    }

    @Override
    public boolean isEnabled() {
        return connection != null && !connection.isConnected();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ConnectAction(actionContext.lookup(DatabaseConnection.class));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(evt));
        } else {
            setEnabled(connection != null && !connection.isConnected());
        }
    }
   
    public static final class ConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        ConnectionDialog dlg;
        
        // This flag is used to detect whether there was a failure to connect
        // when using the progress bar.  The flag is set in the property
        // change listener when the status changes to "failed".          
        volatile boolean failed = false;
        volatile SQLException sqlException = null;

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
                sqlException = exc instanceof SQLException ? (SQLException)exc : null;
                if (exc instanceof ClassNotFoundException) {
                    message = MessageFormat.format(NbBundle.getMessage(ConnectAction.class, "EXC_ClassNotFound"), exc.getMessage()); //NOI18N
                } else {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(DbUtilities.formatError(NbBundle.getMessage(ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage())); //NOI18N
                    if (exc instanceof DDLException && exc.getCause() instanceof SQLException) {
                        sqlException = (SQLException) exc.getCause();
                        SQLException sqlEx = sqlException.getNextException();
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

        @NbBundle.Messages({
            "# {0} - connection name",
            "Progress_ConnectingDB=Connecting to {0}",
        })
        public void showDialog(final DatabaseConnection dbcon, boolean showDialog) {
            String user = dbcon.getUser();
            boolean remember = dbcon.rememberPassword();

            dbcon.addExceptionListener(excListener);

            final boolean headless = GraphicsEnvironment.isHeadless();
            
            // If showDialog is true, show the dialog even if we have all 
            // the connection info
            //
            // Note that we don't have to show the dialog if the password is 
            // null and remember is true; null is often a valid password
            // (and is the default password for MySQL and PostgreSQL).
            if (((!supportsConnectWithoutUsername(dbcon))
                    && (user == null || !remember)) || showDialog) {
                
                final Credentials input;
                if (headless) {
                    input = new CredetialsLine(dbcon);
                } else {
                    ConnectPanel basePanel = new ConnectPanel(this, dbcon);
                    input = new CredetialsUI(basePanel);
                }

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
                            
                            dbcon.setRememberPassword(input.rememberPassword());

                            if (!headless) {
                                SwingUtilities.invokeLater(() -> {
                                    if (dlg != null) {
                                        dlg.close();
                                    }
                                });
                            }
                        }
                    }
                    }
                };

                dbcon.addPropertyChangeListener(connectionListener);

                if (headless) {
                    connectWithNewInfo(dbcon, input);
                } else {
                    ActionListener actionListener = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                connectWithNewInfo(dbcon, input);
                            }
                        }
                    };

                    Mutex.EVENT.writeAccess((Mutex.Action<Void>) () -> {
                        ConnectPanel basePanel = input.getConnectPanel();
                        dlg = new ConnectionDialog(this, basePanel, basePanel.getTitle(), CONNECT_ACTION_HELPCTX, actionListener);
                        dlg.setVisible(true);
                        return null;
                    });
                }
                dbcon.removeExceptionListener(excListener);
            } else { // without dialog with connection data (username, password), just with progress dlg
                try {
                    DialogDescriptor descriptor = null;
                    ProgressHandle progress = null;
                    
                    progress = ProgressHandle.createHandle(Bundle.Progress_ConnectingDB(dbcon.getDisplayName()));
                    
                    final Dialog dialog;
                    
                    if (headless) {
                        dialog = null;
                    } else {
                        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
                        progressComponent.setPreferredSize(new Dimension(350, 20));
                        ConnectProgressDialog panel = new ConnectProgressDialog(progressComponent, null);
                        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ConnectAction.class, "ACS_ConnectingDialogTextA11yDesc"));
                        descriptor = new DialogDescriptor(panel, NbBundle.getMessage (ConnectAction.class, "ConnectingDialogTitle"), true, new Object[] {},
                                null, DialogDescriptor.DEFAULT_ALIGN, null, null);
                        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    }

                    final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                        @Override
                        public void propertyChange(final PropertyChangeEvent event) {
                            if (headless) {
                                handlePropertyChange(event);
                            } else {
                                Mutex.EVENT.readAccess(new Runnable() {
                                    @Override
                                    public void run() {
                                        handlePropertyChange(event);
                                    }
                                });
                            }
                        }

                        private void handlePropertyChange(PropertyChangeEvent event) {
                            if ("state".equals(event.getPropertyName())) {
                                if (event.getNewValue() == State.connected) { //NOI18N
                                    if (dialog != null) {
                                        dialog.setVisible(false);
                                    }
                                } else if (event.getNewValue() == State.failed) { // NOI18N
                                    // We want to bring up the Connect dialog if the
                                    // attempt to connect using the progress bar fails.
                                    // But we can't do it here because we can't control
                                    // what processing the DatabaseConnection does 
                                    // after posting this failure notification.  So
                                    // we set a flag and wait for the connect process
                                    // to fully complete, and *then* raise the Connect
                                    // dialog.
                                    failed = true;
                                    if (dialog != null) {
                                        dialog.setVisible(false);
                                    }
                                }
                            }
                        }
                    };

                    failed = false;

                    dbcon.addPropertyChangeListener(connectionListener);
                    try {
                        RequestProcessor.Task connectTask = dbcon.connectAsync();

                        progress.start();
                        progress.switchToIndeterminate();

                        if (dialog == null) {
                            connectTask.waitFinished();
                        } else {
                            dialog.setVisible(true);
                        }
                        progress.finish();                    

                        if (dialog != null) {
                            dialog.dispose();
                        }
                    } finally {                        
                        dbcon.removePropertyChangeListener(connectionListener);
                    }
                } catch (Exception exc) {
                    String message = MessageFormat.format(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    sqlException = exc instanceof SQLException ? (SQLException)exc : null;
                    failed = true;
                }
                dbcon.removeExceptionListener(excListener);
                if (failed && sqlException != null) {
                    // If the connection fails with a progress bar only, then 
                    // display the full Connect dialog so the user can give it
                    // another shot after changing some values, like the username
                    // or password.
                    Throwable cause = sqlException.getCause();
                    if (cause == null || !(cause instanceof ClassNotFoundException))
                        showDialog(dbcon, true);
                }
            }
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
            JDBCDriver driver = dc.findJDBCDriver();
            if (driver == null) {
                return false;
            }
            List<JdbcUrl> urls = DriverListUtil.getJdbcUrls(driver);
            if (urls.isEmpty()) {
                return false;
            }
            for (JdbcUrl url : urls) {
                if (url.isUsernamePasswordDisplayed()) {
                    return false;
                }
            }
            return true;
        }

        private void connectWithNewInfo(DatabaseConnection dbcon, Credentials input) {
            dbcon.setUser(input.getUser());
            dbcon.setPassword(input.getPassword());
            dbcon.setUser(input.getUser());
            dbcon.setPassword(input.getPassword());
            dbcon.setRememberPassword(input.rememberPassword());

            if (!dbcon.isVitalConnection()) {
                dbcon.connectAsync();
            } else {
                DatabaseConnection realDbcon = ConnectionList.getDefault().getConnection(dbcon);
                if (realDbcon != null) {
                    realDbcon.setPassword(dbcon.getPassword());
                    realDbcon.setRememberPassword(
                            input.rememberPassword());
                }
                dbcon.setRememberPassword(input.rememberPassword());

                if (dlg != null) {
                    dlg.close();
                }
            }
        }
    }

    private static abstract class Credentials {

        abstract String getUser();

        abstract String getPassword();

        abstract boolean rememberPassword();

        abstract ConnectPanel getConnectPanel();
    }

    private static class CredetialsLine extends Credentials {

        final private DatabaseConnection dbconnection;
        private String user;
        private String password;
        private boolean initialized;

        private CredetialsLine(DatabaseConnection dbconn) {
            dbconnection = dbconn;
        }

        private void init() {
            if (!initialized) {
                initialized = true;
                setUsernameAndPassword();
            }
        }

        @Override
        String getUser() {
            init();
            return user;
        }

        @Override
        String getPassword() {
            init();
            return password;
        }

        @Override
        boolean rememberPassword() {
            return dbconnection.rememberPassword();
        }

        @Override
        ConnectPanel getConnectPanel() {
            throw new IllegalStateException();
        }

        private boolean setUsernameAndPassword() {
            CredentialsCallback callback = new CredentialsCallback(dbconnection.getUser());
            NotifyDescriptor.ComposedInput userPassword = new NotifyDescriptor.ComposedInput(dbconnection.getDisplayName(), 2, callback);
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(userPassword)) {
                NotifyDescriptor[] inputs = userPassword.getInputs();
                user = ((NotifyDescriptor.InputLine)inputs[0]).getInputText();
                password = ((NotifyDescriptor.InputLine)inputs[1]).getInputText();
                return true;
            }
            return false;
        }
    }

    private static class CredetialsUI extends Credentials {

        private final ConnectPanel connectPanel;

        private CredetialsUI(ConnectPanel cp) {
            connectPanel = cp;
        }

        @Override
        String getPassword() {
            return connectPanel.getPassword();
        }

        @Override
        String getUser() {
            return connectPanel.getUser();
        }

        @Override
        boolean rememberPassword() {
            return connectPanel.rememberPassword();
        }

        @Override
        ConnectPanel getConnectPanel() {
            return connectPanel;
        }
    }

    @NbBundle.Messages({
        "MSG_EnterUsername=Enter Username",
        "MSG_EnterPassword=Enter Password",})
    private static class CredentialsCallback implements NotifyDescriptor.ComposedInput.Callback {

        private final String initialUser;

        public CredentialsCallback(String user) {
            initialUser = user;
        }

        @Override
        public NotifyDescriptor createInput(NotifyDescriptor.ComposedInput input, int number) {
            if (number == 1) {
                NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(Bundle.MSG_EnterUsername(), Bundle.MSG_EnterUsername());
                inputLine.setInputText(initialUser);
                return inputLine;
            } else if (number == 2) {
                return  new NotifyDescriptor.PasswordLine(Bundle.MSG_EnterPassword(),Bundle.MSG_EnterPassword());
            }
            return null;
        }
    }
}

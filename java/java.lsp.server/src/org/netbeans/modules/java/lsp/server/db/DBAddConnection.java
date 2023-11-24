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
package org.netbeans.modules.java.lsp.server.db;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.ComposedInput;
import org.openide.NotifyDescriptor.ComposedInput.Callback;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.NotifyDescriptor.PasswordLine;
import org.openide.NotifyDescriptor.QuickPick;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "MSG_AddDBConnection=Add Database Connection",
    "MSG_EnterDbUrl=Enter DB URL",
    "MSG_EnterUsername=Enter Username",
    "MSG_EnterPassword=Enter Password",
    "MSG_SelectDriver=Select db driver",
    "MSG_DriverNotFound=Driver not found",
    "MSG_ConnectionAdded=Connection added",
    "MSG_ConnectionFailed=Could not connect to the database \"{0}\", user {1}:\n{2}",
    "MSG_SelectSchema=Select Database Schema"
})
@ServiceProvider(service = CommandProvider.class)
public class DBAddConnection implements CommandProvider {
    public static final String DB_ADD_CONNECTION =  "nbls.db.add.connection"; // NOI18N
    public static final String USER_ID =  "userId"; // NOI18N
    public static final String PASSWORD =  "password"; // NOI18N
    public static final String DRIVER =  "driver"; // NOI18N
    public static final String DB_URL =  "url"; // NOI18N
    public static final String SCHEMA =  "schema"; // NOI18N
    public static final String DISPLAY_NAME =  "displayName"; // NOI18N

    private static final Logger LOG = Logger.getLogger(DBAddConnection.class.getName());
    private static final Map<String, String> urlTemplates = new HashMap<> ();
    static {
        urlTemplates.put("org.postgresql.Driver", "jdbc:postgresql://<HOST>:5432/<DB>");
        urlTemplates.put("org.gjt.mm.mysql.Driver", "jdbc:mysql://<HOST>:3306/<DB>");
        urlTemplates.put("com.mysql.cj.jdbc.Driver", "jdbc:mysql://<HOST>:3306/<DB>");
        urlTemplates.put("org.mariadb.jdbc.Driver", "jdbc:mariadb://<HOST>:3306/<DB>");
        urlTemplates.put("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@//<HOST>[:<PORT>][/<SERVICE>]");
        urlTemplates.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://<HOST>\\<DB>[:<PORT>]");
    }
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        if (arguments != null && !arguments.isEmpty()) {
            final Map m = arguments.get(0) instanceof JsonNull ? Collections.emptyMap() : gson.fromJson((JsonObject) arguments.get(0), Map.class);
            String userId = m != null ? (String) m.get(USER_ID) : null;
            String password = m != null ? (String) m.get(PASSWORD) : null;
            String dbUrl = m != null ? (String) m.get(DB_URL) : null;
            String driverClass = m != null ? (String) m.get(DRIVER) : null;
            if (dbUrl != null && driverClass != null) {

                JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(driverClass);
                if (driver != null && driver.length > 0) {
                    if (userId == null || password == null) {
                        Callback inputCallback = (input, number) -> {
                            switch (number) {
                                case 1: {
                                    InputLine inputLine = new InputLine("", Bundle.MSG_EnterUsername());
                                    String userIdVal = userId != null ? userId : "";
                                    inputLine.setInputText(userIdVal);
                                    return inputLine;
                                }
                                case 2: {
                                    PasswordLine inputLine = new PasswordLine("", Bundle.MSG_EnterUsername());
                                    String passwordVal = password != null ? password : "";
                                    inputLine.setInputText(passwordVal);
                                    return inputLine;
                                }
                                default:
                                    return null;
                            }
                        };
                        DialogDisplayer.getDefault().notifyFuture(new ComposedInput(Bundle.MSG_AddDBConnection(), 2, inputCallback)).thenAccept(input -> {
                            String newUser = ((InputLine) input.getInputs()[0]).getInputText();
                            String newPasswd = ((InputLine) input.getInputs()[1]).getInputText();
                            DatabaseConnection dbconn = DatabaseConnection.create(driver[0], dbUrl, newUser, (String) m.get(SCHEMA), newPasswd, true, (String) m.get(DISPLAY_NAME));
                            try {
                                ConnectionManager.getDefault().addConnection(dbconn);
                                DialogDisplayer.getDefault().notifyLater(new Message(Bundle.MSG_ConnectionAdded(), Message.INFORMATION_MESSAGE));
                            } catch (DatabaseException ex) {
                                LOG.log(Level.INFO, "Add connection", ex);
                                DialogDisplayer.getDefault().notifyLater(new Message(ex.getMessage(), Message.ERROR_MESSAGE));
                            }
                        });
                    } else {
                        DatabaseConnection dbconn = DatabaseConnection.create(driver[0], dbUrl, userId, (String) m.get(SCHEMA), password, true, (String) m.get(DISPLAY_NAME));
                        try {
                            ConnectionManager.getDefault().addConnection(dbconn);
                            DialogDisplayer.getDefault().notifyLater(new Message(Bundle.MSG_ConnectionAdded(), Message.INFORMATION_MESSAGE));
                        } catch (DatabaseException ex) {
                            LOG.log(Level.INFO, "Add connection with schema", ex);
                            DialogDisplayer.getDefault().notifyLater(new Message(ex.getMessage(), Message.ERROR_MESSAGE));
                        }
                    }
                }
                return CompletableFuture.completedFuture(null);
            }
        }
        
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        List<Item> items = new ArrayList<>();
        Map<Item, JDBCDriver> item2Driver = new IdentityHashMap<>();
        for (int i = 0; i < drivers.length; i++) {
            URL[] jars = drivers[i].getURLs();
            if (jars != null && jars.length > 0) {
                FileObject jarFO = URLMapper.findFileObject(jars[0]);
                if (jarFO != null && jarFO.isValid()) {
                    Item item =
                            new Item(drivers[i].getName(), drivers[i].getDisplayName() + " (" + drivers[i].getClassName() + ")"); // NOI18N
                    items.add(item);
                    item2Driver.put(item, drivers[i]);
                }
            }
        }
        if (items.isEmpty()) {
            DialogDisplayer.getDefault().notifyLater(new Message(Bundle.MSG_DriverNotFound(), Message.ERROR_MESSAGE));
        } else {
            List<String> schemas = new ArrayList<>();
            Map<Item, String> item2Scheme = new IdentityHashMap<>();
            Callback inputCallback = new Callback() {
                @Override
                public NotifyDescriptor createInput(ComposedInput input, int number) {
                    switch (number) {
                        case 1:
                            return new QuickPick("", Bundle.MSG_SelectDriver(), items, false);
                        case 2: {
                            JDBCDriver driver = item2Driver.get(getSelectedItem((QuickPick) input.getInputs()[0]));
                            String urlTemplate = driver.getClassName() != null ? urlTemplates.get(driver.getClassName()) : "";

                            if (urlTemplate == null) {
                                urlTemplate = "";
                            }

                            InputLine line = new InputLine("", Bundle.MSG_EnterDbUrl());

                            line.setInputText(urlTemplate);

                            return line;
                        }
                        case 3: {
                            return new InputLine("", Bundle.MSG_EnterUsername());
                        }
                        case 4: {
                            //should be:
                            PasswordLine passwd = new PasswordLine("", Bundle.MSG_EnterPassword());
                            passwd.setInputTextEventEnabled(true);
                            passwd.addPropertyChangeListener(evt -> {
                                if (evt.getPropertyName() == null ||
                                    InputLine.PROP_INPUT_TEXT.equals(evt.getPropertyName())) {
                                    validateConnection(passwd, input);
                                }
                            });
                            return passwd;
                        }
                        case 5: {
                            if (schemas.isEmpty()) {
                                DialogDisplayer.getDefault().notifyLater(new Message(Bundle.MSG_ConnectionAdded(), Message.INFORMATION_MESSAGE));
                                return null;
                            } else {
                                List<Item> schemaItems = new ArrayList<>();

                                for (String schema : schemas) {
                                    Item item = new Item(schema, schema);
                                    schemaItems.add(item);
                                    item2Scheme.put(item, schema);
                                }

                                return new QuickPick("", Bundle.MSG_SelectSchema(), schemaItems, false);
                            }
                        }
                        default:
                            return null;
                    }
                }

                private void validateConnection(NotifyDescriptor current, ComposedInput input) {
                    JDBCDriver driver = item2Driver.get(getSelectedItem((QuickPick) input.getInputs()[0]));
                    String url = ((InputLine) input.getInputs()[1]).getInputText();
                    String user = ((InputLine) input.getInputs()[2]).getInputText();
                    String passwd = ((InputLine) input.getInputs()[3]).getInputText();
                    boolean failed = true;

                    schemas.clear();

                    DatabaseConnection dbconn = DatabaseConnection.create(driver, url, user, null, passwd, true);
                    try {
                        ConnectionManager.getDefault().addConnection(dbconn);
                        schemas.addAll(getSchemas(dbconn));
                        failed = false;
                    } catch(SQLException ex) {
                        LOG.log(Level.INFO, "validate", ex);
                        current.createNotificationLineSupport().setErrorMessage(ex.getMessage());
                        current.setValid(false);
                    } catch (DatabaseException ex) {
                        String message;
                        Throwable cause = ex.getCause();
                        if (cause == null) cause = ex;
                        if (cause.getCause() != null) {
                            message = Bundle.MSG_ConnectionFailed(url, user, cause.getCause().getMessage());
                        } else {
                            message = cause.getMessage();
                        }
                        LOG.log(Level.INFO, "validate", ex);
                        current.createNotificationLineSupport().setErrorMessage(message);
                        current.setValid(false);
                    } finally {
                        try {
                            if (failed || !schemas.isEmpty()) {
                                ConnectionManager.getDefault().removeConnection(dbconn);
                            }
                        } catch (DatabaseException ex) {}
                    }
                }
            };
            return DialogDisplayer.getDefault().notifyFuture(new ComposedInput(Bundle.MSG_AddDBConnection(), 4, inputCallback)).thenApply(input -> {
                JDBCDriver driver = item2Driver.get(getSelectedItem((QuickPick) input.getInputs()[0]));
                String url = ((InputLine) input.getInputs()[1]).getInputText();
                String user = ((InputLine) input.getInputs()[2]).getInputText();
                String passwd = ((InputLine) input.getInputs()[3]).getInputText();
                String schema = item2Scheme.get(getSelectedItem((QuickPick) input.getInputs()[4]));

                if (driver != null && url != null && user != null && passwd != null && schema != null) {
                    DatabaseConnection dbconn = DatabaseConnection.create(driver, url, user, schema, passwd, true);
                    try {
                        ConnectionManager.getDefault().addConnection(dbconn);
                        DialogDisplayer.getDefault().notifyLater(new Message(Bundle.MSG_ConnectionAdded(), Message.INFORMATION_MESSAGE));
                    } catch (DatabaseException ex) {
                        LOG.log(Level.INFO, "add", ex);
                        DialogDisplayer.getDefault().notifyLater(new Message(ex.getMessage(), Message.ERROR_MESSAGE));
                    }
                }
                return null;
            });
        }
        return null;
    }

    private static Item getSelectedItem(QuickPick pick) {
        for (Item i : pick.getItems()) {
            if (i.isSelected()) {
                return i;
            }
        }
        return null;
    }

    private static List<String> getSchemas(DatabaseConnection dbconn) throws SQLException, DatabaseException {
        List<String> schemas = new ArrayList<>();
        if (ConnectionManager.getDefault().connect(dbconn)) {
            DatabaseMetaData dbMetaData = dbconn.getJDBCConnection().getMetaData();
            if (dbMetaData.supportsSchemasInTableDefinitions()) {
                ResultSet rs = dbMetaData.getSchemas();
                if (rs != null) {
                    while (rs.next()) {
                        schemas.add(rs.getString(1).trim());
                    }
                }
            }
        }
        return schemas;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(DB_ADD_CONNECTION);
    }

}

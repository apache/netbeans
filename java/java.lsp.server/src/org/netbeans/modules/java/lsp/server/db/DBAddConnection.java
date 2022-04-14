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
import com.google.gson.JsonObject;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowQuickPickParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "MSG_EnterDbUrl=Enter DB URL",
    "MSG_EnterUsername=Enter Username",
    "MSG_EnterPassword=Enter Password",
    "MSG_SelectDriver=Select db driver",
    "MSG_DriverNotFound=Driver not found",
    "MSG_ConnectionAdded=Connection added",
    "MSG_ConnectionFailed=Connection failed",
    "MSG_SelectSchema=Select Database Schema"
})
@ServiceProvider(service = CodeActionsProvider.class)
public class DBAddConnection extends CodeActionsProvider {
    public static final String DB_ADD_CONNECTION =  "db.add.connection"; // NOI18N
    public static final String USER_ID =  "userId"; // NOI18N
    public static final String PASSWORD =  "password"; // NOI18N
    public static final String DRIVER =  "driver"; // NOI18N
    public static final String DB_URL =  "url"; // NOI18N
    public static final String SCHEMA =  "schema"; // NOI18N
    public static final String DISPLAY_NAME =  "displayName"; // NOI18N

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
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (!DB_ADD_CONNECTION.equals(command)) {
            return null;
        }
        
        String userId = null;
        String dbUrl = null;
        String driverClass = null;
        if (arguments != null && arguments.size() > 0) {
            final Map m = gson.fromJson((JsonObject) arguments.get(0), Map.class);
            if (m != null) {
                userId = (String) m.get(USER_ID);
                dbUrl = (String) m.get(DB_URL);
                driverClass = (String) m.get(DRIVER);
            }
            if (dbUrl != null && driverClass != null) {

                JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(driverClass); //NOI18N
                if (driver != null && driver.length > 0) {
                    CompletableFuture<String> usernameFuture = userId != null ? CompletableFuture.completedFuture(userId) : client.showInputBox(new ShowInputBoxParams(
                            Bundle.MSG_EnterUsername(), userId));

                    usernameFuture.thenAccept((username) -> { //NOI18N
                        if (username == null) {
                            return;
                        }
                        String password = (String) m.get(PASSWORD);
                        CompletableFuture<String> passwordFuture = password != null ? CompletableFuture.completedFuture(password) : client.showInputBox(new ShowInputBoxParams(
                                Bundle.MSG_EnterPassword(), "", true));
                        passwordFuture.thenAccept((p) -> { //NOI18N
                            if (p == null) {
                                return;
                            }
                            DatabaseConnection dbconn = DatabaseConnection.create(driver[0], (String) m.get(DB_URL), username, (String) m.get(SCHEMA), p, true, (String) m.get(DISPLAY_NAME));
                            try {
                                ConnectionManager.getDefault().addConnection(dbconn);
                            } catch (DatabaseException ex) {
                                client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                            }
                        });
                    });
                    client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                }
                return CompletableFuture.completedFuture(null);
            }
        }
        
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        List<QuickPickItem> items = new ArrayList<>();
        for (int i = 0; i < drivers.length; i++) {
            URL[] jars = drivers[i].getURLs();
            if (jars != null && jars.length > 0) {
                FileObject jarFO = URLMapper.findFileObject(jars[0]);
                if (jarFO != null && jarFO.isValid()) {
                    items.add(
                            new QuickPickItem(drivers[i].getName(), null, drivers[i].getDisplayName() + " (" + drivers[i].getClassName() + ")", false, i) // NOI18N
                    );
                }
            }
        }

        return client.showQuickPick(new ShowQuickPickParams(Bundle.MSG_SelectDriver(), false, items))
                .thenApply(selectedItems -> {
                    if (selectedItems == null) {
                        return null;
                    }
                    if (!selectedItems.isEmpty()) {
                        int i = ((Double) selectedItems.get(0).getUserData()).intValue();
                        JDBCDriver driver = drivers[i];
                        String urlTemplate = driver.getClassName() != null ? urlTemplates.get(driver.getClassName()) : "";
                        if (urlTemplate == null) {
                            urlTemplate = "";
                        }
                        client.showInputBox(new ShowInputBoxParams(
                                Bundle.MSG_EnterDbUrl(), urlTemplate)).thenAccept((u) -> {
                            if (u == null) {
                                return;
                            }
                            client.showInputBox(new ShowInputBoxParams(
                                    Bundle.MSG_EnterUsername(), "")).thenAccept((username) -> { //NOI18N
                                if (username == null) {
                                    return;
                                }
                                client.showInputBox(new ShowInputBoxParams(
                                        Bundle.MSG_EnterPassword(), "", true)).thenAccept((password) -> { //NOI18N
                                    if (password == null) {
                                        return;
                                    }
                                    DatabaseConnection dbconn = DatabaseConnection.create(driver, u, username, null, password, true);
                                    try {
                                        ConnectionManager.getDefault().addConnection(dbconn);
                                        List<String> schemas = getSchemas(dbconn);
                                        if (schemas.isEmpty()) {
                                            client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                                        } else {
                                            List<QuickPickItem> schemaItems = schemas.stream().map(schema -> new QuickPickItem(schema)).collect(Collectors.toList());
                                            client.showQuickPick(new ShowQuickPickParams(Bundle.MSG_SelectSchema(), schemaItems)).thenAccept((s) -> {
                                                if (s == null) {
                                                    try {
                                                        // Command was interrupted, we don't want the connection to be added
                                                        ConnectionManager.getDefault().removeConnection(dbconn);
                                                    } catch (DatabaseException ex) {
                                                        StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                                                    }
                                                    return;
                                                }
                                                String schema = s.get(0).getLabel();
                                                DatabaseConnection dbconn1 = DatabaseConnection.create(driver, u, username, schema, password, true);
                                                try {
                                                    ConnectionManager.getDefault().removeConnection(dbconn);
                                                    ConnectionManager.getDefault().addConnection(dbconn1);
                                                } catch (DatabaseException ex) {
                                                    client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                                                    return;
                                                }
                                                client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                                            });
                                        }
                                    } catch (SQLException | DatabaseException ex) {
                                        client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                                    } 
                                });
                            });

                        });

                    } else {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_DriverNotFound()));
                    }
                    return null;
                });
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

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    
}

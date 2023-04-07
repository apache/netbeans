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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.java.lsp.server.input.InputBoxStep;
import org.netbeans.modules.java.lsp.server.input.InputCallbackParams;
import org.netbeans.modules.java.lsp.server.input.InputService;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.QuickPickStep;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
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
        InputService.Registry inputServiceRegistry = Lookup.getDefault().lookup(InputService.Registry.class);
        if (inputServiceRegistry == null) {
            return null;
        }
        
        if (arguments != null && !arguments.isEmpty()) {
            final Map m = arguments.get(0) instanceof JsonNull ? Collections.emptyMap() : gson.fromJson((JsonObject) arguments.get(0), Map.class);
            String userId = m != null ? (String) m.get(USER_ID) : null;
            String password = m != null ? (String) m.get(PASSWORD) : null;
            String dbUrl = m != null ? (String) m.get(DB_URL) : null;
            String driverClass = m != null ? (String) m.get(DRIVER) : null;
            if (dbUrl != null && driverClass != null) {

                JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(driverClass); //NOI18N
                if (driver != null && driver.length > 0) {
                    if (userId == null || password == null) {
                        String inputId = inputServiceRegistry.registerInput(param -> {
                            int totalSteps = 2;
                            switch (param.getStep()) {
                                case 1:
                                    return CompletableFuture.completedFuture(Either.forRight(new InputBoxStep(totalSteps, USER_ID, Bundle.MSG_EnterUsername(), userId)));
                                case 2:
                                    Map<String, Either<List<QuickPickItem>, String>> data = param.getData();
                                    Either<List<QuickPickItem>, String> userData = data.get(USER_ID);
                                    if (userData != null) {
                                        return CompletableFuture.completedFuture(Either.forRight(new InputBoxStep(totalSteps, PASSWORD, null, Bundle.MSG_EnterUsername(), password, true)));
                                    }
                                    return CompletableFuture.completedFuture(null);
                                default:
                                    return CompletableFuture.completedFuture(null);
                            }
                        });
                        client.showMultiStepInput(new ShowMutliStepInputParams(inputId, Bundle.MSG_AddDBConnection())).thenAccept(result -> {
                            Either<List<QuickPickItem>, String> userData = result.get(USER_ID);
                            Either<List<QuickPickItem>, String> passwordData = result.get(PASSWORD);
                            DatabaseConnection dbconn = DatabaseConnection.create(driver[0], dbUrl, userData.getRight(), (String) m.get(SCHEMA), passwordData.getRight(), true, (String) m.get(DISPLAY_NAME));
                            try {
                                ConnectionManager.getDefault().addConnection(dbconn);
                                client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                            } catch (DatabaseException ex) {
                                client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                            }
                        });
                    } else {
                        DatabaseConnection dbconn = DatabaseConnection.create(driver[0], dbUrl, userId, (String) m.get(SCHEMA), password, true, (String) m.get(DISPLAY_NAME));
                        try {
                            ConnectionManager.getDefault().addConnection(dbconn);
                            client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                        } catch (DatabaseException ex) {
                            client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                        }
                    }
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
        if (items.isEmpty()) {
            client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_DriverNotFound()));
        } else {
            List<String> schemas = new ArrayList<>();
            String inputId = inputServiceRegistry.registerInput(new InputService.Callback() {
                @Override
                public CompletableFuture<Either<QuickPickStep, InputBoxStep>> step(InputCallbackParams params) {
                    Map<String, Either<List<QuickPickItem>, String>> data = params.getData();
                    int totalSteps = 4;
                    switch (params.getStep()) {
                        case 1:
                            return CompletableFuture.completedFuture(Either.forLeft(new QuickPickStep(totalSteps, DRIVER, Bundle.MSG_SelectDriver(), items)));
                        case 2: {
                            Either<List<QuickPickItem>,String> driverData = data.get(DRIVER);
                            if (driverData != null && !driverData.getLeft().isEmpty()) {
                                int i = ((Double) driverData.getLeft().get(0).getUserData()).intValue();
                                JDBCDriver driver = drivers[i];
                                String urlTemplate = driver.getClassName() != null ? urlTemplates.get(driver.getClassName()) : "";
                                if (urlTemplate == null) {
                                    urlTemplate = "";
                                }
                                return CompletableFuture.completedFuture(Either.forRight(new InputBoxStep(totalSteps, DB_URL, Bundle.MSG_EnterDbUrl(), urlTemplate)));
                            }
                            return CompletableFuture.completedFuture(null);
                        }
                        case 3: {
                            Either<List<QuickPickItem>,String> urlData = data.get(DB_URL);
                            if (urlData != null && !urlData.getRight().isEmpty()) {
                                return CompletableFuture.completedFuture(Either.forRight(new InputBoxStep(totalSteps, USER_ID, Bundle.MSG_EnterUsername(), "")));
                            }
                            return CompletableFuture.completedFuture(null);
                        }
                        case 4: {
                            Either<List<QuickPickItem>,String> userData = data.get(USER_ID);
                            if (userData != null && !userData.getRight().isEmpty()) {
                                return CompletableFuture.completedFuture(Either.forRight(new InputBoxStep(totalSteps, PASSWORD, null, Bundle.MSG_EnterPassword(), "", true)));
                            }
                            return CompletableFuture.completedFuture(null);
                        }
                        case 5: {
                            Either<List<QuickPickItem>,String> passwordData = data.get(PASSWORD);
                            if (passwordData != null) {
                                if (schemas.isEmpty()) {
                                    client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                                    return CompletableFuture.completedFuture(null);
                                } else {
                                    List<QuickPickItem> schemaItems = schemas.stream().map(schema -> new QuickPickItem(schema)).collect(Collectors.toList());
                                    return CompletableFuture.completedFuture(Either.forLeft(new QuickPickStep(totalSteps + 1, SCHEMA, Bundle.MSG_SelectSchema(), schemaItems)));
                                }
                            }
                            return CompletableFuture.completedFuture(null);
                        }
                        default:
                            return CompletableFuture.completedFuture(null);
                    }
                }

                @Override
                public CompletableFuture<String> validate(InputCallbackParams params) {
                    Map<String, Either<List<QuickPickItem>, String>> data = params.getData();
                    switch (params.getStep()) {
                        case 4:
                            Either<List<QuickPickItem>,String> passwordData = data.get(PASSWORD);
                            if (passwordData != null) {
                                Either<List<QuickPickItem>,String> driverData = data.get(DRIVER);
                                Either<List<QuickPickItem>,String> urlData = data.get(DB_URL);
                                Either<List<QuickPickItem>,String> userData = data.get(USER_ID);
                                int i = ((Double) driverData.getLeft().get(0).getUserData()).intValue();
                                JDBCDriver driver = drivers[i];
                                schemas.clear();
                                DatabaseConnection dbconn = DatabaseConnection.create(driver, urlData.getRight(), userData.getRight(), null, passwordData.getRight(), true);
                                try {
                                    ConnectionManager.getDefault().addConnection(dbconn);
                                    schemas.addAll(getSchemas(dbconn));
                                } catch(DatabaseException | SQLException ex) {
                                    return CompletableFuture.completedFuture(ex.getMessage());
                                } finally {
                                    try {
                                        ConnectionManager.getDefault().removeConnection(dbconn);
                                    } catch (DatabaseException ex) {}
                                }
                            }
                            break;
                    }
                    return CompletableFuture.completedFuture(null);
                }
            });
            return client.showMultiStepInput(new ShowMutliStepInputParams(inputId, Bundle.MSG_AddDBConnection())).thenApply(result -> {
                Either<List<QuickPickItem>,String> driverData = result.get(DRIVER);
                Either<List<QuickPickItem>,String> urlData = result.get(DB_URL);
                Either<List<QuickPickItem>,String> userData = result.get(USER_ID);
                Either<List<QuickPickItem>,String> passwordData = result.get(PASSWORD);
                Either<List<QuickPickItem>,String> schemaData = result.get(SCHEMA);
                if (driverData != null && urlData != null && userData != null && passwordData != null && schemaData != null && !schemaData.getLeft().isEmpty()) {
                    int i = ((Double) driverData.getLeft().get(0).getUserData()).intValue();
                    JDBCDriver driver = drivers[i];
                    String schema = schemaData.getLeft().get(0).getLabel();
                    DatabaseConnection dbconn = DatabaseConnection.create(driver, urlData.getRight(), userData.getRight(), schema, passwordData.getRight(), true);
                    try {
                        ConnectionManager.getDefault().addConnection(dbconn);
                    } catch (DatabaseException ex) {
                        client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                    }
                    client.showMessage(new MessageParams(MessageType.Info, Bundle.MSG_ConnectionAdded()));
                }
                return null;
            });
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

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    
}

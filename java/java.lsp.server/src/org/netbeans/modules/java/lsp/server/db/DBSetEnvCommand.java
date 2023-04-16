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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.UpdateConfigParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.lookup.ServiceProvider;

/**
 * Updates Run Configuratoin with Database env variables.
 * 
 * @author Jan Horvath
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class DBSetEnvCommand extends CodeActionsProvider {
    private static final String  COMMAND_SET_DB_ENV = "java.db.set.env"; //NOI18N
    private static final String CONFIG_SECTION = "java+.runConfig"; //NOI18N
    
    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
        COMMAND_SET_DB_ENV
    ));
    
    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (!COMMAND_SET_DB_ENV.equals(command)) {
            return null;
        }
        DatabaseConnection conn = ConnectionManager.getDefault().getPreferredConnection(true);
        Map<String, String> props = new HashMap<>();
        props.put("DATASOURCES_DEFAULT_URL", conn.getDatabaseURL()); //NOI18N
        props.put("DATASOURCES_DEFAULT_USERNAME", conn.getUser()); //NOI18N
        props.put("DATASOURCES_DEFAULT_PASSWORD", conn.getPassword()); //NOI18N
        props.put("DATASOURCES_DEFAULT_DRIVER_CLASS_NAME", conn.getDriverClass()); //NOI18N
        String values = props.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue()) //NOI18N
                .collect(Collectors.joining(",")); //NOI18N
        client.configurationUpdate(new UpdateConfigParams(CONFIG_SECTION, "env", values)); //NOI18N
        return CompletableFuture.completedFuture(props);
    }
    
    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }
}

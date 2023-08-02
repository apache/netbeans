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
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * @author Jan Horvath
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class DBConnectionProvider extends CodeActionsProvider{
    private static final String  GET_DB_CONNECTION = "java.db.connection"; //NOI18N
    
    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
        GET_DB_CONNECTION
    ));
    

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        Map<String, String> result = new HashMap<> ();
        DatabaseConnection conn = ConnectionManager.getDefault().getPreferredConnection(true);

        if (conn != null) {
            result.put("DATASOURCES_DEFAULT_URL", conn.getDatabaseURL()); //NOI18N
            result.put("DATASOURCES_DEFAULT_USERNAME", conn.getUser()); //NOI18N
            result.put("DATASOURCES_DEFAULT_PASSWORD", conn.getPassword()); //NOI18N
            result.put("DATASOURCES_DEFAULT_DRIVER_CLASS_NAME", conn.getDriverClass()); //NOI18N
            String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
            if (ocid != null && !ocid.isEmpty()) {
                result.put("DATASOURCES_DEFAULT_OCID", ocid); //NOI18N
            }
        }
            
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }
}

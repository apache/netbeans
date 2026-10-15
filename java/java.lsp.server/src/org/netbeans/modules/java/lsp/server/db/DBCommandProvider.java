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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.java.lsp.server.explorer.TreeNodeRegistry;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = CommandProvider.class)
public class DBCommandProvider implements CommandProvider{
    private static final String  COMMAND_GET_PREFERRED_CONNECTION = "nbls.db.preferred.connection";
    
    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
        COMMAND_GET_PREFERRED_CONNECTION
    ));

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        TreeNodeRegistry r = Lookup.getDefault().lookup(TreeNodeRegistry.class);
        DatabaseConnection conn = ConnectionManager.getDefault().getPreferredConnection(true);
        if (conn == null || r == null) {
            return CompletableFuture.completedFuture(null);
        }
        return r.createProvider(DBConnectionExplorer.ID).thenCompose((m) -> {
                Node root = m.getExplorerManager().getRootContext();
                for (Node n : root.getChildren().getNodes(true)) {
                    DatabaseConnection d = n.getLookup().lookup(DatabaseConnection.class);
                    if (d != null && d.getName().equals(conn.getName())) {
                        return (CompletionStage<Object>)(CompletionStage)m.getNodeId(n);
                    }
                }
                return CompletableFuture.completedFuture(null);
            }).toCompletableFuture();
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(COMMAND_GET_PREFERRED_CONNECTION);
    }
}

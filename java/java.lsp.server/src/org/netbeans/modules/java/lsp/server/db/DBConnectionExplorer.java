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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(path = "Explorers/" + DBConnectionExplorer.ID, service = ExplorerManagerFactory.class)
public class DBConnectionExplorer implements ExplorerManagerFactory {
    public static final String ID = "database.connections"; // NOI18N
    
    @Override
    public CompletionStage<ExplorerManager> createManager(String id, Lookup context) {
        if (!ID.equals(id)) {
            throw new IllegalArgumentException("Unsupported view");
        }
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(DatabaseExplorerUIs.connectionsNode());
        return CompletableFuture.completedFuture(em);
    }
}

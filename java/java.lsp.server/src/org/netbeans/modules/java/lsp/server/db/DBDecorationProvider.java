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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides decorations (connection status) for the Database node.
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service = TreeDataProvider.Factory.class, path = "Explorers/database.connections")
public class DBDecorationProvider implements TreeDataProvider.Factory {

    private static final String CONNECTED = "is:connected";             // NOI18N
    private static final String DISCONNECTED = "is:disconnected";       // NOI18N
    private static final Logger LOG = Logger.getLogger(DBDecorationProvider.class.getName());

    @Override
    public TreeDataProvider createProvider(String treeId) {
        LOG.log(Level.FINE, "Creating default DBDecorationProvider for {0}", treeId);   // NOI18N
        return new DBDataProvider();
    }

    private static class DBDataProvider implements TreeDataProvider {

        @Override
        public TreeItemData createDecorations(Node n, boolean expanded) {
            DatabaseConnection conn = n.getLookup().lookup(DatabaseConnection.class);

            if (conn != null) {
                String status = conn.getJDBCConnection() != null ? CONNECTED : DISCONNECTED;
                TreeItemData data = new TreeItemData();
                data.setContextValues(status);
                return data;
            }
            return null;
        }

        @Override
        public void addTreeItemDataListener(TreeDataListener l) {
        }

        @Override
        public void removeTreeItemDataListener(TreeDataListener l) {
        }

        @Override
        public void nodeReleased(Node n) {
        }
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.metadata;

import java.sql.Connection;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbMetaDataListener;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.MetadataModels;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.util.Mutex;

/**
 * Provides access to the database model for DB Explorer database connections.
 * This class is temporary, as such acess should be provided directly by
 * the DB Explorer through a {@code DatabaseConnection.getMetadataModel()} method.
 *
 * @author Andrei Badea
 */
public class MetadataModelManager {
    private static final Logger LOGGER = Logger.getLogger(MetadataModelManager.class.getName());

    // XXX test against memory leak.
    // XXX test if DatabaseConnection can be GC'd.

    private static final WeakHashMap<DatabaseConnection, MetadataModel> conn2Model = new WeakHashMap<DatabaseConnection, MetadataModel>();


    private MetadataModelManager() {}

    public static MetadataModel get(final DatabaseConnection dbconn) {
        synchronized (MetadataModelManager.class) {
            Connection conn = checkAndGetConnection(dbconn);
            MetadataModel model = conn2Model.get(dbconn);
            if (model == null) {
                model = MetadataModels.createModel(conn, dbconn.getSchema());
                conn2Model.put(dbconn, model);
            }
            return model;
        }
    }

    public static void update(final DatabaseConnection dbconn, MetadataModel model) {
        conn2Model.put(dbconn, model);
    }
    
    private static Connection checkAndGetConnection(final DatabaseConnection dbconn) {
        Connection conn = dbconn.getJDBCConnection();

        if (conn == null) {
            conn = Mutex.EVENT.readAccess(new org.openide.util.Mutex.Action<Connection>() {
                public Connection run() {
                    ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    return dbconn.getJDBCConnection();
                }
            });
        }

        return conn;
    }

    private static final class Listener implements DbMetaDataListener {

        // Remove when issue 141698 is fixed.
        private static Listener create() {
            return new Listener();
        }

        private Listener() {}

        // Refreshing in the calling thread is prone to deadlock:
        //
        // 1. TH1: SQL completion acquires model lock, model calls showConnectionDialog() in EDT.
        // 2. EDT: DB Explorer posts connect dialog in EDT, spawns TH2 to connect.
        // 3. TH2: notifies tables have changed.
        // 4. TH2: closes the dialog opened in #2.
        //
        // If in #3 TH2 wants to acquire the model lock, it doesn't createModel it since it
        // is held by TH1, #4 is never performed and the connect dialog stays open.

        public void tablesChanged(DatabaseConnection dbconn) {
            // First make sure we're connected
            Connection conn = checkAndGetConnection(dbconn);

            if (conn == null) {
                return;
            }

            try {
                get(dbconn).runReadAction(new Action<Metadata>() {
                    public void run(Metadata md) {
                        md.refresh();
                    }
                });
            } catch (MetadataModelException mde) {
                LOGGER.log(Level.INFO, mde.getMessage(), mde);
            }

        }

        public void tableChanged(DatabaseConnection dbconn, final String tableName) {
            // First make sure we're connected
            Connection conn = checkAndGetConnection(dbconn);

            if (conn == null) {
                return;
            }

            try {
                get(dbconn).runReadAction(new Action<Metadata>() {
                    public void run(Metadata md) {
                        Table table = md.getDefaultSchema().getTable(tableName);

                        // There is a slight possibility that the table was removed
                        // between the time tableChanged() was invoked and now,
                        // so check first...
                        if (table != null) {
                            md.getDefaultSchema().getTable(tableName).refresh();
                        } else {
                            LOGGER.log(Level.INFO, "Table '" + tableName + "' that was just changed no longer exists");
                        }
                    }
                });
            } catch (MetadataModelException mde) {
                LOGGER.log(Level.INFO, mde.getMessage(), mde);
            }

        }
    }
}

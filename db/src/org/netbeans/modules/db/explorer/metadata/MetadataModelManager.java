/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    private final static WeakHashMap<DatabaseConnection, MetadataModel> conn2Model = new WeakHashMap<DatabaseConnection, MetadataModel>();


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

    private final static class Listener implements DbMetaDataListener {

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

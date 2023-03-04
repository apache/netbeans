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

package org.netbeans.modules.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.modules.ModuleInstall;

public class DatabaseModule extends ModuleInstall {

    public static final String IDENTIFIER_MYSQL = "MySQL"; // NOI18N
    public static final String IDENTIFIER_ORACLE = "Oracle"; // NOI18N
    public static final String IDENTIFIER_ORACLE_OCI_DRIVER = "OCI"; // NOI18N
    
    @Override
    public void close () {
        // XXX this method is called in the event thread and could take long
        // to execute

        if (ConnectionList.getDefault(false) != null) {
            DBConnection[] conns = ConnectionList.getDefault().getConnections();
            for (int i = 0; i < conns.length; i++) {
                try {
                    ((DatabaseConnection)conns[i]).disconnect();
                } catch (Exception e) {
                    // cf. issue 64185 exceptions should only be logged
                    Logger.getLogger(DatabaseModule.class.getName()).log(Level.INFO, null, e);
                }
            }
        }

        // stop all running runtimes
        if (DatabaseRuntimeManager.isInstantiated()) {
            DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
            for (int i = 0; i < runtimes.length; i++) {
                if (runtimes[i].isRunning()) {
                    try {
                        runtimes[i].stop();
                    } catch (Exception e) {
                        // cf. issue 64185 exceptions should only be logged
                        Logger.getLogger(DatabaseModule.class.getName()).log(Level.INFO, null, e);
                    }
                }
            }
        }
    }
}

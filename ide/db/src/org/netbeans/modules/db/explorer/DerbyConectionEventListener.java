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

package org.netbeans.modules.db.explorer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.openide.util.Exceptions;

/**
 * This class receives notifications about connection events which
 * are used to apply the hacks for Derby. It gathers these hacks
 * together instead of having them spread all over the code.
 *
 * <p>In the future and if necessary a ConnectionEventListener interface should be defined,
 * which this class should implement. It should be possible to register
 * implementations of the CEL inteface, for example in the layer.</p>
 * 
 * @author Andrei Badea
 */
public class DerbyConectionEventListener {
    
    // XXX this class should be called DerbyConnectionEventListener (double 'n' in connection)
    
    private static final DerbyConectionEventListener DEFAULT = new DerbyConectionEventListener();
    
    private static final String DERBY_DATABASE_FORCE_LOCK = "derby.database.forceDatabaseLock"; // NOI18N
    private static final String DERBY_SYSTEM_HOME = "derby.system.home"; // NOI18N
    private static final String DERBY_SYSTEM_SHUTDOWN_STATE = "XJ015"; // NOI18N
    
    public static DerbyConectionEventListener getDefault() {
        return DEFAULT;
    }

    /**
     * Called before a database connection is connected.
     *
     * @param dbconn the database connection.
     */
    public void beforeConnect(DatabaseConnection dbconn) {
        if (!dbconn.getDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) { // NOI18N
            return;
        }
        
        // force the database lock -- useful on Linux, see issue 63957
        if (System.getProperty(DERBY_DATABASE_FORCE_LOCK) == null) {
            System.setProperty(DERBY_DATABASE_FORCE_LOCK, "true"); // NOI18N
        }
        
        // set the system directory, see issue 64316
        if (System.getProperty(DERBY_SYSTEM_HOME) == null) { // NOI18N
            File derbySystemHome = new File(System.getProperty("netbeans.user"), "derby"); // NOI18N
            derbySystemHome.mkdirs();
            System.setProperty(DERBY_SYSTEM_HOME, derbySystemHome.getAbsolutePath()); // NOI18N
        }
    }
    
    /**
     * Called after a database connection was disconnected. 
     *
     * @param dbconn the database connection.
     * @param conn the closed {@link java.sql.Connection}. This parameter is needed since dbconn.getJDBCConnection()
        returns null at the moment when afterDisconnect is called.
     */
    public void afterDisconnect(DatabaseConnection dbconn, Connection conn) {
        if (!dbconn.getDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) { // NOI18N
            return;
        }
        
        // shutdown the Derby database instance
        try {
            DbDriverManager.getDefault().getSameDriverConnection(conn, "jdbc:derby:;shutdown=true", new Properties()); // NOI18N
        } catch (SQLException e) {
            if (!DERBY_SYSTEM_SHUTDOWN_STATE.equals(e.getSQLState())) { // NOI18N
                Exceptions.printStackTrace(e);
            }
        }
    }
}

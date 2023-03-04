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

package org.netbeans.modules.db.mysql.impl;

import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.mysql.DatabaseServerManager;
import org.netbeans.modules.db.mysql.util.DatabaseUtils.URLParser;

/**
 * Listen to changes on the connection list, and if we're not registered
 * and a MySQL connection is added, register the MySQL 
 * 
 * @author David Van Couvering
 */
public class DbExplorerConnectionListener implements ConnectionListener {

    public void connectionsChanged() {
        MySQLOptions options = MySQLOptions.getDefault();
        
        if ( options.isProviderRegistered() || options.isProviderRemoved() ) {
            return;
        }

        DatabaseConnection[] connections = 
            ConnectionManager.getDefault().getConnections();

        for ( DatabaseConnection conn : connections ) {
            if ( conn.getDriverClass().equals(MySQLOptions.getDriverClass()) ) {
                DatabaseServer instance = DatabaseServerManager.getDatabaseServer();
                URLParser parser = new URLParser(conn.getDatabaseURL());
                instance.setHost(parser.getHost());
                instance.setPort(parser.getPort());
                instance.setUser(conn.getUser());
                instance.setPassword(conn.getPassword());
                
                ServerNodeProvider.getDefault().setRegistered(true);
            }
        }
    }

}

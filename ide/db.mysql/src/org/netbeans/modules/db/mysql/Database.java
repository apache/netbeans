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

package org.netbeans.modules.db.mysql;

import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;

/**
 * Model class representing a database.  Right now all it has is a 
 * name, but in the future it may provide more information.
 * 
 * @author David Van Couvering
 */
public class Database implements Node.Cookie {
    private final String name;
    private final DatabaseServer server;
    
    public Database(DatabaseServer server, String dbname) {
        name = dbname;
        this.server = server;
    }

    public String getDbName() {
        return name;
    }
    
    public String getDisplayName() {
        return name;
    }

    public String getShortDescription() {
       return Utils.getMessage( 
               "LBL_DBNodeShortDescription",
               getDisplayName());
    }
    
    public DatabaseServer getServer() {
        return server;
    }
    
    @Override
    public boolean equals(Object other) {
        return other instanceof Database &&
               ((Database) other).getDbName().equals(getDbName());
    }
    
    @Override
    public int hashCode() {
        return getDbName().hashCode();
    }
}

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

/**
 * Represents a database user
 * 
 * @author David Van Couvering
 */
public class DatabaseUser {
    private final String user;
    private final String host;
    
    public DatabaseUser(String user, String host) {
        this.user = user;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }    
    
    /** 
     * Used for displaying on forms and such.  DON'T use this in SQL
     * commands, which may require quoting
     */
    @Override
    public String toString() {
        return 
            (user == null || user.equals("") ? "*" : user) +
            "@" +
            (host == null || host.equals("") || host.equals("%") ? "*" : host);
    }
}

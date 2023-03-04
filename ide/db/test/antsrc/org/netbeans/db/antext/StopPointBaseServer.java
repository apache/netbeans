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

package org.netbeans.db.antext;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class implementing way how to shutdown PointDase server.
 * Necessary system properties: <tt>pointbase.db.driverclass</tt>, <tt>pointbase.db.url</tt>, <tt>pointbase.db.user</tt>, <tt>pointbase.db.password</tt>.
 * This class is called from <tt>pointbase-server.xml</tt> script.
 *
 * @author Patrik Knakal
 * @version 1.0
 */
public class StopPointBaseServer extends Object {
    private static final String shutdown = "shutdown force"; //NOI18N

    public static void main(String[] atrg) {
        //System.out.println("driverclass ... " + System.getProperty("pointbase.db.driverclass") ); //NOI18N
        //System.out.println("url         ... " + System.getProperty("pointbase.db.url") ); //NOI18N
        //System.out.println("user        ... " + System.getProperty("pointbase.db.user") ); //NOI18N
        //System.out.println("password    ... " + System.getProperty("pointbase.db.password") ); //NOI18N
        try {
            Class.forName(System.getProperty("pointbase.db.driverclass") ); //NOI18N
            Connection connection = DriverManager.getConnection(System.getProperty("pointbase.db.url"), System.getProperty("pointbase.db.user"), System.getProperty("pointbase.db.password") ); //NOI18N
            Statement statement = connection.createStatement();
            statement.executeUpdate(shutdown);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}

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

package org.netbeans.modules.dbschema.test.dbsupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author David
 */
public class MySQLDBSupport extends DbSupport {
    private static final MySQLDBSupport DEFAULT = new MySQLDBSupport();

    public static MySQLDBSupport getInstance() {
        return DEFAULT;
    }

    private static final FEATURE[] FEATURES = { FEATURE.AUTOINCREMENT };
    private static final Collection<FEATURE> FEATURE_LIST = Arrays.asList(FEATURES);

    public Collection<FEATURE> getSupportedFeatures() {
        return FEATURE_LIST;
    }

    public void createAITable(Connection conn, String tableName, String columnName) throws Exception {
        try {
            conn.createStatement().execute("DROP TABLE " + tableName);
        } catch ( SQLException sqle ) {
            System.out.println("WARNING: Got exception trying to drop table: " + sqle.getMessage());
        }

        String sql = "CREATE TABLE " + tableName +
            " ( " + columnName + " MEDIUMINT NOT NULL AUTO_INCREMENT, PRIMARY KEY(" + columnName + "), " +
            "othercol VARCHAR(255))";
        conn.createStatement().execute(sql);
    }

    public void createSequenceTable(Connection conn, String tableName, String columnName) throws Exception {
        throw new UnsupportedOperationException("MySQL doesn't do sequences");
    }

}

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

package org.netbeans.modules.db.metadata.model.jdbc.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCProcedure;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;

/**
 * @author David, Jiri Rechtacek
 */
public class MySQLSchema extends JDBCSchema {
    
    private static final Logger LOGGER = Logger.getLogger(MySQLSchema.class.getName());
    
    public MySQLSchema(JDBCCatalog jdbcCatalog, String name, boolean _default, boolean synthetic) {
        super(jdbcCatalog, name, _default, synthetic);
    }

    @Override
    protected void createProcedures() {
        LOGGER.log(Level.FINE, "Initializing MySQL procedures in {0}", this);
        Map<String, Procedure> newProcedures = new LinkedHashMap<String, Procedure>();
        // information_schema.routines
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Statement stmt = dmd.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT routine_name,routine_type" // NOI18N
                                           + " FROM information_schema.routines" // NOI18N
                                           + " WHERE routine_type IN ('PROCEDURE','FUNCTION')" // NOI18N
                                           + " AND routine_schema='" + jdbcCatalog.getName() + "'"); // NOI18N
            try {
                while (rs.next()) {
                    String procedureName = rs.getString("routine_name"); // NOI18N
                    Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                    newProcedures.put(procedureName, procedure);
                    LOGGER.log(Level.FINE, "Created MySQL procedure: {0}, type: {1}", new Object[]{procedure, rs.getString("routine_type")});
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                stmt.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        // information_schema.triggers
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Statement stmt = dmd.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TRIGGER_NAME" // NOI18N
                                            + " FROM information_schema.triggers WHERE TRIGGER_SCHEMA='" + jdbcCatalog.getName() + "'"); // NOI18N
            try {
                while (rs.next()) {
                    String procedureName = rs.getString("TRIGGER_NAME"); // NOI18N
                    Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                    newProcedures.put(procedureName, procedure);
                    LOGGER.log(Level.FINE, "Created MySQL trigger: {0}", new Object[]{procedure});
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                stmt.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        procedures = Collections.unmodifiableMap(newProcedures);
    }
    
    @Override
    protected JDBCProcedure createJDBCProcedure(String procedureName) {
        return new MySQLProcedure(this, procedureName);
    }

    @Override
    public String toString() {
        return "MySQLSchema[jdbcCatalog=" + jdbcCatalog.getName() + ", name=" + getName() + "]";
    }

}

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
package org.netbeans.modules.db.metadata.model.jdbc.oracle;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;

/**
 *
 * @author Andrei Badea
 */
public class OracleSchema extends JDBCSchema {

    private static final Logger LOGGER = Logger.getLogger(OracleSchema.class.getName());

    public OracleSchema(JDBCCatalog catalog, String name, boolean _default, boolean synthetic) {
        super(catalog, name, _default, synthetic);
    }

    @Override
    public String toString() {
        return "OracleSchema[name='" + name + "',default=" + _default + ",synthetic=" + synthetic + "]"; // NOI18N
    }

    @Override
    protected void createTables() {
        LOGGER.log(Level.FINE, "Initializing tables in {0}", this);
        Map<String, Table> newTables = new LinkedHashMap<String, Table>();
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Set<String> recycleBinTables = getRecycleBinObjects(dmd, "TABLE"); // NOI18N
            ResultSet rs = dmd.getTables(jdbcCatalog.getName(), name, "%", new String[]{"TABLE"}); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        String type = MetadataUtilities.trimmed(rs.getString("TABLE_TYPE")); //NOI18N
                        String tableName = rs.getString("TABLE_NAME"); // NOI18N
                        if (!recycleBinTables.contains(tableName)) {
                            Table table = createJDBCTable(tableName, type.contains("SYSTEM")).getTable(); //NOI18N
                            newTables.put(tableName, table);
                            LOGGER.log(Level.FINE, "Created table {0}", table);
                        } else {
                            LOGGER.log(Level.FINE, "Ignoring recycle bin table ''{0}''", tableName);
                        }
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        tables = Collections.unmodifiableMap(newTables);
    }

    private Set<String> getRecycleBinObjects(DatabaseMetaData dmd, String... types) {
        String driverName = null;
        String driverVer = null;
        List<String> emptyList = Collections.emptyList();
        Set<String> result = new HashSet<String>();
        try {
            driverName = dmd.getDriverName();
            driverVer = dmd.getDriverVersion();
            int databaseMajorVersion = 0;
            try {
                databaseMajorVersion = dmd.getDatabaseMajorVersion();
            } catch (UnsupportedOperationException use) {
                LOGGER.log(Level.FINEST, "getDatabaseMajorVersion() on " + dmd, use);
            }
            if (databaseMajorVersion < 10 || types == null) {
                return Collections.emptySet();
            }
            Statement stmt = dmd.getConnection().createStatement();
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT OBJECT_NAME, TYPE FROM SYS.DBA_RECYCLEBIN"); // NOI18N
            } catch (SQLException ex) {
                LOGGER.log(Level.FINE, ex.getMessage(), ex); 
                // try both
                rs = stmt.executeQuery("SELECT OBJECT_NAME, TYPE FROM RECYCLEBIN"); // NOI18N
            }
            if (rs != null) {
                List<String> typesL = types == null ? emptyList : Arrays.asList(types);
                try {
                    while (rs.next()) {
                        String type = rs.getString("TYPE"); // NOI18N
                        if (typesL.isEmpty() || typesL.contains(type)) {
                            result.add(rs.getString("OBJECT_NAME")); // NOI18N
                        }
                    }
                } finally {
                    rs.close();
                }
            }
            stmt.close();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Error while analyzing the recycle bin. JDBC Driver: " + driverName + "(" + driverVer + ")", e);
        }
        return result;
    }

    @Override
    protected void createProcedures() {
        LOGGER.log(Level.FINE, "Initializing Oracle procedures in {0}", this);
        Map<String, Procedure> newProcedures = new LinkedHashMap<String, Procedure>();
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            PreparedStatement pstmt = dmd.getConnection().prepareStatement(
                    "SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM SYS.ALL_OBJECTS WHERE OWNER=? " // NOI18N
                    + "AND ( OBJECT_TYPE = 'PROCEDURE' OR OBJECT_TYPE = 'TRIGGER' OR OBJECT_TYPE = 'FUNCTION' )"); // NOI18N
            pstmt.setString(1, name);
            Set<String> recycleBinObjects = getRecycleBinObjects(dmd, "TRIGGER", "FUNCTION", "PROCEDURE"); // NOI18N
            ResultSet rs = pstmt.executeQuery();
            try {
                while (rs.next()) {
                    String procedureName = rs.getString("OBJECT_NAME"); // NOI18N
                    Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                    if (!recycleBinObjects.contains(procedureName)) {
                        newProcedures.put(procedureName, procedure);
                        LOGGER.log(Level.FINE, "Created Oracle procedure: {0}, type: {1}, status: {2}", new Object[]{procedure, rs.getString("OBJECT_TYPE"), rs.getString("STATUS")});
                    } else {
                        LOGGER.log(Level.FINEST, "Oracle procedure found id RECYCLEBIN: {0}, type: {1}, status: {2}", new Object[]{procedure, rs.getString("OBJECT_TYPE"), rs.getString("STATUS")});
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        procedures = Collections.unmodifiableMap(newProcedures);
    }
}

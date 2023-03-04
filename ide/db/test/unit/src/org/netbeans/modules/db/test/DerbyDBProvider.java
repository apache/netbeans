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
package org.netbeans.modules.db.test;

import java.sql.*;

/**
 *
 * @author David
 */
public class DerbyDBProvider extends DefaultDBProvider {

    private String quote(String name, boolean identifier) {
        String quote = identifier ? "\"" : "'";
        return quote + name.replace(quote, quote + quote) + quote;
    }

    private String qualifiedName(String schema, String object) {
        return quote(schema, true) + "." + quote(object, true);
    }

    @Override
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        if (!schemaExists(conn, schemaName)) {
            return;
        }

        // With Derby, you can't just drop the schema.  You have go manually
        // deal with all the constraints
        
        // drop views first, as they depend on tables
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs;
        Statement s = conn.createStatement();

        rs = md.getFunctions(null, schemaName, "%");
        while (rs.next()) {
            s.execute("DROP FUNCTION " + qualifiedName(schemaName, rs.getString("FUNCTION_NAME")));
        }
        rs.close();

        rs = md.getProcedures(null, schemaName, "%");
        while (rs.next()) {
            s.execute("DROP PROCEDURE " + qualifiedName(schemaName, rs.getString("PROCEDURE_NAME")));
        }
        rs.close();

        rs = md.getExportedKeys(null, schemaName, "%");
        while (rs.next()) {
            String referencingTable = qualifiedName(rs.getString("FKTABLE_SCHEMA"),
                    rs.getString("FKTABLE_NAME"));
            s.execute("ALTER TABLE " + referencingTable
                    + " DROP FOREIGN KEY " + rs.getString("FK_NAME"));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"VIEW"});
        while (rs.next()) {
            s.execute("DROP VIEW " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"SYNONYM"});
        while (rs.next()) {
            s.execute("DROP SYNONYM " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"TABLE"});
        while (rs.next()) {
            s.execute("DROP TABLE " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getUDTs(null, schemaName, "%", null);
        while (rs.next()) {
            s.execute("DROP TYPE " + qualifiedName(schemaName, rs.getString("TYPE_NAME"))
                    + " RESTRICT ");
        }
        rs.close();

        try {
            PreparedStatement psf = conn.prepareStatement(
                    "SELECT SEQUENCENAME FROM SYS.SYSSEQUENCES A, SYS.SYSSCHEMAS S"
                    + " WHERE A.SCHEMAID = S.SCHEMAID "
                    + " AND S.SCHEMANAME = ?");
            psf.setString(1, schemaName);
            rs = psf.executeQuery();
            while (rs.next()) {
                s.execute("DROP SEQUENCE " + qualifiedName(schemaName, rs.getString(1)) + " RESTRICT");
            }
            psf.close();
        } catch (SQLException ex) {
            // SQLState 42X05 => Table does not exists => OK, no sequences to drop
            if (!ex.getSQLState().equals("42X05")) {
                throw ex;
            }
        }

        if (!schemaName.equals("APP")) {
            s.executeUpdate("DROP SCHEMA " + quote(schemaName, true) + " RESTRICT");
        }
        s.close();
    }
}

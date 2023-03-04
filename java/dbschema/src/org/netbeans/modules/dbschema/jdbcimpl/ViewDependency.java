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
package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewDependency {

    private static final Logger LOG = Logger.getLogger(ViewDependency.class.getName());

    private final Connection con;
    private final String user;
    private final String view;
    private final DatabaseMetaData dmd;
    private final LinkedList<String> tables;
    private final LinkedList<String> columns;

    /**
     * Creates new ViewDependency
     */
    public ViewDependency(ConnectionProvider cp, String user, String view) throws SQLException {
        con = cp.getConnection();
        this.user = user;
        this.view = view;
        dmd = cp.getDatabaseMetaData();

        tables = new LinkedList<>();
        columns = new LinkedList<>();
    }

    public LinkedList getTables() {
        return tables;
    }

    public LinkedList getColumns() {
        return columns;
    }

    public void constructPK() {
        try {
            String database = dmd.getDatabaseProductName();
            if (database == null) {
                return;
            }
            database = database.trim();
            if (database.equalsIgnoreCase("Oracle")) {
                getOraclePKTable(user, view, new HashSet<String>());
                getOracleViewColumns();
            } else if (database.equalsIgnoreCase("Microsoft SQL Server")) {
                getMSSQLServerPKTable(user, view, new HashSet<String>());
                getMSSQLServerViewColumns();
            }
        } catch (SQLException exc) {
            LOG.log(Level.INFO, "Failed to find primary key by view");
        }
    }

    private void getOraclePKTable(String user, String view, Set<String> analyzedDependency) throws SQLException {
        if (analyzedDependency.contains(view)) {
            LOG.log(Level.WARNING, "Cyclic dependency detected in view definition: {0}", view);
            return;
        }

        analyzedDependency.add(view);

        String query = "select OWNER, REFERENCED_OWNER, REFERENCED_NAME, REFERENCED_TYPE from ALL_DEPENDENCIES where NAME = ? AND OWNER = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, view);
            stmt.setString(2, user);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString(4).trim();
                    if (type.equalsIgnoreCase("TABLE")) {
                        tables.add(rs.getString(3).trim());
                    } else if (type.equalsIgnoreCase("VIEW")) {
                        getOraclePKTable(rs.getString(2), rs.getString(3), analyzedDependency);
                    }
                }
            }
        }
    }

    private void getMSSQLServerPKTable(String user, String view, Set<String> analyzedDependency) throws SQLException {
        if (analyzedDependency.contains(view)) {
            LOG.log(Level.WARNING, "Cyclic dependency detected in view definition: {0}", view);
            return;
        }

        analyzedDependency.add(view);

        try (CallableStatement cs = con.prepareCall("{call sp_depends(?)}")) {
            cs.setString(1, view);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString(2).trim().toLowerCase();
                    String name = rs.getString(1).trim();
                    int pos = name.lastIndexOf(".");
                    name = name.substring(pos + 1);

                    if (type.contains("table")) {
                        if (!tables.contains(name)) {
                            tables.add(name);
                            continue;
                        }
                    } else if (type.equals("view")) {
                        getMSSQLServerPKTable(user, name, analyzedDependency);
                    }
                }
            } catch (Exception exc) {
                LOG.log(Level.INFO, "Failed to resolve MSSQLServerPKTable", exc);
            }
        }
    }

    private void getOracleViewColumns() throws SQLException {
        String query = "select TEXT from ALL_VIEWS where VIEW_NAME = ?";

        String text = null;

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, view);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    text = rs.getString(1).trim();
                }
            }
        }

        if (text == null) {
            return;
        }

        int startPos = text.indexOf(" ");
        int endPos = text.toLowerCase().indexOf("from");
        text = text.substring(startPos, endPos).trim();

        StringTokenizer st = new StringTokenizer(text, ",");
        String colName;
        while (st.hasMoreTokens()) {
            colName = st.nextToken().trim();
            if (colName.startsWith("\"")) {
                colName = colName.substring(1, colName.length() - 1);
            }
            columns.add(colName.toLowerCase());
        }
    }

    private void getMSSQLServerViewColumns() throws SQLException {
        try (CallableStatement cs = con.prepareCall("{call sp_helptext(?)}")) {
            cs.setString(1, view);

            String text = null;

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    text += rs.getString(1).trim();
                }
            }

            if (text == null) {
                return;
            }

            int startPos = text.toLowerCase().indexOf("select") + 6;
            int endPos = text.toLowerCase().indexOf("from");
            text = text.substring(startPos, endPos).trim();

            StringTokenizer st = new StringTokenizer(text, ",");
            String colName;
            while (st.hasMoreTokens()) {
                colName = st.nextToken().trim();
                if (colName.startsWith("\"")) {
                    colName = colName.substring(1, colName.length() - 1);
                }
                columns.add(colName.toLowerCase());
            }
        } catch (Exception exc) {
            LOG.log(Level.INFO, "Failed to resolve MSSQLServerViewColumns", exc);
        }
    }

}

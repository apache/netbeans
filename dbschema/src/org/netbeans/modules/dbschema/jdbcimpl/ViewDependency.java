/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

        tables = new LinkedList();
        columns = new LinkedList();
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

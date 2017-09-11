/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 - 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.metadata.model.jdbc.oracle;

import java.sql.DatabaseMetaData;
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
            Statement stmt = dmd.getConnection().createStatement();
            Set<String> recycleBinObjects = getRecycleBinObjects(dmd, "TRIGGER", "FUNCTION", "PROCEDURE"); // NOI18N
            ResultSet rs = stmt.executeQuery("SELECT OBJECT_NAME, OBJECT_TYPE, STATUS FROM SYS.ALL_OBJECTS WHERE OWNER='" + name + "'" // NOI18N
                    + " AND ( OBJECT_TYPE = 'PROCEDURE' OR OBJECT_TYPE = 'TRIGGER' OR OBJECT_TYPE = 'FUNCTION' )"); // NOI18N
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
            stmt.close();
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        procedures = Collections.unmodifiableMap(newProcedures);
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Andrei Badea
 */
public class MetadataUtilities {

    private MetadataUtilities() {}

    public static <V> V find(String key, Map<String, ? extends V> map) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        for (Entry<String, ? extends V> entry : map.entrySet()) {
            if (equals(key, entry.getKey(), true)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static boolean equals(String str1, String str2) {
        return equals(str1, str2, false);
    }

    private static boolean equals(String str1, String str2, boolean ignoreCase) {
        if (str1 == null) {
            return str2 == null;
        } else {
            return ignoreCase ? str1.equalsIgnoreCase(str2) : str1.equals(str2);
        }
    }
    
    public static String trimmed(String input) {
        if(input == null) {
            return input;
        } else {
            return input.trim();
        }
    }

    /**
     * Call {@link DatabaseMetaData#getColumns(String, String, String, String)},
     * wrapping any internal runtime exception into an {@link SQLException}.
     */
    public static ResultSet getColumns(DatabaseMetaData dmd, String catalog,
            String schemaPattern, String tableNamePattern,
            String columnNamePattern) throws SQLException {
        try {
            return dmd.getColumns(catalog, schemaPattern, tableNamePattern,
                    columnNamePattern);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#getIndexInfo(String, String, String,
     * boolean, boolean)}, wrapping any internal runtime exception into an
     * {@link SQLException}.
     */
    public static ResultSet getIndexInfo(DatabaseMetaData dmd,
            String catalog, String schema, String table,
            boolean unique, boolean approximate) throws SQLException {
        try {
            return dmd.getIndexInfo(catalog, schema, table, unique,
                    approximate);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#getImportedKeys(String, String, String)},
     * wrapping any internal runtime exception into an {@link SQLException}.
     */
    public static ResultSet getImportedKeys(DatabaseMetaData dmd,
            String catalog, String schema, String table) throws SQLException {
        try {
            return dmd.getImportedKeys(catalog, schema, table);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#getPrimaryKeys(String, String, String)},
     * wrapping any internal runtime exeption into an {@link SQLException}.
     */
    public static ResultSet getPrimaryKeys(DatabaseMetaData dmd,
            String catalog, String schema, String table) throws SQLException {
        try {
            return dmd.getPrimaryKeys(catalog, schema, table);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#getTables(String, String, String,
     * String[])}, wrapping any internal runtime exception into an
     * {@link SQLException}.
     */
    public static ResultSet getTables(DatabaseMetaData dmd,
            String catalog, String schemaPattern, String tableNamePattern,
            String[] types) throws SQLException {
        try {
            return dmd.getTables(catalog, schemaPattern, tableNamePattern,
                    types);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#getProcedures(String, String, String)},
     * wrapping any internal runtime exception into an {@link SQLException}.
     */
    public static ResultSet getProcedures(DatabaseMetaData dmd,
            String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        try {
            return dmd.getProcedures(catalog, schemaPattern,
                    procedureNamePattern);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }

    /**
     * Call {@link DatabaseMetaData#geFunctions(String, String, String)},
     * wrapping any internal runtime exception into an {@link SQLException}.
     */
    public static ResultSet getFunctions(DatabaseMetaData dmd,
            String catalog, String schemaPattern, String functionNamePattern)
            throws SQLException {
        try {
            return dmd.getFunctions(catalog, schemaPattern,
                    functionNamePattern);
        } catch (SQLException e) {
            throw e;
        } catch (Throwable t) {
            throw new SQLException(t);
        }
    }
}

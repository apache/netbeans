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
package org.netbeans.modules.db.explorer.action;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Rob Englander
 */
public abstract class QueryAction extends BaseAction {

    private static final Logger LOG = Logger.getLogger(QueryAction.class.getName());
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result;

        // either 1 table or view node, or 1 more more column nodes
        if (activatedNodes.length == 1) {
            Lookup lookup = activatedNodes[0].getLookup();
            result = lookup.lookup(TableNode.class) != null ||
                    lookup.lookup(ViewNode.class) != null ||
                    lookup.lookup(ColumnNode.class) != null;
        } else {
            result = true;
            for (Node node : activatedNodes) {
                if (node.getLookup().lookup(ColumnNode.class) == null) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Used by the {@code getQualifiedTableName} method to append a catalog or a
     * schema into a {@code StringBuilder} that will contain the qualified table
     * name.  This method is run for the name of the table's catalog first and
     * then the table's schema.  The builder is appended if the catalog/schema
     * name is different from that which is the default for the database
     * connection.  The name part is always added if the table has already been
     * qualified (as defined by the {@code tableIsQualified} parameter.  This
     * ensures that the schema name is always appended if the catalog is
     * appended (when the schema is not null).  Some databases, notably MySQL,
     * define catalogs and not schemas.  In this case the schema name is null
     * and is never appended onto the table name buffer.
     * @param tableNameBuilder the buffer that will ultimately contain the table
     *                          name that is sufficiently qualified to be accessed
     *                          over a given database connection
     * @param quoter puts SQL identifiers is quotes when needed.
     * @param name of the catalog/schema that the table is defined under
     * @param authenticatedName the corresponding catalog/schema that is the
     *                          default for this database connection
     * @param tableIsQualified true when the table name has already been appended
     *                          and therefore should be appended to by this call
     *                          (assuming that {@code name} is not null).
     * @return true if table name buffer has been appended to or {@code tableIsQualified} is true
     */
    private boolean appendQualifiedName(
            StringBuilder tableNameBuilder,
            SQLIdentifiers.Quoter quoter,
            String name,
            String authenticatedName,
            boolean tableIsQualified,
            String seperator) {
        if (name != null && (tableIsQualified || !name.equals(authenticatedName))) {
            tableNameBuilder.append(quoter.quoteIfNeeded(name));
            if(seperator == null || seperator.trim().isEmpty()) {
                tableNameBuilder.append( "." );
            } else {
                tableNameBuilder.append(seperator);
            }
            return true;
        } else {
            return tableIsQualified;
        }
    }

    /**
     * Get the table that is sufficiently specified to enable it to be used with
     * the given database connection.  Database connections are initialised with
     * catalog and a schema.  If the table is for the default schema for this
     * database connection then the simple table name does not need to be
     * qualified and {@code simpleTableName} is returned.  If the table is in a
     * different schema but the same catalog then the table name is qualified
     * with the schema and if the table's schema is in a different catalog from
     * the database connection's default then the table name is qualified with
     * the catalog and the schema names.
     * <p/>
     * The parts of a qualified table name are separated by periods (full-stops).
     * For example, the following selects from the city table of the sakila
     * schema {@code select * from sakila.city}.
     * @param simpleTableName unqualified table name
     * @param connection valid database connection that SQL will be run under using the given table.
     * @param provider gives the catalog and the schema names that the given table name is referenced under.
     * @param quoter puts SQL identifiers is quotes when needed.
     * @param dmd DatabaseMetaData to determine catalog separator
     * @return table name that is sufficiently qualified to execute against the given catalog and schema.
     * @throws SQLException failed to identify the default catalog for this database connection
     */
    private String getQualifiedTableName(String simpleTableName, DatabaseConnection connection, SchemaNameProvider provider, SQLIdentifiers.Quoter quoter, DatabaseMetaData dmd) throws SQLException {
        final String schemaName = provider.getSchemaName();
        final String catName = provider.getCatalogName();

        StringBuilder fullTableName = new StringBuilder();
        boolean tableIsQualified = false;

        tableIsQualified = appendQualifiedName(fullTableName, quoter, catName, connection.getJDBCConnection().getCatalog(), tableIsQualified, dmd.getCatalogSeparator());
        // add schema always if possible
        appendQualifiedName(fullTableName, quoter, schemaName, null, tableIsQualified, null);
        fullTableName.append(quoter.quoteIfNeeded(simpleTableName));

        return fullTableName.toString();
    }

    protected String getDefaultQuery(Node[] activatedNodes) {

        DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

        SQLIdentifiers.Quoter quoter;

        try {
            DatabaseMetaData dmd = connection.getJDBCConnection().getMetaData();
            quoter = SQLIdentifiers.createQuoter(dmd);

            SchemaNameProvider provider = activatedNodes[0].getLookup().lookup(SchemaNameProvider.class);

            boolean isColumn = activatedNodes[0].getLookup().lookup(ColumnNode.class) != null;
            
            // This is hardcoded to avoid introducing a cyclic dependency into
            // the module graph (dataview -> db -> dataview)
            //
            // The path, preference name and default value have to correspond to
            // DataViewPageContext
            int limit = NbPreferences.root().node("/org/netbeans/modules/db/dataview").getInt("storedPageSize", 100);
            if(limit <= 0) {
                limit = 100;
            }
            String tableName;
            String columnList;
            
            if (!isColumn) {
                tableName = getQualifiedTableName(activatedNodes[0].getName(), connection, provider, quoter, dmd);
                columnList = "*";
            } else {
                String parentName = activatedNodes[0].getLookup().lookup(ColumnNameProvider.class).getParentName();
                tableName = getQualifiedTableName(parentName, connection, provider, quoter, dmd);

                StringBuilder cols = new StringBuilder();
                for (Node node : activatedNodes) {
                    if (cols.length() > 0) {
                        cols.append(", ");
                    }

                    cols.append(quoter.quoteIfNeeded(node.getName()));
                }

                columnList = cols.toString();
            }
            
            String dbname = dmd.getDatabaseProductName();
            if(dbname == null) {
                dbname = "";
            }

            if( dbname.startsWith("DB2/") 
                    || dbname.equals("Apache Derby")
                    ) {
                // SQL2008 standard
                return "SELECT " + columnList + " FROM " + tableName + " FETCH FIRST " + limit + " ROWS ONLY";
            } else if (dbname.contains("MySQL") 
                    || dbname.contains("MariaDB") 
                    || dbname.equals("H2") 
                    || dbname.startsWith("HSQL ") 
                    || dbname.contains("Sybase") 
                    || dbname.equals("Adaptive Server Anywhere")
                    || dbname.equals("PostgreSQL")
                    || dbname.equals("HypersonicSQL")
                    ) {
                // MySQL Style LIMIT
                return "SELECT " + columnList + " FROM " + tableName + " LIMIT " + limit;
            } else if (dbname.equals("Microsoft SQL Server") 
                    || dbname.equals("ACCESS")
                    ) {
                // MSSQL style TOP
                return "SELECT TOP " + limit + " " + columnList + " FROM " + tableName;
            } else if (dbname.equals("INFORMIX-OnLine") 
                    || dbname.equals("Informix Dynamic Server")
                    || dbname.contains("Informix")
                    || dbname.contains("Firebird")
                    ) {
                // Informix style FIRST
                return "SELECT FIRST " + limit + " " + columnList + " FROM " + tableName;
            } else if (dbname.contains("Oracle")) {
                // Oracle ROWNUM
                return "SELECT " + columnList + " FROM " + tableName + " WHERE ROWNUM <= " + limit;
            } else {
                LOG.log(Level.INFO, "Failed to generate limited SELECT for: DB Connection ''{0}'', DatabaseProductName ''{1}'', Table ''{2}''", // NOI18N
                        new Object[] {connection.getDisplayName(), dbname, tableName});
                return "SELECT " + columnList + " FROM " + tableName;
            }
            
            
        } catch (SQLException ex) {
            String message = NbBundle.getMessage(QueryAction.class, "ShowDataError", ex.getMessage()); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return "";
        }
    }
}

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

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.*;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class JDBCTable extends TableImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCTable.class.getName());

    private final JDBCSchema jdbcSchema;
    private final String name;
    private final boolean system;

    private Map<String, Column> columns;
    private Map<String, Index> indexes;
    private Map<String, ForeignKey> foreignKeys;
    
    private PrimaryKey primaryKey;

    // Need a marker because there may be *no* primary key, and we don't want
    // to hit the database over and over again when there is no primary key
    private boolean primaryKeyInitialized = false;
    private static final String SQL_EXCEPTION_NOT_YET_IMPLEMENTED = "not yet implemented";

    public JDBCTable(JDBCSchema jdbcSchema, String name, boolean system) {
        this.jdbcSchema = jdbcSchema;
        this.name = name;
        this.system = system;
    }

    @Override
    public final Schema getParent() {
        return jdbcSchema.getSchema();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Collection<Column> getColumns() {
        return initColumns().values();
    }

    @Override
    public final Column getColumn(String name) {
        return MetadataUtilities.find(name, initColumns());
    }

    @Override
    public PrimaryKey getPrimaryKey() {
        return initPrimaryKey();
    }

    @Override
    public Index getIndex(String indexName) {
        return MetadataUtilities.find(indexName, initIndexes());
    }

    @Override
    public Collection<Index> getIndexes() {
        return initIndexes().values();
    }

    @Override
    public Collection<ForeignKey> getForeignKeys() {
        return initForeignKeys().values();
    }

    @Override
    public ForeignKey getForeignKeyByInternalName(String name) {
         return MetadataUtilities.find(name, initForeignKeys());
    }

    @Override
    public final void refresh() {
        columns = null;
        primaryKey = null;
        primaryKeyInitialized = false;
    }

    @Override
    public boolean isSystem() {
        return system;
    }

    @Override
    public String toString() {
        return "JDBCTable[name='" + name + "']"; // NOI18N
    }

    protected JDBCColumn createJDBCColumn(ResultSet rs) throws SQLException {
        int position = 0;
        JDBCValue jdbcValue;
        if (isOdbc(rs)) {
            jdbcValue = JDBCValue.createTableColumnValueODBC(rs, this.getTable());
        } else {
            position = rs.getInt("ORDINAL_POSITION");
            jdbcValue = JDBCValue.createTableColumnValue(rs, this.getTable());
        }
        return new JDBCColumn(this.getTable(), position, jdbcValue);
    }

    /** Returns true if this table is under ODBC connection. In such a case
     * some meta data like ORDINAL_POSITION or ASC_OR_DESC are not supported. */
    private boolean isOdbc(ResultSet rs) throws SQLException {
        boolean odbc = jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd().getURL().startsWith("jdbc:odbc");  //NOI18N
        if (odbc) {
            try {
                rs.getInt("PRECISION");
                return true;
            } catch (SQLException e) {
                // ignore and return false at the end; Probably MS Access driver which supports standards
            }
        }
        return false;
    }

    protected JDBCPrimaryKey createJDBCPrimaryKey(String pkName, Collection<Column> pkcols) {
        return new JDBCPrimaryKey(this.getTable(), pkName, pkcols);
    }

    protected void createColumns() {
        Map<String, Column> newColumns = new LinkedHashMap<String, Column>();
        try {
            ResultSet rs = MetadataUtilities.getColumns(
                    jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd(),
                    jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(),
                    name, "%"); // NOI18N
            if (rs != null) {
                try {
                    while (rs.next()) {
                        Column column = createJDBCColumn(rs).getColumn();
                        newColumns.put(column.getName(), column);
                        LOGGER.log(Level.FINE, "Created column {0}", column); //NOI18N
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }
        columns = Collections.unmodifiableMap(newColumns);
    }

    protected void createIndexes() {
        Map<String, Index> newIndexes = new LinkedHashMap<String, Index>();
        try {
            ResultSet rs = MetadataUtilities.getIndexInfo(
                    jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd(),
                    jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(),
                    name, false, true);
            if (rs != null) {
                try {
                    JDBCIndex index = null;
                    String currentIndexName = null;
                    while (rs.next()) {
                        // Ignore Indices marked statistic
                        // explicit: TYPE == DatabaseMetaData or
                        // implicit: ORDINAL_POSITION == 0
                        // @see java.sql.DatabaseMetaData#getIndexInfo
                        if (rs.getShort("TYPE") //NOI18N
                                == DatabaseMetaData.tableIndexStatistic
                                || rs.getInt("ORDINAL_POSITION") == 0) { //NOI18N
                            continue;
                        }

                        String indexName = MetadataUtilities.trimmed(rs.getString("INDEX_NAME")); //NOI18N
                        if (index == null || !(currentIndexName.equals(indexName))) {
                            index = createJDBCIndex(indexName, rs);
                            LOGGER.log(Level.FINE, "Created index {0}", index); //NOI18N

                            newIndexes.put(index.getName(), index.getIndex());
                            currentIndexName = indexName;
                        }

                        JDBCIndexColumn idx = createJDBCIndexColumn(index, rs);
                        if (idx == null) {
                            LOGGER.log(Level.INFO, "Cannot create index column for {0} from {1}",  //NOI18N
                                    new Object[]{indexName, rs});
                        } else {
                            IndexColumn col = idx.getIndexColumn();
                            index.addColumn(col);
                            LOGGER.log(Level.FINE, "Added column {0} to index {1}",   //NOI18N
                                    new Object[]{col.getName(), indexName});
                        }
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        indexes = Collections.unmodifiableMap(newIndexes);
    }

    protected JDBCIndex createJDBCIndex(String name, ResultSet rs) {
        IndexType type = IndexType.OTHER;
        boolean isUnique = false;
        try {
            type = JDBCUtils.getIndexType(rs.getShort("TYPE"));
            isUnique = !rs.getBoolean("NON_UNIQUE");
        } catch (SQLException e) {
            filterSQLException(e);
        }
        return new JDBCIndex(this.getTable(), name, type, isUnique);
    }

    protected JDBCIndexColumn createJDBCIndexColumn(JDBCIndex parent, ResultSet rs) {
        Column column = null;
        int position = 0;
        Ordering ordering = Ordering.NOT_SUPPORTED;
        try {
            column = getColumn(rs.getString("COLUMN_NAME"));
            if (!isOdbc(rs)) {
                position = rs.getInt("ORDINAL_POSITION");
                ordering = JDBCUtils.getOrdering(MetadataUtilities.trimmed(rs.getString("ASC_OR_DESC")));
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }
        if (column == null) {
            LOGGER.log(Level.INFO, "Cannot get column for index {0} from {1}",  //NOI18N
                    new Object[] {parent, rs});
            return null;
        }
        return new JDBCIndexColumn(parent.getIndex(), column.getName(), column, position, ordering);
    }

        protected void createForeignKeys() {
        Map<String,ForeignKey> newKeys = new LinkedHashMap<String,ForeignKey>();
        try {
            ResultSet rs = MetadataUtilities.getImportedKeys(
                    jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd(),
                    jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(),
                    name);
            if (rs != null) {
                try {
                    JDBCForeignKey fkey = null;
                    String currentKeyName = null;
                    while (rs.next()) {
                        String keyName = MetadataUtilities.trimmed(rs.getString("FK_NAME"));
                        // We have to assume that if the foreign key name is null, then this is a *new*
                        // foreign key, even if the last foreign key name was also null.
                    if (fkey == null || keyName == null || !(currentKeyName.equals(keyName))) {
                            fkey = createJDBCForeignKey(keyName, rs);
                            LOGGER.log(Level.FINE, "Created foreign key {0}", keyName);  //NOI18N

                            newKeys.put(fkey.getInternalName(), fkey.getForeignKey());
                            currentKeyName = keyName;
                        }

                        ForeignKeyColumn col = createJDBCForeignKeyColumn(fkey, rs).getForeignKeyColumn();
                        fkey.addColumn(col);
                        LOGGER.log(Level.FINE, "Added foreign key column {0} to foreign key {1}",  //NOI18N
                                new Object[]{col.getName(), keyName});
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        foreignKeys = Collections.unmodifiableMap(newKeys);
    }

    protected JDBCForeignKey createJDBCForeignKey(String name, ResultSet rs) {
        return new JDBCForeignKey(this.getTable(), name);
    }

    protected JDBCForeignKeyColumn createJDBCForeignKeyColumn(JDBCForeignKey parent, ResultSet rs) {
        Table table;
        String colname;
        Column referredColumn = null;
        Column referringColumn = null;
        int position = 0;

        try {
            table = findReferredTable(rs);
            colname = MetadataUtilities.trimmed(rs.getString("PKCOLUMN_NAME")); // NOI18N
            referredColumn = table.getColumn(colname);
            if (referredColumn == null) {
                throwColumnNotFoundException(table, colname);
            }

            colname = MetadataUtilities.trimmed(rs.getString("FKCOLUMN_NAME"));
            referringColumn = getColumn(colname);

            if (referringColumn == null) {
                throwColumnNotFoundException(this.getTable(), colname);
            }
            position = rs.getInt("KEY_SEQ");
        } catch (SQLException e) {
            filterSQLException(e);
        }
        return new JDBCForeignKeyColumn(parent.getForeignKey(), referringColumn.getName(), referringColumn, referredColumn, position);
    }
    
    private void throwColumnNotFoundException(Table table, String colname)
            throws MetadataException {
        String message = getMessage("ERR_COL_NOT_FOUND", //NOI18N
                table.getParent().getParent().getName(),
                table.getParent().getName(), table.getName(), colname);
        MetadataException e = new MetadataException(message);
        LOGGER.log(Level.INFO, message, e);
        throw e;
    }

    private String getMessage(String key, String ... args) {
        return NbBundle.getMessage(JDBCTable.class, key, args);
    }

    private Table findReferredTable(ResultSet rs) {
        JDBCMetadata metadata = jdbcSchema.getJDBCCatalog().getJDBCMetadata();
        Catalog catalog;
        Schema schema;
        Table table = null;

        try {
            String catalogName = MetadataUtilities.trimmed(rs.getString("PKTABLE_CAT")); // NOI18N
            if (catalogName == null || catalogName.length() == 0) {
                catalog = jdbcSchema.getParent();
            } else {
                catalog = metadata.getCatalog(catalogName);
                if (catalog == null) {
                    throw new MetadataException(getMessage("ERR_CATALOG_NOT_FOUND", catalogName)); // NOI18N
                }
            }

            String schemaName = MetadataUtilities.trimmed(rs.getString("PKTABLE_SCHEM")); // NOI18N

            if (schemaName == null || schemaName.length() == 0) {
                schema = catalog.getSyntheticSchema();
            } else {
                schema = catalog.getSchema(schemaName);
                if (schema == null) {
                    throw new MetadataException(getMessage("ERR_SCHEMA_NOT_FOUND", schemaName, catalog.getName()));
                }
            }

            String tableName = MetadataUtilities.trimmed(rs.getString("PKTABLE_NAME"));
            table = schema.getTable(tableName);

            if (table == null) {
                throw new MetadataException(getMessage("ERR_TABLE_NOT_FOUND", catalogName, schemaName, tableName));
            }

        } catch (SQLException e) {
            filterSQLException(e);
        }

        return table;
    }

    protected void createPrimaryKey() {
        String pkname = null;
        Collection<Column> pkcols = new ArrayList<Column>();
        try {
            ResultSet rs = MetadataUtilities.getPrimaryKeys(
                    jdbcSchema.getJDBCCatalog().getJDBCMetadata().getDmd(),
                    jdbcSchema.getJDBCCatalog().getName(), jdbcSchema.getName(),
                    name);
            if (rs != null) {
                try {
                    while (rs.next()) {
                        if (pkname == null) {
                            pkname = MetadataUtilities.trimmed(rs.getString("PK_NAME"));
                        }
                        String colName = MetadataUtilities.trimmed(rs.getString("COLUMN_NAME"));
                        pkcols.add(getColumn(colName));
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException e) {
            filterSQLException(e);
        }

        primaryKey = createJDBCPrimaryKey(pkname, Collections.unmodifiableCollection(pkcols)).getPrimaryKey();
    }

    private Map<String, Column> initColumns() {
        if (columns != null) {
            return columns;
        }
        LOGGER.log(Level.FINE, "Initializing columns in {0}", this);
        createColumns();
        return columns;
    }

    private Map<String, Index> initIndexes() {
        if (indexes != null) {
            return indexes;
        }
        LOGGER.log(Level.FINE, "Initializing indexes in {0}", this);

        createIndexes();
        return indexes;
    }

    private Map<String,ForeignKey> initForeignKeys() {
        if (foreignKeys != null) {
            return foreignKeys;
        }
        LOGGER.log(Level.FINE, "Initializing foreign keys in {0}", this);

        createForeignKeys();
        return foreignKeys;
    }

    private PrimaryKey initPrimaryKey() {
        if (primaryKeyInitialized) {
            return primaryKey;
        }
        LOGGER.log(Level.FINE, "Initializing columns in {0}", this);
        // These need to be initialized first.
        getColumns();
        createPrimaryKey();
        primaryKeyInitialized = true;
        return primaryKey;
    }

    private void filterSQLException(SQLException x) throws MetadataException {
        if (SQL_EXCEPTION_NOT_YET_IMPLEMENTED.equalsIgnoreCase(x.getMessage())) {
            Logger.getLogger(JDBCTable.class.getName()).log(Level.FINE, x.getLocalizedMessage(), x);
        } else {
            throw new MetadataException(x);
        }
    }
}

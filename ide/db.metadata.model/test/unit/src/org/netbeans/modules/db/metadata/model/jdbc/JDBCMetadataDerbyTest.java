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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.View;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadataDerbyTest extends JDBCMetadataTestBase {

    private Connection conn;
    private JDBCMetadata metadata;
    private Statement stmt;

    public JDBCMetadataDerbyTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:" + getWorkDirPath() + "/test;create=true");
        stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE FOO (" +
                "ID INT NOT NULL, " +
                "FOO_NAME VARCHAR(16), " +
                "CONSTRAINT FOO_PK PRIMARY KEY (ID, FOO_NAME))");
        stmt.executeUpdate("CREATE TABLE BAR (" +
                "\"i+d\" INT NOT NULL PRIMARY KEY, " +
                "FOO_ID INT NOT NULL, " +
                "FOO_NAME VARCHAR(16) NOT NULL, " +
                "BAR_NAME VARCHAR(16) NOT NULL, " +
                "DEC_COL DECIMAL(12,2), " +
                "FOREIGN KEY (FOO_ID, FOO_NAME) REFERENCES FOO(ID, FOO_NAME))");
        stmt.executeUpdate("CREATE VIEW BARVIEW AS SELECT * FROM BAR");

        stmt.executeUpdate("CREATE INDEX BAR_INDEX ON BAR(FOO_ID ASC, FOO_NAME DESC)");
        stmt.executeUpdate("CREATE UNIQUE INDEX DEC_COL_INDEX ON BAR(DEC_COL)");
        metadata = new JDBCMetadata(conn, "APP");
    }

    public void testForeignKey() throws Exception {
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        Schema appSchema = defaultCatalog.getSchema("APP");

        Table table = appSchema.getTable("BAR");
        ForeignKey[] keys = table.getForeignKeys().toArray(new ForeignKey[0]);
        assertEquals(1, keys.length);
        ForeignKey key = keys[0];
        Table referredTable = appSchema.getTable("FOO");
        checkForeignKeyColumn(key, referredTable, "FOO_ID", "ID", 1);
        checkForeignKeyColumn(key, referredTable, "FOO_NAME", "FOO_NAME", 2);
    }

    public void testBasic() throws Exception {
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals(1, metadata.getCatalogs().size());
        assertTrue(metadata.getCatalogs().contains(defaultCatalog));
        assertNull(defaultCatalog.getName());
        assertNotNull(defaultCatalog.getSchema("NULLID"));
        assertNotNull(defaultCatalog.getSchema("SYSCAT"));

        Schema appSchema = defaultCatalog.getSchema("APP");
        assertSame(appSchema, metadata.getDefaultSchema());
        assertEquals("APP", appSchema.getName());
        assertFalse(appSchema.isSynthetic());
        assertTrue(appSchema.isDefault());

        Collection<Table> tables = appSchema.getTables();
        assertEquals(2, tables.size());
        Table fooTable = appSchema.getTable("FOO");
        Table barTable = appSchema.getTable("BAR");
        assertTrue(tables.contains(fooTable));
        assertTrue(tables.contains(barTable));
        assertEquals("FOO", fooTable.getName());
        assertEquals("BAR", barTable.getName());

        Collection<Column> columns = barTable.getColumns();

        checkColumns(barTable, columns);
        checkPrimaryKey(fooTable);
    }
    public void testIndexes() {
        Schema schema = metadata.getDefaultSchema();
        Table table = schema.getTable("BAR");
        Collection<Index> indexes = table.getIndexes();
        assertEquals(4, indexes.size());

        Index index = table.getIndex("BAR_INDEX");
        assertNotNull(index);
        assertEquals(index.getParent(), table);
        assertFalse(index.isUnique());
        assertEquals(IndexType.OTHER, index.getIndexType());
        assertEquals("JDBCIndex[name='BAR_INDEX', type=OTHER, unique=false]", index.toString());
        Collection<IndexColumn> columns = index.getColumns();
        assertNames(new HashSet<String>(Arrays.asList("FOO_ID", "FOO_NAME")), columns);

        IndexColumn col = index.getColumn("FOO_ID");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(1, col.getPosition());

        col = index.getColumn("FOO_NAME");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.DESCENDING, col.getOrdering());
        assertEquals(2, col.getPosition());
        assertEquals("JDBCIndexColumn[name='FOO_NAME', ordering=DESCENDING, position=2, " +
                "column=JDBCColumn[name=FOO_NAME, type=VARCHAR, length=16, precision=0, radix=0, scale=0, " +
                "nullable=NOT_NULLABLE, ordinal_position=3]]", col.toString());

        index = table.getIndex("DEC_COL_INDEX");
        assertNotNull(index);
        assertEquals(index.getParent(), table);
        assertTrue(index.isUnique());
        assertEquals(IndexType.OTHER, index.getIndexType());
        columns = index.getColumns();
        assertEquals(1, columns.size());
        assertNames(new HashSet<String>(Arrays.asList("DEC_COL")), columns);
        col = index.getColumn("DEC_COL");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(1, col.getPosition());
    }

    public void testRefreshCatalog() throws Exception {
        Catalog catalog = metadata.getDefaultCatalog();
        Collection<Schema> schemas = catalog.getSchemas();

        int numSchemas = schemas.size();

        stmt.executeUpdate("CREATE SCHEMA testRefreshCatalog");

        catalog.refresh();
        schemas = catalog.getSchemas();
        assertEquals(numSchemas + 1, schemas.size());
        Schema schema = catalog.getSchema("testRefreshCatalog");
        assertNotNull(schema);
        assertTrue(schemas.contains(schema));

        stmt.executeUpdate("DROP SCHEMA testRefreshCatalog RESTRICT");

        catalog.refresh();
        schemas = catalog.getSchemas();
        assertEquals(numSchemas, schemas.size());
        schema = catalog.getSchema("testRefreshCatalog");
        assertNull(schema);
    }

    public void testViews() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<View> views = schema.getViews();
        assertNames(new HashSet<String>(Arrays.asList("BARVIEW")), views);
        View barView = schema.getView("BARVIEW");
        assertTrue(views.contains(barView));
        assertSame(schema, barView.getParent());

        Collection<Column> columns = barView.getColumns();
        checkColumns(barView, columns);
    }

    public void testRefresh() throws Exception {
        assertNull(metadata.getDefaultCatalog().getSchema("FOOBAR"));
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE SCHEMA FOOBAR");
        stmt.close();
        metadata.refresh();
        assertNotNull(metadata.getDefaultCatalog().getSchema("FOOBAR"));
    }

    public void testRefreshTable() throws Exception {
        Table fooTable = metadata.getDefaultSchema().getTable("FOO");
        assertNames(Arrays.asList("ID", "FOO_NAME"), fooTable.getColumns());
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("ALTER TABLE FOO ADD NEW_COLUMN VARCHAR(16)");
        stmt.close();
        fooTable.refresh();
        assertNames(Arrays.asList("ID", "FOO_NAME", "NEW_COLUMN"), fooTable.getColumns());
    }

    private void checkColumns(Tuple parent, Collection<Column> columns) {
        assertEquals(5, columns.size());
        Column[] colarray = columns.toArray(new Column[4]);

        Column col = colarray[0];
        assertEquals("JDBCColumn[name=i+d, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=1]", col.toString());
        assertEquals("i+d", col.getName());
        assertSame(parent, col.getParent());
        assertEquals(SQLType.INTEGER, col.getType());
        assertEquals(0, col.getLength());
        assertEquals(10, col.getRadix());
        assertEquals(10, col.getPrecision());
        assertEquals(0, col.getScale());
        assertEquals(1, col.getPosition());


        col = colarray[1];
        assertEquals("JDBCColumn[name=FOO_ID, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=2]", col.toString());

        col = colarray[2];
        assertEquals("JDBCColumn[name=FOO_NAME, type=VARCHAR, length=16, precision=0, radix=0, scale=0, nullable=NOT_NULLABLE, ordinal_position=3]", col.toString());

        col = colarray[3];
        assertEquals("JDBCColumn[name=BAR_NAME, type=VARCHAR, length=16, precision=0, radix=0, scale=0, nullable=NOT_NULLABLE, ordinal_position=4]", col.toString());

        col = colarray[4];
        assertEquals("JDBCColumn[name=DEC_COL, type=DECIMAL, length=0, precision=12, radix=10, scale=2, nullable=NULLABLE, ordinal_position=5]", col.toString());
    }

    private void checkPrimaryKey(Table fooTable) {
        PrimaryKey key = fooTable.getPrimaryKey();
        assertEquals("FOO_PK", key.getName());
        Column[] pkcols = key.getColumns().toArray(new Column[0]);
        Column col1 = fooTable.getColumn("ID");
        Column col2 = fooTable.getColumn("FOO_NAME");
        assertEquals(pkcols.length, 2);
        assertEquals(col1, pkcols[1]);
        assertEquals(col2, pkcols[0]);
    }
}

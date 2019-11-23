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
package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.ForeignKey;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Index.IndexType;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.View;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.jdbc.mysql.MySQLMetadata;

/**
 * To override the defaults for the tests add properties to
 * nbbuild/user.build.properties
 *
 * <pre><code>
 * test-unit-sys-prop.mysql.host=...
 * test-unit-sys-prop.mysql.port=...
 * test-unit-sys-prop.mysql.database=...
 * test-unit-sys-prop.mysql.user=...
 * test-unit-sys-prop.mysql.password=...
 * </code></pre>
 */
public class JDBCMetadataMySQLTest extends JDBCMetadataTestBase {

    private static final Logger LOG = Logger.getLogger(JDBCMetadataMySQLTest.class.getName());
    
    private JDBCMetadata metadata;
    private String defaultCatalogName;
    private Connection conn;
    private Statement stmt;
    private String mysqlHost;
    private Integer mysqlPort;
    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlDatabase;

    public JDBCMetadataMySQLTest(String name) {
        super(name);
    }

    @Override
    public boolean canRun() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return super.canRun();
        } catch (ClassNotFoundException e) {
            LOG.warning(String.format("Test %s in %s disabled, %s not available", this.getName(), this.getClass().getName(), e.getMessage()));
            return false;
        }
    }

    @Override
    public void setUp() throws Exception {
        mysqlHost = System.getProperty("mysql.host", "localhost");
        mysqlPort = Integer.getInteger("mysql.port", 3306);
        mysqlDatabase = System.getProperty("mysql.database", "test");
        mysqlUser = System.getProperty("mysql.user", "test");
        mysqlPassword = System.getProperty("mysql.password", "test");
        clearWorkDir();
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort, mysqlUser, mysqlPassword);
        stmt = conn.createStatement();
        stmt.executeUpdate("DROP DATABASE IF EXISTS test");
        stmt.executeUpdate("DROP DATABASE IF EXISTS test2");
        stmt.executeUpdate("CREATE DATABASE test");
        stmt.executeUpdate("CREATE DATABASE test2");

        conn = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase, mysqlUser, mysqlPassword);
        stmt.executeUpdate("USE test");

        stmt.executeUpdate("CREATE TABLE groucho (id INT NOT NULL, id2 INT NOT NULL, "
                + "CONSTRAINT groucho_pk PRIMARY KEY (id2, id)) Engine=InnoDB");

        // Create a table in another database with references, let's see if this works
        stmt.executeUpdate("CREATE TABLE test2.harpo (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT) ENGINE=InnoDB");

        stmt.executeUpdate("CREATE TABLE foo ("
                + "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, "
                + "FOO_NAME VARCHAR(16), harpo_id INT NOT NULL, FOREIGN KEY(harpo_id) REFERENCES test2.harpo(id)) ENGINE=InnoDB");

        stmt.executeUpdate("CREATE TABLE bar ("
                + "`i+d` INT NOT NULL PRIMARY KEY, "
                + "foo_id INT NOT NULL, "
                + "bar_name  VARCHAR(16), "
                + "bar_digit DECIMAL(12,2) NOT NULL, "
                + "FOREIGN KEY (foo_id) REFERENCES foo(id)) Engine=InnoDB");

        stmt.executeUpdate("CREATE TABLE chico (id INT NOT NULL, groucho_id2 INT, groucho_id INT, foo_id INT, "
                + "CONSTRAINT groucho_fk FOREIGN KEY (groucho_id2, groucho_id) REFERENCES groucho(id2, id), "
                + "CONSTRAINT foo_fk FOREIGN KEY (foo_id) REFERENCES foo(id)) Engine=InnoDB");

        stmt.executeUpdate("CREATE VIEW barview AS SELECT * FROM bar");

        stmt.executeUpdate("CREATE UNIQUE INDEX groucho_index ON groucho(id2)");

        stmt.executeUpdate("CREATE INDEX bar_name_index ON bar(bar_name, bar_digit)");
        stmt.executeUpdate("CREATE UNIQUE INDEX bar_foo_index ON bar(foo_id)");

        stmt.executeUpdate("CREATE PROCEDURE barproc(IN param1 INT, OUT result VARCHAR(255), INOUT param2 DECIMAL(5,2)) "
                + "BEGIN SELECT * from bar; END");
        stmt.executeUpdate("CREATE PROCEDURE fooproc(IN param1 INT) "
                + "BEGIN SELECT * from foo; END");
        metadata = new MySQLMetadata(conn, null);
        defaultCatalogName = mysqlDatabase;
    }

    public void testBasic() throws Exception {
        Collection<Catalog> catalogs = metadata.getCatalogs();
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals(defaultCatalogName, defaultCatalog.getName());
        assertTrue(catalogs.contains(defaultCatalog));

        Catalog informationSchema = metadata.getCatalog("information_schema");
        assertFalse(informationSchema.isDefault());
        Schema syntheticSchema = informationSchema.getSyntheticSchema();
        assertNotNull(syntheticSchema);
        assertFalse("Only the default catalog should have a default schema", syntheticSchema.isDefault());

        Schema schema = metadata.getDefaultSchema();
        assertTrue(schema.isSynthetic());
        assertTrue(schema.isDefault());
        assertSame(schema, defaultCatalog.getSyntheticSchema());
        assertSame(defaultCatalog, schema.getParent());

        Collection<Table> tables = schema.getTables();
        assertNames(new HashSet<String>(Arrays.asList("foo", "bar", "groucho", "chico")), tables);
        Table barTable = schema.getTable("bar");
        assertTrue(tables.contains(barTable));
        assertSame(schema, barTable.getParent());

        checkColumns(barTable);
    }

    /**
     * In MySQL it's possible to connect without specifying a database name, in
     * which case there should be no default catalog and we should be able to
     * get the full list of catalogs
     *
     * @throws java.lang.Exception
     */
    public void testNoDatabaseSpecified() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort, mysqlUser, mysqlPassword);
        JDBCMetadata md = new MySQLMetadata(connection, null);

        Collection<Catalog> catalogs = md.getCatalogs();
        assertNull(md.getDefaultCatalog().getName());
        assertFalse(catalogs.contains(md.getDefaultCatalog()));
    }

    public void testSchemaRefresh() throws Exception {
        Schema schema = metadata.getDefaultSchema();
        Collection<Table> tables = schema.getTables();
        assertNames(new HashSet<String>(Arrays.asList("foo", "bar", "groucho", "chico")), tables);

        stmt.executeUpdate("CREATE TABLE testSchemaRefresh ("
                + "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, "
                + "FOO_NAME VARCHAR(16))");

        schema.refresh();

        tables = schema.getTables();
        assertNames(new HashSet<String>(Arrays.asList("foo", "bar", "groucho", "chico", "testSchemaRefresh")), tables);

    }

    public void testIndexes() {
        Schema schema = metadata.getDefaultSchema();
        Table table = schema.getTable("groucho");
        Collection<Index> indexes = table.getIndexes();
        assertNames(new HashSet<String>(Arrays.asList("groucho_index", "PRIMARY")), indexes);

        Index index = table.getIndex("groucho_index");
        assertEquals(index.getParent(), table);
        assertTrue(index.isUnique());
        assertEquals(IndexType.OTHER, index.getIndexType());
        Collection<IndexColumn> columns = index.getColumns();
        assertEquals(1, columns.size());
        assertNames(new HashSet<String>(Arrays.asList("id2")), columns);
        IndexColumn col = index.getColumn("id2");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(1, col.getPosition());

        index = table.getIndex("PRIMARY");
        assertEquals(index.getParent(), table);
        assertTrue(index.isUnique());
        assertEquals(IndexType.OTHER, index.getIndexType());
        columns = index.getColumns();
        assertEquals(2, columns.size());
        assertNames(new HashSet<String>(Arrays.asList("id2", "id")), columns);
        col = index.getColumn("id2");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(1, col.getPosition());

        col = index.getColumn("id");
        assertNotNull(col);
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(2, col.getPosition());

        table = schema.getTable("bar");
        indexes = table.getIndexes();
        assertNames(new HashSet<String>(Arrays.asList("bar_name_index", "bar_foo_index", "PRIMARY")), indexes);

        index = table.getIndex("bar_name_index");
        assertEquals(index.getParent(), table);
        assertFalse(index.isUnique());
        columns = index.getColumns();
        assertNames(new HashSet<String>(Arrays.asList("bar_name", "bar_digit")), columns);
        col = index.getColumn("bar_name");
        assertEquals(index, col.getParent());
        assertEquals(Ordering.ASCENDING, col.getOrdering());
        assertEquals(1, col.getPosition());
        assertEquals(col.getColumn(), table.getColumn("bar_name"));
    }

    public void testForeignKey() throws Exception {
        Schema schema = metadata.getDefaultSchema();
        Table table = schema.getTable("chico");

        Collection<ForeignKey> fkeys = table.getForeignKeys();
        assertNames(new HashSet<String>(Arrays.asList("groucho_fk", "foo_fk")), fkeys);

        for (ForeignKey key : fkeys) {
            Collection<ForeignKeyColumn> cols = key.getColumns();

            if ("groucho_fk".equals(key.getName())) {
                assertNames(new HashSet<String>(Arrays.asList("groucho_id2", "groucho_id")), cols);
                Table referredTable = schema.getTable("groucho");
                checkForeignKeyColumn(key, referredTable, "groucho_id2", "id2", 1);
                checkForeignKeyColumn(key, referredTable, "groucho_id", "id", 2);
            } else {
                assertNames(new HashSet<String>(Arrays.asList("foo_id")), cols);
                checkForeignKeyColumn(key, schema.getTable("foo"), "foo_id", "id", 1);
            }
        }

        table = schema.getTable("bar");
        fkeys = table.getForeignKeys();
        ForeignKey[] keys = table.getForeignKeys().toArray(new ForeignKey[0]);
        assertEquals(1, keys.length);

        ForeignKey key = keys[0];
        // Don't know the name of this one, it's generated...
        assertNotNull(key.getName());
        checkForeignKeyColumn(key, schema.getTable("foo"), "foo_id", "id", 1);
    }

    public void testForeignKeyAcrossCatalogs() throws Exception {
        Schema schema = metadata.getDefaultSchema();
        Table table = schema.getTable("foo");

        Collection<ForeignKey> fkeys = table.getForeignKeys();
        assertEquals(1, fkeys.size());
        ForeignKey key = fkeys.toArray(new ForeignKey[1])[0];
        Table referredTable = metadata.getCatalog("test2").getSyntheticSchema().getTable("harpo");
        checkForeignKeyColumn(key, referredTable, "harpo_id", "id", 1);
    }

    public void testPrimaryKey() {
        Schema schema = metadata.getDefaultSchema();
        Table grouchoTable = schema.getTable("groucho");
        PrimaryKey key = grouchoTable.getPrimaryKey();

        // In MySQL, it doesn't matter what identifier you use when you create
        // the primary key, it is always named "PRIMARY"
        assertEquals("PRIMARY", key.getName());
        Column[] pkcols = key.getColumns().toArray(new Column[0]);
        Column col1 = grouchoTable.getColumn("id");
        Column col2 = grouchoTable.getColumn("id2");
        assertEquals(pkcols.length, 2);

        // In MySQL, it doesn't matter how you order the columns in your primary
        // key definition, they are always ordered in the same order as the
        // ordering in th table definition
        assertEquals(col1, pkcols[0]);
        assertEquals(col2, pkcols[1]);
    }

    public void testViews() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<View> views = schema.getViews();
        assertNames(new HashSet<String>(Arrays.asList("barview")), views);
        View barView = schema.getView("barview");
        assertTrue(views.contains(barView));
        assertSame(schema, barView.getParent());

        checkColumns(barView);
    }

    public void testProcedures() throws Exception {
        Schema schema = metadata.getDefaultSchema();

        Collection<Procedure> procs = schema.getProcedures();
        assertNames(Arrays.asList("barproc", "fooproc"), procs);

        Procedure barProc = schema.getProcedure("barproc");
        Collection<Parameter> barParams = barProc.getParameters();
        assertNames(Arrays.asList("param1", "result", "param2"), barParams);

        // MySQL does not tell you what the result columns are - bummer
        assertEquals(0, barProc.getColumns().size());

        Procedure fooProc = schema.getProcedure("fooproc");
        Collection<Parameter> fooParams = fooProc.getParameters();
        assertNames(Arrays.asList("param1"), fooParams);

        // MySQL does not tell you what the result columns are - bummer
        assertEquals(0, barProc.getColumns().size());

        Parameter param = barProc.getParameter("param1");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=param1, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NULLABLE, direction=IN, position=1]", param.toString());
        assertEquals(SQLType.INTEGER, param.getType());
        assertEquals(0, param.getLength());
        assertEquals(Direction.IN, param.getDirection());
        assertEquals(10, param.getPrecision());
        assertEquals(0, param.getScale());
        assertEquals(10, param.getRadix());

        param = barProc.getParameter("result");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=result, type=VARCHAR, length=255, precision=0, radix=10, scale=0, nullable=NULLABLE, direction=OUT, position=2]", param.toString());

        param = barProc.getParameter("param2");
        assertTrue(barParams.contains(param));
        assertSame(barProc, param.getParent());
        assertEquals("JDBCParameter[name=param2, type=DECIMAL, length=0, precision=5, radix=10, scale=2, nullable=NULLABLE, direction=INOUT, position=3]", param.toString());
    }

    private void checkColumns(Tuple parent) {
        Collection<Column> columns = parent.getColumns();
        assertEquals(4, columns.size());
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
        assertEquals("JDBCColumn[name=foo_id, type=INTEGER, length=0, precision=10, radix=10, scale=0, nullable=NOT_NULLABLE, ordinal_position=2]", col.toString());

        col = colarray[2];
        assertEquals("JDBCColumn[name=bar_name, type=VARCHAR, length=16, precision=0, radix=10, scale=0, nullable=NULLABLE, ordinal_position=3]", col.toString());

        col = colarray[3];
        assertEquals(12, col.getPrecision());
        assertEquals(2, col.getScale());
        assertEquals("JDBCColumn[name=bar_digit, type=DECIMAL, length=0, precision=12, radix=10, scale=2, nullable=NOT_NULLABLE, ordinal_position=4]", col.toString());
    }
}

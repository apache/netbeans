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

package org.netbeans.modules.db.metadata.model.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle.Kind;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCMetadata;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class MetadataElementHandleTest extends MetadataTestBase {

    private Connection conn;
    private Metadata metadata;

    public MetadataElementHandleTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        conn = DriverManager.getConnection("jdbc:derby:" + getWorkDirPath() + "/test;create=true");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE FOO (" +
                "ID INT NOT NULL PRIMARY KEY, " +
                "FOO VARCHAR(16))");
        stmt.executeUpdate("CREATE TABLE BAR (ID INT NOT NULL PRIMARY KEY, FOO_ID INT NOT NULL, FOREIGN KEY (FOO_ID) REFERENCES FOO)");
        stmt.executeUpdate("CREATE INDEX FOO_INDEX ON FOO(FOO)");
        stmt.executeUpdate("CREATE VIEW FOOVIEW AS SELECT * FROM FOO");
        stmt.executeUpdate("CREATE PROCEDURE XY (IN S_MONTH INTEGER, IN S_DAYS VARCHAR(255)) "
                + " DYNAMIC RESULT SETS 1 "
                + " PARAMETER STYLE JAVA READS SQL  DATA LANGUAGE JAVA "
                + " EXTERNAL NAME 'org.netbeans.modules.db.metadata.model.api.MetadataElementHandleTest.demoProcedure'");
        stmt.executeUpdate("CREATE FUNCTION TO_DEGREES(RADIANS DOUBLE) RETURNS DOUBLE "
                + "PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA "
                + "EXTERNAL NAME 'java.lang.Math.toDegrees'");
        stmt.close();
        metadata = new JDBCMetadata(conn, "APP").getMetadata();
    }

    public void testResolve() throws SQLException {
        Catalog catalog = metadata.getDefaultCatalog();
        MetadataElementHandle<Catalog> catalogHandle = MetadataElementHandle.create(catalog);
            Catalog resolvedCatalog = catalogHandle.resolve(metadata);
        assertSame(catalog, resolvedCatalog);

        Schema schema = metadata.getDefaultSchema();
        MetadataElementHandle<Schema> schemaHandle = MetadataElementHandle.create(schema);
        Schema resolvedSchema = schemaHandle.resolve(metadata);
        assertSame(schema, resolvedSchema);

        Table table = schema.getTable("FOO");
        MetadataElementHandle<Table> tableHandle = MetadataElementHandle.create(table);
        Table resolvedTable = tableHandle.resolve(metadata);
        assertSame(table, resolvedTable);

        PrimaryKey key = table.getPrimaryKey();
        MetadataElementHandle<PrimaryKey> keyHandle = MetadataElementHandle.create(key);
        PrimaryKey resolvedKey = keyHandle.resolve(metadata);
        assertSame(key, resolvedKey);

        Column column = table.getColumn("FOO");
        MetadataElementHandle<Column> columnHandle = MetadataElementHandle.create(column);
        Column resolvedColumn = columnHandle.resolve(metadata);
        assertSame(column, resolvedColumn);

        Index index = table.getIndex("FOO_INDEX");
        MetadataElementHandle<Index> indexHandle = MetadataElementHandle.create(index);
        Index resolvedIndex = indexHandle.resolve(metadata);
        assertSame(index, resolvedIndex);

        Table barTable = schema.getTable("BAR");
        ForeignKey fkey = barTable.getForeignKeys().toArray(new ForeignKey[0])[0];
        MetadataElementHandle<ForeignKey> fkeyHandle = MetadataElementHandle.create(fkey);
        ForeignKey resolvedForeignKey = fkeyHandle.resolve(metadata);
        assertSame(fkey, resolvedForeignKey);

        View view = schema.getView("FOOVIEW");
        MetadataElementHandle<View> viewHandle = MetadataElementHandle.create(view);
        View resolvedView = viewHandle.resolve(metadata);
        assertSame(view, resolvedView);

        Function function = schema.getFunction("TO_DEGREES");
        MetadataElementHandle<Function> procedureHandle = MetadataElementHandle.create(function);
        Function resolvedFunction = procedureHandle.resolve(metadata);
        assertSame(function, resolvedFunction);

        assertTrue(function.getParameters().size() > 0);

        for (Parameter param : function.getParameters()) {
            MetadataElementHandle<Parameter> paramHandle = MetadataElementHandle.create(param);
            Parameter resolvedParam = paramHandle.resolve(metadata);
            assertSame(param, resolvedParam);
        }

        Value value = function.getReturnValue();
        assertNotNull(value);
        MetadataElementHandle<Value> valueHandle = MetadataElementHandle.create(value);
        Value resolvedValue = valueHandle.resolve(metadata);
        assertSame(value, resolvedValue);

        Procedure procedure = schema.getProcedure("XY");
        MetadataElementHandle<Procedure> functionHandle = MetadataElementHandle.create(procedure);
        Procedure resolvedProcedure = functionHandle.resolve(metadata);
        assertSame(procedure, resolvedProcedure);

        assertTrue(procedure.getParameters().size() > 0);

        for (Parameter param : procedure.getParameters()) {
            MetadataElementHandle<Parameter> paramHandle = MetadataElementHandle.create(param);
            Parameter resolvedParam = paramHandle.resolve(metadata);
            assertSame(param, resolvedParam);
        }

        value = procedure.getReturnValue();
        assertNull(value);

        // Ensure conflicting names of functions and procudures don't spill over
        // a MetadataElementHandle<Parameter> from a procedure must not be
        // resolvable against a function with the same "path" (Function name =
        // procedure name and same parameter name)
        procedure = schema.getProcedure("XY");
        MetadataElementHandle<Parameter> paramHandle = MetadataElementHandle.create(procedure.getParameters().iterator().next());
        assertNotNull(paramHandle.resolve(metadata));

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP PROCEDURE XY");
        stmt.executeUpdate("CREATE FUNCTION XY (S_MONTH INTEGER, S_DAYS VARCHAR(255)) RETURNS DOUBLE "
                + " PARAMETER STYLE JAVA READS SQL  DATA LANGUAGE JAVA "
                + " EXTERNAL NAME 'org.netbeans.modules.db.metadata.model.api.MetadataElementHandleTest.demoProcedure'");
        stmt.close();

        metadata.refresh();

        schema = metadata.getDefaultSchema();

        assertNotNull(schema.getFunction("XY"));

        assertNull(schema.getProcedure("XY"));

        assertNull(paramHandle.resolve(metadata));


        // Negative test - what happens if you create a handle for null
        try {
            MetadataElementHandle<Catalog> bogusHandle = MetadataElementHandle.create((Catalog)null);
            fail("Should have thrown a NullPointerException");
        } catch (NullPointerException npe) {
            //expected
        }
    }

    public void testEquals() {
        String[] names = new String[] {"CATALOG", null, "TABLEORVIEW"};

        Kind[] tableKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA, Kind.TABLE};
        Kind[] viewKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA, Kind.VIEW};
        Kind[] schemaKinds = new Kind[] {Kind.CATALOG, Kind.SCHEMA};

        MetadataElementHandle<? extends MetadataElement> handle1 = MetadataElementHandle.create(Table.class, names, tableKinds);
        MetadataElementHandle<? extends MetadataElement> handle2 = MetadataElementHandle.create(Table.class, names, tableKinds);
        MetadataElementHandle<? extends MetadataElement> handle3 = MetadataElementHandle.create(Table.class, names, tableKinds);

        MetadataElementHandle<? extends MetadataElement> schemaHandle = MetadataElementHandle.create(Schema.class, names, schemaKinds);

        checkHandles(handle1, handle2, handle3, schemaHandle);

        handle1 = MetadataElementHandle.create(View.class, names, viewKinds);
        handle2 = MetadataElementHandle.create(View.class, names, viewKinds);
        handle3 = MetadataElementHandle.create(View.class, names, viewKinds);

        checkHandles(handle1, handle2, handle3, schemaHandle);
    }

    private void checkHandles(MetadataElementHandle<? extends MetadataElement> handle1,
            MetadataElementHandle<? extends MetadataElement> handle2,
            MetadataElementHandle<? extends MetadataElement> handle3,
            MetadataElementHandle<? extends MetadataElement> badHandle) {
        // Reflexivity.
        assertTrue(handle1.equals(handle1));
        assertTrue(handle1.hashCode() == handle2.hashCode());
        // Symmetry.
        assertTrue(handle1.equals(handle2));
        assertTrue(handle2.equals(handle1));
        // Transitivity.
        assertTrue(handle2.equals(handle3));
        assertTrue(handle1.equals(handle3));
        // Not of the same kind, so not equal.
        assertFalse(handle1.equals(badHandle));
    }
}

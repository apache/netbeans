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
package org.netbeans.modules.db.explorer;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.test.DatabaseMetaDataAdapter;
import org.netbeans.modules.db.test.ResultSetAdapter;

/**
 *
 * @author jhavlin
 */
public class DatabaseConnectorTest {

    public DatabaseConnectorTest() {
    }

    /**
     * Set schema that does not exist. Test for bug 75595.
     */
    @Test
    public void testSetSchemaThatDoesNotExist() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(new CustomMetaData("A", "B", "C"));
        ds.setSchema("D");
        assertNull(ds.getSchema());
    }

    /**
     * Set schema that exists. Test for bug 75595.
     */
    @Test
    public void testSetSchemaThatExists() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(new CustomMetaData("A", "B", "C"));
        ds.setSchema("B");
        assertEquals("B", ds.getSchema());
    }

    /**
     * Set schema when no database meta data are available. Test for bug 75595.
     */
    @Test
    public void testSetSchemaWithNoMetaData() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(null);
        ds.setSchema("ANY");
        assertEquals("ANY", ds.getSchema());
    }

    private static class CustomMetaData extends DatabaseMetaDataAdapter {

        String[] schemas;

        public CustomMetaData(String... schemas) {
            this.schemas = schemas;
        }

        @Override
        public ResultSet getSchemas() throws SQLException {
            return new CustomResultSet(schemas);
        }
    }

    private static class CustomResultSet extends ResultSetAdapter {

        String[] schemas;
        int currentRow = 0;

        public CustomResultSet(String[] schemas) {
            this.schemas = schemas;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            assert columnIndex == 1;
            return schemas[currentRow - 1];
        }

        @Override
        public boolean next() throws SQLException {
            if (currentRow < schemas.length) {
                currentRow++;
                return true;
            } else {
                return false;
            }
        }
    }
}
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

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class JDBCStubUtilTest extends TestCase {
    
    public JDBCStubUtilTest(String name) {
        super(name);
    }
    
    public void testColumnsResultSet() throws Exception {
        ResultSet rs = JDBCStubUtil.columnsResultSet(
                new String[] { "REAL_COL", "VARCHAR_COL" },
                new String[] { "REAL", "VARCHAR" },
                new int[] { Types.REAL, Types.VARCHAR },
                new int[] { 10, 20 },
                new int[] { 6, 0 },
                new int[] { DatabaseMetaData.columnNoNulls, DatabaseMetaData.columnNullable }
        );
        
        rs.next();
        assertEquals("REAL_COL", rs.getString("COLUMN_NAME"));
        assertEquals("REAL", rs.getString("TYPE_NAME"));
        assertEquals(Types.REAL, rs.getInt("DATA_TYPE"));
        assertEquals(10, rs.getInt("COLUMN_SIZE"));
        assertEquals(6, rs.getInt("DECIMAL_DIGITS"));
        assertEquals(DatabaseMetaData.columnNoNulls, rs.getInt("NULLABLE"));
        
        rs.next();
        assertEquals("VARCHAR_COL", rs.getString("COLUMN_NAME"));
        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
        assertEquals(Types.VARCHAR, rs.getInt("DATA_TYPE"));
        assertEquals(20, rs.getInt("COLUMN_SIZE"));
        assertEquals(0, rs.getInt("DECIMAL_DIGITS"));
        assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
    }
}

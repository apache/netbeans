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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetImplTest extends TestCase {
    
    public ResultSetImplTest(String name) {
        super(name);
    }
    
    public void testEmpty() throws Exception {
        ResultSet rs = JDBCStubUtil.createResultSet(Collections.EMPTY_LIST);
        assertFalse(rs.next());
    }
    
    public void testNextAndGetObject() throws Exception {
        
        List col1 = Arrays.asList(new String[] { "FOO", "foo1", "foo2"});
        List col2 = Arrays.asList(new String[] { "BAR", "bar1", "bar2"});
        
        ResultSet rs = JDBCStubUtil.createResultSet(Arrays.asList(new List[] { col1, col2 }));
        
        try {
            rs.getObject("foo1");
            fail("SQLException thrown if next() not previously called");
        } catch (SQLException e) { }
        
        assertTrue(rs.next());
        
        assertEquals("foo1", rs.getObject("FOO"));
        assertEquals("bar1", rs.getObject("BAR"));
        
        try {
            rs.getObject("inexistent");
            fail("SQLException thrown if unkown column name");
        } catch (SQLException e) { }
        
        assertTrue(rs.next());
        assertEquals("bar2", rs.getObject("BAR"));
        assertEquals("foo2", rs.getObject("FOO"));
        
        assertFalse(rs.next());
        assertFalse(rs.next());
    }
}

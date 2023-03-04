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
package org.netbeans.api.db.sql.support;

import org.junit.Test;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;

import static org.junit.Assert.*;

public class SQLIdentifiersTest {
    @Test
    public void testQuoterFromNull() {
        // Without DatabaseMetaData a fallback quoter is returned, that
        // unquotes all popular identifier quotes (SQL-99, mssql and mysql) and
        // quotes with SQL-99 quotes (")
        Quoter quoter = SQLIdentifiers.createQuoter(null);
        assertNotNull(quoter);

        assertEquals("hello\"", quoter.unquote("hello\""));
        assertEquals("\"hello", quoter.unquote("\"hello"));
        assertEquals("hello", quoter.unquote("\"hello\""));
        assertEquals("hello`", quoter.unquote("hello`"));
        assertEquals("`hello", quoter.unquote("`hello"));
        assertEquals("hello", quoter.unquote("`hello`"));
        assertEquals("hello]", quoter.unquote("hello]"));
        assertEquals("[hello", quoter.unquote("[hello"));
        assertEquals("hello", quoter.unquote("[hello]"));
        assertEquals("hello", quoter.quoteIfNeeded("hello"));
        assertEquals("\"hello world\"", quoter.quoteIfNeeded("hello world"));
        assertEquals("\"hello\"", quoter.quoteAlways("hello"));
    }
}

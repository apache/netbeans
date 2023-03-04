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

package org.netbeans.modules.db.api.sql;

import junit.framework.*;

/**
 * Tests the SQLKeyword class, that is, it ensures that the is.*Keyword()
 * methods return true for all keywords. Hopefull this will catch someone
 * making the keyword lists not correctly ordered when adding a
 * possibly forgotten keyword.
 *
 * @author Andrei Badea
 */
public class SQLKeywordsTest extends TestCase {

    public SQLKeywordsTest(String testName) {
        super(testName);
    }

    public void testIsSQL99ReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a reserved keyword", SQLKeywords.isSQL99ReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99ReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99ReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99NonReservedKeyword() {
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_NON_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a non-reserved keyword", SQLKeywords.isSQL99NonReservedKeyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99NonReservedKeyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99NonReservedKeyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }

    public void testIsSQL99Keyword() {
        for (int i = 0; i < SQLKeywords.SQL99_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }
        for (int i = 0; i < SQLKeywords.SQL99_NON_RESERVED.length; i++) {
            String identifier = SQLKeywords.SQL99_RESERVED[i].toUpperCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
            identifier = identifier.toLowerCase();
            assertTrue(identifier + " should be a keyword", SQLKeywords.isSQL99Keyword(identifier));
        }

        // should return null for non-keywords
        assertFalse(SQLKeywords.isSQL99Keyword("FOOBAR"));

        // null identifier should throw NPE
        try {
            SQLKeywords.isSQL99Keyword(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) { }
    }
}

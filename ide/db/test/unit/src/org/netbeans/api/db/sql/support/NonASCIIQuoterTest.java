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

import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class NonASCIIQuoterTest extends NbTestCase {

    public NonASCIIQuoterTest(String name) {
        super(name);
    }

    public void testQuoteIfNeeded() {
        Quoter quoter = new NonASCIIQuoter("\"");

        assertEquals("foo", quoter.quoteIfNeeded("foo"));
        assertEquals("_foo", quoter.quoteIfNeeded("_foo"));

        assertEquals("\"12_foo\"", quoter.quoteIfNeeded("12_foo"));
        assertEquals("\"foo bar\"", quoter.quoteIfNeeded("foo bar"));

        assertEquals("\"foo bar\"", quoter.quoteIfNeeded("\"foo bar\""));
    }

    public void testQuoteAlways() {
        Quoter quoter = new NonASCIIQuoter("\"");

        assertEquals("\"foo\"", quoter.quoteAlways("foo"));
        assertEquals("\"foo bar\"", quoter.quoteAlways("\"foo bar\""));
    }
}

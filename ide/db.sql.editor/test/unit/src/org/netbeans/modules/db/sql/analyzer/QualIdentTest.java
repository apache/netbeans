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

package org.netbeans.modules.db.sql.analyzer;

import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class QualIdentTest extends TestCase {

    public QualIdentTest(String testName) {
        super(testName);
    }

    public void testIsPrefixedBy() {
        QualIdent ident1 = new QualIdent("foo", "bar");
        assertTrue(ident1.isPrefixedBy(new QualIdent()));
        assertTrue(ident1.isPrefixedBy(new QualIdent("foo")));
        assertTrue(ident1.isPrefixedBy(new QualIdent("foo", "bar")));
        assertFalse(ident1.isPrefixedBy(new QualIdent("foo", "bar", "baz")));
    }

    public void testCompareTo() {
        assertEquals(0, new QualIdent().compareTo(new QualIdent()));
        assertEquals(0, new QualIdent("foo", "bar").compareTo(new QualIdent("foo", "bar")));
        assertTrue(new QualIdent("foo").compareTo(new QualIdent("foo", "bar")) < 0);
        assertTrue(new QualIdent("foo", "bar").compareTo(new QualIdent("foo")) > 0);
        assertTrue(new QualIdent("a").compareTo(new QualIdent("aaa")) < 0);
        assertTrue(new QualIdent("a").compareTo(new QualIdent("AAA")) < 0);
    }

    public void testEquals() {
        QualIdent ident1 = new QualIdent("foo", "bar");
        QualIdent ident2 = new QualIdent("foo", "bar");
        assertEquals(ident1, ident2);
        assertEquals(ident2, ident1);
    }

    public void testToString() {
        assertEquals("<empty>", new QualIdent().toString());
        assertEquals("foo.bar", new QualIdent("foo", "bar").toString());
    }
}

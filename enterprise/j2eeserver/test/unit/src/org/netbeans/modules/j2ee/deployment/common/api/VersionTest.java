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
package org.netbeans.modules.j2ee.deployment.common.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class VersionTest extends NbTestCase {

    public VersionTest(String name) {
        super(name);
    }

    public void testJsr277() {
        Version version = Version.fromJsr277NotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("something", version.getQualifier());

        version = Version.fromJsr277NotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());
    }

    public void testDotted() {
        Version version = Version.fromDottedNotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromDottedNotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("6", version.getQualifier());
    }

    public void testJsr277OrDotted() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertEquals("10.3.4", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("something");
        assertEquals("something", version.toString());
        assertNull(version.getMajor());
        assertNull(version.getMinor());
        assertNull(version.getMicro());
        assertNull(version.getUpdate());
        assertNull(version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4.5-something");
        assertEquals("10.3.4.5-something", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("something", version.getQualifier());

        version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4.5.6");
        assertEquals("10.3.4.5.6", version.toString());
        assertEquals(Integer.valueOf("10"), version.getMajor());
        assertEquals(Integer.valueOf("3"), version.getMinor());
        assertEquals(Integer.valueOf("4"), version.getMicro());
        assertEquals(Integer.valueOf("5"), version.getUpdate());
        assertEquals("6", version.getQualifier());
    }

    public void testAboveOrEqual() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10")));
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
        assertTrue(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3.4")));
        assertFalse(version.isAboveOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4")));
    }

    public void testBelowOrEqual() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4.5")));
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.4")));
        assertTrue(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3.4")));
        assertFalse(version.isBelowOrEqual(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
    }

    public void testEqualsAndHashCode() {
        Version version = Version.fromJsr277OrDottedNotationWithFallback("10.3.4");
        assertEquals(version, Version.fromJsr277OrDottedNotationWithFallback("10.3.4"));
        assertEquals(version.hashCode(), Version.fromJsr277OrDottedNotationWithFallback("10.3.4").hashCode());

        assertFalse(version.equals(Version.fromJsr277OrDottedNotationWithFallback("10.3")));
    }
}

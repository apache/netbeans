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
package org.netbeans.api.java.platform;

import org.netbeans.junit.NbTestCase;
import org.openide.modules.SpecificationVersion;

public class SpecificationTest extends NbTestCase {

    public SpecificationTest(String name) {
        super(name);
    }

    public void testJdkVersionFromSpecificationVersion() {
        assertEquals(Runtime.Version.parse("1.8"), new Specification("j2se", new SpecificationVersion("1.8")).getJdkVersion());
        assertEquals(Runtime.Version.parse("1.8"), new Specification("j2se", new SpecificationVersion("1.8.0")).getJdkVersion());
        assertEquals(Runtime.Version.parse("17"), new Specification("j2se", new SpecificationVersion("17")).getJdkVersion());
        assertEquals(Runtime.Version.parse("17.0.2"), new Specification("j2se", new SpecificationVersion("17.0.2")).getJdkVersion());
        // trailing zero components are insignificant, Runtime.Version.parse() rejects them
        assertEquals(Runtime.Version.parse("9"), new Specification("j2se", new SpecificationVersion("9.0")).getJdkVersion());
        assertEquals(Runtime.Version.parse("9"), new Specification("j2se", new SpecificationVersion("9.0.0")).getJdkVersion());
        assertEquals(Runtime.Version.parse("17"), new Specification("j2se", new SpecificationVersion("17.0")).getJdkVersion());
        // non-zero update components are kept
        assertEquals(Runtime.Version.parse("9.0.1"), new Specification("j2se", new SpecificationVersion("9.0.1")).getJdkVersion());
        assertNull(new Specification("j2se", (SpecificationVersion) null).getJdkVersion());
        // 1.0's trailing zero is stripped like any other version (1.0 == 1),
        // while 1.0.2 is kept, just like 9.0.1
        assertEquals(Runtime.Version.parse("1"), new Specification("j2se", new SpecificationVersion("1.0")).getJdkVersion());
        assertEquals(Runtime.Version.parse("1.0.2"), new Specification("j2se", new SpecificationVersion("1.0.2")).getJdkVersion());
        assertEquals(new SpecificationVersion("1"), new Specification("j2se", new SpecificationVersion("1.0")).getVersion());
        // a bare "1" parses as-is (feature 1)
        assertEquals(Runtime.Version.parse("1"), new Specification("j2se", new SpecificationVersion("1")).getJdkVersion());
        assertEquals(new SpecificationVersion("1"), new Specification("j2se", new SpecificationVersion("1")).getVersion());
        // "0" is not a JDK version at all
        assertNull(new Specification("j2se", new SpecificationVersion("0")).getJdkVersion());
    }

    public void testVersionFromJdkVersion() {
        assertEquals(new SpecificationVersion("1.8"), new Specification("j2se", Runtime.Version.parse("1.8")).getVersion());
        assertEquals(new SpecificationVersion("17"), new Specification("j2se", Runtime.Version.parse("17")).getVersion());
        assertEquals(new SpecificationVersion("17.0.2"), new Specification("j2se", Runtime.Version.parse("17.0.2")).getVersion());
        // pre-release/build parts are not representable, they are dropped
        assertEquals(new SpecificationVersion("28"), new Specification("j2se", Runtime.Version.parse("28-ea")).getVersion());
        assertEquals(new SpecificationVersion("28"), new Specification("j2se", Runtime.Version.parse("28-ea+21-1707")).getVersion());
        assertEquals(new SpecificationVersion("1.8"), new Specification("j2se", Runtime.Version.parse("1.8-ea")).getVersion());
        assertEquals(new SpecificationVersion("17.0.2"), new Specification("j2se", Runtime.Version.parse("17.0.2+9")).getVersion());
        assertEquals(new SpecificationVersion("9.0.1.2"), new Specification("j2se", Runtime.Version.parse("9.0.1.2")).getVersion());
        // legacy version with insignificant trailing zeros
        assertEquals(new SpecificationVersion("9"), new Specification("j2se", new SpecificationVersion("9.0")).getVersion());
        assertNull(new Specification("j2se", (Runtime.Version) null).getVersion());
    }

    public void testCompareTo() {
        Specification v17 = new Specification("j2se", Runtime.Version.parse("17"));
        Specification v1702 = new Specification("j2se", Runtime.Version.parse("17.0.2"));
        Specification v8 = new Specification("j2se", Runtime.Version.parse("1.8"));
        assertTrue(v17.compareTo(v1702) < 0);
        assertTrue(v1702.compareTo(v17) > 0);
        assertTrue(v8.compareTo(v17) < 0);
        assertEquals(0, v17.compareTo(new Specification("j2se", Runtime.Version.parse("17"))));
        // versionless platforms: fall back to comparing the version strings
        Specification vNone1 = new Specification("j2se", (Runtime.Version) null);
        Specification vNone2 = new Specification("j2se", (Runtime.Version) null);
        assertEquals(0, vNone1.compareTo(vNone2));
        assertTrue(vNone1.compareTo(v17) < 0);
        assertTrue(v17.compareTo(vNone1) > 0);
    }

    public void testEqualityRoundTrip() {
        assertEquals(
            new Specification("j2se", new SpecificationVersion("1.8")),
            new Specification("j2se", Runtime.Version.parse("1.8")));
        assertEquals(
            new Specification("j2se", new SpecificationVersion("17.0.2")),
            new Specification("j2se", Runtime.Version.parse("17.0.2")));
        if (new Specification("j2se", Runtime.Version.parse("17")).
                equals(new Specification("j2se", Runtime.Version.parse("17.0.2")))) {
            fail("17 and 17.0.2 should not be equal");
        }
        assertEquals(
            new Specification("j2se", (SpecificationVersion) null),
            new Specification("j2se", (Runtime.Version) null));
    }
}

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
        assertEquals(Runtime.Version.parse("1.8"), new Specification("j2se", new SpecificationVersion("1.8")).getJdkVersion()); //NOI18N
        assertEquals(Runtime.Version.parse("1.8"), new Specification("j2se", new SpecificationVersion("1.8.0")).getJdkVersion()); //NOI18N
        assertEquals(Runtime.Version.parse("17"), new Specification("j2se", new SpecificationVersion("17")).getJdkVersion()); //NOI18N
        assertEquals(Runtime.Version.parse("17.0.2"), new Specification("j2se", new SpecificationVersion("17.0.2")).getJdkVersion()); //NOI18N
        // trailing zero components are insignificant, Runtime.Version.parse() rejects them
        assertEquals(Runtime.Version.parse("9"), new Specification("j2se", new SpecificationVersion("9.0")).getJdkVersion()); //NOI18N
        assertEquals(Runtime.Version.parse("9"), new Specification("j2se", new SpecificationVersion("9.0.0")).getJdkVersion()); //NOI18N
        assertEquals(Runtime.Version.parse("17"), new Specification("j2se", new SpecificationVersion("17.0")).getJdkVersion()); //NOI18N
        // non-zero update components are kept
        assertEquals(Runtime.Version.parse("9.0.1"), new Specification("j2se", new SpecificationVersion("9.0.1")).getJdkVersion()); //NOI18N
        assertNull(new Specification("j2se", (SpecificationVersion) null).getJdkVersion());
        // unrepresentable as Runtime.Version
        assertNull(new Specification("j2se", new SpecificationVersion("0")).getJdkVersion()); //NOI18N
    }

    public void testVersionFromJdkVersion() {
        assertEquals(new SpecificationVersion("1.8"), new Specification("j2se", Runtime.Version.parse("1.8")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("17"), new Specification("j2se", Runtime.Version.parse("17")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("17.0.2"), new Specification("j2se", Runtime.Version.parse("17.0.2")).getVersion()); //NOI18N
        // pre-release/build parts are not representable, they are dropped
        assertEquals(new SpecificationVersion("28"), new Specification("j2se", Runtime.Version.parse("28-ea")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("28"), new Specification("j2se", Runtime.Version.parse("28-ea+21-1707")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("1.8"), new Specification("j2se", Runtime.Version.parse("1.8-ea")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("17.0.2"), new Specification("j2se", Runtime.Version.parse("17.0.2+9")).getVersion()); //NOI18N
        assertEquals(new SpecificationVersion("9.0.1.2"), new Specification("j2se", Runtime.Version.parse("9.0.1.2")).getVersion()); //NOI18N
        // legacy version with insignificant trailing zeros
        assertEquals(new SpecificationVersion("9"), new Specification("j2se", new SpecificationVersion("9.0")).getVersion()); //NOI18N
        assertNull(new Specification("j2se", (Runtime.Version) null).getVersion());
    }

    public void testCompareTo() {
        Specification v17 = new Specification("j2se", Runtime.Version.parse("17")); //NOI18N
        Specification v1702 = new Specification("j2se", Runtime.Version.parse("17.0.2")); //NOI18N
        Specification v8 = new Specification("j2se", Runtime.Version.parse("1.8")); //NOI18N
        assertTrue(v17.compareTo(v1702) < 0);
        assertTrue(v1702.compareTo(v17) > 0);
        assertTrue(v8.compareTo(v17) < 0);
        assertEquals(0, v17.compareTo(new Specification("j2se", Runtime.Version.parse("17")))); //NOI18N
        // versionless platforms: fall back to comparing the version strings
        Specification vNone1 = new Specification("j2se", (Runtime.Version) null);
        Specification vNone2 = new Specification("j2se", (Runtime.Version) null);
        assertEquals(0, vNone1.compareTo(vNone2));
        assertTrue(vNone1.compareTo(v17) < 0);
        assertTrue(v17.compareTo(vNone1) > 0);
    }

    public void testEqualityRoundTrip() {
        assertEquals(
            new Specification("j2se", new SpecificationVersion("1.8")), //NOI18N
            new Specification("j2se", Runtime.Version.parse("1.8"))); //NOI18N
        assertEquals(
            new Specification("j2se", new SpecificationVersion("17.0.2")), //NOI18N
            new Specification("j2se", Runtime.Version.parse("17.0.2"))); //NOI18N
        if (new Specification("j2se", Runtime.Version.parse("17")). //NOI18N
                equals(new Specification("j2se", Runtime.Version.parse("17.0.2")))) { //NOI18N
            fail("17 and 17.0.2 should not be equal"); //NOI18N
        }
        assertEquals(
            new Specification("j2se", (SpecificationVersion) null), //NOI18N
            new Specification("j2se", (Runtime.Version) null)); //NOI18N
    }
}

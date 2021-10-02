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

package org.netbeans.api.j2ee.core;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProfileTest extends NbTestCase {

    public ProfileTest(String name) {
        super(name);
    }

    public void testFromPropertiesString() {
        assertEquals(Profile.J2EE_13, Profile.fromPropertiesString("1.3"));
        assertEquals(Profile.J2EE_14, Profile.fromPropertiesString("1.4"));
        assertEquals(Profile.JAVA_EE_5, Profile.fromPropertiesString("1.5"));
        assertEquals(Profile.JAVA_EE_6_FULL, Profile.fromPropertiesString("1.6"));
        assertEquals(Profile.JAVA_EE_6_FULL, Profile.fromPropertiesString("EE_6_FULL"));
        assertEquals(Profile.JAVA_EE_6_WEB, Profile.fromPropertiesString("1.6-web"));
        assertEquals(Profile.JAVA_EE_6_WEB, Profile.fromPropertiesString("EE_6_WEB"));
        assertNull(Profile.fromPropertiesString("something"));
    }

    public void testIsHigherJavaEEVersionJavaEE5() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_5));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_5));

        assertTrue(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_5));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_5));
    }

    public void testIsHigherJavaEEVersionJavaEE6full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_6_WEB));

        assertTrue(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_6_WEB));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_6_WEB));
    }

    public void testIsHigherJavaEEVersionJavaEE7full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_7_WEB));

        assertFalse(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertFalse(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertTrue(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_7_WEB));
        assertTrue(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_7_WEB));
    }

    public void testIsHigherJavaEEVersionJavaEE8full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAVA_EE_8_WEB));

        assertFalse(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertTrue(Profile.JAVA_EE_8_WEB.isAtLeast(Profile.JAVA_EE_8_WEB));
        assertTrue(Profile.JAVA_EE_8_FULL.isAtLeast(Profile.JAVA_EE_8_WEB));
    }

    public void testIsHigherJavaEEVersionJakartaEE8full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAKARTA_EE_8_WEB));

        assertFalse(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertFalse(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertTrue(Profile.JAVA_EE_8_WEB.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertTrue(Profile.JAVA_EE_8_FULL.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertTrue(Profile.JAKARTA_EE_8_WEB.isAtLeast(Profile.JAKARTA_EE_8_WEB));
        assertTrue(Profile.JAKARTA_EE_8_FULL.isAtLeast(Profile.JAKARTA_EE_8_WEB));
    }

    public void testIsHigherJavaEEVersionJakartaEE9full() {
        assertFalse(Profile.J2EE_13.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertFalse(Profile.J2EE_14.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertFalse(Profile.JAVA_EE_5.isAtLeast(Profile.JAKARTA_EE_9_WEB));

        assertFalse(Profile.JAVA_EE_6_WEB.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertFalse(Profile.JAVA_EE_6_FULL.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertFalse(Profile.JAVA_EE_7_WEB.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertFalse(Profile.JAVA_EE_7_FULL.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAVA_EE_8_WEB.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAVA_EE_8_FULL.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAKARTA_EE_8_WEB.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAKARTA_EE_8_FULL.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAKARTA_EE_9_WEB.isAtLeast(Profile.JAKARTA_EE_9_WEB));
        assertTrue(Profile.JAKARTA_EE_9_FULL.isAtLeast(Profile.JAKARTA_EE_9_WEB));
    }

}

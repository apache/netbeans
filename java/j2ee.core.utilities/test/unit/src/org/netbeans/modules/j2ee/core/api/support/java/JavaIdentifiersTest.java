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

package org.netbeans.modules.j2ee.core.api.support.java;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class JavaIdentifiersTest extends TestCase {

    public JavaIdentifiersTest(String testName) {
        super(testName);
    }

    public void testIsValidPackageName() {
        assertTrue(JavaIdentifiers.isValidPackageName(""));
        assertTrue(JavaIdentifiers.isValidPackageName("foo"));
        assertTrue(JavaIdentifiers.isValidPackageName("foo.bar"));
        assertTrue(JavaIdentifiers.isValidPackageName("fooBar"));

        assertFalse(JavaIdentifiers.isValidPackageName(".foo"));
        assertFalse(JavaIdentifiers.isValidPackageName("foo-bar"));
        assertFalse(JavaIdentifiers.isValidPackageName("."));
        assertFalse(JavaIdentifiers.isValidPackageName(".foo"));
        assertFalse(JavaIdentifiers.isValidPackageName("foo.bar."));
        assertFalse(JavaIdentifiers.isValidPackageName("foo. .bar"));
        assertFalse(JavaIdentifiers.isValidPackageName(" "));
        assertFalse(JavaIdentifiers.isValidPackageName("public"));
        assertFalse(JavaIdentifiers.isValidPackageName("int"));
        assertFalse(JavaIdentifiers.isValidPackageName("java"));
        assertFalse(JavaIdentifiers.isValidPackageName("java.something"));
    }

    public void testUnqualify() {
        assertEquals("Foo", JavaIdentifiers.unqualify("Foo"));
        assertEquals("Baz", JavaIdentifiers.unqualify("foo.bar.Baz"));
        assertInvalidFQN("foo.");
        assertInvalidFQN(".");
        assertInvalidFQN(".foo.");
        assertInvalidFQN(".foo.Bar");
    }

    private void assertInvalidFQN(String fqn) {
        try {
            JavaIdentifiers.unqualify(fqn);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetPackageName() {
        assertEquals("", JavaIdentifiers.getPackageName("Bop"));
        assertEquals("foo.bar.baz", JavaIdentifiers.getPackageName("foo.bar.baz.Bop"));
        try {
            JavaIdentifiers.getPackageName("foo.bar.");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }
}

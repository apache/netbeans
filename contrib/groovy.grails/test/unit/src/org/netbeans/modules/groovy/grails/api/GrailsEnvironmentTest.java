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

package org.netbeans.modules.groovy.grails.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class GrailsEnvironmentTest extends NbTestCase{

    public GrailsEnvironmentTest(String name) {
        super(name);
    }

    public void testValueOf() {
        GrailsEnvironment env = GrailsEnvironment.valueOf("dev");
        assertEquals(GrailsEnvironment.DEV, env);
        assertFalse(env.isCustom());

        env = GrailsEnvironment.valueOf("prod");
        assertEquals(GrailsEnvironment.PROD, env);
        assertFalse(env.isCustom());

        env = GrailsEnvironment.valueOf("test");
        assertEquals(GrailsEnvironment.TEST, env);
        assertFalse(env.isCustom());

        env = GrailsEnvironment.valueOf("something");
        assertNotNull(env);
        assertTrue(env.isCustom());

        try {
            GrailsEnvironment.valueOf(null);
            fail("Method forString accepts null");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testToString() {
        assertEquals("dev", GrailsEnvironment.DEV.toString());
        assertEquals("prod", GrailsEnvironment.PROD.toString());
        assertEquals("test", GrailsEnvironment.TEST.toString());
        assertEquals("something", GrailsEnvironment.valueOf("something").toString());
    }
}

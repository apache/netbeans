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
package org.netbeans.modules.web.jsfapi.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Benjamin Asbach
 */
public class JsfVersionTest {

    @Test
    public void testVersionComparison_JSF_1_0() {
        JsfVersion jsfVersion = JsfVersion.JSF_1_0;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_1_1() {
        JsfVersion jsfVersion = JsfVersion.JSF_1_1;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_1_2() {
        JsfVersion jsfVersion = JsfVersion.JSF_1_2;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_2_0() {
        JsfVersion jsfVersion = JsfVersion.JSF_2_0;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_2_1() {
        JsfVersion jsfVersion = JsfVersion.JSF_2_1;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_2_2() {
        JsfVersion jsfVersion = JsfVersion.JSF_2_2;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_2_3() {
        JsfVersion jsfVersion = JsfVersion.JSF_2_3;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_3_0() {
        JsfVersion jsfVersion = JsfVersion.JSF_3_0;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testVersionComparison_JSF_4_0() {
        JsfVersion jsfVersion = JsfVersion.JSF_4_0;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertFalse(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }
    
    @Test
    public void testVersionComparison_JSF_4_1() {
        JsfVersion jsfVersion = JsfVersion.JSF_4_1;

        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_1_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_1));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_2));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_2_3));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_3_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_4_0));
        assertTrue(jsfVersion.isAtLeast(JsfVersion.JSF_4_1));
    }

    @Test
    public void testShortName() {
        assertEquals("JSF 1.0", JsfVersion.JSF_1_0.getShortName());
        assertEquals("JSF 1.1", JsfVersion.JSF_1_1.getShortName());
        assertEquals("JSF 1.2", JsfVersion.JSF_1_2.getShortName());
        assertEquals("JSF 2.0", JsfVersion.JSF_2_0.getShortName());
        assertEquals("JSF 2.1", JsfVersion.JSF_2_1.getShortName());
        assertEquals("JSF 2.2", JsfVersion.JSF_2_2.getShortName());
        assertEquals("JSF 2.3", JsfVersion.JSF_2_3.getShortName());
        assertEquals("JSF 3.0", JsfVersion.JSF_3_0.getShortName());
        assertEquals("Faces 4.0", JsfVersion.JSF_4_0.getShortName());
        assertEquals("Faces 4.1", JsfVersion.JSF_4_1.getShortName());
    }

    @Test
    public void testLatest() {
        assertEquals(JsfVersion.JSF_4_1, JsfVersion.latest());
    }
}

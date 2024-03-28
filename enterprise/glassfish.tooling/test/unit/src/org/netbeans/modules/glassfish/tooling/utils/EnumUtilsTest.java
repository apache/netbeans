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
package org.netbeans.modules.glassfish.tooling.utils;

import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_3;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_4;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_6_2_5;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_7_0_13;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishVersion.GF_8_0_0;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test enumeration utilities.
 * <p>
 * @author Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class EnumUtilsTest {
    
    
    /**
     * Test equals method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testEq() {
        assertFalse(EnumUtils.eq(GF_8_0_0, GF_7_0_13), "Equals for a > b shall be false.");
        assertTrue(EnumUtils.eq(GF_8_0_0, GF_8_0_0), "Equals for a == b shall be true.");
        assertFalse(EnumUtils.eq(GF_7_0_13, GF_6_2_5), "Equals for a > b shall be false.");
        assertTrue(EnumUtils.eq(GF_7_0_13, GF_7_0_13), "Equals for a == b shall be true.");
        assertFalse(EnumUtils.eq(GF_4, GF_3), "Equals for a > b shall be false.");
        assertTrue(EnumUtils.eq(GF_4, GF_4), "Equals for a == b shall be true.");
        assertFalse(EnumUtils.eq(GF_3, GF_4), "Equals for a < b shall be false.");
        assertFalse(EnumUtils.eq(GF_3, null), "Equals for a, null shall be false.");
        assertFalse(EnumUtils.eq(null, GF_3), "Equals for null, b shall be false.");
        assertTrue(EnumUtils.eq(null, null), "Equals for null, null shall be true.");
    }

    /**
     * Test not equals method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testNe() {
        assertTrue(EnumUtils.ne(GF_8_0_0, GF_7_0_13), "Not equals for a > b shall be true.");
        assertFalse(EnumUtils.ne(GF_8_0_0, GF_8_0_0), "Not equals for a == b shall be false.");
        assertTrue(EnumUtils.ne(GF_7_0_13, GF_6_2_5), "Not equals for a > b shall be true.");
        assertFalse(EnumUtils.ne(GF_7_0_13, GF_7_0_13), "Not equals for a == b shall be false.");
        assertTrue(EnumUtils.ne(GF_4, GF_3), "Not equals for a > b shall be true.");
        assertFalse(EnumUtils.ne(GF_4, GF_4), "Not equals for a == b shall be false.");
        assertTrue(EnumUtils.ne(GF_3, GF_4), "Not equals for a < b shall be true.");
        assertTrue(EnumUtils.ne(GF_3, null), "Not equals for a, null shall be true.");
        assertTrue(EnumUtils.ne(null, GF_3), "Not equals for null, b shall be true.");
        assertFalse(EnumUtils.ne(null, null), "Not equals for null, null shall be false.");
    }

    /**
     * Test less than method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testLt() {
        assertFalse(EnumUtils.lt(GF_8_0_0, GF_7_0_13), "Less than for a > b shall be false.");
        assertFalse(EnumUtils.lt(GF_8_0_0, GF_8_0_0), "Less than for a == b shall be false.");
        assertFalse(EnumUtils.lt(GF_7_0_13, GF_6_2_5), "Less than for a > b shall be false.");
        assertFalse(EnumUtils.lt(GF_7_0_13, GF_7_0_13), "Less than for a == b shall be false.");
        assertFalse(EnumUtils.lt(GF_4, GF_3), "Less than for a > b shall be false.");
        assertFalse(EnumUtils.lt(GF_4, GF_4), "Less than for a == b shall be false.");
        assertTrue(EnumUtils.lt(GF_3, GF_4), "Less than for a < b shall be true.");
        assertFalse(EnumUtils.lt(GF_3, null), "Less than for a, null shall be false.");
        assertTrue(EnumUtils.lt(null, GF_3), "Less than for null, b shall be true.");
        assertFalse(EnumUtils.lt(null, null), "Less than for null, null shall be false.");
    }

    /**
     * Test less than or equal method.
     * Expected results:<ul>
     * <li>{@code a > b}: false</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: true</li>
     * <li>{@code a, null}: false</li>
     * <li>{@code null, b}: true</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testLe() {
        assertFalse(EnumUtils.le(GF_8_0_0, GF_7_0_13), "Less than or equal for a > b shall be false.");
        assertTrue(EnumUtils.le(GF_8_0_0, GF_8_0_0), "Less than or equal for a == b shall be true.");
        assertFalse(EnumUtils.le(GF_7_0_13, GF_6_2_5), "Less than or equal for a > b shall be false.");
        assertTrue(EnumUtils.le(GF_7_0_13, GF_7_0_13), "Less than or equal for a == b shall be true.");
        assertFalse(EnumUtils.le(GF_4, GF_3), "Less than or equal for a > b shall be false.");
        assertTrue(EnumUtils.le(GF_4, GF_4), "Less than or equal for a == b shall be true.");
        assertTrue(EnumUtils.le(GF_3, GF_4), "Less than or equal for a < b shall be true.");
        assertFalse(EnumUtils.le(GF_3, null), "Less than or equal for a, null shall be false.");
        assertTrue(EnumUtils.le(null, GF_3), "Less than or equal for null, b shall be true.");
        assertTrue(EnumUtils.le(null, null), "Less than or equal for null, null shall be true.");
    }

   /**
     * Test greater than method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: false</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: false</li></ul>
     */
    @Test
    public void testGt() {
        assertTrue(EnumUtils.gt(GF_8_0_0, GF_7_0_13), "Greater than for a > b shall be true.");
        assertFalse(EnumUtils.gt(GF_8_0_0, GF_8_0_0), "Greater than for a == b shall be false.");
        assertTrue(EnumUtils.gt(GF_7_0_13, GF_6_2_5), "Greater than for a > b shall be true.");
        assertFalse(EnumUtils.gt(GF_7_0_13, GF_7_0_13), "Greater than for a == b shall be false.");
        assertTrue(EnumUtils.gt(GF_4, GF_3), "Greater than for a > b shall be true.");
        assertFalse(EnumUtils.gt(GF_4, GF_4), "Greater than for a == b shall be false.");
        assertFalse(EnumUtils.gt(GF_3, GF_4), "Greater than for a < b shall be false.");
        assertTrue(EnumUtils.gt(GF_3, null), "Greater than for a, null shall be true.");
        assertFalse(EnumUtils.gt(null, GF_3), "Greater than for null, b shall be false.");
        assertFalse(EnumUtils.gt(null, null), "Greater than for null, null shall be false.");
    }

    /**
     * Test greater than or equal method.
     * Expected results:<ul>
     * <li>{@code a > b}: true</li>
     * <li>{@code a == b}: true</li>
     * <li>{@code a < b}: false</li>
     * <li>{@code a, null}: true</li>
     * <li>{@code null, b}: false</li>
     * <li>{@code null, null}: true</li></ul>
     */
    @Test
    public void testGe() {
        assertTrue(EnumUtils.ge(GF_8_0_0, GF_7_0_13), "Greater than or equal for a > b shall be true.");
        assertTrue(EnumUtils.ge(GF_8_0_0, GF_8_0_0), "Greater than or equal for a == b shall be true.");
        assertTrue(EnumUtils.ge(GF_7_0_13, GF_6_2_5), "Greater than or equal for a > b shall be true.");
        assertTrue(EnumUtils.ge(GF_7_0_13, GF_7_0_13), "Greater than or equal for a == b shall be true.");
        assertTrue(EnumUtils.ge(GF_4, GF_3), "Greater than or equal for a > b shall be true.");
        assertTrue(EnumUtils.ge(GF_4, GF_4), "Greater than or equal for a == b shall be true.");
        assertFalse(EnumUtils.ge(GF_3, GF_4), "Greater than or equal for a < b shall be false.");
        assertTrue(EnumUtils.ge(GF_3, null), "Greater than or equal for a, null shall be true.");
        assertFalse(EnumUtils.ge(null, GF_3), "Greater than or equal for null, b shall be false.");
        assertTrue(EnumUtils.ge(null, null), "Greater than or equal for null, null shall be true.");
    }

}

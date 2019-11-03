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
package org.netbeans.modules.payara.tooling.utils;

import static org.netbeans.modules.payara.tooling.data.PayaraVersion.PF_4_1_144;
import static org.netbeans.modules.payara.tooling.data.PayaraVersion.PF_5_181;
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
        assertFalse(EnumUtils.eq(PF_5_181, PF_4_1_144), "Equals for a > b shall be false.");
        assertTrue(EnumUtils.eq(PF_5_181, PF_5_181), "Equals for a == b shall be true.");
        assertFalse(EnumUtils.eq(PF_4_1_144, PF_5_181), "Equals for a < b shall be false.");
        assertFalse(EnumUtils.eq(PF_4_1_144, null), "Equals for a, null shall be false.");
        assertFalse(EnumUtils.eq(null, PF_4_1_144), "Equals for null, b shall be false.");
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
        assertTrue(EnumUtils.ne(PF_5_181, PF_4_1_144), "Not equals for a > b shall be true.");
        assertFalse(EnumUtils.ne(PF_5_181, PF_5_181), "Not equals for a == b shall be false.");
        assertTrue(EnumUtils.ne(PF_4_1_144, PF_5_181), "Not equals for a < b shall be true.");
        assertTrue(EnumUtils.ne(PF_4_1_144, null), "Not equals for a, null shall be true.");
        assertTrue(EnumUtils.ne(null, PF_4_1_144), "Not equals for null, b shall be true.");
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
        assertFalse(EnumUtils.lt(PF_5_181, PF_4_1_144), "Less than for a > b shall be false.");
        assertFalse(EnumUtils.lt(PF_5_181, PF_5_181), "Less than for a == b shall be false.");
        assertTrue(EnumUtils.lt(PF_4_1_144, PF_5_181), "Less than for a < b shall be true.");
        assertFalse(EnumUtils.lt(PF_4_1_144, null), "Less than for a, null shall be false.");
        assertTrue(EnumUtils.lt(null, PF_4_1_144), "Less than for null, b shall be true.");
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
        assertFalse(EnumUtils.le(PF_5_181, PF_4_1_144), "Less than or equal for a > b shall be false.");
        assertTrue(EnumUtils.le(PF_5_181, PF_5_181), "Less than or equal for a == b shall be true.");
        assertTrue(EnumUtils.le(PF_4_1_144, PF_5_181), "Less than or equal for a < b shall be true.");
        assertFalse(EnumUtils.le(PF_4_1_144, null), "Less than or equal for a, null shall be false.");
        assertTrue(EnumUtils.le(null, PF_4_1_144), "Less than or equal for null, b shall be true.");
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
        assertTrue(EnumUtils.gt(PF_5_181, PF_4_1_144), "Greater than for a > b shall be true.");
        assertFalse(EnumUtils.gt(PF_5_181, PF_5_181), "Greater than for a == b shall be false.");
        assertFalse(EnumUtils.gt(PF_4_1_144, PF_5_181), "Greater than for a < b shall be false.");
        assertTrue(EnumUtils.gt(PF_4_1_144, null), "Greater than for a, null shall be true.");
        assertFalse(EnumUtils.gt(null, PF_4_1_144), "Greater than for null, b shall be false.");
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
        assertTrue(EnumUtils.ge(PF_5_181, PF_4_1_144), "Greater than or equal for a > b shall be true.");
        assertTrue(EnumUtils.ge(PF_5_181, PF_5_181), "Greater than or equal for a == b shall be true.");
        assertFalse(EnumUtils.ge(PF_4_1_144, PF_5_181), "Greater than or equal for a < b shall be false.");
        assertTrue(EnumUtils.ge(PF_4_1_144, null), "Greater than or equal for a, null shall be true.");
        assertFalse(EnumUtils.ge(null, PF_4_1_144), "Greater than or equal for null, b shall be false.");
        assertTrue(EnumUtils.ge(null, null), "Greater than or equal for null, null shall be true.");
    }

}

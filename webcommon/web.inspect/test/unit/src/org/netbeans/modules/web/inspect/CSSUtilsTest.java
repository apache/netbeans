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
package org.netbeans.modules.web.inspect;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests of class {@code CSSUtils}.
 *
 * @author Jan Stola
 */
public class CSSUtilsTest {

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty1() {
        String name = "color"; // NOI18N
        boolean expResult = true;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty2() {
        String name = "border"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritedProperty} method.
     */
    @Test
    public void testIsInheritedProperty3() {
        String name = "someNonExistentProperty"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritedProperty(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritValue} method.
     */
    @Test
    public void testIsInheritValue1() {
        String value = "inherit"; // NOI18N
        boolean expResult = true;
        boolean result = CSSUtils.isInheritValue(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInheritValue} method.
     */
    @Test
    public void testIsInheritValue2() {
        String value = "black"; // NOI18N
        boolean expResult = false;
        boolean result = CSSUtils.isInheritValue(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeSelector} method.
     */
    @Test
    public void testNormalizeSelector1() {
        String selector = "  div "; // NOI18N
        String expResult = "div"; // NOI18N
        String result = CSSUtils.normalizeSelector(selector);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeSelector} method.
     */
    @Test
    public void testNormalizeSelector2() {
        String selector = "  h1     ,   div "; // NOI18N
        String expResult = "h1,div"; // NOI18N
        String result = CSSUtils.normalizeSelector(selector);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery1() {
        String mediaQueryList = "  only    screen "; // NOI18N
        String expResult = "only screen"; // NOI18N
        String result = CSSUtils.normalizeMediaQuery(mediaQueryList);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery2() {
        String mediaQueryList1 = "(min-width: 768px) and (max-width: 979px)"; // NOI18N
        String mediaQueryList2 = "(max-width: 979px) and (min-width: 768px)"; // NOI18N
        String result1 = CSSUtils.normalizeMediaQuery(mediaQueryList1);
        String result2 = CSSUtils.normalizeMediaQuery(mediaQueryList2);
        assertEquals(result1, result2);
    }

    /**
     * Test of {@code normalizeMediaQuery} method.
     */
    @Test
    public void testNormalizeMediaQuery3() {
        String mediaQueryList1 = "only screen and (min-width: 768px) and (max-width: 979px)"; // NOI18N
        String mediaQueryList2 = "only screen and (  max-width:   979px  )   and  (min-width: 768px)"; // NOI18N
        String result1 = CSSUtils.normalizeMediaQuery(mediaQueryList1);
        String result2 = CSSUtils.normalizeMediaQuery(mediaQueryList2);
        assertEquals(result1, result2);
    }

}

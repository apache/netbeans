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
package org.netbeans.modules.web.inspect.webkit.ui;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of class {@code RuleInfo}.
 *
 * @author Jan Stola
 */
public class RuleInfoTest {

    /**
     * Test of {@code isOverriden} method.
     */
    @Test
    public void testIsOverriden() {
        String propertyName = "color"; // NOI18N
        RuleInfo instance = new RuleInfo();
        boolean expResult = false;
        boolean result = instance.isOverriden(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code setOverriden} method.
     */
    @Test
    public void testSetOverriden() {
        String propertyName = "color"; // NOI18N
        RuleInfo instance = new RuleInfo();
        instance.markAsOverriden(propertyName);
        boolean expResult = true;
        boolean result = instance.isOverriden(propertyName);
        assertEquals(expResult, result);
        propertyName = "border"; // NOI18N
        expResult = false;
        result = instance.isOverriden(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * Test of {@code isInherited} and {@code setInherited} methods.
     */
    @Test
    public void testInherited() {
        RuleInfo instance = new RuleInfo();
        boolean result = instance.isInherited();
        assertFalse(result);
        instance.setInherited(true);
        result = instance.isInherited();
        assertTrue(result);
    }

}

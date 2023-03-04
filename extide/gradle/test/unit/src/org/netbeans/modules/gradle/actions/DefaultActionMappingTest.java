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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laszlo Kishalmi
 */
public class DefaultActionMappingTest {

    /**
     * Test of isApplicable method, of class DefaultActionMapping.
     */
    @Test
    public void testIsApplicable() {
        DefaultActionMapping instance = new DefaultActionMapping();
        instance.withPlugins = new HashSet<>(Arrays.asList("java", "war"));
        assertTrue(instance.isApplicable(new HashSet<>(Arrays.asList("java", "war", "application", "jacoco"))));
    }

    @Test
    public void testIsNotApplicable() {
        DefaultActionMapping instance = new DefaultActionMapping();
        instance.withPlugins = new HashSet<>(Arrays.asList("java", "war"));
        assertFalse(instance.isApplicable(new HashSet<>(Arrays.asList("java", "application", "jacoco"))));
    }

    /**
     * Test of compareTo method, of class DefaultActionMapping.
     */
    @Test
    public void testCompareTo() {
        DefaultActionMapping instance1 = new DefaultActionMapping("run");
        DefaultActionMapping instance2 = new DefaultActionMapping("run");
        int expResult = 0;
        int result = instance1.compareTo(instance2);
        assertEquals(expResult, result);
    }

    @Test
    public void testCompareTo1() {
        DefaultActionMapping instance1 = new DefaultActionMapping("run");
        DefaultActionMapping instance2 = new DefaultActionMapping("run");
        instance1.priority = 100;
        int result = instance1.compareTo(instance2);
        assertTrue(result > 0);
    }

    @Test
    public void testCompareTo2() {
        DefaultActionMapping instance1 = new DefaultActionMapping("run");
        DefaultActionMapping instance2 = new DefaultActionMapping("run");
        instance2.withPlugins = new HashSet<>(Arrays.asList("java", "war"));
        int result = instance1.compareTo(instance2);
        assertTrue(result < 0);
    }

    @Test
    public void testGetName() {
        DefaultActionMapping instance = new DefaultActionMapping("run");
        String expResult = "run";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDisplayName() {
        DefaultActionMapping instance = new DefaultActionMapping("run");
        String expResult = "run";
        String result = instance.getDisplayName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetArgs() {
        DefaultActionMapping instance = new DefaultActionMapping();
        String expResult = "";
        String result = instance.getArgs();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetReloadRule() {
        DefaultActionMapping instance = new DefaultActionMapping();
        ActionMapping.ReloadRule expResult = ActionMapping.ReloadRule.DEFAULT;
        ActionMapping.ReloadRule result = instance.getReloadRule();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetReloadArgs() {
        DefaultActionMapping instance = new DefaultActionMapping();
        String expResult = "";
        String result = instance.getReloadArgs();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsRepeatable() {
        DefaultActionMapping instance = new DefaultActionMapping();
        assertTrue(instance.isRepeatable());
    }

}

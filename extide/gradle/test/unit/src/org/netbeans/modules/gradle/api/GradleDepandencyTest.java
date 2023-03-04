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
package org.netbeans.modules.gradle.api;

import java.util.Collections;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author lkishalmi
 */
public class GradleDepandencyTest {

    @Test
    public void testGetGroup1() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency(":hamcrest-core-1.3:", Collections.emptySet());
        String expResult = "";
        String result = instance.getGroup();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetGroup2() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency("org.hamcrest:hamcrest-core:1.3", Collections.emptySet());
        String expResult = "org.hamcrest";
        String result = instance.getGroup();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetName1() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency(":hamcrest-core-1.3:", Collections.emptySet());
        String expResult = "hamcrest-core-1.3";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetName2() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency("org.hamcrest:hamcrest-core:1.3", Collections.emptySet());
        String expResult = "hamcrest-core";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersion1() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency(":hamcrest-core-1.3:", Collections.emptySet());
        String expResult = "";
        String result = instance.getVersion();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersion2() {
        GradleDependency.ModuleDependency instance = new GradleDependency.ModuleDependency("org.hamcrest:hamcrest-core:1.3", Collections.emptySet());
        String expResult = "1.3";
        String result = instance.getVersion();
        assertEquals(expResult, result);
    }
}

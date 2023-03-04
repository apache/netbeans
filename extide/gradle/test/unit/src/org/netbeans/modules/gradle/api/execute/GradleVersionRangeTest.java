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
package org.netbeans.modules.gradle.api.execute;

import org.gradle.util.GradleVersion;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleVersionRangeTest {

    public GradleVersionRangeTest() {
    }


    /**
     * Test of from method, of class GradleVersionRange.
     */
    @Test
    public void testFrom() {
        GradleDistributionManager.GradleVersionRange range = GradleDistributionManager.GradleVersionRange.from(GradleVersion.version("2.0"));
        assertTrue(range.contains(GradleVersion.version("2.1")));
        assertTrue(range.contains(GradleVersion.version("2.0")));
        assertFalse(range.contains(GradleVersion.version("1.9")));
    }

    @Test
    public void testUntil() {
        GradleDistributionManager.GradleVersionRange range = GradleDistributionManager.GradleVersionRange.until(GradleVersion.version("6.7"));
        assertTrue(range.contains(GradleVersion.version("2.1")));
        assertFalse(range.contains(GradleVersion.version("7.0")));
        assertFalse(range.contains(GradleVersion.version("6.7")));
    }


    @Test
    public void test() {
        GradleDistributionManager.GradleVersionRange range = GradleDistributionManager.GradleVersionRange.range("3.0", "5.0");
        assertTrue(range.contains(GradleVersion.version("3.0")));
        assertTrue(range.contains(GradleVersion.version("4.0")));
        assertFalse(range.contains(GradleVersion.version("5.0")));
        assertFalse(range.contains(GradleVersion.version("6.0")));
    }
}

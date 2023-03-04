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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class WildflyPluginUtilsTest extends NbTestCase {

    public WildflyPluginUtilsTest(String testName) {
        super(testName);
    }

    public void testVersion() {
        WildflyPluginUtils.Version version = new WildflyPluginUtils.Version("4.1.1.update", false); // NOI18N
        assertEquals("4", version.getMajorNumber()); // NOI18N
        assertEquals("1", version.getMinorNumber()); // NOI18N
        assertEquals("1", version.getMicroNumber()); // NOI18N
        assertEquals("update", version.getUpdate()); // NOI18N

        WildflyPluginUtils.Version versionCmp1 = new WildflyPluginUtils.Version("4.1.1.update", false); // NOI18N
        assertEquals(version, versionCmp1);
        assertEquals(0, version.compareTo(versionCmp1));
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp1));
        assertEquals(version.hashCode(), versionCmp1.hashCode());

        WildflyPluginUtils.Version versionCmp2 = new WildflyPluginUtils.Version("4.1.1", false); // NOI18N
        assertTrue(version.compareTo(versionCmp2) > 0);
        assertEquals(0, version.compareToIgnoreUpdate(versionCmp2));
    }

    public void testComparison() {
        WildflyPluginUtils.Version version1 = new WildflyPluginUtils.Version("9.0.1", true); // NOI18N
        WildflyPluginUtils.Version version2 = new WildflyPluginUtils.Version("10.0.0", true); // NOI18N
        WildflyPluginUtils.Version version3 = new WildflyPluginUtils.Version("8.1.1", true); // NOI18N
        WildflyPluginUtils.Version eap7 = new WildflyPluginUtils.Version("7.0.0", false); // NOI18N

        assertTrue(version1.compareTo(version2) < 0);
        assertTrue(version2.compareTo(version1) > 0);
        assertTrue(version3.compareTo(version1) < 0);
        assertTrue(version1.compareTo(version3) > 0);
        assertTrue(version1.equals(version1));
        assertTrue(version2.equals(version2));
        assertEquals(version1.compareTo(version1), 0);
        assertEquals(version2.compareTo(version2), 0);
        assertEquals(version3.compareTo(version3), 0);
        assertEquals(version2.compareTo(new WildflyPluginUtils.Version("10.0.0", true)), 0); // NOI18N
        assertEquals(eap7.compareTo(new WildflyPluginUtils.Version("10.0.0", true)), 0); // NOI18N
        assertTrue(eap7.compareTo(WildflyPluginUtils.WILDFLY_8_0_0) > 0); // NOI18N
    }

}

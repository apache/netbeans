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
package org.netbeans.modules.gradle;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class GradleModuleFileCache21Test extends NbTestCase {

    public GradleModuleFileCache21Test(String name) {
        super(name);
    }
    
    /**
     * Checks GAV split with regular fixed versions.
     */
    public void testGavSplitFixedVersion() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("io.micronaut:micronaut-core:2.3.4"); // NOI18N
        assertEquals("io.micronaut", parts[0]);
        assertEquals("micronaut-core", parts[1]);
        assertEquals("2.3.4", parts[2]);
    }

    /**
     * Checks GAV split with -SNAPSHOT and a maven-like timestamp/sequence as the
     * snapshot unique id.
     */
    public void testGavSplitFixedSnapshotWithMavenTimestamp() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("io.micronaut:micronaut-core:2.3.4-SNAPSHOT:20210302.164619-21"); // NOI18N
        assertEquals("io.micronaut", parts[0]); 
        assertEquals("micronaut-core", parts[1]);
        assertEquals("2.3.4-SNAPSHOT", parts[2]);
    }

    public void testGavSplitFixedSnapshotWithoutUnqiueId() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("io.micronaut:micronaut-core:2.3.4-SNAPSHOT"); // NOI18N
        assertEquals("io.micronaut", parts[0]); 
        assertEquals("micronaut-core", parts[1]);
        assertEquals("2.3.4-SNAPSHOT", parts[2]);
    }

    public void testGavSplitIncomplete() throws Exception {
        try {
            GradleModuleFileCache21.gavSplit("junit:junit");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
            assertEquals("Invalid GAV format: 'junit:junit'", iae.getMessage());
        }
    }

    public void testGavSplitIncomplete2() throws Exception {
        try {
            GradleModuleFileCache21.gavSplit("junit");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
            assertEquals("Invalid GAV format: 'junit'", iae.getMessage());
        }
    }

    public void testGavSplitIncomplete3() throws Exception {
        try {
            GradleModuleFileCache21.gavSplit("");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
            assertEquals("Invalid GAV format: ''", iae.getMessage());
        }
    }

    public void testGavSplitEmpty() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("org.junit.jupiter:junit-jupiter-api:");
        assertEquals("org.junit.jupiter", parts[0]); 
        assertEquals("junit-jupiter-api", parts[1]);
        assertEquals("", parts[2]);
    }

    public void testGavSplitEmpty2() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("org.junit.jupiter::");
        assertEquals("org.junit.jupiter", parts[0]); 
        assertEquals("", parts[1]);
        assertEquals("", parts[2]);
    }

    public void testGavSplitEmpty3() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("::");
        assertEquals("", parts[0]); 
        assertEquals("", parts[1]);
        assertEquals("", parts[2]);
    }
}

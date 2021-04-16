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
        assertEquals(parts[0], "io.micronaut"); // NOI18N
        assertEquals(parts[1], "micronaut-core"); // NOI18N
        assertEquals(parts[2], "2.3.4"); // NOI18N
    }

    /**
     * Checks GAV split with -SNAPSHOT and a maven-like timestamp/sequence as the
     * snapshot unique id.
     */
    public void testGavSplitFixedSnapshotWithMavenTimestamp() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("io.micronaut:micronaut-core:2.3.4-SNAPSHOT:20210302.164619-21"); // NOI18N
        assertEquals(parts[0], "io.micronaut"); // NOI18N
        assertEquals(parts[1], "micronaut-core"); // NOI18N
        assertEquals(parts[2], "2.3.4-SNAPSHOT"); // NOI18N
    }

    public void testGavSplitFixedSnapshotWithoutUnqiueId() throws Exception {
        String[] parts = GradleModuleFileCache21.gavSplit("io.micronaut:micronaut-core:2.3.4-SNAPSHOT"); // NOI18N
        assertEquals(parts[0], "io.micronaut"); // NOI18N
        assertEquals(parts[1], "micronaut-core"); // NOI18N
        assertEquals(parts[2], "2.3.4-SNAPSHOT"); // NOI18N
    }
}

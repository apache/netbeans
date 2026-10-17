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
package org.netbeans.modules.debugger.jpda.ui;

import java.util.List;
import junit.framework.Test;
import org.netbeans.api.debugger.jpda.JPDASupport;

/**
 * Kotlin sources laid out normally ({@code Hi.kt}). The {@code Kotlin} stratum
 * must resolve to {@code Hi.kt} and its line must be used.
 */
public class KotlinSmapTest extends KotlinSmapSupport {

    public KotlinSmapTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(KotlinSmapTest.class);
    }

    public void testKotlinStratumOpensKotlinLine() throws Throwable {
        debug("Hi.kt", frame -> {
            assertEquals("Kotlin", frame.getDefaultStratum());
            assertEquals(List.of("Java", "Kotlin", "KotlinDebug"), frame.getAvailableStrata());
            assertEquals("Java line", 9, frame.getLineNumber("Java"));
            assertEquals("Kotlin line", 2, frame.getLineNumber("Kotlin"));

            assertEquals("Default stratum", "Hi.kt:2", showSource(frame, null));
            assertEquals("Kotlin stratum", "Hi.kt:2", showSource(frame, "Kotlin"));
            assertEquals("Annotation URL for default stratum", "Hi.kt", getURL(frame, null));
            assertEquals("Annotation URL for Kotlin stratum", "Hi.kt", getURL(frame, "Kotlin"));
        });
    }
}

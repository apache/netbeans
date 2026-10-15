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

import junit.framework.Test;
import org.netbeans.api.debugger.jpda.JPDASupport;

/**
 * Like JSP and its generated servlet: the requested stratum ({@code Java} -
 * {@code Hi.kt}) has no source, the default stratum ({@code Kotlin} -
 * {@code HiKt}) has one. The line must come from the default stratum.
 * Separate class, so the source path of {@link KotlinSmapTest} doesn't leak in.
 */
public class KotlinSmapDefaultStratumTest extends KotlinSmapSupport {

    public KotlinSmapDefaultStratumTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(KotlinSmapDefaultStratumTest.class);
    }

    public void testLineFromDefaultStratum() throws Throwable {
        debug("HiKt", frame -> {
            assertEquals("Java stratum falls back to Kotlin", "HiKt:2", showSource(frame, "Java"));
            assertEquals("Kotlin stratum", "HiKt:2", showSource(frame, "Kotlin"));
        });
    }
}

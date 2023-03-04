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

package org.netbeans.api.debugger.test;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

import java.util.*;

/**
 * A dummy debugger implementation.
 *
 * @author Maros Sandor
 */
public class TestDebugger {

    public static final String  ENGINE_ID   = "netbeans-TestSession/Basic";

    public static final String  SESSION_ID  = "netbeans-TestSession";

    private ContextProvider      lookupProvider;
    private TestEngineProvider  testEngineProvider;

    public TestDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        List l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            if (l.get (i) instanceof TestEngineProvider) testEngineProvider = (TestEngineProvider) l.get (i);
        }
        if (testEngineProvider == null) throw new IllegalArgumentException("TestEngineProvider have to be used to start TestDebugger!");
    }

    public void finish() {
        testEngineProvider.getDestructor().killEngine();
    }
}

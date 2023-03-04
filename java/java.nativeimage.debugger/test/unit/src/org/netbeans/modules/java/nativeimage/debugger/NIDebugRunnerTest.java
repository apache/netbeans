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
package org.netbeans.modules.java.nativeimage.debugger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.java.nativeimage.debugger.api.NIDebugRunner;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author martin
 */
public class NIDebugRunnerTest {

    public NIDebugRunnerTest() {
    }

    private static Lookup getTestLookup() {
        Lookup launchCtx = new ProxyLookup(
                        Lookups.fixed(new TestNIDebuggerServiceProvider()),
                        Lookup.getDefault()
                );
        return launchCtx;
    }

    @Test
    public void testDebuggerProviderBreakpoints() {
        LineBreakpoint bp1 = LineBreakpoint.create("file:///testFile", 10);
        DebuggerManager.getDebuggerManager().addBreakpoint(bp1);
        Lookups.executeWith(getTestLookup(), () -> {
            NIDebugger debugger = NIDebugRunner.start(new File("NIFile"), Arrays.asList("ARG1", "ARG2"), "MI", null, "displayName", null, engine -> {});
            try {
                NIVariable result = debugger.evaluate("breakpoints", null, null);
                assertEquals(1, result.getChildren().length);
                assertEquals("/testFile:10", result.getChildren()[0].getValue());
            } catch (EvaluateException ex) {
                throw new AssertionError(ex.getLocalizedMessage(), ex);
            }
        });
        DebuggerManager.getDebuggerManager().removeBreakpoint(bp1);
    }

    @Test
    public void testVersion() {
        Lookups.executeWith(getTestLookup(), () -> {
            NIDebugger debugger = NIDebugRunner.start(new File("NIFile"), Collections.emptyList(), "MI", null, "displayName", null, engine -> {});
            assertEquals("Test1", debugger.getVersion());
        });
    }
}

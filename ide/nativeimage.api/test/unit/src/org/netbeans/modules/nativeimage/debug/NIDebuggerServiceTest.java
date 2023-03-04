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
package org.netbeans.modules.nativeimage.debug;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class NIDebuggerServiceTest {

    public NIDebuggerServiceTest() {
    }

    private static Lookup getTestLookup(String checkStartParams) {
        Lookup launchCtx = new ProxyLookup(
                        Lookups.fixed(new TestNIDebuggerServiceProvider(checkStartParams)),
                        Lookup.getDefault()
                );
        return launchCtx;
    }

    @Test
    public void testFindServiceProvider() {
        Lookups.executeWith(getTestLookup(""), () -> {
            NIDebugger debugger = NIDebugger.newBuilder().build();
            assertNotNull("Test NI debugger service is not available.", debugger);
        });
    }

    @Test
    public void testDebuggerProvider() {
        String checkStartParams = "[CMD1, CMD2]WDMIDdisplayNamenull{ID1=TEST ID1filePath110truecondition1false}nullnull";
        Lookups.executeWith(getTestLookup(checkStartParams), () -> {
            NIDebugger debugger = NIDebugger.newBuilder().build();
            debugger.addLineBreakpoint("ID1", NILineBreakpointDescriptor.newBuilder("filePath1", 10).enabled(true).condition("condition1").hidden(false).build());
            debugger.addLineBreakpoint("ID2", NILineBreakpointDescriptor.newBuilder("filePath2", 20).enabled(false).condition("condition2").hidden(true).build());
            debugger.removeBreakpoint("ID2");
            CompletableFuture<Void> completed = debugger.start(Arrays.asList("CMD1", "CMD2"), new File("WD"), "MID", "displayName", null, engine -> {});
            assertTrue(completed.isDone());
        });
    }
}

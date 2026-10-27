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

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

public class LogpointReviewTest extends NbTestCase {

    public LogpointReviewTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(LogpointReviewTest.class);
    }

    public void testEarlierLocalHyperlinkKeepsObjectIdentity() throws Exception, Throwable {
        var bp = ExceptionBreakpoint.create("java.lang.IllegalStateException", ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT);
        bp.setPrintText("");
        DebuggerManager.getDebuggerManager().addBreakpoint(bp);
        System.setProperty("test.dir.src", new File("test/unit/src").getAbsolutePath());
        AtomicReference<Throwable> error = new AtomicReference<>();
        ObjectVariable[] captured = {null};
        long[] originalId = {0};
        int[] hits = {0};
        bp.addJPDABreakpointListener(event -> {
            try {
                var output = new BreakpointOutput(event.getDebugger().getSession());
                Object io = event.getDebugger().getClass().getMethod("getConsoleIO").invoke(event.getDebugger());
                Object manager = field(field(io, "output"), "ioManager");
                List<?> buffer = (List<?>) field(manager, "buffer");
                synchronized (buffer) {
                    buffer.clear();
                    output.substituteAndPrintText("REVIEW {locals}", event);
                    hits[0]++;
                    if (hits[0] == 1) {
                        boolean item = false;
                        for (Object text : buffer) {
                            if ("item".equals(field(text, "text"))) {
                                item = true;
                            }
                            Object action = field(text, "action");
                            if (item && action != null) {
                                for (Field f : action.getClass().getDeclaredFields()) {
                                    f.setAccessible(true);
                                    if (f.get(action) instanceof ObjectVariable obj) {
                                        captured[0] = obj;
                                    }
                                }
                                assertNotNull("Hyperlink captures object", captured[0]);
                                originalId[0] = captured[0].getUniqueID();
                                break;
                            }
                        }
                    } else {
                        assertNotNull("First item hyperlink captured", captured[0]);
                        System.out.println("REVIEW hyperlink original=" + originalId[0] + " current=" + captured[0].getUniqueID());
                        assertEquals("Earlier hyperlink should still target the first object", originalId[0], captured[0].getUniqueID());
                    }
                    buffer.clear();
                }
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                event.resume();
            }
        });
        var support = JPDASupport.attach(new String[0], "test.LogpointReviewMain", new String[0], new File[] {
            Utilities.toFile(test.LogpointReviewMain.class.getProtectionDomain().getCodeSource().getLocation().toURI())
        });
        for (;;) {
            support.waitState(JPDADebugger.STATE_STOPPED);
            if (support.getDebugger().getState() == JPDADebugger.STATE_DISCONNECTED) {
                break;
            }
            support.doContinue();
        }
        DebuggerManager.getDebuggerManager().removeBreakpoint(bp);
        if (error.get() instanceof Throwable t) {
            throw t;
        }
        assertEquals("Two loop iterations", 2, hits[0]);
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}

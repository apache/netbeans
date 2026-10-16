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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/** Testing {@link BreakpointOutput#printExpressions}
 */
public class BreakpointOutputTest extends NbTestCase {
    private JPDASupport support;
    private ExceptionBreakpoint breakPoint;


    public BreakpointOutputTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointOutputTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        breakPoint = ExceptionBreakpoint.create(
            "java.lang.IllegalStateException",
            ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT
        );
        DebuggerManager.getDebuggerManager().addBreakpoint(breakPoint);

        final URI cpUrl = test.SampleMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        System.setProperty ("test.dir.src", findSourcePath());
        support = JPDASupport.attach(new String[0], "test.SampleMain", new String[0], new File[] {
            Utilities.toFile(cpUrl)
        });
    }



    public void testBreakpointOutputLinks() throws Throwable {
        final Session session = support.getDebugger().getSession();

        //
        // now the real test and collect the printed messages
        //

        var collected = new StringWriter();
        var writer = new PrintWriter(collected);
        var format = "breakpoint hit {=i}, {=when}, {=arr}, {=obj}";
        BreakpointOutput out = new BreakpointOutput(session);
        assertOnBreakpoint((event) -> {
            writer.append("on ").append(event.getThread().getName()).append("\n");
            try {
                printExpressions(out, event, format, (msg, action) -> {
                    if (action != null) {
                        writer.append("<a ");
                    }
                    writer.append(msg);
                    if (action != null) {
                        writer.append(">");
                    }
                }, (msg) -> {
                    writer.append(msg).append("\n");
                });
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace(writer);
            }
        });

        //
        // verify result
        //
        assertMatches("""
        on main
        breakpoint hit 42, <a "Now">, <a #[0-9]+\\(length=1\\)>, <a class test.SampleMain>
        """, collected.toString());
    }

    public void testArithmeticException() throws Exception {
        //
        // now the real test and collect the printed messages
        //

        var collected = new StringWriter();
        var writer = new PrintWriter(collected);
        var format = "arithmetic exception {=i/0}";
        BreakpointOutput out = new BreakpointOutput(support.getDebugger().getSession());
        assertOnBreakpoint((event) -> {
            writer.append("on ").append(event.getThread().getName()).append("\n");
            try {
                printExpressions(out, event, format, (msg, action) -> {
                    if (action != null) {
                        writer.append("<a ");
                    }
                    writer.append(msg);
                    if (action != null) {
                        writer.append(">");
                    }
                }, (msg) -> {
                    writer.append(msg).append("\n");
                });
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace(writer);
            }
        });

        //
        // verify result
        //
        assertMatches("""
        on main
        arithmetic exception <a \\{=i/0\\}>
          <a During evaluation of 'i/0'>
        """, collected.toString());

    }

    private void assertOnBreakpoint(JPDABreakpointListener listener) {
        int[] cnt = { 0 };
        JPDABreakpointListener countingListener = (event) -> {
            cnt[0]++;
            listener.breakpointReached(event);
        };
        breakPoint.addJPDABreakpointListener(countingListener);

        //
        // launch the debugger and run the test
        //

        for (;;) {
            support.waitState(JPDADebugger.STATE_STOPPED);
            if (support.getDebugger().getState() == JPDADebugger.STATE_DISCONNECTED) {
                break;
            }
            support.doContinue();
        }

        breakPoint.removeJPDABreakpointListener(countingListener);

        assertEquals("Breakpoint has been reached and listener is cleared", 1, cnt[0]);
    }

    private String findSourcePath() throws IOException {
        File curDir = getWorkDir();
        for (;;) {
            var testSrc = new File(new File(new File(curDir, "test"), "unit"), "src");
            if (testSrc.isDirectory()) {
                return testSrc.getPath();
            }
            curDir = curDir.getParentFile();
        }
    }

    /**
     * Calls {@link BreakpointOutput#printExpressions}.
     * Have to use reflection even the class and its tests are in the
     * same package. They are loaded by different classloaders.
     */
    private void printExpressions(
        BreakpointOutput out,
        JPDABreakpointEvent event,
        String message,
        BiConsumer<String, Runnable> print,
        Consumer<String> println
    ) throws ReflectiveOperationException {
        Method m = out.getClass().getDeclaredMethod(
                "printExpressions",
                JPDABreakpointEvent.class, String.class, BiConsumer.class, Consumer.class
        );
        m.setAccessible(true);
        m.invoke(out, event, message, print, println);
    }

    private void assertMatches(String expected, String actual) {
        if (!actual.matches(expected)) {
            fail("Expected: " + expected + " but was: " + actual);
        }
    }
}

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
package org.netbeans.modules.debugger.jpda.ui.values;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;

/**
 * Tests whether a location is propertly found in a Kotlin file.
 */
public class BreakpointInKotlinTest extends NbTestCase {

    private JPDASupport support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager();

    private static final String KOTLIN_HI_NAME = "Hi.kt";
    private static final String CLASS_NAME = "HiKt";
    private static final String CLASS_EXCEPTION_NAME = "java.lang.AssertionError";
    private static final String KOTLIN_HI_SRC = """
    fun main() {
      throw java.lang.AssertionError("Hi from Kotlin")
    }
    """;
    /**
     * Content of a class file to test. File named {@code Hi.kt} with content {@link #KOTLIN_HI_SRC}
     * Compiles into following bytes.
     */
    private static final byte[] KOTLIN_HI_CODE = new byte[]{
        (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x34,
        (byte) 0x00, (byte) 0x26, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x48, (byte) 0x69, (byte) 0x4b,
        (byte) 0x74, (byte) 0x07, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x6a,
        (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2f, (byte) 0x6c, (byte) 0x61, (byte) 0x6e, (byte) 0x67,
        (byte) 0x2f, (byte) 0x4f, (byte) 0x62, (byte) 0x6a, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x07,
        (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x6d, (byte) 0x61, (byte) 0x69,
        (byte) 0x6e, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x28, (byte) 0x29, (byte) 0x56, (byte) 0x01,
        (byte) 0x00, (byte) 0x18, (byte) 0x6a, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2f, (byte) 0x6c,
        (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x2f, (byte) 0x41, (byte) 0x73, (byte) 0x73, (byte) 0x65,
        (byte) 0x72, (byte) 0x74, (byte) 0x69, (byte) 0x6f, (byte) 0x6e, (byte) 0x45, (byte) 0x72, (byte) 0x72,
        (byte) 0x6f, (byte) 0x72, (byte) 0x07, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x0e,
        (byte) 0x48, (byte) 0x69, (byte) 0x20, (byte) 0x66, (byte) 0x72, (byte) 0x6f, (byte) 0x6d, (byte) 0x20,
        (byte) 0x4b, (byte) 0x6f, (byte) 0x74, (byte) 0x6c, (byte) 0x69, (byte) 0x6e, (byte) 0x08, (byte) 0x00,
        (byte) 0x09, (byte) 0x01, (byte) 0x00, (byte) 0x06, (byte) 0x3c, (byte) 0x69, (byte) 0x6e, (byte) 0x69,
        (byte) 0x74, (byte) 0x3e, (byte) 0x01, (byte) 0x00, (byte) 0x15, (byte) 0x28, (byte) 0x4c, (byte) 0x6a,
        (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2f, (byte) 0x6c, (byte) 0x61, (byte) 0x6e, (byte) 0x67,
        (byte) 0x2f, (byte) 0x4f, (byte) 0x62, (byte) 0x6a, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x3b,
        (byte) 0x29, (byte) 0x56, (byte) 0x0c, (byte) 0x00, (byte) 0x0b, (byte) 0x00, (byte) 0x0c, (byte) 0x0a,
        (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x0d, (byte) 0x01, (byte) 0x00, (byte) 0x16, (byte) 0x28,
        (byte) 0x5b, (byte) 0x4c, (byte) 0x6a, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2f, (byte) 0x6c,
        (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x2f, (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x69,
        (byte) 0x6e, (byte) 0x67, (byte) 0x3b, (byte) 0x29, (byte) 0x56, (byte) 0x0c, (byte) 0x00, (byte) 0x05,
        (byte) 0x00, (byte) 0x06, (byte) 0x0a, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x10, (byte) 0x01,
        (byte) 0x00, (byte) 0x04, (byte) 0x61, (byte) 0x72, (byte) 0x67, (byte) 0x73, (byte) 0x01, (byte) 0x00,
        (byte) 0x13, (byte) 0x5b, (byte) 0x4c, (byte) 0x6a, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2f,
        (byte) 0x6c, (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x2f, (byte) 0x53, (byte) 0x74, (byte) 0x72,
        (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x3b, (byte) 0x01, (byte) 0x00, (byte) 0x11, (byte) 0x4c,
        (byte) 0x6b, (byte) 0x6f, (byte) 0x74, (byte) 0x6c, (byte) 0x69, (byte) 0x6e, (byte) 0x2f, (byte) 0x4d,
        (byte) 0x65, (byte) 0x74, (byte) 0x61, (byte) 0x64, (byte) 0x61, (byte) 0x74, (byte) 0x61, (byte) 0x3b,
        (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x6d, (byte) 0x76, (byte) 0x03, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x03,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x6b,
        (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x78, (byte) 0x69, (byte) 0x03, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x30, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x64, (byte) 0x31, (byte) 0x01,
        (byte) 0x00, (byte) 0x13, (byte) 0xc0, (byte) 0x80, (byte) 0x06, (byte) 0x0a, (byte) 0xc0, (byte) 0x80,
        (byte) 0x0a, (byte) 0x02, (byte) 0x10, (byte) 0x02, (byte) 0x1a, (byte) 0x06, (byte) 0x10, (byte) 0xc0,
        (byte) 0x80, (byte) 0x1a, (byte) 0x02, (byte) 0x30, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x02,
        (byte) 0x64, (byte) 0x32, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x05,
        (byte) 0x48, (byte) 0x69, (byte) 0x2e, (byte) 0x6b, (byte) 0x74, (byte) 0x01, (byte) 0x00, (byte) 0x04,
        (byte) 0x43, (byte) 0x6f, (byte) 0x64, (byte) 0x65, (byte) 0x01, (byte) 0x00, (byte) 0x0f, (byte) 0x4c,
        (byte) 0x69, (byte) 0x6e, (byte) 0x65, (byte) 0x4e, (byte) 0x75, (byte) 0x6d, (byte) 0x62, (byte) 0x65,
        (byte) 0x72, (byte) 0x54, (byte) 0x61, (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x01, (byte) 0x00,
        (byte) 0x12, (byte) 0x4c, (byte) 0x6f, (byte) 0x63, (byte) 0x61, (byte) 0x6c, (byte) 0x56, (byte) 0x61,
        (byte) 0x72, (byte) 0x69, (byte) 0x61, (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x54, (byte) 0x61,
        (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x01, (byte) 0x00, (byte) 0x0a, (byte) 0x53, (byte) 0x6f,
        (byte) 0x75, (byte) 0x72, (byte) 0x63, (byte) 0x65, (byte) 0x46, (byte) 0x69, (byte) 0x6c, (byte) 0x65,
        (byte) 0x01, (byte) 0x00, (byte) 0x19, (byte) 0x52, (byte) 0x75, (byte) 0x6e, (byte) 0x74, (byte) 0x69,
        (byte) 0x6d, (byte) 0x65, (byte) 0x56, (byte) 0x69, (byte) 0x73, (byte) 0x69, (byte) 0x62, (byte) 0x6c,
        (byte) 0x65, (byte) 0x41, (byte) 0x6e, (byte) 0x6e, (byte) 0x6f, (byte) 0x74, (byte) 0x61, (byte) 0x74,
        (byte) 0x69, (byte) 0x6f, (byte) 0x6e, (byte) 0x73, (byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x02,
        (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
        (byte) 0x00, (byte) 0x19, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x21, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x03,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0xbb, (byte) 0x00,
        (byte) 0x08, (byte) 0x59, (byte) 0x12, (byte) 0x0a, (byte) 0xb7, (byte) 0x00, (byte) 0x0e, (byte) 0xbf,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
        (byte) 0x10, (byte) 0x09, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x0f, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x21, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x22, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xb8, (byte) 0x00,
        (byte) 0x11, (byte) 0xb1, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x23,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x13, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
        (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x25, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x31,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x15,
        (byte) 0x5b, (byte) 0x00, (byte) 0x03, (byte) 0x49, (byte) 0x00, (byte) 0x16, (byte) 0x49, (byte) 0x00,
        (byte) 0x17, (byte) 0x49, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x19, (byte) 0x49, (byte) 0x00,
        (byte) 0x16, (byte) 0x00, (byte) 0x1a, (byte) 0x49, (byte) 0x00, (byte) 0x1b, (byte) 0x00, (byte) 0x1c,
        (byte) 0x5b, (byte) 0x00, (byte) 0x01, (byte) 0x73, (byte) 0x00, (byte) 0x1d, (byte) 0x00, (byte) 0x1e,
        (byte) 0x5b, (byte) 0x00, (byte) 0x02, (byte) 0x73, (byte) 0x00, (byte) 0x05, (byte) 0x73, (byte) 0x00,
        (byte) 0x1f
    };
    private File src;

    public BreakpointInKotlinTest(String s) {
        super(s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointInKotlinTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        var dir = getWorkDir();
        var clazz = new File(dir, CLASS_NAME + ".class");
        Files.write(clazz.toPath(), KOTLIN_HI_CODE);
        src = new File(dir, KOTLIN_HI_NAME);
        Files.write(src.toPath(), KOTLIN_HI_SRC.getBytes());
    }

    @Override
    protected void tearDown() throws Exception {
        if (support != null) {
            support.doFinish(1);
        }
    }

    public void testBreakpointInHiKtSource() throws Throwable {
        ExceptionBreakpoint eb1 = ExceptionBreakpoint.create(CLASS_EXCEPTION_NAME,
                ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT
        );
        TestBreakpointListener tbl = new TestBreakpointListener(
                CLASS_EXCEPTION_NAME,
                eb1,
                1
        );
        eb1.addJPDABreakpointListener(tbl);
        dm.addBreakpoint(eb1);

        System.setProperty ("test.dir.src", getWorkDirPath());
        support = JPDASupport.attach(new String[0], CLASS_NAME, new String[0], new File[]{ getWorkDir() });

        for (;;) {
            support.waitState(JPDADebugger.STATE_STOPPED);
            if (support.getDebugger().getState() == JPDADebugger.STATE_DISCONNECTED) {
                break;
            }
            support.doContinue();
        }
        tbl.assertOneHit();

        dm.removeBreakpoint(eb1);
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int hitCount;
        private Throwable failure;
        private String exceptionClass;
        private ExceptionBreakpoint bpt;

        public TestBreakpointListener(
                String exceptionClass,
                ExceptionBreakpoint bpt,
                int expectedHitCount
        ) {
            this.exceptionClass = exceptionClass;
            this.bpt = bpt;
        }

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            try {
                checkEvent(event);
            } catch (Throwable e) {
                failure = e;
            }
        }

        private void checkEvent(JPDABreakpointEvent event) throws Exception {
            assertEquals("Expecting only one hit", 1, ++hitCount);
            assertEquals("at right class", CLASS_NAME, event.getReferenceType().name());
            assertEquals("It is the right error type", exceptionClass,event.getVariable().getType());
            assertSame("It is exceptional breakpoint", bpt, event.getSource());
            assertNotNull("There is a thread", event.getThread());
            CallStackFrame[] stack = event.getThread().getCallStack();
            assertEquals("There are the stack frames: " + Arrays.toString(stack), 2, stack.length);
            var topmost = stack[0].getLineNumber(null);
            var initial = stack[1].getLineNumber(null);
            assertEquals("Hi.main:2", 2, topmost);
            assertEquals("Hi.main & no line", -1, initial);

            //
            // now the PR-9433 bugfix test
            //

            var sourcePath = new SourcePath(dm.getCurrentSession()) {
                private String showUrl;
                private int showLine;


                @Override
                protected void handleShowSource(String url, int lineNumber, Runnable onFailure) {
                    assertNull("Only one URL to open", showUrl);
                    assertNotNull("New URL is provided", url);
                    showUrl = url;
                    showLine = lineNumber;
                    assertNull("No callback right now in the test", onFailure);
                }

            };
            sourcePath.showSource(stack[0], null);

            assertNotNull("Opening a URL", sourcePath.showUrl);
            assertEquals("Line provided", topmost, sourcePath.showLine);
        }

        public void assertOneHit() throws Throwable {
            if (failure != null) {
                throw failure;
            }
            assertEquals("Expecting one hit", 1, hitCount);
        }
    }
}

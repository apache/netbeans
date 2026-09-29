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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests whether a location is propertly found in a Kotlin file.
 */
public class BreakpointInKotlinPackageTest extends NbTestCase {

    private JPDASupport support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager();

    private static final String KOTLIN_HI_NAME = "Hi.kt";
    private static final String CLASS_PKG = "test";
    private static final String CLASS_NAME = "HiKt";
    private static final String CLASS_EXCEPTION_NAME = "java.lang.AssertionError";
    private static final String KOTLIN_HI_SRC = """
    package test

    inline fun boom(msg: String): Nothing {
      throw java.lang.AssertionError(msg)
    }

    fun main() {
      boom("Hi from Kotlin")
    }

    """;
    /**
     * Content of a class file to test. File named {@code Hi.kt} with content {@link #KOTLIN_HI_SRC}
     * Compiles into following bytes.
     */
    private static final String KOTLIN_HI_CODE = """
        yv66vgAAADQAOwEACXRlc3QvSGlLdAcAAQEAEGphdmEvbGFuZy9PYmplY3QHAAMBAARib29tAQAk
        KExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1ZvaWQ7AQAjTG9yZy9qZXRicmFpbnMvYW5u
        b3RhdGlvbnMvTm90TnVsbDsBAANtc2cIAAgBAB5rb3RsaW4vanZtL2ludGVybmFsL0ludHJpbnNp
        Y3MHAAoBABVjaGVja05vdE51bGxQYXJhbWV0ZXIBACcoTGphdmEvbGFuZy9PYmplY3Q7TGphdmEv
        bGFuZy9TdHJpbmc7KVYMAAwADQoACwAOAQAYamF2YS9sYW5nL0Fzc2VydGlvbkVycm9yBwAQAQAG
        PGluaXQ+AQAVKExqYXZhL2xhbmcvT2JqZWN0OylWDAASABMKABEAFAEACSRpJGYkYm9vbQEAAUkB
        ABJMamF2YS9sYW5nL1N0cmluZzsBAARtYWluAQADKClWAQAOSGkgZnJvbSBLb3RsaW4IABsBAAZt
        c2ckaXYBABYoW0xqYXZhL2xhbmcvU3RyaW5nOylWDAAZABoKAAIAHwEABGFyZ3MBABNbTGphdmEv
        bGFuZy9TdHJpbmc7AQARTGtvdGxpbi9NZXRhZGF0YTsBAAJtdgMAAAACAwAAAAQDAAAAAAEAAWsB
        AAJ4aQMAAAAwAQACZDEBADXAgBIKwIAKAhABCsCACgIQDgrAgAoCEAIaERDAgBoCMAEyBhACGgIw
        A0jChggaBhAEGgIwBQEAAmQyAQAAAQAFSGkua3QBACpMa290bGluL2p2bS9pbnRlcm5hbC9Tb3Vy
        Y2VEZWJ1Z0V4dGVuc2lvbjsBAAV2YWx1ZQEAeVNNQVAKSGkua3QKS290bGluCipTIEtvdGxpbgoq
        RgorIDEgSGkua3QKdGVzdC9IaUt0CipMCjEjMSwxNDoxCjQjMToxNQoqUyBLb3RsaW5EZWJ1Zwoq
        RgorIDEgSGkua3QKdGVzdC9IaUt0CipMCjgjMToxNQoqRQoBAARDb2RlAQAPTGluZU51bWJlclRh
        YmxlAQASTG9jYWxWYXJpYWJsZVRhYmxlAQAbUnVudGltZUludmlzaWJsZUFubm90YXRpb25zAQAk
        UnVudGltZUludmlzaWJsZVBhcmFtZXRlckFubm90YXRpb25zAQAKU291cmNlRmlsZQEAFFNvdXJj
        ZURlYnVnRXh0ZW5zaW9uAQAZUnVudGltZVZpc2libGVBbm5vdGF0aW9ucwAxAAIABAAAAAAAAwAZ
        AAUABgADADMAAABFAAMAAgAAABEqEgm4AA8DPLsAEVkqtwAVvwAAAAIANAAAAAYAAQAIAAQANQAA
        ABYAAgAIAAkAFgAXAAEAAAARAAgAGAAAADYAAAAGAAEABwAAADcAAAAHAQABAAcAAAAZABkAGgAB
        ADMAAABGAAMAAgAAAA4SHEsDPLsAEVkqtwAVvwAAAAIANAAAAAoAAgAAAAgABQAPADUAAAAWAAIA
        BQAJABYAFwABAAMACwAdABgAABAJABkAHgABADMAAAAiAAAAAQAAAAS4ACCxAAAAAQA1AAAADAAB
        AAAABAAhACIAAAAEADgAAAACAC8AOQAAAHlTTUFQCkhpLmt0CktvdGxpbgoqUyBLb3RsaW4KKkYK
        KyAxIEhpLmt0CnRlc3QvSGlLdAoqTAoxIzEsMTQ6MQo0IzE6MTUKKlMgS290bGluRGVidWcKKkYK
        KyAxIEhpLmt0CnRlc3QvSGlLdAoqTAo4IzE6MTUKKkUKADoAAAA9AAEAIwAFACRbAANJACVJACZJ
        ACcAKEkAJQApSQAqACtbAAFzACwALVsABnMABXMALnMACHMALnMAGXMALgA2AAAADgABADAAAQAx
        WwABcwAy
        """;
    private File src;

    public BreakpointInKotlinPackageTest(String s) {
        super(s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointInKotlinPackageTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        var dir = getWorkDir();
        var clazz = new File(new File(dir, CLASS_PKG), CLASS_NAME + ".class");
        clazz.getParentFile().mkdirs();
        var kotlinHiBytes = Base64.getMimeDecoder().decode(KOTLIN_HI_CODE);
        Files.write(clazz.toPath(), kotlinHiBytes);
        boolean inPackage = getName().contains("TestHiKt");
        src = inPackage ?
                new File(new File(dir, CLASS_PKG), KOTLIN_HI_NAME)
                :
                new File(dir, KOTLIN_HI_NAME);
        src.getParentFile().mkdirs();
        Files.write(src.toPath(), KOTLIN_HI_SRC.getBytes());
        assertTrue("Source file created", src.isFile());
    }

    @Override
    protected void tearDown() throws Exception {
        if (support != null) {
            support.doFinish(1);
        }
    }

    public void testBreakpointInHiKtSource() throws Throwable {
        doBreakpointCheck();
    }

    public void testBreakpointInTestHiKtSource() throws Throwable {
        doBreakpointCheck();
    }

    private void doBreakpointCheck() throws Throwable {
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
        support = JPDASupport.attach(new String[0], CLASS_PKG + "." + CLASS_NAME, new String[0], new File[]{ getWorkDir() });

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
            assertEquals("at right class", CLASS_PKG + "." + CLASS_NAME, event.getReferenceType().name());
            assertEquals("It is the right error type", exceptionClass,event.getVariable().getType());
            assertSame("It is exceptional breakpoint", bpt, event.getSource());
            assertNotNull("There is a thread", event.getThread());
            CallStackFrame[] stack = event.getThread().getCallStack();
            assertEquals("There are the stack frames: " + Arrays.toString(stack), 2, stack.length);
            var topmost = stack[0].getLineNumber(null);
            var initial = stack[1].getLineNumber(null);
            assertEquals("Hi.main:4", 4, topmost);
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

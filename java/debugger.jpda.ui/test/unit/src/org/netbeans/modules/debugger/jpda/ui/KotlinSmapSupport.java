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
import java.util.Base64;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Debugs a Kotlin class compiled with an inline call, so it carries an SMAP
 * with strata {@code [Java, Kotlin, KotlinDebug]} (default {@code Kotlin}).
 * Where the exception is thrown the {@code Java} line is 9 (synthetic),
 * the {@code Kotlin} line is 2 and the {@code KotlinDebug} line is 6.
 * The {@code Kotlin} stratum's source path is {@code HiKt} (a class name),
 * its source name is {@code Hi.kt}.
 */
abstract class KotlinSmapSupport extends NbTestCase {
    static final String SRC = """
    inline fun boom(msg: String): Nothing {
      throw java.lang.AssertionError(msg)
    }

    fun main() {
      boom("Hi from Kotlin")
    }
    """;
    // kotlinc 2.4.20
    private static final String CLASS_B64 = """
    yv66vgAAADQAOwEABEhpS3QHAAEBABBqYXZhL2xhbmcvT2JqZWN0BwADAQAEYm9vbQEAJChMamF2
    YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Wb2lkOwEAI0xvcmcvamV0YnJhaW5zL2Fubm90YXRp
    b25zL05vdE51bGw7AQADbXNnCAAIAQAea290bGluL2p2bS9pbnRlcm5hbC9JbnRyaW5zaWNzBwAK
    AQAVY2hlY2tOb3ROdWxsUGFyYW1ldGVyAQAnKExqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL2xhbmcv
    U3RyaW5nOylWDAAMAA0KAAsADgEAGGphdmEvbGFuZy9Bc3NlcnRpb25FcnJvcgcAEAEABjxpbml0
    PgEAFShMamF2YS9sYW5nL09iamVjdDspVgwAEgATCgARABQBAAkkaSRmJGJvb20BAAFJAQASTGph
    dmEvbGFuZy9TdHJpbmc7AQAEbWFpbgEAAygpVgEADkhpIGZyb20gS290bGluCAAbAQAGbXNnJGl2
    AQAWKFtMamF2YS9sYW5nL1N0cmluZzspVgwAGQAaCgACAB8BAARhcmdzAQATW0xqYXZhL2xhbmcv
    U3RyaW5nOwEAEUxrb3RsaW4vTWV0YWRhdGE7AQACbXYDAAAAAgMAAAAEAwAAAAABAAFrAQACeGkD
    AAAAMAEAAmQxAQA1wIASCsCACgIQAQrAgAoCEA4KwIAKAhACGhEQwIAaAjABMgYQAhoCMANIwoYI
    GgYQBBoCMAUBAAJkMgEAAAEABUhpLmt0AQAqTGtvdGxpbi9qdm0vaW50ZXJuYWwvU291cmNlRGVi
    dWdFeHRlbnNpb247AQAFdmFsdWUBAGxTTUFQCkhpLmt0CktvdGxpbgoqUyBLb3RsaW4KKkYKKyAx
    IEhpLmt0CkhpS3QKKkwKMSMxLDg6MQoyIzE6OQoqUyBLb3RsaW5EZWJ1ZwoqRgorIDEgSGkua3QK
    SGlLdAoqTAo2IzE6OQoqRQoBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQASTG9jYWxWYXJpYWJs
    ZVRhYmxlAQAbUnVudGltZUludmlzaWJsZUFubm90YXRpb25zAQAkUnVudGltZUludmlzaWJsZVBh
    cmFtZXRlckFubm90YXRpb25zAQAKU291cmNlRmlsZQEAFFNvdXJjZURlYnVnRXh0ZW5zaW9uAQAZ
    UnVudGltZVZpc2libGVBbm5vdGF0aW9ucwAxAAIABAAAAAAAAwAZAAUABgADADMAAABFAAMAAgAA
    ABEqEgm4AA8DPLsAEVkqtwAVvwAAAAIANAAAAAYAAQAIAAIANQAAABYAAgAIAAkAFgAXAAEAAAAR
    AAgAGAAAADYAAAAGAAEABwAAADcAAAAHAQABAAcAAAAZABkAGgABADMAAABGAAMAAgAAAA4SHEsD
    PLsAEVkqtwAVvwAAAAIANAAAAAoAAgAAAAYABQAJADUAAAAWAAIABQAJABYAFwABAAMACwAdABgA
    ABAJABkAHgABADMAAAAiAAAAAQAAAAS4ACCxAAAAAQA1AAAADAABAAAABAAhACIAAAAEADgAAAAC
    AC8AOQAAAGxTTUFQCkhpLmt0CktvdGxpbgoqUyBLb3RsaW4KKkYKKyAxIEhpLmt0CkhpS3QKKkwK
    MSMxLDg6MQoyIzE6OQoqUyBLb3RsaW5EZWJ1ZwoqRgorIDEgSGkua3QKSGlLdAoqTAo2IzE6OQoq
    RQoAOgAAAD0AAQAjAAUAJFsAA0kAJUkAJkkAJwAoSQAlAClJACoAK1sAAXMALAAtWwAGcwAFcwAu
    cwAIcwAucwAZcwAuADYAAAAOAAEAMAABADFbAAFzADI=
    """;

    private final DebuggerManager dm = DebuggerManager.getDebuggerManager();

    KotlinSmapSupport(String name) {
        super(name);
    }

    interface FrameCheck {
        void check(CallStackFrame frame) throws Exception;
    }

    /**
     * Runs {@code HiKt} with {@link #SRC} stored as {@code sourceFile} and
     * passes the frame that throws the {@code AssertionError} to {@code check}.
     */
    final void debug(String sourceFile, FrameCheck check) throws Throwable {
        clearWorkDir();
        File dir = getWorkDir();
        Files.write(new File(dir, "HiKt.class").toPath(), Base64.getMimeDecoder().decode(CLASS_B64));
        Files.writeString(new File(dir, sourceFile).toPath(), SRC);

        Throwable[] failure = { null };
        int[] hits = { 0 };
        ExceptionBreakpoint eb = ExceptionBreakpoint.create("java.lang.AssertionError",
                ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT);
        eb.addJPDABreakpointListener(new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                hits[0]++;
                try {
                    check.check(event.getThread().getCallStack()[0]);
                } catch (Throwable t) {
                    failure[0] = t;
                }
            }
        });
        dm.addBreakpoint(eb);
        String prevSrc = System.getProperty("test.dir.src");
        System.setProperty("test.dir.src", getWorkDirPath());
        JPDASupport support = null;
        try {
            support = JPDASupport.attach(new String[0], "HiKt", new String[0], new File[]{ dir });
            for (;;) {
                support.waitState(JPDADebugger.STATE_STOPPED);
                if (support.getDebugger().getState() == JPDADebugger.STATE_DISCONNECTED) {
                    break;
                }
                support.doContinue();
            }
        } finally {
            dm.removeBreakpoint(eb);
            if (support != null) {
                support.doFinish(1);
            }
            if (prevSrc == null) {
                System.clearProperty("test.dir.src");
            } else {
                System.setProperty("test.dir.src", prevSrc);
            }
        }
        if (failure[0] != null) {
            throw failure[0];
        }
        assertEquals("Breakpoint hit once", 1, hits[0]);
    }

    /** @return {@code "<file name>:<line>"} opened by {@link SourcePath#showSource(CallStackFrame, String)} */
    final String showSource(CallStackFrame frame, String stratum) {
        String[] opened = { null };
        new SourcePath(dm.getCurrentSession()) {
            @Override
            protected void handleShowSource(String url, int lineNumber, Runnable onFailure) {
                assertNull("Only one URL to open", opened[0]);
                opened[0] = fileName(url) + ":" + lineNumber;
            }
        }.showSource(frame, stratum);
        return opened[0];
    }

    /** @return file name of {@link SourcePath#getURL(CallStackFrame, String)} or {@code null} */
    final String getURL(CallStackFrame frame, String stratum) {
        return fileName(new SourcePath(dm.getCurrentSession()).getURL(frame, stratum));
    }

    private static String fileName(String url) {
        return url == null ? null : url.substring(url.lastIndexOf('/') + 1);
    }
}

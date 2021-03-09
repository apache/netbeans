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
package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import junit.framework.Test;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public abstract class AbstractDebugTest extends NbTestCase {

    protected DebuggerEngine engine;
    protected CPPLiteDebugger debugger;

    private final int[] suspendCount = new int[]{0};
    private final int[] resumeCount = new int[]{0};

    protected AbstractDebugTest(String s) {
        super(s);
    }

    protected final static void compileC(String name, File wd) throws IOException, InterruptedException {
        Process compile = new ProcessBuilder("gcc", "-o", name, "-g", name + ".c").directory(wd).start();
        assertEquals(0, compile.waitFor());
    }

    protected final static void compileCPP(String name, File wd) throws IOException, InterruptedException {
        Process compile = new ProcessBuilder("g++", "-o", name, "-g", name + ".cpp").directory(wd).start();
        assertEquals(0, compile.waitFor());
    }

    protected final void startDebugging(String name, File wd) throws IOException {
        engine = CPPLiteDebugger.startDebugging(new CPPLiteDebuggerConfig(Arrays.asList(new File(wd, name).getAbsolutePath()), wd)).first();
        debugger = engine.lookupFirst(null, CPPLiteDebugger.class);
        debugger.addStateListener(new CPPLiteDebugger.StateListener() {
            @Override
            public void suspended(boolean suspended) {
                int[] count;
                if (suspended) {
                    count = suspendCount;
                } else {
                    count = resumeCount;
                }
                synchronized (count) {
                    count[0]++;
                    count.notifyAll();
                }
            }

            @Override
            public void finished() {
            }

            @Override
            public void currentThread(CPPThread thread) {
            }

            @Override
            public void currentFrame(CPPFrame frame) {
            }
        });
    }

    protected final void waitSuspended(int count) throws InterruptedException {
        synchronized (suspendCount) {
            while (suspendCount[0] < count) {
                suspendCount.wait();
            }
        }
    }

    protected final void waitResumed(int count) throws InterruptedException {
        synchronized (resumeCount) {
            while (resumeCount[0] < count) {
                resumeCount.wait();
            }
        }
    }

    protected final void assertStoppedAt(URI file, int line) {
        CPPFrame currentFrame = debugger.getCurrentFrame();
        assertNotNull(currentFrame);
        assertEquals(file, currentFrame.getSourceURI());
        assertEquals(line, currentFrame.getLine());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration().gui(false).suite();
    }
}

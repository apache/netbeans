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
package org.netbeans.modules.cpplite.debugger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class AbstractDebugTest extends NbTestCase {

    protected DebuggerEngine engine;
    protected CPPLiteDebugger debugger;
    protected Process process;
    protected StringBuffer stdOut;
    protected StringBuffer stdErr;

    private final int[] suspendCount = new int[]{0};
    private final int[] resumeCount = new int[]{0};

    protected AbstractDebugTest(String s) {
        super(s);
    }

    protected static final void createSourceFile(String fileName, File wd, String content) throws IOException {
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "main.cpp");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append(content);
        }
    }

    protected static final void compileC(String name, File wd) throws IOException, InterruptedException {
        Process compile = new ProcessBuilder("gcc", "-o", name, "-g", name + ".c").directory(wd).start();
        assertEquals(0, compile.waitFor());
    }

    protected static final void compileCPP(String name, File wd) throws IOException, InterruptedException {
        Process compile = new ProcessBuilder("g++", "-o", name, "-g", name + ".cpp").directory(wd).start();
        assertEquals(0, compile.waitFor());
    }

    protected final void startDebugging(String name, File wd) throws IOException {
        ExplicitProcessParameters processParameters = ExplicitProcessParameters.builder().workingDirectory(wd).build();
        startDebugging(name, wd, processParameters);
    }

    protected final void startDebugging(String name, File wd, ExplicitProcessParameters processParameters) throws IOException {
        startDebugging(Arrays.asList(new File(wd, name).getAbsolutePath()), processParameters);
    }

    protected final void startDebugging(List<String> executable, ExplicitProcessParameters processParameters) throws IOException {
        this.process = CPPLiteDebugger.startDebugging(
                new CPPLiteDebuggerConfig(executable, processParameters, true, null, "gdb"),
                engine -> this.engine = engine);
        stdOut = outputFrom(process.getInputStream());
        stdErr = outputFrom(process.getErrorStream());
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
        debugger.execRun();
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

    protected boolean isAppProcessAlive() {
        return process.isAlive();
    }

    protected final int waitAppProcessExit() throws InterruptedException {
        return process.waitFor();
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

    private static StringBuffer outputFrom(InputStream inputStream) {
        StringBuffer buffer = new StringBuffer();
        new Thread("Process output") {
            @Override
            public void run() {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        buffer.append(line);
                        buffer.append('\n');
                    }
                } catch (IOException ex) {
                    buffer.append(ex);
                }
            }
        }.start();
        return buffer;
    }
}

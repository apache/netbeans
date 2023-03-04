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
package org.netbeans.modules.java.nativeimage.debugger.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.java.nativeimage.debugger.breakpoints.JPDABreakpointsHandler;
import org.netbeans.modules.java.nativeimage.debugger.displayer.JavaFrameDisplayer;
import org.netbeans.modules.java.nativeimage.debugger.displayer.JavaVariablesDisplayer;
import org.netbeans.modules.nativeimage.api.debug.StartDebugParameters;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Runs debugger with Java translations on a native image.
 *
 * @author martin
 */
public final class NIDebugRunner {

    private NIDebugRunner() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts Native Image debugger.
     *
     * @param niFile Native Image file
     * @param arguments a list of arguments when executing the native image
     * @param debuggerCommand the debugger command
     * @param project a project associated with the native image, or <code>null</code>
     * @param displayName display name of the execution
     * @param executionDescriptor execution descriptor
     * @param startedEngine consumer of the started {@link DebuggerEngine}.
     * @return an instance of {@link NIDebugger}.
     * @throws IllegalStateException when the native debugger is not available.
     * @deprecated Use {@link #start(File, StartDebugParameters, Project, Consumer)} instead.
     */
    @Deprecated
    public static NIDebugger start(File niFile, List<String> arguments, String debuggerCommand, Project project, String displayName, ExecutionDescriptor executionDescriptor, Consumer<DebuggerEngine> startedEngine) throws IllegalStateException {
        JavaVariablesDisplayer variablesDisplayer = new JavaVariablesDisplayer();
        JavaFrameDisplayer frameDisplayer = new JavaFrameDisplayer(project);
        NIDebugger debugger = NIDebugger.newBuilder()
                .frameDisplayer(frameDisplayer)
                .variablesDisplayer(variablesDisplayer)
                .build();
        variablesDisplayer.setDebugger(debugger);
        JPDABreakpointsHandler breakpointsHandler = new JPDABreakpointsHandler(niFile, debugger);
        File workingDirectory = new File(System.getProperty("user.dir"));
        List<String> command = arguments.isEmpty() ? Collections.singletonList(niFile.getAbsolutePath()) : join(niFile.getAbsolutePath(), arguments);
        DialogDisplayer displayer = DialogDisplayer.getDefault(); // The launcher might provide a special displayer in the lookup. This is why we grab it eagerly.
        debugger.start(
                command,
                workingDirectory,
                debuggerCommand,
                COMMAND_DEBUG + " " + niFile.getName(),
                executionDescriptor,
                (engine) -> {
                    if (startedEngine != null) {
                        startedEngine.accept(engine);
                    }
                }).thenRun(() -> {
                    breakpointsHandler.dispose();
                });
        checkVersion(debugger.getVersion(), displayer);
        return debugger;
    }

    public static NIDebugger start(File niFile, StartDebugParameters debugParameters, Project project, Consumer<DebuggerEngine> startedEngine) throws IllegalStateException {
        JavaVariablesDisplayer variablesDisplayer = new JavaVariablesDisplayer();
        JavaFrameDisplayer frameDisplayer = new JavaFrameDisplayer(project);
        NIDebugger debugger = NIDebugger.newBuilder()
                .frameDisplayer(frameDisplayer)
                .variablesDisplayer(variablesDisplayer)
                .build();
        variablesDisplayer.setDebugger(debugger);
        JPDABreakpointsHandler breakpointsHandler = new JPDABreakpointsHandler(niFile, debugger);
        DialogDisplayer displayer = DialogDisplayer.getDefault(); // The launcher might provide a special displayer in the lookup. This is why we grab it eagerly.
        debugger.start(debugParameters,
                (engine) -> {
                    if (startedEngine != null) {
                        startedEngine.accept(engine);
                    }
                }).thenRun(() -> {
                    breakpointsHandler.dispose();
                });
        checkVersion(debugger.getVersion(), displayer);
        return debugger;
    }

    /**
     * Attach debugger to a Native Image.
     *
     * @param niFile Native Image file
     * @param processId a process to attach to
     * @param debuggerCommand the debugger command
     * @param project a project associated with the native image, or <code>null</code>
     * @param startedEngine consumer of the started {@link DebuggerEngine}.
     * @return an instance of {@link NIDebugger}.
     * @throws IllegalStateException when the native debugger is not available.
     * @since 0.3
     * @deprecated Use {@link #start(File, StartDebugParameters, Project, Consumer) and set {@link StartDebugParameters.Builder#processID(long)}.
     */
    @Deprecated
    public static NIDebugger attach(File niFile, long processId, String debuggerCommand, Project project, Consumer<DebuggerEngine> startedEngine) throws IllegalStateException {
        JavaVariablesDisplayer variablesDisplayer = new JavaVariablesDisplayer();
        JavaFrameDisplayer frameDisplayer = new JavaFrameDisplayer(project);
        NIDebugger debugger = NIDebugger.newBuilder()
                .frameDisplayer(frameDisplayer)
                .variablesDisplayer(variablesDisplayer)
                .build();
        variablesDisplayer.setDebugger(debugger);
        JPDABreakpointsHandler breakpointsHandler = new JPDABreakpointsHandler(niFile, debugger);
        DialogDisplayer displayer = DialogDisplayer.getDefault(); // The launcher might provide a special displayer in the lookup. This is why we grab it eagerly.
        CompletableFuture<Void> future = debugger.attach(
                niFile.getAbsolutePath(),
                processId,
                debuggerCommand,
                (engine) -> {
                    if (startedEngine != null) {
                        startedEngine.accept(engine);
                    }
                }).thenRun(() -> {
                    breakpointsHandler.dispose();
                });
        if (future.isDone()) {
            throw new IllegalStateException("Failed to attach.");
        }
        checkVersion(debugger.getVersion(), displayer);
        return debugger;
    }

    @NbBundle.Messages("MSG_GDBVersionBug=gdb bug #26139 will affect the debugging.\nWe recommend to upgrade to version 10.1 or newer.")
    private static void checkVersion(String version, DialogDisplayer displayer) {
        String gdbVersion = "GNU gdb";
        int i = version.indexOf(gdbVersion);
        if (i >= 0) {
            i += gdbVersion.length();
            i = skipParanthesis(version, i);
            int eol = version.indexOf("\\n", i);
            if (eol > 0) {
                String v = version.substring(i, eol).trim();
                if (v.startsWith("8.") && !v.startsWith("8.0") || v.startsWith("9.")) {
                    NotifyDescriptor descriptor = new NotifyDescriptor.Message(Bundle.MSG_GDBVersionBug(), NotifyDescriptor.WARNING_MESSAGE);
                    displayer.notifyLater(descriptor);
                }
            }
        }
    }

    private static int skipParanthesis(String str, int i) {
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        if (i < str.length() && '(' == str.charAt(i)) {
            int p = str.indexOf(')', i + 1);
            if (p > 0) {
                i = p + 1;
            }
        }
        return i;
    }

    private static List<String> join(String first, List<String> next) {
        List<String> joined = new ArrayList<>(next.size() + 1);
        joined.add(first);
        joined.addAll(next);
        return joined;
    }

}

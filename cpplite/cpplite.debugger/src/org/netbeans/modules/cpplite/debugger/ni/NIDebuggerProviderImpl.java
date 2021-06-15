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
package org.netbeans.modules.cpplite.debugger.ni;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebuggerConfig;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;

import org.openide.LifecycleManager;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;

/**
 *
 * @author martin
 */
public class NIDebuggerProviderImpl implements NIDebuggerProvider {

    private final NIBreakpoints breakpointsHandler = new NIBreakpoints();
    private volatile CPPLiteDebugger debugger;
    private volatile FrameDisplayer frameDisplayer;
    private volatile VariableDisplayer variablesDisplayer;
    private final RequestProcessor varDisplayerRP = new RequestProcessor(NIDebuggerProvider.class);

    public NIDebuggerProviderImpl() {
    }

    @Override
    public Breakpoint addLineBreakpoint(Object id, NILineBreakpointDescriptor breakpointDescriptor) {
        return breakpointsHandler.addLineBreakpoint(id, breakpointDescriptor);
    }

    @Override
    public void removeBreakpoint(Object id) {
        breakpointsHandler.removeBreakpoint(id);
    }

    @Override
    public void setFrameDisplayer(FrameDisplayer frameDisplayer) {
        this.frameDisplayer = frameDisplayer;
    }

    @Override
    public void setVariablesDisplayer(VariableDisplayer variablesDisplayer) {
        this.variablesDisplayer = variablesDisplayer;
    }

    @Override
    public CompletableFuture<Void> start(List<String> command, File workingDirectory, String miDebugger, String displayName, ExecutionDescriptor executionDescriptor, Consumer<DebuggerEngine> startedEngine) {
        if (debugger != null) {
            throw new IllegalStateException("Debugger has started already.");
        }
        if (executionDescriptor == null) {
            executionDescriptor = new ExecutionDescriptor()
                .showProgress(true)
                .showSuspended(true)
                .frontWindowOnError(true)
                .controllable(true);
        }
        CompletableFuture<Void> completed = new CompletableFuture<>();
        Semaphore started = new Semaphore(0);
        ExecutionService.newService(() -> {
            Pair<DebuggerEngine, Process> engineProcess;
            CPPLiteDebugger debugger;
            try {
                LifecycleManager.getDefault().saveAll();
                engineProcess = CPPLiteDebugger.startDebugging(
                        new CPPLiteDebuggerConfig(command, workingDirectory, miDebugger),
                        frameDisplayer,
                        variablesDisplayer);
                DebuggerEngine engine = engineProcess.first();
                debugger = engine.lookupFirst(null, CPPLiteDebugger.class);
                this.debugger = debugger;
                if (startedEngine != null) {
                    startedEngine.accept(engine);
                }
                debugger.addStateListener(new CPPLiteDebugger.StateListener() {
                    @Override
                    public void currentThread(CPPThread thread) {}

                    @Override
                    public void currentFrame(CPPFrame frame) {}

                    @Override
                    public void suspended(boolean suspended) {}

                    @Override
                    public void finished() {
                        breakpointsHandler.dispose();
                        completed.complete(null);
                    }
                });
            } finally {
                started.release();
            }
            debugger.execRun();
            return engineProcess.second();
        }, executionDescriptor, displayName).run();
        // Wait for the debugger to actually start up.
        // This is necessary to be able to safely call other methods on the NIDebuggerProvider.
        started.acquireUninterruptibly();
        return completed;
    }

    @Override
    public CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName, NIFrame frame) {
        CompletableFuture<NIVariable> result = new CompletableFuture<>();
        CPPFrame cframe;
        if (frame != null) {
            CPPThread thread = debugger.getThreads().get(frame.getThreadId());
            if (thread == null) {
                result.completeExceptionally(new EvaluateException("No thread " + frame.getThreadId()));
                return result;
            }
            try {
                cframe = (CPPFrame) thread.getFrames().get(frame.getLevel());
            } catch (IndexOutOfBoundsException ioobex) {
                result.completeExceptionally(new EvaluateException(ioobex.getLocalizedMessage()));
                return result;
            }
        } else {
            cframe = debugger.getCurrentFrame();
        }
        cframe.evaluateAsync(expression, resultName).thenAccept(
                rawResult -> {
                    // We must not run the VariableDisplayer synchronously with the MI command thread
                    varDisplayerRP.post(() -> {
                        NIVariable[] variables = variablesDisplayer.displayed(rawResult);
                        NIVariable variable = (variables.length > 0) ? variables[0] : rawResult;
                        result.complete(variable);
                    });
                }).exceptionally(
                exception -> {
                    result.completeExceptionally(exception);
                    return null;
                });
        return result;
    }

    @Override
    public String readMemory(String address, long offset, int length) {
        return debugger.readMemory(address, offset, length);
    }

    @Override
    public String getVersion() {
        return debugger.getVersion();
    }
}

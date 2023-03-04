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
package org.netbeans.modules.cpplite.debugger.ni;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebuggerConfig;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.nativeimage.api.Location;
import org.netbeans.modules.nativeimage.api.SourceInfo;
import org.netbeans.modules.nativeimage.api.Symbol;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.api.debug.StartDebugParameters;
import org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;

import org.openide.LifecycleManager;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;
import org.openide.util.Lookup;

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
    public CompletableFuture<Void> start(StartDebugParameters debugParameters, Consumer<DebuggerEngine> startedEngine) {
        if (debugger != null) {
            throw new IllegalStateException("Debugger has started already.");
        }
        List<String> command = debugParameters.getCommand();
        String miDebugger = debugParameters.getDebugger();
        String displayName = debugParameters.getDisplayName();
        boolean printObjects = debugParameters.isDebuggerDisplayObjects();
        Long processId = debugParameters.getProcessId();
        ExecutionDescriptor executionDescriptor = debugParameters.getExecutionDescriptor();
        Lookup contextLookup = debugParameters.getContextLookup();
        ExplicitProcessParameters explicitParameters = contextLookup != null ? ExplicitProcessParameters.buildExplicitParameters(contextLookup) : null;
        if (explicitParameters == null) {
            explicitParameters = ExplicitProcessParameters.builder().workingDirectory(debugParameters.getWorkingDirectory()).build();
        }
        final ExplicitProcessParameters processParameters = explicitParameters;
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
            Process engineProcess;
            CPPLiteDebugger[] debugger = new CPPLiteDebugger[] { null };
            try {
                LifecycleManager.getDefault().saveAll();
                engineProcess = CPPLiteDebugger.startDebugging(
                        new CPPLiteDebuggerConfig(command, processParameters, printObjects, processId, miDebugger),
                        engine -> {
                            debugger[0] = engine.lookupFirst(null, CPPLiteDebugger.class);
                            this.debugger = debugger[0];
                            if (startedEngine != null) {
                                startedEngine.accept(engine);
                            }
                            debugger[0].addStateListener(new CPPLiteDebugger.StateListener() {
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
                        },
                        frameDisplayer,
                        variablesDisplayer);
            } catch (Exception ex) {
                completed.completeExceptionally(ex);
                throw ex;
            } finally {
                started.release();
            }
            debugger[0].execRun();
            return engineProcess;
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

    @Override
    public List<Location> listLocations(String filePath) {
        return debugger.listLocations(filePath);
    }

    @Override
    public Map<SourceInfo, List<Symbol>> listFunctions(String name, boolean includeNondebug, int maxResults) {
        return debugger.listFunctions(name, includeNondebug, maxResults);
    }

    @Override
    public Map<SourceInfo, List<Symbol>> listVariables(String name, boolean includeNondebug, int maxResults) {
        return debugger.listVariables(name, includeNondebug, maxResults);
    }

}

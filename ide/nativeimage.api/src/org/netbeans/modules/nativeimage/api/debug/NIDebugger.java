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
package org.netbeans.modules.nativeimage.api.debug;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.nativeimage.api.Location;
import org.netbeans.modules.nativeimage.api.SourceInfo;
import org.netbeans.modules.nativeimage.api.Symbol;
import org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider;
import org.netbeans.modules.nativeimage.spi.debug.NIDebuggerServiceProvider;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;
import org.openide.util.Lookup;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Representation of a native image debugger.
 * @since 0.1
 */
public final class NIDebugger {

    private final NIDebuggerProvider provider;

    NIDebugger(NIDebuggerProvider provider) {
        this.provider = provider;
    }

    /**
     * Creates a builder of a new debugger instance.
     *
     * @return a builder of a new debugger instance
     * @throws IllegalStateException when the native debugger is not available
     *         (there is not an implementation of {@link NIDebuggerServiceProvider}
     *         registered in the default lookup).
     * @since 0.1
     */
    @NbBundle.Messages({"MSG_NoNativeDebug=No native debugger is available. Please install native debugger module."})
    public static Builder newBuilder() throws IllegalStateException {
        NIDebuggerServiceProvider provider = Lookup.getDefault().lookup(NIDebuggerServiceProvider.class);
        if (provider == null) {
            throw Exceptions.attachLocalizedMessage(new IllegalStateException(), Bundle.MSG_NoNativeDebug());
        } else {
            return new Builder(provider.create());
        }
    }

    /**
     * Add or change a line breakpoint into the debugger.
     * A breakpoint is added when the `id` is used for the first time and modified
     * when breakpoint with that `id` was added already.
     *
     * @param id a unique ID of the breakpoint
     * @param breakpointDescriptor the breakpoint descriptor
     * @return an instance of the native breakpoint
     * @since 0.1
     */
    public Breakpoint addLineBreakpoint(Object id, NILineBreakpointDescriptor breakpointDescriptor) {
        Breakpoint breakpoint = provider.addLineBreakpoint(id, breakpointDescriptor);
        assert breakpoint != null;
        return breakpoint;
    }

    /**
     * Remove breakpoint with the given id.
     *
     * @param id the ID of the breakpoint to remove
     * @since 0.1
     */
    public void removeBreakpoint(Object id) {
        provider.removeBreakpoint(id);
    }

    /**
     * Start the actual debugging session. Call this typically after breakpoints are added.
     *
     * @param command a command to run the native image
     * @param workingDirectory working directory
     * @param debugger the native debugger command
     * @param displayName display name of the debugger task
     * @param executionDescriptor execution descriptor that describes the runtime attributes
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @return future that completes on the execution finish
     * @since 0.1
     * @deprecated Use {@link #start(org.netbeans.modules.nativeimage.api.debug.StartDebugParameters, java.util.function.Consumer)}
     */
    @Deprecated
    public CompletableFuture<Void> start(List<String> command, File workingDirectory, String debugger, String displayName, ExecutionDescriptor executionDescriptor, Consumer<DebuggerEngine> startedEngine) {
        return provider.start(command, workingDirectory, debugger, displayName, executionDescriptor, startedEngine);
    }

    /**
     * Start the actual debugging session. Call this typically after breakpoints are added.
     *
     * @param debugParameters parameters to start the debugging with
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @return a future which is completed when the started debugger session finishes
     * @since 0.5
     */
    public CompletableFuture<Void> start(StartDebugParameters debugParameters, Consumer<DebuggerEngine> startedEngine) {
        Objects.requireNonNull(debugParameters);
        CompletableFuture<Void> start = provider.start(debugParameters, startedEngine);
        if (start.isCompletedExceptionally()) {
            try {
                start.get();
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof UnsupportedOperationException) {
                    return startLegacy(debugParameters, startedEngine);
                }
            } catch (Throwable ex) {}
        }
        return start;
    }

    private CompletableFuture<Void> startLegacy(StartDebugParameters debugParameters, Consumer<DebuggerEngine> startedEngine) {
        return provider.start(debugParameters.getCommand(), debugParameters.getWorkingDirectory(), debugParameters.getDebugger(), debugParameters.getDisplayName(), debugParameters.getExecutionDescriptor(), startedEngine);
    }

    /**
     * Attach to a process and create a debugging session. Call this typically after breakpoints are added.
     *
     * @param executablePath path to an executable representing the native image
     * @param processId a process to attach to
     * @param debugger the native debugger command
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @return future that completes on the execution finish
     * @since 0.4
     * @deprecated Use {@link #start(StartDebugParameters, Consumer)} and set {@link StartDebugParameters.Builder#processID(Long)}.
     */
    @Deprecated
    public CompletableFuture<Void> attach(String executablePath, long processId, String debugger, Consumer<DebuggerEngine> startedEngine) {
        return provider.attach(executablePath,  processId, debugger, startedEngine);
    }

    /**
     * An asynchronous expression evaluation.
     *
     * @param expression the expression to evaluate
     * @param resultName preferred name of the result variable,
     *                   when <code>null</code> the expression is used as the name
     * @param frame the frame to evaluate at
     * @return the completable future with the evaluation result
     * @since 0.1
     */
    public CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName, NIFrame frame) {
        return provider.evaluateAsync(expression, resultName, frame);
    }

    /**
     * A synchronous expression evaluation. Delegates to the asynchronous evaluation
     * and wait for it's result.
     *
     * @param expression the expression to evaluate
     * @param resultName preferred name of the result variable,
     *                   when <code>null</code> the expression is used as the name
     * @param frame the frame to evaluate at
     * @return the evaluation result
     * @throws EvaluateException when evaluation fails
     * @since 0.1
     */
    public NIVariable evaluate(String expression, String resultName, NIFrame frame) throws EvaluateException {
        try {
            return provider.evaluateAsync(expression, resultName, frame).get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new EvaluateException(ex.getCause());
        }
    }

    /**
     * Read data from memory.
     *
     * @param address address where to read the data from
     * @param offset offset relative to the address where to start reading
     * @param length number of bytes to read
     * @return hexadecimal representation of the memory content, or <code>null</code>
     *         when the read is not successful
     * @since 0.1
     */
    public String readMemory(String address, long offset, int length) {
        return provider.readMemory(address, offset, length);
    }

    /**
     * Get version of the underlying native debugger.
     *
     * @since 0.1
     */
    public String getVersion() {
        return provider.getVersion();
    }

    /**
     * Get a list of locations for a given file path.
     *
     * @param filePath a file path
     * @return list of locations, or <code>null</code> when there's no location
     *         information about such file.
     * @since 0.2
     */
    @CheckForNull
    public List<Location> listLocations(String filePath) {
        return provider.listLocations(filePath);
    }

    /**
     * Get functions of a given name.
     *
     * @param name a name pattern
     * @param includeNondebug include also symbols from the symbol table
     * @param maxResults maximum number of results
     * @return map of source information and their symbols, or <code>null</code>
     *         when there are no matching symbols.
     * @since 0.2
     */
    @CheckForNull
    public Map<SourceInfo, List<Symbol>> listFunctions(String name, boolean includeNondebug, int maxResults) {
        return provider.listFunctions(name, includeNondebug, maxResults);
    }

    /**
     * Get variables of a given name.
     *
     * @param name a name pattern
     * @param includeNondebug include also symbols from the symbol table
     * @param maxResults maximum number of results
     * @return map of source information and their symbols, or <code>null</code>
     *         when there are no matching symbols.
     * @since 0.2
     */
    @CheckForNull
    public Map<SourceInfo, List<Symbol>> listVariables(String name, boolean includeNondebug, int maxResults) {
        return provider.listVariables(name, includeNondebug, maxResults);
    }

    /**
     * A builder that creates a Native Image debugger with optional displayers.
     *
     * @since 0.1
     */
    public static final class Builder {

        private final NIDebuggerProvider debuggerProvider;

        Builder(NIDebuggerProvider debuggerProvider) {
            this.debuggerProvider = debuggerProvider;
        }

        /**
         * Displayer of native frames.
         *
         * @param frameDisplayer translator of the native frame to it's displayed information
         * @since 0.1
         */
        public Builder frameDisplayer(FrameDisplayer frameDisplayer) {
            this.debuggerProvider.setFrameDisplayer(frameDisplayer);
            return this;
        }

        /**
         * Displayer of native variables.
         *
         * @param variablesDisplayer translator of native variables to displayed variables.
         * @since 0.1
         */
        public Builder variablesDisplayer(VariableDisplayer variablesDisplayer) {
            this.debuggerProvider.setVariablesDisplayer(variablesDisplayer);
            return this;
        }

        /**
         * Create the debugger instance.
         * @since 0.1
         */
        public NIDebugger build() {
            return new NIDebugger(debuggerProvider);
        }
    }
}

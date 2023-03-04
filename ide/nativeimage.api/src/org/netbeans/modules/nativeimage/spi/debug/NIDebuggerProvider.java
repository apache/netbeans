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
package org.netbeans.modules.nativeimage.spi.debug;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.nativeimage.api.Location;
import org.netbeans.modules.nativeimage.api.SourceInfo;
import org.netbeans.modules.nativeimage.api.Symbol;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.api.debug.StartDebugParameters;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;

/**
 * Provider of the native image debugger.
 *
 * @author martin
 * @since 0.1
 */
public interface NIDebuggerProvider {

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
    Breakpoint addLineBreakpoint(Object id, NILineBreakpointDescriptor breakpointDescriptor);

    /**
     * Remove breakpoint with the given id.
     *
     * @param id the ID of the breakpoint to remove
     * @since 0.1
     */
    void removeBreakpoint(Object id);

    /**
     * Set a displayer of native frames.
     *
     * @param frameDisplayer translator of the native frame to it's displayed information
     * @since 0.1
     */
    void setFrameDisplayer(FrameDisplayer frameDisplayer);

    /**
     * Set a displayer of native variables.
     *
     * @param variablesDisplayer translator of native variables to displayed variables.
     * @since 0.1
     */
    void setVariablesDisplayer(VariableDisplayer variablesDisplayer);

    /**
     * Start the actual debugging session. Called typically after breakpoints are added.
     *
     * @param command a command to run the native image
     * @param workingDirectory working directory
     * @param debugger the native debugger command
     * @param displayName display name of the debugger task
     * @param executionDescriptor execution descriptor that describes the runtime attributes
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @since 0.1
     * @deprecated Use {@link #start(org.netbeans.modules.nativeimage.api.debug.StartDebugParameters, java.util.function.Consumer)}
     */
    @Deprecated
    default CompletableFuture<Void> start(List<String> command, File workingDirectory, String debugger, String displayName, ExecutionDescriptor executionDescriptor, Consumer<DebuggerEngine> startedEngine) {
        StartDebugParameters parameters = StartDebugParameters.newBuilder(command)
                .workingDirectory(workingDirectory)
                .debugger(debugger)
                .displayName(displayName)
                .executionDescriptor(executionDescriptor)
                .build();
        return start(parameters, startedEngine);
    }

    /**
     * Start the actual debugging session. Called typically after breakpoints are added.
     * 
     * @param debugParameters parameters to start the debugging with
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @return a future which is completed when the started debugger session finishes
     * @since 0.5
     */
    default CompletableFuture<Void> start(StartDebugParameters debugParameters, Consumer<DebuggerEngine> startedEngine) {
        CompletableFuture cf = new CompletableFuture();
        cf.completeExceptionally(new UnsupportedOperationException());
        return cf;
    }

    /**
     * Attach to a process and create a debugging session. Called typically after breakpoints are added.
     *
     * @param executablePath path to an executable representing the native image
     * @param processId a process to attach to
     * @param debugger the native debugger command
     * @param startedEngine the corresponding DebuggerEngine is passed to this consumer
     * @return future that completes on the execution finish
     * @since 0.4
     * @deprecated Use {@link #start(StartDebugParameters, Consumer)} and set {@link StartDebugParameters.Builder#processID(long)}.
     */
    @Deprecated
    default CompletableFuture<Void> attach(String executablePath, long processId, String debugger, Consumer<DebuggerEngine> startedEngine) {
        CompletableFuture cf = new CompletableFuture();
        cf.completeExceptionally(new UnsupportedOperationException());
        return cf;
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
    CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName, NIFrame frame);

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
    String readMemory(String address, long offset, int length);

    /**
     * Get version of the underlying native debugger.
     *
     * @since 0.1
     */
    String getVersion();

    /**
     * Provide a list of locations for a given file path.
     *
     * @param filePath a file path
     * @return list of locations, or <code>null</code> when there's no location
     *         information about such file.
     * @since 0.2
     */
    default List<Location> listLocations(String filePath) {
        return null;
    }

    /**
     * Provide a list of functions in the debuggee.
     *
     * @since 0.2
     */
    default Map<SourceInfo, List<Symbol>> listFunctions(String name, boolean includeNondebug, int maxResults) {
        return null;
    }

    /**
     * Provide a list of global variables in the debuggee.
     *
     * @since 0.2
     */
    default Map<SourceInfo, List<Symbol>> listVariables(String name, boolean includeNondebug, int maxResults) {
        return null;
    }
}

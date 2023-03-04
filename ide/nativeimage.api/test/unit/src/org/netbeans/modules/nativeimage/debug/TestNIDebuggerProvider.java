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
package org.netbeans.modules.nativeimage.debug;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import static org.junit.Assert.assertEquals;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider;
import org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;

public class TestNIDebuggerProvider implements NIDebuggerProvider {

    private final Map<Object, String> breakpoints = new HashMap<>();
    private final String checkStartParams;
    private FrameDisplayer frameDisplayer;
    private VariableDisplayer variablesDisplayer;

    public TestNIDebuggerProvider(String checkStartParams) {
        this.checkStartParams = checkStartParams;
    }

    @Override
    public Breakpoint addLineBreakpoint(Object id, NILineBreakpointDescriptor bd) {
        String testBP = id + bd.getFilePath() + bd.getLine() + bd.isEnabled() + bd.getCondition() + bd.isHidden();
        breakpoints.put(id, testBP);
        return new Breakpoint() {
            @Override
            public boolean isEnabled() {
                return true;
            }
            @Override
            public void disable() {}
            @Override
            public void enable() {}
        };
    }

    @Override
    public void removeBreakpoint(Object id) {
        breakpoints.remove(id);
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
    public CompletableFuture<Void> start(List<String> command, File workingDirectory, String debugger, String displayName, ExecutionDescriptor executionDescriptor, Consumer<DebuggerEngine> startedEngine) {
        assertEquals(checkStartParams, command.toString() + workingDirectory + debugger + displayName + executionDescriptor + breakpoints.toString() + frameDisplayer + variablesDisplayer);
        startedEngine.accept(null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName, NIFrame frame) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String readMemory(String address, long offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        return "Test_NI";
    }

}

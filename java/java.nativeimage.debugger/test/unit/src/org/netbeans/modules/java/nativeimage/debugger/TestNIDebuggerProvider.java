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
package org.netbeans.modules.java.nativeimage.debugger;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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

    private final Map<Object, Breakpoint> breakpoints = new LinkedHashMap<>();
    private FrameDisplayer frameDisplayer;
    private VariableDisplayer variablesDisplayer;

    public TestNIDebuggerProvider() {
    }

    @Override
    public Breakpoint addLineBreakpoint(Object id, NILineBreakpointDescriptor breakpointDescriptor) {
        Breakpoint nativeBreakpoint = new Breakpoint() {
            @Override
            public boolean isEnabled() {
                return breakpointDescriptor.isEnabled();
            }

            @Override
            public void disable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void enable() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String toString() {
                return breakpointDescriptor.getFilePath() + ':' + breakpointDescriptor.getLine();
            }
        };
        breakpoints.put(id, nativeBreakpoint);
        return nativeBreakpoint;
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
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<NIVariable> evaluateAsync(String expression, String resultName, NIFrame frame) {
        NIVariable result;
        if ("breakpoints".equals(expression)) {
            NIVariable[] children = new NIVariable[breakpoints.size()];
            result = new TestNIVariable(expression, "BP", "BP", null, children, null);
            int i = 0;
            for (Breakpoint b : breakpoints.values()) {
                children[i++] = new TestNIVariable("b" + i, "BP", b.toString(), result, new NIVariable[]{}, null);
            }
        } else {
            result = new TestNIVariable(expression, "type", "value", null, new NIVariable[]{}, null);
        }
        result = variablesDisplayer.displayed(result)[0];
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public String readMemory(String address, long offset, int length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVersion() {
        return "Test1";
    }

}

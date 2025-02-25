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
package org.netbeans.modules.lsp.client.debugger;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.Exceptions;

/**
 * Representation of a variable.
 */
public final class DAPVariable {

    private final DAPDebugger debugger;
    private final DAPFrame frame;
    private final DAPVariable parentVariable;
    private final int variableReference;
    private final String name;
    private final String type;
    private final String value;
    private final int totalChildren;
    private final AtomicReference<DAPVariable[]> children = new AtomicReference<>();

    DAPVariable(DAPDebugger debugger, DAPFrame frame, DAPVariable parentVariable, int variableReference, String name, String type, String value, int totalChildren) {
        this.debugger = debugger;
        this.frame = frame;
        this.parentVariable = parentVariable;
        this.variableReference = variableReference;
        this.name = name;
        this.type = type;
        this.value = value;
        this.totalChildren = totalChildren;
    }

    public DAPFrame getFrame() {
        return frame;
    }

    public DAPVariable getParent() {
        return parentVariable;
    }

    public int getVariableReference() {
        return variableReference;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getTotalChildren() { //XXX:
        return totalChildren;
    }

    public DAPVariable[] getChildren(int from, int to) {
        DAPVariable[] vars = children.get();

        if (vars == null) {
            try {
                children.set(vars = debugger.getVariableChildren(frame, this).get().toArray(DAPVariable[]::new));
            } catch (InterruptedException | ExecutionException ex) {
                return new DAPVariable[0];
            }
        }

        if (from >= 0) {
            to = Math.min(to, vars.length);
            if (from < to) {
                vars = Arrays.copyOfRange(vars, from, to);
            } else {
                vars = new DAPVariable[0];
            }
        }
        return vars;
    }

}

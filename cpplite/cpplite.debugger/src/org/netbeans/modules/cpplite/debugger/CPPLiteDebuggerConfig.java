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

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public final class CPPLiteDebuggerConfig {

    private final List<String> executable;
    private final ExplicitProcessParameters processParameters;
    private final boolean printObjects;
    @NullAllowed
    private final Long processId;
    private final String debugger;

    public CPPLiteDebuggerConfig(List<String> executable, ExplicitProcessParameters processParameters, boolean printObjects, @NullAllowed Long processId, String debugger) {
        this.processParameters = processParameters;
        this.processId = processId;
        this.debugger = debugger;
        if (processParameters.isArgReplacement()) {
            this.executable = new ArrayList<>();
            this.executable.add(executable.get(0));
            if (processParameters.getArguments() != null) {
                this.executable.addAll(processParameters.getArguments());
            }
        } else {
            if (processParameters.getArguments() != null) {
                this.executable = new ArrayList<>();
                this.executable.addAll(executable);
                this.executable.addAll(processParameters.getArguments());
            } else {
                this.executable = executable;
            }
        }
        if (processParameters.getLauncherArguments() != null) {
            Exceptions.printStackTrace(new IllegalStateException("Launcher arguments " + processParameters.getLauncherArguments() + " can not be accepted by CPPLite debugger"));
        }
        this.printObjects = printObjects;
    }

    public String getDisplayName() {
        return (!executable.isEmpty()) ? executable.get(0) : "<empty>";
    }

    /**
     * Get the executable with arguments.
     */
    public List<String> getExecutable() {
        return executable;
    }

    /**
     * Get the parameters which the executable command is to be launched with.
     */
    public ExplicitProcessParameters getProcessParameters() {
        return processParameters;
    }

    /**
     * Check if debugger should attach to a running process. Get the process ID
     * from {@link #getAttachProcessId()}.
     */
    public boolean isAttach() {
        return processId != null;
    }

    /**
     * Get the process ID to attach to.
     *
     * @return the process ID
     * @throws IllegalStateException when {@link #isAttach()} is false.
     */
    public long getAttachProcessId() {
        if (processId == null) {
            throw new IllegalStateException("No process to attach to.");
        }
        return processId;
    }

    public String getDebugger() {
        return debugger;
    }

    public boolean isPrintObjects() {
        return printObjects;
    }
}

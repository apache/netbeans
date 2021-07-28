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

package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.util.List;

import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author lahvac
 */
public final class CPPLiteDebuggerConfig {

    private final List<String> executable;
    private final File directory;
    @NullAllowed
    private final Long processId;
    private final String debugger;

    public CPPLiteDebuggerConfig(List<String> executable, File directory, @NullAllowed Long processId, String debugger) {
        this.executable = executable;
        this.directory = directory;
        this.processId = processId;
        this.debugger = debugger;
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
     * Get the directory in which the executable command is to be launched.
     */
    public File getDirectory() {
        return directory;
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
}

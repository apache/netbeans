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
package org.netbeans.modules.nativeimage.api.debug;

import java.io.File;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.openide.util.Lookup;

/**
 * Parameters passed to {@link NIDebugger#start(org.netbeans.modules.nativeimage.api.debug.StartDebugParameters, java.util.function.Consumer)}.
 *
 * @since 0.5
 */
public final class StartDebugParameters {

    private final List<String> command;
    private final File workingDirectory;
    private final String debugger;
    private final String displayName;
    private final boolean displayObjects;
    private final Long processId;
    private final ExecutionDescriptor executionDescriptor;
    private final Lookup contextLookup;

    private StartDebugParameters(List<String> command, File workingDirectory, String debugger,
                                 String displayName, boolean displayObjects, Long processId,
                                 ExecutionDescriptor executionDescriptor, Lookup contextLookup) {
        this.command = command;
        this.workingDirectory = workingDirectory;
        this.debugger = debugger;
        this.displayName = displayName;
        this.displayObjects = displayObjects;
        this.processId = processId;
        this.executionDescriptor = executionDescriptor;
        this.contextLookup = contextLookup;
    }

    /**
     * The command to run the native image.
     *
     * @return the command with arguments.
     */
    public List<String> getCommand() {
        return command;
    }

    /**
     * Working directory of the process.
     *
     * @return the working directory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * The native debugger command.
     *
     * @return the debugger command
     */
    public String getDebugger() {
        return debugger;
    }

    /**
     * Display name of the debugger task.
     *
     * @return the display name of debugger task
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check whether debugger may display objects using it's own rules.
     *
     * @return <code>true</code> if debugger may display objects using it's own rules,
     *         <code>false</code> otherwise.
     * @since 0.8
     */
    public boolean isDebuggerDisplayObjects() {
        return displayObjects;
    }

    /**
     * Get a process ID to attach to.
     * When <code>null</code>, the command is to be launched. Otherwise,
     * the debugger is attached to process with that ID.
     *
     * @return the process ID to attach to, or <code>null</code> to launch the command.
     * @since 0.8
     */
    @CheckForNull
    public Long getProcessId() {
        return processId;
    }

    /**
     * Execution descriptor that describes the runtime attributes.
     *
     * @return the execution descriptor
     */
    public ExecutionDescriptor getExecutionDescriptor() {
        return executionDescriptor;
    }

    /**
     * Context lookup. The lookup may contain other parameters, like ExplicitProcessParameters.
     *
     * @return the context lookup
     */
    public Lookup getContextLookup() {
        return contextLookup;
    }

    /**
     * Create a new debug parameters builder.
     *
     * @param command the command to run the native image.
     * @return a new builder
     */
    public static Builder newBuilder(List<String> command) {
        return new Builder(command);
    }

    /**
     * Builder of start debug parameters.
     *
     * @since 0.5
     */
    public static final class Builder {

        private final List<String> command;
        private File workingDirectory;
        private String debugger;
        private String displayName;
        private boolean displayObjects = true;
        private Long processId = null;
        private ExecutionDescriptor executionDescriptor;
        private Lookup contextLookup;

        Builder(List<String> command) {
            this.command = command;
        }

        /**
         * Set the working directory.
         * @return the builder
         */
        public Builder workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        /**
         * Set the native debugger command.
         * @return the builder
         */
        public Builder debugger(String debugger) {
            this.debugger = debugger;
            return this;
        }

        /**
         * Set display name of the debugger task.
         *
         * @return the builder
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Set whether debugger may display objects using it's own rules.
         * It's <code>true</code> by default.
         *
         * @since 0.8
         */
        public Builder debuggerDisplayObjects(boolean displayObjects) {
            this.displayObjects = displayObjects;
            return this;
        }

        /**
         * Set a process ID to attach to instead of launching the command. The command
         * should correspond to the process.
         * Use <code>null</code> to launch the command. It's <code>null</code> by default.
         *
         * @since 0.8
         */
        public Builder processID(@NullAllowed Long processId) { 
            this.processId = processId;
            return this;
        }

        /**
         * Set execution descriptor that describes the runtime attributes.
         *
         * @return the builder
         */
        public Builder executionDescriptor(ExecutionDescriptor executionDescriptor) {
            this.executionDescriptor = executionDescriptor;
            return this;
        }

        /**
         * Context lookup. The lookup may contain other parameters, like ExplicitProcessParameters.
         *
         * @return the builder
         */
        public Builder lookup(Lookup contextLookup) {
            this.contextLookup = contextLookup;
            return this;
        }

        /**
         * Build the {@link StartDebugParameters} based on the properties set.
         *
         * @return a new instance of {@link StartDebugParameters}.
         */
        public StartDebugParameters build() {
            return new StartDebugParameters(command, workingDirectory, debugger, displayName, displayObjects, processId, executionDescriptor, contextLookup);
        }
    }
}

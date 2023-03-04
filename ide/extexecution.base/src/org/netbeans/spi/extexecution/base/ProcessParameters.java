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
package org.netbeans.spi.extexecution.base;

import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.base.ProcessParametersAccessor;

/**
 * The parameters configured for process creation.
 *
 * @see ProcessBuilderImplementation
 * @author Petr Hejl
 */
public final class ProcessParameters {

    private final String executable;

    private final String workingDirectory;

    private final List<String> arguments;

    private final boolean redirectErrorStream;

    private final Map<String, String> environmentVariables;

    static {
        ProcessParametersAccessor.setDefault(new ProcessParametersAccessor() {

            @Override
            public ProcessParameters createProcessParameters(String executable, String workingDirectory,
                    List<String> arguments, boolean redirectErrorStream, Map<String, String> environmentVariables) {
                return new ProcessParameters(executable, workingDirectory, arguments,
                        redirectErrorStream, environmentVariables);
            }
        });
    }

    private ProcessParameters(String executable, String workingDirectory, List<String> arguments,
            boolean redirectErrorStream, Map<String, String> environmentVariables) {
        this.executable = executable;
        this.workingDirectory = workingDirectory;
        this.arguments = arguments;
        this.redirectErrorStream = redirectErrorStream;
        this.environmentVariables = environmentVariables;
    }

    /**
     * Returns the configured executable.
     *
     * @return the configured executable
     */
    @NonNull
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns the configured working directory or <code>null</code> in case it
     * was not configured.
     *
     * @return the configured working directory or <code>null</code> in case it
     *             was not configured
     */
    @CheckForNull
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns the arguments configured for the process.
     *
     * @return the arguments configured for the process
     */
    @NonNull
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Returns <code>true</code> if standard error stream should be redirected
     * to standard output stream.
     *
     * @return <code>true</code> if standard error stream should be redirected
     *             to standard output stream
     */
    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * Returns the environment variables configured for the process.
     *
     * @return the environment variables configured for the process
     */
    @NonNull
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }
}

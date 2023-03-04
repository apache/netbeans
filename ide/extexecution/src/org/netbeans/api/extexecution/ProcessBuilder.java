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
package org.netbeans.api.extexecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.Environment;
import org.netbeans.modules.extexecution.ProcessBuilderAccessor;
import org.netbeans.spi.extexecution.ProcessBuilderImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.UserQuestionException;

/**
 * Abstraction of process builders. You can freely configure the parameters
 * and then create a process by calling the {@link #call()} method. You can
 * also (re)configure the builder and spawn a different process.
 * <p>
 * Note the API does not prescribe the actual meaning of {@link Process}.
 * It may be local process, remote process or some other implementation.
 * <p>
 * You can use the default implementation returned by {@link #getLocal()}
 * for creating the local machine OS processes.
 * <p>
 * <i>Thread safety</i> of this class depends on thread safety of
 * the {@link ProcessBuilderImplementation} the class is using. If it is thread
 * safe (if possible the implementation should be even stateless) this class
 * is thread safe as well.
 *
 * @author Petr Hejl
 * @since 1.28
 * @deprecated use {@link org.netbeans.api.extexecution.base.ProcessBuilder}
 */
@Deprecated
public final class ProcessBuilder implements Callable<Process> {

    private final ProcessBuilderImplementation implementation;

    private final String description;

    /**<i>GuardedBy("this")</i>*/
    private String executable;

    /**<i>GuardedBy("this")</i>*/
    private String workingDirectory;

    /**<i>GuardedBy("this")</i>*/
    private final List<String> arguments = new ArrayList<String>();

    /**<i>GuardedBy("this")</i>*/
    private final List<String> paths = new ArrayList<String>();

    /**<i>GuardedBy("this")</i>*/
    private final Map<String, String> envVariables = new HashMap<String, String>();

    /**<i>GuardedBy("this")</i>*/
    private boolean redirectErrorStream;

    static {
        ProcessBuilderAccessor.setDefault(new ProcessBuilderAccessor() {

            @Override
            public ProcessBuilder createProcessBuilder(ProcessBuilderImplementation impl, String description) {
                return new ProcessBuilder(impl, description);
            }
        });
    }

    private ProcessBuilder(ProcessBuilderImplementation implementation, String description) {
        this.implementation = implementation;
        this.description = description;
    }

    /**
     * Returns the {@link ProcessBuilder} creating the OS process on local
     * machine. Returned implementation is <code>thread safe</code>.
     *
     * @return the {@link ProcessBuilder} creating the OS process on local
     *             machine
     */
    public static ProcessBuilder getLocal() {
        return new ProcessBuilder(new LocalProcessFactory(),
                NbBundle.getMessage(ProcessBuilder.class, "LocalProcessBuilder"));
    }

    /**
     * Returns the human readable description of this builder.
     *
     * @return the human readable description of this builder
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * Sets the executable to run. There is no default value. The {@link #call()}
     * methods throws {@link IllegalStateException} when there is no executable
     * configured.
     *
     * @param executable the executable to run
     */
    public void setExecutable(@NonNull String executable) {
        Parameters.notNull("executable", executable);

        synchronized (this) {
            this.executable = executable;
        }
    }

    /**
     * Sets the working directory for the process created by subsequent call
     * of {@link #call()}. The default value is implementation specific.
     *
     * @param workingDirectory the working directory of the process
     */
    public void setWorkingDirectory(@NullAllowed String workingDirectory) {
        synchronized (this) {
            this.workingDirectory = workingDirectory;
        }
    }

    /**
     * Sets the arguments passed to the process created by subsequent call
     * of {@link #call()}. By default there are no arguments.
     *
     * @param arguments the arguments passed to the process
     */
    public void setArguments(@NonNull List<String> arguments) {
        Parameters.notNull("arguments", arguments);

        synchronized (this) {
            this.arguments.clear();
            this.arguments.addAll(arguments);
        }
    }

    /**
     * Sets the environment variables for the process created by subsequent call
     * of {@link #call()}. By default there are no environment variables with
     * exception of <code>PATH</code> possibly configured by {@link #setPaths(java.util.List)}.
     *
     * @param envVariables the environment variables for the process
     */
    public void setEnvironmentVariables(@NonNull Map<String, String> envVariables) {
        Parameters.notNull("envVariables", envVariables);

        synchronized (this) {
            this.envVariables.clear();
            this.envVariables.putAll(envVariables);
        }
    }

    /**
     * Sets the additional paths to be included in <code>PATH</code> environment
     * variable for the process.
     *
     * @param paths the additional paths to be included in <code>PATH</code>
     *             environment variable
     */
    public void setPaths(@NonNull List<String> paths) {
        Parameters.notNull("paths", paths);

        synchronized (this) {
            this.paths.clear();
            this.paths.addAll(paths);
        }
    }

    /**
     * Configures the error stream redirection. If <code>true</code> the error
     * stream of process created by subsequent call of {@link #call()} method
     * will be redirected to standard output stream.
     *
     * @param redirectErrorStream the error stream redirection
     */
    public void setRedirectErrorStream(boolean redirectErrorStream) {
        synchronized (this) {
            this.redirectErrorStream = redirectErrorStream;
        }
    }

    /**
     * Creates the new {@link Process} based on the properties configured
     * in this builder.
     * <p>
     * Actual behavior depends on the builder implementation, but it should
     * respect all the properties configured on this builder.
     * <p>
     * Since version 1.35 implementors of this method are advised to throw
     * a {@link UserQuestionException} in case the execution cannot be
     * performed and requires additional user confirmation, or configuration.
     * Callers of this method may check for this exception and handle it
     * appropriately.
     *
     * @see ProcessBuilderImplementation
     * @return the new {@link Process} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     * @throws IllegalStateException if there is no executable configured
     *             by {@link #setExecutable(java.lang.String)}
     * @throws UserQuestionException if the execution cannot be performed
     *     without permission from the user
     */
    @NonNull
    @Override
    public Process call() throws IOException {
        String currentExecutable = null;
        String currentWorkingDirectory = null;
        List<String> currentArguments = new ArrayList<String>();
        List<String> currentPaths = new ArrayList<String>();
        Map<String, String> currentEnvVariables = new HashMap<String, String>();
        boolean currentRedirectErrorStream = false;

        synchronized (this) {
            currentExecutable = executable;
            currentWorkingDirectory = workingDirectory;
            currentArguments.addAll(arguments);
            currentPaths.addAll(paths);
            currentEnvVariables.putAll(envVariables);
            currentRedirectErrorStream = redirectErrorStream;
        }

        if (currentExecutable == null) {
            throw new IllegalStateException("The executable has not been configured");
        }

        return implementation.createProcess(currentExecutable, currentWorkingDirectory, currentArguments,
                currentPaths, currentEnvVariables, currentRedirectErrorStream);
    }

    private static class LocalProcessFactory implements ProcessBuilderImplementation {

        @Override
        public Process createProcess(String executable, String workingDirectory, List<String> arguments,
                List<String> paths, Map<String, String> environment, boolean redirectErrorStream) throws IOException {

            org.netbeans.api.extexecution.base.ProcessBuilder builder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
            builder.setExecutable(executable);
            builder.setWorkingDirectory(workingDirectory);
            builder.setArguments(arguments);
            builder.setRedirectErrorStream(redirectErrorStream);
            Environment env  = builder.getEnvironment();
            for (String path : paths) {
                env.prependPath("PATH", path);
            }
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                env.setVariable(entry.getKey(), entry.getValue());
            }

            return builder.call();
        }
    }
}

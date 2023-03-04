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
package org.netbeans.modules.nativeexecution.api;

import java.io.IOException;

/**
 * A {@link NativeProcessBuilder} starts a system process and returns an
 * instance of the {@link NativeProcess} which is a subclass of the
 * {@link Process java.lang.Process}.
 * The differentiator is that this implementation can represent as local as well
 * as remote process, has information about process' PID and about it's
 * {@link NativeProcess.State state}.
 */
public abstract class NativeProcess extends Process {

    /**
     * Returns execution environment of the process.
     *
     * @return execution environment of the process.
     */
    public abstract ExecutionEnvironment getExecutionEnvironment();

    /**
     * Returns PID of the underlaying system process.<br>
     * @return PID of the underlaying system process.
     * @throws IllegalStateException if no PID was obtained prior to method
     *         invocation.
     */
    public abstract int getPID() throws IOException;

    /**
     * Returns the current {@link NativeProcess.State state} of the process.
     * @return current state of the process.
     */
    public abstract State getState();

    /**
     * Returns ProcessInfo (if available), or null
     * @return ProcessInfo
     */
    public abstract ProcessInfo getProcessInfo();

    /**
     * Returns an extended information of the process exit status.
     *
     * Could be null.
     *
     * @return an extended information of the process exit status.
     * @exception IllegalThreadStateException if the subprocess represented by
     * this <code>Process</code> object has not yet terminated.
     */
    public abstract ProcessStatusEx getExitStatusEx();

    /**
     * Enumerates possible states of the {@link NativeProcess}.
     */
    public static enum State {

        /**
         * Native process is in an Initial state. This means that it has not been
         * started yet.
         */
        INITIAL,
        /**
         * Native process is starting. This means that it has been submitted,
         * but no PID is recieved so far.
         */
        STARTING,
        /**
         * Native process runs. This means that process successfully started and
         * it's PID is already known.
         */
        RUNNING,
        /**
         * Native process exited, but exit status is not available yet.
         */
        FINISHING,
        /**
         * Native process exited.
         */
        FINISHED,
        /**
         * Native process submission failed due to some exception.
         */
        ERROR,
        /**
         * Native process forcibly terminated.
         */
        CANCELLED
    }
}

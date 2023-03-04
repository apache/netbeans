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

import java.util.Map;

/**
 * The interface representing the implementation
 * of {@link org.netbeans.api.extexecution.base.Processes}.
 * <p>
 * Implementation of this interface should be published in default lookup
 * in order to be used by
 * {@link org.netbeans.api.extexecution.base.Processes}
 *
 * @see org.netbeans.api.extexecution.base.Processes
 * @author Petr Hejl
 */
public interface ProcessesImplementation {

    /**
     * Kills the process passed as parameter and <i>attempts</i> to terminate
     * all child processes in process tree.
     * <p>
     * Any process running in environment containing the same variables
     * with the same values as those passed in <code>env</code> (all of them)
     * is supposed to be part of the process tree and may be killed.
     *
     * @param process process to kill
     * @param environment map containing the variables and their values which the
     *             process should have to be considered being part of
     *             the tree to kill; used as a hint to find subprocesses
     * @throws UnsupportedOperationException when this implementation is not able
     *             to even attempt to kill the process and the subprocesses
     */
    void killTree(Process process, Map<String, String> environment) throws UnsupportedOperationException;
}

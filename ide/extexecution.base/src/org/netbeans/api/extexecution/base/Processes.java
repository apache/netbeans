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

package org.netbeans.api.extexecution.base;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extexecution.base.WrapperProcess;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;
import org.openide.util.Lookup;

/**
 * The utility class for better processes handling.
 *
 * @author Petr Hejl
 * @see ProcessesImplementation
 */
public final class Processes {

    private static final Logger LOGGER = Logger.getLogger(Processes.class.getName());

    private Processes() {
        super();
    }

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
     */
    public static void killTree(Process process, Map<String, String> environment) {
        if (process instanceof WrapperProcess) {
            killTree(((WrapperProcess) process).getDelegate(), environment);
            return;
        }
        for (ProcessesImplementation impl : Lookup.getDefault().lookupAll(ProcessesImplementation.class)) {
            try {
                impl.killTree(process, environment);
                LOGGER.log(Level.FINE, "Process tree killed using {0}", impl.getClass().getName()); // NOI18N
                break;
            } catch (UnsupportedOperationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        process.destroy();
    }
}

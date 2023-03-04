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

import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.spi.extexecution.destroy.ProcessDestroyPerformer;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Utility class capable of properly terminating external process along with any
 * child processes created during execution.
 *
 * @author mkleint
 * @since 1.16
 * @deprecated use {@link Processes}
 */
@Deprecated
public final class ExternalProcessSupport {

    private ExternalProcessSupport() {
        super();
    }

    /**
     * Destroys the process passed as parameter and attempts to terminate all child
     * processes created during the process' execution.
     * <p>
     * Any process running in environment containing the same variables
     * with the same values as those passed in <code>env</code> (all of them)
     * is supposed to be part of the process tree and may be terminated.
     *
     * @param process process to kill
     * @param env map containing the variables and their values which the
     *             process must have to be considered being part of
     *             the tree to kill
     */
    public static void destroy(@NonNull Process process, @NonNull Map<String, String> env) {
        Parameters.notNull("process", process);
        Parameters.notNull("env", env);

        ProcessDestroyPerformer pdp = Lookup.getDefault().lookup(ProcessDestroyPerformer.class);
        if (pdp != null) {
            // XXX not nice, but there should be no PDPs anyway
            if ("org.netbeans.modules.extexecution.base.WrapperProcess".equals(process.getClass().getName())) { // NOI18N
                process.destroy();
                return;
            }
            pdp.destroy(process, env);
        } else {
            Processes.killTree(process, env);
        }
    }
}

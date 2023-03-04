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

package org.netbeans.modules.maven.cos;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation;
import org.openide.util.Parameters;

/**
 * API for an alternative Compile on Save execution.
 *
 * @see CoSAlternativeExecutorImplementation
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 2.99
 */
public final class CoSAlternativeExecutor {

    private CoSAlternativeExecutor() {
    }

    /**
     * Perform an alternative execution of all registered {@link CoSAlternativeExecutorImplementation}.
     *
     * <p>
     * Using the given {@link RunConfig}, finds all {@link CoSAlternativeExecutorImplementation}
     * registered for the project and performs their execute method. We only perform executors until
     * one of them is able to take over the build. The rest of executors are skipped in such case.
     *
     * <p>
     * If none of the executors is able to take over the build, the default execution is proceed.
     *
     * @param config configuration
     * @param context execution context
     * @return {@code true} if one of the registered execution was successful,
     *         {@code false} if all registered executions were not successful
     */
    public static boolean execute(@NonNull RunConfig config, @NonNull ExecutionContext context) {
        Parameters.notNull("config", config);   // NOI18N
        Parameters.notNull("context", context); // NOI18N

        Collection<? extends CoSAlternativeExecutorImplementation> impls = config.getProject().getLookup().lookupAll(CoSAlternativeExecutorImplementation.class);
        for (CoSAlternativeExecutorImplementation impl : impls) {
            if (impl.execute(config, context)) {
                return true;
            }
        }
        // None of the implementations were able to take over the build
        return false;
    }
}

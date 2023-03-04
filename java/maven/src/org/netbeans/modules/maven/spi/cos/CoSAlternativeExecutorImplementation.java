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

package org.netbeans.modules.maven.spi.cos;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.cos.CoSAlternativeExecutor;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;

/**
 * Alternative executor enables to rewrite the way how compile on save execution is
 * performed by default.
 *
 * <p>
 * This can be useful in cases when we don't need to execute standard run behavior.
 * For example when re-running Maven Web application with enabled CoS/DoS, we don't
 * want to rebuild whole project every-time and simply re-opening index.html is enough.
 *
 * <p>
 * If the project want to use {@link CoSAlternativeExecutorImplementation} it should register
 * it in it's project {@link Lookup}.
 *
 * <p>
 * This class should not be used directly. Use {@link CoSAlternativeExecutor} API class instead.
 *
 * <p>
 * See issue 230565 for some details about why this was needed in the first place.
 *
 * @see CoSAlternativeExecutor
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 2.99
 */
public interface CoSAlternativeExecutorImplementation {

    /**
     * Perform an alternative execution.
     *
     * <p>
     * SPI client should perform whatever he wants to do instead of the default CoS execution behavior.
     *
     * @param config configuration
     * @param executionContext execution context
     * @return {@code true} if the execution was successful, {@code false} otherwise
     */
    boolean execute(@NonNull RunConfig config, @NonNull ExecutionContext executionContext);

}

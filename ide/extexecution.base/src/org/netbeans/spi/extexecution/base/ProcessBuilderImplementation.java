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

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.Environment;
import org.openide.util.Lookup;

/**
 * The interface representing the implementation
 * of {@link org.netbeans.api.extexecution.base.ProcessBuilder}.
 *
 * @see org.netbeans.api.extexecution.base.ProcessBuilder
 * @author Petr Hejl
 */
public interface ProcessBuilderImplementation extends Lookup.Provider {

    /**
     * Returns the object for environment variables manipulation.
     *
     * @return the object for environment variables manipulation
     */
    @NonNull
    Environment getEnvironment();

    /**
     * Provides an extension point to the implementors. One may enhance the
     * functionality of {@link org.netbeans.api.extexecution.base.ProcessBuilder}
     * by this as the content of the {@link Lookup} is included in
     * {@link org.netbeans.api.extexecution.base.ProcessBuilder#getLookup()}
     *
     * @return a lookup providing an extension point
     */
    @Override
    Lookup getLookup();

    /**
     * Creates a process using the specified parameters.
     * <p>
     * The environment variables stored in parameters are acquired by call to
     * {@link Environment#values()}. So if the implementation does not aim to be
     * or can't be thread safe it may check or use the {@link Environment}
     * directly.
     *
     * @param parameters the instance describing the process parameters
     * @return a process created with specified parameters
     * @throws IOException if the process could not be created
     */
    @NonNull
    Process createProcess(@NonNull ProcessParameters parameters) throws IOException;

}

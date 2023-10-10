/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.extexecution;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.UserQuestionException;

/**
 * The interface representing the implementation
 * of {@link org.netbeans.api.extexecution.ProcessBuilder}.
 *
 * <div class="nonnormative">
 * <p>
 * Although it is not required it is reasonable to have implementation of this
 * interface stateless. In such case instances of {@link org.netbeans.api.extexecution.ProcessBuilder}
 * using it will be <i>thread safe</i>.
 * </div>
 *
 * @see org.netbeans.api.extexecution.ProcessBuilder
 * @author Petr Hejl
 * @since 1.28
 * @deprecated use {@link org.netbeans.spi.extexecution.base.ProcessBuilderImplementation}
 *             and {@link org.netbeans.spi.extexecution.base.ProcessBuilderFactory}
 */
@Deprecated
public interface ProcessBuilderImplementation {

    /**
     * Creates a process using the specified parameters and environment
     * configuration.
     *
     * @param executable the name of the executable to run
     * @param workingDirectory the working directory of the created process or
     *             <code>null</code> as implementation specific default
     * @param arguments the arguments passed to the process
     * @param paths the additional paths to add to <code>PATH</code> environment
     *             variable
     * @param environment environment variables to configure for the process
     * @param redirectErrorStream when <code>true</code> the error stream of
     *             the process should be redirected to standard output stream
     * @return a process created with specified parameters and environment
     *             configuration
     * @throws IOException IOException if the process could not be created
     * @throws UserQuestionException in case there is a need to interact with
     *    user, don't be afraid to throw a subclass of 
     *    {@link UserQuestionException} with overriden {@link UserQuestionException#confirmed()}
     *    method.
     */
    @NonNull
    Process createProcess(@NonNull String executable, @NullAllowed String workingDirectory, @NonNull List<String> arguments,
            @NonNull List<String> paths, @NonNull Map<String, String> environment, boolean redirectErrorStream) throws IOException;

}

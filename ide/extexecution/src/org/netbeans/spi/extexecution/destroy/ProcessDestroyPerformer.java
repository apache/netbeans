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

package org.netbeans.spi.extexecution.destroy;

import java.util.Map;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;

/**
 * A service capable of properly terminating external process along with any
 * child processes created during execution.
 * <p>
 * Implementation of this interface should be published in default lookup
 * in order to be used by
 * {@link org.netbeans.api.extexecution.ExternalProcessSupport#destroy(java.lang.Process, java.util.Map)}
 * and {@link org.netbeans.api.extexecution.ExternalProcessBuilder}.
 * <p>
 * Note: not to be implemented by modules, might not be present in all versions
 * of the application.
 * Please use {@link org.netbeans.api.extexecution.ExternalProcessSupport#destroy(java.lang.Process, java.util.Map)}
 * for accessing the service.
 *
 * @author mkleint
 * @since 1.16
 * @deprecated use {@link ProcessesImplementation} and {@link Processes}
 */
@Deprecated
public interface ProcessDestroyPerformer {

    /**
     * Destroys the process passed as parameter and attempts to terminate all child
     * processes created during the process' execution.
     *
     * @param process process to kill
     * @param env Map containing environment variable names and values.
     *             Any process running with such envvar's value will be
     *             terminated. Improves localization of child processes.
     */
    void destroy(Process process, Map<String, String> env);
}

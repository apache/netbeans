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

package org.netbeans.modules.cnd.spi.remote;

import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * A factory for HostInfoProvider
 */
public interface HostInfoProviderFactory {

    /**
     * Determines whether this factory is applicable for the given execution environment
     * @param execEnv
     * @return true is this factory is applicable for the given execution environment,
     * otherwiae false
     */
    boolean canCreate(ExecutionEnvironment execEnv);

    /**
     * Creates new HostInfoProvider
     * @param execEnv execution environment to create the provider for
     * @return HostInfoProvider for the given environment or null
     * in the case such environment is not supported by this factory.

     * It must return non-null in the case canCreate with the same environment
     * returned true
     */
    HostInfoProvider create(ExecutionEnvironment execEnv);

}

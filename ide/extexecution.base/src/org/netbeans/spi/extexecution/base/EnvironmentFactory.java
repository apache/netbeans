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

import org.netbeans.api.extexecution.base.Environment;
import org.netbeans.modules.extexecution.base.EnvironmentAccessor;

/**
 * The factory allowing SPI implementors of {@link EnvironmentImplementation}
 * to create its API instances {@link Environment}.
 *
 * @author Petr Hejl
 */
public final class EnvironmentFactory {

    private EnvironmentFactory() {
        super();
    }

    /**
     * Creates the instance of {@link Environment} from its SPI representation.
     *
     * @param impl SPI representation
     * @return the API instance
     */
    public static Environment createEnvironment(EnvironmentImplementation impl) {
        return EnvironmentAccessor.getDefault().createEnvironment(impl);
    }
}

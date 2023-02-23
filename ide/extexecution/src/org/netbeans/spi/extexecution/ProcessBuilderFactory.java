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

import org.netbeans.modules.extexecution.ProcessBuilderAccessor;

/**
 * The factory allowing SPI implementors of {@link ProcessBuilderImplementation}
 * to create its API instances {@link org.netbeans.api.extexecution.ProcessBuilder}.
 *
 * @author Petr Hejl
 * @since 1.28
 * @deprecated use {@link org.netbeans.spi.extexecution.base.ProcessBuilderFactory}
 *             and {@link org.netbeans.spi.extexecution.base.ProcessBuilderImplementation}
 */
@Deprecated
public class ProcessBuilderFactory {

    private ProcessBuilderFactory() {
        super();
    }

    /**
     * Creates the instance of {@link org.netbeans.api.extexecution.ProcessBuilder}
     * from its SPI representation.
     *
     * @param impl SPI representation
     * @param description human readable description of the builder
     * @return the API instance
     */
    public static org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(
            ProcessBuilderImplementation impl, String description) {
        return ProcessBuilderAccessor.getDefault().createProcessBuilder(impl, description);
    }
}

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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.config.ResourceChangeReporterAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter;

/**
 * Factory class to create {@link ResourceChangeReporter} instance. In fact it
 * translates SPI into API class.
 *
 * @author Petr Hejl
 * @since 1.63
 */
public class ResourceChangeReporterFactory {

    /**
     * Creates a {@link ResourceChangeReporter} for the specified
     * {@link ResourceChangeReporterImplementation}.
     *
     * @param impl SPI object
     * @return {@link ResourceChangeReporter} API instance
     */
    @NonNull
    public static ResourceChangeReporter createResourceChangeReporter(ResourceChangeReporterImplementation impl) {
        return ResourceChangeReporterAccessor.getDefault().createResourceChangeReporter(impl);
    }
}

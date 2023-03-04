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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.config.ResourceChangeReporterAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation;

/**
 * API class for indication of changes in resources intended to be deployed
 * to server.
 *
 * @author Petr Hejl
 * @since 1.63
 */
public final class ResourceChangeReporter {

    static {
        ResourceChangeReporterAccessor.setDefault(new ResourceChangeReporterAccessor() {

            @Override
            public ResourceChangeReporter createResourceChangeReporter(ResourceChangeReporterImplementation impl) {
                return new ResourceChangeReporter(impl);
            }
        });
    }

    private final ResourceChangeReporterImplementation impl;

    private ResourceChangeReporter(ResourceChangeReporterImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns <code>true</code> if resource was changed.
     *
     * @param lastDeploy timestamp of the last deploy
     * @return <code>true</code> if resource was changed.
     */
    public boolean isServerResourceChanged(long lastDeploy) {
        return impl.isServerResourceChanged(lastDeploy);
    }
}

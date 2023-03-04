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
package org.netbeans.modules.gradle.execute;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;

/**
 * Internal API to manipulate visible configurations. Exposed from project's Lookup to decouple UI
 * and service layers.
 * @author sdedic
 */
public interface ProjectConfigurationUpdater {
    // incidentally matches ProjectConfigurationProvider.getConfigurations singnature
    
    /**
     * @return current list of configurations. Must include {@link GradleExecConfiguration#DEFAULT}.
     */
    public @NonNull Collection<? extends GradleExecConfiguration>  getConfigurations();
    
    /**
     * Shared configurations. Does not return fixed configurations that have not been customized.
     * @return list of customized shared configurations.
     */
    public @NonNull Collection<GradleExecConfiguration> getSharedConfigurations();

    /**
     * @return list of private configurations.
     */
    public @NonNull Collection<GradleExecConfiguration> getPrivateConfigurations();
    
    /**
     * Returns fixed configurations defined by plugin code. Must include {@link GradleExecConfiguration#DEFAULT}.
     * @return fixed configurations.
     */
    public @NonNull Collection<GradleExecConfiguration> getFixedConfigurations();
    
    /**
     * Updates configuration definitions. Sets the customized shared and private configs to the supplied lists.
     * Any configuration not present in the list(s) will be removed. Configuration can be made shared or private by
     * moving it between the two lists.
     * 
     * @param sharedConfigs shared configurations
     * @param privateConfigs private configurations
     * @throws IOException on I/O error
     */
    public void setConfigurations(@NonNull List<GradleExecConfiguration> sharedConfigs, @NonNull List<GradleExecConfiguration> privateConfigs) throws IOException;
}

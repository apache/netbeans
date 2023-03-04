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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;

/**
 * Configuration useful for setting and getting the web module context root.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface ContextRootConfiguration {
    
    /**
     * Return the web module context root.
     *
     * @return web module context root.
     *
     * @throws ConfigurationException reports errors in getting the web context
     *         root.
     */
    String getContextRoot() throws ConfigurationException;

    /**
     * Set the web context root.
     *
     * @param contextRoot context root to be set.
     *
     * @throws ConfigurationException reports errors in setting the web context
     *         root.
     */
    void setContextRoot(String contextRoot) throws ConfigurationException;
    
}

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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.DeploymentManager;

/**
 * Mandatory factory class for producing {@link J2eePlatformImpl}. Plugin is
 * required to register instance of this class in module layer in the
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code> folder.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public abstract class J2eePlatformFactory {

    /**
     * Return <code>J2eePlatformImpl</code> for the given <code>DeploymentManager</code>.
     *
     * @param dm <code>DeploymentManager</code>.
     */
    public abstract J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm);
    
}

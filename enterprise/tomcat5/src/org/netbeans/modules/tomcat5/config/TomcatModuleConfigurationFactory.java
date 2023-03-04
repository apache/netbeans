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

package org.netbeans.modules.tomcat5.config;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;

/**
 * Tomcat implementation of the ModuleConfigurationFactory.
 * 
 * @author sherold
 */
public class TomcatModuleConfigurationFactory implements ModuleConfigurationFactory2 {
    
    private final TomcatVersion version;

    public TomcatModuleConfigurationFactory() {
        this(null);
    }

    private TomcatModuleConfigurationFactory(TomcatVersion version) {
        this.version = version;
    }
    
    public static TomcatModuleConfigurationFactory create50() {
        return new TomcatModuleConfigurationFactory(TomcatVersion.TOMCAT_50);
    }
    
    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException {
        // XXX is there a better value for unknown tomcat ?
        return new TomcatModuleConfiguration(j2eeModule,
                version != null ? version : TomcatVersion.TOMCAT_90, null);
    }

    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule, String instanceUrl) throws ConfigurationException {
        try {
            TomcatManager manager = (TomcatManager) TomcatFactory.getInstance().getDisconnectedDeploymentManager(instanceUrl);
            return new TomcatModuleConfiguration(j2eeModule, manager.getTomcatVersion(), manager.getTomEEVersion());
        } catch (DeploymentManagerCreationException ex) {
            return create(j2eeModule);
        }
    }
}

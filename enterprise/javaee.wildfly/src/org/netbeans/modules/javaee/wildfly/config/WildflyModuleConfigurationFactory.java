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

package org.netbeans.modules.javaee.wildfly.config;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentFactory;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils.Version;

/**
 * JBoss implementation of the ModuleConfigurationFactory.
 *
 * @author sherold
 */
public class WildflyModuleConfigurationFactory implements ModuleConfigurationFactory2 {

    /** Creates a new instance of JBModuleConfigurationFactory */
    public WildflyModuleConfigurationFactory() {
    }

    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule) throws ConfigurationException {
        if (J2eeModule.Type.WAR.equals(j2eeModule.getType())) {
            return new WarDeploymentConfiguration(j2eeModule);
        } else if (J2eeModule.Type.EJB.equals(j2eeModule.getType())) {
            return new EjbDeploymentConfiguration(j2eeModule);
        } else if (J2eeModule.Type.CAR.equals(j2eeModule.getType())) {
            return new CarDeploymentConfiguration(j2eeModule);
        } else if (J2eeModule.Type.EAR.equals(j2eeModule.getType())) {
            return new EarDeploymentConfiguration(j2eeModule);
        }
        throw new ConfigurationException("Not supported module: " + j2eeModule.getType());
    }

    @Override
    public ModuleConfiguration create(J2eeModule j2eeModule, String instanceUrl) throws ConfigurationException {
        if (!instanceUrl.startsWith(WildflyDeploymentFactory.URI_PREFIX)) {
            return create(j2eeModule);
        }
        try {
            WildflyDeploymentManager dm = (WildflyDeploymentManager) WildflyDeploymentFactory.getInstance().getDisconnectedDeploymentManager(instanceUrl);
            Version version = dm.getServerVersion();
            if (J2eeModule.Type.WAR.equals(j2eeModule.getType())) {
                return new WarDeploymentConfiguration(j2eeModule, version, dm.isWildfly());
            } else if (J2eeModule.Type.EJB.equals(j2eeModule.getType())) {
                return new EjbDeploymentConfiguration(j2eeModule, version, dm.isWildfly());
            } else if (J2eeModule.Type.EAR.equals(j2eeModule.getType())) {
                return new EarDeploymentConfiguration(j2eeModule, version, dm.isWildfly());
            }
        } catch (DeploymentManagerCreationException ex) {
            return create(j2eeModule);
        }
        throw new ConfigurationException("Not supported module: " + j2eeModule.getType());
    }

}

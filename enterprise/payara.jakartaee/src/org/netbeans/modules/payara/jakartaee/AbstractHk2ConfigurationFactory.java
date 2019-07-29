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
package org.netbeans.modules.payara.jakartaee;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.netbeans.modules.payara.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;

/**
 * Abstract factory to construct Java EE server configuration API support object.
 * <p/>
 * @author Vince Kraemer, Tomas Kraus
 */
abstract class AbstractHk2ConfigurationFactory implements ModuleConfigurationFactory2 {
    /** Deployment manager. */
    private final Hk2DeploymentManager hk2dm;

    /**
     * Creates a new instance of {@link PayaraConfigurationFactory
     */
    AbstractHk2ConfigurationFactory(final Hk2DeploymentManager hk2dm) {
        this.hk2dm = hk2dm;
    }

    /**
     * Constructs proper module configuration object without having
     * Payara server.
     * <p/>
     * Proper configuration object is selected depending on Payara specific web
     * application meta data file (<code>WEB-INF/payara-web.xml</code>,
     * <code>WEB-INF/glassfish-web.xml</code> or
     * <code>WEB-INF/sun-web.xml</code>) existence.
     * <p/>
     * @param module Java EE module.
     * @return Module configuration object.
     * @throws ConfigurationException if there is a problem with the server-specific
     *         configuration.
     */
    @Override
    public ModuleConfiguration create(final J2eeModule module)
            throws ConfigurationException {
        ModuleConfiguration retVal = null;
        try {
            if (J2eeModuleHelper.isPayaraWeb(module) || J2eeModuleHelper.isGlassFishWeb(module)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Three1Configuration(module, PayaraVersion.PF_4_1_144), hk2dm);
            } else {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, PayaraVersion.PF_4_1_144), hk2dm);
            }
        } catch (ConfigurationException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new ConfigurationException(module.toString(), ex);
        }
        return retVal;
    }

    /**
     * Constructs proper module configuration object depending on
     * Payara server.
     * <p/>
     * @param module      Java EE module.
     * @param instanceUrl Payara server internal URL.
     * @return Module configuration object.
     * @throws ConfigurationException if there is a problem with the server-specific
     *         configuration.
     */
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public ModuleConfiguration create(final @NonNull J2eeModule module,
            final @NonNull String instanceUrl) throws ConfigurationException {
        ModuleConfiguration retVal = null;
        final PayaraInstance instance
                = PayaraInstanceProvider.getProvider()
                .getPayaraInstance(instanceUrl);
        final PayaraVersion version = instance != null
                ? instance.getVersion() : null;
        try {
            final Hk2DeploymentManager dm = hk2dm != null
                    ? hk2dm
                    : (Hk2DeploymentManager) Hk2DeploymentFactory.createEe(version)
                            .getDisconnectedDeploymentManager(instanceUrl);
            if (version != null
                    && PayaraVersion.ge(version, PayaraVersion.PF_4_1_144)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Three1Configuration(module, version), dm);
            } else {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            }
        } catch (ConfigurationException ce) {
            throw ce;
        } catch (Exception ex) {
            throw new ConfigurationException(module.toString(), ex);
        }
        return retVal;
    }

}

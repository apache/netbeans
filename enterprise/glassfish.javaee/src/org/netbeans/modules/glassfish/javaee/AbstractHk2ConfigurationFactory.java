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
package org.netbeans.modules.glassfish.javaee;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
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
     * Creates a new instance of {@link GlassFishConfigurationFactory
     */
    AbstractHk2ConfigurationFactory(final Hk2DeploymentManager hk2dm) {
        this.hk2dm = hk2dm;
    }

    /**
     * Constructs proper module configuration object without having
     * GlassFish server.
     * <p/>
     * Proper configuration object is selected depending on GlassFish specific
     * web application meta data file (<code>WEB-INFsun-web.xml</code>
     * or <code>WEB-INF/glassfish-web.xml</code>) existence.
     * GlassFish version 3.0 is passed to old module configuration object
     * to rely on {@code sun-resources.xml} resource file.
     * GlassFish version 3.1 is passed to new module configuration object
     * to rely on {@code glassfish-resources.xml} resource file.
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
            if (J2eeModuleHelper.isGlassFishWeb(module)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Three1Configuration(module, GlassFishVersion.GF_3_1), hk2dm);
            } else {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, GlassFishVersion.GF_3), hk2dm);
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
     * GlassFish server.
     * <p/>
     * Proper configuration object is selected depending on GlassFish version.
     * Old module configuration object is created for server before version 3.1
     * and new module configuration object for server version 3.1 and later.
     * <p/>
     * @param module      Java EE module.
     * @param instanceUrl GlassFish server internal URL.
     * @return Module configuration object.
     * @throws ConfigurationException if there is a problem with the server-specific
     *         configuration.
     */
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public ModuleConfiguration create(final @NonNull J2eeModule module,
            final @NonNull String instanceUrl) throws ConfigurationException {
        ModuleConfiguration retVal = null;
        final GlassfishInstance instance
                = GlassfishInstanceProvider.getProvider()
                .getGlassfishInstance(instanceUrl);
        final GlassFishVersion version = instance != null
                ? instance.getVersion() : null;
        try {
            Hk2DeploymentManager evaluatedDm = null;
            if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_8_0_0)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createJakartaEe11()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_7_0_0)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createJakartaEe10()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createJakartaEe91()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createJakartaEe9()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createJakartaEe8()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createEe8()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_4)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createEe7()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else if(version != null && GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createEe6()
                    .getDisconnectedDeploymentManager(instanceUrl);
            } else {
                evaluatedDm = (Hk2DeploymentManager) Hk2DeploymentFactory.createEe8()
                    .getDisconnectedDeploymentManager(instanceUrl);
            }
            final Hk2DeploymentManager dm = hk2dm != null
                    ? hk2dm
                    : evaluatedDm;
            if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_8_0_0)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_7_0_0)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_6_1_0)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_6)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_5_1_0)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_5)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_4)) {
                retVal = new ModuleConfigurationImpl(
                        module, new Hk2Configuration(module, version), dm);
            } else if (version != null
                    && GlassFishVersion.ge(version, GlassFishVersion.GF_3_1)) {
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

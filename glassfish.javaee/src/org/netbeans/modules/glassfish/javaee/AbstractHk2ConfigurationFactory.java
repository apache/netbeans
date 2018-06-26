/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
            final Hk2DeploymentManager dm = hk2dm != null
                    ? hk2dm
                    : (Hk2DeploymentManager) Hk2DeploymentFactory.createEe6()
                            .getDisconnectedDeploymentManager(instanceUrl);
            if (version != null
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

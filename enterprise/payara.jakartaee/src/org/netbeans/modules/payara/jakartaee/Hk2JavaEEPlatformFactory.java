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

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.payara.tooling.data.PayaraJavaEEConfig;
import org.netbeans.modules.payara.tooling.data.PayaraJavaSEConfig;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilder;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.openide.util.NbBundle;


/**
 * Payara JavaEE platform factory.
 * <p/>
 * Creates Payara JavaEE platform instances for individual Payara server
 * instances from deployment manager.
 * <p/>
 * Works as a singleton instance in regular use-cases. Unfortunately
 * <code>layer.xml</code> does not allow to work with singletons so we allow
 * it to create more instances.
 * <p/>
 * @author Tomas Kraus, Vince Kraemer
 */
public class Hk2JavaEEPlatformFactory extends J2eePlatformFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Payara JavaEE platform lookup key.
     */
    private static final String V3_LOOKUP_KEY = "J2EE/DeploymentPlugins/pfv3ee6/Lookup";

    /**
     * Payara V4 JavaEE platform lookup key.
     * <p/>
     * We will keep V3 value now because no one knows what will get broken when
     * changing it.
     */
    private static final String V4_LOOKUP_KEY = "J2EE/DeploymentPlugins/pfv4ee7/Lookup";

    /**
     * Payara V5 JavaEE platform lookup key.
     * <p/>
     * We will keep V3 value now because no one knows what will get broken when
     * changing it.
     */
    private static final String V5_LOOKUP_KEY = "J2EE/DeploymentPlugins/pfv5ee8/Lookup";

    /** Payara JavaEE platform factory singleton object. */
    private static volatile Hk2JavaEEPlatformFactory instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return {@see Hk2JavaEEPlatformFactory} singleton instance.
     */
    public static Hk2JavaEEPlatformFactory getFactory() {
        if (instance != null) {
            return instance;
        }
        synchronized(Hk2JavaEEPlatformFactory.class) {
            if (instance == null) {
                instance = new Hk2JavaEEPlatformFactory();
            }
        }
        return instance;
    }

    /**
     * Get Payara JavaEE platform name from bundle properties for given
     * Payara server version.
     * <p/>
     * @param version Payara server version used to pick up display name.
     * @return Payara JavaEE platform name related to given server version.
     */
    private static String getDisplayName(final PayaraVersion version) {
        final int ord = version.ordinal();
        if (ord >= PayaraVersion.PF_5_181.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V5ServerPlatform");
        } else if (ord >= PayaraVersion.PF_4_1_144.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V4ServerPlatform");
        } else {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V1ServerPlatform");
        }
    }

    /**
     * Get Payara JavaEE library name from bundle properties for given
     * Payara server version.
     * <p/>
     * @param version Payara server version used to pick up display name.
     * @return Payara JavaEE library name related to given server version.
     */
    private static String getLibraryName(final PayaraVersion version) {
        final int ord = version.ordinal();
        if (ord >= PayaraVersion.PF_5_181.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V5ServerLibraries");
        } else if (ord >= PayaraVersion.PF_4_1_144.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V4ServerLibraries");
        } else {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V1ServerLibraries");
        }
    }

    /**
     * Get Payara JavaEE platform lookup key for given Payara
     * server version.
     * <p/>
     * @param version Payara server version used to pick up lookup key.
     * @return Lookup key for given Payara server version.
     */
    private static String getLookupKey(final PayaraVersion version) {
        final int ord = version.ordinal();
        if (ord >= PayaraVersion.PF_5_181.ordinal()) {
            return V5_LOOKUP_KEY;
        } else if (ord >= PayaraVersion.PF_4_1_144.ordinal()) {
            return V4_LOOKUP_KEY;
        } else {
            return V3_LOOKUP_KEY;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // J2eePlatformFactory methods                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return {@see J2eePlatformImpl} for the given {@see DeploymentManager}.
     * <p/>
     * @param dm {@see DeploymentManager} object for which JavaEE platform
     *           environment object is created.
     */
    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(final DeploymentManager dm) {
        if (dm instanceof Hk2DeploymentManager) {
            final PayaraServer server = ((Hk2DeploymentManager)dm)
                    .getCommonServerSupport().getInstance();
            final PayaraVersion version = server.getVersion();
            final ConfigBuilder cb = ConfigBuilderProvider.getBuilder(server);
            final PayaraJavaSEConfig javaSEConfig = cb.getJavaSEConfig(version);
            final PayaraJavaEEConfig javaEEConfig = cb.getJavaEEConfig(version);
            final String[] platforms = Hk2JavaEEPlatformImpl.nbJavaSEProfiles(
                    javaSEConfig.getPlatforms());
            final Profile[] profiles = Hk2JavaEEPlatformImpl
                    .nbJavaEEProfiles(javaEEConfig.getProfiles());
            final J2eeModule.Type[] types = Hk2JavaEEPlatformImpl
                    .nbModuleTypes(javaEEConfig.getModuleTypes());
            return new Hk2JavaEEPlatformImpl((Hk2DeploymentManager)dm,
                    platforms, profiles, types, getDisplayName(version),
                    getLibraryName(version), getLookupKey(version));
        }
        throw new IllegalArgumentException(
                "Deployment manager instance is not instance  of Hk2DeploymentManager");
    }

}

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

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.glassfish.tooling.data.GlassFishJavaEEConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishJavaSEConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.server.config.ConfigBuilder;
import org.netbeans.modules.glassfish.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.openide.util.NbBundle;


/**
 * GlassFish JavaEE platform factory.
 * <p/>
 * Creates GlassFish JavaEE platform instances for individual GlassFish server
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

    /** GlassFish V3 JavaEE platform lookup key. */
    private static final String V3_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv3ee6/Lookup";

    /** GlassFish V4 JavaEE platform lookup key. */
    private static final String V4_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv4ee7/Lookup";

    /** GlassFish V5 JavaEE platform lookup key. */
    private static final String V5_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv5ee8/Lookup";
    private static final String V510_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv510ee8/Lookup";
    
    /** GlassFish V6 JakartaEE platform lookup key. */
    private static final String V6_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv6ee9/Lookup";
    private static final String V610_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv610ee9/Lookup";
    
    /** GlassFish V7 JakartaEE platform lookup key. */
    private static final String V7_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv700ee10/Lookup";
    
    /** GlassFish V8 JakartaEE platform lookup key. */
    private static final String V8_LOOKUP_KEY = "J2EE/DeploymentPlugins/gfv800ee11/Lookup";

    /** GlassFish JavaEE platform factory singleton object. */
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
     * Get GlassFish Java/Jakarta EE platform name from bundle properties 
     * for given GlassFish server version.
     * <p/>
     * @param version GlassFish server version used to pick up display name.
     * @return GlassFish JavaEE platform name related to given server version.
     */
    private static String getDisplayName(final GlassFishVersion version) {
        final int ord = version.ordinal();
        if(ord >= GlassFishVersion.GF_8_0_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V800ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_7_0_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V700ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_6_2_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V620ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_6_1_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V610ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_6.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V6ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_5_1_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V51ServerPlatform");
        } else if(ord >= GlassFishVersion.GF_5.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V5ServerPlatform");
        } else if (ord >= GlassFishVersion.GF_4.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V4ServerPlatform");
        } else if (ord >= GlassFishVersion.GF_3.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V3ServerPlatform");
        // We do not support V1 and V2 servers so this should never be used.
        } else if (ord >= GlassFishVersion.GF_2.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V2ServerPlatform");
        } else {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "MSG_V1ServerPlatform");
        }
    }

    /**
     * Get GlassFish Java/Jakarta EE library name from bundle properties
     * for given GlassFish server version.
     * <p/>
     * @param version GlassFish server version used to pick up display name.
     * @return GlassFish JavaEE library name related to given server version.
     */
    private static String getLibraryName(final GlassFishVersion version) {
        final int ord = version.ordinal();
        if (ord >= GlassFishVersion.GF_8_0_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V800ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_7_0_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V700ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_6_2_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V620ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_6_1_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V610ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_6.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V6ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_5_1_0.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V51ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_5.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V5ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_4.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V4ServerLibraries");
        } else if (ord >= GlassFishVersion.GF_3.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V3ServerLibraries");
        // We do not support V1 and V2 servers so this should never be used.
        } else if (ord >= GlassFishVersion.GF_2.ordinal()) {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V2ServerLibraries");
        } else {
            return NbBundle.getMessage(
                    Hk2JavaEEPlatformFactory.class, "LBL_V1ServerLibraries");
        }
    }

    /**
     * Get GlassFish Java/Jakarta EE platform lookup key for given GlassFish
     * server version.
     * <p/>
     * @param version GlassFish server version used to pick up lookup key.
     * @return Lookup key for given GlassFish server version.
     */
    private static String getLookupKey(final GlassFishVersion version) {
        final int ord = version.ordinal();
        if (ord >= GlassFishVersion.GF_8_0_0.ordinal()){
            return V8_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_7_0_0.ordinal()){
            return V7_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_6_1_0.ordinal()){
            return V610_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_6.ordinal()){
            return V6_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_5_1_0.ordinal()){
            return V510_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_5.ordinal()) {
            return V5_LOOKUP_KEY;
        } else if (ord >= GlassFishVersion.GF_4.ordinal()) {
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
            final GlassFishServer server = ((Hk2DeploymentManager)dm)
                    .getCommonServerSupport().getInstance();
            final GlassFishVersion version = server.getVersion();
            final ConfigBuilder cb = ConfigBuilderProvider.getBuilder(server);
            final GlassFishJavaSEConfig javaSEConfig = cb.getJavaSEConfig(version);
            final GlassFishJavaEEConfig javaEEConfig = cb.getJavaEEConfig(version);
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

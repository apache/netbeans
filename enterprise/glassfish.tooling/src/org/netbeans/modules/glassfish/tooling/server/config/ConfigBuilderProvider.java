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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;

/**
 * Configuration builder provider.
 * <p/>
 * This class is responsible for handling providers for individual server
 * instances. Because {@link ConfigBuilder} class instance shall not be used
 * for multiple GlassFish server versions there must be one configuration class
 * instance for every single GlassFish server version.
 * Also every single server instance has it's own directory structure which
 * is used to search for modules. Because of that every single GlassFish server
 * instance must have it's own configuration builder.
 * Configuration builder is created with first request for given server version
 * and reused for every subsequent request.
 * <p/>
 * @author Tomas Kraus
 */
public class ConfigBuilderProvider {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Library builder default configuration file. */
    private static final URL CONFIG_V3
            = ConfigBuilderProvider.class.getResource("GlassFishV3.xml");

    /** Library builder configuration since GlassFish 4. */
    private static final Config.Next CONFIG_V4
            = new Config.Next(GlassFishVersion.GF_4,
            ConfigBuilderProvider.class.getResource("GlassFishV4.xml"));

    /** Library builder configuration since GlassFish 4.1. */
    private static final Config.Next CONFIG_V4_1
            = new Config.Next(GlassFishVersion.GF_4_1,
            ConfigBuilderProvider.class.getResource("GlassFishV4_1.xml"));

    /** Library builder configuration since GlassFish 5. */
    private static final Config.Next CONFIG_V5
            = new Config.Next(GlassFishVersion.GF_5,
            ConfigBuilderProvider.class.getResource("GlassFishV5.xml"));

    /** Library builder configuration since GlassFish 5.0.1. */
    private static final Config.Next CONFIG_V5_0_1
            = new Config.Next(GlassFishVersion.GF_5_0_1,
            ConfigBuilderProvider.class.getResource("GlassFishV5.xml"));

    /** Library builder configuration since GlassFish 5.1. */
    private static final Config.Next CONFIG_V5_1
            = new Config.Next(GlassFishVersion.GF_5_1_0,
            ConfigBuilderProvider.class.getResource("GlassFishV5_1_0.xml"));

    /** Library builder configuration since GlassFish 6.0. */
    private static final Config.Next CONFIG_V6
            = new Config.Next(GlassFishVersion.GF_6,
            ConfigBuilderProvider.class.getResource("GlassFishV6.xml"));

    /** Library builder configuration since GlassFish 6.1.0. */
    private static final Config.Next CONFIG_V6_1_0
            = new Config.Next(GlassFishVersion.GF_6_1_0,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_1_0.xml"));

    /** Library builder configuration since GlassFish 6.2.0. */
    private static final Config.Next CONFIG_V6_2_0
            = new Config.Next(GlassFishVersion.GF_6_2_0,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_0.xml"));
    
    /** Library builder configuration since GlassFish 6.2.1. */
    private static final Config.Next CONFIG_V6_2_1
            = new Config.Next(GlassFishVersion.GF_6_2_1,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_1.xml"));
    
    /** Library builder configuration since GlassFish 6.2.2. */
    private static final Config.Next CONFIG_V6_2_2
            = new Config.Next(GlassFishVersion.GF_6_2_2,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_2.xml"));
    
    /** Library builder configuration since GlassFish 6.2.3. */
    private static final Config.Next CONFIG_V6_2_3
            = new Config.Next(GlassFishVersion.GF_6_2_3,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_3.xml"));
    
    /** Library builder configuration since GlassFish 6.2.4. */
    private static final Config.Next CONFIG_V6_2_4
            = new Config.Next(GlassFishVersion.GF_6_2_4,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_4.xml"));
    
    /** Library builder configuration since GlassFish 6.2.5. */
    private static final Config.Next CONFIG_V6_2_5
            = new Config.Next(GlassFishVersion.GF_6_2_5,
                    ConfigBuilderProvider.class.getResource("GlassFishV6_2_4.xml"));
    
    /** Library builder configuration since GlassFish 7.0.0. */
    private static final Config.Next CONFIG_V7_0_0
            = new Config.Next(GlassFishVersion.GF_7_0_0,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_0.xml"));
    
    /** Library builder configuration since GlassFish 7.0.1. */
    private static final Config.Next CONFIG_V7_0_1
            = new Config.Next(GlassFishVersion.GF_7_0_1,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));
    
    /** Library builder configuration since GlassFish 7.0.2. */
    private static final Config.Next CONFIG_V7_0_2
            = new Config.Next(GlassFishVersion.GF_7_0_2,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));

    /** Library builder configuration since GlassFish 7.0.3. */
    private static final Config.Next CONFIG_V7_0_3
            = new Config.Next(GlassFishVersion.GF_7_0_3,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));

    /** Library builder configuration since GlassFish 7.0.4. */
    private static final Config.Next CONFIG_V7_0_4
            = new Config.Next(GlassFishVersion.GF_7_0_4,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));

    /** Library builder configuration since GlassFish 7.0.5. */
    private static final Config.Next CONFIG_V7_0_5
            = new Config.Next(GlassFishVersion.GF_7_0_5,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));

    /** Library builder configuration since GlassFish 7.0.6. */
    private static final Config.Next CONFIG_V7_0_6
            = new Config.Next(GlassFishVersion.GF_7_0_6,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));
    
    /** Library builder configuration since GlassFish 7.0.7. */
    private static final Config.Next CONFIG_V7_0_7
            = new Config.Next(GlassFishVersion.GF_7_0_7,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));
    
    /** Library builder configuration since GlassFish 7.0.8. */
    private static final Config.Next CONFIG_V7_0_8
            = new Config.Next(GlassFishVersion.GF_7_0_8,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_1.xml"));
    
    /** Library builder configuration since GlassFish 7.0.9. */
    private static final Config.Next CONFIG_V7_0_9
            = new Config.Next(GlassFishVersion.GF_7_0_9,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_9.xml"));
    
    /** Library builder configuration since GlassFish 7.0.10. */
    private static final Config.Next CONFIG_V7_0_10
            = new Config.Next(GlassFishVersion.GF_7_0_10,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_9.xml"));
    
    /** Library builder configuration since GlassFish 7.0.11. */
    private static final Config.Next CONFIG_V7_0_11
            = new Config.Next(GlassFishVersion.GF_7_0_11,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_9.xml"));

    /** Library builder configuration since GlassFish 7.0.12. */
    private static final Config.Next CONFIG_V7_0_12
            = new Config.Next(GlassFishVersion.GF_7_0_12,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_9.xml"));

    /** Library builder configuration since GlassFish 7.0.13. */
    private static final Config.Next CONFIG_V7_0_13
            = new Config.Next(GlassFishVersion.GF_7_0_13,
                    ConfigBuilderProvider.class.getResource("GlassFishV7_0_9.xml"));

    /** Library builder configuration since GlassFish 8.0.0. */
    private static final Config.Next CONFIG_V8_0_0
            = new Config.Next(GlassFishVersion.GF_8_0_0,
                    ConfigBuilderProvider.class.getResource("GlassFishV8_0_0.xml"));

    /** Library builder configuration for GlassFish cloud. */
    private static final Config config
            = new Config(CONFIG_V3, CONFIG_V4, CONFIG_V4_1, CONFIG_V5, 
                         CONFIG_V5_0_1, CONFIG_V5_1, CONFIG_V6, CONFIG_V6_1_0,
                         CONFIG_V6_2_0, CONFIG_V6_2_1, CONFIG_V6_2_2, 
                         CONFIG_V6_2_3, CONFIG_V6_2_4, CONFIG_V6_2_5,
                         CONFIG_V7_0_0, CONFIG_V7_0_1, CONFIG_V7_0_2,
                         CONFIG_V7_0_3, CONFIG_V7_0_4, CONFIG_V7_0_5,
                         CONFIG_V7_0_6, CONFIG_V7_0_7, CONFIG_V7_0_8,
                         CONFIG_V7_0_9, CONFIG_V7_0_10, CONFIG_V7_0_11,
                         CONFIG_V7_0_12, CONFIG_V7_0_13, CONFIG_V8_0_0);

    /** Builders array for each server instance. */
    private static final ConcurrentMap<GlassFishServer, ConfigBuilder> builders
            = new ConcurrentHashMap<>(32);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get library builder configuration for given GlassFish server version.
     * <p/>
     * @param version GlassFish server version.
     * @return Library builder configuration for given GlassFish server version.
     */
    public static URL getBuilderConfig(final GlassFishVersion version) {
        return config.configFiles[config.index[version.ordinal()]];
    }

    /**
     * Get configuration builder instance for given GlassFish server entity
     * instance.
     * <p/>
     * @param server GlassFish server entity for which builder is returned.
     * <p/>
     * @return Configuration builder for given GlassFish server entity.
     * @throws ServerConfigException when there is no version ser in GlassFish
     *         server entity object or this object is null.
     */
    public static ConfigBuilder getBuilder(final GlassFishServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "GlassFish server entity shall not be null");
        }
        String serverHome = server.getServerHome();
        return builders.computeIfAbsent(server, key -> 
                new ConfigBuilder(config, serverHome, serverHome, serverHome));
    }
    

    /**
     * Remove configuration builder instance for given GlassFish server entity
     * instance.
     * <p/>
     * Allows to free resources when configuration builder instance will no more
     * be needed (e.g. GlassFish server entity is being destroyed).
     * <p/>
     * @param server GlassFish server entity for which builder is destroyed.
     */
    public static void destroyBuilder(final GlassFishServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "GlassFish server entity shall not be null");
        }
        builders.remove(server);
    }

}

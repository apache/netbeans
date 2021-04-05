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
package org.netbeans.modules.payara.tooling.server.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Configuration builder provider.
 * <p/>
 * This class is responsible for handling providers for individual server
 * instances. Because {@link ConfigBuilder} class instance shall not be used
 * for multiple Payara server versions there must be one configuration class
 * instance for every single Payara server version.
 * Also every single server instance has it's own directory structure which
 * is used to search for modules. Because of that every single Payara server
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
            = ConfigBuilderProvider.class.getResource("PayaraV3.xml");

    /** Library builder configuration since Payara 4.1. */
    private static final Config.Next CONFIG_V4_1
            = new Config.Next(PayaraVersion.PF_4_1_144,
            ConfigBuilderProvider.class.getResource("PayaraV4.xml"));

    /** Library builder configuration since Payara 5.181 */
    private static final Config.Next CONFIG_V5_181
            = new Config.Next(PayaraVersion.PF_5_181,
            ConfigBuilderProvider.class.getResource("PayaraV5.xml"));

    /** Library builder configuration since Payara 5.192 */
    private static final Config.Next CONFIG_V5_192
            = new Config.Next(PayaraVersion.PF_5_192,
            ConfigBuilderProvider.class.getResource("PayaraV5_192.xml"));

    /** Library builder configuration for Payara. */
    private static final Config config
            = new Config(CONFIG_V3, CONFIG_V4_1, CONFIG_V5_181, CONFIG_V5_192);

    /** Builders array for each server instance. */
    private static final Map<PayaraServer, ConfigBuilder> builders
            = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get library builder configuration for given Payara server version.
     * <p/>
     * @param version Payara server version.
     * @return Library builder configuration for given Payara server version.
     */
    public static URL getBuilderConfig(final PayaraVersion version) {
        return config.configFiles[config.index[version.ordinal()]];
    }

    /**
     * Get configuration builder instance for given Payara server entity
     * instance.
     * <p/>
     * @param server Payara server entity for which builder is returned.
     * <p/>
     * @return Configuration builder for given Payara server entity.
     * @throws ServerConfigException when there is no version ser in Payara
     *         server entity object or this object is null.
     */
    public static ConfigBuilder getBuilder(final PayaraServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "Payara server entity shall not be null");
        }
        ConfigBuilder builder;
        synchronized (builders) {
            builder = builders.get(server);
            if (builder != null) {
                return builder;
            }
            String serverHome = server.getServerHome();
            builder = new ConfigBuilder(config, serverHome, serverHome, serverHome);
            builders.put(server, builder);
        }
        return builder;
    }


    /**
     * Remove configuration builder instance for given Payara server entity
     * instance.
     * <p/>
     * Allows to free resources when configuration builder instance will no more
     * be needed (e.g. Payara server entity is being destroyed).
     * <p/>
     * @param server Payara server entity for which builder is destroyed.
     */
    public static void destroyBuilder(final PayaraServer server) {
        if (server == null) {
            throw new ServerConfigException(
                    "Payara server entity shall not be null");
        }
        synchronized (builders) {
            builders.remove(server);
        }
    }

}

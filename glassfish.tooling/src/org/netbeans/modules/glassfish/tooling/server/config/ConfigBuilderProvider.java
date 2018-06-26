/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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

    /** Library builder configuration for GlassFish cloud. */
    private static final Config config
            = new Config(CONFIG_V3, CONFIG_V4, CONFIG_V4_1);

    /** Builders array for each server instance. */
    private static final Map<GlassFishServer, ConfigBuilder> builders
            = new HashMap<>();

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
        synchronized (builders) {
            builders.remove(server);
        }
    }

}

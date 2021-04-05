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
import org.netbeans.modules.payara.tooling.data.PayaraVersion;

/**
 * Library builder configuration.
 * <p/>
 * Stores library configuration files mapped to Payara versions.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Config {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Class used to pass library builder configuration for next (newer)
     * Payara versions to library builder configuration constructor.
     * <p/>
     * Contains pair of Payara version and related libraries configuration
     * file to define configuration file change points in version sequence.
     */
    public static class Next {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Libraries XML configuration file. */
        URL configFile;

        /** Payara version. */
        PayaraVersion version;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of libraries configuration for given version.
         * <p/>
         * @param version        Payara version.
         * @param configFile     Libraries XML configuration file associated
         *                       to given version.
         */
        public Next(PayaraVersion version, URL configFile) {
            this.configFile = configFile;
            this.version = version;
        }

    }

    ////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                //
    ////////////////////////////////////////////////////////////////////////

    /** Configuration files. */
    final URL[] configFiles;

    /** Version to configuration file mapping table. */
    final int[] index;

    
    /**
     * Creates an instance of library builder configuration.
     * <p/>
     * @param defaultConfig Default libraries configuration file.
     * @param nextConfig    Next libraries configuration file(s) starting from
     *                      provided version. Versions must be passed
     *                      in ascending order.
     */
    public Config(URL defaultConfig,
            Next... nextConfig) {
        int indexSize
                = nextConfig == null ? 1 : nextConfig.length + 1;        
        index = new int[PayaraVersion.length];        
        configFiles = new URL[indexSize];
        int i = 0;
        configFiles[i] = defaultConfig;
        Next config = nextConfig != null && i < nextConfig.length
                ? nextConfig[i] : null;
        for (PayaraVersion version : PayaraVersion.values()) {
            int versionIndex = version.ordinal();
            if (config != null
                    && config.version.ordinal() <= version.ordinal()) {
                configFiles[++i] = config.configFile;
                config = i < nextConfig.length
                        ? nextConfig[i] : null;
            }
            index[versionIndex] = i;
        }
    }

}

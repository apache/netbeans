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
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;

/**
 * Library builder configuration.
 * <p/>
 * Stores library configuration files mapped to GlassFish versions.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Config {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Class used to pass library builder configuration for next (newer)
     * GlassFish versions to library builder configuration constructor.
     * <p/>
     * Contains pair of GlassFish version and related libraries configuration
     * file to define configuration file change points in version sequence.
     */
    public static class Next {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Libraries XML configuration file. */
        URL configFile;

        /** GlassFish version. */
        GlassFishVersion version;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of libraries configuration for given version.
         * <p/>
         * @param version        GlassFish version.
         * @param configFile     Libraries XML configuration file associated
         *                       to given version.
         */
        public Next(GlassFishVersion version, URL configFile) {
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
        index = new int[GlassFishVersion.length];        
        configFiles = new URL[indexSize];
        int i = 0;
        configFiles[i] = defaultConfig;
        Next config = nextConfig != null && i < nextConfig.length
                ? nextConfig[i] : null;
        for (GlassFishVersion version : GlassFishVersion.values()) {
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

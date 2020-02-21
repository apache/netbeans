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

package org.netbeans.modules.cnd.makeproject.spi.configurations;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class PkgConfigManager {
    private static final PkgConfigManager DEFAULT = new Default();

    public abstract PkgConfig getPkgConfig(ExecutionEnvironment env, MakeConfiguration conf);

    protected PkgConfigManager() {
    }

    /**
     * Static method to obtain the PkgConfig implementation.
     * @return the PkgConfig
     */
    public static synchronized PkgConfigManager getDefault() {
        return DEFAULT;
    }
    
    public static interface PkgConfig {
        PackageConfiguration getPkgConfig(String pkg);
        List<PackageConfiguration> getAvaliablePkgConfigs();
        Collection<ResolvedPath> getResolvedPath(String include);
    }

    public static interface PackageConfiguration {
        String getName();
        String getDisplayName();
        String getVersion();
        Collection<String> getIncludePaths();
        Collection<String> getMacros();
        String getLibs();
    }

    public static interface ResolvedPath {
        String getIncludePath();
        Collection<PackageConfiguration> getPackages();
    }

    /**
     * Implementation of the default PkgConfig
     */
    private static final class Default extends PkgConfigManager {
        private final Lookup.Result<PkgConfigManager> res;
        private static final boolean FIX_SERVICE = true;
        private PkgConfigManager fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(PkgConfigManager.class);
        }

        private PkgConfigManager getService(){
            PkgConfigManager service = fixedSelector;
            if (service == null) {
                for (PkgConfigManager selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    fixedSelector = service;
                }
            }
            return service;
        }

        @Override
        public PkgConfig getPkgConfig(ExecutionEnvironment env, MakeConfiguration conf) {
            PkgConfigManager service = getService();
            if (service != null) {
                return service.getPkgConfig(env, conf);
            }
            return null;
        }
    }
}


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

import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CompileOptionsProvider {
    private static final CompileOptionsProvider DEFAULT = new Default();

    public abstract void onRename(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, String newName);
    public abstract void onRemove(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration);
    public abstract AllOptionsProvider getOptions(Item item);
    
    protected CompileOptionsProvider() {
    }

    /**
     * Static method to obtain the PkgConfig implementation.
     * @return the PkgConfig
     */
    public static synchronized CompileOptionsProvider getDefault() {
        return DEFAULT;
    }
    
    /**
     * Implementation of the default CompileOptionsProvider
     */
    private static final class Default extends CompileOptionsProvider {
        private final Lookup.Result<CompileOptionsProvider> res;
        private static final boolean FIX_SERVICE = true;
        private CompileOptionsProvider fixedSelector;
        Default() {
            res = Lookup.getDefault().lookupResult(CompileOptionsProvider.class);
        }

        private CompileOptionsProvider getService(){
            CompileOptionsProvider service = fixedSelector;
            if (service == null) {
                for (CompileOptionsProvider selector : res.allInstances()) {
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
        public AllOptionsProvider getOptions(Item item) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                return service.getOptions(item);
            }
            return null;
        }

        @Override
        public void onRename(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, String newName) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                service.onRename(cd, makeConfiguration, newName);
            }
        }

        @Override
        public void onRemove(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration) {
            CompileOptionsProvider service = getService();
            if (service != null) {
                service.onRemove(cd, makeConfiguration);
            }
        }
    }
}

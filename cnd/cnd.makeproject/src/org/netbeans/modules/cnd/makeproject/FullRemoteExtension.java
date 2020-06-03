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
package org.netbeans.modules.cnd.makeproject;

import java.util.Collection;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.FullRemoteExtensionProvider;
import org.openide.util.Lookup;

/**
 *
 */
public class FullRemoteExtension {

    private static final Collection<? extends FullRemoteExtensionProvider> PROVIDERS = 
            Lookup.getDefault().lookupAll(FullRemoteExtensionProvider.class);
    
    private FullRemoteExtension() {
    }
    
    public static void configurationSaving(MakeConfigurationDescriptor makeConfigurationDescriptor) {
        for (FullRemoteExtensionProvider provider : PROVIDERS) {
            if (!provider.configurationSaving(makeConfigurationDescriptor)) {
                break;
            }
        }
        
    }

    public static void configurationSaved(MakeConfigurationDescriptor makeConfigurationDescriptor, boolean success) {
        for (FullRemoteExtensionProvider provider : PROVIDERS) {
            if (!provider.configurationSaved(makeConfigurationDescriptor, success)) {
                break;
            }
        }
    }
    
    public static boolean canChangeHost(MakeConfiguration makeConfiguration) {
        for (FullRemoteExtensionProvider provider : PROVIDERS) {
            if (provider.canChangeHost(makeConfiguration)) {
                return true;
            }
        }
        return false;
    }
}

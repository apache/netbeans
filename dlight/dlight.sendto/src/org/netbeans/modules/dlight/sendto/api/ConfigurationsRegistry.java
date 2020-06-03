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

package org.netbeans.modules.dlight.sendto.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class ConfigurationsRegistry {

    private final static ConfigurationsRegistry instance;
    private final ConfigurationsModel model = ConfigurationsModel.getDefault();

    static {
        instance = new ConfigurationsRegistry();
        restore();
    }

    private ConfigurationsRegistry() {
    }

    public static ConfigurationsRegistry getDefault() {
        return instance;
    }

    public static void update(ConfigurationsModel newModel) {
        instance.model.setDataFrom(newModel);
    }

    public static void store() {
        try {
            Preferences registry = NbPreferences.forModule(ConfigurationsRegistry.class);
            registry.clear();
            int h_idx = 1;

            for (Configuration cfg : instance.model.getConfigurations()) {
                String prefix = Integer.toString(h_idx++) + '_';
                Map<String, String> properties = cfg.getProperties();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    registry.put(prefix + entry.getKey(), entry.getValue());
                }
            }

            registry.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void restore() {
        try {
            Preferences registry = NbPreferences.forModule(ConfigurationsRegistry.class);

            if (registry.keys().length == 0) {
                // First start... copy some template...
                registry.absolutePath();
                InputStream is = ConfigurationsRegistry.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/sendto/resources/initialConfiguration"); // NOI18N

                if (is == null) {
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s;
                while ((s = br.readLine()) != null) {
                    int idx = s.indexOf('=');
                    if (idx > 0) {
                        registry.put(s.substring(0, idx), s.substring(idx + 1).replace("\\n", "\n")); // NOI18N
                    }
                }
            }

            HashMap<String, Configuration> configs = new HashMap<String, Configuration>();

            for (String key : registry.keys()) {
                int idx = key.indexOf('_');

                if (idx < 0) {
                    // Should not happen
                    continue;
                }
                
                String prefix = key.substring(0, idx);

                Configuration cfg;

                if (!configs.containsKey(prefix)) {
                    configs.put(prefix, new Configuration());
                }

                cfg = configs.get(prefix);
                String property = key.substring(idx + 1);
                String val = registry.get(key, "<error>"); // NOI18N
                cfg.set(property, val);
            }

            for (Configuration cfg : configs.values()) {
                instance.model.add(cfg);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static ConfigurationsModel getModelCopy() {
        return (ConfigurationsModel) instance.model.clone();
    }

    public static List<Configuration> getConfigurations() {
        return instance.model.getConfigurations();
    }
}

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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;

/**
 * Cache for configuration values with support for hierarchical keys and change
 * notifications
 *
 * @author atalati
 */
public class ConfigValueCache {

    private final ConcurrentHashMap<String, Object> rootCache = new ConcurrentHashMap<>();
    private static final String ROOT_KEY_VALUE = "";
    private static final Logger LOG = Logger.getLogger(ConfigValueCache.class.getName());

    public void registerListener(String section, BiConsumer<String, JsonElement> listener) {
        ConfigData configData = getCachedConfigData(section);
        if (configData != null) {
            configData.setConsumer(listener);
        } else {
            setConfigInTree(section, new ConfigData(listener));
        }
    }

    public JsonElement getConfigValue(String section, String scope) {
        ConfigData configData = getCachedConfigData(section);
        if (configData == null) {
            return null;
        } else if (scope == null) {
            return configData.getRootValue();
        }
        return configData.getScopedValue(scope);
    }

    public void updateCache(NbCodeLanguageClient client, String section, Object cacheValue, JsonElement tree) {
        if (tree == null || cacheValue == null) {
            return;
        }

        ConfigData rootData;
        ConfigValueCache currentCache = null;

        if (isConfigDataInstance(cacheValue)) {
            rootData = (ConfigData) cacheValue;
        } else if (isConfigValueInstance(cacheValue)) {
            currentCache = (ConfigValueCache) cacheValue;
            rootData = currentCache.getRootCacheValue();
        } else {
            return;
        }

        if (rootData != null) {
            rootData.setRootValue(tree);
            try {
                BiConsumer<String, JsonElement> consumer = rootData.getConsumer();
                if (consumer != null) {
                    consumer.accept(section, tree);
                }
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Exception occurred while calling config change consumer handler, config: {0} and excpetion: {1}", new Object[]{section, e.getMessage()});
            }
            Map<String, JsonElement> scopedValuesMap = rootData.getAllScopedValues();
            if (scopedValuesMap != null && !scopedValuesMap.isEmpty()) {
                List<ConfigurationItem> configs = new ArrayList<>();
                scopedValuesMap.forEach((key, value) -> {
                    ConfigurationItem item = new ConfigurationItem();
                    item.setScopeUri(key);
                    item.setSection(section);
                    configs.add(item);
                });

                client.configuration(new ConfigurationParams(configs))
                        .thenAccept(results -> {
                            if (results != null) {
                                for (int i = 0; i < results.size(); i++) {
                                    if (results.get(i) != null) {
                                        String scopeUri = configs.get(i).getScopeUri();
                                        rootData.setScopedValue(scopeUri, (JsonElement) results.get(i));
                                    }
                                }
                            }
                        });
            }
        }

        if (currentCache != null && tree.isJsonObject()) {
            if (currentCache.getRootCacheValue() != null) {
                currentCache.getRootCacheValue().setRootValue(tree);
            }
            Map<String, JsonElement> entries
                    = new HashMap<>(tree.getAsJsonObject().asMap());
            for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
                Object child = currentCache.getCacheData(entry.getKey());
                String newSection = section != null ? section + "." + entry.getKey() : entry.getKey();
                updateCache(client, newSection, child, entry.getValue());
            }
        }
    }

    public void cacheConfigValue(String section, JsonElement value, String scope) {
        registerCache(section);
        ConfigData configData = getCachedConfigData(section);
        if (configData != null) {
            if (scope != null) {
                configData.setScopedValue(scope, value);
            } else {
                configData.setRootValue(value);
            }
        }
    }
    
    // Method used only in unit test
    protected void registerCache(String section){
        ConfigData configData = getCachedConfigData(section);
        if (configData == null) {
            setConfigInTree(section, new ConfigData());
        }
    }
    
    // Method used only in unit test
    protected ConfigData getConfigData(String section) {
        return getCachedConfigData(section);
    }

    private ConfigData getCachedConfigData(String section) {
        if (section == null || section.isEmpty()) {
            return null;
        }

        Object current = this;
        String[] parts = section.split("\\.");

        for (int i = 0; i <= parts.length; i++) {
            if (!isConfigValueInstance(current)) {
                return i == parts.length ? (ConfigData) current : null;
            }
            if (i == parts.length) {
                break;
            }

            ConfigValueCache currentCache = (ConfigValueCache) current;
            current = currentCache.getCacheData(parts[i]);

            if (current == null) {
                return null;
            }
        }

        if (isConfigValueInstance(current)) {
            return ((ConfigValueCache) current).getRootCacheValue();
        }

        return isConfigDataInstance(current) ? (ConfigData) current : null;
    }

    private Object getCacheData(String section) {
        return rootCache.get(section);
    }

    private ConfigData getRootCacheValue() {
        return (ConfigData) rootCache.get(ROOT_KEY_VALUE);
    }

    private void setRootCacheValue(ConfigData configData) {
        rootCache.put(ROOT_KEY_VALUE, configData);
    }

    private Object put(String section, Object value) {
        return rootCache.merge(section, value, (oldValue, newValue) -> {
            if (value == null) {
                return null;
            } else if (isConfigValueInstance(oldValue) && isConfigDataInstance(newValue)) {
                ((ConfigValueCache) oldValue).setRootCacheValue((ConfigData) newValue);
                return oldValue;
            } else if (isConfigDataInstance(oldValue) && isConfigValueInstance(newValue)) {
                ((ConfigValueCache) newValue).setRootCacheValue((ConfigData) oldValue);
            }
            return newValue;
        });
    }

    private void setConfigInTree(String section, ConfigData configData) {
        if (section == null || section.isEmpty() || configData == null) {
            return;
        }

        Object current = this;
        String[] parts = section.split("\\.");

        for (String part : Arrays.asList(parts).subList(0, parts.length - 1)) {
            if (!isConfigValueInstance(current)) {
                return;
            }

            Object child = ((ConfigValueCache) current).getCacheData(part);
            if (isConfigValueInstance(child)) {
                current = child;
            } else {
                ConfigValueCache configValueCache = new ConfigValueCache();
                if (child != null) {
                    configValueCache.setRootCacheValue((ConfigData) child);
                }
                current = ((ConfigValueCache) current).put(part, configValueCache);
            }
        }
        if (!isConfigValueInstance(current)) {
            return;
        }
        ((ConfigValueCache) current).put(parts[parts.length - 1], configData);
    }

    private boolean isConfigValueInstance(Object o) {
        return o instanceof ConfigValueCache;
    }

    private boolean isConfigDataInstance(Object o) {
        return o instanceof ConfigData;
    }

    // protected due to unit test requirements
    protected class ConfigData {

        private JsonElement rootValue;
        private BiConsumer<String, JsonElement> consumer;
        private final ConcurrentHashMap<String, JsonElement> scopedValues = new ConcurrentHashMap<>();

        public ConfigData() {
        }

        public ConfigData(BiConsumer<String, JsonElement> consumer) {
            this(null, consumer);
        }

        public ConfigData(JsonElement value) {
            this(value, null);
        }

        public ConfigData(JsonElement value, BiConsumer<String, JsonElement> consumer) {
            this.rootValue = value;
            this.consumer = consumer;
        }

        public JsonElement getRootValue() {
            return rootValue;
        }

        public void setRootValue(JsonElement value) {
            this.rootValue = value;
        }

        public BiConsumer<String, JsonElement> getConsumer() {
            return consumer;
        }

        public void setConsumer(BiConsumer<String, JsonElement> consumer) {
            this.consumer = consumer;
        }

        public JsonElement getScopedValue(String fo) {
            return scopedValues.get(fo);
        }

        public void setScopedValue(String fo, JsonElement value) {
            this.scopedValues.put(fo, value);
        }

        public Map<String, JsonElement> getAllScopedValues() {
            return Collections.unmodifiableMap(new HashMap<>(scopedValues));
        }
    }
}

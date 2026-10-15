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
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Manages configuration settings for LSP clients
 *
 * @author atalati
 */
public class ClientConfigurationManager {

    final ConfigValueCache cache = new ConfigValueCache();
    final NbCodeLanguageClient client;

    public ClientConfigurationManager(NbCodeLanguageClient client) {
        this.client = client;
    }

    public void registerConfigChangeListener(String section, BiConsumer<String, JsonElement> consumer) {
        cache.registerListener(section, consumer);
    }

    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefix(String config) {
        return lookupCacheAndGetConfig(List.of(config), null, true, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefix(String config, String scope) {
        return lookupCacheAndGetConfig(List.of(config), scope, true, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }
    
    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefixWithoutCaching(String config) {
        return lookupCacheAndGetConfig(List.of(config), null, false, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefixWithoutCaching(String config, String scope) {
        return lookupCacheAndGetConfig(List.of(config), null, false, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<List<JsonElement>> getConfigurationsUsingAltPrefix(List<String> configs) {
        return lookupCacheAndGetConfig(configs, null, true, true);
    }

    public CompletableFuture<JsonElement> getConfiguration(String config) {
        return lookupCacheAndGetConfig(List.of(config), null, false, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfigurationWithoutCaching(String config) {
        return lookupCacheAndGetConfig(List.of(config), null, false, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfigurationWithoutCaching(String config, String scope) {
        return lookupCacheAndGetConfig(List.of(config), null, false, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfiguration(String config, String scope) {
        return lookupCacheAndGetConfig(List.of(config), scope, false, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<List<JsonElement>> getConfigurations(List<String> configs) {
        return lookupCacheAndGetConfig(configs, null, false, true);
    }

    public CompletableFuture<List<JsonElement>> getConfigurations(List<String> configs, String scope) {
        return lookupCacheAndGetConfig(configs, scope, false, true);
    }

    private CompletableFuture<List<JsonElement>> lookupCacheAndGetConfig(List<String> configs, String scope, boolean isAltPrefix, boolean isCachingRequired) {
        final String configPrefix = isAltPrefix ? client.getNbCodeCapabilities().getAltConfigurationPrefix() : client.getNbCodeCapabilities().getConfigurationPrefix();
        List<ConfigurationItem> itemsToRequest = new ArrayList<>();
        List<JsonElement> result = new ArrayList<>();
        String prjScope = getProjectFromFileScope(scope);

        for (int i = 0; i < configs.size(); i++) {
            String config = configs.get(i);
            String prefixedConfig = configPrefix + config;
            JsonElement cachedValue = cache.getConfigValue(prefixedConfig, prjScope);
            if (cachedValue != null) {
                result.add(cachedValue);
            } else {
                ConfigurationItem item = new ConfigurationItem();
                if (scope != null) {
                    item.setScopeUri(scope);
                }
                item.setSection(prefixedConfig);
                itemsToRequest.add(item);
                result.add(null);
            }
        }

        if (itemsToRequest.isEmpty()) {
            return CompletableFuture.completedFuture(result);
        }

        return client.configuration(new ConfigurationParams(itemsToRequest))
                .thenApply(clientConfigs -> {
                    if (clientConfigs != null && clientConfigs.size() == result.size()) {
                        int j = 0;
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i) == null) {
                                JsonElement value = (JsonElement) clientConfigs.get(j);
                                result.set(i, value);
                                String prefixedConfig = configPrefix + configs.get(i);
                                if (isCachingRequired) {
                                    cache.cacheConfigValue(prefixedConfig, value, prjScope);
                                }
                                j++;
                            }
                        }
                    } else {
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i) == null) {
                                result.set(i, new JsonObject());
                            }
                        }
                    }
                    return result;
                });
    }

    public void handleConfigurationChange(JsonObject settings) {
        cache.updateCache(client, null, cache, settings);
    }

    private String getProjectFromFileScope(String scope) {
        try {
            if (scope == null) {
                return null;
            }
            FileObject fo = Utils.fromUri(scope);
            if (fo == null) {
                return null;
            }
            Project prj = FileOwnerQuery.getOwner(fo);
            if (prj == null) {
                return findWorkspaceFolder(fo);
            }

            return Utils.toUri(prj.getProjectDirectory());
        } catch (MalformedURLException ignored) {
        }
        return null;
    }

    // Copied from abstract class SingleFileOptionsQueryImpl
    private String findWorkspaceFolder(FileObject file) {
        Workspace workspace = Lookup.getDefault().lookup(Workspace.class);
        if (workspace == null) {
            return null;
        }
        for (FileObject workspaceFolder : workspace.getClientWorkspaceFolders()) {
            if (FileUtil.isParentOf(workspaceFolder, file) || workspaceFolder == file) {
                return Utils.toUri(workspaceFolder);
            }
        }

        //in case file is a source root, and the workspace folder is nested inside the root:
        for (FileObject workspaceFolder : workspace.getClientWorkspaceFolders()) {
            if (FileUtil.isParentOf(file, workspaceFolder)) {
                return Utils.toUri(workspaceFolder);
            }
        }

        return null;
    }
}

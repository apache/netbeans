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
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.services.LanguageClient;
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

    final WeakHashMap<LanguageClient, ConfigValueCache> clientCaches = new WeakHashMap<>();

    private ClientConfigurationManager() {
    }

    public static ClientConfigurationManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {

        private static final ClientConfigurationManager INSTANCE = new ClientConfigurationManager();
    }

    public void registerClient(LanguageClient client) {
        clientCaches.put(client, new ConfigValueCache());
    }

    public void registerConfigChangeListener(NbCodeLanguageClient client, String section, BiConsumer<String, JsonElement> consumer) {
        ConfigValueCache cache = clientCaches.get(client);
        if (cache != null) {
            cache.registerListener(section, consumer);
        }
    }

    public void registerConfigCache(NbCodeLanguageClient client, String section) {
        ConfigValueCache cache = clientCaches.get(client);
        if (cache != null) {
            cache.registerCache(section);
        }
    }

    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefix(NbCodeLanguageClient client, String config) {
        return lookupCacheAndGetConfig(client, List.of(config), null, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfigurationUsingAltPrefix(NbCodeLanguageClient client, String config, String scope) {
        return lookupCacheAndGetConfig(client, List.of(config), scope, true)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<List<JsonElement>> getConfigurationsUsingAltPrefix(NbCodeLanguageClient client, List<String> configs) {
        return lookupCacheAndGetConfig(client, configs, null, true);
    }

    public CompletableFuture<JsonElement> getConfiguration(NbCodeLanguageClient client, String config) {
        return lookupCacheAndGetConfig(client, List.of(config), null, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<JsonElement> getConfiguration(NbCodeLanguageClient client, String config, String scope) {
        return lookupCacheAndGetConfig(client, List.of(config), scope, false)
                .thenApply(result -> result.isEmpty() ? null : result.get(0));
    }

    public CompletableFuture<List<JsonElement>> getConfigurations(NbCodeLanguageClient client, List<String> configs) {
        return lookupCacheAndGetConfig(client, configs, null, false);
    }

    public CompletableFuture<List<JsonElement>> getConfigurations(NbCodeLanguageClient client, List<String> configs, String scope) {
        return lookupCacheAndGetConfig(client, configs, scope, false);
    }

    private CompletableFuture<List<JsonElement>> lookupCacheAndGetConfig(NbCodeLanguageClient client, List<String> configs, String scope, boolean isAltPrefix) {
        ConfigValueCache cache = clientCaches.get(client);
        if (cache == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
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
                    int j = 0;
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i) == null) {
                            JsonElement value = (JsonElement) clientConfigs.get(j);
                            result.set(i, value);
                            String prefixedConfig = configPrefix + configs.get(i);
                            cache.cacheConfigValueIfNeeded(prefixedConfig, value, prjScope);
                            j++;
                        }
                    }
                    return result;
                });
    }

    public void handleConfigurationChange(NbCodeLanguageClient client, JsonObject settings) {
        ConfigValueCache cache = clientCaches.get(client);
        if (cache != null) {
            cache.updateCache(client, null, cache, settings);
        }
    }

    private String getProjectFromFileScope(String scope) {
        try {
            if (scope == null) {
                return null;
            }
            FileObject fo = Utils.fromUri(scope);
            if(fo == null){
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

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
import com.google.gson.JsonPrimitive;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ConfigurationParams;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.TestCodeLanguageClient;

public class ClientConfigurationManagerTest extends NbTestCase {
    private MockClient mockClient;

    public ClientConfigurationManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        mockClient = new MockClient();
    }

    @Test
    public void testGetConfiguration() throws InterruptedException, ExecutionException {
        String section = "project.jdkhome";
        JsonElement expectedValue = new JsonPrimitive("test JDK path");
        mockClient.addConfig(mockClient.codeCapa.getConfigurationPrefix() + section, expectedValue);

        CompletableFuture<JsonElement> future = mockClient.getClientConfigurationManager().getConfiguration(section);
        JsonElement actualValue = future.get();

        assertEquals("Config value mismatch", expectedValue, actualValue);
    }

    @Test
    public void testGetConfigurationWithScope() throws InterruptedException, ExecutionException, URISyntaxException {
        String section = "project.jdkhome";
        JsonElement expectedValue = new JsonPrimitive("test JDK path");
        URI expectedScope = new URI("file://scope1");
        mockClient.addConfig(mockClient.codeCapa.getConfigurationPrefix() + section, expectedValue);

        CompletableFuture<JsonElement> future = mockClient.getClientConfigurationManager().getConfiguration(section, expectedScope.toString());
        JsonElement actualValue = future.get();

        assertEquals("Scoped value mismatch", expectedValue, actualValue.getAsJsonObject().get("value"));
        assertEquals("Scope URI mismatch", expectedScope.toString(), actualValue.getAsJsonObject().get("scope").getAsString());
    }

    @Test
    public void testGetAltConfiguration() throws InterruptedException, ExecutionException {
        String section = "project.jdkhome";
        JsonElement expectedValue = new JsonPrimitive("test JDK path");
        mockClient.addConfig(mockClient.codeCapa.getAltConfigurationPrefix() + section, expectedValue);

        CompletableFuture<JsonElement> future = mockClient.getClientConfigurationManager().getConfigurationUsingAltPrefix(section);
        JsonElement actualValue = future.get();

        assertEquals("Alt config value mismatch", expectedValue, actualValue);
    }

    @Test
    public void testGetConfigurations() throws ExecutionException, InterruptedException {
        List<String> configKeys = List.of("format.enabled", "debug.enabled");
        List<JsonElement> configValues = List.of(new JsonPrimitive(Boolean.TRUE), new JsonPrimitive(Boolean.FALSE));

        for (int i = 0; i < configValues.size(); i++) {
            mockClient.addConfig(mockClient.codeCapa.getConfigurationPrefix() + configKeys.get(i), configValues.get(i));
        }

        CompletableFuture<List<JsonElement>> future = mockClient.getClientConfigurationManager().getConfigurations(configKeys);
        List<JsonElement> results = future.get();

        assertEquals("Config list size mismatch", configValues.size(), results.size());
        for (int i = 0; i < configValues.size(); i++) {
            assertEquals("Config value mismatch at index " + i, configValues.get(i), results.get(i));
        }
    }

    @Test
    public void testGetConfigurationsWithScope() throws ExecutionException, InterruptedException, URISyntaxException {
        List<String> configKeys = List.of("format.enabled", "debug.enabled");
        List<JsonElement> configValues = List.of(new JsonPrimitive(Boolean.TRUE), new JsonPrimitive(Boolean.FALSE));
        URI expectedScope = new URI("file://scope1");

        for (int i = 0; i < configValues.size(); i++) {
            mockClient.addConfig(mockClient.codeCapa.getConfigurationPrefix() + configKeys.get(i), configValues.get(i));
        }

        CompletableFuture<List<JsonElement>> future = mockClient.getClientConfigurationManager().getConfigurations(configKeys, expectedScope.toString());
        List<JsonElement> results = future.get();

        assertEquals("Scoped config list size mismatch", configValues.size(), results.size());
        for (int i = 0; i < configValues.size(); i++) {
            assertEquals("Scoped value mismatch at index " + i, configValues.get(i), results.get(i).getAsJsonObject().get("value"));
            assertEquals("Scope URI mismatch at index " + i, expectedScope.toString(), results.get(i).getAsJsonObject().get("scope").getAsString());
        }
    }

    @Test
    public void testRegisterConfigListener() {
        String expectedSection = "project.jdkhome";
        JsonPrimitive expectedValue = new JsonPrimitive("New Value");

        BiConsumer<String, JsonElement> listener = (actualSection, actualValue) -> {
            assertEquals("Section mismatch in listener", expectedSection, actualSection);
            assertEquals("Value mismatch in listener", expectedValue, actualValue);
        };

        mockClient.getClientConfigurationManager().registerConfigChangeListener(expectedSection, listener);

        mockClient.addConfig(expectedSection, new JsonPrimitive("Old Value"));
        JsonObject newConfigTree = mockClient.updateSectionAndGetDeepCopy(expectedSection, expectedValue);
        mockClient.getClientConfigurationManager().handleConfigurationChange(newConfigTree);
    }

    private class MockClient extends TestCodeLanguageClient {

        NbCodeClientCapabilities codeCapa = new NbCodeClientCapabilities();
        JsonObject rootConfiguration = new JsonObject();
        ClientConfigurationManager confManager;

        public MockClient() {
            codeCapa.setConfigurationPrefix("jdk");
            codeCapa.setAltConfigurationPrefix("java+");
            confManager = new ClientConfigurationManager(this);
        }

        public void addConfig(String section, JsonElement value) {
            addConfigToObject(rootConfiguration, section, value);
        }

        public JsonObject updateSectionAndGetDeepCopy(String section, JsonElement newValue) {
            JsonObject obj = rootConfiguration.deepCopy();
            addConfigToObject(obj, section, newValue);
            return obj;
        }

        public JsonElement getConfigurationValue(String section) {
            String[] keys = section.split("\\.");
            JsonObject current = rootConfiguration;

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];

                if (!current.has(key)) {
                    return null;
                }

                if (i == keys.length - 1) {
                    return current.get(key);
                }

                JsonElement next = current.get(key);
                if (!next.isJsonObject()) {
                    return null;
                }

                current = next.getAsJsonObject();
            }

            return null;
        }

        private void addConfigToObject(JsonObject root, String section, JsonElement value) {
            String[] keys = section.split("\\.");
            JsonObject current = root;

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];

                if (i == keys.length - 1) {
                    current.add(key, value);
                } else {
                    if (!current.has(key) || !current.get(key).isJsonObject()) {
                        current.add(key, new JsonObject());
                    }
                    current = current.getAsJsonObject(key);
                }
            }
        }

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            return codeCapa;
        }
        
        @Override
        public ClientConfigurationManager getClientConfigurationManager() {
            return confManager;
        }

        @Override
        public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
            List<Object> result = params.getItems().stream()
                    .map(item -> {
                        if (item.getScopeUri() == null) {
                            return getConfigurationValue(item.getSection());
                        }
                        JsonObject o = new JsonObject();
                        o.add("value", getConfigurationValue(item.getSection()));
                        o.add("scope", new JsonPrimitive(item.getScopeUri()));
                        return o;
                    })
                    .collect(Collectors.toList());

            return CompletableFuture.completedFuture(result);
        }
    }
}

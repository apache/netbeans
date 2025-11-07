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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ConfigurationParams;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.TestCodeLanguageClient;

public class ConfigValueCacheTest extends NbTestCase {

    public ConfigValueCacheTest(String name) {
        super(name);
    }

    @Test
    public void testRegistrationOfListener() {
        ConfigValueCache cache = new ConfigValueCache();
        BiConsumer<String, JsonElement> expectedListener = (section, value) -> {
        };
        String section = "jdk.test.section";

        assertNull("Config should be null before registering listener", cache.getConfigData(section));

        cache.registerListener(section, expectedListener);
        ConfigValueCache.ConfigData configData = cache.getConfigData(section);

        assertNotNull("Config should exist after registering listener", configData);
        assertEquals("Listener should match", expectedListener, configData.getConsumer());
    }

    @Test
    public void testGetConfigValueWithInvalidSections() {
        ConfigValueCache cache = new ConfigValueCache();

        JsonElement result1 = cache.getConfigValue(null, null);
        assertNull("Result should be null for null section", result1);

        JsonElement result2 = cache.getConfigValue("non.existent.section", null);
        assertNull("Result should be null for non-existent section", result2);
    }

    @Test
    public void testGetConfigValueWithDifferentScopes() {
        ConfigValueCache cache = new ConfigValueCache();
        String section = "project.jdkhome";
        String scope1 = "file:///project1";
        String scope2 = "file:///project2";
        String nonExistentScope = "file:///non-existent";

        JsonElement rootValue = new JsonPrimitive("default");
        JsonElement scope1Value = new JsonPrimitive("project1");
        JsonElement scope2Value = new JsonPrimitive("project2");

        cache.registerCache(section);
        ConfigValueCache.ConfigData configData = cache.getConfigData(section);
        configData.setRootValue(rootValue);
        configData.setScopedValue(scope1, scope1Value);
        configData.setScopedValue(scope2, scope2Value);

        JsonElement rootResult = cache.getConfigValue(section, null);
        JsonElement scope1Result = cache.getConfigValue(section, scope1);
        JsonElement scope2Result = cache.getConfigValue(section, scope2);
        JsonElement nonExistentResult = cache.getConfigValue(section, nonExistentScope);

        assertEquals("Should return root value with null scope", rootValue, rootResult);
        assertEquals("Should return scope1 value", scope1Value, scope1Result);
        assertEquals("Should return scope2 value", scope2Value, scope2Result);
        assertNull("Should return null for non-existent scope", nonExistentResult);
    }

    @Test
    public void testGetConfigValueWithHierarchicalSections() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        
        String section1 = "editor";
        String section2 = "editor.format";
        String section3 = "editor.format.indentation";
        String section4 = "editor.size";

        JsonElement value3 = new JsonPrimitive("indentation value");
        JsonElement value4 = new JsonPrimitive("editor size value");
        
        mockClient.addConfig(section3, value3);
        mockClient.addConfig(section4, value4);

        cache.registerCache(section1);
        cache.registerCache(section2);
        cache.registerCache(section3);
        cache.registerCache(section4);
        
        cache.updateCache(mockClient, section1, cache, mockClient.getTree());

        JsonElement result1 = cache.getConfigValue(section1, null);
        JsonElement result2 = cache.getConfigValue(section2, null);
        JsonElement result3 = cache.getConfigValue(section3, null);
        JsonElement result4 = cache.getConfigValue(section4, null);

        assertEquals("Should return level 1 value", mockClient.getConfigurationValue(section1), result1);
        assertEquals("Should return level 2 value", mockClient.getConfigurationValue(section2), result2);
        assertEquals("Should return level 2 value", value4, result4);
        assertEquals("Should return level 3 value", value3, result3);
    }
    
        @Test
    public void testGetConfigValueWithHierarchicalSectionsRegisterInReverseOrder() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        
        String section1 = "editor";
        String section2 = "editor.format";
        String section3 = "editor.format.indentation";
        String section4 = "editor.size";

        JsonElement value3 = new JsonPrimitive("indentation value");
        JsonElement value4 = new JsonPrimitive("editor size value");
        
        mockClient.addConfig(section3, value3);
        mockClient.addConfig(section4, value4);

        cache.registerCache(section4);
        cache.registerCache(section3);
        cache.registerCache(section2);
        cache.registerCache(section1);
        
        cache.updateCache(mockClient, section1, cache, mockClient.getTree());

        JsonElement result1 = cache.getConfigValue(section1, null);
        JsonElement result2 = cache.getConfigValue(section2, null);
        JsonElement result3 = cache.getConfigValue(section3, null);
        JsonElement result4 = cache.getConfigValue(section4, null);

        assertEquals("Should return level 1 value", mockClient.getConfigurationValue(section1), result1);
        assertEquals("Should return level 2 value", mockClient.getConfigurationValue(section2), result2);
        assertEquals("Should return level 2 value", value4, result4);
        assertEquals("Should return level 3 value", value3, result3);
    }

    @Test
    public void testCacheConfigValue() {
        ConfigValueCache cache = new ConfigValueCache();
        String section = "project.jdkhome";
        String scope1 = "file:///project1";
        JsonElement rootValue = new JsonPrimitive("root value");
        JsonElement scopedValue = new JsonPrimitive("scoped value");

        cache.cacheConfigValue(section, rootValue, null);
        JsonElement result1 = cache.getConfigValue(section, null);
        assertEquals("Root value should be updated", rootValue, result1);

        cache.cacheConfigValue(section, scopedValue, scope1);
        JsonElement result2 = cache.getConfigValue(section, scope1);
        assertEquals("Scoped value should be updated", scopedValue, result2);

        JsonElement result3 = cache.getConfigValue(section, null);
        assertEquals("Root value should be unchanged", rootValue, result3);
    }

    @Test
    public void testUpdateCacheWithNullValues() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        String section = "project.jdkhome";
        JsonElement tree = new JsonPrimitive("test JDK value");
        cache.updateCache(mockClient, section, new ConfigValueCache().new ConfigData(), null);
        assertNull("Cache should not be updated with null tree", cache.getConfigValue(section, null));

        cache.updateCache(mockClient, section, null, tree);
        assertNull("Cache should not be updated with null cacheValue", cache.getConfigValue(section, null));
    }

    @Test
    public void testUpdateCacheWithConfigDataInstance() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        String section = "project.jdkhome";
        JsonElement tree = new JsonPrimitive("test JDK value");
        AtomicReference<JsonElement> listenerValue = new AtomicReference<>();

        cache.registerListener(section, (s, v) -> listenerValue.set(v));
        ConfigValueCache.ConfigData configData = cache.getConfigData(section);

        cache.updateCache(mockClient, section, configData, tree);

        JsonElement result = cache.getConfigValue(section, null);
        assertEquals("Root value should be updated", tree, result);

        assertEquals("Listener should be called with tree value", tree, listenerValue.get());
    }

    @Test
    public void testUpdateCacheWithConfigValueInstance() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        String parentSection = "editor";
        String fontColorSection = parentSection + ".fontColor";
        String fontSizeSection = parentSection + ".fontSize";
        
        cache.registerCache(fontColorSection);        
        cache.registerCache(fontSizeSection);

        JsonObject tree = new JsonObject();
        JsonObject childTree = new JsonObject();
        tree.add(parentSection, childTree);
        JsonElement expectedFontColorValue = new JsonPrimitive("blue");
        JsonElement expectedFontSizeValue = new JsonPrimitive(12);
        childTree.add("fontColor", expectedFontColorValue);
        childTree.add("fontSize", expectedFontSizeValue);

        mockClient.addConfig(fontColorSection, expectedFontColorValue);
        mockClient.addConfig(fontSizeSection, expectedFontSizeValue);

        cache.updateCache(mockClient, parentSection, cache, tree);

        JsonElement fontColorValue = cache.getConfigValue(fontColorSection, null);
        JsonElement fontSizeValue = cache.getConfigValue(fontSizeSection, null);

        assertNotNull("Nested section should be created", cache.getConfigData(fontColorSection));
        assertNotNull("Nested section should be created", cache.getConfigData(fontSizeSection));
        assertEquals("Nested value should be updated", expectedFontColorValue, fontColorValue);
        assertEquals("Nested value should be updated", expectedFontSizeValue, fontSizeValue);
    }

    @Test
    public void testUpdateCacheWithScopedValues() {
        ConfigValueCache cache = new ConfigValueCache();
        MockClient mockClient = new MockClient();
        String section = "project.jdkhome";
        JsonElement tree = new JsonPrimitive("updated value");

        cache.registerCache(section);
        ConfigValueCache.ConfigData configData = cache.getConfigData(section);

        String scope1 = "file:///project1";
        String scope2 = "file:///project2";
        JsonElement scope1Value = new JsonPrimitive("scope1 value");
        JsonElement scope2Value = new JsonPrimitive("scope2 value");
        configData.setScopedValue(scope1, scope1Value);
        configData.setScopedValue(scope2, scope2Value);

        mockClient.resetRequests();
        cache.updateCache(mockClient, section, configData, tree);
        Map<String, Map<String, JsonElement>> requests = mockClient.getRequestsReceived();

        assertTrue("Should request client for updated scoped values", requests.get(section).containsKey(scope1));
        assertTrue("Should request client for updated scoped values", requests.get(section).containsKey(scope2));
        assertEquals("Scope1 should have updated value", scope1Value, cache.getConfigValue(section, scope1));
        assertEquals("Scope2 should have updated value", scope2Value, cache.getConfigValue(section, scope2));
    }

    private class MockClient extends TestCodeLanguageClient {

        NbCodeClientCapabilities codeCapa = new NbCodeClientCapabilities();
        JsonObject rootConfiguration = new JsonObject();
        Map<String, Map<String, JsonElement>> requestsReceived = new HashMap<>();

        public MockClient() {
            codeCapa.setConfigurationPrefix("jdk");
        }

        public void addConfig(String section, JsonElement value) {
            String[] keys = section.split("\\.");
            JsonObject current = rootConfiguration;

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
        
        public void resetRequests() {
            requestsReceived = new HashMap<>();
        }

        public Map<String, Map<String, JsonElement>> getRequestsReceived() {
            return Collections.unmodifiableMap(requestsReceived);
        }
        
        public JsonObject getTree(){
            return rootConfiguration;
        }

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            return codeCapa;
        }

        @Override
        public CompletableFuture<List<Object>> configuration(ConfigurationParams params) {
            List<Object> result = params.getItems().stream()
                    .map(item -> {
                        if (item.getScopeUri() == null) {
                            return getConfigurationValue(item.getSection());
                        }
                        JsonElement value = getConfigurationValue(item.getSection());
                        if (requestsReceived.containsKey(item.getSection())) {
                            requestsReceived.get(item.getSection()).put(item.getScopeUri(), value);
                        } else {
                            requestsReceived.put(item.getSection(), new HashMap<>());
                            requestsReceived.get(item.getSection()).put(item.getScopeUri(), value);
                        }
                        return value;
                    })
                    .collect(Collectors.toList());

            return CompletableFuture.completedFuture(result);
        }
    }
}

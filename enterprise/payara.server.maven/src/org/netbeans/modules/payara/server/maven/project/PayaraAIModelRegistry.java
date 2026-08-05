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
package org.netbeans.modules.payara.server.maven.project;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches available AI model names for each provider.
 * <p>
 * Cloud providers: fetched from OpenRouter's public API with a 30-minute cache.<br>
 * Local providers (Ollama, LM Studio, GPT4All): fetched live from the local
 * server API using the configured or default base URL.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public final class PayaraAIModelRegistry {

    // ── Cloud (OpenRouter) ────────────────────────────────────────────────────

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/models";
    private static final long   CACHE_TTL_MS   = Duration.ofMinutes(30).toMillis();

    private static final Pattern ID_PATTERN   = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    private static volatile List<String> CACHE     = Collections.emptyList();
    private static volatile long         lastFetch = 0;

    private PayaraAIModelRegistry() {}

    // ── Local providers ───────────────────────────────────────────────────────

    public static final Set<String> LOCAL_PROVIDERS = new HashSet<>(
            Arrays.asList("OLLAMA", "LM_STUDIO", "GPT4ALL"));

    private static final Map<String, String> DEFAULT_BASE_URLS = new LinkedHashMap<>();
    static {
        DEFAULT_BASE_URLS.put("OLLAMA",    "http://localhost:11434");
        DEFAULT_BASE_URLS.put("LM_STUDIO", "http://localhost:1234");
        DEFAULT_BASE_URLS.put("GPT4ALL",   "http://localhost:4891");
    }

    /**
     * Returns model names for a local provider by querying its running server.
     * Falls back to an empty list if the server is not reachable.
     *
     * @param provider       local provider key (OLLAMA, LM_STUDIO, GPT4ALL)
     * @param providerLocation  custom base URL, or empty/null to use the default
     */
    public static List<String> getLocalModels(String provider, String providerLocation) {
        String base = (providerLocation != null && !providerLocation.trim().isEmpty())
                ? providerLocation.trim().replaceAll("/$", "")
                : DEFAULT_BASE_URLS.getOrDefault(provider, "http://localhost:8080");
        try {
            if ("OLLAMA".equals(provider)) {
                return fetchNames(base + "/api/tags", NAME_PATTERN);
            } else {
                // LM Studio and GPT4All both expose an OpenAI-compatible /v1/models
                return fetchIds(base + "/v1/models");
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    // ── Cloud providers (OpenRouter) ──────────────────────────────────────────

    private static final Map<String, List<String>> PROVIDER_PREFIXES = new LinkedHashMap<>();
    static {
        PROVIDER_PREFIXES.put("OPEN_AI",        Arrays.asList("openai/"));
        PROVIDER_PREFIXES.put("ANTHROPIC",      Arrays.asList("anthropic/"));
        PROVIDER_PREFIXES.put("GOOGLE",         Arrays.asList("google/"));
        PROVIDER_PREFIXES.put("MISTRAL",        Arrays.asList("mistralai/", "mistral/"));
        PROVIDER_PREFIXES.put("GROQ",           Arrays.asList("meta-llama/", "groq/", "mixtral", "llama"));
        PROVIDER_PREFIXES.put("DEEPSEEK",       Arrays.asList("deepseek/"));
        PROVIDER_PREFIXES.put("DEEPINFRA",      Collections.emptyList());
        PROVIDER_PREFIXES.put("CUSTOM_OPEN_AI", Collections.emptyList());
    }

    public static List<String> getModels() {
        if (!CACHE.isEmpty()
                && (System.currentTimeMillis() - lastFetch) < CACHE_TTL_MS) {
            return CACHE;
        }
        synchronized (PayaraAIModelRegistry.class) {
            if (!CACHE.isEmpty()
                    && (System.currentTimeMillis() - lastFetch) < CACHE_TTL_MS) {
                return CACHE;
            }
            try {
                List<String> fetched = fetchIds(OPENROUTER_URL);
                if (!fetched.isEmpty()) {
                    CACHE     = fetched;
                    lastFetch = System.currentTimeMillis();
                }
            } catch (Exception ignored) {
            }
            return CACHE;
        }
    }

    public static List<String> getModelsForProvider(String provider) {
        List<String> all = getModels();
        List<String> prefixes = PROVIDER_PREFIXES.get(provider);
        if (prefixes == null || prefixes.isEmpty()) {
            return all;
        }
        List<String> filtered = new ArrayList<>();
        for (String id : all) {
            for (String prefix : prefixes) {
                if (id.startsWith(prefix)) {
                    int slash = id.indexOf('/');
                    filtered.add(slash >= 0 ? id.substring(slash + 1) : id);
                    break;
                }
            }
        }
        return filtered;
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private static String httpGet(String apiUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3_000);
        conn.setReadTimeout(5_000);
        conn.setRequestProperty("Accept", "application/json");
        try {
            if (conn.getResponseCode() != 200) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    /** Extracts all {@code "id":"value"} entries from a JSON response. */
    private static List<String> fetchIds(String apiUrl) throws Exception {
        String json = httpGet(apiUrl);
        List<String> ids = new ArrayList<>();
        Matcher m = ID_PATTERN.matcher(json);
        while (m.find()) {
            ids.add(m.group(1));
        }
        return ids;
    }

    /** Extracts all {@code "name":"value"} entries from a JSON response (Ollama). */
    private static List<String> fetchNames(String apiUrl, Pattern pattern) throws Exception {
        String json = httpGet(apiUrl);
        List<String> names = new ArrayList<>();
        Matcher m = pattern.matcher(json);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }
}

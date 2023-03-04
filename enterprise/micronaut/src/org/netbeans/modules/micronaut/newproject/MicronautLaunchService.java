/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.micronaut.newproject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Singleton class managing connection to the Micronaut Launch service.
 *
 * @author Dusan Balek
 */
public class MicronautLaunchService {

    public static final String defaultURL = "https://launch.micronaut.io";
    public static final String snapshotURL = "https://snapshot.micronaut.io";

    private static final MicronautLaunchService INSTANCE = new MicronautLaunchService();
    private static final String VERSIONS = "versions/";
    private static final String OPTIONS = "select-options/";
    private static final String APPLICATION_TYPES = "application-types/";
    private static final String FEATURES = "features/";
    private static final String CREATE = "create/";

    private final Gson gson = new Gson();

    private MicronautLaunchService() {
    }

    public static MicronautLaunchService getInstance() {
        return INSTANCE;
    }

    public String getMicronautVersion(String serviceUrl) throws IOException {
        if (!serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl + '/';
        }
        JsonObject json = gson.fromJson(getJson(serviceUrl + VERSIONS), JsonObject.class);
        return json.getAsJsonObject("versions").get("micronaut.version").getAsString();
    }

    public List<ApplicationType> getApplicationTypes(String serviceUrl) throws IOException {
        if (!serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl + '/';
        }
        JsonObject json = gson.fromJson(getJson(serviceUrl + APPLICATION_TYPES), JsonObject.class);
        JsonArray jsonArray = json.getAsJsonArray("types");
        ArrayList<ApplicationType> types = new ArrayList<>(jsonArray.size());
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            types.add(new ApplicationType(jsonObject.get("title").getAsString(), jsonObject.get("value").getAsString()));
        }
        return types;
    }

    public List<String> getJdkVersions(String serviceUrl) throws IOException {
        if (!serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl + '/';
        }
        JsonObject json = gson.fromJson(getJson(serviceUrl + OPTIONS), JsonObject.class);
        JsonArray jsonArray = json.getAsJsonObject("jdkVersion").getAsJsonArray("options");
        ArrayList<String> jdkVersions = new ArrayList<>(jsonArray.size());
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jdkVersions.add(jsonObject.get("label").getAsString());
        }
        return jdkVersions;
    }

    public List<Feature> getFeatures(String serviceUrl, ApplicationType appType) throws IOException {
        if (!serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl + '/';
        }
        JsonObject json = gson.fromJson(getJson(serviceUrl + APPLICATION_TYPES + appType.value + '/' + FEATURES), JsonObject.class);
        JsonArray jsonArray = json.getAsJsonArray("features");
        ArrayList<Feature> features = new ArrayList<>(jsonArray.size());
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            features.add(new Feature(jsonObject.get("title").getAsString(), jsonObject.get("name").getAsString(),
                    jsonObject.get("category").getAsString(), jsonObject.get("description").getAsString()));
        }
        return features;
    }

    public InputStream create(String serviceUrl, ApplicationType appType, String appName, String javaVersion, String language, String buildTool, String testFramework, Set<Feature> features) throws IOException {
        StringBuilder sb = new StringBuilder(serviceUrl);
        if (!serviceUrl.endsWith("/")) {
            sb.append('/');
        }
        sb.append(CREATE).append(appType.value).append('/').append(appName);
        sb.append("?javaVersion=JDK_").append(javaVersion);
        sb.append("&lang=").append(language);
        sb.append("&build=").append(buildTool);
        sb.append("&test=").append(testFramework);
        if (features != null) {
            for (Feature feature : features) {
                sb.append("&features=").append(feature.name);
            }
        }
        return get(sb.toString());
    }

    private InputStream get(String serviceUrl) throws IOException {
        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return conn.getInputStream();
    }

    private String getJson(String serviceUrl) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(get(serviceUrl)))) {
            String line;
            while ((line = rd.readLine()) != null) {
               result.append(line);
            }
        }
        return result.toString();
    }

    public static class ApplicationType {

        private String title;
        private String value;

        private ApplicationType(String name, String value) {
            this.title = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public static class Feature {
        private String title;
        private String name;
        private String category;
        private String description;

        private Feature(String title, String name, String category, String description) {
            this.title = title;
            this.name = name;
            this.category = category;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }
    }
}

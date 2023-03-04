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
package org.netbeans.modules.javascript2.requirejs;

import java.util.Collections;
import java.util.TreeMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsPreferences {
    private static final String DEFAULT_VALUE = ""; // NOI18N
    private static final String MAPPINGS_SEPARATOR = "}{"; //NOI18N
    private static final String MAPPINGS_PATH_SEPARATOR = " , "; //NOI18N
    
    public static final Property<Boolean> ENABLED = new Property<Boolean>("enabled") {
        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    };
    
    private static final Property<String> MAPPINGS = new Property<String>("mappings") { // NOI18N
        @Override
        public String getDefaultValue() {
            return "";
        }
    };
    
    
    public static class Property<T> {

        private final String key;

        private Property(String key) {
            assert key != null;
            this.key = key;
        }

        String getKey() {
            return key;
        }

        public T getDefaultValue() {
            return null;
        }

    }
    
    public static Boolean getBoolean(Project project, Property<Boolean> property) {
        return Boolean.parseBoolean(get(project, property));
    }
    
    public static void putBoolean(Project project, Property<Boolean> property, boolean value) {
        put(project, property, Boolean.toString(value));
    }
    
    public static final Map <String, String> getMappings(Project project) {
        String storedMappings = get(project, MAPPINGS);
        if (storedMappings == null || storedMappings.isEmpty()) {
            return Collections.emptyMap();
        }
        String[] mappings = storedMappings.split(Pattern.quote(MAPPINGS_SEPARATOR));
        Map<String, String> result = new TreeMap<String, String>();
        String pattern = Pattern.quote(MAPPINGS_PATH_SEPARATOR);
        for (String mapping : mappings) {
            String[] parts = mapping.split(pattern);
            if (parts.length == 2) {
                if (parts[0].startsWith("{")) {
                    parts[0] = parts[0].substring(1);
                }
                parts[0] = parts[0].trim();
                if (parts[1].endsWith("}")) {
                    parts[1] = parts[1].substring(0, parts[1].length() - 1);
                }
                parts[1] = parts[1].trim();
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
    
    public static final void storeMappings(Project project, Map<String, String> mappings) {
        StringBuilder storedMappings = new StringBuilder();
        if (mappings.isEmpty()) {
            storedMappings.append(MAPPINGS.getDefaultValue());
        } else {
            storedMappings.append('{');
            for (Map.Entry<String, String> mapping : mappings.entrySet()) {
                if (storedMappings.length() > 1) {
                    storedMappings.append(MAPPINGS_SEPARATOR);
                }
                storedMappings.append(mapping.getKey().trim()).append(MAPPINGS_PATH_SEPARATOR).append(mapping.getValue().trim());
            }
            storedMappings.append('}');
        }
        put(project, MAPPINGS, storedMappings.toString());
    }
    
    public static String get(final Project project, Property<? extends Object> property) {
        if (project == null) {
            return null;
        }
        Preferences preferences = getPreferences(project);
        // get default value lazyly since it can do anything...
        String value = preferences.get(property.getKey(), DEFAULT_VALUE);
        if (DEFAULT_VALUE.equals(value)) {
            Object defaultValue = property.getDefaultValue();
            if (defaultValue == null) {
                return null;
            }
            return defaultValue.toString();
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value;
    }
    
    public static void put(Project project, Property<? extends Object> property, String value) {
        if (project != null) {
            if (StringUtils.hasText(value) && !value.equals(property.getDefaultValue())) {
                getPreferences(project).put(property.getKey(), value);
            } else {
                getPreferences(project).remove(property.getKey());
            }
        }
    }
    
    private static Preferences getPreferences(Project project) {
        return ProjectUtils.getPreferences(project, RequireJsPreferences.class, true);
    }
}

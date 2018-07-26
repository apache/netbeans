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
package org.netbeans.modules.html.ojet.data;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;


/**
 *
 * @author Petr Pisl
 */
public class OJETPreferences {
    private static final String DEFAULT_VALUE = ""; // NOI18N
    
    public static final Property<String> VERSION = new Property<String>("ojet_version") { // NOI18N
        @Override
        public String getDefaultValue() {
            return DataProviderImpl.DEFAULT_VERSION;
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
        if (!hasText(value)) {
            return null;
        }
        return value;
    }
    
    public static void put(Project project, Property<? extends Object> property, String value) {
        if (project != null) {
            if (hasText(value) && !value.equals(property.getDefaultValue())) {
                getPreferences(project).put(property.getKey(), value);
            } else {
                getPreferences(project).remove(property.getKey());
            }
        }
    }
    
    private static Preferences getPreferences(Project project) {
        return ProjectUtils.getPreferences(project, OJETPreferences.class, true);
    }
    
    private static boolean hasText(String input) {
        return input != null && !input.trim().isEmpty();
    }
}

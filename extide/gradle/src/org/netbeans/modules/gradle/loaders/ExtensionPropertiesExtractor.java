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
package org.netbeans.modules.gradle.loaders;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;

/**
 *
 * @author sdedic
 */
public class ExtensionPropertiesExtractor implements ProjectInfoExtractor {

    @Override
    public Result fallback(GradleFiles files) {
        return null;
    }

    @Override
    public Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
        return new Result() {
            @Override
            public Set getExtract() {
                Map<String, String> values = (Map<String, String>)props.getOrDefault("extensions.propertyValues", Collections.emptyMap()); // NOI18N
                Map<String, String> types = (Map<String, String>)props.getOrDefault("extensions.propertyTypes", Collections.emptyMap()); // NOI18N
                Map<String, String> taskValues = (Map<String, String>)props.getOrDefault("taskProperties", Collections.emptyMap()); // NOI18N
                Map<String, String> taskTypes = (Map<String, String>)props.getOrDefault("taskPropertyTypes", Collections.emptyMap()); // NOI18N
                Accessor a = new Accessor(values, types, taskValues, taskTypes);
                return Collections.singleton(a);
            }

            @Override
            public Set<String> getProblems() {
                return Collections.emptySet();
            }
        };
    }
    
    private static class Accessor implements BuildPropertiesImplementation {
        private final Map<String, String> propertyMap;
        private final Map<String, String> propertyTypes;
        private final Map<String, String> taskPropertyMap;
        private final Map<String, String> taskPropertyTypes;

        public Accessor(Map<String, String> propertyMap, Map<String, String> propertyTypes,
                        Map<String, String> taskPropertyMap, Map<String, String> taskPropertyTypes) {
            this.propertyMap = propertyMap;
            this.propertyTypes = propertyTypes;
            this.taskPropertyMap = taskPropertyMap;
            this.taskPropertyTypes = taskPropertyTypes;
        }

        @Override
        public BuildPropertiesSupport.Property findExtensionProperty(String extension, String propertyPath) {
            return find(extension + "." + propertyPath, propertyMap, propertyTypes);
        }
        
        @Override
        public BuildPropertiesSupport.Property findTaskProperty(String task, String propertyPath) {
            return find(task + "." + propertyPath, taskPropertyMap, taskPropertyTypes);
        }
        
        private BuildPropertiesSupport.Property find(String propertyPath, Map<String, String> values, Map<String, String> types) {
            String t = types.get(propertyPath);
            String v = values.get(propertyPath);
            String c = types.get(propertyPath + "#col");
            
            if (t == null && v == null) {
                return null;
            }
            
            if (c != null) {
                // collection values not supported now.
                return new BuildPropertiesSupport.Property(propertyPath, 
                        "named".equals(c)  ? BuildPropertiesSupport.PropertyKind.MAP : BuildPropertiesSupport.PropertyKind.LIST, 
                        t, v);
            }
            if ("".equals(t)) { // NOI18N
                return new BuildPropertiesSupport.Property(propertyPath, BuildPropertiesSupport.PropertyKind.EXISTS, t, null);
            }
            int dot = t.indexOf('.');
            BuildPropertiesSupport.PropertyKind kind;
            if (dot == -1 || (t.startsWith("java.lang") && !"java.lang.Object".equals(t))) {
                kind = BuildPropertiesSupport.PropertyKind.PRIMITIVE;
            } else {
                kind = BuildPropertiesSupport.PropertyKind.STRUCTURE;
            }
            return new BuildPropertiesSupport.Property(propertyPath, kind, t, v);
        }
    }
}

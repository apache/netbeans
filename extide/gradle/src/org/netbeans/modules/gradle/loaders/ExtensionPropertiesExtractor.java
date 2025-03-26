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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ProjectInfoExtractor.class)
public class ExtensionPropertiesExtractor implements ProjectInfoExtractor {

    @Override
    public Result fallback(GradleFiles files) {
        return new Result() {
            @Override
            public Set<?> getExtract() {
                return Set.of();
            }

            @Override
            public Set<String> getProblems() {
                return Collections.emptySet();
            }
        };
    }

    @Override
    public Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
        return new Result() {
            @Override
            public Set<?> getExtract() {
                Map<String, String> values = (Map<String, String>)props.getOrDefault("extensions.propertyValues", Collections.emptyMap()); // NOI18N
                Map<String, String> types = (Map<String, String>)props.getOrDefault("extensions.propertyTypes", Collections.emptyMap()); // NOI18N
                Map<String, String> taskValues = (Map<String, String>)props.getOrDefault("tasks.propertyValues", Collections.emptyMap()); // NOI18N
                Map<String, String> taskTypes = (Map<String, String>)props.getOrDefault("tasks.propertyTypes", Collections.emptyMap()); // NOI18N
                PropertyEvaluator a = new PropertyEvaluator(values, types, taskValues, taskTypes);
                return Set.of(a);
            }

            @Override
            public Set<String> getProblems() {
                return Set.of();
            }
        };
    }
    
    /**
     * Extracts properties from the String maps provided by the NbProjectInfoBuilder. The protocol between this Evaluator and the NbProjectInfoBuilder
     * is 'private', meaning it is not specified in an API - all clients should use BuildPropertiesSupport to access the properties.
     * <p>
     * The following maps are present in the info output:
     * <ul>
     * <li>extensions.propertyValues
     * <li>extensions.propertyTypes
     * <li>tasks.propertyValues
     * <li>tasks.propertyTypes
     * </ul>
     * The values hold the stringified values of the properties; propertyTypes specify type of the property. For properties nested in map- and list-style
     * properties, the propert ID encodes the map key or the list index.
     * 
     */
    private static class PropertyEvaluator implements BuildPropertiesImplementation {
        private final Map<String, String> propertyMap;
        private final Map<String, String> propertyTypes;
        private final Map<String, String> taskPropertyMap;
        private final Map<String, String> taskPropertyTypes;

        public PropertyEvaluator(Map<String, String> propertyMap, Map<String, String> propertyTypes,
                        Map<String, String> taskPropertyMap, Map<String, String> taskPropertyTypes) {
            this.propertyMap = propertyMap;
            this.propertyTypes = propertyTypes;
            this.taskPropertyMap = taskPropertyMap;
            this.taskPropertyTypes = taskPropertyTypes;
        }

        @Override
        public BuildPropertiesSupport.Property findProperty(BuildPropertiesSupport.Property base, String propertyPath) {
            Map<String, String> values;
            Map<String, String> types;
            
            switch (base.getScope()) {
                case BuildPropertiesSupport.EXTENSION:  
                    values = propertyMap; 
                    types = propertyTypes;
                    break;
                case BuildPropertiesSupport.TASK: 
                    values = taskPropertyMap; 
                    types = taskPropertyTypes; 
                    break;
                
                default:
                    return null;
            }
            if (base.getName()== null) {
                return null;
            }
            Object id = base.getId();
            String path;
            String suffix = propertyPath == null ? "" : "." + propertyPath; // NOI18N
            if (id == null) {
                path = base.getName() + suffix; // NOI18N
            } else if (id instanceof String) {
                path = id.toString() + suffix; // NOI18N
            } else {
                return null;
            }
             
            return find(base.getScope(), path, values, types, ""); // NOI18N
        }
        
        private Map<String, String> types(BuildPropertiesSupport.Property p) {
            switch (p.getScope()) {
                case BuildPropertiesSupport.EXTENSION:  
                    return propertyTypes;
                case BuildPropertiesSupport.TASK: 
                    return taskPropertyTypes; 
                default:
                    return Collections.emptyMap();
            }
        }

        private Map<String, String> values(BuildPropertiesSupport.Property p) {
            switch (p.getScope()) {
                case BuildPropertiesSupport.EXTENSION:  
                    return propertyMap;
                case BuildPropertiesSupport.TASK: 
                    return taskPropertyMap; 
                default:
                    return Collections.emptyMap();
            }
        }

        @Override
        public Collection<String> keys(BuildPropertiesSupport.Property container) {
            if (container.getId() == null) {
                return null;
            }
            String path = container.getId().toString();
            Map<String, String> types = types(container);
            String c = types.get(path + "#keys");
            if (c == null) {
                return null;
            }
            return Stream.of(c.split(";;")).map(s -> s.replace("\\;", ";")).collect(Collectors.toList());
        }

        @Override
        public BuildPropertiesSupport.Property get(BuildPropertiesSupport.Property container, String key, String propertyPath) {
            if (container.getId() == null) {
                return null;
            }
            Map<String, String> types = types(container);
            String coltype = types.get(container.getId().toString() + "#col");
            if (!("named".equals(coltype) || "map".equals(coltype))) {
                return null;
            }
            String path = container.getId().toString();
            Map<String, String> values = values(container);
            String itemType = types.getOrDefault(container.getId().toString() + "#itemType", "");
            
            String escaped = key.replace(";", "\\;");
            String k = path + "." + escaped;
            String v = values.get(k);
            if (v == null) {
                k = path + "[" + key + "]";
                v = values.get(k);
            }
            if (v == null) {
                return null;
            }
            return find(container.getScope(), k, values, types, itemType);
        }

        @Override
        public Iterator<BuildPropertiesSupport.Property> items(BuildPropertiesSupport.Property container, String property) {
            if (container.getId() == null) {
                return null;
            }
            Map<String, String> types = types(container);
            if (!"list".equals(types.get(container.getId().toString() + "#col"))) {
                return null;
            }
            Map<String, String> values = values(container);
            String path = container.getId().toString() + "[";
            String itemType = types.getOrDefault(container.getId().toString() + "#itemType", "");
            return new Iterator<BuildPropertiesSupport.Property>() {
                int index = 0;
                String obj;
                
                @Override
                public boolean hasNext() {
                    if (obj != null) {
                        return true;
                    }
                    String k = path + index + "]";
                    obj = values.get(k);
                    return obj != null;
                }

                @Override
                public BuildPropertiesSupport.Property next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    String k = path + index + "]";
                    obj = null;
                    index++;
                    if (property != null) {
                        k = k + property;
                    }
                    return find(container.getScope(), k, 
                            values, types, itemType);
                }
            };
        }
        
        private BuildPropertiesSupport.Property find(String src, String propertyPath, Map<String, String> values, Map<String, String> types, String defType) {
            String t = types.get(propertyPath);
            String c = types.get(propertyPath + "#col");
            Object v = values.get(propertyPath);
            
            if (t == null && (v == null && c == null)) {
                return null;
            }
            
            int lastDot = propertyPath.lastIndexOf('.');
            String n = propertyPath.substring(lastDot + 1);
            
            if (c != null) {
                // collection values not supported now.
                return new BuildPropertiesSupport.Property(propertyPath, src, n,
                        "named".equals(c) || "map".equals(c) ? BuildPropertiesSupport.PropertyKind.MAP : BuildPropertiesSupport.PropertyKind.LIST, 
                        t, Objects.toString(v, null));
            }
            if (t == null) {
                t = defType == null ? "" : defType;
            }
            if ("".equals(t)) { // NOI18N
                return new BuildPropertiesSupport.Property(propertyPath, src, n, BuildPropertiesSupport.PropertyKind.EXISTS, t, null);
            }
            int dot = t.indexOf('.');
            BuildPropertiesSupport.PropertyKind kind;
            if (dot == -1 || (t.startsWith("java.lang") && !"java.lang.Object".equals(t))) {
                kind = BuildPropertiesSupport.PropertyKind.PRIMITIVE;
            } else {
                kind = BuildPropertiesSupport.PropertyKind.STRUCTURE;
            }
            return new BuildPropertiesSupport.Property(propertyPath, src, n, kind, t, Objects.toString(v, null));
        }
    }
}

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
package org.netbeans.modules.gradle.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gradle.loaders.BuildPropertiesImplementation;

/**
 *
 * @author sdedic
 */
public final class BuildPropertiesSupport {
    private final BuildPropertiesImplementation impl;

    BuildPropertiesSupport(BuildPropertiesImplementation impl) {
        this.impl = impl;
    }
    
    /**
     * Attempts to find / locate a property value in the Gradle script. The property
     * may be written in the script, in the settings or implied by convention.
     * Even though the property really exists in the buildscript, this method may
     * not be able to locate its value; in that case the method retuns {@code null}.
     * In some cases, it's known that the property exists, but its value could not
     * be computed or obtained - in that case, the return will indicate {@link PropertyKind#EXISTS}.
     * <p>
     * The {@code propertyPath} is a dot-separated fully qualified property name.
     * <p>
     * This method only supports properties in singleton objects, no lists or map item access
     * is (currently).
     * 
     * @param propertyPath
     * @return Property instance or {@code null}
     */
    public Property findPropertyValue(String propertyPath) {
        return impl.findExtensionProperty(propertyPath);
    }
    
    public enum PropertyKind {
        PRIMITIVE,
        STRUCTURE,
        MAP,
        LIST,
        
        EXISTS
    }
    
    public static final class Property {
        private final String id;
        private final PropertyKind kind;
        private final String type;
        private final String value;

        public Property(String id, PropertyKind kind, String type, String value) {
            this.id = id;
            this.kind = kind;
            this.type = type;
            this.value = value;
        }
        
        /**
         * Returns the property id. The ID is a token that could be used to identify
         * the property, but should not be interpreted: the structure implementation detail
         * and may change at any time.
         * @return property ID
         */
        public @NonNull String getId() {
            return id;
        }
        
        /**
         * Returns type of the property. For {@link PropertyKind#LIST} or {@link PropertyKind#MAP}
         * it means the type of items in the collection / map. May return {@code null} for {@link PropertyKind#EXISTS}.
         * @return type of the property or collection item.
         */
        public @CheckForNull String getType() {
            return type;
        }

        public boolean isList() {
            return kind == PropertyKind.LIST;
        }

        public boolean isMap() {
            return kind == PropertyKind.MAP;
        }

        /**
         * Returns value of the property, as String. Value can be unknown, in that case it 
         * returns {@code null}.
         * @return property value or {@code null}
         */
        public @CheckForNull String getStringValue() {
            return value;
        }
    }
}

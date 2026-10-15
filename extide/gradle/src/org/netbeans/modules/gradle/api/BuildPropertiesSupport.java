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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.loaders.BuildPropertiesImplementation;

/**
 * API to access values of build properties. The Gradle project defines multiple properties at both top level
 * and at level of individual extensions and tasks. Some of the properties are computed, but many of them are
 * defined either explicitly in the build script, or derived from the explicit definitions by various conventions.
 * Such values are computed <b>before</b> any of the build tasks even execute.
 * <p>
 * This API provides access to certain such values, which are safe to extract and transport to the IDE process. Not all
 * properties are computed and ready to be read, and not all properties are supported by the Gradle Projects infrastructure,
 * but many of them are. Only String values are supported; other values are converted to String using {@link Object#toString()}.
 * <p>
 * To access an extension or a task property, obtain an instance of {@code BuildPropertiesSupport} by caling {@link BuildPropertiesSupport#get(org.netbeans.api.project.Project)},
 * then use
 * <ul>
 * <li>{@link BuildPropertiesSupport#findExtensionProperty(java.lang.String, java.lang.String)}, or
 * <li>{@link BuildPropertiesSupport#findTaskProperty(java.lang.String, java.lang.String)}
 * </ul>
 * <p>
 * There's a limited support for maps and lists. If a {@link Property} indicates a {@link BuildPropertiesSupport.PropertyKind#MAP} type, it is
 * possible to use {@link #keys} to enumerate keys of the map. For {@link PropertyKind#LIST}s, {@link #items} enumerate items of the list.
 * @author sdedic
 * @since 2.28
 */
public final class BuildPropertiesSupport {
    private final List<BuildPropertiesImplementation> impls;

    /**
     * Scope for extension properties
     */
    public static final String EXTENSION = "extension";

    /**
     * Scope for task properties
     */
    public static final String TASK = "task";
    
    BuildPropertiesSupport(List<BuildPropertiesImplementation> impls) {
        this.impls = impls;
    }
   
    /**
     * Obtain an instance for the project. Returns {@code null}, if the project does not support
     * BuildProperties access. 
     * @param p the project
     * @return instance of {@link BuildPropertiesSupport} for the project, or {@code null}.
     */
    public static BuildPropertiesSupport get(Project p) {
        NbGradleProject nbgp = NbGradleProject.get(p);
        if (nbgp == null) {
            return null;
        }
        List<BuildPropertiesImplementation> impls = new ArrayList<>(nbgp.refreshableProjectLookup().lookupAll(BuildPropertiesImplementation.class));
        return impls.isEmpty() ? null : new BuildPropertiesSupport(impls);
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
     * If a property is an item of a list or a map, the owning List or Map must be obtained first as a 
     * Property. Then individual items may be accessed using index or key.
     * 
     * @param propertyPath path to the property
     * @return Property instance or {@code null}
     */
    public Property findExtensionProperty(String extensionName, String propertyPath) {
        for (BuildPropertiesImplementation impl : impls) {
            Property p = impl.findProperty(new Property(null, EXTENSION, extensionName, PropertyKind.MAP, null, null), propertyPath);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Attempts to find property value of a named task. The same as {@link #findExtensionProperty} but works for
     * task properties. 
     * @param taskName task name.
     * @param propertyPath path to the property.
     * @return Property instance or {@code null}
     * @see #findExtensionProperty(java.lang.String, java.lang.String) 
     */
    public Property findTaskProperty(String taskName, String propertyPath) {
        for (BuildPropertiesImplementation impl : impls) {
            Property p = impl.findProperty(new Property(null, TASK, taskName, PropertyKind.MAP, null, null), propertyPath);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Enumerates items of a list-style property. Works only for properties of {@link PropertyKind#LIST}. If {@code path} is null,
     * the contained items are enumerated. If {@code path} is not null, it identifies a property within items - the method enumerates
     * those properties. If an item does not contain the specified path, {@code null} is returned in the enumeration.
     * s
     * @param owner the list property
     * @param path path into individual items.
     * @return enumeration of items or item properties.
     */
    public Iterable<Property> items(Property owner, String path) {
        if (owner.getKind() != PropertyKind.LIST) {
            return Collections.emptyList();
        }
        class It implements Iterator<Property> {
            Iterator<BuildPropertiesImplementation> containers = impls.iterator();
            Iterator<Property> del;

            @Override
            public boolean hasNext() {
                if (del != null && !del.hasNext()) {
                    del = null;
                }
                while (del == null) {
                    if (!containers.hasNext()) {
                        return false;
                    }
                    del = containers.next().items(owner, path);
                    if (del != null && !del.hasNext()) {
                        del = null;
                    }
                }
                return del.hasNext();
            }

            @Override
            public Property next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return del.next();
            }
        }
        return new Iterable<Property>() {
            @Override
            public Iterator<Property> iterator() {
                return new It();
            }
        };
    }
    
    /**
     * Returns keys of map-style properties. Works only for properties of {@link PropertyKind#MAP} or
     * {@link PropertyKind#STRUCTURE}. May return an empty list, if keys cannot be enumerated or represented as Strings.
     * @param owner the property
     * @return list of keys, possibly empty. Does not return null
     */
    @NonNull
    public Collection<String> keys(Property owner) {
        if ((owner.getKind() != PropertyKind.STRUCTURE) && (owner.getKind() != PropertyKind.MAP)) {
            return Collections.emptyList();
        }
        Set<String> s = new LinkedHashSet<>();
        for (BuildPropertiesImplementation impl : impls) {
            Collection<String> keys = impl.keys(owner);
            if (keys != null) {
                s.addAll(keys);
            }
        }
        return s;
    }
    
    /**
     * Returns a named item, or a property of a named item from map-style property. Works only for
     * map-style properties.
     * @param base the map property
     * @param key map item's key
     * @param path optional path within an item to the property.
     * @return map item, or a property of a map item. Null, if the item can not be found.
     */
    @CheckForNull
    public Property get(Property base, String key, String path) {
        if ((base.getKind() != PropertyKind.MAP)) {
            return null;
        }
        for (BuildPropertiesImplementation impl : impls) {
            Property p = impl.get(base, key, path);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Describes type / kind of the property.
     */
    public enum PropertyKind {
        /**
         * The property is a primitive value, or String
         */
        PRIMITIVE,
        
        /**
         * The property is a structure or object.
         */
        STRUCTURE,
        
        /**
         * The property is a Map
         */
        MAP,
        
        /**
         * The property is a list of values.
         */
        LIST,
        
        /**
         * The property exists, but its value cannot be accessed.
         */
        EXISTS
    }
    
    /**
     * Describes a property and its value.
     */
    public record Property(Object id, String scope, String name, PropertyKind kind, String type, String value) {
        
        /**
         * Returns the property id. The ID is a token that could be used to identify
         * the property, but should not be interpreted: the structure implementation detail
         * and may change at any time.
         * @return property ID
         */
        public @NonNull Object getId() {
            return id;
        }

        /**
         * Scope of a property. Basic supported scopes are {@link #EXTENSION} and {@link #TASK}. Additional plugins
         * may add additional scopes.
         * @return scope of the property
         */
        public String getScope() {
            return scope;
        }

        /**
         * @return Basic type of a property.
         */
        public PropertyKind getKind() {
            return kind;
        }

        /**
         * @return name of the property.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns type of the property. For {@link PropertyKind#LIST} or {@link PropertyKind#MAP}
         * it means the type of items in the collection / map. May return {@code null} for {@link PropertyKind#EXISTS}.
         * Fully qualified type name is returned.
         * 
         * @return type of the property or collection item.
         */
        public @CheckForNull String getType() {
            return type;
        }

        /**
         * True, if the property is a list.
         * @return 
         */
        public boolean isList() {
            return kind == PropertyKind.LIST;
        }

        /**
         * True, if the property is a Map.
         * @return 
         */
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

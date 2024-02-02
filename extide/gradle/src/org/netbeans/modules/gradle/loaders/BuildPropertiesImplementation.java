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

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;

/**
 * This is a "private SPI" for {@link BuildPropertiesSupport}. Currently only bridges the public API
 * to a gradle private implementation. 
 * @author sdedic
 */
public interface BuildPropertiesImplementation {
    /**
     * Retrieves property of the specified task. Returns {@code null} if the property does not exist
     * or is not known. Properties can be indexed, structured, or Map-like: the individual items, members
     * or keyed values are described using the <b>base property</b> retrieved by a previous call, and 
     * an index or String selector that points to the specific nested value.
     * <p>
     * Property instances with {@code null} ids are special cases; no regular Property instances have {@code null} id. If such a property has {@code null} 
     * {@link BuildPropertiesSupport.Property#getSourceName()}, it represents the container of tasks, extensions
     * or other toplevel containers. Non-null {@link BuildPropertiesSupport.Property#getSourceName()} identifies the task, extension
     * or other particular toplevel container.
     * <p>
     * {@code propertyPath} is a structured path to the desired property. Individual path components are separated by dots ('.'). Indexes
     * or map key accessors are not allowed.
     * 
     * @param base the base property.
     * @param index
     * @param selector
     * @param propertyPath
     * @return {@link BuildPropertiesSupport.Property} or null
     */
    public BuildPropertiesSupport.Property findProperty(BuildPropertiesSupport.Property base, String propertyPath);
    
    /**
     * Returns an item on the key, and path-identified property of it, starting from the {@code container} property.
     * @param base container to index
     * @param index index to access
     * @param propertyPath optional; if {@code null} the item itself is returned. Otherwise item's property identified by the path
     * 
     * @return item at the specified index or its property.
     */
    public default BuildPropertiesSupport.Property get(BuildPropertiesSupport.Property base, String key, String propertyPath) {
        return null;
    }
    
    /**
     * Enumerates values in children of the collection property. Should return {@code null} for an unknown property, if the implementation
     * does not support enumerating the container. The result list may contain {@code null}s. If {@code path} is specified, should return
     * for each contained item a property (if they exist) at the specified path.
     * @param container the container property
     * @param path optional property path, possibly null.
     * @return iterator or {@code null}
     */
    public default Iterator<BuildPropertiesSupport.Property> items(BuildPropertiesSupport.Property container, String path) {
        return null;
    }
    
    /**
     * Returns keys of a Map property or a structure. For non-existing properties, or if unsupported for the property {@code null} should be returned. For properties that are not
     * maps or structures, an empty collection should be returned.
     * @param p the property
     * @return keys of map/structure or {@code null}.
     */
    public default Collection<String> keys(BuildPropertiesSupport.Property p) {
        return null;
    }
}

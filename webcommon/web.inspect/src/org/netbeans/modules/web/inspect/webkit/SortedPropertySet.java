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
package org.netbeans.modules.web.inspect.webkit;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.nodes.Node;

/**
 * Property set sorted according to property names.
 * 
 * @author Jan Stola
 */
abstract class SortedPropertySet<T extends Node.Property> extends Node.PropertySet {
    /** Property name to property map sorted according to property names. */
    private final SortedMap<String,T> properties;
    /** Determines whether this property set has been initialized. */
    private boolean initialized;

    /**
     * Creates a new {@code SortedPropertySet}.
     * 
     * @param name name of the property set.
     * @param displayName display name of the property set.
     * @param shortDescription short description of the property set.
     */
    SortedPropertySet(String name, String displayName, String shortDescription) {
        super(name, displayName, shortDescription);
        properties = new TreeMap<String,T>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * Adds a property into this property set.
     * 
     * @param property property to add into this set.
     */
    synchronized void addProperty(T property) {
        properties.put(property.getName(), property);
    }

    /**
     * Removes a property from this property set.
     * 
     * @param property property to remove from this set.
     */
    synchronized void removeProperty(T property) {
        properties.remove(property.getName());
    }

    /**
     * Returns the property of the specified name.
     * 
     * @param name name of the requested property.
     * @return property of the specified name.
     */
    synchronized T getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Updates this property set, i.e., forces synchronization with its model/source.
     */
    abstract void update();

    @Override
    public synchronized Node.Property<?>[] getProperties() {
        if (!initialized) {
            initialized = true;
            update();
        }
        return properties.values().toArray(new Node.Property<?>[properties.size()]);
    }

}

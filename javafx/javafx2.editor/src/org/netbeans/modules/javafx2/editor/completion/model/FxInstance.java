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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sdedic
 */
public abstract class FxInstance extends FxObjectBase {
    private String          id;

    /**
     * Properties for this instance
     */
    private Map<String, PropertyValue>  properties = Collections.emptyMap();
    
    /**
     * Static properties attached to this instance
     */
    private Map<String, StaticProperty> staticProperties = Collections.emptyMap();
    
    /**
     * Event handlers
     */
    private Map<String, EventHandler> eventHandlers = Collections.emptyMap();
    
    /**
     * Script fragments embedded in the object tag
     */
    private List<FxScriptFragment>  scripts = Collections.emptyList();

    public String getId() {
        return id;
    }

    void addProperty(PropertyValue p) {
        if (properties.isEmpty()) {
            properties = new HashMap<String, PropertyValue>();
        }
        properties.put(p.getPropertyName(), p);
    }
    
    void addStaticProperty(StaticProperty p) {
        if (staticProperties.isEmpty()) {
            staticProperties = new HashMap<String, StaticProperty>();
        }
        staticProperties.put(p.getPropertyName(), p);
    }
    
    void addEvent(EventHandler p) {
        if (eventHandlers.isEmpty()) {
            eventHandlers = new HashMap<String, EventHandler>();
        }
        eventHandlers.put(p.getEvent(), p);
    }
    
    void addScript(FxScriptFragment script) {
        if (scripts.isEmpty()) {
            scripts = new LinkedList<FxScriptFragment>();
        }
        scripts.add(script);
    }
    
    public List<FxScriptFragment> getScripts() {
        return Collections.unmodifiableList(scripts);
    }
    
    public Collection<String>   getEventNames() {
        return Collections.unmodifiableCollection(eventHandlers.keySet());
    }
    
    public EventHandler getEventHandler(String n) {
        return eventHandlers.get(n);
    }

    public PropertyValue getProperty(String n) {
        PropertyValue p = properties.get(n);
        if (p == null) {
            p = staticProperties.get(n);
        }
        return p;
    }
    
    public Collection<? extends EventHandler>    getEvents() {
        return Collections.unmodifiableCollection(eventHandlers.values());
    }

    public Collection<PropertyValue>    getProperties() {
        return Collections.unmodifiableCollection(properties.values());
    }
    
    public Collection<StaticProperty>    getStaticProperties() {
        return Collections.unmodifiableCollection(staticProperties.values());
    }

    FxInstance withId(String id) {
        this.id = id;
        return this;
    }
    
    @Override
    void detachChild(FxNode child) {
        if (child instanceof StaticProperty) {
            staticProperties.remove(((StaticProperty)child).getPropertyName());
        } else if (child instanceof PropertySetter) {
            properties.remove(((PropertySetter)child).getPropertyName());
        }
    }
    
    @Override
    void addChild(FxNode child) {
        super.addChild(child);
        if (child instanceof StaticProperty) {
            addStaticProperty((StaticProperty)child);
        } else if (child instanceof PropertyValue) {
            addProperty((PropertyValue)child);
        } else if (child instanceof EventHandler) {
            addEvent((EventHandler)child);
        } else if (child instanceof FxScriptFragment) {
            addScript((FxScriptFragment)child);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    void propertyRenamed(PropertyValue pv) {
        if (this.properties.values().remove(pv)) {
            this.properties.put(pv.getPropertyName(), pv);
        } 
    }
}

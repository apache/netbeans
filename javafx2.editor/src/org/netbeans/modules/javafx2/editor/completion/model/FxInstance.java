/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

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
package org.netbeans.modules.javafx2.editor.completion.beans;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Describes event source on an FX object. 
 *
 * @author sdedic
 */
public final class FxEvent extends FxDefinition {
    /**
     * FQN of the event fired
     */
    private String eventClassName;
    
    /**
     * Java type of the event
     */
    private ElementHandle<TypeElement>  eventType;

    /**
     * True, if the event is a property change
     */
    private boolean propertyChange;

    /**
     * Name of the event object class
     * 
     * @return event object class name
     */
    public String getEventClassName() {
        return eventClassName;
    }

    /**
     * Type of the event object. May return {@code null},
     * if the event type wasn't resolved (class was missing}
     * 
     * @return handle to the event type
     */
    @CheckForNull
    public ElementHandle<TypeElement> getEventType() {
        return eventType;
    }

    FxEvent(String name) {
        super(name);
    }
    
    void setPropertyChange(boolean change) {
        this.propertyChange = change;
    }

    void setEventClassName(String eventClassName) {
        this.eventClassName = eventClassName;
    }

    void setEventType(ElementHandle<TypeElement> eventType) {
        this.eventType = eventType;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Event[");
        sb.append("name: ").append(getName()).
                append("; type: ").append(getEventType());
        sb.append("]");
        return sb.toString();
    }
    
    public boolean isPropertyChange() {
        return propertyChange;
    }
    
    public String getPropertyName() {
        if (!propertyChange) {
            return null;
        }
        String s = getName();
        return s.substring(0, s.length() - 6); // minus Change at the end.
    }
    
    public String getSymbol() {
        String s = getName();
        return "on" + Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    
    /**
     * Returns EVENT type
     * @return EVENT
     */
    public FxDefinitionKind getKind() {
        return FxDefinitionKind.EVENT;
    }
}

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
package org.netbeans.modules.xml.axi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;

/**
 * Listener to listen to this model changes. Used by code generator.
 * 
 * @author Ayub
 */
public class AXIModelListener implements PropertyChangeListener {
    List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
    
    public void propertyChange(PropertyChangeEvent evt) {
        
        //filter events if not intended for code generator.
        if(!validatePropertyChangeEvent(evt))
            return;
        
        //add event to the event queue.
        events.add(evt);
    }
    
    public List<PropertyChangeEvent> getEvents() {
        return events;
    }
    
    public void clearEvents() { events.clear();}
    
    /**
     * Checks the validity of this event. Certain events shouldn't go to
     * the code generator. For example events coming from proxy components.
     * There are certain other cases as well.
     */
    private boolean validatePropertyChangeEvent(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        //events coming from model are valid
        if( !(source instanceof AXIComponent) )
            return true;
                
        //ignore proxy related events
        if(proxyRelated(evt))
            return false;
                        
        
        return true;
    }
    
    private boolean proxyRelated(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof AXIComponentProxy)
            return true;
        
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        //proxy child added
        if( (oldValue == null) && (newValue instanceof AXIComponentProxy) )
            return true;
        //proxy child removed
        if( (newValue == null) && (oldValue instanceof AXIComponentProxy) )
            return true;
        
        return false;
    }
    
}

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

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.impl.SchemaUpdate.UpdateUnit.Type;

/**
 *
 * @author Ayub Khan
 */
public class SchemaUpdate {
    
    /** Creates a new instance of SchemaUpdate */
    public SchemaUpdate() {
    }
    
    public Collection<UpdateUnit> getUpdateUnits() {
        return Collections.unmodifiableList(units);
    }
    
    public void addUpdateUnit(UpdateUnit uu) {
        units.add(uu);
    }
    
    public UpdateUnit createUpdateUnit(Type type,
            AXIComponent source, Object oldValue, Object newValue, String propertyName) {
        AXIComponent key = null;
        if(type == UpdateUnit.Type.CHILD_MODIFIED)
            key = source;
        else if(type == UpdateUnit.Type.CHILD_ADDED)
            key = (AXIComponent) newValue;
        else if(type == UpdateUnit.Type.CHILD_DELETED)
            key = (AXIComponent) oldValue;
        
        if(key instanceof AXIComponentProxy) {
            key = key.getOriginal();
        }
        if(key != null) {
            List<AXIComponent> items = uniqueMap.get(key);
            if(items == null) {
                items = new ArrayList<>();
                uniqueMap.put(key, items);
            }
            items.add(key);
            return new UpdateUnit(String.valueOf(count++), type, source, oldValue, newValue,
                    propertyName);
        }
        return null;
    }
    
    public static class UpdateUnit {
        
        public static enum Type {CHILD_ADDED, CHILD_DELETED, CHILD_MODIFIED};
        
        private String id;
        
        private Type type;
        
        private AXIComponent source;
        
        private Object oldValue;
        
        private Object newValue;
        
        private String propertyName;
        
        public UpdateUnit(String id, Type type,
                AXIComponent source, Object oldValue, Object newValue,
                String propertyName) {
            this.id = id;
            this.type = type;
            this.source = source;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.propertyName = propertyName;
        }
        
        public String getId() {
            return id;
        }
        
        public AXIComponent getSource() {
            return source;
        }
        
        public Type getType() {
            return type;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
    }
    
    private List<UpdateUnit> units = new ArrayList<UpdateUnit>();
    
    private HashMap<AXIComponent, List<AXIComponent>> uniqueMap =
            new HashMap<AXIComponent, List<AXIComponent>>();
    
    private int count = 0;
}

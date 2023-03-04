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
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaModelListener implements PropertyChangeListener {
        
    /**
     * Creates a new instance of SchemaModelListener
     */
    public SchemaModelListener(AXIModelImpl model) {
        this.model = model;
    }
    
    /**
     * Returns true if the event pool is not empty,
     * false otherwise.
     */
    boolean needsSync() {
        return !events.isEmpty();
    }
    
    void syncCompleted() {
        events.clear();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        assert(model != null);        
        if(model.isIntransaction() || !isValidEvent(evt))
            return;

        events.add(evt);
        ((ModelAccessImpl)model.getAccess()).setDirty();
    }
    
    private boolean isValidEvent(PropertyChangeEvent evt) {
        if(evt.getSource() instanceof SchemaModel) {
            return true;
        }
        
        if(!(evt.getSource() instanceof SchemaComponent))
            return false;
        
        SchemaComponent component = (SchemaComponent)evt.getSource();
        if( (evt.getOldValue() == null) &&
            (evt.getNewValue() != null) &&
            (evt.getNewValue() instanceof SchemaComponent) ) {
            component = (SchemaComponent)evt.getNewValue();
        }
        
        if( (evt.getNewValue() == null) &&
            (evt.getOldValue() != null) &&
            (evt.getOldValue() instanceof SchemaComponent) ) {
            component = (SchemaComponent)evt.getOldValue();
        }
        
        //query to check if this component affects the model
        AXIModelBuilderQuery query = new AXIModelBuilderQuery(model);
        return query.affectsModel(component);
    }
    
    private List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
    private AXIModelImpl model;
}

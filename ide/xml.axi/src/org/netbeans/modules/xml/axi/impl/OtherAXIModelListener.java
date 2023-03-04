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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class OtherAXIModelListener implements ComponentListener {
        
    /**
     * Creates a new instance of AXIModelListener
     */
    public OtherAXIModelListener(AXIModelImpl model) {
        this.model = model;
    }
        
    public void valueChanged(ComponentEvent event) {
        eventReceived(event);
    }
    
    public void childrenDeleted(ComponentEvent event) {
        eventReceived(event);
    }
    
    public void childrenAdded(ComponentEvent event) {
        eventReceived(event);
    }
    
    /**
     * Some events received.
     */
    private void eventReceived(ComponentEvent event) {
        assert(model != null);
        events.add(event);
        ((ModelAccessImpl)model.getAccess()).setDirty();
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
    
    private List<ComponentEvent> events = new ArrayList<ComponentEvent>();
    private AXIModelImpl model;
}

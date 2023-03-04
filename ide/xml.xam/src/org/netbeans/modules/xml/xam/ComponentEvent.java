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

package org.netbeans.modules.xml.xam;


import java.util.EventObject;

/**
 *
 * @author Nam Nguyen
 * @author Rico Cruz
 * @author Chris Webster
 */
public class ComponentEvent extends EventObject {
    static final long serialVersionUID = 1L;
    
    private EventType event;
    
    /**
     * Creates a new instance of ComponentEvent
     */
    public ComponentEvent(Object source, EventType t) {
        super(source);
        event = t;
    }
    
    public enum EventType {
        /**
         * Component value (attributes or properties) changed
         */
        VALUE_CHANGED {
            public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.valueChanged(evt);
                }}, 
        /**
         * Childen component added
         */
        CHILD_ADDED {
                public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.childrenAdded(evt);
                }}, 
        /**
         * Childen component removed
         */
        CHILD_REMOVED {
                public void fireEvent(ComponentEvent evt, 
                                           ComponentListener l) {
                l.childrenDeleted(evt);
                }};
        
        public abstract void fireEvent(ComponentEvent evt, 
                                       ComponentListener l);
    }
    
    public EventType getEventType() {
        return event;
    }
    
    @Override
    public String toString() {
        return event + ":" + source;
    }
}

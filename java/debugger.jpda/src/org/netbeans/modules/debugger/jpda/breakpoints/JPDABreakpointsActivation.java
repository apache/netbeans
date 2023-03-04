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

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.ActiveBreakpoints;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.BreakpointsActivationProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * JPDA activation/deactivation of breakpoints.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession", types = BreakpointsActivationProvider.class)
public class JPDABreakpointsActivation implements BreakpointsActivationProvider {
    
    private final JPDADebugger debugger;
    private final Map<PropertyChangeListener, DelegateListener> listenersMap = Collections.synchronizedMap(new HashMap<PropertyChangeListener, DelegateListener>());
    
    public JPDABreakpointsActivation(ContextProvider contextProvider) {
        this.debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public boolean areBreakpointsActive() {
        return debugger.getBreakpointsActive();
    }

    @Override
    public void setBreakpointsActive(boolean active) {
        debugger.setBreakpointsActive(active);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        DelegateListener dl = new DelegateListener(this, l);
        listenersMap.put(l, dl);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, dl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        DelegateListener dl = listenersMap.remove(l);
        if (dl != null) {
            debugger.removePropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, dl);
        }
    }
    
    private static class DelegateListener implements PropertyChangeListener {
        
        private final Object source;
        private final PropertyChangeListener l;
        
        public DelegateListener(Object source, PropertyChangeListener l) {
            this.source = source;
            this.l = l;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            l.propertyChange(new PropertyChangeEvent(
                    source,
                    ActiveBreakpoints.PROP_BREAKPOINTS_ACTIVE,
                    evt.getOldValue(),
                    evt.getNewValue()));
        }
    }
    
}

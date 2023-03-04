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

package org.netbeans.modules.javascript.v8debug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.ActiveBreakpoints;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.spi.debugger.BreakpointsActivationProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path=V8DebuggerEngineProvider.ENGINE_NAME, types = BreakpointsActivationProvider.class)
public class V8BreakpointsActivation implements BreakpointsActivationProvider {
    
    private final BreakpointsHandler breakpointsHandler;
    private final Map<PropertyChangeListener, DelegateListener> listenersMap = Collections.synchronizedMap(new HashMap<PropertyChangeListener, DelegateListener>());

    public V8BreakpointsActivation(ContextProvider contextProvider) {
        V8Debugger debugger = contextProvider.lookupFirst(null, V8Debugger.class);
        breakpointsHandler = debugger.getBreakpointsHandler();
    }

    @Override
    public boolean areBreakpointsActive() {
        return breakpointsHandler.areBreakpointsActive();
    }

    @Override
    public void setBreakpointsActive(boolean active) {
        breakpointsHandler.setBreakpointsActive(active);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        DelegateListener dl = new DelegateListener(this, l);
        listenersMap.put(l, dl);
        breakpointsHandler.addBreakpointsActiveListener(dl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        DelegateListener dl = listenersMap.remove(l);
        if (dl != null) {
            breakpointsHandler.removeBreakpointsActiveListener(dl);
        }
    }
    
    private class DelegateListener implements BreakpointsHandler.BreakpointsActiveListener {
        
        private final Object source;
        private final PropertyChangeListener pchl;
        
        public DelegateListener(Object source, PropertyChangeListener pchl) {
            this.source = source;
            this.pchl = pchl;
        }

        @Override
        public void breakpointsActivated(boolean activated) {
            pchl.propertyChange(new PropertyChangeEvent(source, ActiveBreakpoints.PROP_BREAKPOINTS_ACTIVE, !activated, activated));
        }

    }
    
}

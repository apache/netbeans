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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.ActiveBreakpoints;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.spi.debugger.BreakpointsActivationProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.RequestProcessor;

/**
 * WEB JavaScript activation/deactivation of breakpoints.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="javascript-debuggerengine", types = BreakpointsActivationProvider.class)
public class WebBreakpointsActivation implements BreakpointsActivationProvider {

    private static final RequestProcessor RP = new RequestProcessor(WebBreakpointsActivation.class.getName());
    private final Debugger debugger;
    private final Map<PropertyChangeListener, DelegateListener> listenersMap = Collections.synchronizedMap(new HashMap<PropertyChangeListener, DelegateListener>());
    
    public WebBreakpointsActivation(ContextProvider contextProvider) {
        this.debugger = contextProvider.lookupFirst(null, Debugger.class);
    }

    @Override
    public boolean areBreakpointsActive() {
        return debugger.areBreakpointsActive();
    }

    @Override
    public void setBreakpointsActive(final boolean active) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                debugger.setBreakpointsActive(active);
            }
        });
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        DelegateListener dl = new DelegateListener(this, l);
        listenersMap.put(l, dl);
        debugger.addPropertyChangeListener(dl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        DelegateListener dl = listenersMap.remove(l);
        if (dl != null) {
            debugger.removePropertyChangeListener(dl);
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
            if (Debugger.PROP_BREAKPOINTS_ACTIVE.equals(evt.getPropertyName())) {
                l.propertyChange(new PropertyChangeEvent(
                        source,
                        ActiveBreakpoints.PROP_BREAKPOINTS_ACTIVE,
                        evt.getOldValue(),
                        evt.getNewValue()));
            }
        }
    }

}

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
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = JSBreakpointsInfo.class)
public class WebBreakpointsActiveService implements JSBreakpointsInfo {

    private volatile boolean active = true;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public WebBreakpointsActiveService() {
        SessionActiveListener sal = new SessionActiveListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, sal);
    }
    
    @Override
    public boolean areBreakpointsActivated() {
        return active;
    }
    
    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            pcs.firePropertyChange(PROP_BREAKPOINTS_ACTIVE, !active, active);
        }
    }
    
    @Override
    public boolean isAnnotatable(FileObject fo) {
        String mimeType = fo.getMIMEType();
        return MiscEditorUtil.isJSOrWrapperMIMEType(mimeType);
    }

    @Override
    public boolean isTransientURL(URL url) {
        return false;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private class SessionActiveListener extends DebuggerManagerAdapter {
        
        private Debugger currentDebugger;
        
        public SessionActiveListener() {
            currentDebugger = getCurrentDebugger();
            if (currentDebugger != null) {
                active = currentDebugger.areBreakpointsActive();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                Debugger newDebugger = getCurrentDebugger();
                synchronized (this) {
                    if (currentDebugger != null) {
                        currentDebugger.removePropertyChangeListener(this);
                    }
                    currentDebugger = newDebugger;
                }
                if (newDebugger != null) {
                    setActive(newDebugger.areBreakpointsActive());
                } else {
                    setActive(true);
                }
            }
            if (Debugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
                setActive(((Debugger) evt.getSource()).areBreakpointsActive());
            }
        }
        
        private Debugger getCurrentDebugger() {
            Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (s != null) {
                Debugger debugger = s.lookupFirst(null, Debugger.class);
                if (debugger != null) {
                    debugger.addPropertyChangeListener(this);
                }
                return debugger;
            } else {
                return null;
            }
        }

    }
}

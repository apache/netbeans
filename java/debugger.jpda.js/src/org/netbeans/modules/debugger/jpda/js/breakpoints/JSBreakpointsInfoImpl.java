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

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.javascript2.debug.sources.SourceFilesCache;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = JSBreakpointsInfo.class)
public class JSBreakpointsInfoImpl implements JSBreakpointsInfo {
    
    private volatile boolean active = true;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public JSBreakpointsInfoImpl() {
        SessionActiveListener sal = new SessionActiveListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, sal);
    }

    @Override
    public boolean isAnnotatable(FileObject fo) {
        String mimeType = fo.getMIMEType();
        return JSUtils.JS_MIME_TYPE.equals(mimeType);
    }

    @Override
    public boolean isTransientURL(URL url) {
        return SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol());
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
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private class SessionActiveListener extends DebuggerManagerAdapter {
        
        private JPDADebugger currentDebugger;
        
        public SessionActiveListener() {
            currentDebugger = getCurrentDebugger();
            if (currentDebugger != null) {
                active = currentDebugger.getBreakpointsActive();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                JPDADebugger newDebugger = getCurrentDebugger();
                synchronized (this) {
                    if (currentDebugger != null) {
                        currentDebugger.removePropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
                    }
                    currentDebugger = newDebugger;
                }
                if (newDebugger != null) {
                    setActive(newDebugger.getBreakpointsActive());
                } else {
                    setActive(true);
                }
            }
            if (JPDADebugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
                setActive(((JPDADebugger) evt.getSource()).getBreakpointsActive());
            }
        }
        
        private JPDADebugger getCurrentDebugger() {
            Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (s != null) {
                JPDADebugger debugger = s.lookupFirst(null, JPDADebugger.class);
                if (debugger != null) {
                    debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, this);
                }
                return debugger;
            } else {
                return null;
            }
        }

    }
}

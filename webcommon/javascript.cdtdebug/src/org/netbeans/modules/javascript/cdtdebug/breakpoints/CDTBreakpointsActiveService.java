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

package org.netbeans.modules.javascript.cdtdebug.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.javascript2.debug.sources.SourceFilesCache;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Entlicher
 */
@ServiceProvider(service = JSBreakpointsInfo.class)
public class CDTBreakpointsActiveService implements JSBreakpointsInfo {

    public static final String JS_MIME_TYPE = "text/javascript";    // NOI18N

    private volatile boolean active = true;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public CDTBreakpointsActiveService() {
        SessionActiveListener sal = new SessionActiveListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, sal);
    }

    @Override
    public boolean isAnnotatable(FileObject fo) {
        String mimeType = fo.getMIMEType();
        return JS_MIME_TYPE.equals(mimeType);
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

    private class SessionActiveListener extends DebuggerManagerAdapter implements BreakpointsHandler.BreakpointsActiveListener {

        private BreakpointsHandler bh;

        public SessionActiveListener() {
            bh = getCurrentBreakpointsHandler();
            if (bh != null) {
                active = bh.areBreakpointsActive();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                BreakpointsHandler newBh = getCurrentBreakpointsHandler();
                synchronized (this) {
                    if (bh != null) {
                        bh.removeBreakpointsActiveListener(this);
                    }
                    bh = newBh;
                }
                if (newBh != null) {
                    setActive(newBh.areBreakpointsActive());
                } else {
                    setActive(true);
                }
            }
        }

        @Override
        public void breakpointsActivated(boolean activated) {
            setActive(activated);
        }

        private BreakpointsHandler getCurrentBreakpointsHandler() {
            Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (s != null) {
                CDTDebugger debugger = s.lookupFirst(null, CDTDebugger.class);
                if (debugger != null) {
                    BreakpointsHandler bh = debugger.getBreakpointsHandler();
                    bh.addBreakpointsActiveListener(this);
                    return bh;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

    }
}

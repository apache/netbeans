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

package org.netbeans.modules.web.javascript.debugger.callstack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 * The default JavaScript thread.
 * 
 * @author Martin Entlicher
 */
public class JSThread implements DebuggingView.DVThread {
    
    private final Debugger debugger;
    private final DebuggingView.DVSupport dvSupport;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    public JSThread(Debugger debugger, DebuggingView.DVSupport dvSupport) {
        this.debugger = debugger;
        this.dvSupport = dvSupport;
        ChangeListener chl = new ChangeListener();
        debugger.addListener(chl);
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public boolean isSuspended() {
        return debugger.isSuspended();
    }

    @Override
    public void resume() {
        debugger.resume();
    }

    @Override
    public void suspend() {
        debugger.pause();
    }

    @Override
    public void makeCurrent() {
    }

    @Override
    public DebuggingView.DVSupport getDVSupport() {
        return dvSupport;
    }

    @Override
    public List<DebuggingView.DVThread> getLockerThreads() {
        return Collections.emptyList();
    }

    @Override
    public void resumeBlockingThreads() {
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return null; // TODO
    }

    @Override
    public boolean isInStep() {
        return false; // Used when a deadlock occurs during stepping.
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pchs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pchs.removePropertyChangeListener(pcl);
    }

    private class ChangeListener implements Debugger.Listener {

        public ChangeListener() {}

        @Override
        public void paused(List<CallFrame> callStack, String reason) {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_SUSPENDED, null, true);
        }

        @Override
        public void resumed() {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_SUSPENDED, null, false);
        }

        @Override
        public void reset() {
        }

        @Override
        public void enabled(boolean enabled) {
        }
        
        
    }
    
}

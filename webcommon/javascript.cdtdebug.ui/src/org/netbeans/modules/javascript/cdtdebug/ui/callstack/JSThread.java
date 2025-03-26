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

package org.netbeans.modules.javascript.cdtdebug.ui.callstack;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.breakpoints.BreakpointsHandler;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 * The default JavaScript thread.
 */
public class JSThread implements DebuggingView.DVThread {

    private final CDTDebugger dbg;
    private final DebuggingView.DVSupport dvSupport;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);

    public JSThread(CDTDebugger dbg, DebuggingView.DVSupport dvSupport) {
        this.dbg = dbg;
        this.dvSupport = dvSupport;
        ChangeListener chl = new ChangeListener();
        dbg.addListener(chl);
        dbg.getBreakpointsHandler().addActiveBreakpointListener(chl);
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public boolean isSuspended() {
        return dbg.isSuspended();
    }

    @Override
    public void resume() {
        dbg.resume();
    }

    @Override
    public void suspend() {
        dbg.suspend();
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
        return Collections.EMPTY_LIST;
    }

    @Override
    public void resumeBlockingThreads() {
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return dbg.getBreakpointsHandler().getActiveBreakpoint();
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

    private class ChangeListener implements CDTDebugger.Listener, BreakpointsHandler.ActiveBreakpointListener {

        public ChangeListener() {}

        @Override
        public void notifySuspended(boolean suspended) {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_SUSPENDED, null, suspended);
        }

        @Override
        public void notifyCurrentFrame(CallFrame cf) {
        }

        @Override
        public void notifyFinished() {
        }

        @Override
        public void notifyActiveBreakpoint(JSLineBreakpoint activeBreakpoint) {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_BREAKPOINT, null, activeBreakpoint);
        }


    }

}

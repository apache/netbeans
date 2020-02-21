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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugging;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 *
 */

public abstract class NativeDVSupportImpl extends DebuggingView.DVSupport{    
    private final NativeDebugger debugger;
    
    public NativeDVSupportImpl(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
    }

    @Override
    public STATE getState() {
        if (debugger.state().isRunning) {
            return STATE.RUNNING;
        }

        return STATE.DISCONNECTED;
    }

    @Override
    public List<DebuggingView.DVThread> getAllThreads() {
        return Arrays.asList((DebuggingView.DVThread[])  debugger.getThreadsWithStacks());
    }

    @Override
    public DebuggingView.DVThread getCurrentThread() {
        for (DebuggingView.DVThread dVThread : getAllThreads()) {
            if (((org.netbeans.modules.cnd.debugger.common2.debugger.Thread) dVThread).isCurrent()) {
                return dVThread;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        return "";
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return debugger.session().coreSession();
    }

    @Override
    public void resume() {
         debugger.go();
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        return null;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        List<DebuggingView.DVFilter> list = new ArrayList<DebuggingView.DVFilter>();
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendedThreadsOnly));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showThreadGroups));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendTable));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSystemThreads));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showMonitors));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showQualifiedNames));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortSuspend));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortAlphabetic));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortNatural));
        return list;
    }
}

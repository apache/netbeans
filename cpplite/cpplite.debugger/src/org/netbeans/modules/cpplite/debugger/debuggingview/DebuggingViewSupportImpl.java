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
package org.netbeans.modules.cpplite.debugger.debuggingview;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.cpplite.debugger.ThreadsCollector;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

@DebuggingView.DVSupport.Registration(path="CPPLiteSession")
public class DebuggingViewSupportImpl extends DebuggingView.DVSupport implements CPPLiteDebugger.StateListener, ThreadsCollector.StateListener {

    static final String THREAD_SUSPENDED_ICON =
            "org/netbeans/modules/debugger/resources/threadsView/thread_suspended_16.png";
    static final String THREAD_RUNNING_ICON =
            "org/netbeans/modules/debugger/resources/threadsView/thread_running_16.png";

    private final CPPLiteDebugger debugger;
    private final Session session;

    public DebuggingViewSupportImpl(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, CPPLiteDebugger.class);
        session = contextProvider.lookupFirst(null, Session.class);
        debugger.addStateListener(WeakListeners.create(CPPLiteDebugger.StateListener.class, this, debugger));
        debugger.getThreads().addStateListener(WeakListeners.create(ThreadsCollector.StateListener.class, this, debugger.getThreads()));
    }

    @Override
    public STATE getState() {
        return debugger.isFinished() ? STATE.DISCONNECTED : STATE.RUNNING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DebuggingView.DVThread> getAllThreads() {
        return (List<DebuggingView.DVThread>) (List) debugger.getThreads().getAll();
    }

    @Override
    public DebuggingView.DVThread getCurrentThread() {
        return debugger.getCurrentThread();
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        return ((CPPThread) thread).getName();
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        String iconPath = thread.isSuspended() ? THREAD_SUSPENDED_ICON : THREAD_RUNNING_ICON;
        return ImageUtilities.loadImage(iconPath);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void resume() {
        session.getCurrentEngine().getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        return null;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        return Collections.emptyList();
    }

    @Override
    public void currentThread(CPPThread thread) {
        firePropertyChange(PROP_CURRENT_THREAD, null, thread);
    }

    @Override
    public void currentFrame(CPPFrame frame) {
    }

    @Override
    public void suspended(boolean suspended) {
    }

    @Override
    public void finished() {
    }

    @Override
    public void threadStarted(CPPThread thread) {
        firePropertyChange(PROP_THREAD_STARTED, null, thread);
    }

    @Override
    public void threadDied(CPPThread thread) {
        firePropertyChange(PROP_THREAD_DIED, thread, null);
    }

    public void doFirePropertyChange(String propertyName, Object oldValue, Object newValue) {
        firePropertyChange(propertyName, oldValue, newValue);
    }

}

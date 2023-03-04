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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ThreadsCollector;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadGroupImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@DebuggingView.DVSupport.Registration(path="netbeans-JPDASession")
public class DebuggingViewSupportImpl extends DebuggingView.DVSupport {
    
    private final JPDADebuggerImpl debugger;
    private final Map<JPDAThreadImpl, JPDADVThread> threadsMap = new WeakCacheMap<>();
    private final Map<JPDAThreadGroupImpl, JPDADVThreadGroup> threadGroupsMap = new WeakCacheMap<>();
    
    public DebuggingViewSupportImpl(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        ChangeListener chl = new ChangeListener();
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, chl);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_THREAD, chl);
        debugger.getThreadsCollector().addPropertyChangeListener(chl);
        debugger.getThreadsCollector().getDeadlockDetector().addPropertyChangeListener(chl);
    }
    
    @Override
    public DebuggingView.DVThread getCurrentThread() {
        JPDAThreadImpl currentThread = (JPDAThreadImpl) debugger.getCurrentThread();
        if (currentThread != null &&
                !(currentThread.isSuspended() || currentThread.isSuspendedNoFire()) &&
                !currentThread.isMethodInvoking()) {
            currentThread = null;
        }
        return get(currentThread);
    }

    @Override
    public STATE getState() {
        int state = debugger.getState();
        if (state == JPDADebugger.STATE_DISCONNECTED) {
            return STATE.DISCONNECTED;
        } else {
            return STATE.RUNNING;
        }
    }

    @Override
    public List<DebuggingView.DVThread> getAllThreads() {
        List<JPDAThread> threads = debugger.getThreadsCollector().getAllThreads();
        List<DebuggingView.DVThread> dvThreads = new ArrayList<>(threads.size());
        for (JPDAThread t : threads) {
            dvThreads.add(get(t));
        }
        return Collections.unmodifiableList(dvThreads);
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        String name;
        try {
            JPDAThread jt = ((JPDADVThread) thread).getKey();
            name = DebuggingNodeModel.getDisplayName(jt, false);
            Session session = debugger.getSession();
            Session currSession = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (session != currSession) {
                String str = NbBundle.getMessage(DebuggingViewSupportImpl.class, "CTL_Session",
                        session.getName());
                name = name.charAt(0) + str + ", " + name.substring(1);
            }
        } catch (UnknownTypeException e) {
            name = thread.getName();
        }
        return name;
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        return ImageUtilities.loadImage(DebuggingNodeModel.getIconBase(((JPDADVThread) thread).getKey()));
    }

    @Override
    public Session getSession() {
        return debugger.getSession();
    }

    @Override
    public void resume() {
        debugger.resume();
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        Set<DeadlockDetector.Deadlock> dds = debugger.getThreadsCollector().getDeadlockDetector().getDeadlocks();
        if (dds == null) {
            return null;
        }
        Set<DebuggingView.Deadlock> dvds = new HashSet<DebuggingView.Deadlock>(dds.size());
        for (DeadlockDetector.Deadlock dd : dds) {
            Collection threads = dd.getThreads();
            dvds.add(createDeadlock(threads));
        }
        return dvds;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        List<DebuggingView.DVFilter> list = new ArrayList<DebuggingView.DVFilter>();
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendedThreadsOnly));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showThreadGroups));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendTable));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSystemThreads));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showMonitors));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showQualifiedNames));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortSuspend));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortAlphabetic));
        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortNatural));
        return list;
    }
    
    private static Preferences preferences;
    public static Preferences getFilterPreferences() {
        if (preferences == null) {
            preferences = DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showThreadGroups).getPreferences();
        }
        return preferences;
    }
    
    public JPDADVThread get(JPDAThread t) {
        if (t == null) {
            return null;
        }
        JPDADVThread dvt;
        synchronized (threadsMap) {
            dvt = threadsMap.get(t);
            if (dvt == null) {
                dvt = new JPDADVThread(this, (JPDAThreadImpl) t);
                threadsMap.put((JPDAThreadImpl) t, dvt);
            }
        }
        return dvt;
    }
    
    public JPDADVThread[] get(JPDAThread[] threads) {
        int n = threads.length;
        JPDADVThread[] dvThreads = new JPDADVThread[n];
        for (int i = 0; i < n; i++) {
            dvThreads[i] = get((JPDAThreadImpl) threads[i]);
        }
        return dvThreads;

    }
    
    public JPDADVThreadGroup get(JPDAThreadGroup tg) {
        if (tg == null) {
            return null;
        }
        JPDADVThreadGroup dvtg;
        synchronized (threadGroupsMap) {
            dvtg = threadGroupsMap.get(tg);
            if (dvtg == null) {
                dvtg = new JPDADVThreadGroup(this, (JPDAThreadGroupImpl) tg);
                threadGroupsMap.put((JPDAThreadGroupImpl) tg, dvtg);
            }
        }
        return dvtg;
    }
    
    public JPDADVThreadGroup[] get(JPDAThreadGroup[] threadGroups) {
        int n = threadGroups.length;
        JPDADVThreadGroup[] dvGroups = new JPDADVThreadGroup[n];
        for (int i = 0; i < n; i++) {
            dvGroups[i] = get((JPDAThreadGroupImpl) threadGroups[i]);
        }
        return dvGroups;
    }
    
    protected int getFrameCount(JPDADVThread thread) {
        return thread.getKey().getStackDepth();
    }
    
    protected List<DVFrame> getFrames(JPDADVThread thread, int from, int to) {
        return JPDADVThread.getFrames(thread, from, to);
    }

    private class ChangeListener implements PropertyChangeListener {
        
        private STATE state = STATE.DISCONNECTED;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (ThreadsCollector.PROP_THREAD_STARTED.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_THREAD_STARTED,
                                   get((JPDAThreadImpl) evt.getOldValue()),
                                   get((JPDAThreadImpl) evt.getNewValue()));
            } else
            if (ThreadsCollector.PROP_THREAD_DIED.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_THREAD_DIED,
                                   get((JPDAThreadImpl) evt.getOldValue()),
                                   get((JPDAThreadImpl) evt.getNewValue()));
            } else
            if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_CURRENT_THREAD,
                                   get((JPDAThreadImpl) evt.getOldValue()),
                                   get((JPDAThreadImpl) evt.getNewValue()));
            } else
            if (ThreadsCollector.PROP_THREAD_SUSPENDED.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_THREAD_SUSPENDED,
                                   get((JPDAThreadImpl) evt.getOldValue()),
                                   get((JPDAThreadImpl) evt.getNewValue()));
            } else
            if (ThreadsCollector.PROP_THREAD_RESUMED.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_THREAD_RESUMED,
                                   get((JPDAThreadImpl) evt.getOldValue()),
                                   get((JPDAThreadImpl) evt.getNewValue()));
            } else
            if (JPDADebugger.PROP_STATE.equals(propertyName)) {
                int ds = debugger.getState();
                if (ds == JPDADebugger.STATE_RUNNING && this.state != STATE.RUNNING) {
                    this.state = STATE.RUNNING;
                    firePropertyChange(DebuggingView.DVSupport.PROP_STATE, STATE.DISCONNECTED, STATE.RUNNING);
                } else
                if (ds == JPDADebugger.STATE_DISCONNECTED) {
                    firePropertyChange(DebuggingView.DVSupport.PROP_STATE, STATE.RUNNING, STATE.DISCONNECTED);
                }
            } else
            if (DeadlockDetector.PROP_DEADLOCK.equals(propertyName)) {
                firePropertyChange(DebuggingView.DVSupport.PROP_DEADLOCK, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
    
}

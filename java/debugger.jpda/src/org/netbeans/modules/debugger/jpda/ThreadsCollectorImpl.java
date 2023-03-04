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

package org.netbeans.modules.debugger.jpda;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ThreadsCollector;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.WeakListeners;

/**
 *
 * @author martin
 */
public class ThreadsCollectorImpl extends ThreadsCollector {
    
    private JPDADebuggerImpl debugger;
    
    private PropertyChangeListener changesInThreadsListener;
    private final Map<JPDAThread, ThreadStateListener> threadStateListeners = new WeakHashMap<JPDAThread, ThreadStateListener>();
    private final List<JPDAThread> threads = new ArrayList<JPDAThread>();

    public ThreadsCollectorImpl(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        List<JPDAThread> allThreads = debugger.getAllThreads();
        synchronized (threads) {
            threads.addAll(allThreads);
        }
        changesInThreadsListener = new ChangesInThreadsListener();
        debugger.addPropertyChangeListener(WeakListeners.propertyChange(changesInThreadsListener, debugger));
        for (JPDAThread thread : allThreads) {
            watchThread(thread);
        }
    }

    @Override
    public List<JPDAThread> getAllThreads() {
        synchronized (threads) {
            return Collections.unmodifiableList(new ArrayList<JPDAThread>(threads));
        }
    }

    @Override
    public DeadlockDetector getDeadlockDetector() {
        return debugger.getDeadlockDetector();
    }
    
    private void watchThread(JPDAThread thread) {
        synchronized (threadStateListeners) {
            if (!threadStateListeners.containsKey(thread)) {
                threadStateListeners.put(thread, new ThreadStateListener(thread));
            }
        }
    }

    public boolean isSomeThreadRunning() {
        for (JPDAThread thread : getAllThreads()) {
            if (!thread.isSuspended() && !((JPDAThreadImpl) thread).isMethodInvoking()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSomeThreadSuspended() {
        for (JPDAThread thread : getAllThreads()) {
            if (thread.isSuspended() || ((JPDAThreadImpl) thread).isMethodInvoking()) {
                return true;
            }
        }
        return false;
    }

    private class ThreadStateListener implements PropertyChangeListener {
        
        //private JPDAThread thread;
        
        public ThreadStateListener(JPDAThread thread) {
            //this.thread = thread;
            ((JPDAThreadImpl) thread).addPropertyChangeListener(WeakListeners.propertyChange(this, thread));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDAThread.PROP_SUSPENDED.equals(evt.getPropertyName())) {
                if ("methodInvoke".equals(evt.getPropagationId())) {
                    return ; // Ignore events associated with method invocations
                }
                JPDAThread thread = (JPDAThread) evt.getSource();
                if (thread.isSuspended()) {
                    firePropertyChange(PROP_THREAD_SUSPENDED, null, thread);
                } else {
                    firePropertyChange(PROP_THREAD_RESUMED, null, thread);
                }
            }
        }
        
    }
    
    private class ChangesInThreadsListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (JPDADebugger.PROP_THREAD_STARTED.equals(propertyName)) {
                JPDAThread thread = (JPDAThread) evt.getNewValue();
                watchThread(thread);
                synchronized (threads) {
                    if (!threads.contains(thread)) {
                        threads.add(thread); // Could be already added in constructor...
                    }
                }
                firePropertyChange(PROP_THREAD_STARTED, evt.getOldValue(), evt.getNewValue());
            } else if (JPDADebugger.PROP_THREAD_DIED.equals(propertyName)) {
                JPDAThread thread = (JPDAThread) evt.getOldValue();
                synchronized (threads) {
                    threads.remove(thread);
                }
                firePropertyChange(PROP_THREAD_DIED, evt.getOldValue(), evt.getNewValue());
            } else if (JPDADebugger.PROP_THREAD_GROUP_ADDED.equals(propertyName)) {
                firePropertyChange(PROP_THREAD_GROUP_ADDED, evt.getOldValue(), evt.getNewValue());
            }
        }
        
    }
}

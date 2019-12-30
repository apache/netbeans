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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.util.Exceptions;

@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class ThreadsListener extends DebuggerManagerAdapter {

    private static ThreadsListener instance;
    private static PropertyChangeSupport pchs = new PropertyChangeSupport(ThreadsListener.class);
    private static final ThreadsPropertyChangeListener tpchl = new ThreadsPropertyChangeListener();
    
    final LinkedList<DVThread> currentThreadsHistory = new LinkedList<>();
    final BreakpointHits hits = new BreakpointHits();
    private Map<DVSupport, DebuggerListener> debuggerToListener = new WeakHashMap<DVSupport, DebuggerListener>();
    private DVSupport currentDebugger = null;
    private DebuggingViewComponent debuggingView;
    
    /**
     * Constructor for the registry only.
     * @deprecated Do not call, use ThreadsListener.getDefault() instead.
     */
    @Deprecated()
    public ThreadsListener() {
        instance = this;
    }

    /** Can return <code>null</code> when not initialized yet.
     * @return The ThreadsListener instance or <code>null</code>.
     */
    public static ThreadsListener getDefault() {
        return instance;
    }
    
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
    }
    
    public void setDebuggingView(DVSupport dvs, DebuggingViewComponent debuggingView) {
        this.debuggingView = debuggingView;
        InfoPanel infoPanel = debuggingView.getInfoPanel();
        infoPanel.setBreakpointHits(dvs, getHits());
    }
    
    public synchronized void changeDebugger(DVSupport deb) {
        if (currentDebugger == deb) {
            return;
        }
        if (currentDebugger != null) {
            currentDebugger.removePropertyChangeListener(tpchl);
        }
        if (deb != null) {
            deb.addPropertyChangeListener(tpchl);
            DVThread currThread = deb.getCurrentThread();
            if (currThread != null) {
                synchronized(currentThreadsHistory) {
                    currentThreadsHistory.remove(currThread);
                    currentThreadsHistory.addFirst(currThread);
                }
            }
            
        }
        if (debuggingView == null) {
            this.currentDebugger = deb;
            return;
        }
        if (currentDebugger != null) {
            InfoPanel infoPanel = debuggingView.getInfoPanel();
            infoPanel.setShowDeadlock(false);
            infoPanel.setShowThreadLocks(null, null);
            infoPanel.setShowStepBrkp(null, null, null);
        }
        if (deb != null) {
            InfoPanel infoPanel = debuggingView.getInfoPanel();
            DebuggerListener listener = debuggerToListener.get(deb);
            if (listener != null) {
                //DeadlockDetector detector = deb.getThreadsCollector().getDeadlockDetector();
                //detector.addPropertyChangeListener(this);
                if (deb.getDeadlocks() != null) {
                    infoPanel.setShowDeadlock(true);
                }
                infoPanel.setShowThreadLocks(listener.lockedThread, listener.lockerThreads);
                infoPanel.setShowStepBrkp(listener.debugger, listener.stepBrkpThread, listener.stepBrkpBreakpoint);
            }
            infoPanel.recomputeMenuItems(deb, getHits());
        } else {
            // Release reference to DebuggingView when there's no debugger.
            debuggingView = null;
        }
        this.currentDebugger = deb;
    }
    
    public synchronized List<DVThread> getCurrentThreadsHistory() {
        synchronized(currentThreadsHistory) {
            List<DVThread> result = new ArrayList<DVThread>(currentThreadsHistory.size());
            for (DVThread thread : currentThreadsHistory) {
                if (thread.isSuspended()) {
                    result.add(thread);
                }
            }
            return result;
        }
    }
    
    public synchronized List<DVThread> getThreads() {
        List<DVThread> result = new ArrayList<DVThread>();
        for (DVSupport debugger : debuggerToListener.keySet()) {
            if (debugger != null && debugger.getState() != DVSupport.STATE.DISCONNECTED) {
                result.addAll(debugger.getAllThreads());
            }
        }
        return result;
    }

    private void addBreakpointHit(DVThread thread) {
        if (thread != null && !hits.contains(thread)) {
            // System.out.println("Hit added: " + thread.getName());
            hits.add(thread);
            if (debuggingView != null) {
                debuggingView.getInfoPanel().addBreakpointHit(thread, hits.size());
            }
        }
    }

    private void removeBreakpointHit(DVThread thread) {
        if (thread != null && hits.contains(thread)) {
            // System.out.println("Hit removed: " + thread.getName());
            hits.remove(thread);
            if (debuggingView != null) {
                debuggingView.getInfoPanel().removeBreakpointHit(thread, hits.size());
            }
        }
    }
    
    public synchronized List<DVThread> getHits() {
        List<DVThread> result = new ArrayList<DVThread>();
        for (DVThread thread : hits.getStoppedThreads()) {
            result.add(thread);
        }
        return result;
    }
    
    public synchronized int getHitsCount() {
        return hits.size();
    }
    
    public synchronized boolean isBreakpointHit(DVThread thread) {
        return hits.contains(thread);
    }
    
    public synchronized void goToHit() {
        hits.goToHit();
    }
    
    public DVSupport getDVSupport() {
        return currentDebugger;
    }

    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_DEBUGGER_ENGINES};
    }

    @Override
    public synchronized void engineAdded(DebuggerEngine engine) {
        DVSupport deb = engine.lookupFirst(null, DVSupport.class);
        if (deb != null) {
            DebuggerListener listener = new DebuggerListener(deb);
            debuggerToListener.put(deb, listener);
            if (debuggingView != null) {
                debuggingView.updateSessionsComboBox();
            }
        }
    }

    @Override
    public synchronized void engineRemoved(DebuggerEngine engine) {
        DVSupport deb = engine.lookupFirst(null, DVSupport.class);
        if (deb != null) {
            DebuggerListener listener = debuggerToListener.remove(deb);
            if (listener != null) {
                listener.unregister();
            }
            if (debuggingView != null) {
                debuggingView.updateSessionsComboBox();
            }
        }
    }
    
    // **************************************************************************
    // inner classes
    // **************************************************************************

    class DebuggerListener implements PropertyChangeListener {

        private DVSupport debugger;
        Set<DVThread> threads = new HashSet<DVThread>();
        List<DVThread> lockerThreads;
        DVThread lockedThread;
        DVThread stepBrkpThread;
        Breakpoint stepBrkpBreakpoint;

        DebuggerListener(DVSupport debugger) {
            this.debugger = debugger;
            debugger.addPropertyChangeListener(this);
            List<DVThread> allThreads = debugger.getAllThreads();
            for (DVThread thread : allThreads) {
                threads.add(thread);
                thread.addPropertyChangeListener(this);
            }
            //DeadlockDetector detector = debugger.getThreadsCollector().getDeadlockDetector();
            //detector.addPropertyChangeListener(this);
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            //System.out.println("PROP. NAME: " + evt.getPropertyName() + ", " + evt.getSource().getClass().getSimpleName());
            String propName = evt.getPropertyName();
            Object source = evt.getSource();

            if (source instanceof DVSupport) {
                if (DVSupport.PROP_THREAD_STARTED.equals(propName)) {
                    //System.out.println("STARTED: " + evt.getNewValue());
                    final DVThread dvThread = (DVThread)evt.getNewValue();
                    if (threads.add(dvThread)) {
                        dvThread.addPropertyChangeListener(this);
                        // System.out.println("WATCHED: " + dvThread.getName());
                    }
                } else if (DVSupport.PROP_THREAD_DIED.equals(propName)) {
                    //System.out.println("DIED: " + evt.getOldValue());
                    DVThread dvThread = (DVThread)evt.getOldValue();
                    if (threads.remove(dvThread)) {
                        synchronized(currentThreadsHistory) {
                            currentThreadsHistory.remove(dvThread);
                        }
                        dvThread.removePropertyChangeListener(this);
                        removeBreakpointHit(dvThread);
                        // System.out.println("RELEASED: " + dvThread.getName());
                    }
                } else if (DVSupport.PROP_CURRENT_THREAD.equals(propName)) {
                    DVThread currentThread = debugger.getCurrentThread();
                    if (currentThread != null) {
                        removeBreakpointHit(currentThread);
                        synchronized(currentThreadsHistory) {
                            currentThreadsHistory.remove(currentThread);
                            currentThreadsHistory.addFirst(currentThread);
                        }
                    }
                } else if (DVSupport.PROP_STATE.equals(propName) &&
                           debugger != null && debugger.getState() == DVSupport.STATE.DISCONNECTED) {
                    unregister();
                } else if (DVSupport.PROP_DEADLOCK.equals(propName)) {
                    setShowDeadlock(true);
                }
            } else if (source instanceof DVThread) {
                final DVThread thread = (DVThread)source;
                if (DVThread.PROP_BREAKPOINT.equals(propName)) {
                    // System.out.println("THREAD: " + thread.getName() + ", curr: " + isCurrent(thread) + ", brk: " + isAtBreakpoint(thread));
                    if (!isCurrent(thread)) {
                        if (isAtBreakpoint(thread)) {
                            addBreakpointHit(thread);
                        } else {
                            removeBreakpointHit(thread);
                        }
                    } else {
                        removeBreakpointHit(thread);
                    }
                    if (debugger == currentDebugger && debuggingView != null) {
                        debuggingView.refreshView(); // [TODO]
                    }
                } else if (DVThread.PROP_SUSPENDED.equals(propName)) {
                    if (!thread.isSuspended()) {
                        removeBreakpointHit(thread);
                    }
                    if (debugger == currentDebugger && debuggingView != null) {
                        debuggingView.refreshView(); // [TODO]
                    }
                } else if (DVThread.PROP_LOCKER_THREADS.equals(propName)) { // NOI18N
                    // Calling List<DVThread> getLockerThreads()
                    List<DVThread> currLockerThreads = thread.getLockerThreads();
                    /*List<DVThread> currLockerThreads;
                    try {
                        java.lang.reflect.Method lockerThreadsMethod = thread.getClass().getMethod("getLockerThreads", new Class[] {}); // NOI18N
                        currLockerThreads = (List<DVThread>) lockerThreadsMethod.invoke(thread, new Object[] {});
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        currLockerThreads = null;
                    }*/
                    setShowThreadLocks(thread, currLockerThreads);
                } else if ("stepSuspendedByBreakpoint".equals(propName)) {
                    setShowStepBrkp(thread, (Breakpoint) evt.getNewValue());
                }
            }
        }

        private synchronized void unregister() {
            if (debugger == null) return ;
            for (DVThread thread : threads) {
                thread.removePropertyChangeListener(this);
            }
            synchronized(currentThreadsHistory) {
                for (DVThread thread : threads) {
                    currentThreadsHistory.remove(thread);
                }
            }
            synchronized(hits) {
                for (DVThread thread : threads) {
                    removeBreakpointHit(thread);
                }
            }
            threads.clear();
            lockedThread = null;
            lockerThreads = null;
            stepBrkpThread = null;
            stepBrkpBreakpoint = null;
            debugger.removePropertyChangeListener(this);
            debugger./*getThreadsCollector().getDeadlockDetector().*/removePropertyChangeListener(this);
            debugger = null;
        }
        
        private boolean isCurrent(DVThread thread) {
            return debugger.getCurrentThread() == thread;
        }

        private boolean isAtBreakpoint(DVThread thread) {
            Breakpoint breakpoint = thread.getCurrentBreakpoint();
            return breakpoint != null;// && !breakpoint.isHidden();
        }

        private void setShowDeadlock(boolean detected) {
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowDeadlock(detected);
            }
        }

        private void setShowThreadLocks(DVThread thread, List<DVThread> currLockerThreads) {
            lockerThreads = currLockerThreads;
            lockedThread = thread;
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowThreadLocks(thread, lockerThreads);
            }
        }
        
        private void setShowStepBrkp(DVThread thread, Breakpoint breakpoint) {
            stepBrkpThread = thread;
            stepBrkpBreakpoint = breakpoint;
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowStepBrkp(debugger, thread, breakpoint);
            }
        }

    }

    static class BreakpointHits {
        private Set<DVThread> stoppedThreadsSet = new HashSet<DVThread>();
        private LinkedList<DVThread> stoppedThreads = new LinkedList<DVThread>();
        
        public void goToHit() {
            DVThread thread;
            synchronized (this) {
                thread = stoppedThreads.getLast();
            }
            thread.makeCurrent();
        }
        
        public synchronized boolean contains(DVThread thread) {
            return stoppedThreadsSet.contains(thread);
        }
        
        public synchronized boolean add(DVThread thread) {
            if (stoppedThreadsSet.add(thread)) {
                stoppedThreads.addFirst(thread);
                return true;
            }
            return false;
        }
        
        public synchronized boolean remove(DVThread thread) {
            if (stoppedThreadsSet.remove(thread)) {
                stoppedThreads.remove(thread);
                return true;
            }
            return false;
        }
        
        public synchronized void clear() {
            stoppedThreadsSet.clear();
            stoppedThreads.clear();
        }
        
        public synchronized int size() {
            return stoppedThreads.size();
        }

        private synchronized Iterable<DVThread> getStoppedThreads() {
            return new ArrayList<DVThread>(stoppedThreads);
        }
        
    }
    
    private static class ThreadsPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //pchs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            pchs.firePropertyChange(evt);
        }
        
    }

}

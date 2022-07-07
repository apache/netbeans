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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingViewSupportImpl;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.models.SourcesModel.AbstractColumn;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThreadGroup;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CachedChildrenTreeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types={TreeModel.class, AsynchronousModelFilter.class},
                             position=10000)
public class DebuggingTreeModel extends CachedChildrenTreeModel {
    
    public static final String SORT_ALPHABET = "sort.alphabet";
    public static final String SORT_SUSPEND = "sort.suspend";
    public static final String SHOW_SYSTEM_THREADS = "show.systemThreads";
    public static final String SHOW_THREAD_GROUPS = "show.threadGroups";
    public static final String SHOW_SUSPENDED_THREADS_ONLY = "show.suspendedThreadsOnly";
    
    private static final Set<String> SYSTEM_THREAD_NAMES = new HashSet<String>(Arrays.asList(new String[] {
                                                           "Reference Handler",
                                                           "Signal Dispatcher",
                                                           "Finalizer",
                                                           "Java2D Disposer",
                                                           "TimerQueue",
                                                           "Attach Listener"}));
    private static final Set<String> SYSTEM_MAIN_THREAD_NAMES = new HashSet<String>(Arrays.asList(new String[] {
                                                           "DestroyJavaVM",
                                                           "AWT-XAWT",
                                                           "AWT-Shutdown"}));
    
    private final JPDADebugger debugger;
    private Listener listener;
    private PreferenceChangeListener prefListener;
    private final PropertyChangeListener debuggerListener = new DebuggerFinishListener();
    private final Collection<ModelListener> listeners = new HashSet<ModelListener>();
    private final Map<JPDAThread, ThreadStateListener> threadStateListeners = new WeakHashMap<JPDAThread, ThreadStateListener>();
    private final Preferences preferences = DebuggingViewSupportImpl.getFilterPreferences();
    private final DebuggingViewSupportImpl dvSupport;

    private final DebuggingMonitorModel.Children monitorChildrenFilter;
    private final TreeModel childrenModelImpl;

    private final RequestProcessor RP = new RequestProcessor("Debugging Tree View Refresh", 1); // NOI18N
    
    public DebuggingTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        dvSupport = (DebuggingViewSupportImpl) lookupProvider.lookupFirst(null, DebuggingView.DVSupport.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, debuggerListener);
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, debuggerListener);
        } else {
            prefListener = new DebuggingPreferenceChangeListener();
            preferences.addPreferenceChangeListener(prefListener);
        }
        monitorChildrenFilter = new DebuggingMonitorModel.Children(debugger, dvSupport,
                new ModelListener() {
                    @Override
                    public void modelChanged(ModelEvent event) {
                        fireModelChange(event);
                    }
                }, this);
        childrenModelImpl = new ChildrenImplModel();
    }

    @Override
    protected Object[] computeChildren(Object parent) throws UnknownTypeException {
        return monitorChildrenFilter.getChildren(childrenModelImpl, parent, 0, Integer.MAX_VALUE);
    }

    void doRefreshCache(Object node) {
        refreshCache(node);
    }

    private class ChildrenImplModel implements TreeModel {

        @Override
        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            //System.err.println("DebuggingTreeModel.computeChildren("+parent+")");
            if (parent == ROOT) {
                boolean showThreadGroups = preferences.getBoolean(SHOW_THREAD_GROUPS, false);
                if (showThreadGroups) {
                    return getTopLevelThreadsAndGroups();
                } else {
                    JPDAThread[] threads = debugger.getThreadsCollector().getAllThreads().toArray(new JPDAThread[0]);
                    for (JPDAThread t : threads) {
                        watchState(t);
                    }
                    return dvSupport.get(threads);
                }
            }
            if (parent instanceof JPDADVThread) {
                JPDADVThread dvt = (JPDADVThread) parent;
                JPDAThread t = dvt.getKey();
                watchState(t);
                try {
                    return t.getCallStack();
                } catch (AbsentInformationException aiex) {
                    return new Object[0];
                }
            }
            if (parent instanceof JPDADVThreadGroup) {
                JPDAThreadGroup group = ((JPDADVThreadGroup) parent).getKey();
                JPDAThread[] threads = group.getThreads();
                for (JPDAThread t : threads) {
                    watchState(t);
                }
                JPDAThreadGroup[] groups = group.getThreadGroups();
                
                Object[] result = new Object[threads.length + groups.length];
                System.arraycopy(dvSupport.get(threads), 0, result, 0, threads.length);
                System.arraycopy(dvSupport.get(groups), 0, result, threads.length, groups.length);
                return result;
            }
            if (parent instanceof CallStackFrame) {
                return new Object[0];
            }
            throw new UnknownTypeException(parent.toString());
        }

        @Override
        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (node instanceof CallStackFrame) {
                return 0;
            }
            if (node instanceof JPDADVThread) {
                if (!((JPDADVThread) node).isSuspended()) {
                    return 0;
                }
            }
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getRoot() {
            return ROOT;
        }

        @Override
        public boolean isLeaf(Object node) throws UnknownTypeException {
            if (node instanceof CallStackFrame) {
                return true;
            }
            if (node instanceof JPDADVThread) {
                JPDAThreadImpl t = ((JPDADVThread) node).getKey();
                if (!t.isSuspended() && !t.isMethodInvoking()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void addModelListener(ModelListener l) {
        }

        @Override
        public void removeModelListener(ModelListener l) {
        }

    }
    
    protected Object[] reorder(Object[] nodes) {
        boolean showSystemThreads = preferences.getBoolean(SHOW_SYSTEM_THREADS, false);
        boolean showSuspendedThreadsOnly = preferences.getBoolean(SHOW_SUSPENDED_THREADS_ONLY, false);
        if (!showSystemThreads || showSuspendedThreadsOnly) {
            nodes = filterThreadsAndGroups(nodes, !showSystemThreads, showSuspendedThreadsOnly);
        }
        boolean alphabet = preferences.getBoolean(SORT_ALPHABET, true);
        if (!alphabet) {
            boolean suspend = preferences.getBoolean(SORT_SUSPEND, false);
            if (suspend) {
                Object[] newNodes = new Object[nodes.length];
                System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
                nodes = newNodes;
                Arrays.sort(nodes, new ThreadSuspendComparator());
            }
        } else {
            Object[] newNodes = new Object[nodes.length];
            System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
            nodes = newNodes;
            Arrays.sort(nodes, new ThreadAlphabetComparator());
        }
        return nodes;
    }
    
    private Object[] filterThreadsAndGroups(Object[] nodes, boolean filterSystem, boolean filterRunning) {
        List list = null;
        JPDAThread currentThread = debugger.getCurrentThread();
        for (Object node : nodes) {
            if (node instanceof JPDADVThread) {
                JPDAThread t = ((JPDADVThread) node).getKey();
                watchState(t);
                if (!t.isSuspended() && (filterSystem && isSystem(t) ||
                        (filterRunning && t != currentThread))) {
                    if (list == null) {
                        list = new ArrayList(Arrays.asList(nodes));
                    }
                    list.remove(node);
                } // if
            } else if (filterRunning && node instanceof JPDADVThreadGroup) {
                if (!containsThread((JPDADVThreadGroup) node, dvSupport.get(currentThread))) {
                    if (list == null) {
                        list = new ArrayList(Arrays.asList(nodes));
                    }
                    list.remove(node);
                }
            }
        } // for
        return (list != null) ? list.toArray() : nodes;
    }

    private boolean containsThread(DVThreadGroup group, DVThread currentThread) {
        DVThread[] threads = group.getThreads();
        for (int x = 0; x < threads.length; x++) {
            if (threads[x].isSuspended() || threads[x] == currentThread) {
                return true;
            }
        }
        DVThreadGroup[] groups = group.getThreadGroups();
        for (int x = 0; x < groups.length; x++) {
            if (containsThread(groups[x], currentThread)) {
                return true;
            }
        }
        return false;
    }

    private Object[] getTopLevelThreadsAndGroups() {
        List<Object> result = new LinkedList<>();
        Set<JPDADVThreadGroup> groups = new HashSet<>();

        for (JPDAThread thread : debugger.getThreadsCollector().getAllThreads()) {
            JPDAThreadGroup group = thread.getParentThreadGroup();
            if (group == null) {
                result.add(dvSupport.get(thread));
            } else {
                while (group.getParentThreadGroup() != null) {
                    group = group.getParentThreadGroup();
                } // while
                groups.add(dvSupport.get(group));
            } // if
        } // for
        result.addAll(groups);
        return result.toArray();
    }
    
    private boolean isSystem(JPDAThread t) {
        if (SYSTEM_THREAD_NAMES.contains(t.getName())) {
            JPDAThreadGroup g = t.getParentThreadGroup();
            return (g != null && "system".equals(g.getName()));
        }
        if (SYSTEM_MAIN_THREAD_NAMES.contains(t.getName())) {
            JPDAThreadGroup g = t.getParentThreadGroup();
            return (g != null && "main".equals(g.getName()));
        }
        return false;
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return monitorChildrenFilter.getChildrenCount(childrenModelImpl, node);
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        return monitorChildrenFilter.isLeaf(childrenModelImpl, node);
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null) {
                listener = new Listener (this, debugger);
            }
        }
    }

    public void removeModelListener (ModelListener l) {
        boolean destroyListeners = false;
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0 && listener != null) {
                listener.destroy ();
                listener = null;
                destroyListeners = true;
            }
        }
        if (destroyListeners) {
            destroyThreadStateListeners();
            clearCache();
        }
    }

    private void destroyThreadStateListeners() {
        synchronized (threadStateListeners) {
            for (Map.Entry<JPDAThread, ThreadStateListener> entry : threadStateListeners.entrySet()) {
                PropertyChangeListener pcl = entry.getValue().getThreadPropertyChangeListener();
                ((Customizer) entry.getKey()).removePropertyChangeListener(pcl);
            }
            threadStateListeners.clear();
        }
    }

    private void fireModelChange(ModelEvent me) {
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged(me);
        }
    }

    public void fireNodeChanged (Object node) {
        //System.err.println("FIRE node changed ("+node+")");
        //Thread.dumpStack();
        try {
            recomputeChildren();
        } catch (UnknownTypeException ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        fireModelChange(ev);
    }

    private void fireNodeChildrenChanged (Object node) {
        //System.err.println("FIRE node children changed ("+node+")");
        //Thread.dumpStack();
        try {
            recomputeChildren();
        } catch (UnknownTypeException ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.CHILDREN_MASK);
        fireModelChange(ev);
    }


    /**
     * Listens on JPDADebugger state property and updates all threads hierarchy.
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        //private ThreadsCache tc;
        private WeakReference<DebuggingTreeModel> model;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private Set<Object> nodesToRefresh;
        private Preferences preferences = DebuggingViewSupportImpl.getFilterPreferences();
        
        public Listener (
            DebuggingTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            //this.tc = debugger.getThreadsCache();
            model = new WeakReference<DebuggingTreeModel>(tm);
            debugger.addPropertyChangeListener(this);
            //tc.addPropertyChangeListener(this);
        }

        private DebuggingTreeModel getModel () {
            DebuggingTreeModel tm = model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            synchronized (this) {
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    task = null;
                }
            }
        }
        
        private RequestProcessor.Task createTask() {
            RequestProcessor rp = null;
            try {
                Session s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
                rp = s.lookupFirst(null, RequestProcessor.class);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            if (rp == null) {
                rp = RequestProcessor.getDefault();
            }
            RequestProcessor.Task task = rp.create(new RefreshTree());
            return task;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            //System.err.println("ThreadsTreeModel.propertyChange("+e+")");
            //System.err.println("    "+e.getPropertyName()+", "+e.getOldValue()+" => "+e.getNewValue());
            boolean showThreadGroups = preferences.getBoolean(SHOW_THREAD_GROUPS, false);
            JPDAThreadGroup tg = null;
            if (e.getPropertyName() == JPDADebugger.PROP_THREAD_STARTED) {
                JPDAThread t = (JPDAThread) e.getNewValue();
                if (showThreadGroups) {
                    tg = t.getParentThreadGroup();
                }
            } else if (e.getPropertyName() == JPDADebugger.PROP_THREAD_DIED) {
                JPDAThread t = (JPDAThread) e.getOldValue();
                if (showThreadGroups) {
                    tg = t.getParentThreadGroup();
                    /*while (tg != null && tg.getThreads().length == 0 && tg.getThreadGroups().length == 0) {
                        tg = tg.getParentThreadGroup();
                    }*/
                }
            } else if (e.getPropertyName() == JPDADebugger.PROP_THREAD_GROUP_ADDED) {
                if (showThreadGroups) {
                    tg = (JPDAThreadGroup) e.getNewValue();
                    tg = tg.getParentThreadGroup();
                }
            } else {
                return ;
            }
            List<Object> nodes = new ArrayList<>();
            DebuggingTreeModel tm = getModel();
            if (tg == null || !showThreadGroups) {
                nodes.add(ROOT);
            } else if (tg != null && tm != null) {
                do {
                    nodes.add(0, tm.dvSupport.get(tg));
                    tg = tg.getParentThreadGroup();
                } while (tg != null);
                if (showThreadGroups) {
                    nodes.add(0, ROOT);
                }
            }
            synchronized (this) {
                if (task == null) {
                    task = createTask();
                }
                if (nodesToRefresh == null) {
                    nodesToRefresh = new LinkedHashSet<>();
                }
                nodesToRefresh.addAll(nodes);
                task.schedule(100);
            }
        }
        
        private class RefreshTree implements Runnable {
            public RefreshTree () {}
            
            public void run() {
                DebuggingTreeModel tm = getModel ();
                if (tm == null) return;
                List nodes;
                synchronized (Listener.this) {
                    nodes = new ArrayList(nodesToRefresh);
                    nodesToRefresh.clear();
                }
                for (Object node : nodes) {
                    tm.fireNodeChildrenChanged(node);
                }
            }
        }
    }

    
    private void fireThreadStateChanged (JPDAThread node) {
        JPDADVThread dvnode = dvSupport.get(node);
        boolean showThreadGroups = preferences.getBoolean(SHOW_THREAD_GROUPS, false);
        if (preferences.getBoolean(SHOW_SUSPENDED_THREADS_ONLY, false)) {
            Object parent = null;
            if (showThreadGroups) {
                parent = dvSupport.get(node.getParentThreadGroup());
            }
            if (parent == null) parent = ROOT;
            fireNodeChildrenChanged(parent);
        } else if (!preferences.getBoolean(SHOW_SYSTEM_THREADS, false)
                   && isSystem(node)) {

            Object parent = null;
            if (showThreadGroups) {
                parent = dvSupport.get(node.getParentThreadGroup());
            }
            if (parent == null) parent = ROOT;
            fireNodeChildrenChanged(parent);
        }
        try {
            recomputeChildren(dvnode);
        } catch (UnknownTypeException ex) {
            refreshCache(dvnode);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, dvnode,
                ModelEvent.NodeChanged.CHILDREN_MASK |
                ModelEvent.NodeChanged.EXPANSION_MASK);
        fireModelChange(event);
    }
    
    private void watchState(JPDAThread t) {
        synchronized (threadStateListeners) {
            if (!threadStateListeners.containsKey(t)) {
                threadStateListeners.put(t, new ThreadStateListener(t));
            }
        }
    }
    
    private class ThreadStateListener implements PropertyChangeListener {
        
        private Reference<JPDAThread> tr;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private final PropertyChangeListener propertyChangeListener;
        private boolean wasMethodInvoke = false;
        
        public ThreadStateListener(JPDAThread t) {
            this.tr = new WeakReference(t);
            this.propertyChangeListener = WeakListeners.propertyChange(this, t);
            ((Customizer) t).addPropertyChangeListener(propertyChangeListener);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(JPDAThread.PROP_SUSPENDED)) return ;
            JPDAThread t = tr.get();
            if (t == null) return ;
            // Refresh the children of the thread (stack frames) when the thread
            // gets suspended or is resumed
            // When thread is resumed because of a method invocation, do the
            // refresh only if the method takes a long time.
            boolean isMethodInvoking = "methodInvoke".equals(evt.getPropagationId());   // NOI18N
            boolean suspended = t.isSuspended();
            if (suspended || !isMethodInvoking) {
                synchronized (this) {
                    if (task == null) {
                        task = RP.create(new Refresher());
                    }
                    int delay;
                    if (!suspended || wasMethodInvoke) {
                        delay = 1000;
                    } else {
                        delay = 200;
                    }
                    //Logger.getLogger("DEBUGGING").severe("isMethodInvoking = "+isMethodInvoking+", suspended = "+suspended+", wasMethodInvoke = "+wasMethodInvoke+" => delay = "+delay);
                    task.schedule(delay);
                }
            }
            wasMethodInvoke = isMethodInvoking;
        }

        PropertyChangeListener getThreadPropertyChangeListener() {
            return propertyChangeListener;
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                JPDAThread t = tr.get();
                if (t != null) {
                    fireThreadStateChanged(t);
                }
            }
        }
    }

    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    @ColumnModelRegistration(path="netbeans-JPDASession/DebuggingView", position=100)
    public static class DefaultDebuggingColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultDebuggingColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).
                getString ("CTL_Debugging_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        @Override
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString
                ("CTL_Debugging_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    @ColumnModelRegistration(path="netbeans-JPDASession/DebuggingView", position=200)
    public static class DebuggingSuspendColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "suspend";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Name");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns 
         * <code>null</code> - do not use tooltip.
         *
         * @return  tooltip for given node or <code>null</code>
         */
        @Override
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation 
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible () {
            return true;
        }
    }
    
    private static final class ThreadAlphabetComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof JPDADVThreadGroup) {
                if (o2 instanceof JPDADVThreadGroup) {
                    String tgn1 = ((JPDADVThreadGroup) o1).getName();
                    String tgn2 = ((JPDADVThreadGroup) o2).getName();
                    return java.text.Collator.getInstance().compare(tgn1, tgn2);
                }
                return 1;
            } else if (o2 instanceof JPDADVThreadGroup) {
                return -1;
            }
            if (!(o1 instanceof JPDADVThread) && !(o2 instanceof JPDADVThread)) {
                return 0;
            }
            String n1 = ((JPDADVThread) o1).getName();
            String n2 = ((JPDADVThread) o2).getName();
            return java.text.Collator.getInstance().compare(n1, n2);
        }
        
    }

    private static final class ThreadSuspendComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof JPDADVThreadGroup) {
                if (o2 instanceof JPDADVThreadGroup) {
                    return 0;
                }
                return 1;
            } else if (o2 instanceof JPDADVThreadGroup) {
                return -1;
            }
            if (!(o1 instanceof JPDADVThread) && !(o2 instanceof JPDADVThread)) {
                return 0;
            }
            boolean s1 = ((JPDADVThread) o1).isSuspended();
            boolean s2 = ((JPDADVThread) o2).isSuspended();
            if (s1 && !s2) return -1;
            if (!s1 && s2) return +1;
            return 0;
        }
        
    }
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (SORT_ALPHABET.equals(key) || SORT_SUSPEND.equals(key) ||
                    SHOW_SYSTEM_THREADS.equals(key) || SHOW_THREAD_GROUPS.equals(key) ||
                    SHOW_SUSPENDED_THREADS_ONLY.equals(key) ||
                    DebuggingNodeModel.SHOW_PACKAGE_NAMES.equals(key)) {
                // We have to catch the Throwables, so that the AbstractPreferences.EventDispatchThread
                // is not killed. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6467096
                try {
                    fireNodeChanged(ROOT);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        }

    }

    private final class DebuggerFinishListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    if (prefListener != null) {
                        try {
                            preferences.removePreferenceChangeListener(prefListener);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                }
            }
        }
        
    }
    
}

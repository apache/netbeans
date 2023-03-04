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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;

import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ThreadGroupReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMOutOfMemoryExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.ThreadsCache;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ThreadsView", types=TreeModel.class)
public class ThreadsTreeModel implements TreeModel {

    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('t') >= 0);
    
    private JPDADebuggerImpl    debugger;
    private Map<Object, ChildrenTree> childrenCache = new WeakHashMap<Object, ChildrenTree>();
    private Listener            listener;
    private Collection<ModelListener> listeners = new HashSet<ModelListener>();
    
    
    public ThreadsTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }

    public Object getRoot () {
        return ROOT;
    }
    
    public Object[] getChildren (Object o, int from, int to)
    throws UnknownTypeException {
        Object[] ch;
        synchronized (childrenCache) {
            //ch = (List) childrenCache.get(o);
            ChildrenTree cht = childrenCache.get(o);
            if (cht != null) {
                ch = cht.getChildren();
            } else {
                ch = null;
            }
        }
        if (ch == null) {
            ch = computeChildren(o);
            if (ch == null) {
                throw new UnknownTypeException (o);
            } else {
                synchronized (childrenCache) {
                    ChildrenTree cht = new ChildrenTree();
                    cht.setChildren(ch);
                    childrenCache.put(o, cht);
                }
            }
        }
        int l = ch.length;
        from = Math.min(l, from);
        to = Math.min(l, to);
        if (from == 0 && to == l) {
            return ch;
        } else {
            Object[] ch1 = new Object[to - from];
            System.arraycopy(ch, from, ch1, 0, to - from);
            ch = ch1;
        }
        return ch;
    }
    
    private Object[] computeChildren(Object node) {
        if (node.equals (ROOT)) {
            
            if (verbose) {
                com.sun.jdi.VirtualMachine vm = debugger.getVirtualMachine();
                if (vm == null) {
                    System.err.println("\nThreadsTreeModel.computeChildren():\nVM is null!\n");
                } else {
                    try {
                        List<ThreadReference> threads = VirtualMachineWrapper.allThreads(vm);
                        System.err.println("\nThreadsTreeModel.computeChildren() ALL Threads:");
                        for (ThreadReference t : threads) {
                            System.err.println("  "+ThreadReferenceWrapper.name(t)+" is suspended: "+ThreadReferenceWrapper.isSuspended(t)+", suspend count = "+ThreadReferenceWrapper.suspendCount(t));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.err.println("");
                }
            }
            
            return debugger.getTopLevelThreadGroups();
        } else if (node instanceof JPDAThreadGroup) {
            JPDAThreadGroup tg = (JPDAThreadGroup) node;
            JPDAThreadGroup[] tgs = tg.getThreadGroups();
            JPDAThread[] ts = tg.getThreads();
            int n = tgs.length + ts.length;
            Object[] ch = new Object[n];
            System.arraycopy(tgs, 0, ch, 0, tgs.length);
            System.arraycopy(ts, 0, ch, tgs.length, ts.length);
            return ch;
        } else {
            return new Object[0];
        }
    }
    
    private void recomputeChildren() {
        synchronized (childrenCache) {
            recomputeChildren(getRoot());
        }
    }
    
    private void recomputeChildren(Object node) {
        ChildrenTree cht = childrenCache.get(node);
        if (cht != null) {
            Set keys = childrenCache.keySet();
            Object[] newCh = computeChildren(node);
            cht.setChildren(newCh);
            for (int i = 0; i < newCh.length; i++) {
                if (keys.contains(newCh[i])) {
                    recomputeChildren(newCh[i]);
                }
            }
        }
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        // Performance, see issue #59058.
        return Integer.MAX_VALUE;
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o instanceof JPDAThread) return true;
        if (o instanceof JPDAThreadGroup) return false;
        if (o == ROOT) return false;
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null) {
                listener = new Listener (this, debugger);
            }
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0) {
                listener.destroy ();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        recomputeChildren();
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }

    public void fireNodeChanged (Object node) {
        recomputeChildren();
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }

    /**
     * Listens on JPDADebugger state property and updates all threads hierarchy.
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebuggerImpl debugger;
        private ThreadsCache tc;
        private WeakReference<ThreadsTreeModel> model;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private Set<Object> nodesToRefresh;
        
        public Listener (
            ThreadsTreeModel tm,
            JPDADebuggerImpl debugger
        ) {
            this.debugger = debugger;
            this.tc = debugger.getThreadsCache();
            model = new WeakReference<ThreadsTreeModel>(tm);
            tc.addPropertyChangeListener(this);
        }
        
        private ThreadsTreeModel getModel () {
            ThreadsTreeModel tm = model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        void destroy () {
            tc.removePropertyChangeListener (this);
            synchronized (this) {
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("TTM cancel old task " + task);
                    task = null;
                }
            }
        }
        
        private RequestProcessor.Task createTask() {
            RequestProcessor.Task task =
                debugger.getRequestProcessor().create(
                                new RefreshTree());
            if (verbose)
                System.out.println("TTM  create task " + task);
            return task;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            //System.err.println("ThreadsTreeModel.propertyChange("+e+")");
            //System.err.println("    "+e.getPropertyName()+", "+e.getOldValue()+" => "+e.getNewValue());
            ThreadGroupReference tg;
            if (e.getPropertyName() == ThreadsCache.PROP_THREAD_STARTED) {
                ThreadReference t = (ThreadReference) e.getNewValue();
                try {
                    tg = ThreadReferenceWrapper.threadGroup(t);
                } catch (InternalExceptionWrapper ex) {
                    tg = null;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                } catch (VMOutOfMemoryExceptionWrapper ex) {
                    return ;
                } catch (ObjectCollectedExceptionWrapper ex) {
                    return ;
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    tg = null;
                }
            } else if (e.getPropertyName() == ThreadsCache.PROP_THREAD_DIED) {
                ThreadReference t = (ThreadReference) e.getOldValue();
                try {
                    tg = ThreadReferenceWrapper.threadGroup(t);
                } catch (InternalExceptionWrapper ex) {
                    tg = null;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                } catch (VMOutOfMemoryExceptionWrapper ex) {
                    tg = null;
                } catch (ObjectCollectedExceptionWrapper ex) {
                    tg = null;
                } catch (IllegalThreadStateExceptionWrapper ex) {
                    tg = null;
                }
            } else if (e.getPropertyName() == ThreadsCache.PROP_GROUP_ADDED) {
                tg = (ThreadGroupReference) e.getNewValue();
                try {
                    tg = ThreadGroupReferenceWrapper.parent(tg);
                } catch (InternalExceptionWrapper ex) {
                    tg = null;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    tg = null;
                } catch (ObjectCollectedExceptionWrapper ex) {
                    tg = null;
                }
            } else {
                return ;
            }
            Object node;
            if (tg == null) {
                node = ROOT;
            } else {
                node = debugger.getThreadGroup(tg);
            }
            synchronized (this) {
                if (task == null) {
                    task = createTask();
                }
                if (nodesToRefresh == null) {
                    nodesToRefresh = new LinkedHashSet<Object>();
                }
                nodesToRefresh.add(node);
                task.schedule(100);
            }
        }
        
        private class RefreshTree implements Runnable {
            public RefreshTree () {}
            
            public void run() {
                ThreadsTreeModel tm = getModel ();
                if (tm == null) return;
                if (verbose)
                    System.out.println("TTM do R task " + task);
                List nodes;
                synchronized (Listener.this) {
                    nodes = new ArrayList(nodesToRefresh);
                    nodesToRefresh.clear();
                }
                for (Object node : nodes) {
                    tm.fireNodeChanged(node);
                }
            }
        }
    }
    
    private static class ChildrenTree {
        
        private Object[] ch;
        
        public ChildrenTree() {
        }
        
        public void setChildren(Object[] ch) {
            this.ch = ch;
        }
        
        public Object[] getChildren() {
            return ch;
        }
        
    }
    
}


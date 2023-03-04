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

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ThreadsView", types=NodeModel.class)
public class ThreadsNodeModel implements NodeModel {
    
    public static final String CURRENT_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread"; // NOI18N
    public static final String RUNNING_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N
    public static final String SUSPENDED_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread"; // NOI18N
    public static final String THREAD_GROUP =
        "org/netbeans/modules/debugger/resources/threadsView/ThreadGroup"; // NOI18N
    public static final String CURRENT_THREAD_GROUP =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThreadGroup"; // NOI18N
    
    
    private JPDADebugger debugger;
    private Session session;
    private Vector listeners = new Vector ();
    private Set<Object> currentNodes = new HashSet<Object>();
    
    
    public ThreadsNodeModel (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        new Listener (this, debugger, currentNodes);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle (ThreadsNodeModel.class).getString
                ("CTL_ThreadsModel_Column_Name_Name");
        } else
        if (o instanceof JPDAThread) {
            if (debugger.getCurrentThread () == o) {
                synchronized(currentNodes) {
                    currentNodes.add(o);
                }
                return BoldVariablesTableModelFilter.toHTML (
                    ((JPDAThread) o).getName (),
                    true,
                    false,
                    null
                );
            } else {
                synchronized(currentNodes) {
                    currentNodes.remove(o);
                }
                return ((JPDAThread) o).getName ();
            }
        } else
        if (o instanceof JPDAThreadGroup) {
            if (isCurrent ((JPDAThreadGroup) o)) {
                synchronized(currentNodes) {
                    currentNodes.add(o);
                }
                return BoldVariablesTableModelFilter.toHTML (
                    ((JPDAThreadGroup) o).getName (),
                    true,
                    false,
                    null
                );
            } else {
                synchronized(currentNodes) {
                    currentNodes.remove(o);
                }
                return ((JPDAThreadGroup) o).getName ();
            }
        } else 
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle (ThreadsNodeModel.class).getString
                ("CTL_ThreadsModel_Column_Name_Desc");
        } else
        if (o instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) o;
            int i = t.getState ();
            String s = "";
            switch (i) {
                case JPDAThread.STATE_UNKNOWN:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Unknown");
                    break;
                case JPDAThread.STATE_MONITOR:
                    ObjectVariable ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (ThreadsNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Monitor");
                    else
                        try {
                            s = java.text.MessageFormat.
                                format (
                                    NbBundle.getBundle (ThreadsNodeModel.class).
                                        getString (
                                    "CTL_ThreadsModel_State_ConcreteMonitor"), 
                                    new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_NOT_STARTED:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_NotStarted");
                    break;
                case JPDAThread.STATE_RUNNING:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Running");
                    break;
                case JPDAThread.STATE_SLEEPING:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Sleeping");
                    break;
                case JPDAThread.STATE_WAIT:
                    ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (ThreadsNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Waiting");
                    else
                        try {
                            s = java.text.MessageFormat.format
                                (NbBundle.getBundle (ThreadsNodeModel.class).
                                getString ("CTL_ThreadsModel_State_WaitingOn"), 
                                new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_ZOMBIE:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Zombie");
                    break;
            }
            if (t.isSuspended () && (t.getStackDepth () > 0)) {
                try { 
                    CallStackFrame sf = t.getCallStack (0, 1) [0];
                    s += " " + CallStackNodeModel.getCSFName (session, sf, true);
                } catch (AbsentInformationException e) {
                }
            }
            return s;
        } else
        if (o instanceof JPDAThreadGroup) {
            return ((JPDAThreadGroup) o).getName ();
        } else 
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof String) {
            return THREAD_GROUP;
        } else
        if (o instanceof JPDAThread) {
            if (o == debugger.getCurrentThread ())
                return CURRENT_THREAD;
            return ((JPDAThread) o).isSuspended () ? 
                SUSPENDED_THREAD : RUNNING_THREAD;
            
        } else
        if (o instanceof JPDAThreadGroup) {
            if (isCurrent ((JPDAThreadGroup) o))
                return CURRENT_THREAD_GROUP;
            else
                return THREAD_GROUP;
        } else 
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (null);
    }
    
    private void fireNodeChanged (Object node) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
                ModelEvent.NodeChanged.ICON_MASK |
                ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (event);
    }
    
    private boolean isCurrent (JPDAThreadGroup tg) {
        JPDAThread t = debugger.getCurrentThread ();
        if (t == null)
            return false;
        JPDAThreadGroup ctg = t.getParentThreadGroup ();
        while (ctg != null) {
            if (ctg == tg) return true;
            ctg = ctg.getParentThreadGroup ();
        }
        return false;
    }

    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private JPDADebugger debugger;
        Set<Object> currentNodes;
        
        private Listener (
            ThreadsNodeModel rm,
            JPDADebugger debugger,
            Set<Object> currentNodes
        ) {
            ref = new WeakReference (rm);
            this.debugger = debugger;
            this.currentNodes = currentNodes;
            debugger.addPropertyChangeListener (
                debugger.PROP_CURRENT_THREAD,
                this
            );
        }
        
        private ThreadsNodeModel getModel () {
            ThreadsNodeModel rm = (ThreadsNodeModel) ref.get ();
            if (rm == null) {
                debugger.removePropertyChangeListener (
                    debugger.PROP_CURRENT_THREAD,
                    this
                );
            }
            return rm;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            ThreadsNodeModel rm = getModel ();
            if (rm == null) return;
            Set nodes;
            synchronized(currentNodes) {
                nodes = new HashSet();
                for (Object obj : currentNodes) {
                    nodes.add(obj);
                    if (obj instanceof JPDAThread) {
                        nodes.add(new MonitorModel.ThreadWithBordel((JPDAThread)obj)); // fix for #136921
                    }
                }
            }
            JPDAThread t = debugger.getCurrentThread();
            if (t != null) {
                nodes.add(t);
                nodes.add(new MonitorModel.ThreadWithBordel(t)); // fix for #136921
                JPDAThreadGroup tg = t.getParentThreadGroup();
                while (tg != null) {
                    nodes.add(tg);
                    tg = tg.getParentThreadGroup();
                }
            }
            for (Object node : nodes) {
                rm.fireNodeChanged (node);
            }
        }
    }
}

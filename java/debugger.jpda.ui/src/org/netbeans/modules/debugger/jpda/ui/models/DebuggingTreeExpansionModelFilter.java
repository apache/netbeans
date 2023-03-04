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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThreadGroup;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakSet;


/**
 *
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=TreeExpansionModelFilter.class,
                             position=12000)
public class DebuggingTreeExpansionModelFilter implements TreeExpansionModelFilter {
    
    private static final Map<JPDADebugger, DebuggingTreeExpansionModelFilter> FILTERS = new WeakHashMap<JPDADebugger, DebuggingTreeExpansionModelFilter>();
    
    private final Set<Object> expandedNodes = new WeakSet<Object>();
    private final Set<Object> expandedExplicitly = new WeakSet<Object>();
    private final Set<Object> collapsedExplicitly = new WeakSet<Object>();
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private final Reference<JPDADebugger> debuggerRef;
    
    
    public DebuggingTreeExpansionModelFilter(ContextProvider lookupProvider) {
        JPDADebugger debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        this.debuggerRef = new WeakReference(debugger);
        synchronized (FILTERS) {
            FILTERS.put(debugger, this);
        }
    }
    
    static boolean isExpanded(JPDADebugger debugger, Object node) {
        DebuggingTreeExpansionModelFilter filter;
        synchronized (FILTERS) {
            filter = FILTERS.get(debugger);
        }
        if (filter == null) return false;
        return filter.isExpanded(node);
    }
    
    static void expand(JPDADebugger debugger, Object node) {
        DebuggingTreeExpansionModelFilter filter;
        synchronized (FILTERS) {
            filter = FILTERS.get(debugger);
        }
        if (filter != null) {
            filter.expand(node);
        }
    }
    
    private void expand(Object node) {
        synchronized (this) {
            if (collapsedExplicitly.contains(node)) {
                return ; // Ignore manually collapsed nodes.
            }
            expandedExplicitly.add(node);
        }
        //nodeExpanded(node);
        fireNodeExpanded(node);
        
    }

    private boolean isExpanded(Object node) {
        synchronized (this) {
            return expandedNodes.contains(node) ||
                   (expandedExplicitly.contains(node) && !collapsedExplicitly.contains(node));
        }
    }

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    public boolean isExpanded (TreeExpansionModel original, Object node) throws UnknownTypeException {
        JPDADebugger debugger = debuggerRef.get();
        if (debugger == null) return false;
        Set nodesInDeadlock = DebuggingNodeModel.getNodesInDeadlock(debugger);
        if (nodesInDeadlock != null) {
            synchronized (nodesInDeadlock) {
                if (nodesInDeadlock.contains(node)) {
                    return true;
                }
            }
        }
        synchronized (this) {
            if (expandedExplicitly.contains(node)) {
                return true;
            }
            if (collapsedExplicitly.contains(node)) {
                return false;
            }
        }
        if (node instanceof JPDADVThreadGroup) {
            return true;
        }
        if (node instanceof JPDADVThread) {
            if (((JPDADVThread) node).getCurrentBreakpoint() != null) {
                return true;
            }
            JPDAThread currentThread = debugger.getCurrentThread();
            if (currentThread != null) {
                JPDAThread thread = ((JPDADVThread) node).get();
                if (currentThread == thread && thread.isSuspended()) {
                    return true;
                }
            }
        }
        if (node instanceof CallStackFrame) {
            return true;
        }
        if (node instanceof DebuggingMonitorModel.OwnedMonitors) {
            return ((DebuggingMonitorModel.OwnedMonitors) node).monitors != null;
        }
        return original.isExpanded(node);
    }

    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public void nodeExpanded (Object node) {
        synchronized (this) {
            expandedNodes.add(node);
            collapsedExplicitly.remove(node);
        }
        if (node instanceof JPDADVThread || node instanceof JPDADVThreadGroup) {
            fireNodeChanged(node);
        }
    }
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    public void nodeCollapsed (Object node) {
        synchronized (this) {
            expandedNodes.remove(node);
            expandedExplicitly.remove(node);
            collapsedExplicitly.add(node);
        }
        if (node instanceof JPDADVThread || node instanceof JPDADVThreadGroup) {
            fireNodeChanged(node);
        }
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireNodeChanged (Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private void fireNodeExpanded(Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.EXPANSION_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
}

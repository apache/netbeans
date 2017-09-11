/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

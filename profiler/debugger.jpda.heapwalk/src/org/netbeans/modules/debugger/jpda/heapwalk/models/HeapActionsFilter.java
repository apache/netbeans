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

package org.netbeans.modules.debugger.jpda.heapwalk.models;

import org.netbeans.lib.profiler.heap.Instance;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.security.auth.Refreshable;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.InstanceImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.views.DebuggerHeapFragmentWalker;
import org.netbeans.modules.debugger.jpda.heapwalk.views.InstancesView;

import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Heap Walker actions on ObjectVariable
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=NodeActionsProviderFilter.class,
                                 position=1000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=NodeActionsProviderFilter.class,
                                 position=1000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=NodeActionsProviderFilter.class,
                                 position=1000)
})
public class HeapActionsFilter implements NodeActionsProviderFilter {
    
    private JPDADebugger debugger;
    private RequestProcessor rp;
    
    /** Creates a new instance of HeapActionsFilter */
    public HeapActionsFilter(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        rp = contextProvider.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            rp = new RequestProcessor(HeapActionsFilter.class);
        }
    }
    
    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        if (node instanceof ObjectVariable && debugger.canGetInstanceInfo()) {
            int index;
            for (index = 0; index < actions.length; index++) {
                if (actions[index] == null)
                    break;
            }
            Action[] newActions = new Action[actions.length + 1];
            System.arraycopy(actions, 0, newActions, 0, index);
            newActions[index] = HEAP_REFERENCES_ACTION;
            if (index < actions.length) {
                System.arraycopy(actions, index, newActions, index + 1, actions.length - index);
            }
            actions = newActions;
        }
        return actions;
    }

    private final Action HEAP_REFERENCES_ACTION = Models.createAction (
        NbBundle.getMessage(HeapActionsFilter.class, "CTL_References_Label"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                if ((!(node instanceof ObjectVariable))) {
                    return false;
                }
                ObjectVariable var = (ObjectVariable) node;
                if (var instanceof Refreshable) {
                    if (!((Refreshable) var).isCurrent()) {
                        return false;
                    }
                }
                return var.getUniqueID() != 0L;
            }
            @Override
            public void perform (Object[] nodes) {
                ObjectVariable var = (ObjectVariable) nodes[0];
                if (var.getUniqueID() == 0L) return ;
                final InstancesView instances = openInstances(true);
                final Reference<ObjectVariable> varRef = new WeakReference<ObjectVariable>(var);
                final Reference<JPDADebugger> debuggerRef = new WeakReference<JPDADebugger>(debugger);
                InstancesView.HeapFragmentWalkerProvider provider =
                        new InstancesView.HeapFragmentWalkerProvider() {
                    @Override
                    public synchronized HeapFragmentWalker getHeapFragmentWalker() {
                        HeapFragmentWalker hfw = instances.getCurrentFragmentWalker();
                        HeapImpl heap = (hfw != null) ? (HeapImpl) hfw.getHeapFragment() : null;
                        JPDADebugger debugger = debuggerRef.get();
                        if (heap == null || debugger != null && heap.getDebugger() != debugger) {
                            heap = new HeapImpl(debugger);
                            hfw = new DebuggerHeapFragmentWalker(heap);
                        }
                        final ObjectVariable var = varRef.get();
                        final HeapFragmentWalker fhfw = hfw;
                        if (var != null) {
                            final HeapImpl fheap = heap;
                            rp.post(new Runnable() {
                                @Override
                                public void run() {
                                    final Instance instance = InstanceImpl.createInstance(fheap, var);
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            fhfw.getInstancesController().showInstance(instance);
                                        }
                                    });
                                }
                            });
                            //Instance instance = InstanceImpl.createInstance(heap, var);
                            //hfw.getInstancesController().showInstance(instance);
                        }
                        return hfw;
                    }
                };
                instances.setHeapFragmentWalkerProvider(provider);
            }
            
            private InstancesView openInstances (boolean activate) {
                TopComponent view = WindowManager.getDefault().findTopComponent("dbgInstances");
                if (view == null) {
                    throw new IllegalArgumentException("dbgInstances");
                }
                view.open();
                if (activate) {
                    view.requestActive();
                }
                return (InstancesView) view;
            }
    
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
                if ((node == null) || (!(node instanceof ObjectVariable))) {
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

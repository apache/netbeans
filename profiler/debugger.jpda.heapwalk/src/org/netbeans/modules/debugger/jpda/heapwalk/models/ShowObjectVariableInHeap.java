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
package org.netbeans.modules.debugger.jpda.heapwalk.models;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.InstanceImpl;
import org.netbeans.modules.debugger.jpda.heapwalk.views.DebuggerHeapFragmentWalker;
import org.netbeans.modules.debugger.jpda.heapwalk.views.InstancesView;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

final class ShowObjectVariableInHeap {

    private final JPDADebugger debugger;
    private final RequestProcessor rp;

    ShowObjectVariableInHeap(JPDADebugger debugger, RequestProcessor rp) {
        this.debugger = debugger;
        this.rp = rp;
    }

    void showObjectVariable(ObjectVariable var) {
        final InstancesView instances = openInstances(true);
        final Reference<ObjectVariable> varRef = new WeakReference<ObjectVariable>(var);
        final Reference<JPDADebugger> debuggerRef = new WeakReference<JPDADebugger>(debugger);
        InstancesView.HeapFragmentWalkerProvider provider
                = new InstancesView.HeapFragmentWalkerProvider() {
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

    private InstancesView openInstances(boolean activate) {
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

}
